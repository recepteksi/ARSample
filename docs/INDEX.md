# ARSample Documentation Index

> **AR Sample Application** - Kotlin Multiplatform app for importing and placing 3D objects in AR scenes (Android ARCore + iOS ARKit)

## Quick Links

| Document | Description |
|----------|-------------|
| [README.md](../README.md) | Project overview and getting started |
| [CLAUDE.md](../CLAUDE.md) | AI assistant instructions and architecture |
| [CHANGELOG.md](../CHANGELOG.md) | Version history |

---

## Documentation Structure

### Architecture

| File | Description |
|------|-------------|
| [architecture/technical-analysis.md](./architecture/technical-analysis.md) | Technical architecture analysis and decisions |

---

### Design (UI/UX)

| File | Description |
|------|-------------|
| [design/README.md](./design/README.md) | Design documentation overview |
| [design/UI_UX_DESIGN_GUIDE.md](./design/UI_UX_DESIGN_GUIDE.md) | Material 3 + iOS HIG design system |
| [design/AR_COMPETITOR_ANALYSIS.md](./design/AR_COMPETITOR_ANALYSIS.md) | Industry analysis (IKEA, Amazon, Houzz) |
| [design/DESIGN_TOKENS.md](./design/DESIGN_TOKENS.md) | Colors, typography, spacing, icons |
| [design/drag-drop-design.md](./design/drag-drop-design.md) | Drag-to-move and drag-to-delete feature design |
| [design/app-icon/README.md](./design/app-icon/README.md) | App icon design documentation |
| [design/app-icon/SUMMARY.md](./design/app-icon/SUMMARY.md) | App icon package summary |
| [design/app-icon/color-palette.md](./design/app-icon/color-palette.md) | App icon color palette |
| [design/app-icon/android-adaptive-guide.md](./design/app-icon/android-adaptive-guide.md) | Android adaptive icon guide |
| [design/app-icon/ios-integration-guide.md](./design/app-icon/ios-integration-guide.md) | iOS icon integration guide |
| [design/splash/README.md](./design/splash/README.md) | Splash screen documentation |
| [design/splash/SPLASH_SCREEN_DESIGN_SPEC.md](./design/splash/SPLASH_SCREEN_DESIGN_SPEC.md) | Splash screen design spec |

---

### Android (ARCore)

| File | Description |
|------|-------------|
| [guides/arcore-quick-reference.md](./guides/arcore-quick-reference.md) | ARCore patterns and gotchas quick reference |
| [guides/arcore-best-practices.md](./guides/arcore-best-practices.md) | ARCore best practices cheatsheet |
| [guides/hit-testing/README.md](./guides/hit-testing/README.md) | Hit testing documentation hub |
| [guides/hit-testing/android-arcore-analysis.md](./guides/hit-testing/android-arcore-analysis.md) | ARCore hit testing analysis and fixes |
| [guides/hit-testing/implementation.md](./guides/hit-testing/implementation.md) | Hit testing implementation guide |
| [guides/hit-testing/quick-reference.md](./guides/hit-testing/quick-reference.md) | Hit testing quick reference |
| [guides/hit-testing/design.md](./guides/hit-testing/design.md) | Hit testing design document |
| [reports/android-arcore-summary.md](./reports/android-arcore-summary.md) | Executive summary of Android implementation |
| [reports/android-arcore-state-sync-fix.md](./reports/android-arcore-state-sync-fix.md) | AndroidView stale closure fix |
| [reports/android-expert-session-summary.md](./reports/android-expert-session-summary.md) | Android expert session documentation |
| [reports/android-fix-complete.md](./reports/android-fix-complete.md) | Android fix completion report |

---

### iOS (ARKit)

| File | Description |
|------|-------------|
| [ios/ios-arkit-quick-fix-guide.md](./ios/ios-arkit-quick-fix-guide.md) | Critical iOS issues quick fix guide |
| [ios/ios-arkit-hit-testing-report.md](./ios/ios-arkit-hit-testing-report.md) | iOS ARKit hit testing comprehensive report |
| [ios/ios-expert-report.md](./ios/ios-expert-report.md) | Comprehensive iOS ARKit implementation report |
| [ios/ios-expert-summary.md](./ios/ios-expert-summary.md) | iOS implementation summary |
| [ios/ios-implementation-checklist.md](./ios/ios-implementation-checklist.md) | iOS implementation checklist |
| [ios/ios-implementation-code-examples.md](./ios/ios-implementation-code-examples.md) | iOS code examples and snippets |
| [ios/ios-quick-reference.md](./ios/ios-quick-reference.md) | Quick reference for iOS implementation |
| [ios/ios-issues-analysis.md](./ios/ios-issues-analysis.md) | iOS issues analysis |
| [ios/ios-workflow-readme.md](./ios/ios-workflow-readme.md) | iOS CI/CD workflow documentation |

