package dev.borisochieng.sketchpad.ui.screens.drawingboard.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.borisochieng.sketchpad.database.PERMISSION_CODE
import dev.borisochieng.sketchpad.database.permissions
import java.io.File

internal fun Activity.checkAndAskPermission(continueNext: () -> Unit) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(this,
            permissions[0]) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(this,
            permissions,
            PERMISSION_CODE
        )
        return
    }
    continueNext()
}

internal fun activityChooser(uri: Uri?) = Intent.createChooser(Intent().apply {
    type = "image/*"
    action = Intent.ACTION_VIEW
    data = uri
}, "Select Gallery App")

internal fun activityChooserForPdf(uri: Uri?) = Intent.createChooser(Intent().apply {
    type = "application/pdf" // Set the MIME type for PDF
    action = Intent.ACTION_VIEW// You can keep the ACTION_VIEW for opening the PDF
    data = uri
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Important for granting temporary access
}, "Choose PDF Viewer")

//writing files to storage via scope and normal manner acc. to Api level
internal fun Context.saveImage(bitmap: Bitmap): Uri? {
    var uri: Uri? = null
    try {
        val fileName = System.nanoTime().toString() + ".png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            } else {
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val file = File(directory, fileName)
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }
        }

        uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it).use { output ->
                if (output != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.apply {
                    clear()
                    put(MediaStore.Audio.Media.IS_PENDING, 0)
                }
                contentResolver.update(uri, values, null, null)
            }
        }
        return uri
    } catch (e: java.lang.Exception) {
        if (uri != null) {
            // Don't leave an orphan entry in the MediaStore
            contentResolver.delete(uri, null, null)
        }
        throw e
    }
}

internal fun Context.savePdf(bitmap: Bitmap): Uri? {
    var uri: Uri? = null
    try {val fileName = System.nanoTime().toString() + ".pdf"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(directory, fileName)
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }
        }

        // For PDF, use MediaStore.Files.getContentUri instead of MediaStore.Images
        uri= contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
        uri?.let {
            contentResolver.openOutputStream(it).use { output ->
                if (output != null) {
                    // Use a PDF library to write the bitmap to the output stream
                    // Example using PdfDocument (requires API level 19 or higher):
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas = page.canvas
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                        pdfDocument.finishPage(page)
                        pdfDocument.writeTo(output)
                        pdfDocument.close()
                    } else {
                        // Handle older API levels if necessary
                        // You might need a third-party PDF library for this
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.apply {
                    clear()
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                contentResolver.update(uri, values, null, null)
            }
        }
        return uri
    } catch (e: Exception) {
        if (uri != null) {
            contentResolver.delete(uri, null, null)
        }
        throw e
    }
}