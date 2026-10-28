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
            val travel = 16
            val p = AmbientPlacement.calculate(panelLeft + travel, panelTop + travel, panelWidth - 2 * travel,
                panelHeight - 2 * travel, brand, source, viewer, forecast, phase, corner, false,
                drift = AmbientPlacement.drift(phase * 5 + corner, travel))
            val regions = listOf(p.brand to brand, p.source to source, p.viewer to viewer, p.forecast to forecast)
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
