package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.Order;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.SelectionDialog;
import com.demo.aaronapplication.weizu.UIUtil;
import com.demo.aaronapplication.weizu.goods;
import com.demo.aaronapplication.weizu.kdniao;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import io.rong.imkit.RongIM;

/**
 * Created by Aaron on 2016/3/27.
 */
public class OrderProcessActivity extends Activity implements View.OnClickListener, ImageManager.onFinishLoadListener {

    //6 descriptions of 6 stage, layout id are set static for convenience
    private TextView[] stage_descriptions;
    private static final int[] desc_id = {R.id.stage1_desc, R.id.stage2_desc, R.id.stage3_desc, R.id.stage4_desc, R.id.stage5_desc, R.id.stage6_desc};
    private static final int[][] finish_desc_id = { {R.string.lessor_stage1_finish, R.string.lessor_stage2_finish, R.string.lessor_stage3_finish, R.string.lessor_stage4_finish, R.string.lessor_stage5_finish},
                                                          {R.string.lessee_stage1_finish, R.string.lessee_stage2_finish, R.string.lessee_stage3_finish, R.string.lessee_stage4_finish, R.string.lessee_stage5_finish} };
    private static final int[] stagedrawableid = {R.drawable.stage_1, R.drawable.stage_2, R.drawable.stage_3, R.drawable.stage_4, R.drawable.stage_5, R.drawable.stage_6};

    private static final int stars_id[] = new int[]{ R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5 };
    private static final int stars_press_id[] = new int[]{ R.id.left_1, R.id.right_1, R.id.left_2, R.id.right_2, R.id.left_3, R.id.right_3,
            R.id.left_4, R.id.right_4, R.id.left_5, R.id.right_5 };

    //button for check express process
    private TextView stage2_check_exp, stage4_check_exp;
    //button for confirm send out and sign for
    private View stage3_confirm_signfor, stage4_confirm_send;
    //button on the bottom-right corner, it always change in lessor's view
    private TextView function_btn;
    //button for closing the order, only show up when leaser has not confirm the order
    private TextView close_btn;
    //imageview showing the current stage of the order
    private ImageView stage;
    //the order this page shows
    private Order order;

    //the rate of leaser for leasee
    private int rate_leaser = 9;
    private Bitmap starFull, starHalf, starEmpty;

    private PopupWindow pop_ConfirmSignfor, pop_conConfirmSendout, waiting;
    private float screenwidth, screenheight;

    private Handler orderHandler;   //处理服务器返回的订单详情数据
    private Handler expIdentifyHandler; //处理快递鸟单号识别的结果
    private Handler orderUpdateHandler; //更新订单状态的Handler
    private Handler orderInfoHandler;   //从服务器获取orderInfo的handler
    private Handler payHandler; //支付宝同步通知处理
    private Handler confirmHandler; //向服务器确认支付宝异步通知的Handler

    private ImageManager imageManager;


    //输入物流单号时才会用到
    private ArrayList<String> possibleComCode, possibleComName; //输入单号后调用快递鸟api进行识别后的结果列表
    private String selectedComCode, selectedComName;    //用户从以上列表中选出的结果
    private String expNo;
    private AlertDialog inputExpNo;
    private SelectionDialog selectExpCom;
    private EditText inputCode; //单号输入框
    private TextView confirmExpNo, companyName;  //确认,快递公司名称
    private View progress;  //输入物流单号的loading栏
    private View comBar;    //快递公司选择结果栏


