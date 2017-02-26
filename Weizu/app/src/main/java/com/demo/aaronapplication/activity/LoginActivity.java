package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.demo.aaronapplication.fragments.userFragment;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.MD5Util;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Aaron on 2016/4/13.
 */
public class LoginActivity extends FragmentActivity implements View.OnClickListener {

    public static final int REGISTER = 0;
    private SharedPreferences account;
    private PopupWindow logging;

    private EditText input_un, input_pwd;

    private Handler portraitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 0) {
                    JSONObject res = new JSONObject(msg.obj.toString());
                    if (res.getBoolean("success")) {
                        byte[] data = Base64.decode(res.getString("portrait"), Base64.DEFAULT);
                        Bitmap portrait = BitmapFactory.decodeByteArray(data, 0, data.length);
                        savePortrait(portrait, account.getString("uid","0"));
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "获取用户头像失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Intent intent = new Intent();
                LoginActivity.this.setResult(RESULT_OK, intent);
                LoginActivity.this.finish();
            }
        }
    };

    private Handler loginhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                try {
                    JSONObject res = new JSONObject(msg.obj.toString());
                    int status = res.getInt("status");
                    if (status == 1) {
                        String uid = res.getString("uid");
                        String username = res.getString("username");
                        String token = res.getString("token");
                        String alipay = res.getString("alipay");
                        boolean has_portrait = res.getBoolean("portrait");

                        account = getSharedPreferences("account", MODE_PRIVATE);
                        SharedPreferences.Editor editor = account.edit();
                        editor.putString("username", username);
                        editor.putString("phonenumber", input_un.getText().toString());
                        editor.putString("token", token);
                        editor.putString("uid", uid);
                        editor.putString("alipay", alipay);
                        editor.putBoolean("hasphoto", has_portrait);
                        editor.putBoolean("login", true);
                        editor.commit();

                        if (!has_portrait || (has_portrait && isPortraitExist(uid))) {
                            Intent intent = new Intent();
                            LoginActivity.this.setResult(RESULT_OK, intent);
                            LoginActivity.this.finish();
                        } else {
                            getPortrait(uid);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, "返回格式错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "登录失败，错误信息"+String.valueOf(msg.what),Toast.LENGTH_SHORT).show();
            }

            if (logging.isShowing()) {
                logging.dismiss();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);

        input_un = (EditText)findViewById(R.id.phonenumber_input);
        input_pwd = (EditText)findViewById(R.id.password_input);
        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);

        init_pop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                drawbackimm();
                String pnum = input_un.getText().toString();
                String pwd = input_pwd.getText().toString();
                if (pnum.matches("^(13|15|18)\\d{9}$")) {
                    if (pwd.length() >= 6) {
                        logging.showAtLocation(findViewById(R.id.root), Gravity.CENTER|Gravity.CENTER, 0,0);
                        String encryptedPWD = MD5Util.getMD5(pwd);
                        HttpUtil.HttpClientGET(HttpUtil.host+"log?pnum=" + pnum + "&pwd=" + encryptedPWD, loginhandler);
                    } else {
                        Toast.makeText(LoginActivity.this,"密码必须大于6位",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,"你以为我没见过手机号嘛",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.back_btn:
                drawbackimm();
                finish();
                break;

            case R.id.register:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            /** 如果注册成功，直接返回用户界面 **/
            case REGISTER:
                setResult(RESULT_OK, data);
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("key","pressed");
        if (keyCode == 4) {
            Log.i("back","clicked");
            if (logging.isShowing()) {
                logging.dismiss();
                return false;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode,event);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("back","pressed");
        if (logging.isShowing()) {
            logging.dismiss();
        }
    }

    private void init_pop() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.loging, null);
        logging = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        logging.setFocusable(true);
    }

    private void drawbackimm() {
        InputMethodManager imm =  (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
        }
    }

    private boolean isPortraitExist(String uid) {
        File f = new File(userFragment.filepath + uid + ".jpeg");
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void getPortrait(String uid) {
        HttpUtil.HttpClientGET(HttpUtil.host+"p?action=0&uid=" +uid, portraitHandler);
    }

    private void savePortrait(Bitmap portrait, String uid) {
        try {
            File f = new File(userFragment.filepath + uid+".jpeg");
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream outputStream = new FileOutputStream(f);
            portrait.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException f) {
            Toast.makeText(LoginActivity.this, "保存头像时出错 FileNotFoundException", Toast.LENGTH_SHORT).show();
        } catch (IOException ie) {
            Toast.makeText(LoginActivity.this, "保存头像时出错 IOException", Toast.LENGTH_SHORT).show();
        }
    }

}
