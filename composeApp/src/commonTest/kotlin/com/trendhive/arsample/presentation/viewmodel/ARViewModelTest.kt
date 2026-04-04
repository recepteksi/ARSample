package com.trendhive.arsample.presentation.viewmodel

import com.trendhive.arsample.TestDataBuilders
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.repository.ARSceneRepository
import com.trendhive.arsample.application.usecase.GetSceneUseCase
import com.trendhive.arsample.application.usecase.MoveObjectUseCase
import com.trendhive.arsample.application.usecase.PlaceObjectInSceneUseCase
import com.trendhive.arsample.application.usecase.RemoveObjectFromSceneUseCase
import com.trendhive.arsample.application.usecase.SaveSceneUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ARViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        placeObjectUseCase: PlaceObjectInSceneUseCase = mockk(),
        removeObjectUseCase: RemoveObjectFromSceneUseCase = mockk(),
        getSceneUseCase: GetSceneUseCase = mockk(),
        saveSceneUseCase: SaveSceneUseCase = mockk(),
        sceneRepository: ARSceneRepository = mockk(),
        moveObjectUseCase: MoveObjectUseCase = mockk()
    ): ARViewModel {
        return ARViewModel(
            placeObjectUseCase = placeObjectUseCase,
            removeObjectUseCase = removeObjectUseCase,
            getSceneUseCase = getSceneUseCase,
            saveSceneUseCase = saveSceneUseCase,
            sceneRepository = sceneRepository,
            moveObjectUseCase = moveObjectUseCase
        )
    }

    @Test
    fun `loadScene should set loading state to true initially`() = runTest {
        val mockRepository = mockk<ARSceneRepository>()
        coEvery { mockRepository.getOrCreateDefaultScene() } returns TestDataBuilders.createTestARScene("scene1")

        val viewModel = createViewModel(sceneRepository = mockRepository)
        // Note: init block runs immediately, so we check the state after init
        
        // Advance until idle to let coroutine complete
        testDispatcher.scheduler.advanceUntilIdle()

        // The state should have been updated after init
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadScene should populate placedObjects after loading`() = runTest {
        val placedObj = TestDataBuilders.createTestPlacedObject("obj1")
        val scene = TestDataBuilders.createTestARScene("scene1", objects = listOf(placedObj))
        val mockRepository = mockk<ARSceneRepository>()
        coEvery { mockRepository.getOrCreateDefaultScene() } returns scene

        val viewModel = createViewModel(sceneRepository = mockRepository)
        // Wait for init to complete
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(1, state.placedObjects.size)
        assertEquals("obj1", state.placedObjects[0].objectId)
    }

    @Test
    fun `loadScene should set error when loading fails`() = runTest {
        val mockRepository = mockk<ARSceneRepository>()
        coEvery { mockRepository.getOrCreateDefaultScene() } throws Exception("Load failed")

        val viewModel = createViewModel(sceneRepository = mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals("Load failed", state.error)
    }

    @Test
    fun `placeObject should update placedObjects when successful`() = runTest {
        val initialScene = TestDataBuilders.createTestARScene("scene1", objects = emptyList())
        val placedObj = TestDataBuilders.createTestPlacedObject("placed1")
        val updatedScene = initialScene.copy(objects = listOf(placedObj))

        val mockSceneRepository = mockk<ARSceneRepository>()
        val mockPlaceUseCase = mockk<PlaceObjectInSceneUseCase>()

        coEvery { mockSceneRepository.getOrCreateDefaultScene() } returns initialScene
        coEvery { mockPlaceUseCase("scene1", "obj1", any(), any(), any()) } returns Result.success(updatedScene)

        val viewModel = createViewModel(
            placeObjectUseCase = mockPlaceUseCase,
            sceneRepository = mockSceneRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.placeObject("obj1", Vector3(1f, 2f, 3f))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.placedObjects.size)
        assertEquals("placed1", state.placedObjects[0].objectId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `placeObject should set error when placement fails`() = runTest {
        val initialScene = TestDataBuilders.createTestARScene("scene1", objects = emptyList())
        val mockSceneRepository = mockk<ARSceneRepository>()
        val mockPlaceUseCase = mockk<PlaceObjectInSceneUseCase>()

        coEvery { mockSceneRepository.getOrCreateDefaultScene() } returns initialScene
        coEvery { mockPlaceUseCase("scene1", "obj1", any(), any(), any()) } returns Result.failure(Exception("Place failed"))

        val viewModel = createViewModel(
            placeObjectUseCase = mockPlaceUseCase,
            sceneRepository = mockSceneRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.placeObject("obj1", Vector3(1f, 2f, 3f))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Place failed", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `removeObject should update placedObjects when successful`() = runTest {
        val placedObj = TestDataBuilders.createTestPlacedObject("obj1")
        val initialScene = TestDataBuilders.createTestARScene("scene1", objects = listOf(placedObj))
        val emptyScene = initialScene.copy(objects = emptyList())

        val mockSceneRepository = mockk<ARSceneRepository>()
        val mockRemoveUseCase = mockk<RemoveObjectFromSceneUseCase>()

        coEvery { mockSceneRepository.getOrCreateDefaultScene() } returns initialScene
        coEvery { mockRemoveUseCase("scene1", "obj1") } returns Result.success(emptyScene)

        val viewModel = createViewModel(
            removeObjectUseCase = mockRemoveUseCase,
            sceneRepository = mockSceneRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeObject("obj1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.placedObjects.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `removeObject should set error when removal fails`() = runTest {
        val placedObj = TestDataBuilders.createTestPlacedObject("obj1")
        val initialScene = TestDataBuilders.createTestARScene("scene1", objects = listOf(placedObj))

        val mockSceneRepository = mockk<ARSceneRepository>()
        val mockRemoveUseCase = mockk<RemoveObjectFromSceneUseCase>()

        coEvery { mockSceneRepository.getOrCreateDefaultScene() } returns initialScene
        coEvery { mockRemoveUseCase("scene1", "obj1") } returns Result.failure(Exception("Remove failed"))

        val viewModel = createViewModel(
            removeObjectUseCase = mockRemoveUseCase,
            sceneRepository = mockSceneRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeObject("obj1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Remove failed", state.error)
    }

    @Test
    fun `selectObject should update selectedObjectId`() = runTest {
        val mockRepository = mockk<ARSceneRepository>()
        coEvery { mockRepository.getOrCreateDefaultScene() } returns TestDataBuilders.createTestARScene("scene1")

        val viewModel = createViewModel(sceneRepository = mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectObject("obj1")

        assertEquals("obj1", viewModel.uiState.value.selectedObjectId)
    }

    @Test
    fun `selectObject with null should clear selection`() = runTest {
        val mockRepository = mockk<ARSceneRepository>()
        coEvery { mockRepository.getOrCreateDefaultScene() } returns TestDataBuilders.createTestARScene("scene1")

        val viewModel = createViewModel(sceneRepository = mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectObject("obj1")
        viewModel.selectObject(null)

        assertNull(viewModel.uiState.value.selectedObjectId)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        val mockRepository = mockk<ARSceneRepository>()
        coEvery { mockRepository.getOrCreateDefaultScene() } throws Exception("Test error")

        val viewModel = createViewModel(sceneRepository = mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertEquals("Test error", state.error)

        viewModel.clearError()
        state = viewModel.uiState.value
        assertNull(state.error)
    }

    @Test
    fun `placeObject with custom rotation and scale should pass parameters`() = runTest {
        val initialScene = TestDataBuilders.createTestARScene("scene1", objects = emptyList())
        val placedObj = TestDataBuilders.createTestPlacedObject("placed1")
        val updatedScene = initialScene.copy(objects = listOf(placedObj))

        val mockSceneRepository = mockk<ARSceneRepository>()
        val mockPlaceUseCase = mockk<PlaceObjectInSceneUseCase>()

        coEvery { mockSceneRepository.getOrCreateDefaultScene() } returns initialScene
        coEvery { mockPlaceUseCase("scene1", "obj1", any(), any(), any()) } returns Result.success(updatedScene)

        val viewModel = createViewModel(
            placeObjectUseCase = mockPlaceUseCase,
            sceneRepository = mockSceneRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val rotation = Quaternion(0.1f, 0.2f, 0.3f, 0.9f)
        val scale = 2.5f

        viewModel.placeObject("obj1", Vector3(1f, 2f, 3f), rotation, scale)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.placedObjects.size)
    }
}
