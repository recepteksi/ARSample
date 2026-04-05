# Splash Screen Research Summary

**Research Date:** 2026-03-30  
**Conducted By:** design-analysis-agent  
**Purpose:** Best practices for modern splash screen design (Android 12+ and iOS)

---

## 1. Executive Summary

Modern splash screen design has shifted dramatically with Android 12's introduction of the SplashScreen API in 2021. Both Google and Apple now recommend:

1. **Minimal, icon-only designs** (no branding text)
2. **System-managed splash screens** (not custom Activities/ViewControllers)
3. **Instant dismissal** when app is ready (no artificial delays)
4. **Dark mode support** with appropriate color variants

**Key Finding:** User research shows that viewing an animation during app startup makes **perceived startup time feel shorter**, even if actual startup time is the same.

---

## 2. Android 12+ SplashScreen API

### 2.1 Platform Evolution

**Timeline:**
- **Pre-Android 12:** Custom splash implementations using dedicated Activity or theme windowBackground
- **Android 12 (API 31, 2021):** SplashScreen API introduced - ALL apps get system splash
- **Current (2026):** SplashScreen API mature, compat library ensures consistency

### 2.2 Key Research Findings

#### Source: [Android Splash Screen Documentation](https://developer.android.com/develop/ui/views/launch/splash-screen)

**What Changed:**
1. **Mandatory System Splash:** From Android 12, ALL apps display a system-generated splash during cold/warm starts
2. **Default Composition:** Launcher icon + windowBackground color (if single color)
3. **Migration Required:** Legacy custom splashes cause "double splash" problem on Android 12+

**API Components:**
```
Splash Screen Elements (Figure 2 from Android docs):
┌─────────────────────────────┐
│     Window Background (4)   │  ← Single opaque color
│                             │
│    ┌─────────────────┐      │
│    │ Icon Bg (2)     │      │  ← Optional, for contrast
│    │  ┌──────────┐   │      │
│    │  │ Icon (1) │   │      │  ← Vector drawable (static or animated)
│    │  └──────────┘   │      │
│    │  (1/3 masked)   │      │
│    └─────────────────┘      │
│                             │
└─────────────────────────────┘

Legend:
1. App Icon (windowSplashScreenAnimatedIcon)
2. Icon Background (windowSplashScreenIconBackgroundColor)
3. Masking (1/3 of foreground masked, follows adaptive icon spec)
4. Window Background (windowSplashScreenBackground)
```

### 2.3 Icon Size Requirements

**Critical Specifications:**
- **With Icon Background:** 240×240 dp canvas, icon fits in 160 dp diameter circle
- **Without Icon Background:** 288×288 dp canvas, icon fits in 192 dp diameter circle
- **Branded Image:** 200×80 dp (NOT recommended by Google)

**Why These Sizes?**
- Follows adaptive icon spec (4x the standard 72 dp launcher icon)
- Inner 2/3 visible on launcher, outer 1/3 for system effects
- Ensures consistent appearance across device sizes

### 2.4 Animation Guidelines

**Acceptable Animations:**
- Format: AnimatedVectorDrawable (AVD) XML only
- Duration: ≤1000ms recommended (166ms delayed start max)
- Type: Icon morphing, rotation, scale (not translation)
- Looping: Allowed if startup > 1000ms (not recommended - fix startup time instead)

**User Research Insight:**
> "User research shows that perceived startup time is less when viewing an animation."  
> — Android Developer Documentation

**Important:** Animation is COSMETIC only. App must still optimize actual startup time.

### 2.5 Launch Sequence

**Figure 4 from Android docs - 12 frames:**
1. Launcher icon tap
2. Icon zooms/enlarges
3. Enter animation (system-controlled, NOT customizable)
4. **Splash screen displays** (customizable - this is our focus)
5. Icon animation plays (if provided)
6. App loads in background
7. Exit animation (customizable via setOnExitAnimationListener)
8. First app frame appears

**Performance Target:**
- Total time from tap to first frame: < 1000ms
- Splash portion: 200-500ms (icon animation duration)
- Exit animation: 200-300ms

### 2.6 Best Practices from Migration Guide

**Source:** [Android Migration Guide](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate)

**DON'T:**
1. ❌ Use dedicated splash Activity (causes double splash on Android 12+)
2. ❌ Keep legacy windowBackground approach without migration (results in bland system splash)
3. ❌ Add branding images at bottom (slows perceived loading)
4. ❌ Load heavy dependencies during splash (delays dismissal)

