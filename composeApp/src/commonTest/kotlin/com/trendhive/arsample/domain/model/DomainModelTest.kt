package com.trendhive.arsample.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ARObjectTest {

    @Test
    fun `should create ARObject with correct properties`() {
        val obj = ARObject(
            id = "123",
            name = "Chair",
            modelUri = "/models/chair.glb",
            modelType = ModelType.GLB
        )
        assertEquals("123", obj.id)
        assertEquals("Chair", obj.name)
        assertEquals("/models/chair.glb", obj.modelUri)
        assertEquals(ModelType.GLB, obj.modelType)
    }

    @Test
    fun `should have default createdAt timestamp`() {
        val before = System.currentTimeMillis()
        val obj = ARObject("1", "Test", "/path", ModelType.GLB)
        val after = System.currentTimeMillis()
        assertTrue(obj.createdAt in before..after)
    }

    @Test
    fun `should have correct default createdAt`() {
        val before = System.currentTimeMillis()
        val obj = ARObject(
            id = "123",
            name = "Chair",
            modelUri = "/models/chair.glb",
            modelType = ModelType.GLB
        )
        val after = System.currentTimeMillis()
        assertTrue(obj.createdAt in before..after)
    }

    @Test
    fun `lastPlacedAt should be null by default`() {
        val obj = ARObject(
            id = "123",
            name = "Chair",
            modelUri = "/models/chair.glb",
            modelType = ModelType.GLB
        )
        assertEquals(null, obj.lastPlacedAt)
    }

    @Test
    fun `should support custom lastPlacedAt`() {
        val timestamp = 1234567890L
        val obj = ARObject(
            id = "123",
            name = "Chair",
            modelUri = "/models/chair.glb",
            modelType = ModelType.GLB,
            lastPlacedAt = timestamp
        )
        assertEquals(timestamp, obj.lastPlacedAt)
    }
}

class PlacedObjectTest {

    @Test
    fun `should create PlacedObject with default values`() {
        val placed = PlacedObject(
            objectId = "p1",
            arObjectId = "/path/model.glb",
            position = Vector3(1f, 2f, 3f)
        )
        assertEquals("p1", placed.objectId)
        assertEquals(1f, placed.position.x)
        assertEquals(1f, placed.scale) // default scale
        assertEquals(Quaternion.IDENTITY, placed.rotation) // default rotation
    }

    @Test
    fun `should have createdAt timestamp`() {
        val before = System.currentTimeMillis()
        val placed = PlacedObject(
            objectId = "test",
            arObjectId = "ar-obj",
            position = Vector3(0f, 0f, 0f)
        )
        val after = System.currentTimeMillis()

        assertTrue(placed.createdAt >= before)
        assertTrue(placed.createdAt <= after)
    }

    @Test
    fun `should accept custom createdAt`() {
        val customTimestamp = 123456789L
        val placed = PlacedObject(
            objectId = "test",
            arObjectId = "ar-obj",
            position = Vector3(0f, 0f, 0f),
            createdAt = customTimestamp
        )

        assertEquals(customTimestamp, placed.createdAt)
    }

    @Test
    fun `should sort by createdAt descending for newest first ordering`() {
        val oldest = PlacedObject("1", "ar1", Vector3.ZERO, createdAt = 1000L)
        val middle = PlacedObject("2", "ar2", Vector3.ZERO, createdAt = 2000L)
        val newest = PlacedObject("3", "ar3", Vector3.ZERO, createdAt = 3000L)

        val unsorted = listOf(oldest, newest, middle)
        val sorted = unsorted.sortedByDescending { it.createdAt }

        assertEquals("3", sorted[0].objectId) // newest first
        assertEquals("2", sorted[1].objectId)
        assertEquals("1", sorted[2].objectId) // oldest last
    }
}

class Vector3Test {

    @Test
    fun `distanceTo should calculate correct distance`() {
        val v1 = Vector3(0f, 0f, 0f)
        val v2 = Vector3(3f, 4f, 0f)
        assertEquals(5f, v1.distanceTo(v2))
    }

    @Test
    fun `plus should add vectors`() {
        val v1 = Vector3(1f, 2f, 3f)
        val v2 = Vector3(4f, 5f, 6f)
        val result = v1 + v2
        assertEquals(5f, result.x)
        assertEquals(7f, result.y)
        assertEquals(9f, result.z)
    }

