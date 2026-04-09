package com.trendhive.arsample.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.presentation.ui.components.AddIcon
import com.trendhive.arsample.presentation.ui.components.DeleteIcon
import com.trendhive.arsample.presentation.ui.components.ImportDialog
import com.trendhive.arsample.presentation.platform.rememberModelFilePicker
import com.trendhive.arsample.presentation.viewmodel.ObjectListUiState
import org.jetbrains.compose.resources.stringResource
import arsample.composeapp.generated.resources.Res
import arsample.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectListScreen(
    uiState: ObjectListUiState,
    onObjectSelected: (String) -> Unit,
    onStartAR: () -> Unit,
    onImportClick: (uri: String, name: String, type: com.trendhive.arsample.domain.model.ModelType) -> Unit,
    onDeleteObject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImport by remember {
        mutableStateOf<Pair<String, com.trendhive.arsample.domain.model.ModelType>?>(null)
    }

    val launchPicker = rememberModelFilePicker { uri ->
        val (name, type) = pendingImport ?: return@rememberModelFilePicker
        pendingImport = null
        onImportClick(uri, name, type)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${stringResource(Res.string.app_name)} - ${stringResource(Res.string.my_objects)}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showImportDialog = true }) {
                Icon(AddIcon, contentDescription = stringResource(Res.string.import))
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.objects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(Res.string.no_objects_yet),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(Res.string.tap_to_import),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.objects) { obj ->
                        ObjectListItem(
                            obj = obj,
                            onClick = { onObjectSelected(obj.id) },
                            onDelete = { onDeleteObject(obj.id) }
                        )
                    }
                }

                Button(
                    onClick = onStartAR,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(Res.string.start_ar))
                }
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { /* clear error */ }) {
                            Text(stringResource(Res.string.dismiss))
                        }
                    }
                ) {
                    Text(error)
                }
            }
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

@Composable
fun ObjectListItem(
    obj: ARObject,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or icon
            ObjectThumbnailItem(
                thumbnailUri = obj.thumbnailUri,
                modelType = obj.modelType,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = obj.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = obj.modelType.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    DeleteIcon,
                    contentDescription = stringResource(Res.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Displays a thumbnail for a 3D object in the object list.
 * Shows a custom thumbnail image if available, otherwise displays
 * a model-type specific icon.
 */
@Composable
private fun ObjectThumbnailItem(
    thumbnailUri: String?,
    modelType: ModelType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // For now, use placeholder icons based on model type
        // TODO: When thumbnailUri is available, load actual thumbnail image
        Icon(
            imageVector = Icons.Default.ViewInAr,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
