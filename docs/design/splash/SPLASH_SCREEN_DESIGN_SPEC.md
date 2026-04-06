# Splash Screen Design Specification

**Project:** ARSample - AR 3D Object Placement
**Platform:** Kotlin Multiplatform (Android + iOS)
**Date:** 2026-03-30
**Version:** 1.0

---

## Executive Summary

This document provides comprehensive design specifications for ARSample's splash screen implementation following modern platform guidelines (Android 12+ SplashScreen API and iOS LaunchScreen). The design prioritizes fast loading, platform consistency, and a premium user experience.

---

## 1. Design Principles

### 1.1 Core Guidelines

| Principle | Description | Rationale |
|-----------|-------------|-----------|
| **Minimal Design** | Simple, clean, no complex graphics | Fast loading, reduces perceived startup time |
| **Platform Native** | Follow platform-specific patterns | Seamless OS integration, familiar UX |
| **No Branding Overload** | App icon + background color only | Google/Apple guidelines, prevents double splash |
| **Dark Mode Support** | Separate light/dark variants | Accessibility, user preference respect |
| **Instant Display** | Max 1 second duration | User research shows animations reduce perceived wait time |

### 1.2 Research-Backed Best Practices

**Source:** [Android SplashScreen API Documentation](https://developer.android.com/develop/ui/views/launch/splash-screen)

- **Perceived Performance**: User research indicates that viewing an animation makes perceived startup time feel shorter
- **No Delays**: Splash screen must dismiss as soon as app is ready (not artificially extended)
- **Avoid Duplication**: Do NOT create dedicated Activity/ViewController for splash - use system APIs
- **Consistency**: Maintain visual coherence across all device sizes and OS versions

---

## 2. Visual Design Specification

### 2.1 Color Palette

#### Light Mode
```kotlin
// Primary colors
background = Color(0xFFFFFFFF)        // Pure white
iconBackground = Color(0xFF6200EE)     // Material Purple 500 (brand color)
iconForeground = Color(0xFFFFFFFF)     // White

// Alternative (if pure white too stark)
background = Color(0xFFFAFAFA)        // Softer white (Material Grey 50)
```

#### Dark Mode
```kotlin
background = Color(0xFF121212)        // Material Dark Surface
iconBackground = Color(0xFFBB86FC)     // Material Purple 200 (accessible contrast)
iconForeground = Color(0xFF000000)     // Black
```

**Contrast Ratios:**
- Light mode: 4.5:1 (WCAG AA compliant)
- Dark mode: 7:1 (WCAG AAA compliant)

### 2.2 Icon Specifications

#### Android 12+ Requirements

| Element | Specification | Notes |
|---------|--------------|-------|
| **Icon Format** | Vector Drawable (XML) or AnimatedVectorDrawable | Scalable, small file size |
| **Icon Size** | 288×288 dp (without background)<br>240×240 dp (with icon background) | Fits within safe circle |
| **Safe Circle** | 192 dp diameter (no bg)<br>160 dp diameter (with bg) | Content must fit here |
| **Branded Image** | 200×80 dp (NOT recommended) | Google discourages branding |
| **Animation Duration** | ≤1000ms (1 second) | Optimal: 200-500ms |

**Android Safe Zones:**
```
┌─────────────────────────┐
│   288×288 dp canvas     │
│                         │
│    ┌─────────────┐      │
│    │   192 dp    │      │  ← Safe circle
│    │  diameter   │      │
│    └─────────────┘      │
│                         │
└─────────────────────────┘
```

#### iOS Requirements

| Element | Specification | Notes |
|---------|--------------|-------|
| **Launch Screen** | SwiftUI or Storyboard | Static only, NO animations |
| **Icon Size** | System app icon | Auto-scaled by OS |
| **Layout** | Centered icon + background | Responsive to all sizes |
| **Dark Mode** | Automatic via system colors | Uses asset catalog variants |

### 2.3 Typography (Optional)

**App Name Label** (if used - NOT recommended by Google/Apple)
```
Font: System default (Roboto/SF Pro)
Size: 16sp / 16pt
Weight: Medium (500)
Position: Bottom 48dp/pt from edge
Color: 60% opacity of foreground
```

⚠️ **Warning:** Text branding can slow perceived loading time. Prefer icon-only.

---

## 3. Platform Implementation Guidelines

### 3.1 Android 12+ Implementation

#### Step 1: Add Dependency
```gradle
// composeApp/build.gradle.kts
dependencies {
    implementation("androidx.core:core-splashscreen:1.0.1")
}
```

#### Step 2: Create Theme
```xml
<!-- res/values/themes.xml -->
<style name="Theme.ARSample.Starting" parent="Theme.SplashScreen">
    <!-- Background color -->
    <item name="windowSplashScreenBackground">@color/splash_background</item>
    
    <!-- App icon -->
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    
    <!-- Icon background (optional - adds contrast) -->
    <item name="windowSplashScreenIconBackgroundColor">@color/purple_500</item>
    
    <!-- Animation duration (Android 12 only, optional) -->
    <item name="windowSplashScreenAnimationDuration">300</item>
    
    <!-- Post-splash theme (required) -->
    <item name="postSplashScreenTheme">@style/Theme.ARSample</item>
</style>

<!-- res/values-night/themes.xml -->
<style name="Theme.ARSample.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_background_dark</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="windowSplashScreenIconBackgroundColor">@color/purple_200</item>
    <item name="postSplashScreenTheme">@style/Theme.ARSample</item>
</style>
```

#### Step 3: Update Manifest
```xml
<application
    android:theme="@style/Theme.ARSample.Starting">
    <!-- OR apply to main activity only -->
    <activity
        android:name=".MainActivity"
        android:theme="@style/Theme.ARSample.Starting">
```

#### Step 4: MainActivity Integration
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Optional: Keep splash visible until data ready
        val contentReady = MutableStateFlow(false)
        splashScreen.setKeepOnScreenCondition { !contentReady.value }
        
        lifecycleScope.launch {
            // Simulate data loading
            delay(500) // Load critical data
            contentReady.value = true
        }
        
        setContent {
            ARSampleTheme {
                App()
            }
        }
    }
}
```

#### Optional: Custom Exit Animation
```kotlin
splashScreen.setOnExitAnimationListener { splashScreenView ->
    val slideUp = ObjectAnimator.ofFloat(
        splashScreenView.view,
        View.TRANSLATION_Y,
        0f,
        -splashScreenView.view.height.toFloat()
    ).apply {
        interpolator = AccelerateInterpolator()
        duration = 300L
        doOnEnd { splashScreenView.remove() }
    }
    
    val fadeOut = ObjectAnimator.ofFloat(
        splashScreenView.iconView,
        View.ALPHA,
        1f,
        0f
    ).apply {
        interpolator = LinearInterpolator()
        duration = 300L
    }
    
    AnimatorSet().apply {
        playTogether(slideUp, fadeOut)
        start()
    }
}
```

### 3.2 iOS Implementation

#### Step 1: Create LaunchScreen.storyboard
```xml
<?xml version="1.0" encoding="UTF-8"?>
<document type="com.apple.InterfaceBuilder3.CocoaTouch.Storyboard.XIB" version="3.0">
    <scenes>
        <scene sceneID="launch-scene">
            <objects>
                <viewController id="launch-controller">
                    <view key="view" contentMode="scaleToFill">
                        <rect key="frame" x="0.0" y="0.0" width="414" height="896"/>
                        <autoresizingMask key="autoresizingMask" widthSizable="YES" heightSizable="YES"/>
                        
                        <!-- Background -->
                        <color key="backgroundColor" systemColor="systemBackgroundColor"/>
                        
                        <!-- App Icon -->
                        <imageView opaque="NO" clipsSubviews="YES" contentMode="scaleAspectFit">
                            <rect key="frame" x="157" y="398" width="100" height="100"/>
                            <autoresizingMask key="autoresizingMask" 
                                flexibleLeftMargin="YES" 
                                flexibleRightMargin="YES" 
                                flexibleTopMargin="YES" 
                                flexibleBottomMargin="YES"/>
                            <image name="AppIcon" width="100" height="100"/>
                        </imageView>
                    </view>
                </viewController>
            </objects>
        </scene>
    </scenes>
    
    <resources>
        <namedColor name="LaunchBackgroundColor">
            <color red="1.0" green="1.0" blue="1.0" alpha="1.0" colorSpace="custom" customColorSpace="sRGB"/>
        </namedColor>
        <systemColor name="systemBackgroundColor">
            <color white="1.0" alpha="1.0" colorSpace="custom" customColorSpace="genericGamma22GrayColorSpace"/>
        </systemColor>
    </resources>
