package com.demo.aaronapplication.weizu;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.demo.aaronapplication.activity.ReleaseActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HttpUtil {
    public static final int SHOW_RESPONSE = 0;
    public static String res = new String();
    public static String host = "http://115.159.101.25:8008/";
    private static HttpParams params = new BasicHttpParams();

    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 20*1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码

    public static String sendHttpRequest(String address) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String HttpClientGET(String address, Handler handler) {
        final String pass = address;
        final Handler myhandler = handler;
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    HttpConnectionParams.setConnectionTimeout(params,10000);
                    HttpConnectionParams.setSoTimeout(params,10000);
                    HttpConnectionParams.setSocketBufferSize(params, 8192);
                    HttpClient httpClient = new DefaultHttpClient(params);
                    HttpGet httpGet = new HttpGet(pass);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");
                        if (myhandler != null) {
                            Message message = new Message();
                            message.what = SHOW_RESPONSE;
                            message.obj = response.toString();
                            myhandler.sendMessage(message);
                        }
                        res = response;
                    } else {
                        Message message = new Message();
                        message.what = httpResponse.getStatusLine().getStatusCode();
                        myhandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    Message message = new Message();
                    message.what = -1;
                    message.obj = e.toString();
                    myhandler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }).start();
        return res;
    }

    public static String SynHttpClientGet(String address) {
        String res = new String();
        try {
            HttpConnectionParams.setConnectionTimeout(params,20000);
            HttpConnectionParams.setSoTimeout(params,20000);
            HttpConnectionParams.setSocketBufferSize(params, 8192);
            HttpClient httpClient = new DefaultHttpClient(params);
            HttpGet httpGet = new HttpGet(address);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String response = EntityUtils.toString(entity, "utf-8");
                res = response;
            } else {
                Message message = new Message();
                message.what = httpResponse.getStatusLine().getStatusCode();
                res = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            res = null;
        } finally {
            return res;
        }
    }

    public static String HttpClientPOST(String address, List<NameValuePair> params) {
        final String pass = address;
        final List<NameValuePair> param = params;
        new Thread() {
            public void run() {
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(pass);
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(param, "utf-8");
                    httpPost.setEntity(entity);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entityGet = httpResponse.getEntity();
                        String response = EntityUtils.toString(entityGet, "utf-8");
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = response;
                        res = response;
                        Log.d("Test", "POST successfully" + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return res;
    }

    public static String JSONHttpPOST(String address, JSONObject params, Handler handler) {
        final String addr = address;
        final JSONObject param = params;
        final Handler mHandler = handler;
        new Thread() {
            public void run() {
                try {
                    HttpPost request = new HttpPost(addr);
                    StringEntity se = new StringEntity(param.toString());
                    request.setEntity(se);
                    HttpResponse httpResponse = new DefaultHttpClient().execute(request);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        String response = EntityUtils.toString(httpResponse.getEntity(),"utf-8");
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = response;
                        mHandler.sendMessage(message);
                        res = response;
                    } else {
                        Message message = new Message();
                        message.what = httpResponse.getStatusLine().getStatusCode();
                        mHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    Message message = new Message();
                    message.what = -1;
                    message.obj = e.toString();
                    mHandler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }.start();
        return res;
    }

    public static void upload(File f, String Url, final Handler myhandler) {
        final String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
        final String PREFIX = "--", LINE_END = "\r\n";
        final String CONTENT_TYPE = "multipart/form-data"; //内容类型
        final File file = f;
        final String _url = Url;
        final Message message = new Message();
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(_url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(TIME_OUT);
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setDoInput(true); //允许输入流
                    conn.setDoOutput(true); //允许输出流
                    conn.setUseCaches(false); //不允许使用缓存
                    conn.setRequestMethod("POST"); //请求方式
                    conn.setRequestProperty("Charset", CHARSET); //设置编码
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
                    if (file != null) {
                        /** * 当文件不为空，把文件包装并且上传 */
                        OutputStream outputSteam = conn.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(outputSteam);
                        StringBuffer sb = new StringBuffer();
                        sb.append(PREFIX);
                        sb.append(BOUNDARY);
                        sb.append(LINE_END);

                        sb.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"" + LINE_END);
                        sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                        sb.append(LINE_END);
                        dos.write(sb.toString().getBytes());
                        InputStream is = new FileInputStream(file);
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((len = is.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                        }
                        is.close();
                        dos.write(LINE_END.getBytes());
                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                        dos.write(end_data);
                        dos.flush();

                        if (conn.getResponseCode() == 200) {
                            // 获取响应的输入流对象
                            InputStream ips = conn.getInputStream();
                            // 创建字节输出流对象
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            len = 0;
                            // 定义缓冲区
                            byte buffer[] = new byte[1024];
                            // 按照缓冲区的大小，循环读取
                            while ((len = ips.read(buffer)) != -1) {
                                // 根据读取的长度写入到os对象中
                                baos.write(buffer, 0, len);
                            }
                            // 释放资源
                            ips.close();
                            baos.close();
                            // 返回字符串
                            final String result = new String(baos.toByteArray());
                            message.what = 0;
                            message.obj = result;
                        } else {
                            message.what = conn.getResponseCode();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    message.what = -1;
                } catch (IOException e) {
                    e.printStackTrace();
                    message.what = -1;
                }
                myhandler.sendMessage(message);
            }
        }.start();
    }

    public static void uploadRelease(final int action, final String TextUrl, final String ImageUrl, final JSONObject param, final String[] paths, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (paths.length > 0) {
                    //将图片保存到goods目录中，且对过大的图片进行缩放
                    String[] md5s = new String[paths.length];
                    for (int i = 0 ; i != paths.length ; ++i) {
                        md5s[i] = ImageManager.ShrinkImage(paths[i]);
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.arg1 = 0;
                    msg.sendToTarget();

                    JSONArray arr = null;
                    try {
                        param.put("cover", md5s[param.getInt("coverindex")]);

                        arr = new JSONArray();
                        for (int i = 0; i != md5s.length; ++i) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("md5", md5s[i]);
                            arr.put(jsonObject);
                        }

                        param.put("pictures", arr);

                    } catch (JSONException je) {
                        je.printStackTrace();
                    }

                    JSONObject exist = null;

                    try {
                        HttpPost request = new HttpPost(host + "verify");
                        StringEntity se = new StringEntity(arr.toString());
                        request.setEntity(se);
                        HttpResponse httpResponse = new DefaultHttpClient().execute(request);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            String res = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                            Message message = handler.obtainMessage();
                            message.what = 0;
                            exist = new JSONObject(res);
                        } else {
                            Message message = handler.obtainMessage();
                            message.what = httpResponse.getStatusLine().getStatusCode();
                            message.sendToTarget();
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Message message = handler.obtainMessage();
                        message.what = -1;
                        message.sendToTarget();
                        return;
                    } catch (JSONException je) {
                        je.printStackTrace();
                        Message message = handler.obtainMessage();
                        message.what = -1;
                        message.sendToTarget();
                        return;
                    }

                    String PREFIX = "--", LINE_END = "\r\n";
                    String CONTENT_TYPE = "multipart/form-data";
                    Log.e("start", "uploading");
                    int i = 0, retry = 0;
                    while (i < paths.length) {
                        Log.e("uploading", i + "");
                        try {
                            if (exist.getBoolean(md5s[i])) {
                                Message message = handler.obtainMessage();
                                message.what = 0;
                                message.arg1 = i + 1; //第i+1张图片上传成功
                                message.sendToTarget();
                                Log.e("message", String.valueOf(i+1)+" sent");
                                //该图片已存在于服务器上
                                i++;
                                retry = 0;
                                continue;
                            }
                            File file = new File(ImageManager.saveDir[ImageManager.GOODS]+md5s[i]+".jpeg");
                            if (!file.exists()) {
                                //应该不大可能出现
                                i++;
                                retry = 0;
                                continue;
                            }

                            String BOUNDARY = UUID.randomUUID().toString();
                            URL url = new URL(ImageUrl + "?md5=" + md5s[i]);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setReadTimeout(TIME_OUT);
                            conn.setConnectTimeout(TIME_OUT);
                            conn.setDoInput(true); //允许输入流
                            conn.setDoOutput(true); //允许输出流
                            conn.setUseCaches(false); //不允许使用缓存
                            conn.setRequestMethod("POST"); //请求方式
                            conn.setRequestProperty("Charset", CHARSET);
                            conn.setRequestProperty("connection", "keep-alive");
                            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                            OutputStream outputSteam = conn.getOutputStream();
                            DataOutputStream dos = new DataOutputStream(outputSteam);
                            StringBuffer sb = new StringBuffer();
                            sb.append(PREFIX);
                            sb.append(BOUNDARY);
                            sb.append(LINE_END);

                            sb.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + md5s[i] + ".jpeg" + "\"" + LINE_END);
                            sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                            sb.append(LINE_END);
                            dos.write(sb.toString().getBytes());
                            InputStream is = new FileInputStream(file);
                            byte[] bytes = new byte[2048];
                            int len = 0;
                            while ((len = is.read(bytes)) != -1) {
                                dos.write(bytes, 0, len);
                            }
                            is.close();
                            dos.write(LINE_END.getBytes());
                            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                            dos.write(end_data);
                            dos.flush();

                            if (conn.getResponseCode() == 200) {
                                InputStream ips = conn.getInputStream();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                len = 0;
                                byte buffer[] = new byte[64];
                                while ((len = ips.read(buffer)) != -1) {
                                    baos.write(buffer, 0, len);
                                }
                                ips.close();
                                baos.close();
                                final String result = new String(baos.toByteArray());
                                if (result.equals("1")) {
                                    Message message = handler.obtainMessage();
                                    message.what = 0;
                                    message.arg1 = i + 1; //第i+1张图片上传成功
                                    message.sendToTarget();
                                    i++;
                                    retry = 0;
                                    Log.e("message", String.valueOf(i+1)+" sent");
                                } else {
                                    //失败重试，最多三次
                                    if (retry < 3) {
                                        retry++;
                                    } else {
                                        i++;
                                        retry = 0;
                                    }
                                }
                            } else {
                                //失败重试，最多三次
                                if (retry < 3) {
                                    retry++;
                                } else {
                                    i++;
                                    retry = 0;
                                }
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            i++;
                            retry = 0;
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                            i++;
                            retry = 0;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            i++;
                            retry = 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (retry < 3) {
                                retry++;
                            } else {
                                i++;
                                retry = 0;
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                            Message message = handler.obtainMessage();
                            message.what = -1;
                            message.sendToTarget();
                            return;
                        }
                    }
                } else {
                    try {
                        param.put("cover", "");
                        param.put("pictures", "");
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }

                String gid;
                try {
                    HttpPost request = new HttpPost(TextUrl);
                    StringEntity se = new StringEntity(param.toString());
                    request.setEntity(se);
                    HttpResponse httpResponse = new DefaultHttpClient().execute(request);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        String res = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                        Message message = handler.obtainMessage();
                        message.what = 0;
                        if (action == ReleaseActivity.NEW) {
                            gid = res;
                            if (res.equals("-1"))  {
                                message.arg1 = -1;
                                message.sendToTarget();
                                return;
                            } else {
                                message.arg1 = 0; //0代表文本信息上传成功
                                message.sendToTarget();
                            }
                        } else {
                            gid = String.valueOf(param.getInt("gid"));
                            if (res.equals("1"))  {
                                message.arg1 = 0; //0代表文本信息上传成功
                                message.sendToTarget();
                            } else {
                                message.arg1 = -1;
                                message.sendToTarget();
                                return;
                            }
                        }

                    } else {
                        Message message = handler.obtainMessage();
                        message.what = httpResponse.getStatusLine().getStatusCode();
                        message.sendToTarget();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = -1;
                    message.sendToTarget();
                    return;
                } catch (JSONException e) {
                    //不可能
                    Message message = handler.obtainMessage();
                    message.what = -1;
                    message.sendToTarget();
                    e.printStackTrace();
                    return;
                }

                Message message = handler.obtainMessage();
                message.what = 100;
                message.obj = gid;
                message.sendToTarget();
            }
        }).start();
    }

    public static void downloadPic(final String filename, final String Url, final String saveDir, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Url);
                    // 记住使用的是HttpURLConnection类
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    //如果运行超过5秒会自动失效 这是android规定
                    conn.setConnectTimeout(5 * 1000);
                    InputStream inStream = conn.getInputStream();
                    //调用readStream方法
                    ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = inStream.read(buffer)) != -1) {
                        outSteam.write(buffer, 0, len);
                    }
                    outSteam.close();
                    inStream.close();
                    byte[] data = outSteam.toByteArray();
                    FileOutputStream foutStream = new FileOutputStream(new File(saveDir + filename));
                    foutStream.write(data);
                    foutStream.close();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Message msg = handler.obtainMessage();
                msg.what = 0;
                msg.sendToTarget();
            }
        }).start();

    }
}
