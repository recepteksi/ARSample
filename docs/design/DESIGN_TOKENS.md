# ARSample Design Tokens

**Quick Reference Guide**  
**Version:** 1.0  
**Last Updated:** 2026-03-30

---

## Color Palette

### Android (Material 3)

#### Light Theme

```kotlin
// Primary
val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFEADDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF21005D)

// Secondary
val md_theme_light_secondary = Color(0xFF625B71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)

// Tertiary
val md_theme_light_tertiary = Color(0xFF7D5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD8E4)
val md_theme_light_onTertiaryContainer = Color(0xFF31111D)

// Error
val md_theme_light_error = Color(0xFFB3261E)
val md_theme_light_errorContainer = Color(0xFFF9DEDC)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410E0B)

// Background
val md_theme_light_background = Color(0xFFFFFBFE)
val md_theme_light_onBackground = Color(0xFF1C1B1F)

// Surface
val md_theme_light_surface = Color(0xFFFFFBFE)
val md_theme_light_onSurface = Color(0xFF1C1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE7E0EC)
val md_theme_light_onSurfaceVariant = Color(0xFF49454F)

// Outline
val md_theme_light_outline = Color(0xFF79747E)
val md_theme_light_outlineVariant = Color(0xFFCAC4D0)
```

#### Dark Theme

```kotlin
// Primary
val md_theme_dark_primary = Color(0xFFD0BCFF)
val md_theme_dark_onPrimary = Color(0xFF381E72)
val md_theme_dark_primaryContainer = Color(0xFF4F378B)
val md_theme_dark_onPrimaryContainer = Color(0xFFEADDFF)

// Secondary
val md_theme_dark_secondary = Color(0xFFCCC2DC)
val md_theme_dark_onSecondary = Color(0xFF332D41)
val md_theme_dark_secondaryContainer = Color(0xFF4A4458)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8DEF8)

// Tertiary
val md_theme_dark_tertiary = Color(0xFFEFB8C8)
val md_theme_dark_onTertiary = Color(0xFF492532)
val md_theme_dark_tertiaryContainer = Color(0xFF633B48)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD8E4)

// Error
val md_theme_dark_error = Color(0xFFF2B8B5)
val md_theme_dark_errorContainer = Color(0xFF8C1D18)
val md_theme_dark_onError = Color(0xFF601410)
val md_theme_dark_onErrorContainer = Color(0xFFF9DEDC)

// Background
val md_theme_dark_background = Color(0xFF1C1B1F)
val md_theme_dark_onBackground = Color(0xFFE6E1E5)

// Surface
val md_theme_dark_surface = Color(0xFF1C1B1F)
val md_theme_dark_onSurface = Color(0xFFE6E1E5)
val md_theme_dark_surfaceVariant = Color(0xFF49454F)
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4D0)

// Outline
val md_theme_dark_outline = Color(0xFF938F99)
val md_theme_dark_outlineVariant = Color(0xFF49454F)
```

#### AR-Specific Colors

```kotlin
// Placement Indicator
val ar_placement_valid = Color(0xFF2196F3)      // Blue
val ar_placement_invalid = Color(0xFFF44336)    // Red
val ar_placement_alpha = 0.6f

// Selection
val ar_selection_outline = Color(0xFFFFEB3B)    // Yellow
val ar_selection_alpha = 0.4f

// Grid Overlay
val ar_grid_color = Color(0xFFFFFFFF)           // White
val ar_grid_alpha = 0.3f

// Shadows
val ar_shadow_color = Color(0xFF000000)         // Black
val ar_shadow_alpha = 0.4f
```

---

### iOS (SwiftUI)

#### System Colors

```swift
// Semantic Colors (Auto Light/Dark)
extension Color {
    // Backgrounds
    static let arBackground = Color(.systemBackground)
    static let arSecondaryBackground = Color(.secondarySystemBackground)
    static let arTertiaryBackground = Color(.tertiarySystemBackground)
    
    // Labels
    static let arLabel = Color(.label)
    static let arSecondaryLabel = Color(.secondaryLabel)
    static let arTertiaryLabel = Color(.tertiaryLabel)
    static let arQuaternaryLabel = Color(.quaternaryLabel)
    
    // Fills
    static let arFill = Color(.systemFill)
    static let arSecondaryFill = Color(.secondarySystemFill)
    static let arTertiaryFill = Color(.tertiarySystemFill)
    
    // Grouped Backgrounds
    static let arGroupedBackground = Color(.systemGroupedBackground)
    static let arSecondaryGroupedBackground = Color(.secondarySystemGroupedBackground)
}
```

