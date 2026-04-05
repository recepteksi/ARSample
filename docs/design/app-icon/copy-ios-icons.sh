#!/bin/bash

# iOS App Icon Copy Script
# Copies exported icons to Xcode asset catalog

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IOS_EXPORT_DIR="$SCRIPT_DIR/ios"
ASSET_CATALOG="$SCRIPT_DIR/../../iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"

# Check if asset catalog exists
if [ ! -d "$ASSET_CATALOG" ]; then
    echo "❌ Asset catalog not found: $ASSET_CATALOG"
    echo "Creating asset catalog directory..."
    mkdir -p "$ASSET_CATALOG"
fi

# Check if icons exist
if [ ! -d "$IOS_EXPORT_DIR" ] || [ -z "$(ls -A $IOS_EXPORT_DIR 2>/dev/null)" ]; then
    echo "❌ iOS icons not exported yet. Run ./export-script.sh first"
    exit 1
fi

echo "📱 Copying iOS App Icons to Asset Catalog"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Copy all PNG files
echo "Copying icon files..."
cp "$IOS_EXPORT_DIR"/*.png "$ASSET_CATALOG/" 2>/dev/null || {
    echo "❌ Failed to copy icon files"
    exit 1
}

# Create or update Contents.json
echo "Creating Contents.json..."
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

echo ""
echo "✅ iOS App Icons Copied Successfully!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📊 Asset Catalog Location:"
echo "  $ASSET_CATALOG"
echo ""
echo "📋 Next Steps:"
echo "  1. Open iosApp/iosApp.xcworkspace in Xcode"
echo "  2. Navigate to Assets.xcassets/AppIcon.appiconset"
echo "  3. Verify all icons are present (no warnings)"
echo "  4. Build and test: Product → Run (⌘R)"
echo "  5. Check home screen icon on device/simulator"
echo ""
echo "💡 Testing:"
echo "  • Home screen: Check icon appearance"
echo "  • Settings → General → ARSample: Verify small icon"
echo "  • Spotlight search: Check search result icon"
echo "  • Dark mode: Toggle and verify visibility"
echo ""
