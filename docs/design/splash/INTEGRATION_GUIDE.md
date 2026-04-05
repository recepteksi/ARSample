# Splash Screen Quick Integration Guide

**Quick reference for android-expert-agent and ios-expert-agent**

---

## Android Implementation (5 Steps)

### Step 1: Add Dependency
```gradle
// composeApp/build.gradle.kts
dependencies {
    implementation("androidx.core:core-splashscreen:1.0.1")
}
```

### Step 2: Create Color Resources
```xml
<!-- res/values/colors.xml -->
<color name="splash_background">#FFFFFF</color>
<color name="splash_icon_background">#6200EE</color>

<!-- res/values-night/colors.xml -->
<color name="splash_background">#121212</color>
<color name="splash_icon_background">#BB86FC</color>
```

### Step 3: Create Splash Theme
```xml
<!-- res/values/themes.xml -->
<style name="Theme.ARSample.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_background</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="windowSplashScreenIconBackgroundColor">@color/splash_icon_background</item>
    <item name="windowSplashScreenAnimationDuration">300</item>
    <item name="postSplashScreenTheme">@style/Theme.ARSample</item>
</style>
```

### Step 4: Update AndroidManifest.xml
```xml
<application
    android:theme="@style/Theme.ARSample.Starting">
```

### Step 5: Add to MainActivity
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()  // BEFORE super.onCreate()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

---

## iOS Implementation (3 Steps)

### Step 1: Create LaunchScreen.storyboard
Use Xcode Interface Builder or XML:
- Add centered ImageView with AppIcon
- Set background to systemBackgroundColor
- Configure Auto Layout constraints (centerX, centerY)

### Step 2: Add Dark Mode Color Asset
1. Open Assets.xcassets
2. Create "LaunchBackground" Color Set
3. Configure:
   - Light: #FFFFFF
   - Dark: #121212

### Step 3: Update Info.plist
```xml
<key>UILaunchScreen</key>
<dict>
    <key>UIImageName</key>
    <string>AppIcon</string>
    <key>UIColorName</key>
    <string>LaunchBackground</string>
</dict>
```

---

## Testing Commands

### Android
```bash
# Cold start test
adb shell am force-stop com.trendhive.arsample
adb shell am start -n com.trendhive.arsample/.MainActivity

# Toggle dark mode
adb shell "cmd uimode night yes"
adb shell "cmd uimode night no"

# Check startup time
adb shell am start -W com.trendhive.arsample/.MainActivity
```

### iOS
```bash
# Toggle appearance
xcrun simctl ui booted appearance dark
xcrun simctl ui booted appearance light
```

---

## Validation Checklist

**Android:**
- [ ] Dependency added to build.gradle
- [ ] Theme created in themes.xml (light + dark)
- [ ] Colors defined in colors.xml (light + dark)
- [ ] AndroidManifest.xml updated with splash theme
- [ ] installSplashScreen() called in MainActivity
- [ ] App launches without crash
- [ ] Dark mode toggle works
- [ ] Splash dismisses within 1 second

**iOS:**
- [ ] LaunchScreen.storyboard created
- [ ] AppIcon centered in storyboard
- [ ] Dark mode color asset added
- [ ] Info.plist configured
- [ ] App launches on simulator
- [ ] Dark mode toggle works
- [ ] Layout correct on iPhone + iPad

---

## Common Issues

| Issue | Solution |
|-------|----------|
| **Android: Splash not showing** | Check manifest theme is applied to `<application>` or launcher `<activity>` |
| **Android: Theme crash** | Ensure `postSplashScreenTheme` points to existing theme |
| **Android: Icon too large** | Verify icon fits within safe circle (see design spec 2.2) |
| **iOS: Blank screen** | Check LaunchScreen.storyboard is set in Info.plist |
| **iOS: Wrong colors** | Verify systemBackgroundColor used for auto dark mode |
| **Dark mode not working** | Ensure `-night` variants exist (Android) or Appearances configured (iOS) |

---

**Full Specification:** See `SPLASH_SCREEN_DESIGN_SPEC.md`