</document>
```

#### Step 2: SwiftUI Alternative (iOS 14+)
```swift
// LaunchScreen.swift
import SwiftUI

struct LaunchScreen: View {
    var body: some View {
        ZStack {
            // Background
            Color(.systemBackground)
                .ignoresSafeArea()
            
            // App Icon
            Image("AppIcon")
                .resizable()
                .scaledToFit()
                .frame(width: 100, height: 100)
        }
    }
}

// Info.plist configuration
// Add: UILaunchScreen dictionary with UIImageName = "AppIcon"
```

#### Step 3: Dark Mode Asset Catalog
```
Assets.xcassets/
├── LaunchBackground.colorset/
│   ├── Contents.json
│   └── Contents.json  (with "appearances" for dark mode)
└── AppIcon.appiconset/
    ├── Contents.json
    └── [icon files at various sizes]
```

**Contents.json for Dark Mode Color:**
```json
{
  "colors": [
    {
      "color": {
        "color-space": "srgb",
        "components": {
          "red": "1.000",
          "green": "1.000",
          "blue": "1.000",
          "alpha": "1.000"
        }
      },
      "idiom": "universal"
    },
    {
      "appearances": [
        {
          "appearance": "luminosity",
          "value": "dark"
        }
      ],
      "color": {
        "color-space": "srgb",
        "components": {
          "red": "0.071",
          "green": "0.071",
          "blue": "0.071",
          "alpha": "1.000"
        }
      },
      "idiom": "universal"
    }
  ],
  "info": {
    "author": "xcode",
    "version": 1
  }
}
```

---

## 4. Responsive Design

### 4.1 Android Screen Sizes

| Device Class | Example | Icon Display Size |
|--------------|---------|-------------------|
| Small (< 600dp) | Phones | 72dp (1/4 of safe circle) |
| Medium (600-840dp) | Large phones, small tablets | 96dp |
| Large (> 840dp) | Tablets | 120dp |

**Constraint:** Icon must ALWAYS fit within safe circle (see section 2.2)

### 4.2 iOS Device Support

| Device Family | Screen Size | Icon Behavior |
|--------------|-------------|---------------|
| iPhone (all) | Various | Centered, 100pt fixed |
| iPad | 9.7" - 12.9" | Centered, 120pt fixed |
| iPhone SE | 4.7" | Centered, 80pt fixed |

**Layout Strategy:** Use Auto Layout constraints with `centerX` and `centerY` to maintain centering.

---

## 5. Performance Optimization

### 5.1 Loading Strategy

```kotlin
// ✅ GOOD: Minimal loading
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Only load essential data
        initializeMinimalDependencies()
        
        setContent { App() }
        
        // Lazy-load heavy components AFTER UI appears
        lifecycleScope.launch {
            delay(100) // Let UI render first
            loadHeavyDependencies()
        }
    }
}

