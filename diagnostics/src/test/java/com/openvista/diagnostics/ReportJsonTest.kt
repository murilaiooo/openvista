package com.openvista.diagnostics

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ReportJsonTest {
    private fun sample() = DeviceSnapshot(
        capturedAtUtc = "2026-10-25T22:00:00Z",
        manufacturer = "Amazon",
        model = "AFTHA001",
        androidVersion = "9",
        apiLevel = 28,
        supportedAbis = listOf("armeabi-v7a"),
        cpuCores = 4,
        displayModes = listOf(ScreenMode(3840, 2160, 60.0)),
        currentMode = ScreenMode(3840, 2160, 60.0),
        hdrTypes = listOf("HDR10"),
        totalMemoryBytes = 1695116000L,
        availableMemoryBytes = 305528000L,
        storageTotalBytes = 11811160064L,
        storageAvailableBytes = 7730941132L,
        decoders = listOf(VideoDecoder("OMX.MS.HEVC.Decoder", "video/hevc", true, true, null)),
        documentPickerAvailable = true,
        folderPickerAvailable = false,
        savePickerAvailable = true,
        leanbackAdvertised = true,
        gamepadAdvertised = false,
        warnings = emptyList()
    )

    @Test fun exportedReportUsesVersionedSchemaAndUnverifiedPlayback() {
        val report = JSONObject(ReportJson.encode(sample()))
        assertEquals(1, report.getInt("schemaVersion"))
        assertEquals("AFTHA001", report.getJSONObject("device").getString("model"))
        assertEquals(3840, report.getJSONObject("display").getJSONObject("currentMode").getInt("width"))
        assertTrue(report.getJSONArray("videoDecoders").getJSONObject(0).getBoolean("supports4k30"))
        assertTrue(report.getJSONArray("videoDecoders").getJSONObject(0).isNull("supports4k60"))
        assertEquals("not_tested", report.getJSONObject("checks").getString("physicalPlayback"))
        assertFalse(report.getJSONObject("pickers").getBoolean("usbEnumerationTested"))
    }

    @Test fun reportExcludesIdentifiersUrisAndPersonalContent() {
        val encoded = ReportJson.encode(sample())
        val report = JSONObject(encoded)
        val device = report.getJSONObject("device")
        for (name in listOf("serial", "fingerprint", "androidId", "ipAddress", "macAddress", "accounts")) {
            assertFalse(name, device.has(name))
            assertFalse(name, report.has(name))
        }
        for (fragment in listOf("content://", "file://", "192.168.", "mediaPath")) {
            assertFalse(fragment, encoded.contains(fragment, ignoreCase = true))
        }
    }

    @Test fun unknownCapabilitiesRemainUnknownRatherThanAssumedSupported() {
        val unknown = sample().copy(
            decoders = emptyList(), currentMode = null, totalMemoryBytes = null,
            documentPickerAvailable = false, folderPickerAvailable = false
        )
        val report = JSONObject(ReportJson.encode(unknown))
        assertTrue(report.getJSONObject("display").isNull("currentMode"))
        assertTrue(report.getJSONObject("resources").isNull("totalMemoryBytes"))
        assertEquals("unsupported", report.getJSONObject("checks").getString("codecInventory"))
        assertEquals("unsupported", report.getJSONObject("checks").getString("folderPickerResolution"))
        assertEquals(0, report.getJSONArray("videoDecoders").length())
    }

    @Test fun reportLabelsRemovePathSeparatorsAndControlCharacters() {
        assertEquals("Some_Model_private", ReportPolicy.safeLabel("Some/Model\\private"))
        assertEquals("unavailable", ReportPolicy.safeLabel(null))
        assertTrue(ReportPolicy.safeLabel("a".repeat(160)).length <= 80)
    }
}
