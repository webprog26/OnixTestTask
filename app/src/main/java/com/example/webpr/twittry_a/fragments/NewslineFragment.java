package com.example.webpr.twittry_a.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.webpr.twittry_a.InnerActivity;
import com.example.webpr.twittry_a.MainActivity;
import com.example.webpr.twittry_a.R;
import com.example.webpr.twittry_a.managers.BitmapDownloader;
import com.example.webpr.twittry_a.models.Tweet;
import com.example.webpr.twittry_a.twitter.TwitterSingleton;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
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
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * Created by webpr on 04.01.2017.
 */

public class NewslineFragment extends TwitterFragment {

    private static final String TAG = "NewslineFragment";

    private ListView mListView;
    private Subscription mUserTimeLineSubscription;
    private ProgressBar mProgressBar;

    @Override
    protected int getLayout() {
        return R.layout.newsline;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mListView = (ListView) view.findViewById(R.id.listView);

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
                        ListViewAdapter adapter = new ListViewAdapter(getActivity(), tweets);
                        mListView.setAdapter(adapter);
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

    private static class ListViewAdapter extends ArrayAdapter<Tweet>{

        public ListViewAdapter(Context context, List<Tweet> tweets) {
            super(context, 0, tweets);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.tweet_item, null);
            }

            TextView tvUserName = (TextView) convertView.findViewById(R.id.tvTweetUserName);
            TextView tvTweetText = (TextView) convertView.findViewById(R.id.tvTweetText);
            ImageView ivTweetImage = (ImageView) convertView.findViewById(R.id.ivTweetImage);

            Tweet tweet = getItem(position);

            tvUserName.setText(tweet.getUserName());
            tvTweetText.setText(tweet.getText());
            ivTweetImage.setImageBitmap(tweet.getImage());

            return convertView;
        }
    }
}
