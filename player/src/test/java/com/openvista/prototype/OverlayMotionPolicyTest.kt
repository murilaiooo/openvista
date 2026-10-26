package com.openvista.prototype.ambient

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OverlayMotionPolicyTest {
    @Test fun orderedModeMovesEveryMinuteWithoutKeepingAnyPanelInPlace() {
        for (corner in 0..3) {
            val next = OverlayMotionPolicy.nextCorner(corner, 1, randomized = false)
            assertNotEquals(corner, next)
            assertNotEquals(OverlayMotionPolicy.oppositeCorner(corner), OverlayMotionPolicy.oppositeCorner(next))
        }
    }
    @Test fun randomMovementNeverReturnsTheSameCorner() {
        for (corner in 0..3) for (step in 1..3) {
            val next = OverlayMotionPolicy.nextCorner(corner, step, randomized = true)
            assertNotEquals(corner, next)
            assertNotEquals(next, OverlayMotionPolicy.oppositeCorner(next))
        }
    }
    @Test fun opposingPanelsAlwaysOccupyDifferentQuadrants() {
        for (corner in 0..3) {
            assertEquals(corner, OverlayMotionPolicy.oppositeCorner(OverlayMotionPolicy.oppositeCorner(corner)))
            assertNotEquals(corner, OverlayMotionPolicy.oppositeCorner(corner))
        }
    }
}
