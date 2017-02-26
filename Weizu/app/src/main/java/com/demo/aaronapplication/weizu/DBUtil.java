package com.demo.aaronapplication.weizu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Aaron on 2016/3/23.
 */
public class DBUtil extends SQLiteOpenHelper {

    private static final String DBName = "Weizu.db";
    public static final String search_historyTable = "search_history", addressTable="addresses", releaseTable = "releases",
                            orderTable = "orders", expressTable = "expresses", contactsTable = "contacts";
    private static final int Version = 1;

    public DBUtil(Context context) {
        super(context, DBName, null, Version);
    }

    /** onCreate will be called when the getreadabledatabase() or getwritabledatabase() is called while the db does not exist, otherwise not **/
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + search_historyTable +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "key VARCHAR, " +
                "time INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + releaseTable +
                "(gid INTEGER, " +
                "title VARCHAR, " +
                "desc VARCHAR, " +
                "category INTEGER, " +
                "childtype INTEGER, " +
                "rent REAL, " +
                "deposit REAL, " +
                "freight REAL, " +
                "contact VARCHAR, " +
                "location VARCHAR, " +
                "leaser INTEGER, " +
                "sales INTEGER," +
                "date INTEGER," +
                "picnum INTEGER," +
                "coverindex INTEGER," +
                "period INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + orderTable +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lessor_id INTEGER, " +
                "lessee_id INTEGER, " +
                "goods_id INTEGER, " +
                "amount INTEGER, " +
                "way_rcv INTEGER, " +
                "way_send INTEGER, " +
                "exp_id_lessor VARVHAR, " +
                "exp_id_lessee VARCHAR, " +
                "stage INTEGER, " +
                "side INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + expressTable +
                "(expno VARCHAR," +
                "lastQueryTime INTEGER," +
                "info VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + contactsTable +
                "(uid VARCHAR," +
                "username VARCHAR," +
                "portrait INTEGER DEFAULT 1)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("ALTER TABLE events ADD COLUMN other STRING");
    }
}