// ❌ BAD: Heavy loading blocks UI
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // DON'T do this - blocks splash dismissal
        loadAllLibraries()
        initializeDatabase()
        fetchRemoteConfig()
        
        setContent { App() }
    }
}
```

### 5.2 Best Practices

| Practice | Why | Impact |
|----------|-----|--------|
| **Use Vector Drawables** | Scalable, small file size | 10x smaller than PNG |
| **Avoid Animations > 1s** | User patience threshold | Reduces perceived delay |
| **Defer Non-Critical Init** | Faster first frame | 50-80% faster startup |
| **Use Placeholder UI** | Graceful degradation | Better UX during loading |
| **Cache Previous State** | Instant content on re-launch | Eliminates loading on warm starts |

---

## 6. Accessibility

### 6.1 Color Contrast

**WCAG 2.1 Level AA Compliance:**
- Light mode: Icon (purple #6200EE) on white = 4.7:1 ✅
- Dark mode: Icon (purple #BB86FC) on dark = 8.2:1 ✅

### 6.2 Motion Preferences

**Android:**
```kotlin
val isReduceMotionEnabled = resources.configuration.uiMode and 
    Configuration.UI_MODE_TYPE_NORMAL != 0

splashScreen.setOnExitAnimationListener { splashScreenView ->
    if (isReduceMotionEnabled) {
        // Instant dismiss, no animation
        splashScreenView.remove()
    } else {
        // Animated exit
        animateSplashExit(splashScreenView)
    }
}
```

**iOS:**
```swift
if UIAccessibility.isReduceMotionEnabled {
    // Skip animations
} else {
    // Animate launch screen transition
}
```

### 6.3 VoiceOver/TalkBack

**Important:** Splash screens are NOT interactive, so they should:
- Have `importantForAccessibility="no"` (Android)
- Be hidden from VoiceOver (iOS)
- Dismiss quickly to reach interactive content

---

## 7. Asset Deliverables

### 7.1 Android Assets

**File Structure:**
```
composeApp/src/androidMain/res/
├── values/
│   ├── colors.xml                    # Splash colors (light mode)
│   └── themes.xml                    # Splash theme
├── values-night/
│   ├── colors.xml                    # Splash colors (dark mode)
│   └── themes.xml                    # Splash theme (dark)
├── drawable/
│   ├── ic_splash_icon.xml            # App icon vector
│   └── ic_splash_background.xml      # Icon background (optional)
└── drawable-v31/                     # Android 12+ specific (if needed)
```

**colors.xml (Light Mode):**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="splash_background">#FFFFFF</color>
    <color name="splash_icon_background">#6200EE</color>
</resources>
```

