package com.trendhive.arsample.infrastructure.persistence.repository

import com.trendhive.arsample.TestDataBuilders
import com.trendhive.arsample.infrastructure.persistence.local.ARObjectLocalDataSource
import com.trendhive.arsample.infrastructure.persistence.local.ModelFileStorage
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
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

class ARObjectRepositoryImplTest {

    private val mockLocalDataSource = mockk<ARObjectLocalDataSource>()
    private val mockFileStorage = mockk<ModelFileStorage>()
    private val repository = ARObjectRepositoryImpl(mockLocalDataSource, mockFileStorage)

    @Test
    fun `getAllObjects should return list from data source`() = runTest {
        val objects = listOf(
            TestDataBuilders.createTestARObject("1", "Chair"),
            TestDataBuilders.createTestARObject("2", "Table")
        )
        coEvery { mockLocalDataSource.getAllObjects() } returns objects

        val result = repository.getAllObjects()
        assertEquals(2, result.size)
        assertEquals("Chair", result[0].name)
    }

    @Test
    fun `getObjectById should return object when found`() = runTest {
        val obj = TestDataBuilders.createTestARObject("1", "Chair")
        coEvery { mockLocalDataSource.getObjectById("1") } returns obj

        val result = repository.getObjectById("1")
        assertEquals(obj, result)
    }

    @Test
    fun `getObjectById should return null when not found`() = runTest {
        coEvery { mockLocalDataSource.getObjectById("nonexistent") } returns null

        val result = repository.getObjectById("nonexistent")
        assertNull(result)
    }

    @Test
    fun `saveObject should delegate to data source`() = runTest {
        val obj = TestDataBuilders.createTestARObject()
        coEvery { mockLocalDataSource.saveObject(obj) } just runs

        repository.saveObject(obj)
        coVerify { mockLocalDataSource.saveObject(obj) }
    }

    @Test
    fun `deleteObject should remove file and object`() = runTest {
        val obj = TestDataBuilders.createTestARObject("1", modelUri = "/path/model.glb")
        coEvery { mockLocalDataSource.getObjectById("1") } returns obj
        coEvery { mockFileStorage.deleteModel("/path/model.glb") } returns true
        coEvery { mockLocalDataSource.deleteObject("1") } just runs

        repository.deleteObject("1")

        coVerify { mockLocalDataSource.deleteObject("1") }
        coVerify { mockFileStorage.deleteModel("/path/model.glb") }
    }

    @Test
    fun `deleteObject should not throw when object not found`() = runTest {
        coEvery { mockLocalDataSource.getObjectById("nonexistent") } returns null

        repository.deleteObject("nonexistent")
        coVerify(exactly = 0) { mockFileStorage.deleteModel(any()) }
    }

    @Test
    fun `importObject should save file and create object record`() = runTest {
        val uri = "content://test/model.glb"
        val name = "Test Model"
        val modelData = byteArrayOf(1, 2, 3, 4, 5)
        val savedPath = "/internal/uuid.glb"

        coEvery { mockFileStorage.readFromUri(uri) } returns modelData
        coEvery { mockFileStorage.saveModel(modelData, any()) } returns savedPath
        coEvery { mockLocalDataSource.saveObject(any()) } just runs

        val result = repository.importObject(uri, name, ModelType.GLB)

        assertTrue(result.isSuccess)
        val importedObj = result.getOrNull()
        assertEquals(name, importedObj?.name)
        assertEquals(savedPath, importedObj?.modelUri)
        assertEquals(ModelType.GLB, importedObj?.modelType)
    }

    @Test
    fun `importObject should return failure when file read fails`() = runTest {
        val uri = "content://test/model.glb"
        coEvery { mockFileStorage.readFromUri(uri) } throws Exception("Read failed")

        val result = repository.importObject(uri, "name", ModelType.GLB)

        assertTrue(result.isFailure)
    }

    @Test
    fun `importObject should return failure when file save fails`() = runTest {
        val uri = "content://test/model.glb"
        val modelData = byteArrayOf(1, 2, 3)

        coEvery { mockFileStorage.readFromUri(uri) } returns modelData
        coEvery { mockFileStorage.saveModel(modelData, any()) } throws Exception("Save failed")

        val result = repository.importObject(uri, "name", ModelType.GLB)

        assertTrue(result.isFailure)
    }

    @Test
    fun `importObject creates object with unique ID`() = runTest {
        val uri = "content://test/model.glb"
        val modelData = byteArrayOf(1, 2, 3)
        val savedPath = "/internal/uuid.glb"

        coEvery { mockFileStorage.readFromUri(uri) } returns modelData
        coEvery { mockFileStorage.saveModel(modelData, any()) } returns savedPath
        coEvery { mockLocalDataSource.saveObject(any()) } just runs

        val result1 = repository.importObject(uri, "Model1", ModelType.GLB)
        val result2 = repository.importObject(uri, "Model2", ModelType.GLB)

        val obj1 = result1.getOrNull()
        val obj2 = result2.getOrNull()

        assertTrue(obj1?.id != obj2?.id)
    }
}
