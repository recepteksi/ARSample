package com.trendhive.arsample.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.presentation.ui.components.AddIcon
import com.trendhive.arsample.presentation.ui.components.DeleteIcon
import com.trendhive.arsample.presentation.ui.components.ImportDialog
import com.trendhive.arsample.presentation.platform.rememberModelFilePicker
import com.trendhive.arsample.presentation.viewmodel.ObjectListUiState

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
                title = { Text("AR Sample - My Objects") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showImportDialog = true }) {
                Icon(AddIcon, contentDescription = "Import Object")
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
                            "No objects yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap + to import a 3D model",
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
                    Text("Start AR")
                }
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { /* clear error */ }) {
                            Text("Dismiss")
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
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
