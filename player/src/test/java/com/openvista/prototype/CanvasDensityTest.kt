package com.openvista.prototype

import com.openvista.prototype.ui.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Every display maps onto the 960 x 540 dp design canvas, whatever density the TV reports. */
class CanvasDensityTest {
    private fun widthDp(width: Int, height: Int): Float = width * 160f / AppLocale.canvasDensity(width, height)!!

    @Test fun fourKUhdPanelUsesTheSameCanvasAsFullHd() {
        assertEquals(640, AppLocale.canvasDensity(3840, 2160))
        assertEquals(320, AppLocale.canvasDensity(1920, 1080))
        assertEquals(213, AppLocale.canvasDensity(1280, 720))
        assertEquals(960f, widthDp(3840, 2160), .5f)
        assertEquals(960f, widthDp(1920, 1080), .5f)
    }

    @Test fun nonSixteenByNineDisplaysKeepTheWholeCanvasVisible() {
        // 4:3 is width-bound, ultra-wide is height-bound; neither crops the 960 x 540 layout.
        val fourThree = AppLocale.canvasDensity(1440, 1080)!!
        assertEquals(240, fourThree)
        assertEquals(540f, 1080 * 160f / AppLocale.canvasDensity(2560, 1080)!!, .5f)
        assertEquals(AppLocale.canvasDensity(1920, 1080), AppLocale.canvasDensity(1080, 1920))
    }

    @Test fun emptyMetricsLeaveTheSystemDensityUntouched() {
        assertNull(AppLocale.canvasDensity(0, 1080))
    }
}
