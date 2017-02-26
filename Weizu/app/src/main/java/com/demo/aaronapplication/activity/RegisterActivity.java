package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.MD5Util;
import com.demo.aaronapplication.weizu.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Aaron on 2016/4/15.
 */
public class RegisterActivity extends Activity implements View.OnClickListener {

    private EditText inputs[];
    private ImageView checks[];
    private static final int Vids[] = { R.id.pnumcheck, R.id.uncheck, R.id.pwdcheck, R.id.pwdconfirmcheck, R.id.vcodecheck};
    private static final int Inputs[] = { R.id.pnum, R.id.username, R.id.password, R.id.password_confirm, R.id.vcode };
    private static final int PHONENUMBER = 0, USERNAME = 1, PASSWORD = 2, PASSWORDCONFIRM = 3, VCODE = 4, VCODEGENERATED = 5;
    private Handler verifyHandler, registerHandler, countdownHandler;
    private boolean checkstatus[];
    private TextView registerbtn, getvcodebtn;
    private String pnum, vcode;
    private Timelimit timelimit;
    private File user_photo;
    private Bitmap portrait;

    private static final int PHOTO_PICKED_WITH_DATA = 0, PHOTO_CROP = 1;

    private SharedPreferences account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerpage);

        checkstatus = new boolean[]{ false,false,false,false,false,false };
        inputs = new EditText[5];
        checks = new ImageView[5];
        for (int i = 0 ; i != 5 ; ++i) {
            inputs[i] = (EditText)findViewById(Inputs[i]);
            inputs[i].addTextChangedListener(new myTextWatcher(i));
            checks[i] = (ImageView)findViewById(Vids[i]);
        }

        findViewById(R.id.back_btn).setOnClickListener(this);
        registerbtn = (TextView)findViewById(R.id.register);
        registerbtn.setOnClickListener(this);
        getvcodebtn = (TextView)findViewById(R.id.get_vcode);
        getvcodebtn.setOnClickListener(this);
        findViewById(R.id.setphoto).setOnClickListener(this);

        verifyHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /** 这里的0是指已用该手机号注册的账户数 **/
                if (msg.obj.toString().compareTo("0") == 0) {
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("apikey","8cb3dbc0b50bf2705558e753823199ba"));
                    params.add(new BasicNameValuePair("mobile",pnum));
                    generate_vcode();
                    params.add(new BasicNameValuePair("content","【微租】 您的注册验证码"+vcode));
                    HttpUtil.HttpClientPOST("https://api.dingdongcloud.com/v1/sms/sendyzm",params);
                    getvcodebtn.setBackground(getResources().getDrawable(R.drawable.disabledthemebtn));
                    getvcodebtn.setClickable(false);
                    timelimit = new Timelimit();
                    timelimit.start();
                } else {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.warning_duplicatepnum), Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    try {
                        JSONObject res = new JSONObject(msg.obj.toString());
                        /** 成功 obj = 用户uid 失败 = 0 **/
                        if (res.getInt("status") == 0) {
                            /** 用户名重复 **/
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.warning_duplicateun), Toast.LENGTH_SHORT).show();
                        } else {
                            /** 注册成功 **/
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            account = RegisterActivity.this.getSharedPreferences("account", MODE_PRIVATE);
                            SharedPreferences.Editor editor = account.edit();
                            editor.putString("username", inputs[USERNAME].getText().toString());
                            editor.putString("phonenumber", inputs[PHONENUMBER].getText().toString());
                            editor.putInt("credit", 10);
                            editor.putString("uid", String.valueOf(res.getInt("status")));
                            editor.putString("token", res.getString("token"));
                            editor.putBoolean("login", true);

                            //重命名头像文件，下次登录可直接加载(若用户设置了头像)
                            if (user_photo != null) {
                                editor.putBoolean("hasphoto", true);
                                File tmp = new File("/sdcard/weizu/img/portrait/tmp.jpeg");
                                File newfile = new File("/sdcard/weizu/img/portrait/" + String.valueOf(res.getInt("status")) + ".jpeg");
                                tmp.renameTo(newfile);
                            }
                            editor.commit();

                            Intent intent = new Intent();
                            intent.putExtra("username", inputs[USERNAME].getText().toString());
                            RegisterActivity.this.setResult(RESULT_OK, intent);
                            RegisterActivity.this.finish();
                        }
                    } catch (JSONException JE) {
                        JE.printStackTrace();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败，错误信息"+String.valueOf(msg.what),Toast.LENGTH_SHORT).show();
                }
            }
        };

        countdownHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    getvcodebtn.setBackground(getResources().getDrawable(R.drawable.themebtn));
                    getvcodebtn.setText("获取验证码");
                    getvcodebtn.setClickable(true);
                } else {
                    getvcodebtn.setText("获取验证码("+String.valueOf(msg.what)+")");
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timelimit != null && timelimit.isrun)
            timelimit.isrun = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                drawbackimm();
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.register:
                drawbackimm();
                //TODO RECOVER
                if (inputs[VCODE].getText().toString().compareTo(vcode) != 0) {
                    Toast.makeText(this, getResources().getString(R.string.warning_wrongvcode), Toast.LENGTH_SHORT).show();
                } else {
                    String encryptedPWD = MD5Util.getMD5(inputs[PASSWORD].getText().toString());
                    Map<String, Object> regInfo = new HashMap<>();
                    regInfo.put("pnum", inputs[PHONENUMBER].getText().toString());
                    regInfo.put("pwd", encryptedPWD);
                    regInfo.put("un", Base64.encodeToString(inputs[USERNAME].getText().toString().getBytes(), Base64.DEFAULT));
                    if (user_photo != null) {
                        regInfo.put("has_portrait", true);
                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                        portrait.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                        byte[] p = ostream.toByteArray();
                        try {
                            ostream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        regInfo.put("portrait", Base64.encodeToString(p, Base64.DEFAULT));
                    } else {
                        regInfo.put("has_portrait", false);
                    }
                    JSONObject js = new JSONObject(regInfo);
                    HttpUtil.JSONHttpPOST(HttpUtil.host + "reg", js, registerHandler);
                }
                break;
            case R.id.get_vcode:
                if (checkstatus[PHONENUMBER]) {
                    pnum = inputs[PHONENUMBER].getText().toString();
                    HttpUtil.HttpClientGET(HttpUtil.host+"verify?pnum="+pnum, verifyHandler);
                } else {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.warning_invalidpnum),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.setphoto:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA:
                Uri uri = data.getData();
                if (uri != null) {
                    startPhotoZoom(uri);
                }
                break;
            case PHOTO_CROP:
                portrait = data.getParcelableExtra("data");
                if (portrait != null) {
                    Log.i("photo","not null");
                    save_photo(portrait);
                    ((ImageView)findViewById(R.id.photo)).setImageBitmap(portrait);
                }
                break;
        }
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_CROP);
    }

    private void save_photo(Bitmap photo) {
        try {
            File filedir = new File("/sdcard/weizu/img/portrait");
            if (!filedir.exists())
                filedir.mkdirs();
            File file = new File("/sdcard/weizu/img/portrait/tmp.jpeg");
            if (file.exists())
                file.delete();
            FileOutputStream out = new FileOutputStream(file);
            photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            user_photo = file;
            Log.i("tmp", "saved");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class myTextWatcher implements TextWatcher {
        private int entry;

        public myTextWatcher(int pos) { entry = pos; }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            switch (entry) {
                case PHONENUMBER:
                    if (s.toString().matches("^(13|15|18)\\d{9}$")) {
                        checks[PHONENUMBER].setVisibility(View.VISIBLE);
                        checks[PHONENUMBER].setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ok));
                        checkstatus[PHONENUMBER] = true;
                    } else {
                        checks[PHONENUMBER].setVisibility(View.VISIBLE);
                        checks[PHONENUMBER].setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.error));
                        checkstatus[PHONENUMBER] = false;
                    }
                    break;
                case USERNAME:
                    if (s.length()>0) {
                        checks[USERNAME].setVisibility(View.VISIBLE);
                        checks[USERNAME].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ok));
                        checkstatus[USERNAME] = true;
                    } else {
                        checks[USERNAME].setVisibility(View.VISIBLE);
                        checks[USERNAME].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.error));
                        checkstatus[USERNAME] = false;
                    }
                    break;
                case PASSWORD:
                    if (s.length() > 5) {
                        checks[PASSWORD].setVisibility(View.VISIBLE);
                        checks[PASSWORD].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ok));
                        checkstatus[PASSWORD] = true;
                    } else {
                        checks[PASSWORD].setVisibility(View.VISIBLE);
                        checks[PASSWORD].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.error));
                        checkstatus[PASSWORD] = false;
                    }
                    break;
                case PASSWORDCONFIRM:
                    if (s.toString().compareTo(inputs[PASSWORD].getText().toString()) == 0 && s.length() != 0) {
                        checks[PASSWORDCONFIRM].setVisibility(View.VISIBLE);
                        checks[PASSWORDCONFIRM].setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ok));
                        checkstatus[PASSWORDCONFIRM] = true;
                    } else {
                        checks[PASSWORDCONFIRM].setVisibility(View.VISIBLE);
                        checks[PASSWORDCONFIRM].setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.error));
                        checkstatus[PASSWORDCONFIRM] = false;
                    }
                    break;
                case VCODE:
                    //TODO huifu
                    if (s.length() == 6 && checkstatus[VCODEGENERATED]) {
                        checks[VCODE].setVisibility(View.VISIBLE);
                        checks[VCODE].setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ok));
                        checkstatus[VCODE] = true;
                    } else {
                        checks[VCODE].setVisibility(View.VISIBLE);
                        checks[VCODE].setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.error));
                        checkstatus[VCODE] = false;
                    }
                    break;
            }

            boolean alliswell = true;
            //TODO 5->6
            for (int i = 0 ; i != 6 ; ++i) {
                if (!checkstatus[i]) {
                    alliswell = false;
                }
            }
            if (alliswell) {
                registerbtn.setClickable(true);
                registerbtn.setBackground(getResources().getDrawable(R.drawable.themebtn));
            } else {
                registerbtn.setClickable(false);
                registerbtn.setBackground(getResources().getDrawable(R.drawable.disabledthemebtn));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void generate_vcode() {
        Random random = new Random();
        vcode = "";
        for (int i = 0 ; i != 6 ; ++i) {
            vcode += String.valueOf(random.nextInt(10));
        }
        checkstatus[VCODEGENERATED] = true;
        //Toast.makeText(this, "生成验证码："+vcode,Toast.LENGTH_SHORT).show();
    }

    private void drawbackimm() {
        InputMethodManager imm =  (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
        }
    }

    private class Timelimit extends Thread {
        public boolean isrun;
        private int countdown = 60;

        public Timelimit() {
            super();
            isrun = true;
        }

        @Override
        public void run() {
            while (isrun) {
                countdown--;
                Message msg = new Message();
                msg.what = countdown;
                countdownHandler.sendMessage(msg);
                isrun = countdown > 0;
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
