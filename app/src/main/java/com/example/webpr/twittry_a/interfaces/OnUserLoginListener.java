package com.example.webpr.twittry_a.interfaces;

/**
 * Created by webpr on 03.01.2017.
 */

public interface OnUserLoginListener {

    /**
     * Shares auth URL with MainActivity
     * @param authUrl
     */
    public void onUserLogin(String authUrl);
}
