# ARSample Documentation Index

> **AR Sample Application** - Kotlin Multiplatform app for importing and placing 3D objects in AR scenes (Android ARCore + iOS ARKit)

## 📋 Quick Links

| Document | Description |
|----------|-------------|
| [README.md](../README.md) | Project overview and getting started |
| [CLAUDE.md](../CLAUDE.md) | AI assistant instructions and architecture |
| [.github/copilot-instructions.md](../.github/copilot-instructions.md) | GitHub Copilot instructions |

---

## 📂 Documentation Structure

### 🏗️ Architecture
Documentation about system design and architecture.

| File | Description |
|------|-------------|
| [technical-analysis.md](./architecture/technical-analysis.md) | Technical architecture analysis and decisions |
| **[DRAG_DROP_DESIGN.md](./DRAG_DROP_DESIGN.md)** | **🆕 NEW** - Drag-to-move and drag-to-delete feature design |

---

### 👥 Agents
Multi-agent system documentation for development workflow.

| File | Description |
|------|-------------|
| [../.claude/agents/README.md](../.claude/agents/README.md) | Agent system overview and workflow |
| [../.claude/agents/design-analysis-agent.md](../.claude/agents/design-analysis-agent.md) | Research and architecture design agent |
| [../.claude/agents/android-expert-agent.md](../.claude/agents/android-expert-agent.md) | ARCore implementation expert |
| [../.claude/agents/ios-expert-agent.md](../.claude/agents/ios-expert-agent.md) | ARKit implementation expert |
| [../.claude/agents/main-developer-agent.md](../.claude/agents/main-developer-agent.md) | Core development agent (Domain/Data/Presentation) |
| [../.claude/agents/bug-fixer-agent.md](../.claude/agents/bug-fixer-agent.md) | Debugging and bug fixing agent |
| [../.claude/agents/test-developer-agent.md](../.claude/agents/test-developer-agent.md) | Unit testing agent |
| [../.claude/agents/code-reviewer-agent.md](../.claude/agents/code-reviewer-agent.md) | Code quality control agent |

---

### 📱 Platform Specific

#### Android (ARCore)
Android platform-specific documentation (ARCore, SceneView, Kotlin).

| File | Description |
|------|-------------|
| **[ANDROID_ARCORE_STATE_SYNC_FIX.md](./ANDROID_ARCORE_STATE_SYNC_FIX.md)** | **🔴 FIXED** - AndroidView stale closure fix with rememberUpdatedState |
| [ARCORE_QUICK_REFERENCE.md](./ARCORE_QUICK_REFERENCE.md) | **📚 Quick Reference** - Patterns, templates, gotchas for AR development |
| [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) | **🚀 Deployment** - Build, test, and deployment instructions |
| [ANDROID_EXPERT_SESSION_SUMMARY.md](./ANDROID_EXPERT_SESSION_SUMMARY.md) | **📋 Session Summary** - Complete analysis and solution documentation |
| [android-arcore-analysis.md](./guides/hit-testing/android-arcore-analysis.md) | ARCore hit testing analysis and fixes |
| [android-arcore-summary.md](./android-arcore-summary.md) | Executive summary of Android implementation issues |
| [arcore-best-practices-cheatsheet.md](./arcore-best-practices-cheatsheet.md) | Quick reference for ARCore best practices |

#### iOS (ARKit)
iOS platform-specific documentation (ARKit, RealityKit, Swift).

| File | Description |
|------|-------------|
| [ios/ios-arkit-quick-fix-guide.md](./ios/ios-arkit-quick-fix-guide.md) | **🔥 START HERE** - 3-4 saat içinde critical issues düzelt |
| [ios/ios-arkit-hit-testing-report.md](./ios/ios-arkit-hit-testing-report.md) | **📚 COMPREHENSIVE** - iOS ARKit hit testing kapsamlı implementation raporu (1000+ satır) |
| [ios/ios-expert-report.md](./ios/ios-expert-report.md) | Comprehensive iOS ARKit implementation report |
| [ios/ios-expert-summary.md](./ios/ios-expert-summary.md) | iOS implementation summary |
| [ios/ios-implementation-checklist.md](./ios/ios-implementation-checklist.md) | iOS implementation checklist |
| [ios/ios-implementation-code-examples.md](./ios/ios-implementation-code-examples.md) | iOS code examples and snippets |
| [ios/ios-quick-reference.md](./ios/ios-quick-reference.md) | Quick reference for iOS implementation |

---

### 📖 Guides
Step-by-step guides and checklists.

| File | Description |
|------|-------------|
| [guides/test-implementation-guide.md](./guides/test-implementation-guide.md) | Guide for implementing unit tests |
| [guides/code-review-checklist.md](./guides/code-review-checklist.md) | Code review checklist and standards |
| **[guides/hit-testing/README.md](./guides/hit-testing/README.md)** | **📘 NEW** - Comprehensive hit testing documentation hub |

---

### 📊 Reports
Development reports, summaries, and status updates.

| File | Description |
|------|-------------|
| [reports/code-fixes-index.md](./reports/code-fixes-index.md) | Complete code fixes index and verification |
| [reports/implementation-complete.md](./reports/implementation-complete.md) | Implementation completion report |
| [reports/changes-reference.md](./reports/changes-reference.md) | Reference of all code changes made |
| [reports/code-fixes-summary.md](./reports/code-fixes-summary.md) | Summary of bug fixes and improvements |
| [reports/test-coverage-report.md](./reports/test-coverage-report.md) | Test coverage metrics and analysis |
| [reports/test-files-summary.md](./reports/test-files-summary.md) | Summary of all test files |

