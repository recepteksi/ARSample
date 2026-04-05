package com.trendhive.arsample.domain.model.valueobjects

import com.trendhive.arsample.domain.exception.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for ObjectName Value Object.
 * 
 * Following DDD testing principles:
 * - Test all validation rules
 * - Test edge cases thoroughly
 * - Test immutability and value equality
 * - Given-When-Then pattern
 */
class ObjectNameTest {
    
    // ==================== Valid Name Tests ====================
    
    @Test
    fun `valid name should be created successfully`() {
        // GIVEN
        val name = "Chair"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
    }
    
    @Test
    fun `single character name should be valid`() {
        // GIVEN
        val name = "A"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
    }
    
    @Test
    fun `name with maximum length should be valid`() {
        // GIVEN - Exactly 50 characters
        val name = "a".repeat(50)
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
        assertEquals(50, result.getOrNull()?.value?.length)
    }
    
    @Test
    fun `name with spaces should be valid`() {
        // GIVEN
        val name = "Modern Chair v2"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
    }
    
    @Test
    fun `name with numbers should be valid`() {
        // GIVEN
        val name = "Chair123"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
    }
    
    @Test
    fun `name with special characters should be valid`() {
        // GIVEN
        val name = "Chair-2000_v3.1 (Modern)"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
    }
    
    @Test
    fun `name with unicode characters should be valid`() {
        // GIVEN
        val name = "椅子 Chair Стул"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
    }
    
