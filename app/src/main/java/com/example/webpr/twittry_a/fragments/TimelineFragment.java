package com.example.webpr.twittry_a.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.webpr.twittry_a.MainActivity;
import com.example.webpr.twittry_a.R;
import com.example.webpr.twittry_a.TweetImageActivity;
import com.example.webpr.twittry_a.adapters.TweetsListAdapter;
import com.example.webpr.twittry_a.interfaces.OnTweetImageClickListener;
import com.example.webpr.twittry_a.managers.BitmapDownloader;
import com.example.webpr.twittry_a.models.Tweet;
import com.example.webpr.twittry_a.twitter.TwitterSingleton;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

/**
 * Created by webpr on 04.01.2017.
 */

public class TimelineFragment extends TwitterFragment implements OnTweetImageClickListener{

    private static final String TAG = "TimelineFragment";

    private RecyclerView mRecyclerView;
    private Subscription mUserTimeLineSubscription;
    private ProgressBar mProgressBar;
    private TweetsListAdapter mAdapter;
    private List<Tweet> mTweets;

    @Override
    protected int getLayout() {
        return R.layout.newsline;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTweets = new ArrayList<>();
        mAdapter = new TweetsListAdapter(mTweets, this);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        PublishSubject<AccessToken> userTimelinePublishSubject = PublishSubject.create();
        mUserTimeLineSubscription = userTimelinePublishSubject.observeOn(Schedulers.io())
                .map(new Func1<AccessToken, List<Status>>() {
                    @Override
                    public List<Status> call(AccessToken accessToken) {
                        try{
                            Twitter twitter = TwitterSingleton.getInstance().getAuthorizedTwitter(accessToken);
                            return twitter.getHomeTimeline();
                        } catch (TwitterException te){
                            te.printStackTrace();
                        }
                        return null;
                    }
                }).map(new Func1<List<Status>, List<Tweet>>() {
                    @Override
                    public List<Tweet> call(List<Status> statuses) {
                        List<Tweet> tweets = new ArrayList<Tweet>();
                        for(Status status: statuses){
                            Tweet.Builder builder = Tweet.newBuilder();
                            builder.setId(status.getId())
                                    .setUserName(status.getUser().getName())
                                    .setText(status.getText());
                                    for(MediaEntity mediaEntity: status.getMediaEntities()){
                                        String mediaImageUrl = mediaEntity.getMediaURL();
                                        if(mediaImageUrl.endsWith("jpg")){
                                            builder.setImageUrl(mediaImageUrl);
                                            builder.setImage(BitmapDownloader.getBitmapFromURL(mediaEntity.getMediaURL()));
                                            break;
                                        }
                                    }
                            tweets.add(builder.build());
                        }
                        return tweets;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Tweet>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if(mProgressBar.getVisibility() == View.VISIBLE){
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNext(List<Tweet> tweets) {
                        if(mProgressBar.getVisibility() == View.VISIBLE){
                            mProgressBar.setVisibility(View.GONE);
                        }
                        mTweets = tweets;
                        mAdapter.updateData(mTweets);
                    }
                });

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final AccessToken accessToken = new AccessToken(
                sharedPreferences.getString(MainActivity.PREF_KEY_OAUTH_TOKEN, null),
                sharedPreferences.getString(MainActivity.PREF_KEY_OAUTH_SECRET, null));
        userTimelinePublishSubject.onNext(accessToken);
        userTimelinePublishSubject.onCompleted();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mUserTimeLineSubscription != null && getActivity().isFinishing()){
            mUserTimeLineSubscription.unsubscribe();
        }
    }

    /**
     * Changes the mode of the tweets from short ru fullscreen and backward
     */
    public void changeViewMode(){
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//                super.getItemOffsets(outRect, view, parent, state);
                if(view.getVisibility() == View.VISIBLE){
                    Log.i(TAG, view.toString());
                }
            }
        });
    }

    @Override
    public void onTweetImageClick(String bitmapUrl) {
        Log.i(TAG, bitmapUrl.toString());
        Intent tweetImageIntent = new Intent(getActivity(), TweetImageActivity.class);
        tweetImageIntent.putExtra(TweetImageActivity.TWEET_IMAGE_SEPARATE_VIEW, bitmapUrl);
        startActivity(tweetImageIntent);
    }
}
