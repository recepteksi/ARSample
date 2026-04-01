package com.trendhive.arsample.ar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.trendhive.arsample.domain.model.PlacedObject

@Composable
expect fun PlatformARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null
)
