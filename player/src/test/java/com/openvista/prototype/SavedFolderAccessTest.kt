package com.openvista.prototype

import android.content.Context
import android.net.Uri
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SavedFolderAccessTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before fun clearOldPreference() {
        context.getSharedPreferences("openvista_saved_folder", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test fun freshInstallNeverAutoSelectsUserContent() {
        assertNull(SavedFolderAccess(context).restore())
    }

    @Test fun revokedGrantIsNotReusedButSelectionSurvivesForReconnection() {
        val preferences = context.getSharedPreferences("openvista_saved_folder", Context.MODE_PRIVATE)
        val saved = "content://private.documents/tree/expired"
        preferences.edit().putString("tree_uri", saved).commit()
        assertNull("A revoked grant must not be reused.", SavedFolderAccess(context).restore())
        assertEquals("A disconnected provider must not erase the user's choice.", saved,
            preferences.getString("tree_uri", null))
    }

    @Test fun pickerGrantCanBeRememberedAndRestoredInTheSimulator() {
        // Robolectric simulates takePersistableUriPermission without enforcing an OS picker grant.
        // Actual provider authorization must be verified on a physical device.
        val uri = Uri.parse("content://private.documents/tree/selected")
        val folder = SavedFolderAccess(context)
        assertTrue(folder.remember(uri))
        assertEquals(uri, SavedFolderAccess(context).restore())
        folder.forget()
        assertNull(SavedFolderAccess(context).restore())
    }

    @Test fun forgetWorksWhenTheOsHasAlreadyRevokedTheGrant() {
        val preferences = context.getSharedPreferences("openvista_saved_folder", Context.MODE_PRIVATE)
        preferences.edit().putString("tree_uri", "content://private.documents/tree/removed").commit()
        SavedFolderAccess(context).forget()
        assertNull(preferences.getString("tree_uri", null))
    }
}
