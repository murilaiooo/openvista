package com.openvista.prototype.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class TvUi(val context: Context) {
    fun dp(value: Int) = (value * context.resources.displayMetrics.density + .5f).toInt()
    fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    fun row() = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    fun text(value: String, size: Float = TvDesign.bodySize, color: Int = TvDesign.text) = TextView(context).apply {
        text = value; textSize = size; setTextColor(color)
        typeface = TvDesign.regular
        setPadding(dp(4), dp(6), dp(4), dp(6))
    }
    fun heading(value: String) = text(value, TvDesign.titleSize).apply {
        typeface = TvDesign.light
        setAccessibilityHeading(true)
    }
    fun description(value: String) = text(value, TvDesign.bodySize, TvDesign.muted)
    fun scroll(child: View) = ScrollView(context).apply {
        isFillViewport = true
        clipToPadding = false
        isFocusable = false
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isVerticalScrollBarEnabled = false
        addView(child, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
    fun button(parent: LinearLayout, title: String, key: String = title, onClick: () -> Unit): Button {
        return Button(context).apply {
            id = View.generateViewId()
            tag = key
            text = title
            textSize = TvDesign.bodySize
            typeface = TvDesign.medium
            isAllCaps = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            minHeight = dp(TvDesign.rowHeight)
            minWidth = 0
            minimumWidth = 0
            setPadding(dp(18), dp(10), dp(18), dp(10))
            setTextColor(TvDesign.text)
            background = TvDesign.control(context)
            setOnClickListener { onClick() }
            parent.addView(this, LinearLayout.LayoutParams(
                if (parent.orientation == LinearLayout.HORIZONTAL) 0 else ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                if (parent.orientation == LinearLayout.HORIZONTAL) 1f else 0f,
            ).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) })
        }
    }

    /** A setting has one remote target; its labels never become separate focus stops. */
    fun settingRow(parent: LinearLayout, title: String, value: String = "", key: String = title,
                   summary: String? = null, enabled: Boolean = true, onClick: () -> Unit): LinearLayout =
        addSettingRow(parent, title, value, key, summary, enabled, null, onClick)

    fun toggleRow(parent: LinearLayout, title: String, checked: Boolean, key: String = title,
                  summary: String? = null, enabled: Boolean = true, onClick: () -> Unit): LinearLayout =
        addSettingRow(parent, title, context.getString(if (checked) com.openvista.prototype.R.string.on else com.openvista.prototype.R.string.off),
            key, summary, enabled, checked, onClick)

    private fun addSettingRow(parent: LinearLayout, title: String, value: String, key: String,
                              summary: String?, enabled: Boolean, checked: Boolean?, onClick: () -> Unit): LinearLayout {
        val row = object : LinearLayout(context) {
            override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(info)
                info.className = if (checked == null) "android.widget.Button" else "android.widget.Switch"
                info.isCheckable = checked != null
                if (checked != null) info.isChecked = checked
            }
        }.apply {
            id = View.generateViewId()
            tag = key
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(64)
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = TvDesign.control(context)
            isEnabled = enabled
            isFocusable = enabled
            isClickable = enabled
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            contentDescription = listOf(title, value, summary.orEmpty()).filter(String::isNotBlank).joinToString(". ")
            if (enabled) setOnClickListener { onClick() }
        }
        val labels = column()
        labels.addView(text(title, TvDesign.bodySize, if (enabled) TvDesign.text else TvDesign.muted).apply {
            typeface = TvDesign.medium
            setPadding(0, 0, dp(12), 0)
        })
        if (!summary.isNullOrBlank()) labels.addView(text(summary, 13f, TvDesign.muted).apply {
            setPadding(0, dp(4), dp(12), 0)
        })
        row.addView(labels, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        if (value.isNotBlank()) row.addView(text(value, 15f, if (enabled) TvDesign.accent else TvDesign.muted).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            maxWidth = dp(190)
            setPadding(dp(10), 0, 0, 0)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        if (checked != null) row.addView(ToggleMark(context, checked).apply { alpha = if (enabled) 1f else .45f },
            LinearLayout.LayoutParams(dp(38), dp(24)).apply { marginStart = dp(12) })
        else if (enabled) row.addView(text("›", 25f, TvDesign.muted).apply { setPadding(dp(14), 0, 0, 0) })
        for (index in 0 until row.childCount) {
            row.getChildAt(index).importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, dp(3), 0, dp(3))
        })
        return row
    }

    private class ToggleMark(context: Context, private val checked: Boolean) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val inset = height * .18f
            val radius = (height - inset * 2) / 2
            paint.color = if (checked) TvDesign.accent else 0xFF516574.toInt()
            canvas.drawRoundRect(0f, inset, width.toFloat(), height - inset, radius, radius, paint)
            paint.color = if (checked) TvDesign.background else TvDesign.muted
            canvas.drawCircle(if (checked) width - height / 2f else height / 2f, height / 2f, height * .25f, paint)
        }
    }
    fun field(parent: LinearLayout, label: String, value: String = "", max: Int = 100,
              type: Int = InputType.TYPE_CLASS_TEXT): EditText {
        val caption = text(label, 16f)
        parent.addView(caption)
        return EditText(context).apply {
            id = View.generateViewId()
            caption.labelFor = id
            setText(value)
            textSize = 18f
            setTextColor(TvDesign.text)
            typeface = TvDesign.regular
            setHintTextColor(TvDesign.muted)
            inputType = type or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            isSingleLine = true
            filters = arrayOf(InputFilter.LengthFilter(max))
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            isSaveEnabled = false
            tag = "field_$label"
            setPadding(dp(12), dp(6), dp(12), dp(6))
            backgroundTintList = ColorStateList.valueOf(TvDesign.accent)
            parent.addView(this, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))
        }
    }
    fun announce(view: TextView, message: String) {
        view.text = message
        view.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
    }
}