#### Custom AR Colors

```swift
extension Color {
    // Primary Brand Colors
    static let arPrimary = Color("ARPrimary")        // Define in Assets
    static let arSecondary = Color("ARSecondary")
    
    // AR-Specific
    static let arPlacementValid = Color.blue.opacity(0.6)
    static let arPlacementInvalid = Color.red.opacity(0.6)
    static let arSelectionHighlight = Color.yellow.opacity(0.4)
    static let arGridOverlay = Color.white.opacity(0.3)
    static let arShadow = Color.black.opacity(0.4)
}
```

#### Hex Values for Assets Catalog

```
ARPrimary (Light): #6750A4
ARPrimary (Dark): #D0BCFF

ARSecondary (Light): #625B71
ARSecondary (Dark): #CCC2DC
```

---

## Typography

### Android (Compose)

```kotlin
// Display
displayLarge:   57sp / 64sp line height / -0.25sp letter spacing
displayMedium:  45sp / 52sp line height / 0sp letter spacing
displaySmall:   36sp / 44sp line height / 0sp letter spacing

// Headline
headlineLarge:  32sp / 40sp line height / 0sp letter spacing
headlineMedium: 28sp / 36sp line height / 0sp letter spacing
headlineSmall:  24sp / 32sp line height / 0sp letter spacing

// Title
titleLarge:     22sp / 28sp line height / 0sp letter spacing     (Regular)
titleMedium:    16sp / 24sp line height / 0.15sp letter spacing  (Medium)
titleSmall:     14sp / 20sp line height / 0.1sp letter spacing   (Medium)

// Body
bodyLarge:      16sp / 24sp line height / 0.5sp letter spacing   (Regular)
bodyMedium:     14sp / 20sp line height / 0.25sp letter spacing  (Regular)
bodySmall:      12sp / 16sp line height / 0.4sp letter spacing   (Regular)

// Label
labelLarge:     14sp / 20sp line height / 0.1sp letter spacing   (Medium)
labelMedium:    12sp / 16sp line height / 0.5sp letter spacing   (Medium)
labelSmall:     11sp / 16sp line height / 0.5sp letter spacing   (Medium)
```

---

### iOS (SwiftUI)

```swift
// Large Titles
largeTitle:     34pt / Bold

// Titles
title:          28pt / Semibold
title2:         22pt / Semibold
title3:         20pt / Semibold

// Headlines
headline:       17pt / Semibold

// Body
body:           17pt / Regular
callout:        16pt / Regular

// Subheadline & Footnotes
subheadline:    15pt / Regular
footnote:       13pt / Regular
caption:        12pt / Regular
caption2:       11pt / Regular
```

---

## Spacing

### Scale (8pt Grid System)

```
4pt   = 0.5 unit  (Extra Small)
8pt   = 1 unit    (Small)
12pt  = 1.5 units (Small-Medium)
16pt  = 2 units   (Medium) - Default padding
24pt  = 3 units   (Large)
32pt  = 4 units   (Extra Large)
48pt  = 6 units   (XXL)
64pt  = 8 units   (XXXL)
```

### Android (dp)

```kotlin
val spacing_xxs = 4.dp
val spacing_xs = 8.dp
val spacing_sm = 12.dp
val spacing_md = 16.dp   // Default
val spacing_lg = 24.dp
val spacing_xl = 32.dp
val spacing_xxl = 48.dp
val spacing_xxxl = 64.dp
```

### iOS (pt)

```swift
let spacingXXS: CGFloat = 4
let spacingXS: CGFloat = 8
let spacingSM: CGFloat = 12
let spacingMD: CGFloat = 16    // Default
let spacingLG: CGFloat = 24
let spacingXL: CGFloat = 32
let spacingXXL: CGFloat = 48
let spacingXXXL: CGFloat = 64
```

---

## Border Radius

### Android

```kotlin
val radius_xs = 4.dp
val radius_sm = 8.dp
val radius_md = 12.dp   // Cards
val radius_lg = 16.dp
val radius_xl = 24.dp
val radius_full = 9999.dp  // Fully rounded (Capsule)
```

### iOS

```swift
let radiusXS: CGFloat = 4
let radiusSM: CGFloat = 8
let radiusMD: CGFloat = 12    // Cards
let radiusLG: CGFloat = 16
let radiusXL: CGFloat = 24
let radiusFull: CGFloat = 9999  // Capsule
```