    @Test
    fun `name with emoji should be valid`() {
        // GIVEN
        val name = "Chair 🪑"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.value)
    }
    
    // ==================== Trimming Behavior ====================
    
    @Test
    fun `name with leading spaces should be trimmed`() {
        // GIVEN
        val name = "  Chair"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Chair", result.getOrNull()?.value)
    }
    
    @Test
    fun `name with trailing spaces should be trimmed`() {
        // GIVEN
        val name = "Chair  "
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Chair", result.getOrNull()?.value)
    }
    
    @Test
    fun `name with leading and trailing spaces should be trimmed`() {
        // GIVEN
        val name = "  Chair  "
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Chair", result.getOrNull()?.value)
    }
    
    @Test
    fun `name with leading and trailing tabs should be trimmed`() {
        // GIVEN
        val name = "\t\tChair\t\t"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Chair", result.getOrNull()?.value)
    }
    
    @Test
    fun `name with leading and trailing newlines should be trimmed`() {
        // GIVEN
        val name = "\n\nChair\n\n"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Chair", result.getOrNull()?.value)
    }
    
    @Test
    fun `name with mixed whitespace should be trimmed`() {
        // GIVEN
        val name = " \t\n Chair \n\t "
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Chair", result.getOrNull()?.value)
    }
    
    @Test
    fun `internal spaces should be preserved after trimming`() {
        // GIVEN
        val name = "  Modern  Chair  "
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Modern  Chair", result.getOrNull()?.value)
    }
    
    // ==================== Invalid Name Tests ====================
    
    @Test
    fun `empty string should fail with specific message`() {
        // GIVEN
        val name = ""
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
        assertEquals("Object name cannot be empty", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `whitespace-only string should fail`() {
        // GIVEN
        val name = "   "
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
        assertEquals("Object name cannot be empty", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `tab-only string should fail`() {
        // GIVEN
        val name = "\t\t\t"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
        assertEquals("Object name cannot be empty", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `newline-only string should fail`() {
        // GIVEN
        val name = "\n\n"
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
        assertEquals("Object name cannot be empty", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `name longer than 50 characters should fail`() {
        // GIVEN - 51 characters
        val name = "a".repeat(51)
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
        assertIs<ValidationException>(result.exceptionOrNull())
        assertEquals("Object name must be at most 50 characters", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `name with 100 characters should fail`() {
        // GIVEN
        val name = "a".repeat(100)
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("50 characters") == true)
    }
    
    @Test
    fun `trimmed name exceeding max length should fail`() {
        // GIVEN - After trim, still > 50
        val name = "  " + "a".repeat(51) + "  "
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
        assertEquals("Object name must be at most 50 characters", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `trimmed name with exactly max length should succeed`() {
        // GIVEN - After trim, exactly 50
        val name = "  " + "a".repeat(50) + "  "
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(50, result.getOrNull()?.value?.length)
    }
    
    // ==================== Boundary Tests ====================
    
    @Test
    fun `name with 49 characters should be valid`() {
        // GIVEN
        val name = "a".repeat(49)
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(49, result.getOrNull()?.value?.length)
    }
    
    @Test
    fun `name with exactly 50 characters should be valid`() {
        // GIVEN
        val name = "a".repeat(50)
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(50, result.getOrNull()?.value?.length)
    }
    
    @Test
    fun `name with 51 characters should fail`() {
        // GIVEN
        val name = "a".repeat(51)
        
        // WHEN
        val result = ObjectName.create(name)
        
        // THEN
        assertTrue(result.isFailure)
    }
    
    // ==================== Value Object Behavior ====================
    
    @Test
    fun `two ObjectNames with same value should be equal`() {
        // GIVEN
        val name1 = ObjectName.create("Chair").getOrNull()
        val name2 = ObjectName.create("Chair").getOrNull()
        
        // WHEN / THEN
        assertNotNull(name1)
        assertNotNull(name2)
        assertEquals(name1, name2)
        assertEquals(name1.hashCode(), name2.hashCode())
    }
    
    @Test
    fun `two ObjectNames with different values should not be equal`() {
        // GIVEN
        val name1 = ObjectName.create("Chair").getOrNull()
        val name2 = ObjectName.create("Table").getOrNull()
        
        // WHEN / THEN
        assertNotNull(name1)
        assertNotNull(name2)
        assertFalse(name1 == name2)
    }
    
    @Test
    fun `ObjectNames with trimmed equivalence should be equal`() {
        // GIVEN
        val name1 = ObjectName.create("  Chair  ").getOrNull()
        val name2 = ObjectName.create("Chair").getOrNull()
        
        // WHEN / THEN
        assertNotNull(name1)
        assertNotNull(name2)
        assertEquals(name1, name2)
        assertEquals(name1.hashCode(), name2.hashCode())
    }
    
    @Test
    fun `ObjectName should be immutable`() {
        // GIVEN
        val originalName = "Chair"
        val objectName = ObjectName.create(originalName).getOrNull()
        
        // WHEN
        assertNotNull(objectName)
        val value = objectName.value
        
        // THEN - Value should be the same (no way to modify)
        assertEquals(originalName, value)
        assertEquals(originalName, objectName.value)
    }
    
    @Test
    fun `toString should contain class name and value`() {
        // GIVEN
        val name = "Chair"
        val objectName = ObjectName.create(name).getOrNull()
        
        // WHEN
        assertNotNull(objectName)
        val string = objectName.toString()
        
        // THEN
        assertTrue(string.contains("ObjectName"))
        assertTrue(string.contains(name))
    }
    
    @Test
    fun `same instance should equal itself`() {
        // GIVEN
        val objectName = ObjectName.create("Chair").getOrNull()
        
        // WHEN / THEN
        assertNotNull(objectName)
        assertEquals(objectName, objectName)
    }
    
    @Test
    fun `ObjectName should not equal null`() {
        // GIVEN
        val objectName = ObjectName.create("Chair").getOrNull()
        
        // WHEN / THEN
        assertNotNull(objectName)
        assertFalse(objectName.equals(null))
    }
    
    @Test
    fun `ObjectName should not equal different type`() {
        // GIVEN
        val objectName = ObjectName.create("Chair").getOrNull()
        val string = "Chair"
        
        // WHEN / THEN
        assertNotNull(objectName)
        assertFalse(objectName.equals(string))
    }
    
    // ==================== Result Type Tests ====================
    
    @Test
    fun `create should return Result success for valid input`() {
        // GIVEN
        val validName = "Chair"
        
        // WHEN
        val result = ObjectName.create(validName)
        
        // THEN
        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertNotNull(result.getOrNull())
        assertEquals(null, result.exceptionOrNull())
    }
    
    @Test
    fun `create should return Result failure for invalid input`() {
        // GIVEN
        val invalidName = ""
        
        // WHEN
        val result = ObjectName.create(invalidName)
        
        // THEN
        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertEquals(null, result.getOrNull())
        assertNotNull(result.exceptionOrNull())
    }
    
    @Test
    fun `Result getOrThrow should return value for success`() {
        // GIVEN
        val validName = "Chair"
        
        // WHEN
        val result = ObjectName.create(validName)
        val objectName = result.getOrThrow()
        
        // THEN
        assertEquals(validName, objectName.value)
    }
    
    @Test
    fun `Result getOrThrow should throw for failure`() {
        // GIVEN
        val invalidName = ""
        
        // WHEN
        val result = ObjectName.create(invalidName)
        
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
    
    // ==================== Real-World Scenarios ====================
    
    @Test
    fun `common object names should be valid`() {
        // GIVEN
        val commonNames = listOf(
            "Chair",
            "Table",
            "Sofa",
            "Lamp",
            "Desk",
            "Modern Chair v2",
            "Office Table (Large)",
            "3D Model_2023"
        )
        
        // WHEN / THEN
        commonNames.forEach { name ->
            val result = ObjectName.create(name)
            assertTrue(result.isSuccess, "Expected '$name' to be valid")
        }
    }
    
    @Test
    fun `edge case names should be handled correctly`() {
        // GIVEN & WHEN & THEN
        
        // Valid edge cases
        assertTrue(ObjectName.create("1").isSuccess) // Single digit
        assertTrue(ObjectName.create("_").isSuccess) // Single underscore
        assertTrue(ObjectName.create("a".repeat(50)).isSuccess) // Max length
        
        // Invalid edge cases
        assertTrue(ObjectName.create("").isFailure) // Empty
        assertTrue(ObjectName.create(" ").isFailure) // Single space
        assertTrue(ObjectName.create("a".repeat(51)).isFailure) // Over max
    }
}
