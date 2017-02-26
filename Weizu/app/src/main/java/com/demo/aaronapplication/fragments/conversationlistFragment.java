package com.demo.aaronapplication.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.aaronapplication.weizu.R;

/**
 * Created by Aaron on 2016/9/5.
 */
public class conversationlistFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversationlist, container, false);
        return v;
    }
}
