# Android Adaptive Icon Integration Guide

**Version:** 1.0  
**Platform:** Android 8.0+ (API 26+)  
**Date:** 2026-03-30

---

## 📋 Overview

Android adaptive icons consist of two layers:
1. **Background Layer:** Solid gradient (can be color resource or drawable)
2. **Foreground Layer:** 3D cube + AR corners (transparent PNG)

The system masks these layers into different shapes (circle, squircle, rounded square) based on device manufacturer.

---

## 📐 Specifications

### Layer Dimensions
- **Total Size:** 108dp x 108dp
- **Safe Zone:** 66dp diameter circle (center-weighted)
- **Maskable Area:** Varies by device (circle, squircle, rounded square)

### Export Sizes

| Density | Size (px) | Folder |
|---------|-----------|--------|
| mdpi | 108x108 | mipmap-mdpi |
| hdpi | 162x162 | mipmap-hdpi |
| xhdpi | 216x216 | mipmap-xhdpi |
| xxhdpi | 324x324 | mipmap-xxhdpi |
| xxxhdpi | 432x432 | mipmap-xxxhdpi |

---

## 🛠️ Export Instructions

### Step 1: Export Foreground Layer

```bash
cd docs/design/app-icon

# Export foreground with transparency
inkscape android-foreground.svg --export-filename=android/foreground-432.png -w 432 -h 432 --export-background-opacity=0.0
inkscape android-foreground.svg --export-filename=android/foreground-324.png -w 324 -h 324 --export-background-opacity=0.0
inkscape android-foreground.svg --export-filename=android/foreground-216.png -w 216 -h 216 --export-background-opacity=0.0
inkscape android-foreground.svg --export-filename=android/foreground-162.png -w 162 -h 162 --export-background-opacity=0.0
inkscape android-foreground.svg --export-filename=android/foreground-108.png -w 108 -h 108 --export-background-opacity=0.0
```

### Step 2: Background Layer Options

**Option A: Use Solid Color (Recommended)**
```xml
<!-- res/values/colors.xml -->
<color name="ic_launcher_background">#667eea</color>
```

**Option B: Use Gradient Drawable**
```xml
<!-- res/drawable/ic_launcher_background.xml -->
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:angle="135"
        android:startColor="#667eea"
        android:endColor="#764ba2"
        android:type="linear" />
</shape>
```

---

## 📁 File Structure

```
composeApp/src/androidMain/res/
├── drawable/
│   └── ic_launcher_background.xml          # Optional gradient
├── mipmap-mdpi/
│   ├── ic_launcher.png                     # Legacy icon (48x48)
│   └── ic_launcher_foreground.png          # Foreground (108x108)
├── mipmap-hdpi/
│   ├── ic_launcher.png                     # Legacy icon (72x72)
│   └── ic_launcher_foreground.png          # Foreground (162x162)
├── mipmap-xhdpi/
│   ├── ic_launcher.png                     # Legacy icon (96x96)
│   └── ic_launcher_foreground.png          # Foreground (216x216)
├── mipmap-xxhdpi/
│   ├── ic_launcher.png                     # Legacy icon (144x144)
│   └── ic_launcher_foreground.png          # Foreground (324x324)
├── mipmap-xxxhdpi/
│   ├── ic_launcher.png                     # Legacy icon (192x192)
│   └── ic_launcher_foreground.png          # Foreground (432x432)
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml                     # Adaptive icon descriptor
│   └── ic_launcher_round.xml               # Round icon descriptor
└── values/
    └── colors.xml                          # Background color
```

---

## 📝 XML Configuration

### 1. Adaptive Icon Descriptor

**File:** `res/mipmap-anydpi-v26/ic_launcher.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
```

### 2. Round Icon Descriptor

**File:** `res/mipmap-anydpi-v26/ic_launcher_round.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
```

### 3. Colors Resource

**File:** `res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- App Icon Background -->
    <color name="ic_launcher_background">#667eea</color>
    
    <!-- Optional: Material You dynamic color fallback -->
    <color name="ic_launcher_background_dynamic">@android:color/system_accent1_500</color>
</resources>
```

---

## 🎨 Material You Support (Android 12+)

### Dynamic Color Integration

For Android 12+ devices with Material You theming:

```xml
<!-- res/mipmap-anydpi-v31/ic_launcher.xml -->
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Use dynamic color for background -->
    <background android:drawable="@color/ic_launcher_background_dynamic"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <!-- Optional: Monochrome icon for themed icons -->
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
```

### Monochrome Icon (Themed Icons)

Create a simplified version for Android 13+ themed icons:

**File:** `res/drawable/ic_launcher_monochrome.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108"
    android:tint="@android:color/white">
    <!-- Simplified cube path -->
    <path
        android:fillColor="@android:color/white"
        android:pathData="M54,40 L71,50 L54,60 L37,50 Z M37,50 L54,60 L54,75 L37,65 Z M54,60 L71,50 L71,65 L54,75 Z"/>
</vector>
```

---

## 🧪 Testing Adaptive Icons

### Preview in Android Studio

