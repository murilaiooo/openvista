package com.openvista.prototype

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.service.dreams.DreamService
import android.view.KeyEvent
import org.robolectric.Robolectric
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DreamRegistrationTest {
    @Test fun engineeringDreamHandlesDirectionalAndSelectKeyDown() {
        val controller = Robolectric.buildService(OpenVistaDreamService::class.java).create()
        try {
            val dream = controller.get()
            assertTrue(dream.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)))
            assertTrue(dream.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)))
            assertTrue(dream.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER)))
        } finally {
            controller.destroy()
        }
    }

    @Test fun dreamIsOptInAndBoundOnlyByTheAndroidDreamSystem() {
        val context = RuntimeEnvironment.getApplication()
        val services = context.packageManager.queryIntentServices(
            Intent(DreamService.SERVICE_INTERFACE), PackageManager.GET_META_DATA,
        )
        val ours = services.singleOrNull {
            it.serviceInfo.packageName == context.packageName &&
                it.serviceInfo.name.endsWith(".OpenVistaDreamService")
        }
        assertNotNull("The system must be able to discover the optional OpenVista DreamService", ours)
        val service = requireNotNull(ours).serviceInfo
        assertEquals(Manifest.permission.BIND_DREAM_SERVICE, service.permission)
        assertTrue(service.exported)
        assertTrue("Dream service metadata must be declared", service.metaData?.getInt(DreamService.DREAM_META_DATA) != 0)
    }
}
