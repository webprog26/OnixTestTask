package com.example.webpr.twittry_a.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.webpr.twittry_a.MainActivity;
import com.example.webpr.twittry_a.R;
import com.example.webpr.twittry_a.adapters.TweetsListAdapter;
import com.example.webpr.twittry_a.adapters.TweetsListAdapterFull;
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

public class NewslineFragment extends TwitterFragment {

    private static final String TAG = "NewslineFragment";

    private RecyclerView mRecyclerView;
    private Subscription mUserTimeLineSubscription;
    private ProgressBar mProgressBar;
    private List<Tweet> mTweetList;
    private boolean isInFullView = false;

    @Override
    protected int getLayout() {
        return R.layout.newsline;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTweetList = new ArrayList<>();

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                                    Log.i(TAG, status.getText());
                                    for(MediaEntity mediaEntity: status.getMediaEntities()){
                                        if(mediaEntity.getMediaURL().endsWith("jpg")){
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
                        mTweetList = tweets;
                        TweetsListAdapter adapter = new TweetsListAdapter(mTweetList);
                        mRecyclerView.setAdapter(adapter);
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

    public void changeViewMode(){
        if(!isInFullView){
            TweetsListAdapterFull adapter = new TweetsListAdapterFull(mTweetList);
            mRecyclerView.setAdapter(adapter);
            isInFullView = true;
        } else {
            TweetsListAdapter adapter = new TweetsListAdapter(mTweetList);
            mRecyclerView.setAdapter(adapter);
            isInFullView = false;
        }
    }
}
