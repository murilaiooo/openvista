package com.openvista.diagnostics

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaCodecList
import android.os.Build
import android.os.StatFs
import android.view.Display

internal data class VideoDecoder(
    val name: String,
    val mimeType: String,
    val supports1080p30: Boolean?,
    val supports4k30: Boolean?,
    val supports4k60: Boolean?
)

internal data class ScreenMode(val width: Int, val height: Int, val refreshRateHz: Double)

internal data class DeviceSnapshot(
    val capturedAtUtc: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val supportedAbis: List<String>,
    val cpuCores: Int,
    val displayModes: List<ScreenMode>,
    val currentMode: ScreenMode?,
    val hdrTypes: List<String>,
    val totalMemoryBytes: Long?,
    val availableMemoryBytes: Long?,
    val storageTotalBytes: Long?,
    val storageAvailableBytes: Long?,
    val decoders: List<VideoDecoder>,
    val documentPickerAvailable: Boolean,
    val folderPickerAvailable: Boolean,
    val savePickerAvailable: Boolean,
    val leanbackAdvertised: Boolean,
    val gamepadAdvertised: Boolean,
    val warnings: List<String>
)

internal class DeviceInspector(private val activity: Activity) {
    fun inspect(): DeviceSnapshot {
        val warnings = mutableListOf<String>()
        val display = activity.windowManager.defaultDisplay
        val modes = try {
            display.supportedModes.map { ScreenMode(it.physicalWidth, it.physicalHeight, it.refreshRate.toDouble()) }
        } catch (error: RuntimeException) {
            warnings += "Display modes unavailable"
            emptyList()
        }
        val currentMode = try {
            val mode = display.mode
            ScreenMode(mode.physicalWidth, mode.physicalHeight, mode.refreshRate.toDouble())
        } catch (error: RuntimeException) {
            null
        }
        val hdrTypes = try {
            display.hdrCapabilities.supportedHdrTypes.map { type ->
                when (type) {
                    Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
                    Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
                    Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
                    Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
                    else -> "Unknown HDR type"
                }
            }.distinct()
        } catch (error: RuntimeException) {
            warnings += "HDR support unavailable"
            emptyList()
        }
        val memory = try {
            val result = ActivityManager.MemoryInfo()
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(result)
            result
        } catch (error: RuntimeException) {
            warnings += "Memory information unavailable"
            null
        }
        val storage = try {
            StatFs(activity.filesDir.absolutePath)
        } catch (error: RuntimeException) {
            warnings += "Storage information unavailable"
            null
        }
        val decoders = try {
            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
                .filter { !it.isEncoder }
                .flatMap { codec ->
                    codec.supportedTypes.filter { it.startsWith("video/") }.map { mime ->
                        val caps = try {
                            codec.getCapabilitiesForType(mime).videoCapabilities
                        } catch (error: Exception) {
                            null
                        }
                        fun supports(width: Int, height: Int, fps: Double): Boolean? =
                            try { caps?.areSizeAndRateSupported(width, height, fps) }
                            catch (error: Exception) { null }
                        VideoDecoder(
                            name = ReportPolicy.safeLabel(codec.name),
                            mimeType = ReportPolicy.safeLabel(mime),
                            supports1080p30 = supports(1920, 1080, 30.0),
                            supports4k30 = supports(3840, 2160, 30.0),
                            supports4k60 = supports(3840, 2160, 60.0)
                        )
                    }
                }.sortedWith(compareBy({ it.mimeType }, { it.name }))
        } catch (error: Exception) {
            warnings += "Decoder inventory unavailable"
            emptyList()
        }
        val pm = activity.packageManager
        fun pickerAvailable(action: String, mimeType: String?): Boolean =
            try {
                val intent = Intent(action).addCategory(Intent.CATEGORY_OPENABLE)
                if (mimeType != null) intent.type = mimeType
                pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
            } catch (error: RuntimeException) {
                false
            }
        val treePickerAvailable = try {
            pm.resolveActivity(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), PackageManager.MATCH_DEFAULT_ONLY) != null
        } catch (error: RuntimeException) {
            false
        }
        return DeviceSnapshot(
            capturedAtUtc = ReportPolicy.utcNow(),
            manufacturer = ReportPolicy.safeLabel(Build.MANUFACTURER),
            model = ReportPolicy.safeLabel(Build.MODEL),
            androidVersion = ReportPolicy.safeLabel(Build.VERSION.RELEASE),
            apiLevel = Build.VERSION.SDK_INT,
            supportedAbis = Build.SUPPORTED_ABIS.map(ReportPolicy::safeLabel),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            displayModes = modes,
            currentMode = currentMode,
            hdrTypes = hdrTypes,
            totalMemoryBytes = memory?.totalMem,
            availableMemoryBytes = memory?.availMem,
            storageTotalBytes = storage?.totalBytes,
            storageAvailableBytes = storage?.availableBytes,
            decoders = decoders,
            documentPickerAvailable = pickerAvailable(Intent.ACTION_OPEN_DOCUMENT, "*/*"),
            folderPickerAvailable = treePickerAvailable,
            savePickerAvailable = pickerAvailable(Intent.ACTION_CREATE_DOCUMENT, "application/json"),
            leanbackAdvertised = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK),
            gamepadAdvertised = pm.hasSystemFeature(PackageManager.FEATURE_GAMEPAD),
            warnings = warnings
        )
    }
}
