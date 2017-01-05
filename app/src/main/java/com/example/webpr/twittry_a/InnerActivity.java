package com.example.webpr.twittry_a;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.webpr.twittry_a.fragments.NewslineFragment;
import com.example.webpr.twittry_a.fragments.UserProfileFragment;
import com.example.webpr.twittry_a.interfaces.OnPageTitleChangedCallback;
import com.example.webpr.twittry_a.managers.PageTitleManager;
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

public class InnerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, OnPageTitleChangedCallback{

    private static final String TAG = "InnerActivity_TAG";

    private Subscription mLoadUserSubscription;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;
    private DrawerLayout mDrawerLayout;
    private TextView mTvPageTitle;
    private List<String> mPageTitles;
    private ViewPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inner);

        mPageTitles = new ArrayList<>();
        mPageTitles.add(getResources().getString(R.string.timeline));
        mPageTitles.add(getResources().getString(R.string.user_profile));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setContentInsetsAbsolute(0,0);


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

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
                                if(mProgressBar.getVisibility() == View.VISIBLE){
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onNext(User user) {
                                if(mProgressBar.getVisibility() == View.VISIBLE){
                                    mProgressBar.setVisibility(View.GONE);
                                }
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

        ImageButton ibDrawerControl = (ImageButton) findViewById(R.id.ibDrawerControl);
        ibDrawerControl.setOnClickListener(this);
        mTvPageTitle = (TextView) findViewById(R.id.tvPageTitle);

        if(null == mTvPageTitle.getText() || "".equals(mTvPageTitle.getText())){
            mTvPageTitle.setText(mPageTitles.get(0));
        }

        ImageButton ibDescription = (ImageButton) findViewById(R.id.ibDescription);
        ibDescription.setOnClickListener(this);
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

        public Fragment getNewslineFragment(){
            for(Fragment fragment: mFragments){
                if(fragment instanceof NewslineFragment){
                    return fragment;
                }
            }
            return null;
        }
    }

    private void initViewPagerWithAdapter(ViewPager viewPager, User user){
        PageTitleManager pageTitleManager = new PageTitleManager(mPageTitles, InnerActivity.this);

        viewPager.addOnPageChangeListener(pageTitleManager);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new NewslineFragment(), mPageTitles.get(0));
        mViewPagerAdapter.addFragment(UserProfileFragment.newInstance(user), mPageTitles.get(1));
        viewPager.setAdapter(mViewPagerAdapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_help:
                Log.i(TAG, "Menu help selected");
                break;
            case R.id.nav_logOut:
                Log.i(TAG, "Menu LogOut selected");
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.END)){
            mDrawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibDrawerControl:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.END)){
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                }
                break;
            case R.id.ibDescription:
                if(null != mViewPagerAdapter){
                    NewslineFragment newslineFragment = (NewslineFragment) mViewPagerAdapter.getNewslineFragment();
                    if(null != newslineFragment){
                        newslineFragment.changeViewMode();
                    }
                }
                break;
        }
    }

    @Override
    public void onPageTitleChanged(String pageTitle) {
        mTvPageTitle.setText(pageTitle);
    }
}
