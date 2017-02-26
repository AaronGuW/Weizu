package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.demo.aaronapplication.weizu.Address;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.demo.aaronapplication.weizu.goods;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;

public class confirmpaymentFragment extends Fragment implements ImageManager.onFinishLoadListener {

    private TextView title, desc, address, amount, time, rcv, rtn, note, total;
    private ImageView goodspic;
    private View pay;
    private goods target;
    private Address addr;

    private PopupWindow waiting;

    private int oid; //提交订单生成的订单号，用于验证同步通知信息

    private ImageManager imageManager;

    private Handler orderHandler, payHandler, statusHandler;

    private static final DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");
    private static final int ORDERINFO = 100, PAYRESULT = 200;

    public interface onConfirmpaymentListener {
        void onConfirmpayment();
    }

    private onConfirmpaymentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (onConfirmpaymentListener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        orderHandler.removeCallbacksAndMessages(null);
        payHandler.removeCallbacksAndMessages(null);
        statusHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        target = (goods)(getArguments().getSerializable("goods"));
        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);
        orderHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleOrderMessage(msg);
            }
        };
        payHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handlePayMessage(msg);
            }
        };
        statusHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleStatusMessage(msg);
            }
        };

        View v = inflater.inflate(R.layout.confirm_payment, container, false);
        init_view(v);

        return v;
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        if (holder != null) {
            int side = UIUtil.dp2px(getActivity(), 60);
            Picasso.with(getActivity()).load(new File(path)).resize(side, side).centerInside().into(holder);
        }
    }

    private void init_view(View v) {
        title = (TextView)v.findViewById(R.id.title);
        desc = (TextView)v.findViewById(R.id.desc);
        goodspic = (ImageView)v.findViewById(R.id.goods_pic);
        address = (TextView)v.findViewById(R.id.address);
        amount = (TextView)v.findViewById(R.id.amount);
        time = (TextView)v.findViewById(R.id.time);
        rcv = (TextView)v.findViewById(R.id.rcv_way);
        rtn = (TextView)v.findViewById(R.id.rtn_way);
        note = (TextView)v.findViewById(R.id.note);
        total = (TextView)v.findViewById(R.id.total);
        pay = v.findViewById(R.id.pay);

        Bundle params = getArguments();
        addr = (Address) params.getSerializable("address");

        title.setText(target.getTitle());
        desc.setText(target.getShortDescription());
        address.setText(addr.getShortAddress());
        amount.setText(String.valueOf(params.getInt("amount")));
        time.setText(String.valueOf(params.getInt("time")) + " " + (getString(target.getPeriod() == goods.HOUR ? R.string.hour:R.string.day)));
        rcv.setText(params.getInt("rcv") == 0 ? "面交":"快递");
        rtn.setText(params.getInt("rtn") == 0 ? "面交":"快递");
        note.setText(params.getString("note").length() != 0 ? params.getString("note"):"无");
        total.setText(decimalFormat.format(target.sum(params.getInt("amount"),params.getInt("time"),params.getInt("rcv") == 1)));
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay.setClickable(false);
                show_processing(ORDERINFO);
                uploadOrder();
            }
        });

        String path = imageManager.getImagePath(target.getCoverName(), ImageManager.GOODS);
        if (path != null) {
            int side = UIUtil.dp2px(getActivity(), 60);
            Picasso.with(getActivity()).load(new File(path)).resize(side,side).centerInside().into(goodspic);
        } else {
            imageManager.downloadImage(goodspic, target.getCoverName(), ImageManager.GOODS);
        }
    }

    private void uploadOrder() {
        try {
            Bundle params = getArguments();
            JSONObject json = new JSONObject();
            json.put("gid", target.getGid());
            json.put("leaser", target.getLeaser());
            Log.e("leaser", target.getLeaser()+"");
            json.put("leasee", getActivity().getSharedPreferences("account", Context.MODE_PRIVATE).getString("uid","0"));
            json.put("rcv", params.getInt("rcv"));
            json.put("rtn", params.getInt("rtn"));
            json.put("time", params.getInt("time"));
            json.put("amount", params.getInt("amount"));
            json.put("time", params.getInt("time"));
            json.put("note", Base64.encodeToString(params.getString("note").getBytes(),Base64.DEFAULT));
            json.put("aid", addr.getAid());

            HttpUtil.JSONHttpPOST(HttpUtil.host + "order?action=new", json, orderHandler);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    private void confirmPayment(String oid) {
        HttpUtil.HttpClientGET(HttpUtil.host+"order?action=confirm&oid="+oid, statusHandler);
    }

    private void handleOrderMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                JSONObject json = new JSONObject(res);
                oid = json.getInt("oid");
                final String orderInfo = json.getString("orderInfo");
                Runnable payRunnable = new Runnable() {

                    @Override
                    public void run() {
                        PayTask alipay = new PayTask(getActivity());
                        Map<String, String> result = alipay.payV2(orderInfo, true);
                        Log.i("msp", result.toString());

                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = result;
                        payHandler.sendMessage(msg);
                    }
                };
                if (waiting.isShowing())
                    waiting.dismiss();
                Thread payThread = new Thread(payRunnable);
                payThread.start();
            } else {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePayMessage(Message msg) {
        Map<String, String> res = (Map<String, String>) msg.obj;
        Log.e("syn_resultStatus", res.get("resultStatus"));

        String status = res.get("resultStatus");
        if (status.compareTo("9000") == 0 || status.compareTo("8000") == 0) {
            confirmPayment(String.valueOf(oid));
            show_processing(PAYRESULT);
        } else {
            listener.onConfirmpayment();
            Toast.makeText(getActivity(), "请及时支付，未支付的订单将在当日24:00关闭",Toast.LENGTH_SHORT).show();
        }
    }

    private void handleStatusMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (waiting.isShowing()) {
                    waiting.dismiss();
                }
                if (res.compareTo("TRADE_SUCCESS") != 0) {
                    Toast.makeText(getActivity(), "请在我的订单中查询支付结果", Toast.LENGTH_SHORT).show();
                }
                listener.onConfirmpayment();
            } else {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show_processing(int what) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.loging, null);
        switch (what) {
            case ORDERINFO:
                ((TextView)v.findViewById(R.id.hint)).setText(getString(R.string.waiting_orderInfo));
                break;
            case PAYRESULT:
                ((TextView)v.findViewById(R.id.hint)).setText(getString(R.string.waiting_payResult));
                break;
        }
        waiting = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        waiting.setFocusable(true);
        waiting.showAtLocation(getActivity().findViewById(R.id.root), Gravity.CENTER|Gravity.CENTER, 0,0);
    }
}
