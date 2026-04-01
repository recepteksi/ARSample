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
    var showObjectList by remember { mutableStateOf(false) }
    var showPlacedObjects by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImport by remember {
        mutableStateOf<Pair<String, com.trendhive.arsample.domain.model.ModelType>?>(null)
    }
    val selectedObject = remember(uiState.selectedObjectId, availableObjects) {
        uiState.selectedObjectId?.let { id -> availableObjects.firstOrNull { it.id == id } }
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
                title = { Text("AR Scene") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(ArrowBackIcon, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showImportDialog = true }) {
                        Text("Import")
                    }
                    IconButton(onClick = { showObjectList = true }) {
                        Icon(MenuIcon, contentDescription = "Object List")
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
            PlatformARView(
                modifier = Modifier.fillMaxSize(),
                placedObjects = uiState.placedObjects,
                onModelPlaced = { _, x, y, z ->
                    uiState.selectedObjectId?.let { selectedId ->
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
                    text = "${uiState.placedObjects.size} objects",
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
                            text = selectedObject?.name ?: "Selected: ${selectedId.take(8)}…",
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { showObjectList = true },
                        ) {
                            Text("Objects")
                        }
                    }
                }
            }

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
            text = "Select Object",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (objects.isEmpty()) {
            Text(
                text = "No imported objects yet",
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
                            Text("Selected", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onObjectSelected(obj.id) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap an object to select it, then tap on a plane to place it.",
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
            text = "Placed Objects",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (placedObjects.isEmpty()) {
            Text(
                text = "No objects placed yet",
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
                            Text("Remove")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
