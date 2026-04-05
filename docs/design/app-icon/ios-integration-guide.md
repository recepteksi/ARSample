# iOS App Icon Integration Guide

**Version:** 1.0  
**Platform:** iOS 13.0+  
**Date:** 2026-03-30

---

## 📋 Overview

iOS app icons must meet specific requirements:
- **No transparency:** All pixels must be opaque
- **No alpha channel:** PNG must have fully opaque background
- **Pre-rendered:** iOS applies corner radius automatically
- **sRGB color space:** For consistent colors across devices

---

## 📐 Required Sizes

### iPhone

| Size | Scale | Purpose | Filename |
|------|-------|---------|----------|
| 180x180 | 3x | Home Screen (iPhone 14 Pro Max, 14 Pro, 13 Pro Max, etc.) | `AppIcon-180.png` |
| 120x120 | 2x | Home Screen (iPhone SE, 13 mini, etc.) | `AppIcon-120.png` |
| 87x87 | 3x | Settings | `AppIcon-87.png` |
| 80x80 | 2x | Spotlight | `AppIcon-80.png` |
| 60x60 | 2x | Spotlight (iPhone) | `AppIcon-60.png` |
| 58x58 | 2x | Settings | `AppIcon-58.png` |
| 40x40 | 2x | Notifications | `AppIcon-40.png` |
| 29x29 | 1x | Settings | `AppIcon-29.png` |
| 20x20 | 1x | Notifications | `AppIcon-20.png` |

### iPad

| Size | Scale | Purpose | Filename |
|------|-------|---------|----------|
| 152x152 | 2x | Home Screen | `AppIcon-152.png` |
| 76x76 | 1x | Home Screen | `AppIcon-76.png` |
| 80x80 | 2x | Spotlight | `AppIcon-80.png` |
| 40x40 | 1x | Spotlight | `AppIcon-40.png` |
| 58x58 | 2x | Settings | `AppIcon-58.png` |
| 29x29 | 1x | Settings | `AppIcon-29.png` |

### App Store

| Size | Purpose | Filename |
|------|---------|----------|
| 1024x1024 | App Store Connect | `AppIcon-1024.png` |

---

## 🛠️ Export Instructions

### Using Inkscape (Automated)

```bash
cd docs/design/app-icon

# Export all iOS sizes
./export-script.sh
```

This will create:
```
ios/
├── AppIcon-1024.png  # App Store
├── AppIcon-180.png   # iPhone 3x
├── AppIcon-120.png   # iPhone 2x
├── AppIcon-87.png    # iPhone 3x Settings
├── AppIcon-80.png    # Spotlight 2x
├── AppIcon-76.png    # iPad 1x
├── AppIcon-60.png    # iPhone 2x Spotlight
├── AppIcon-58.png    # Settings 2x
├── AppIcon-40.png    # Spotlight 1x
├── AppIcon-29.png    # Settings 1x
└── AppIcon-20.png    # Notifications 1x
```

### Manual Export (if needed)

```bash
# Single size export example
inkscape icon-master.svg \
    --export-filename=ios/AppIcon-180.png \
    -w 180 -h 180 \
    --export-background-opacity=1.0
```

---

## 📁 Asset Catalog Structure

### File Location

```
iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/
├── Contents.json
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

### Contents.json

**File:** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/Contents.json`

