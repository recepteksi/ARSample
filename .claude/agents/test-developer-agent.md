---
name: test-developer-agent
description: Writes unit tests - domain, use case, repository, ViewModel tests
type: reference
---

# Test Developer Agent

**Project:** ARSample - 3D Object Placement/Removal
**Platform:** Kotlin Multiplatform (Android + iOS)
**Date:** 2026-03-30

---

## Mission

Write unit tests for written code, increase test coverage.

---

## Responsibilities

### 1. Domain Layer Tests

**Value Object Tests:**
```kotlin
// commonTest/kotlin/com/trendhive/arsample/domain/model/valueobjects/
class ModelUriTest {
    @Test
    fun `should create valid GLB uri`() {
        val result = ModelUri.create("/path/model.glb")
        assertTrue(result.isSuccess)
        assertEquals("/path/model.glb", result.getOrNull()?.value)
    }

    @Test
    fun `should fail for blank uri`() {
        val result = ModelUri.create("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should fail for invalid format`() {
        val result = ModelUri.create("/path/model.txt")
        assertTrue(result.isFailure)
    }

    @Test
    fun `should accept USDZ format`() {
        val result = ModelUri.create("/path/model.usdz")
        assertTrue(result.isSuccess)
    }
}

class ObjectNameTest {
    @Test
    fun `should create valid name`() {
        val result = ObjectName.create("Chair")
        assertTrue(result.isSuccess)
        assertEquals("Chair", result.getOrNull()?.value)
    }

    @Test
    fun `should fail for blank name`() {
        val result = ObjectName.create("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `should fail for name longer than 50 characters`() {
        val longName = "a".repeat(51)
        val result = ObjectName.create(longName)
        assertTrue(result.isFailure)
    }
}
```

**Entity Tests:**
```kotlin
// commonTest/kotlin/com/trendhive/arsample/domain/model/
class ARObjectTest {
    @Test
    fun `should create ARObject with correct properties`() {
        val obj = ARObject(
            id = "123",
            name = "Chair",
            filePath = "/models/chair.glb",
            format = ModelFormat.GLB
        )
        assertEquals("123", obj.id)
        assertEquals("Chair", obj.name)
        assertEquals(ModelFormat.GLB, obj.format)
    }

    @Test
    fun `should have default createdAt timestamp`() {
        val before = System.currentTimeMillis()
        val obj = ARObject("1", "Test", "/path", ModelFormat.GLB)
        val after = System.currentTimeMillis()
        assertTrue(obj.createdAt in before..after)
    }
}

class PlacedObjectTest {
    @Test
    fun `should create PlacedObject with default scale`() {
        val placed = PlacedObject(
            id = "p1",
            arObjectId = "o1",
            position = Vector3(1f, 2f, 3f),
            rotation = Quaternion(0f, 0f, 0f, 1f)
        )
        assertEquals(1f, placed.scale)
    }
}
```

**UseCase Tests:**
```kotlin
// commonTest/kotlin/com/trendhive/arsample/domain/usecase/
class ImportObjectUseCaseTest {
    private lateinit var repository: ARObjectRepository
    private lateinit var useCase: ImportObjectUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ImportObjectUseCase(repository)
    }

