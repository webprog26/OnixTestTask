package com.example.webpr.twittry_a;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.webpr.twittry_a.fragments.NewslineFragment;
import com.example.webpr.twittry_a.fragments.UserProfileFragment;
import com.example.webpr.twittry_a.twitter.TwitterSingleton;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class InnerActivity extends AppCompatActivity {

    private static final String TAG = "InnerActivity_TAG";

    private Subscription mLoadUserSubscription;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inner);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        if(null != getIntent()){
            User user = (User) getIntent().getSerializableExtra(MainActivity.LOGGED_IN_USER);
            if(null != user){
               initViewPagerWithAdapter(mViewPager, user);
               mTabLayout.setupWithViewPager(mViewPager);
            } else {
                PublishSubject<AccessToken> accessTokenPublishSubject = PublishSubject.create();
                mLoadUserSubscription = accessTokenPublishSubject.observeOn(Schedulers.io())
                        .map(new Func1<AccessToken, User>() {
                            @Override
                            public User call(AccessToken accessToken) {
                                try{
                                    return TwitterSingleton.getInstance().getAuthorizedTwitter(accessToken).showUser(accessToken.getUserId());
                                } catch (TwitterException te){
                                    te.printStackTrace();
                                }
                                return null;
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<User>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(User user) {
                                if(null != user){
                                    initViewPagerWithAdapter(mViewPager, user);
                                    mTabLayout.setupWithViewPager(mViewPager);
                                } else {
                                    Log.i(TAG, "user = null");
                                }
                            }
                        });

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(InnerActivity.this);
                final AccessToken accessToken = new AccessToken(
                        sharedPreferences.getString(MainActivity.PREF_KEY_OAUTH_TOKEN, null),
                        sharedPreferences.getString(MainActivity.PREF_KEY_OAUTH_SECRET, null));
                accessTokenPublishSubject.onNext(accessToken);
                accessTokenPublishSubject.onCompleted();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLoadUserSubscription != null && isFinishing()){
            mLoadUserSubscription.unsubscribe();
        }
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter{

        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentsTitles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentsTitles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentsTitles.get(position);
        }
    }

    private void initViewPagerWithAdapter(ViewPager viewPager, User user){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(NewslineFragment.newInstance(user), "Newsline");
        adapter.addFragment(UserProfileFragment.newInstance(user), "UserrProfile");
        viewPager.setAdapter(adapter);
    }
}
