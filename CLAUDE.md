# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

**Android:**
```shell
./gradlew :composeApp:assembleDebug
```

**iOS:**
Open `iosApp/iosApp.xcodeproj` in Xcode and run from there.

**Run tests:**
```shell
./gradlew :composeApp:testDebugUnitTest
```

## Architecture

This is a Kotlin Multiplatform project targeting Android and iOS with Jetpack Compose.

- `composeApp/src/commonMain/` - Shared Kotlin code (UI, business logic)
- `composeApp/src/androidMain/` - Android-specific Kotlin (MainActivity, Platform.android.kt)
- `composeApp/src/iosMain/` - iOS-specific Kotlin (MainViewController, Platform.ios.kt)
- `iosApp/iosApp/` - SwiftUI iOS entry point that embeds the Compose framework

Platform-specific implementations use `expect`/`actual` pattern (e.g., `Platform.kt`).

Dependencies are managed via version catalog in `gradle/libs.versions.toml`.

## Key Patterns (Flutter-inspired DDD Implementation)

> **Update (2026-04-01)**: Bu pattern'ler Halleder projelerinden (Flutter Clean Architecture) öğrenilen best practice'lerle zenginleştirilmiştir.

### Pattern Principles
1. **Clean Architecture** - 3-layer strict separation (Domain, Data, Presentation)
2. **Single Responsibility** - Her class tek bir iş yapar
3. **Dependency Inversion** - Dependencies point inward (Domain ← Data ← Presentation)
4. **Result<T> Pattern** - Functional error handling (no exceptions in happy path)
5. **Value Objects** - Domain validation at boundary
6. **Input/Output Models** - Typed use case interfaces

### 1. Value Objects (Domain Validation)
Domain validation için sealed class pattern kullanılır:
```kotlin
sealed class ModelUri private constructor(val value: String) {
    companion object {
        fun create(uri: String): Result<ModelUri>
    }
}
sealed class ObjectName private constructor(val value: String) {
    companion object {
        fun create(name: String): Result<ObjectName>
    }
}
```

### 2. Base Classes
- `BaseModel` - Tüm domain model'ler için marker interface
- `BaseUseCase<Input, Output>` - Use case standardı
- `BaseMapper<DTO, Model>` - DTO ↔ Model dönüşümü
- `BaseRepository` - Repository marker interface

### 3. DTO ve Mapper Pattern
Her domain model için:
- DTO (Data Transfer Object) - serialization için
- Mapper - DTO ↔ Model dönüşümü
```kotlin
class ARObjectMapper : BaseMapper<ARObjectDTO, ARObject> {
    override fun toDTO(model: ARObject): ARObjectDTO
    override fun toModel(dto: ARObjectDTO): ARObject
}
```

### 4. Result<T> Pattern
Tüm repository ve use case'ler Result<T> döner:
```kotlin
suspend fun getAllObjects(): Result<List<ARObject>>
suspend fun invoke(input: Input): Result<Output>
```

### 5. Exception Hierarchy
```kotlin
sealed class DomainException(message: String) : Exception(message)
class ValidationException(message: String) : DomainException(message)
class EntityNotFoundException(message: String) : DomainException(message)
class StorageException(message: String) : DomainException(message)
```

## Project Structure

This project follows **Eric Evans' DDD + Clean Architecture + MVVM** pattern with strict layer separation.

```
composeApp/src/
├── commonMain/kotlin/com/trendhive/arsample/
│   ├── domain/                    # Domain Layer (innermost - NO dependencies)
│   │   ├── base/                  # BaseModel, BaseRepository
│   │   ├── exception/             # Domain exceptions (ValidationException, EntityNotFoundException, StorageException)
│   │   ├── model/                 # Entities (ARObject, ARScene, PlacedObject)
│   │   │   └── valueobjects/      # Value Objects (ModelUri, ObjectName)
│   │   └── repository/            # Repository interfaces only
│   │
│   ├── application/               # Application Layer (depends on Domain)
│   │   ├── base/                  # BaseUseCase<Input, Output>
│   │   ├── dto/                   # Use case Input/Output DTOs (NoInput, ListResult, etc.)
│   │   └── usecase/               # Business workflows (ImportObjectUseCase, PlaceObjectUseCase)
│   │
│   ├── infrastructure/            # Infrastructure Layer (depends on Application + Domain)
│   │   └── persistence/
│   │       ├── dto/               # Persistence DTOs (ARObjectDTO, ARSceneDTO)
│   │       ├── mapper/            # DTO ↔ Model mappers (ARObjectMapper, ARSceneMapper)
│   │       ├── repository/        # Repository implementations (ARObjectRepositoryImpl)
│   │       ├── local/             # Data source interfaces (ARObjectLocalDataSource, ModelFileStorage)
│   │       └── BaseMapper.kt      # Mapper base class
│   │
│   └── presentation/              # Presentation Layer (depends on Application)
│       ├── viewmodel/             # ViewModels with state management
│       └── ui/                    # Compose screens and components
│           ├── screens/
│           └── components/
│
├── androidMain/                   # Android-specific (ARCore + DataStore implementations)
│   ├── ar/                        # ARCore/SceneView
│   └── infrastructure/persistence/local/  # Android data source implementations
│
└── iosMain/                       # iOS-specific (ARKit + UserDefaults implementations)
    ├── ar/                        # ARKit/RealityKit
    └── infrastructure/persistence/local/  # iOS data source implementations
```

