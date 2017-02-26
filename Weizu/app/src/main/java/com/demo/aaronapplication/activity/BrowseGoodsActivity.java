package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.fragments.trolleyFragment;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.ImageViewPopupWindow;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.RecyclerImageView;
import com.demo.aaronapplication.weizu.UIUtil;
import com.demo.aaronapplication.weizu.goods;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.rong.imkit.RongIM;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Aaron on 2016/3/27.
 */
public class BrowseGoodsActivity extends Activity implements View.OnClickListener, ImageManager.onFinishLoadListener{

    private View back_btn, view_comments, send_msg, add_to_trolley, rent_now;
    private TextView title, price, freight, deposit, sales, comment_num, leaser, desc;
    private LayoutInflater inflater;
    private PopupWindow pop;

    private SharedPreferences account;

    private Handler popdismiss_handler;
    private Handler loadHandler, addHandler;
    private goods viewedGoods;

    private ImageManager imageManager;
    private ViewPager pictures;
    private myPagerAdapter picturesAdapter;
    private ArrayList<page> images;

    private ImageViewPopupWindow popView;

    private TextView alert; //弹出窗口中的提示信息

    private static final DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goodsdetail_page);

        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);
        inflater = LayoutInflater.from(this);
        account = getSharedPreferences("account", MODE_PRIVATE);

        init_view();
        init_pop();
        popdismiss_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                pop.dismiss();
            }
        };
        loadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleLoadMessage(msg);
            }
        };
        addHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleAddMessage(msg);
            }
        };

        loadGoods(getIntent().getIntExtra("gid",0));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.viewComments:
                if (viewedGoods != null) {
                    Intent intent = new Intent(BrowseGoodsActivity.this, CommentsActivity.class);
                    intent.putExtra("gid", viewedGoods.getGid());
                    intent.putExtra("total", viewedGoods.getComment_num());
                    startActivity(intent);
                }
                break;
            case R.id.send_msg:
                if (viewedGoods != null) {
                    if (account.getBoolean("login",false)) {
                        if (viewedGoods.getLeaser() != Integer.valueOf(account.getString("uid", "0"))) {
                            if (RongIM.getInstance() != null) {
                                RongIM.getInstance().startPrivateChat(this, String.valueOf(viewedGoods.getLeaser()), viewedGoods.getLeaserName());
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.notalk), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.add_to_trolley:
                if (viewedGoods != null) {
                    if (account.getBoolean("login",false)) {
                        if (viewedGoods.getLeaser() != Integer.valueOf(account.getString("uid", "0"))) {
                            if (viewedGoods.isDeleted()) {
                                Toast.makeText(this, getString(R.string.deletedgoods), Toast.LENGTH_SHORT).show();
                            } else {
                                addToTrolley(viewedGoods.getGid());
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.yourRelease), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.rent_btn:
                if (viewedGoods != null) {
                    if (account.getBoolean("login",false)) {
                        if (viewedGoods.getLeaser() != Integer.valueOf(account.getString("uid", "0"))) {
                            if (viewedGoods.isDeleted()) {
                                Toast.makeText(this, getString(R.string.deletedgoods), Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent1 = new Intent(this, tradeActivity.class);
                                intent1.putExtra("goods", viewedGoods);
                                intent1.putExtra("amount", 1);
                                startActivity(intent1);
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.yourRelease), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && popView != null && popView.isShowing()) {
            popView.dismiss();
            return true;
        } else {
            return super.onKeyDown(keyCode,event);
        }
    }

    private void init_view() {
        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);
        title = (TextView)findViewById(R.id.title);
        price = (TextView)findViewById(R.id.price);
        freight = (TextView)findViewById(R.id.transfee);
        deposit = (TextView)findViewById(R.id.deposit);
        sales = (TextView)findViewById(R.id.sales);
        comment_num = (TextView)findViewById(R.id.comment_num);
        leaser = (TextView)findViewById(R.id.leaser);
        desc = (TextView)findViewById(R.id.desc);
        send_msg = findViewById(R.id.send_msg);
        send_msg.setOnClickListener(this);
        add_to_trolley = findViewById(R.id.add_to_trolley);
        add_to_trolley.setOnClickListener(this);
        rent_now = findViewById(R.id.rent_btn);
        rent_now.setOnClickListener(this);
        view_comments = findViewById(R.id.viewComments);
        view_comments.setOnClickListener(this);

        pictures = (ViewPager) findViewById(R.id.pictures);

    }

    private void init_pop() {
        View v = inflater.inflate(R.layout.popup_addtotrolley,null);
        alert = (TextView)v.findViewById(R.id.alert);
        pop = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        pop.setAnimationStyle(R.style.addtotrolley_anim_style);
        pop.setOutsideTouchable(false);
    }

    private void loadGoods(int gid) {
        HttpUtil.HttpClientGET(HttpUtil.host+"release?action="+String.valueOf(ReleaseActivity.FULL)+"&gid="+String.valueOf(gid), loadHandler);
    }

    private void addToTrolley(int gid) {
        HttpUtil.HttpClientGET(HttpUtil.host+"trolley?action="+String.valueOf(trolleyFragment.ADD)+"&gid="+String.valueOf(gid)
                                +"&uid="+getSharedPreferences("account",MODE_PRIVATE).getString("uid","0"), addHandler);
    }

    private void loadView() {
        title.setText(viewedGoods.getTitle());
        price.setText(viewedGoods.getFormatPrice());
        deposit.setText(decimalFormat.format(viewedGoods.getDeposit()));
        freight.setText(decimalFormat.format(viewedGoods.getFreight()));
        sales.setText(String.valueOf(viewedGoods.getSales()));
        comment_num.setText(String.valueOf(viewedGoods.getComment_num()));
        leaser.setText(String.valueOf(viewedGoods.getLeaserName()));
        desc.setText(viewedGoods.getDescription());

        if (viewedGoods.isDeleted())
            rent_now.setBackgroundColor(0xff888888);

        LayoutInflater inflater = LayoutInflater.from(this);
        images = new ArrayList<>();
        final int picnum = viewedGoods.getPicnum();
        String gid = String.valueOf(viewedGoods.getGid());
        String[] md5s = viewedGoods.getPictures();
        for (int i = 0; i != picnum; ++i) {
            View v = inflater.inflate(R.layout.goodpicitem, null);
            RecyclerImageView image = (RecyclerImageView) v.findViewById(R.id.holder);
            page p = new page(v);
            String filename = md5s[i]+".jpeg";
            String path = imageManager.getImagePath(filename, ImageManager.GOODS);
            if (path != null) {
                int size[] = UIUtil.getScreenSize(this);
                Log.e("size",size[0]+" "+size[1]+"");
                Picasso.with(this).load(new File(path)).resize((int)(size[0]*1.5),size[0]).centerInside().into(image);
                v.findViewById(R.id.progress).setVisibility(View.GONE);
                p.setLoaded();
                p.setPath(path);
            } else {
                imageManager.downloadImage(image, filename, ImageManager.GOODS);
            }
            images.add(p);
        }
        picturesAdapter = new myPagerAdapter(images);
        pictures.setAdapter(picturesAdapter);
        pictures.setCurrentItem(0);
    }

    private void handleLoadMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (!res.equals("0")) {
                    JSONObject json = new JSONObject(res);
                    viewedGoods = new goods(json, false);
                    loadView();
                } else {
                    Toast.makeText(BrowseGoodsActivity.this, "载入失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BrowseGoodsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (res.equals("0")) {
                    alert.setText(getString(R.string.fullTrolley));
                } else if (res.equals("1")) {
                    alert.setText(getString(R.string.alreadyInTrolley));
                } else if (res.equals("2")) {
                    alert.setText(getString(R.string.addToTrolleyOK));
                }
                pop.showAtLocation(findViewById(R.id.root), Gravity.CENTER | Gravity.CENTER, 0, 0);
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        popdismiss_handler.sendMessage(new Message());
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task,1500);
            } else {
                Toast.makeText(BrowseGoodsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        if (holder != null) {
            int[] size = UIUtil.calcViewSize(holder);
            if (size[0] <= 0 && size[1] <= 0)
                return;
            Picasso.with(this).load(new File(path)).resize(size[0], size[1]).centerInside().into(holder);

            for (int i = 0; i != images.size(); ++i) {
                if (holder.equals(images.get(i).getView().findViewById(R.id.holder))) {
                    images.get(i).getView().findViewById(R.id.progress).setVisibility(View.GONE);
                    images.get(i).setPath(path);
                    images.get(i).setLoaded();
                    break;
                }
            }
        }
    }

    private class myPagerAdapter extends PagerAdapter {

        private ArrayList<page> views;

        public myPagerAdapter(ArrayList<page> v) {
            views = v;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final int _position = position;
            View v = images.get(position).getView();
            if (images.get(position).hasLoaded()) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*String path = images.get(position).getPath();
                        int[] size = UIUtil.getScreenSize(BrowseGoodsActivity.this);
                        Picasso.with(BrowseGoodsActivity.this).load(new File(path)).resize(size[0]*2, size[1]*2).centerInside().into(big_image);
                        mAttacher.update();
                        pop_img.showAtLocation(findViewById(R.id.root), Gravity.CENTER, 0, 0);*/
                        //Toast.makeText(BrowseGoodsActivity.this, "page " + String.valueOf(position) + " clicked", Toast.LENGTH_SHORT).show();
/*                        if (popView == null) {*/
                        popView = new ImageViewPopupWindow(BrowseGoodsActivity.this, images, _position);
                        if (!popView.isShowing()) {
                            popView.setBackgroundDrawable(getResources().getDrawable(R.drawable.emptybg));
                            popView.setAnimationStyle(R.style.bigimage_anim_style);
                            popView.showAtLocation(findViewById(R.id.root), Gravity.CENTER, 0, UIUtil.getStatusBarHeight(BrowseGoodsActivity.this));
                        }
/*                        } else {
                            if (!popView.isShowing()) {
                                popView.showAtLocation(findViewById(R.id.root), Gravity.CENTER, 0, UIUtil.getStatusBarHeight(BrowseGoodsActivity.this));
                                popView.resetCurrentItem(_position);
                            }
                        }*/
                    }
                });
            }
            container.addView(v, 0);
            return v;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (View)arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    public static class page {
        private View v;
        private String path;
        private boolean loaded;

        public page(View v) {
            this.v = v;
            loaded = false;
        }

        public void setPath(String p) {
            path = p;
        }

        public void setLoaded() {
            loaded = true;
        }

        public View getView() { return v; }
        public String getPath() { return path; }
        public boolean hasLoaded() { return loaded; }
    }

}
