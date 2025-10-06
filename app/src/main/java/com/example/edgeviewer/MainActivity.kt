package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: GLRenderer
    private lateinit var toggleButton: Button
    private lateinit var fpsText: TextView

    private lateinit var cameraExecutor: ExecutorService
    private val nativeProcessor = NativeProcessor()

    private var edgeDetectionEnabled = true
    private var inputMatAddr: Long = 0
    private var outputMatAddr: Long = 0

    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0.0

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glSurfaceView = findViewById(R.id.glSurfaceView)
        toggleButton = findViewById(R.id.toggleButton)
        fpsText = findViewById(R.id.fpsText)

        glSurfaceView.setEGLContextClientVersion(2)
        renderer = GLRenderer()
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        toggleButton.setOnClickListener {
            edgeDetectionEnabled = !edgeDetectionEnabled
            toggleButton.text = if (edgeDetectionEnabled) {
                "Mode: Edge Detection"
            } else {
                "Mode: Raw Camera"
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImage(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis
                )

                Log.d(TAG, "Camera started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        try {
            val width = imageProxy.width
            val height = imageProxy.height

            if (inputMatAddr == 0L) {
                inputMatAddr = nativeProcessor.createMat(width, height)
                outputMatAddr = nativeProcessor.createMat(width, height)
            }

            val yuvBytes = imageProxyToByteArray(imageProxy)
            nativeProcessor.convertYUVtoRGBA(yuvBytes, width, height, inputMatAddr)
            nativeProcessor.processFrame(inputMatAddr, outputMatAddr, edgeDetectionEnabled)

            val outputData = nativeProcessor.getMatData(outputMatAddr)

            if (outputData != null && outputData.isNotEmpty()) {
                // CRITICAL FIX: After 90° rotation, dimensions are swapped
                renderer.updateFrame(outputData, height, width)
            }

            updateFps()

        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun imageProxyToByteArray(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)

        val uvPixelStride = image.planes[1].pixelStride
        if (uvPixelStride == 1) {
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
        } else {
            var pos = ySize
            for (i in 0 until uSize step uvPixelStride) {
                nv21[pos++] = vBuffer.get(i)
                nv21[pos++] = uBuffer.get(i)
            }
        }

        return nv21
    }

    private fun updateFps() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastFpsTime

        if (elapsedTime >= 1000) {
            currentFps = (frameCount * 1000.0) / elapsedTime
            frameCount = 0
            lastFpsTime = currentTime

            runOnUiThread {
                fpsText.text = "FPS: %.1f".format(currentFps)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (inputMatAddr != 0L) {
            nativeProcessor.deleteMat(inputMatAddr)
        }
        if (outputMatAddr != 0L) {
            nativeProcessor.deleteMat(outputMatAddr)
        }

        cameraExecutor.shutdown()
    }
}