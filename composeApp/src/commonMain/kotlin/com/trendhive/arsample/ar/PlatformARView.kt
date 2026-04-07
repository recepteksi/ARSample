package com.trendhive.arsample.ar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.trendhive.arsample.domain.model.PlacedObject

@Composable
expect fun PlatformARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float, scale: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null,
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit = { _, _ -> },
    onObjectPositionChanged: ((placedObjectId: String, x: Float, y: Float, z: Float) -> Unit)? = null,
    onDragStart: ((objectId: String) -> Unit)? = null,
    onDragMove: ((objectId: String, screenX: Float, screenY: Float) -> Unit)? = null,
    onDragEnd: ((objectId: String, screenX: Float, screenY: Float) -> Unit)? = null,
    captureRequest: Boolean = false,
    onCaptureComplete: ((ByteArray?) -> Unit)? = null,
    // Video recording callbacks - set by MediaRepository to enable recording
    onRecordingCallbacksReady: ((onStart: (String) -> Boolean, onStop: () -> Boolean) -> Unit)? = null,
    onRecordingCallbacksClear: (() -> Unit)? = null
)
