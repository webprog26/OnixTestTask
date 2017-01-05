package com.example.webpr.twittry_a.managers;

import android.support.v4.view.ViewPager;

import com.example.webpr.twittry_a.interfaces.OnPageTitleChangedCallback;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by webpr on 05.01.2017.
 */

public class PageTitleManager implements ViewPager.OnPageChangeListener{

    private List<String> mPageTitles;
    private WeakReference<OnPageTitleChangedCallback> mOnPageTitleChangedCallbackWeakReference;

    public PageTitleManager(List<String> mPageTitles, OnPageTitleChangedCallback onPageTitleChangedCallback) {
        this.mPageTitles = mPageTitles;
        this.mOnPageTitleChangedCallbackWeakReference = new WeakReference<OnPageTitleChangedCallback>(onPageTitleChangedCallback);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        mOnPageTitleChangedCallbackWeakReference.get().onPageTitleChanged(mPageTitles.get(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {}
}
