package com.frankegan.sqrshare;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author frankegan created on 1/3/15.
 */
public class SquareBitmapGenerator {
    public final static int MAX_RAW_IMG = 640;

    /**
     * Decodes an appropriately sized bitmap from the given Uri to be displayed by an ImageView.
     *
     * @param uri       The URI of the picture to be displayed.
     * @param reqWidth  The requested width of the output image.
     * @param reqHeight The requested height of the output image.
     * @return An appropriately sized bitmap.
     * @throws java.io.IOException if the provided URI could not be opened.
     */
    public static Bitmap decodeSampledBitmapFromUri(Activity act, Uri uri, int reqWidth, int reqHeight)
            throws IOException {

        InputStream input = act.getContentResolver().openInputStream(uri);
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        input = act.getContentResolver().openInputStream(uri);
        Bitmap result = BitmapFactory.decodeStream(input, null, options);
        input.close();
        return result;
    }

    /**
     * A method for adding a border to a bitmap so that the resulting image is square.
     *
     * @param source bitmap to be made square.
     * @return the original bitmap with borders added to either the top or bottom
     * such that it is now square.
     */
    public static Bitmap makeSquare(Bitmap source) {
        boolean landscape = source.getWidth() > source.getHeight();
        final int LENGTH = landscape ? source.getWidth() : source.getHeight();
        Rect rect = landscape ?
                new Rect(0, (LENGTH / 2) - (source.getHeight() / 2),
                        LENGTH, (LENGTH / 2) + (source.getHeight() / 2))
                :
                new Rect((LENGTH / 2) - (source.getWidth() / 2), 0,
                        (LENGTH / 2) + (source.getWidth() / 2), LENGTH);
        Bitmap square = Bitmap.createBitmap(LENGTH, LENGTH, source.getConfig());
        Canvas canvas = new Canvas(square);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(source, null, rect, null);
        return square;
    }

    /**
     * A method that calculates how much a bitmap should be scaled down by.
     *
     * @param options   {@link android.graphics.BitmapFactory.Options} with "inJustDecodeBounds"
     *                  set to true that has already been used to decoded a bitmap.
     * @param reqWidth  The requested width of the output image.
     * @param reqHeight The requested height of the output image.
     * @return the nearest power of two that can be used to scale a bitmap to the requested
     * height and width.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (((halfHeight / inSampleSize) > reqHeight)
                    && ((halfWidth / inSampleSize) > reqWidth))
                inSampleSize *= 2;
        }
        return inSampleSize;
    }

    //TODO  better error handling here, no nulls
    public static Bitmap generateSqrBitmap(Activity act, Uri imageUri) {
        try {
            return makeSquare(decodeSampledBitmapFromUri(act, imageUri, MAX_RAW_IMG, MAX_RAW_IMG));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
