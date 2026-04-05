package com.trendhive.arsample.infrastructure.persistence.mapper

import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.infrastructure.persistence.dto.PlacedObjectDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlacedObjectMapperTest {

    private val mapper = PlacedObjectMapper()

    @Test
    fun `toDTO should include createdAt`() {
        val placedObject = PlacedObject(
            objectId = "obj-123",
            arObjectId = "ar-456",
            position = Vector3(1f, 2f, 3f),
            rotation = Quaternion(0f, 0f, 0f, 1f),
            scale = 1.5f,
            createdAt = 999888777L
        )

        val dto = mapper.toDTO(placedObject)

        assertEquals(999888777L, dto.createdAt)
        assertEquals("obj-123", dto.objectId)
        assertEquals("ar-456", dto.arObjectId)
        assertEquals(1f, dto.posX)
        assertEquals(2f, dto.posY)
        assertEquals(3f, dto.posZ)
        assertEquals(1.5f, dto.scale)
    }

    @Test
    fun `toModel should restore createdAt from DTO`() {
        val dto = PlacedObjectDTO(
            objectId = "obj-123",
            arObjectId = "ar-456",
            posX = 1f,
            posY = 2f,
            posZ = 3f,
            rotX = 0f,
            rotY = 0f,
            rotZ = 0f,
            rotW = 1f,
            scale = 1.5f,
            createdAt = 999888777L
        )

        val model = mapper.toModel(dto)

        assertEquals(999888777L, model.createdAt)
        assertEquals("obj-123", model.objectId)
        assertEquals("ar-456", model.arObjectId)
        assertEquals(1f, model.position.x)
        assertEquals(2f, model.position.y)
        assertEquals(3f, model.position.z)
        assertEquals(1.5f, model.scale)
    }

    @Test
    fun `toModel should use current timestamp for legacy DTO with createdAt=0`() {
        val dto = PlacedObjectDTO(
            objectId = "obj-123",
            arObjectId = "ar-456",
            posX = 1f,
            posY = 2f,
            posZ = 3f,
            rotX = 0f,
            rotY = 0f,
            rotZ = 0f,
            rotW = 1f,
            scale = 1.5f,
            createdAt = 0L // Legacy DTO without timestamp
        )

        val before = System.currentTimeMillis()
        val model = mapper.toModel(dto)
        val after = System.currentTimeMillis()

        // Should have generated a new timestamp for legacy data
        assertTrue(model.createdAt >= before, "createdAt should be >= before time")
        assertTrue(model.createdAt <= after, "createdAt should be <= after time")
    }

    @Test
    fun `roundtrip conversion should preserve all fields`() {
        val original = PlacedObject(
            objectId = "test-id",
            arObjectId = "ar-test-id",
            position = Vector3(1.5f, 2.5f, 3.5f),
            rotation = Quaternion(0.1f, 0.2f, 0.3f, 0.9f),
            scale = 2.0f,
            createdAt = 1234567890L
        )

        val dto = mapper.toDTO(original)
        val restored = mapper.toModel(dto)

        assertEquals(original.objectId, restored.objectId)
        assertEquals(original.arObjectId, restored.arObjectId)
        assertEquals(original.position.x, restored.position.x)
        assertEquals(original.position.y, restored.position.y)
        assertEquals(original.position.z, restored.position.z)
        assertEquals(original.rotation.x, restored.rotation.x)
        assertEquals(original.rotation.y, restored.rotation.y)
        assertEquals(original.rotation.z, restored.rotation.z)
        assertEquals(original.rotation.w, restored.rotation.w)
        assertEquals(original.scale, restored.scale)
        assertEquals(original.createdAt, restored.createdAt)
    }
}
