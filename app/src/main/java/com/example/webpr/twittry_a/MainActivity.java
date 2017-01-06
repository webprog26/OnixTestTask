package com.example.webpr.twittry_a;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.webpr.twittry_a.interfaces.OnUserLoginListener;
import com.example.webpr.twittry_a.receivers.LoginEventReceiver;
import com.example.webpr.twittry_a.services.LoginIntentService;
import com.example.webpr.twittry_a.twitter.TwitterSingleton;

import java.io.Serializable;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MainActivity extends AppCompatActivity implements OnUserLoginListener{

    private static final String TAG = "MainActivity_TAG";
    // Twitter oauth urls
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";

    public static final String LOGIN_TO_TWITTER_ACTION = "com.example.webprog26.twittry_a.user_login_to_twitter_action";

    public static final String LOGGED_IN_USER = "com.example.webprog26.twittry_a.logged_in_user";

    public static final String PREF_KEY_OAUTH_TOKEN = "pref_key_oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "pref_key_oauth_secret";
    public static final String IS_USER_LOGGED_IN = "is_user_logged_in";

    private LoginEventReceiver mLoginEventReceiver;

    private static final int LOGIN_USER_REQUEST_CODE = 1;
    private PublishSubject<Uri> userPublishSubject = PublishSubject.create();
    private Subscription mSubscription;

    private Button mBtnLoginToTwitter;
    private ProgressBar mPbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(IS_USER_LOGGED_IN, false))
        {
            Intent intent = new Intent(MainActivity.this, InnerActivity.class);
            startActivity(intent);
            finish();
        }

        mLoginEventReceiver = new LoginEventReceiver(this);

        mPbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        mBtnLoginToTwitter = (Button) findViewById(R.id.btnLoginToTwitter);
        mBtnLoginToTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(!v.isEnabled());
                Intent loginIntentServiceIntent = new Intent(MainActivity.this, LoginIntentService.class);
                startService(loginIntentServiceIntent);
            }
        });

        mSubscription = userPublishSubject.observeOn(Schedulers.io())
                .map(new Func1<Uri, User>() {
                    @Override
                    public User call(Uri uri) {
                        String verifier = uri.getQueryParameter(MainActivity.URL_TWITTER_OAUTH_VERIFIER);
                        try {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                            RequestToken requestToken = new RequestToken(
                                    sharedPreferences.getString(LoginIntentService.TOKEN, null),
                                    sharedPreferences.getString(LoginIntentService.TOKEN_SECRET, null));

                            Twitter twitter = TwitterSingleton.getInstance().getTwitter();
                            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                            saveUserInfo(sharedPreferences, accessToken);
                            return twitter.showUser(accessToken.getUserId());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                            return null;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        if(mPbLoading.getVisibility() == View.VISIBLE){
                            mPbLoading.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBtnLoginToTwitter.setEnabled(!mBtnLoginToTwitter.isEnabled());

                        if(mPbLoading.getVisibility() == View.VISIBLE){
                            mPbLoading.setVisibility(View.GONE);
                        }
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(User user) {
                        if(null != user){
                            Log.i(TAG, "username " + user.getName() + ", isSerializable " + String.valueOf(user instanceof Serializable));
                            Intent intent = new Intent(MainActivity.this, InnerActivity.class);
                            intent.putExtra(LOGGED_IN_USER, user);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(LOGIN_TO_TWITTER_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLoginEventReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoginEventReceiver);

        if(mSubscription != null && isFinishing()){
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onUserLogin(String authUrl) {
        if(null != authUrl){
            Log.i(TAG, "authUrl " + authUrl);
            final Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, authUrl);
            startActivityForResult(intent, LOGIN_USER_REQUEST_CODE);
        } else {
            Log.i(TAG, "authUrl is null");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LOGIN_USER_REQUEST_CODE && data != null) {
                mPbLoading.setVisibility(View.VISIBLE);
                final Uri uri = Uri.parse(data.getStringExtra("KEY_URI"));
                userPublishSubject.onNext(uri);
                userPublishSubject.onCompleted();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Saves useful data via using {@link SharedPreferences}
     * @param sharedPreferences {@link SharedPreferences}
     * @param accessToken {@link AccessToken}
     */
    private void saveUserInfo(SharedPreferences sharedPreferences, AccessToken accessToken){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken()).apply();
        editor.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret()).apply();
        editor.putBoolean(IS_USER_LOGGED_IN, true).apply();
    }
}
