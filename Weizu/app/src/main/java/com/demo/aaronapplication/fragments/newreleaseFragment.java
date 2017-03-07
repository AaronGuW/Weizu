package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.activity.ReleaseActivity;
import com.demo.aaronapplication.alipay.SignUtils;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.demo.aaronapplication.weizu.fileUtil;
import com.demo.aaronapplication.weizu.goods;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


public class newreleaseFragment extends Fragment implements View.OnClickListener, ImageManager.onFinishLoadListener {

    private ImageView release;
    private EditText title_input, description_input, rent_input, deposit_input, freight_input, contact_input, location_input;
    private TextView warning;
    private Uri imageuri;
    private Handler mHandler, loadHandler;
    private goods newGood, origGood;

    private Spinner category, childType, period;
    private boolean firsttime;

    private View upload; /* 用户点击该view已上传图片，该view一直存在于viewpager最后直至上传达到上限 */
    private GridView images;
    private myGridViewAdapter gridViewAdapter;
    private ArrayList<mImage> imageArrayList;
    private ImageManager imageManager;
    private LayoutInflater inflater;
    private int IVWidth, IVHeight;

    private View root; // 根控件，用于popupWindow showatlocation的第一个参数
    private ProgressWindow progressWindow;

    public static final int MAXPICTURES = 16; /* 最多可上传图片数 */

    private static final int array_res[] = { R.array.Books, R.array.Electronics, R.array.Transportation, R.array.Clothes, R.array.Sports,
                                                    R.array.Chemical, R.array.Activities, R.array.Time, R.array.Skill, R.array.Others };

