package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.aaronapplication.weizu.R;

/**
 * Created by Aaron on 2016/3/27.
 */
public class releaseokFragment extends Fragment {

    private View confirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.releasefinish, container, false);
        confirm = v.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent action = new Intent();
                action.putExtra("modify",true);
                getActivity().setResult(Activity.RESULT_OK, action);
                getActivity().finish();
            }
        });

        return v;
    }
}
