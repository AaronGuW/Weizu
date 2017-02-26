package com.demo.aaronapplication.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.demo.aaronapplication.fragments.confirmorderFragment;
import com.demo.aaronapplication.fragments.confirmpaymentFragment;
import com.demo.aaronapplication.fragments.finishpaymentFragment;
import com.demo.aaronapplication.weizu.Address;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.goods;

/**
 * Created by Aaron on 2016/3/26.
 */
public class tradeActivity extends FragmentActivity implements confirmpaymentFragment.onConfirmpaymentListener, confirmorderFragment.onConfirmOrderListener {

    private confirmorderFragment confirmorder_page;
    private TextView headline;
    private View back;
    private goods target;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintradepage);
        headline = (TextView)findViewById(R.id.headline);
        back = findViewById(R.id.back_btn);
        target = (goods)(getIntent().getSerializableExtra("goods"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 1 && fm.getBackStackEntryCount() < 3) {
                    fm.popBackStack();
                    fm.executePendingTransactions();
                } else {
                    setResult(0);
                    finish();
                }
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        confirmorder_page = new confirmorderFragment();
        Bundle goods = new Bundle();
        goods.putSerializable("goods",target);
        goods.putInt("amount", getIntent().getIntExtra("amount",1));
        confirmorder_page.setArguments(goods);
        transaction.replace(R.id.mainframe, confirmorder_page);
        transaction.addToBackStack("confirmo");
        transaction.commit();
        headline.setText(getResources().getText(R.string.confirmorder));
    }

    @Override
    public void onConfirmpayment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.mainframe, new finishpaymentFragment());
        transaction.addToBackStack("finish");
        transaction.commit();
    }

    @Override
    public void onConfirmOrder(String note, Address address, int amount, int renttime, int rcv, int rtn) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        confirmpaymentFragment confirmpayment = new confirmpaymentFragment();
        Bundle param = new Bundle();
        param.putSerializable("goods",target);
        param.putSerializable("address", address);
        param.putString("note", note);
        param.putInt("amount", amount);
        param.putInt("time", renttime);
        param.putInt("rcv", rcv);
        param.putInt("rtn", rtn);
        confirmpayment.setArguments(param);
        transaction.replace(R.id.mainframe, confirmpayment);
        transaction.addToBackStack("confirmp");
        transaction.commit();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 1 && fm.getBackStackEntryCount() < 3) {
                fm.popBackStack();
                fm.executePendingTransactions();
                return true;
            } else {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
