package com.example.surfacerecorder

import android.graphics.Matrix
import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaRecorder
import android.opengl.GLES20
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import java.io.File

private const val VIDEO_FRAME_RATE = 30

class ViewRecorder {

    private val surface = MediaCodec.createPersistentInputSurface()
    private val mediaRecorder = MediaRecorder()
    private val videoQuality = VideoQuality.HD_720
    private var recordableView: View? = null
    private val matrix = Matrix()

    var recording = false
    private set

    val renderer = object : ViewTreeObserver.OnDrawListener {
        private val framesInterval = 1000 / VIDEO_FRAME_RATE
        private var previousRenderTime = 0L
        private var skipFrame = false

        override fun onDraw() {
            if (skipFrame) {
                return
            }
            val recordableView = recordableView ?: return

            val currentTime = System.currentTimeMillis()
            if (currentTime > previousRenderTime + framesInterval) {
                val canvas = surface.lockHardwareCanvas()
                canvas.drawARGB(255, 255, 255, 255)
                canvas.matrix = matrix
                skipFrame = true
                recordableView.draw(canvas)
                skipFrame = false
                surface.unlockCanvasAndPost(canvas)
                previousRenderTime = currentTime
            }
        }
    }

    fun setRecordingView(recordingView: View) {
        this.recordableView = recordingView

        if (!recordingView.isLaidOut) {
            throw IllegalStateException("View must be laid out")
        }

        val maxScale = Math.max(
            videoQuality.width.toFloat() / recordingView.width,
            videoQuality.height.toFloat() / recordingView.height
        )

        matrix.setScale(maxScale, maxScale)
    }

    fun prepare(outputFile: File) {
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setOutputFile(outputFile.absolutePath)

        mediaRecorder.setVideoSize(videoQuality.width, videoQuality.height)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)

        mediaRecorder.setVideoEncodingBitRate(videoQuality.bitrate)
        mediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE)
        mediaRecorder.setInputSurface(surface)

        try {
            mediaRecorder.prepare()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    fun start() {
        if (!recording) {
            recording = true
            mediaRecorder.start()
        }
    }

    fun stop() {
        if (recording) {
            mediaRecorder.stop()
            mediaRecorder.reset()
            recording = false
        }
    }


    enum class VideoQuality(
        val bitrate: Int,
        val width: Int,
        val height: Int
    ) {

        SD(2000000, 640, 360),

        HD_720(4000000, 1280, 720),

        HD_1080(10000000, 1920, 1080),

        SD_PORT(2000000, 360, 640),

        HD_720_PORT(4000000, 720, 1280),

        HD_1080_PORT(10000000, 1080, 1920)
    }
}