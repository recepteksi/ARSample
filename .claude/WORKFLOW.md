# 🔄 ARSample Development Workflow

**Version:** 1.0  
**Last Updated:** 2026-04-04  
**Status:** Active

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Git Branch Strategy](#git-branch-strategy)
3. [Agent Workflow](#agent-workflow)
4. [Branch Naming Convention](#branch-naming-convention)
5. [Task Lifecycle](#task-lifecycle)
6. [Code Review Process](#code-review-process)
7. [Merge Strategy](#merge-strategy)
8. [Examples](#examples)
9. [Best Practices](#best-practices)

---

## 🎯 Overview

This project follows a **GitFlow-inspired workflow** with agent-driven development. Each task is developed in isolation on feature branches, reviewed by the Code Reviewer agent, and merged to `dev` upon approval.

### Key Principles

- ✅ **Branch Isolation**: Every task gets its own feature branch
- ✅ **Agent Ownership**: Each agent works on dedicated branches
- ✅ **Code Review Gate**: All changes must pass code review before merge
- ✅ **Integration Branch**: `dev` is the integration branch for ongoing work
- ✅ **Stable Main**: `main` branch contains only production-ready code

---

## 🌳 Git Branch Strategy

```
main (production-ready)
  │
  └─── dev (integration branch)
        │
        ├─── feature/ARS-5-3d-model-preview (task branch)
        ├─── bugfix/ARS-6-gallery-crash (task branch)
        ├─── fix/ARS-7-camera-controls-symmetry (task branch)
        ├─── feature/ARS-8-short-description (task branch)
        └─── bugfix/ARS-9-short-description (task branch)
```

### Branch Types

| Branch Type | Prefix | Purpose | Lifespan | Example |
|-------------|--------|---------|----------|---------|
| **main** | - | Production-ready code | Permanent | `main` |
| **dev** | - | Integration branch | Permanent | `dev` |
| **feature** | `feature/` | New features | Temporary | `feature/drag-and-drop` |
| **bugfix** | `bugfix/` | Bug fixes | Temporary | `bugfix/bottomsheet-scroll` |
| **ci** | `ci/` | CI/CD pipelines | Temporary | `ci/android-workflow` |
| **refactor** | `refactor/` | Code refactoring | Temporary | `refactor/ddd-value-objects` |
| **test** | `test/` | Test improvements | Temporary | `test/ar-scene-tests` |

---

## 🤖 Agent Workflow

### Standard Agent Task Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. TASK ASSIGNMENT                                          │
│    Agent receives task from orchestrator                     │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. BRANCH CREATION                                          │
│    Agent creates feature branch from 'dev'                   │
│    git checkout dev                                          │
│    git pull origin dev                                       │
│    git checkout -b feature/ARS-N-task-name                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. IMPLEMENTATION                                           │
│    Agent implements changes on feature branch                │
│    - Writes code                                             │
│    - Writes tests                                            │
│    - Updates documentation                                   │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. LOCAL VALIDATION                                         │
│    Agent runs local checks                                   │
│    - Build: ./gradlew :composeApp:assembleDebug             │
│    - Tests: ./gradlew :composeApp:testDebugUnitTest         │
│    - Lint: Check for warnings                                │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. COMMIT & PUSH                                            │
│    Agent commits and pushes to remote                        │
│    git add .                                                 │
│    git commit -m "feat: task description"                    │
│    git push origin feature/ARS-N-task-name                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. CODE REVIEW REQUEST                                      │
│    Orchestrator invokes Code Reviewer agent                  │
│    Reviewer checks:                                          │
│    - Architecture compliance (DDD, Clean Architecture)       │
│    - Code quality (conventions, patterns)                    │
│    - Test coverage (85%+ target)                             │
│    - Documentation                                           │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
                  ┌─────┴─────┐
                  │  APPROVED? │
                  └─────┬─────┘
                        │
              ┌─────────┴─────────┐
              │                   │
              ▼                   ▼
         ✅ YES                ❌ NO
              │                   │
              ▼                   ▼
┌────────────────────┐  ┌────────────────────┐
│ 7a. MERGE TO DEV   │  │ 7b. FIX ISSUES     │
│    git checkout dev│  │    Agent addresses │
│    git pull        │  │    review comments │
│    git merge       │  │    Return to step 3│
│    git push        │  └────────────────────┘
└────────────────────┘
              │
              ▼
┌────────────────────┐
│ 8. CLEANUP         │
│    git branch -d   │
│    feature/branch  │
└────────────────────┘
```

---

## 📝 Branch Naming Convention

### Format

```
<type>/<task-id>-<short-description>
```

### Components

- **type**: Branch type prefix (feature, bugfix, ci, refactor, test)
- **task-id**: Task ID from Jira board (ARS-N format, e.g. ARS-5)
- **short-description**: Optional 2-3 word description

### Examples

| Task ID | Branch Name | Agent |
|---------|-------------|-------|
| `ARS-5` | `feature/ARS-5-3d-model-preview` | android-expert-agent |
| `ARS-6` | `bugfix/ARS-6-gallery-crash` | bug-fixer-agent |
| `ARS-7` | `fix/ARS-7-camera-controls-symmetry` | main-developer-agent |
| `ARS-8` | `feature/ARS-8-short-description` | main-developer-agent |
| `ARS-9` | `bugfix/ARS-9-short-description` | bug-fixer-agent |

### Branch Type Selection Guide

```
Is it a bug? ──────────────────────────> bugfix/
Is it CI/CD related? ──────────────────> ci/
Is it refactoring existing code? ──────> refactor/
Is it adding tests only? ──────────────> test/
Is it a new feature? ──────────────────> feature/
```

---

## 🔄 Task Lifecycle

### Phase 1: Planning (SQL Database)

```sql
-- Task created in 'pending' status
INSERT INTO todos (id, title, description, assigned_agent, priority)
VALUES ('drag-and-drop', 'Drag-and-Drop Objects', '...', 'android-expert-agent', 4);
```

### Phase 2: Development (Git)

```bash
# Agent creates branch
git checkout dev
git pull origin dev
git checkout -b feature/drag-and-drop

# Agent implements
# ... code changes ...

# Agent commits
git add .
git commit -m "feat(ar): implement drag-and-drop for placed objects

- Add long-press gesture recognizer
- Update PlatformARView interface
- Implement Android drag using SceneView
- Add visual feedback during drag
- Update ViewModel for position tracking

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

git push origin feature/drag-and-drop
```

```sql
-- Update task status
UPDATE todos SET status = 'in_progress' WHERE id = 'drag-and-drop';
```

### Phase 3: Code Review

```bash
# Orchestrator invokes code-reviewer-agent
# Reviewer checks the branch 'feature/drag-and-drop'
```

**Review Checklist** (from `code-reviewer-agent.md`):
- ✅ DDD principles followed
- ✅ Clean Architecture layers respected
- ✅ Kotlin/Swift conventions
- ✅ Test coverage ≥85%
- ✅ No code smells
- ✅ Documentation updated

### Phase 4: Merge or Fix

**If Approved:**
```bash
git checkout dev
git pull origin dev
git merge --no-ff feature/drag-and-drop
git push origin dev
git branch -d feature/drag-and-drop
```

```sql
UPDATE todos SET status = 'done' WHERE id = 'drag-and-drop';
```

**If Rejected:**
```sql
-- Agent receives feedback, makes fixes
UPDATE todos SET status = 'in_progress' WHERE id = 'drag-and-drop';
-- Return to Phase 2
```

---

## 👨‍💻 Code Review Process

### Automated Review (code-reviewer-agent)

The Code Reviewer agent performs comprehensive checks:

#### 1. **Architecture Compliance**
- ✅ Domain layer has no external dependencies
- ✅ Dependencies flow inward (Presentation → Application → Domain ← Infrastructure)
- ✅ Use cases have single responsibility
- ✅ Repository pattern followed (interface in domain, impl in infrastructure)

#### 2. **Code Quality**
- ✅ Kotlin conventions (naming, formatting)
- ✅ Functions under 50 lines
- ✅ Classes under 300 lines
- ✅ No magic numbers/strings
- ✅ Proper error handling (Result<T> pattern)

#### 3. **Testing**
- ✅ Unit tests exist for new code
- ✅ Coverage ≥85% for critical paths
- ✅ Tests use Given-When-Then pattern
- ✅ MockK for all mocking

#### 4. **Documentation**
- ✅ Public APIs documented
- ✅ Complex logic explained
- ✅ README updated if needed

### Review Output

```markdown
# Code Review Report

**Branch:** feature/drag-and-drop  
**Agent:** android-expert-agent  
**Status:** ✅ APPROVED / ❌ NEEDS WORK

## Summary
[Brief summary of changes]

## Architecture ✅
- [x] Clean Architecture compliance
- [x] DDD patterns followed

## Code Quality ✅
- [x] Kotlin conventions
- [x] No code smells

## Testing ⚠️
- [ ] Coverage: 78% (target: 85%)
- Action: Add tests for ARView gesture handling

## Recommendation
APPROVE with minor suggestions
```

---

## 🔀 Merge Strategy

### dev ← feature branch (Automated)

**Strategy:** `--no-ff` (no fast-forward)

```bash
git checkout dev
git merge --no-ff feature/drag-and-drop -m "Merge feature/drag-and-drop into dev"
git push origin dev
```

**Why no-ff?**
- Preserves feature branch history
- Clear merge commits for traceability
- Easy to revert entire features

### main ← dev (Manual, User-Controlled)

**Strategy:** Manual merge when ready for release

```bash
# User decides when to promote dev to main
git checkout main
git pull origin main
git merge --no-ff dev -m "Release v1.2.0: Drag-and-drop + iOS CI"
git tag -a v1.2.0 -m "Release 1.2.0"
git push origin main --tags
```

**When to merge dev → main:**
- ✅ All features in milestone complete
- ✅ Full test suite passes
- ✅ Manual QA completed
- ✅ Ready for production release

---

## 📚 Examples

### Example 1: Feature Implementation

**Task:** Implement drag-and-drop feature

```bash
# 1. Start from dev
git checkout dev
git pull origin dev

# 2. Create feature branch
git checkout -b feature/drag-and-drop

# 3. Implement (by android-expert-agent)
# ... code changes in ARView.kt, PlatformARView.kt ...

# 4. Test locally
./gradlew :composeApp:testDebugUnitTest --tests "*DragAndDropTest"

# 5. Commit
git add .
git commit -m "feat(ar): implement drag-and-drop for AR objects

- Add long-press gesture detection
- Update PlatformARView with onObjectPositionChanged
- Implement SceneView node editing
- Add visual feedback (scale 1.1x during drag)

Closes: drag-and-drop
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# 6. Push
git push origin feature/drag-and-drop

# 7. Code review (automated)
# code-reviewer-agent checks the branch

# 8. If approved, merge to dev
git checkout dev
git merge --no-ff feature/drag-and-drop
git push origin dev

# 9. Cleanup
git branch -d feature/drag-and-drop
git push origin --delete feature/drag-and-drop
```

### Example 2: Bug Fix

**Task:** Fix ModalBottomSheet scroll bug

```bash
# 1. Start from dev
git checkout dev
git pull origin dev

# 2. Create bugfix branch
git checkout -b bugfix/bottomsheet-scroll

# 3. Analyze and fix (by bug-fixer-agent)
# ... fix in ARScreen.kt ...

# 4. Test
./gradlew :composeApp:assembleDebug

# 5. Commit
git add .
git commit -m "fix(ui): prevent unwanted ModalBottomSheet dismissal on scroll

Root cause: Nested scroll events not consumed before reaching
dismiss handler in Material3 ModalBottomSheet.

Solution: Configure rememberModalBottomSheetState with
skipPartiallyExpanded=false and explicit drag handle.

Fixes: bottomsheet-scroll-bug
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# 6. Push and review
git push origin bugfix/bottomsheet-scroll

# 7. Merge to dev after approval
git checkout dev
git merge --no-ff bugfix/bottomsheet-scroll
git push origin dev

# 8. Cleanup
git branch -d bugfix/bottomsheet-scroll
```

### Example 3: CI/CD Pipeline

**Task:** Add iOS CI workflow

```bash
# 1. Create CI branch
git checkout dev
git checkout -b ci/ios-workflow

# 2. Create workflow (by ios-expert-agent)
# ... create .github/workflows/ios-ci.yml ...

# 3. Commit
git add .github/workflows/
git commit -m "ci: add GitHub Actions workflow for iOS builds

- macOS 14 runner with Xcode 15+
- Build KMP framework (linkDebugFrameworkIosSimulatorArm64)
- Run xcodebuild for simulator
- SwiftLint code quality checks
- Gradle dependency caching

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# 4. Push and merge
git push origin ci/ios-workflow
# ... code review ...
git checkout dev
git merge --no-ff ci/ios-workflow
git push origin dev
```

---

## ✅ Best Practices

### For Agents

1. **Always start from latest dev**
   ```bash
   git checkout dev && git pull origin dev
   ```

2. **One task = One branch**
   - Don't mix multiple tasks in one branch
   - Keep changes focused

3. **Commit message convention**
   - Use conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `ci:`, `docs:`
   - Include task ID in commit body
   - Add co-authored trailer

4. **Run tests before push**
   ```bash
   ./gradlew :composeApp:testDebugUnitTest
   ```

5. **Keep branches short-lived**
   - Merge within 1-2 days
   - Don't let branches drift from dev

### For Orchestrator

1. **Track dependencies**
   - Check `todo_deps` table before assigning
   - Don't start dependent tasks until dependencies are merged

2. **Coordinate merges**
   - Ensure code review before merge
   - Handle merge conflicts if they arise

3. **Update SQL after merge**
   ```sql
   UPDATE todos SET status = 'done' WHERE id = 'task-id';
   ```

### For User

1. **Review dev regularly**
   - Check `dev` branch for integrated features
   - Test manually if needed

2. **Merge to main when ready**
   - Only when ready for release
   - Tag with version number

3. **Monitor branch count**
   ```bash
   git branch -a | grep feature | wc -l
   ```
   - Clean up stale branches periodically

---

## 🔧 Troubleshooting

### Merge Conflicts

```bash
# If conflict during merge to dev
git checkout dev
git merge feature/task-name

# CONFLICT in file.kt
# Resolve manually, then:
git add file.kt
git commit -m "Merge feature/task-name into dev (resolved conflicts)"
git push origin dev
```

### Stale Branch

```bash
# If feature branch is behind dev
git checkout feature/task-name
git merge dev  # Bring dev changes into feature
# Resolve conflicts if any
git push origin feature/task-name
```

### Abandoned Branch

```bash
# Delete local branch
git branch -D feature/abandoned-task

# Delete remote branch
git push origin --delete feature/abandoned-task

# Update SQL
UPDATE todos SET status = 'pending' WHERE id = 'abandoned-task';
```

---

## 📊 Workflow Metrics

Track workflow health:

```sql
-- Active feature branches
SELECT COUNT(*) FROM todos WHERE status = 'in_progress';

-- Completed tasks
SELECT COUNT(*) FROM todos WHERE status = 'done';

-- Blocked tasks
SELECT id, title FROM todos WHERE status = 'blocked';

-- Average time to merge (manual tracking)
```

---

## 🎯 Summary

| Branch | Purpose | Merge Target | Merge Frequency |
|--------|---------|--------------|-----------------|
| `main` | Production | - | Release only |
| `dev` | Integration | `main` | On release |
| `feature/*` | New features | `dev` | After review |
| `bugfix/*` | Bug fixes | `dev` | After review |
| `ci/*` | CI/CD | `dev` | After review |

**Golden Rule:** Every change goes through `feature/bugfix/ci → dev → main` flow.

---

**Status:** ✅ **Active Workflow**  
**Next Review:** When project scales beyond 10 agents
