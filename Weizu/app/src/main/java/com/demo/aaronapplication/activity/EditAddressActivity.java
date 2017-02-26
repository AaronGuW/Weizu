package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.Address;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aaron on 2016/3/26.
 */
public class EditAddressActivity extends Activity implements View.OnClickListener {

    private EditText label, recipient, phonenumber, address;
    private int aid;
    private Handler opHandler;
    private int action;

    private Map<String, Object> result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addresseditor);
        action = getIntent().getIntExtra("action",0);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.finish).setOnClickListener(this);

        opHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleOpMessage(msg);
            }
        };

        initViews();
    }

    private void initViews() {
        label = (EditText)findViewById(R.id.label_input);
        recipient = (EditText)findViewById(R.id.recipient_input);
        phonenumber = (EditText)findViewById(R.id.phonenumber_input);
        address = (EditText)findViewById(R.id.address_input);

        if (action == SelectAddressActivity.MOD) {
            try {
                JSONObject addr = new JSONObject(getIntent().getStringExtra("address"));
                aid = addr.getInt("aid");
                label.setText(addr.getString("label"));
                recipient.setText(addr.getString("recipient"));
                phonenumber.setText(addr.getString("phonenumber"));
                address.setText(addr.getString("address"));
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            ((TextView)findViewById(R.id.headline)).setText(getString(R.string.newaddress));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.finish:
                if (isValid()) {
                    switch (action) {
                        case SelectAddressActivity.NEW:
                            submitNewAddress();
                            break;
                        case SelectAddressActivity.MOD:
                            submitChanges();
                            break;
                    }
                }
                break;
        }
    }

    private boolean isValid() {
        if (label.getText().length() == 0) {
            Toast.makeText(EditAddressActivity.this, "标签不可为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (recipient.getText().length() == 0) {
            Toast.makeText(EditAddressActivity.this, "收件人姓名不可为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (phonenumber.getText().length() == 0) {
            Toast.makeText(EditAddressActivity.this, "手机号不可为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (address.getText().length() == 0) {
            Toast.makeText(EditAddressActivity.this, "地址不可为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        result = new HashMap();
        if (action == SelectAddressActivity.MOD)
            result.put("aid",aid);
        result.put("label", label.getText().toString());
        result.put("recipient", recipient.getText().toString());
        result.put("phonenumber", phonenumber.getText().toString());
        result.put("address", address.getText().toString());
        return true;
    }

    private void submitChanges() {;
        JSONObject address = new JSONObject();
        try {
            address.put("aid",aid);
            address.put("label", Base64.encodeToString(result.get("label").toString().getBytes(),Base64.DEFAULT));
            address.put("recipient", Base64.encodeToString(result.get("recipient").toString().getBytes(),Base64.DEFAULT));
            address.put("phonenumber", Base64.encodeToString(result.get("phonenumber").toString().getBytes(),Base64.DEFAULT));
            address.put("address", Base64.encodeToString(result.get("address").toString().getBytes(),Base64.DEFAULT));
        } catch (JSONException JE) {
            JE.printStackTrace();
        }
        HttpUtil.JSONHttpPOST(HttpUtil.host+"addr?action="+ Address.MOD+"&uid="+getSharedPreferences("account",MODE_PRIVATE).getString("uid","0"),address,opHandler);
    }

    private void submitNewAddress() {
        JSONObject address = new JSONObject();
        try {
            address.put("label", Base64.encodeToString(result.get("label").toString().getBytes(),Base64.DEFAULT));
            address.put("recipient", Base64.encodeToString(result.get("recipient").toString().getBytes(),Base64.DEFAULT));
            address.put("phonenumber", Base64.encodeToString(result.get("phonenumber").toString().getBytes(),Base64.DEFAULT));
            address.put("address", Base64.encodeToString(result.get("address").toString().getBytes(),Base64.DEFAULT));
        } catch (JSONException JE) {
            JE.printStackTrace();
        }
        HttpUtil.JSONHttpPOST(HttpUtil.host+"addr?action="+ Address.NEW+"&uid="+getSharedPreferences("account",MODE_PRIVATE).getString("uid","0"),address,opHandler);
    }

    private void handleOpMessage(Message msg) {
        /* handle the result of operation according to the initial action */
        try {
            if (msg.what == 0) {
                switch (action) {
                    case SelectAddressActivity.NEW:
                        int aid = Integer.valueOf(msg.obj.toString());
                        result.put("aid",aid);
                        JSONObject json = new JSONObject(result);
                        Intent res = new Intent();
                        res.putExtra("created",true);
                        res.putExtra("address",json.toString());
                        setResult(RESULT_OK, res);
                        finish();
                        break;
                    case SelectAddressActivity.MOD:
                        JSONObject js = new JSONObject(result);
                        Intent res2 = new Intent();
                        res2.putExtra("modified", true);
                        res2.putExtra("address", js.toString());
                        setResult(RESULT_OK, res2);
                        finish();
                        break;
                }
            } else {
                Toast.makeText(EditAddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
