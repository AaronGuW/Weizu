package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.activity.mainActivity;
import com.demo.aaronapplication.weizu.DBManager;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aaron on 2016/3/23.
 */
public class searchFragment extends Fragment implements View.OnClickListener {

    private int action;

    //The embedded fragments of this fragment are history and result
    private historyFragment history_component;
    private resultFragment result_component;

    private EditText search_input;
    private View back_btn, erase_btn, search_btn;

    private DBManager database;

    //requestcode 1 back, 2 search
    public static final int BACK = 1, SEARCH = 2;
    private onHeadlineClickListener HeadlineClickListener;

    private Handler resultHandler;

    public interface onHeadlineClickListener {
        void onSearchHeadlineClicked(int requestcode, String data);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            HeadlineClickListener = (onHeadlineClickListener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout)inflater.inflate(R.layout.searchpage, container, false);

        Bundle param = getArguments();
        action = param.getInt("action");

        //assign the views
        search_input = (EditText)v.findViewById(R.id.search_input);
        search_input.setFocusable(true);
        search_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm =  (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(),0);
                    }
                    String key = search_input.getText().toString();
                    if (key.length() == 0) {
                        Toast.makeText(getActivity(),"请输入您要搜索的关键词", Toast.LENGTH_SHORT).show();
                    } else {
                        database.save_key(key, System.currentTimeMillis());
                        search(key);
                    }
                }
                return false;
            }
        });
        back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);
        search_btn = v.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);
        erase_btn = v.findViewById(R.id.erase_btn);
        erase_btn.setOnClickListener(this);

        database = new DBManager(getActivity());

        resultHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleResultMessage(msg);
            }
        };

        //according to the action, set the corresponding content
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (action == mainActivity.INPUT) {
            history_component = new historyFragment();
            transaction.replace(R.id.searchpage_content, history_component);
        } else if (action == mainActivity.PICTURE) {
            searchPic(param.getString("photo"));
        } else if (action == mainActivity.SHOWRESULT) {
            result_component = new resultFragment();
            Bundle bundle = new Bundle();
            bundle.putString("res", getArguments().getString("res"));
            result_component.setArguments(bundle);
            transaction.replace(R.id.searchpage_content, result_component);
        }
        transaction.commit();

        return v;
    }

    /**
     * change the content, meanwhile hide the keyboard if it's active
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                InputMethodManager imm =  (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(),0);
                }
                HeadlineClickListener.onSearchHeadlineClicked(BACK, null);
                break;
            case R.id.erase_btn:
                search_input.setText("");
                break;
            case R.id.search_btn:
                InputMethodManager imm2 =  (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (imm2.isActive()) {
                    imm2.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(),0);
                }
                String key = search_input.getText().toString();
                if (key.length() == 0) {
                    Toast.makeText(getActivity(),"请输入您要搜索的关键词", Toast.LENGTH_SHORT).show();
                } else {
                    database.save_key(key, System.currentTimeMillis());
                    search(key);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }

    private void search(String keyword) {
        action = mainActivity.INPUT;
        try {
            JSONObject param = new JSONObject();
            param.put("method", "keyword");
            param.put("key", Base64.encodeToString(keyword.getBytes(), Base64.DEFAULT));
            HttpUtil.JSONHttpPOST(HttpUtil.host + "s", param , resultHandler);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    private void searchPic(String photo) {
        action = mainActivity.SHOWRESULT;
        try {
            JSONObject param = new JSONObject();
            param.put("method", "image");
            param.put("key", photo);
            HttpUtil.JSONHttpPOST(HttpUtil.host + "s", param , resultHandler);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    private void handleResultMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                result_component = new resultFragment();
                Bundle bundle = new Bundle();
                bundle.putString("res", res);
                result_component.setArguments(bundle);
                transaction.replace(R.id.searchpage_content, result_component);
                transaction.commit();
            } else {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
