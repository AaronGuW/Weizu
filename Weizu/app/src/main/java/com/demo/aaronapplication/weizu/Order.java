package com.demo.aaronapplication.weizu;



import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Created by Aaron on 2016/3/27.
 */
public class Order implements Serializable {
    //constants
    public static final int FACETOFACE = 0, EXPRESS = 1;
    public static final int LEASER = 0, LEASEE = 1;

    private int oid; //订单ID
    private String title; //商品标题
    private int leaserId, leaseeId; //招租方和租用方的ID
    private int gid;    //目标商品的ID
    private int period; //目标商品的计费周期 0 小时/1 天
    private int amount, renttime, way_rcv, way_rtn; //数量，时长，收/还货方式
    private String exp_id_leaser, exp_id_leasee;    //来回的快递编号
    private String exp_com_code_leaser, exp_com_code_leasee;    //来回快递公司编号
    private String exp_com_name_leaser, exp_com_name_leasee;    //来回快递公司名称
    private int stage;  //1~6 不存在介于1阶段已完成和2阶段未完成间的状态
    private int status;     //订单状态 0 未付款, 1 进行中， 2 已关闭， 3 已完成，
    private String leaserName, leaseeName;  //出租方和租赁方的用户名
    private int side;   //当前用户属于哪一方
    private String note;    //可选备注
    private String intro;       //文字介绍
    private String cover;   //封面图片md5
    private float rent, deposit, freight;  //租金（单价），押金，邮费

    private int aid; //收货地址ID
    private String phone, recipient, address; //收货人号码， 姓名和 具体住址

    private int leasee_cid; //承租方的评论id
    private int leaser_rate;    //出租方给承租方的打分

    private static final DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");

    public Order() {}
    public Order(int stage, int side) {
        this.stage = stage;
        this.side = side;
    }

    public Order(JSONObject res, boolean simple) {
        try {
            oid = res.getInt("oid");
            title = res.getString("title");
            status = res.getInt("status");
            stage = res.getInt("stage");
            leasee_cid = res.getInt("leasee_cid");
            leaser_rate = res.getInt("leaser_rate");
            if (!simple) {
                gid = res.getInt("gid");
                leaseeId = res.getInt("leaseeId");
                leaserId = res.getInt("leaserId");
                leaseeName = res.getString("leaseeName");
                leaserName = res.getString("leaserName");
                period = res.getInt("period");
                amount = res.getInt("amount");
                renttime = res.getInt("time");
                way_rcv = res.getInt("way_rcv");
                way_rtn = res.getInt("way_rtn");
                exp_id_leasee = res.getString("exp_id_leasee");
                exp_id_leaser = res.getString("exp_id_leaser");
                exp_com_code_leasee = res.getString("exp_com_code_leasee");
                exp_com_name_leasee = res.getString("exp_com_name_leasee");
                exp_com_code_leaser = res.getString("exp_com_code_leaser");
                exp_com_name_leaser = res.getString("exp_com_name_leaser");
                note = res.getString("note");
                cover = res.getString("cover");

                rent = (float) res.getDouble("rent");
                deposit = (float) res.getDouble("deposit");
                freight = (float) res.getDouble("freight");
                intro = res.getString("intro");

                aid = res.getInt("aid");
                phone = res.getString("phone");
                recipient = res.getString("recipient");
                address = res.getString("address");

            }
        } catch (JSONException JE) {
            JE.printStackTrace();
        }
    }

    public void setSide(int s) { side = s; }
    public int getStage() { return stage; }
    public int getSide() { return side; }
    public void setId(int id) { this.oid = id; }
    public int getId() { return oid; }
    public void nextStage() {
        if (stage < 6)
            stage++;
    }
    public String getShortTitle() {
        if (title.length() > 12) {
            return title.substring(0,12)+"...";
        } else {
            return title;
        }
    }

    public int getLeaserId() { return leaserId; }
    public int getLeaseeId() { return leaseeId; }
    public int getPeriod() { return period; }
    public int getAmount() { return amount; }
    public int getRenttime() { return renttime; }
    public int getGid() { return gid; }

    public int getWay_rcv() {
        return way_rcv;
    }

    public int getWay_rtn() {
        return way_rtn;
    }

    public int getOid() {
        return oid;
    }

    public String getExp_id_leasee() {
        return exp_id_leasee;
    }

    public String getExp_id_leaser() {
        return exp_id_leaser;
    }

    public String getExp_com_code_leaser() { return exp_com_code_leaser; }

    public String getExp_com_code_leasee() { return exp_com_code_leasee; }

    public String getExp_com_name_leaser() { return exp_com_name_leaser; }

    public String getExp_com_name_leasee() { return exp_com_name_leasee; }

    public void setExp_leaser(String exp_id, String com_code, String com_name) {
        exp_id_leaser = exp_id;
        exp_com_code_leaser = com_code;
        exp_com_name_leaser = com_name;
    }

    public void setExp_leasee(String exp_id, String com_code, String com_name) {
        exp_id_leasee = exp_id;
        exp_com_code_leasee = com_code;
        exp_com_name_leasee = com_name;
    }

    public String getLeaseeName() {
        return leaseeName;
    }

    public String getLeaserName() {
        return leaserName;
    }

    public String getShortIntro() {
        if (intro.length() > 15)
            return intro.substring(0,15)+"...";
        return intro;
    }

    public String getNote() {
        return note;
    }

    public String getFormatRent() {
        return decimalFormat.format(rent);
    }

    public String getFormatDeposit() {
        return decimalFormat.format(deposit);
    }

    public String getFormatFreight() {
        return decimalFormat.format(freight);
    }

    public String getCoverName() {
        if (cover.length() == 0)
            return null;
        return cover+".jpeg";
    }

    public String getCoverMD5() {
        if (cover.length() == 0)
            return null;
        return cover;
    }

    public String getShortAddress() {
        String compatAddress = recipient + " ";
        if (address.length() > 12) {
            compatAddress += address.substring(0,12)+"...";
        } else {
            compatAddress += address;
        }
        return compatAddress;
    }

    public String getPhone() { return phone; }
    public String getRecipient() { return recipient; }
    public String getAddress() { return address; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getLeasee_cid() { return leasee_cid; }
    public int getLeaser_rate() { return leaser_rate; }
    public void setLeaser_rate(int rate) { leaser_rate = rate; }
    public void setLeasee_cid(int cid) { leasee_cid = cid; }
}
