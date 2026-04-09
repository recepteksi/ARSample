package com.trendhive.arsample.infrastructure.persistence.local

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Surface
import com.trendhive.arsample.domain.exception.StorageException
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.CapturedPhoto
import com.trendhive.arsample.domain.model.CapturedVideo
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android implementation of MediaRepository.
 * Uses MediaStore API for Android 10+ and direct file access for older versions.
 * 
 * Video recording is managed through a surface-based approach, requiring
 * the AR view to provide a recording surface via [setRecordingSurface].
 */
class MediaRepositoryImpl(
    private val context: Context
) : MediaRepository {
    
    companion object {
        private const val APP_SUBFOLDER = "ARSample"
    }
    
    // Video recording state
    private val _isRecording = AtomicBoolean(false)
    private var recordingStartTime: Long = 0L
    private var currentRecordingPath: String? = null
    
    // Callback for starting/stopping recording on the AR surface
    private var onStartRecordingCallback: ((String) -> Boolean)? = null
    private var onStopRecordingCallback: (() -> Boolean)? = null
    
    /**
     * Set callbacks for video recording.
     * The AR view should provide these to handle the actual recording.
     * @param onStart Called when startVideoRecording is invoked, receives output path, returns success
     * @param onStop Called when stopVideoRecording is invoked, returns success
     */
    fun setRecordingCallbacks(
        onStart: (String) -> Boolean,
        onStop: () -> Boolean
    ) {
        onStartRecordingCallback = onStart
        onStopRecordingCallback = onStop
    }
    
    /**
     * Clear recording callbacks (e.g., when AR view is destroyed).
     */
    fun clearRecordingCallbacks() {
        onStartRecordingCallback = null
        onStopRecordingCallback = null
    }
    
    override suspend fun savePhoto(imageData: ByteArray, filename: String): Result<CapturedPhoto> = 
        withContext(Dispatchers.IO) {
            try {
                val (filePath, width, height) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveWithMediaStore(imageData, filename)
                } else {
                    saveWithDirectAccess(imageData, filename)
                }
                
                val photo = CapturedPhoto(
                    id = UUID.randomUUID().toString(),
                    filePath = filePath,
                    timestamp = currentTimeMillis(),
                    width = width,
                    height = height
                )
                
                Result.success(photo)
            } catch (e: Exception) {
                Result.failure(StorageException("Failed to save photo: ${e.message}", e))
            }
        }
    
    /**
     * Save using MediaStore API (Android 10+).
     * Photos are saved to Pictures/ARSample folder.
     */
    private fun saveWithMediaStore(imageData: ByteArray, filename: String): Triple<String, Int, Int> {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$APP_SUBFOLDER")
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw StorageException("Failed to create MediaStore entry")
        
        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(imageData)
        } ?: throw StorageException("Failed to open output stream for MediaStore")
        
        // Get image dimensions
        val (width, height) = getImageDimensions(imageData)
        
        return Triple(uri.toString(), width, height)
    }
    
    /**
     * Save with direct file access (Android 9 and below).
     */
    @Suppress("DEPRECATION")
    private fun saveWithDirectAccess(imageData: ByteArray, filename: String): Triple<String, Int, Int> {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appDir = File(picturesDir, APP_SUBFOLDER)
        
        if (!appDir.exists() && !appDir.mkdirs()) {
            throw StorageException("Failed to create pictures directory")
        }
        
        val file = File(appDir, filename)
        file.writeBytes(imageData)
        
        // Notify media scanner for older Android versions
        android.media.MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf("image/jpeg"),
            null
        )
        
        val (width, height) = getImageDimensions(imageData)
        
        return Triple(file.absolutePath, width, height)
    }
    
    /**
     * Get image dimensions from raw JPEG data.
     */
    private fun getImageDimensions(imageData: ByteArray): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
        return Pair(options.outWidth, options.outHeight)
    }
    
    override suspend fun getPhotos(): Result<List<CapturedPhoto>> = withContext(Dispatchers.IO) {
        try {
            val photos = mutableListOf<CapturedPhoto>()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.RELATIVE_PATH
                )
                
                val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
                val selectionArgs = arrayOf("${Environment.DIRECTORY_PICTURES}/$APP_SUBFOLDER%")
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
                
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                    val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                    val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                    
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val contentUri = android.content.ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        
                        photos.add(
                            CapturedPhoto(
                                id = id.toString(),
                                filePath = contentUri.toString(),
                                timestamp = cursor.getLong(dateColumn) * 1000, // Convert to millis
                                width = cursor.getInt(widthColumn),
                                height = cursor.getInt(heightColumn)
                            )
                        )
                    }
                }
            } else {
                // Direct file access for older versions
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, APP_SUBFOLDER)
                
                if (appDir.exists()) {
                    appDir.listFiles()
                        ?.filter { it.extension.lowercase() in listOf("jpg", "jpeg") }
                        ?.sortedByDescending { it.lastModified() }
                        ?.forEach { file ->
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeFile(file.absolutePath, options)
                            
                            photos.add(
                                CapturedPhoto(
                                    id = file.name,
                                    filePath = file.absolutePath,
                                    timestamp = file.lastModified(),
                                    width = options.outWidth,
                                    height = options.outHeight
                                )
                            )
                        }
                }
            }
            
            Result.success(photos)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to load photos: ${e.message}", e))
        }
    }
    
    override suspend fun deletePhoto(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = android.content.ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toLongOrNull() ?: throw StorageException("Invalid photo ID: $id")
                )
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted == 0) {
                    return@withContext Result.failure(StorageException("Photo not found: $id"))
                }
            } else {
                // Direct file deletion for older versions
                val file = File(id)
                if (!file.exists()) {
                    return@withContext Result.failure(StorageException("Photo not found: $id"))
                }
                if (!file.delete()) {
                    return@withContext Result.failure(StorageException("Failed to delete photo: $id"))
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to delete photo: ${e.message}", e))
        }
    }
    
    // ==================== Video Operations ====================
    
    override suspend fun startVideoRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (_isRecording.get()) {
                return@withContext Result.failure(ValidationException("Video recording is already in progress"))
            }
            
            val startCallback = onStartRecordingCallback
                ?: return@withContext Result.failure(StorageException("Recording not available - AR view not configured"))
            
            // Generate output path
            val timestamp = currentTimeMillis()
            val filename = "AR_Video_$timestamp.mp4"
            val outputPath = getVideoOutputPath(filename)
            
            // Start recording via callback
            val success = startCallback(outputPath)
            if (!success) {
                return@withContext Result.failure(StorageException("Failed to start video recording"))
            }
            
            _isRecording.set(true)
            recordingStartTime = timestamp
            currentRecordingPath = outputPath
            
            Result.success(Unit)
        } catch (e: Exception) {
            _isRecording.set(false)
            currentRecordingPath = null
            Result.failure(StorageException("Failed to start video recording: ${e.message}", e))
        }
    }
    
    override suspend fun stopVideoRecording(): Result<CapturedVideo> = withContext(Dispatchers.IO) {
        try {
            if (!_isRecording.get()) {
                return@withContext Result.failure(ValidationException("No video recording in progress"))
            }
            
            val stopCallback = onStopRecordingCallback
                ?: return@withContext Result.failure(StorageException("Recording not available - AR view not configured"))
            
            val recordingPath = currentRecordingPath
                ?: return@withContext Result.failure(StorageException("Recording path not found"))
            
            // Stop recording via callback
            val success = stopCallback()
            if (!success) {
                return@withContext Result.failure(StorageException("Failed to stop video recording"))
            }
            
            _isRecording.set(false)
            
            // Calculate duration
            val durationMs = currentTimeMillis() - recordingStartTime
            
            // Get video metadata
            val (width, height, actualDuration) = getVideoMetadata(recordingPath)
            
            // Register with MediaStore if on Android 10+
            val finalPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                registerVideoWithMediaStore(recordingPath)
            } else {
                scanVideoFile(recordingPath)
                recordingPath
            }
            
            val video = CapturedVideo(
                id = UUID.randomUUID().toString(),
                filePath = finalPath,
                timestamp = recordingStartTime,
                durationMs = actualDuration ?: durationMs,
                width = width,
                height = height
            )
            
            currentRecordingPath = null
            recordingStartTime = 0L
            
            Result.success(video)
        } catch (e: Exception) {
            _isRecording.set(false)
            currentRecordingPath = null
            recordingStartTime = 0L
            Result.failure(StorageException("Failed to stop video recording: ${e.message}", e))
        }
    }
    
    override suspend fun getVideos(): Result<List<CapturedVideo>> = withContext(Dispatchers.IO) {
        try {
            val videos = mutableListOf<CapturedVideo>()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val projection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.WIDTH,
                    MediaStore.Video.Media.HEIGHT,
                    MediaStore.Video.Media.RELATIVE_PATH
                )
                
                val selection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
                val selectionArgs = arrayOf("${Environment.DIRECTORY_MOVIES}/$APP_SUBFOLDER%")
                val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
                
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                    val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                    
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val contentUri = android.content.ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        
                        videos.add(
                            CapturedVideo(
                                id = id.toString(),
                                filePath = contentUri.toString(),
                                timestamp = cursor.getLong(dateColumn) * 1000,
                                durationMs = cursor.getLong(durationColumn),
                                width = cursor.getInt(widthColumn),
                                height = cursor.getInt(heightColumn)
                            )
                        )
                    }
                }
            } else {
                // Direct file access for older versions
                @Suppress("DEPRECATION")
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val appDir = File(moviesDir, APP_SUBFOLDER)
                
                if (appDir.exists()) {
                    appDir.listFiles()
                        ?.filter { it.extension.lowercase() == "mp4" }
                        ?.sortedByDescending { it.lastModified() }
                        ?.forEach { file ->
                            val (width, height, duration) = getVideoMetadata(file.absolutePath)
                            
                            videos.add(
                                CapturedVideo(
                                    id = file.name,
                                    filePath = file.absolutePath,
                                    timestamp = file.lastModified(),
                                    durationMs = duration ?: 0L,
                                    width = width,
                                    height = height
                                )
                            )
                        }
                }
            }
            
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to load videos: ${e.message}", e))
        }
    }
    
    override suspend fun deleteVideo(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = android.content.ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toLongOrNull() ?: throw StorageException("Invalid video ID: $id")
                )
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted == 0) {
                    return@withContext Result.failure(StorageException("Video not found: $id"))
                }
            } else {
                val file = File(id)
                if (!file.exists()) {
                    return@withContext Result.failure(StorageException("Video not found: $id"))
                }
                if (!file.delete()) {
                    return@withContext Result.failure(StorageException("Failed to delete video: $id"))
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to delete video: ${e.message}", e))
        }
    }
    
    override fun isRecording(): Boolean = _isRecording.get()
    
    // ==================== Video Helper Methods ====================
    
    /**
     * Get video output path based on Android version.
     */
    private fun getVideoOutputPath(filename: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use app's cache directory for temporary recording, then move to MediaStore
            File(context.cacheDir, filename).absolutePath
        } else {
            @Suppress("DEPRECATION")
            val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val appDir = File(moviesDir, APP_SUBFOLDER)
            if (!appDir.exists()) appDir.mkdirs()
            File(appDir, filename).absolutePath
        }
    }
    
    /**
     * Get video metadata (width, height, duration) using MediaMetadataRetriever.
     */
    private fun getVideoMetadata(filePath: String): Triple<Int, Int, Long?> {
        return try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(filePath)
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                Triple(width, height, duration)
            }
        } catch (e: Exception) {
            Triple(0, 0, null)
        }
    }
    
    /**
     * Register video with MediaStore (Android 10+).
     * Moves from cache to MediaStore and returns content URI.
     */
    private fun registerVideoWithMediaStore(sourcePath: String): String {
        val sourceFile = File(sourcePath)
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/$APP_SUBFOLDER")
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw StorageException("Failed to create MediaStore entry for video")
        
        resolver.openOutputStream(uri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw StorageException("Failed to open output stream for video")
        
        // Delete temporary file
        sourceFile.delete()
        
        return uri.toString()
    }
    
    /**
     * Scan video file for older Android versions.
     */
    private fun scanVideoFile(filePath: String) {
        android.media.MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf("video/mp4"),
            null
        )
    }
}
