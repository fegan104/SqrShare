package com.frankegan.sqrshare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author frankegan on 11/24/14.
 */
public class PictureFragment extends Fragment implements PictureHolder, View.OnClickListener {
    private final static int SELECT_PHOTO = 100;
    private OnColorsCalculatedListener parent;
    private PicGenerator generator;
    private static ImageView imageView;
    private FloatingActionButton fab;
    private static Bitmap data;


    /**
     * An interface for communicating that the colors of the ImageView hav been calculated,
     * to the parent Activity
     */
    public interface OnColorsCalculatedListener {
        /**
         * A method to be called whenever the colors have been calculated for a specific Bitmap. This allows to Activity
         *
         * @param vibrant the color that was calculated
         */
        public void onColorsCalculated(Integer vibrant);
    }

    /**
     * An interface for Activities to implement that insure a way for Fragments
     * to retrieve Bitmaps generated before the onActivityCreated is called.
     */
    public interface PicGenerator {
        /**
         * A method for getting the Bitmap that was calculated in the activity
         *
         * @return the Bitmap that was calculated in the Activity
         */
        public Bitmap getGeneratedPic();
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

        try {
            generator = (PicGenerator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PicGenerator");
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        //This is used when an app shares a picture to the Activity.
        if (generator.getGeneratedPic() != null)
            setPicture(generator.getGeneratedPic());

        //This is used when the device has a configuration change.
        if (savedInstanceState != null)
            setPicture(getFragmentData());
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
        setFragmentData(getPictureBitmap());
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
            imageView.setImageBitmap(SqrBitmapGenerator.generate(getActivity(), uri));
            Animation scale = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_sqr);
            imageView.startAnimation(scale);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("frankegan", "IOException" + e.toString());
        }
        calculateColors();
    }

    /**
     * A method for setting the Picture to be displayed in the {@link android.support.v4.app.Fragment}.
     *
     * @param bm The Bitmap to be displayed.
     */
    @Override
    public void setPicture(Bitmap bm) {
        if (imageView != null) {
            imageView.setImageBitmap(bm);
            Animation scale = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_sqr);
            imageView.startAnimation(scale);
        } else {
            Log.i("frankegan", "there was no imageview");
        }
        calculateColors();
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
     * sets the color of the Floating action button.
     *
     * @param clr the new background color of the FAB.
     */
    @Override
    public void setFabColor(Integer clr) {
        fab.setColorNormal(clr);
    }

    /**
     * Sets the Bitmap data to be restored.
     *
     * @param data data to be saved
     */
    @Override
    public void setFragmentData(Bitmap data) {
        this.data = data;
    }

    /**
     * Gets the data saved to the fragment for configuration changes.
     *
     * @return the Bitmap data saved to the fragment.
     */
    @Override
    public Bitmap getFragmentData() {
        return data;
    }

    /**
     * Gets the Bitmap of the picture displayed in the Imageview.
     *
     * @return the picture's Bitmap
     */
    @Override
    public Bitmap getPictureBitmap() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if (bitmapDrawable != null)
            return bitmapDrawable.getBitmap();
        return null;
    }

    /**
     * A helper method for generating and setting all the activity colors to match the Picture.
     */
    public void calculateColors() {
        if (imageView != null)
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
     * Saves a scaled version of the square picture to external storage and then gets the Uri of the file.
     *
     * @return The Uri of a scaled version of the square you're sharing.
     */
    private Uri saveBitmapAndGetUri() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if (bitmapDrawable != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),
                    SqrBitmapGenerator.MAX_RAW_IMG, SqrBitmapGenerator.MAX_RAW_IMG, true);

            String file_path = Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + "/Squares";
            File dir = new File(file_path);
            if (!dir.exists())
                dir.mkdirs();
            String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
            File file = new File(dir, "SQR_" + timeStamp + ".png");
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
}
