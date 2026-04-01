package com.trendhive.arsample.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.presentation.ui.components.ImportDialog
import com.trendhive.arsample.presentation.viewmodel.ObjectListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    objectListViewModel: ObjectListViewModel,
    onStartAR: () -> Unit = {}
) {
    val uiState by objectListViewModel.uiState.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Sample") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showImportDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Object")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.objects.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No 3D models imported yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap + to import a 3D model")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.objects) { obj ->
                        ListItem(
                            headlineContent = { Text(obj.name) },
                            supportingContent = { Text(obj.modelType.name) },
                            trailingContent = {
                                IconButton(onClick = { objectListViewModel.deleteObject(obj.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
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
        }

        if (showImportDialog) {
            ImportDialog(
                onDismiss = { showImportDialog = false },
                onConfirm = { name, type ->
                    objectListViewModel.importObject("/sample/path.glb", name, type)
                    showImportDialog = false
                }
            )
        }
    }
}