---

## Elevation (Shadows)

### Android (Material 3)

```kotlin
// Elevation levels
val elevation_level0 = 0.dp     // No shadow
val elevation_level1 = 1.dp     // FAB resting
val elevation_level2 = 3.dp     // Cards, Buttons
val elevation_level3 = 6.dp     // Dialogs, Menus
val elevation_level4 = 8.dp     // Navigation Drawer
val elevation_level5 = 12.dp    // Modal Bottom Sheets
```

### iOS (SwiftUI)

```swift
// Shadow styles
struct ElevationStyle {
    let radius: CGFloat
    let y: CGFloat
    let opacity: Double
}

let elevation1 = ElevationStyle(radius: 1, y: 1, opacity: 0.12)
let elevation2 = ElevationStyle(radius: 2, y: 2, opacity: 0.14)
let elevation3 = ElevationStyle(radius: 4, y: 4, opacity: 0.16)
let elevation4 = ElevationStyle(radius: 6, y: 6, opacity: 0.18)
let elevation5 = ElevationStyle(radius: 8, y: 8, opacity: 0.20)

// Usage
.shadow(
    color: .black.opacity(elevation2.opacity),
    radius: elevation2.radius,
    y: elevation2.y
)
```

---

## Icons

### Android (Material Icons)

```kotlin
// Navigation
Icons.Default.ArrowBack
Icons.Default.Close
Icons.Default.Menu

// Actions
Icons.Default.Add
Icons.Default.Delete
Icons.Default.Edit
Icons.Default.Search
Icons.Default.Settings

// AR-Specific
Icons.Default.CameraAlt         // AR view
Icons.Default.ThreeDRotation    // 3D object
Icons.Default.ViewInAr          // AR mode toggle
Icons.Default.Undo
Icons.Default.Redo
```

### iOS (SF Symbols)

```swift
// Navigation
"arrow.left"
"xmark"
"line.horizontal.3"

// Actions
"plus"
"trash"
"pencil"
"magnifyingglass"
"gearshape"

// AR-Specific
"camera.fill"                   // AR view
"rotate.3d"                     // 3D object
"arkit"                         // AR mode
"arrow.uturn.backward"          // Undo
"arrow.uturn.forward"           // Redo
"cube.fill"                     // 3D object
"viewfinder"                    // Placement reticle
```

---

## Animation Duration

### Standard Durations

```kotlin
// Android
val duration_instant = 0.ms
val duration_quick = 100.ms      // Button press
val duration_short = 200.ms      // Fade, Slide
val duration_medium = 300.ms     // Default transitions
val duration_long = 500.ms       // Complex animations
val duration_extraLong = 1000.ms // Coaching overlays
```

```swift
// iOS
let durationInstant: TimeInterval = 0
let durationQuick: TimeInterval = 0.1    // Button press
let durationShort: TimeInterval = 0.2    // Fade, Slide
let durationMedium: TimeInterval = 0.3   // Default
let durationLong: TimeInterval = 0.5     // Complex
let durationExtraLong: TimeInterval = 1.0 // Coaching
```

### Easing Curves

```kotlin
// Android
val easing_standard = FastOutSlowInEasing
val easing_emphasized = EmphasizedDecelerate
val easing_decelerated = DecelerationEasing
val easing_accelerated = AccelerationEasing
```

```swift
// iOS
let easingStandard = Animation.easeInOut
let easingDecelerated = Animation.easeOut
let easingAccelerated = Animation.easeIn
let easingSpring = Animation.spring(
    response: 0.3,
    dampingFraction: 0.7
)
```

---

## Touch Targets

### Minimum Sizes

```
Android:  48dp x 48dp  (Material Design)
iOS:      44pt x 44pt  (HIG)
```

### Implementation

```kotlin
// Android
Modifier.size(48.dp)
Modifier.minimumInteractiveComponentSize()
```

```swift
// iOS
.frame(minWidth: 44, minHeight: 44)
```

---

## Opacity Levels

```
Fully Opaque:       1.0  (100%)
High Emphasis:      0.87 (87%)
Medium Emphasis:    0.60 (60%)
Low Emphasis:       0.38 (38%)
Disabled:           0.38 (38%)
Dividers:           0.12 (12%)

AR Overlays:        0.6-0.8
AR Backgrounds:     0.3-0.5
AR Reticles:        0.5-0.7
```

---

## Component Sizes

### Buttons

