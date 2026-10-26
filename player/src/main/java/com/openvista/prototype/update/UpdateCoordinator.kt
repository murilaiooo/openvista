package com.openvista.prototype.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InterruptedIOException
import java.net.URL
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.HttpsURLConnection

/** No constructor/startup network traffic. All work starts with an explicit caller action. */
class UpdateCoordinator(context: Context) : AutoCloseable {
    private val app = context.applicationContext
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val generation = AtomicLong()
    private val stateLock = Any()
    private var task: Future<*>? = null
    @Volatile private var connection: HttpsURLConnection? = null
    @Volatile private var closed = false
    private var callback: ((UpdateState) -> Unit)? = null
    private var offered: UpdateRelease? = null
    @Volatile private var downloaded: DownloadedUpdate? = null
    private var sessionId: Int? = null
    @Volatile private var operationDeadline = 0L
    private val directory get() = File(app.cacheDir, "verified-updates")
    private val downloadPreferences get() = app.getSharedPreferences("verified-update", Context.MODE_PRIVATE)

    fun discover(callback: (UpdateState) -> Unit) = launch(callback) { id ->
        emit(id, UpdateState.Checking)
        offered = null
        if (!UpdateChannel.APPROVED) {
            emit(id, UpdateState.Unavailable)
            return@launch
        }
        val installed = installedInfo()
        requireProduction(installed)
        val conn = open(URL(UpdateChannel.API), false, id)
        val body = try {
            if (conn.responseCode == 404) {
                emit(id, UpdateState.Unavailable)
                return@launch
            }
            check(conn.responseCode == 200)
            check(conn.contentLengthLong < 0 || conn.contentLengthLong <= UpdateChannel.MAX_JSON_BYTES)
            conn.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    checkActive(id)
                    val count = input.read(buffer)
                    if (count < 0) break
                    check(count > 0 && output.size() + count <= UpdateChannel.MAX_JSON_BYTES)
                    output.write(buffer, 0, count)
                }
                output.toString(Charsets.UTF_8.name())
            }
        } finally { conn.disconnect(); connection = null }
        val json = JSONObject(body)
        check(!json.getBoolean("draft") && !json.getBoolean("prerelease"))
        val tag = json.getString("tag_name")
        check(tag.matches(Regex("v[1-9][0-9]{0,9}")))
        val version = tag.drop(1).toLong()
        if (version <= installed.longVersionCode) {
            emit(id, UpdateState.Unavailable)
            return@launch
        }
        val assets = json.getJSONArray("assets")
        check(assets.length() <= 32)
        val name = "openvista-${app.packageName}-$tag.apk"
        val matches = (0 until assets.length()).map { assets.getJSONObject(it) }
            .filter { it.getString("name") == name && it.getString("state") == "uploaded" }
        check(matches.size == 1)
        val asset = matches.single()
        val size = asset.getLong("size")
        check(size in 1..UpdateChannel.MAX_APK_BYTES)
        val digest = asset.getString("digest")
        check(digest.matches(Regex("sha256:[0-9a-f]{64}")))
        val source = asset.getString("browser_download_url")
        check(source == "${UpdateChannel.PROJECT}$tag/$name")
        val release = UpdateRelease(tag, version, size, source, digest.removePrefix("sha256:"))
        offered = release
        emit(id, UpdateState.Available(release))
    }

    fun download(release: UpdateRelease, callback: (UpdateState) -> Unit) = launch(callback) { id ->
        check(UpdateChannel.APPROVED && release == offered)
        downloaded?.file?.delete()
        downloaded = null
        downloadPreferences.edit().clear().apply()
        check(directory.isDirectory || directory.mkdirs())
        directory.listFiles()?.forEach { cached ->
            if (cached.isFile && cached.canonicalFile.parentFile == directory.canonicalFile &&
                (cached.name.matches(Regex("[a-f0-9-]{36}\\.apk")) ||
                    cached.name.matches(Regex("update-[a-zA-Z0-9-]+\\.part")))) cached.delete()
        }
        val partial = File.createTempFile("update-", ".part", directory)
        try {
            val conn = open(URL(release.downloadUrl), true, id)
            try {
                check(conn.responseCode == 200)
                check(conn.contentLengthLong < 0 || conn.contentLengthLong == release.bytes)
                conn.inputStream.use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(32_768)
                        var bytes = 0L
                        var lastProgress = 0L
                        while (true) {
                            checkActive(id)
                            val count = input.read(buffer)
                            if (count < 0) break
                            bytes += count
                            check(count > 0 && bytes <= release.bytes && bytes <= UpdateChannel.MAX_APK_BYTES)
                            output.write(buffer, 0, count)
                            if (bytes - lastProgress >= 262_144) {
                                emit(id, UpdateState.Downloading(bytes, release.bytes))
                                lastProgress = bytes
                            }
                        }
                        check(bytes == release.bytes)
                        output.fd.sync()
                    }
                }
            } finally { conn.disconnect(); connection = null }
            validate(partial, release, id)
            checkActive(id)
            val staged = File(directory, "${UUID.randomUUID()}.apk")
            check(partial.renameTo(staged))
            val artifact = DownloadedUpdate(release, staged)
            rememberDownload(artifact)
            downloaded = artifact
            emit(id, UpdateState.Ready(artifact))
        } finally { partial.delete() }
    }

    fun install(update: DownloadedUpdate, callback: (UpdateState) -> Unit) = launch(callback) { id ->
        check(UpdateChannel.APPROVED && update == downloaded)
        check(!UpdateInstallReceiver.hasActiveSession(app))
        if (!app.packageManager.canRequestPackageInstalls()) {
            emit(id, UpdateState.NeedsPermission)
            return@launch
        }
        validate(update.file, update.release, id)
        val installer = app.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(app.packageName)
        params.setSize(update.file.length())
        val created = installer.createSession(params)
        sessionId = created
        var committed = false
        try {
            installer.openSession(created).use { session ->
                update.file.inputStream().use { input ->
                    session.openWrite("base.apk", 0, update.file.length()).use { output ->
                        val buffer = ByteArray(32_768)
                        while (true) {
                            checkActive(id)
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                        }
                        session.fsync(output)
                    }
                }
                val nonce = UUID.randomUUID().toString()
                val intent = Intent(app, UpdateInstallReceiver::class.java)
                    .setAction(UpdateInstallReceiver.ACTION)
                    .setData(Uri.parse("openvista-update://session/$created/$nonce"))
                    .putExtra(UpdateInstallReceiver.NONCE, nonce)
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
                val result = PendingIntent.getBroadcast(app, created, intent, flags)
                synchronized(stateLock) {
                    checkActive(id)
                    UpdateInstallReceiver.register(app, created, nonce) { state -> emit(id, state) }
                    session.commit(result.intentSender)
                    committed = true
                    emit(id, UpdateState.Installing)
                }
            }
        } finally {
            if (!committed) {
                runCatching { installer.abandonSession(created) }
                sessionId = null
                UpdateInstallReceiver.forget(app)
            }
        }
    }

    fun unknownAppsSettingsIntent(): Intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        Uri.parse("package:${app.packageName}"))

    /** Reattach to an installer result after UI/process recreation; this performs no network work. */
    fun resumeInstallation(callback: (UpdateState) -> Unit): Boolean {
        check(!closed)
        val state = UpdateInstallReceiver.savedState(app)
        if (state != null) {
            pause()
            synchronized(stateLock) {
                this.callback = callback
                val id = generation.get()
                sessionId = UpdateInstallReceiver.activeSession(app)
                UpdateInstallReceiver.attach { result -> emit(id, result) }
                emit(id, UpdateInstallReceiver.savedState(app) ?: state)
            }
            return true
        }
        val retained = downloaded ?: retainedDownload() ?: return false
        launch(callback) { id ->
            validate(retained.file, retained.release, id)
            downloaded = retained
            offered = retained.release
            emit(id, if (app.packageManager.canRequestPackageInstalls()) UpdateState.Ready(retained)
                else UpdateState.NeedsPermission)
        }
        return true
    }

    /** Stop foreground work without cancelling an installation owned by the system installer. */
    fun pause() = synchronized(stateLock) {
        generation.incrementAndGet()
        connection?.disconnect()
        connection = null
        task?.cancel(true)
        task = null
        callback = null
        UpdateInstallReceiver.detach()
    }

    /** Explicit user cancellation may abandon a staged/committed session if Android permits it. */
    fun cancel() {
        val sink = callback
        val cancelledGeneration = synchronized(stateLock) {
            pause()
            (sessionId ?: UpdateInstallReceiver.activeSession(app))?.let {
                runCatching { app.packageManager.packageInstaller.abandonSession(it) }
            }
            sessionId = null
            UpdateInstallReceiver.forget(app)
            (downloaded ?: retainedDownload())?.file?.delete()
            downloaded = null
            downloadPreferences.edit().clear().apply()
            generation.get()
        }
        main.post { if (!closed && generation.get() == cancelledGeneration) sink?.invoke(UpdateState.Cancelled) }
    }

    override fun close() {
        synchronized(stateLock) {
            closed = true
            pause()
        }
        worker.shutdownNow()
    }

    private fun launch(sink: (UpdateState) -> Unit, action: (Long) -> Unit) {
        check(!closed)
        generation.incrementAndGet()
        connection?.disconnect()
        task?.cancel(true)
        callback = sink
        val id = generation.get()
        operationDeadline = SystemClock.elapsedRealtime() + 180_000L
        if (UpdateInstallReceiver.hasActiveSession(app)) {
            UpdateInstallReceiver.attach { result -> emit(id, result) }
            UpdateInstallReceiver.savedState(app)?.let { emit(id, it) }
            return
        }
        task = worker.submit {
            try { action(id) } catch (_: Exception) {
                if (generation.get() == id && !closed) emit(id, UpdateState.Failed)
            }
        }
    }

    private fun emit(id: Long, state: UpdateState) {
        main.post { if (!closed && generation.get() == id) callback?.invoke(state) }
    }

    private fun checkActive(id: Long) {
        if (closed || generation.get() != id || Thread.currentThread().isInterrupted ||
            SystemClock.elapsedRealtime() >= operationDeadline) throw InterruptedIOException()
    }

    private fun rememberDownload(update: DownloadedUpdate) {
        val release = update.release
        check(downloadPreferences.edit().clear().putString("file", update.file.name)
            .putString("tag", release.tag).putLong("version", release.versionCode)
            .putLong("bytes", release.bytes).putString("source", release.downloadUrl)
            .putString("sha256", release.sha256).putLong("created", System.currentTimeMillis()).commit())
    }

    /** Private metadata restores provenance; complete bytes/signatures are rechecked before Ready. */
    private fun retainedDownload(): DownloadedUpdate? {
        return runCatching {
        if (!UpdateChannel.APPROVED) return null
        val prefs = downloadPreferences
        val age = System.currentTimeMillis() - prefs.getLong("created", 0)
        if (age !in 0..86_400_000L) return null
        val name = prefs.getString("file", null) ?: return null
        if (!name.matches(Regex("[a-f0-9-]{36}\\.apk"))) return null
        val file = File(directory, name)
        if (!file.isFile || file.canonicalFile.parentFile != directory.canonicalFile) return null
        val tag = prefs.getString("tag", null) ?: return null
        if (!tag.matches(Regex("v[1-9][0-9]{0,9}"))) return null
        val version = prefs.getLong("version", -1)
        val bytes = prefs.getLong("bytes", -1)
        val source = prefs.getString("source", null) ?: return null
        val digest = prefs.getString("sha256", null) ?: return null
        if (version != tag.drop(1).toLong() || bytes !in 1..UpdateChannel.MAX_APK_BYTES ||
            source != "${UpdateChannel.PROJECT}$tag/openvista-${app.packageName}-$tag.apk" ||
            !digest.matches(Regex("[0-9a-f]{64}"))) return null
        DownloadedUpdate(UpdateRelease(tag, version, bytes, source, digest), file)
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun installedInfo(): PackageInfo = app.packageManager.getPackageInfo(app.packageName,
        PackageManager.GET_SIGNING_CERTIFICATES)

    private fun requireProduction(info: PackageInfo) {
        val flags = info.applicationInfo?.flags ?: error("Missing application metadata")
        check(flags and (ApplicationInfo.FLAG_DEBUGGABLE or ApplicationInfo.FLAG_TEST_ONLY) == 0)
        check(info.versionName.orEmpty().matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+")))
    }

    @Suppress("DEPRECATION")
    private fun validate(file: File, release: UpdateRelease, id: Long) {
        check(file.canonicalFile.parentFile == directory.canonicalFile && file.length() == release.bytes)
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(32_768)
            while (true) {
                checkActive(id)
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        check(digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) } == release.sha256)
        val installed = installedInfo()
        requireProduction(installed)
        val candidate = app.packageManager.getPackageArchiveInfo(file.path,
            PackageManager.GET_SIGNING_CERTIFICATES) ?: error("Invalid package")
        requireProduction(candidate)
        check(candidate.packageName == app.packageName)
        check((candidate.applicationInfo?.minSdkVersion ?: Int.MAX_VALUE) <= Build.VERSION.SDK_INT)
        check(candidate.longVersionCode == release.versionCode && candidate.longVersionCode > installed.longVersionCode)
        val currentSigning = installed.signingInfo ?: error("No installed signer")
        val nextSigning = candidate.signingInfo ?: error("No candidate signer")
        // Conservative continuity: rotation requires a separately reviewed policy, even with a lineage.
        val current = currentSigning.apkContentsSigners.map { it.toCharsString() }.toSet()
        val next = nextSigning.apkContentsSigners.map { it.toCharsString() }.toSet()
        check(current.isNotEmpty() && current == next)
        checkActive(id)
    }

    private fun open(initial: URL, asset: Boolean, id: Long): HttpsURLConnection {
        var url = initial
        repeat(5) { hop ->
            checkActive(id)
            check(url.protocol == "https" && url.userInfo == null && (url.port == -1 || url.port == 443))
            if (!asset) check(url.toString() == UpdateChannel.API)
            else if (hop == 0) check(url.toString().startsWith(UpdateChannel.PROJECT))
            else check(url.host == "release-assets.githubusercontent.com")
            val conn = url.openConnection() as HttpsURLConnection
            connection = conn
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("User-Agent", "OpenVista-Update")
            conn.setRequestProperty("Accept-Encoding", "identity")
            conn.setRequestProperty("Accept", if (asset) "application/octet-stream" else "application/vnd.github+json")
            conn.setRequestProperty("X-GitHub-Api-Version", "2026-03-10")
            val status = try { conn.responseCode } catch (error: Exception) { conn.disconnect(); throw error }
            if (status !in setOf(301, 302, 303, 307, 308)) return conn
            val destination = conn.getHeaderField("Location")
            conn.disconnect()
            check(asset && hop < 4 && destination != null)
            url = URL(url, destination)
        }
        error("Redirect limit")
    }
}