```json
{
  "images" : [
    {
      "filename" : "AppIcon-40.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "20x20"
    },
    {
      "filename" : "AppIcon-60.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "20x20"
    },
    {
      "filename" : "AppIcon-58.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "29x29"
    },
    {
      "filename" : "AppIcon-87.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "29x29"
    },
    {
      "filename" : "AppIcon-80.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "40x40"
    },
    {
      "filename" : "AppIcon-120.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "40x40"
    },
    {
      "filename" : "AppIcon-120.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "60x60"
    },
    {
      "filename" : "AppIcon-180.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "60x60"
    },
    {
      "filename" : "AppIcon-20.png",
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
    {
      "filename" : "AppIcon-29.png",
      "idiom" : "ipad",
      "scale" : "1x",
      "size" : "29x29"
    },
    {
      "filename" : "AppIcon-58.png",
      "idiom" : "ipad",
      "scale" : "2x",
      "size" : "29x29"
    },
    {
      "filename" : "AppIcon-40.png",
      "idiom" : "ipad",
      "scale" : "1x",
      "size" : "40x40"
    },
    {
      "filename" : "AppIcon-80.png",
      "idiom" : "ipad",
      "scale" : "2x",
      "size" : "40x40"
    },
    {
      "filename" : "AppIcon-76.png",
      "idiom" : "ipad",
      "scale" : "1x",
      "size" : "76x76"
    },
    {
      "filename" : "AppIcon-152.png",
      "idiom" : "ipad",
      "scale" : "2x",
      "size" : "76x76"
    },
    {
      "filename" : "AppIcon-167.png",
      "idiom" : "ipad",
      "scale" : "2x",
      "size" : "83.5x83.5"
    },
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

## 🎨 Dark Mode Considerations

### Current Design

The ARSample icon works well in both light and dark modes:
- **Light Mode:** Purple gradient stands out against light wallpapers
- **Dark Mode:** White 3D cube maintains high visibility

**No separate dark mode variant is needed.**

### Testing Dark Mode

1. Open iOS Settings → Display & Brightness → Dark
2. Return to home screen
3. Verify icon visibility against dark wallpaper
4. Check settings icon (small size) for clarity

---

## 📱 iOS 18+ Tinted Icons

### What Are Tinted Icons?

iOS 18 introduces the ability for users to tint all app icons to match their wallpaper theme.

### How to Support

Create a simplified monochrome version:

**File:** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/MonochromeIcon.png`

```bash
# Create monochrome version (single color on transparent background)
inkscape icon-master.svg \
    --export-filename=ios/MonochromeIcon-1024.png \
    -w 1024 -h 1024 \
    --export-id=cube-layer \
    --export-background-opacity=0.0
```

**Update Contents.json:**

```json
{
  "images": [
    ...
    {
      "filename": "MonochromeIcon-1024.png",
      "idiom": "universal",
      "platform": "ios",
      "scale": "1x",
      "size": "1024x1024",
      "appearance": "monochrome"
    }
  ]
}
```

---

## 🧪 Testing

### Xcode Asset Catalog Viewer

1. Open `iosApp/iosApp.xcworkspace` in Xcode
2. Navigate to `Assets.xcassets/AppIcon.appiconset`
3. Verify all sizes appear correctly
4. Check for warnings (missing sizes, alpha channels, wrong dimensions)

### Simulator Testing

```bash
# Build and run on iPhone simulator
cd iosApp
xcodebuild -workspace iosApp.xcworkspace \
    -scheme iosApp \
    -destination 'platform=iOS Simulator,name=iPhone 15 Pro' \
    build

# Check home screen icon
# Settings → General → iPhone Storage → ARSample (check icon)
```

### Device Testing

1. **Install via Xcode:**
   - Connect iPhone
   - Product → Run (⌘R)
   - Check home screen icon

2. **TestFlight:**
   - Upload to App Store Connect
   - Install via TestFlight
   - Verify App Store icon (1024x1024)

### Visual Checklist

- [ ] Home screen (180x180 @ 3x): Clear and recognizable
- [ ] Home screen (120x120 @ 2x): No pixelation
- [ ] Settings (87x87 @ 3x): Readable at small size
- [ ] Spotlight (80x80 @ 2x): AR elements visible
- [ ] Notifications (40x40 @ 2x): Cube still distinguishable
- [ ] App Store (1024x1024): Professional quality
- [ ] Dark mode: Good contrast
- [ ] Light mode: Good contrast
- [ ] No transparency warnings in Xcode
- [ ] No color profile warnings

---

## ⚙️ Info.plist Configuration

### CFBundleIcons

**File:** `iosApp/iosApp/Info.plist`

```xml
<key>CFBundleIcons</key>
<dict>
    <key>CFBundlePrimaryIcon</key>
    <dict>
        <key>CFBundleIconFiles</key>
        <array>
            <string>AppIcon</string>
        </array>
        <key>CFBundleIconName</key>
        <string>AppIcon</string>
    </dict>
</dict>
```

