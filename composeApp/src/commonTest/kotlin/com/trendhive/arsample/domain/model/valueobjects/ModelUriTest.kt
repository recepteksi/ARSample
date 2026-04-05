package com.trendhive.arsample.domain.model.valueobjects

import com.trendhive.arsample.domain.exception.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for ModelUri Value Object.
 * 
 * Following DDD testing principles:
 * - Test all validation rules
 * - Test edge cases thoroughly
 * - Test immutability and value equality
 * - Given-When-Then pattern
 * 
 * Coverage targets:
 * - Line Coverage: 100%
 * - Branch Coverage: 100%
 * - Method Coverage: 100%
 */
class ModelUriTest {
    
    // ==================== Content URI Tests (Android) ====================
    
    @Test
    fun `content URI from Android Downloads provider should be valid`() {
        // GIVEN
        val contentUri = "content://com.android.providers.downloads.documents/document/msf%3A1234"
        
        // WHEN
        val result = ModelUri.create(contentUri)
        
        // THEN
        assertTrue(result.isSuccess, "Content URI should be valid")
        assertEquals(contentUri, result.getOrNull()?.value)
    }
    
    @Test
    fun `content URI from MediaStore should be valid`() {
        // GIVEN
        val contentUri = "content://media/external/downloads/12345"
        
        // WHEN
        val result = ModelUri.create(contentUri)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(contentUri, result.getOrNull()?.value)
    }
    
    @Test
    fun `content URI from external storage provider should be valid`() {
        // GIVEN
        val contentUri = "content://com.android.externalstorage.documents/document/primary%3ADownload%2Fchair.glb"
        
        // WHEN
        val result = ModelUri.create(contentUri)
        
        // THEN
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(contentUri, result.getOrNull()?.value)
    }
    
    @Test
    fun `content URI with query parameters should be valid`() {
        // GIVEN
        val contentUri = "content://provider/path/123?param=value&other=data"
        
        // WHEN
        val result = ModelUri.create(contentUri)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(contentUri, result.getOrNull()?.value)
    }
    
    @Test
    fun `content URI without extension should be valid`() {
        // GIVEN - Content URIs often don't have extensions; Android resolves MIME type
        val contentUri = "content://com.google.android.apps.docs.storage/document/acc%3D1%3Bdoc%3Dencoded_id"
        
        // WHEN
        val result = ModelUri.create(contentUri)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(contentUri, result.getOrNull()?.value)
    }
    
