package com.example.webpr.twittry_a.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.webpr.twittry_a.InnerActivity;
import com.example.webpr.twittry_a.MainActivity;
import com.example.webpr.twittry_a.R;
import com.example.webpr.twittry_a.twitter.TwitterSingleton;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
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

    private User mUser;
    private Subscription mUserTimeLineSubscription;

    public static NewslineFragment newInstance(User user){
        Bundle args = new Bundle();
        args.putSerializable(USER_BUNDLE, user);

        NewslineFragment newslineFragment = new NewslineFragment();
        newslineFragment.setArguments(args);

        return newslineFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(null != getArguments()){
            mUser = (User) getArguments().getSerializable(USER_BUNDLE);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.newsline;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Status>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Status> statuses) {
                        for(Status status: statuses){
                            Log.i(TAG, status.getUser().getName());
                        }
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
}
