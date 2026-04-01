# Hit Testing Documentation

> Comprehensive documentation for AR hit testing implementation on Android (ARCore) and iOS (ARKit)

## 📚 Overview

Hit testing is the core mechanism for placing 3D objects in AR scenes. This documentation covers design decisions, implementation details, and platform-specific considerations.

## 📖 Documentation

| Document | Description | Audience |
|----------|-------------|----------|
| **[design.md](./design.md)** | High-level design and architecture decisions | Architects, Lead Developers |
| **[implementation.md](./implementation.md)** | Step-by-step implementation guide | Developers |
| **[quick-reference.md](./quick-reference.md)** | Quick reference and code snippets | All Developers |
| **[android-arcore-analysis.md](./android-arcore-analysis.md)** | ARCore-specific analysis and fixes | Android Developers |

## 🎯 Quick Start

### For Developers
1. Start with **[design.md](./design.md)** to understand the architecture
2. Follow **[implementation.md](./implementation.md)** for step-by-step guide
3. Use **[quick-reference.md](./quick-reference.md)** during development

### For Android Developers
1. Review **[android-arcore-analysis.md](./android-arcore-analysis.md)** for ARCore specifics
2. Check SceneView integration details
3. Refer to quick reference for code snippets

### For iOS Developers
1. Start with **[design.md](./design.md)** for shared architecture
2. Follow **[implementation.md](./implementation.md)** for ARKit specifics
3. See [../ios/](../../ios/) for iOS-specific documentation

## 🔑 Key Concepts

### Hit Testing
The process of detecting where a user taps on the screen and determining the corresponding 3D point in the AR scene.

### Plane Detection
Detecting horizontal and vertical surfaces in the real world for object placement.

### Anchor Management
Creating and managing AR anchors to maintain stable object positions in the scene.

## 🏗️ Architecture

```
User Tap → Hit Test → Plane Detection → Create Anchor → Place Object
```

### Platform-Specific Implementations

**Android (ARCore + SceneView):**
- Uses `ArSceneView` with built-in hit testing
- `onTapArPlane` listener for plane detection
- `Anchor` system for position stability

**iOS (ARKit + RealityKit):**
- Uses `ARView` with raycast queries
- `ARRaycastQuery` for hit testing
- `ARAnchor` system for position stability

## 📊 Related Documentation

- [Test Implementation Guide](../test-implementation-guide.md)
- [Code Review Checklist](../code-review-checklist.md)
- [iOS ARKit Documentation](../../ios/)
- [Technical Architecture](../../architecture/technical-analysis.md)

## 🔗 External Resources

- [ARCore Hit Testing](https://developers.google.com/ar/develop/java/hit-testing)
- [ARKit Raycast](https://developer.apple.com/documentation/arkit/arayrcastquery)
- [SceneView Documentation](https://github.com/SceneView/sceneview-android)

---

**Last Updated**: 2026-04-01
