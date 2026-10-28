package com.openvista.prototype.ui

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.LibraryRoute
import com.openvista.prototype.OpenVistaActivity
import com.openvista.prototype.R

/**
 * Frame for library, onboarding and other secondary screens: the shared logo and quiet
 * Home/Settings actions above one glass panel, matching the Settings surfaces.
 */
@UnstableApi
class ScreenShell(private val host: OpenVistaActivity) {
    private val ui get() = host.ui

    class Parts(val root: View, val navigation: LinearLayout, val feedback: TextView)

    fun create(body: LinearLayout, feedbackText: String): Parts {
        val root = FrameLayout(host).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFF0B2536.toInt(), 0xFF081A28.toInt(), 0xFF05111C.toInt()))
        }
        val column = ui.column().apply {
            setPadding(ui.dp(34), ui.dp(22), ui.dp(34), ui.dp(24))
        }
        val header = ui.row().apply { minimumHeight = ui.dp(40) }
        header.addView(OpenVistaLogo.create(host, 15.4f), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val navigation = ui.row()
        pill(navigation, R.string.home, TvIcon.Symbol.HOME, "navigation_home") { host.goHome() }
        pill(navigation, R.string.settings, TvIcon.Symbol.SETTINGS, "navigation_settings") { host.open(LibraryRoute("settings")) }
        header.addView(navigation)
        column.addView(header)
        val feedback = ui.text(feedbackText, 10f).apply {
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            setPadding(ui.dp(2), ui.dp(6), ui.dp(2), ui.dp(4))
            visibility = if (feedbackText.isBlank()) View.GONE else View.VISIBLE
        }
        column.addView(feedback)
        body.setPadding(ui.dp(17), ui.dp(12), ui.dp(15), ui.dp(16))
        val panel = FrameLayout(host).apply {
            background = TvDesign.panel(host, TvDesign.glass, TvDesign.glassStroke, radiusDp = 7f)
            clipToOutline = true
            addView(ui.scroll(body).apply {
                isVerticalFadingEdgeEnabled = true
                setFadingEdgeLength(ui.dp(14))
            }, FrameLayout.LayoutParams(-1, -1))
        }
        column.addView(panel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = ui.dp(8) })
        root.addView(column, FrameLayout.LayoutParams(-1, -1))
        return Parts(root, navigation, feedback)
    }

    private fun pill(parent: LinearLayout, title: Int, icon: TvIcon.Symbol, key: String, action: () -> Unit): Button =
        ui.button(parent, host.getString(title), key, action).apply {
            textSize = 10.5f
            gravity = Gravity.CENTER
            minHeight = ui.dp(28)
            minimumHeight = ui.dp(28)
            setPadding(ui.dp(12), ui.dp(4), ui.dp(14), ui.dp(4))
            background = TvDesign.control(host, base = 0x14FFFFFF, stroke = 0x26FFFFFF, radiusDp = 14f)
            setCompoundDrawablesRelativeWithIntrinsicBounds(TvIcon(host, icon, 11f, TvDesign.text, 1.7f), null, null, null)
            compoundDrawablePadding = ui.dp(6)
            layoutParams = (layoutParams as LinearLayout.LayoutParams).apply {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                weight = 0f
                marginStart = ui.dp(6)
            }
        }
}