    @Test
    fun `minus should subtract vectors`() {
        val v1 = Vector3(5f, 7f, 9f)
        val v2 = Vector3(1f, 2f, 3f)
        val result = v1 - v2
        assertEquals(4f, result.x)
        assertEquals(5f, result.y)
        assertEquals(6f, result.z)
    }

    @Test
    fun `times should scale vector`() {
        val v = Vector3(1f, 2f, 3f)
        val result = v * 2f
        assertEquals(2f, result.x)
        assertEquals(4f, result.y)
        assertEquals(6f, result.z)
    }

    @Test
    fun `ZERO should be origin`() {
        assertEquals(0f, Vector3.ZERO.x)
        assertEquals(0f, Vector3.ZERO.y)
        assertEquals(0f, Vector3.ZERO.z)
    }
}

class QuaternionTest {

    @Test
    fun `normalize should return identity for zero quaternion`() {
        val q = Quaternion(0f, 0f, 0f, 0f)
        val normalized = q.normalize()
        assertEquals(Quaternion.IDENTITY, normalized)
    }

    @Test
    fun `conjugate should negate vector part`() {
        val q = Quaternion(1f, 2f, 3f, 4f)
        val conj = q.conjugate()
        assertEquals(-1f, conj.x)
        assertEquals(-2f, conj.y)
        assertEquals(-3f, conj.z)
        assertEquals(4f, conj.w)
    }

    @Test
    fun `IDENTITY should have w=1 and xyz=0`() {
        assertEquals(1f, Quaternion.IDENTITY.w)
        assertEquals(0f, Quaternion.IDENTITY.x)
        assertEquals(0f, Quaternion.IDENTITY.y)
        assertEquals(0f, Quaternion.IDENTITY.z)
    }
}

class ARSceneTest {

    @Test
    fun `createDefault should create scene with generated id`() {
        val scene = ARScene.createDefault()
        assertTrue(scene.id.startsWith("scene_"))
        assertEquals("Default Scene", scene.name)
        assertTrue(scene.objects.isEmpty())
    }

    @Test
    fun `should create scene with custom name`() {
        val scene = ARScene(
            id = "scene1",
            name = "My Custom Scene",
            objects = emptyList()
        )
        assertEquals("scene1", scene.id)
        assertEquals("My Custom Scene", scene.name)
        assertTrue(scene.objects.isEmpty())
    }

    @Test
    fun `should support adding objects via copy`() {
        val scene = ARScene.createDefault()
        val placed = PlacedObject("obj1", "/path", Vector3(1f, 2f, 3f))
        val updated = scene.copy(objects = scene.objects + placed)
        assertEquals(1, updated.objects.size)
        assertEquals("obj1", updated.objects[0].objectId)
    }

    @Test
    fun `should support removing objects via copy`() {
        val placed = PlacedObject("obj1", "/path", Vector3(1f, 2f, 3f))
        val scene = ARScene("scene1", "Test", listOf(placed))
        val afterRemove = scene.copy(objects = scene.objects.filter { it.objectId != "obj1" })
        assertTrue(afterRemove.objects.isEmpty())
    }

    @Test
    fun `should have default createdAt timestamp`() {
        val before = System.currentTimeMillis()
        val scene = ARScene("scene1", "Test")
        val after = System.currentTimeMillis()
        assertTrue(scene.createdAt in before..after)
    }
}

class ModelTypeTest {

    @Test
    fun `fromExtension should return correct type`() {
        assertEquals(ModelType.GLB, ModelType.fromExtension("glb"))
        assertEquals(ModelType.GLB, ModelType.fromExtension("GLB"))
        assertEquals(ModelType.GLTF, ModelType.fromExtension("gltf"))
        assertEquals(ModelType.OBJ, ModelType.fromExtension("obj"))
        assertEquals(ModelType.USDZ, ModelType.fromExtension("usdz"))
    }

    @Test
    fun `fromExtension should return null for unknown extension`() {
        assertEquals(null, ModelType.fromExtension("fbx"))
        assertEquals(null, ModelType.fromExtension("unknown"))
        assertEquals(null, ModelType.fromExtension("png"))
    }

    @Test
    fun `all ModelType values should have fromExtension mapping`() {
        for (modelType in ModelType.entries) {
            val extension = modelType.name.lowercase()
            assertEquals(modelType, ModelType.fromExtension(extension))
        }
    }
}