---

## 🗺️ Navigation by Topic

### Getting Started
1. [README.md](../README.md) - Start here
2. [CLAUDE.md](../CLAUDE.md) - Understand the architecture
3. [../.claude/agents/README.md](../.claude/agents/README.md) - Learn about the development workflow

### Architecture & Design
1. [architecture/technical-analysis.md](./architecture/technical-analysis.md)
2. [../.claude/agents/design-analysis-agent.md](../.claude/agents/design-analysis-agent.md)
3. [CLAUDE.md](../CLAUDE.md) - Key patterns section

### Platform Implementation

**Android (ARCore):**
- 🔴 **[guides/hit-testing/android-arcore-analysis.md](./guides/hit-testing/android-arcore-analysis.md)** - Critical implementation issues
- [android-arcore-summary.md](./android-arcore-summary.md) - Executive summary
- [arcore-best-practices-cheatsheet.md](./arcore-best-practices-cheatsheet.md) - Quick reference
- [../.claude/agents/android-expert-agent.md](../.claude/agents/android-expert-agent.md) - Agent documentation

**iOS (ARKit):**
- [ios/ios-arkit-hit-testing-report.md](./ios/ios-arkit-hit-testing-report.md) - Hit testing implementation
- [ios/ios-expert-report.md](./ios/ios-expert-report.md) - Comprehensive report
- [ios/ios-implementation-code-examples.md](./ios/ios-implementation-code-examples.md) - Code examples
- [ios/ios-quick-reference.md](./ios/ios-quick-reference.md) - Quick reference

### Development Workflow
1. [../.claude/agents/main-developer-agent.md](../.claude/agents/main-developer-agent.md) - Core development
2. [../.claude/agents/test-developer-agent.md](../.claude/agents/test-developer-agent.md) - Testing
3. [../.claude/agents/code-reviewer-agent.md](../.claude/agents/code-reviewer-agent.md) - Code review
4. [../.claude/agents/bug-fixer-agent.md](../.claude/agents/bug-fixer-agent.md) - Bug fixing

### Testing
1. [guides/TEST_IMPLEMENTATION_GUIDE.md](./guides/TEST_IMPLEMENTATION_GUIDE.md)
2. [reports/TEST_COVERAGE_REPORT.md](./reports/TEST_COVERAGE_REPORT.md)
3. [reports/TEST_FILES_SUMMARY.md](./reports/TEST_FILES_SUMMARY.md)

### Quality Assurance
1. [guides/CODE_REVIEW_CHECKLIST.md](./guides/CODE_REVIEW_CHECKLIST.md)
2. [agents/code-reviewer-agent.md](./agents/code-reviewer-agent.md)
3. [reports/CODE_FIXES_SUMMARY.md](./reports/CODE_FIXES_SUMMARY.md)

---

## 📝 Document Status

| Category | File Count | Status |
|----------|------------|--------|
| Architecture | 1 | ✅ Complete |
| Agents | 8 | ✅ Complete |
| Android Docs | 7 | ✅ State Sync Fixed |
| iOS Docs | 6 | ✅ Complete |
| Guides | 4 | ✅ Complete |
| Reports | 6 | ✅ Complete |
| **Total** | **32** | 🟢 Android Fix Deployed |

---

## 🔄 Recent Updates

- **2026-03-30**: 🆕 **[Drag-and-Drop Design Document](./DRAG_DROP_DESIGN.md)** - Complete UX/Architecture design for drag-to-move and drag-to-delete
- **2024-04-02**: ✅ **Android State Sync Bug FIXED** - rememberUpdatedState solution implemented
- **2024-04-02**: Added 4 new Android documentation files (fix analysis, quick reference, deployment guide, session summary)
- **2024-03-30**: 🔴 **Android ARCore critical analysis completed** - 5 major issues found in hit testing
- **2024-03-30**: Added Android ARCore implementation documentation (3 new files)
- **2024-03-31**: Documentation reorganized into `docs/` folder structure
- **2024-03-31**: Agents updated with Flutter-inspired DDD patterns
- **2024-03-31**: Added Value Objects, DTO/Mapper patterns, Base Classes
- **2024-03-30**: Initial agent system created

## ⚠️ Action Required

### 🔴 Android Critical Issues
The Android ARCore implementation has **5 critical issues** that must be fixed before production:

1. **Missing Anchor usage** - Models not anchored properly
2. **No hit result filtering** - Distance/confidence checks missing
3. **No plane tracking state checks** - Can place on invalid planes
4. **No pose polygon validation** - Models can float in air
5. **DepthPoint not prioritized** - Missing most accurate placement

**Estimated fix time:** 5 hours (Sprint 1)  
**See:** [ANDROID_ARCORE_HIT_TEST_ANALYSIS.md](./ANDROID_ARCORE_HIT_TEST_ANALYSIS.md) for detailed fixes

---

## 📚 External References

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [ARCore Developer Guide](https://developers.google.com/ar)
- [ARKit Documentation](https://developer.apple.com/documentation/arkit)
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design Reference](https://www.domainlanguage.com/ddd/reference/)
