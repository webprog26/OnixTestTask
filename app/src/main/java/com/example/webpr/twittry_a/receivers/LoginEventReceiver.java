package com.example.webpr.twittry_a.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.webpr.twittry_a.MainActivity;
import com.example.webpr.twittry_a.interfaces.OnUserLoginListener;
import com.example.webpr.twittry_a.services.LoginIntentService;

import java.lang.ref.WeakReference;

/**
 * Created by webpr on 03.01.2017.
 */

public class LoginEventReceiver extends BroadcastReceiver {

    private WeakReference<OnUserLoginListener> mLoginListenerWeakReference;

    public LoginEventReceiver(OnUserLoginListener loginListener) {
        this.mLoginListenerWeakReference = new WeakReference<OnUserLoginListener>(loginListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(MainActivity.LOGIN_TO_TWITTER_ACTION.equals(intent.getAction())){
            mLoginListenerWeakReference.get().onUserLogin(intent.getStringExtra(LoginIntentService.TWITTER_AUTH_URL));
        }
    }
}
