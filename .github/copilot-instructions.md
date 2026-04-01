# Copilot Instructions for ARSample

## Build, Test, and Lint Commands

**Android:**
```shell
./gradlew :composeApp:assembleDebug
```

**iOS:**
Open `iosApp/iosApp.xcodeproj` in Xcode and run from there.

**Run all tests:**
```shell
./gradlew :composeApp:testDebugUnitTest
```

**Run a single test class:**
```shell
./gradlew :composeApp:testDebugUnitTest --tests "com.trendhive.arsample.domain.usecase.ImportObjectUseCaseTest"
```

**Run a single test method:**
```shell
./gradlew :composeApp:testDebugUnitTest --tests "com.trendhive.arsample.domain.usecase.ImportObjectUseCaseTest.testImportValidObject"
```

## Architecture Overview

This is a **Kotlin Multiplatform** project targeting Android (ARCore) and iOS (ARKit) with Jetpack Compose UI. The architecture follows **DDD + Clean Architecture + MVVM**.

### Layer Structure

```
composeApp/src/
├── commonMain/kotlin/com/trendhive/arsample/
│   ├── domain/           # Domain Layer (Entities, UseCases, Repository Interfaces)
│   ├── data/             # Data Layer (Repository Implementations, Local Storage)
│   └── presentation/     # Presentation Layer (ViewModels, UI Screens)
├── androidMain/          # Android-specific (ARCore/SceneView)
└── iosMain/              # iOS-specific (ARKit/RealityKit)
```

### Platform-Specific Code

- Uses Kotlin's `expect`/`actual` pattern for platform-specific implementations
- `commonMain/` contains `expect` declarations (e.g., `Platform.kt`, `ModelFilePicker.kt`, `TimeProvider.kt`)
- `androidMain/` and `iosMain/` contain `actual` implementations
- AR functionality is abstracted via `PlatformARView` interface

### Key Abstractions

- **Domain Layer:** Pure business logic, no platform dependencies
  - Entities: `ARObject`, `ARScene`, `PlacedObject`, `Vector3`, `Quaternion`
  - Use Cases: Single-responsibility classes with `invoke()` operator (e.g., `ImportObjectUseCase`, `PlaceObjectInSceneUseCase`)
  - Repository Interfaces: Define contracts for data access

- **Data Layer:** Implements repositories and handles persistence
  - `ARObjectRepository` → `ARObjectRepositoryImpl` (manages 3D model files)
  - `ARSceneRepository` → `ARSceneRepositoryImpl` (manages AR scene state)
  - Local storage via DataStore (Android) and UserDefaults (iOS)

- **Presentation Layer:** UI and ViewModels
  - `ObjectListViewModel` - manages imported objects list
  - `ARViewModel` - manages AR scene state and interactions
  - Screens: `ObjectListScreen`, `ARScreen`

## Project-Specific Conventions

### Use Case Pattern

All use cases follow this pattern:
- Interface + Implementation separation
- Typed input/output: `BaseUseCase<Input : BaseModel, Output : BaseModel>`
- Constructor injection of dependencies (repositories)
- `suspend operator fun invoke(input: Input): Result<Output>` as entry point
- Validation using Value Objects before calling repository
- Return `Result<T>` for error handling

Example:
```kotlin
interface ImportObjectUseCaseInterface : BaseUseCase<ImportObjectInput, ARObject>

class ImportObjectUseCase(
    private val repository: ARObjectRepository
) : ImportObjectUseCaseInterface {
    override suspend fun invoke(input: ImportObjectInput): Result<ARObject> {
        // Validation with Value Objects
        val nameResult = ObjectName.create(input.name)
        if (nameResult.isFailure) return Result.failure(nameResult.exceptionOrNull()!!)
        
        val uriResult = ModelUri.create(input.uri)
        if (uriResult.isFailure) return Result.failure(uriResult.exceptionOrNull()!!)
        
        // Delegate to repository
        return repository.importObject(input.uri, input.name, input.modelType)
    }
}

data class ImportObjectInput(
    val uri: String,
    val name: String,
    val modelType: ModelType
) : BaseModel
```

### Value Objects (DDD Advanced Pattern)

Domain validation encapsulated in sealed classes:
- Immutable and type-safe
- Created via `create()` companion function returning `Result<T>`
- Private constructors prevent invalid states
- Used in domain entities instead of raw primitives

Example:
```kotlin
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

// Usage in entity
data class ARObject(
    val id: String,
    val name: ObjectName,    // Value Object instead of String
    val modelUri: ModelUri,  // Value Object instead of String
    val modelType: ModelType
) : BaseModel
```

### DTO and Mapper Pattern

Separation between domain models and data transfer objects:
- **DTO**: Serializable data structures for storage/network
- **Mapper**: Converts between DTO and domain model

