package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.alipay.Base64;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Aaron on 2016/9/7.
 */
public class MyAccountActivity extends Activity implements View.OnClickListener {

    private ImageView stars[];
    private ImageView portrait;
    private TextView username, ID, alipay;

    private SharedPreferences account;

    private Handler creditHandler;  //credit是会变的，所以每次用户打开时获取一次
    private Handler alipayHandler;
    private Handler portraitHandler;    //更换头像的handler

    private int credit;

    private AlertDialog bindAlipay; //绑定支付宝的过程在dialog中进行，该dialog只在用户使用时初始化
    private String alipayAccount;   //支付宝账号

    private AlertDialog newPortrait;    //更换头像时的dialog
    private Bitmap bitmapNewPortrait;   //更换的头像
    private ImageView dialogPortraitView;   //dialog中显示头像的imageView
    private boolean portraitRefreshed;
    private static final int PHOTO_CROP = 100, PHOTO_PICKED_WITH_DATA = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myaccount);

        findViewById(R.id.back_btn).setOnClickListener(this);

        account = getSharedPreferences("account",MODE_PRIVATE);

        creditHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleCreditMessage(msg);
            }
        };

         alipayHandler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 handleBindMessage(msg);
             }
         };

        getCredit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        creditHandler.removeCallbacksAndMessages(null);
        alipayHandler.removeCallbacksAndMessages(null);
        if (portraitHandler != null) {
            portraitHandler.removeCallbacksAndMessages(null);
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
                bitmapNewPortrait = data.getParcelableExtra("data");
                if (portrait != null) {
                    Log.i("photo","not null");
                    dialogPortraitView.setImageBitmap(bitmapNewPortrait);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (portraitRefreshed) {
                Intent intent = new Intent();
                intent.putExtra("refreshed", portraitRefreshed);
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode,event);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.back_btn:
                if (portraitRefreshed) {
                    intent = new Intent();
                    intent.putExtra("refreshed", portraitRefreshed);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
                break;
            case R.id.changePassword:
                intent = new Intent(MyAccountActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.bindAlipay:
                bindAlipay();
                break;
            case R.id.logout:
                dialog();
                break;
            case R.id.manageAddress:
                intent = new Intent(MyAccountActivity.this, SelectAddressActivity.class);
                intent.putExtra("manage", true);
                startActivity(intent);
                break;
            case R.id.feedback:
                feedback();
                break;
            case R.id.portrait:
                newPortrait();
                break;
            case R.id.choose:
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
                break;
            case R.id.submit:
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                bitmapNewPortrait.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                byte[] p = ostream.toByteArray();
                try {
                    ostream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String strPortrait = android.util.Base64.encodeToString(p, android.util.Base64.DEFAULT);
                JSONObject param = new JSONObject();
                try {
                    param.put("uid", account.getString("uid", "0"));
                    param.put("portrait", strPortrait);
                } catch (JSONException je) {
                    je.printStackTrace();
                }
                HttpUtil.JSONHttpPOST(HttpUtil.host+"image", param, portraitHandler);
                break;
            case R.id.cancel:
                newPortrait.dismiss();
                break;
        }
    }

    private void getCredit() {
        HttpUtil.HttpClientGET(HttpUtil.host+"a?action=2&uid="+account.getString("uid","0"), creditHandler);
    }


    private void initViews() {
        findViewById(R.id.changePassword).setOnClickListener(this);
        findViewById(R.id.bindAlipay).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);
        findViewById(R.id.manageAddress).setOnClickListener(this);
        findViewById(R.id.feedback).setOnClickListener(this);
        findViewById(R.id.portrait).setOnClickListener(this);

        stars = new ImageView[5];
        stars[0] = (ImageView)findViewById(R.id.star1);
        stars[1] = (ImageView)findViewById(R.id.star2);
        stars[2] = (ImageView)findViewById(R.id.star3);
        stars[3] = (ImageView)findViewById(R.id.star4);
        stars[4] = (ImageView)findViewById(R.id.star5);

        portrait = (ImageView)findViewById(R.id.portrait);
        username = (TextView)findViewById(R.id.username);
        ID = (TextView)findViewById(R.id.userId);
        alipay = (TextView)findViewById(R.id.alipayAccount);

        String curAlipayAccount = account.getString("alipay","NULL");
        if (curAlipayAccount.compareTo("NULL") != 0) {
            alipay.setText(partialAccount(curAlipayAccount));
        }

        ID.setText(account.getString("uid","0"));
        username.setText(account.getString("username","0"));
        if (account.getBoolean("hasphoto",false)) {
            String uid = account.getString("uid", "WTF");
            Bitmap bitmap;
            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/portrait/" + uid + ".jpeg");
            if (bitmap == null)
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.usrphoto);
            portrait.setImageBitmap(bitmap);
        }

        //设置信用星级的图标
        for (int i = 4 ; i > 0 ; i--) {
            int limit = i*2 + 1;
            if (credit == limit) {
                stars[i].setImageDrawable(getResources().getDrawable(R.drawable.star_half));
                break; //画到半颗可以直接退出循环
            } else if (credit < limit) {
                stars[i].setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
            } else
                break;
        }
    }


    private void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAccountActivity.this);
        builder.setMessage("确认退出当前账号吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = account.edit();
                editor.putBoolean("login",false);
                editor.commit();
                dialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra("refreshed", false);
                MyAccountActivity.this.setResult(RESULT_OK, intent);
                MyAccountActivity.this.finish();
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


    private void bindAlipay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v;
        builder.setTitle(getString(R.string.bindAlipay));

        final String curAlipayAccount = account.getString("alipay", "NULL");
        if (curAlipayAccount.compareTo("NULL") == 0) {
            v = inflater.inflate(R.layout.bindalipay , null);
            final EditText inputAccount = (EditText)v.findViewById(R.id.account);
            final EditText confirmAccount = (EditText)v.findViewById(R.id.confirmAccount);

            builder.setNegativeButton("取消", null);
            builder.setPositiveButton("绑定", null);
            builder.setView(v);
            bindAlipay = builder.create();
            bindAlipay.show();

            bindAlipay.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputAccount.getText().length() == 0) {
                        Toast.makeText(MyAccountActivity.this, "支付宝账号不可为空", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (confirmAccount.getText().length() == 0) {
                        Toast.makeText(MyAccountActivity.this, "请再次输入您的支付宝账号", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (confirmAccount.getText().toString().compareTo(inputAccount.getText().toString()) != 0){
                        Toast.makeText(MyAccountActivity.this, "两次输入的支付宝账号不一致", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        alipayAccount = inputAccount.getText().toString();
                        HttpUtil.HttpClientGET(HttpUtil.host + "a?action=1&uid="+ account.getString("uid","0")+"&binded=0&account=" + alipayAccount, alipayHandler);
                    }
                }
            });
            bindAlipay.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bindAlipay.dismiss();
                }
            });
        } else {
            v = inflater.inflate(R.layout.rebindalipay, null);
            rebindOnClickListener listener = new rebindOnClickListener(v);

            ((TextView)v.findViewById(R.id.current_account)).setText(partialAccount(account.getString("alipay","NULL")));
            v.findViewById(R.id.rebind).setOnClickListener(listener);
            v.findViewById(R.id.unbind).setOnClickListener(listener);

            v.findViewById(R.id.unbind_confirm).setOnClickListener(listener);
            v.findViewById(R.id.unbind_cancel).setOnClickListener(listener);
            v.findViewById(R.id.rebind_confirm).setOnClickListener(listener);
            v.findViewById(R.id.rebind_cancel).setOnClickListener(listener);

            builder.setView(v);
            bindAlipay = builder.create();
            bindAlipay.show();
        }
    }

    private void handleCreditMessage(Message msg) {
        try {
            if (msg.what == 0) {
                int res = Integer.valueOf(msg.obj.toString());
                if (res == -1) {
                    Toast.makeText(MyAccountActivity.this, "查询信用失败", Toast.LENGTH_SHORT).show();
                    credit = 10;
                } else {
                    credit = res;
                }
                initViews();
            } else {
                Toast.makeText(MyAccountActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBindMessage(Message msg) {
        try {
            if (msg.what == 0) {
                switch (msg.obj.toString()) {
                    case "1":
                        Toast.makeText(MyAccountActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = account.edit();
                        editor.putString("alipay", alipayAccount);
                        editor.commit();
                        alipay.setText(partialAccount(alipayAccount));
                        bindAlipay.dismiss();
                        break;
                    case "2":
                        Toast.makeText(MyAccountActivity.this, "更改绑定成功", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor1 = account.edit();
                        editor1.putString("alipay", alipayAccount);
                        editor1.commit();
                        alipay.setText(partialAccount(alipayAccount));
                        bindAlipay.dismiss();
                        break;
                    case "3":
                        Toast.makeText(MyAccountActivity.this, "解除绑定成功", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor2 = account.edit();
                        editor2.putString("alipay", "NULL");
                        editor2.commit();
                        alipay.setText(getString(R.string.unbinded));
                        bindAlipay.dismiss();
                        break;
                    case "4":
                        Toast.makeText(MyAccountActivity.this, "您尚有正在进行中的订单，无法解除绑定", Toast.LENGTH_SHORT).show();
                        bindAlipay.dismiss();
                        break;
                }
            } else {
                Toast.makeText(MyAccountActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String partialAccount(String account) {
        if (account.length() > 7) {
            return account.substring(0,3)+"***"+account.substring(account.length()-4, account.length());
        } else {
            return account.substring(0,1)+"***"+account.substring(account.length()-1, account.length());
        }
    }

    private class rebindOnClickListener implements View.OnClickListener {

        private View rebind_panel, unbind_panel;
        private EditText inputAccount, confirmAccount;

        public rebindOnClickListener(View v) {
            rebind_panel = v.findViewById(R.id.rebind_panel);
            unbind_panel = v.findViewById(R.id.unbind_panel);
            inputAccount = (EditText) v.findViewById(R.id.account);
            confirmAccount = (EditText) v.findViewById(R.id.confirmAccount);
        }

        private boolean valid() {
            if (inputAccount.getText().length() == 0) {
                Toast.makeText(MyAccountActivity.this, "支付宝账号不可为空", Toast.LENGTH_SHORT).show();
                return false;
            } else if (confirmAccount.getText().length() == 0) {
                Toast.makeText(MyAccountActivity.this, "请再次输入您的支付宝账号", Toast.LENGTH_SHORT).show();
                return false;
            } else if (confirmAccount.getText().toString().compareTo(inputAccount.getText().toString()) != 0){
                Toast.makeText(MyAccountActivity.this, "两次输入的支付宝账号不一致", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.unbind:
                    rebind_panel.setVisibility(View.GONE);
                    unbind_panel.setVisibility(View.VISIBLE);
                    break;
                case R.id.rebind:
                    unbind_panel.setVisibility(View.GONE);
                    rebind_panel.setVisibility(View.VISIBLE);
                    break;
                case R.id.unbind_confirm:
                    HttpUtil.HttpClientGET(HttpUtil.host+"a?action=1&uid="+ account.getString("uid","0")+"&binded=1&account=NULL", alipayHandler);
                    break;
                case R.id.rebind_confirm:
                    if (valid()) {
                        alipayAccount = inputAccount.getText().toString();
                        HttpUtil.HttpClientGET(HttpUtil.host+"a?action=1&uid="+account.getString("uid","0")+"&binded=1&account="+alipayAccount, alipayHandler);
                    }
                    break;
                case R.id.unbind_cancel:
                    if (bindAlipay != null) {
                        bindAlipay.dismiss();
                    }
                    break;
                case R.id.rebind_cancel:
                    if (bindAlipay != null) {
                        bindAlipay.dismiss();
                    }
                    break;
            }
        }
    }

    private void feedback() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(getString(R.string.hint_feedback));
        builder.create().show();
    }

    private void newPortrait() {
        portraitHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handlePortraitMessage(msg);
            }
        };
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.newportrait, null);

        v.findViewById(R.id.choose).setOnClickListener(this);
        v.findViewById(R.id.cancel).setOnClickListener(this);
        v.findViewById(R.id.submit).setOnClickListener(this);
        dialogPortraitView = (ImageView)v.findViewById(R.id.newportrait);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.newPortrait));
        builder.setView(v);
        newPortrait = builder.create();
        newPortrait.show();
    }

    public void handlePortraitMessage(Message msg) {
        try {
            if (msg.what == 0) {
                newPortrait.dismiss();
                SharedPreferences.Editor editor = account.edit();
                editor.putBoolean("hasphoto", true);
                editor.commit();
                portrait.setImageBitmap(bitmapNewPortrait);
                portraitRefreshed = true;
                save_photo(bitmapNewPortrait);
                Toast.makeText(this, "更换头像成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            File file = new File("/sdcard/weizu/img/portrait/"+account.getString("uid","0")+".jpeg");
            if (file.exists())
                file.delete();
            FileOutputStream out = new FileOutputStream(file);
            photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.i("new portrait", "saved");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
