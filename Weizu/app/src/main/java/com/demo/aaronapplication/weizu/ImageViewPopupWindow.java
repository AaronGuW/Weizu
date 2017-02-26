package com.demo.aaronapplication.weizu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.demo.aaronapplication.activity.BrowseGoodsActivity.page;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Aaron on 2017/1/30.
 */
public class ImageViewPopupWindow extends PopupWindow {

    private ArrayList<page> images;
    private ViewPager pager;
    private LinearLayout cursorBar;
    private RecyclerImageView[] cursors;
    private myPagerAdapter adapter;

    private LayoutInflater inflater;
    private Context context;

    private int currentItem;
    private Bitmap cursor_inactive, cursor_active;

    public ImageViewPopupWindow(Context context, ArrayList<page> images, int curIndex) {
        super(context);
        this.images = images;
        this.context = context;
        currentItem = curIndex;

        int[] size = calWidthAndHeight(context);
        setWidth(size[0]);
        setHeight(size[1]);

        inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.bigpicturesbrowser, null);
        setContentView(v);
        pager = (ViewPager)v.findViewById(R.id.pager);
        cursorBar = (LinearLayout)v.findViewById(R.id.cursor_holder);
        v.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewPopupWindow.this.dismiss();
            }
        });

        init(curIndex);
    }

    private void init(final int curIndex) {
        cursor_active = BitmapFactory.decodeResource(context.getResources(), R.drawable.cursor_active);
        cursor_inactive = BitmapFactory.decodeResource(context.getResources(), R.drawable.cursor_inactive);

        cursors = new RecyclerImageView[images.size()];
        for (int i = 0 ; i != images.size() ; ++i) {
            RecyclerImageView cursor = (RecyclerImageView)inflater.inflate(R.layout.cursor, null);
            cursors[i] = cursor;
            if (i == curIndex) {
                cursor.setImageBitmap(cursor_active);
            }
            cursorBar.addView(cursor);
        }

        adapter = new myPagerAdapter();
        pager.setAdapter(adapter);
        pager.setCurrentItem(curIndex);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                cursors[currentItem].setImageBitmap(cursor_inactive);
                cursors[currentItem = position].setImageBitmap(cursor_active);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    /**
     * 设置PopupWindow的大小
     * @param context
     */
    private int[] calWidthAndHeight(Context context) {
        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics= new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        int size[] = new int[2];
        size[0] = metrics.widthPixels;
        size[1] = metrics.heightPixels - UIUtil.getStatusBarHeight(context);
        return size;
    }

    private class myPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View)object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = inflater.inflate(R.layout.goodpicitem, null);
            if (images.get(position).hasLoaded()) {
                v.findViewById(R.id.progress).setVisibility(View.GONE);
                RecyclerImageView holder = (RecyclerImageView)v.findViewById(R.id.holder);
                final PhotoViewAttacher attacher = new PhotoViewAttacher(holder);
                String path = images.get(position).getPath();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                int height = options.outHeight, width = options.outWidth;
                float h_scale = (float)height/1280, w_scale = (float)width/720;
                int newh, neww;
                if (h_scale > 1 || w_scale > 1) {
                    if (h_scale > w_scale) {
                        newh = 1280;
                        neww = (int) ((float) width / h_scale);
                    } else {
                        newh = (int) ((float) height / w_scale);
                        neww = 720;
                    }
                } else {
                    newh = height;
                    neww = width;
                }
                Picasso.with(context).load(new File(images.get(position).getPath()))
                        .resize(neww, newh)
                        .centerInside()
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(holder, new com.squareup.picasso.Callback() {

                            @Override
                            public void onSuccess() {
                                attacher.update();
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
            container.addView(v, 0);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }

}
