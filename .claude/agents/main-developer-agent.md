---
name: main-developer-agent
description: Core code development - domain, application, infrastructure, presentation layers and AR implementation
type: reference
---

# Main Developer Agent

**Project:** ARSample - 3D Object Placement/Removal
**Platform:** Kotlin Multiplatform (Android + iOS)
**Date:** 2026-03-30

---

## Mission

Develop code according to Design & Analysis Agent's design, implement for Android and iOS.

---

## Responsibilities

> **Note**: This agent is enriched with Clean Architecture best practices learned from Halleder projects. Uses Flutter-inspired patterns optimized for Kotlin Multiplatform.

### 1. Domain Layer (DDD)

**Layer Rules:**
- ✅ Contains no external dependencies (only Kotlin stdlib)
- ✅ Business logic lives in this layer
- ✅ Repository interfaces defined here (implementation in data layer)
- ✅ Validation done with Value Objects
- ✅ Use Case interfaces defined here

**Base Classes (Flutter-inspired pattern):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/domain/base/
interface BaseModel  // Marker interface for all domain models

interface BaseUseCase<in Input : BaseModel, out Output : BaseModel> {
    suspend operator fun invoke(input: Input): Result<Output>
}

interface BaseRepository  // Marker interface for repositories

abstract class BaseMapper<DTO, Model : BaseModel> {
    abstract fun toDTO(model: Model): DTO
    abstract fun toModel(dto: DTO): Model
    
    fun updateModel(existingModel: Model, dto: DTO): Model {
        return toModel(dto)
    }
}
```

**Value Objects (Domain Validation):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/domain/model/valueobjects/
sealed class ModelUri private constructor(val value: String) {
    companion object {
        fun create(uri: String): Result<ModelUri> {
            return when {
                uri.isBlank() -> Result.failure(ValidationException("URI cannot be blank"))
                !uri.matches(Regex(".*\\.(glb|usdz|fbx|obj)$")) -> 
                    Result.failure(ValidationException("Invalid model format"))
                else -> Result.success(ValidModelUri(uri))
            }
        }
    }
    private class ValidModelUri(value: String) : ModelUri(value)
}

sealed class ObjectName private constructor(val value: String) {
    companion object {
        fun create(name: String): Result<ObjectName> {
            return when {
                name.isBlank() -> Result.failure(ValidationException("Name cannot be blank"))
                name.length > 50 -> Result.failure(ValidationException("Name too long (max 50)"))
                else -> Result.success(ValidObjectName(name))
            }
        }
    }
    private class ValidObjectName(value: String) : ObjectName(value)
}
```

**Domain Exceptions:**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/domain/exception/
sealed class DomainException(message: String) : Exception(message)
class ValidationException(message: String) : DomainException(message)
class EntityNotFoundException(message: String) : DomainException(message)
class StorageException(message: String) : DomainException(message)
```

**Entities (with Value Objects):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/domain/model/
data class ARObject(
    val id: String,
    val name: ObjectName,  // Value Object
    val modelUri: ModelUri,  // Value Object
    val modelType: ModelType,
    val thumbnailUri: String? = null,
    val createdAt: Long = currentTimeMillis(),
    val lastPlacedAt: Long? = null
) : BaseModel

data class PlacedObject(
    val id: String,
    val arObjectId: String,
    val position: Vector3,
    val rotation: Quaternion,
    val scale: Float = 1f,
    val anchorId: String? = null,
    val placedAt: Long = currentTimeMillis()
) : BaseModel

data class ARScene(
    val id: String,
    val name: String,
    val placedObjects: List<PlacedObject> = emptyList(),
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis()
) : BaseModel

enum class ModelType {
    GLB, USDZ, FBX, OBJ
}

data class Vector3(val x: Float, val y: Float, val z: Float) : BaseModel
data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float) : BaseModel
```

**Repository Interfaces (Feature-based organization):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/domain/repository/
interface ARObjectRepository : BaseRepository {
    suspend fun getAllObjects(): Result<List<ARObject>>
    suspend fun getObjectById(id: String): Result<ARObject?>
    suspend fun saveObject(obj: ARObject): Result<Unit>
    suspend fun deleteObject(id: String): Result<Unit>
    suspend fun importObject(uri: String, name: String, modelType: ModelType): Result<ARObject>
}

