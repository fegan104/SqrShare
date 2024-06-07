package com.frankegan.sqrshare

import android.graphics.Bitmap
import android.net.Uri

/**
 * @author frankegan on 12/28/14.
 */
interface PictureHolder {
    fun setPicture(uri: Uri?)
    fun setPicture(bm: Bitmap?)
    val pictureUri: Uri?
    val pictureBitmap: Bitmap?
    fun setFragmentData(bm: Bitmap)
    fun getFragmentData(): Bitmap?
    fun setFabColor(clr: Int?)
    fun rotatePicture()
}
