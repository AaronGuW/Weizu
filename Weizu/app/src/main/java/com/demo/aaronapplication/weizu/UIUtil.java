package com.demo.aaronapplication.weizu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Aaron on 2016/8/25.
 */

public class UIUtil {


    public static int dp2px(Context context, int dp) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.density;
        return (int)(dp*density);
    }

    /**
     * 计算控件长宽，主要为picasso服务，载入前先resize节省内存
     * @param v 目标view
     * @return {width, height}
     */
    public static int[] calcViewSize(View v) {
        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int height = v.getMeasuredHeight();
        int width = v.getMeasuredWidth();
        Log.e("measure","finished");
        return new int[]{width, height};
    }

    public static int[] getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        return new int[]{size.x, size.y};
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}
