package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.DBManager;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.goods;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Aaron on 2016/9/7.
 */
public class MyReleaseActivity extends Activity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView myreleaselist;
    private BaseAdapter releaseAdapter;
    private LayoutInflater layoutInflater;
    private ArrayList<goods> releaseArraylist;
    private SwipeRefreshLayout swipeList;

    private ImageView newrelease;

    private int loadStatus;
    private static final int SUCCEED = 0, SUCCEEDBUTNONE = 1, FAILURE = 2;

    private Handler mHandler;

    private goods emptyGoods;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myrelease);

        swipeList = (SwipeRefreshLayout)findViewById(R.id.swipelist);
        swipeList.setOnRefreshListener(this);
        myreleaselist = (ListView)findViewById(R.id.myreleaselist);
        newrelease = (ImageView)findViewById(R.id.newrelease);
        newrelease.setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);
        layoutInflater = LayoutInflater.from(this);
        releaseArraylist = new ArrayList<>();
        emptyGoods = new goods();
        releaseArraylist.add(emptyGoods);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleRefreshMessage(msg);
            }
        };

        init_releaselist();
        refresh();
    }

    private void init_releaselist() {
        loadStatus = SUCCEEDBUTNONE;

        releaseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return releaseArraylist.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                switch (loadStatus) {
                    case SUCCEED:
                        if (convertView != null) {
                            if (convertView.getId() == R.id.unique_goods)
                                v = convertView;
                            else
                                v = layoutInflater.inflate(R.layout.releaseitem, null);
                        } else {
                            v = layoutInflater.inflate(R.layout.releaseitem, null);
                        }
                        ((TextView) v.findViewById(R.id.title)).setText(releaseArraylist.get(position).getTitle());
                        break;
                    case SUCCEEDBUTNONE:
                        v = layoutInflater.inflate(R.layout.norelease, null);
                        break;
                    case FAILURE:
                        v = layoutInflater.inflate(R.layout.loadfailure, null);
                        break;
                    default:
                        v = layoutInflater.inflate(R.layout.norelease, null);
                }
                return v;
            }
        };
        myreleaselist.setAdapter(releaseAdapter);
        myreleaselist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (loadStatus != SUCCEED)
                    return;
                Intent intent = new Intent(MyReleaseActivity.this, ReleaseActivity.class);
                intent.putExtra("action",ReleaseActivity.MODIFY);
                intent.putExtra("gid",releaseArraylist.get(position).getGid());
                startActivityForResult(intent,mainActivity.NEW_MODIFY_RELEASE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newrelease:
                Intent intent = new Intent(MyReleaseActivity.this, ReleaseActivity.class);
                intent.putExtra("action", ReleaseActivity.NEW);
                startActivityForResult(intent, mainActivity.NEW_MODIFY_RELEASE);
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (data.getBooleanExtra("modify",false)) {
            refresh();
        } else if (data.getBooleanExtra("delete",false)) {
            refresh();
        }
    }

    /**
     * 用户下拉刷新后，处理从服务器请求获得的发布列表
     */
    private void handleRefreshMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                JSONObject response = new JSONObject(res);

                releaseArraylist.clear();

                int resCode = response.getInt("rescode");
                if (resCode == -1) {
                    loadStatus = FAILURE;
                    releaseArraylist.add(emptyGoods); /* add one empty object to render the hint entry */
                } else if (resCode == 0) {
                    loadStatus = SUCCEEDBUTNONE;
                    releaseArraylist.add(emptyGoods);
                } else {
                    loadStatus = SUCCEED;
                    JSONArray content = new JSONArray(response.getString("content"));
                    for (int i = 0 ; i != content.length() ; ++i) {
                        JSONObject release = content.getJSONObject(i);
                        goods g = new goods(release,true);
                        releaseArraylist.add(g);
                    }
                }
            } else {
                Toast.makeText(MyReleaseActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                loadStatus = FAILURE;
                releaseArraylist.clear();
                releaseArraylist.add(emptyGoods);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseAdapter.notifyDataSetChanged();
            if (swipeList.isRefreshing())
                swipeList.setRefreshing(false);
        }
    }

    /**
     * refresh the release list, called by father fragment
     */
    public void refresh() {
        HttpUtil.HttpClientGET(HttpUtil.host+"release?action="+String.valueOf(ReleaseActivity.GET)+
                "&uid="+getSharedPreferences("account", Context.MODE_PRIVATE).getString("uid","0"),mHandler);
    }
}
