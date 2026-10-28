package com.openvista.prototype.ambient

import com.openvista.prototype.library.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmbientPlacementTest {
    private val brand = OverlayExtent(145, 40)
    private val source = OverlayExtent(345, 230)
    private val viewer = OverlayExtent(260, 205)
    private val forecast = OverlayExtent(540, 180)

    private fun positions(phase: Int, corner: Int = 0, controls: Boolean = false) =
        AmbientPlacement.calculate(60, 40, 900, 560, brand, source, viewer, forecast,
            phase, corner, controls, insetX = 20, insetY = 18)

    @Test fun periodicMovementChangesAllFourVisualGroups() {
        val poses = (0..3).map { positions(it) }
        assertEquals(4, poses.map { it.brand }.distinct().size)
        assertEquals(4, poses.map { it.source }.distinct().size)
        assertEquals(4, poses.map { it.viewer }.distinct().size)
        assertEquals(4, poses.map { it.forecast }.distinct().size)
        assertTrue(kotlin.math.abs(poses[0].source.x - poses[1].source.x) >= 250)
        assertTrue(kotlin.math.abs(poses[1].source.y - poses[2].source.y) >= 150)
    }

    @Test fun allPhasesAndUserCornersRemainInsideSafeAreaAndDoNotOverlap() {
        for (corner in 0..3) for (phase in 0..3) {
            val p = positions(phase, corner)
            val groups = listOf(p.brand to brand, p.source to source, p.viewer to viewer, p.forecast to forecast)
            groups.forEach { (point, box) ->
                assertTrue("left edge for $corner/$phase", point.x >= 60)
                assertTrue("upper edge for $corner/$phase", point.y >= 40)
                assertTrue("right edge for $corner/$phase", point.x + box.width <= 960)
                assertTrue("lower edge for $corner/$phase", point.y + box.height <= 600)
            }
            groups.forEachIndexed { i, (a, asize) ->
                for (j in i + 1 until groups.size) {
                    val (b, bsize) = groups[j]
                    val overlap = a.x < b.x + bsize.width && b.x < a.x + asize.width &&
                        a.y < b.y + bsize.height && b.y < a.y + asize.height
                    assertFalse("overlap of $i and $j at $corner/$phase", overlap)
                }
            }
        }
    }

    @Test fun focusedPlaybackControlsAlwaysKeepInformationalSourceAboveLowerBand() {
        for (corner in 0..3) for (phase in 0..3) {
            val p = positions(phase, corner, controls = true)
            assertTrue("source must stay above playback controls", p.source.y < 200)
        }
    }

    @Test fun movementIsEnabledForNewInstallationsButRemainsUserConfigurable() {
        assertTrue(AppSettings().moveOverlay)
        assertFalse(AppSettings().copy(moveOverlay = false).moveOverlay)
    }
}
