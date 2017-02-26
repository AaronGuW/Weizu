package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.aaronapplication.weizu.R;

/**
 * Created by Aaron on 2016/3/27.
 */
public class finishpaymentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.orderfinish, container, false);
        v.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        });

        return v;
    }

}
