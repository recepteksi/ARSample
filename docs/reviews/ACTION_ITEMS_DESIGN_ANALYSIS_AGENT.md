# Action Items for design-analysis-agent

**Date:** 2026-04-05  
**From:** code-reviewer-agent  
**Priority:** 🔥 CRITICAL - Must fix before merge

---

## 🎯 Critical Fixes Required

You need to fix **3 critical issues** in `DESIGN_TOKENS.md` before this branch can be merged to dev.

**Branch:** feature/splash-ios  
**Commit:** e959b95  
**File:** docs/design/DESIGN_TOKENS.md  
**Estimated Time:** 2-3 hours

---

## ⚠️ Issue #1: iOS Custom Color Asset Setup Missing

**Location:** DESIGN_TOKENS.md, around line ~150

**Problem:**
You show code like `Color("ARPrimary")` but iOS developers won't know how to create these custom color assets in Xcode.

**What to Add:**
Add a new section "3.5 iOS Asset Catalog Color Setup" with:

```markdown
### 3.5 iOS Asset Catalog Color Setup

**Why Asset Catalog?**
iOS requires custom colors to be defined in the Asset Catalog to support light/dark mode variants automatically.

**Step-by-Step Setup:**

#### Step 1: Open Asset Catalog
1. In Xcode, open `iosApp/Assets.xcassets`
2. Right-click in the navigator
3. Select **New Color Set**

#### Step 2: Configure ARPrimary Color
1. Name the color set: `ARPrimary`
2. Click on the color set
3. In the Attributes Inspector:
   - **Appearances:** Select "Any, Dark"
   
4. Set colors:
   - **Any Appearance (Light Mode):** `#6200EE` (Purple 500)
   - **Dark Appearance:** `#BB86FC` (Purple 200)

