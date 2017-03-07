package com.demo.aaronapplication.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.activity.BrowseGoodsActivity;
import com.demo.aaronapplication.activity.ReleaseActivity;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.demo.aaronapplication.weizu.goods;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Aaron on 2016/3/23.
 */
public class resultFragment extends Fragment implements ImageManager.onFinishLoadListener {

    private ListView goodslist;
    private BaseAdapter goodsadapter;
    private ArrayList<goods> goodsArrayList;
    private String gids[];
    private LayoutInflater layoutinflater;
    private int currentIndex; /* 已经加载的商品数 */

    private static final int LOADAMOUNT = 500; /* 单次加载的商品数 */
    //private static DecimalFormat decimalFormat = new DecimalFormat(".00");
    private static final DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");

    private Handler resHandler;

    private ImageManager imageManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.goodspage, container, false);
        goodslist = (ListView)v.findViewById(R.id.goodslist);

        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);

        resHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleGoodsMessage(msg);
            }
         };

        goodsArrayList = new ArrayList<>();
        layoutinflater = LayoutInflater.from(getActivity());
        init_goodslist();

        String res = getArguments().getString("res");
        gids = res.split("#");
        currentIndex = 0;
        String goods2load = join();
        Log.e("gids",goods2load);
        loadGoods(goods2load);

        return v;
    }

    private void init_goodslist() {
        goodsadapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return goodsArrayList.size();
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
                if (convertView != null) {
                    v = convertView;
                } else {
                    v = layoutinflater.inflate(R.layout.goodsitem,null);
                }
                goods g = goodsArrayList.get(position);
                if (g.hasCover()) {
                    String path = imageManager.getImagePath(g.getCoverName(), ImageManager.THUMBNAIL);
                    ImageView holder = (ImageView)v.findViewById(R.id.goodpic);
                    if (path != null) {
                        Picasso.with(getActivity()).load(new File(path)).resize(128, 128).centerInside().into(holder);
                    } else {
                        imageManager.downloadImage(holder, g.getCoverName(), ImageManager.THUMBNAIL);
                    }
                }
                ((TextView)v.findViewById(R.id.goodname)).setText(g.getTitle());
                ((TextView)v.findViewById(R.id.goodbrief)).setText(g.getShortDescription());
                ((TextView)v.findViewById(R.id.goodprice)).setText(g.getFormatPrice());
                ((TextView)v.findViewById(R.id.goodloc)).setText(g.getLocation());
                return v;
            }
        };

        goodslist.setAdapter(goodsadapter);
        goodslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), BrowseGoodsActivity.class);
                intent.putExtra("gid",goodsArrayList.get(position).getGid());
                startActivity(intent);
            }
        });
    }

    private void loadGoods(String gids) {
        HttpUtil.HttpClientGET(HttpUtil.host+"release?action="+ String.valueOf(ReleaseActivity.SIMPLE)+"&gids="+gids, resHandler);
    }

    private void handleGoodsMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (!res.equals("0")) {
                    JSONArray jsons = new JSONArray(res);
                    if (jsons.length() > 0) {
                        for (int i = 0 ; i != jsons.length() ; ++i) {
                            goodsArrayList.add(new goods(jsons.getJSONObject(i), true));
                        }
                        goodsadapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "抱歉,没有符合条件的宝贝", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "抱歉,没有符合条件的宝贝", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String join() {
        if (currentIndex == gids.length)
            return null;
        String strgid = new String();
        int end = currentIndex + LOADAMOUNT > gids.length? gids.length: currentIndex + LOADAMOUNT;
        for (int i = currentIndex; i != end - 1 ; ++i) {
            strgid += gids[i]+"/";
        }
        strgid += gids[end-1];
        currentIndex = end;
        return strgid;
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        try {
            Picasso.with(getActivity())
                    .load(new File(path))
                    .resize(128, 128)
                    .centerInside()
                    .into(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
