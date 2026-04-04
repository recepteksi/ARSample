# Agents

Central documentation containing 6 agent definitions and task specifications.

## Agent List

| # | Agent | Role | Output |
|---|-------|-----|--------|
| 1 | [Design & Analysis](./design-analysis-agent.md) | Web research, AR examples, design | Design document |
| 2 | [Android Expert](./android-expert-agent.md) | ARCore research, Android report | Android report |
| 3 | [iOS Expert](./ios-expert-agent.md) | ARKit research, iOS report | iOS report |
| 4 | [Main Developer](./main-developer-agent.md) | Code development (domain, application, infrastructure, presentation) | Code files |
| 5 | [Test Developer](./test-developer-agent.md) | Unit testing | Test files |
| 6 | [Code Reviewer](./code-reviewer-agent.md) | Code standards verification | Review report |

## Agent Communication Flow

```
Design & Analysis Agent (research + design)
        ↓
    ↓                  ↓
Android Expert    iOS Expert
(report)         (report)
    ↓                  ↓
    ↓←←←←Main Developer→→→→↓
    ↓                  ↓
    ↓←Test Developer (tests)
    ↓
Code Reviewer (quality control)
    ↓
Main Developer (fixes)
```

## Project Information

**Project:** Sample application for importing, placing, and removing 3D objects using ARCore (Android) and ARKit (iOS).

**Architecture:** DDD (Domain-Driven Design) + Clean Architecture + MVVM

**Layers:**
- **Domain:** Pure business logic (entities, value objects, repository interfaces)
- **Application:** Use cases and orchestration (business workflows)
- **Infrastructure:** Technical implementations (database, file system, AR platform)
- **Presentation:** User interface (ViewModels, UI screens)

**Platform:** Kotlin Multiplatform (Android + iOS)

**Model Format:** GLB (preferred), USDZ (iOS)

**Storage:** Local persistence (objects remain saved when app is closed/reopened)

## Detailed Agent Documentation

1. [Design & Analysis Agent](./design-analysis-agent.md) - Design and research
2. [Android Expert Agent](./android-expert-agent.md) - Android ARCore implementation
3. [iOS Expert Agent](./ios-expert-agent.md) - iOS ARKit implementation
4. [Main Developer Agent](./main-developer-agent.md) - Core code development
5. [Test Developer Agent](./test-developer-agent.md) - Unit tests
6. [Code Reviewer Agent](./code-reviewer-agent.md) - Code quality control