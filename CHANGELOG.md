# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.0] - 2026-04-04

### Changed
- **BREAKING**: Restructured project to proper 4-layer DDD architecture following Eric Evans' book
  - Renamed `data/` → `infrastructure/` across all modules (commonMain, androidMain, iosMain, commonTest)
  - Moved use cases: `domain/usecase/` → `application/usecase/`
  - Moved BaseUseCase: `domain/base/BaseUseCase.kt` → `application/base/BaseUseCase.kt`
  - Moved UseCaseTypes: `domain/model/UseCaseTypes.kt` → `application/dto/UseCaseTypes.kt`
  - Moved BaseMapper: `domain/base/BaseMapper.kt` → `infrastructure/persistence/BaseMapper.kt`
- Updated all import statements across 40+ files to reflect new structure
- Updated multi-agent documentation (8 agent files) with DDD layer descriptions
- Infrastructure now properly organized: `infrastructure/persistence/{dto,mapper,repository,datasource,BaseMapper}`

### Technical
- **Layer Dependencies** (Eric Evans DDD):
  - Domain: Pure business logic (no dependencies)
  - Application: Use cases (depends on Domain)
  - Infrastructure: Technical implementations (depends on Application + Domain)
  - Presentation: UI (depends on Application)
- All builds verified: Android (assembleDebug), iOS (compileKotlinIosArm64), Tests (testDebugUnitTest)

### Fixed
- Import path corrections in ViewModels, repositories, and tests
- Package declarations in test files after folder restructure

## [0.2.0] - 2026-04-03

### Added
- DDD + Clean Architecture refactoring
- Domain Layer: Value Objects (ModelUri, ObjectName), BaseModel interface
- Application Layer: Use Cases with typed Input/Output models
- Infrastructure Layer: DTOs, Mappers extending BaseMapper
- Domain exceptions hierarchy (DomainException, ValidationException, EntityNotFoundException, StorageException)
- Comprehensive unit tests with coroutine dispatcher setup

### Changed
- Renamed data layer DTOs from Entity suffix to DTO
- All use cases now implement BaseUseCase<Input, Output>
- Repository interfaces extend BaseRepository
- All domain models implement BaseModel

### Removed
- Unused template files (Greeting.kt, Platform.kt)
- Debug println statements
- Backup files (.bak)

### Fixed
- iOS material icons compilation error
- Unit test failures after DDD refactoring
- Value Object validation in use cases

## [0.1.0] - 2026-03-30

### Added
- Initial ARSample Kotlin Multiplatform project
- Android ARCore integration with SceneView
- iOS ARKit integration
- 3D object import functionality (GLB format)
- Object placement in AR scene
- Object removal from scene
- Drag-to-delete with trash zone
- Local persistence (DataStore/UserDefaults)
- Multi-agent development system
