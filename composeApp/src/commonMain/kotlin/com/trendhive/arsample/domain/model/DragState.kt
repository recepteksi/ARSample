package com.trendhive.arsample.domain.model

/**
 * Sealed class representing the state machine for drag operations on AR objects.
 *
 * Drag operations progress through these states:
 * 1. [Idle] - No drag in progress
 * 2. [Detecting] - Touch detected, waiting to confirm drag intent (vs tap)
 * 3. [Dragging] - Active drag in progress, object following touch
 * 4. [Dropping] - Touch released, determining final action
 *
 * This state machine enables:
 * - Distinguishing between tap (select) and drag (move) gestures
 * - Smooth drag-to-delete with trash zone feedback
 * - Cancellation and position restoration
 */
sealed class DragState {

    /**
     * No drag operation is in progress.
     * This is the default state when no object is being manipulated.
     */
    data object Idle : DragState()

    /**
     * Touch detected on an object, determining if this is a drag or tap.
     *
     * The system waits for either:
     * - Movement beyond threshold → transitions to [Dragging]
     * - Time threshold exceeded → transitions to [Dragging]
     * - Touch released quickly → interpreted as tap, returns to [Idle]
     *
     * @property objectId Unique identifier of the touched object
     * @property initialTouchPosition Screen position where touch began
     * @property objectStartPosition World position of the object when touch started
     * @property startTime Timestamp when touch began (for hold detection)
     */
    data class Detecting(
        val objectId: String,
        val initialTouchPosition: ScreenPosition,
        val objectStartPosition: Vector3,
        val startTime: Long
    ) : DragState()

    /**
     * Active drag operation in progress.
     *
     * The object follows the user's touch and can be dragged to:
     * - A new position on an AR surface → [DropAction.REPOSITION]
     * - The trash zone for deletion → [DropAction.DELETE]
     *
     * @property objectId Unique identifier of the object being dragged
     * @property currentPosition Current world position of the object
     * @property isOverTrashZone True if the object is currently over the trash zone
     * @property trashZoneProgress Animation progress (0.0 - 1.0) for trash zone hover effect
     */
    data class Dragging(
        val objectId: String,
        val currentPosition: Vector3,
        val isOverTrashZone: Boolean = false,
        val trashZoneProgress: Float = 0f
    ) : DragState() {
        init {
            require(trashZoneProgress in 0f..1f) {
                "trashZoneProgress must be between 0.0 and 1.0, was: $trashZoneProgress"
            }
        }
    }

    /**
     * Touch released, preparing to execute the final action.
     *
     * This is a transient state that triggers the appropriate action
     * and then transitions back to [Idle].
     *
     * @property objectId Unique identifier of the dropped object
     * @property finalPosition Final world position where the object was released
     * @property action The action to execute based on drop location
     */
    data class Dropping(
        val objectId: String,
        val finalPosition: Vector3,
        val action: DropAction
    ) : DragState()
}
