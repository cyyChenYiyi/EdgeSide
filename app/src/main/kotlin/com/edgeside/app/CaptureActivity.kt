package com.edgeside.app

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Transparent activity that drives the MediaProjection screen-capture flow:
 *  1. Launch the system "Allow EdgeSide to capture the screen?" prompt.
 *  2. On grant: spin up a VirtualDisplay + ImageReader, grab one frame.
 *  3. Save the bitmap to Pictures/EdgeSide/ (or MediaStore on Q+).
 *  4. Toast confirmation + self-finish.
 *
 * Must be an Activity because MediaProjection consent MUST come from an
 * Activity-started intent. The activity itself stays invisible to the user.
 */
class CaptureActivity : ComponentActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var captureLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Stay invisible.
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setLayout(1, 1)
        window.setGravity(Gravity.START or Gravity.TOP)

        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        captureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                startCapture(result.resultCode, result.data!!)
            } else {
                Toast.makeText(this, "\u622A\u56FE\u53D6\u6D88", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        captureLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun startCapture(resultCode: Int, data: Intent) {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(metrics)
        } else {
            windowManager.defaultDisplay.getRealMetrics(metrics)
        }
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val projection = projectionManager.getMediaProjection(resultCode, data)
        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        val handler = Handler(Looper.getMainLooper())
        val virtualDisplay: VirtualDisplay = projection.createVirtualDisplay(
            "EdgeSideCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, handler
        )

        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                virtualDisplay.release()
                imageReader.close()
            }
        }, handler)

        // Give the VirtualDisplay a moment to render the first frame.
        lifecycleScope.launch {
            delay(250)
            val image = withContext(Dispatchers.IO) { imageReader.acquireLatestImage() }
            if (image == null) {
                Toast.makeText(this@CaptureActivity, "\u622A\u56FE\u5931\u8D25", Toast.LENGTH_SHORT).show()
                projection.stop()
                finish()
                return@launch
            }
            val bitmap = withContext(Dispatchers.IO) { imageToBitmap(image) }
            image.close()
            val saved = withContext(Dispatchers.IO) { saveBitmap(bitmap) }
            projection.stop()
            val msg = if (saved != null) "\u622A\u56FE\u5DF2\u4FDD\u5B58" else "\u4FDD\u5B58\u5931\u8D25"
            Toast.makeText(this@CaptureActivity, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width
        val bitmapWidth = if (rowPadding == 0) image.width
            else image.width + rowPadding / pixelStride
        val bitmap = Bitmap.createBitmap(bitmapWidth, image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return if (rowPadding > 0) Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
            else bitmap
    }

    private fun saveBitmap(bitmap: Bitmap): String? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "EdgeSide_$timestamp.png"
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/EdgeSide"
                    )
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@runCatching null
                contentResolver.openOutputStream(uri)?.use { os: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }
                filename
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val dir = File(picturesDir, "EdgeSide").apply { mkdirs() }
                val file = File(dir, filename)
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                sendBroadcast(Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE").apply {
                    data = android.net.Uri.fromFile(file)
                })
                filename
            }
        }.getOrNull()
    }
}