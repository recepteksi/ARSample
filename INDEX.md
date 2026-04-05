# ARSample - Documentation Hub

![Architecture](https://img.shields.io/badge/Architecture-DDD-green)
![Platform](https://img.shields.io/badge/Platform-Kotlin%20Multiplatform-blue)
![Android](https://img.shields.io/badge/Android-ARCore-green)
![iOS](https://img.shields.io/badge/iOS-ARKit-black)

> **AR Sample Application** - A Kotlin Multiplatform app for importing and placing 3D objects in AR scenes on both Android (ARCore) and iOS (ARKit).

---

## 🚀 Quick Start

| Document | Description |
|----------|-------------|
| **[README.md](./README.md)** | Project overview and setup instructions |
| **[CLAUDE.md](./CLAUDE.md)** | AI assistant configuration and key patterns |
| **[.github/copilot-instructions.md](./.github/copilot-instructions.md)** | GitHub Copilot instructions |
| **[docs/INDEX.md](./docs/INDEX.md)** | Complete documentation index |

---

## 📂 Documentation Structure

All documentation is organized in the `docs/` folder:

```
docs/
├── INDEX.md                    # Master documentation index
├── architecture/               # Architecture and design docs
├── design/                     # UI/UX design system and guidelines
├── agents/                     # Multi-agent system documentation
├── ios/                        # iOS-specific documentation
├── guides/                     # How-to guides and checklists
└── reports/                    # Status reports and summaries
```

### 📖 Quick Navigation

- **[Full Documentation Index](./docs/INDEX.md)** - Browse all documentation
- **[Architecture](./docs/architecture/TECHNICAL_ANALYSIS.md)** - System design
- **[UI/UX Design](./docs/design/)** - Design system and guidelines
- **[Agents System](./docs/agents/README.md)** - Development workflow
- **[iOS Guide](./docs/ios/ios-quick-reference.md)** - iOS implementation
- **[Testing Guide](./docs/guides/TEST_IMPLEMENTATION_GUIDE.md)** - Testing
- **[Status Reports](./docs/reports/)** - Project status

---

## 🏗️ Architecture Overview

This project follows **Eric Evans' DDD + Clean Architecture + MVVM** pattern with strict layer separation:

```
Domain Layer (innermost) → Pure business logic, NO dependencies
    ↑
Application Layer → Use cases, depends on Domain only
    ↑
Infrastructure Layer → Technical implementations, depends on Domain
    ↑
Presentation Layer → UI, depends on Application
```

### Key Patterns

- **Value Objects**: Domain validation (ModelUri, ObjectName) in `domain/model/valueobjects/`
- **Base Classes**: 
  - `BaseModel`, `BaseRepository` in `domain/base/`
  - `BaseUseCase` in `application/base/`
  - `BaseMapper` in `infrastructure/persistence/`
- **DTO/Mapper**: Separation between domain and persistence
  - Persistence DTOs in `infrastructure/persistence/dto/`
  - Mappers in `infrastructure/persistence/mapper/`
- **Result<T>**: Functional error handling
- **Interface Segregation**: Every component has an interface

### Layer Structure

**1. Domain Layer** (`domain/`)
- `base/` - BaseModel, BaseRepository
- `model/` - Entities (ARObject, ARScene, PlacedObject)
  - `valueobjects/` - ModelUri, ObjectName
- `repository/` - Repository interfaces
- `exception/` - Domain exceptions

**2. Application Layer** (`application/`)
- `base/` - BaseUseCase
- `dto/` - Use case Input/Output DTOs
- `usecase/` - Business workflows

**3. Infrastructure Layer** (`infrastructure/persistence/`)
- `dto/` - Persistence DTOs
- `mapper/` - DTO ↔ Model mappers
- `repository/` - Repository implementations
- `local/` - Data source interfaces
- `BaseMapper.kt` - Mapper base class

**4. Presentation Layer** (`presentation/`)
- `viewmodel/` - State management
- `ui/` - Compose screens and components

📚 **[Read more about architecture →](./docs/architecture/TECHNICAL_ANALYSIS.md)**

---

## 👥 Multi-Agent Development System

This project uses a multi-agent approach for development:

| Agent | Role |
|-------|------|
| **Design & Analysis** | Research and architecture design |
| **Android Expert** | ARCore implementation |
| **iOS Expert** | ARKit implementation |
| **Main Developer** | Core feature development |
| **Test Developer** | Unit test creation |
| **Code Reviewer** | Quality control |
| **Bug Fixer** | Debugging and fixes |

📚 **[Learn about agents →](./.claude/agents/README.md)**

---

## 🛠️ Build & Test Commands

**Android:**
```bash
./gradlew :composeApp:assembleDebug
```

**iOS:**
```bash
open iosApp/iosApp.xcodeproj  # Then run from Xcode
```

**Run tests:**
```bash
./gradlew :composeApp:testDebugUnitTest
```

📚 **[More commands in CLAUDE.md →](./CLAUDE.md)**

---

## 📱 Platform Support

- **Android**: ARCore + SceneView library
- **iOS**: ARKit + RealityKit
- **Models**: GLB (primary), USDZ (iOS)
- **Storage**: DataStore (Android), UserDefaults (iOS)

---

## 📊 Project Status

| Category | Status |
|----------|--------|
| Core Architecture | ✅ Complete |
| Android Implementation | ✅ Complete |
| iOS Implementation | ✅ Complete |
| Unit Tests | ✅ Complete |
| Documentation | ✅ Complete |

📚 **[View detailed reports →](./docs/reports/)**

---

## 📚 Documentation Categories

### By Topic

- **🏗️ Architecture**: [Technical Analysis](./docs/architecture/technical-analysis.md)
- **🎨 Design**: [UI/UX Design System](./docs/design/)
- **👥 Development**: [Agents System](./docs/agents/README.md)
- **📱 iOS**: [iOS Documentation](./docs/ios/)
- **📖 Guides**: [Implementation Guides](./docs/guides/)
- **📊 Reports**: [Status Reports](./docs/reports/)

### By Role

- **For Developers**: [Main Developer Agent](./.claude/agents/main-developer-agent.md)
- **For Testers**: [Test Implementation Guide](./docs/guides/test-implementation-guide.md)
- **For Reviewers**: [Code Review Checklist](./docs/guides/code-review-checklist.md)
- **For iOS Devs**: [iOS Quick Reference](./docs/ios/ios-quick-reference.md)
- **For Android Devs**: [Android Expert Agent](./.claude/agents/android-expert-agent.md)

---

## 🔗 External Resources

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [ARCore Developer Guide](https://developers.google.com/ar)
- [ARKit Documentation](https://developer.apple.com/documentation/arkit)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/reference/)

---

## 📝 Recent Updates

- **2026-03-30**: Added comprehensive [UI/UX Design System](./docs/design/) with Material 3 + iOS guidelines
- **2026-03-30**: Added [AR Competitor Analysis](./docs/design/AR_COMPETITOR_ANALYSIS.md) research
- **2026-03-30**: Added [Design Tokens](./docs/design/DESIGN_TOKENS.md) quick reference
- **2026-03-30**: Added [Drag-and-Drop Design Document](./docs/DRAG_DROP_DESIGN.md)
- **2026-03-31**: Documentation reorganized into `docs/` structure
- **2026-03-31**: Agents updated with Flutter-inspired DDD patterns
- **2026-03-31**: Added Value Objects, DTO/Mapper patterns
- **2026-03-30**: Initial implementation completed

---

## 📄 License

This project is private and not published.

---

<p align="center">
  <strong>📖 For comprehensive documentation, visit **[docs/INDEX.md](./docs/INDEX.md)**</strong>
</p>
