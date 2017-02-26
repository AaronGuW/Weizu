package com.demo.aaronapplication.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.demo.aaronapplication.activity.OrderProcessActivity;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Aaron on 2016/3/28.
 */
public class orderlistFragment extends Fragment {

    private ListView finished, unfinished;
    private myBaseAdapter finished_adapter, unfinished_adapter;
    private LayoutInflater layoutInflater;
    private ArrayList<Order> finishedArraylist, unfinishedArraylist;
    private static final int stageText[] = { R.string.stage1, R.string.stage2, R.string.stage3, R.string.stage4, R.string.stage5,
                                        R.string.stage6_uncommented, R.string.stage6_leaser_commented, R.string.stage6_leasee_commented, R.string.stage6_both_commented };
    private int side;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.orderlistpage, container, false);
        finishedArraylist = new ArrayList<>();
        unfinishedArraylist = new ArrayList<>();
        finished = (ListView)v.findViewById(R.id.finished);
        unfinished =(ListView)v.findViewById(R.id.unfinished);

        Bundle bundle = getArguments();
        side = bundle.getInt("side");
        String raw = bundle.getString("orders");
        try {
            JSONArray jsonArray = new JSONArray(raw);
            for (int i = 0 ; i != jsonArray.length() ; ++i) {
                JSONObject o = jsonArray.getJSONObject(i);
                if (o.getInt("status") <= 1 ) {
                    unfinishedArraylist.add(new Order(o, true));
                } else {
                    finishedArraylist.add(new Order(o, true));
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }

        init_list();

        return v;
    }

    private void init_list() {
        layoutInflater = LayoutInflater.from(getActivity());
        finished_adapter = new myBaseAdapter(finishedArraylist);
        unfinished_adapter = new myBaseAdapter(unfinishedArraylist);
        finished.setAdapter(finished_adapter);
        unfinished.setAdapter(unfinished_adapter);
        finished.setOnItemClickListener(new myonItemClickListener(finishedArraylist));
        unfinished.setOnItemClickListener(new myonItemClickListener(unfinishedArraylist));
    }


    private class myBaseAdapter extends BaseAdapter {

        private ArrayList<Order> orderArrayList;

        public myBaseAdapter(ArrayList<Order> list) {
            orderArrayList = list;
        }

        @Override
        public int getCount() {
            return orderArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return orderArrayList.get(position);
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
                v = layoutInflater.inflate(R.layout.order_item, null);
            }
            Order order = orderArrayList.get(position);
            ((TextView)v.findViewById(R.id.order_name)).setText(order.getShortTitle());

            TextView hint_stage = (TextView)v.findViewById(R.id.order_stage);
            if (order.getStatus() == 0) {
                hint_stage.setText(getString(R.string.stage0));
            } else if (order.getStatus() == 2) {
                hint_stage.setText(getString(R.string.stageClose));
            } else {
                if (order.getStage() == 6) {
                    if (order.getLeasee_cid() == -1 && order.getLeaser_rate() == -1)
                        hint_stage.setText(getString(stageText[5]));
                    else if (order.getLeasee_cid() == -1)
                        hint_stage.setText(getString(stageText[6]));
                    else if (order.getLeaser_rate() == -1)
                        hint_stage.setText(getString(stageText[7]));
                    else
                        hint_stage.setText(getString(stageText[8]));
                } else {
                    hint_stage.setText(getString(stageText[order.getStage() - 1]));
                }
            }

            return v;
        }
    }

    private class myonItemClickListener implements AdapterView.OnItemClickListener {

        private ArrayList<Order> orderArrayList;

        public myonItemClickListener(ArrayList<Order> list) { orderArrayList = list; }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), OrderProcessActivity.class);
            intent.putExtra("oid",orderArrayList.get(position).getOid());
            intent.putExtra("side", side);
            startActivity(intent);
        }
    }

}