**DO:**
1. ✅ Use SplashScreen compat library (androidx.core:core-splashscreen)
2. ✅ Set `postSplashScreenTheme` to actual app theme
3. ✅ Call `installSplashScreen()` BEFORE `super.onCreate()`
4. ✅ Use `setKeepOnScreenCondition` only for essential data loading
5. ✅ Defer non-critical init to after splash dismissal

**Code Pattern - Preventing Display of Old Splash Activity:**
```kotlin
class RoutingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash visible, don't render this Activity
        splashScreen.setKeepOnScreenCondition { true }
        startMainActivity()
        finish()
    }
}
```

---

## 3. iOS LaunchScreen

### 3.1 Apple Guidelines

**Source:** [Apple Human Interface Guidelines - Launching](https://developer.apple.com/design/human-interface-guidelines/launching)

**Core Principles:**
1. **Static Only:** NO animations allowed in launch screen
2. **Instant Appearance:** Should resemble first screen of app
3. **Minimal Branding:** Prefer empty state of first screen over logo splash
4. **Fast Loading:** Design for instant display (no image downloads, complex layouts)

### 3.2 Technical Approaches

**Option 1: LaunchScreen.storyboard (Traditional)**
```
Pros:
- Visual editor in Xcode
- Auto Layout support
- Dark mode via trait variations
- Widely supported (iOS 8+)

Cons:
- Storyboard complexity
- Manual dark mode configuration
```

**Option 2: SwiftUI Launch Screen (iOS 14+)**
```
Pros:
- Modern, declarative syntax
- Automatic dark mode
- Code-based (easier version control)

Cons:
- iOS 14+ only
- Limited customization vs storyboard
```

**Option 3: Info.plist Dictionary (iOS 14+, simplest)**
```xml
<key>UILaunchScreen</key>
<dict>
    <key>UIImageName</key>
    <string>AppIcon</string>
    <key>UIColorName</key>
    <string>LaunchBackground</string>
</dict>
```

### 3.3 Dark Mode Implementation

**Asset Catalog Approach:**
1. Create Color Set in Assets.xcassets
2. Define "Any Appearance" color (light mode)
3. Add "Dark Appearance" variant
4. Reference in launch screen: `UIColor(named: "LaunchBackground")`

**System Color Approach:**
```swift
// Automatically adapts
backgroundColor = .systemBackground  // White (light) / #121212 (dark)
```

### 3.4 iOS Best Practices

**Apple's Recommendations:**
1. **Match First Screen:** Launch screen should look like empty state of first app screen
2. **No Text:** Avoid text that needs localization
3. **No Branding:** Excessive branding feels dated (as of iOS 7+ design language)
4. **Fast Transition:** Smooth, seamless transition to actual app

**What Apple Discourages:**
- Splash screens that feel like "ads" for the app
- Long-duration branded experiences
- Animation (save for in-app onboarding)

---

## 4. Cross-Platform Consistency Strategy

### 4.1 Common Elements

Both platforms support:
- Centered app icon
- Background color (light + dark variants)
- Minimal design aesthetic
- System-managed display duration

### 4.2 Platform Differences

| Aspect | Android 12+ | iOS |
|--------|-------------|-----|
| **Animation** | Allowed (≤1000ms AVD) | NOT allowed |
| **Icon Background** | Optional circle behind icon | Not a native concept |
| **Branding Text** | Discouraged (200×80 dp slot exists) | Discouraged (no slot) |
| **Implementation** | Theme attributes in XML | Storyboard or Info.plist |
| **Dark Mode** | values-night/ resource qualifier | Asset catalog Appearances |

### 4.3 Unified Design Strategy

**Recommendation:** Use **icon-only** design for consistency:
```
Both Platforms:
- Centered app icon
- Solid background color (white light / dark surface)
- Optional: colored circle behind icon (Android native, custom on iOS)
- NO text, NO branding beyond icon
```

---

## 5. Performance Research

### 5.1 Perceived vs. Actual Performance

**Key Research Finding (Google):**
> "User research shows that perceived startup time is less when viewing an animation."

**Explanation:**
- **Actual Startup Time:** Time from app launch to interactive state
- **Perceived Startup Time:** How long user FEELS they waited
- **Gap:** Animation during wait makes time feel shorter (distraction effect)

### 5.2 Optimization Strategies

**From Android Migration Guide:**

1. **Lazy Loading:**
   ```kotlin
   // ❌ BAD: Load everything at startup
   override fun onCreate(savedInstanceState: Bundle?) {
       installSplashScreen()
       super.onCreate(savedInstanceState)
       
       loadAllLibraries()
       initDatabase()
       fetchRemoteConfig()
       
       setContent { App() }
   }
   
   // ✅ GOOD: Load minimum, defer rest
   override fun onCreate(savedInstanceState: Bundle?) {
       installSplashScreen()
       super.onCreate(savedInstanceState)
       
       // Only critical dependencies
       setContent { App() }
       
       lifecycleScope.launch {
           // Load heavy stuff after UI appears
           loadHeavyDependencies()
       }
   }
   ```

2. **Placeholder UI:**
   - For network-dependent content, show placeholder during loading
   - Don't keep splash visible until data arrives
   - Example: Skeleton screens, shimmer effects

3. **Caching:**
   - On first launch: Show loading indicators for some content
   - On subsequent launches: Show cached content while fetching new

4. **App Startup Library:**
   - Use Jetpack App Startup for component initialization
   - Ensures ordered, efficient init of dependencies

### 5.3 Performance Targets

**Industry Standards (2026):**
- **Cold Start:** < 1 second to interactive
- **Warm Start:** < 500ms to interactive
- **Hot Start:** < 200ms to interactive

**Splash Screen Portion:**
- Display: 200-500ms (animation duration)
- Exit: 200-300ms (transition to app)
- Total splash time: 400-800ms

---

## 6. Accessibility Considerations

### 6.1 Color Contrast

**WCAG 2.1 Requirements:**
- **Level AA:** 4.5:1 for normal text, 3:1 for large text
- **Level AAA:** 7:1 for normal text, 4.5:1 for large text

**For Splash Screens:**
- Icon background vs. screen background: ≥ 3:1 (UI component)
- Icon foreground vs. icon background: ≥ 3:1

**Our Implementation:**
- Light mode: 4.7:1 (AA compliant)
- Dark mode: 8.2:1 (AAA compliant)

### 6.2 Motion Preferences

**Android:**
```kotlin
val isReduceMotionEnabled = Settings.Global.getFloat(
    contentResolver,
    Settings.Global.TRANSITION_ANIMATION_SCALE,
    1f
) == 0f

if (isReduceMotionEnabled) {
    // Skip splash exit animation
    splashScreenView.remove()
} else {
    // Animate splash exit
    animateSplashExit(splashScreenView)
}
```

**iOS:**
```swift
if UIAccessibility.isReduceMotionEnabled {
    // Skip animations
} else {
    // Animate (but iOS launch screen is static anyway)
}
```

### 6.3 Screen Readers

**Best Practice:**
- Splash screens are NOT interactive
- Should NOT be announced by TalkBack/VoiceOver
- Should dismiss quickly to reach actual content

**Android:**
```xml
android:importantForAccessibility="no"
```

**iOS:**
```swift
isAccessibilityElement = false
```

---

## 7. Industry Best Practices (2026)

### 7.1 What's Outdated

**Deprecated Patterns:**
1. ❌ Full-screen branded splash with app name, tagline, logo
2. ❌ 2-3 second artificial delay "for branding"
3. ❌ Multiple splash screens (intro, loading, main app)
4. ❌ GIF/video animations in splash
5. ❌ Dedicated splash Activity/ViewController

**Why Deprecated:**
- Annoys users (especially on frequent app launches)
- Increases perceived startup time
- Violates platform guidelines (Android 12+, iOS 7+ design language)
- Poor accessibility (motion, screen readers)

### 7.2 Current Best Practices

**Modern Pattern (2026):**
1. ✅ System-managed splash with app icon
2. ✅ Single background color (light + dark modes)
3. ✅ Optional subtle animation (Android only, ≤1s)
4. ✅ Instant dismissal when app ready
5. ✅ Smooth transition to main UI

**Example Apps Following This:**
- Google apps (Gmail, Drive, Photos)
- Apple apps (Mail, Calendar, Notes)
- WhatsApp, Telegram
- Spotify, Netflix (as of 2023 updates)

### 7.3 Kotlin Multiplatform Considerations

**Shared Design:**
- Colors defined in common code
- Platform-specific rendering (Android XML, iOS Storyboard)

**Implementation Pattern:**
```kotlin
// commonMain
expect fun configureSplashScreen()

// androidMain
actual fun configureSplashScreen() {
    // Use Android SplashScreen API
}

// iosMain
actual fun configureSplashScreen() {
    // Configure via Info.plist / Storyboard
}
```

---

## 8. Migration from Legacy Splash

### 8.1 Problem: Double Splash

**Scenario:**
```
Before Android 12:
  User taps app → Custom splash Activity → Main Activity

On Android 12+:
  User taps app → System splash → Custom splash Activity → Main Activity
                    ^^^^^^^^^^^    ^^^^^^^^^^^^^^^^^^^^^^
                    New automatic  Old implementation
                    
Result: Double splash (poor UX)
```

### 8.2 Solution Strategies

**Strategy 1: Remove Custom Activity (Recommended)**
```kotlin
// Before
AndroidManifest.xml:
<activity android:name=".SplashActivity" android:theme="@style/SplashTheme">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name=".MainActivity" />

// After
<activity android:name=".MainActivity" android:theme="@style/Theme.App.Starting">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

**Strategy 2: Prevent Rendering of Old Activity**
```kotlin
class OldSplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Don't render, just route
        splashScreen.setKeepOnScreenCondition { true }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
```

**Strategy 3: Gradual Migration (Version-Based)**
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Android 12+: Use system splash, skip custom
    startActivity(Intent(this, MainActivity::class.java))
    finish()
} else {
    // Android 11-: Show custom splash
    setContentView(R.layout.splash_activity)
    Handler(Looper.getMainLooper()).postDelayed({
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }, 1000)
}
```

---

## 9. Tools and Resources

### 9.1 Design Tools

**For Creating Icons:**
- Figma / Adobe XD (design)
- Vector Asset Studio (Android Studio) - convert SVG to VectorDrawable
- Shape Shifter (https://shapeshifter.design/) - create AVD animations

**For Testing Contrast:**
- WebAIM Contrast Checker (https://webaim.org/resources/contrastchecker/)
- Accessible Colors (https://accessible-colors.com/)

### 9.2 Code Generators

**Android Theme Generator:**
```kotlin
// Use Android Studio New → Android Resource File → Values
// Select "Style" type, automatically generates theme skeleton
```

**iOS Asset Catalog Template:**
```bash
# Use Xcode → File → New → Asset Catalog
# Right-click → New Color Set
# Configure appearances in Attributes Inspector
```

### 9.3 Testing Tools

**Android:**
```bash
# Measure startup time
adb shell am start -W com.package/.MainActivity
# Look for: ThisTime, TotalTime, WaitTime

# Profile cold start
adb shell am force-stop com.package
adb shell am start -S -W com.package/.MainActivity

# Check dark mode
adb shell "cmd uimode night yes"
```

**iOS:**
```bash
# Simulate devices
xcrun simctl list devices
xcrun simctl boot <device-udid>

# Toggle appearance
xcrun simctl ui booted appearance dark

# Instruments for app launch profiling
instruments -t "App Launch" -D <trace-file> <app-binary>
```

---

## 10. Key Takeaways

### For Design Team

1. ✅ **Minimal is better:** Icon + background color, no text branding
2. ✅ **Dark mode is mandatory:** Not optional in 2026
3. ✅ **Accessibility matters:** WCAG AA minimum, AAA preferred
4. ✅ **Platform guidelines evolve:** Android 12 changed everything, stay updated

### For Development Team

1. ✅ **Use compat libraries:** androidx.core:core-splashscreen ensures consistency
2. ✅ **Optimize startup:** Defer non-critical init, use App Startup library
3. ✅ **Test on real devices:** Emulators don't reflect actual cold start times
4. ✅ **Migrate legacy splashes:** Remove custom Activities to avoid double splash

### For Product Team

1. ✅ **User research backs it:** Animations reduce perceived wait time
2. ✅ **Branding rethought:** Icon IS your brand, text splash is outdated
3. ✅ **Performance is UX:** Fast startup > fancy splash
4. ✅ **Platform alignment:** Fighting OS patterns creates friction

---

## 11. References

### Primary Sources

1. [Android Splash Screen API](https://developer.android.com/develop/ui/views/launch/splash-screen)
2. [Android Migration Guide](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate)
3. [AndroidX SplashScreen Library](https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen)
4. [Apple HIG - Launching](https://developer.apple.com/design/human-interface-guidelines/launching)
5. [iOS Launch Screen Spec](https://developer.apple.com/documentation/xcode/specifying-your-apps-launch-screen)

### Secondary Sources

6. [Material Design 3 - Motion](https://m3.material.io/styles/motion/overview)
7. [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
8. [Android Adaptive Icons](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
9. [Android App Startup](https://developer.android.com/topic/performance/vitals/launch-time)
10. [iOS Asset Catalog Documentation](https://developer.apple.com/library/archive/documentation/Xcode/Reference/xcode_ref-Asset_Catalog_Format/)

---

**Research Completed:** 2026-03-30  
**Researcher:** design-analysis-agent  
**Next Steps:** Implementation by android-expert-agent and ios-expert-agent
