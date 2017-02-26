package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.activity.MyAccountActivity;
import com.demo.aaronapplication.activity.MyOrdersAcitivity;
import com.demo.aaronapplication.activity.LoginActivity;
import com.demo.aaronapplication.activity.MyReleaseActivity;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Aaron on 2016/3/22.
 */
public class userFragment extends Fragment implements View.OnClickListener, ImageManager.onFinishLoadListener {

    public static final int LOGIN = 0, MYACCOUNT = 1;

    private SharedPreferences account;
    public static final String filepath = "/sdcard/weizu/img/portrait/";

    private ImageManager imageManager;

    private ImageView user_photo;
    private TextView under_photo;

    public static final String CHANGEPASSWORD = "0", BINDALIPAY = "1";

    public interface OperationListener {
        void onLogin();
    }

    private OperationListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OperationListener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.userpage, container, false);
        v.findViewById(R.id.myrelease).setOnClickListener(this);
        v.findViewById(R.id.myorder).setOnClickListener(this);
        v.findViewById(R.id.myaccount).setOnClickListener(this);
        v.findViewById(R.id.user_info).setOnClickListener(this);
        user_photo = (ImageView)v.findViewById(R.id.user_photo);
        under_photo = (TextView)v.findViewById(R.id.under_photo);
        account = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);

        imageManager = new ImageManager();

        if (account.getBoolean("login",false)) {
            under_photo.setText(account.getString("username","WTF"));
            //加载用户头像
            if (account.getBoolean("hasphoto",false)) {
                String uid = account.getString("uid", "WTF");
                String path = imageManager.getImagePath(uid+".jpeg", ImageManager.PORTRAIT);
                if (path != null) {
                    int side = UIUtil.dp2px(getActivity(),80);
                    Picasso.with(getActivity()).load(new File(path)).resize(side,side).centerInside().into(user_photo);
                } else {
                    imageManager.downloadImage(user_photo, uid+".jpeg", ImageManager.PORTRAIT);
                }
/*                Bitmap bitmap;
                bitmap = BitmapFactory.decodeFile(filepath + uid + ".jpeg");
                if (bitmap == null)
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.usrphoto);
                user_photo.setImageBitmap(bitmap);*/
            }
        }

        return v;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.myrelease:
                if (account.getBoolean("login",false)) {
                    intent = new Intent(getActivity(), MyReleaseActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.myorder:
                if (account.getBoolean("login",false)) {
                    intent = new Intent(getActivity(), MyOrdersAcitivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.myaccount:
                if (account.getBoolean("login",false)) {
                    intent = new Intent(getActivity(), MyAccountActivity.class);
                    startActivityForResult(intent, MYACCOUNT);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.user_info:
                if (!account.getBoolean("login",false)) {
                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, LOGIN);
                } else {
                    intent = new Intent(getActivity(), MyAccountActivity.class);
                    startActivityForResult(intent, MYACCOUNT);
                }
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case LOGIN:
                listener.onLogin();
                Log.i("login","succeed");
                under_photo.setText(account.getString("username","_"));
                if (account.getBoolean("hasphoto",false)) {
                    String uid = account.getString("uid", "WTF");
                    Bitmap bitmap = BitmapFactory.decodeFile(filepath + uid + ".jpeg");
                    if (bitmap == null)
                        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.usrphoto);
                    user_photo.setImageBitmap(bitmap);
                }
                break;
            case MYACCOUNT:
                checkLogStateChange();
                if (data.getBooleanExtra("refreshed", false)) {
                    if (account.getBoolean("hasphoto",false)) {
                        String uid = account.getString("uid", "WTF");
                        Bitmap bitmap = BitmapFactory.decodeFile(filepath + uid + ".jpeg");
                        if (bitmap == null)
                            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.usrphoto);
                        user_photo.setImageBitmap(bitmap);
                    }
                }
                break;
        }
    }


    @Override
    public void onFinishLoading(ImageView holder, String path) {
        if (holder != null) {
            int side = UIUtil.dp2px(getActivity(), 80);
            Picasso.with(getActivity()).load(new File(path)).resize(side, side).centerInside().into(holder);
        }
    }

    private void checkLogStateChange() {
        if (!account.getBoolean("login",false)) {
            under_photo.setText("登录/注册");
            user_photo.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.usrphoto));
        }
    }
}
