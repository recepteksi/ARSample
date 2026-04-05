# ARSample - UI/UX Design Guide

**Project:** ARSample - 3D Object Placement/Removal Application  
**Platform:** Kotlin Multiplatform (Android + iOS)  
**Architecture:** Clean Architecture + Domain-Driven Design (DDD)  
**Date:** 2026-03-30  
**Version:** 1.0

---

## Table of Contents

1. [Design Philosophy](#design-philosophy)
2. [Material Design 3 (Android)](#material-design-3-android)
3. [iOS Human Interface Guidelines](#ios-human-interface-guidelines)
4. [AR-Specific Design Principles](#ar-specific-design-principles)
5. [Color System](#color-system)
6. [Typography](#typography)
7. [Component Design Patterns](#component-design-patterns)
8. [AR Interaction Patterns](#ar-interaction-patterns)
9. [Spatial UI Guidelines](#spatial-ui-guidelines)
10. [Accessibility](#accessibility)
11. [Performance Considerations](#performance-considerations)

---

## Design Philosophy

### Core Principles

**1. AR-First Experience**
- AR interaction should feel natural and intuitive
- Minimize cognitive load during AR sessions
- Reduce attention switching between real and virtual content

**2. Platform Consistency**
- Follow Material Design 3 on Android
- Follow iOS Human Interface Guidelines on iOS
- Maintain brand identity across platforms

**3. Progressive Disclosure**
- Show only necessary UI elements during AR experience
- Use coaching overlays for first-time users
- Hide UI elements when user is focused on AR content

**4. Real-World Integration**
- Virtual objects should blend seamlessly with reality
- Use realistic lighting and shadows
- Respect physical space constraints

---

## Material Design 3 (Android)

### Key Features

**1. Dynamic Color**
- Material You: System-generated color schemes based on wallpaper
- Support for light and dark themes
- High contrast ratios for accessibility

**2. Adaptive Design**
- Support for different screen sizes (phones, tablets, foldables)
- Responsive layouts with breakpoints
- Window size classes: Compact, Medium, Expanded

**3. Components**
- Updated Material 3 components (Buttons, Cards, FABs, etc.)
- Rounded corners and softer shadows
- Large touch targets (minimum 48dp)

### Implementation

```kotlin
// Jetpack Compose Material 3
import androidx.compose.material3.*

@Composable
fun ARSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
```

### Android-Specific AR UI

**AR Session Controls**
- Floating Action Button (FAB) for primary action (Add Object)
- Bottom App Bar for secondary actions (Undo, Clear Scene)
- Top App Bar with close/back button

**Coaching Overlay**
- Material 3 Snackbar for quick tips
- Dialog for detailed instructions
- Animated illustrations for gestures

---

## iOS Human Interface Guidelines

### Key Features

**1. SF Symbols**
- Use SF Symbols for icons
- Consistent visual language across iOS
- Automatic weight and size adjustments

**2. Navigation Patterns**
- NavigationStack for hierarchical navigation
- Tab bar for top-level destinations
- Modal sheets for AR placement

**3. Design Tokens**
- System colors (systemBackground, label, etc.)
- Dynamic Type for text scaling
- Vibrancy effects for overlays

### Implementation

```swift
// SwiftUI iOS AR View
struct ARPlacementView: View {
    @State private var showingObjectPicker = false
    
    var body: some View {
        ZStack {
            ARViewContainer()
                .edgesIgnoringSafeArea(.all)
            
            VStack {
                // Top Controls
                HStack {
                    Button("Close") {
                        // Dismiss AR
                    }
                    .buttonStyle(.bordered)
                    .tint(.white)
                    
                    Spacer()
                    
                    Button("Clear") {
                        // Clear scene
                    }
                    .buttonStyle(.bordered)
                    .tint(.red)
                }
                .padding()
                
                Spacer()
                
                // Bottom Controls
                HStack {
                    Button {
                        showingObjectPicker.toggle()
                    } label: {
                        Label("Add Object", systemImage: "plus.circle.fill")
                            .font(.title2)
                    }
                    .controlSize(.large)
                    .buttonStyle(.borderedProminent)
                }
                .padding()
            }
        }
    }
}
```

### iOS-Specific AR Features

**Coaching Overlay**
- Use ARCoachingOverlayView for plane detection guidance
- System-provided animations and messages
- Automatic dismissal when tracking is ready

**Quick Look Integration**
- Preview USDZ models before placing
- AR Quick Look for instant AR experiences
- Share button for exporting scenes

---

## AR-Specific Design Principles

### Based on Nielsen Norman Group & Industry Best Practices

**1. Reduce Interaction Cost**
- Minimize explicit commands
- Use contextual information for actions
- Automate repetitive tasks

**2. Minimize Cognitive Load**
- Don't require users to remember part numbers or coordinates
- Display relevant information in AR overlay
- Use visual cues instead of text labels

**3. Combine Information Sources**
- Overlay virtual content on real-world objects
- Show measurements and metadata in-place
- Reduce attention switching

**4. Provide Immediate Feedback**
- Haptic feedback on object placement
- Visual confirmation (scale animation, glow effect)
- Audio cues for important events

**5. Support Error Recovery**
- Undo/Redo functionality
- Gesture-based object deletion
- Auto-save scene state

---

## Color System

### Android Material 3 Color Scheme

```kotlin
// Light Theme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),        // Deep Purple
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    
    secondary = Color(0xFF625B71),       // Gray Purple
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    
    tertiary = Color(0xFF7D5260),        // Brownish Red
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    
    scrim = Color(0xFF000000),
)

// Dark Theme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    
    scrim = Color(0xFF000000),
)
```

### iOS Color Palette

```swift
// System Colors (Dynamic - automatically adapt to light/dark mode)
extension Color {
    // Primary Colors
    static let arPrimary = Color("ARPrimary")          // Custom asset color
    static let arSecondary = Color("ARSecondary")
    
    // System Semantic Colors
    static let arBackground = Color(.systemBackground)
    static let arSecondaryBackground = Color(.secondarySystemBackground)
    static let arLabel = Color(.label)
    static let arSecondaryLabel = Color(.secondaryLabel)
    
    // AR-Specific
    static let arPlacementIndicator = Color.blue.opacity(0.6)
    static let arSelectedObject = Color.yellow.opacity(0.4)
    static let arGridOverlay = Color.white.opacity(0.3)
}
```

### AR Overlay Colors

**Reticle/Placement Indicator**
- Color: Semi-transparent blue or white
- Alpha: 0.5-0.7 for visibility
- Pulsating animation for depth perception

**Selected Object Highlight**
- Color: Yellow/Gold with glow effect
- Alpha: 0.3-0.5
- Edge outline with 2-3dp stroke

**Grid/Plane Visualization**
- Color: White or light blue
- Alpha: 0.2-0.4
- Fade out after 2-3 seconds

---

## Typography

### Android Material 3 Typography Scale

```kotlin
val AppTypography = Typography(
    // Display (Reserved for short, important text)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    
    // Headline (For high-emphasis text)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    
    // Title (For medium-emphasis text)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    
    // Body (For body text)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    
    // Label (For labels and buttons)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
```

### iOS Typography (San Francisco Font)

```swift
// SF Pro Text (Default system font)
extension Font {
    // Large Titles
    static let arLargeTitle = Font.largeTitle.weight(.bold)  // 34pt
    
    // Titles
    static let arTitle1 = Font.title.weight(.semibold)       // 28pt
    static let arTitle2 = Font.title2.weight(.semibold)      // 22pt
    static let arTitle3 = Font.title3.weight(.semibold)      // 20pt
    
    // Headlines
    static let arHeadline = Font.headline.weight(.semibold)  // 17pt
    
    // Body
    static let arBody = Font.body                            // 17pt
    static let arCallout = Font.callout                      // 16pt
    
    // Subheadline and Footnote
    static let arSubheadline = Font.subheadline              // 15pt
    static let arFootnote = Font.footnote                    // 13pt
    static let arCaption1 = Font.caption                     // 12pt
    static let arCaption2 = Font.caption2                    // 11pt
}

// Dynamic Type Support
Text("Object Name")
    .font(.arHeadline)
    .dynamicTypeSize(.large)  // Adjusts with user preferences
```

---

## Component Design Patterns

### 1. Object List (Library Screen)

**Android (Compose)**
```kotlin
@Composable
fun ObjectListScreen(
    objects: List<ARObject>,
    onObjectClick: (ARObject) -> Unit,
    onAddObject: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Objects") },
                actions = {
                    IconButton(onClick = onAddObject) {
                        Icon(Icons.Default.Add, "Add Object")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddObject,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Import Object") }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(objects) { obj ->
                ObjectCard(
                    object = obj,
                    onClick = { onObjectClick(obj) }
                )
            }
        }
    }
}

@Composable
fun ObjectCard(
    object: ARObject,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column {
            // Thumbnail
            AsyncImage(
                model = object.thumbnailUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
            
            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = object.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = object.fileSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

**iOS (SwiftUI)**
```swift
struct ObjectListView: View {
    @StateObject private var viewModel: ObjectListViewModel
    @State private var showingImportPicker = false
    
    let columns = [
        GridItem(.adaptive(minimum: 160), spacing: 16)
    ]
    
    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(viewModel.objects) { object in
                        ObjectCardView(object: object)
                            .onTapGesture {
                                viewModel.selectObject(object)
                            }
                    }
                }
                .padding()
            }
            .navigationTitle("AR Objects")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showingImportPicker.toggle()
                    } label: {
                        Label("Add", systemImage: "plus")
                    }
                }
            }
            .fileImporter(
                isPresented: $showingImportPicker,
                allowedContentTypes: [.usdz, .glb]
            ) { result in
                viewModel.importObject(from: result)
            }
        }
    }
}

struct ObjectCardView: View {
    let object: ARObject
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Thumbnail
            AsyncImage(url: object.thumbnailURL) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                ProgressView()
            }
            .frame(height: 160)
            .clipped()
            .cornerRadius(12)
            
            // Info
            VStack(alignment: .leading, spacing: 4) {
                Text(object.name)
                    .font(.headline)
                    .lineLimit(1)
                
                Text(object.fileSize)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}
```

### 2. AR Placement Screen

**Android (Compose + ARCore)**
```kotlin
@Composable
fun ARPlacementScreen(
    viewModel: ARPlacementViewModel,
    onClose: () -> Unit
) {
    val selectedObject by viewModel.selectedObject.collectAsState()
    val isPlacementMode by viewModel.isPlacementMode.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // ARCore SceneView
        AndroidView(
            factory = { context ->
                ARSceneView(context).apply {
                    // Configure ARCore session
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // UI Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top Controls
            TopControls(
                onClose = onClose,
                onClearScene = { viewModel.clearScene() }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Placement Instructions
            if (isPlacementMode) {
                PlacementInstructions(
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Bottom Controls
            BottomControls(
                onAddObject = { viewModel.enterPlacementMode() },
                onUndo = { viewModel.undo() },
                onSettings = { /* Show settings */ }
            )
        }
        
        // Coaching Overlay (First Time)
        if (viewModel.showCoaching) {
            CoachingOverlay(
                onDismiss = { viewModel.dismissCoaching() }
            )
        }
    }
}

@Composable
fun TopControls(
    onClose: () -> Unit,
    onClearScene: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Icon(Icons.Default.Close, "Close")
        }
        
        IconButton(
            onClick = onClearScene,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
            )
        ) {
            Icon(Icons.Default.Delete, "Clear Scene")
        }
    }
}

@Composable
fun BottomControls(
    onAddObject: () -> Unit,
    onUndo: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onUndo) {
            Icon(Icons.Default.Undo, "Undo")
        }
        
        FloatingActionButton(
            onClick = onAddObject,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.Add, "Add Object")
        }
        
        IconButton(onClick = onSettings) {
            Icon(Icons.Default.Settings, "Settings")
        }
    }
}
```

**iOS (SwiftUI + ARKit)**
```swift
struct ARPlacementView: View {
    @StateObject private var viewModel: ARPlacementViewModel
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        ZStack {
            // ARKit View
            ARViewContainer(viewModel: viewModel)
                .edgesIgnoringSafeArea(.all)
            
            // UI Overlay
            VStack {
                // Top Controls
                HStack {
                    Button {
                        dismiss()
                    } label: {
                        Label("Close", systemImage: "xmark.circle.fill")
                            .labelStyle(.iconOnly)
                            .font(.title2)
                            .foregroundStyle(.white)
                            .padding()
                            .background(.ultraThinMaterial)
                            .clipShape(Circle())
                    }
                    
                    Spacer()
                    
                    Button(role: .destructive) {
                        viewModel.clearScene()
                    } label: {
                        Label("Clear", systemImage: "trash.circle.fill")
                            .labelStyle(.iconOnly)
                            .font(.title2)
                            .foregroundStyle(.white)
                            .padding()
                            .background(.ultraThinMaterial)
                            .clipShape(Circle())
                    }
                }
                .padding()
                
                Spacer()
                
                // Placement Instructions
                if viewModel.isPlacementMode {
                    PlacementInstructionsView()
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                
                // Bottom Controls
                HStack(spacing: 24) {
                    Button {
                        viewModel.undo()
                    } label: {
                        Image(systemName: "arrow.uturn.backward.circle.fill")
                            .font(.title)
                            .foregroundStyle(.white)
                    }
                    .disabled(!viewModel.canUndo)
                    
                    Button {
                        viewModel.enterPlacementMode()
                    } label: {
                        Label("Add Object", systemImage: "plus.circle.fill")
                            .font(.title)
                            .padding()
                            .background(.blue)
                            .clipShape(Capsule())
                            .foregroundStyle(.white)
                    }
                    
                    Button {
                        // Show settings
                    } label: {
                        Image(systemName: "gearshape.circle.fill")
                            .font(.title)
                            .foregroundStyle(.white)
                    }
                }
                .padding()
            }
            
            // Coaching Overlay (ARKit built-in)
            if viewModel.showCoaching {
                ARCoachingOverlayView()
                    .transition(.opacity)
            }
        }
    }
}
```

### 3. Object Transform Controls

**Gesture-Based Manipulation**

```kotlin
// Android - ARCore Gesture Handling
fun setupGestures(sceneView: ARSceneView) {
    sceneView.setOnTapListener { hitResult, plane, motionEvent ->
        if (isPlacementMode) {
            placeObject(hitResult.hitPose)
        } else {
            selectObject(hitResult)
        }
    }
    
    sceneView.setOnPinchListener { scaleFactor ->
        selectedNode?.let { node ->
            val newScale = node.localScale * scaleFactor
            node.localScale = newScale.clamp(0.1f, 5.0f)
        }
    }
    
    sceneView.setOnRotateListener { rotation ->
        selectedNode?.let { node ->
            node.localRotation = Quaternion.fromAxisAngle(
                Vector3.up(),
                rotation.toDegrees()
            )
        }
    }
    
    sceneView.setOnLongPressListener { node ->
        showContextMenu(node) // Show delete/duplicate options
    }
}
```

```swift
// iOS - ARKit Gesture Handling
func setupGestures(for arView: ARView) {
    // Tap to Place
    let tapGesture = UITapGestureRecognizer(
        target: self,
        action: #selector(handleTap(_:))
    )
    arView.addGestureRecognizer(tapGesture)
    
    // Pinch to Scale
    let pinchGesture = UIPinchGestureRecognizer(
        target: self,
        action: #selector(handlePinch(_:))
    )
    arView.addGestureRecognizer(pinchGesture)
    
    // Rotate
    let rotateGesture = UIRotationGestureRecognizer(
        target: self,
        action: #selector(handleRotation(_:))
    )
    arView.addGestureRecognizer(rotateGesture)
    
    // Long Press for Context Menu
    let longPressGesture = UILongPressGestureRecognizer(
        target: self,
        action: #selector(handleLongPress(_:))
    )
    arView.addGestureRecognizer(longPressGesture)
}

@objc func handleTap(_ sender: UITapGestureRecognizer) {
    let location = sender.location(in: arView)
    
    if isPlacementMode {
        performRaycast(at: location) { result in
            placeObject(at: result.worldTransform)
        }
    } else {
        selectObject(at: location)
    }
}

@objc func handlePinch(_ sender: UIPinchGestureRecognizer) {
    guard let selectedEntity = selectedEntity else { return }
    
    let scale = Float(sender.scale)
    selectedEntity.setScale(
        SIMD3(repeating: scale).clamped(
            lowerBound: SIMD3(repeating: 0.1),
            upperBound: SIMD3(repeating: 5.0)
        ),
        relativeTo: selectedEntity.parent
    )
    
    if sender.state == .ended {
        sender.scale = 1.0
    }
}
```

---

## AR Interaction Patterns

### 1. Object Placement

**Workflow:**
1. User taps "Add Object" button
2. Object appears at screen center with placement indicator
3. User moves phone to position object
4. Tap to confirm placement
5. Haptic feedback + visual confirmation

**Visual Indicators:**
- **Reticle:** Circular indicator showing placement position
- **Shadow:** Projected shadow on detected plane
- **Grid:** Optional grid overlay for alignment

### 2. Object Selection

**Visual Feedback:**
- Outline glow around selected object
- Transform handles (scale, rotate icons)
- Bounding box visualization

**Interaction:**
- Tap to select
- Tap again to deselect
- Long press for context menu (Delete, Duplicate, Properties)

### 3. Object Manipulation

**Scale (Pinch Gesture):**
- Pinch in/out to scale
- Maintain aspect ratio
- Min scale: 0.1x, Max scale: 5.0x
- Haptic feedback at limits

**Rotate (Two-Finger Rotation):**
- Two-finger rotation gesture
- Snap to 15° increments (optional)
- Visual rotation indicator

**Move (Drag Gesture):**
- Touch and drag to reposition
- Stick to detected planes
- Visual feedback during drag

### 4. Object Deletion

**Methods:**
1. Long press → Context menu → Delete
2. Swipe gesture (left/right)
3. Drag to trash icon (appears on selection)

**Confirmation:**
- Brief animation (fade out + scale down)
- Haptic feedback
- Undo option (Snackbar/Toast)

---

## Spatial UI Guidelines

### 1. UI Placement in AR

**Top Zone (Status & Controls):**
- Close/Back button (top-left)
- Status indicators (tracking quality, battery)
- Settings/Options (top-right)

**Center Zone (AR Content):**
- Keep clear for AR experience
- Temporary overlays only (placement reticle)
- Coaching overlays (dismissible)

**Bottom Zone (Actions):**
- Primary action (FAB/Button)
- Secondary actions (Icon buttons)
- Object gallery (swipeable)

### 2. Depth & Hierarchy

**Z-Index Layers:**
1. AR Content (farthest)
2. AR Overlays (placement indicators)
3. UI Controls (floating buttons)
4. Modal Dialogs (nearest)

**Visual Depth Cues:**
- Shadows for UI elements
- Blur background for dialogs
- Semi-transparent overlays

### 3. Text in AR

**Avoid Floating Text:**
- Use billboards (always face camera)
- Anchor to objects
- Limit text length (5-7 words max)

**Readability:**
- High contrast (white text on dark background)
- Large font sizes (minimum 18sp/pt)
- Drop shadows or background plates

### 4. Animations

**Object Appearance:**
- Scale from 0 to target size (200-300ms)
- Ease-out interpolation
- Optional: Slight bounce effect

**Object Removal:**
- Scale down to 0 (200ms)
- Fade out simultaneously
- Ease-in interpolation

**UI Transitions:**
- Slide in/out for panels (300ms)
- Fade for overlays (200ms)
- Consistent easing curves

---

## Accessibility

### 1. Visual Accessibility

**Color Contrast:**
- WCAG AA compliance (4.5:1 for normal text)
- AAA for critical UI (7:1 contrast)
- Test with color blindness simulators

**Dynamic Type (iOS):**
- Support all Dynamic Type sizes
- Minimum touch target: 44x44pt
- Adjust layouts for large text

**Font Scaling (Android):**
- Support system font size (up to 200%)
- Use `sp` units for text
- Minimum touch target: 48x48dp

### 2. Haptic Feedback

**Object Placement:**
- Light impact on placement
- Medium impact on error

**Object Selection:**
- Selection feedback (light)
- Deletion confirmation (medium)

**Gesture Limits:**
- Notification feedback at scale/rotation limits

### 3. Voice Over / TalkBack

**Content Descriptions:**
- All buttons and icons
- AR object names and states
- Status messages

**Focus Order:**
- Logical reading order
- Skip decorative elements
- Group related controls

### 4. Reduced Motion

**Respect User Preferences:**
- Disable non-essential animations
- Use fade instead of slide
- Reduce parallax effects

```kotlin
// Android - Check Reduced Motion
fun isReduceMotionEnabled(context: Context): Boolean {
    val animator = context.getSystemService(Context.ACCESSIBILITY_SERVICE) 
        as AccessibilityManager
    return animator.isEnabled && 
           Settings.Global.getFloat(
               context.contentResolver,
               Settings.Global.TRANSITION_ANIMATION_SCALE,
               1f
           ) == 0f
}
```

```swift
// iOS - Check Reduced Motion
if UIAccessibility.isReduceMotionEnabled {
    // Use simpler animations
}
```

---

## Performance Considerations

### 1. UI Rendering

**Compose (Android):**
- Use `remember` for expensive calculations
- Avoid recomposition in AR loop
- Use `LaunchedEffect` for side effects

**SwiftUI (iOS):**
- Use `@State` and `@Binding` appropriately
- Avoid unnecessary `body` re-evaluations
- Use `EquatableView` for complex views

### 2. AR Session

**Frame Rate:**
- Target: 60 FPS
- Minimum: 30 FPS
- Monitor with GPU profiler

**Draw Calls:**
- Minimize state changes
- Batch rendering
- Use instancing for repeated objects

### 3. Memory Management

**Texture Memory:**
- Compress textures (ETC2 for Android, ASTC for iOS)
- Use mipmaps
- Release unused textures

**Model Caching:**
- Lazy loading
- LRU cache for models
- Thumbnail generation

---

## Design Checklist

### Before Implementation

- [ ] Design system defined (colors, typography)
- [ ] Component library documented
- [ ] Interaction patterns specified
- [ ] Accessibility requirements defined

### During Development

- [ ] Follow platform guidelines (Material 3 / HIG)
- [ ] Test on multiple screen sizes
- [ ] Implement haptic feedback
- [ ] Add loading states

### Before Release

- [ ] Accessibility audit (TalkBack/VoiceOver)
- [ ] Color contrast verification
- [ ] Performance testing (60 FPS)
- [ ] Localization support

---

## Resources

### Official Guidelines

- [Material Design 3](https://m3.material.io/)
- [iOS Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [ARCore Design Guidelines](https://developers.google.com/ar/design)
- [ARKit Design Resources](https://developer.apple.com/design/resources/)

### UX Research

- [Nielsen Norman Group - AR UX](https://www.nngroup.com/articles/augmented-reality-ux/)
- [Google Design - AR/VR Best Practices](https://design.google/library/ar-design-best-practices/)

### Tools

- [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
- [SF Symbols](https://developer.apple.com/sf-symbols/)
- [Figma Material 3 Kit](https://www.figma.com/community/file/1035203688168086460)

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-30  
**Maintained By:** Design & Analysis Team
