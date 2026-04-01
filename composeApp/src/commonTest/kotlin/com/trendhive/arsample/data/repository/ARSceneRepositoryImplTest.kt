package com.trendhive.arsample.data.repository

import com.trendhive.arsample.TestDataBuilders
import com.trendhive.arsample.data.local.ARSceneDataStore
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Vector3
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ARSceneRepositoryImplTest {

    private val mockDataStore = mockk<ARSceneDataStore>()
    private val repository = ARSceneRepositoryImpl(mockDataStore)

    @Test
    fun `getAllScenes should return list from data store`() = runTest {
        val scenes = listOf(
            TestDataBuilders.createTestARScene("scene1", "Scene 1"),
            TestDataBuilders.createTestARScene("scene2", "Scene 2")
        )
        coEvery { mockDataStore.getAllScenes() } returns scenes

        val result = repository.getAllScenes()
        assertEquals(2, result.size)
        assertEquals("Scene 1", result[0].name)
    }

    @Test
    fun `getSceneById should return scene when found`() = runTest {
        val scene = TestDataBuilders.createTestARScene("scene1", "Test Scene")
        coEvery { mockDataStore.getScene("scene1") } returns scene

        val result = repository.getSceneById("scene1")
        assertEquals(scene, result)
    }

    @Test
    fun `getSceneById should return null when not found`() = runTest {
        coEvery { mockDataStore.getScene("nonexistent") } returns null

        val result = repository.getSceneById("nonexistent")
        assertNull(result)
    }

    @Test
    fun `saveScene should delegate to data store`() = runTest {
        val scene = TestDataBuilders.createTestARScene("scene1")
        coEvery { mockDataStore.saveScene(scene) } just runs

        repository.saveScene(scene)
        coVerify { mockDataStore.saveScene(scene) }
    }

    @Test
    fun `deleteScene should delegate to data store`() = runTest {
        coEvery { mockDataStore.deleteScene("scene1") } just runs

        repository.deleteScene("scene1")
        coVerify { mockDataStore.deleteScene("scene1") }
    }

    @Test
    fun `deleteScene should clear defaultSceneId if matches`() = runTest {
        coEvery { mockDataStore.deleteScene("scene1") } just runs
        // First, create a default scene
        val defaultScene = TestDataBuilders.createTestARScene("scene1", "Default")
        coEvery { mockDataStore.getScene("scene1") } returns defaultScene

        repository.getOrCreateDefaultScene()
        repository.deleteScene("scene1")

        // Verify default was cleared by checking second getOrCreateDefaultScene creates new one
        val newScene = TestDataBuilders.createTestARScene("scene2", "New Default")
        coEvery { mockDataStore.saveScene(any()) } just runs
        coEvery { mockDataStore.getScene(any()) } returns null

        val result = repository.getOrCreateDefaultScene()
        assertTrue(result.id != "scene1")
    }

    @Test
    fun `getOrCreateDefaultScene should create new on first call`() = runTest {
        coEvery { mockDataStore.getScene(any()) } returns null
        coEvery { mockDataStore.saveScene(any()) } just runs

        val result = repository.getOrCreateDefaultScene()
        assertEquals("Default Scene", result.name)
        assertTrue(result.objects.isEmpty())
    }

    @Test
    fun `getOrCreateDefaultScene should return cached scene on second call`() = runTest {
        val defaultScene = TestDataBuilders.createTestARScene("scene1", "Default Scene")
        coEvery { mockDataStore.saveScene(any()) } just runs
        coEvery { mockDataStore.getScene("scene1") } returns defaultScene

        val first = repository.getOrCreateDefaultScene()
        val second = repository.getOrCreateDefaultScene()

        assertEquals(first.id, second.id)
    }

    @Test
    fun `addObjectToScene should add object to scene`() = runTest {
        val scene = TestDataBuilders.createTestARScene("scene1", objects = emptyList())
        val placedObj = TestDataBuilders.createTestPlacedObject("obj1")

        coEvery { mockDataStore.getScene("scene1") } returns scene
        coEvery { mockDataStore.saveScene(any()) } just runs

        val result = repository.addObjectToScene("scene1", placedObj)
        assertTrue(result.isSuccess)

        val resultScene = result.getOrNull()
        assertEquals(1, resultScene?.objects?.size)
        assertEquals("obj1", resultScene?.objects?.get(0)?.objectId)
    }

    @Test
    fun `addObjectToScene should return failure when scene not found`() = runTest {
        coEvery { mockDataStore.getScene("nonexistent") } returns null

        val placedObj = TestDataBuilders.createTestPlacedObject()
        val result = repository.addObjectToScene("nonexistent", placedObj)
        assertTrue(result.isFailure)
    }

    @Test
    fun `removeObjectFromScene should remove object from scene`() = runTest {
        val placedObj = TestDataBuilders.createTestPlacedObject("obj1")
        val scene = TestDataBuilders.createTestARScene("scene1", objects = listOf(placedObj))

        coEvery { mockDataStore.getScene("scene1") } returns scene
        coEvery { mockDataStore.saveScene(any()) } just runs

        val result = repository.removeObjectFromScene("scene1", "obj1")
        assertTrue(result.isSuccess)

        val resultScene = result.getOrNull()
        assertTrue(resultScene?.objects?.isEmpty() == true)
    }

    @Test
    fun `removeObjectFromScene should return failure when scene not found`() = runTest {
        coEvery { mockDataStore.getScene("nonexistent") } returns null

        val result = repository.removeObjectFromScene("nonexistent", "obj1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `updateObjectInScene should update existing object`() = runTest {
        val oldObj = TestDataBuilders.createTestPlacedObject("obj1", position = Vector3(0f, 0f, 0f))
        val newObj = TestDataBuilders.createTestPlacedObject("obj1", position = Vector3(1f, 1f, 1f), scale = 2f)
        val scene = TestDataBuilders.createTestARScene("scene1", objects = listOf(oldObj))

        coEvery { mockDataStore.getScene("scene1") } returns scene
        coEvery { mockDataStore.saveScene(any()) } just runs

        val result = repository.updateObjectInScene("scene1", newObj)
        assertTrue(result.isSuccess)

        val resultScene = result.getOrNull()
        assertEquals(1, resultScene?.objects?.size)
        assertEquals(Vector3(1f, 1f, 1f), resultScene?.objects?.get(0)?.position)
        assertEquals(2f, resultScene?.objects?.get(0)?.scale)
    }

    @Test
    fun `updateObjectInScene should return failure when scene not found`() = runTest {
        coEvery { mockDataStore.getScene("nonexistent") } returns null

        val obj = TestDataBuilders.createTestPlacedObject()
        val result = repository.updateObjectInScene("nonexistent", obj)
        assertTrue(result.isFailure)
    }
}
