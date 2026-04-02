package com.trendhive.arsample.domain.model

/**
 * Value object representing 2D screen coordinates.
 *
 * Used for touch input and UI position calculations during drag operations.
 * Coordinates are in screen pixels where (0,0) is typically the top-left corner.
 *
 * @property x Horizontal position in screen pixels
 * @property y Vertical position in screen pixels
 */
data class ScreenPosition(
    val x: Float,
    val y: Float
) {
    /**
     * Calculates the Euclidean distance to another screen position.
     *
     * @param other The target position to measure distance to
     * @return Distance in screen pixels
     */
    fun distanceTo(other: ScreenPosition): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    operator fun plus(other: ScreenPosition): ScreenPosition =
        ScreenPosition(x + other.x, y + other.y)

    operator fun minus(other: ScreenPosition): ScreenPosition =
        ScreenPosition(x - other.x, y - other.y)

    companion object {
        /** Origin position at (0, 0) */
        val ZERO = ScreenPosition(0f, 0f)
    }
}
