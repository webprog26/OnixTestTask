package com.example.webpr.twittry_a.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by webpr on 04.01.2017.
 */

public abstract class TwitterFragment extends Fragment {

    public static final String USER_BUNDLE = "com.example.webprog26.twittry_a.user_bundle";

    protected abstract int getLayout();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layout = getLayout();
        return inflater.inflate(layout, container, false);
    }
}
