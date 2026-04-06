#!/bin/bash

# ARSample App Icon Export Script
# Version: 1.0
# Requires: Inkscape (brew install inkscape)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SVG_FILE="$SCRIPT_DIR/icon-master.svg"
ANDROID_DIR="$SCRIPT_DIR/android"
IOS_DIR="$SCRIPT_DIR/ios"

# Check if Inkscape is installed
if ! command -v inkscape &> /dev/null; then
    echo "❌ Inkscape is not installed."
    echo "Install it with: brew install inkscape"
    exit 1
fi

# Check if SVG file exists
if [ ! -f "$SVG_FILE" ]; then
    echo "❌ SVG file not found: $SVG_FILE"
    exit 1
fi

# Create output directories
mkdir -p "$ANDROID_DIR"
mkdir -p "$IOS_DIR"

echo "🎨 ARSample App Icon Export"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Export iOS sizes
echo "📱 Exporting iOS icons..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

declare -a IOS_SIZES=(
    "1024:AppIcon-1024"
    "180:AppIcon-180"
    "120:AppIcon-120"
    "87:AppIcon-87"
    "80:AppIcon-80"
    "76:AppIcon-76"
    "60:AppIcon-60"
    "58:AppIcon-58"
    "40:AppIcon-40"
    "29:AppIcon-29"
    "20:AppIcon-20"
)

for size_info in "${IOS_SIZES[@]}"; do
    IFS=':' read -r size name <<< "$size_info"
    output_file="$IOS_DIR/${name}.png"
    
    echo "  • ${name}.png (${size}x${size})"
    inkscape "$SVG_FILE" \
        --export-filename="$output_file" \
        -w "$size" \
        -h "$size" \
        --export-background-opacity=1.0 \
        > /dev/null 2>&1
done

echo ""
echo "🤖 Exporting Android icons..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

declare -a ANDROID_SIZES=(
    "192:xxxhdpi"
    "144:xxhdpi"
    "96:xhdpi"
    "72:hdpi"
    "48:mdpi"
)

for size_info in "${ANDROID_SIZES[@]}"; do
    IFS=':' read -r size density <<< "$size_info"
    output_file="$ANDROID_DIR/ic_launcher-${size}.png"
    
    echo "  • ic_launcher-${size}.png (${density})"
    inkscape "$SVG_FILE" \
        --export-filename="$output_file" \
        -w "$size" \
        -h "$size" \
        --export-background-opacity=1.0 \
        > /dev/null 2>&1
done

echo ""
echo "✅ Export Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📊 Summary:"
echo "  iOS icons:     11 files in $IOS_DIR"
echo "  Android icons: 5 files in $ANDROID_DIR"
echo ""
echo "🚀 Next Steps:"
echo "  1. Review exported icons in Finder"
echo "  2. Create Android adaptive icon layers (foreground + background)"
echo "  3. Run: main-developer-agent to integrate into project"
echo ""
echo "💡 Tips:"
echo "  • Test icons on actual devices"
echo "  • Verify adaptive icon on Android 8.0+"
echo "  • Check App Store screenshot with 1024x1024"
echo ""
