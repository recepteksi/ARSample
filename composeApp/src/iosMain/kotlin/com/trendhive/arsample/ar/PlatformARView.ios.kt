package com.trendhive.arsample.ar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.trendhive.arsample.domain.model.PlacedObject

@Composable
actual fun PlatformARView(
    modifier: Modifier,
    placedObjects: List<PlacedObject>,
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit,
    modelPathToLoad: String?
) {
    ARViewWrapper(
        modifier = modifier,
        placedObjects = placedObjects,
        onModelPlaced = onModelPlaced,
        onModelRemoved = onModelRemoved,
        modelPathToLoad = modelPathToLoad
    )
}
