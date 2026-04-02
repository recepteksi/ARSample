package com.trendhive.arsample.domain.model

/**
 * Sealed class representing the visual states of the trash zone UI element.
 *
 * The trash zone appears during drag operations to allow object deletion.
 * It provides visual feedback as the user drags objects toward it.
 *
 * State transitions:
 * - [Hidden] → [Visible]: When a drag operation starts
 * - [Visible] → [Hover]: When dragged object enters trash zone bounds
 * - [Hover] → [Visible]: When dragged object exits trash zone bounds
 * - [Visible]/[Hover] → [Hidden]: When drag operation ends
 */
sealed class TrashZoneState {

    /**
     * Trash zone is not visible.
     * Default state when no drag operation is in progress.
     */
    data object Hidden : TrashZoneState()

    /**
     * Trash zone is visible but not being hovered.
     * Displayed during active drag operations to indicate deletion option.
     */
    data object Visible : TrashZoneState()

    /**
     * Trash zone is being hovered by a dragged object.
     *
     * The progress value controls the visual feedback animation:
     * - 0.0: Just entered the zone, minimal highlight
     * - 1.0: Fully activated, maximum visual feedback (pulsing, color change, etc.)
     *
     * @property progress Animation progress from 0.0 (entering) to 1.0 (fully activated)
     */
    data class Hover(val progress: Float) : TrashZoneState() {
        init {
            require(progress in 0f..1f) {
                "progress must be between 0.0 and 1.0, was: $progress"
            }
        }

        companion object {
            /** Minimum hover state (just entered the zone) */
            val MIN = Hover(0f)

            /** Maximum hover state (fully activated) */
            val MAX = Hover(1f)
        }
    }
}
