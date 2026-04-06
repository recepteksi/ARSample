# iOS Splash Screen Implementation

## Overview
iOS splash screen implementation using LaunchScreen.storyboard with dark mode support and Material Design color scheme.

## Files Created

### 1. LaunchScreen.storyboard
- **Location:** `iosApp/iosApp/LaunchScreen.storyboard`
- **Purpose:** Launch screen displayed during app startup
- **Features:**
  - Centered icon with circular background
  - Auto-layout constraints for all device sizes
  - Safe area compatible
  - Dark mode support via named colors

### 2. Color Assets

#### LaunchBackground.colorset
- **Location:** `Assets.xcassets/LaunchBackground.colorset/`
- **Light Mode:** #FFFFFF (White)
- **Dark Mode:** #121212 (Material Dark Surface)
- **Usage:** Screen background color

#### LaunchIconBackground.colorset
- **Location:** `Assets.xcassets/LaunchIconBackground.colorset/`
- **Light Mode:** #6200EE (Material Purple 500)
- **Dark Mode:** #BB86FC (Material Purple 200)
- **Usage:** Circular background behind app icon

### 3. Splash Icon
- **Location:** `Assets.xcassets/SplashIcon.imageset/`
- **Sizes:**
  - `SplashIcon@1x.png` - 200x200 px
  - `SplashIcon@2x.png` - 400x400 px
  - `SplashIcon@3x.png` - 600x600 px
- **Source:** Resized from AppIcon (1024x1024)

### 4. Info.plist
- **Key Added:** `UILaunchStoryboardName`
- **Value:** `LaunchScreen`

## Design Specifications

### Color Palette

| Mode | Background | Icon Background | Icon Foreground |
|------|-----------|----------------|-----------------|
| Light | #FFFFFF | #6200EE | White |
| Dark | #121212 | #BB86FC | Black |

### Layout
```
┌────────────────────────────┐
│                            │
│                            │
│         ┌────────┐         │
│         │ Purple │         │  ← 200x200 circle
│         │ Circle │         │
│         │  Icon  │         │  ← 150x150 icon
│         └────────┘         │
│                            │
│                            │
└────────────────────────────┘
```

## Device Support

### Responsive Design
- Auto-layout constraints ensure proper scaling
- Center-aligned on all screen sizes
- Safe area compatible (notch, Dynamic Island)

### Tested Devices
- iPhone SE (375x667)
- iPhone 15 Pro (393x852)
- iPhone 15 Pro Max (430x932)
- iPad Pro 12.9" (1024x1366)

## Dark Mode Support

### Implementation
1. **Automatic Adaptation:** Color assets have separate light/dark variants
2. **System Integration:** Uses `appearance: luminosity, value: dark` in Contents.json
3. **No Code Required:** iOS automatically switches based on user preference

### Testing Dark Mode
```bash
# Xcode Simulator
Settings → Developer → Dark Appearance

# Programmatic Test (development)
Environment.colorScheme == .dark
```

## Build Instructions

### Open in Xcode
```bash
cd iosApp
open iosApp.xcodeproj
```

### Run on Simulator
1. Select target device (e.g., iPhone 15 Pro)
2. Press `Cmd + R` to build and run
3. Splash screen appears on launch

### Run on Physical Device
1. Connect iPhone/iPad via USB
2. Select device in Xcode
3. Build and run
4. To see splash again: Force quit app and relaunch

## Performance

### Launch Time
- **Target:** < 1 second display
- **Optimization:** No animations, static images only
- **Best Practice:** Dismissed immediately when app is ready

### Image Optimization
- PNG format with @1x, @2x, @3x scales
- Total size: ~50KB for all variants
- Loaded instantly by iOS

## Troubleshooting

### Splash Not Showing
1. **Clean Build Folder:** Xcode → Product → Clean Build Folder
2. **Reset Simulator:** Device → Erase All Content and Settings
3. **Check Info.plist:** Verify `UILaunchStoryboardName` = `LaunchScreen`

### Wrong Colors in Dark Mode
1. **Check Asset Catalog:** Verify dark appearance variants exist
2. **System Settings:** Ensure device is in dark mode
3. **Xcode Preview:** Use Environment Overrides to test

### Icon Not Centered
1. **Constraints:** Verify centerX and centerY constraints
2. **Safe Area:** Check safe area layout guide usage
3. **Device Testing:** Test on multiple screen sizes

## References

### Apple Documentation
- [Launch Screen Guidelines](https://developer.apple.com/design/human-interface-guidelines/launch-screen)
- [Asset Catalog Format](https://developer.apple.com/library/archive/documentation/Xcode/Reference/xcode_ref-Asset_Catalog_Format/)
- [Dark Mode Support](https://developer.apple.com/documentation/xcode/supporting-dark-mode-in-your-interface)

### Project Documentation
- [Design Spec](../../docs/design/splash/SPLASH_SCREEN_DESIGN_SPEC.md)
- [Color Spec](../../docs/design/splash/COLOR_SPEC.md)
- [Integration Guide](../../docs/design/splash/INTEGRATION_GUIDE.md)

## Next Steps

### Optional Enhancements
1. **SwiftUI Alternative:** Create SwiftUI-based launch screen for iOS 14+
2. **Animation:** Add subtle fade-in (requires custom implementation)
3. **Localization:** Support multiple languages (if app name shown)

### Related Tasks
- [ ] Android splash screen implementation
- [ ] Cross-platform splash consistency check
- [ ] Performance benchmarking
- [ ] Accessibility audit

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-04-05 | Initial iOS splash screen implementation |

---

**Branch:** `feature/splash-ios`  
**Status:** ✅ Ready for Testing  
**Reviewer:** iOS Team
