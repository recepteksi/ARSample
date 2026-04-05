# ARSample Design Documentation

**Project:** ARSample - 3D Object Placement/Removal Application  
**Platform:** Kotlin Multiplatform (Android + iOS)  
**Architecture:** Clean Architecture + Domain-Driven Design (DDD)

---

## 📚 Documentation Overview

This directory contains comprehensive design documentation for the ARSample application, covering UI/UX guidelines, design tokens, competitor analysis, and best practices.

---

## 📂 Document Index

### 1. [UI/UX Design Guide](./UI_UX_DESIGN_GUIDE.md)

**Comprehensive design system documentation**

- ✅ Design Philosophy & Principles
- ✅ Material Design 3 (Android) Guidelines
- ✅ iOS Human Interface Guidelines
- ✅ AR-Specific Design Principles
- ✅ Color System (Light/Dark Themes)
- ✅ Typography Scale
- ✅ Component Design Patterns
- ✅ AR Interaction Patterns
- ✅ Spatial UI Guidelines
- ✅ Accessibility Standards
- ✅ Performance Considerations

**Key Topics:**
- Platform-specific implementations (Android Compose + iOS SwiftUI)
- Code examples for common components
- AR coaching overlays
- Gesture-based interactions
- Visual feedback patterns

**Use When:**
- Designing new UI components
- Implementing AR interactions
- Ensuring platform consistency
- Planning accessibility features

---

### 2. [AR Competitor Analysis](./AR_COMPETITOR_ANALYSIS.md)

**Industry research and best practices**

- ✅ Analysis of Top AR Apps (IKEA Place, Amazon AR, Houzz, Pokemon GO)
- ✅ Feature Comparison Matrix
- ✅ UI/UX Pattern Library
- ✅ Coaching & Onboarding Strategies
- ✅ Visual Feedback Examples
- ✅ Gesture Interaction Patterns
- ✅ Performance Benchmarks

**Key Insights:**
- Bottom sheet object selection (IKEA pattern)
- Instant placement (Amazon pattern)
- Comparison mode (Houzz pattern)
- Progressive disclosure strategies
- Color psychology in AR

**Use When:**
- Researching feature implementation approaches
- Comparing against industry standards
- Identifying UX gaps
- Planning feature roadmap

---

### 3. [Design Tokens](./DESIGN_TOKENS.md)

**Quick reference for design values**

- ✅ Color Palette (Material 3 + iOS)
- ✅ Typography Scale (Android + iOS)
- ✅ Spacing System (8pt Grid)
- ✅ Border Radius Values
- ✅ Elevation/Shadow Styles
- ✅ Icon References (Material Icons + SF Symbols)
- ✅ Animation Durations & Easing
- ✅ Touch Target Sizes
- ✅ Opacity Levels
- ✅ AR-Specific Values

**Quick Access:**
- Copy-paste code snippets
- Platform-specific token values
- Usage examples
- Responsive breakpoints

**Use When:**
- Implementing UI components
- Styling screens
- Ensuring design consistency
- Setting up theme configuration

---

## 🎨 Design System Summary

### Color Scheme

**Android Material 3:**
- Primary: Purple (`#6750A4` / `#D0BCFF`)
- Secondary: Gray-Purple (`#625B71` / `#CCC2DC`)
- Supports Dynamic Color (Material You)

**iOS:**
- System Semantic Colors (Auto Light/Dark)
- Custom Brand Colors in Assets Catalog

**AR Colors:**
- Placement Valid: Blue (`#2196F3` @ 60% opacity)
- Placement Invalid: Red (`#F44336` @ 60% opacity)
- Selection: Yellow (`#FFEB3B` @ 40% opacity)
- Grid Overlay: White (`#FFFFFF` @ 30% opacity)

---

### Typography

**Android:**
- Font Family: Default (Roboto)
- Scale: Material 3 Type Scale (Display, Headline, Title, Body, Label)
- Units: `sp` (scalable pixels)

**iOS:**
- Font Family: SF Pro (System Font)
- Scale: Dynamic Type (Large Title, Title, Headline, Body, Caption)
- Units: `pt` (points)

---

### Spacing (8pt Grid)

```
xs:   8dp/pt   (Tight spacing)
md:   16dp/pt  (Default padding)
lg:   24dp/pt  (Section spacing)
xl:   32dp/pt  (Large gaps)
```

---

### Component Patterns

**Object List Screen:**
- LazyGrid layout (Adaptive columns)
- Card-based items
- Thumbnail + metadata
- Add button (FAB/Toolbar)

**AR Placement Screen:**
- Full-screen AR view
- Minimal UI chrome
- Floating controls (top/bottom)
- Coaching overlay (first-time)
- Gesture-based manipulation

---

## 🚀 Implementation Guidelines

### 1. Platform Consistency

**Android:**
- Follow Material Design 3 guidelines
- Use Jetpack Compose for UI
- Implement Material You dynamic colors
- Support dark theme

**iOS:**
- Follow Human Interface Guidelines
- Use SwiftUI for UI
- Leverage SF Symbols
- Support Dynamic Type

---

### 2. AR Best Practices

**Visual Design:**
- Minimize UI during AR mode
- Use semi-transparent overlays
- High contrast for readability
- Clear visual indicators

**Interaction:**
- Gestures over buttons
- Immediate haptic feedback
- Error prevention (undo/redo)
- Progressive enhancement

