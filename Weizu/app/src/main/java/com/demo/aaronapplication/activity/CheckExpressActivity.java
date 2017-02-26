package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.DBManager;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.kdniao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aaron on 2016/9/1.
 */

public class CheckExpressActivity extends Activity implements View.OnClickListener{

    private static final String ONROAD = "在途", SIGNED = "已签收", PROBLEM = "问题件";

    private LinearLayout traceList;
    private LayoutInflater inflater;
    private String expressInfo;
    private Handler expressHandler;

    private String expCode, expNo, company;

    private DBManager database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expresspage);

        database = new DBManager(this);

        findViewById(R.id.back_btn).setOnClickListener(this);
        traceList = (LinearLayout) findViewById(R.id.traces);
        inflater = LayoutInflater.from(this);

        Intent intent = getIntent();
        expCode = intent.getStringExtra("code");
        expNo = intent.getStringExtra("no");
        company = intent.getStringExtra("company");
        Log.e("company",company);

        ((TextView)findViewById(R.id.company)).setText("承运公司: " + company);
        ((TextView)findViewById(R.id.expNo)).setText("运单编号: " + expNo);

        expressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleExpressMessage(msg);
            }
        };
        getExpressInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
        }
    }

    private void getExpressInfo() {
        if (database.canQueryExpress(expNo)) {
            kdniao.getOrderTracesByJson(expCode,expNo,expressHandler);
        } else {
            expressInfo = database.getExpressInfo(expNo);
            Message msg = expressHandler.obtainMessage();
            msg.what = 0;
            msg.obj = "1";
            msg.sendToTarget();
        }
    }

    private void loadExpressInfo() {
        try {
            JSONObject json = new JSONObject(expressInfo);
            boolean success = json.getBoolean("Success");
            if (success) {
                switch (json.getInt("State")) {
                    case 2:
                        ((TextView)findViewById(R.id.state)).setText("物流状态: " + ONROAD);
                        break;
                    case 3:
                        ((TextView)findViewById(R.id.state)).setText("物流状态: " + SIGNED);
                        break;
                    case 4:
                        ((TextView)findViewById(R.id.state)).setText("物流状态: " + PROBLEM);
                        break;
                }
                String strtraces = json.getString("Traces");
                JSONArray traces = new JSONArray(strtraces);
                for (int i = 0 ; i != traces.length(); ++i) {
                    JSONObject trace = traces.getJSONObject(i);
                    View traceEntry = inflater.inflate(R.layout.expressstatus, null);
                    TextView info = (TextView) traceEntry.findViewById(R.id.info);
                    TextView remark = (TextView) traceEntry.findViewById(R.id.remark);
                    TextView time = (TextView) traceEntry.findViewById(R.id.time);
                    if (i == traces.length() - 1) {
                        ((ImageView) traceEntry.findViewById(R.id.dot)).setImageDrawable(getResources().getDrawable(R.drawable.ongoing));
                        info.setTextColor(Color.parseColor("#00cc00"));
                        remark.setTextColor(Color.parseColor("#00cc00"));
                        time.setTextColor(Color.parseColor("#00cc00"));
                    }
                    if (trace.has("Remark")) {
                        remark.setVisibility(View.VISIBLE);
                        remark.setText(trace.getString("Remark"));
                    }
                    info.setText(trace.getString("AcceptStation"));
                    time.setText(trace.getString("AcceptTime"));

                    traceList.addView(traceEntry,0);
                }
            } else {
                Toast.makeText(CheckExpressActivity.this, json.getString("Reason"),Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException Je) {
            Je.printStackTrace();
        }
    }

    private void handleExpressMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (res.equals("1")) {
                    loadExpressInfo();
                    Toast.makeText(CheckExpressActivity.this, getString(R.string.warning_frequency),Toast.LENGTH_SHORT).show();
                } else {
                    expressInfo = res;
                    database.saveExpressInfo(expNo, expressInfo);
                    loadExpressInfo();
                }
            } else {
                Toast.makeText(CheckExpressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
