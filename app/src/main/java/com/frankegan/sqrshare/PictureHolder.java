package com.frankegan.sqrshare;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * @author frankegan on 12/28/14.
 */
public interface PictureHolder {

    public void setPicture(Uri uri);

    public void setPicture(Bitmap bm);

    public Uri getPictureUri();

    public Bitmap getPictureBitmap();

    public void setFragmentData(Bitmap bm);

    public Bitmap getFragmentData();

    public void setFabColor(Integer clr);
}
