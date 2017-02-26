package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.fragments.newreleaseFragment;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;

/**
 * Created by Aaron on 2016/3/26.
 */
public class ReleaseActivity extends Activity implements View.OnClickListener {

    //Intent type
    public static final int NEW = 0, MODIFY = 1, DELETE = 2, GET = 3, SIMPLE = 4, FULL = 5;
    private int action;

    private newreleaseFragment newrelease_page;

    private TextView headline;
    private View back_btn;
    private View delete_btn;

    private int gid;

    private Handler deleteHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintradepage);
        headline = (TextView)findViewById(R.id.headline);
        delete_btn = findViewById(R.id.delete);
        delete_btn.setOnClickListener(this);

        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);

        deleteHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleDeleteMessage(msg);
            }
        };

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Intent intent = getIntent();
        action = intent.getIntExtra("action",0);

        Bundle param = new Bundle();
        param.putInt("action",action);

        switch (action) {
            case NEW:
                headline.setText("发布订单");
                delete_btn.setVisibility(View.INVISIBLE);
                delete_btn.setClickable(false);
                break;
            case MODIFY:
                headline.setText("修改订单");
                delete_btn.setVisibility(View.VISIBLE);
                delete_btn.setClickable(true);
                gid = intent.getIntExtra("gid",0);
                param.putInt("gid",gid);
                break;
        }

        newrelease_page = new newreleaseFragment();
        newrelease_page.setArguments(param);
        transaction.replace(R.id.mainframe, newrelease_page);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                setResult(0);
                finish();
                break;
            case R.id.delete:
                dialog();
                break;
        }
    }

    private void handleDeleteMessage(Message msg) {
        if (msg.what == 0) {
            if (msg.obj.toString().equals("1")) {
                Intent action = new Intent();
                action.putExtra("delete",true);
                action.putExtra("gid",gid);
                setResult(RESULT_OK, action);
                finish();
            } else {
                Toast.makeText(ReleaseActivity.this, "该商品还有正在进行的订单，无法删除", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReleaseActivity.this);
        builder.setMessage("确认要删除该商品吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HttpUtil.HttpClientGET(HttpUtil.host+"release?action=" + String.valueOf(DELETE) +
                        "&uid=" + getSharedPreferences("account",MODE_PRIVATE).getString("uid","0") +
                        "&gid=" + String.valueOf(gid), deleteHandler);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