interface ARSceneRepository : BaseRepository {
    suspend fun getCurrentScene(): Result<ARScene?>
    suspend fun saveScene(scene: ARScene): Result<Unit>
    suspend fun addObjectToScene(sceneId: String, placedObject: PlacedObject): Result<ARScene>
    suspend fun removeObjectFromScene(sceneId: String, objectId: String): Result<ARScene>
    suspend fun clearScene(sceneId: String): Result<Unit>
}
```

**Use Cases (Flutter-inspired pattern with Input/Output):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/application/usecase/
// Use case interfaces
interface ImportObjectUseCaseInterface : BaseUseCase<ImportObjectInput, ARObject>
interface GetAllObjectsUseCaseInterface : BaseUseCase<Unit, List<ARObject>>
interface PlaceObjectUseCaseInterface : BaseUseCase<PlaceObjectInput, ARScene>

// Input models
data class ImportObjectInput(
    val uri: String,
    val name: String,
    val modelType: ModelType
) : BaseModel

data class PlaceObjectInput(
    val sceneId: String,
    val objectId: String,
    val position: Vector3,
    val rotation: Quaternion,
    val scale: Float = 1f
) : BaseModel

// Use case implementations
class ImportObjectUseCase(
    private val repository: ARObjectRepository
) : ImportObjectUseCaseInterface {
    
    override suspend fun invoke(input: ImportObjectInput): Result<ARObject> {
        // Validate using Value Objects
        val nameResult = ObjectName.create(input.name)
        if (nameResult.isFailure) return Result.failure(nameResult.exceptionOrNull()!!)
        
        val uriResult = ModelUri.create(input.uri)
        if (uriResult.isFailure) return Result.failure(uriResult.exceptionOrNull()!!)
        
        return repository.importObject(input.uri, input.name, input.modelType)
    }
}

class GetAllObjectsUseCase(
    private val repository: ARObjectRepository
) : GetAllObjectsUseCaseInterface {
    override suspend fun invoke(input: Unit): Result<List<ARObject>> {
        return repository.getAllObjects()
    }
}

class PlaceObjectInSceneUseCase(
    private val sceneRepository: ARSceneRepository,
    private val objectRepository: ARObjectRepository
) : PlaceObjectUseCaseInterface {
    
    override suspend fun invoke(input: PlaceObjectInput): Result<ARScene> {
        // Verify object exists
        val objectResult = objectRepository.getObjectById(input.objectId)
        if (objectResult.isFailure) return Result.failure(objectResult.exceptionOrNull()!!)
        
        val obj = objectResult.getOrNull()
            ?: return Result.failure(EntityNotFoundException("Object not found"))
        
        val placedObject = PlacedObject(
            id = generateId(),
            arObjectId = input.objectId,
            position = input.position,
            rotation = input.rotation,
            scale = input.scale
        )
        
        return sceneRepository.addObjectToScene(input.sceneId, placedObject)
    }
}
```
    suspend operator fun invoke(sceneId: String, objectId: String, position: Vector3, rotation: Quaternion): Result<ARScene>
}

class RemoveObjectFromSceneUseCase(private val sceneRepository: ARSceneRepository) {
    suspend operator fun invoke(sceneId: String, placedObjectId: String): Result<ARScene>
}

class GetCurrentSceneUseCase(private val sceneRepository: ARSceneRepository) {
    suspend operator fun invoke(): ARScene?
}

class SaveSceneUseCase(private val sceneRepository: ARSceneRepository) {
    suspend operator fun invoke(scene: ARScene)
}
```

---

### 2. Application Layer (NEW - DDD Pattern)

**Layer Rules:**
- ✅ Orchestrates domain objects and repositories
- ✅ Contains use cases (business workflows)
- ✅ Application services for coordination
- ✅ Input/Output DTOs for use cases
- ✅ Depends ONLY on domain layer
- ❌ NO domain logic here (only orchestration)
- ❌ NO infrastructure dependencies

**Use Case Pattern:**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/application/usecase/
// Each use case: Interface + Implementation
interface ImportObjectUseCaseInterface : BaseUseCase<ImportObjectInput, ARObject>

class ImportObjectUseCase(
    private val repository: ARObjectRepository  // Domain interface
) : ImportObjectUseCaseInterface {
    override suspend fun invoke(input: ImportObjectInput): Result<ARObject> {
        // 1. Validate using domain Value Objects
        val nameResult = ObjectName.create(input.name)
        if (nameResult.isFailure) return Result.failure(nameResult.exceptionOrNull()!!)
        
        val uriResult = ModelUri.create(input.uri)
        if (uriResult.isFailure) return Result.failure(uriResult.exceptionOrNull()!!)
        
        // 2. Orchestrate domain repository
        return repository.importObject(input.uri, input.name, input.modelType)
    }
}

// Input/Output models in application layer
data class ImportObjectInput(
    val uri: String,
    val name: String,
    val modelType: ModelType
) : BaseModel
```

