# 📋 Workflow Examples

Real-world examples of the Git workflow in action.

---

## Example 1: Feature Implementation

**Task:** Implement drag-and-drop for AR objects  
**Agent:** android-expert-agent  
**Task ID:** `drag-and-drop`

### Step-by-Step

```bash
# 1. Agent starts from dev
git checkout dev
git pull origin dev

# 2. Create feature branch
git checkout -b feature/drag-and-drop

# Current branch: feature/drag-and-drop
# Working directory: clean

# 3. Agent implements drag-and-drop
# Modifies:
# - composeApp/src/androidMain/kotlin/com/trendhive/arsample/ar/ARView.kt
# - composeApp/src/commonMain/kotlin/com/trendhive/arsample/ar/PlatformARView.kt
# - composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/viewmodel/ARViewModel.kt

# 4. Agent runs tests
./gradlew :composeApp:testDebugUnitTest --tests "*DragDropTest"
# Result: ✅ All tests pass

# 5. Agent commits changes
git add composeApp/src/androidMain/kotlin/com/trendhive/arsample/ar/ARView.kt
git add composeApp/src/commonMain/kotlin/com/trendhive/arsample/ar/PlatformARView.kt
git add composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/viewmodel/ARViewModel.kt

git commit -m "feat(ar): implement drag-and-drop for placed AR objects

- Add long-press gesture detection in ARView.kt
- Update PlatformARView interface with onObjectPositionChanged callback
- Implement SceneView node editing (isPositionEditable = true)
- Add visual feedback during drag (scale 1.1x)
- Update ARViewModel to track position changes

Implements: drag-and-drop
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# 6. Agent pushes to remote
git push origin feature/drag-and-drop

# 7. Agent reports to orchestrator
# "Task drag-and-drop completed. Branch feature/drag-and-drop ready for review."
```

### Code Review Phase

```bash
# Orchestrator invokes code-reviewer-agent
# Reviewer checks feature/drag-and-drop branch

# code-reviewer-agent performs checks:
git fetch origin
git checkout feature/drag-and-drop

# Check 1: Build verification
./gradlew :composeApp:assembleDebug
# ✅ Build successful

# Check 2: Test coverage
./gradlew :composeApp:testDebugUnitTest
# ✅ 87% coverage (target: 85%)

# Check 3: Architecture compliance
# ✅ PlatformARView interface updated correctly (expect/actual pattern)
# ✅ ViewModel properly handles state
# ✅ No domain layer violations

# Check 4: Code quality
# ✅ Kotlin conventions followed
# ✅ No code smells detected
# ✅ Functions under 50 lines

# Reviewer generates report:
```

**Review Report:**
```markdown
# Code Review Report

**Branch:** feature/drag-and-drop  
**Agent:** android-expert-agent  
**Reviewer:** code-reviewer-agent  
**Date:** 2026-04-04

## Summary
Implementation of drag-and-drop functionality for placed AR objects.
Changes are well-structured and follow Clean Architecture principles.

## Architecture ✅
- [x] Clean Architecture compliance verified
- [x] Platform-specific code properly isolated (expect/actual)
- [x] ViewModel state management correct

## Code Quality ✅
- [x] Kotlin conventions followed
- [x] No code smells detected
- [x] Proper documentation added
- [x] Function length acceptable (max: 42 lines)

## Testing ✅
- [x] Unit tests added for new functionality
- [x] Coverage: 87% (exceeds 85% target)
- [x] All tests passing

## Build ✅
- [x] Compiles successfully
- [x] No warnings

## Recommendation
✅ **APPROVED** - Ready to merge to dev

## Next Steps
1. Merge feature/drag-and-drop → dev
2. Delete feature branch after merge
3. Update SQL: status = 'done'
```

### Merge Phase

```bash
# Orchestrator performs merge (after approval)
git checkout dev
git pull origin dev

# Merge with --no-ff to preserve feature history
git merge --no-ff feature/drag-and-drop -m "Merge feature/drag-and-drop into dev

Implements drag-and-drop for AR objects with visual feedback.

Reviewed-by: code-reviewer-agent
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# Push to remote
git push origin dev

# Cleanup: Delete feature branch
git branch -d feature/drag-and-drop
git push origin --delete feature/drag-and-drop

# Update SQL database
# UPDATE todos SET status = 'done' WHERE id = 'drag-and-drop';
```

**Result:**
- ✅ Feature merged to `dev`
- ✅ Feature branch deleted
- ✅ Task marked as done in SQL
- ✅ `dev` contains drag-and-drop functionality

---

## Example 2: Bug Fix

**Task:** Fix ModalBottomSheet scroll-to-dismiss bug  
**Agent:** bug-fixer-agent  
**Task ID:** `bottomsheet-scroll-bug`

### Step-by-Step

```bash
# 1. Start from dev
git checkout dev
git pull origin dev

# 2. Create bugfix branch
git checkout -b bugfix/bottomsheet-scroll

# 3. Agent analyzes bug
# Root cause: Nested scroll events not consumed
# File: ARScreen.kt

# 4. Agent implements fix
# Modified: composeApp/src/commonMain/kotlin/.../ARScreen.kt

# 5. Agent tests locally
./gradlew :composeApp:assembleDebug
# ✅ Build successful

# 6. Agent commits
git add composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/ui/screens/ARScreen.kt

git commit -m "fix(ui): prevent unwanted ModalBottomSheet dismissal on scroll

Root cause: Nested scroll events were not consumed before reaching
the ModalBottomSheet dismiss handler. Material3's default configuration
doesn't distinguish between content scrolling and swipe-to-dismiss.

Solution:
- Configure rememberModalBottomSheetState with skipPartiallyExpanded=false
- Add explicit drag handle for intentional dismissal
- Apply .fillMaxWidth() modifier for proper touch handling

Tested:
- ✅ Scrolling to bottom does not dismiss sheet
- ✅ Drag handle still dismisses correctly
- ✅ Tap outside still dismisses

Fixes: bottomsheet-scroll-bug
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# 7. Push and request review
git push origin bugfix/bottomsheet-scroll
```