    private int action;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.releasepage, container, false);

        action = getArguments().getInt("action");

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handlePostMessage(msg);
            }
        };
        loadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleLoadReleaseMessage(msg);
            }
        };

        initView(v);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.releasenow:
                int titlelen = title_input.getText().toString().length();
                int deslen = description_input.getText().toString().length();
                int pricelen = rent_input.getText().toString().length();
                int depolen = deposit_input.getText().toString().length();
                int frelen = freight_input.getText().toString().length();
                int contactlen = contact_input.getText().toString().length();
                int locationlen = location_input.getText().toString().length();

                if (titlelen == 0) {
                    warning.setText(getResources().getText(R.string.warning_emptytitle));
                } else if (deslen == 0) {
                    warning.setText(getResources().getText(R.string.warning_emptydescription));
                } else if (pricelen == 0) {
                    warning.setText(getResources().getText(R.string.warning_emptyprice));
                } else if (depolen == 0) {
                    warning.setText(getResources().getText(R.string.warning_emptydeposit));
                } else if (frelen == 0) {
                    warning.setText(getResources().getString(R.string.warning_emptyfreight));
                } else if (contactlen == 0 ) {
                    warning.setText(getResources().getText(R.string.warning_emptycontactinfo));
                } else if (locationlen == 0 ) {
                    warning.setText(getResources().getText(R.string.warning_emptyregion));
                } else {
                    newGood = new goods( title_input.getText().toString(),Float.valueOf(rent_input.getText().toString()),
                            Float.valueOf(deposit_input.getText().toString()), Float.valueOf(freight_input.getText().toString()),
                            description_input.getText().toString(), contact_input.getText().toString(),
                            location_input.getText().toString(), category.getSelectedItemPosition(), childType.getSelectedItemPosition(),
                            Integer.valueOf(getActivity().getSharedPreferences("account", Context.MODE_PRIVATE).getString("uid","0")),
                            0, System.currentTimeMillis(), imageArrayList.size(), findCover(), period.getSelectedItemPosition());

                    //检查完成后post提交订单
                    JSONObject param = packPOSTData();
                    String[] paths = uris2paths();
                    if (paths == null) {
                        Toast.makeText(getActivity(), getString(R.string.image_unloaded), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.e("read path","gocha");
                    progressWindow = new ProgressWindow(imageArrayList.size());
                    progressWindow.show();

                    HttpUtil.uploadRelease(action, HttpUtil.host+"release", HttpUtil.host+"upload",param, paths, mHandler);
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case 1:
                if (data != null) {
                    imageuri = null;
                    imageuri = data.getData();
                    if (imageuri != null) {
                        Log.i("geturi", "succeed");
                        insertImage(imageuri);
                    } else {
                        /*Bundle extra = data.getExtras();
                        if (extra != null) {
                            image = extra.getParcelable("data");
                            if (image != null) {
                                insertImage(image, null);
                            }
                        }*/
                    }
                }
                break;
        }
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        try {
            Picasso.with(getActivity()).load(new File(path)).resize(IVWidth,IVHeight).centerInside().into(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView(View v) {
        IVWidth = IVHeight = 168;
        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);
        inflater = LayoutInflater.from(getActivity());
        root = v.findViewById(R.id.root);
        release = (ImageView)v.findViewById(R.id.releasenow);
        category = (Spinner)v.findViewById(R.id.category_spinner);
        category.setSelection(0);
        childType = (Spinner)v.findViewById(R.id.childType_spinner);
        period = (Spinner)v.findViewById(R.id.period_spinner);
        title_input = (EditText)v.findViewById(R.id.title_input);
        description_input = (EditText)v.findViewById(R.id.description_input);
        rent_input = (EditText)v.findViewById(R.id.price_input);
        deposit_input = (EditText)v.findViewById(R.id.deposit_input);
        freight_input = (EditText)v.findViewById(R.id.freight_input);
        contact_input = (EditText)v.findViewById(R.id.contactinfo_input);
        location_input = (EditText)v.findViewById(R.id.region_input);
        warning = (TextView)v.findViewById(R.id.warning);

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, R.id.text, getResources().getStringArray(array_res[position]));
                childType.setAdapter(adapter);
                if (firsttime) {
                    firsttime = false;
                    childType.setSelection(origGood.getChildType());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        upload = inflater.inflate(R.layout.clicktoupload, null);

        images = (GridView)v.findViewById(R.id.images);
        imageArrayList = new ArrayList<>();

        /** 若用户要修改订单，先载入原订单 **/
        if (action == ReleaseActivity.MODIFY) {
            loadOriginalRelease(getArguments().getInt("gid"));
        }

        gridViewAdapter = new myGridViewAdapter();
        images.setAdapter(gridViewAdapter);
        images.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (imageArrayList.size() < MAXPICTURES && position == imageArrayList.size()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 1);
                } else {
                    for (mImage image : imageArrayList) {
                        image.setCover(false);
                    }
                    imageArrayList.get(position).setCover(true);
                    gridViewAdapter.notifyDataSetChanged();
                }
            }
        });

        release.setOnClickListener(this);

    }

    private void loadOriginalRelease(int gid) {
        HttpUtil.HttpClientGET(HttpUtil.host+"release?action="+String.valueOf(ReleaseActivity.FULL)+"&gid="+String.valueOf(gid), loadHandler);
    }

    private JSONObject packPOSTData() {
        JSONObject param = new JSONObject();
        try {
            param.put("action",action);  //action 封装在json里
            param.put("title", Base64.encodeToString(newGood.getTitle().getBytes(),Base64.DEFAULT));
            param.put("freight", newGood.getFreight());
            param.put("rent", newGood.getRent());
            param.put("deposit", newGood.getDeposit());
            param.put("sales", newGood.getSales());
            param.put("leaser", newGood.getLeaser());
            param.put("intro", Base64.encodeToString(newGood.getDescription().getBytes(),Base64.DEFAULT));
            param.put("type", newGood.getCategory());
            param.put("childtype", newGood.getChildType());
            param.put("contact", Base64.encodeToString(newGood.getContact().getBytes(),Base64.DEFAULT));
            param.put("location", Base64.encodeToString(newGood.getLocation().getBytes(),Base64.DEFAULT));
            param.put("date", newGood.getDate());

            param.put("picnum", imageArrayList.size());
            param.put("coverindex", findCover());
            param.put("period", newGood.getPeriod());
            if (action == ReleaseActivity.MODIFY)
                param.put("gid", origGood.getGid());
        } catch (JSONException j) {
            Toast.makeText(getActivity(), "JSON封装时出错",Toast.LENGTH_SHORT).show();
            return null;
        }
        return param;
    }

    /**
     * 将图片uri转成真实路径，也用于检查是否所有图片已加载完毕，如果没有，则不能提交
     * @return
     */
    private String[] uris2paths() {
        String[] paths = new String[imageArrayList.size()];
        for (int i = 0 ; i != imageArrayList.size() ; ++i) {
            paths[i] = imageArrayList.get(i).getRealPath();
            if (paths[i] == null) {
                Log.e("image null index",String.valueOf(i));
                return null;
            }
        }
        return paths;
    }

    private void handlePostMessage(Message msg) {
        if (msg.what == 0) {
            if (msg.arg1 == -1) {
                Toast.makeText(getActivity(), "上传失败",Toast.LENGTH_SHORT).show();
                progressWindow.dismiss();
            } else
                progressWindow.setProgress(msg.arg1);
        } else if (msg.what == 100) {
            progressWindow.dismiss();
            String gid = msg.obj.toString();
            if (!gid.equals("0")) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.mainframe, new releaseokFragment());
                transaction.commit();
                if (action == ReleaseActivity.NEW) {
                    Toast.makeText(getActivity(), "发布成功，您的商品将在1小时内完成上架", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "修改成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "发布失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            progressWindow.dismiss();
        }
    }

    private void handleLoadReleaseMessage(Message msg) {
        Log.e("load","finish");
        if (msg.what == 0) {
            try {
                String res = msg.obj.toString();
                origGood = new goods(new JSONObject(res), false);
                category.setSelection(origGood.getCategory());
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, R.id.text,
                        getResources().getStringArray(array_res[origGood.getChildType()]));
                childType.setAdapter(adapter);
                childType.setSelection(origGood.getChildType());
                firsttime = true;
                period.setSelection(origGood.getPeriod());
                title_input.setText(origGood.getTitle());
                description_input.setText(origGood.getDescription());
                rent_input.setText(String.valueOf(origGood.getRent()));
                deposit_input.setText(String.valueOf(origGood.getDeposit()));
                freight_input.setText(String.valueOf(origGood.getFreight()));
                contact_input.setText(origGood.getContact());
                location_input.setText(origGood.getLocation());

                int picnum = origGood.getPicnum();
                Log.e("picnum",picnum+"");
                String cover = origGood.getCoverMD5();
                String[] md5s = origGood.getPictures();
                for (int i = 0 ; i != picnum ; ++i) {
                    mImage tmp = new mImage(md5s[i]);
                    if (md5s[i].compareTo(cover) == 0) {
                        tmp.setCover(true);
                    }
                    imageArrayList.add(tmp);
                }
                gridViewAdapter.notifyDataSetChanged();
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 插入用户从图库选择的图片
     * @param uri 图片的uri
     */
    private void insertImage(Uri uri) {
        mImage image = new mImage(uri);
        image.setNew(true);
        image.setPath(fileUtil.getPath(getActivity(), uri));
        if (imageArrayList.size() == 0)
            image.setCover(true);
        imageArrayList.add(image);
        gridViewAdapter.notifyDataSetChanged();
    }

    /**
     * 删除图片
     * @param index
     */
    private void deleteImage(int index) {
        if (imageArrayList.get(index).isCover) {
            if (imageArrayList.size() != 1) {
                imageArrayList.remove(index);
                imageArrayList.get(0).setCover(true);
            } else {
                imageArrayList.remove(index);
            }
        } else {
            imageArrayList.remove(index);
        }
        gridViewAdapter.notifyDataSetChanged();
    }

    /**
     * 用户修改订单时，在新信息提交后，删除本地所有原订单图片
     */
    private void deleteOrigImageFiles() {
        int picnum = origGood.getPicnum();
        String gid = String.valueOf(origGood.getGid());
        String dir = ImageManager.saveDir[ImageManager.GOODS];
        for (int i = 0 ; i != picnum ; ++i) {
            File file = new File(dir+gid+"_"+String.valueOf(i)+".jpeg");
            if (file.exists()) {
                file.delete();
            }
        }
    }


    private class myGridViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return imageArrayList.size() < MAXPICTURES ? imageArrayList.size()+1: MAXPICTURES;
        }

        @Override
        public Object getItem(int position) {
            if (imageArrayList.size() < MAXPICTURES) {
                if (position < imageArrayList.size() - 1)
                    return imageArrayList.get(position);
                else
                    return upload;
            } else {
                return imageArrayList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (imageArrayList.size() != MAXPICTURES && position == imageArrayList.size())
                return upload;
            else {
                View v;
                if (convertView != null) {
                    if (convertView != upload) {
                        v = convertView;
                    } else {
                        v = inflater.inflate(R.layout.image, null);
                    }
                } else {
                    v = inflater.inflate(R.layout.image, null);
                }

                mImage image = imageArrayList.get(position);

                if (image.isNew()) {
                    Picasso.with(getActivity())
                            .load(image.getImgURI())
                            .resize(IVWidth, IVHeight)
                            .centerInside()
                            .into((ImageView) v.findViewById(R.id.image));
                } else {
                    String filename = image.getFilename();
                    String path = imageManager.getImagePath(filename, ImageManager.GOODS);
                    if (path != null) {
                        image.setPath(path);
                        Picasso.with(getActivity())
                                .load(new File(path))
                                .resize(IVWidth, IVHeight)
                                .centerInside()
                                .into((ImageView) v.findViewById(R.id.image));
                    } else {
                        imageManager.downloadImage((ImageView) v.findViewById(R.id.image), filename, ImageManager.GOODS);
                    }
                }
                if (image.isCover())
                    v.findViewById(R.id.isCover).setVisibility(View.VISIBLE);
                else
                    v.findViewById(R.id.isCover).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.deletepic).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteImage(position);
                    }
                });
                return v;
            }
        }
    }

    private class mImage {
        //private Bitmap img;
        private Uri imgURI;
        private String realPath;
        private String md5; //修改原
        private boolean isCover;
        private boolean isNew;

        public mImage() { isNew = false; } //修改原招租信息时没有Uri
        public mImage(Uri uri) {
            imgURI = uri;
        }
        public mImage(String md5) {
            isNew = false;
            this.md5 = md5;
        }
        public void setPath(String p) { realPath = p; }
        public Uri getImgURI() { return imgURI; }
        public String getRealPath() { return realPath; }
        public void setCover(boolean c) {
            isCover = c;
        }
        public void setNew(boolean isNew) { this.isNew = isNew; }
        public boolean isCover() {
            return isCover;
        }
        public boolean isNew() { return isNew; }
        public String getFilename() { return md5+".jpeg"; }
    }


    /**
     * 返回封面编号
     * @return
     */
    private int findCover() {
        for (int i = 0; i != imageArrayList.size(); ++i) {
            if (imageArrayList.get(i).isCover()) {
                return i;
            }
        }
        return -1;
    }

    private class ProgressWindow {
        private TextView hint;
        private PopupWindow pop;
        private int total;

        public ProgressWindow(int total) {
            this.total = total;
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.loging, null);
            hint = (TextView)v.findViewById(R.id.hint);
            hint.setText("正在调整图片大小...");
            pop = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
            pop.setFocusable(true);
        }

        public void show() {
            if (!pop.isShowing()) {
                pop.showAtLocation(root, Gravity.CENTER|Gravity.CENTER, 0,0);
            }
        }

        public void dismiss() {
            if (pop.isShowing()) {
                pop.dismiss();
            }
        }

        public void setProgress(int p) {
            String text = null;
            if (p < total) {
                text = "正在上传图片，已完成" + String.valueOf(p) + "张,共" + String.valueOf(total) + "张";
            } else {
                text = "正在上传文本信息...";
            }
            hint.setText(text);
        }
    }
}
