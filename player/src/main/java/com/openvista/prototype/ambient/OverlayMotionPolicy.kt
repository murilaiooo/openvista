package com.openvista.prototype.ambient

/** Screensaver panel placement: always leave the previous corner when movement is enabled. */
internal object OverlayMotionPolicy {
    fun nextCorner(previous: Int, randomStep: Int, randomized: Boolean): Int {
        val offset = if (randomized) randomStep.coerceIn(1, 3) else 1
        return (previous.coerceIn(0, 3) + offset) % 4
    }
    fun oppositeCorner(corner: Int) = corner.coerceIn(0, 3) xor 3
}
