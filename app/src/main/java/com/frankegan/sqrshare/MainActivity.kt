package com.frankegan.sqrshare

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.frankegan.sqrshare.PictureFragment.OnColorsCalculatedListener
import com.frankegan.sqrshare.PictureFragment.PicGenerator
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices
import java.io.IOException

/**
 * @author frankegan on 11/24/14.
 */
class MainActivity : AppCompatActivity(), OnColorsCalculatedListener, PicGenerator {
    private var status: FrameLayout? = null
    private val tag = "pic"
    private var pic_fragment: PictureHolder? = null

    /**
     * A method for Fragments to retrieve Bitmaps that are generated
     * in our activity before the FragmentTransaction is committed.
     *
     * @return a square Bitmap to be displayed in the Fragment.
     */
    override var generatedPic: Bitmap? = null
        private set

    /**
     * {@inheritDoc}
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        status = findViewById<View>(R.id.status) as FrameLayout
        val toolbar = findViewById<View>(R.id.my_awesome_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val intent = intent
        val action = intent.action
        val type = intent.type
        pic_fragment = supportFragmentManager.findFragmentByTag(tag) as PictureHolder?
        if (pic_fragment == null) {
            pic_fragment = PictureFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragment, (pic_fragment as Fragment?)!!, tag).commit()
        }
        status!!.minimumHeight = statusBarHeight

        //this if for apps sharing to the app
        if (Intent.ACTION_SEND == action && type != null && type.startsWith("image/")) {
            Log.i("frankegan", "image was sent to activity")
            handleSentImage(intent) // Handle single image being sent to you
        }

        //this is for apps trying to open images with our app
        if (Intent.ACTION_VIEW == action && type != null && type.startsWith("image/")) {
            Log.i("frankegan", "image was viewed in activity")
            handleViewImage(intent) // Handle single image being sent to you
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        super.onDestroy()
        pic_fragment!!.setFragmentData(pic_fragment!!.pictureBitmap!!)
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //finds the actionBar item that was clicked
        val itemId = item.itemId
        if (itemId == R.id.action_about) {
            showAbout()
            return true
        } else if (itemId == R.id.action_share) {
            sharePicture()
            return true
        } else if (itemId == R.id.action_rotate) {
            pic_fragment!!.rotatePicture()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * {@inheritDoc}
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
    }

    /**
     * {@inheritDoc}
     */
    override fun onColorsCalculated(vib: Int?) {
        pic_fragment!!.setFabColor(vib)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(vib!!))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) status!!.setBackgroundColor(vib)
    }

    val statusBarHeight: Int
        /**
         * Gets the height of the status bar.
         *
         * @return the status bar's height in pixels.
         */
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    /**
     * A helper method for when an app shares an [android.content.Intent] to be opened by our app.
     * It changes the [android.widget.ImageView] to the image of the given intent.
     *
     * @param intent The [android.content.Intent] of the image to be displayed.
     */
    private fun handleSentImage(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (imageUri != null) try {
            generatedPic = SqrBitmapGenerator.generate(this, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("frankegan", "IOException$e")
        } else Log.i("frankegan", "bad intent")
    }

    /**
     * A helper method for when an app shares an [android.content.Intent] to be Viewed in our app.
     * It changes the [android.widget.ImageView] to the image of the given intent.
     *
     * @param intent The [android.content.Intent] of the image to be displayed.
     */
    private fun handleViewImage(intent: Intent) {
        val imageUri = intent.data
        if (imageUri != null) try {
            generatedPic = SqrBitmapGenerator.generate(this, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("frankegan", "IOException$e")
        } else Log.i("frankegan", "bad intent")
    }

    /**
     * Shares the image currently being displayed by the [android.widget.ImageView].
     */
    private fun sharePicture() {
        val uri = pic_fragment?.pictureUri
        if (uri != null) {
            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.setType("image/*")
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
        }
    }

    /**
     * Opens a dialog listing all licenses of 3rd party libraries used in the project.
     */
    private fun showAbout() {
        val notices = Notices()
        notices.addNotice(
            Notice(
                "LicensesDialog",
                "http://psdev.de",
                "Copyright 2013 Philip Schiffer <admin@psdev.de>",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "FloatingActionButton",
                "https://github.com/makovkastar/FloatingActionButton",
                "Copyright (c) 2014 Oleksandr Melnykov",
                MITLicense()
            )
        )
        LicensesDialog.Builder(this).setNotices(notices).build().show()
    }
}
