package com.example.webpr.twittry_a.managers;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;

import com.example.webpr.twittry_a.interfaces.OnPageChangedCallback;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by webpr on 05.01.2017.
 */

public class PageTitleManager implements ViewPager.OnPageChangeListener{

    private List<String> mPageTitles;
    private WeakReference<OnPageChangedCallback> mOnPageTitleChangedCallbackWeakReference;
    private WeakReference<ImageButton> mImageButtonWeakReference;

    public PageTitleManager(List<String> mPageTitles, OnPageChangedCallback onPageChangedCallback, ImageButton imageButton) {
        this.mPageTitles = mPageTitles;
        this.mOnPageTitleChangedCallbackWeakReference = new WeakReference<OnPageChangedCallback>(onPageChangedCallback);
        this.mImageButtonWeakReference = new WeakReference<ImageButton>(imageButton);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if(position > 0){
            mImageButtonWeakReference.get().setVisibility(View.GONE);
        } else {
            mImageButtonWeakReference.get().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageSelected(int position) {
        mOnPageTitleChangedCallbackWeakReference.get().onPageTitleChanged(mPageTitles.get(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {}
}
