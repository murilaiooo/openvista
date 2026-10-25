package com.openvista.diagnostics

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class DiagnosticsActivity : Activity() {
    private lateinit var summary: LinearLayout
    private lateinit var exportButton: Button
    private var reportJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(32), dp(24), dp(32), dp(24))
            setBackgroundColor(Color.rgb(13, 22, 35))
        }
        setContentView(root)
        root.addView(text("OPENVISTA / DEVICE CHECK", 30f, Color.WHITE).apply {
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(text(
            "Local diagnostics only. Nothing is collected until you run a check. No files or network are accessed.",
            17f, Color.rgb(173, 190, 209)
        ))
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            setPadding(0, dp(16), 0, dp(16))
        }
        root.addView(actions)
        val runButton = action("Run Device Check", actions) { runCheck() }
        exportButton = action("Export JSON", actions) { confirmExport() }
        exportButton.isEnabled = false
        action("Exit", actions) { finish() }
        val scroller = ScrollView(this).apply {
            isFillViewport = true
            isFocusable = true
            setBackgroundColor(Color.rgb(22, 35, 52))
        }
        root.addView(scroller, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        summary = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(20))
        }
        scroller.addView(summary)
        section("STATUS", "No check has been run. Press Run Device Check to read advertised hardware capabilities.")
        section("PRIVACY", "Saving needs your confirmation and the system document picker. This app cannot access your media or send data.")
        runButton.requestFocus()
    }

    private fun runCheck() {
        try {
            val snapshot = DeviceInspector(this).inspect()
            reportJson = ReportJson.encode(snapshot)
            exportButton.isEnabled = true
            summary.removeAllViews()
            section("DEVICE", snapshot.manufacturer + " " + snapshot.model +
                "\nAndroid " + snapshot.androidVersion + " / API " + snapshot.apiLevel +
                "\nABIs: " + snapshot.supportedAbis.joinToString(", ") +
                "\nCPU cores available: " + snapshot.cpuCores)
            section("DISPLAY", "Active: " + formatMode(snapshot.currentMode) +
                "\nReported modes: " + snapshot.displayModes.joinToString(", ", transform = ::formatMode) +
                "\nAdvertised HDR: " + snapshot.hdrTypes.ifEmpty { listOf("Not reported") }.joinToString(", "))
            section("MEMORY AND STORAGE", "Total RAM: " + size(snapshot.totalMemoryBytes) +
                "    Available RAM: " + size(snapshot.availableMemoryBytes) +
                "\nApp partition: " + size(snapshot.storageAvailableBytes) +
                " available / " + size(snapshot.storageTotalBytes) + " total")
            section("DOCUMENT PICKERS",
                "Open file: " + availability(snapshot.documentPickerAvailable) +
                "    Open folder: " + availability(snapshot.folderPickerAvailable) +
                "    Save JSON: " + availability(snapshot.savePickerAvailable) +
                "\nUSB browsing and persistent grants: not tested")
            section("REMOTE INPUT", "Android TV / Leanback: " + availability(snapshot.leanbackAdvertised) +
                "\nGamepad feature: " + availability(snapshot.gamepadAdvertised) +
                "\nPhysical D-pad test: not tested by this report")
            val codecText = snapshot.decoders.joinToString("\n") { decoder ->
                decoder.mimeType + " / " + decoder.name +
                    "\n   1080p30: " + supported(decoder.supports1080p30) +
                    "   4K30: " + supported(decoder.supports4k30) +
                    "   4K60: " + supported(decoder.supports4k60)
            }.ifEmpty { "No advertised video decoders returned." }
            section("ADVERTISED VIDEO DECODERS (" + snapshot.decoders.size + ")", codecText)
            section("LIMITATIONS", ReportPolicy.EVIDENCE_NOTE +
                "\nSoftware/hardware classification is not exposed consistently on Android 9." +
                if (snapshot.warnings.isEmpty()) "" else "\n" + snapshot.warnings.joinToString("\n"))
            Toast.makeText(this, "Device check complete. No report saved yet.", Toast.LENGTH_LONG).show()
        } catch (error: Exception) {
            reportJson = null
            exportButton.isEnabled = false
            summary.removeAllViews()
            section("CHECK FAILED", "One or more platform APIs failed. No report was saved.")
        }
    }

    private fun confirmExport() {
        if (reportJson == null) return
        AlertDialog.Builder(this)
            .setTitle("Export a local report?")
            .setMessage("A system window will ask you where to save the JSON file. The report contains device capabilities only. It will not be uploaded.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Choose location") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("application/json")
                        .putExtra(Intent.EXTRA_TITLE, "openvista-device-check.json")
                    startActivityForResult(intent, REQUEST_EXPORT)
                } catch (error: Exception) {
                    Toast.makeText(this, "No supported save picker is available.", Toast.LENGTH_LONG).show()
                }
            }.show()
    }

    @Deprecated("Android 9 compatible document picker callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_EXPORT || resultCode != RESULT_OK) return
        val uri = data?.data
        val contents = reportJson
        if (uri == null || contents == null) {
            Toast.makeText(this, "Export cancelled or no report available.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val stream = contentResolver.openOutputStream(uri, "w")
                ?: throw IllegalStateException("No output stream")
            stream.use { it.write(contents.toByteArray(Charsets.UTF_8)) }
            Toast.makeText(this, "Report saved to the selected document.", Toast.LENGTH_LONG).show()
        } catch (error: Exception) {
            Toast.makeText(this, "Could not save the report.", Toast.LENGTH_LONG).show()
        }
    }

    private fun action(label: String, row: LinearLayout, pressed: () -> Unit): Button {
        val button = Button(this).apply {
            text = label
            textSize = 16f
            isAllCaps = false
            minHeight = dp(56)
            backgroundTintList = ColorStateList.valueOf(Color.rgb(62, 80, 96))
            setTextColor(Color.WHITE)
            setOnFocusChangeListener { _, focused ->
                backgroundTintList = ColorStateList.valueOf(
                    if (focused) Color.rgb(117, 201, 233) else Color.rgb(62, 80, 96)
                )
                setTextColor(if (focused) Color.rgb(13, 22, 35) else Color.WHITE)
            }
            setOnClickListener { pressed() }
        }
        row.addView(button, LinearLayout.LayoutParams(0, dp(64), 1f).apply {
            marginEnd = dp(12)
        })
        return button
    }

    private fun section(title: String, value: String) {
        summary.addView(text(title, 19f, Color.rgb(117, 201, 233)).apply {
            setPadding(0, dp(16), 0, dp(6))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        summary.addView(text(value, 16f, Color.rgb(229, 237, 247)))
    }

    private fun text(value: String, size: Float, color: Int): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        setLineSpacing(dp(3).toFloat(), 1.0f)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
    private fun availability(value: Boolean): String = if (value) "Advertised" else "Not resolved"
    private fun supported(value: Boolean?): String = when (value) {
        true -> "yes"
        false -> "no"
        null -> "unknown"
    }
    private fun formatMode(mode: ScreenMode?): String = if (mode == null) "Unavailable" else
        mode.width.toString() + "x" + mode.height + " @ " +
            String.format(Locale.US, "%.1f", mode.refreshRateHz) + "Hz"
    private fun size(bytes: Long?): String = if (bytes == null) "Unavailable" else
        String.format(Locale.US, "%.2f GiB", bytes / 1073741824.0)

    companion object {
        private const val REQUEST_EXPORT = 301
    }
}
