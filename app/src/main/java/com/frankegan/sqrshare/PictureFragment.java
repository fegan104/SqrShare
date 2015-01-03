package com.frankegan.sqrshare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author frankegan on 11/24/14.
 */
public class PictureFragment extends Fragment implements PictureHolder, View.OnClickListener {
    private final static int SELECT_PHOTO = 100;
    private final static int MAX_RAW_IMG = 640;
    private OnColorsCalculatedListener parent;
    private static ImageView imageView;
    private FloatingActionButton fab;
    private static Bitmap data;

    /**
     * An interface for communicating that the colors of the ImageView hav been calculated,
     * to the parent Activity
     */
    public interface OnColorsCalculatedListener {
        public void onColorsCalculated(Integer vibrant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            setPicture(getData());
            calculateColors();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            parent = (OnColorsCalculatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnColorsChangeListener");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_layout, container, false);
        imageView = (ImageView) v.findViewById(R.id.imageView);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    this.setPicture(selectedImage);
                }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setData(getPictureBitmap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                openPicture();
                break;
        }
    }

    /**
     * A method for setting the Picture to be displayed in the {@link android.support.v4.app.Fragment}.
     *
     * @param uri The URI of the picture to be displayed.
     */
    @Override
    public void setPicture(Uri uri) {
        try {
            imageView.setImageBitmap(makeSquare(decodeSampledBitmapFromUri(uri, MAX_RAW_IMG, MAX_RAW_IMG)));
            calculateColors();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "couldn't get that picture", Toast.LENGTH_SHORT).show();
        }
    }

    public void setPicture(Bitmap bm) {
        imageView.setImageBitmap(bm);
    }

    /**
     * A helper method for generating and setting all the activity colors to match the Picture.
     */
    public void calculateColors() {
        Palette.generateAsync(((BitmapDrawable) imageView.getDrawable()).getBitmap(),
                new Palette.PaletteAsyncListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onGenerated(Palette palette) {
                        Integer colorVib = palette.getVibrantColor(Color.parseColor("black"));
                        parent.onColorsCalculated(colorVib);
                    }
                });
    }

    /**
     * Decodes an appropriately sized bitmap from the given Uri to be displayed by an ImageView.
     *
     * @param uri       The URI of the picture to be displayed.
     * @param reqWidth  The requested width of the output image.
     * @param reqHeight The requested height of the output image.
     * @return An appropriately sized bitmap.
     * @throws java.io.IOException if the provided URI could not be opened.
     */
    public Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight)
            throws IOException {

        InputStream input = getActivity().getContentResolver().openInputStream(uri);
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        input = getActivity().getContentResolver().openInputStream(uri);
        Bitmap result = BitmapFactory.decodeStream(input, null, options);
        input.close();
        return result;
    }

    /**
     * A method for getting the Uri for sharing the new square picture.
     *
     * @return the Uri of th fragment's ImageView.
     */
    @Override
    public Uri getPictureUri() {
        return saveBitmapAndGetUri();
    }

    /**
     * Saves a scaled version of the square picture to external storage and then gets the Uri of the file.
     *
     * @return The Uri of a scaled version of the square you're sharing.
     */
    //TODO change file name to SQR_XXXX instead of date
    private Uri saveBitmapAndGetUri() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if (bitmapDrawable != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),
                    MAX_RAW_IMG, MAX_RAW_IMG, true);

            String file_path = Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + "/Squares";
            File dir = new File(file_path);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, new Date().toString() + ".png");
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                scaled.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Uri.fromFile(file);
        }
        return null;
    }

    /**
     * Lets you choose a picture from the Gallery to be opened in the app.
     */
    public void openPicture() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
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
     * sets the color of the Floating action button.
     *
     * @param clr the new background color of the FAB.
     */
    public void setFabColor(Integer clr) {
        fab.setColorNormal(clr);
    }

    /**
     * Gets the Bitmap of the picture displayed in the Imageview.
     * @return the picture's Bitmap
     */
    public Bitmap getPictureBitmap() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if (bitmapDrawable != null)
            return bitmapDrawable.getBitmap();
        return null;
    }

    /**
     * Sets the Bitmap data to be restored.
     * @param data data to be saved
     */
    public void setData(Bitmap data) {
        this.data = data;
    }

    /**
     * Gets the data saved to the fragment for configuration changes.
     * @return the Bitmap data saved to the fragment.
     */
    public Bitmap getData() {
        return data;
    }
}
