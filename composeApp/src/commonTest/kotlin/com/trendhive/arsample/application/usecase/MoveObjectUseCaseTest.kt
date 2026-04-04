package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.exception.EntityNotFoundException
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.repository.ARSceneRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoveObjectUseCaseTest {

    private lateinit var repository: ARSceneRepository
    private lateinit var useCase: MoveObjectUseCase

    @BeforeTest
    fun setup() {
        repository = mockk()
        useCase = MoveObjectUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    // Test 1: Successfully move object to new position
    @Test
    fun `invoke with valid input should update object position`() = runTest {
        // Given
        val objectId = "obj-123"
        val sceneId = "scene-456"
        val oldPosition = Vector3(0f, 0f, 0f)
        val newPosition = Vector3(1f, 2f, 3f)

        val placedObject = PlacedObject(
            objectId = objectId,
            arObjectId = "model-1",
            position = oldPosition,
            scale = 1f
        )

        val scene = ARScene(
            id = sceneId,
            name = "Test Scene",
            objects = listOf(placedObject)
        )

        val expectedUpdatedObject = placedObject.copy(position = newPosition)
        val expectedScene = scene.copy(objects = listOf(expectedUpdatedObject))

        coEvery { repository.getSceneById(sceneId) } returns scene
        coEvery { repository.updateObjectInScene(sceneId, expectedUpdatedObject) } returns Result.success(expectedScene)

        // When
        val result = useCase(sceneId, objectId, newPosition)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(newPosition, result.getOrNull()?.objects?.first()?.position)
        coVerify { repository.updateObjectInScene(sceneId, expectedUpdatedObject) }
    }

    // Test 2: Fail when sceneId is blank
    @Test
    fun `invoke with blank sceneId should return failure`() = runTest {
        // When
        val result = useCase("", "obj-123", Vector3(1f, 2f, 3f))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.getSceneById(any()) }
    }

    // Test 3: Fail when objectId is blank
    @Test
    fun `invoke with blank objectId should return failure`() = runTest {
        // When
        val result = useCase("scene-1", "", Vector3(1f, 2f, 3f))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    // Test 4: Fail when scene not found
    @Test
    fun `invoke when scene not found should return failure`() = runTest {
        // Given
        coEvery { repository.getSceneById("non-existent") } returns null

        // When
        val result = useCase("non-existent", "obj-1", Vector3(1f, 2f, 3f))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EntityNotFoundException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Scene not found") == true)
    }

    // Test 5: Fail when object not found in scene
    @Test
    fun `invoke when object not in scene should return failure`() = runTest {
        // Given
        val scene = ARScene(
            id = "scene-1",
            name = "Test Scene",
            objects = emptyList()
        )
        coEvery { repository.getSceneById("scene-1") } returns scene

        // When
        val result = useCase("scene-1", "non-existent-obj", Vector3(1f, 2f, 3f))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EntityNotFoundException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Object not found") == true)
    }

    // Test 6: Fail when position contains NaN
    @Test
    fun `invoke with NaN in x position should return failure`() = runTest {
        // When
        val result = useCase("scene-1", "obj-1", Vector3(Float.NaN, 0f, 0f))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        assertTrue(result.exceptionOrNull()?.message?.contains("invalid") == true || 
                   result.exceptionOrNull()?.message?.contains("NaN") == true)
    }

    @Test
    fun `invoke with NaN in y position should return failure`() = runTest {
        // When
        val result = useCase("scene-1", "obj-1", Vector3(0f, Float.NaN, 0f))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `invoke with NaN in z position should return failure`() = runTest {
        // When
        val result = useCase("scene-1", "obj-1", Vector3(0f, 0f, Float.NaN))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    // Test 7: Move object when scene has multiple objects
    @Test
    fun `invoke should only update target object in scene with multiple objects`() = runTest {
        // Given
        val targetObjectId = "obj-1"
        val otherObjectId = "obj-2"
        val newPosition = Vector3(5f, 5f, 5f)

        val targetObject = PlacedObject(
            objectId = targetObjectId,
            arObjectId = "model-1",
            position = Vector3.ZERO,
            scale = 1f
        )
        val otherObject = PlacedObject(
            objectId = otherObjectId,
            arObjectId = "model-2",
            position = Vector3(1f, 1f, 1f),
            scale = 1f
        )

        val scene = ARScene(id = "scene-1", name = "Test", objects = listOf(targetObject, otherObject))

        val updatedTarget = targetObject.copy(position = newPosition)
        val expectedScene = scene.copy(objects = listOf(updatedTarget, otherObject))

        coEvery { repository.getSceneById("scene-1") } returns scene
        coEvery { repository.updateObjectInScene("scene-1", updatedTarget) } returns Result.success(expectedScene)

        // When
        val result = useCase("scene-1", targetObjectId, newPosition)

        // Then
        assertTrue(result.isSuccess)
        val resultScene = result.getOrNull()!!
        assertEquals(2, resultScene.objects.size)
        assertEquals(newPosition, resultScene.objects.find { it.objectId == targetObjectId }?.position)
        assertEquals(Vector3(1f, 1f, 1f), resultScene.objects.find { it.objectId == otherObjectId }?.position)
    }

    // Test 8: Repository returns failure
    @Test
    fun `invoke should propagate repository failure`() = runTest {
        // Given
        val objectId = "obj-1"
        val sceneId = "scene-1"
        val newPosition = Vector3(1f, 2f, 3f)

        val placedObject = PlacedObject(
            objectId = objectId,
            arObjectId = "model-1",
            position = Vector3.ZERO,
            scale = 1f
        )
        val scene = ARScene(id = sceneId, name = "Test", objects = listOf(placedObject))
        val expectedUpdatedObject = placedObject.copy(position = newPosition)

        coEvery { repository.getSceneById(sceneId) } returns scene
        coEvery { repository.updateObjectInScene(sceneId, expectedUpdatedObject) } returns Result.failure(
            Exception("Database error")
        )

        // When
        val result = useCase(sceneId, objectId, newPosition)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Database error") == true)
    }

    // Test 9: Move object to same position (no-op but should succeed)
    @Test
    fun `invoke with same position should succeed`() = runTest {
        // Given
        val objectId = "obj-1"
        val sceneId = "scene-1"
        val samePosition = Vector3(1f, 2f, 3f)

        val placedObject = PlacedObject(
            objectId = objectId,
            arObjectId = "model-1",
            position = samePosition,
            scale = 1f
        )
        val scene = ARScene(id = sceneId, name = "Test", objects = listOf(placedObject))
        val expectedUpdatedObject = placedObject.copy(position = samePosition)
        val expectedScene = scene.copy(objects = listOf(expectedUpdatedObject))

        coEvery { repository.getSceneById(sceneId) } returns scene
        coEvery { repository.updateObjectInScene(sceneId, expectedUpdatedObject) } returns Result.success(expectedScene)

        // When
        val result = useCase(sceneId, objectId, samePosition)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(samePosition, result.getOrNull()?.objects?.first()?.position)
    }

    // Test 10: Move object with negative coordinates
    @Test
    fun `invoke with negative coordinates should succeed`() = runTest {
        // Given
        val objectId = "obj-1"
        val sceneId = "scene-1"
        val negativePosition = Vector3(-5f, -10f, -15f)

        val placedObject = PlacedObject(
            objectId = objectId,
            arObjectId = "model-1",
            position = Vector3.ZERO,
            scale = 1f
        )
        val scene = ARScene(id = sceneId, name = "Test", objects = listOf(placedObject))
        val expectedUpdatedObject = placedObject.copy(position = negativePosition)
        val expectedScene = scene.copy(objects = listOf(expectedUpdatedObject))

        coEvery { repository.getSceneById(sceneId) } returns scene
        coEvery { repository.updateObjectInScene(sceneId, expectedUpdatedObject) } returns Result.success(expectedScene)

        // When
        val result = useCase(sceneId, objectId, negativePosition)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(negativePosition, result.getOrNull()?.objects?.first()?.position)
    }

    // Test 11: Move object preserves other properties (rotation, scale)
    @Test
    fun `invoke should preserve object rotation and scale`() = runTest {
        // Given
        val objectId = "obj-1"
        val sceneId = "scene-1"
        val newPosition = Vector3(10f, 20f, 30f)
        val originalScale = 2.5f
        val originalRotation = com.trendhive.arsample.domain.model.Quaternion(0.5f, 0.5f, 0.5f, 0.5f)

        val placedObject = PlacedObject(
            objectId = objectId,
            arObjectId = "model-1",
            position = Vector3.ZERO,
            rotation = originalRotation,
            scale = originalScale
        )
        val scene = ARScene(id = sceneId, name = "Test", objects = listOf(placedObject))
        val expectedUpdatedObject = placedObject.copy(position = newPosition)
        val expectedScene = scene.copy(objects = listOf(expectedUpdatedObject))

        coEvery { repository.getSceneById(sceneId) } returns scene
        coEvery { repository.updateObjectInScene(sceneId, expectedUpdatedObject) } returns Result.success(expectedScene)

        // When
        val result = useCase(sceneId, objectId, newPosition)

        // Then
        assertTrue(result.isSuccess)
        val movedObject = result.getOrNull()?.objects?.first()
        assertEquals(newPosition, movedObject?.position)
        assertEquals(originalScale, movedObject?.scale)
        assertEquals(originalRotation, movedObject?.rotation)
    }
}
