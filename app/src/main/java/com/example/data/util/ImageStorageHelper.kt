package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    private const val LOGO_FILE_NAME = "store_logo_profile.png"
    private const val MAX_IMAGE_DIMENSION = 512

    /**
     * Calculates the optimal inSampleSize for bitmap downsampling to prevent OutOfMemory errors
     * and minimize RAM consumption as recommended by Google Play Console.
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    /**
     * Copies the image from the given Uri into the app's private files directory,
     * downsampling the image before decoding to prevent high memory usage.
     * Returns the absolute path of the saved file or null if failed.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val destinationFile = File(context.filesDir, LOGO_FILE_NAME)

            // Step 1: Decode only bounds to inspect dimensions without loading pixels into RAM
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }

            // Step 2: Compute downsampling ratio
            val sampleSize = calculateInSampleSize(boundsOptions, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)

            // Step 3: Decode the downsampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val downsampledBitmap = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // Step 4: Scale down to exact maxDimension if still larger
            val finalBitmap = if (downsampledBitmap.width > MAX_IMAGE_DIMENSION || downsampledBitmap.height > MAX_IMAGE_DIMENSION) {
                val ratio = downsampledBitmap.width.toFloat() / downsampledBitmap.height.toFloat()
                val newWidth: Int
                val newHeight: Int
                if (ratio > 1f) {
                    newWidth = MAX_IMAGE_DIMENSION
                    newHeight = (MAX_IMAGE_DIMENSION / ratio).toInt().coerceAtLeast(1)
                } else {
                    newHeight = MAX_IMAGE_DIMENSION
                    newWidth = (MAX_IMAGE_DIMENSION * ratio).toInt().coerceAtLeast(1)
                }
                val scaled = Bitmap.createScaledBitmap(downsampledBitmap, newWidth, newHeight, true)
                if (scaled != downsampledBitmap) {
                    downsampledBitmap.recycle()
                }
                scaled
            } else {
                downsampledBitmap
            }

            FileOutputStream(destinationFile).use { outputStream ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream)
                outputStream.flush()
            }
            finalBitmap.recycle()

            destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e("ImageStorageHelper", "Error saving logo image with downsampling", e)
            null
        }
    }

    /**
     * Loads a Bitmap from a local file path with downsampling.
     * Returns null if file does not exist or fails to decode.
     */
    fun loadBitmapFromPath(filePath: String?, reqWidth: Int = 256, reqHeight: Int = 256): Bitmap? {
        if (filePath.isNullOrBlank()) return null
        return try {
            val file = File(filePath)
            if (!file.exists() || file.length() == 0L) return null

            // Step 1: Query image dimensions
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

            // Step 2: Decode with downsampled inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (e: Exception) {
            Log.e("ImageStorageHelper", "Error loading downsampled bitmap from path: $filePath", e)
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
