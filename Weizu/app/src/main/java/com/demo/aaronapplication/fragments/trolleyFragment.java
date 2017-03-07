package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.activity.BrowseGoodsActivity;
import com.demo.aaronapplication.activity.tradeActivity;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.demo.aaronapplication.weizu.goods;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class trolleyFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ImageManager.onFinishLoadListener {

    private ListView goodslist;
    private ArrayList<trolleyItem> trolleyArray;
    private Handler trolleyHandler, deleteHandler;
    private BaseAdapter goodsAdapter;
    private LayoutInflater layoutInflater;
    private SwipeRefreshLayout listWrapper;
    private View settle;
    private TextView total;

    private ImageManager imageManager;

    //private static final DecimalFormat decimalFormat = new DecimalFormat(".00");
    private static final DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");

    public static final int GET = 0, ADD = 1, DEL = 2, SETTLE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.trolleypage, container, false);
        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);
        listWrapper = (SwipeRefreshLayout)v.findViewById(R.id.listWrapper);
        listWrapper.setOnRefreshListener(this);
        goodslist = (ListView)v.findViewById(R.id.goodslist);
        settle = v.findViewById(R.id.settle);
        settle.setOnClickListener(this);
        total = (TextView)v.findViewById(R.id.total);
        layoutInflater = LayoutInflater.from(getActivity());
        trolleyHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleTrolleyMessage(msg);
            }
        };
        deleteHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleDeleteMessage(msg);
            }
        };

        init_goodslist();
        refreshTrolley();
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.settle) {
            trolleyItem target = findSelectedGoods();
            if (target != null) {
                if (target.isDeleted()) {
                    Toast.makeText(getActivity(), "该商品已下架", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), tradeActivity.class);
                    intent.putExtra("goods", target.getGoods());
                    intent.putExtra("amount", target.getNum());
                    startActivityForResult(intent, SETTLE);
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.noTarget), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == SETTLE) {
            listWrapper.setRefreshing(true);
            refreshTrolley();
        }
    }

    private void init_goodslist() {
        trolleyArray = new ArrayList<>();
        goodsAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return trolleyArray.size() == 0 ? 1: trolleyArray.size();
            }

            @Override
            public Object getItem(int position) {
                return trolleyArray.size() == 0 ? null: trolleyArray.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                if (trolleyArray.size() == 0) {
                    v = layoutInflater.inflate(R.layout.emptytrolley, null);
                } else {
                    if (convertView != null) {
                        if (convertView.getId() != R.id.empty)
                            v = convertView;
                        else
                            v = layoutInflater.inflate(R.layout.trolleyitem, null);
                    } else {
                        v = layoutInflater.inflate(R.layout.trolleyitem, null);
                    }
                    final trolleyItem item = trolleyArray.get(position);

                    if (item.isDeleted()) {
                        v.setBackgroundColor(0xffe8e8e8);
                    }

                    EditText amount = (EditText) v.findViewById(R.id.amount);
                    TextView total = (TextView) v.findViewById(R.id.totalPrice);
                    amount.addTextChangedListener(new myTextWatcher(item, amount, total));

                    myOnClickListener onClickListener = new myOnClickListener(amount, item, total);
                    v.findViewById(R.id.plus).setOnClickListener(onClickListener);
                    v.findViewById(R.id.minus).setOnClickListener(onClickListener);

                    ((TextView)v.findViewById(R.id.goodsname)).setText(item.isDeleted()?"已下架":item.getTitle());
                    ((TextView)v.findViewById(R.id.brief)).setText(item.getIntro());
                    ((TextView)v.findViewById(R.id.totalPrice)).setText(decimalFormat.format(item.getTotalPrice()));

                    String coverName = item.getCover();
                    ImageView holder = (ImageView)v.findViewById(R.id.goodspicture);
                    if (coverName != null) {
                        String path = imageManager.getImagePath(coverName, ImageManager.GOODS);
                        if (path != null) {
                            Picasso.with(getActivity()).load(new File(path)).resize(100,100).centerInside().into(holder);
                        } else {
                            imageManager.downloadImage(holder, coverName, ImageManager.GOODS);
                        }
                    }

                    v.findViewById(R.id.deleteItem).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog(item.getId());
                        }
                    });

                    ImageView checkBtn = (ImageView)v.findViewById(R.id.checkbtn);
                    checkBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                                    item.isSelected()?R.drawable.selected:R.drawable.unselected));
                    checkBtn.setOnClickListener(new myCheckbtnlistener(position));
                }
                return v;
            }
        };
        goodslist.setAdapter(goodsAdapter);
        goodslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), BrowseGoodsActivity.class);
                intent.putExtra("gid", trolleyArray.get(position).getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRefresh() {
        refreshTrolley();
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        try {
            Picasso.with(getActivity()).load(new File(path)).resize(100, 100).centerInside().into(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTrolleyMessage(Message msg) {
        try {
            if (listWrapper.isRefreshing())
                listWrapper.setRefreshing(false);
            if (msg.what == 0) {
                String res = msg.obj.toString();
                trolleyArray.clear();

                if (res.equals("-1")) {
                    Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                } else if (!res.equals("0")) {
                    JSONArray jsons = new JSONArray(res);
                    for (int i = 0 ; i != jsons.length() ; ++i) {
                        JSONObject json = jsons.getJSONObject(i);
                        goods g = new goods(json, true);
                        trolleyArray.add(new trolleyItem(g, json.getInt("status")));
                    }
                }
                goodsAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (Integer.valueOf(res) > 0) {
                    int gid = Integer.valueOf(res);
                    for (trolleyItem item: trolleyArray) {
                        if (item.getId() == gid) {
                            if (item.isSelected()) {    //防止出现商品删除后总价依旧为非零的情况
                                total.setText(decimalFormat.format(0.0));
                            }
                            trolleyArray.remove(item);
                            goodsAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                } else if (res.equals("0")) {
                    Toast.makeText(getActivity(), "商品不存在", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTrolley() {
        HttpUtil.HttpClientGET(HttpUtil.host+"trolley?action="+String.valueOf(GET)+
                "&uid="+getActivity().getSharedPreferences("account", Context.MODE_PRIVATE).getString("uid","0"), trolleyHandler);
    }

    private void deleteFromTrolley(int gid) {
        HttpUtil.HttpClientGET(HttpUtil.host+"trolley?action="+String.valueOf(DEL)+"&gid="+String.valueOf(gid)+
                "&uid="+getActivity().getSharedPreferences("account", Context.MODE_PRIVATE).getString("uid","0"), deleteHandler);
    }

    @Nullable
    private trolleyItem findSelectedGoods() {
        for (trolleyItem item: trolleyArray) {
            if (item.isSelected()) {
                return item;
            }
        }
        return null;
    }

    /**
     * 在check button和数量的状态发生改变时更新总价的显示
     */
    private void refreshTotal() {
        for (trolleyItem item : trolleyArray) {
            if (item.isSelected()) {
                total.setText(decimalFormat.format(item.getTotalPrice()));
                return;
            }
        }
        total.setText("¥0.00");
    }

    private class myOnClickListener implements View.OnClickListener  {

        private EditText amount;
        private TextView total;
        private trolleyItem item;

        public myOnClickListener(EditText amount, trolleyItem item, TextView total) {
            this.amount = amount;
            this.item = item;
            this.total = total;
        }

        public void onClick(View v) {
            int num = Integer.valueOf(amount.getText().toString());
            switch (v.getId()) {
                case R.id.minus:
                    if (num > 0) {
                        amount.setText(String.valueOf(num - 1));
                        item.setNum(num - 1);
                        total.setText(decimalFormat.format(item.getTotalPrice()));
                    }
                    break;
                case R.id.plus:
                    if (num < 999) {
                        amount.setText(String.valueOf(num + 1));
                        item.setNum(num + 1);
                        total.setText(decimalFormat.format(item.getTotalPrice()));
                    }
                    break;
            }
            refreshTotal();
        }
    }

    private class myTextWatcher implements TextWatcher {
        private trolleyItem item;
        private EditText obj;
        private TextView total;

        public myTextWatcher(trolleyItem item, EditText obj, TextView total) {
            this.item = item;
            this.obj = obj;
            this.total = total;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String num = s.toString();
            int n = num.length()!=0?Integer.valueOf(num):0;
            if (n == 0 && num.length() != 1) {
                obj.setText("0");
            }
            else if (n != 0 && (int)Math.log10(n) + 1 != num.length()) {
                obj.setText(String.valueOf(n));
            }
            item.setNum(n);
            total.setText(decimalFormat.format(item.getTotalPrice()));
            refreshTotal();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }

    /**
     * used only for the check button, because the view v will be cast to a ImageView
     */
    private class myCheckbtnlistener implements View.OnClickListener {

        private int position;

        public myCheckbtnlistener(int p) {
            position = p;
        }

        public void onClick(View v) {
            boolean selected = trolleyArray.get(position).isSelected();
            if (!selected) {
                for (int i = 0 ; i != trolleyArray.size() ; ++i) {
                    if (i != position) {
                        trolleyArray.get(i).setSelected(false);
                    }
                }
            }
            trolleyArray.get(position).setSelected(!selected);
            refreshTotal();
            goodsAdapter.notifyDataSetChanged();
        }
    }

    private class trolleyItem {
        private goods g;
        private int num;
        private int status; //状态 0 正常 1 已下架
        private float totalPrice;
        private boolean selected;

        public trolleyItem(goods g, int s) {
            this.g = g;
            num = 1;
            totalPrice = g.getRent();
            status = s;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean status) {
            selected = status;
        }

        public float getTotalPrice() {
            return totalPrice;
        }

        public goods getGoods() { return g; }

        public void setNum(int newNum) {
            num = newNum;
            totalPrice = g.getRent()*num;
        }

        public int getNum() { return num; }

        public String getCover() {
            return g.getCoverName();
        }

        public String getTitle() {
            String longTitle = g.getTitle();
            if (longTitle.length() > 5) {
                return longTitle.substring(0,5)+"...";
            } else {
                return longTitle;
            }
        }

        public String getIntro() {
            String longDesc = g.getDescription();
            if (longDesc.length() > 19) {
                return longDesc.substring(0,19)+"...";
            } else {
                return longDesc;
            }
        }

        public int getId() {
            return g.getGid();
        }

        public boolean isDeleted() { return status == 1; }
    }

    private void dialog(final int toDel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("确认要删除该商品吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFromTrolley(toDel);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
