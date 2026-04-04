# 🌟 ARSample - Augmented Reality Object Placement

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.7.1-4285F4?style=flat&logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-lightgrey.svg)](https://github.com/recepteksi/ARSample)

**A professional Kotlin Multiplatform AR application demonstrating Clean Architecture, DDD principles, and platform-specific AR implementations (ARCore/ARKit)**

[Features](#-features) • [Architecture](#-architecture) • [Tech Stack](#-tech-stack) • [Getting Started](#-getting-started) • [Documentation](#-documentation)

</div>

---

## 📱 Overview

ARSample is a production-ready Augmented Reality application built with **Kotlin Multiplatform Mobile (KMM)**, showcasing enterprise-grade architecture patterns and modern mobile development practices. The app allows users to import, place, and manage 3D objects in AR scenes across both Android and iOS platforms with a fully shared business logic layer.

### 🎯 Key Highlights

- 🏗️ **Clean Architecture + DDD**: Domain-driven design with clear separation of concerns
- 🔄 **95%+ Code Sharing**: Business logic, UI, and domain layer shared between platforms
- 📐 **MVVM Pattern**: Reactive state management with Kotlin Flow
- 🎨 **Jetpack Compose**: Modern declarative UI for both platforms
- 🧪 **High Test Coverage**: 85-100% test coverage with comprehensive unit tests
- 🔍 **Type Safety**: Value Objects pattern for domain validation
- 📦 **Repository Pattern**: Clean data abstraction with DTO/Mapper pattern

---

## ✨ Features

### Core Functionality
- ✅ **3D Model Import**: Support for GLB and USDZ formats
- ✅ **AR Object Placement**: Real-time hit testing and object positioning
- ✅ **Scene Persistence**: Auto-save/restore AR scenes across app restarts
- ✅ **Object Management**: Add, remove, and list imported 3D models
- ✅ **Cross-Platform UI**: Identical user experience on Android and iOS

### Technical Features
- 🔐 **Domain Validation**: Value Objects with sealed classes (ModelUri, ObjectName)
- 🚀 **Result Pattern**: Type-safe error handling throughout the application
- 🎯 **Use Case Pattern**: Single-responsibility business logic units
- 🗂️ **Local Storage**: Platform-specific implementations (DataStore/UserDefaults)
- 🧩 **Expect/Actual Pattern**: Clean platform-specific abstractions

---

## 🏛️ Architecture

This project follows **Eric Evans' Domain-Driven Design (DDD) + Clean Architecture** principles with four distinct layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  • ViewModels (State Management)                            │
│  • Compose UI Screens                                       │
│  • Platform-specific AR Views (AndroidView/UIViewWrapper)  │
│  • Depends on: Application Layer                            │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                   Application Layer                          │
│  • Use Cases (ImportObject, PlaceObject, RemoveObject)      │
│  • Business Workflows                                       │
│  • Use Case DTOs (Input/Output models)                      │
│  • Depends on: Domain Layer only                            │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                     Domain Layer                             │
│  • Entities (ARObject, ARScene, PlacedObject)               │
│  • Value Objects (ModelUri, ObjectName)                     │
│  • Repository Interfaces                                    │
│  • Domain Exceptions                                        │
│  • NO dependencies (innermost layer)                        │
└────────────────────────────────┬────────────────────────────┘
                                 ▲
┌────────────────────────────────┴────────────────────────────┐
│                 Infrastructure Layer                         │
│  • Repository Implementations                                │
│  • DTOs + Mappers (Persistence)                             │
│  • Local Data Sources (Platform-specific)                   │
│  • File Storage (Internal Storage / Documents Directory)    │
│  • Depends on: Domain Layer                                 │
└─────────────────────────────────────────────────────────────┘
```

### Layer Dependencies (DDD Structure)

```
Presentation → Application → Domain ← Infrastructure
```

**Key Architectural Rules:**
1. **Domain Layer** (innermost): Pure business logic, zero dependencies
2. **Application Layer**: Orchestrates domain objects, depends on Domain only
3. **Infrastructure Layer**: Technical implementations, depends on Domain (not Application)
4. **Presentation Layer**: UI layer, depends on Application

### Architectural Decisions

#### 1. **Value Objects for Domain Validation**
```kotlin
sealed class ModelUri private constructor(val value: String) {
    companion object {
        fun create(uri: String): Result<ModelUri> {
            return when {
                uri.isBlank() -> Result.failure(ValidationException("URI cannot be blank"))
                !uri.matches(Regex(".*\\.(glb|usdz)$")) -> 
                    Result.failure(ValidationException("Invalid model format"))
                else -> Result.success(ValidModelUri(uri))
            }
        }
    }
    private class ValidModelUri(value: String) : ModelUri(value)
}
```

#### 2. **Use Case Pattern**
```kotlin
// Use cases in APPLICATION LAYER (not domain)
// Location: application/usecase/
interface ImportObjectUseCaseInterface : BaseUseCase<ImportObjectInput, ARObject>

class ImportObjectUseCase(
    private val repository: ARObjectRepository
) : ImportObjectUseCaseInterface {
    override suspend fun invoke(input: ImportObjectInput): Result<ARObject> {
        // Validation with Value Objects (from domain)
        val nameResult = ObjectName.create(input.name)
        if (nameResult.isFailure) return Result.failure(nameResult.exceptionOrNull()!!)
        
        return repository.importObject(input.uri, input.name, input.modelType)
    }
}

// Import paths:
import com.trendhive.arsample.application.usecase.ImportObjectUseCase
import com.trendhive.arsample.application.base.BaseUseCase
import com.trendhive.arsample.application.dto.ImportObjectInput
```

#### 3. **DTO + Mapper Pattern**
```kotlin
// Persistence DTO in INFRASTRUCTURE LAYER
// Location: infrastructure/persistence/dto/
@Serializable
data class ARObjectDTO(
    val id: String,
    val name: String,
    val modelUri: String,
    val modelType: String
)

// Mapper in INFRASTRUCTURE LAYER
// Location: infrastructure/persistence/mapper/
class ARObjectMapper : BaseMapper<ARObjectDTO, ARObject> {
    override fun toDTO(model: ARObject): ARObjectDTO
    override fun toModel(dto: ARObjectDTO): ARObject
}

// Import paths:
import com.trendhive.arsample.infrastructure.persistence.dto.ARObjectDTO
import com.trendhive.arsample.infrastructure.persistence.mapper.ARObjectMapper
import com.trendhive.arsample.infrastructure.persistence.BaseMapper
```

---

## 🛠️ Tech Stack

### Core Technologies
- **[Kotlin 2.1.0](https://kotlinlang.org/)** - Primary programming language
- **[Compose Multiplatform 1.7.1](https://www.jetbrains.com/lp/compose-multiplatform/)** - Declarative UI framework
- **[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** - Asynchronous programming
- **[Kotlin Flow](https://kotlinlang.org/docs/flow.html)** - Reactive state management

### AR Frameworks
- **[ARCore](https://developers.google.com/ar)** (Android) - Google's AR platform
- **[SceneView](https://github.com/SceneView/sceneview-android)** - ARCore wrapper library
- **[ARKit](https://developer.apple.com/arkit/)** (iOS) - Apple's AR platform
- **[RealityKit](https://developer.apple.com/documentation/realitykit/)** - iOS AR rendering

### Data & Storage
- **[Kotlin Serialization](https://kotlinlang.org/docs/serialization.html)** - JSON serialization
- **[DataStore](https://developer.android.com/topic/libraries/architecture/datastore)** (Android) - Preferences storage
- **[UserDefaults](https://developer.apple.com/documentation/foundation/userdefaults)** (iOS) - Preferences storage

### Testing
- **[Kotlin Test](https://kotlinlang.org/api/latest/kotlin.test/)** - Testing framework
- **[MockK](https://mockk.io/)** - Mocking library
- **[Turbine](https://github.com/cashapp/turbine)** - Flow testing utilities

### Build & Tooling
- **[Gradle Version Catalog](https://docs.gradle.org/current/userguide/platforms.html)** - Dependency management
- **[Android Gradle Plugin 8.7.3](https://developer.android.com/build/releases/gradle-plugin)** - Android build
- **[Xcode 15+](https://developer.apple.com/xcode/)** - iOS build

---

## 🚀 Getting Started

### Prerequisites

**Required:**
- JDK 17 or higher
- Android Studio Ladybug (2024.2.1) or newer
- Xcode 15+ (for iOS development)
- macOS (for iOS builds)

**AR Device Requirements:**
- Android: ARCore-supported device ([Check compatibility](https://developers.google.com/ar/devices))
- iOS: A12+ chip with ARKit support (iPhone XS and newer)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/recepteksi/ARSample.git
   cd ARSample
   ```

2. **Build Android**
   ```bash
   ./gradlew :composeApp:assembleDebug
   ```

3. **Build iOS**
   ```bash
   # Open in Xcode
   open iosApp/iosApp.xcodeproj
   # Or use xcodebuild
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug
   ```

### Running Tests

```bash
# Run all tests
./gradlew :composeApp:testDebugUnitTest

# Run specific test class
./gradlew :composeApp:testDebugUnitTest --tests "com.trendhive.arsample.domain.usecase.ImportObjectUseCaseTest"

# Run with coverage
./gradlew :composeApp:testDebugUnitTest --tests "*" --info
```

---

## 📂 Project Structure

```
ARSample/
├── composeApp/src/
│   ├── commonMain/kotlin/com/trendhive/arsample/
│   │   ├── domain/                   # Domain Layer (NO dependencies)
│   │   │   ├── base/                 # BaseModel, BaseRepository
│   │   │   ├── model/                # Domain entities (ARObject, ARScene, PlacedObject)
│   │   │   │   └── valueobjects/     # Value Objects (ModelUri, ObjectName)
│   │   │   ├── repository/           # Repository interfaces
│   │   │   └── exception/            # Domain exceptions
│   │   │
│   │   ├── application/              # Application Layer (depends on Domain)
│   │   │   ├── base/                 # BaseUseCase<Input, Output>
│   │   │   ├── dto/                  # Use Case Input/Output DTOs
│   │   │   └── usecase/              # Business workflows (use cases)
│   │   │
│   │   ├── infrastructure/           # Infrastructure Layer (depends on Domain)
│   │   │   └── persistence/
│   │   │       ├── dto/              # Persistence DTOs
│   │   │       ├── mapper/           # DTO ↔ Model mappers
│   │   │       ├── repository/       # Repository implementations
│   │   │       ├── local/            # Data source interfaces
│   │   │       └── BaseMapper.kt     # Mapper base class
│   │   │
│   │   └── presentation/             # Presentation Layer (depends on Application)
│   │       ├── viewmodel/            # State management
│   │       └── ui/                   # Compose screens and components
│   │
│   ├── androidMain/                  # Android-specific (ARCore, DataStore)
│   │   ├── ar/                       # ARCore implementation
│   │   └── infrastructure/persistence/local/  # Android data sources
│   │
│   ├── iosMain/                      # iOS-specific (ARKit, UserDefaults)
│   │   ├── ar/                       # ARKit implementation
│   │   └── infrastructure/persistence/local/  # iOS data sources
│   │
│   └── commonTest/                   # Shared unit tests
│
├── iosApp/                           # iOS app entry point
├── docs/                             # Architecture docs and guides
└── .claude/agents/                   # AI agent system documentation
```

---

## 🧪 Testing Strategy

### Test Coverage
- **Domain Layer**: 90%+ coverage
- **Use Cases**: 100% coverage
- **ViewModels**: 85%+ coverage
- **Repositories**: 90%+ coverage

### Test Structure
```kotlin
class ImportObjectUseCaseTest {
    private lateinit var repository: ARObjectRepository
    private lateinit var useCase: ImportObjectUseCase
    
    @Test
    fun `import valid object succeeds`() = runTest {
        // Arrange
        val input = ImportObjectInput("file://model.glb", "Chair", ModelType.GLB)
        coEvery { repository.importObject(any(), any(), any()) } returns 
            Result.success(mockARObject)
        
        // Act
        val result = useCase(input)
        
        // Assert
        assertTrue(result.isSuccess)
        coVerify { repository.importObject("file://model.glb", "Chair", ModelType.GLB) }
    }
}
```

---

## 📚 Documentation

### Core Concepts
- **[Architecture Overview](docs/architecture/technical-analysis.md)** - System design and patterns
- **[Agent System](.claude/agents/README.md)** - Multi-agent development workflow
- **[Hit Testing Guide](docs/guides/hit-testing/)** - AR interaction implementation

### Platform-Specific
- **[Android ARCore](docs/android-arcore-summary.md)** - Android AR implementation
- **[iOS ARKit](docs/ios/ios-expert-report.md)** - iOS AR implementation
- **[Code Review Checklist](docs/guides/code-review-checklist.md)** - Quality standards

---

## 🎨 Key Design Patterns

### 1. Repository Pattern
```kotlin
interface ARObjectRepository : BaseRepository {
    suspend fun importObject(uri: String, name: String, type: ModelType): Result<ARObject>
    suspend fun getAllObjects(): Result<List<ARObject>>
    suspend fun deleteObject(id: String): Result<Unit>
}
```

### 2. Base Abstractions
```kotlin
// BaseModel and BaseRepository in DOMAIN layer
interface BaseModel
interface BaseRepository

// BaseUseCase in APPLICATION layer
interface BaseUseCase<Input : BaseModel, Output : BaseModel> {
    suspend operator fun invoke(input: Input): Result<Output>
}

// BaseMapper in INFRASTRUCTURE layer
interface BaseMapper<DTO, Model> {
    fun toDTO(model: Model): DTO
    fun toModel(dto: DTO): Model
}

// Import paths:
import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.domain.base.BaseRepository
import com.trendhive.arsample.application.base.BaseUseCase
import com.trendhive.arsample.infrastructure.persistence.BaseMapper
```

### 3. Exception Hierarchy
```kotlin
sealed class DomainException(message: String) : Exception(message)
class ValidationException(message: String) : DomainException(message)
class EntityNotFoundException(message: String) : DomainException(message)
class StorageException(message: String) : DomainException(message)
```

---

## 🤝 Contributing

Contributions are welcome! This project follows professional development practices:

1. **Code Standards**: Kotlin conventions, Clean Architecture compliance
2. **Testing**: All new features must include unit tests
3. **Documentation**: Update relevant docs with changes
4. **Review Process**: Code review checklist validation

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Recep Tekşi**

- GitHub: [@recepteksi](https://github.com/recepteksi)
- LinkedIn: [Recep Tekşi](https://www.linkedin.com/in/recep-ek%C5%9Fi-473b29222/)

---

## 🌟 Showcase

This project demonstrates:

✅ **Modern Android/iOS Development** - KMM, Compose, ARCore/ARKit  
✅ **Enterprise Architecture** - Clean Architecture, DDD, SOLID principles  
✅ **Professional Practices** - High test coverage, type safety, documentation  
✅ **Platform Expertise** - Native AR implementations, platform-specific optimizations  
✅ **Team Collaboration** - Multi-agent system, code review standards

---

<div align="center">

**Built with ❤️ using Kotlin Multiplatform**

⭐ Star this repo if you find it useful!

</div>
