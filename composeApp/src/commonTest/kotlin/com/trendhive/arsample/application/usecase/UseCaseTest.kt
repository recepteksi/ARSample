package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.repository.ARObjectRepository
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportObjectUseCaseTest {

    private val repository = mockk<ARObjectRepository>()
    private val useCase = ImportObjectUseCase(repository)

    @Test
    fun `should return failure when uri is blank`() = runTest {
        val result = useCase("", "name", ModelType.GLB)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should return failure when name is blank`() = runTest {
        val result = useCase("content://test/model.glb", "", ModelType.GLB)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should delegate to repository on success`() = runTest {
        val uri = "content://test/model.glb"
        val name = "Test Model"
        val expectedObj = ARObject("1", name, "/path", ModelType.GLB)

        coEvery { repository.importObject(uri, name, ModelType.GLB) } returns Result.success(expectedObj)

        val result = useCase(uri, name, ModelType.GLB)
        assertTrue(result.isSuccess)
        assertEquals(expectedObj, result.getOrNull())
    }
}

class GetAllObjectsUseCaseTest {

    @Test
    fun `should return list of all objects`() = runTest {
        val repository = mockk<ARObjectRepository>()
        val useCase = GetAllObjectsUseCase(repository)

        val objects = listOf(
            ARObject("1", "Chair", "/chair.glb", ModelType.GLB),
            ARObject("2", "Table", "/table.glb", ModelType.GLB)
        )
        coEvery { repository.getAllObjects() } returns objects

        val result = useCase()
        assertEquals(2, result.size)
    }

    @Test
    fun `should return empty list when no objects`() = runTest {
        val repository = mockk<ARObjectRepository>()
        val useCase = GetAllObjectsUseCase(repository)

        coEvery { repository.getAllObjects() } returns emptyList()

        val result = useCase()
        assertTrue(result.isEmpty())
    }
}

class DeleteObjectUseCaseTest {

    @Test
    fun `should delegate to repository`() = runTest {
        val repository = mockk<ARObjectRepository>()
        val useCase = DeleteObjectUseCase(repository)

        coEvery { repository.deleteObject("123") } returns Unit

        useCase("123")
        coEvery { repository.deleteObject("123") }
    }
}

class PlaceObjectInSceneUseCaseTest {

    private val sceneRepository = mockk<com.trendhive.arsample.domain.repository.ARSceneRepository>()
    private val objectRepository = mockk<ARObjectRepository>()
    private val useCase = PlaceObjectInSceneUseCase(sceneRepository, objectRepository)

    @Test
    fun `should return failure when scene not found`() = runTest {
        coEvery { sceneRepository.getSceneById("scene1") } returns null

        val result = useCase(
            sceneId = "scene1",
            objectId = "obj1",
            position = com.trendhive.arsample.domain.model.Vector3(0f, 0f, 0f),
            rotation = com.trendhive.arsample.domain.model.Quaternion(),
            scale = 1f
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when object not found`() = runTest {
        coEvery { sceneRepository.getSceneById("scene1") } returns com.trendhive.arsample.domain.model.ARScene.createDefault()
        coEvery { objectRepository.getObjectById("obj1") } returns null

        val result = useCase(
            sceneId = "scene1",
            objectId = "obj1",
            position = com.trendhive.arsample.domain.model.Vector3(0f, 0f, 0f),
            rotation = com.trendhive.arsample.domain.model.Quaternion(),
            scale = 1f
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when scale is not positive`() = runTest {
        coEvery { sceneRepository.getSceneById("scene1") } returns com.trendhive.arsample.domain.model.ARScene.createDefault()
        coEvery { objectRepository.getObjectById("obj1") } returns ARObject("obj1", "Test", "/path", ModelType.GLB)

        val result = useCase(
            sceneId = "scene1",
            objectId = "obj1",
            position = com.trendhive.arsample.domain.model.Vector3(0f, 0f, 0f),
            rotation = com.trendhive.arsample.domain.model.Quaternion(),
            scale = 0f
        )

        assertTrue(result.isFailure)
    }
}

class RemoveObjectFromSceneUseCaseTest {

    private val repository = mockk<com.trendhive.arsample.domain.repository.ARSceneRepository>()
    private val useCase = RemoveObjectFromSceneUseCase(repository)

    @Test
    fun `should return failure when sceneId is blank`() = runTest {
        val result = useCase("", "obj1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when objectId is blank`() = runTest {
        val result = useCase("scene1", "")
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when scene not found`() = runTest {
        coEvery { repository.getSceneById("scene1") } returns null

        val result = useCase("scene1", "obj1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when object not in scene`() = runTest {
        coEvery { repository.getSceneById("scene1") } returns com.trendhive.arsample.domain.model.ARScene.createDefault()

        val result = useCase("scene1", "nonexistent")
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return success when object removed successfully`() = runTest {
        val placedObj = com.trendhive.arsample.domain.model.PlacedObject(
            objectId = "obj1",
            arObjectId = "/path",
            position = com.trendhive.arsample.domain.model.Vector3(0f, 0f, 0f)
        )
        val scene = com.trendhive.arsample.domain.model.ARScene("scene1", "Test", listOf(placedObj))
        val updatedScene = scene.copy(objects = emptyList())

        coEvery { repository.getSceneById("scene1") } returns scene
        coEvery { repository.removeObjectFromScene("scene1", "obj1") } returns Result.success(updatedScene)

        val result = useCase("scene1", "obj1")
        assertTrue(result.isSuccess)
        val resultScene = result.getOrNull()
        assertTrue(resultScene?.objects?.isEmpty() == true)
    }
}

class GetSceneUseCaseTest {

    private val repository = mockk<com.trendhive.arsample.domain.repository.ARSceneRepository>()
    private val useCase = GetSceneUseCase(repository)

    @Test
    fun `invoke should return scene by id`() = runTest {
        val scene = com.trendhive.arsample.domain.model.ARScene.createDefault()
        coEvery { repository.getSceneById("scene1") } returns scene

        val result = useCase("scene1")
        assertEquals(scene, result)
    }

    @Test
    fun `invoke should return null when scene not found`() = runTest {
        coEvery { repository.getSceneById("scene1") } returns null

        val result = useCase("scene1")
        assertEquals(null, result)
    }

    @Test
    fun `getDefaultScene should return or create default scene`() = runTest {
        val scene = com.trendhive.arsample.domain.model.ARScene.createDefault()
        coEvery { repository.getOrCreateDefaultScene() } returns scene

        val result = useCase.getDefaultScene()
        assertEquals(scene, result)
    }
}

class SaveSceneUseCaseTest {

    private val repository = mockk<com.trendhive.arsample.domain.repository.ARSceneRepository>()
    private val useCase = SaveSceneUseCase(repository)

    @Test
    fun `should return success when scene saved`() = runTest {
        val scene = com.trendhive.arsample.domain.model.ARScene.createDefault()
        coEvery { repository.saveScene(scene) } just runs

        val result = useCase(scene)
        assertTrue(result.isSuccess)
        assertEquals(scene, result.getOrNull())
    }

    @Test
    fun `should return failure when save throws exception`() = runTest {
        val scene = com.trendhive.arsample.domain.model.ARScene.createDefault()
        coEvery { repository.saveScene(scene) } throws Exception("Save failed")

        val result = useCase(scene)
        assertTrue(result.isFailure)
    }
}

class GetObjectByIdUseCaseTest {

    @Test
    fun `should return object by id`() = runTest {
        val repository = mockk<ARObjectRepository>()
        val useCase = GetObjectByIdUseCase(repository)
        val obj = ARObject("1", "Test", "/path", ModelType.GLB)

        coEvery { repository.getObjectById("1") } returns obj

        val result = useCase("1")
        assertEquals(obj, result)
    }

    @Test
    fun `should return null when object not found`() = runTest {
        val repository = mockk<ARObjectRepository>()
        val useCase = GetObjectByIdUseCase(repository)

        coEvery { repository.getObjectById("nonexistent") } returns null

        val result = useCase("nonexistent")
        assertEquals(null, result)
    }
}

class CreateSceneUseCaseTest {

    private val repository = mockk<com.trendhive.arsample.domain.repository.ARSceneRepository>()
    private val useCase = CreateSceneUseCase(repository)

    @Test
    fun `should return failure when name is blank`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `should create scene with provided name`() = runTest {
        val sceneName = "My New Scene"
        coEvery { repository.saveScene(any()) } just runs

        val result = useCase(sceneName)
        assertTrue(result.isSuccess)
        val scene = result.getOrNull()
        assertEquals(sceneName, scene?.name)
        assertTrue(scene?.objects?.isEmpty() == true)
    }

    @Test
    fun `should return failure when save throws exception`() = runTest {
        coEvery { repository.saveScene(any()) } throws Exception("Save failed")

        val result = useCase("New Scene")
        assertTrue(result.isFailure)
    }
}