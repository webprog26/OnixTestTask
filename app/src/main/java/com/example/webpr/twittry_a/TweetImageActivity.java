package com.example.webpr.twittry_a;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.webpr.twittry_a.managers.BitmapDownloader;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class TweetImageActivity extends AppCompatActivity {

    private static final String TAG = "TweetImageActivity_TAG";

    private Subscription mTweetImageSubscription;


    private SubsamplingScaleImageView mIvTweetImageSeparateView;
    private ProgressBar mProgressBar;


    public static final String TWEET_IMAGE_SEPARATE_VIEW = "com.example.webprog26.twittry_a.tweet_image_separate_view";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Image");



        mProgressBar = (ProgressBar) findViewById(R.id.pbTweetImageLoading);
        mIvTweetImageSeparateView = (SubsamplingScaleImageView) findViewById(R.id.ivTweetImageSeparateView);
        mIvTweetImageSeparateView.setZoomEnabled(true);
        mIvTweetImageSeparateView.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);

        if(null != getIntent()){
            String tweetImageBitmapUrl = getIntent().getStringExtra(TWEET_IMAGE_SEPARATE_VIEW);
            Log.i(TAG, "bitmap url received via Intent " + tweetImageBitmapUrl);
            if(null != tweetImageBitmapUrl){
                PublishSubject<String> tweetImagePublishSubject = PublishSubject.create();
                mTweetImageSubscription = tweetImagePublishSubject.observeOn(Schedulers.io())
                        .map(new Func1<String, Bitmap>() {
                            @Override
                            public Bitmap call(String s) {
                                return BitmapDownloader.getBitmapFromURL(s);
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Bitmap>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "onCompleted()");
                            }

                            @Override
                            public void onError(Throwable e) {
                                if(mProgressBar.getVisibility() == View.VISIBLE){
                                    mProgressBar.setVisibility(View.GONE);
                                }
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Bitmap bitmap) {
                                if(mProgressBar.getVisibility() == View.VISIBLE){
                                    mProgressBar.setVisibility(View.GONE);
                                }
                                Log.i(TAG, "onNext(Bitmap bitmap)");
                                mIvTweetImageSeparateView.setImage(ImageSource.bitmap(bitmap));
                            }
                        });
                        tweetImagePublishSubject.onNext(tweetImageBitmapUrl);
                        tweetImagePublishSubject.onCompleted();
            } else {
                Log.i(TAG, "bitmap url is null");
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mTweetImageSubscription != null && isFinishing()){
            mTweetImageSubscription.unsubscribe();
        }
    }
}
