package com.trendhive.arsample.infrastructure.persistence.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ModelFileStorageImpl(
    private val context: Context,
    private val filesDir: File
) : ModelFileStorage {

    private val modelsDir: File
        get() = File(filesDir, "ar_models").also {
            if (!it.exists()) it.mkdirs()
        }

    override suspend fun saveModel(data: ByteArray, fileName: String): String =
        withContext(Dispatchers.IO) {
            val file = File(modelsDir, fileName)
            file.writeBytes(data)
            // SceneView loads file models reliably with a "file://" Uri string.
            Uri.fromFile(file).toString()
        }

    override suspend fun deleteModel(filePath: String): Boolean = withContext(Dispatchers.IO) {
        val uri = Uri.parse(filePath)
        when (uri.scheme) {
            "file" -> File(uri.path ?: return@withContext false).delete()
            null -> File(filePath).delete()
            else -> false
        }
    }

    override suspend fun getModel(filePath: String): ByteArray? = withContext(Dispatchers.IO) {
        val uri = Uri.parse(filePath)
        val file = when (uri.scheme) {
            "file" -> File(uri.path ?: return@withContext null)
            null -> File(filePath)
            else -> return@withContext null
        }
        if (file.exists()) file.readBytes() else null
    }

    override fun listModels(): List<String> {
        return modelsDir.listFiles()
            ?.filter { it.isFile && (it.extension == "glb" || it.extension == "gltf" || it.extension == "obj") }
            ?.map { Uri.fromFile(it).toString() }
            ?: emptyList()
    }

    override fun getModelsDirectory(): String {
        return modelsDir.absolutePath
    }

    override suspend fun readFromUri(uri: String): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
            ?: throw Exception("Failed to open URI: $uri")
    }
}
