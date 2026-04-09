# iOS CI Workflow Documentation

## Overview
This GitHub Actions workflow builds and tests the iOS app on every push to `main`/`develop` branches and pull requests.

## Workflow File
`.github/workflows/ios-ci.yml`

## Triggers
- **Push** to `main` or `develop` branches
- **Pull Requests** targeting `main` or `develop` branches

## Workflow Steps

### 1. Environment Setup
- **Runner**: macOS 14 (Xcode 15+)
- **Xcode**: Latest stable version
- **Java**: Temurin JDK 17 (for Kotlin Multiplatform)

### 2. Caching
- Gradle dependencies (KMP framework builds)
- Reduces build time on subsequent runs

### 3. Build Process
1. **KMP Framework**: Builds the shared Kotlin framework for iOS Simulator ARM64
   ```bash
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```

2. **iOS App**: Builds the iOS app using xcodebuild
   ```bash
   xcodebuild clean build \
     -project iosApp.xcodeproj \
     -scheme iosApp \
     -sdk iphonesimulator \
     -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest'
   ```

3. **Tests**: Runs iOS unit tests (if available)
   - `continue-on-error: true` - doesn't fail build if no tests exist

4. **SwiftLint**: Checks Swift code quality
   - `continue-on-error: true` - doesn't fail build on warnings initially
   - Uses `.swiftlint.yml` configuration

### 4. Artifacts
- Uploads build logs on failure for debugging
- Retention: 7 days

## GitHub Actions Badge

Add this badge to your README.md:

```markdown
[![iOS CI](https://github.com/YOUR_USERNAME/ARSample/actions/workflows/ios-ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/ARSample/actions/workflows/ios-ci.yml)
```

Replace `YOUR_USERNAME` with your GitHub username/organization.

## SwiftLint Configuration

Location: `iosApp/.swiftlint.yml`

**Key Rules:**
- Line length: 120 chars (warning), 200 chars (error)
- Function body: 60 lines (warning), 100 lines (error)
- File length: 500 lines (warning), 1000 lines (error)
- Enforces `isEmpty` over `count == 0`
- Warns on force unwrapping (`!`)
- Custom rule: Warns on `print()` statements

## Project-Specific Adjustments

### ✅ Made for ARSample:
1. **No CocoaPods**: Project doesn't use Podfile, so pod install step is skipped
2. **KMP Task**: Uses `linkDebugFrameworkIosSimulatorArm64` (confirmed available)
3. **Scheme**: Uses `iosApp` scheme (confirmed in Xcode project)
4. **Simulator Target**: iPhone 15 with latest iOS
5. **Code Signing**: Disabled for CI builds (no certificates needed)

### 📝 Adjustments from Template:
- Removed `assembleXCFramework` task (not available in this KMP setup)
- Removed CocoaPods installation (no Podfile)
- Changed to simulator ARM64 framework build (for M-series macOS runners)
- Set `continue-on-error: true` for tests and SwiftLint initially

## Estimated Build Times

### First Run (No Cache)
- **Total**: 10-15 minutes
  - Gradle dependencies download: ~3-5 min
  - KMP framework build: ~2-3 min
  - iOS build: ~3-5 min
  - SwiftLint: ~1 min

### Cached Runs
- **Total**: 5-8 minutes
  - Gradle (cached): ~30 sec
  - KMP framework: ~1-2 min
  - iOS build: ~2-3 min
  - SwiftLint: ~30 sec

## Cost Considerations

⚠️ **macOS runners use 10x minutes** compared to Linux runners:
- 1 minute on macOS = 10 GitHub Actions minutes
- Free tier: 2,000 minutes/month (= 200 macOS minutes)

**Optimization Tips:**
1. Run only on `main` branch if budget is tight:
   ```yaml
   on:
     push:
       branches: [ main ]
   ```

2. Skip iOS build on non-iOS changes:
   ```yaml
   on:
     push:
       paths:
         - 'iosApp/**'
         - 'composeApp/**'
         - '.github/workflows/ios-ci.yml'
   ```

## Troubleshooting

### Build Fails on Framework Step
```bash
# Check available tasks locally
./gradlew tasks --all | grep -i framework

# Try alternative task
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

### Scheme Not Found
```bash
# List available schemes
xcodebuild -project iosApp/iosApp.xcodeproj -list
```

### SwiftLint Errors
```bash
# Run locally
cd iosApp
swiftlint lint

# Auto-fix issues
swiftlint lint --fix
```

### Simulator Not Available
Update destination in workflow:
```yaml
-destination 'platform=iOS Simulator,name=iPhone 14,OS=17.0'
```

## Testing Locally

### Option 1: Using `act` (GitHub Actions locally)
```bash
# Install act
brew install act

# Run workflow
act -j build-ios
```

**Note**: Requires Docker and significant resources. May not work perfectly for macOS workflows.

### Option 2: Manual Steps
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

# 3. Run SwiftLint
brew install swiftlint
swiftlint lint
```

## Verification Checklist

After pushing the workflow:

- [ ] Workflow file appears in GitHub UI (Actions tab)
- [ ] Workflow triggered on push/PR
- [ ] Java setup succeeds
- [ ] Gradle cache works
- [ ] KMP framework builds successfully
- [ ] iOS app builds without errors
- [ ] SwiftLint runs (warnings OK for now)
- [ ] Build time is reasonable (<15 min first run)
- [ ] Badge displays in README

## Next Steps

### 1. Enable Stricter Linting (Optional)
Remove `continue-on-error: true` from SwiftLint step:
```yaml
- name: Run SwiftLint
  working-directory: iosApp
  run: swiftlint lint --strict
```

### 2. Add Test Coverage (When Tests Exist)
```yaml
- name: Run iOS tests with coverage
  run: |
    xcodebuild test \
      -project iosApp.xcodeproj \
      -scheme iosApp \
      -sdk iphonesimulator \
      -enableCodeCoverage YES \
      -derivedDataPath DerivedData
      
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
```

### 3. Release Builds (Future)
Add a separate workflow for App Store releases:
- Build release configuration
- Sign with distribution certificate
- Upload to TestFlight
- Archive for App Store submission

## Files Created

1. `.github/workflows/ios-ci.yml` - Main workflow file
2. `iosApp/.swiftlint.yml` - SwiftLint configuration
3. `.github/workflows/IOS_WORKFLOW_README.md` - This documentation

## Resources

- [GitHub Actions - macOS runners](https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources)
- [Xcode Build Settings](https://help.apple.com/xcode/mac/current/#/itcaec37c2a6)
- [SwiftLint Rules](https://realm.github.io/SwiftLint/rule-directory.html)
- [Kotlin Multiplatform - iOS Integration](https://kotlinlang.org/docs/multiplatform-mobile-integrate-in-existing-app.html)
