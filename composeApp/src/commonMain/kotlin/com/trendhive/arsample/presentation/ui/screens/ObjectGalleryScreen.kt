package com.trendhive.arsample.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.presentation.ui.components.AddIcon
import com.trendhive.arsample.presentation.ui.components.ArrowBackIcon
import com.trendhive.arsample.presentation.ui.components.DeleteIcon
import com.trendhive.arsample.presentation.ui.components.ImportDialog
import com.trendhive.arsample.presentation.ui.components.ModelPreviewThumbnail
import com.trendhive.arsample.presentation.platform.rememberModelFilePicker
import com.trendhive.arsample.presentation.viewmodel.ObjectListUiState
import org.jetbrains.compose.resources.stringResource
import arsample.composeapp.generated.resources.Res
import arsample.composeapp.generated.resources.*

/**
 * Object Gallery Screen - Displays 3D objects in a grid layout for browsing and management.
 *
 * @param uiState The current UI state containing objects and loading/error states
 * @param onObjectClick Called when a user taps an object to select it for AR placement
 * @param onObjectDelete Called when a user requests to delete an object
 * @param onImportClick Called when a user imports a new 3D model
 * @param onNavigateBack Called when user wants to go back
 * @param onNavigateToAR Called when user wants to open AR scene
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectGalleryScreen(
    uiState: ObjectListUiState,
    onObjectClick: (ARObject) -> Unit,
    onObjectDelete: (String) -> Unit,
    onImportClick: (uri: String, name: String, type: ModelType) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAR: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<Pair<String, ModelType>?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val launchPicker = rememberModelFilePicker { uri ->
        val (name, type) = pendingImport ?: return@rememberModelFilePicker
        pendingImport = null
        onImportClick(uri, name, type)
    }

    // Filter objects by search query
    val filteredObjects = remember(uiState.objects, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.objects
        } else {
            uiState.objects.filter { obj ->
                obj.name.contains(searchQuery, ignoreCase = true) ||
                obj.modelType.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.object_gallery)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(ArrowBackIcon, contentDescription = stringResource(Res.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AR FAB
                FloatingActionButton(
                    onClick = onNavigateToAR,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        Icons.Default.ViewInAr,
                        contentDescription = stringResource(Res.string.start_ar)
                    )
                }
                // Import FAB
                FloatingActionButton(
                    onClick = { showImportDialog = true }
                ) {
                    Icon(AddIcon, contentDescription = stringResource(Res.string.import))
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.objects.isEmpty() -> {
                    EmptyGalleryState(
                        onImportClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                filteredObjects.isEmpty() -> {
                    NoSearchResultsState(
                        query = searchQuery,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    ObjectGalleryGrid(
                        objects = filteredObjects,
                        onObjectClick = onObjectClick,
                        onObjectDelete = onObjectDelete,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { /* clear error handled by parent */ }) {
                            Text(stringResource(Res.string.dismiss))
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // Import Dialog
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

/**
 * Search bar component for filtering objects
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(Res.string.search_objects)) },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* Already filtering on change */ }),
        modifier = modifier
    )
}

/**
 * Grid layout displaying object cards
 */
@Composable
private fun ObjectGalleryGrid(
    objects: List<ARObject>,
    onObjectClick: (ARObject) -> Unit,
    onObjectDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(
            items = objects,
            key = { it.id }
        ) { arObject ->
            ObjectGalleryCard(
                arObject = arObject,
                onClick = { onObjectClick(arObject) },
                onDelete = { onObjectDelete(arObject.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

/**
 * Individual card displaying a 3D object with preview and info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectGalleryCard(
    arObject: ARObject,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Preview area (2/3 of card) - 3D model preview
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ModelPreviewThumbnail(
                        modelPath = arObject.modelUri,
                        modifier = Modifier.fillMaxSize(),
                        autoRotate = true
                    )
                }

                // Info area (1/3 of card)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Object name
                    Text(
                        text = arObject.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Created date
                        Text(
                            text = formatDate(arObject.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Model type badge (top-right corner)
            ModelTypeBadge(
                modelType = arObject.modelType,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )

            // Delete button (bottom-right corner)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        DeleteIcon,
                        contentDescription = stringResource(Res.string.delete),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Confirmation dropdown
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.delete)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                DeleteIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Badge showing the model file type (GLB, USDZ, etc.)
 */
@Composable
private fun ModelTypeBadge(
    modelType: ModelType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (modelType) {
        ModelType.GLB, ModelType.GLTF -> Color(0xFF4CAF50) // Green for glTF family
        ModelType.USDZ -> Color(0xFF2196F3) // Blue for Apple format
        ModelType.OBJ -> Color(0xFFFF9800) // Orange for OBJ
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = modelType.name,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Empty state when no objects have been imported yet
 */
@Composable
private fun EmptyGalleryState(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ViewInAr,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(Res.string.no_objects_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(Res.string.tap_to_import),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onImportClick) {
            Icon(AddIcon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.import_first_object))
        }
    }
}

/**
 * State shown when search yields no results
 */
@Composable
private fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.no_results_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(Res.string.no_results_for_query, query),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Format timestamp to readable date string
 */
private fun formatDate(timestamp: Long): String {
    // Simple date formatting - in production, use platform-specific formatting
    val seconds = timestamp / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}
