# ARSample App Icon Design

**Version:** 1.0  
**Date:** 2026-03-30  
**Status:** Ready for Export

---

## 🎨 Design Concept

### Theme
Modern AR application icon featuring:
- **3D Isometric Cube:** Represents 3D model placement
- **AR Scan Corners:** Corner brackets suggesting AR scanning/detection
- **GLB Badge:** Highlights the 3D model format support
- **Gradient Background:** Modern, eye-catching purple-indigo gradient

### Visual Elements
1. **Background Gradient:** Purple to indigo (#667eea → #764ba2)
2. **3D Cube:** White to light indigo shades for depth
3. **AR Corner Markers:** White scan frame corners
4. **Grid Dots:** Subtle AR plane detection hints
5. **GLB Badge:** Format identifier (optional)

---

## 🎨 Color Palette

| Element | Color | Hex Code | Purpose |
|---------|-------|----------|---------|
| **Primary Gradient Start** | Purple | `#667eea` | Background top-left |
| **Primary Gradient End** | Deep Purple | `#764ba2` | Background bottom-right |
| **Cube Top Face** | White | `#ffffff` → `#e0e7ff` | Lightest face |
| **Cube Left Face** | Light Indigo | `#c7d2fe` → `#a5b4fc` | Medium shade |
| **Cube Right Face** | Indigo | `#818cf8` → `#a5b4fc` | Darker shade |
| **AR Corners** | White | `#ffffff` (80% opacity) | Scan frame |
| **Grid Dots** | White | `#ffffff` (15% opacity) | Subtle detail |
| **GLB Text** | White | `#ffffff` | Badge text |

---

## 📐 Design Specifications

### Master File
- **File:** `icon-master.svg`
- **Size:** 1024x1024 px
- **Format:** SVG (vector)
- **Border Radius:** 226.67px (iOS rounded square guideline)

### Android Requirements
- **Adaptive Icon:** Foreground + Background layers
- **Safe Zone:** 66dp diameter circle (center-weighted content)
- **Keyline Shapes:** Square, Circle, Rounded Square, Squircle

**Required Sizes:**
```
mipmap-mdpi/    → 48x48 px
mipmap-hdpi/    → 72x72 px
mipmap-xhdpi/   → 96x96 px
mipmap-xxhdpi/  → 144x144 px
mipmap-xxxhdpi/ → 192x192 px
```

### iOS Requirements
- **No Transparency:** PNG with opaque background
- **Pre-rounded:** iOS applies corner radius automatically
- **Flat Design:** No alpha channel

**Required Sizes:**
```
AppIcon-1024   → 1024x1024 px (App Store)
AppIcon-180    → 180x180 px (iPhone 3x)
AppIcon-120    → 120x120 px (iPhone 2x)
AppIcon-87     → 87x87 px (iPhone 3x Settings)
AppIcon-80     → 80x80 px (iPad 2x Spotlight)
AppIcon-76     → 76x76 px (iPad 1x)
AppIcon-60     → 60x60 px (iPhone 1x Spotlight)
AppIcon-58     → 58x58 px (iPad 1x Settings)
AppIcon-40     → 40x40 px (iPad 1x Spotlight)
AppIcon-29     → 29x29 px (iPhone 1x Settings)
AppIcon-20     → 20x20 px (iPad 1x Notifications)
```

---

## 🛠️ Export Instructions

### Option 1: Using Inkscape (Free)

```bash
# Install Inkscape
brew install inkscape

# Export all iOS sizes
inkscape icon-master.svg --export-filename=ios/AppIcon-1024.png -w 1024 -h 1024
inkscape icon-master.svg --export-filename=ios/AppIcon-180.png -w 180 -h 180
inkscape icon-master.svg --export-filename=ios/AppIcon-120.png -w 120 -h 120
inkscape icon-master.svg --export-filename=ios/AppIcon-87.png -w 87 -h 87
inkscape icon-master.svg --export-filename=ios/AppIcon-80.png -w 80 -h 80
inkscape icon-master.svg --export-filename=ios/AppIcon-76.png -w 76 -h 76
inkscape icon-master.svg --export-filename=ios/AppIcon-60.png -w 60 -h 60
inkscape icon-master.svg --export-filename=ios/AppIcon-58.png -w 58 -h 58
inkscape icon-master.svg --export-filename=ios/AppIcon-40.png -w 40 -h 40
inkscape icon-master.svg --export-filename=ios/AppIcon-29.png -w 29 -h 29
inkscape icon-master.svg --export-filename=ios/AppIcon-20.png -w 20 -h 20

# Export Android sizes
inkscape icon-master.svg --export-filename=android/ic_launcher-192.png -w 192 -h 192
inkscape icon-master.svg --export-filename=android/ic_launcher-144.png -w 144 -h 144
inkscape icon-master.svg --export-filename=android/ic_launcher-96.png -w 96 -h 96
inkscape icon-master.svg --export-filename=android/ic_launcher-72.png -w 72 -h 72
inkscape icon-master.svg --export-filename=android/ic_launcher-48.png -w 48 -h 48
```

### Option 2: Using ImageMagick

```bash
# Install ImageMagick
brew install imagemagick

# Convert SVG to PNG and resize
convert -background none icon-master.svg -resize 1024x1024 ios/AppIcon-1024.png
convert -background none icon-master.svg -resize 180x180 ios/AppIcon-180.png
# ... (repeat for all sizes)
```

### Option 3: Online Tool
Use [App Icon Generator](https://www.appicon.co/) or [MakeAppIcon](https://makeappicon.com/):
1. Upload `icon-master.svg` (or export to 1024x1024 PNG)
2. Download Android + iOS asset packages
3. Extract to respective folders

---

## 📁 Folder Structure

```
docs/design/app-icon/
├── README.md                 # This file
├── icon-master.svg          # Master design file
├── color-palette.md         # Detailed color specs
├── export-script.sh         # Automated export script
├── android/                 # Android exports
│   ├── ic_launcher-48.png
│   ├── ic_launcher-72.png
│   ├── ic_launcher-96.png
│   ├── ic_launcher-144.png
│   └── ic_launcher-192.png
└── ios/                     # iOS exports
    ├── AppIcon-1024.png
    ├── AppIcon-180.png
    ├── AppIcon-120.png
    ├── AppIcon-87.png
    ├── AppIcon-80.png
    ├── AppIcon-76.png
    ├── AppIcon-60.png
    ├── AppIcon-58.png
    ├── AppIcon-40.png
    ├── AppIcon-29.png
    └── AppIcon-20.png
```

---

## 🚀 Integration Guide

### Android Integration

**Step 1: Prepare Adaptive Icon Layers**

For Android adaptive icons, we need separate foreground and background:

1. **Background Layer:** Solid gradient or color
2. **Foreground Layer:** 3D cube + AR corners (transparent PNG)

**File locations:**
```
composeApp/src/androidMain/res/
├── mipmap-mdpi/ic_launcher.png
├── mipmap-hdpi/ic_launcher.png
├── mipmap-xhdpi/ic_launcher.png
├── mipmap-xxhdpi/ic_launcher.png
├── mipmap-xxxhdpi/ic_launcher.png
├── mipmap-anydpi-v26/ic_launcher.xml  # Adaptive icon descriptor
└── values/colors.xml                  # Background color
```

**Step 2: Create `ic_launcher.xml`**

```xml
<!-- composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher.xml -->
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
```

**Step 3: Update `colors.xml`**

```xml
<!-- composeApp/src/androidMain/res/values/colors.xml -->
<resources>
    <color name="ic_launcher_background">#667eea</color>
</resources>
```

### iOS Integration

**File location:**
```
iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/
├── Contents.json
├── AppIcon-1024.png
├── AppIcon-180.png
├── AppIcon-120.png
└── ... (all sizes)
```

**Update `Contents.json`:**

```json
{
  "images" : [
    {
      "filename" : "AppIcon-40.png",
      "idiom" : "ipad",
      "scale" : "1x",
      "size" : "20x20"
    },
    {
      "filename" : "AppIcon-40.png",
      "idiom" : "ipad",
      "scale" : "2x",
      "size" : "20x20"
    },
    ...
    {
      "filename" : "AppIcon-1024.png",
      "idiom" : "ios-marketing",
      "scale" : "1x",
      "size" : "1024x1024"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
```

---

## ✅ Design Checklist

- [x] AR theme representation (3D cube + scan corners)
- [x] Modern gradient background
- [x] Scalable vector format (SVG)
- [x] Clear at small sizes (20x20)
- [x] No fine details that blur when scaled
- [x] High contrast elements
- [x] Platform-appropriate corner radius
- [x] Color accessibility (WCAG AA compliant)
- [x] No text dependency (GLB badge is optional)
- [ ] Export all required sizes
- [ ] Test on actual devices
- [ ] Verify adaptive icon on Android
- [ ] Check App Store guidelines compliance

---

## 🎯 Next Steps

1. **Review Design:** Confirm visual concept with team
2. **Export Assets:** Run export script for all sizes
3. **Create Adaptive Layers:** Separate foreground/background for Android
4. **Integration:** Hand off to `main-developer-agent` for implementation
5. **Testing:** Verify on real devices (Android + iOS)
6. **Iteration:** Adjust based on feedback

---

## 📝 Notes

- **GLB Badge:** Can be removed for cleaner design if needed
- **Alternative Colors:** Can adjust gradient to match brand colors
- **Simplification:** For very small sizes (20x20), consider removing grid dots
- **Accessibility:** Ensure sufficient contrast for visibility

---

## 🔗 References

- [Android Adaptive Icons Guide](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
- [iOS Human Interface Guidelines - App Icons](https://developer.apple.com/design/human-interface-guidelines/app-icons)
- [Material Design - Product Icons](https://m3.material.io/styles/icons/designing-icons)