---

### Guides

| File | Description |
|------|-------------|
| [guides/test-implementation-guide.md](./guides/test-implementation-guide.md) | Unit test implementation guide |
| [guides/code-review-checklist.md](./guides/code-review-checklist.md) | Code review checklist and standards |
| [guides/deployment-guide.md](./guides/deployment-guide.md) | Build, test, and deployment instructions |
| [guides/ios-ci-quick-reference.md](./guides/ios-ci-quick-reference.md) | iOS CI/CD quick reference |

---

### Bugs

| File | Description |
|------|-------------|
| [bugs/BUG-001-import-feature-not-working.md](./bugs/BUG-001-import-feature-not-working.md) | BUG-001 import feature analysis |
| [bugs/bug-001-ar-placement-fix.md](./bugs/bug-001-ar-placement-fix.md) | BUG-001 AR placement fix |
| [bugs/bug-001-verification.md](./bugs/bug-001-verification.md) | BUG-001 fix verification |

---

### Reports

| File | Description |
|------|-------------|
| [reports/implementation-complete.md](./reports/implementation-complete.md) | Implementation completion report |
| [reports/changes-reference.md](./reports/changes-reference.md) | Reference of all code changes |
| [reports/code-fixes-summary.md](./reports/code-fixes-summary.md) | Bug fixes and improvements summary |
| [reports/code-fixes-index.md](./reports/code-fixes-index.md) | Complete code fixes index |
| [reports/test-coverage-report.md](./reports/test-coverage-report.md) | Test coverage metrics |
| [reports/test-files-summary.md](./reports/test-files-summary.md) | Summary of all test files |
| [reports/test-summary.md](./reports/test-summary.md) | ModelUri bug fix test suite summary |
| [reports/fix-drag-delete.md](./reports/fix-drag-delete.md) | Drag-delete fix report |
| [reports/DDD_STRUCTURE_UPDATE.md](./reports/DDD_STRUCTURE_UPDATE.md) | DDD structure update report |
| [reports/DDD_STRUCTURE_COMPARISON.md](./reports/DDD_STRUCTURE_COMPARISON.md) | DDD structure comparison |
| [reports/ANDROID_SPLASH_IMPLEMENTATION_REPORT.md](./reports/ANDROID_SPLASH_IMPLEMENTATION_REPORT.md) | Android splash screen implementation report |
| [reports/ios-workflow-implementation-summary.md](./reports/ios-workflow-implementation-summary.md) | iOS workflow implementation summary |
| [reports/i18n-implementation-report.md](./reports/i18n-implementation-report.md) | i18n implementation report |
| [reports/i18n-build-fix-report.md](./reports/i18n-build-fix-report.md) | i18n build fix report |

---

### Reviews

| File | Description |
|------|-------------|
| [reviews/README.md](./reviews/README.md) | Reviews overview |
| [reviews/DESIGN_REVIEW_SUMMARY.md](./reviews/DESIGN_REVIEW_SUMMARY.md) | Design review summary |
| [reviews/DESIGN_DOCS_REVIEW_2026-04-05.md](./reviews/DESIGN_DOCS_REVIEW_2026-04-05.md) | Design docs review |
| [reviews/APP_ICON_INTEGRATION_REVIEW_2026-04-05.md](./reviews/APP_ICON_INTEGRATION_REVIEW_2026-04-05.md) | App icon integration review |
| [reviews/SPLASH_REVIEW_SUMMARY.md](./reviews/SPLASH_REVIEW_SUMMARY.md) | Splash screen review summary |
| [reviews/SPLASH_SCREENS_REVIEW_2026-04-05.md](./reviews/SPLASH_SCREENS_REVIEW_2026-04-05.md) | Splash screens review |
| [reviews/ACTION_ITEMS_DESIGN_ANALYSIS_AGENT.md](./reviews/ACTION_ITEMS_DESIGN_ANALYSIS_AGENT.md) | Design analysis agent action items |

---

## Navigation by Role

- **Developers**: [CLAUDE.md](../CLAUDE.md) → [architecture/technical-analysis.md](./architecture/technical-analysis.md) → [guides/](./guides/)
- **Android Devs**: [guides/arcore-quick-reference.md](./guides/arcore-quick-reference.md) → [guides/hit-testing/](./guides/hit-testing/)
- **iOS Devs**: [ios/ios-quick-reference.md](./ios/ios-quick-reference.md) → [ios/ios-arkit-hit-testing-report.md](./ios/ios-arkit-hit-testing-report.md)
- **Testers**: [guides/test-implementation-guide.md](./guides/test-implementation-guide.md) → [reports/test-coverage-report.md](./reports/test-coverage-report.md)
- **Reviewers**: [guides/code-review-checklist.md](./guides/code-review-checklist.md)