**Application Services (Complex Workflows):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/application/service/
class ARSceneService(
    private val sceneRepository: ARSceneRepository,
    private val objectRepository: ARObjectRepository
) {
    suspend fun placeObjectWithValidation(
        sceneId: String,
        objectId: String,
        position: Vector3
    ): Result<ARScene> {
        // Multi-step orchestration
        val objectResult = objectRepository.getObjectById(objectId)
        if (objectResult.isFailure) return Result.failure(objectResult.exceptionOrNull()!!)
        
        val sceneResult = sceneRepository.getCurrentScene()
        if (sceneResult.isFailure) return Result.failure(sceneResult.exceptionOrNull()!!)
        
        // Business rule: Max 10 objects per scene
        val scene = sceneResult.getOrNull()
        if (scene != null && scene.placedObjects.size >= 10) {
            return Result.failure(ValidationException("Scene is full"))
        }
        
        val placedObject = PlacedObject(
            id = generateId(),
            arObjectId = objectId,
            position = position,
            rotation = Quaternion(0f, 0f, 0f, 1f),
            scale = 1f
        )
        
        return sceneRepository.addObjectToScene(sceneId, placedObject)
    }
}
```

---

### 3. Infrastructure Layer (Renamed from Data Layer)

**Layer Rules:**
- ✅ Technical implementations (database, file system, AR platform)
- ✅ Repository implementations (ARObjectRepositoryImpl)
- ✅ Data sources (local storage, file system)
- ✅ DTOs and Mappers
- ✅ Depends on domain layer
- ❌ NO business logic here

**DTOs (Data Transfer Objects):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/infrastructure/persistence/dto/
@Serializable
data class ARObjectDTO(
    val id: String,
    val name: String,
    val modelUri: String,
    val modelType: String,
    val thumbnailUri: String? = null,
    val createdAt: Long,
    val lastPlacedAt: Long? = null
)

@Serializable
data class PlacedObjectDTO(
    val id: String,
    val arObjectId: String,
    val posX: Float, val posY: Float, val posZ: Float,
    val rotX: Float, val rotY: Float, val rotZ: Float, val rotW: Float,
    val scale: Float,
    val anchorId: String? = null,
    val placedAt: Long
)

@Serializable
data class ARSceneDTO(
    val id: String,
    val name: String,
    val placedObjects: List<PlacedObjectDTO>,
    val createdAt: Long,
    val updatedAt: Long
)
```

**Mappers (DTO ↔ Model transformation):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/infrastructure/persistence/mapper/
class ARObjectMapper : BaseMapper<ARObjectDTO, ARObject> {
    override fun toDTO(model: ARObject): ARObjectDTO {
        return ARObjectDTO(
            id = model.id,
            name = model.name.value,
            modelUri = model.modelUri.value,
            modelType = model.modelType.name,
            thumbnailUri = model.thumbnailUri,
            createdAt = model.createdAt,
            lastPlacedAt = model.lastPlacedAt
        )
    }
    
    override fun toModel(dto: ARObjectDTO): ARObject {
        return ARObject(
            id = dto.id,
            name = ObjectName.create(dto.name).getOrThrow(),
            modelUri = ModelUri.create(dto.modelUri).getOrThrow(),
            modelType = ModelType.valueOf(dto.modelType),
            thumbnailUri = dto.thumbnailUri,
            createdAt = dto.createdAt,
            lastPlacedAt = dto.lastPlacedAt
        )
    }
}

