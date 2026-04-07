package com.trendhive.arsample.infrastructure.persistence.local

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.trendhive.arsample.domain.exception.StorageException
import com.trendhive.arsample.domain.model.CapturedPhoto
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Android implementation of MediaRepository.
 * Uses MediaStore API for Android 10+ and direct file access for older versions.
 */
class MediaRepositoryImpl(
    private val context: Context
) : MediaRepository {
    
    companion object {
        private const val APP_SUBFOLDER = "ARSample"
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
}