**Performance:**
- Target 60 FPS
- Model LOD (Level of Detail)
- Texture compression
- Battery optimization

---

### 3. Accessibility

**Visual:**
- WCAG AA compliance (4.5:1 contrast)
- Support Dynamic Type / Font Scaling
- Minimum touch targets (44pt/48dp)

**Haptic:**
- Light feedback on placement
- Medium feedback on errors
- Notification at gesture limits

**Screen Readers:**
- VoiceOver / TalkBack support
- Descriptive labels for all UI
- Logical focus order

---

## 📋 Checklists

### Pre-Implementation

- [ ] Review [UI/UX Design Guide](./UI_UX_DESIGN_GUIDE.md)
- [ ] Check [Design Tokens](./DESIGN_TOKENS.md) for values
- [ ] Research competitor patterns in [AR Competitor Analysis](./AR_COMPETITOR_ANALYSIS.md)
- [ ] Plan component hierarchy
- [ ] Define state management

### During Development

- [ ] Use design tokens (no hard-coded values)
- [ ] Follow platform guidelines
- [ ] Implement accessibility features
- [ ] Add haptic feedback
- [ ] Test on multiple screen sizes
- [ ] Optimize for performance (60 FPS)

### Before Merge

- [ ] Design review (against guidelines)
- [ ] Accessibility audit (TalkBack/VoiceOver)
- [ ] Color contrast verification
- [ ] Performance testing
- [ ] Device testing (various screen sizes)

---

## 🛠️ Tools & Resources

### Design Tools

- **Figma:** [Material 3 Design Kit](https://www.figma.com/community/file/1035203688168086460)
- **Material Theme Builder:** [Online Tool](https://material-foundation.github.io/material-theme-builder/)
- **SF Symbols App:** [Download](https://developer.apple.com/sf-symbols/)
- **Color Contrast Checker:** [WebAIM](https://webaim.org/resources/contrastchecker/)

### Documentation

- **Material Design 3:** https://m3.material.io/
- **iOS HIG:** https://developer.apple.com/design/human-interface-guidelines/
- **ARCore Guidelines:** https://developers.google.com/ar/design
- **ARKit Resources:** https://developer.apple.com/design/resources/

### Testing

- **Accessibility Scanner (Android):** Google Play Store
- **Xcode Accessibility Inspector (iOS):** Built-in
- **Color Blindness Simulator:** [Coblis](https://www.color-blindness.com/coblis-color-blindness-simulator/)

---

## 📱 Platform-Specific Notes

### Android (Compose)

**Key Libraries:**
```kotlin
// Material 3
implementation("androidx.compose.material3:material3:1.1.2")

// Coil for image loading
implementation("io.coil-kt:coil-compose:2.5.0")

// Accompanist for system UI
implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
```

**Theme Setup:**
```kotlin
// See UI_UX_DESIGN_GUIDE.md for full implementation
@Composable
fun ARSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) { /* ... */ }
```

---

### iOS (SwiftUI)

**Key APIs:**
```swift
// SF Symbols
Image(systemName: "arkit")

// Dynamic Type
.font(.headline)

// System Colors
Color(.systemBackground)

// Haptics
UIImpactFeedbackGenerator(style: .light).impactOccurred()
```

**Theme Setup:**
```swift
// See UI_UX_DESIGN_GUIDE.md for full implementation
extension Color {
    static let arPrimary = Color("ARPrimary")
    static let arSecondary = Color("ARSecondary")
}
```

---

## 🎯 Design Goals

### User Experience Goals

1. **Intuitive AR Placement**
   - First-time success rate: > 90%
   - Average time to place object: < 5 seconds
   - Clear visual feedback at every step

2. **Minimal Learning Curve**
   - Coaching overlay on first launch
   - Progressive disclosure of features
   - Gesture hints when user is idle

3. **Performance**
   - AR session load time: < 2 seconds
   - Consistent 60 FPS
   - Smooth object manipulation

4. **Accessibility**
   - WCAG AA compliance
   - Full screen reader support
   - Large touch targets

---

## 🔄 Document Maintenance

### Update Schedule

- **Weekly:** Review for typos and minor corrections
- **Monthly:** Update with new component patterns
- **Quarterly:** Competitor analysis refresh
- **Annually:** Full design system audit

### Versioning

- **Major Version (X.0):** Complete redesign or new design system
- **Minor Version (0.X):** New components or significant pattern changes
- **Patch (0.0.X):** Corrections, clarifications, token updates

**Current Version:** 1.0.0

---

## 📞 Contact & Contributions

### Questions or Suggestions?

- **Design Team:** design@arsample.com
- **Issue Tracker:** GitHub Issues
- **Slack:** #design-system channel

### Contributing

1. Review existing documentation
2. Propose changes via PR
3. Include rationale and examples
4. Update version number

---

## 📜 License

This design documentation is proprietary to the ARSample project.

---

## 🔖 Quick Links

| Document | Purpose | Last Updated |
|----------|---------|--------------|
| [UI/UX Design Guide](./UI_UX_DESIGN_GUIDE.md) | Comprehensive design system | 2026-03-30 |
| [AR Competitor Analysis](./AR_COMPETITOR_ANALYSIS.md) | Industry research | 2026-03-30 |
| [Design Tokens](./DESIGN_TOKENS.md) | Quick reference values | 2026-03-30 |

---

**Documentation Version:** 1.0.0  
**Last Updated:** 2026-03-30  
**Maintained By:** Design & Analysis Team
