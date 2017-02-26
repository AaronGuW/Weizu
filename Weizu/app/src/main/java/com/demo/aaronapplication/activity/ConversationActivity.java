package com.demo.aaronapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.demo.aaronapplication.weizu.R;

/**
 * Created by Aaron on 2016/9/12.
 */
public class ConversationActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);
        ((TextView)findViewById(R.id.username)).setText(getIntent().getData().getQueryParameter("title"));
        findViewById(R.id.back_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                if (isTaskRoot()) {
                    startActivity(new Intent(ConversationActivity.this, mainActivity.class));
                    ConversationActivity.this.finish();
                } else {
                    ConversationActivity.this.finish();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (isTaskRoot()) {
                startActivity(new Intent(ConversationActivity.this, mainActivity.class));
                ConversationActivity.this.finish();
            } else {
                ConversationActivity.this.finish();
            }
            return false;
        } else {
            return super.onKeyDown(keyCode,event);
        }
    }
}
