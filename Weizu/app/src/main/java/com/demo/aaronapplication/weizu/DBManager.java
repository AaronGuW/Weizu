package com.demo.aaronapplication.weizu;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Aaron on 2016/3/23.
 */
public class DBManager {
    //SQLiteopenhelper
    private DBUtil helper;
    private SQLiteDatabase db;
    private Context context;

    public DBManager(Context context) {
        helper = new DBUtil(context);
        this.context = context;
        db = helper.getReadableDatabase();
    }

    /**
     * save the most recent search action
     * @param key           key word
     * @param timestamp     time
     */
    public void save_key(String key, long timestamp) {
        if (key.equals(""))
            return;
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBUtil.search_historyTable + " WHERE key = ?", new String[]{key});
        if (!cursor.moveToNext()) {
            db.execSQL("INSERT INTO " + DBUtil.search_historyTable + " VALUES (?,?,?)", new Object[]{null,key,timestamp});
        }
    }

    /**
     * Get the search history from the database
     * @param history   the results are put here
     * @param max        maximum number of search records we take
     */
    public void get_search_history(ArrayList<String> history, int max) {
        Cursor cursor = db.rawQuery("SELECT key FROM " + DBUtil.search_historyTable, null);
        String key;
        int cnt = 1;
        while (cursor.moveToNext()) {
            key = cursor.getString(cursor.getColumnIndex("key"));
            history.add(0,key);
            if (++cnt > max) {
                return;
            }
        }
    }

    public void clearAllRelease() {
        db.execSQL("delete from "+ DBUtil.releaseTable);
    }

    public void get_releaselist(ArrayList<goods> releaselist) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBUtil.releaseTable, null);
        String title, desc, contact, region;
        long date;
        int category, childtype, leaser, sales, gid;
        float price, deposit, freight;
        while (cursor.moveToNext()) {
            gid = cursor.getInt(cursor.getColumnIndex("gid"));
            title = cursor.getString(cursor.getColumnIndex("title"));
            desc = cursor.getString(cursor.getColumnIndex("desc"));
            contact = cursor.getString(cursor.getColumnIndex("contact"));
            region = cursor.getString(cursor.getColumnIndex("location"));
            category = cursor.getInt(cursor.getColumnIndex("category"));
            childtype = cursor.getInt(cursor.getColumnIndex("childtype"));
            price = cursor.getFloat(cursor.getColumnIndex("rent"));
            deposit = cursor.getFloat(cursor.getColumnIndex("deposit"));
            freight = cursor.getFloat(cursor.getColumnIndex("freight"));
            sales = cursor.getInt(cursor.getColumnIndex("sales"));
            leaser = cursor.getInt(cursor.getColumnIndex("leaser"));
            date = cursor.getLong(cursor.getColumnIndex("date"));
            int picnum = cursor.getInt(cursor.getColumnIndex("picnum"));
            int coverindex = cursor.getInt(cursor.getColumnIndex("coverindex"));
            int period = cursor.getInt(cursor.getColumnIndex("period"));
            goods ng = new goods(title,price,deposit,freight,desc,contact,region,category,childtype,leaser,sales, date, picnum, coverindex, period);
            ng.setGid(gid);
            releaselist.add(ng);
        }
        cursor.close();
    }

    public goods get_goods(int gid) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBUtil.releaseTable + " where gid=?", new String[]{String.valueOf(gid)});
        if (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String desc = cursor.getString(cursor.getColumnIndex("desc"));
            String contact = cursor.getString(cursor.getColumnIndex("contact"));
            String region = cursor.getString(cursor.getColumnIndex("location"));
            int category = cursor.getInt(cursor.getColumnIndex("category"));
            int childtype = cursor.getInt(cursor.getColumnIndex("childtype"));
            float price = cursor.getFloat(cursor.getColumnIndex("rent"));
            float deposit = cursor.getFloat(cursor.getColumnIndex("deposit"));
            float freight = cursor.getFloat(cursor.getColumnIndex("freight"));
            int sales = cursor.getInt(cursor.getColumnIndex("sales"));
            int leaser = cursor.getInt(cursor.getColumnIndex("leaser"));
            long date = cursor.getLong(cursor.getColumnIndex("date"));
            int picnum = cursor.getInt(cursor.getColumnIndex("picnum"));
            int coverindex = cursor.getInt(cursor.getColumnIndex("coverindex"));
            int period = cursor.getInt(cursor.getColumnIndex("period"));
            goods ng = new goods(title,price,deposit,freight,desc,contact,region,category,childtype,leaser,sales, date, picnum, coverindex, period);
            ng.setGid(gid);
            return ng;
        } else {
            return null;
        }
    }

    public boolean canQueryExpress(String expNo) {
        long now = System.currentTimeMillis();
        Cursor cursor = db.rawQuery("select lastQueryTime from " + DBUtil.expressTable + " WHERE expno=?", new String[]{expNo});
        if (cursor.moveToNext()) {
            long last = cursor.getLong(cursor.getColumnIndex("lastQueryTime"));
            if ((now - last)/14400000 >= 4) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public void saveExpressInfo(String expNo, String info) {
        long now = System.currentTimeMillis();
        Cursor cursor = db.rawQuery("select * from " + DBUtil.expressTable + " where expno=? limit 1", new String[]{expNo});
        if (cursor.moveToNext()) {
            db.execSQL("update " + DBUtil.expressTable + " set info=? where expno=?", new Object[]{info, expNo});
        } else {
            db.execSQL("insert into " + DBUtil.expressTable + " VALUES (?,?,?)", new Object[]{expNo, now, info});
        }
    }

    public String getExpressInfo(String expNo) {
        Cursor cursor = db.rawQuery("select info from " + DBUtil.expressTable + " where expno=?", new String[]{expNo});
        if (cursor.moveToNext())
            return cursor.getString(cursor.getColumnIndex("info"));
        else
            return null;
    }

    public void save_new_release(goods r) {
        db.execSQL("INSERT INTO " + DBUtil.releaseTable + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{ r.getGid(), r.getTitle(),r.getDescription(),r.getCategory(),
                                                                                                            r.getChildType(), r.getRent(),r.getDeposit(),r.getFreight(),r.getContact(),r.getLocation(),
                                                                                                            r.getLeaser(),r.getSales(),r.getDate(),r.getPicnum(),r.getCoverindex(),r.getPeriod() });
    }

    public void modify_old_release(goods r) {
        db.execSQL("UPDATE " + DBUtil.releaseTable + " SET title=?,desc=?,category=?,childtype=?,rent=?,deposit=?,freight=?,contact=?,location=?,picnum=?,coverindex=?,period=? WHERE gid=?",
                new Object[]{r.getTitle(),r.getDescription(),r.getCategory(),r.getChildType(),r.getRent(),r.getDeposit(),r.getFreight(),r.getContact(),r.getLocation(),r.getPicnum(),r.getCoverindex(),r.getPeriod(),r.getGid()});
    }

    public void get_all_orders(ArrayList<Order> lessor_finished, ArrayList<Order> lessor_unfinished, int s) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBUtil.orderTable + " WHERE side=0", null);
        int order_id, lessor_id, lessee_id, goods_id, amount, way_rcv, way_send, stage, side;
        String exp_id_lessor, exp_id_lessee;
        while (cursor.moveToNext()) {
            order_id = cursor.getInt(cursor.getColumnIndex("_id"));
            lessor_id = cursor.getInt(cursor.getColumnIndex("lessor_id"));
            lessee_id = cursor.getInt(cursor.getColumnIndex("lessee_id"));
            goods_id = cursor.getInt(cursor.getColumnIndex("goods_id"));
            amount = cursor.getInt(cursor.getColumnIndex("amount"));
            way_rcv = cursor.getInt(cursor.getColumnIndex("way_rcv"));
            way_send = cursor.getInt(cursor.getColumnIndex("way_send"));
            stage = cursor.getInt(cursor.getColumnIndex("stage"));
            side = cursor.getInt(cursor.getColumnIndex("side"));
            exp_id_lessor = cursor.getString(cursor.getColumnIndex("exp_id_lessor"));
            exp_id_lessee = cursor.getString(cursor.getColumnIndex("exp_id_lessee"));
            //TODO intialize a complete order object
            if (stage != 6) {
                lessor_unfinished.add(new Order(stage,side));
            } else {
                lessor_finished.add(new Order(stage,side));
            }
        }
        cursor.close();
    }

    public void get_all_orders(ArrayList<Order> lessee_finished, ArrayList<Order> lessee_unfinished) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBUtil.orderTable + " WHERE side=1", null);
        int order_id, lessor_id, lessee_id, goods_id, amount, way_rcv, way_send, stage, side;
        String exp_id_lessor, exp_id_lessee;
        while (cursor.moveToNext()) {
            order_id = cursor.getInt(cursor.getColumnIndex("_id"));
            lessor_id = cursor.getInt(cursor.getColumnIndex("lessor_id"));
            lessee_id = cursor.getInt(cursor.getColumnIndex("lessee_id"));
            goods_id = cursor.getInt(cursor.getColumnIndex("goods_id"));
            amount = cursor.getInt(cursor.getColumnIndex("amount"));
            way_rcv = cursor.getInt(cursor.getColumnIndex("way_rcv"));
            way_send = cursor.getInt(cursor.getColumnIndex("way_send"));
            stage = cursor.getInt(cursor.getColumnIndex("stage"));
            side = cursor.getInt(cursor.getColumnIndex("side"));
            exp_id_lessor = cursor.getString(cursor.getColumnIndex("exp_id_lessor"));
            exp_id_lessee = cursor.getString(cursor.getColumnIndex("exp_id_lessee"));
            //TODO intialize a complete order object
            if (stage != 6) {
                lessee_unfinished.add(new Order(stage,side));
            } else {
                lessee_finished.add(new Order(stage,side));
            }
        }
        cursor.close();
    }

    public void save_order(int id, int stage, String exp_id_lessee, String exp_id_lessor) {
        if (exp_id_lessee != null) {
            db.execSQL("UPDATE " + DBUtil.orderTable + " SET stage=?,exp_id_lessee=? WHERE _id=?", new Object[]{stage, exp_id_lessee, id});
        } else if (exp_id_lessor != null) {
            db.execSQL("UPDATE " + DBUtil.orderTable + " SET stage=?,exp_id_lessor=? WHERE _id=?", new Object[]{stage, exp_id_lessor, id});
        } else {
            db.execSQL("UPDATE " + DBUtil.orderTable + " SET stage=? WHERE _id=?", new Object[]{stage, id});
        }
    }

    public void new_order(Order order) {
        db.execSQL("INSERT INTO " + DBUtil.orderTable + " VALUES (?,?,?,?,?,?,?,?,?,?,?)", new Object[]{null,null,null,null,null,null,null,null,null,order.getStage(),order.getSide()});
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + DBUtil.orderTable,null);
        if (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            order.setId(id);
        } else {
            Log.e("id", "not found!!!");
        }
    }

    public void saveContact(String uid, String username) {
        Cursor cursor = db.rawQuery("select username from " + DBUtil.contactsTable + " where uid=? limit 1", new String[]{uid});
        if (cursor.moveToNext()) {
            if (!cursor.getString(cursor.getColumnIndex("username")).equals(username))
                db.execSQL("update " + DBUtil.contactsTable + " set username=? where uid=?", new Object[]{username, uid});
        } else {
            db.execSQL("insert into " + DBUtil.contactsTable + " values (?,?)", new Object[]{uid, username});
        }
    }

    public String getUsernameById(String uid) {
        Cursor cursor = db.rawQuery("select username from " + DBUtil.contactsTable + " where uid=?", new String[]{uid});
        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex("username"));
        } else {
            return null;
        }
    }

    public int PortraitInfo(String uid) {
        Cursor cursor = db.rawQuery("select portrait from " + DBUtil.contactsTable + " where uid=?", new String[]{uid});
        if (cursor.moveToNext()) {
            return cursor.getInt(cursor.getColumnIndex("portrait"));
        } else {
            return -1;
        }
    }

    public void savePortrait(String uid) {
        db.execSQL("update " + DBUtil.contactsTable + " set portrait=2 where uid=?", new String[]{uid});
    }

    public void close() {
        db.close();
    }
}
