package com.demo.aaronapplication.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class categoryFragment extends Fragment implements View.OnClickListener {

    private int cur_father_class, child_class;
    private static final int BOOKS = 0, ELECTRONICS = 1, TRANSPORTATION = 2, CLOTHES = 3, SPORTS = 4, CHEMICAL = 5, ACTIVITIES = 6, TIME = 7, SKILL = 8, OTHERS = 9;

    //clickable linearlayout as button
    private View[] father,children;
    //icons and names of children, modify them when father class changes
    private ImageView[] child_icons;
    private TextView[] child_names;
    private int side;
    //ids to be used for initialization
    private static final int ids[] = { R.id.child1, R.id.child1_icon, R.id.child1_name, R.id.child2, R.id.child2_icon, R.id.child2_name,
                                   R.id.child3, R.id.child3_icon, R.id.child3_name, R.id.child4, R.id.child4_icon, R.id.child4_name,
                                   R.id.child5, R.id.child5_icon, R.id.child5_name, R.id.child6, R.id.child6_icon, R.id.child6_name };
    private static final int father_ids[] = { R.id.books, R.id.electronics, R.id.transportation, R.id.clothes, R.id.sports, R.id.chemical, R.id.activities, R.id.time, R.id.skill, R.id.other };
    private static final int namestrid[][] = {
            { R.string.math, R.string.computer, R.string.construction, R.string.architecture, R.string.english, R.string.politics },
            { R.string.cellphone, R.string.laptop, R.string.SLR, R.string.tablet, R.string.ebook, R.string.headphone },
            { R.string.bike, R.string.unibike, R.string.motionbike, R.string.auto, R.string.skateboard, R.string.roller },
            { R.string.costume, R.string.schooluniform, R.string.suit, R.string.eveningdressing, R.string.cosplay, R.string.dailyoutfit },
            { R.string.balls, R.string.team, R.string.fitness, R.string.outdoor, R.string.accessory, R.string.wearable },
            { R.string.makeup, R.string.makeuptools, R.string.hairdressing, R.string.nail, R.string.bodycare, R.string.scream },
            { R.string.tent, R.string.portablefurniture, R.string.audio, R.string.boardgame, R.string.BBQ, R.string.daily },
            { R.string.mathtutor, R.string.pingpang, R.string.attendance, R.string.shopping, R.string.exercise, R.string.fitting},
            { R.string.swim, R.string.ppt, R.string.ps, R.string.project, R.string.date, R.string.poster},
            { R.string.audiovisual, R.string.kitchen, R.string.software, R.string.musicalinstrument, R.string.appliance, R.string.office}};
    private static final int icondrawableid[][] = {
            { R.drawable.math, R.drawable.computer, R.drawable.construction, R.drawable.architecture, R.drawable.english, R.drawable.politics },
            { R.drawable.ceilphone, R.drawable.laptop, R.drawable.slr, R.drawable.tablet, R.drawable.ebook, R.drawable.headphone },
            { R.drawable.bike, R.drawable.unibike, R.drawable.motionbike, R.drawable.auto, R.drawable.skateboard, R.drawable.roller },
            { R.drawable.costume, R.drawable.schooluniform, R.drawable.suit, R.drawable.eveningdressing, R.drawable.cosplay, R.drawable.dailyoutfit },
            { R.drawable.balls, R.drawable.team, R.drawable.fitness, R.drawable.outdoor, R.drawable.accessory, R.drawable.wearable },
            { R.drawable.makeup, R.drawable.makeuptools, R.drawable.hairdressing, R.drawable.nail, R.drawable.bodycare, R.drawable.scream },
            { R.drawable.tent, R.drawable.portablefurniture, R.drawable.audio, R.drawable.boardgame, R.drawable.bbq, R.drawable.daily },
            { R.drawable.mathtutor, R.drawable.pingpang, R.drawable.attendence, R.drawable.shopping, R.drawable.exercise, R.drawable.fitting },
            { R.drawable.swim, R.drawable.ppt, R.drawable.ps, R.drawable.project, R.drawable.date, R.drawable.poster },
            { R.drawable.audiovisual, R.drawable.kitchen, R.drawable.software, R.drawable.musicalinstrument, R.drawable.appliance, R.drawable.office }
    };

    private Handler resultHandler;
    private onTypeSearchListener searchListener;

    public interface onTypeSearchListener {
        void onTypeSearch(String res);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            searchListener = (onTypeSearchListener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.categorypage, container, false);
        resultHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleResultMessage(msg);
            }
        };
        father = new View[10];
        children = new View[6];
        child_icons = new ImageView[6];
        child_names = new TextView[6];
        for (int i = 0 ; i != 6 ; ++i) {
            children[i] = v.findViewById(ids[i*3]);
            child_icons[i] = (ImageView)v.findViewById(ids[i*3+1]);
            child_names[i] = (TextView)v.findViewById(ids[i*3+2]);
            children[i].setOnClickListener(this);
        }
        for (int i = 0 ; i != father_ids.length ; ++i) {
            father[i] = v.findViewById(father_ids[i]);
            father[i].setOnClickListener(this);
        }

        cur_father_class = BOOKS;
        side = UIUtil.dp2px(getActivity(), 80);
        for (int i = 0 ; i != 6 ; ++i) {
            child_names[i].setText(getString(namestrid[cur_father_class][i]));
            Picasso.with(getActivity()).load(icondrawableid[cur_father_class][i]).resize(side,side).centerInside().into(child_icons[i]);
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.books:
                switchCategory(BOOKS);
                break;
            case R.id.electronics:
                switchCategory(ELECTRONICS);
                break;
            case R.id.transportation:
                switchCategory(TRANSPORTATION);
                break;
            case R.id.clothes:
                switchCategory(CLOTHES);
                break;
            case R.id.sports:
                switchCategory(SPORTS);
                break;
            case R.id.chemical:
                switchCategory(CHEMICAL);
                break;
            case R.id.activities:
                switchCategory(ACTIVITIES);
                break;
            case R.id.time:
                switchCategory(TIME);
                break;
            case R.id.skill:
                switchCategory(SKILL);
                break;
            case R.id.other:
                switchCategory(OTHERS);
                break;

            case R.id.child1:
                searchByType(0);
                break;
            case R.id.child2:
                searchByType(1);
                break;
            case R.id.child3:
                searchByType(2);
                break;
            case R.id.child4:
                searchByType(3);
                break;
            case R.id.child5:
                searchByType(4);
                break;
            case R.id.child6:
                searchByType(5);
                break;
        }
    }

    /**
     * refresh view when category changes
     * @param category new category
     */
    private void switchCategory(int category) {
        if (cur_father_class != category) {
            father[cur_father_class].setBackground(getResources().getDrawable(R.drawable.sqrbg));
            father[category].setBackground(getResources().getDrawable(R.drawable.chosen));
            cur_father_class = category;
            for (int i = 0 ; i != 6 ; ++i) {
                child_names[i].setText(getString(namestrid[category][i]));
                Picasso.with(getActivity()).load(icondrawableid[category][i]).resize(side,side).centerInside().into(child_icons[i]);
                //child_icons[i].setImageDrawable(getResources().getDrawable(icondrawableid[category][i]));
            }
        }
    }

    private void searchByType(int childType) {
        try {
            JSONObject params = new JSONObject();
            params.put("method","type");
            params.put("father", cur_father_class);
            params.put("child", childType);
            HttpUtil.JSONHttpPOST(HttpUtil.host+"s", params, resultHandler);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    private void handleResultMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                searchListener.onTypeSearch(res);
                Log.e("result", res);
            } else {
                Toast.makeText(getActivity(),"网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
