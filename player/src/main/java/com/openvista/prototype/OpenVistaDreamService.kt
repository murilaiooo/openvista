package com.openvista.prototype

import android.graphics.Color
import android.service.dreams.DreamService
import android.view.Gravity
import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Optional, owner-selected Android DreamService compatibility probe.
 * No user media or bundled demo content is started inside the system dream.
 * App-mode playback remains available independently of OEM dream support.
 */
class OpenVistaDreamService : DreamService() {
    private var feedback: TextView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setInteractive(true)
        setFullscreen(true)
        // Do not override the device's display dimming behavior.
        setScreenBright(false)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.rgb(12, 23, 39))
        }
        root.addView(TextView(this).apply {
            text = "OPENVISTA / DREAM PROBE"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        })
        root.addView(TextView(this).apply {
            text = "Engineering test only. No media plays in dream mode yet."
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(0xFFB5C8D7.toInt())
        })
        feedback = TextView(this).apply {
            text = "Press arrows or OK to test the remote. Press Back to exit."
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }.also(root::addView)
        setContentView(root)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Android DreamService handles Back/wake itself; preserve that system behavior.
        if (event.keyCode == KeyEvent.KEYCODE_BACK || event.action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event)
        }
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                feedback?.text = "OK received by OpenVista DreamService. Press Back to exit."
                true
            }
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                feedback?.text = "Direction received by OpenVista DreamService. Press Back to exit."
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onDetachedFromWindow() {
        feedback = null
        super.onDetachedFromWindow()
    }
}
