package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Aaron on 2016/3/22.
 */
public class homepageFragment extends Fragment implements View.OnClickListener {

    //search bar related views
    private View search_by_text, fake_input, search_by_pic;

    //popupwindow for select a picture to search
    private PopupWindow takeorpick;

    //helpers
    private float screenwidth, screenheight;

    //requestCode of listener
    public static final int TEXT = 0, TAKE_PHOTO = 1, PICK_PHOTO = 2;

    //content type on homepage
    public static final int HOT1 = 0, HOT2 = 1, NEW = 2, PROMOTE = 3;

    //interface notifying main activity to switch fragment
    private onHeadlineClickListener HeadlineClickListener;

    //interface when user click the homepage pictures
    private onHomeTypeSearchListener homeTypeSearchListener;

    //home page message handler
    private Handler homeHandler;

    //search result handler
    private Handler resultHandler;

    //saves the entries on the home page
    private SharedPreferences homeEntry;
    private boolean homeLoaded;

    //ImageViews on the home page
    private ImageView[] entryViews;
    private static final int[] viewIds = { R.id.hot1, R.id.hot2, R.id.newitem, R.id.promoteitem };
    private int[] size;

    public interface onHeadlineClickListener {
        void onHomeHeadlineClicked(int requestCode);
    }

    public interface onHomeTypeSearchListener {
        void onHomeTypeSearch(String res);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            HeadlineClickListener = (onHeadlineClickListener) activity;
            homeTypeSearchListener = (onHomeTypeSearchListener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.homepage, container, false);
        init_pop();

