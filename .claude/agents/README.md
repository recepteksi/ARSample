# Agents

Central documentation containing 7 agent definitions and task specifications.

## 🔄 Development Workflow

**See:** [../WORKFLOW.md](../WORKFLOW.md) for complete Git branching strategy and agent workflow.

### Quick Workflow Summary

Each agent follows this flow:
1. **Receives task** from orchestrator (with task ID)
2. **Creates feature branch** from `dev`: `git checkout -b feature/task-id`
3. **Implements changes** on isolated branch
4. **Commits and pushes** with conventional commits
5. **Requests code review** from Code Reviewer agent
6. **Merges to dev** if approved
7. **User merges dev → main** when ready for release

## Agent List

| # | Agent | Role | Output |
|---|-------|-----|--------|
| 1 | [Design & Analysis](./design-analysis-agent.md) | Web research, AR examples, design | Design document |
| 2 | [Android Expert](./android-expert-agent.md) | ARCore research, Android report | Android report |
| 3 | [iOS Expert](./ios-expert-agent.md) | ARKit research, iOS report | iOS report |
| 4 | [Main Developer](./main-developer-agent.md) | Code development (domain, application, infrastructure, presentation) | Code files |
| 5 | [Bug Fixer](./bug-fixer-agent.md) | Debugging and bug fixing | Bug reports, fixed code |
| 6 | [Test Developer](./test-developer-agent.md) | Unit testing | Test files |
| 7 | [Code Reviewer](./code-reviewer-agent.md) | Code standards verification | Review report |

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
5. [Bug Fixer Agent](./bug-fixer-agent.md) - Debugging and bug fixing
6. [Test Developer Agent](./test-developer-agent.md) - Unit tests
7. [Code Reviewer Agent](./code-reviewer-agent.md) - Code quality control