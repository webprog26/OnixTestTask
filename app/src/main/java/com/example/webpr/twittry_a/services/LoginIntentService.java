package com.example.webpr.twittry_a.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.webpr.twittry_a.MainActivity;
import com.example.webpr.twittry_a.twitter.TwitterSingleton;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by webpr on 03.01.2017.
 */

public class LoginIntentService extends IntentService {

//    private Twitter mTwitter;
//
//    public static final String TWITTER_CONSUMER_KEY = "2vjveEZeELBE0sHb9qy3JZP4q";
//    public static final String TWITTER_CONSUMER_SECRET = "JasH4nfsbDm8mxgmmHb4jyGArBGOOJlmP7k9FdcCXh0yKcrTkx";

    public static final String TOKEN = "com.example.webprog26.twittry_a.token";
    public static final String TOKEN_SECRET = "com.example.webprog26.twittry_a.token_secret";

    public static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";


    private static final String TAG = "LoginIntentService";

    public static final String TWITTER_AUTH_URL = "com.example.webprog26.twittry_a.twitter_auth_url";

    public LoginIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Twitter twitter = TwitterSingleton.getInstance().getTwitter();
        try{
           RequestToken oAuthRequestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
           SharedPreferences sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(this);
           SharedPreferences.Editor editor = sharedPreferences.edit();

           editor.putString(TOKEN, oAuthRequestToken.getToken()).apply();
           editor.putString(TOKEN_SECRET, oAuthRequestToken.getTokenSecret()).apply();

           sendLocalBroadcast(oAuthRequestToken.getAuthenticationURL());
        } catch (TwitterException te){
            Log.e(TAG, te.getMessage());
        }
    }

    private void sendLocalBroadcast(String twitterAuthUrl){
        Intent twitterAuthUrlIntent = new Intent(MainActivity.LOGIN_TO_TWITTER_ACTION);
        twitterAuthUrlIntent.putExtra(TWITTER_AUTH_URL, twitterAuthUrl);
        LocalBroadcastManager.getInstance(this).sendBroadcast(twitterAuthUrlIntent);
    }
}
