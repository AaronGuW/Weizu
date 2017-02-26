package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.Address;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aaron on 2016/3/26.
 */
public class SelectAddressActivity extends Activity implements View.OnClickListener{

    private ListView addresslist;
    private ArrayList<Address> addressArrayList;
    private BaseAdapter addressAdapter;
    private LayoutInflater inflater;

    private Handler addrHandler, opHandler;

    private String uid; //方便一点

    private boolean manage; //该activity有两个入口，管理地址和选择地址

    public static final int NEW = 0, MOD = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectaddresspage);

        manage = getIntent().getBooleanExtra("manage", false);

        uid = getSharedPreferences("account",MODE_PRIVATE).getString("uid","0");

        addrHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleAddrMessage(msg);
            }
        };
        opHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleOpMessage(msg);
            }
        };

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.newAddress).setOnClickListener(this);

        addresslist = (ListView)findViewById(R.id.addresslist);
        addressArrayList = new ArrayList<>();
        inflater = LayoutInflater.from(this);

        init_addresslist();
        refreshAddressList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case MOD:
                if (data.getBooleanExtra("modified", false)) {
                    String newAddr = data.getStringExtra("address");
                    try {
                        JSONObject js = new JSONObject(newAddr);
                        modifyAddress(js);
                    } catch (JSONException JE) {
                        JE.printStackTrace();
                    }
                }
                break;
            case NEW:
                if (data.getBooleanExtra("created", false)) {
                    String newAddr = data.getStringExtra("address");
                    try {
                        JSONObject js = new JSONObject(newAddr);
                        addAddress(js);
                    } catch (JSONException JE) {
                        JE.printStackTrace();
                    }
                }
        }
    }

    private void init_addresslist() {
        addressAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return addressArrayList.size();
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
                    v = inflater.inflate(R.layout.addressitem, null);
                }
                ((TextView)v.findViewById(R.id.address)).setText(addressArrayList.get(position).getAddress());
                ((TextView)v.findViewById(R.id.label)).setText(addressArrayList.get(position).getLabel());
                ((TextView)v.findViewById(R.id.name)).setText(addressArrayList.get(position).getRecipient());
                ((TextView)v.findViewById(R.id.phone)).setText(addressArrayList.get(position).getPhonenumber());

                final Address addr = addressArrayList.get(position);
                v.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("edit","clicked");
                        Map<String,Object> address = new HashMap<>();
                        address.put("aid",addr.getAid());
                        address.put("label",addr.getLabel());
                        address.put("recipient", addr.getRecipient());
                        address.put("address", addr.getAddress());
                        address.put("phonenumber", addr.getPhonenumber());
                        JSONObject js = new JSONObject(address);

                        Intent intent = new Intent(SelectAddressActivity.this, EditAddressActivity.class);
                        intent.putExtra("action", MOD);
                        intent.putExtra("address", js.toString());
                        startActivityForResult(intent, MOD);
                    }
                });
                v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog(addr.getAid());
                    }
                });
                return v;
            }
        };
        addresslist.setAdapter(addressAdapter);
        if (!manage) {
            addresslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Address addr = addressArrayList.get(position);
                /*Map<String,Object> address = new HashMap<>();
                address.put("aid",addr.getAid());
                address.put("label",addr.getLabel());
                address.put("recipient", addr.getRecipient());
                address.put("address", addr.getAddress());
                address.put("phonenumber", addr.getPhonenumber());
                JSONObject json = new JSONObject(address);*/

                    Intent result = new Intent();
                    result.putExtra("selected", true);
                    result.putExtra("address", addr);
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
        }
    }

    private void modifyAddress(JSONObject jsonAddr) {
        try {
            int aid = jsonAddr.getInt("aid");
            for (Address addr : addressArrayList) {
                if (addr.getAid() == aid) {
                    addr.modify(jsonAddr);
                    break;
                }
            }
            addressAdapter.notifyDataSetChanged();
        } catch (JSONException JE) {
            JE.printStackTrace();
        }
    }

    private void addAddress(JSONObject jsonAddr) {
        Address newAddr = new Address(jsonAddr);
        addressArrayList.add(newAddr);
        addressAdapter.notifyDataSetChanged();
    }

    private void refreshAddressList() {
        HttpUtil.HttpClientGET(HttpUtil.host + "addr?action=" + Address.GET + "&uid=" + uid, addrHandler);
    }

    private void deleteAddress(int aid) {
        HttpUtil.HttpClientGET(HttpUtil.host + "addr?action=" + Address.DEL + "&aid=" + String.valueOf(aid) + "&uid=" + uid, opHandler);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                if (!manage) {
                    Intent emptyIntent = new Intent();
                    setResult(RESULT_OK, emptyIntent);
                }
                finish();
                break;
            case R.id.newAddress:
                Intent intent = new Intent(SelectAddressActivity.this, EditAddressActivity.class);
                intent.putExtra("action",NEW);
                startActivityForResult(intent, NEW);
                break;
        }
    }

    private void handleAddrMessage(Message msg) {
        try {
            if (msg.what == 0) {
                String res = msg.obj.toString();
                if (res.equals("0")) {
                    Toast.makeText(SelectAddressActivity.this, "您的收货地址列表为空~",Toast.LENGTH_SHORT).show();
                } else {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0 ; i != jsonArray.length(); ++i) {
                        JSONObject js = jsonArray.getJSONObject(i);
                        Address addr = new Address(js);
                        addressArrayList.add(addr);
                    }
                    addressAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(SelectAddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOpMessage(Message msg) {
        //the only operation handled directly in this activity is delete
        try {
            if (msg.what == 0) {
                if (!msg.obj.toString().equals("0")) {
                    int aid = Integer.valueOf(msg.obj.toString());
                    for (Address addr : addressArrayList) {
                        if (addr.getAid() == aid) {
                            addressArrayList.remove(addr);
                            break;
                        }
                    }
                    addressAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(SelectAddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dialog(final int aid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认要删除该地址吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAddress(aid);
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
