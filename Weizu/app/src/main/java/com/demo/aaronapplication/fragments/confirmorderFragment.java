package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.activity.SelectAddressActivity;
import com.demo.aaronapplication.weizu.Address;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.demo.aaronapplication.weizu.goods;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Aaron on 2016/3/27.
 */
public class confirmorderFragment extends Fragment implements View.OnClickListener, ImageManager.onFinishLoadListener {

    private TextView address, title, desc, price, deposit, sum, unit;
    private EditText amount, renttime, note;
    private ImageView goodspic, checkbtn_rcv_face, checkbtn_rcv_exp, checkbtn_rtn_face, checkbtn_rtn_exp;
    private View deal, select_address, rcv_face, rcv_exp, rtn_face, rtn_exp;

    private ImageManager imageManager;

    private static final int RCV = 0, RTN = 1;
    private static final int FACETOFACE = 0, EXPRESS = 1;
    private int rcvway, rtnway;

    private int num; //商品数量,只在初始化时用到
    private int defaultTime = 1; //租赁时长默认为1
    private goods target;
    private Address selected_Address;

    private static final DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");


    public interface onConfirmOrderListener {
        void onConfirmOrder(String note, Address address, int amount, int renttime, int rcv, int rtn);
    }

    private onConfirmOrderListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (onConfirmOrderListener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        target = (goods)(getArguments().getSerializable("goods"));
        num = getArguments().getInt("amount");
        Log.e("num",String.valueOf(num));
        View v = inflater.inflate(R.layout.confirm_order, container, false);
        initView(v);

        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rcv_face:
                if (rcvway != FACETOFACE) {
                    rcvway = FACETOFACE;
                    checkbtn_switch(RCV, R.id.checkbtn_rcv_face);
                }
                break;
            case R.id.rcv_exp:
                if (rcvway != EXPRESS) {
                    rcvway = EXPRESS;
                    checkbtn_switch(RCV, R.id.checkbtn_rcv_exp);
                }
                break;
            case R.id.rtn_face:
                if (rtnway != FACETOFACE) {
                    rtnway = FACETOFACE;
                    checkbtn_switch(RTN, R.id.checkbtn_rtn_face);
                }
                break;
            case R.id.rtn_exp:
                if (rtnway != EXPRESS){
                    rtnway = EXPRESS;
                    checkbtn_switch(RTN, R.id.checkbtn_rtn_exp);
                }
                break;
            case R.id.confirm_order:
                if (amount.getText().toString().equals("0")) {
                    Toast.makeText(getActivity(), "租借时间不可为0", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (address.getText().toString().equals("请选择收货地址")) {
                    Toast.makeText(getActivity(), "请选择收货地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                listener.onConfirmOrder(note.getText().toString(), selected_Address,
                        Integer.valueOf(amount.getText().toString()),Integer.valueOf(renttime.getText().toString()), rcvway, rtnway);
                break;
            case R.id.select_address:
                Intent intent = new Intent(getActivity(), SelectAddressActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == 0) {
            if (data.getBooleanExtra("selected",false)) {
                Address addr = (Address)data.getSerializableExtra("address");
                setAddress(addr);
            }
        }
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        if (holder != null) {
            int side = UIUtil.dp2px(getActivity(), 60);
            Picasso.with(getActivity()).load(new File(path)).resize(side, side).centerInside().into(holder);
        }
    }

    private void initView(View v) {
        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);

        address = (TextView)v.findViewById(R.id.address);
        title = (TextView)v.findViewById(R.id.title);
        title.setText(target.getTitle());
        desc = (TextView)v.findViewById(R.id.desc);
        desc.setText(target.getShortDescription());
        price = (TextView)v.findViewById(R.id.price);
        price.setText(target.getFormatPrice());
        deposit = (TextView)v.findViewById(R.id.deposit);
        deposit.setText(decimalFormat.format(target.getDeposit()));
        unit = (TextView)v.findViewById(R.id.unit);
        unit.setText(getString(target.getPeriod() == goods.DAY ? R.string.day : R.string.hour));
        sum = (TextView)v.findViewById(R.id.total);
        sum.setText(decimalFormat.format(target.sum(num,defaultTime,false)));

        amount = (EditText) v.findViewById(R.id.amount);
        amount.setText(String.valueOf(num));
        amount.addTextChangedListener(new myTextWatcher(amount, sum, myTextWatcher.AMOUNT));
        renttime = (EditText) v.findViewById(R.id.renttime);
        renttime.setText(String.valueOf(defaultTime));
        renttime.addTextChangedListener(new myTextWatcher(renttime, sum, myTextWatcher.RENTTIME));

        goodspic = (ImageView)v.findViewById(R.id.goods_pic);
        checkbtn_rcv_exp = (ImageView)v.findViewById(R.id.checkbtn_rcv_exp);
        checkbtn_rcv_face = (ImageView)v.findViewById(R.id.checkbtn_rcv_face);
        checkbtn_rtn_face = (ImageView)v.findViewById(R.id.checkbtn_rtn_face);
        checkbtn_rtn_exp = (ImageView)v.findViewById(R.id.checkbtn_rtn_exp);

        rcv_face = v.findViewById(R.id.rcv_face);
        rcv_face.setOnClickListener(this);
        rcv_exp = v.findViewById(R.id.rcv_exp);
        rcv_exp.setOnClickListener(this);
        rtn_face = v.findViewById(R.id.rtn_face);
        rtn_face.setOnClickListener(this);
        rtn_exp = v.findViewById(R.id.rtn_exp);
        rtn_exp.setOnClickListener(this);
        note = (EditText) v.findViewById(R.id.note);
        deal = v.findViewById(R.id.confirm_order);
        deal.setOnClickListener(this);
        select_address = v.findViewById(R.id.select_address);
        select_address.setOnClickListener(this);

        myOnClickListener l = new myOnClickListener(sum);
        v.findViewById(R.id.amount_plus).setOnClickListener(l);
        v.findViewById(R.id.amount_minus).setOnClickListener(l);
        myOnClickListener ll = new myOnClickListener(sum);
        v.findViewById(R.id.renttime_minus).setOnClickListener(ll);
        v.findViewById(R.id.renttime_plus).setOnClickListener(ll);


        if (target.hasCover()) {
            String path = imageManager.getImagePath(target.getCoverName(), ImageManager.GOODS);
            if (path != null) {
                int side = UIUtil.dp2px(getActivity(),60);
                Picasso.with(getActivity()).load(new File(path)).resize(side,side).centerInside().into(goodspic);
            } else {
                imageManager.downloadImage(goodspic, target.getCoverName(), ImageManager.GOODS);
            }
        }

        rcvway = rtnway = FACETOFACE;
    }

    private void checkbtn_switch(int dir, int id) {
        if (dir == RCV) {
            switch (id) {
                case R.id.checkbtn_rcv_face:
                    checkbtn_rcv_exp.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.unselected));
                    checkbtn_rcv_face.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.selected));
                    sum.setText(decimalFormat.format(target.sum(Integer.valueOf(amount.getText().toString()),Integer.valueOf(renttime.getText().toString()), false)));
                    break;
                case R.id.checkbtn_rcv_exp:
                    checkbtn_rcv_face.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.unselected));
                    checkbtn_rcv_exp.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.selected));
                    sum.setText(decimalFormat.format(target.sum(Integer.valueOf(amount.getText().toString()),Integer.valueOf(renttime.getText().toString()), true)));
                    break;
            }
        } else {
            switch (id) {
                case R.id.checkbtn_rtn_face:
                    checkbtn_rtn_exp.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.unselected));
                    checkbtn_rtn_face.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.selected));
                    break;
                case R.id.checkbtn_rtn_exp:
                    checkbtn_rtn_face.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.unselected));
                    checkbtn_rtn_exp.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.selected));
                    break;
            }
        }
    }

    private void setAddress(Address addr) {
        selected_Address = addr;
        address.setText(addr.getShortAddress());
    }

    /**
     * amount 和 renttime 的加减公用的Onclicklistener
     */
    private class myOnClickListener implements View.OnClickListener  {

        private TextView total;

        public myOnClickListener(TextView total) {
            this.total = total;
        }

        public void onClick(View v) {
            int num = Integer.valueOf(amount.getText().toString());
            int time = Integer.valueOf(renttime.getText().toString());
            switch (v.getId()) {
                case R.id.amount_minus:
                    if (num > 1) {
                        amount.setText(String.valueOf(num - 1));
                        total.setText(decimalFormat.format(target.sum(num - 1,time, rcvway == EXPRESS)));
                    }
                    break;
                case R.id.renttime_minus:
                    if (time > 1) {
                        renttime.setText(String.valueOf(time - 1));
                        total.setText(decimalFormat.format(target.sum(num, time - 1, rcvway == EXPRESS)));
                    }
                    break;
                case R.id.amount_plus:
                    if (num < 999) {
                        amount.setText(String.valueOf(num + 1));
                        total.setText(decimalFormat.format(target.sum(num + 1,time, rcvway == EXPRESS)));
                    }
                    break;
                case R.id.renttime_plus:
                    if (time < 999) {
                        renttime.setText(String.valueOf(time + 1));
                        total.setText(decimalFormat.format(target.sum(num, time + 1, rcvway == EXPRESS)));
                    }
                    break;
            }
        }
    }

    /**
     * amount 和 renttime 的输入框公用的textwatcher, 用type来区分
     */
    private class myTextWatcher implements TextWatcher {
        public static final int AMOUNT = 0, RENTTIME = 1;
        private int type;
        private EditText obj;
        private TextView total;

        public myTextWatcher(EditText obj, TextView total, int type) {
            this.obj = obj;
            this.total = total;
            this.type = type;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String num = s.toString();
            int n = num.length() != 0 ? Integer.valueOf(num):0; //获取编辑后的实际值，若字段被删净，则视为0, 删除数字最前面多余的0
            if (n == 0 && num.length() != 1) {
                obj.setText("0");
            }
            else if (n != 0 && (int)Math.log10(n) + 1 != num.length()) {
                obj.setText(String.valueOf(n));
            }
            if (type == AMOUNT) {
                total.setText(decimalFormat.format(target.sum(n, Integer.valueOf(renttime.getText().toString()), rcvway == EXPRESS)));
            } else {
                total.setText(decimalFormat.format(target.sum(Integer.valueOf(amount.getText().toString()), n, rcvway == EXPRESS)));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }
}
