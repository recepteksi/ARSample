# iOS CI Workflow - Quick Reference

## 🚀 Quick Start

### Local Testing (Before Push)
```bash
# 1. Build KMP framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# 2. Build iOS app
cd iosApp
xcodebuild clean build \
  -project iosApp.xcodeproj \
  -scheme iosApp \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO

# 3. Run SwiftLint (optional)
brew install swiftlint  # One-time setup
swiftlint lint
```

### Commit and Push
```bash
git add .github/workflows/ iosApp/.swiftlint.yml
git commit -m "Add iOS CI workflow with SwiftLint"
git push origin main
```

### Monitor Workflow
1. Go to: `https://github.com/YOUR_USERNAME/ARSample/actions`
2. Click on the latest workflow run
3. Check each step for success/failure

---

## 📋 Workflow Details

| Property | Value |
|----------|-------|
| **File** | `.github/workflows/ios-ci.yml` |
| **Runner** | macOS 14 (Xcode 15+) |
| **Triggers** | Push to main/develop, PRs |
| **Build Time** | 10-15 min (first), 5-8 min (cached) |
| **Cost** | 10x GitHub Actions minutes |

---

## 🔧 Key Build Steps

```yaml
1. Checkout code
2. Setup Xcode (latest-stable)
3. Setup Java 17
4. Cache Gradle
5. Build KMP framework (linkDebugFrameworkIosSimulatorArm64)
6. Build iOS app (xcodebuild)
7. Run tests (continue-on-error)
8. SwiftLint checks (continue-on-error)
9. Upload logs (on failure)
```

---

## ⚙️ Project-Specific Settings

### KMP Framework Task
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```
✅ Verified available (replaces `assembleXCFramework`)

### Xcode Configuration
- **Project**: `iosApp.xcodeproj`
- **Scheme**: `iosApp`
- **SDK**: `iphonesimulator`
- **Destination**: `iPhone 15, OS=latest`
- **Architecture**: ARM64 (M-series Macs)
- **Code Signing**: Disabled (`CODE_SIGNING_REQUIRED=NO`)

### No CocoaPods
❌ Project doesn't use Podfile → Pod install steps removed

---

## 🐛 Troubleshooting

### Workflow Fails: Framework Build
```bash
# Check available tasks
./gradlew tasks --all | grep -i framework

# Try alternative
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

### Workflow Fails: Scheme Not Found
```bash
# List schemes
xcodebuild -project iosApp/iosApp.xcodeproj -list

# Update workflow with correct scheme name
```

### Workflow Fails: Simulator Not Available
```yaml
# Update destination in ios-ci.yml
-destination 'platform=iOS Simulator,name=iPhone 14,OS=17.0'
```

### SwiftLint Errors Locally
```bash
# Check issues
cd iosApp
swiftlint lint

# Auto-fix
swiftlint lint --fix

# Disable specific rules in .swiftlint.yml
disabled_rules:
  - rule_name
```

---

## 💰 Cost Optimization

### Option 1: Main Branch Only
```yaml
on:
  push:
    branches: [ main ]  # Remove develop
```

### Option 2: Path-Based Triggers
```yaml
on:
  push:
    paths:
      - 'iosApp/**'
      - 'composeApp/**'
      - '.github/workflows/ios-ci.yml'
```

### Option 3: Combine Both
```yaml
on:
  push:
    branches: [ main ]
    paths:
      - 'iosApp/**'
      - 'composeApp/**'
```

---

## 📱 SwiftLint Configuration

### Key Rules (`iosApp/.swiftlint.yml`)
- **Line length**: 120 chars (warning), 200 chars (error)
- **Function length**: 60 lines (warning), 100 lines (error)
- **File length**: 500 lines (warning), 1000 lines (error)

### Custom Rules
- ⚠️ Warns on `print()` statements
- ✅ Enforces `isEmpty` over `count == 0`
- ✅ Enforces `isEmpty` over `== ""`

### Disable Linting Locally
```swift
// swiftlint:disable rule_name
// Your code here
// swiftlint:enable rule_name
```

---

## 🎯 README Badge

```markdown
[![iOS CI](https://github.com/YOUR_USERNAME/ARSample/actions/workflows/ios-ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/ARSample/actions/workflows/ios-ci.yml)
```

**Replace** `YOUR_USERNAME` with your GitHub username.

---

## 📊 Build Time Breakdown

### First Run (No Cache)
- Gradle dependencies: ~3-5 min
- KMP framework: ~2-3 min
- iOS build: ~3-5 min
- SwiftLint: ~1 min
- **Total**: ~10-15 min

### Cached Runs
- Gradle (cached): ~30 sec
- KMP framework: ~1-2 min
- iOS build: ~2-3 min
- SwiftLint: ~30 sec
- **Total**: ~5-8 min

---

## 📚 Files Created

```
.github/workflows/
├── ios-ci.yml                    # Main workflow file
├── IOS_WORKFLOW_README.md        # Detailed documentation
├── IMPLEMENTATION_SUMMARY.md     # Implementation report
└── QUICK_REFERENCE.md            # This file

iosApp/
└── .swiftlint.yml                # SwiftLint configuration
```

---

## ✅ Verification Checklist

After pushing:
- [ ] Workflow appears in Actions tab
- [ ] Workflow triggered successfully
- [ ] Java 17 installed correctly
- [ ] Gradle cache working
- [ ] KMP framework builds
- [ ] iOS app builds
- [ ] SwiftLint runs
- [ ] Build time < 15 min (first run)
- [ ] Badge displays in README

---

## 🔗 Useful Links

- [GitHub Actions](https://github.com/YOUR_USERNAME/ARSample/actions)
- [Workflow File](.github/workflows/ios-ci.yml)
- [Full Documentation](.github/workflows/IOS_WORKFLOW_README.md)
- [Implementation Report](.github/workflows/IMPLEMENTATION_SUMMARY.md)

---

**Last Updated**: 2024-04-04  
**Status**: Ready to deploy (after fixing compilation errors)