        homeEntry = getActivity().getSharedPreferences("home", Context.MODE_PRIVATE);
        homeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleHomeMessage(msg);
            }
        };
        resultHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleResultMessage(msg);
            }
        };

        search_by_text = v.findViewById(R.id.search_by_text);
        search_by_text.setOnClickListener(this);
        fake_input = v.findViewById(R.id.fake_input);
        fake_input.setOnClickListener(this);
        search_by_pic = v.findViewById(R.id.search_by_pic);
        search_by_pic.setOnClickListener(this);

        entryViews = new ImageView[4];
        for (int i = 0 ; i != 4 ; ++i) {
            entryViews[i] = (ImageView)v.findViewById(viewIds[i]);
            entryViews[i].setOnClickListener(this);
        }

        initContent();

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_by_text:
            case R.id.fake_input:
                HeadlineClickListener.onHomeHeadlineClicked(TEXT);
                break;
            case R.id.search_by_pic:
                if (!takeorpick.isShowing()) {
                    int location[] = new int[2];
                    search_by_pic.getLocationOnScreen(location);
                    int x = location[0], y = location[1];
                    int h = search_by_pic.getHeight();
                    //Log.i("x, y, h",String.valueOf(x)+" "+String.valueOf(y)+" "+String.valueOf(h));
                    takeorpick.showAtLocation(search_by_pic, Gravity.NO_GRAVITY,x-(int)(screenwidth*0.53),y+h-10);
                }
                break;
            case R.id.take_photo:
                takeorpick.dismiss();
                HeadlineClickListener.onHomeHeadlineClicked(TAKE_PHOTO);
                break;
            case R.id.pick_existent:
                takeorpick.dismiss();
                HeadlineClickListener.onHomeHeadlineClicked(PICK_PHOTO);
                break;
            case R.id.hot1:
                searchByType(homeEntry.getString("hot1_t",""), homeEntry.getString("hot1_ct",""));
                break;
            case R.id.hot2:
                searchByType(homeEntry.getString("hot2_t",""), homeEntry.getString("hot2_ct",""));
                break;
            case R.id.newitem:
                searchByType(homeEntry.getString("new_t",""), homeEntry.getString("new_ct",""));
                break;
            case R.id.promoteitem:
                searchByType(homeEntry.getString("promote_t",""), homeEntry.getString("promote_ct",""));
                break;
        }
    }

    /**
     * initialize the popupwindow
     */
    private void init_pop() {
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenwidth = metric.widthPixels;
        screenheight = metric.heightPixels;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.searchoption,null);

        v.findViewById(R.id.take_photo).setOnClickListener(this);
        v.findViewById(R.id.pick_existent).setOnClickListener(this);

        takeorpick = new PopupWindow(v, (int)(screenwidth*0.67), (int)(screenwidth*0.31), false);
        takeorpick.setAnimationStyle(R.style.searchoption_anim_style);
        takeorpick.setBackgroundDrawable(getResources().getDrawable(R.drawable.emptybg));
        takeorpick.setFocusable(true);
        takeorpick.setOutsideTouchable(true);
    }

    private void initContent() {
        HttpUtil.HttpClientGET(HttpUtil.host+"nvg?action=noncontent",homeHandler);
    }

    private void handleHomeMessage(Message msg) {
        if (msg.what == 0) {
            try {
                homeLoaded = true;
                JSONObject res = new JSONObject(msg.obj.toString());
                boolean content = res.getBoolean("content");
                if (content) {
                    int entry = res.getInt("entry");
                    String rawPic = res.getString("pic");
                    byte[] data = Base64.decode(rawPic, Base64.DEFAULT);
                    Bitmap pic = BitmapFactory.decodeByteArray(data, 0, data.length);
                    String filename = homeEntry.getString(String.valueOf(entry),"e");
                    cachePic(pic, filename);
                    loadImage(entry, filename);
                } else {
                    String[] hot1 = res.getString("hot1").split("\\*"), hot2 = res.getString("hot2").split("\\*"),
                            newProduct = res.getString("new").split("\\*"), promote = res.getString("promote").split("\\*");
                    SharedPreferences.Editor editor = homeEntry.edit();
                    Log.e("hot1",hot1[0]);
                    Log.e("hot2",hot2[0]);
                    Log.e("new",newProduct[0]);
                    Log.e("promote",promote[0]);
                    editor.putString(String.valueOf(HOT1), hot1[0]);
                    editor.putString(String.valueOf(HOT2), hot2[0]);
                    editor.putString(String.valueOf(NEW), newProduct[0]);
                    editor.putString(String.valueOf(PROMOTE), promote[0]);
                    editor.putString("hot1_t", hot1[1]);
                    editor.putString("hot2_t", hot2[1]);
                    editor.putString("new_t", newProduct[1]);
                    editor.putString("promote_t", promote[1]);
                    editor.putString("hot1_ct", hot1.length == 3? hot1[2]: "");
                    editor.putString("hot2_ct", hot2.length == 3? hot2[2]: "");
                    editor.putString("new_ct", newProduct.length == 3? newProduct[2]: "");
                    editor.putString("promote_ct", promote.length == 3? promote[2]: "");
                    editor.commit();

                    loadImage(HOT1, hot1[0]);
                    loadImage(HOT2, hot2[0]);
                    loadImage(NEW, newProduct[0]);
                    loadImage(PROMOTE, promote[0]);
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "返回格式错误",Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isCached(String filename) {
        String root = Environment.getExternalStorageDirectory().getPath();
        File file = new File(root+"/weizu/img/home/"+filename+".png");
        if (file.exists())
            return true;
        else
            return false;
    }

    private void cachePic(Bitmap pic, String filename) {
        try {
            String root = Environment.getExternalStorageDirectory().getPath();
            File dir = new File(root + "/weizu/img/home");
            File f = new File(root + "/weizu/img/home/" + filename+".png");
            if (!dir.exists())
                dir.mkdirs();
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream outputStream = new FileOutputStream(f);
            pic.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException f) {
            Toast.makeText(getActivity(), "保存图片时出错 FileNotFoundException", Toast.LENGTH_SHORT).show();
        } catch (IOException ie) {
            Toast.makeText(getActivity(), "保存图片时出错 IOException", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadImage(int entry, String name) {
        if (isCached(name)) {
            String root = Environment.getExternalStorageDirectory().getPath();
            int[] size = UIUtil.getScreenSize(getActivity());
            Log.e("size",String.valueOf(size[0])+","+String.valueOf(size[1]));
            Picasso.with(getActivity())
                    .load(new File(root+"/weizu/img/home/"+name+".png"))
                    .resize(size[0],(int)(size[0]*0.2647))
                    .centerCrop()
                    .into(entryViews[entry]);
        } else {
            HttpUtil.HttpClientGET(HttpUtil.host+"nvg?action=content&entry="+String.valueOf(entry)+"&picname="+name, homeHandler);
        }
    }

    private void searchByType(String type, String childType) {
        if (homeLoaded) {
            try {
                JSONObject params = new JSONObject();
                params.put("method", "type");
                params.put("father", Integer.valueOf(type));
                params.put("child", childType.length() != 0 ? Integer.valueOf(childType):-1);
                HttpUtil.JSONHttpPOST(HttpUtil.host + "s", params, resultHandler);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
    }

    private void handleResultMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                homeTypeSearchListener.onHomeTypeSearch(res);
                Log.e("result", res);
            } else {
                Toast.makeText(getActivity(),"网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