    private static final int ORDERINFO = 100, PAYRESULT = 200, UPDATE = 300, REFUND = 400, CLOSE = 500;
    private static final int COMMENT = 123; //MakeCommentActivity的requestCode

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ongoing_order);

        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);

        orderHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleLoadMessage(msg);
            }
        };
        expIdentifyHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleExpIdentifyMessage(msg);
            }
        };
        orderUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleUpdateMessage(msg);
            }
        };
        orderInfoHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleOrderInfoMessage(msg);
            }
        };
        payHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handlePayMessage(msg);
            }
        };
        confirmHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleConfirmMessage(msg);
            }
        };

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.send_msg).setOnClickListener(this);

        stage_descriptions = new TextView[6];

        for (int i = 0 ; i != 6 ; ++i) {
            stage_descriptions[i] = (TextView)findViewById(desc_id[i]);
        }
        stage2_check_exp = (TextView)findViewById(R.id.stage2_check_exp);
        stage2_check_exp.setOnClickListener(this);
        stage4_check_exp = (TextView)findViewById(R.id.stage4_check_exp);
        stage4_check_exp.setOnClickListener(this);
        stage3_confirm_signfor = findViewById(R.id.stage3_signfor);
        stage3_confirm_signfor.setOnClickListener(this);
        stage4_confirm_send = findViewById(R.id.stage4_sendout);
        stage4_confirm_send.setOnClickListener(this);
        function_btn = (TextView)findViewById(R.id.refund_btn);
        function_btn.setOnClickListener(this);
        close_btn = (TextView)findViewById(R.id.close_btn);
        close_btn.setOnClickListener(this);
        findViewById(R.id.goods_panel).setOnClickListener(this);

        stage = (ImageView)findViewById(R.id.stage);

        Intent intent = getIntent();
        loadOrder(intent.getIntExtra("oid", 0));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case COMMENT:
                function_btn.setText(getString(R.string.complain));
                order.setLeasee_cid(Integer.valueOf(data.getStringExtra("res")));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        confirmHandler.removeCallbacksAndMessages(null);
        expIdentifyHandler.removeCallbacksAndMessages(null);
        orderHandler.removeCallbacksAndMessages(null);
        orderInfoHandler.removeCallbacksAndMessages(null);
        orderUpdateHandler.removeCallbacksAndMessages(null);
        payHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        if (holder != null) {
            Picasso.with(this).load(new File(path)).resize(128, 128).centerInside().into(holder);
        }
    }

    public void loadViews() {
        ((TextView)findViewById(R.id.side)).setText(getString(order.getSide() == Order.LEASER ? R.string.leasee:R.string.leaser));
        ((TextView)findViewById(R.id.name)).setText(order.getSide() == Order.LEASER ? order.getLeaseeName() : order.getLeaserName());
        findViewById(R.id.viewAddress).setOnClickListener(this);
        ((TextView)findViewById(R.id.title)).setText(order.getShortTitle());
        ((TextView)findViewById(R.id.desc)).setText(order.getShortIntro());
        ((TextView)findViewById(R.id.amount)).setText(String.valueOf(order.getAmount()));
        ((TextView)findViewById(R.id.renttime)).setText(String.valueOf(order.getRenttime())+ getString(order.getPeriod() == goods.HOUR ? R.string.hour : R.string.day));
        ((TextView)findViewById(R.id.price)).setText(order.getFormatRent() +"/"+ getString(order.getPeriod() == goods.HOUR ? R.string.hour : R.string.day));
        ((TextView)findViewById(R.id.deposit)).setText(order.getFormatDeposit());
        ((TextView)findViewById(R.id.way_rcv)).setText(getString(order.getWay_rcv() == 0 ? R.string.facetoface : R.string.express));
        ((TextView)findViewById(R.id.way_rtn)).setText(getString(order.getWay_rtn() == 0 ? R.string.facetoface : R.string.express));


        String path = imageManager.getImagePath(order.getCoverName(), ImageManager.THUMBNAIL);
        ImageView goods_pic = (ImageView)findViewById(R.id.goods_pic);
        if (path != null) {
            Picasso.with(this).load(new File(path)).resize(128,128).centerInside().into(goods_pic);
        } else {
            imageManager.downloadImage(goods_pic, order.getCoverName(), ImageManager.THUMBNAIL);
        }
        setProcess();
    }

    public void setProcess() {
        int side = order.getSide(), stage_ord = order.getStage();
        setDesc(stage_ord-1, side);
        Picasso.with(this).load(stagedrawableid[stage_ord-1]).into(stage);
        int status = order.getStatus();
        if (status == 0) {  //等待买家付款
            if (side == Order.LEASEE) {
                function_btn.setText("前往付款");
            } else {
                function_btn.setVisibility(View.GONE);
                close_btn.setVisibility(View.VISIBLE);
            }
        } else if (status == 2) {
            function_btn.setText("订单已关闭");
            function_btn.setEnabled(false);
            function_btn.setBackgroundColor(0xff888888);
        } else {
            switch (order.getStage()) {
                case 1:
                    if (side == Order.LEASER) {
                        function_btn.setText(getResources().getText(R.string.confirmorder));
                        close_btn.setVisibility(View.VISIBLE);
                    }
                    break;
                case 2:
                    if (side == Order.LEASER) {
                        if (order.getWay_rcv() == Order.EXPRESS)
                            function_btn.setText(getResources().getText(R.string.input_exp_id));
                        else
                            function_btn.setText(getResources().getText(R.string.confirmgiveout));
                    } else {
                        function_btn.setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    if (side == Order.LEASER) {
                        function_btn.setVisibility(View.GONE);
                    } else {
                        stage3_confirm_signfor.setVisibility(View.VISIBLE);
                        function_btn.setVisibility(View.GONE);
                    }
                    if (order.getWay_rcv() == Order.EXPRESS)
                        stage2_check_exp.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    if (side == Order.LEASER) {
                        function_btn.setVisibility(View.GONE);
                    } else {
                        stage4_confirm_send.setVisibility(View.VISIBLE);
                        function_btn.setVisibility(View.GONE);
                    }
                    if (order.getWay_rcv() == Order.EXPRESS)
                        stage2_check_exp.setVisibility(View.VISIBLE);
                    break;
                case 5:
                    if (side == Order.LEASER) {
                        function_btn.setText(getResources().getText(R.string.confirmsignfor));
                    } else {
                        function_btn.setVisibility(View.GONE);
                    }
                    if (order.getWay_rcv() == Order.EXPRESS)
                        stage2_check_exp.setVisibility(View.VISIBLE);
                    if (order.getWay_rtn() == Order.EXPRESS)
                        stage4_check_exp.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    function_btn.setVisibility(View.VISIBLE);
                    if (side == Order.LEASEE) {
                        if (order.getLeasee_cid() == -1) {
                            function_btn.setText(getString(R.string.go_comment));
                        } else {
                            function_btn.setText(getString(R.string.complain));
                        }
                    } else {
                        if (order.getLeaser_rate() == -1) {
                            function_btn.setText(getString(R.string.go_comment));
                        } else {
                            function_btn.setText(getString(R.string.complain));
                        }
                    }
                    if (order.getWay_rcv() == Order.EXPRESS)
                        stage2_check_exp.setVisibility(View.VISIBLE);
                    if (order.getWay_rtn() == Order.EXPRESS)
                        stage4_check_exp.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.goods_panel:
                if (order != null) {
                    Intent browseIntent = new Intent(this, BrowseGoodsActivity.class);
                    browseIntent.putExtra("gid", order.getGid());
                    startActivity(browseIntent);
                }
                break;
            case R.id.viewAddress:
                viewAddress();
                break;
            case R.id.stage3_signfor:
                confirmSignFor(3);
                break;
            case R.id.stage4_sendout:
                if (order.getWay_rtn() == Order.EXPRESS)
                    inputExpressCode();
                else {
                    JSONObject param4 = new JSONObject();
                    try {
                        param4.put("oid", order.getOid());
                        param4.put("obj", order.getLeaserId());
                        param4.put("currentStage", 4);
                        param4.put("expCode", "/");
                        param4.put("expComCode", "/");
                        param4.put("expComName", "/");
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }

                    updateOrder(param4);
                }
                break;
            case R.id.stage2_check_exp:
                Intent intent1 = new Intent(OrderProcessActivity.this, CheckExpressActivity.class);
                intent1.putExtra("code",order.getExp_com_code_leaser());
                intent1.putExtra("no", order.getExp_id_leaser());
                intent1.putExtra("company",order.getExp_com_name_leaser());
                startActivity(intent1);
                break;
            case R.id.stage4_check_exp:
                Intent intent2 = new Intent(OrderProcessActivity.this, CheckExpressActivity.class);
                intent2.putExtra("code",order.getExp_com_code_leasee());
                intent2.putExtra("no",order.getExp_id_leasee());
                intent2.putExtra("company",order.getExp_com_name_leasee());
                startActivity(intent2);
                break;
            case R.id.refund_btn:
                if (order.getSide() == Order.LEASER) {
                    switch (order.getStage()) {
                        case 1:
                            if (getSharedPreferences("account", MODE_PRIVATE).getString("alipay","NULL").compareTo("NULL") == 0) {
                                Toast.makeText(OrderProcessActivity.this, "请先绑定您的支付宝账号", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            JSONObject param = new JSONObject();
                            try {
                                param.put("oid", order.getOid());
                                param.put("obj", order.getLeaseeId());
                                param.put("currentStage", 1);
                            } catch (JSONException je) {
                                je.printStackTrace();
                            }
                            updateOrder(param);
                            break;
                        case 2:
                            if (order.getWay_rcv() == Order.EXPRESS)
                                inputExpressCode();
                            else {
                                JSONObject param2 = new JSONObject();
                                try {
                                    param2.put("oid", order.getOid());
                                    param2.put("obj", order.getLeaseeId());
                                    param2.put("currentStage", 2);
                                    param2.put("expCode", "/");
                                    param2.put("expComCode", "/");
                                    param2.put("expComName", "/");
                                } catch (JSONException je) {
                                    je.printStackTrace();
                                }
                                updateOrder(param2);
                            }
                            break;
                        case 5:
                            confirmSignFor(5);
                            break;
                        case 6:
                            if (order.getLeaser_rate() == -1) {
                                rateLeasee();
                            }
                            break;
                    }
                } else {
                    if (order.getStatus() == 0) {
                        //买家未完成付款,
                        show_processing(ORDERINFO);
                        HttpUtil.HttpClientGET(HttpUtil.host+"order?action=orderInfo&oid="+String.valueOf(order.getOid()), orderInfoHandler);
                    } else {
                        if (order.getStage() == 6) {
                            if (order.getLeasee_cid() == -1) {
                                Intent intent = new Intent(OrderProcessActivity.this, MakeCommentActivity.class);
                                intent.putExtra("order", order);
                                startActivityForResult(intent, COMMENT);
                            }
                        } else if (order.getStage() == 1) {
                            confirmAction(REFUND);
                        }
                    }
                }
                break;
            case R.id.close_btn:
                if (order.getSide() == Order.LEASER && (order.getStatus() == 0 || (order.getStatus() == 1 && order.getStage() == 1))) {
                    confirmAction(CLOSE);
                } else {
                    Log.e("OrderProcessActivity","close button shows up under wrong circumstance");
                }
                break;
            case R.id.send_msg:
                if (RongIM.getInstance() != null) {
                    RongIM.getInstance().startPrivateChat(this,
                            String.valueOf(order.getSide() == Order.LEASER?order.getLeaseeId():order.getLeaserId()),
                            order.getSide() == Order.LEASER?order.getLeaseeName():order.getLeaserName());
                }
                break;
            //输入物流单号的弹出窗中的cancel
            case R.id.cancelInput:
                inputExpNo.dismiss();
                break;
            case R.id.confirmExpNo:
                inputExpNo.dismiss();

                JSONObject param = new JSONObject();
                try {
                    param.put("oid", order.getOid());
                    param.put("obj", order.getStage() == 2 ? order.getLeaseeId():order.getLeaserId());
                    param.put("currentStage", order.getStage());
                    param.put("expCode", expNo);
                    param.put("expComCode", selectedComCode);
                    param.put("expComName", Base64.encodeToString(selectedComName.getBytes(), Base64.DEFAULT));
                } catch (JSONException je) {
                    je.printStackTrace();
                }

                updateOrder(param);
                break;
            case R.id.identify:
                if (inputCode.getText().toString().length() > 0) {
                    progress.setVisibility(View.VISIBLE);
                    expNo = inputCode.getText().toString();
                    kdniao.recognizeExpNo(expNo, expIdentifyHandler);
                } else {
                    Toast.makeText(OrderProcessActivity.this, "单号不可为空", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setDesc(int stage, int side) {
        for (int i = 0 ; i != stage ; ++i) {
            stage_descriptions[i].setText(getText(finish_desc_id[side][i]));
            stage_descriptions[i].setTextColor(0xffe8e8e8);
        }
        stage_descriptions[stage].setTextColor(0xffe8591a);
    }

    private void reset_page() {
        stage2_check_exp.setVisibility(View.INVISIBLE);
        stage4_check_exp.setVisibility(View.INVISIBLE);
        stage3_confirm_signfor.setVisibility(View.INVISIBLE);
        stage4_confirm_send.setVisibility(View.INVISIBLE);
        function_btn.setText(getString(R.string.refund));
        close_btn.setVisibility(View.GONE);
    }

    private void viewAddress() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View content = inflater.inflate(R.layout.dialog_address, null);
        ((TextView)content.findViewById(R.id.recipient)).setText(order.getRecipient());
        ((TextView)content.findViewById(R.id.phone)).setText(order.getPhone());
        ((TextView)content.findViewById(R.id.address)).setText(order.getAddress());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("收货信息").setView(content);
        builder.create().show();
    }

    private void confirmSignFor(int stage) {
        final int currentStage = stage;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setCancelable(false);
        builder.setMessage("确认签收？");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JSONObject param = new JSONObject();
                try {
                    param.put("oid", order.getOid());
                    param.put("obj", currentStage == 3?order.getLeaserId():order.getLeaseeId());
                    param.put("currentStage", currentStage);
                    if (currentStage == 5) {
                        param.put("gid", order.getGid());
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
                show_processing(UPDATE);
                HttpUtil.JSONHttpPOST(HttpUtil.host+"order?action=update", param, orderUpdateHandler);
            }
        });
        inputExpNo = builder.create();
        inputExpNo.setCanceledOnTouchOutside(false); //点击空白处不消失
        inputExpNo.show();
    }

    private void confirmAction(int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        switch (action) {
            case REFUND:
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        show_processing(REFUND);
                        HttpUtil.HttpClientGET(HttpUtil.host+"order?action=refund&" +
                                "oid="+String.valueOf(order.getOid())+"&leaser="+String.valueOf(order.getLeaserId()), orderUpdateHandler);
                    }
                });
                builder.setMessage("在出租方确认订单前您可以取消订单，交易将被关闭，确定要取消订单吗？");
                break;
            case CLOSE:
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        show_processing(CLOSE);
                        HttpUtil.HttpClientGET(HttpUtil.host+"order?action=close&" +
                                "oid="+String.valueOf(order.getOid())+"&leasee="+String.valueOf(order.getLeaseeId()), orderUpdateHandler);
                    }
                });
                builder.setMessage("确认要关闭订单吗，您可以联系承租方说明关闭理由。");
                break;
        }

        builder.create().show();
    }

    private void inputExpressCode() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View inputView = inflater.inflate(R.layout.expressinput,null);
        inputCode = (EditText)inputView.findViewById(R.id.expNo);
        confirmExpNo = (TextView) inputView.findViewById(R.id.confirmExpNo);
        confirmExpNo.setOnClickListener(this);
        confirmExpNo.setClickable(false);
        progress = inputView.findViewById(R.id.progress);
        comBar = inputView.findViewById(R.id.comBar);
        companyName = (TextView)inputView.findViewById(R.id.comName);

        inputView.findViewById(R.id.cancelInput).setOnClickListener(this);
        inputView.findViewById(R.id.identify).setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入物流单号").setView(inputView);
        inputExpNo = builder.create();
        inputExpNo.setCanceledOnTouchOutside(false); //点击空白处不消失
        inputExpNo.show();
    }

    private void selectExpCom(ArrayList<String> options) {
        selectExpCom = new SelectionDialog(this, options, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedComName = selectExpCom.getSelected();
                if (selectedComName != null) {
                    Log.e("selected index", String.valueOf(selectedComName));
                    selectedComCode = possibleComCode.get(possibleComName.indexOf(selectedComName));
                    selectExpCom.dismiss();
                    comBar.setVisibility(View.VISIBLE);
                    companyName.setText(selectedComName);
                    confirmExpNo.setClickable(true);
                    confirmExpNo.setTextColor(Color.rgb(0,0,0));
                } else {
                    Toast.makeText(OrderProcessActivity.this, "请选择", Toast.LENGTH_SHORT).show();
                }
            }
        });
        selectExpCom.setTitle("请选择快递公司");
        selectExpCom.show();
    }

    private void rateLeasee() {
        starEmpty = BitmapFactory.decodeResource(getResources(), R.drawable.star_empty);
        starHalf = BitmapFactory.decodeResource(getResources(), R.drawable.star_half);
        starFull = BitmapFactory.decodeResource(getResources(), R.drawable.star_full);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.ratepanel, null);
        final ImageView[] stars = new ImageView[5];
        for (int i = 0 ; i != 5 ; ++i) {
            stars[i] = (ImageView)v.findViewById(stars_id[i]);
        }
        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.left_1:
                        drawStars(stars, rate_leaser, 0);
                        rate_leaser = 0;
                        break;
                    case R.id.right_1:
                        drawStars(stars, rate_leaser, 1);
                        rate_leaser = 1;
                        break;
                    case R.id.left_2:
                        drawStars(stars, rate_leaser, 2);
                        rate_leaser = 2;
                        break;
                    case R.id.right_2:
                        drawStars(stars, rate_leaser, 3);
                        rate_leaser = 3;
                        break;
                    case R.id.left_3:
                        drawStars(stars, rate_leaser, 4);
                        rate_leaser = 4;
                        break;
                    case R.id.right_3:
                        drawStars(stars, rate_leaser, 5);
                        rate_leaser = 5;
                        break;
                    case R.id.left_4:
                        drawStars(stars, rate_leaser, 6);
                        rate_leaser = 6;
                        break;
                    case R.id.right_4:
                        drawStars(stars, rate_leaser, 7);
                        rate_leaser = 7;
                        break;
                    case R.id.left_5:
                        drawStars(stars, rate_leaser, 8);
                        rate_leaser = 8;
                        break;
                    case R.id.right_5:
                        drawStars(stars, rate_leaser, 9);
                        rate_leaser = 9;
                        break;
                }
            }
        };
        for (int i = 0 ; i != 10 ; ++i) {
            v.findViewById(stars_press_id[i]).setOnClickListener(listener);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请给承租方打分").setView(v);
        builder.setPositiveButton("提交", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                JSONObject param = new JSONObject();
                try {
                    param.put("side", order.getSide());
                    param.put("oid", order.getOid());
                    param.put("leaser_rate", rate_leaser+1);
                    param.put("leasee_id", order.getLeaseeId());
                } catch (JSONException je) {
                    je.printStackTrace();
                }

                show_processing(UPDATE);
                HttpUtil.JSONHttpPOST(HttpUtil.host+"c", param, orderUpdateHandler);
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

    private void drawStars(ImageView stars[], int origin, int current) {
        if (origin == current)
            return;
        int o_star = origin/2;
        int c_star = current/2;
        boolean isCurFull = current%2 == 1;
        if (o_star < c_star) {
            for (int i = o_star ; i != c_star ; ++i) {
                stars[i].setImageBitmap(starFull);
            }
        } else {
            for (int i = o_star ; i != c_star ; --i) {
                stars[i].setImageBitmap(starEmpty);
            }
        }
        if (isCurFull) {
            stars[c_star].setImageBitmap(starFull);
        } else {
            stars[c_star].setImageBitmap(starHalf);
        }
    }

    private void init_pop() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v1 = inflater.inflate(R.layout.popup_confirmsignfor, null);
        View v2 = inflater.inflate(R.layout.popup_input_exp_id, null);
        v1.findViewById(R.id.yes).setOnClickListener(this);
        v1.findViewById(R.id.no).setOnClickListener(this);
        v2.findViewById(R.id.confirm).setOnClickListener(this);
        v2.findViewById(R.id.cancel_btn).setOnClickListener(this);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenwidth = metric.widthPixels;
        screenheight = metric.heightPixels;

        pop_ConfirmSignfor = new PopupWindow(v1, (int)(screenwidth*0.6),(int)(screenheight*0.1145), false);
        pop_ConfirmSignfor.setFocusable(true);
        pop_ConfirmSignfor.setBackgroundDrawable(getResources().getDrawable(R.drawable.emptybg));
        pop_ConfirmSignfor.setOutsideTouchable(true);

        pop_conConfirmSendout = new PopupWindow(v2, (int)(screenwidth*0.6),(int)(screenheight*0.18), false);
        pop_conConfirmSendout.setFocusable(true);
        pop_conConfirmSendout.setBackgroundDrawable(getResources().getDrawable(R.drawable.emptybg));
        pop_conConfirmSendout.setOutsideTouchable(true);

    }

    public void loadOrder(int oid) {
        HttpUtil.HttpClientGET(HttpUtil.host+"order?action=full&oid="+String.valueOf(oid), orderHandler);
    }

    public void updateOrder(JSONObject data) {
        show_processing(UPDATE);
        HttpUtil.JSONHttpPOST(HttpUtil.host+"order?action=update", data, orderUpdateHandler);
    }

    public void handleLoadMessage(Message msg) {
        try {
            if (msg.what == 0) {
                JSONObject json = new JSONObject(msg.obj.toString());
                order = new Order(json, false);
                order.setSide(getIntent().getIntExtra("side",0));
                loadViews();
            } else {
                Toast.makeText(this,"网络异常" , Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleExpIdentifyMessage(Message msg) {
        try {
            if (msg.what == 0) {
                progress.setVisibility(View.INVISIBLE);
                JSONObject json = new JSONObject(msg.obj.toString());
                boolean identified = json.getBoolean("Success");
                if (identified) {
                    possibleComCode = new ArrayList<>();
                    possibleComName = new ArrayList<>();
                    String raw = json.getString("Shippers");
                    JSONArray shippers = new JSONArray(raw);
                    for (int i = 0 ; i != shippers.length() ; ++i) {
                        possibleComCode.add(shippers.getJSONObject(i).getString("ShipperCode"));
                        possibleComName.add(shippers.getJSONObject(i).getString("ShipperName"));
                    }
                    selectExpCom(possibleComName);
                } else {
                    Toast.makeText(this, "无法识别该快递单号，请检查后重试", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateMessage(Message msg) {
        try {
            if (waiting.isShowing()) {
                waiting.dismiss();
            }
            if (msg.what == 0) {
                String res = msg.obj.toString();
                Log.e("update res", res);
                if (res.compareTo("0") == 0) {
                    if (order.getStage() == 2) {
                        order.setExp_leaser(expNo, selectedComCode, selectedComName);
                    } else if (order.getStage() == 4) {
                        order.setExp_leasee(expNo, selectedComCode, selectedComName);
                    }
                    order.nextStage();
                    reset_page();
                    setProcess();
                } else if (res.compareTo("2") == 0) {
                    //设置出租方给承租方的评分
                    order.setLeaser_rate(rate_leaser);
                    function_btn.setText(getString(R.string.complain));
                } else if (res.compareTo("3") == 0) {
                    Toast.makeText(this, "已关闭订单", Toast.LENGTH_SHORT).show();
                    order.setStatus(2);
                    reset_page();
                    setProcess();
                } else if (res.compareTo("4") == 0) {
                    Toast.makeText(this, "出租方已发货，无法取消订单", Toast.LENGTH_SHORT).show();
                } else if (res.compareTo("5") == 0) {
                    Toast.makeText(this, "评价成功", Toast.LENGTH_SHORT).show();
                    function_btn.setText(getString(R.string.complain));
                    order.setLeaser_rate(rate_leaser);
                } else {
                    JSONObject json = new JSONObject(res);
                    if (json.getString("code").compareTo("10000") == 0) {
                        Toast.makeText(this, "退款成功，订单已关闭", Toast.LENGTH_SHORT).show();
                        order.setStatus(2);
                        reset_page();
                        setProcess();
                    } else {
                        Toast.makeText(this, "退款失败，请稍候再试 code:" + json.getString("code"), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(OrderProcessActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOrderInfoMessage(Message msg) {
        try {
            if (waiting.isShowing())
                waiting.dismiss();
            if (msg.what == 0) {
                final String orderInfo = msg.obj.toString();
                Runnable payRunnable = new Runnable() {

                    @Override
                    public void run() {
                        PayTask alipay = new PayTask(OrderProcessActivity.this);
                        Map<String, String> result = alipay.payV2(orderInfo, true);
                        Log.i("msp", result.toString());

                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = result;
                        payHandler.sendMessage(msg);
                    }
                };
                Thread payThread = new Thread(payRunnable);
                payThread.start();
            } else {
                Toast.makeText(OrderProcessActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePayMessage(Message msg) {
        Map<String, String> res = (Map<String, String>) msg.obj;

        String status = res.get("resultStatus");
        if (status.compareTo("9000") == 0 || status.compareTo("8000") == 0) {
            show_processing(PAYRESULT);
            HttpUtil.HttpClientGET(HttpUtil.host + "order?action=confirm&oid=" + String.valueOf(order.getOid()), confirmHandler);
        } else {
            Toast.makeText(OrderProcessActivity.this, "支付未完成，请尽快支付", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleConfirmMessage(Message msg) {
        try {
            if (waiting.isShowing())
                waiting.dismiss();
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (res.compareTo("TRADE_SUCCESS") == 0) {
                    order.setStatus(1);
                    reset_page();
                    setProcess();
                } else if (res.compareTo("timeout") == 0) {
                    Toast.makeText(OrderProcessActivity.this, "订单支付状态更新失败，请稍候重试", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrderProcessActivity.this, "支付未完成，请尽快支付", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OrderProcessActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show_processing(int what) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.loging, null);
        switch (what) {
            case ORDERINFO:
                ((TextView)v.findViewById(R.id.hint)).setText(getString(R.string.waiting_orderInfo));
                break;
            case PAYRESULT:
                ((TextView)v.findViewById(R.id.hint)).setText(getString(R.string.waiting_payResult));
                break;
            case UPDATE:
                ((TextView)v.findViewById(R.id.hint)).setText(getString(R.string.waiting_updateOrder));
                break;
            case REFUND:
                ((TextView)v.findViewById(R.id.hint)).setText(getString(R.string.waiting_refund));
                break;
            case CLOSE:
                ((TextView)v.findViewById(R.id.hint)).setText(getString(R.string.waiting_close));
                break;
        }

        waiting = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        waiting.setFocusable(true);
        waiting.showAtLocation(findViewById(R.id.root), Gravity.CENTER|Gravity.CENTER, 0,0);
    }
}
