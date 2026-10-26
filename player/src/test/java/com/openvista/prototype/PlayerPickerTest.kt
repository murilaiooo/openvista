package com.openvista.prototype

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PlayerPickerTest {
    @Test fun importListRequiresUserActionAndLaunchesReadOnlyDocumentPicker() {
        val controller = Robolectric.buildActivity(PlayerPrototypeActivity::class.java).setup()
        try {
            val activity = controller.get()
            val shadow = Shadows.shadowOf(activity)
            assertNull("The app must not open any picker on startup", shadow.peekNextStartedActivityForResult())

            button(activity.window.decorView, "Import a list").performClick()
            val intent = shadow.nextStartedActivityForResult?.intent
            assertNotNull("Picker should open only on user action", intent)
            assertEquals(Intent.ACTION_OPEN_DOCUMENT, intent?.action)
            assertTrue(intent!!.hasCategory(Intent.CATEGORY_OPENABLE))
            assertEquals("*/*", intent.type)
            assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
            assertFalse(intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0)
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test fun playMyVideosLaunchesOnlyScopedFolderSelection() {
        val controller = Robolectric.buildActivity(PlayerPrototypeActivity::class.java).setup()
        try {
            val activity = controller.get()
            val shadow = Shadows.shadowOf(activity)
            assertNull(shadow.peekNextStartedActivityForResult())
            button(activity.window.decorView, "Play my videos").performClick()
            val intent = shadow.nextStartedActivityForResult?.intent
            assertNotNull(intent)
            assertEquals(Intent.ACTION_OPEN_DOCUMENT_TREE, intent?.action)
            assertTrue(intent!!.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
            assertTrue("The picker should request a persistable read grant", intent.flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0)
            assertFalse(intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0)
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test fun emptyLibraryCannotPlayImportedOrLocalMediaWithoutOwnerInput() {
        val controller = Robolectric.buildActivity(PlayerPrototypeActivity::class.java).setup()
        try {
            val activity = controller.get()
            assertFalse(button(activity.window.decorView, "Play first imported").isEnabled)
            assertFalse(button(activity.window.decorView, "Play first video").isEnabled)
            assertFalse(button(activity.window.decorView, "Forget folder").isEnabled)
            assertNull(Shadows.shadowOf(activity).peekNextStartedActivityForResult())
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test fun invalidManualUrlDoesNotLaunchPicker() {
        val controller = Robolectric.buildActivity(PlayerPrototypeActivity::class.java).setup()
        try {
            val activity = controller.get()
            button(activity.window.decorView, "Play URL").performClick()
            assertNull(Shadows.shadowOf(activity).peekNextStartedActivityForResult())
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test fun soundCanBeEnabledAndDisabledOnlyByTheViewer() {
        val controller = Robolectric.buildActivity(PlayerPrototypeActivity::class.java).setup()
        try {
            val activity = controller.get()
            val root = activity.window.decorView
            val enable = button(root, "Sound: Off")
            assertTrue(enable.isEnabled)
            enable.performClick()
            val disable = button(root, "Sound: On")
            assertTrue(disable.isEnabled)
            disable.performClick()
            assertTrue(button(root, "Sound: Off").isEnabled)
            assertNull(Shadows.shadowOf(activity).peekNextStartedActivityForResult())
        } finally {
            controller.pause().stop().destroy()
        }
    }

    private fun button(view: View, title: String): Button {
        if (view is Button && view.text.toString() == title) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                try { return button(view.getChildAt(i), title) }
                catch (_: NoSuchElementException) { /* Try next child */ }
            }
        }
        throw NoSuchElementException(title)
    }
}
