package com.trendhive.arsample.ar

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.view.Surface
import io.github.sceneview.ar.ARSceneView
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Helper class for recording video from ARSceneView using MediaRecorder.
 * 
 * Uses surface-based recording approach:
 * 1. MediaRecorder creates a Surface
 * 2. Frames from ARSceneView are rendered to this surface
 * 3. MediaRecorder encodes and saves the video
 */
class VideoRecorder(
    private val context: Context
) {
    companion object {
        private const val TAG = "VideoRecorder"
        
        // Default video settings
        private const val DEFAULT_VIDEO_WIDTH = 1920
        private const val DEFAULT_VIDEO_HEIGHT = 1080
        private const val DEFAULT_VIDEO_BIT_RATE = 10_000_000 // 10 Mbps
        private const val DEFAULT_VIDEO_FRAME_RATE = 30
    }
    
    private var mediaRecorder: MediaRecorder? = null
    private var recordingSurface: Surface? = null
    private val isRecording = AtomicBoolean(false)
    private var currentOutputPath: String? = null
    private var arSceneView: ARSceneView? = null
    
    /**
     * Set the ARSceneView to record from.
     */
    fun setARSceneView(view: ARSceneView?) {
        arSceneView = view
    }
    
    /**
     * Start video recording to the specified output path.
     * @param outputPath The full path where the video file will be saved
     * @return true if recording started successfully, false otherwise
     */
    fun startRecording(outputPath: String): Boolean {
        if (isRecording.get()) {
            Log.w(TAG, "Already recording")
            return false
        }
        
        val view = arSceneView
        if (view == null) {
            Log.e(TAG, "ARSceneView not set")
            return false
        }
        
        return try {
            // Ensure parent directory exists
            File(outputPath).parentFile?.mkdirs()
            
            // Get view dimensions for recording
            val width = if (view.width > 0) view.width else DEFAULT_VIDEO_WIDTH
            val height = if (view.height > 0) view.height else DEFAULT_VIDEO_HEIGHT
            
            // Create and configure MediaRecorder
            mediaRecorder = createMediaRecorder(outputPath, width, height)
            
            // Get the surface from MediaRecorder
            recordingSurface = mediaRecorder?.surface
            
            if (recordingSurface == null) {
                Log.e(TAG, "Failed to get recording surface from MediaRecorder")
                releaseRecorder()
                return false
            }
            
            // Start recording
            mediaRecorder?.start()
            
            // Start rendering to the recording surface
            // Note: SceneView doesn't have a direct API for this, so we use PixelCopy approach
            // or rely on the view's built-in recording capabilities
            startFrameCapture(view)
            
            isRecording.set(true)
            currentOutputPath = outputPath
            
            Log.d(TAG, "Started recording to: $outputPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            releaseRecorder()
            false
        }
    }
    
    /**
     * Stop video recording.
     * @return true if recording stopped successfully, false otherwise
     */
    fun stopRecording(): Boolean {
        if (!isRecording.get()) {
            Log.w(TAG, "Not recording")
            return false
        }
        
        return try {
            // Stop frame capture first
            stopFrameCapture()
            
            // Stop and release MediaRecorder
            mediaRecorder?.apply {
                stop()
                reset()
            }
            
            isRecording.set(false)
            
            val path = currentOutputPath
            currentOutputPath = null
            
            Log.d(TAG, "Stopped recording: $path")
            
            releaseRecorder()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            isRecording.set(false)
            releaseRecorder()
            false
        }
    }
    
    /**
     * Check if currently recording.
     */
    fun isRecording(): Boolean = isRecording.get()
    
    /**
     * Release all resources.
     */
    fun release() {
        if (isRecording.get()) {
            stopRecording()
        }
        releaseRecorder()
        arSceneView = null
    }
    
    @Suppress("DEPRECATION")
    private fun createMediaRecorder(outputPath: String, width: Int, height: Int): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(width, height)
            setVideoFrameRate(DEFAULT_VIDEO_FRAME_RATE)
            setVideoEncodingBitRate(DEFAULT_VIDEO_BIT_RATE)
            setOutputFile(outputPath)
            prepare()
        }
    }
    
    private fun releaseRecorder() {
        try {
            recordingSurface?.release()
            recordingSurface = null
            
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recorder", e)
        }
    }
    
    // Frame capture thread for recording
    private var captureThread: Thread? = null
    @Volatile private var captureStopped = false
    
    private fun startFrameCapture(view: ARSceneView) {
        captureStopped = false
        val surface = recordingSurface ?: return
        
        captureThread = Thread {
            val frameIntervalMs = 1000L / DEFAULT_VIDEO_FRAME_RATE
            
            while (!captureStopped && isRecording.get()) {
                try {
                    // Use PixelCopy to capture frame and draw to recording surface
                    captureFrameToSurface(view, surface)
                    Thread.sleep(frameIntervalMs)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Frame capture error", e)
                }
            }
        }.apply {
            name = "VideoRecorder-FrameCapture"
            start()
        }
    }
    
    private fun stopFrameCapture() {
        captureStopped = true
        captureThread?.interrupt()
        try {
            captureThread?.join(1000)
        } catch (e: InterruptedException) {
            // Ignore
        }
        captureThread = null
    }
    
    private fun captureFrameToSurface(view: ARSceneView, surface: Surface) {
        // This is a simplified approach. In practice, we'd need to:
        // 1. Use OpenGL to render to both the screen and the recording surface
        // 2. Or use VirtualDisplay / MediaProjection
        // 
        // For now, we use a PixelCopy-based approach which captures the rendered view
        try {
            if (!surface.isValid) return
            
            val bitmap = android.graphics.Bitmap.createBitmap(
                view.width.coerceAtLeast(1),
                view.height.coerceAtLeast(1),
                android.graphics.Bitmap.Config.ARGB_8888
            )
            
            val latch = java.util.concurrent.CountDownLatch(1)
            var copySuccess = false
            
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    android.view.PixelCopy.request(
                        view,
                        bitmap,
                        { result ->
                            copySuccess = result == android.view.PixelCopy.SUCCESS
                            latch.countDown()
                        },
                        android.os.Handler(android.os.Looper.getMainLooper())
                    )
                } catch (e: Exception) {
                    latch.countDown()
                }
            }
            
            // Wait for PixelCopy with timeout
            latch.await(100, java.util.concurrent.TimeUnit.MILLISECONDS)
            
            if (copySuccess && surface.isValid) {
                val canvas = surface.lockCanvas(null)
                try {
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                } finally {
                    surface.unlockCanvasAndPost(canvas)
                }
            }
            
            bitmap.recycle()
        } catch (e: Exception) {
            // Ignore frame capture errors - they're common during transitions
        }
    }
}
