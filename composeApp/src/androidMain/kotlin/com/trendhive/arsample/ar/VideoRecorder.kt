package com.trendhive.arsample.ar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import io.github.sceneview.ar.ARSceneView
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Helper class for recording video from ARSceneView using MediaCodec and PixelCopy.
 * 
 * Uses Surface-based encoding approach:
 * 1. Create MediaCodec encoder with input Surface
 * 2. Capture frames from ARSceneView using PixelCopy
 * 3. Draw captured frames to encoder's input Surface
 * 4. Write encoded data to file using MediaMuxer
 * 
 * This approach is compatible with ARSceneView and provides good performance.
 */
class VideoRecorder(
    private val context: Context
) {
    companion object {
        private const val TAG = "VideoRecorder"
        
        // Default video settings
        private const val DEFAULT_VIDEO_WIDTH = 1280
        private const val DEFAULT_VIDEO_HEIGHT = 720
        private const val DEFAULT_VIDEO_BIT_RATE = 8_000_000 // 8 Mbps
        private const val DEFAULT_VIDEO_FRAME_RATE = 30
        private const val MIME_TYPE = "video/avc" // H.264
        private const val I_FRAME_INTERVAL = 1 // I-frame every 1 second
    }
    
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var inputSurface: Surface? = null
    private var trackIndex: Int = -1
    private var muxerStarted = false
    private val isRecording = AtomicBoolean(false)
    private var currentOutputPath: String? = null
    private var arSceneView: ARSceneView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Video dimensions - set when recording starts
    private var videoWidth = DEFAULT_VIDEO_WIDTH
    private var videoHeight = DEFAULT_VIDEO_HEIGHT
    
    /**
     * Set the ARSceneView to record from.
     */
    fun setARSceneView(view: ARSceneView?) {
        arSceneView = view
    }
    
    /**
     * Start video recording to the specified output path.
     * This method is thread-safe and can be called from any thread.
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
        
        // Use a latch to wait for main thread operations if we're not on main thread
        val result = AtomicBoolean(false)
        
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Already on main thread
            result.set(startRecordingInternal(outputPath, view))
        } else {
            // Run on main thread and wait for result
            val latch = CountDownLatch(1)
            mainHandler.post {
                try {
                    result.set(startRecordingInternal(outputPath, view))
                } finally {
                    latch.countDown()
                }
            }
            try {
                // Wait up to 5 seconds for recording to start
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Timeout waiting for recording to start")
                    return false
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Interrupted while starting recording", e)
                return false
            }
        }
        
        return result.get()
    }
    
    private fun startRecordingInternal(outputPath: String, view: ARSceneView): Boolean {
        return try {
            // Ensure parent directory exists
            File(outputPath).parentFile?.mkdirs()
            
            // Get view dimensions for recording, ensuring even numbers (required by H.264)
            videoWidth = ((if (view.width > 0) view.width else DEFAULT_VIDEO_WIDTH) / 2) * 2
            videoHeight = ((if (view.height > 0) view.height else DEFAULT_VIDEO_HEIGHT) / 2) * 2
            
            // Cap dimensions to reasonable limits to avoid memory issues
            if (videoWidth > 1920) {
                val scale = 1920f / videoWidth
                videoWidth = 1920
                videoHeight = ((videoHeight * scale).toInt() / 2) * 2
            }
            if (videoHeight > 1080) {
                val scale = 1080f / videoHeight
                videoHeight = 1080
                videoWidth = ((videoWidth * scale).toInt() / 2) * 2
            }
            
            Log.d(TAG, "Starting recording with dimensions: ${videoWidth}x${videoHeight}")
            
            // Create MediaCodec encoder
            val format = MediaFormat.createVideoFormat(MIME_TYPE, videoWidth, videoHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_VIDEO_BIT_RATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_VIDEO_FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            }
            
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                // Get the input surface before starting
                inputSurface = createInputSurface()
                start()
            }
            
            if (inputSurface == null || !inputSurface!!.isValid) {
                Log.e(TAG, "Failed to create valid input surface")
                releaseRecorder()
                return false
            }
            
            // Create MediaMuxer
            mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            trackIndex = -1
            muxerStarted = false
            
            isRecording.set(true)
            currentOutputPath = outputPath
            
            // Start frame capture
            startFrameCapture(view)
            
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
     * This method is thread-safe and can be called from any thread.
     * @return true if recording stopped successfully, false otherwise
     */
    fun stopRecording(): Boolean {
        if (!isRecording.get()) {
            Log.w(TAG, "Not recording")
            return false
        }
        
        // Mark recording as stopped first to stop frame capture
        isRecording.set(false)
        
        // Use a latch to wait for main thread operations if we're not on main thread
        val result = AtomicBoolean(false)
        
        if (Looper.myLooper() == Looper.getMainLooper()) {
            result.set(stopRecordingInternal())
        } else {
            val latch = CountDownLatch(1)
            mainHandler.post {
                try {
                    result.set(stopRecordingInternal())
                } finally {
                    latch.countDown()
                }
            }
            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Timeout waiting for recording to stop")
                    return false
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Interrupted while stopping recording", e)
                return false
            }
        }
        
        return result.get()
    }
    
    private fun stopRecordingInternal(): Boolean {
        return try {
            // Stop frame capture first
            stopFrameCapture()
            
            // Signal end of stream to encoder via surface
            try {
                mediaCodec?.signalEndOfInputStream()
            } catch (e: Exception) {
                Log.w(TAG, "Error signaling end of input stream", e)
            }
            
            // Drain remaining encoded data
            drainEncoder(true)
            
            // Stop muxer
            if (muxerStarted) {
                try {
                    mediaMuxer?.stop()
                } catch (e: Exception) {
                    Log.w(TAG, "Error stopping muxer", e)
                }
            }
            
            val path = currentOutputPath
            currentOutputPath = null
            
            Log.d(TAG, "Stopped recording: $path")
            
            releaseRecorder()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
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
    
    private fun releaseRecorder() {
        try {
            inputSurface?.release()
            inputSurface = null
            
            mediaCodec?.stop()
            mediaCodec?.release()
            mediaCodec = null
            
            mediaMuxer?.release()
            mediaMuxer = null
            
            trackIndex = -1
            muxerStarted = false
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recorder", e)
        }
    }
    
    // Frame capture thread for recording
    private var captureThread: Thread? = null
    @Volatile private var captureStopped = false
    
    private fun startFrameCapture(view: ARSceneView) {
        captureStopped = false
        
        captureThread = Thread {
            val frameIntervalMs = 1000L / DEFAULT_VIDEO_FRAME_RATE
            
            while (!captureStopped && isRecording.get()) {
                try {
                    // Capture frame and draw to encoder surface
                    captureAndDrawFrame(view)
                    
                    // Drain encoded data to muxer
                    drainEncoder(false)
                    
                    Thread.sleep(frameIntervalMs)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Frame capture error", e)
                }
            }
            Log.d(TAG, "Frame capture thread stopped")
        }.apply {
            name = "VideoRecorder-FrameCapture"
            start()
        }
    }
    
    private fun stopFrameCapture() {
        captureStopped = true
        captureThread?.interrupt()
        try {
            captureThread?.join(2000)
        } catch (e: InterruptedException) {
            // Ignore
        }
        captureThread = null
    }
    
    private fun captureAndDrawFrame(view: ARSceneView) {
        val surface = inputSurface ?: return
        if (!surface.isValid) return
        
        try {
            // Create bitmap for capture (use view's actual dimensions for PixelCopy)
            val viewWidth = view.width.coerceAtLeast(1)
            val viewHeight = view.height.coerceAtLeast(1)
            
            val bitmap = Bitmap.createBitmap(
                viewWidth,
                viewHeight,
                Bitmap.Config.ARGB_8888
            )
            
            val latch = CountDownLatch(1)
            var copySuccess = false
            
            mainHandler.post {
                try {
                    PixelCopy.request(
                        view,
                        bitmap,
                        { result ->
                            copySuccess = result == PixelCopy.SUCCESS
                            latch.countDown()
                        },
                        mainHandler
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "PixelCopy request failed", e)
                    latch.countDown()
                }
            }
            
            // Wait for PixelCopy with timeout
            if (!latch.await(100, TimeUnit.MILLISECONDS) || !copySuccess) {
                bitmap.recycle()
                return
            }
            
            // Draw bitmap to encoder's input surface
            val canvas: Canvas? = surface.lockCanvas(null)
            if (canvas != null) {
                try {
                    // Scale bitmap to match video dimensions if needed
                    if (viewWidth != videoWidth || viewHeight != videoHeight) {
                        val scaleX = videoWidth.toFloat() / viewWidth
                        val scaleY = videoHeight.toFloat() / viewHeight
                        val matrix = Matrix().apply {
                            setScale(scaleX, scaleY)
                        }
                        canvas.drawBitmap(bitmap, matrix, null)
                    } else {
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                    }
                } finally {
                    surface.unlockCanvasAndPost(canvas)
                }
            }
            
            bitmap.recycle()
        } catch (e: Exception) {
            // Ignore frame capture errors - they're common during transitions
        }
    }
    
    private fun drainEncoder(endOfStream: Boolean) {
        val codec = mediaCodec ?: return
        val muxer = mediaMuxer ?: return
        
        val bufferInfo = MediaCodec.BufferInfo()
        val timeoutUs = if (endOfStream) 10000L else 0L
        
        while (true) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            
            when {
                outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                    // Keep trying when ending stream, but with a limit
                }
                outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) {
                        Log.w(TAG, "Format changed after muxer started")
                    } else {
                        val newFormat = codec.outputFormat
                        Log.d(TAG, "Encoder output format changed: $newFormat")
                        trackIndex = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                }
                outputBufferIndex >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                    
                    if (outputBuffer != null && muxerStarted) {
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            bufferInfo.size = 0
                        }
                        
                        if (bufferInfo.size > 0) {
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                        }
                    }
                    
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
                else -> break
            }
        }
    }
}
