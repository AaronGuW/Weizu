package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.fragments.orderlistFragment;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.Order;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONObject;

/**
 * Created by Aaron on 2016/3/28.
 */
public class MyOrdersAcitivity extends Activity implements View.OnClickListener{

    private static final int LESSOR = 0, LESSEE = 1;

    private TextView leaser, leasee;

    private orderlistFragment leaser_view, leasee_view;
    private int cur_state;

    private Handler orderHandler;
    private String rawAsLeasee, rawAsLeaser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myorderspage);
        findViewById(R.id.back_btn).setOnClickListener(this);

        orderHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleOrderMessage(msg);
            }
        };

        leaser = (TextView)findViewById(R.id.lessor_view);
        leaser.setOnClickListener(this);
        leaser.setClickable(false);
        leasee = (TextView)findViewById(R.id.lessee_view);
        leasee.setOnClickListener(this);

        cur_state = LESSEE;
        getOrders();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.lessee_view:
                if (cur_state != LESSEE) {
                    update_textColor(LESSOR);
                    cur_state = LESSEE;
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    leasee_view = new orderlistFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("side",Order.LEASEE);
                    bundle.putString("orders", rawAsLeasee);
                    leasee_view.setArguments(bundle);
                    transaction.replace(R.id.mainframe, leasee_view);
                    transaction.commit();
                }
                break;
            case R.id.lessor_view:
                if (cur_state != LESSOR) {
                    update_textColor(LESSEE);
                    cur_state = LESSOR;
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    leaser_view = new orderlistFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("side",Order.LEASER);
                    bundle.putString("orders", rawAsLeaser);
                    leaser_view.setArguments(bundle);
                    transaction.replace(R.id.mainframe, leaser_view);
                    transaction.commit();
                }
                break;
        }
    }

    private void update_textColor(int current) {
        if (current == LESSEE) {
            leaser.setTextColor(0xffe8591a);
            leasee.setTextColor(0xff898989);
        } else {
            leasee.setTextColor(0xffe8591a);
            leaser.setTextColor(0xff898989);
        }
    }

    private void getOrders() {
        HttpUtil.HttpClientGET(HttpUtil.host+"order?action=get&uid="+getSharedPreferences("account", MODE_PRIVATE).getString("uid","0"), orderHandler);
    }

    private void handleOrderMessage(Message msg) {
        try {
            if (msg.what == 0) {
                JSONObject res = new JSONObject(msg.obj.toString());
                if (res.getInt("status") == 1) {
                    rawAsLeasee = res.getString("asleasee");
                    rawAsLeaser = res.getString("asleaser");

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    leasee_view = new orderlistFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("side",Order.LEASEE);
                    bundle.putString("orders", rawAsLeasee);
                    leasee_view.setArguments(bundle);
                    transaction.replace(R.id.mainframe, leasee_view);
                    transaction.commit();

                    leasee.setClickable(true);
                    leaser.setClickable(true);
                }
            } else {
                Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
