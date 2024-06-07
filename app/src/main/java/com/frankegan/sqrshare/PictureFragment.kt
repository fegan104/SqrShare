package com.frankegan.sqrshare

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.melnykov.fab.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @author frankegan on 11/24/14.
 */
class PictureFragment : Fragment(), PictureHolder, View.OnClickListener {
    private var parent: OnColorsCalculatedListener? = null
    private var generator: PicGenerator? = null
    private var fab: FloatingActionButton? = null

    /**
     * An interface for communicating that the colors of the ImageView hav been calculated,
     * to the parent Activity
     */
    interface OnColorsCalculatedListener {
        /**
         * A method to be called whenever the colors have been calculated for a specific Bitmap. This allows to Activity
         *
         * @param vibrant the color that was calculated
         */
        fun onColorsCalculated(vibrant: Int?)
    }

    /**
     * An interface for Activities to implement that insure a way for Fragments
     * to retrieve Bitmaps generated before the onActivityCreated is called.
     */
    interface PicGenerator {
        /**
         * A method for getting the Bitmap that was calculated in the activity
         *
         * @return the Bitmap that was calculated in the Activity
         */
        val generatedPic: Bitmap?
    }

    /**
     * {@inheritDoc}
     */
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        parent = try {
            activity as OnColorsCalculatedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                activity.toString()
                        + " must implement OnColorsChangeListener"
            )
        }
        generator = try {
            activity as PicGenerator
        } catch (e: ClassCastException) {
            throw ClassCastException(
                activity.toString()
                        + " must implement PicGenerator"
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.picture_fragment_layout, container, false)
        imageView = v.findViewById<View>(R.id.imageView) as ImageView
        fab = v.findViewById<View>(R.id.fab) as FloatingActionButton
        fab!!.setOnClickListener(this)
        return v
    }

    /**
     * {@inheritDoc}
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true

        //This is used when an app shares a picture to the Activity.
        if (generator!!.generatedPic != null) setPicture(generator!!.generatedPic)

        //This is used when the device has a configuration change.
        if (savedInstanceState != null) setPicture(getFragmentData())
        setHasOptionsMenu(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        when (requestCode) {
            SELECT_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent!!.data
                this.setPicture(selectedImage)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pictureBitmap?.let { setFragmentData(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.picture_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * {@inheritDoc}
     */
    override fun onClick(v: View) {
        if (v.id == R.id.fab) {
            selectPicture()
        }
    }

    /**
     * A method for setting the Picture to be displayed in the [Fragment].
     *
     * @param uri The URI of the picture to be displayed.
     */
    override fun setPicture(uri: Uri?) {
        try {
            imageView!!.setImageBitmap(SqrBitmapGenerator.generate(activity, uri))
            val scale = AnimationUtils.loadAnimation(activity, R.anim.scale_sqr)
            imageView!!.startAnimation(scale)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("frankegan", "IOException$e")
        }
        calculateColors()
    }

    /**
     * A method for setting the Picture to be displayed in the [Fragment].
     *
     * @param bm The Bitmap to be displayed.
     */
    override fun setPicture(bm: Bitmap?) {
        if (imageView != null) {
            imageView!!.setImageBitmap(bm)
            val scale = AnimationUtils.loadAnimation(activity, R.anim.scale_sqr)
            imageView!!.startAnimation(scale)
        } else {
            Log.i("frankegan", "there was no imageview")
        }
        calculateColors()
    }

    override val pictureUri: Uri?
        /**
         * A method for getting the Uri for sharing the new square picture.
         *
         * @return the Uri of th fragment's ImageView.
         */
        get() = saveBitmapAndGetUri()

    /**
     * sets the color of the Floating action button.
     *
     * @param clr the new background color of the FAB.
     */
    override fun setFabColor(clr: Int?) {
        fab!!.colorNormal = clr!!
    }

    /**
     * Sets the Bitmap data to be restored.
     *
     * @param bm data to be saved
     */
    override fun setFragmentData(bm: Bitmap) {
        this.setFragmentData(bm)
    }

    /**
     * Gets the data saved to the fragment for configuration changes.
     *
     * @return the Bitmap data saved to the fragment.
     */
    override fun getFragmentData(): Bitmap? {
        return data
    }

    override val pictureBitmap: Bitmap?
        /**
         * Gets the Bitmap of the picture displayed in the Imageview.
         *
         * @return the picture's Bitmap
         */
        get() {
            return (imageView?.drawable as? BitmapDrawable)?.bitmap
        }

    //TODO apply bitmap memory management techniques
    override fun rotatePicture() {
        if (imageView!!.drawable != null) {
            Log.i("frankegan", "rotated")
            val matrix = Matrix()
            val bitmap = pictureBitmap
            matrix.postRotate(90f)
            val bMapRotate = Bitmap.createBitmap(
                bitmap!!, 0, 0, bitmap.width,
                bitmap.height, matrix, true
            )
            imageView!!.setImageBitmap(bMapRotate)
            val rotate = AnimationUtils.loadAnimation(activity, R.anim.rotate_sqr)
            imageView!!.startAnimation(rotate)
        }
    }

    /**
     * A helper method for generating and setting all the activity colors to match the Picture.
     */
    fun calculateColors() {
        if (imageView != null) Palette.generateAsync(
            (imageView!!.drawable as BitmapDrawable).bitmap
        ) { palette ->
            val colorVib = palette!!.getVibrantColor(Color.parseColor("black"))
            parent!!.onColorsCalculated(colorVib)
        }
    }

    /**
     * Saves a scaled version of the square picture to external storage and then gets the Uri of the file.
     *
     * @return The Uri of a scaled version of the square you're sharing.
     */
    private fun saveBitmapAndGetUri(): Uri? {
        val bitmapDrawable = imageView!!.drawable as BitmapDrawable
        if (bitmapDrawable != null) {
            val scaled = Bitmap.createScaledBitmap(
                bitmapDrawable.bitmap,
                SqrBitmapGenerator.MAX_RAW_IMG, SqrBitmapGenerator.MAX_RAW_IMG, true
            )
            val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .absolutePath + "/Squares"
            val dir = File(file_path)
            if (!dir.exists()) dir.mkdirs()
            val timeStamp = SimpleDateFormat("HHmmss").format(Calendar.getInstance().time)
            val file = File(dir, "SQR_$timeStamp.png")
            val fOut: FileOutputStream
            try {
                fOut = FileOutputStream(file)
                scaled.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                fOut.flush()
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return Uri.fromFile(file)
        }
        return null
    }

    /**
     * Lets you choose a picture from the Gallery to be opened in the app.
     */
    fun selectPicture() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.setType("image/*")
        startActivityForResult(photoPickerIntent, SELECT_PHOTO)
    }

    companion object {
        private const val SELECT_PHOTO = 100
        private var imageView: ImageView? = null
        private val data: Bitmap? = null
    }
}
