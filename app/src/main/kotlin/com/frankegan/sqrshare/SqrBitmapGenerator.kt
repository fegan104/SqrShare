package com.frankegan.sqrshare

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream
import androidx.core.graphics.createBitmap

/**
 * @author frankegan created on 1/3/15.
 */
object SqrBitmapGenerator {
    private const val MAX_RAW_IMG = 1080

    /**
     * Decodes an appropriately sized bitmap from the given Uri to be displayed by the ui.
     *
     * @param uri       The URI of the picture to be displayed.
     * @param reqWidth  The requested width of the output image.
     * @param reqHeight The requested height of the output image.
     * @return An appropriately sized bitmap.
     */
    private suspend fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int = MAX_RAW_IMG,
        reqHeight: Int = MAX_RAW_IMG,
    ): Bitmap? = withContext(Dispatchers.IO) {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        context.contentResolver?.openInputStreamOrNull(uri).use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false

        context.contentResolver?.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
    }

    /**
     * An extension function for safely opening an [InputStream].
     *
     * @param uri The file to be opened.
     * @return The [InputStream] or null if the file could not be opened.
     */
    private fun ContentResolver?.openInputStreamOrNull(uri: Uri): InputStream? {
        return try {
            this?.openInputStream(uri)
        } catch (fnf: FileNotFoundException) {
            Log.e("SqrBitmapGenerator", "Could not open file", fnf)
            return null
        }
    }

    /**
     * A method for adding a border to a bitmap so that the resulting image is square.
     *
     * @param source bitmap to be made square.
     * @return the original bitmap with borders added to either the top or bottom
     * such that it is now square.
     */
    private fun makeSquare(source: Bitmap?): Bitmap? {
        val sourceConfig = source?.config ?: return null
        val landscape = source.width > source.height
        val length = if (landscape) source.width else source.height
        val rect = if (landscape) Rect(
            0, length / 2 - source.height / 2,
            length, length / 2 + source.height / 2
        ) else Rect(
            length / 2 - source.width / 2, 0,
            length / 2 + source.width / 2, length
        )
        val square = createBitmap(length, length, sourceConfig)
        val canvas = Canvas(square)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(source, null, rect, null)
        return square
    }

    /**
     * A method that calculates how much a bitmap should be scaled down by.
     *
     * @param options   [BitmapFactory.Options] with "inJustDecodeBounds"
     * set to true that has already been used to decoded a bitmap.
     * @param reqWidth  The requested width of the output image.
     * @param reqHeight The requested height of the output image.
     * @return the nearest power of two that can be used to scale a bitmap to the requested
     * height and width.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) inSampleSize *= 2
        }
        return inSampleSize
    }

    /**
     * Generates a Square Bitmap from the given Uri.
     *
     * @param context  The context in which to open the InputStream needed to decode the Bitmap.
     * @param imageUri The Uri of the image to be turned into a sqr.
     * @return an optimally scaled Bitmap for displaying in an ImageView.
     */
    suspend fun generate(context: Context, imageUri: Uri): Bitmap? {
        return makeSquare(decodeSampledBitmapFromUri(context, imageUri))
    }
}
