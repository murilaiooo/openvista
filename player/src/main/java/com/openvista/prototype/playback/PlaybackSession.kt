package com.openvista.prototype.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Display
import android.view.WindowManager
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Format
import androidx.media3.common.Tracks
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Log
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.openvista.prototype.R
import com.openvista.prototype.diagnostics.DiagnosticJournal
import com.openvista.prototype.library.Availability
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import com.openvista.prototype.library.LibraryStorageException
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.library.PlaybackCheckpoint
import java.util.Locale

/** One decoder owner shared by the app and system-selected dream entry points. */
@UnstableApi
class PlaybackSession(
    private val context: Context,
    private val store: LibraryStore,
    private val onStatus: (String) -> Unit,
    private val onItem: (LibraryItem?) -> Unit,
    private val onReleased: () -> Unit = {},
    private val dreamMode: Boolean = false,
) {
    val player: ExoPlayer
    private val trackSelector = DefaultTrackSelector(context)

    var currentItem: LibraryItem? = null
        private set
    var isFallback = false
        private set

    private val handler = Handler(Looper.getMainLooper())
    private var queue = emptyList<LibraryItem>()
    private var cameraQueue = emptyList<LibraryItem>()
    private var index = -1
    private var released = false
    private var failed = mutableSetOf<String>()
    private var playingItem: LibraryItem? = null
    private var hasRenderedFrame = false
    private var timeoutArmed = false
    private var restorePosition = 0L
    private var recoveryItem: LibraryItem? = null
    private var recoveryAttempts = 0
    private var recoveryCursor = 0
    private var recoveryAfter = 0L
    private var localResumeIndex = 0
    private var lastErrorCode: Int? = null
    private var generation = 0L
    private var storageFailed = false
    private var stayOnVideos = false
    private var desiredPlayWhenReady = true
    private var terminal = false
    private var lastSnapshot: PlaybackSnapshot? = null
    private var completedCurrent = false
    private val rejectedVideoFormats = mutableSetOf<Format>()
    private var rotationDueAt = 0L
    private var dreamMuted = true

    val isReleased get() = released

    private val checkpointTask = object : Runnable {
        override fun run() {
            if (released) return
            refreshSources()
            saveCheckpoint()
            handler.postDelayed(this, CHECKPOINT_INTERVAL_MS)
        }
    }
    private val rotationTask = Runnable {
        if (!released && currentItem?.family == MediaFamily.CAMERAS && player.isPlaying) next()
    }
    private val startupTimeout = Runnable {
        timeoutArmed = false
        if (!released && player.playWhenReady && playingItem != null &&
            (!hasRenderedFrame || player.playbackState == Player.STATE_BUFFERING)) failCurrent()
    }
    private val stablePlayback = Runnable {
        // A first frame alone is not evidence that a repeatedly stalling source recovered.
        if (!released && player.isPlaying && hasRenderedFrame) failed.clear()
    }

    init {
        check(Looper.myLooper() == Looper.getMainLooper())
        // Media3's default exception logs can contain a user URI. Keep only our numeric journal.
        Log.setLogLevel(Log.LOG_LEVEL_OFF)
        // Activity and DreamService can briefly overlap during window handoff.
        activeSession?.release()
        player = ExoPlayer.Builder(
            context,
            DefaultRenderersFactory(context).setEnableDecoderFallback(true),
        ).setTrackSelector(trackSelector).build()
        trackSelector.parameters = trackSelector.parameters.buildUpon()
            .setExceedRendererCapabilitiesIfNecessary(false)
            .setExceedVideoConstraintsIfNecessary(false)
            .build()
        activeSession = this
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (!acceptPlayerEvent()) return
                lastErrorCode = error.errorCode
                DiagnosticJournal.record(context, DiagnosticJournal.Event.PLAYBACK_ERROR, error.errorCode)
                failCurrent()
            }

            override fun onRenderedFirstFrame() {
                if (!acceptPlayerEvent()) return
                if (!selectCompatibleVideo(player.currentTracks, player.videoFormat)) return
                val item = playingItem ?: return
                hasRenderedFrame = true
                clearTimeout()
                handler.removeCallbacks(stablePlayback)
                handler.postDelayed(stablePlayback, STABLE_PLAYBACK_MS)
                storeWrite { store.updateAvailability(item.id, Availability.AVAILABLE) }
                if (recoveryItem != null) {
                    recoveryItem = null
                    queue = cameraQueue
                    index = queue.indexOfFirst { it.id == item.id }
                    isFallback = false
                    currentItem = item
                    storeWrite { store.setLastItem(item.id) }
                    onItem(item)
                }
                status(if (desiredPlayWhenReady) R.string.playback_playing else R.string.playback_paused)
                scheduleRotation()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (!acceptPlayerEvent()) return
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (restorePosition > 0 && playingItem?.family == MediaFamily.VIDEOS) {
                            val duration = player.duration
                            val target = if (duration > 0) {
                                if (restorePosition >= duration - COMPLETION_MARGIN_MS) 0L
                                else restorePosition.coerceAtMost((duration - 1).coerceAtLeast(0))
                            } else restorePosition
                            restorePosition = 0
                            if (player.isCurrentMediaItemSeekable) player.seekTo(target)
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        status(if (recoveryItem != null) R.string.playback_checking_camera else R.string.playback_connecting)
                        // Bound both initial connection and a later stalled stream.
                        if (player.playWhenReady) armTimeout()
                        handler.removeCallbacks(stablePlayback)
                    }
                    Player.STATE_ENDED -> {
                        if (playingItem == null) return
                        if (recoveryItem != null || !hasRenderedFrame) {
                            failCurrent()
                        } else if (playingItem?.family == MediaFamily.VIDEOS) {
                            failed.clear()
                            completedCurrent = true
                            saveCheckpoint(completed = true)
                            if (!tryRecoveryAtBoundary()) advance(1)
                        } else failCurrent()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!acceptPlayerEvent()) return
                if (isPlaying) {
                    // Resume after an ordinary rebuffer may not render a new first-frame event.
                    if (recoveryItem == null && hasRenderedFrame) {
                        clearTimeout()
                        handler.removeCallbacks(stablePlayback)
                        handler.postDelayed(stablePlayback, STABLE_PLAYBACK_MS)
                        scheduleRotation()
                        status(R.string.playback_playing)
                    }
                } else {
                    handler.removeCallbacks(rotationTask)
                    handler.removeCallbacks(stablePlayback)
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                if (acceptPlayerEvent()) selectCompatibleVideo(tracks)
            }
        })
        applySettings()
        handler.postDelayed(checkpointTask, CHECKPOINT_INTERVAL_MS)
    }

    fun play(items: List<LibraryItem>, selectedId: String? = null, shuffle: Boolean = false) {
        if (released) return
        saveCheckpoint()
        clearTimeout()
        recoveryItem = null
        isFallback = false
        stayOnVideos = false
        cameraQueue = emptyList()
        recoveryAttempts = 0
        recoveryCursor = 0
        failed.clear()
        terminal = false
        desiredPlayWhenReady = true
        // Cross-family lists are never allowed to silently mix camera and local behavior.
        val selected = selectedId?.let(store::item)
        val family = selected?.family ?: items.firstOrNull()?.family
        val candidates = items.mapNotNull { store.item(it.id) }.distinctBy { it.id }.filter { it.family == family }
        queue = if (shuffle) candidates.shuffled() else candidates
        index = queue.indexOfFirst { it.id == selected?.id }.coerceAtLeast(0)
        if (queue.isEmpty()) stopEmpty(R.string.playback_empty) else start(queue[index])
    }

    /** Preserve order and fallback lineage without keeping a decoder alive in the background. */
    fun snapshot(): PlaybackSnapshot {
        if (released) return lastSnapshot ?: PlaybackSnapshot(emptyList(), emptyList(), null, false, emptySet(), 0, 0, false)
        if (!released) saveCheckpoint()
        val probing = recoveryItem != null
        val selected = if (probing) queue.getOrNull(localResumeIndex)?.id else currentItem?.id
        val remaining = if (isFallback) {
            if (probing) recoveryDelay()
            else (recoveryAfter - SystemClock.elapsedRealtime()).coerceAtLeast(0)
        } else 0
        return PlaybackSnapshot(
            itemIds = queue.map { it.id },
            cameraIds = if (isFallback) cameraQueue.map { it.id } else emptyList(),
            selectedId = selected,
            fallback = isFallback,
            failedIds = failed.toSet(),
            recoveryAttempts = recoveryAttempts,
            recoveryDelayMs = remaining.coerceAtMost(RECOVERY_MAX_DELAY_MS),
            stayOnVideos = stayOnVideos,
            recoveryCursor = recoveryCursor,
            playWhenReady = desiredPlayWhenReady,
        )
    }

    /** A restore resolves every identifier through the current library and keeps failed items bounded. */
    fun restore(snapshot: PlaybackSnapshot) {
        if (released) return
        saveCheckpoint()
        generation++
        clearTimeout()
        handler.removeCallbacks(rotationTask)
        playingItem = null
        recoveryItem = null
        player.stop()
        terminal = false
        desiredPlayWhenReady = snapshot.playWhenReady
        val items = snapshot.itemIds.take(2_000).distinct().mapNotNull(store::item).distinctBy { it.id }
        val selected = snapshot.selectedId?.let(store::item)
        val family = if (snapshot.fallback) MediaFamily.VIDEOS else selected?.family ?: items.firstOrNull()?.family
        queue = items.filter { it.family == family }
        cameraQueue = if (snapshot.fallback) snapshot.cameraIds.take(2_000).distinct().mapNotNull(store::item)
            .filter { it.family == MediaFamily.CAMERAS }.distinctBy { it.id } else emptyList()
        isFallback = snapshot.fallback
        stayOnVideos = snapshot.stayOnVideos
        recoveryAttempts = snapshot.recoveryAttempts.coerceIn(0, MAX_RECOVERY_ATTEMPTS)
        recoveryCursor = if (cameraQueue.isEmpty()) 0 else snapshot.recoveryCursor.coerceAtLeast(0) % cameraQueue.size
        recoveryAfter = SystemClock.elapsedRealtime() + snapshot.recoveryDelayMs.coerceIn(0, RECOVERY_MAX_DELAY_MS)
        val ids = queue.map { it.id }.toSet()
        failed = snapshot.failedIds.mapNotNull { store.item(it)?.id }.filterTo(mutableSetOf()) { it in ids }
        if (queue.isEmpty()) {
            stopEmpty(R.string.playback_empty)
            return
        }
        val selectedIndex = queue.indexOfFirst { it.id == selected?.id }
        index = selectedIndex.coerceAtLeast(0)
        val candidate = (0 until queue.size).firstOrNull { offset -> queue[(index + offset) % queue.size].id !in failed }
        if (candidate == null) {
            if (!isFallback && family == MediaFamily.CAMERAS && beginFallback()) return
            stopEmpty(R.string.playback_all_unavailable)
            return
        }
        index = (index + candidate) % queue.size
        start(queue[index])
    }

    fun next() {
        if (released) return
        cancelRecovery()
        advance(1)
    }

    fun previous() {
        if (released) return
        cancelRecovery()
        advance(-1)
    }

    fun seekBy(deltaMs: Long) {
        if (released || recoveryItem != null || currentItem?.family != MediaFamily.VIDEOS ||
            !player.isCurrentMediaItemSeekable) return
        val duration = player.duration
        val limit = if (duration > 0) (duration - 1).coerceAtLeast(0) else Long.MAX_VALUE
        val target = (player.currentPosition.coerceAtLeast(0) + deltaMs).coerceIn(0, limit)
        player.seekTo(target)
        saveCheckpoint()
    }

    fun togglePause() {
        if (released || terminal || playingItem == null) return
        desiredPlayWhenReady = !desiredPlayWhenReady
        player.playWhenReady = desiredPlayWhenReady
        if (!player.playWhenReady) {
            handler.removeCallbacks(rotationTask)
            clearTimeout()
            saveCheckpoint()
            status(R.string.playback_paused)
        } else {
            scheduleRotation()
            if (!player.isPlaying) armTimeout()
            status(R.string.playback_playing)
        }
    }

    fun setMuted(muted: Boolean) {
        if (released) return
        if (dreamMode) dreamMuted = muted
        else storeWrite { store.saveSettings(store.settings().copy(muted = muted)) }
        player.volume = if (muted) 0f else 1f
    }

    fun applySettings() {
        if (released) return
        val settings = store.settings()
        player.volume = if (if (dreamMode) dreamMuted else settings.muted) 0f else 1f
        val tracks = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .clearVideoSizeConstraints()
        if (settings.maxVideoHeight > 0) {
            val width = if (settings.maxVideoHeight <= 1080) 1920 else 3840
            tracks.setMaxVideoSize(width, settings.maxVideoHeight.coerceAtMost(2160))
        }
        player.trackSelectionParameters = tracks.build()
        rotationDueAt = 0
        scheduleRotation()
    }

    /** Explicit retry starts a fresh finite pass; it never runs a background retry loop. */
    fun retry() {
        if (released) return
        val retryItems = if (isFallback && cameraQueue.isNotEmpty()) cameraQueue else queue
        if (retryItems.isEmpty()) {
            status(R.string.playback_empty)
            return
        }
        play(retryItems, if (isFallback) retryItems.firstOrNull()?.id else currentItem?.id)
    }

    fun stayLocal() {
        if (released) return
        stayOnVideos = true
        storeWrite { store.saveSettings(store.settings().copy(stayLocal = true)) }
        val wasRecovering = recoveryItem != null
        cancelRecovery()
        if (wasRecovering && queue.isNotEmpty()) {
            index = localResumeIndex.coerceIn(queue.indices)
            queue.getOrNull(index)?.let { store.item(it.id) }?.let { start(it) } ?: advance(1)
        }
        status(R.string.playback_staying_local)
    }

    fun release() {
        if (released) return
        lastSnapshot = snapshot()
        resumeSnapshot = lastSnapshot
        released = true
        handler.removeCallbacksAndMessages(null)
        player.release()
        if (activeSession === this) activeSession = null
        onReleased()
    }

    /** Deliberately excludes source URLs, file paths, titles and platform error messages. */
    fun diagnostics(): String {
        if (released) return context.getString(R.string.playback_diagnostics_released)
        val format = player.videoFormat
        val codec = format?.sampleMimeType ?: context.getString(R.string.playback_unknown)
        val resolution = if (format != null && format.width > 0 && format.height > 0)
            "${format.width} × ${format.height}" else context.getString(R.string.playback_unknown)
        return context.getString(
            R.string.playback_diagnostics, resolution, codec,
            player.bufferedPercentage, lastErrorCode?.toString() ?: "—",
        )
    }

    private fun start(item: LibraryItem, recovery: Boolean = false) {
        if (released) return
        terminal = false
        val requestGeneration = ++generation
        handler.removeCallbacks(rotationTask)
        handler.removeCallbacks(stablePlayback)
        clearTimeout()
        playingItem = item
        rotationDueAt = 0
        completedCurrent = false
        rejectedVideoFormats.clear()
        hasRenderedFrame = false
        restorePosition = if (item.family == MediaFamily.VIDEOS)
            store.checkpoint(item.id)?.positionMs?.coerceAtLeast(0) ?: 0 else 0
        if (!recovery) {
            currentItem = item
            storeWrite { store.setLastItem(item.id) }
            onItem(item)
        }
        try {
            // A previous source's language/SDR override must not constrain the next source.
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO).clearOverridesOfType(C.TRACK_TYPE_AUDIO).build()
            val media = MediaItem.Builder().setMediaId(item.id).setUri(item.uri)
            if (item.family == MediaFamily.CAMERAS) {
                media.setMimeType(when (item.streamKind?.uppercase(Locale.ROOT)) {
                    "DASH" -> MimeTypes.APPLICATION_MPD
                    else -> MimeTypes.APPLICATION_M3U8
                })
            }
            player.setMediaItem(media.build())
            player.prepare()
            player.playWhenReady = desiredPlayWhenReady
            if (desiredPlayWhenReady) armTimeout()
        } catch (_: RuntimeException) {
            // Defer to avoid recursion when every URI in a large list fails immediately.
            handler.post { if (!released && generation == requestGeneration && playingItem?.id == item.id) failCurrent() }
        }
    }

    private fun advance(direction: Int) {
        if (released || queue.isEmpty()) return
        saveCheckpoint()
        val next = (1..queue.size).asSequence().map { offset ->
            Math.floorMod(index + direction * offset, queue.size)
        }.mapNotNull { store.item(queue[it].id) }.firstOrNull { it.id !in failed }
        queue = queue.mapNotNull { store.item(it.id) }.distinctBy { it.id }
        if (queue.isEmpty()) { stopEmpty(R.string.playback_empty); return }
        if (next == null) {
            if (!isFallback && currentItem?.family == MediaFamily.CAMERAS && beginFallback()) return
            stopEmpty(R.string.playback_all_unavailable)
        } else {
            index = queue.indexOfFirst { it.id == next.id }
            start(next)
        }
    }

    private fun failCurrent() {
        val item = playingItem ?: return
        saveCheckpoint()
        clearTimeout()
        handler.removeCallbacks(rotationTask)
        handler.removeCallbacks(stablePlayback)
        storeWrite { store.updateAvailability(item.id, Availability.UNAVAILABLE) }
        if (recoveryItem != null) {
            recoveryItem = null
            recoveryAfter = SystemClock.elapsedRealtime() + recoveryDelay()
            index = localResumeIndex
            status(R.string.playback_recovery_wait)
            queue.getOrNull(index)?.let { start(it) } ?: stopEmpty(R.string.playback_all_unavailable)
            return
        }
        failed.add(item.id)
        status(if (item.family == MediaFamily.VIDEOS) R.string.playback_video_unavailable else R.string.playback_camera_unavailable)
        // Stop immediately before scheduling the next item; never leave a failing loader running.
        playingItem = null
        player.stop()
        val failedGeneration = generation
        handler.post { if (!released && generation == failedGeneration && playingItem == null) advance(1) }
    }

    private fun beginFallback(): Boolean {
        val settings = store.settings()
        val playlist = store.playlists().firstOrNull {
            it.id == settings.fallbackPlaylistId && it.family == MediaFamily.VIDEOS
        } ?: return false
        val videos = playlist.itemIds.distinct().mapNotNull(store::item).filter { it.family == MediaFamily.VIDEOS }
        if (videos.isEmpty()) return false
        cameraQueue = queue
        queue = if (playlist.shuffle) videos.shuffled() else videos
        index = 0
        failed.clear()
        isFallback = true
        recoveryAttempts = 0
        recoveryCursor = 0
        recoveryAfter = SystemClock.elapsedRealtime() + RECOVERY_MIN_DELAY_MS
        status(R.string.playback_using_fallback)
        start(queue[index])
        return true
    }

    private fun tryRecoveryAtBoundary(): Boolean {
        cameraQueue = cameraQueue.mapNotNull { store.item(it.id) }.filter { it.family == MediaFamily.CAMERAS }.distinctBy { it.id }
        if (!isFallback || stayOnVideos || store.settings().stayLocal || cameraQueue.isEmpty() ||
            SystemClock.elapsedRealtime() < recoveryAfter) return false
        // A completed finite pass gets a full cooldown before another pass. The cursor
        // continues across passes so larger camera libraries are not starved.
        if (recoveryAttempts >= MAX_RECOVERY_ATTEMPTS) recoveryAttempts = 0
        // No second player, socket probe or decoder. A completed local video is the only
        // opportunity to test a camera, and only its rendered frame commits the return.
        localResumeIndex = (index + 1) % queue.size
        val candidate = cameraQueue[recoveryCursor % cameraQueue.size]
        recoveryCursor = (recoveryCursor + 1) % cameraQueue.size
        recoveryAttempts++
        recoveryItem = candidate
        status(R.string.playback_checking_camera)
        start(candidate, recovery = true)
        return true
    }

    private fun cancelRecovery() {
        if (recoveryItem == null) return
        recoveryItem = null
        recoveryAfter = SystemClock.elapsedRealtime() + recoveryDelay()
        clearTimeout()
        playingItem = null
        player.stop()
    }

    private fun recoveryDelay(): Long =
        (RECOVERY_MIN_DELAY_MS * (1L shl recoveryAttempts.coerceAtMost(3))).coerceAtMost(RECOVERY_MAX_DELAY_MS)

    private fun saveCheckpoint(completed: Boolean = false) {
        val item = playingItem ?: return
        if (released || item.family != MediaFamily.VIDEOS || recoveryItem != null) return
        if (!completed && (!hasRenderedFrame || restorePosition > 0)) return
        val duration = player.duration.takeIf { it > 0 && it != C.TIME_UNSET } ?: 0
        // Do not overwrite an existing checkpoint when preparation failed before any position existed.
        if (!completed && player.playbackState == Player.STATE_IDLE && player.currentPosition == 0L) return
        val position = if (completed || completedCurrent || player.playbackState == Player.STATE_ENDED ||
            (duration > 0 && player.currentPosition >= duration - COMPLETION_MARGIN_MS)) 0
            else player.currentPosition.coerceAtLeast(0).let { if (duration > 0) it.coerceAtMost(duration) else it }
        storeWrite { store.saveCheckpoint(PlaybackCheckpoint(item.id, position, duration)) }
    }

    private fun scheduleRotation() {
        handler.removeCallbacks(rotationTask)
        if (released || currentItem?.family != MediaFamily.CAMERAS || recoveryItem != null || !player.isPlaying) return
        val now = SystemClock.elapsedRealtime()
        if (rotationDueAt == 0L) rotationDueAt = now + store.settings().rotationMinutes.coerceIn(1, 60) * 60_000L
        // Adaptive rendition changes must not restart the scene's entire rotation interval.
        handler.postDelayed(rotationTask, (rotationDueAt - now).coerceAtLeast(0))
    }

    private fun stopEmpty(message: Int) {
        terminal = true
        generation++
        clearTimeout()
        handler.removeCallbacks(rotationTask)
        handler.removeCallbacks(stablePlayback)
        playingItem = null
        currentItem = null
        player.stop()
        player.clearMediaItems()
        onItem(null)
        status(message)
    }

    private fun status(message: Int) {
        val value = context.getString(message)
        onStatus(if (storageFailed && message != R.string.playback_storage_error)
            "$value\n${context.getString(R.string.playback_storage_error)}" else value)
    }

    private inline fun storeWrite(write: () -> Unit) {
        try {
            write()
        } catch (_: LibraryStorageException) {
            storageFailed = true
            status(R.string.playback_storage_error)
        }
    }

    private fun armTimeout() {
        if (timeoutArmed || !player.playWhenReady) return
        timeoutArmed = true
        handler.postDelayed(startupTimeout, STARTUP_TIMEOUT_MS)
    }

    private fun clearTimeout() {
        handler.removeCallbacks(startupTimeout)
        timeoutArmed = false
    }

    private fun acceptPlayerEvent(): Boolean = !released && !terminal && playingItem != null &&
        player.currentMediaItem?.mediaId == playingItem?.id

    /** Observe edits made by the other entry point without retaining removed source records. */
    private fun refreshSources() {
        if (terminal) return
        cameraQueue = cameraQueue.mapNotNull { store.item(it.id) }.distinctBy { it.id }
        val playing = playingItem ?: return
        val fresh = store.item(playing.id)
        if (fresh == null || fresh.family != playing.family) {
            if (recoveryItem != null) {
                cancelRecovery()
                advance(1)
            } else {
                failed.add(playing.id)
                advance(1)
            }
        } else if (fresh.uri != playing.uri) {
            saveCheckpoint()
            if (recoveryItem != null) recoveryItem = fresh
            start(fresh, recovery = recoveryItem != null)
        } else {
            playingItem = fresh
            if (recoveryItem == null && currentItem != fresh) {
                currentItem = fresh
                onItem(fresh)
            }
        }
    }

    /** Only choose advertised, decoder-supported tracks; never attempt HDR-to-SDR conversion. */
    private fun selectCompatibleVideo(tracks: Tracks, decodedFormat: Format? = null): Boolean {
        val groups = tracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }
        if (groups.isEmpty()) return true
        val maxHeight = store.settings().maxVideoHeight.takeIf { it > 0 }?.coerceAtMost(2160)
        val maxWidth = maxHeight?.let { if (it <= 1080) 1920 else 3840 }
        val hdrTypes = try {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                ?.defaultDisplay?.hdrCapabilities?.supportedHdrTypes?.toSet().orEmpty()
        } catch (_: RuntimeException) { emptySet() }
        fun allowed(format: Format): Boolean {
            // Some manifests omit dimensions until the elementary stream is read.
            if (maxHeight != null && (format.height > maxHeight || format.width > (maxWidth ?: 3840))) return false
            return when {
                format.sampleMimeType == MimeTypes.VIDEO_DOLBY_VISION -> Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION in hdrTypes
                format.colorInfo?.colorTransfer == C.COLOR_TRANSFER_ST2084 -> Display.HdrCapabilities.HDR_TYPE_HDR10 in hdrTypes
                format.colorInfo?.colorTransfer == C.COLOR_TRANSFER_HLG -> Display.HdrCapabilities.HDR_TYPE_HLG in hdrTypes
                else -> true
            }
        }
        val decodedIncompatible = decodedFormat != null && !allowed(decodedFormat)
        if (decodedIncompatible) {
            hasRenderedFrame = false
            handler.removeCallbacks(stablePlayback)
            armTimeout()
        }
        if (decodedIncompatible) groups.forEach { group ->
            (0 until group.length).filter(group::isTrackSelected).forEach { rejectedVideoFormats.add(group.getTrackFormat(it)) }
        }
        val supported = groups.map { group -> group to (0 until group.length).filter {
            group.isTrackSupported(it) && allowed(group.getTrackFormat(it)) &&
                group.getTrackFormat(it) !in rejectedVideoFormats
        } }.filter { it.second.isNotEmpty() }
        if (supported.isEmpty()) {
            val requested = generation
            handler.post { if (!released && requested == generation) failCurrent() }
            return false
        }
        val selectedCompatible = groups.any { group -> (0 until group.length).any {
            group.isTrackSelected(it) && group.isTrackSupported(it) && allowed(group.getTrackFormat(it)) &&
                group.getTrackFormat(it) !in rejectedVideoFormats
        } }
        val hasIncompatibleVideo = groups.any { group -> (0 until group.length).any {
            group.isTrackSupported(it) && (!allowed(group.getTrackFormat(it)) || group.getTrackFormat(it) in rejectedVideoFormats)
        } }
        if (!selectedCompatible || hasIncompatibleVideo || decodedIncompatible) {
            // Restrict adaptive selection to the compatible renditions of one track group.
            val (group, indexes) = supported.firstOrNull { (group, indexes) -> indexes.any(group::isTrackSelected) }
                ?: supported.first()
            val override = TrackSelectionOverride(group.mediaTrackGroup, indexes)
            if (player.trackSelectionParameters.overrides[group.mediaTrackGroup] != override) {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().setOverrideForType(override).build()
            }
        }
        return !decodedIncompatible
    }

    companion object {
        private const val CHECKPOINT_INTERVAL_MS = 15_000L
        private const val STARTUP_TIMEOUT_MS = 15_000L
        private const val STABLE_PLAYBACK_MS = 60_000L
        private const val COMPLETION_MARGIN_MS = 3_000L
        private const val RECOVERY_MIN_DELAY_MS = 60_000L
        private const val RECOVERY_MAX_DELAY_MS = 300_000L
        private const val MAX_RECOVERY_ATTEMPTS = 6
        private var activeSession: PlaybackSession? = null
        private var resumeSnapshot: PlaybackSnapshot? = null

        /** In-process handoff only; persisted checkpoints remain in the shared library. */
        fun lastSnapshot(): PlaybackSnapshot? = activeSession?.snapshot() ?: resumeSnapshot
    }
}