class ARSceneMapper(
    private val placedObjectMapper: PlacedObjectMapper
) : BaseMapper<ARSceneDTO, ARScene> {
    override fun toDTO(model: ARScene): ARSceneDTO {
        return ARSceneDTO(
            id = model.id,
            name = model.name,
            placedObjects = model.placedObjects.map { placedObjectMapper.toDTO(it) },
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
    
    override fun toModel(dto: ARSceneDTO): ARScene {
        return ARScene(
            id = dto.id,
            name = dto.name,
            placedObjects = dto.placedObjects.map { placedObjectMapper.toModel(it) },
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
}
```

**Repository Implementations (with error handling):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/infrastructure/persistence/repository/
class ARObjectRepositoryImpl(
    private val localDataSource: ARObjectLocalDataSource,
    private val fileStorage: ModelFileStorage,
    private val mapper: ARObjectMapper
) : ARObjectRepository {
    
    override suspend fun getAllObjects(): Result<List<ARObject>> {
        return try {
            val dtos = localDataSource.getAllObjects()
            val models = dtos.map { mapper.toModel(it) }
            Result.success(models)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to load objects: ${e.message}"))
        }
    }
    
    override suspend fun getObjectById(id: String): Result<ARObject?> {
        return try {
            val dto = localDataSource.getObjectById(id)
            val model = dto?.let { mapper.toModel(it) }
            Result.success(model)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to load object: ${e.message}"))
        }
    }
    
    override suspend fun saveObject(obj: ARObject): Result<Unit> {
        return try {
            val dto = mapper.toDTO(obj)
            localDataSource.saveObject(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to save object: ${e.message}"))
        }
    }
    
    override suspend fun deleteObject(id: String): Result<Unit> {
        return try {
            val dto = localDataSource.getObjectById(id)
            dto?.let {
                fileStorage.deleteModel(it.modelUri)
                localDataSource.deleteObject(id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to delete object: ${e.message}"))
        }
    }
    
    override suspend fun importObject(
        uri: String, 
        name: String, 
        modelType: ModelType
    ): Result<ARObject> {
        return try {
            // Validate using Value Objects
            val nameResult = ObjectName.create(name)
            if (nameResult.isFailure) return Result.failure(nameResult.exceptionOrNull()!!)
            
            val uriResult = ModelUri.create(uri)
            if (uriResult.isFailure) return Result.failure(uriResult.exceptionOrNull()!!)
            
            // Read and save model file
            val modelData = fileStorage.readFromUri(uri)
            val fileName = "${generateId()}.${modelType.name.lowercase()}"
            val savedPath = fileStorage.saveModel(modelData, fileName)
            
            // Create and save entity
            val newObject = ARObject(
                id = generateId(),
                name = nameResult.getOrThrow(),
                modelUri = ModelUri.create(savedPath).getOrThrow(),
                modelType = modelType,
                createdAt = currentTimeMillis()
            )
            
            val dto = mapper.toDTO(newObject)
            localDataSource.saveObject(dto)
            
            Result.success(newObject)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to import object: ${e.message}"))
        }
    }
}
```

**Data Source Interfaces:**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/infrastructure/persistence/datasource/
interface ARObjectLocalDataSource {
    suspend fun getAllObjects(): List<ARObjectDTO>
    suspend fun getObjectById(id: String): ARObjectDTO?
    suspend fun saveObject(dto: ARObjectDTO)
    suspend fun deleteObject(id: String)
}

interface ModelFileStorage {
    suspend fun saveModel(data: ByteArray, fileName: String): String
    suspend fun deleteModel(filePath: String): Boolean
    suspend fun readFromUri(uri: String): ByteArray
    fun listModels(): List<String>
}

interface ARSceneDataStore {
    suspend fun saveScene(dto: ARSceneDTO)
    suspend fun getScene(): ARSceneDTO?
    suspend fun clearScene()
}
```

---

### 3. Presentation Layer (MVVM)

**ViewModels (State management):**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/presentation/viewmodel/
class ARViewModel(
    private val placeObjectUseCase: PlaceObjectUseCaseInterface,
    private val removeObjectUseCase: RemoveObjectUseCaseInterface,
    private val getCurrentSceneUseCase: GetCurrentSceneUseCaseInterface
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ARUiState>(ARUiState.Initial)
    val uiState: StateFlow<ARUiState> = _uiState.asStateFlow()
    
    fun placeObject(objectId: String, position: Vector3, rotation: Quaternion) {
        viewModelScope.launch {
            _uiState.value = ARUiState.Loading
            
            val input = PlaceObjectInput(
                sceneId = "default",
                objectId = objectId,
                position = position,
                rotation = rotation
            )
            
            placeObjectUseCase(input).fold(
                onSuccess = { scene ->
                    _uiState.value = ARUiState.Success(scene)
                },
                onFailure = { error ->
                    _uiState.value = ARUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}

// UI State (sealed class)
sealed class ARUiState {
    object Initial : ARUiState()
    object Loading : ARUiState()
    data class Success(val scene: ARScene) : ARUiState()
    data class Error(val message: String) : ARUiState()
}
```

---

## Project Structure (DDD Pattern - Eric Evans)

```
composeApp/src/
├── commonMain/kotlin/com/trendhive/arsample/
│   ├── domain/                          # Domain Layer (Pure Business Logic)
│   │   ├── base/
│   │   │   ├── BaseModel.kt
│   │   │   ├── BaseUseCase.kt          # Marker interface
│   │   │   ├── BaseRepository.kt
│   │   │   └── BaseMapper.kt
│   │   ├── exception/
│   │   │   ├── DomainException.kt
│   │   │   ├── ValidationException.kt
│   │   │   ├── EntityNotFoundException.kt
│   │   │   └── StorageException.kt
│   │   ├── model/                       # Entities & Value Objects
│   │   │   ├── ARObject.kt
│   │   │   ├── PlacedObject.kt
│   │   │   ├── ARScene.kt
│   │   │   ├── Vector3.kt
│   │   │   ├── Quaternion.kt
│   │   │   ├── ModelType.kt
│   │   │   └── valueobjects/
│   │   │       ├── ModelUri.kt
│   │   │       └── ObjectName.kt
│   │   └── repository/                  # Repository interfaces ONLY
│   │       ├── ARObjectRepository.kt
│   │       └── ARSceneRepository.kt
│   │
│   ├── application/                     # Application Layer (NEW - Use Cases)
│   │   ├── usecase/                     # Business workflows
│   │   │   ├── ImportObjectUseCase.kt
│   │   │   ├── GetAllObjectsUseCase.kt
│   │   │   ├── DeleteObjectUseCase.kt
│   │   │   ├── PlaceObjectInSceneUseCase.kt
│   │   │   ├── RemoveObjectFromSceneUseCase.kt
│   │   │   ├── GetSceneUseCase.kt
│   │   │   └── SaveSceneUseCase.kt
│   │   ├── service/                     # Application services (complex workflows)
│   │   │   └── ARSceneService.kt
│   │   └── dto/                         # Input/Output DTOs for use cases
│   │       ├── ImportObjectInput.kt
│   │       └── PlaceObjectInput.kt
│   │
│   ├── infrastructure/                  # Infrastructure Layer (Technical Implementations)
│   │   └── persistence/
│   │       ├── dto/                     # Database DTOs
│   │       │   ├── ARObjectDTO.kt
│   │       │   ├── PlacedObjectDTO.kt
│   │       │   └── ARSceneDTO.kt
│   │       ├── mapper/                  # DTO ↔ Domain Model mappers
│   │       │   ├── ARObjectMapper.kt
│   │       │   ├── PlacedObjectMapper.kt
│   │       │   └── ARSceneMapper.kt
│   │       ├── repository/              # Repository implementations
│   │       │   ├── ARObjectRepositoryImpl.kt
│   │       │   └── ARSceneRepositoryImpl.kt
│   │       └── datasource/              # Data source interfaces
│   │           ├── ARObjectLocalDataSource.kt
│   │           ├── ModelFileStorage.kt
│   │           └── ARSceneDataStore.kt
│   │
│   └── presentation/                    # Presentation Layer (UI)
│       ├── viewmodel/
│       │   ├── ARViewModel.kt
│       │   └── ObjectListViewModel.kt
│       └── ui/
│           ├── screens/
│           │   ├── ARScreen.kt
│           │   └── ObjectListScreen.kt
│           └── components/
│               ├── ImportDialog.kt
│               └── ObjectListItem.kt
│
├── androidMain/kotlin/com/trendhive/arsample/
│   ├── infrastructure/
│   │   └── persistence/                 # Android implementations
│   │       ├── ARObjectLocalDataSourceImpl.kt
│   │       ├── ModelFileStorageImpl.kt
│   │       └── ARSceneDataStoreImpl.kt
│   ├── ar/                              # AR platform code
│   │   ├── ARSessionManager.kt
│   │   └── ARView.kt
│   └── MainActivity.kt
│
└── iosMain/kotlin/com/trendhive/arsample/
    ├── infrastructure/
    │   └── persistence/                 # iOS implementations
    │       ├── ARObjectLocalDataSourceIOSImpl.kt
    │       ├── ModelFileStorageIOSImpl.kt
    │       └── ARSceneDataStoreIOSImpl.kt
    ├── ar/                              # AR platform code
    │   ├── ARSessionManager.kt
    │   └── ARViewWrapper.kt
    └── MainViewController.kt
```

---

## Key Patterns ve Prensipler

### 1. Value Objects (DDD)
- Domain validation için sealed class pattern
- İmutability ve type safety
- Örnek: `ModelUri`, `ObjectName`

### 2. Result<T> Pattern
- Kotlin'in native Result kullanımı
- Error handling için functional approach
- Örnek: `Result.success()`, `Result.failure()`

### 3. Base Classes
- `BaseModel`: Tüm domain model'ler için marker
- `BaseUseCase<Input, Output>`: Use case standardı
- `BaseMapper<DTO, Model>`: DTO ↔ Model dönüşümü
- `BaseRepository`: Repository marker

### 4. Feature-based Organization
- Her feature kendi klasöründe (dto/, mapper/, repository/, usecase/)
- Clear separation of concerns
- Easy to navigate and maintain

### 5. Interface Segregation
- Her component'in interface'i var
- Implementation detayları gizli
- Test edilebilirlik artar

### 6. Exception Hierarchy
- `DomainException` base class
- Specialized exceptions: `ValidationException`, `StorageException`, etc.
- Type-safe error handling

---

## Raporlama

Main Developer şu agentlarla işbirliği yapar:

1. **Design & Analysis Agent'a** → Tasarım soruları sorar
2. **Android/iOS Expert Agent'lardan** → Platform-specific implementasyon detayları ister
3. **Test Developer'a** → Test gereksinimlerini iletir
4. **Code Reviewer'a** → Kod gönderir

---

## Output Files

- **Domain layer** (base/, exception/, model/, repository/)
- **Application layer** (usecase/, service/, dto/)
- **Infrastructure layer** (persistence/dto/, persistence/mapper/, persistence/repository/, persistence/datasource/)
- **Presentation layer** (viewmodel/, ui/)
- **Platform-specific** implementations (androidMain/, iosMain/)

---

### 4. Presentation Layer (MVVM)

**ViewModels:**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/presentation/viewmodel/
class ARViewModel(
    private val placeObjectUseCase: PlaceObjectInSceneUseCase,
    private val removeObjectUseCase: RemoveObjectFromSceneUseCase,
    private val getCurrentSceneUseCase: GetCurrentSceneUseCase
) : ViewModel {

    private val _uiState = MutableStateFlow(ARUiState())
    val uiState: StateFlow<ARUiState> = _uiState.asStateFlow()

    fun placeObject(objectId: String, position: Vector3, rotation: Quaternion) { ... }
    fun removeObject(placedObjectId: String) { ... }
    fun loadScene() { ... }
}

class ObjectListViewModel(
    private val getAllObjectsUseCase: GetAllObjectsUseCase,
    private val deleteObjectUseCase: DeleteObjectUseCase,
    private val importObjectUseCase: ImportObjectUseCase
) : ViewModel {

    private val _objects = MutableStateFlow<List<ARObject>>(emptyList())
    val objects: StateFlow<List<ARObject>> = _objects.asStateFlow()

    fun loadObjects() { ... }
    fun importObject(uri: String, name: String) { ... }
    fun deleteObject(id: String) { ... }
}

data class ARUiState(
    val currentScene: ARScene? = null,
    val placedObjects: List<PlacedObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**UI Screens:**
```kotlin
// commonMain/kotlin/com/trendhive/arsample/presentation/ui/
@Composable
fun ARScreen(
    viewModel: ARViewModel,
    onNavigateBack: () -> Unit
)

@Composable
fun ObjectListScreen(
    viewModel: ObjectListViewModel,
    onObjectSelected: (String) -> Unit,
    onStartAR: () -> Unit
)
```

---

### 4. Platform-Specific AR

**Android (ARCore/SceneView):**
```kotlin
// androidMain/kotlin/com/trendhive/arsample/ar/
class ARSessionManager {
    fun resume() { ... }
    fun pause() { ... }
    fun placeModel(modelPath: String, anchor: Anchor) { ... }
    fun removeModel(anchorId: String) { ... }
    fun raycast(x: Float, y: Float): HitResult? { ... }
}
```

**iOS (ARKit/RealityKit):**
```swift
// iosMain/kotlin/com/trendhive/arsample/ar/
class ARSessionManager {
    func resume() { ... }
    func pause() { ... }
    func placeModel(modelPath: String, anchor: ARAnchor) { ... }
    func removeModel(anchorId: UUID) { ... }
    func raycast(point: CGPoint) -> ARRaycastResult? { ... }
}
```

---

## Raporlama

1. Design & Analysis Agent'a tasarım soruları sorar
2. Android/iOS Expert Agent'lardan rapor ister
3. Code Reviewer'a kod gönderir
4. Test Developer'a test gereksinimlerini iletir

---

## Çıktı

- Domain katmanı dosyaları
- Data katmanı dosyaları
- Presentation katmanı dosyaları
- Platform-specific dosyalar

---

## Project Structure (Duplicate - Remove This Section)

```
composeApp/src/
├── commonMain/kotlin/com/trendhive/arsample/
│   ├── domain/
│   │   ├── model/                       # Entities & Value Objects
│   │   │   ├── ARObject.kt
│   │   │   ├── PlacedObject.kt
│   │   │   ├── ARScene.kt
│   │   │   ├── Vector3.kt
│   │   │   └── Quaternion.kt
│   │   └── repository/                  # Repository interfaces
│   │       ├── ARObjectRepository.kt
│   │       └── ARSceneRepository.kt
│   ├── application/                     # Use Cases (NEW)
│   │   └── usecase/
│   │       ├── ImportObjectUseCase.kt
│   │       ├── GetAllObjectsUseCase.kt
│   │       ├── DeleteObjectUseCase.kt
│   │       ├── PlaceObjectInSceneUseCase.kt
│   │       ├── RemoveObjectFromSceneUseCase.kt
│   │       ├── GetCurrentSceneUseCase.kt
│   │       └── SaveSceneUseCase.kt
│   ├── infrastructure/                  # Repository Implementations
│   │   └── persistence/
│   │       ├── repository/
│   │       │   ├── ARObjectRepositoryImpl.kt
│   │       │   └── ARSceneRepositoryImpl.kt
│   │       ├── datasource/
│   │       │   ├── ModelFileStorage.kt
│   │       │   └── ARSceneDataStore.kt
│   │       └── mapper/
│   │           └── ARObjectMapper.kt
│   └── presentation/
│       ├── viewmodel/
│       │   ├── ARViewModel.kt
│       │   └── ObjectListViewModel.kt
│       └── ui/
│           ├── screens/
│           │   ├── ARScreen.kt
│           │   └── ObjectListScreen.kt
│           └── components/
│               ├── ARViewComponent.kt
│               ├── ObjectListItem.kt
│               └── ImportDialog.kt
├── androidMain/kotlin/com/trendhive/arsample/
│   ├── infrastructure/
│   │   └── persistence/
│   │       └── ARObjectRepositoryImpl.kt
│   └── ar/
│       ├── ARSessionManager.kt
│       └── ARView.kt
└── iosMain/kotlin/com/trendhive/arsample/
    ├── infrastructure/
    │   └── persistence/
    │       └── ARObjectRepositoryImpl.kt
    └── ar/
        ├── ARSessionManager.kt
        └── ARViewWrapper.swift
```

---

## 🚀 Best Practices (Halleder'den Öğrenilenler)

### 1. Use Case Implementation Pattern

**✅ DO - İyi Örnek:**
```kotlin
class CreateFeatureUseCase(
    private val repository: FeatureRepository,
    private val validator: FeatureValidator
) : CreateFeatureUseCaseInterface {
    
    override suspend fun invoke(input: CreateFeatureInput): Result<Feature> {
        // 1. Input validation
        val nameResult = FeatureName.create(input.name)
        if (nameResult.isFailure) {
            return Result.failure(nameResult.exceptionOrNull()!!)
        }
        
        // 2. Business rules check
        val existingFeature = repository.findByName(input.name)
        if (existingFeature.isSuccess && existingFeature.getOrNull() != null) {
            return Result.failure(ValidationException("Feature already exists"))
        }
        
        // 3. Execute operation
        return repository.create(input)
    }
}
```

**❌ DON'T - Kötü Örnek:**
```kotlin
class CreateFeatureUseCase(private val repository: FeatureRepository) {
    // ❌ Result<T> yerine exception throw
    // ❌ Value Object validation yok
    // ❌ Business rule check yok
    suspend fun execute(name: String): Feature {
        if (name.isEmpty()) throw IllegalArgumentException()
        return repository.create(name)
    }
}
```

### 2. Repository Implementation Pattern

**✅ DO - Mapper ile DTO dönüşümü:**
```kotlin
class ARObjectRepositoryImpl(
    private val localDataSource: ARObjectLocalDataSource,
    private val fileStorage: ModelFileStorage,
    private val mapper: ARObjectMapper
) : ARObjectRepository {
    
    override suspend fun getAllObjects(): Result<List<ARObject>> {
        return try {
            val dtos = localDataSource.getAllObjects()
            val models = dtos.map { mapper.toModel(it) }
            Result.success(models)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed to load: ${e.message}"))
        }
    }
    
    override suspend fun importObject(
        uri: String,
        name: String,
        modelType: ModelType
    ): Result<ARObject> {
        return try {
            // 1. Copy file to app storage
            val localUri = fileStorage.copyToAppStorage(uri)
            
            // 2. Create domain entity
            val arObject = ARObject(
                id = UUID.randomUUID().toString(),
                name = ObjectName.create(name).getOrThrow(),
                modelUri = ModelUri.create(localUri).getOrThrow(),
                modelType = modelType,
                createdAt = System.currentTimeMillis()
            )
            
            // 3. Convert to DTO and save
            val dto = mapper.toDTO(arObject)
            localDataSource.save(dto)
            
            Result.success(arObject)
        } catch (e: Exception) {
            Result.failure(StorageException("Import failed: ${e.message}"))
        }
    }
}
```

### 3. Mapper Best Practices

**✅ DO - Null-safe dönüşüm:**
```kotlin
class ARObjectMapper : BaseMapper<ARObjectDTO, ARObject> {
    override fun toDTO(model: ARObject): ARObjectDTO {
        return ARObjectDTO(
            id = model.id,
            name = model.name.value,  // Value Object'ten çıkar
            modelUri = model.modelUri.value,
            modelType = model.modelType.name,
            thumbnailUri = model.thumbnailUri,  // Nullable
            createdAt = model.createdAt,
            lastPlacedAt = model.lastPlacedAt
        )
    }
    
    override fun toModel(dto: ARObjectDTO): ARObject {
        return ARObject(
            id = dto.id,
            name = ObjectName.create(dto.name).getOrThrow(),  // Validate!
            modelUri = ModelUri.create(dto.modelUri).getOrThrow(),
            modelType = ModelType.valueOf(dto.modelType),
            thumbnailUri = dto.thumbnailUri,
            createdAt = dto.createdAt,
            lastPlacedAt = dto.lastPlacedAt
        )
    }
    
    // Bonus: Partial update helper
    fun updateModel(existing: ARObject, dto: ARObjectDTO): ARObject {
        return existing.copy(
            name = ObjectName.create(dto.name).getOrElse { existing.name },
            lastPlacedAt = dto.lastPlacedAt ?: existing.lastPlacedAt
        )
    }
}
```

### 4. ViewModel State Management

**✅ DO - Immutable state, sealed class:**
```kotlin
sealed class ObjectListState {
    object Loading : ObjectListState()
    data class Success(val objects: List<ARObject>) : ObjectListState()
    data class Error(val message: String) : ObjectListState()
}

class ObjectListViewModel(
    private val getAllObjectsUseCase: GetAllObjectsUseCase,
    private val importObjectUseCase: ImportObjectUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<ObjectListState>(ObjectListState.Loading)
    val state: StateFlow<ObjectListState> = _state.asStateFlow()
    
    init {
        loadObjects()
    }
    
    fun loadObjects() {
        viewModelScope.launch {
            _state.value = ObjectListState.Loading
            
            val result = getAllObjectsUseCase.invoke(Unit)
            _state.value = result.fold(
                onSuccess = { ObjectListState.Success(it) },
                onFailure = { ObjectListState.Error(it.message ?: "Unknown error") }
            )
        }
    }
    
    fun importObject(uri: String, name: String, modelType: ModelType) {
        viewModelScope.launch {
            val input = ImportObjectInput(uri, name, modelType)
            val result = importObjectUseCase.invoke(input)
            
            result.fold(
                onSuccess = { loadObjects() },  // Refresh list
                onFailure = { _state.value = ObjectListState.Error(it.message ?: "Import failed") }
            )
        }
    }
}
```

### 5. Error Handling Pattern

**✅ DO - Typed exceptions, Result<T>:**
```kotlin
// Domain exceptions
sealed class DomainException(message: String) : Exception(message)
class ValidationException(message: String) : DomainException(message)
class EntityNotFoundException(message: String) : DomainException(message)
class StorageException(message: String) : DomainException(message)
class NetworkException(message: String) : DomainException(message)

// Use case error handling
suspend fun invoke(input: Input): Result<Output> {
    return try {
        // Validation
        val validated = validate(input)
        if (validated.isFailure) {
            return Result.failure(validated.exceptionOrNull()!!)
        }
        
        // Business logic
        val result = repository.execute(validated.getOrThrow())
        result
    } catch (e: Exception) {
        when (e) {
            is ValidationException -> Result.failure(e)
            is EntityNotFoundException -> Result.failure(e)
            else -> Result.failure(StorageException("Unexpected error: ${e.message}"))
        }
    }
}

// ViewModel error handling
result.fold(
    onSuccess = { /* Handle success */ },
    onFailure = { exception ->
        val errorMessage = when (exception) {
            is ValidationException -> "Validation error: ${exception.message}"
            is EntityNotFoundException -> "Not found: ${exception.message}"
            is StorageException -> "Storage error: ${exception.message}"
            else -> "Unknown error: ${exception.message}"
        }
        _state.value = State.Error(errorMessage)
    }
)
```

### 6. Testing Pattern (inspired by BLoC test)

**✅ DO - Given-When-Then pattern:**
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
    fun `invoke with valid input should return success`() = runTest {
        // GIVEN
        val input = ImportObjectInput(
            uri = "file://models/chair.glb",
            name = "Chair",
            modelType = ModelType.GLB
        )
        val expected = ARObject(/*...*/)
        coEvery { repository.importObject(any(), any(), any()) } returns Result.success(expected)
        
        // WHEN
        val result = useCase.invoke(input)
        
        // THEN
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { repository.importObject(input.uri, input.name, input.modelType) }
    }
    
    @Test
    fun `invoke with invalid name should return validation error`() = runTest {
        // GIVEN
        val input = ImportObjectInput(
            uri = "file://models/chair.glb",
            name = "",  // Invalid: empty
            modelType = ModelType.GLB
        )
        
        // WHEN
        val result = useCase.invoke(input)
        
        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.importObject(any(), any(), any()) }
    }
}
```

### 7. Code Organization Checklist

**Domain Layer Checklist:**
- [ ] Entities have Value Objects for validation
- [ ] Repository interfaces defined (no implementations)
- [ ] Use case interfaces + implementations
- [ ] No platform-specific code (pure Kotlin)
- [ ] Exception hierarchy defined
- [ ] Input/Output models for use cases

**Data Layer Checklist:**
- [ ] DTOs with @Serializable annotation
- [ ] Mappers implement BaseMapper
- [ ] Repository implementations inject data sources
- [ ] Exception handling with try-catch
- [ ] Result<T> return types

**Presentation Layer Checklist:**
- [ ] ViewModels use StateFlow
- [ ] Sealed class for state
- [ ] Use cases injected via constructor
- [ ] Error messages user-friendly
- [ ] Loading states handled

### 8. Dependency Injection Pattern

**✅ DO - Manual DI (Kotlin Multiplatform compatible):**
```kotlin
// DI Container (commonMain)
object AppContainer {
    // Data sources (platform-specific)
    lateinit var arObjectLocalDataSource: ARObjectLocalDataSource
    lateinit var fileStorage: ModelFileStorage
    
    // Mappers (shared)
    val arObjectMapper = ARObjectMapper()
    
    // Repositories (shared, uses platform data sources)
    val arObjectRepository: ARObjectRepository by lazy {
        ARObjectRepositoryImpl(
            arObjectLocalDataSource,
            fileStorage,
            arObjectMapper
        )
    }
    
    // Use cases (shared)
    val importObjectUseCase by lazy {
        ImportObjectUseCase(arObjectRepository)
    }
    
    val getAllObjectsUseCase by lazy {
        GetAllObjectsUseCase(arObjectRepository)
    }
    
    // ViewModels (presentation)
    fun createObjectListViewModel(): ObjectListViewModel {
        return ObjectListViewModel(
            getAllObjectsUseCase,
            importObjectUseCase
        )
    }
}

// Platform initialization (androidMain)
actual fun initializePlatform() {
    AppContainer.arObjectLocalDataSource = AndroidARObjectLocalDataSource(context)
    AppContainer.fileStorage = AndroidModelFileStorage(context)
}

// Platform initialization (iosMain)
actual fun initializePlatform() {
    AppContainer.arObjectLocalDataSource = IOSARObjectLocalDataSource()
    AppContainer.fileStorage = IOSModelFileStorage()
}
```

---

## 📚 Referanslar

**Clean Architecture Kaynaklar:**
- Domain Driven Design (DDD) principles
- SOLID principles
- Dependency Rule: Dependencies point inward (Domain ← Data ← Presentation)

**Pattern Kaynakları:**
- Repository Pattern (Martin Fowler)
- Use Case Pattern (Clean Architecture)
- Value Object Pattern (DDD)
- Mapper Pattern (DTO transformation)
- State Management (Unidirectional data flow)

**Bu dokümantasyon Halleder projelerinden (Flutter/Dart) öğrenilen Clean Architecture best practice'leri ile zenginleştirilmiştir.**
---

## CRITICAL RULES

### Rule 1: Verify Before Completion
**Your task is NOT complete until:**
1. `./gradlew :composeApp:assembleDebug` = BUILD SUCCESSFUL
2. `./gradlew :composeApp:testDebugUnitTest` = All tests pass
3. `./gradlew :composeApp:compileKotlinIosArm64` = BUILD SUCCESSFUL

### Rule 2: Fix-Verify Loop
After making changes, ALWAYS run verification:
```bash
./gradlew :composeApp:assembleDebug :composeApp:compileKotlinIosArm64
```

If errors exist:
1. Fix the error
2. Re-run verification
3. Repeat until BUILD SUCCESSFUL

### Rule 3: Never Leave Broken Code
- Do NOT report "done" if build fails
- Do NOT skip compilation check
- Do NOT assume code works without verification
