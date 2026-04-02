package com.trendhive.arsample.domain.model

/**
 * Represents the action to perform when a drag operation ends.
 *
 * Determines the outcome of dropping an AR object after dragging.
 */
enum class DropAction {
    /**
     * Object will be repositioned to a new valid surface location.
     * This is the default action when dropping on a detected AR plane.
     */
    REPOSITION,

    /**
     * Object will be deleted from the scene.
     * Triggered when the object is dropped on the trash zone.
     */
    DELETE,

    /**
     * Drag operation was cancelled or drop location is invalid.
     * Object returns to its original position.
     */
    CANCEL
}
