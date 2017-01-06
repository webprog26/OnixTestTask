package com.example.webpr.twittry_a;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.webpr.twittry_a.services.LoginIntentService;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    public static String EXTRA_URL = "extra_url";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        final String url = this.getIntent().getStringExtra(EXTRA_URL);
        if (null == url) {
            Log.e("Twitter", "URL cannot be null");
            finish();
        }

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                if( url.contains(LoginIntentService.TWITTER_CALLBACK_URL)){
                    Uri uri = Uri.parse(url);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("KEY_URI", uri.toString());
                    setResult(RESULT_OK, resultIntent);
                /* closing webview */
                    finish();
                    return true;
                }
                return false;
            }

        });
        webView.loadUrl(url);
    }
}
