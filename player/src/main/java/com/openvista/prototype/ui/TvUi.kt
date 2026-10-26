package com.openvista.prototype.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class TvUi(val context: Context) {
    fun dp(value: Int) = (value * context.resources.displayMetrics.density + .5f).toInt()
    fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    fun row() = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    fun text(value: String, size: Float = 18f, color: Int = Color.WHITE) = TextView(context).apply {
        text = value; textSize = size; setTextColor(color)
        setPadding(dp(4), dp(6), dp(4), dp(6))
    }
    fun heading(value: String) = text(value, 30f).apply {
        setTypeface(typeface, Typeface.BOLD)
        setAccessibilityHeading(true)
    }
    fun description(value: String) = text(value, 17f, 0xFFC0D0DF.toInt())
    fun scroll(child: View) = ScrollView(context).apply {
        isFillViewport = true
        clipToPadding = false
        addView(child, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
    fun button(parent: LinearLayout, title: String, key: String = title, onClick: () -> Unit): Button {
        return Button(context).apply {
            id = View.generateViewId()
            tag = key
            text = title
            textSize = 18f
            isAllCaps = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            minHeight = dp(56)
            setPadding(dp(20), dp(10), dp(20), dp(10))
            setTextColor(Color.WHITE)
            background = shape(false)
            setOnFocusChangeListener { _, focused ->
                background = shape(focused)
                setTextColor(if (focused) 0xFF071929.toInt() else Color.WHITE)
            }
            setOnClickListener { onClick() }
            parent.addView(this, LinearLayout.LayoutParams(
                if (parent.orientation == LinearLayout.HORIZONTAL) 0 else ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                if (parent.orientation == LinearLayout.HORIZONTAL) 1f else 0f,
            ).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) })
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
            setTextColor(Color.WHITE)
            setHintTextColor(0xFFB5C8D7.toInt())
            inputType = type or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            isSingleLine = true
            filters = arrayOf(InputFilter.LengthFilter(max))
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            setPadding(dp(12), dp(6), dp(12), dp(6))
            backgroundTintList = ColorStateList.valueOf(0xFF80D6F0.toInt())
            parent.addView(this, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))
        }
    }
    fun announce(view: TextView, message: String) {
        view.text = message
        view.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
    }
    private fun shape(focused: Boolean) = GradientDrawable().apply {
        setColor(if (focused) 0xFF8BDEF2.toInt() else 0xFF203448.toInt())
        cornerRadius = dp(12).toFloat()
        setStroke(dp(if (focused) 3 else 1), if (focused) Color.WHITE else 0xFF405569.toInt())
    }
}
