package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.ImageManager;
import com.demo.aaronapplication.weizu.Order;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.UIUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Aaron on 2017/1/22.
 */
public class MakeCommentActivity extends Activity implements View.OnClickListener, ImageManager.onFinishLoadListener {

    private static final int stars_id[] = { R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5 };
    private static final int stars_press_id[] = { R.id.left_1, R.id.right_1, R.id.left_2, R.id.right_2, R.id.left_3, R.id.right_3,
                                                            R.id.left_4, R.id.right_4, R.id.left_5, R.id.right_5 };
    private ImageView[] stars;

    private RadioGroup commentOverall;
    private Bitmap starFull, starHalf, starEmpty;

    private int rate_leaser;    //给出租方的打分
    private RadioGroup overall; //总体评价的radio group
    private EditText content;

    private Order order;

    private ImageView cover;
    private ImageManager imageManager;

    private Handler submitHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.makecomment);

        submitHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleSubmitMessage(msg);
            }
        };

        imageManager = new ImageManager();
        imageManager.setOnFinishLoadListener(this);

        order = (Order)getIntent().getSerializableExtra("order");
        rate_leaser = 9;
        init();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            alert_abandon();
            return true;
        } else {
            return super.onKeyDown(keyCode,event);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                alert_abandon();
                break;
            case R.id.submit:
                if (content.getText().length() > 0)
                    submit_comment();
                else
                    Toast.makeText(MakeCommentActivity.this, "评论不可为空", Toast.LENGTH_SHORT).show();
                break;
            case R.id.left_1:
                drawStars(rate_leaser, 0);
                rate_leaser = 0;
                break;
            case R.id.right_1:
                drawStars(rate_leaser, 1);
                rate_leaser = 1;
                break;
            case R.id.left_2:
                drawStars(rate_leaser, 2);
                rate_leaser = 2;
                break;
            case R.id.right_2:
                drawStars(rate_leaser, 3);
                rate_leaser = 3;
                break;
            case R.id.left_3:
                drawStars(rate_leaser, 4);
                rate_leaser = 4;
                break;
            case R.id.right_3:
                drawStars(rate_leaser, 5);
                rate_leaser = 5;
                break;
            case R.id.left_4:
                drawStars(rate_leaser, 6);
                rate_leaser = 6;
                break;
            case R.id.right_4:
                drawStars(rate_leaser, 7);
                rate_leaser = 7;
                break;
            case R.id.left_5:
                drawStars(rate_leaser, 8);
                rate_leaser = 8;
                break;
            case R.id.right_5:
                drawStars(rate_leaser, 9);
                rate_leaser = 9;
                break;
        }
    }

    @Override
    public void onFinishLoading(ImageView holder, String path) {
        if (holder != null) {
            Picasso.with(this).load(new File(path)).resize(128, 128).centerInside().into(holder);
        }
    }

    private void init() {
        cover = (ImageView) findViewById(R.id.cover);
        String coverName = order.getCoverName();
        if (coverName != null) {
            String path = imageManager.getImagePath(order.getCoverName(), ImageManager.THUMBNAIL);
            if (path != null) {
                Picasso.with(this).load(new File(path)).resize(128, 128).centerInside().into(cover);
            } else {
                imageManager.downloadImage(cover, order.getCoverName(), ImageManager.THUMBNAIL);
            }
        }
        stars = new ImageView[5];
        for (int i = 0 ; i != 5 ; ++i) {
            stars[i] = (ImageView)findViewById(stars_id[i]);
        }

        for (int i = 0 ; i != 10 ; ++i) {
            findViewById(stars_press_id[i]).setOnClickListener(this);
        }

        starFull = BitmapFactory.decodeResource(getResources(), R.drawable.star_full);
        starHalf = BitmapFactory.decodeResource(getResources(), R.drawable.star_half);
        starEmpty = BitmapFactory.decodeResource(getResources(), R.drawable.star_empty);

        overall = (RadioGroup)findViewById(R.id.overall);
        content = (EditText)findViewById(R.id.comment);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    private void submit_comment() {
        JSONObject comment = new JSONObject();
        try {
            comment.put("side", order.getSide());
            comment.put("gid", order.getGid());
            comment.put("oid", order.getOid());
            comment.put("uid", order.getLeaseeId());    //用户Id即订单的承租方id
            comment.put("leaser_id", order.getLeaserId());
            comment.put("date", System.currentTimeMillis());
            comment.put("content", Base64.encodeToString(content.getText().toString().getBytes(), Base64.DEFAULT));
            int overallRate;
            if (overall.getCheckedRadioButtonId() == R.id.radioButton_Positive) {
                Log.e("radio","positive");
                overallRate = 0;
            } else if (overall.getCheckedRadioButtonId() == R.id.radioButton_Neutral) {
                Log.e("radio","neutral");
                overallRate = 1;
            } else if (overall.getCheckedRadioButtonId() == R.id.radioButton_Negative) {
                Log.e("radio","negative");
                overallRate = 2;
            } else {
                Log.e("radio","wtf!");
                overallRate = -1;
            }
            comment.put("overall", overallRate);
            comment.put("leaser", rate_leaser+1);
        } catch (JSONException je) {
            je.printStackTrace();
        }
        HttpUtil.JSONHttpPOST(HttpUtil.host+"c", comment, submitHandler);
    }

    private void alert_abandon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("您尚未提交评论,确定要返回吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MakeCommentActivity.this.setResult(RESULT_CANCELED);
                MakeCommentActivity.this.finish();
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

    private void drawStars(int origin, int current) {
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

    private void handleSubmitMessage(Message msg) {
        try {
            if (msg.what == 0) {
                Toast.makeText(MakeCommentActivity.this, "评价成功", Toast.LENGTH_SHORT).show();
                Intent result = new Intent();
                result.putExtra("res", msg.obj.toString());
                MakeCommentActivity.this.setResult(RESULT_OK, result);
                MakeCommentActivity.this.finish();
            } else {
                Toast.makeText(MakeCommentActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
