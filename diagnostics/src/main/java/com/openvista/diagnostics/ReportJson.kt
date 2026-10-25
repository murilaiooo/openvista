package com.openvista.diagnostics

import org.json.JSONArray
import org.json.JSONObject

internal object ReportJson {
    private fun nullable(value: Any?): Any = value ?: JSONObject.NULL

    fun encode(snapshot: DeviceSnapshot): String {
        val device = JSONObject()
            .put("manufacturer", snapshot.manufacturer)
            .put("model", snapshot.model)
            .put("androidVersion", snapshot.androidVersion)
            .put("apiLevel", snapshot.apiLevel)
            .put("supportedAbis", JSONArray(snapshot.supportedAbis))
            .put("availableCpuCores", snapshot.cpuCores)

        fun mode(mode: ScreenMode): JSONObject = JSONObject()
            .put("width", mode.width)
            .put("height", mode.height)
            .put("refreshRateHz", mode.refreshRateHz)

        val display = JSONObject()
            .put("currentMode", nullable(snapshot.currentMode?.let(::mode)))
            .put("supportedModes", JSONArray().also { list -> snapshot.displayModes.forEach { list.put(mode(it)) } })
            .put("advertisedHdrTypes", JSONArray(snapshot.hdrTypes))
        val resources = JSONObject()
            .put("totalMemoryBytes", nullable(snapshot.totalMemoryBytes))
            .put("availableMemoryBytes", nullable(snapshot.availableMemoryBytes))
            .put("appPartitionTotalBytes", nullable(snapshot.storageTotalBytes))
            .put("appPartitionAvailableBytes", nullable(snapshot.storageAvailableBytes))
        val decoders = JSONArray().also { items ->
            snapshot.decoders.forEach { codec ->
                items.put(JSONObject()
                    .put("name", codec.name)
                    .put("mimeType", codec.mimeType)
                    .put("supports1080p30", nullable(codec.supports1080p30))
                    .put("supports4k30", nullable(codec.supports4k30))
                    .put("supports4k60", nullable(codec.supports4k60))
                    .put("hardwareClassification", "not_exposed_on_api_28"))
            }
        }
        val pickers = JSONObject()
            .put("openDocumentResolvable", snapshot.documentPickerAvailable)
            .put("openDocumentTreeResolvable", snapshot.folderPickerAvailable)
            .put("createDocumentResolvable", snapshot.savePickerAvailable)
            .put("usbEnumerationTested", false)
        val checks = JSONObject()
            .put("deviceInspection", "pass")
            .put("codecInventory", if (snapshot.decoders.isNotEmpty()) "pass" else "unsupported")
            .put("documentPickerResolution", if (snapshot.documentPickerAvailable) "pass" else "unsupported")
            .put("folderPickerResolution", if (snapshot.folderPickerAvailable) "pass" else "unsupported")
            .put("savePickerResolution", if (snapshot.savePickerAvailable) "pass" else "unsupported")
            .put("physicalPlayback", "not_tested")
            .put("usbImport", "not_tested")
            .put("dreamService", "not_tested")
        return JSONObject()
            .put("schemaVersion", ReportPolicy.SCHEMA_VERSION)
            .put("capturedAtUtc", snapshot.capturedAtUtc)
            .put("appVersion", BuildConfig.VERSION_NAME)
            .put("capabilityEvidence", ReportPolicy.EVIDENCE_NOTE)
            .put("device", device)
            .put("display", display)
            .put("resources", resources)
            .put("videoDecoders", decoders)
            .put("pickers", pickers)
            .put("input", JSONObject()
                .put("leanbackAdvertised", snapshot.leanbackAdvertised)
                .put("gamepadAdvertised", snapshot.gamepadAdvertised)
                .put("dpadFunctionalTest", "not_tested"))
            .put("checks", checks)
            .put("warnings", JSONArray(snapshot.warnings))
            .toString(2)
    }
}
