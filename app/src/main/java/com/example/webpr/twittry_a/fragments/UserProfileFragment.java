package com.example.webpr.twittry_a.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.webpr.twittry_a.R;
import com.example.webpr.twittry_a.managers.BitmapDownloader;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import twitter4j.User;

/**
 * Created by webpr on 04.01.2017.
 */

public class UserProfileFragment extends TwitterFragment{

    private static final String TAG = "UserProfileFragment";

    private User mUser;
    private ImageView mIvUserProfile;
    private TextView mTvUserName;

    private Subscription mUserImageSubscription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(null != getArguments()){
            mUser = (User) getArguments().getSerializable(USER_BUNDLE);
        }
    }

    public static UserProfileFragment newInstance(User user){
        Bundle args = new Bundle();
        args.putSerializable(USER_BUNDLE, user);

        UserProfileFragment userProfileFragment = new UserProfileFragment();
        userProfileFragment.setArguments(args);

        return userProfileFragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.user_profile;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIvUserProfile = (ImageView) view.findViewById(R.id.ivUserProfile);
        mTvUserName = (TextView) view.findViewById(R.id.tvUserName);

        PublishSubject<String> userImageStringPublishSubject = PublishSubject.create();
        mUserImageSubscription = userImageStringPublishSubject.observeOn(Schedulers.io())
                .map(new Func1<String, Bitmap>() {
                    @Override
                    public Bitmap call(String s) {
                        return BitmapDownloader.getBitmapFromURL(s);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        mIvUserProfile.setImageBitmap(bitmap);
                        mTvUserName.setText(mUser.getScreenName());
                    }
                });
        userImageStringPublishSubject.onNext(mUser.getBiggerProfileImageURL());
        userImageStringPublishSubject.onCompleted();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mUserImageSubscription != null && getActivity().isFinishing()){
            mUserImageSubscription.unsubscribe();
        }
    }
}
