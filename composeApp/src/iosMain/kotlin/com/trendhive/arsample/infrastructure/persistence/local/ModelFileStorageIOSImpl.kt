package com.trendhive.arsample.infrastructure.persistence.local

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import platform.Foundation.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.ExperimentalForeignApi
import platform.darwin.NSUInteger

@OptIn(ExperimentalForeignApi::class)
class ModelFileStorageIOSImpl : ModelFileStorage {

    private val fileManager = NSFileManager.defaultManager

    private val documentsDir: String
        get() {
            val paths = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            )
            return paths.firstOrNull() as? String ?: throw Exception("Documents directory not found")
        }

    private val modelsDir: String
        get() {
            val dir = "$documentsDir/ar_models"
            if (!fileManager.fileExistsAtPath(dir)) {
                fileManager.createDirectoryAtPath(
                    dir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
            return dir
        }

    override suspend fun saveModel(data: ByteArray, fileName: String): String = withContext(Dispatchers.Default) {
        val filePath = "$modelsDir/$fileName"
        
        // Convert ByteArray to NSData
        val nsData = data.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), data.size.toULong())
        }
        
        // Write to file
        val success = nsData?.writeToFile(filePath, atomically = true) ?: false
        if (!success) {
            throw Exception("Failed to write file: $filePath")
        }
        
        filePath
    }

    override suspend fun deleteModel(filePath: String): Boolean = withContext(Dispatchers.Default) {
        if (fileManager.fileExistsAtPath(filePath)) {
            fileManager.removeItemAtPath(filePath, error = null)
        } else {
            false
        }
    }

    override suspend fun getModel(filePath: String): ByteArray? = withContext(Dispatchers.Default) {
        if (!fileManager.fileExistsAtPath(filePath)) {
            return@withContext null
        }
        
        val nsData = NSData.dataWithContentsOfFile(filePath) ?: return@withContext null
        
        val bytes = ByteArray(nsData.length.toInt())
        if (bytes.isNotEmpty()) {
            bytes.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
            }
        }
        bytes
    }

    override fun listModels(): List<String> {
        if (!fileManager.fileExistsAtPath(modelsDir)) {
            return emptyList()
        }
        
        val files = fileManager.contentsOfDirectoryAtPath(modelsDir, error = null) as? List<*>
            ?: return emptyList()
        
        return files.mapNotNull { fileName ->
            val name = fileName as? String ?: return@mapNotNull null
            val ext = (name.split(".").lastOrNull() ?: "").lowercase()
            if (ext in listOf("glb", "usdz", "gltf")) {
                "$modelsDir/$name"
            } else {
                null
            }
        }
    }

    override fun getModelsDirectory(): String {
        return modelsDir
    }

    override suspend fun readFromUri(uri: String): ByteArray = withContext(Dispatchers.Default) {
        val url = NSURL.URLWithString(uri) ?: throw Exception("Invalid URI: $uri")
        val nsData = NSData.dataWithContentsOfURL(url) ?: throw Exception("Failed to read data from URI: $uri")
        
        val bytes = ByteArray(nsData.length.toInt())
        if (bytes.isNotEmpty()) {
            bytes.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
            }
        }
        bytes
    }
}