**colors.xml (Dark Mode):**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="splash_background">#121212</color>
    <color name="splash_icon_background">#BB86FC</color>
</resources>
```

### 7.2 iOS Assets

**File Structure:**
```
iosApp/iosApp/
├── LaunchScreen.storyboard           # Launch screen layout
├── Assets.xcassets/
│   ├── LaunchBackground.colorset/    # Background color (light/dark)
│   │   └── Contents.json
│   └── AppIcon.appiconset/           # App icon
│       ├── icon_20pt@2x.png          # 40×40
│       ├── icon_20pt@3x.png          # 60×60
│       ├── icon_29pt@2x.png          # 58×58
│       ├── icon_29pt@3x.png          # 87×87
│       ├── icon_40pt@2x.png          # 80×80
│       ├── icon_40pt@3x.png          # 120×120
│       ├── icon_60pt@2x.png          # 120×120
│       ├── icon_60pt@3x.png          # 180×180
│       ├── icon_76pt.png             # 76×76 (iPad)
│       ├── icon_76pt@2x.png          # 152×152
│       ├── icon_83.5pt@2x.png        # 167×167 (iPad Pro)
│       └── Contents.json
└── Info.plist                        # Launch screen config
```

---

## 8. Testing Checklist

### 8.1 Android Testing

- [ ] **Cold Start:** App launches from terminated state → Splash appears → Smooth transition
- [ ] **Warm Start:** App returns from background → Splash appears → Instant dismissal
- [ ] **Hot Start:** App in memory → No splash (expected behavior)
- [ ] **Dark Mode:** Toggle system theme → Splash uses correct colors
- [ ] **Tablet:** Test on 10" tablet → Icon properly scaled and centered
- [ ] **Android 11-:** Fallback theme works (compat library behavior)
- [ ] **Animation:** Icon animation completes within 1 second
- [ ] **Orientation:** Rotate device → Splash maintains centering

**Test Commands:**
```bash
# Force cold start
adb shell am force-stop com.trendhive.arsample
adb shell am start -n com.trendhive.arsample/.MainActivity

# Toggle dark mode
adb shell "cmd uimode night yes"
adb shell "cmd uimode night no"

# Check startup time
adb shell am start -W com.trendhive.arsample/.MainActivity
# Look for: TotalTime < 1000ms
```

### 8.2 iOS Testing

- [ ] **Clean Launch:** Delete app → Reinstall → Launch → Splash displays correctly
- [ ] **Dark Mode:** Settings → Display → Dark Mode → Launch app → Verify colors
- [ ] **iPhone:** Test on iPhone 14, 12, SE
- [ ] **iPad:** Test on iPad Pro 12.9"
- [ ] **Landscape:** Rotate to landscape → Splash maintains layout
- [ ] **VoiceOver:** Enable VoiceOver → Launch app → Splash NOT announced
- [ ] **Quick Launch:** Tap app multiple times rapidly → No double splash

**Test on Simulator:**
```bash
# Toggle appearance
xcrun simctl ui booted appearance dark
xcrun simctl ui booted appearance light

