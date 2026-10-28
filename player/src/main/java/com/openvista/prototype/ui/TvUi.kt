package com.openvista.prototype.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
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
import com.openvista.prototype.R

class TvUi(val context: Context) {
    /** Right-hand affordance of a settings row, matching what selecting it does. */
    enum class RowControl { CHEVRON, DROPDOWN, NONE }

    fun dp(value: Int) = TvDesign.dp(context, value)
    fun dp(value: Float) = TvDesign.dp(context, value)
    fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    fun row() = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    fun text(value: String, size: Float = TvDesign.bodySize, color: Int = TvDesign.text) = TextView(context).apply {
        text = value; textSize = size; setTextColor(color)
        typeface = TvDesign.regular
        includeFontPadding = false
        setLineSpacing(dp(2f).toFloat(), 1f)
        setPadding(dp(2), dp(3), dp(2), dp(3))
    }
    fun heading(value: String) = text(value, TvDesign.titleSize).apply {
        typeface = TvDesign.light
        setAccessibilityHeading(true)
    }
    fun description(value: String) = text(value, 10f, TvDesign.muted)

    /** Tracked uppercase section label used above groups of rows. */
    fun caps(value: String, color: Int = TvDesign.muted) = text(value, TvDesign.capsSize, color).apply {
        typeface = TvDesign.semibold
        TvDesign.caps(this, .3f)
        setAccessibilityHeading(true)
    }

