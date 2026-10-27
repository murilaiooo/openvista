package com.openvista.prototype.ambient

/** TV-safe geometry for two moving, unboxed overlays. No Android dependencies. */
internal object AmbientLayoutPolicy {
    data class Size(val width: Int, val height: Int)
    data class Position(val x: Int, val y: Int)

    fun layout(
        screenWidth: Int,
        screenHeight: Int,
        safeX: Int,
        safeY: Int,
        camera: Size,
        viewer: Size,
        cameraCorner: Int,
    ): Pair<Position, Position>? {
        if (screenWidth <= 0 || screenHeight <= 0 || safeX < 0 || safeY < 0 ||
            camera.width < 0 || camera.height < 0 || viewer.width <= 0 || viewer.height <= 0) return null
        val availableW = screenWidth - safeX * 2
        val availableH = screenHeight - safeY * 2
        if (availableW < 1 || availableH < 1 ||
            camera.width > availableW || camera.height > availableH ||
            viewer.width > availableW || viewer.height > availableH) return null

        fun cornerPosition(size: Size, slot: Int) = Position(
            if (slot % 2 == 1) screenWidth - safeX - size.width else safeX,
            if (slot >= 2) screenHeight - safeY - size.height else safeY,
        )

        val cameraSlot = cameraCorner.coerceIn(0, 3)
        val cameraPosition = cornerPosition(camera, cameraSlot)
        val viewerPosition = cornerPosition(viewer, cameraSlot xor 3)

        val cameraVisible = camera.width > 0 && camera.height > 0
        val intersect = cameraVisible &&
            cameraPosition.x < viewerPosition.x + viewer.width &&
            cameraPosition.x + camera.width > viewerPosition.x &&
            cameraPosition.y < viewerPosition.y + viewer.height &&
            cameraPosition.y + camera.height > viewerPosition.y
        return if (intersect) null else cameraPosition to viewerPosition
    }
}
