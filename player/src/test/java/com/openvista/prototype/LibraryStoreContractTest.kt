package com.openvista.prototype.library

import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Validation-owned behavior tests for the production library, not a mock storage layer. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LibraryStoreContractTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val stores = mutableListOf<LibraryStore>()
    private fun open(): LibraryStore = LibraryStore(context).also(stores::add)

    @Before fun before() = clearTestFiles()
    @After fun after() {
        stores.forEach { it.flush(); it.close() }
        clearTestFiles()
    }

    private fun clearTestFiles() {
        for (suffix in listOf("", ".bak", ".new")) {
            File(context.filesDir, "openvista-library.json$suffix").delete()
        }
    }

    @Test fun firstInstallStartsEmptyMutedAndWithoutSavedContent() {
        val store = open()
        assertTrue(store.items().isEmpty())
        assertTrue(store.playlists().isEmpty())
        assertTrue(store.folders().isEmpty())
        assertTrue(store.settings().muted)
        assertFalse(store.settings().onboardingComplete)
        assertEquals(1, store.settings().rotationMinutes)
        assertEquals(5, store.settings().videoRotationMinutes)
        assertEquals(5, store.settings().viewerForecastDays)
        assertTrue(store.settings().moveOverlay)
        assertTrue(store.settings().randomOverlayPosition)
        assertTrue(store.settings().showLocation)
    }

    @Test fun importingDuplicateSourcesCreatesOneRecordAndOrderedPlaylistAtomically() {
        val store = open()
        val first = LibraryItem("a", "First", "https://example.invalid/live.m3u8", MediaFamily.CAMERAS, "HLS")
        val second = first.copy(id = "b", title = "Duplicate")
        val result = store.importCameraList("Test list", listOf(first, second))
        assertEquals(1, result.added)
        assertEquals(1, result.duplicateCount)
        assertEquals(listOf("a"), store.playlists().single().itemIds)
        assertEquals("First", store.items().single().title)
        store.flush()
        val restored = open()
        assertEquals(1, restored.items().size)
        assertEquals(listOf("a"), restored.playlists().single().itemIds)
    }

    @Test fun invalidMixedFamilyPlaylistCannotMutateSavedLibrary() {
        val store = open()
        store.upsert(LibraryItem("camera", "Camera", "https://example.invalid/stream.mpd", MediaFamily.CAMERAS, "DASH"))
        try {
            store.savePlaylist(SavedPlaylist("wrong", "Mixed", MediaFamily.VIDEOS, listOf("camera")))
            fail("Mixing camera sources into a video playlist must be rejected")
        } catch (_: LibraryStorageException) {
            assertTrue(store.playlists().isEmpty())
            assertEquals(1, store.items().size)
        }
    }

    @Test fun unpluggedFolderKeepsNamesFavoritesAndPlaylistEntries() {
        val store = open()
        val folder = "content://documents.example/tree/usb-root"
        val video = LibraryItem("video", "Keep my title", "content://documents.example/document/v1",
            MediaFamily.VIDEOS, folderUri = folder, favorite = true)
        store.saveFolder(FolderGrant(folder, "USB", persisted = true))
        store.upsert(video)
        store.savePlaylist(SavedPlaylist("videos", "Favorites", MediaFamily.VIDEOS, listOf("video")))
        store.removeFolder(folder)
        assertTrue(store.folders().isEmpty())
        val retained = store.item("video")!!
        assertEquals("Keep my title", retained.title)
        assertTrue(retained.favorite)
        assertEquals(Availability.UNAVAILABLE, retained.availability)
        assertEquals(listOf("video"), store.playlists().single().itemIds)
    }

    @Test fun directAndTreeRoutesMergeWithoutLosingFavoritePlaylistOrResume() {
        val store = open()
        val folder = "content://documents.example/tree/root"
        val fromTree = "content://documents.example/tree/root/document/root%2Fclip"
        val direct = "content://documents.example/document/root%2Fclip"
        store.saveFolder(FolderGrant(folder, "USB", persisted = true))
        store.upsert(LibraryItem("tree-clip", "Original title", fromTree, MediaFamily.VIDEOS, folderUri = folder))
        store.savePlaylist(SavedPlaylist("set", "Videos", MediaFamily.VIDEOS, listOf("tree-clip")))
        store.saveCheckpoint(PlaybackCheckpoint("tree-clip", 2_000L, 5_000L))
        store.upsert(LibraryItem("direct-clip", "Second title", direct, MediaFamily.VIDEOS, favorite = true))

        assertEquals(1, store.items().size)
        assertEquals("Original title", store.items().single().title)
        assertTrue(store.items().single().favorite)
        assertEquals(2, store.items().single().accessRoutes.size)
        assertEquals(listOf("tree-clip"), store.playlists().single().itemIds)
        assertEquals(2_000L, store.checkpoint("direct-clip")?.positionMs)
        store.flush()

        val restored = open()
        assertEquals(1, restored.items().size)
        assertEquals("tree-clip", restored.item("direct-clip")?.id)
        assertEquals(2, restored.items().single().accessRoutes.size)
        assertEquals(listOf("tree-clip"), restored.playlists().single().itemIds)
    }

    @Test fun viewerForecastDaysPersistAndStayWithinThreeToSeven() {
        val store = open()
        store.saveSettings(store.settings().copy(viewerForecastDays = 7))
        store.flush()
        assertEquals(7, open().settings().viewerForecastDays)
        store.saveSettings(store.settings().copy(viewerForecastDays = 100))
        assertEquals(7, store.settings().viewerForecastDays)
        store.saveSettings(store.settings().copy(viewerForecastDays = 1))
        assertEquals(3, store.settings().viewerForecastDays)
    }

    @Test fun staticInformationPanelsCannotBeSavedAsAlwaysStationary() {
        val store = open()
        store.saveSettings(store.settings().copy(moveOverlay = false, randomOverlayPosition = false))
        assertTrue(store.settings().moveOverlay)
        assertFalse(store.settings().randomOverlayPosition)
    }

    @Test fun cameraAndVideoIntervalsAndMotionSettingsPersistIndependently() {
        val store = open()
        store.saveSettings(store.settings().copy(rotationMinutes = 2, videoRotationMinutes = 7,
            moveOverlay = true, randomOverlayPosition = false, showLocation = true))
        store.flush()
        val restored = open()
        assertEquals(2, restored.settings().rotationMinutes)
        assertEquals(7, restored.settings().videoRotationMinutes)
        assertTrue(restored.settings().moveOverlay)
        assertFalse(restored.settings().randomOverlayPosition)
        assertTrue(restored.settings().showLocation)
    }

    @Test fun settingsAreBoundedAndPersistWithoutInternetOrAccount() {
        val store = open()
        store.saveSettings(store.settings().copy(rotationMinutes = 200, language = "xx",
            onboardingComplete = true, muted = false))
        store.flush()
        val restored = open()
        assertEquals(60, restored.settings().rotationMinutes)
        assertEquals("system", restored.settings().language)
        assertTrue(restored.settings().onboardingComplete)
        assertFalse(restored.settings().muted)
    }
}