    /**
     * Scrolling container. Without an exact height (inside a dialog) it caps itself at 65 % of the
     * screen, so long forms scroll and the dialog's buttons never leave the display.
     */
    fun scroll(child: View) = object : ScrollView(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val cap = (resources.displayMetrics.heightPixels * .65f).toInt()
            val spec = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) heightMeasureSpec
                else MeasureSpec.makeMeasureSpec(
                    if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) minOf(cap, MeasureSpec.getSize(heightMeasureSpec)) else cap,
                    MeasureSpec.AT_MOST)
            super.onMeasure(widthMeasureSpec, spec)
        }
    }.apply {
        isFillViewport = true
        clipToPadding = false
        isFocusable = false
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isVerticalScrollBarEnabled = false
        isSmoothScrollingEnabled = true
        addView(child, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    fun button(parent: LinearLayout, title: String, key: String = title, onClick: () -> Unit): Button {
        return Button(context).apply {
            id = View.generateViewId()
            tag = key
            text = title
            textSize = 11f
            typeface = TvDesign.medium
            isAllCaps = false
            includeFontPadding = false
            stateListAnimator = null
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            minHeight = dp(TvDesign.rowHeight - 6)
            minimumHeight = dp(TvDesign.rowHeight - 6)
            minWidth = 0
            minimumWidth = 0
            setPadding(dp(14), dp(7), dp(14), dp(7))
            setTextColor(TvDesign.text)
            background = TvDesign.control(context)
            setOnClickListener { onClick() }
            parent.addView(this, LinearLayout.LayoutParams(
                if (parent.orientation == LinearLayout.HORIZONTAL) 0 else ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                if (parent.orientation == LinearLayout.HORIZONTAL) 1f else 0f,
            ).apply { setMargins(dp(2.5f), dp(2.5f), dp(2.5f), dp(2.5f)) })
        }
    }

    /** A setting has one remote target; its labels never become separate focus stops. */
    fun settingRow(parent: LinearLayout, title: String, value: String = "", key: String = title,
                   summary: String? = null, enabled: Boolean = true, icon: TvIcon.Symbol? = null,
                   control: RowControl = if (value.isBlank()) RowControl.CHEVRON else RowControl.DROPDOWN,
                   onClick: () -> Unit): LinearLayout =
        addSettingRow(parent, title, value, key, summary, enabled, null, icon, control, onClick)

    fun toggleRow(parent: LinearLayout, title: String, checked: Boolean, key: String = title,
                  summary: String? = null, enabled: Boolean = true, icon: TvIcon.Symbol? = null,
                  onClick: () -> Unit): LinearLayout =
        addSettingRow(parent, title, context.getString(if (checked) R.string.on else R.string.off),
            key, summary, enabled, checked, icon, RowControl.NONE, onClick)

    private fun addSettingRow(parent: LinearLayout, title: String, value: String, key: String,
                              summary: String?, enabled: Boolean, checked: Boolean?, icon: TvIcon.Symbol?,
                              control: RowControl, onClick: () -> Unit): LinearLayout {
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
            minimumHeight = dp(TvDesign.rowHeight + 3)
            setPadding(dp(icon?.let { 14 } ?: 15), dp(7), dp(9), dp(7))
            background = TvDesign.control(context, base = 0x0EFFFFFF, radiusDp = 4.6f)
            isEnabled = enabled
            isFocusable = enabled
            isClickable = enabled
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            contentDescription = listOf(title, value, summary.orEmpty()).filter(String::isNotBlank).joinToString(". ")
            if (enabled) setOnClickListener { onClick() }
        }
        val dim = if (enabled) 1f else .5f
        if (icon != null) row.addView(View(context).apply {
            background = TvIcon(context, icon, 18f, 0xFFC8D3DB.toInt(), 1.4f)
            alpha = dim
        }, LinearLayout.LayoutParams(dp(18), dp(18)).apply { marginEnd = dp(15.6f) })
        val labels = column()
        labels.addView(text(title, 11f, TvDesign.text).apply {
            setPadding(0, 0, dp(10), 0)
            alpha = dim
        })
        if (!summary.isNullOrBlank()) labels.addView(text(summary, 8.6f, TvDesign.muted).apply {
            setPadding(0, dp(3), dp(10), 0)
            setLineSpacing(dp(1.5f).toFloat(), 1f)
            alpha = dim
        })
        row.addView(labels, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        when {
            checked != null -> row.addView(TogglePill(context, checked, value).apply { alpha = if (enabled) 1f else .45f },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(23)))
            control == RowControl.DROPDOWN && value.isNotBlank() -> row.addView(dropdown(value, enabled),
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            value.isNotBlank() -> row.addView(text(value, 10.5f, if (enabled) TvDesign.text else TvDesign.muted).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                maxWidth = dp(170)
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = if (enabled && control == RowControl.CHEVRON) dp(4) else dp(6)
            })
        }
        if (enabled && checked == null && control == RowControl.CHEVRON) row.addView(View(context).apply {
            background = TvIcon(context, TvIcon.Symbol.CHEVRON_RIGHT, 12f, 0xFFD5DEE4.toInt(), 1.8f)
        }, LinearLayout.LayoutParams(dp(12), dp(12)).apply { marginEnd = dp(8) })
        for (index in 0 until row.childCount) {
            row.getChildAt(index).importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, dp(1.5f), 0, dp(1.5f))
        })
        return row
    }

    /** The current choice in a bordered box with a down chevron, as in a native drop-down. */
    private fun dropdown(value: String, enabled: Boolean) = row().apply {
        background = TvDesign.panel(context, 0x33061421, 0x40FFFFFF, radiusDp = 3.5f)
        setPadding(dp(12), dp(4), dp(10), dp(4))
        minimumWidth = dp(122)
        minimumHeight = dp(26)
        alpha = if (enabled) 1f else .5f
        // Long translated values wrap inside the box instead of being cut off.
        addView(text(value, 10.5f).apply {
            setPadding(0, 0, dp(6), 0)
            maxWidth = dp(190)
            minWidth = dp(122 - 12 - 10 - 10)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(View(context).apply {
            background = TvIcon(context, TvIcon.Symbol.CHEVRON_DOWN, 10f, 0xFFD5DEE4.toInt(), 1.8f)
        }, LinearLayout.LayoutParams(dp(10), dp(10)))
    }

    /** Labelled pill switch: filled teal with a bright knob when on, outlined when off. */
    private class TogglePill(context: Context, private val checked: Boolean, private val label: String) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val box = RectF()
        private val density = context.resources.displayMetrics.density
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = TvDesign.regular
            textSize = 10f * context.resources.displayMetrics.scaledDensity
            color = TvDesign.text
        }

        /** Wide enough for the translated state label; never shorter than the reference pill. */
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val height = MeasureSpec.getSize(heightMeasureSpec)
            val wanted = (11.5f * density + textPaint.measureText(label) + 8f * density + height).toInt()
            setMeasuredDimension(resolveSize(maxOf(wanted, (84 * density).toInt()), widthMeasureSpec), height)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val stroke = density
            box.set(stroke / 2, stroke / 2, width - stroke / 2, height - stroke / 2)
            val radius = box.height() / 2
            paint.style = Paint.Style.FILL
            paint.color = if (checked) TvDesign.toggleOn else 0x14FFFFFF
            canvas.drawRoundRect(box, radius, radius, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = stroke
            paint.color = if (checked) TvDesign.accentStrong else 0x59FFFFFF
            canvas.drawRoundRect(box, radius, radius, paint)
            val knob = height * .35f
            paint.style = Paint.Style.FILL
            paint.color = if (checked) 0xFFF4FBFC.toInt() else 0xFFB9C3CB.toInt()
            val knobX = if (checked) width - height / 2f - density else width - height / 2f - 2.5f * density
            canvas.drawCircle(knobX, height / 2f, knob, paint)
            val baseline = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(label, 11.5f * density, baseline, textPaint)
        }
    }

    fun field(parent: LinearLayout, label: String, value: String = "", max: Int = 100,
              type: Int = InputType.TYPE_CLASS_TEXT): EditText {
        val caption = text(label, 10f, TvDesign.muted).apply { setPadding(dp(2), dp(8), dp(2), dp(3)) }
        parent.addView(caption)
        return EditText(context).apply {
            id = View.generateViewId()
            caption.labelFor = id
            setText(value)
            textSize = 12f
            setTextColor(TvDesign.text)
            typeface = TvDesign.regular
            setHintTextColor(TvDesign.muted)
            inputType = type or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            isSingleLine = true
            filters = arrayOf(InputFilter.LengthFilter(max))
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            isSaveEnabled = false
            tag = "field_$label"
            setPadding(dp(12), dp(4), dp(12), dp(4))
            background = TvDesign.control(context, base = 0x33061421, stroke = 0x40FFFFFF, radiusDp = 4f)
            backgroundTintList = null
            highlightColor = 0x665CCBDB
            parent.addView(this, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(34)))
        }
    }
    fun announce(view: TextView, message: String) {
        view.text = message
        view.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
    }
}
