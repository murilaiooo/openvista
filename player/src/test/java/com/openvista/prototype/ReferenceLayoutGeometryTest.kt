package com.openvista.prototype.ambient

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression protection for the reference-style four-zone display-care positions. */
class ReferenceLayoutGeometryTest {
    @Test fun referenceZonesStaySeparatedInEveryDisplayCarePhase() {
        val brand = OverlayExtent(190, 50)
        val source = OverlayExtent(485, 310)
        val viewer = OverlayExtent(720, 245)
        val forecast = OverlayExtent(835, 164)
        val panelLeft = 88
        val panelTop = 48
        val panelWidth = 1744
        val panelHeight = 984
        for (phase in 0..3) for (corner in 0..3) {
            val p = AmbientPlacement.calculate(panelLeft, panelTop, panelWidth, panelHeight,
                brand, source, viewer, forecast, phase, corner, false)
            val sourceBottom = (corner >= 2) != (phase >= 2)
            val sourceRight = (corner % 2 == 0) != (phase % 2 == 1)
            val adjustedViewer = OverlayOrigin(p.viewer.x, p.viewer.y + if (sourceBottom) 18 else -18)
            val adjustedForecast = OverlayOrigin(
                p.forecast.x + if (sourceRight) -90 else 90,
                p.forecast.y + if (sourceBottom) 16 else -16,
            )
            val regions = listOf(p.brand to brand, p.source to source,
                adjustedViewer to viewer, adjustedForecast to forecast)
            regions.forEach { (origin, extent) ->
                assertTrue("left $phase/$corner", origin.x >= panelLeft)
                assertTrue("top $phase/$corner", origin.y >= panelTop)
                assertTrue("right $phase/$corner", origin.x + extent.width <= panelLeft + panelWidth)
                assertTrue("bottom $phase/$corner", origin.y + extent.height <= panelTop + panelHeight)
            }
            for (i in regions.indices) for (j in i + 1 until regions.size) {
                val (a, aw) = regions[i]
                val (b, bw) = regions[j]
                val overlap = a.x < b.x + bw.width && b.x < a.x + aw.width &&
                    a.y < b.y + bw.height && b.y < a.y + aw.height
                assertFalse("collision of $i/$j in phase $phase corner $corner", overlap)
            }
        }
    }
}
