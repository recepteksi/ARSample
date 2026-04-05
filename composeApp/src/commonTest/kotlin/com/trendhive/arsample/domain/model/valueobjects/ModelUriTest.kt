package com.trendhive.arsample.domain.model.valueobjects

import com.trendhive.arsample.domain.exception.ValidationException
import kotlin.test.Test
import kotlin.test.assertTrue

class ModelUriTest {

    @Test
    fun `should accept valid GLB file path`() {
        val result = ModelUri.create("/path/to/model.glb")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should accept valid USDZ file path`() {
        val result = ModelUri.create("/path/to/model.usdz")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should accept valid OBJ file path`() {
        val result = ModelUri.create("/path/to/model.obj")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should accept valid FBX file path`() {
        val result = ModelUri.create("/path/to/model.fbx")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should accept content URI`() {
        val contentUri = "content://com.android.providers.downloads.documents/document/12345"
        val result = ModelUri.create(contentUri)
        assertTrue(result.isSuccess, "Content URIs should be accepted without extension validation")
    }

    @Test
    fun `should accept file URI`() {
        val fileUri = "file:///storage/emulated/0/Download/model.glb"
        val result = ModelUri.create(fileUri)
        assertTrue(result.isSuccess, "file:// URIs should be accepted")
    }

    @Test
    fun `should accept content URI without extension in path`() {
        val contentUri = "content://media/external/downloads/12345"
        val result = ModelUri.create(contentUri)
        assertTrue(result.isSuccess, "Content URIs without extension should be accepted")
    }

    @Test
    fun `should reject blank URI`() {
        val result = ModelUri.create("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should reject whitespace URI`() {
        val result = ModelUri.create("   ")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should reject invalid extension for file paths`() {
        val result = ModelUri.create("/path/to/model.txt")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should reject file path without extension`() {
        val result = ModelUri.create("/path/to/model")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should be case insensitive for extensions`() {
        val result1 = ModelUri.create("/path/to/model.GLB")
        val result2 = ModelUri.create("/path/to/model.Glb")
        val result3 = ModelUri.create("/path/to/model.glB")
        
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertTrue(result3.isSuccess)
    }
}
