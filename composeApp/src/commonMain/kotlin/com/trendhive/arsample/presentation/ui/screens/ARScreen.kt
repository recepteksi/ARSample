package com.trendhive.arsample.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.trendhive.arsample.ar.PlatformARView
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.presentation.ui.components.ArrowBackIcon
import com.trendhive.arsample.presentation.ui.components.ImportDialog
import com.trendhive.arsample.presentation.ui.components.MenuIcon
import com.trendhive.arsample.presentation.platform.rememberModelFilePicker
import com.trendhive.arsample.presentation.viewmodel.ARUiState
import org.jetbrains.compose.resources.stringResource
import arsample.composeapp.generated.resources.Res
import arsample.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARScreen(
    uiState: ARUiState,
    availableObjects: List<ARObject>,
    onSelectObject: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onImportObject: (uri: String, name: String, type: com.trendhive.arsample.domain.model.ModelType) -> Unit,
    onObjectPlaced: (objectId: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onObjectRemoved: (placedObjectId: String) -> Unit,
    onObjectDeleted: (objectId: String) -> Unit,
    onObjectPositionChanged: (placedObjectId: String, x: Float, y: Float, z: Float) -> Unit = { _, _, _, _ -> },
    onDragStart: (objectId: String, touchX: Float, touchY: Float) -> Unit = { _, _, _ -> },
    onDragUpdate: (newX: Float, newY: Float, newZ: Float, screenX: Float, screenY: Float, isOverTrash: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // CRITICAL FIX: Use rememberUpdatedState to ensure callbacks always capture latest state
    // This prevents lambda closures from capturing stale uiState references
    val currentUiState by rememberUpdatedState(uiState)
    
    var showObjectList by remember { mutableStateOf(false) }
    var showPlacedObjects by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImport by remember {
        mutableStateOf<Pair<String, com.trendhive.arsample.domain.model.ModelType>?>(null)
    }
    val selectedObject = remember(uiState.selectedObjectId, availableObjects) {
        uiState.selectedObjectId?.let { id -> availableObjects.firstOrNull { it.id == id } }
    }

    // Drag-to-delete UI state
    var isDragging by remember { mutableStateOf(false) }
    var isOverTrashZone by remember { mutableStateOf(false) }
    var draggingObjectId by remember { mutableStateOf<String?>(null) }

    val launchPicker = rememberModelFilePicker { uri ->
        val (name, type) = pendingImport ?: return@rememberModelFilePicker
        pendingImport = null
        onImportObject(uri, name, type)
        // Auto-open object list after import so user can select.
        showObjectList = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.ar_scene)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(ArrowBackIcon, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = { showImportDialog = true }) {
                        Text(stringResource(Res.string.import))
                    }
                    IconButton(onClick = { showObjectList = true }) {
                        Icon(MenuIcon, contentDescription = stringResource(Res.string.object_list))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val density = LocalDensity.current
            val trashZoneSize = 80.dp
            val trashZonePadding = 16.dp
            val trashZoneSizePx = with(density) { trashZoneSize.toPx() }
            val trashZonePaddingPx = with(density) { trashZonePadding.toPx() }
            val screenHeightPx = with(density) { maxHeight.toPx() }
            val screenWidthPx = with(density) { maxWidth.toPx() }
            
            // Trash zone is at bottom-right: check both X and Y coordinates
            fun isOverTrashZone(screenX: Float, screenY: Float): Boolean {
                val trashLeft = screenWidthPx - trashZoneSizePx - trashZonePaddingPx
                val trashTop = screenHeightPx - trashZoneSizePx - trashZonePaddingPx
                return screenX >= trashLeft && screenY >= trashTop
            }

            // Platform-specific AR View
            // CRITICAL FIX: Use currentUiState (rememberUpdatedState) instead of uiState
            // to ensure the lambda always captures the latest state value
            PlatformARView(
                modifier = Modifier.fillMaxSize(),
                placedObjects = uiState.placedObjects,
                onModelPlaced = { modelPath, x, y, z, scale ->
                    currentUiState.selectedObjectId?.let { selectedId ->
                        onObjectPlaced(selectedId, x, y, z)
                    }
                },
                onModelRemoved = onObjectRemoved,
                modelPathToLoad = selectedObject?.modelUri,
                onObjectPositionChanged = onObjectPositionChanged,
                onDragStart = { objectId ->
                    isDragging = true
                    draggingObjectId = objectId
                    isOverTrashZone = false
                    // Call ViewModel drag start (with screen position 0,0 as placeholder)
                    onDragStart(objectId, 0f, 0f)
                },
                onDragMove = { objectId, screenX, screenY ->
                    if (draggingObjectId != objectId) return@PlatformARView
                    val isOverTrash = isOverTrashZone(screenX, screenY)
                    isOverTrashZone = isOverTrash
                    
                    // Find the current object to get its position
                    val currentObj = currentUiState.placedObjects.find { it.objectId == objectId }
                    // FIX: Always call onDragUpdate even if currentObj is not found
                    // Use default position if object not found (position will be ignored if dragging to trash)
                    val position = currentObj?.position
                    val progress = if (isOverTrash) 1f else 0f
                    
                    // Call ViewModel drag update with position (use 0,0,0 if not found)
                    onDragUpdate(
                        position?.x ?: 0f,
                        position?.y ?: 0f,
                        position?.z ?: 0f,
                        screenX,
                        screenY,
                        isOverTrash
                    )
                },
                onDragEnd = { objectId, screenX, screenY ->
                    if (draggingObjectId == objectId) {
                        // FIX: Perform final trash zone check at drag end position
                        // This ensures deletion works even if last onDragMove was missed
                        val finalIsOverTrash = isOverTrashZone(screenX, screenY)
                        if (finalIsOverTrash) {
                            // Update ViewModel state one last time before ending drag
                            val currentObj = currentUiState.placedObjects.find { it.objectId == objectId }
                            val position = currentObj?.position
                            onDragUpdate(
                                position?.x ?: 0f,
                                position?.y ?: 0f,
                                position?.z ?: 0f,
                                screenX,
                                screenY,
                                true  // Force isOverTrash = true
                            )
                        }
                        // Call ViewModel drag end (which handles trash zone logic)
                        onDragEnd()
                    }
                    isDragging = false
                    draggingObjectId = null
                    isOverTrashZone = false
                }
            )

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Error display
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }

            // Placed objects count indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "${uiState.placedObjects.size} ${stringResource(Res.string.objects)}",
                    modifier = Modifier
                        .clickable { showPlacedObjects = true }
                        .padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Selected object indicator - compact chip style
            uiState.selectedObjectId?.let { selectedId ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = selectedObject?.name ?: "${stringResource(Res.string.selected)}: ${selectedId.take(8)}…",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(Res.string.long_press_to_place),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Subtle cancel button
                        IconButton(
                            onClick = { onSelectObject(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.cancel),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            TrashZone(
                isVisible = isDragging,
                isHovered = isOverTrashZone,
                modifier = Modifier.align(Alignment.BottomEnd)
            )

        }

        // Object selection sheet
        if (showObjectList) {
            ModalBottomSheet(
                onDismissRequest = { showObjectList = false }
            ) {
                AvailableObjectsList(
                    objects = availableObjects,
                    selectedObjectId = uiState.selectedObjectId,
                    onObjectSelected = {
                        onSelectObject(it)
                        showObjectList = false
                    },
                    onObjectDeleted = { id ->
                        if (uiState.selectedObjectId == id) onSelectObject(null)
                        onObjectDeleted(id)
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (showPlacedObjects) {
            ModalBottomSheet(
                onDismissRequest = { showPlacedObjects = false }
            ) {
                PlacedObjectsList(
                    placedObjects = uiState.placedObjects.sortedByDescending { it.createdAt },
                    onRemove = {
                        onObjectRemoved(it)
                        showPlacedObjects = false
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (showImportDialog) {
            ImportDialog(
                onDismiss = { showImportDialog = false },
                onConfirm = { name, type ->
                    pendingImport = name to type
                    launchPicker()
                    showImportDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = { content() }
    )
}

@Composable
fun TrashZone(
    isVisible: Boolean,
    isHovered: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .padding(16.dp)
                .size(80.dp)
                .background(
                    color = if (isHovered)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = if (isHovered)
                        MaterialTheme.colorScheme.onError
                    else
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(if (isHovered) 32.dp else 28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHovered) "Release" else "Delete",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHovered)
                        MaterialTheme.colorScheme.onError
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun AvailableObjectsList(
    objects: List<ARObject>,
    selectedObjectId: String?,
    onObjectSelected: (String) -> Unit,
    onObjectDeleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.select_object),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (objects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.no_imported_objects),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(objects, key = { it.id }) { obj ->
                    SwipeToDeleteItem(onDelete = { onObjectDeleted(obj.id) }) {
                        ObjectListItem(
                            arObject = obj,
                            isSelected = obj.id == selectedObjectId,
                            onClick = { onObjectSelected(obj.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.tap_to_place),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ObjectListItem(
    arObject: ARObject,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = arObject.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = arObject.modelType.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PlacedObjectsList(
    placedObjects: List<PlacedObject>,
    onRemove: (placedObjectId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.placed_objects),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (placedObjects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.no_placed_objects),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(placedObjects, key = { it.objectId }) { obj ->
                    SwipeToDeleteItem(onDelete = { onRemove(obj.objectId) }) {
                        PlacedObjectListItem(obj = obj)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun PlacedObjectListItem(
    obj: PlacedObject
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Object ${obj.arObjectId.take(8)}…",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "(${obj.position.x}, ${obj.position.y}, ${obj.position.z})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