Example:
```kotlin
@Serializable
data class ARObjectDTO(
    val id: String,
    val name: String,
    val modelUri: String,
    val modelType: String
)

class ARObjectMapper : BaseMapper<ARObjectDTO, ARObject> {
    override fun toDTO(model: ARObject): ARObjectDTO {
        return ARObjectDTO(
            id = model.id,
            name = model.name.value,        // Extract from Value Object
            modelUri = model.modelUri.value,
            modelType = model.modelType.name
        )
    }
    
    override fun toModel(dto: ARObjectDTO): ARObject {
        return ARObject(
            id = dto.id,
            name = ObjectName.create(dto.name).getOrThrow(),      // Create Value Object
            modelUri = ModelUri.create(dto.modelUri).getOrThrow(),
            modelType = ModelType.valueOf(dto.modelType)
        )
    }
}
```

### Data Persistence

- Android: Uses `DataStore` for preferences, internal storage for files
- iOS: Uses `UserDefaults` for preferences, Documents directory for files
- **DTOs**: Both use JSON serialization (Kotlin Serialization) for complex objects
- **Mappers**: DTO ↔ Model transformation via `BaseMapper`
- File paths stored as strings in metadata, actual files stored separately

Example Repository Implementation:
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
            Result.failure(StorageException("Failed to load objects: ${e.message}"))
        }
    }
}
```

### 3D Model Formats

- **Primary format:** GLB (supported on both platforms)
- **iOS fallback:** USDZ (if GLB not available)
- Model files are copied to app-private storage on import
- Use UUIDs for unique file names to avoid collisions

### AR Platform Specifics

- **Android:** Uses ARCore + SceneView library
  - `ARView.kt` contains AndroidView wrapper
  - Hit testing via SceneView's tap listeners
  
- **iOS:** Uses ARKit + RealityKit
  - `ARViewWrapper.kt` contains UIViewRepresentable wrapper
  - Hit testing via ARView's raycast

### Dependency Management

- All dependencies in `gradle/libs.versions.toml` (version catalog)
- Use version references: `libs.androidx.lifecycle.viewmodelCompose`
- Key libraries:
  - `sceneview` (ARCore wrapper for Android)
  - `kotlinx-serialization-json` (data persistence)
  - `androidx-datastore-preferences` (Android settings)
  - `mockk` (testing)

### Testing Conventions

- Test classes in `commonTest/` for shared logic
- Use MockK for mocking dependencies
- Follow naming: `<ClassName>Test.kt`
- Test use cases for business logic validation
- Repository tests verify data layer behavior

## Multi-Agent System

This project uses a multi-agent development approach. All agent definitions are in `.claude/agents/`.

### Agent Responsibilities

| Agent | Role | Key Focus |
|-------|------|-----------|
| **Design & Analysis** | Research & Architecture | AR best practices (ARCore/ARKit), 3D model formats (GLB/USDZ), Clean Architecture + DDD design, domain entities, use case definitions |
| **Android Expert** | ARCore Implementation | SceneView library integration, ARCore session management, Android storage (Room/DataStore), manifest permissions |
| **iOS Expert** | ARKit Implementation | ARKit/RealityKit integration, model format conversion (GLB→USDZ), iOS storage (FileManager/SQLite), Info.plist config |
| **Main Developer** | Core Development | Domain layer (entities, use cases), data layer (repository implementations), presentation layer (ViewModels, screens), platform-specific AR code |
| **Bug Fixer** | Debugging & Fixes | Bug detection and categorization, root cause analysis (5 Why), fix implementation, regression validation |
| **Test Developer** | Unit Testing | Domain/use case/repository/ViewModel tests using MockK, achieve 85-100% coverage targets |
| **Code Reviewer** | Quality Control | Kotlin/Swift conventions, Clean Architecture compliance, DDD pattern validation, code smell detection |

### Development Flow

```
Design & Analysis → defines architecture, entities, use cases
    ↓
Android/iOS Experts → provide platform-specific implementation details
    ↓
Main Developer → implements domain/data/presentation layers
    ↓
Test Developer → writes comprehensive unit tests
    ↓
Code Reviewer → validates architecture compliance, code quality
```

### Key Agent Patterns

**Design & Analysis outputs:**
- Clean Architecture layer structure
- Domain entities: `ARObject`, `ARScene`, `PlacedObject`
- Repository interfaces in domain layer
- Use case definitions (single responsibility)

**Main Developer follows:**
- DDD principles (entities contain no business logic)
- Use cases with `operator fun invoke()` pattern
- Repository pattern (interface in domain, impl in data)
- Platform-specific code via `expect`/`actual`

**Test Developer ensures:**
- Domain layer: 90%+ coverage
- Use cases: 100% coverage
- ViewModels: 85%+ coverage
- Uses MockK for all mocking

**Code Reviewer checks:**
- Dependency rule: Domain has no external dependencies
- Use cases have single responsibility
- No magic numbers/strings
- Functions under 50 lines, classes under 300 lines
