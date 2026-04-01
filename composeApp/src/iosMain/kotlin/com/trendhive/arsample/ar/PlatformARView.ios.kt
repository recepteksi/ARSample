package com.trendhive.arsample.ar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.trendhive.arsample.domain.model.PlacedObject

@Composable
actual fun PlatformARView(
    modifier: Modifier,
    placedObjects: List<PlacedObject>,
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float, scale: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit,
    modelPathToLoad: String?,
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit
) {
    ARViewWrapper(
        modifier = modifier,
        placedObjects = placedObjects,
        onModelPlaced = onModelPlaced,
        onModelRemoved = onModelRemoved,
        modelPathToLoad = modelPathToLoad
    )
}
