package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.DBManager;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Aaron on 2016/3/23.
 */
public class historyFragment extends Fragment {

    private DBManager database;

    private ListView hotlist,historylist;
    private ArrayList<String> hotkeys, historykeys;
    private BaseAdapter hotadapter, historyadapter;
    private LayoutInflater layoutinflater;

    private Handler hotKeysHandler;

    private historylistener hlistener;

    public interface historylistener {
        public void fillinput(String key);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            hlistener = (historylistener)activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.historypage, container, false);

        hotKeysHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleHotKeyMessage(msg);
            }
        };

        database = new DBManager(getActivity());
        hotkeys = new ArrayList<>();
        historykeys = new ArrayList<>();
        database.get_search_history(historykeys, 10);

        layoutinflater = LayoutInflater.from(getActivity());
        hotlist = (ListView)v.findViewById(R.id.hot_search);
        historylist = (ListView)v.findViewById(R.id.search_history);
        init_listview();
        getHotKeys();
        return v;
    }

    public void init_listview() {
        hotadapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return hotkeys.size();
            }

            @Override
            public Object getItem(int position) {
                return hotkeys.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                if (convertView != null) {
                    v = convertView;
                } else {
                    v = layoutinflater.inflate(R.layout.keyhistory,null);
                }

                ((TextView)v.findViewById(R.id.key)).setText(hotkeys.get(position));
                return v;
            }
        };
        hotlist.setAdapter(hotadapter);
        hotlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hlistener.fillinput(hotkeys.get(position));
            }
        });

        historyadapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return historykeys.size();
            }

            @Override
            public Object getItem(int position) {
                return historykeys.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                if (convertView != null) {
                    v = convertView;
                } else {
                    v = layoutinflater.inflate(R.layout.keyhistory,null);
                }

                ((TextView)v.findViewById(R.id.key)).setText(historykeys.get(position));
                return v;
            }
        };
        historylist.setAdapter(historyadapter);
        historylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hlistener.fillinput(historykeys.get(position));
            }
        });
    }

    private void getHotKeys() {
        HttpUtil.HttpClientGET(HttpUtil.host+"s", hotKeysHandler);
    }

    private void handleHotKeyMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                String keys[] = res.split("#");
                for (String key: keys) {
                    if (key.length() > 0) {
                        hotkeys.add(key);
                    }
                }
                hotadapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "获取热搜失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
