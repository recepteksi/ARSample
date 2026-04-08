package com.trendhive.arsample.presentation.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [ModelPreviewThumbnailHelper].
 *
 * These tests cover:
 *  - iOS extension-routing logic (resolvePreviewStrategy)
 *  - Android initial-state determination (resolveInitialAndroidState)
 *  - Android post-load state determination (resolveAndroidStateAfterLoad)
 *  - Pure extension-extraction utility (extractExtension)
 *
 * @see ModelPreviewThumbnail          (expect declaration)
 * @see ModelPreviewThumbnailHelper    (subject under test)
 */
class ModelPreviewThumbnailHelperTest {

    // -------------------------------------------------------------------------
    // resolvePreviewStrategy — iOS routing
    // -------------------------------------------------------------------------

    @Test
    fun `resolvePreviewStrategy with usdz extension should return USDZ strategy`() {
        // GIVEN
        val modelPath = "/models/chair.usdz"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.USDZ, result)
    }

    @Test
    fun `resolvePreviewStrategy with glb extension should return GLB_THUMBNAIL strategy`() {
        // GIVEN
        val modelPath = "/models/chair.glb"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.GLB_THUMBNAIL, result)
    }

    @Test
    fun `resolvePreviewStrategy with gltf extension should return GLB_THUMBNAIL strategy`() {
        // GIVEN
        val modelPath = "/models/scene.gltf"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.GLB_THUMBNAIL, result)
    }

    @Test
    fun `resolvePreviewStrategy with unknown extension should return PLACEHOLDER strategy`() {
        // GIVEN
        val modelPath = "/models/asset.fbx"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.PLACEHOLDER, result)
    }

    @Test
    fun `resolvePreviewStrategy with no extension should return PLACEHOLDER strategy`() {
        // GIVEN
        val modelPath = "/models/noextension"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.PLACEHOLDER, result)
    }

    @Test
    fun `resolvePreviewStrategy with empty path should return PLACEHOLDER strategy`() {
        // GIVEN
        val modelPath = ""

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.PLACEHOLDER, result)
    }

    @Test
    fun `resolvePreviewStrategy with txt extension should return PLACEHOLDER strategy`() {
        // GIVEN
        val modelPath = "/models/readme.txt"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.PLACEHOLDER, result)
    }

    @Test
    fun `resolvePreviewStrategy with uppercase USDZ extension should return USDZ strategy`() {
        // GIVEN - extension casing should be normalised
        val modelPath = "/models/chair.USDZ"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.USDZ, result)
    }

    @Test
    fun `resolvePreviewStrategy with uppercase GLB extension should return GLB_THUMBNAIL strategy`() {
        // GIVEN
        val modelPath = "/models/chair.GLB"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.GLB_THUMBNAIL, result)
    }

    @Test
    fun `resolvePreviewStrategy with mixed-case GLTF extension should return GLB_THUMBNAIL strategy`() {
        // GIVEN
        val modelPath = "/models/scene.GlTf"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.GLB_THUMBNAIL, result)
    }

    @Test
    fun `resolvePreviewStrategy with path containing dots in directory name should use last segment`() {
        // GIVEN - a path with dots in intermediate directory names
        val modelPath = "/app/v1.2.3/models/chair.usdz"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN – the extension of the final filename is "usdz"
        assertEquals(PreviewStrategy.USDZ, result)
    }

    @Test
    fun `resolvePreviewStrategy with path containing dots in directory and glb file`() {
        // GIVEN
        val modelPath = "/app/v1.2.3/models/chair.glb"

        // WHEN
        val result = ModelPreviewThumbnailHelper.resolvePreviewStrategy(modelPath)

        // THEN
        assertEquals(PreviewStrategy.GLB_THUMBNAIL, result)
    }

    // -------------------------------------------------------------------------
    // resolveInitialAndroidState — Android file-existence gate
    // -------------------------------------------------------------------------

    @Test
    fun `resolveInitialAndroidState when file exists should return Loading`() {
        // GIVEN
        val fileExists = true

        // WHEN
        val state = ModelPreviewThumbnailHelper.resolveInitialAndroidState(fileExists)

        // THEN
        assertEquals(ThumbnailState.Loading, state)
    }

    @Test
    fun `resolveInitialAndroidState when file does not exist should return Error`() {
        // GIVEN
        val fileExists = false

        // WHEN
        val state = ModelPreviewThumbnailHelper.resolveInitialAndroidState(fileExists)

        // THEN
        assertEquals(ThumbnailState.Error, state)
    }

    // -------------------------------------------------------------------------
    // resolveAndroidStateAfterLoad — Android post-load state
    // -------------------------------------------------------------------------

    @Test
    fun `resolveAndroidStateAfterLoad when instance loaded successfully should return Loaded`() {
        // GIVEN
        val instanceLoaded = true

        // WHEN
        val state = ModelPreviewThumbnailHelper.resolveAndroidStateAfterLoad(instanceLoaded)

        // THEN
        assertEquals(ThumbnailState.Loaded, state)
    }

    @Test
    fun `resolveAndroidStateAfterLoad when instance is null should return Error`() {
        // GIVEN – createModelInstance returned null
        val instanceLoaded = false

        // WHEN
        val state = ModelPreviewThumbnailHelper.resolveAndroidStateAfterLoad(instanceLoaded)

        // THEN
        assertEquals(ThumbnailState.Error, state)
    }

    // -------------------------------------------------------------------------
    // State machine transitions — combined scenarios
    // -------------------------------------------------------------------------

    @Test
    fun `android happy path transitions from Loading to Loaded`() {
        // GIVEN - file exists (initial state = Loading)
        val initialState = ModelPreviewThumbnailHelper.resolveInitialAndroidState(fileExists = true)
        assertEquals(ThumbnailState.Loading, initialState)

        // WHEN - model loads successfully
        val finalState = ModelPreviewThumbnailHelper.resolveAndroidStateAfterLoad(instanceLoaded = true)

        // THEN
        assertEquals(ThumbnailState.Loaded, finalState)
    }

    @Test
    fun `android error path transitions directly to Error when file missing`() {
        // GIVEN + WHEN
        val state = ModelPreviewThumbnailHelper.resolveInitialAndroidState(fileExists = false)

        // THEN - Error without reaching Loading or Loaded
        assertEquals(ThumbnailState.Error, state)
    }

    @Test
    fun `android error path transitions from Loading to Error when createModelInstance returns null`() {
        // GIVEN - file exists
        val initialState = ModelPreviewThumbnailHelper.resolveInitialAndroidState(fileExists = true)
        assertEquals(ThumbnailState.Loading, initialState)

        // WHEN - model loader returns null
        val finalState = ModelPreviewThumbnailHelper.resolveAndroidStateAfterLoad(instanceLoaded = false)

        // THEN
        assertEquals(ThumbnailState.Error, finalState)
    }

    // -------------------------------------------------------------------------
    // extractExtension — utility
    // -------------------------------------------------------------------------

    @Test
    fun `extractExtension should return lowercase extension from simple path`() {
        assertEquals("glb", ModelPreviewThumbnailHelper.extractExtension("/models/chair.glb"))
    }

    @Test
    fun `extractExtension should normalise uppercase to lowercase`() {
        assertEquals("usdz", ModelPreviewThumbnailHelper.extractExtension("/models/chair.USDZ"))
    }

    @Test
    fun `extractExtension should return empty string when no extension present`() {
        assertEquals("", ModelPreviewThumbnailHelper.extractExtension("/models/noext"))
    }

    @Test
    fun `extractExtension should return empty string for empty path`() {
        assertEquals("", ModelPreviewThumbnailHelper.extractExtension(""))
    }

    @Test
    fun `extractExtension should return last segment extension when path contains multiple dots`() {
        assertEquals("glb", ModelPreviewThumbnailHelper.extractExtension("/app/v1.2/chair.glb"))
    }

    @Test
    fun `extractExtension should return empty string when path ends with a dot`() {
        // edge case: file named "chair."
        assertEquals("", ModelPreviewThumbnailHelper.extractExtension("/models/chair."))
    }
}
