package com.trendhive.arsample.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trendhive.arsample.domain.model.CapturedPhoto
import com.trendhive.arsample.domain.model.CapturedVideo
import com.trendhive.arsample.domain.model.MediaItem
import com.trendhive.arsample.presentation.ui.components.ArrowBackIcon
import com.trendhive.arsample.presentation.ui.components.PlayArrowIcon
import com.trendhive.arsample.presentation.viewmodel.GalleryFilter
import com.trendhive.arsample.presentation.viewmodel.GalleryUiState

/**
 * Gallery screen for viewing captured photos and videos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    uiState: GalleryUiState,
    onPhotoClick: (CapturedPhoto) -> Unit,
    onVideoClick: (CapturedVideo) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onDeleteVideo: (String) -> Unit,
    onFilterChange: (GalleryFilter) -> Unit,
    onClosePreview: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteConfirmation by remember { mutableStateOf<MediaItem?>(null) }
    
    // Show error in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            onClearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(ArrowBackIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            FilterChipRow(
                currentFilter = uiState.filter,
                photoCount = uiState.photoCount,
                videoCount = uiState.videoCount,
                onFilterChange = onFilterChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.filteredMedia.isEmpty() -> {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.isEmpty -> {
                        EmptyState(
                            filter = uiState.filter,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        MediaGrid(
                            items = uiState.filteredMedia,
                            onItemClick = { item ->
                                when (item) {
                                    is MediaItem.Photo -> onPhotoClick(item.photo)
                                    is MediaItem.Video -> onVideoClick(item.video)
                                }
                            },
                            onDeleteClick = { item ->
                                deleteConfirmation = item
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Loading overlay for delete operations
                if (uiState.isLoading && uiState.filteredMedia.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
        
        // Full screen preview
        AnimatedVisibility(
            visible = uiState.isPreviewVisible && uiState.selectedMedia != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            uiState.selectedMedia?.let { media ->
                FullScreenPreview(
                    media = media,
                    onClose = onClosePreview,
                    onDelete = {
                        when (media) {
                            is MediaItem.Photo -> onDeletePhoto(media.id)
                            is MediaItem.Video -> onDeleteVideo(media.id)
                        }
                    }
                )
            }
        }
        
        // Delete confirmation dialog
        deleteConfirmation?.let { media ->
            DeleteConfirmationDialog(
                mediaType = when (media) {
                    is MediaItem.Photo -> "photo"
                    is MediaItem.Video -> "video"
                },
                onConfirm = {
                    when (media) {
                        is MediaItem.Photo -> onDeletePhoto(media.id)
                        is MediaItem.Video -> onDeleteVideo(media.id)
                    }
                    deleteConfirmation = null
                },
                onDismiss = { deleteConfirmation = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipRow(
    currentFilter: GalleryFilter,
    photoCount: Int,
    videoCount: Int,
    onFilterChange: (GalleryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == GalleryFilter.ALL,
            onClick = { onFilterChange(GalleryFilter.ALL) },
            label = { Text("All (${photoCount + videoCount})") },
            leadingIcon = {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        FilterChip(
            selected = currentFilter == GalleryFilter.PHOTOS,
            onClick = { onFilterChange(GalleryFilter.PHOTOS) },
            label = { Text("Photos ($photoCount)") },
            leadingIcon = {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        FilterChip(
            selected = currentFilter == GalleryFilter.VIDEOS,
            onClick = { onFilterChange(GalleryFilter.VIDEOS) },
            label = { Text("Videos ($videoCount)") },
            leadingIcon = {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

@Composable
private fun MediaGrid(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    onDeleteClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items, key = { it.id }) { item ->
            MediaGridItem(
                item = item,
                onClick = { onItemClick(item) },
                onDeleteClick = { onDeleteClick(item) }
            )
        }
    }
}

@Composable
private fun MediaGridItem(
    item: MediaItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail placeholder (platform-specific image loading would go here)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item) {
                        is MediaItem.Photo -> Icons.Default.Image
                        is MediaItem.Video -> Icons.Default.Videocam
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Video duration badge
            if (item is MediaItem.Video) {
                VideoDurationBadge(
                    durationMs = item.video.durationMs,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }
            
            // Delete button (top-right corner)
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoDurationBadge(
    durationMs: Long,
    modifier: Modifier = Modifier
) {
    val seconds = (durationMs / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val formattedDuration = if (minutes > 0) {
        "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
    } else {
        "0:${remainingSeconds.toString().padStart(2, '0')}"
    }
    
    Row(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.7f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            PlayArrowIcon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = formattedDuration,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun EmptyState(
    filter: GalleryFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when (filter) {
                GalleryFilter.ALL -> "No media yet"
                GalleryFilter.PHOTOS -> "No photos yet"
                GalleryFilter.VIDEOS -> "No videos yet"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Capture photos and videos from your AR scenes to see them here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading media...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FullScreenPreview(
    media: MediaItem,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Media display (placeholder)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when (media) {
                        is MediaItem.Photo -> Icons.Default.Image
                        is MediaItem.Video -> Icons.Default.Videocam
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = when (media) {
                        is MediaItem.Photo -> "Photo Preview"
                        is MediaItem.Video -> "Video Preview"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                
                // Video play button
                if (media is MediaItem.Video) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable { /* TODO: Play video */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PlayArrowIcon,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
        
        // Top bar with close and delete buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    mediaType: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $mediaType?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
