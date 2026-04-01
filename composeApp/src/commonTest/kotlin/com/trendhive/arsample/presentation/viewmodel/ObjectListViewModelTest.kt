package com.trendhive.arsample.presentation.viewmodel

import com.trendhive.arsample.TestDataBuilders
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.usecase.DeleteObjectUseCase
import com.trendhive.arsample.domain.usecase.GetAllObjectsUseCase
import com.trendhive.arsample.domain.usecase.ImportObjectUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObjectListViewModelTest {

    private fun createViewModel(
        getAllObjectsUseCase: GetAllObjectsUseCase = mockk(),
        deleteObjectUseCase: DeleteObjectUseCase = mockk(),
        importObjectUseCase: ImportObjectUseCase = mockk()
    ): ObjectListViewModel {
        return ObjectListViewModel(
            getAllObjectsUseCase = getAllObjectsUseCase,
            deleteObjectUseCase = deleteObjectUseCase,
            importObjectUseCase = importObjectUseCase
        )
    }

    @Test
    fun `loadObjects should populate objects list`() = runTest {
        val objects = listOf(
            TestDataBuilders.createTestARObject("1", "Chair"),
            TestDataBuilders.createTestARObject("2", "Table")
        )
        val mockUseCase = mockk<GetAllObjectsUseCase>()
        coEvery { mockUseCase() } returns objects

        val viewModel = createViewModel(getAllObjectsUseCase = mockUseCase)
        val state = viewModel.uiState.value

        assertEquals(2, state.objects.size)
        assertEquals("Chair", state.objects[0].name)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadObjects should set error when loading fails`() = runTest {
        val mockUseCase = mockk<GetAllObjectsUseCase>()
        coEvery { mockUseCase() } throws Exception("Load failed")

        val viewModel = createViewModel(getAllObjectsUseCase = mockUseCase)
        val state = viewModel.uiState.value

        assertEquals("Load failed", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadObjects should return empty list when no objects`() = runTest {
        val mockUseCase = mockk<GetAllObjectsUseCase>()
        coEvery { mockUseCase() } returns emptyList()

        val viewModel = createViewModel(getAllObjectsUseCase = mockUseCase)
        val state = viewModel.uiState.value

        assertTrue(state.objects.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `importObject should update importSuccess flag`() = runTest {
        val newObject = TestDataBuilders.createTestARObject("3", "Lamp")
        val mockGetUseCase = mockk<GetAllObjectsUseCase>()
        val mockImportUseCase = mockk<ImportObjectUseCase>()

        coEvery { mockGetUseCase() } returns emptyList()
        coEvery { mockImportUseCase("content://uri", "Lamp", ModelType.GLB) } returns Result.success(newObject)
        coEvery { mockGetUseCase() } returns listOf(newObject)

        val viewModel = createViewModel(
            getAllObjectsUseCase = mockGetUseCase,
            importObjectUseCase = mockImportUseCase
        )

        viewModel.importObject("content://uri", "Lamp", ModelType.GLB)

        val state = viewModel.uiState.value
        assertTrue(state.importSuccess)
        assertEquals(1, state.objects.size)
    }

    @Test
    fun `importObject should set error when import fails`() = runTest {
        val mockGetUseCase = mockk<GetAllObjectsUseCase>()
        val mockImportUseCase = mockk<ImportObjectUseCase>()

        coEvery { mockGetUseCase() } returns emptyList()
        coEvery { mockImportUseCase("content://uri", "Lamp", ModelType.GLB) } returns Result.failure(Exception("Import failed"))

        val viewModel = createViewModel(
            getAllObjectsUseCase = mockGetUseCase,
            importObjectUseCase = mockImportUseCase
        )

        viewModel.importObject("content://uri", "Lamp", ModelType.GLB)

        val state = viewModel.uiState.value
        assertEquals("Import failed", state.error)
        assertFalse(state.importSuccess)
    }

    @Test
    fun `deleteObject should remove from list`() = runTest {
        val objects = listOf(
            TestDataBuilders.createTestARObject("1", "Chair"),
            TestDataBuilders.createTestARObject("2", "Table")
        )
        val mockGetUseCase = mockk<GetAllObjectsUseCase>()
        val mockDeleteUseCase = mockk<DeleteObjectUseCase>()

        coEvery { mockGetUseCase() } returns objects
        coEvery { mockDeleteUseCase("1") } returns Result.success(Unit)
        coEvery { mockGetUseCase() } returns listOf(objects[1])

        val viewModel = createViewModel(
            getAllObjectsUseCase = mockGetUseCase,
            deleteObjectUseCase = mockDeleteUseCase
        )

        viewModel.deleteObject("1")

        val state = viewModel.uiState.value
        assertEquals(1, state.objects.size)
        assertEquals("Table", state.objects[0].name)
    }

    @Test
    fun `deleteObject should set error when deletion fails`() = runTest {
        val objects = listOf(
            TestDataBuilders.createTestARObject("1", "Chair")
        )
        val mockGetUseCase = mockk<GetAllObjectsUseCase>()
        val mockDeleteUseCase = mockk<DeleteObjectUseCase>()

        coEvery { mockGetUseCase() } returns objects
        coEvery { mockDeleteUseCase("1") } throws Exception("Delete failed")

        val viewModel = createViewModel(
            getAllObjectsUseCase = mockGetUseCase,
            deleteObjectUseCase = mockDeleteUseCase
        )

        viewModel.deleteObject("1")

        val state = viewModel.uiState.value
        assertEquals("Delete failed", state.error)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        val mockGetUseCase = mockk<GetAllObjectsUseCase>()
        val mockImportUseCase = mockk<ImportObjectUseCase>()

        coEvery { mockGetUseCase() } returns emptyList()
        coEvery { mockImportUseCase(any(), any(), any()) } returns Result.failure(Exception("Test error"))

        val viewModel = createViewModel(
            getAllObjectsUseCase = mockGetUseCase,
            importObjectUseCase = mockImportUseCase
        )

        viewModel.importObject("uri", "name", ModelType.GLB)
        var state = viewModel.uiState.value
        assertEquals("Test error", state.error)

        viewModel.clearError()
        state = viewModel.uiState.value
        assertNull(state.error)
    }

    @Test
    fun `clearImportSuccess should reset import success flag`() = runTest {
        val newObject = TestDataBuilders.createTestARObject("1", "Chair")
        val mockGetUseCase = mockk<GetAllObjectsUseCase>()
        val mockImportUseCase = mockk<ImportObjectUseCase>()

        coEvery { mockGetUseCase() } returns emptyList()
        coEvery { mockImportUseCase(any(), any(), any()) } returns Result.success(newObject)
        coEvery { mockGetUseCase() } returns listOf(newObject)

        val viewModel = createViewModel(
            getAllObjectsUseCase = mockGetUseCase,
            importObjectUseCase = mockImportUseCase
        )

        viewModel.importObject("uri", "Chair", ModelType.GLB)
        var state = viewModel.uiState.value
        assertTrue(state.importSuccess)

        viewModel.clearImportSuccess()
        state = viewModel.uiState.value
        assertFalse(state.importSuccess)
    }

    @Test
    fun `loadObjects should be callable multiple times`() = runTest {
        val mockGetUseCase = mockk<GetAllObjectsUseCase>()
        val objects = listOf(TestDataBuilders.createTestARObject("1", "Chair"))

        coEvery { mockGetUseCase() } returns objects

        val viewModel = createViewModel(getAllObjectsUseCase = mockGetUseCase)
        viewModel.loadObjects()

        val state = viewModel.uiState.value
        assertEquals(1, state.objects.size)
    }
}