**Note:** This is usually auto-generated by Xcode. Only edit if custom setup needed.

---

## 🚀 Automated Copy Script

**File:** `docs/design/app-icon/copy-ios-icons.sh`

```bash
#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IOS_EXPORT_DIR="$SCRIPT_DIR/ios"
ASSET_CATALOG="../../iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"

# Check if asset catalog exists
if [ ! -d "$ASSET_CATALOG" ]; then
    echo "❌ Asset catalog not found: $ASSET_CATALOG"
    exit 1
fi

# Check if icons exist
if [ ! -d "$IOS_EXPORT_DIR" ]; then
    echo "❌ iOS icons not exported yet. Run ./export-script.sh first"
    exit 1
fi

echo "📱 Copying iOS app icons to asset catalog"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Copy all PNG files
cp "$IOS_EXPORT_DIR"/*.png "$ASSET_CATALOG/"

# Create or update Contents.json
cat > "$ASSET_CATALOG/Contents.json" << 'EOF'
{
  "images" : [
    {
      "filename" : "AppIcon-40.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "20x20"
    },
    {
      "filename" : "AppIcon-60.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "20x20"
    },
    {
      "filename" : "AppIcon-58.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "29x29"
    },
    {
      "filename" : "AppIcon-87.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "29x29"
    },
    {
      "filename" : "AppIcon-80.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "40x40"
    },
    {
      "filename" : "AppIcon-120.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "40x40"
    },
    {
      "filename" : "AppIcon-120.png",
      "idiom" : "iphone",
      "scale" : "2x",
      "size" : "60x60"
    },
    {
      "filename" : "AppIcon-180.png",
      "idiom" : "iphone",
      "scale" : "3x",
      "size" : "60x60"
    },
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
EOF

echo "✅ iOS app icons copied successfully!"
echo ""
echo "📋 Next Steps:"
echo "  1. Open iosApp/iosApp.xcworkspace in Xcode"
echo "  2. Navigate to Assets.xcassets/AppIcon.appiconset"
echo "  3. Verify all icons are present"
echo "  4. Build and test on simulator/device"
echo ""
```

---

## 🔍 Troubleshooting

### Icon Not Updating on Device

**Solution:**
```bash
# Delete app from device
# Clean build folder
cd iosApp
rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*
xcodebuild clean

# Rebuild and install
xcodebuild -workspace iosApp.xcworkspace -scheme iosApp build
```

### Xcode Warning: "Alpha channel"

**Issue:** PNG has transparency  
**Fix:** Re-export with `--export-background-opacity=1.0`

```bash
inkscape icon-master.svg \
    --export-filename=AppIcon-1024.png \
    -w 1024 -h 1024 \
    --export-background-opacity=1.0
```

### Xcode Warning: "Color profile"

**Issue:** Incorrect color space  
**Fix:** Convert to sRGB using ImageMagick

```bash
convert AppIcon-1024.png \
    -colorspace sRGB \
    -strip \
    AppIcon-1024-fixed.png
```

### Icon Blurry on Device

**Issue:** Wrong scale or size  
**Fix:** Verify filename matches Contents.json exactly

---

## ✅ Integration Checklist

- [ ] Export all iOS icon sizes (11 files)
- [ ] Verify no transparency (all opaque)
- [ ] Verify sRGB color space
- [ ] Copy to `AppIcon.appiconset/`
- [ ] Update/create `Contents.json`
- [ ] Build in Xcode (no warnings)
- [ ] Test on iPhone simulator
- [ ] Test on iPad simulator
- [ ] Test on real device
- [ ] Verify in Settings app (small icon)
- [ ] Verify in Spotlight search
- [ ] Test dark mode visibility
- [ ] Upload 1024x1024 to App Store Connect

---

## 📸 App Store Screenshots

When uploading to App Store Connect, the 1024x1024 icon will be used for:
- App Store listing
- Search results
- App details page
- Promotional materials

**Quality Requirements:**
- 72 DPI minimum (our export is 1024x1024, sufficient)
- RGB color space (sRGB)
- No transparency
- Sharp edges (vector export ensures this)

---

**Ready for main-developer-agent integration!** 🍎