1. Open `AndroidManifest.xml`
2. Find `<application android:icon="@mipmap/ic_launcher">`
3. Click icon preview in left gutter
4. Test different shapes: Circle, Squircle, Rounded Square

### Test on Device

```bash
# Build and install
./gradlew :composeApp:installDebug

# Check icon on home screen
# Long-press app → App info → Icon should display correctly
```

### Visual Test Checklist

- [ ] Circle mask: Icon content visible in safe zone
- [ ] Squircle mask: No content clipping
- [ ] Rounded square: Background gradient visible
- [ ] Legacy devices (< API 26): Standard icon displays
- [ ] Material You: Dynamic color applied (Android 12+)
- [ ] Themed icons: Monochrome variant displays (Android 13+)

---

## ⚙️ Manifest Configuration

### Application Tag

**File:** `composeApp/src/androidMain/AndroidManifest.xml`

```xml
<application
    android:name=".ARSampleApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:label="@string/app_name"
    android:theme="@style/Theme.ARSample"
    android:supportsRtl="true">
    
    <!-- Activities -->
    ...
</application>
```

---

## 📦 Complete Export Script

Save as `export-adaptive-icons.sh`:

```bash
#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FOREGROUND_SVG="$SCRIPT_DIR/android-foreground.svg"
ANDROID_DIR="$SCRIPT_DIR/android"

# Check Inkscape
if ! command -v inkscape &> /dev/null; then
    echo "❌ Inkscape required: brew install inkscape"
    exit 1
fi

mkdir -p "$ANDROID_DIR"

echo "🤖 Exporting Android Adaptive Icon Layers"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Export foreground layers
declare -a SIZES=(
    "432:xxxhdpi"
    "324:xxhdpi"
    "216:xhdpi"
    "162:hdpi"
    "108:mdpi"
)

for size_info in "${SIZES[@]}"; do
    IFS=':' read -r size density <<< "$size_info"
    output_file="$ANDROID_DIR/ic_launcher_foreground_${density}.png"
    
    echo "  • ic_launcher_foreground_${density}.png (${size}x${size})"
    inkscape "$FOREGROUND_SVG" \
        --export-filename="$output_file" \
        -w "$size" \
        -h "$size" \
        --export-background-opacity=0.0 \
        > /dev/null 2>&1
done

echo ""
echo "✅ Adaptive icon layers exported!"
echo ""
echo "📋 Next Steps:"
echo "  1. Copy PNG files to respective mipmap-* folders"
echo "  2. Create ic_launcher.xml in mipmap-anydpi-v26/"
echo "  3. Add ic_launcher_background color to values/colors.xml"
echo "  4. Update AndroidManifest.xml"
echo ""
```

---

## 🚀 Automated Integration

After exporting, run this to copy files to correct locations:

```bash
#!/bin/bash

# Copy foreground layers
cp android/ic_launcher_foreground_mdpi.png composeApp/src/androidMain/res/mipmap-mdpi/ic_launcher_foreground.png
cp android/ic_launcher_foreground_hdpi.png composeApp/src/androidMain/res/mipmap-hdpi/ic_launcher_foreground.png
cp android/ic_launcher_foreground_xhdpi.png composeApp/src/androidMain/res/mipmap-xhdpi/ic_launcher_foreground.png
cp android/ic_launcher_foreground_xxhdpi.png composeApp/src/androidMain/res/mipmap-xxhdpi/ic_launcher_foreground.png
cp android/ic_launcher_foreground_xxxhdpi.png composeApp/src/androidMain/res/mipmap-xxxhdpi/ic_launcher_foreground.png

echo "✅ Files copied to Android resource folders"
```

---

## 🔍 Troubleshooting

### Icon Not Showing

1. **Clean build:** `./gradlew clean`
2. **Rebuild:** `./gradlew :composeApp:assembleDebug`
3. **Clear launcher cache:** Settings → Apps → Launcher → Storage → Clear Cache
4. **Reinstall app:** `adb uninstall com.trendhive.arsample && ./gradlew installDebug`

### Icon Clipped

- **Issue:** Content outside 66dp safe zone
- **Fix:** Reduce foreground layer content size
- **Test:** Use Android Studio icon preview tool

### Wrong Colors

- **Issue:** Background color not applied
- **Fix:** Verify `@color/ic_launcher_background` exists in `values/colors.xml`
- **Check:** `res/mipmap-anydpi-v26/ic_launcher.xml` references correct drawable

---

## ✅ Integration Checklist

- [ ] Export foreground layers (all densities)
- [ ] Create `ic_launcher.xml` in `mipmap-anydpi-v26/`
- [ ] Create `ic_launcher_round.xml` in `mipmap-anydpi-v26/`
- [ ] Add `ic_launcher_background` color to `values/colors.xml`
- [ ] Update `AndroidManifest.xml` with icon references
- [ ] Copy legacy icons to `mipmap-*` folders
- [ ] Test on Android 8.0+ device
- [ ] Test on Android 12+ (Material You)
- [ ] Test on Android 13+ (Themed icons)
- [ ] Verify all shapes (circle, squircle, rounded square)

---

**Ready for main-developer-agent integration!** 🚀
