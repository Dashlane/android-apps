package com.dashlane.barcodescanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Surface.ROTATION_0
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModelProvider
import com.dashlane.permission.PermissionsManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.computeStatusBarColor
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.statusBarColor
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
@ExperimentalGetImage
class BarCodeCaptureActivity : DashlaneActivity() {

    private lateinit var previewView: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraSelector: CameraSelector
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null

    private val screenAspectRatio: Int
        get() {
            
            val (width, height) = getMetrics()
            return aspectRatio(width, height)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)
        statusBarColor = computeStatusBarColor(getThemeAttrColor(R.attr.colorPrimary))
        setupCamera()
        (findViewById<View>(R.id.toolbar) as Toolbar).setNavigationOnClickListener { finish() }
        val prompt = intent.getStringExtra(PROMPT)
        val header = intent.getStringExtra(HEADER)
        if (header.isNullOrEmpty() && prompt.isNullOrEmpty()) {
            findViewById<View>(R.id.header).visibility = View.GONE
            return
        }
        if (prompt.isNullOrEmpty()) {
            findViewById<View>(R.id.scan_prompt_label).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.scan_prompt_label).text = prompt
        }
        if (header.isNullOrEmpty()) {
            findViewById<View>(R.id.scan_header_label).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.scan_header_label).text = header
        }
    }

    private fun setupCamera() {
        previewView = findViewById(R.id.preview_view)
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CameraXViewModel::class.java]
            .processCameraProvider
            .observe(this) { provider: ProcessCameraProvider? ->
                cameraProvider = provider
                if (permissionsManager.isAllowed(Manifest.permission.CAMERA)) {
                    bindCameraUseCases()
                } else {
                    permissionsManager.requestPermission(
                        this,
                        PermissionsManager.PERMISSION_CAMERA,
                        object : PermissionsManager.OnPermissionResponseHandler {
                            override fun onApproval() {
                                bindCameraUseCases()
                            }

                            override fun onAlwaysDisapproved() {
                                
                                finish()
                            }

                            override fun onDisapproval() {
                                
                                finish()
                            }
                        },
                        Manifest.permission.CAMERA
                    )
                }
            }
    }

    private fun bindCameraUseCases() {
        bindPreviewUseCase()
        val format = intent.getIntExtra(BARCODE_FORMAT, Barcode.FORMAT_ALL_FORMATS)
        bindAnalyseUseCase(format)
    }

    private fun bindPreviewUseCase() {
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        previewUseCase = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView.display?.rotation ?: ROTATION_0)
            .build()
        previewUseCase!!.setSurfaceProvider(previewView.surfaceProvider)

        try {
                this,
                cameraSelector,
                previewUseCase
            )
        } catch (illegalStateException: IllegalStateException) {
            finish()
        } catch (illegalArgumentException: IllegalArgumentException) {
            finish()
        }
    }

    private fun bindAnalyseUseCase(barCodeFormat: Int) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(barCodeFormat)
            .build()
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)

        if (cameraProvider == null) {
            finish()
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView.display?.rotation ?: ROTATION_0)
            .build()

        
        val cameraExecutor = Executors.newSingleThreadExecutor()

        analysisUseCase?.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(barcodeScanner, imageProxy)
        }

        try {
                this,
                cameraSelector,
                analysisUseCase
            )
        } catch (illegalStateException: IllegalStateException) {
            finish()
        } catch (illegalArgumentException: IllegalArgumentException) {
            finish()
        }
    }

    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val inputImage = createInputImage(imageProxy)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isEmpty()) return@addOnSuccessListener
                cameraProvider!!.unbind(previewUseCase)
                val codes = barcodes.mapNotNull {
                    it.rawValue
                }
                setResult(
                    Activity.RESULT_OK,
                    Intent().apply {
                    putExtra(RESULT_EXTRA_BARCODE_VALUES, codes.toTypedArray())
                }
                )
                finish()
            }
            .addOnFailureListener {
                finish()
            }.addOnCompleteListener {
                
                
                
                imageProxy.close()
            }
    }

    private fun createInputImage(imageProxy: ImageProxy) =
        InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    @Suppress("DEPRECATION")
    private fun getMetrics(): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            val metrics = DisplayMetrics().also { previewView.display?.getRealMetrics(it) }
            metrics.widthPixels to metrics.heightPixels
        }
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        const val RESULT_EXTRA_BARCODE_VALUES = "extra_barcode_values"
        const val BARCODE_FORMAT = "extra_barcode_format"
        const val HEADER = "extra_header"
        const val PROMPT = "extra_prompt"
    }
}
