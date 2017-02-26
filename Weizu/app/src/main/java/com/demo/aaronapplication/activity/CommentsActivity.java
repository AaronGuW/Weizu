package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Aaron on 2017/1/13.
 */
public class CommentsActivity extends Activity implements View.OnClickListener {

    private ListView comments;  //评论的ListView
    private ArrayList<JSONObject> commentsArrayList; //评论数据列表
    private BaseAdapter commentsAdapter;    //评论的listview的适配器

    private Handler commentsLoadingHandler; //加载评论的handler

    private LayoutInflater inflater;

    private int gid; //查看评论的商品ID
    private int total;  //总评论数
    private int loaded_num;    //已经加载的评论数
    private View allCommentLoaded, click2LoadMore, Loading;

    private boolean loading;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commentspage);

        gid = getIntent().getIntExtra("gid",0);
        total = getIntent().getIntExtra("total",0);
        loaded_num = 0;

        initialize();
    }

    private void initialize() {
        inflater = LayoutInflater.from(this);
        Loading = inflater.inflate(R.layout.loading, null);
        allCommentLoaded = inflater.inflate(R.layout.allcommentloaded, null);
        click2LoadMore = inflater.inflate(R.layout.click2loadmore, null);
        click2LoadMore.findViewById(R.id.hint).setOnClickListener(this);

        findViewById(R.id.back_btn).setOnClickListener(this);

        commentsArrayList = new ArrayList<>();

        comments = (ListView)findViewById(R.id.commentslist);

        commentsLoadingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleLoadingMessage(msg);
            }
        };

        loadComments(loaded_num);

        commentsAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return commentsArrayList.size() != 0 ? commentsArrayList.size()+1:1;
            }

            @Override
            public Object getItem(int position) {
                return commentsArrayList.size() != 0 ? commentsArrayList.get(position): null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (commentsArrayList.size() == 0) {
                    if (loading) {
                        return Loading;
                    } else {
                        return inflater.inflate(R.layout.nocomment, null);
                    }
                }
                //listview的最后一项是功能项，显示：1.点击加载更多，2.加载中, 3.已加载全部评论
                if (position == commentsArrayList.size()) {
                    if (loading) {
                        return Loading;
                    } else {
                        if (loaded_num < total) {
                            return click2LoadMore;
                        } else {
                            return allCommentLoaded;
                        }
                    }
                }
                View v;
                if (convertView != null && convertView.getId() == R.id.comment_item) {
                    v = convertView;
                } else {
                    v = inflater.inflate(R.layout.comment_item, null);
                }
                JSONObject comment = commentsArrayList.get(position);
                try {
                    ((TextView) v.findViewById(R.id.username)).setText(comment.getString("username"));
                    ((TextView) v.findViewById(R.id.content)).setText(comment.getString("content"));
                    ((TextView) v.findViewById(R.id.date)).setText(dateFormat.format(new Date(comment.getLong("date"))));

                    ImageView stars[] = new ImageView[5];
                    stars[0] = (ImageView)v.findViewById(R.id.star1);
                    stars[1] = (ImageView)v.findViewById(R.id.star2);
                    stars[2] = (ImageView)v.findViewById(R.id.star3);
                    stars[3] = (ImageView)v.findViewById(R.id.star4);
                    stars[4] = (ImageView)v.findViewById(R.id.star5);
                    int credit = comment.getInt("credit");
                    for (int i = 4 ; i > 0 ; i--) {
                        int limit = i*2 + 1;
                        if (credit == limit) {
                            stars[i].setImageDrawable(getResources().getDrawable(R.drawable.star_half));
                            break;
                        } else if (credit < limit) {
                            stars[i].setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
                        } else
                            break;
                    }

                } catch (JSONException je) { //never gonna happen
                    je.printStackTrace();
                }
                return v;
            }
        };
        comments.setAdapter(commentsAdapter);
    }

    private void loadComments(int number) {
        loading = true;
        HttpUtil.HttpClientGET(HttpUtil.host+"c?gid="+String.valueOf(gid)+"&num="+String.valueOf(number), commentsLoadingHandler);
    }

    private void handleLoadingMessage(Message msg) {
        try {
            if (msg.what == 0) {
                loading = false;
                JSONArray jsonArray = new JSONArray(msg.obj.toString());
                loaded_num += jsonArray.length();
                for (int i = 0 ; i != jsonArray.length() ; ++i) {
                    commentsArrayList.add(jsonArray.getJSONObject(i));
                }
                commentsAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(CommentsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                CommentsActivity.this.finish();
                break;
            case R.id.hint: //click to load more中的hint
                loading = true;
                loadComments(loaded_num);
                commentsAdapter.notifyDataSetChanged();
                break;
        }
    }

}
