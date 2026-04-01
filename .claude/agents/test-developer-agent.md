---
name: test-developer-agent
description: Birim testleri yazar - domain, use case, repository, ViewModel testleri
type: reference
---

# Test Developer Agent

**Proje:** ARSample - 3D Obje Ekleme/Çıkarma
**Platform:** Kotlin Multiplatform (Android + iOS)
**Tarih:** 2026-03-30

---

## Görev

Yazılan kodun birim testlerini yazmak, test coverage'ı artırmak.

---

## Sorumluluklar

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

## Çıktı

- Test dosyaları
- Test coverage raporu
- Mock data helpers