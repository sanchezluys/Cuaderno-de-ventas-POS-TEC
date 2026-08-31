package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    private const val LOGO_FILE_NAME = "store_logo_profile.png"

    /**
     * Copies the image from the given Uri into the app's private files directory,
     * ensuring it remains permanently accessible offline without permission expiration.
     * Returns the absolute path of the saved file or null if failed.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val destinationFile = File(context.filesDir, LOGO_FILE_NAME)
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                // Decode to verify and optionally optimize size
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (originalBitmap != null) {
                    // Scale down to max 512x512 to preserve memory and keep receipt printing crisp & fast
                    val maxDimension = 512
                    val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                        val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                        val newWidth: Int
                        val newHeight: Int
                        if (ratio > 1) {
                            newWidth = maxDimension
                            newHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
                        } else {
                            newHeight = maxDimension
                            newWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
                        }
                        Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                    } else {
                        originalBitmap
                    }

                    FileOutputStream(destinationFile).use { outputStream ->
                        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream)
                        outputStream.flush()
                    }
                    destinationFile.absolutePath
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ImageStorageHelper", "Error saving logo image", e)
            null
        }
    }

    /**
     * Loads a Bitmap from a local file path. Returns null if file does not exist or fails to decode.
     */
    fun loadBitmapFromPath(filePath: String?): Bitmap? {
        if (filePath.isNullOrBlank()) return null
        return try {
            val file = File(filePath)
            if (file.exists() && file.length() > 0) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ImageStorageHelper", "Error loading bitmap from path: $filePath", e)
            null
        }
    }

    /**
     * Deletes the store logo file from internal storage.
     */
    fun deleteLogo(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, LOGO_FILE_NAME)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("ImageStorageHelper", "Error deleting logo", e)
            false
        }
    }
}
