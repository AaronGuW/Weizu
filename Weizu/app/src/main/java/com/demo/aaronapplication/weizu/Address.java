package com.demo.aaronapplication.weizu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Aaron on 2016/3/25.
 */
public class Address implements Serializable{
    private int aid;
    private String label;
    private String recipient;
    private String phonenumber;
    private String address;

    public static final String GET = "0", NEW = "1", MOD = "2", DEL = "3";

    public Address(String label, String recipient, String phonenumber, String address) {
        this.label = label;
        this.recipient = recipient;
        this.phonenumber = phonenumber;
        this.address = address;
    }

    public Address(JSONObject addr) {
        try {
            aid = addr.getInt("aid");
            label = addr.getString("label");
            recipient = addr.getString("recipient");
            address = addr.getString("address");
            phonenumber = addr.getString("phonenumber");
        } catch (JSONException JE) {
            JE.printStackTrace();
        }
    }

    public void modify(JSONObject newAddr) {
        try {
            label = newAddr.getString("label");
            recipient = newAddr.getString("recipient");
            address = newAddr.getString("address");
            phonenumber = newAddr.getString("phonenumber");
        } catch (JSONException JE) {
            JE.printStackTrace();
        }
    }

    public void setAid(int id) {
        aid = id;
    }
    public int getAid() { return aid; }
    public String getLabel() { return label; }
    public String getRecipient() { return recipient; }
    public String getPhonenumber() { return phonenumber; }
    public String getAddress() { return address; }
    public String getShortAddress() {
        String compatAddress = recipient + " ";
        if (address.length() > 9) {
            compatAddress += address.substring(0,9)+"...";
        } else {
            compatAddress += address;
        }
        return compatAddress;
    }
}