    @Test
    fun `should return success when import succeeds`() = runTest {
        val uri = "content://test/model.glb"
        val name = "Test Model"

        coEvery { repository.importObject(uri, name) } returns Result.success(
            ARObject("1", name, "/path", ModelFormat.GLB)
        )

        val result = useCase(uri, name)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when repository throws`() = runTest {
        coEvery { repository.importObject(any(), any()) } throws Exception("Import failed")

        val result = useCase("uri", "name")
        assertTrue(result.isFailure)
    }
}

class GetAllObjectsUseCaseTest {
    @Test
    fun `should return list of all objects`() = runTest {
        val repository = mockk<ARObjectRepository>()
        val useCase = GetAllObjectsUseCase(repository)

        val objects = listOf(
            ARObject("1", "Chair", "/chair.glb", ModelFormat.GLB),
            ARObject("2", "Table", "/table.glb", ModelFormat.GLB)
        )
        coEvery { repository.getAllObjects() } returns objects

        val result = useCase()
        assertEquals(2, result.size)
    }
}
```

---

### 2. Data Layer Tests

**Repository Implementation Tests:**
```kotlin
// commonTest/kotlin/com/trendhive/arsample/data/repository/
class ARObjectRepositoryImplTest {
    private lateinit var localDataSource: ARModelLocalDataSource
    private lateinit var fileStorage: ModelFileStorage
    private lateinit var mapper: ARObjectMapper
    private lateinit var repository: ARObjectRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk()
        fileStorage = mockk()
        mapper = mockk()
        repository = ARObjectRepositoryImpl(localDataSource, fileStorage, mapper)
    }

    @Test
    fun `saveObject should store object in local data source`() = runTest {
        val arObject = ARObject("1", "Test", "/path.glb", ModelFormat.GLB)
        coEvery { localDataSource.saveObject(any()) } just runs

        repository.saveObject(arObject)

        coVerify { localDataSource.saveObject(arObject) }
    }

    @Test
    fun `deleteObject should remove object and file`() = runTest {
        val objectId = "1"
        val filePath = "/path.glb"

        coEvery { localDataSource.getObjectById(objectId) } returns ARObject(objectId, "Test", filePath, ModelFormat.GLB)
        coEvery { localDataSource.deleteObject(objectId) } just runs
        coEvery { fileStorage.deleteModel(filePath) } returns true

        repository.deleteObject(objectId)

        coVerify { localDataSource.deleteObject(objectId) }
        coVerify { fileStorage.deleteModel(filePath) }
    }
}
```

---

### 3. Presentation Layer Tests

**ViewModel Tests:**
```kotlin
// commonTest/kotlin/com/trendhive/arsample/presentation/viewmodel/
class ARViewModelTest {
    private lateinit var placeObjectUseCase: PlaceObjectInSceneUseCase
    private lateinit var removeObjectUseCase: RemoveObjectFromSceneUseCase
    private lateinit var getCurrentSceneUseCase: GetCurrentSceneUseCase
    private lateinit var viewModel: ARViewModel

    @Before
    fun setup() {
        placeObjectUseCase = mockk()
        removeObjectUseCase = mockk()
        getCurrentSceneUseCase = mockk()
        viewModel = ARViewModel(placeObjectUseCase, removeObjectUseCase, getCurrentSceneUseCase)
    }

    @Test
    fun `placeObject should update state with new object`() = runTest {
        val objectId = "o1"
        val position = Vector3(1f, 2f, 3f)
        val rotation = Quaternion(0f, 0f, 0f, 1f)

        coEvery { placeObjectUseCase("scene1", objectId, position, rotation) } returns Result.success(
            ARScene("scene1", "Test", listOf(PlacedObject("p1", objectId, position, rotation)))
        )

        viewModel.placeObject(objectId, position, rotation)

        assertEquals(1, viewModel.uiState.value.placedObjects.size)
    }

    @Test
    fun `removeObject should update state`() = runTest {
        coEvery { removeObjectUseCase("scene1", "p1") } returns Result.success(
            ARScene("scene1", "Test", emptyList())
        )

        viewModel.removeObject("p1")

        assertTrue(viewModel.uiState.value.placedObjects.isEmpty())
    }
}

class ObjectListViewModelTest {
    @Test
    fun `loadObjects should update objects state`() = runTest {
        val repository = mockk<ARObjectRepository>()
        val useCase = GetAllObjectsUseCase(repository)
        val viewModel = ObjectListViewModel(useCase, mockk(), mockk())

        val objects = listOf(
            ARObject("1", "Chair", "/chair.glb", ModelFormat.GLB)
        )
        coEvery { repository.getAllObjects() } returns objects

        viewModel.loadObjects()

        assertEquals(1, viewModel.objects.value.size)
    }
}
```

---

## Test Frameworks

| Platform | Framework | Mocking |
|----------|-----------|---------|
| Kotlin | Kotlin Test, JUnit | MockK |
| iOS | XCTest | Mockingbird |

---

## Test Dosyaları Konumu

```
composeApp/src/
├── commonTest/kotlin/com/trendhive/arsample/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── ARObjectTest.kt
│   │   │   ├── PlacedObjectTest.kt
│   │   │   └── ARSceneTest.kt
│   │   └── usecase/
│   │       ├── ImportObjectUseCaseTest.kt
│   │       ├── GetAllObjectsUseCaseTest.kt
│   │       ├── PlaceObjectInSceneUseCaseTest.kt
│   │       └── RemoveObjectFromSceneUseCaseTest.kt
│   ├── data/
│   │   └── repository/
│   │       ├── ARObjectRepositoryImplTest.kt
│   │       └── ARSceneRepositoryImplTest.kt
│   └── presentation/
│       └── viewmodel/
│           ├── ARViewModelTest.kt
│           └── ObjectListViewModelTest.kt
└── iosTest/...
    └── (Swift test files)
```

---

## Coverage Hedefi

- **Domain Layer:** 90%+
- **Use Cases:** 100%
- **Repository Interfaces:** 80%+
- **ViewModels:** 85%+

---

## 🚀 Advanced Test Patterns (Halleder'den Öğrenilenler)

### 1. Given-When-Then Pattern (BDD Style)

**✅ DO - Anlaşılır test yapısı:**
```kotlin
class ImportObjectUseCaseTest {
    private lateinit var repository: ARObjectRepository
    private lateinit var useCase: ImportObjectUseCase
    
    @Before
    fun setup() {
        repository = mockk()
        useCase = ImportObjectUseCase(repository)
    }
    
    @Test
    fun `invoke with valid input should import object successfully`() = runTest {
        // GIVEN - Test verisi hazırla
        val input = ImportObjectInput(
            uri = "file://models/chair.glb",
            name = "Modern Chair",
            modelType = ModelType.GLB
        )
        val expectedObject = ARObject(
            id = "test-id",
            name = ObjectName.create("Modern Chair").getOrThrow(),
            modelUri = ModelUri.create("file://models/chair.glb").getOrThrow(),
            modelType = ModelType.GLB
        )
        coEvery { 
            repository.importObject(any(), any(), any()) 
        } returns Result.success(expectedObject)
        
        // WHEN - Test edilen fonksiyonu çağır
        val result = useCase.invoke(input)
        
        // THEN - Sonucu doğrula
        assertTrue(result.isSuccess)
        assertEquals(expectedObject, result.getOrNull())
        assertEquals("Modern Chair", result.getOrNull()?.name?.value)
        
        // Verify interactions
        coVerify(exactly = 1) {
            repository.importObject(
                uri = input.uri,
                name = input.name,
                modelType = input.modelType
            )
        }
    }
    
    @Test
    fun `invoke with empty name should return validation error`() = runTest {
        // GIVEN
        val input = ImportObjectInput(
            uri = "file://models/chair.glb",
            name = "",  // Invalid
            modelType = ModelType.GLB
        )
        
        // WHEN
        val result = useCase.invoke(input)
        
        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        assertContains(result.exceptionOrNull()?.message ?: "", "blank")
        
        // Repository should NOT be called
        coVerify(exactly = 0) { repository.importObject(any(), any(), any()) }
    }
    
    @Test
    fun `invoke with repository failure should propagate error`() = runTest {
        // GIVEN
        val input = ImportObjectInput(
            uri = "file://models/chair.glb",
            name = "Chair",
            modelType = ModelType.GLB
        )
        val storageError = StorageException("Disk full")
        coEvery {
            repository.importObject(any(), any(), any())
        } returns Result.failure(storageError)
        
        // WHEN
        val result = useCase.invoke(input)
        
        // THEN
        assertTrue(result.isFailure)
        assertEquals(storageError, result.exceptionOrNull())
    }
}
```

### 2. ViewModel Test Pattern (inspired by BLoC test)

**✅ DO - StateFlow test with turbine:**
```kotlin
class ObjectListViewModelTest {
    private lateinit var getAllObjectsUseCase: GetAllObjectsUseCase
    private lateinit var importObjectUseCase: ImportObjectUseCase
    private lateinit var viewModel: ObjectListViewModel
    
    @Before
    fun setup() {
        getAllObjectsUseCase = mockk()
        importObjectUseCase = mockk()
        
        // Default: empty list
        coEvery { getAllObjectsUseCase.invoke(Unit) } returns Result.success(emptyList())
        
        viewModel = ObjectListViewModel(getAllObjectsUseCase, importObjectUseCase)
    }
    
    @Test
    fun `initial state should be Loading`() = runTest {
        // GIVEN - Fresh ViewModel
        val freshViewModel = ObjectListViewModel(getAllObjectsUseCase, importObjectUseCase)
        
        // WHEN - Collect first state
        val initialState = freshViewModel.state.value
        
        // THEN
        assertTrue(initialState is ObjectListState.Loading)
    }
    
    @Test
    fun `loadObjects with success should emit Success state`() = runTest {
        // GIVEN
        val objects = listOf(
            createTestARObject(id = "1", name = "Chair"),
            createTestARObject(id = "2", name = "Table")
        )
        coEvery { getAllObjectsUseCase.invoke(Unit) } returns Result.success(objects)
        
        // WHEN
        viewModel.loadObjects()
        
        // Wait for state update
        advanceUntilIdle()
        
        // THEN
        val state = viewModel.state.value
        assertTrue(state is ObjectListState.Success)
        assertEquals(2, (state as ObjectListState.Success).objects.size)
    }
    
    @Test
    fun `loadObjects with failure should emit Error state`() = runTest {
        // GIVEN
        val error = StorageException("Database error")
        coEvery { getAllObjectsUseCase.invoke(Unit) } returns Result.failure(error)
        
        // WHEN
        viewModel.loadObjects()
        advanceUntilIdle()
        
        // THEN
        val state = viewModel.state.value
        assertTrue(state is ObjectListState.Error)
        assertContains((state as ObjectListState.Error).message, "Database error")
    }
    
    @Test
    fun `importObject with success should reload objects`() = runTest {
        // GIVEN
        val input = ImportObjectInput("file://chair.glb", "Chair", ModelType.GLB)
        val newObject = createTestARObject(id = "new", name = "Chair")
        
        coEvery { importObjectUseCase.invoke(any()) } returns Result.success(newObject)
        coEvery { getAllObjectsUseCase.invoke(Unit) } returns Result.success(listOf(newObject))
        
        // WHEN
        viewModel.importObject(input.uri, input.name, input.modelType)
        advanceUntilIdle()
        
        // THEN
        coVerify(exactly = 1) { importObjectUseCase.invoke(any()) }
        coVerify(exactly = 2) { getAllObjectsUseCase.invoke(Unit) }  // init + reload
        
        val state = viewModel.state.value
        assertTrue(state is ObjectListState.Success)
        assertEquals(1, (state as ObjectListState.Success).objects.size)
    }
    
    // Test helper
    private fun createTestARObject(id: String, name: String): ARObject {
        return ARObject(
            id = id,
            name = ObjectName.create(name).getOrThrow(),
            modelUri = ModelUri.create("file://test.glb").getOrThrow(),
            modelType = ModelType.GLB
        )
    }
}
```

### 3. Repository Test Pattern

**✅ DO - Data source + Mapper integration:**
```kotlin
class ARObjectRepositoryImplTest {
    private lateinit var localDataSource: ARObjectLocalDataSource
    private lateinit var fileStorage: ModelFileStorage
    private lateinit var mapper: ARObjectMapper
    private lateinit var repository: ARObjectRepositoryImpl
    
    @Before
    fun setup() {
        localDataSource = mockk()
        fileStorage = mockk()
        mapper = ARObjectMapper()  // Real mapper
        repository = ARObjectRepositoryImpl(localDataSource, fileStorage, mapper)
    }
    
    @Test
    fun `getAllObjects should map DTOs to models`() = runTest {
        // GIVEN
        val dtos = listOf(
            ARObjectDTO(
                id = "1",
                name = "Chair",
                modelUri = "file://chair.glb",
                modelType = "GLB",
                createdAt = 1000L
            ),
            ARObjectDTO(
                id = "2",
                name = "Table",
                modelUri = "file://table.glb",
                modelType = "GLB",
                createdAt = 2000L
            )
        )
        coEvery { localDataSource.getAllObjects() } returns dtos
        
        // WHEN
        val result = repository.getAllObjects()
        
        // THEN
        assertTrue(result.isSuccess)
        val objects = result.getOrNull()!!
        assertEquals(2, objects.size)
        assertEquals("Chair", objects[0].name.value)
        assertEquals("Table", objects[1].name.value)
        coVerify(exactly = 1) { localDataSource.getAllObjects() }
    }
    
    @Test
    fun `getAllObjects with data source exception should return failure`() = runTest {
        // GIVEN
        coEvery { localDataSource.getAllObjects() } throws IOException("Disk error")
        
        // WHEN
        val result = repository.getAllObjects()
        
        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is StorageException)
        assertContains(result.exceptionOrNull()?.message ?: "", "Disk error")
    }
    
    @Test
    fun `importObject should copy file and save DTO`() = runTest {
        // GIVEN
        val sourceUri = "content://picker/chair.glb"
        val localUri = "file://app/models/uuid-chair.glb"
        val name = "Modern Chair"
        val modelType = ModelType.GLB
        
        coEvery { fileStorage.copyToAppStorage(sourceUri) } returns localUri
        coEvery { localDataSource.save(any()) } just Runs
        
        // WHEN
        val result = repository.importObject(sourceUri, name, modelType)
        
        // THEN
        assertTrue(result.isSuccess)
        val obj = result.getOrNull()!!
        assertEquals(name, obj.name.value)
        assertEquals(localUri, obj.modelUri.value)
        assertEquals(modelType, obj.modelType)
        
        // Verify interactions
        coVerify(exactly = 1) { fileStorage.copyToAppStorage(sourceUri) }
        coVerify(exactly = 1) { localDataSource.save(any()) }
    }
}
```

### 4. Mapper Test Pattern

**✅ DO - Bidirectional transformation:**
```kotlin
class ARObjectMapperTest {
    private val mapper = ARObjectMapper()
    
    @Test
    fun `toDTO should extract Value Object values`() {
        // GIVEN
        val model = ARObject(
            id = "test-id",
            name = ObjectName.create("Test Object").getOrThrow(),
            modelUri = ModelUri.create("file://test.glb").getOrThrow(),
            modelType = ModelType.GLB,
            thumbnailUri = "file://thumb.jpg",
            createdAt = 12345L,
            lastPlacedAt = 67890L
        )
        
        // WHEN
        val dto = mapper.toDTO(model)
        
        // THEN
        assertEquals("test-id", dto.id)
        assertEquals("Test Object", dto.name)  // Extracted from Value Object
        assertEquals("file://test.glb", dto.modelUri)
        assertEquals("GLB", dto.modelType)
        assertEquals("file://thumb.jpg", dto.thumbnailUri)
        assertEquals(12345L, dto.createdAt)
        assertEquals(67890L, dto.lastPlacedAt)
    }
    
    @Test
    fun `toModel should create Value Objects with validation`() {
        // GIVEN
        val dto = ARObjectDTO(
            id = "dto-id",
            name = "Valid Name",
            modelUri = "file://valid.glb",
            modelType = "GLB",
            createdAt = 11111L
        )
        
        // WHEN
        val model = mapper.toModel(dto)
        
        // THEN
        assertEquals("dto-id", model.id)
        assertEquals("Valid Name", model.name.value)
        assertEquals("file://valid.glb", model.modelUri.value)
        assertEquals(ModelType.GLB, model.modelType)
    }
    
    @Test
    fun `toModel with invalid DTO should throw`() {
        // GIVEN
        val invalidDto = ARObjectDTO(
            id = "bad",
            name = "",  // Invalid: blank
            modelUri = "file://test.glb",
            modelType = "GLB",
            createdAt = 1000L
        )
        
        // WHEN / THEN
        assertFailsWith<ValidationException> {
            mapper.toModel(invalidDto)
        }
    }
    
    @Test
    fun `round-trip conversion should preserve data`() {
        // GIVEN
        val originalModel = ARObject(
            id = "round-trip",
            name = ObjectName.create("Round Trip").getOrThrow(),
            modelUri = ModelUri.create("file://trip.glb").getOrThrow(),
            modelType = ModelType.GLB,
            createdAt = 99999L
        )
        
        // WHEN
        val dto = mapper.toDTO(originalModel)
        val reconstructedModel = mapper.toModel(dto)
        
        // THEN
        assertEquals(originalModel.id, reconstructedModel.id)
        assertEquals(originalModel.name.value, reconstructedModel.name.value)
        assertEquals(originalModel.modelUri.value, reconstructedModel.modelUri.value)
        assertEquals(originalModel.modelType, reconstructedModel.modelType)
    }
}
```

### 5. Test Data Builders

**✅ DO - Reusable test data factories:**
```kotlin
// commonTest/kotlin/com/trendhive/arsample/test/builders/
object TestDataBuilders {
    
    fun buildARObject(
        id: String = "test-${UUID.randomUUID()}",
        name: String = "Test Object",
        modelUri: String = "file://test.glb",
        modelType: ModelType = ModelType.GLB,
        thumbnailUri: String? = null,
        createdAt: Long = System.currentTimeMillis(),
        lastPlacedAt: Long? = null
    ): ARObject {
        return ARObject(
            id = id,
            name = ObjectName.create(name).getOrThrow(),
            modelUri = ModelUri.create(modelUri).getOrThrow(),
            modelType = modelType,
            thumbnailUri = thumbnailUri,
            createdAt = createdAt,
            lastPlacedAt = lastPlacedAt
        )
    }
    
    fun buildARObjectDTO(
        id: String = "dto-${UUID.randomUUID()}",
        name: String = "Test DTO",
        modelUri: String = "file://dto.glb",
        modelType: String = "GLB",
        createdAt: Long = System.currentTimeMillis()
    ): ARObjectDTO {
        return ARObjectDTO(
            id = id,
            name = name,
            modelUri = modelUri,
            modelType = modelType,
            createdAt = createdAt
        )
    }
    
    fun buildPlacedObject(
        id: String = "placed-${UUID.randomUUID()}",
        arObjectId: String = "obj-id",
        position: Vector3 = Vector3(0f, 0f, 0f),
        rotation: Quaternion = Quaternion(0f, 0f, 0f, 1f),
        scale: Float = 1f
    ): PlacedObject {
        return PlacedObject(
            id = id,
            arObjectId = arObjectId,
            position = position,
            rotation = rotation,
            scale = scale
        )
    }
}

// Usage in tests
class SomeTest {
    @Test
    fun `test with default data`() {
        val obj = TestDataBuilders.buildARObject()
        // Use obj in test
    }
    
    @Test
    fun `test with custom name`() {
        val obj = TestDataBuilders.buildARObject(name = "Custom Name")
        assertEquals("Custom Name", obj.name.value)
    }
}
```

### 6. Test Coverage Best Practices

**Test Pyramid:**
```
         E2E Tests (5%)
       /              \
    Integration (15%)
   /                    \
  Unit Tests (80%)
```

**Coverage Checklist:**
- [ ] **Unit Tests (80%)**: Use cases, mappers, Value Objects
- [ ] **Integration Tests (15%)**: Repository + data source, ViewModel + use cases
- [ ] **UI Tests (5%)**: Critical user flows only

**Test File Naming:**
- `[ClassName]Test.kt` - Unit tests
- `[Feature]IntegrationTest.kt` - Integration tests
- `[Screen]UITest.kt` - UI tests

---

## Çıktı

- Test dosyaları
- Test coverage raporu
- Mock data helpers
- Test data builders