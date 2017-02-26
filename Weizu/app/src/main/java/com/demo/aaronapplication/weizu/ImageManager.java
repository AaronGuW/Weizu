package com.demo.aaronapplication.weizu;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 *  主要为picasso库服务，我们不把从网络获取图片的任务交给picasso，我们自己获取并缓存，Picasso只负责加载本地图片
 */
public class ImageManager {
    private onFinishLoadListener listener;

    public static final int GOODS = 0, PORTRAIT = 1, ORDER = 2; //order 暂时弃用
    public static final String[] textType = {"goods","portrait","order"};
    public static final String[] saveDir = { Environment.getExternalStorageDirectory().getPath()+"/weizu/img/goods/",
                                                Environment.getExternalStorageDirectory().getPath()+"/weizu/img/portrait/",
                                                Environment.getExternalStorageDirectory().getPath()+"/weizu/img/orders/"};

    public interface onFinishLoadListener {
        void onFinishLoading(ImageView holder, String path);
    }

    public ImageManager() {}

    public void setOnFinishLoadListener(onFinishLoadListener l) {
        listener = l;
    }

    public String getImagePath(String filename, int type) {
        if (isImageCached(filename, type)) {
            return saveDir[type] + filename;
        }
        return null;
    }

    public void downloadImage(final ImageView holder, final String filename, final int type) {
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    listener.onFinishLoading(holder, saveDir[type] + filename);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                try {
                    String Url = HttpUtil.host+"image?type=" + textType[type] +"&file="+filename; //组合url
                    URL url = new URL(Url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5 * 1000);
                    if (conn.getResponseCode() == 200) {
                        InputStream inStream = conn.getInputStream();
                        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        boolean empty = true;
                        while ((len = inStream.read(buffer)) != -1) {
                            if (empty && len > 100)
                                empty = false;
                            outSteam.write(buffer, 0, len);
                        }
                        outSteam.close();
                        inStream.close();
                        if (!empty) {
                            byte[] data = outSteam.toByteArray();
                            File file = new File(saveDir[type] + filename);
                            if (file.exists())
                                file.delete();
                            FileOutputStream foutStream = new FileOutputStream(file);
                            foutStream.write(data);
                            foutStream.close();
                            msg.what = 0;
                        } else {
                            msg.what = 1; //服务器上没有该文件
                        }
                    } else {
                        msg.what = -1;
                        Log.e("ImageManager failure",String.valueOf(conn.getResponseCode()));
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    msg.what = -1;
                } catch (MalformedURLException e) {
                    msg.what = -1;
                    e.printStackTrace();
                } catch (IOException e) {
                    msg.what = -1;
                    e.printStackTrace();
                } finally {
                    msg.sendToTarget();
                }
            }
        }).start();
    }

    public static boolean isImageCached(String filename, int type) {
        File file = new File(saveDir[type]+filename);
        return file.exists();
    }

}