    @Test
    fun `content URI with special characters should be valid`() {
        // GIVEN
        val contentUri = "content://provider/path/file%20with%20spaces%20and%20%E2%9C%93%20unicode"
        
        // WHEN
        val result = ModelUri.create(contentUri)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `content URI scheme is case sensitive`() {
        // GIVEN
        val upperCaseUri = "CONTENT://provider/path/file"
        
        // WHEN
        val result = ModelUri.create(upperCaseUri)
        
        // THEN - Should fail because "CONTENT://" is not recognized as content:// scheme
        assertTrue(result.isFailure)
    }
    
    // ==================== File Path Tests ====================
    
    @Test
    fun `file path with GLB extension should be valid`() {
        // GIVEN
        val filePath = "/models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(filePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `file path with USDZ extension should be valid`() {
        // GIVEN
        val filePath = "/models/table.usdz"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(filePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `file path with FBX extension should be valid`() {
        // GIVEN
        val filePath = "/models/character.fbx"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(filePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `file path with OBJ extension should be valid`() {
        // GIVEN
        val filePath = "/models/building.obj"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(filePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `file path with spaces should be valid`() {
        // GIVEN
        val filePath = "/models/modern chair v2.glb"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(filePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `file path with unicode characters should be valid`() {
        // GIVEN
        val filePath = "/models/椅子_chair_стул.glb"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(filePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `Windows-style file path should be valid`() {
        // GIVEN
        val windowsPath = "C:\\Users\\Documents\\Models\\chair.glb"
        
        // WHEN
        val result = ModelUri.create(windowsPath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(windowsPath, result.getOrNull()?.value)
    }
    
    @Test
    fun `Unix-style file path should be valid`() {
        // GIVEN
        val unixPath = "/home/user/models/table.usdz"
        
        // WHEN
        val result = ModelUri.create(unixPath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(unixPath, result.getOrNull()?.value)
    }
    
    @Test
    fun `relative file path should be valid`() {
        // GIVEN
        val relativePath = "models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(relativePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(relativePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `file path with multiple dots should validate last extension`() {
        // GIVEN
        val filePath = "/models/chair.v2.final.glb"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(filePath, result.getOrNull()?.value)
    }
    
    @Test
    fun `file path with multiple dots and invalid last extension should fail`() {
        // GIVEN
        val filePath = "/models/chair.glb.backup"
        
        // WHEN
        val result = ModelUri.create(filePath)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
    }
    
    // ==================== Edge Cases ====================
    
    @Test
    fun `empty string should fail with specific message`() {
        // GIVEN
        val emptyUri = ""
        
        // WHEN
        val result = ModelUri.create(emptyUri)
        
        // THEN
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<ValidationException>(exception)
        assertEquals("Model URI cannot be blank", exception?.message)
    }
    
    @Test
    fun `whitespace-only string should fail`() {
        // GIVEN
        val whitespaceUri = "   "
        
        // WHEN
        val result = ModelUri.create(whitespaceUri)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
        assertEquals("Model URI cannot be blank", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `tab and newline should fail`() {
        // GIVEN
        val whitespaceUri = "\t\n"
        
        // WHEN
        val result = ModelUri.create(whitespaceUri)
        
        // THEN
        assertTrue(result.isFailure)
        assertEquals("Model URI cannot be blank", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `very long URI should be valid if format correct`() {
        // GIVEN
        val longPath = "a".repeat(500)
        val longUri = "/models/$longPath.glb"
        
        // WHEN
        val result = ModelUri.create(longUri)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(longUri, result.getOrNull()?.value)
    }
    
    @Test
    fun `URI with special characters should be valid`() {
        // GIVEN
        val specialPath = "/models/@special#file\$name%.glb"
        
        // WHEN
        val result = ModelUri.create(specialPath)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `file URI scheme should be valid`() {
        // GIVEN
        val fileUri = "file:///storage/emulated/0/Download/chair.glb"
        
        // WHEN
        val result = ModelUri.create(fileUri)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(fileUri, result.getOrNull()?.value)
    }
    
    @Test
    fun `http URL with valid extension should be valid`() {
        // GIVEN - Current implementation accepts any URI with valid extension
        val httpUrl = "http://example.com/models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(httpUrl)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `https URL with valid extension should be valid`() {
        // GIVEN - Current implementation accepts any URI with valid extension
        val httpsUrl = "https://example.com/models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(httpsUrl)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `http URL without valid extension should fail`() {
        // GIVEN
        val httpUrl = "http://example.com/models/chair.png"
        
        // WHEN
        val result = ModelUri.create(httpUrl)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
    }
    
    @Test
    fun `URI with only extension should be valid`() {
        // GIVEN - Just an extension is technically a valid filename
        val onlyExtension = ".glb"
        
        // WHEN
        val result = ModelUri.create(onlyExtension)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `URI with leading spaces should be valid if extension is valid`() {
        // GIVEN - Leading spaces are part of the URI, not trimmed
        val leadingSpaces = "  /models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(leadingSpaces)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(leadingSpaces, result.getOrNull()?.value)
    }
    
    // ==================== Extension Validation ====================
    
    @Test
    fun `GLB extension case insensitive should be valid - lowercase`() {
        // GIVEN
        val uri = "/models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `GLB extension case insensitive should be valid - uppercase`() {
        // GIVEN
        val uri = "/models/chair.GLB"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `GLB extension case insensitive should be valid - mixed case`() {
        // GIVEN
        val uri = "/models/chair.GlB"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `USDZ extension case insensitive should be valid`() {
        // GIVEN
        val uri = "/models/table.USDZ"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `FBX extension case insensitive should be valid`() {
        // GIVEN
        val uri = "/models/character.FBX"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `OBJ extension case insensitive should be valid`() {
        // GIVEN
        val uri = "/models/building.OBJ"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `unsupported extension PNG should fail with message`() {
        // GIVEN
        val uri = "/models/image.png"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
        assertTrue(
            result.exceptionOrNull()?.message?.contains("Invalid model format") == true,
            "Expected error message about invalid format"
        )
    }
    
    @Test
    fun `unsupported extension JPG should fail`() {
        // GIVEN
        val uri = "/models/texture.jpg"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `unsupported extension TXT should fail`() {
        // GIVEN
        val uri = "/models/readme.txt"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `no extension should fail for file paths`() {
        // GIVEN
        val uri = "/models/chair"
        
        // WHEN
        val result = ModelUri.create(uri)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
    }
    
    @Test
    fun `error message should list supported formats`() {
        // GIVEN
        val invalidUri = "/models/file.txt"
        
        // WHEN
        val result = ModelUri.create(invalidUri)
        
        // THEN
        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message
        assertNotNull(message)
        assertTrue(message.contains(".glb"))
        assertTrue(message.contains(".usdz"))
        assertTrue(message.contains(".fbx"))
        assertTrue(message.contains(".obj"))
    }
    
    // ==================== Value Object Behavior ====================
    
    @Test
    fun `two ModelUris with same value should be equal`() {
        // GIVEN
        val uri1 = ModelUri.create("/models/chair.glb").getOrNull()
        val uri2 = ModelUri.create("/models/chair.glb").getOrNull()
        
        // WHEN / THEN
        assertNotNull(uri1)
        assertNotNull(uri2)
        assertEquals(uri1, uri2)
        assertEquals(uri1.hashCode(), uri2.hashCode())
    }
    
    @Test
    fun `two ModelUris with different values should not be equal`() {
        // GIVEN
        val uri1 = ModelUri.create("/models/chair.glb").getOrNull()
        val uri2 = ModelUri.create("/models/table.glb").getOrNull()
        
        // WHEN / THEN
        assertNotNull(uri1)
        assertNotNull(uri2)
        assertFalse(uri1 == uri2)
    }
    
    @Test
    fun `ModelUri should be immutable`() {
        // GIVEN
        val originalUri = "/models/chair.glb"
        val modelUri = ModelUri.create(originalUri).getOrNull()
        
        // WHEN
        assertNotNull(modelUri)
        val value = modelUri.value
        
        // THEN - Value should be the same (no way to modify)
        assertEquals(originalUri, value)
        assertEquals(originalUri, modelUri.value)
    }
    
    @Test
    fun `toString should contain class name and value`() {
        // GIVEN
        val uri = "/models/chair.glb"
        val modelUri = ModelUri.create(uri).getOrNull()
        
        // WHEN
        assertNotNull(modelUri)
        val string = modelUri.toString()
        
        // THEN
        assertTrue(string.contains("ModelUri"))
        assertTrue(string.contains(uri))
    }
    
    @Test
    fun `same instance should equal itself`() {
        // GIVEN
        val modelUri = ModelUri.create("/models/chair.glb").getOrNull()
        
        // WHEN / THEN
        assertNotNull(modelUri)
        assertEquals(modelUri, modelUri)
    }
    
    @Test
    fun `ModelUri should not equal null`() {
        // GIVEN
        val modelUri = ModelUri.create("/models/chair.glb").getOrNull()
        
        // WHEN / THEN
        assertNotNull(modelUri)
        assertFalse(modelUri.equals(null))
    }
    
    @Test
    fun `ModelUri should not equal different type`() {
        // GIVEN
        val modelUri = ModelUri.create("/models/chair.glb").getOrNull()
        val string = "/models/chair.glb"
        
        // WHEN / THEN
        assertNotNull(modelUri)
        assertFalse(modelUri.equals(string))
    }
    
    // ==================== Result Type Tests ====================
    
    @Test
    fun `create should return Result success for valid input`() {
        // GIVEN
        val validUri = "/models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(validUri)
        
        // THEN
        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertNotNull(result.getOrNull())
        assertEquals(null, result.exceptionOrNull())
    }
    
    @Test
    fun `create should return Result failure for invalid input`() {
        // GIVEN
        val invalidUri = "/models/file.txt"
        
        // WHEN
        val result = ModelUri.create(invalidUri)
        
        // THEN
        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertEquals(null, result.getOrNull())
        assertNotNull(result.exceptionOrNull())
    }
    
    @Test
    fun `Result getOrThrow should return value for success`() {
        // GIVEN
        val validUri = "/models/chair.glb"
        
        // WHEN
        val result = ModelUri.create(validUri)
        val modelUri = result.getOrThrow()
        
        // THEN
        assertEquals(validUri, modelUri.value)
    }
    
    @Test
    fun `Result getOrThrow should throw for failure`() {
        // GIVEN
        val invalidUri = ""
        
        // WHEN
        val result = ModelUri.create(invalidUri)
        
        // THEN
        var thrownException: Throwable? = null
        try {
            result.getOrThrow()
        } catch (e: Throwable) {
            thrownException = e
        }
        assertNotNull(thrownException)
        assertIs<ValidationException>(thrownException)
    }
}
