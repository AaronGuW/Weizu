package com.demo.aaronapplication.weizu;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aaron on 2016/3/21.
 */
public class goods implements Serializable {

    private static final DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");

    private int gid;
    private String title;   //标题
    private int category;  //大类
    private int childType; //子类
    private float rent;    //租金
    private float deposit;  //押金
    private float freight; //运费
    private int leaser;      //出租者id
    private String leaserName; //出租者用户名
    private String description;  //文字描述
    private String contact;  //持有者的联系方式
    private String location;     //持有者所在区域
    private int sales;       //销量
    private Long date;      //发布日期的时间戳
    private int picnum;     //图片数量
    private int coverindex; //封面图片的序号
    private int period; //计费周期 天/小时
    private int comment_num;    //目前的总评论数
    private String cover;   //封面的Md5
    private String[] pictures;  //所有图片的md5
    private int status; //0 正常 1 已下架

    private int format;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final int SIMPLE = 0, FULL = 1;
    public static final int HOUR = 0, DAY = 1;

    public goods() {}

    public goods(String t, float r, float d, float f, String des, String c, String l, int ctgy, int ctype, int le, int sa, Long da, int num, int index, int p) {
        title = t;
        rent = r;
        deposit = d;
        freight = f;
        description = des;
        contact = c;
        location = l;
        category = ctgy;
        childType = ctype;
        leaser = le;
        sales = sa;
        date = da;
        picnum = num;
        coverindex = index;
        period = p;
    }

    public goods(JSONObject r, Boolean Simple) {
        try {
            if (!Simple) {
                format = FULL;

                title = r.getString("title");
                rent = (float) (r.getDouble("rent"));
                deposit = (float) (r.getDouble("deposit"));
                freight = (float) (r.getDouble("freight"));
                description = r.getString("intro");
                contact = r.getString("contact");
                location = r.getString("location");
                category = r.getInt("type");
                childType = r.getInt("childtype");
                leaser = r.getInt("leaser");
                if (r.has("leasername"))
                    leaserName = r.getString("leasername");
                sales = r.getInt("sales");
                date = Long.valueOf(r.getString("date"));
                period = r.getInt("period");
                comment_num = r.getInt("commentcnt");
                gid = r.getInt("gid");
                cover = r.getString("cover");
                String tmp = r.getString("md5s");
                pictures = new String[tmp.length()/32];
                for (int i = 0 ; i != tmp.length()/32 ; ++i) {
                    pictures[i] = tmp.substring(i*32,(i+1)*32);
                }
                status = r.getInt("status");
            } else {
                title = r.getString("title");
                rent = (float) (r.getDouble("rent"));
                deposit = (float) (r.getDouble("deposit"));
                freight = (float) (r.getDouble("freight"));
                location = r.getString("location");
                description = r.getString("intro");
                gid = r.getInt("gid");
                Log.e(title, String.valueOf(gid));
                cover = r.getString("cover");
                period = r.getInt("period");
                leaser = r.getInt("leaser");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() { return title; }
    public int getGid() { return gid; }
    public float getRent() { return rent; }
    public float getDeposit() { return deposit; }
    public float getFreight() { return freight; }
    public String getDescription() { return description; }
    public String getContact() { return contact; }
    public String getLocation() { return location; }
    public int getCategory() { return category; }
    public int getChildType() { return childType; }

    public void setGid(int id) { gid = id; }
    public int getSales() { return sales; }
    public int getLeaser() { return leaser; }
    public int getComment_num() { return comment_num; }
    public long getDate() { return date; }
    public int getPicnum() {
        if (format == SIMPLE) {
            Log.e("release", "a simple release stores no pictures");
            return 0;
        }
        return pictures.length;
    }
    public int getCoverindex() { return coverindex; }
    public int getPeriod() { return period; }
    public String getLeaserName() { return leaserName; }
    public String getFormatPrice() {
        return decimalFormat.format(rent)+"/"+(period == 0?"小时":"天");  //懒得传入resource了... 就用明文吧
    }
    public String getFormatDate() {
        String dateString = dateFormat.format(new Date(date));
        return dateString;
    }

    public String getCoverName() {
        if (cover.length() == 0) {
            return null;
        } else {
            return cover+".jpeg";
        }
    }

    public String getCoverMD5() {
        if (cover.length() == 0) {
            return null;
        } else {
            return cover;
        }
    }

    public String getShortDescription() {
        if (description.length() > 37) {
            return description.substring(0,37) + "...";
        } else {
            return description;
        }
    }

    public boolean hasCover() {
        return cover.length() != 0;
    }

    /**
     * calculate the total price
     * @param num
     * @param express receive the goods by express or not
     * @return total price
     */
    public float sum(int num, int time, boolean express) {
        if (num == 0 || time == 0) {
            return 0;
        }
        return (rent*time+deposit)*num+(express?freight:0);
    }

    public String[] getPictures() {
        if (format == SIMPLE) {
            Log.e("release", "a simple release stores no pictures");
            return null;
        }
        return pictures;
    }

    public boolean isDeleted() { return status == 1; }

}
