package com.example.surfacerecorder

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

private const val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0

class MainActivity : AppCompatActivity() {

    private val viewRecorder = ViewRecorder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onStartRecording(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startViewRecording()
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun startViewRecording() {
        if (viewRecorder.recording) {
            return
        }

        val outputFile =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/recordViewTest.mp4")
        if (outputFile.exists()) {
            outputFile.delete()
        }

        drawingContainer.viewTreeObserver.addOnDrawListener {
            viewRecorder.renderer.onDraw()
        }

        viewRecorder.setRecordingView(drawingContainer)
        viewRecorder.prepare(outputFile)
        viewRecorder.start()
    }

    fun onStopRecording(v: View) {
        viewRecorder.stop()
        recordableView.drawListener = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startViewRecording()
            }
        }
    }
}