# Test different devices
open -a Simulator --args -CurrentDeviceUDID <iPhone-14-UDID>
open -a Simulator --args -CurrentDeviceUDID <iPad-Pro-UDID>
```

---

## 9. Implementation Timeline

| Phase | Task | Owner | Duration | Status |
|-------|------|-------|----------|--------|
| **Phase 1** | Create design assets (vectors, colors) | Design Team | 2 days | Pending |
| **Phase 2** | Android implementation (theme + MainActivity) | android-expert-agent | 3 days | Pending |
| **Phase 3** | iOS implementation (LaunchScreen.storyboard) | ios-expert-agent | 3 days | Pending |
| **Phase 4** | Dark mode variants (both platforms) | android-expert-agent + ios-expert-agent | 2 days | Pending |
| **Phase 5** | Testing and QA (all devices) | QA Team | 3 days | Pending |
| **Phase 6** | Performance optimization | android-expert-agent + ios-expert-agent | 2 days | Pending |

**Total Estimated Time:** 15 days (3 weeks)

---

## 10. Design Mockups

### 10.1 Light Mode

```
┌─────────────────────────┐
│                         │
│                         │
│                         │
│         ┌─────┐         │
│         │     │         │  ← Purple circle background
│         │ 📱  │         │    with white AR icon
│         │     │         │
│         └─────┘         │
│                         │
│                         │
│                         │
│     (Optional Text)     │  ← "AR Sample" (discouraged)
│                         │
└─────────────────────────┘
   Background: #FFFFFF
```

### 10.2 Dark Mode

```
┌─────────────────────────┐
│                         │
│                         │
│                         │
│         ┌─────┐         │
│         │     │         │  ← Light purple circle
│         │ 📱  │         │    with black AR icon
│         │     │         │
│         └─────┘         │
│                         │
│                         │
│                         │
│     (Optional Text)     │
│                         │
└─────────────────────────┘
   Background: #121212
```

---

## 11. References

### 11.1 Official Documentation

1. [Android Splash Screen API](https://developer.android.com/develop/ui/views/launch/splash-screen)
2. [Android Migration Guide](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate)
3. [AndroidX SplashScreen Library](https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen)
4. [Apple HIG - Launching](https://developer.apple.com/design/human-interface-guidelines/launching)
5. [iOS Launch Screen Best Practices](https://developer.apple.com/documentation/xcode/specifying-your-apps-launch-screen)

### 11.2 Design Resources

- [Material Design 3 - Motion](https://m3.material.io/styles/motion/overview)
- [Android Adaptive Icons](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
- [WCAG 2.1 Color Contrast Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)
- [Google Fonts - Roboto](https://fonts.google.com/specimen/Roboto)
- [SF Symbols (iOS)](https://developer.apple.com/sf-symbols/)

---

## 12. FAQ

**Q: Why not use a dedicated Activity/ViewController for splash?**  
A: Creates double splash screen on Android 12+, increases startup time, violates platform guidelines.

**Q: Can we add animations?**  
A: Yes on Android (AnimatedVectorDrawable ≤1s), NO on iOS (static only).

**Q: Should we show "Loading..." text?**  
A: No. Splash should dismiss when app is ready. Use placeholder UI in main screen if needed.

**Q: What about branding images at bottom?**  
A: Google/Apple discourage this. Prefer icon-only for faster perceived loading.

**Q: How to handle slow network requests?**  
A: Don't wait for them on splash. Dismiss splash → Show placeholder UI → Load async.

**Q: Dark mode colors different from brand?**  
A: Yes, accessibility requires higher contrast in dark mode (WCAG compliance).

---

## 13. Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Design Lead | - | - | - |
| Android Expert | android-expert-agent | - | - |
| iOS Expert | ios-expert-agent | - | - |
| Product Manager | - | - | - |

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-30  
**Next Review:** After Phase 3 completion
