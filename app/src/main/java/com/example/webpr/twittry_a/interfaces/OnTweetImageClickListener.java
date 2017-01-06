package com.example.webpr.twittry_a.interfaces;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by webpr on 05.01.2017.
 */

public interface OnTweetImageClickListener {

    /**
     * Handles on tweet image click event
     * @param bitmapUrl {@link String}
     */
    public void onTweetImageClick(String bitmapUrl);
}
