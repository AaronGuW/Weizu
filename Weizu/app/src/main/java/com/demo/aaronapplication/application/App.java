package com.demo.aaronapplication.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;

import io.rong.imkit.RongIM;

/**
 * Created by Aaron on 2016/9/4.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        createFolder(); //创建必要的文件夹
        /**
         * 初始化融云
         */
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext())) ||
                "io.rong.push".equals(getCurProcessName(getApplicationContext()))) {

            /**
             * IMKit SDK调用第一步 初始化
             */
            RongIM.init(this);
        }
    }

    /**
     * 获得当前进程的名字
     *
     * @param context
     * @return 进程号
     */
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private void createFolder() {
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu");
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img");
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/goods");
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/orders");
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/portrait");
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/thumbnails");
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