### Review & Merge

```bash
# Code review: ✅ APPROVED

# Merge to dev
git checkout dev
git merge --no-ff bugfix/bottomsheet-scroll
git push origin dev

# Cleanup
git branch -d bugfix/bottomsheet-scroll

# SQL: UPDATE todos SET status = 'done' WHERE id = 'bottomsheet-scroll-bug';
```

---

## Example 3: CI/CD Pipeline

**Task:** Add iOS CI workflow  
**Agent:** ios-expert-agent  
**Task ID:** `github-workflow-ios`

### Workflow

```bash
# 1. Create CI branch
git checkout dev
git pull origin dev
git checkout -b ci/ios-workflow

# 2. Create workflow files
# Created:
# - .github/workflows/ios-ci.yml
# - docs/ios/ios-workflow-readme.md
# - iosApp/.swiftlint.yml

# 3. Commit
git add .github/workflows/ios-ci.yml
git add docs/ios/ios-workflow-readme.md
git add iosApp/.swiftlint.yml

git commit -m "ci: add GitHub Actions workflow for iOS builds

Features:
- macOS 14 runner with Xcode 15+
- Build KMP framework (linkDebugFrameworkIosSimulatorArm64)
- Run xcodebuild for simulator
- SwiftLint code quality checks
- Gradle dependency caching for faster builds

Configuration:
- Triggers on push to main/develop and PRs
- No code signing (simulator build)
- Tests run with continue-on-error (optional)
- Build artifacts uploaded on failure

Documentation:
- Comprehensive README with troubleshooting
- Quick reference cheat sheet
- Implementation summary

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# 4. Push and review
git push origin ci/ios-workflow

# 5. Review: ✅ APPROVED

# 6. Merge
git checkout dev
git merge --no-ff ci/ios-workflow
git push origin dev

# 7. Cleanup
git branch -d ci/ios-workflow
```

---

## Example 4: Refactoring

**Task:** Implement DDD Value Object pattern  
**Agent:** main-developer-agent  
**Task ID:** `ddd-value-objects`

### Workflow

```bash
# 1. Create refactor branch
git checkout dev
git pull origin dev
git checkout -b refactor/ddd-value-objects

# 2. Implement BaseValueObject
# Created:
# - domain/base/BaseValueObject.kt
# - domain/model/valueobjects/ModelUri.kt (refactored)
# - domain/model/valueobjects/ObjectName.kt (refactored)

# 3. Run tests
./gradlew :composeApp:testDebugUnitTest
# ✅ All tests pass

# 4. Commit
git add composeApp/src/commonMain/kotlin/com/trendhive/arsample/domain/base/BaseValueObject.kt
git add composeApp/src/commonMain/kotlin/com/trendhive/arsample/domain/model/valueobjects/

git commit -m "refactor(domain): implement BaseValueObject pattern for DDD

- Create BaseValueObject abstract class with generic type support
- Refactor ModelUri to extend BaseValueObject
- Refactor ObjectName to extend BaseValueObject
- Add equals/hashCode/toString implementations
- Maintain factory method pattern (create() returning Result<T>)

Benefits:
- Consistent Value Object pattern across domain
- Type-safe validation at boundaries
- Immutability enforced
- DDD principles properly implemented

Tests:
- ✅ ModelUri validation tests pass
- ✅ ObjectName validation tests pass
- ✅ BaseValueObject equality tests pass

Implements: ddd-value-objects
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# 5. Push, review, merge
git push origin refactor/ddd-value-objects
# ... review: APPROVED ...
git checkout dev
git merge --no-ff refactor/ddd-value-objects
git push origin dev
git branch -d refactor/ddd-value-objects
```

---

## Example 5: Release to Main

**Scenario:** Dev branch has 5 completed features, ready for release

```bash
# User decides to release dev to main
git checkout main
git pull origin main

# Review what's in dev
git log main..dev --oneline
# Output:
# abc123 Merge ci/ios-workflow into dev
# def456 Merge bugfix/bottomsheet-scroll into dev
# ghi789 Merge feature/drag-and-drop into dev
# jkl012 Merge refactor/ddd-value-objects into dev

# Merge dev → main
git merge --no-ff dev -m "Release v1.2.0: Major feature update

Features:
- Drag-and-drop for AR objects
- DDD Value Object pattern implementation
- iOS CI/CD pipeline

Bug Fixes:
- ModalBottomSheet scroll-to-dismiss issue

Infrastructure:
- GitHub Actions workflow for iOS builds
- SwiftLint integration

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# Tag the release
git tag -a v1.2.0 -m "Release 1.2.0

Major feature update with drag-and-drop, DDD refactoring, and iOS CI."

# Push to remote
git push origin main --tags

# Continue development on dev
git checkout dev
```

**Result:**
- ✅ `main` updated with v1.2.0
- ✅ Git tag created
- ✅ `dev` continues for next sprint

---

## Key Takeaways

1. **Every task = dedicated branch** from `dev`
2. **Conventional commits** (feat:, fix:, refactor:, ci:, test:, docs:)
3. **Code review before merge** (code-reviewer-agent)
4. **No-ff merges** to preserve history
5. **User controls main** (releases when ready)

---

**See:** [WORKFLOW.md](../WORKFLOW.md) for complete workflow documentation.
