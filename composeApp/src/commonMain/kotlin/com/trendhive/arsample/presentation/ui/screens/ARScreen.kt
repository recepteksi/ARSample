package com.trendhive.arsample.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        val obj = uiState.selectedObjectId?.let { id -> availableObjects.firstOrNull { it.id == id } }
        println("ARScreen: selectedObjectId=${uiState.selectedObjectId}, availableObjects.size=${availableObjects.size}, selectedObject=$obj, modelUri=${obj?.modelUri}")
        obj
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Platform-specific AR View
            // CRITICAL FIX: Use currentUiState (rememberUpdatedState) instead of uiState
            // to ensure the lambda always captures the latest state value
            PlatformARView(
                modifier = Modifier.fillMaxSize(),
                placedObjects = uiState.placedObjects,
                onModelPlaced = { modelPath, x, y, z, scale ->
                    println("ARScreen: onModelPlaced - modelPath=$modelPath, selectedObjectId=${currentUiState.selectedObjectId}")
                    currentUiState.selectedObjectId?.let { selectedId ->
                        onObjectPlaced(selectedId, x, y, z)
                    }
                },
                onModelRemoved = onObjectRemoved,
                modelPathToLoad = selectedObject?.modelUri
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

            // Selected object controls
            uiState.selectedObjectId?.let { selectedId ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = selectedObject?.name ?: "${stringResource(Res.string.selected)}: ${selectedId.take(8)}…",
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { showObjectList = true },
                        ) {
                            Text(stringResource(Res.string.objects))
                        }
                    }
                }
            }

        }

        // Object selection sheet
        if (showObjectList) {
            ModalBottomSheet(
                onDismissRequest = { 
                    println("ARScreen: Modal dismissing, selectedObjectId BEFORE=${uiState.selectedObjectId}")
                    showObjectList = false 
                    println("ARScreen: Modal dismissed, selectedObjectId AFTER=${uiState.selectedObjectId}")
                }
            ) {
                AvailableObjectsList(
                    objects = availableObjects,
                    selectedObjectId = uiState.selectedObjectId,
                    onObjectSelected = {
                        println("ARScreen: Object selected in modal, id=$it")
                        onSelectObject(it)
                        showObjectList = false  // Close immediately - no delay needed
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
                    placedObjects = uiState.placedObjects,
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

@Composable
fun AvailableObjectsList(
    objects: List<ARObject>,
    selectedObjectId: String?,
    onObjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.select_object),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (objects.isEmpty()) {
            Text(
                text = stringResource(Res.string.no_imported_objects),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            objects.forEach { obj ->
                ListItem(
                    headlineContent = { Text(obj.name) },
                    supportingContent = { Text(obj.modelType.name) },
                    trailingContent = {
                        if (obj.id == selectedObjectId) {
                            Text(stringResource(Res.string.selected), color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onObjectSelected(obj.id) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.tap_to_place),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PlacedObjectsList(
    placedObjects: List<PlacedObject>,
    onRemove: (placedObjectId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.placed_objects),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (placedObjects.isEmpty()) {
            Text(
                text = stringResource(Res.string.no_placed_objects),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            placedObjects.forEach { obj ->
                ListItem(
                    headlineContent = { Text("Object ${obj.objectId.take(8)}…") },
                    supportingContent = {
                        Text("Position: (${obj.position.x}, ${obj.position.y}, ${obj.position.z})")
                    },
                    trailingContent = {
                        TextButton(onClick = { onRemove(obj.objectId) }) {
                            Text(stringResource(Res.string.remove))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
