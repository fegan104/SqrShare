package com.frankegan.sqrshare

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import java.io.IOException

/**
 * @author frankegan created on 1/3/15.
 */
object SqrBitmapGenerator {
    const val MAX_RAW_IMG = 640

    /**
     * Decodes an appropriately sized bitmap from the given Uri to be displayed by an ImageView.
     *
     * @param uri       The URI of the picture to be displayed.
     * @param reqWidth  The requested width of the output image.
     * @param reqHeight The requested height of the output image.
     * @return An appropriately sized bitmap.
     * @throws java.io.IOException if the provided URI could not be opened.
     */
    @Throws(IOException::class)
    fun decodeSampledBitmapFromUri(c: Context?, uri: Uri?, reqWidth: Int, reqHeight: Int): Bitmap? {
        var input = c!!.contentResolver.openInputStream(uri!!)
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(input, null, options)
        input!!.close()

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        input = c.contentResolver.openInputStream(uri)
        val result = BitmapFactory.decodeStream(input, null, options)
        input!!.close()
        return result
    }

    /**
     * A method for adding a border to a bitmap so that the resulting image is square.
     *
     * @param source bitmap to be made square.
     * @return the original bitmap with borders added to either the top or bottom
     * such that it is now square.
     */
    fun makeSquare(source: Bitmap?): Bitmap {
        val landscape = source!!.width > source.height
        val LENGTH = if (landscape) source.width else source.height
        val rect = if (landscape) Rect(
            0, LENGTH / 2 - source.height / 2,
            LENGTH, LENGTH / 2 + source.height / 2
        ) else Rect(
            LENGTH / 2 - source.width / 2, 0,
            LENGTH / 2 + source.width / 2, LENGTH
        )
        val square = Bitmap.createBitmap(LENGTH, LENGTH, source.config)
        val canvas = Canvas(square)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(source, null, rect, null)
        return square
    }

    /**
     * A method that calculates how much a bitmap should be scaled down by.
     *
     * @param options   [android.graphics.BitmapFactory.Options] with "inJustDecodeBounds"
     * set to true that has already been used to decoded a bitmap.
     * @param reqWidth  The requested width of the output image.
     * @param reqHeight The requested height of the output image.
     * @return the nearest power of two that can be used to scale a bitmap to the requested
     * height and width.
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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
     * @param c        The context in which to open the InputStream needed to decode the Bitmap.
     * @param imageUri The Uri of the image to be turned into a sqr.
     * @return an optimally scaled Bitmap for displaying in an ImageView.
     */
    @Throws(IOException::class)
    fun generate(c: Context?, imageUri: Uri?): Bitmap {
        return makeSquare(decodeSampledBitmapFromUri(c, imageUri, MAX_RAW_IMG, MAX_RAW_IMG))
    }
}
