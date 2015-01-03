package com.frankegan.sqrshare;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * @author frankegan on 11/24/14.
 */
public class MainActivity extends ActionBarActivity implements
        PictureFragment.OnColorsCalculatedListener {
    private FrameLayout status;
    private final String tag = "pic";
    private PictureFragment pic_fragment;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        status = (FrameLayout) findViewById(R.id.status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        pic_fragment = (PictureFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (pic_fragment != null && pic_fragment.getData() != null) {
            pic_fragment.setPicture(pic_fragment.getData());
        } else if (pic_fragment == null) {
            pic_fragment = new PictureFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, pic_fragment, tag).commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            status.setMinimumHeight(getStatusBarHeight());

        //this if for apps sharing to the app
        if (Intent.ACTION_SEND.equals(action) && type != null && (type.startsWith("image/"))) {
            handleSentImage(intent); // Handle single image being sent to you

        }

        //this is for apps trying to open images with our app
        if ((Intent.ACTION_VIEW.equals(action)) && (type != null) && (type.startsWith("image/")))
            pic_fragment.setPicture(intent.getData());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pic_fragment.setData(pic_fragment.getPictureBitmap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //finds the actionBar item that was clicked
        switch (item.getItemId()) {
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_share:
                sharePicture();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onColorsCalculated(Integer vib) {
        pic_fragment.setFabColor(vib);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(vib));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            status.setBackgroundColor(vib);
    }

    /**
     * Gets the height of the status bar.
     *
     * @return the status bar's height in pixels.
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * A helper method for when an app shares an {@link android.content.Intent} to be opened by our app.
     * It changes the {@link android.widget.ImageView} to the image of the given intent.
     *
     * @param intent The {@link android.content.Intent} of the image to be displayed.
     */
    private void handleSentImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null)
            pic_fragment.setPicture(imageUri);
    }

    /**
     * Shares the image currently being displayed by the {@link android.widget.ImageView}.
     */
    private void sharePicture() {
        Uri uri = pic_fragment.getPictureUri();
        if (uri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
        }
    }

    /**
     * Opens a dialog listing all licenses of 3rd party libraries used in the project.
     */
    private void showAbout() {
        Notices notices = new Notices();
        notices.addNotice(new Notice("LicensesDialog",
                "http://psdev.de",
                "Copyright 2013 Philip Schiffer <admin@psdev.de>",
                new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("FloatingActionButton",
                "https://github.com/makovkastar/FloatingActionButton",
                "Copyright (c) 2014 Oleksandr Melnykov",
                new MITLicense()));
        new LicensesDialog.Builder(this).setNotices(notices).build().show();
    }
}
