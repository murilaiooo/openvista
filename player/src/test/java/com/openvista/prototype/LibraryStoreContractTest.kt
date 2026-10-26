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