```kotlin
// Android
Small:    40dp height
Medium:   48dp height (Default)
Large:    56dp height

// FAB
Small:    40dp
Medium:   56dp (Default)
Large:    96dp
Extended: 48dp height, variable width
```

```swift
// iOS
.controlSize(.mini)       // Small
.controlSize(.regular)    // Default
.controlSize(.large)      // Large
```

---

## Grid System

### Android (Lazy Grid)

```kotlin
// Adaptive columns (responsive)
GridCells.Adaptive(minSize = 160.dp)

// Fixed columns
GridCells.Fixed(count = 2)   // Phone
GridCells.Fixed(count = 3)   // Tablet
```

### iOS (Lazy VGrid)

```swift
// Adaptive columns
GridItem(.adaptive(minimum: 160))

// Fixed columns
GridItem(.fixed(160))
GridItem(.flexible())
```

---

## Breakpoints (Responsive Design)

### Window Size Classes

```
Compact:    width < 600dp/pt   (Phone portrait)
Medium:     600dp ≤ width < 840dp  (Tablet portrait, phone landscape)
Expanded:   width ≥ 840dp      (Tablet landscape, desktop)
```

### Implementation

```kotlin
// Android
val windowSizeClass = calculateWindowSizeClass(this)

when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.Compact -> { /* Phone */ }
    WindowWidthSizeClass.Medium -> { /* Tablet portrait */ }
    WindowWidthSizeClass.Expanded -> { /* Tablet landscape */ }
}
```

```swift
// iOS (Size Classes)
@Environment(\.horizontalSizeClass) var horizontalSizeClass

if horizontalSizeClass == .compact {
    // Phone portrait
} else {
    // iPad or landscape
}
```

---

## Z-Index (Layering)

```
Background:        0
AR Scene:          1
AR Overlays:       2
UI Controls:       3
Floating Buttons:  4
Dialogs:           5
Modals:            6
Tooltips:          7
Toasts/Snackbars:  8
```

---

## AR-Specific Values

### Placement

```kotlin
// Reticle size
val ar_reticle_size = 100.dp

// Minimum/Maximum scale
val ar_scale_min = 0.1f
val ar_scale_max = 5.0f

// Rotation snap angle
val ar_rotation_snap = 15f  // degrees

// Placement distance from camera
val ar_placement_distance_min = 0.2f  // meters
val ar_placement_distance_max = 10.0f // meters
```

### Performance

```kotlin
// Target frame rate
val ar_target_fps = 60

// Model complexity limits
val ar_max_polygons = 100_000
val ar_max_textures = 4
val ar_max_texture_size = 2048  // px

// Loading timeout
val ar_model_load_timeout = 5000.ms
```

---

## Usage Examples

### Android Compose

```kotlin
@Composable
fun ARSampleButton() {
    Button(
        onClick = { /* Action */ },
        modifier = Modifier
            .height(48.dp)  // spacing_md
            .padding(horizontal = 16.dp),  // spacing_md
        shape = RoundedCornerShape(12.dp),  // radius_md
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "Place Object",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

### iOS SwiftUI

```swift
struct ARSampleButton: View {
    var body: some View {
        Button("Place Object") {
            // Action
        }
        .font(.headline)
        .foregroundStyle(.white)
        .padding(.horizontal, 16)
        .frame(height: 44)
        .background(.arPrimary)
        .cornerRadius(12)
    }
}
```

---

## Quick Reference Tables

### Colors

| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| Primary | #6750A4 | #D0BCFF | Primary actions, FAB |
| Secondary | #625B71 | #CCC2DC | Secondary actions |
| Error | #B3261E | #F2B8B5 | Errors, destructive |
| Background | #FFFBFE | #1C1B1F | Screen background |
| Surface | #FFFBFE | #1C1B1F | Cards, sheets |

### Spacing

| Token | Value | Usage |
|-------|-------|-------|
| xs | 8dp/pt | Tight spacing |
| md | 16dp/pt | Default padding |
| lg | 24dp/pt | Section spacing |
| xl | 32dp/pt | Large gaps |

### Typography (Most Used)

| Token | Size | Weight | Usage |
|-------|------|--------|-------|
| headlineSmall | 24sp/pt | Regular | Screen titles |
| titleMedium | 16sp/pt | Medium | Card titles |
| bodyLarge | 16sp/pt | Regular | Body text |
| labelLarge | 14sp/pt | Medium | Buttons |

---

**Tokens Version:** 1.0  
**Platform Compatibility:** Android 12+ (API 31+), iOS 15+  
**Last Updated:** 2026-03-30