### Layer Dependencies (DDD Structure)

```
Presentation → Application → Domain ← Infrastructure
                              ↑
                     Platform-Specific
                     (android/ios)
```

**Dependency Rules:**
- **Domain Layer**: Pure business logic, NO dependencies on any other layer
- **Application Layer**: Orchestrates domain objects, depends on Domain only
- **Infrastructure Layer**: Technical implementations, depends on Domain
- **Presentation Layer**: UI layer, depends on Application and Domain

### Import Path Examples

```kotlin
// Domain entities
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.valueobjects.ModelUri

// Repository interface (domain)
import com.trendhive.arsample.domain.repository.ARObjectRepository

// Use case (application layer)
import com.trendhive.arsample.application.usecase.ImportObjectUseCase
import com.trendhive.arsample.application.usecase.ImportObjectUseCaseInterface
import com.trendhive.arsample.application.dto.ImportObjectInput
import com.trendhive.arsample.application.base.BaseUseCase

// Repository implementation (infrastructure)
import com.trendhive.arsample.infrastructure.persistence.repository.ARObjectRepositoryImpl

// Mapper (infrastructure)
import com.trendhive.arsample.infrastructure.persistence.mapper.ARObjectMapper
import com.trendhive.arsample.infrastructure.persistence.BaseMapper

// Persistence DTO (infrastructure)
import com.trendhive.arsample.infrastructure.persistence.dto.ARObjectDTO

// ViewModel (presentation)
import com.trendhive.arsample.presentation.viewmodel.ARViewModel
```

## Agent System

This project uses a multi-agent system for development. All agent definitions and documentation are located in `.claude/agents/`.

See [.claude/agents/README.md](./.claude/agents/README.md) for the full agent system documentation.

### Agent List and Responsibilities

> **Update (2026-04-01)**: Agent'lar Halleder projelerinden öğrenilen Clean Architecture, BLoC/Cubit patterns, ve test stratejileri ile güçlendirilmiştir.

| Agent | Role | Key Responsibilities | Output | Improvements |
|-------|------|---------------------|---------|--------------|
| **Design & Analysis** | Research & Architecture | AR best practices, 3D model formats, Clean Architecture design, domain entities, use case definitions | Design document, entity definitions | ✅ Layer dependency rules |
| **Android Expert** | ARCore Implementation | SceneView integration, ARCore SDK, Android local storage (Room/DataStore), manifest configuration | Android implementation report | ✅ Build variants |
| **iOS Expert** | ARKit Implementation | ARKit/RealityKit integration, USDZ/GLB handling, iOS storage (FileManager/SQLite), Info.plist configuration | iOS implementation report | ✅ State management |
| **Main Developer** | Core Development | Domain layer (entities, use cases, repositories), data layer (repository implementations), presentation layer (ViewModels, UI screens), platform-specific AR code | Source code files | ✅ Use case patterns, Service layer, DI patterns |
| **Bug Fixer** | Debugging & Fixes | Bug detection, root cause analysis (5 Why technique), fix implementation, regression testing | Bug reports, fixed code | ✅ Error handling patterns |
| **Test Developer** | Unit Testing | Domain tests, use case tests, repository tests, ViewModel tests using MockK | Test files with 85-100% coverage | ✅ Given-When-Then, BDD patterns, Test builders |
| **Code Reviewer** | Quality Control | Kotlin/Swift conventions, Clean Architecture compliance, DDD pattern validation, code smell detection | Review report, approve/reject | ✅ Enhanced checklists, Architecture rules |

### Agent Workflow

```
Design & Analysis (research + design)
        ↓
    ↓                  ↓
Android Expert    iOS Expert
(platform reports)
    ↓                  ↓
    ↓←←←Main Developer→→→→↓
    ↓                  ↓
    ↓←Test Developer (tests)
    ↓
Code Reviewer (quality control)
    ↓
Main Developer (fixes)

## Project Overview

**AR Sample Application:** A Kotlin Multiplatform app that allows users to import 3D objects (GLB format) and place/remove them in an AR scene on both Android (ARCore) and iOS (ARKit).

**Key Features:**
- Import 3D models (GLB format)
- Place models in AR scene using hit testing
- Remove placed models
- Persist data locally (app restart preserves state)
- Cross-platform (Android + iOS)

**Model Format:** GLB (preferred), USDZ for iOS