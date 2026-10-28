package com.openvista.prototype.ambient

import kotlin.math.roundToInt

/** The visible bounds of an overlay group, after any fit-to-safe-area scaling. */
internal data class OverlayExtent(val width: Int, val height: Int)

internal data class OverlayOrigin(val x: Int, val y: Int)

internal data class AmbientPositions(
    val brand: OverlayOrigin,
    val source: OverlayOrigin,
    val viewer: OverlayOrigin,
    val forecast: OverlayOrigin,
)

/**
 * Four distinct, non-overlapping compositions. Both information rows change sides
 * and alternate between the upper and lower safe bands. Unlike pixel shifting,
 * this moves the entire clock/forecast/logo footprint away from fixed pixels.
 * Each visit to a composition also uses a different drift offset inside a reserved
 * travel margin, so no group returns to exactly the same pixels for 32 cycles.
 *
 * This calculation is Android-independent so the safe-area contract can be tested.
 */
internal object AmbientPlacement {
    /** Distinct (composition, drift) pairs before the schedule repeats. */
    const val CYCLE_LENGTH = 32

    /** Unit drift directions: the eight compass points of the reserved travel margin. */
    private val directions = listOf(
        0f to 0f, 1f to 0f, .71f to .71f, 0f to 1f, -.71f to .71f, -1f to 0f, -.71f to -.71f, 0f to -1f,
    )

    fun phase(cycle: Int) = Math.floorMod(cycle, 4)

    /** Every composition sees all eight drift offsets: (c % 4, (c + c / 4) % 8) is unique for c < 32. */
    fun drift(cycle: Int, travel: Int): OverlayOrigin {
        val c = Math.floorMod(cycle, CYCLE_LENGTH)
        val (dx, dy) = directions[(c + c / 4) % directions.size]
        return OverlayOrigin((dx * travel).roundToInt(), (dy * travel).roundToInt())
    }

    /**
     * Places groups inside [left, top, width, height]. A non-zero [drift] moves every group
     * together; callers reserve |drift| on each side so the result stays inside the safe area.
     * [gap] is the minimum horizontal space kept between the two groups that share a row.
     */
    fun calculate(
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        brand: OverlayExtent,
        source: OverlayExtent,
        viewer: OverlayExtent,
        forecast: OverlayExtent,
        phase: Int,
        corner: Int,
        controlsVisible: Boolean,
        insetX: Int = 0,
        insetY: Int = 0,
        drift: OverlayOrigin = OverlayOrigin(0, 0),
    ): AmbientPositions {
        val sourceRight = ((corner % 2) == 0) != (phase % 2 == 1)
        // Contextual controls occupy the lower screen; never move text into them.
        val sourceBottom = !controlsVisible && ((corner >= 2) != ((phase % 4) >= 2))
        val topRowHeight = if (sourceBottom) maxOf(viewer.height, forecast.height)
            else maxOf(source.height, brand.height)
        val bottomRowHeight = if (sourceBottom) maxOf(source.height, brand.height)
            else maxOf(viewer.height, forecast.height)
        val freeHeight = (height - topRowHeight - bottomRowHeight).coerceAtLeast(0)
        // Inset is limited to available inter-row space and never pushes beyond the safe area.
        val verticalInset = insetY.coerceIn(0, freeHeight / 3)
        val upper = top + verticalInset
        val lower = top + height - bottomRowHeight - verticalInset
        val sourceRow = if (sourceBottom) lower else upper
        val infoRow = if (sourceBottom) upper else lower
        val sourceBrandGap = (width - source.width - brand.width).coerceAtLeast(0)
        val viewerForecastGap = (width - viewer.width - forecast.width).coerceAtLeast(0)
        val horizontalSourceInset = insetX.coerceIn(0, sourceBrandGap / 3)
        val horizontalInfoInset = insetX.coerceIn(0, viewerForecastGap / 3)
        val right = left + width
        val sourceX = if (sourceRight) right - source.width - horizontalSourceInset
            else left + horizontalSourceInset
        val brandX = if (sourceRight) left + horizontalSourceInset
            else right - brand.width - horizontalSourceInset
        val viewerX = if (sourceRight) left + horizontalInfoInset
            else right - viewer.width - horizontalInfoInset
        val forecastX = if (sourceRight) right - forecast.width - horizontalInfoInset
            else left + horizontalInfoInset

        // Bottom content is baseline-aligned; upper content starts at the same top edge.
        fun atRow(x: Int, extent: OverlayExtent, row: Int, rowHeight: Int): OverlayOrigin =
            OverlayOrigin(x + drift.x, (if (row == upper) row else row + rowHeight - extent.height) + drift.y)

        return AmbientPositions(
            brand = atRow(brandX, brand, sourceRow, if (sourceBottom) bottomRowHeight else topRowHeight),
            source = atRow(sourceX, source, sourceRow, if (sourceBottom) bottomRowHeight else topRowHeight),
            viewer = atRow(viewerX, viewer, infoRow, if (sourceBottom) topRowHeight else bottomRowHeight),
            forecast = atRow(forecastX, forecast, infoRow, if (sourceBottom) topRowHeight else bottomRowHeight),
        )
    }
}