#### Step 3: Repeat for All Custom Colors
Create color sets for:
- ARPrimary (#6200EE / #BB86FC)
- ARSecondary (#03DAC6 / #03DAC6)
- ARError (#B00020 / #CF6679)
- ARBackground (#FFFFFF / #121212)
- ARSurface (#FFFFFF / #1E1E1E)

#### Step 4: Use in Code
```swift
// Now you can use:
Color("ARPrimary")
Color("ARSecondary")
```

**Asset Catalog JSON Structure:**
For reference, the `ARPrimary.colorset/Contents.json` should look like:
```json
{
  "colors" : [
    {
      "color" : {
        "color-space" : "srgb",
        "components" : {
          "alpha" : "1.000",
          "blue" : "0.933",
          "green" : "0.000",
          "red" : "0.384"
        }
      },
      "idiom" : "universal"
    },
    {
      "appearances" : [
        {
          "appearance" : "luminosity",
          "value" : "dark"
        }
      ],
      "color" : {
        "color-space" : "srgb",
        "components" : {
          "alpha" : "1.000",
          "blue" : "0.988",
          "green" : "0.525",
          "red" : "0.733"
        }
      },
      "idiom" : "universal"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
```

**Visual Guide:**
```
Xcode → Assets.xcassets
  ├── AppIcon.appiconset
  └── Colors/
      ├── ARPrimary.colorset
      │   ├── Light: #6200EE
      │   └── Dark:  #BB86FC
      ├── ARSecondary.colorset
      └── ARBackground.colorset
```
```

**Why This Matters:**
Without this, iOS developers will get build errors when trying to use `Color("ARPrimary")`.

---

## ⚠️ Issue #2: Shadow/Elevation System Incomplete

**Location:** DESIGN_TOKENS.md, around line ~320

**Problem:**
Material Design 3 has a complete elevation system, but only AR shadows are documented. iOS shadow specification is completely missing.

**What to Add:**
Add a new section "5.3 Elevation & Shadows" with:

```markdown
### 5.3 Elevation & Shadows

Material Design 3 uses elevation to create depth hierarchy. iOS uses explicit shadows.

#### Android Material 3 Elevation Scale

| Component | Default Elevation | Pressed/Dragged | Usage |
|-----------|------------------|-----------------|-------|
| **Surface** | 0dp | - | Base layer |
| **Button** | 0dp | 3dp | Text/Outlined buttons |
| **Filled Button** | 1dp | 3dp | Primary actions |
| **Card** | 1dp | 6dp | Content containers |
| **FAB** | 6dp | 12dp | Primary floating action |
| **Bottom Sheet** | 8dp | - | Modal overlays |
| **App Bar** | 0dp | 3dp (scrolled) | Top navigation |
| **Dialog** | 24dp | - | Modal dialogs |

**Compose Implementation:**
```kotlin
// Tonal Elevation (Material 3)
Surface(
    tonalElevation = 2.dp,  // Creates subtle tint
    shadowElevation = 2.dp  // Creates shadow
) {
    Text("Elevated surface")
}

// Button with state elevation
Button(
    onClick = { },
    elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 1.dp,
        pressedElevation = 3.dp,
        hoveredElevation = 2.dp,
        focusedElevation = 2.dp
    )
) {
    Text("Button")
}

// Card elevation
Card(
    elevation = CardDefaults.cardElevation(
        defaultElevation = 1.dp,
        draggedElevation = 6.dp
    )
) { }
```

#### iOS Shadow System

iOS doesn't use elevation, but explicit shadow properties.

**Shadow Specification:**

| Component | Shadow Radius | Offset (x, y) | Color | Opacity |
|-----------|--------------|---------------|-------|---------|
| **Card** | 8pt | (0, 2) | Black | 0.15 |
| **Button (Raised)** | 4pt | (0, 2) | Black | 0.20 |
| **Modal** | 16pt | (0, 8) | Black | 0.25 |
| **FAB** | 12pt | (0, 6) | Black | 0.20 |
| **Bottom Sheet** | 16pt | (0, -4) | Black | 0.20 |

**SwiftUI Implementation:**
```swift
// Card shadow
RoundedRectangle(cornerRadius: 12)
    .fill(Color.white)
    .shadow(
        color: Color.black.opacity(0.15),
        radius: 8,
        x: 0,
        y: 2
    )

// Button shadow
Button("Action") { }
    .buttonStyle(.borderedProminent)
    .shadow(
        color: Color.black.opacity(0.20),
        radius: 4,
        x: 0,
        y: 2
    )

// Modal/Dialog shadow
VStack {
    // Modal content
}
.background(Color.white)
.cornerRadius(16)
.shadow(
    color: Color.black.opacity(0.25),
    radius: 16,
    x: 0,
    y: 8
)
```

#### AR-Specific Shadows

AR objects in 3D space use different shadow techniques:

```kotlin
// Android ARCore - Shadow plane
val shadowPlane = renderable.shadow
shadowPlane.let {
    it.material.setFloat3("color", Color.BLACK)
    it.material.setFloat("opacity", 0.4f)  // 40% opacity
    it.material.setFloat("softness", 0.8f) // Soft edges
}
```

```swift
// iOS ARKit - Shadow anchor
let shadowAnchor = ARAnchor(transform: object.transform)
shadowAnchor.shadowIntensity = 0.4  // 40% opacity
shadowAnchor.shadowRadius = 0.2     // 20cm radius
```

**Key Differences:**
- **UI Shadows:** 2D, fixed to screen, use dp/pt
- **AR Shadows:** 3D, anchored to world, use meters
- **UI:** Soft, translucent, subtle
- **AR:** Sharp, directional, realistic

#### Accessibility Note
High contrast mode should increase shadow opacity:
- Normal: 0.15-0.25 opacity
- High contrast: 0.30-0.40 opacity
```

**Why This Matters:**
Components need proper depth to create visual hierarchy. Without this, UI will look flat and unprofessional.

---

## ⚠️ Issue #3: Typography Scaling Inconsistency Not Explained

**Location:** DESIGN_TOKENS.md, around line ~200

**Problem:**
You show Android `displayLarge = 57.sp` but iOS `largeTitle = 34pt`. These are NOT equivalent sizes (57px ≠ 34pt = 45.3px), but there's no explanation.

**What to Add:**
Add a new subsection "2.3.1 Platform-Specific Typography Scaling" after the typography tables:

```markdown
### 2.3.1 Platform-Specific Typography Scaling

#### Why Different Values?

Android and iOS use different base type scales because:

1. **Different Design Systems:**
   - Android: Material Design 3 type scale (Google)
   - iOS: Human Interface Guidelines type scale (Apple)

2. **Different Font Families:**
   - Android: Roboto (designed for larger sizes)
   - iOS: SF Pro (optimized for smaller sizes, better readability)

3. **Different UI Chrome:**
   - iOS navigation bars take more vertical space
   - iOS uses smaller base sizes to accommodate UI elements

#### Size Comparison

| Purpose | Android (MD3) | iOS (HIG) | Notes |
|---------|--------------|----------|-------|
| **Screen Title** | 57sp (displayLarge) | 34pt (largeTitle) | iOS uses in navigation |
| **Section Header** | 45sp (displayMedium) | 28pt (title1) | Android more prominent |
| **Card Title** | 24sp (titleLarge) | 22pt (title2) | Nearly equivalent |
| **Body Text** | 16sp (bodyLarge) | 17pt (body) | iOS slightly larger |
| **Caption** | 12sp (labelSmall) | 11pt (caption2) | Android slightly larger |

#### Conversion Formula (Reference Only)

**Physical pixels:**
- 1sp on Android ≈ 1dp ≈ 1/160 inch at mdpi
- 1pt on iOS ≈ 1/72 inch at 1x scale

**Do NOT convert directly!** Use platform-specific scales.

**Example:**
```kotlin
// Android - Use Material 3 scale
Text(
    "Welcome",
    style = MaterialTheme.typography.displayLarge  // 57sp
)
```

```swift
// iOS - Use HIG scale
Text("Welcome")
    .font(.largeTitle)  // 34pt (system default)
```

#### Why Not Use Same Sizes?

Using platform-specific scales ensures:
- ✅ Familiar UX for platform users
- ✅ Optimal readability with platform fonts
- ✅ Proper spacing with platform UI components
- ✅ Native feel, not "ported" app

**Typography Philosophy:**
- **Android:** Bold, geometric, spacious
- **iOS:** Refined, compact, elegant

Both are correct for their platforms. The goal is NOT visual parity, but **native platform excellence**.

#### Dynamic Type Support

**iOS:**
iOS users can adjust system-wide text size. Always respect Dynamic Type:

```swift
// ✅ Respects user preference
Text("Settings")
    .font(.body)

// ❌ Fixed size, ignores preference
Text("Settings")
    .font(.system(size: 16))
```

**Android:**
Android users can adjust font scale. Handle in theme:

```kotlin
// Automatically scales with user preference
MaterialTheme(
    typography = AppTypography  // Uses sp units, scales automatically
) { }
```

#### Testing Different Scales

**Android:**
Settings → Display → Font size → Large

**iOS:**
Settings → Accessibility → Display & Text Size → Larger Text

Both platforms should maintain hierarchy even at largest sizes.
```

**Why This Matters:**
Developers need to understand why platforms differ to avoid trying to "match" sizes, which would create poor UX on both platforms.

---

## ✅ Verification Checklist

After making these changes, verify:

- [ ] iOS color asset setup section added with step-by-step guide
- [ ] Asset Catalog JSON example provided
- [ ] Material 3 elevation scale complete (all components)
- [ ] iOS shadow specification complete (radius, offset, opacity, color)
- [ ] AR-specific shadow documentation added
- [ ] Typography scaling explanation added
- [ ] Conversion formula provided with "Do NOT convert" warning
- [ ] Dynamic Type support documented
- [ ] Code examples for both platforms
- [ ] Tables formatted correctly
- [ ] All code blocks have language syntax highlighting

---

## 📝 How to Submit

1. Checkout branch: `git checkout feature/splash-ios`
2. Edit file: `docs/design/DESIGN_TOKENS.md`
3. Add the 3 sections above
4. Commit: `git commit -m "fix: complete DESIGN_TOKENS with iOS setup, elevation, typography scaling"`
5. Push: `git push origin feature/splash-ios`
6. Notify: code-reviewer-agent for re-review

---

## 🔄 After You Fix

I will:
1. Re-review DESIGN_TOKENS.md
2. If approved → Recommend merge to dev
3. If issues remain → Provide feedback

**Target:** Get this merged today! The documentation is excellent, just needs these 3 clarifications.

---

**Questions?** Tag code-reviewer-agent

**Estimated Time:** 2-3 hours  
**Priority:** 🔥 Critical (blocking merge)

---

**Generated by:** code-reviewer-agent  
**Date:** 2026-04-05
