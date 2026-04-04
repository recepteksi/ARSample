# 🚀 Git Workflow Quick Start

**Quick reference for agents and developers using the ARSample Git workflow.**

---

## 📌 Branch Structure

```
main (production)
  │
  └─── dev (integration)
        │
        ├─── feature/drag-and-drop
        ├─── bugfix/bottomsheet-scroll
        ├─── ci/ios-workflow
        └─── refactor/ddd-value-objects
```

---

## 🎯 Quick Commands for Agents

### Start a New Task

```bash
# 1. Get latest dev
git checkout dev
git pull origin dev

# 2. Create feature branch
git checkout -b <type>/<task-id>

# Examples:
git checkout -b feature/drag-and-drop
git checkout -b bugfix/bottomsheet-scroll
git checkout -b ci/android-workflow
git checkout -b refactor/ddd-value-objects
```

### Commit Changes

```bash
# 1. Stage your changes
git add .

# 2. Commit with conventional commits
git commit -m "<type>(<scope>): <description>

<body explaining what and why>

<task-reference>
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# Examples:
git commit -m "feat(ar): implement drag-and-drop

- Add gesture recognizers
- Update ViewModel

Implements: drag-and-drop
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

git commit -m "fix(ui): prevent bottomsheet dismiss on scroll

Fixes: bottomsheet-scroll-bug
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Push for Review

```bash
# Push to remote
git push origin <branch-name>

# Example:
git push origin feature/drag-and-drop

# Report to orchestrator:
# "Task completed. Branch feature/drag-and-drop ready for review."
```

### After Code Review Approval

```bash
# Orchestrator merges to dev:
git checkout dev
git pull origin dev
git merge --no-ff feature/drag-and-drop
git push origin dev

# Cleanup
git branch -d feature/drag-and-drop
git push origin --delete feature/drag-and-drop
```

---

## 🏷️ Branch Types

| Prefix | Use Case | Example |
|--------|----------|---------|
| `feature/` | New features | `feature/drag-and-drop` |
| `bugfix/` | Bug fixes | `bugfix/bottomsheet-scroll` |
| `ci/` | CI/CD changes | `ci/ios-workflow` |
| `refactor/` | Code refactoring | `refactor/ddd-value-objects` |
| `test/` | Test improvements | `test/ar-scene-coverage` |

---

## 💬 Commit Types

| Type | Description | Example |
|------|-------------|---------|
| `feat:` | New feature | `feat(ar): add drag-and-drop` |
| `fix:` | Bug fix | `fix(ui): bottomsheet scroll bug` |
| `refactor:` | Code refactoring | `refactor(domain): DDD value objects` |
| `test:` | Add/update tests | `test(viewmodel): add AR tests` |
| `ci:` | CI/CD changes | `ci: add iOS workflow` |
| `docs:` | Documentation | `docs: update workflow guide` |
| `chore:` | Maintenance | `chore: update dependencies` |

---

## 🔍 Check Status

```bash
# Current branch
git branch --show-current

# Changed files
git status --short

# Recent commits
git log --oneline -5

# All branches
git branch -a
```

---

## ⚠️ Important Rules

1. ✅ **Always branch from dev** (not main)
2. ✅ **One task = one branch**
3. ✅ **Code review before merge**
4. ✅ **Use conventional commits**
5. ✅ **No direct commits to dev or main**

---

## 📚 Full Documentation

- **Complete Workflow:** `.claude/WORKFLOW.md`
- **Examples:** `.claude/WORKFLOW_EXAMPLES.md`
- **Agent Docs:** `.claude/agents/`

---

**Current Branch:** `dev`  
**Integration:** All features merge to `dev` first  
**Production:** User merges `dev` → `main` for releases
