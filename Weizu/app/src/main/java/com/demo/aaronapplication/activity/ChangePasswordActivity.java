package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.aaronapplication.fragments.userFragment;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.MD5Util;
import com.demo.aaronapplication.weizu.R;

/**
 * Created by Aaron on 2016/9/8.
 */
public class ChangePasswordActivity extends Activity implements View.OnClickListener{

    private EditText old_input, new_input, confirm_input;
    private String uid;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);
        uid = getSharedPreferences("account",MODE_PRIVATE).getString("uid","0");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handlePwdMessage(msg);
            }
        };
        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.submit:
                if (isValid()) {
                    submit();
                }
                break;
        }
    }

    private void initView() {
        old_input = (EditText)findViewById(R.id.oldPassword);
        new_input = (EditText)findViewById(R.id.newPassword);
        confirm_input = (EditText)findViewById(R.id.confirmNew);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    private boolean isValid() {
        String old = old_input.getText().toString();
        String newPwd = new_input.getText().toString();
        String confirm = confirm_input.getText().toString();
        if (newPwd.length() < 6) {
            Toast.makeText(this, "密码必须大于6位", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (newPwd.compareTo(confirm) != 0) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return false;
            } else if (newPwd.compareTo(old) == 0) {
                Toast.makeText(this, "新密码不可与旧密码相同", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        }
    }

    private void submit() {
        String old = MD5Util.getMD5(old_input.getText().toString());
        String n = MD5Util.getMD5(new_input.getText().toString());
        HttpUtil.HttpClientGET(HttpUtil.host+"a?action=" + userFragment.CHANGEPASSWORD + "&uid="+uid+"&old="+old+"&new="+n, mHandler);
    }

    private void handlePwdMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                switch (res) {
                    case "1":
                        finish();
                        Toast.makeText(ChangePasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        break;
                    case "0":
                        Toast.makeText(ChangePasswordActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    case "-1":
                        Toast.makeText(ChangePasswordActivity.this, "账号不存在", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                Toast.makeText(ChangePasswordActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
