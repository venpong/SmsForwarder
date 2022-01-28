package com.idormy.sms.forwarder.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.idormy.sms.forwarder.model.LogTable;
import com.idormy.sms.forwarder.model.RuleTable;
import com.idormy.sms.forwarder.model.SenderTable;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final String TAG = "DbHelper";
    public static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "sms_forwarder.db";

    private static final List<String> SQL_CREATE_ENTRIES =
            Arrays.asList(
                    "CREATE TABLE " + LogTable.LogEntry.TABLE_NAME + " (" +
                            LogTable.LogEntry._ID + " INTEGER PRIMARY KEY," +
                            LogTable.LogEntry.COLUMN_NAME_TYPE + " TEXT NOT NULL DEFAULT 'sms'," +
                            LogTable.LogEntry.COLUMN_NAME_FROM + " TEXT," +
                            LogTable.LogEntry.COLUMN_NAME_CONTENT + " TEXT," +
                            LogTable.LogEntry.COLUMN_NAME_SIM_INFO + " TEXT," +
                            LogTable.LogEntry.COLUMN_NAME_RULE_ID + " INTEGER," +
                            LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS + " INTEGER NOT NULL DEFAULT 1," +
                            LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE + " TEXT NOT NULL DEFAULT 'ok'," +
                            LogTable.LogEntry.COLUMN_NAME_TIME + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)"
                    , "CREATE TABLE " + RuleTable.RuleEntry.TABLE_NAME + " (" +
                            RuleTable.RuleEntry._ID + " INTEGER PRIMARY KEY," +
                            RuleTable.RuleEntry.COLUMN_NAME_TYPE + " TEXT NOT NULL DEFAULT 'sms'," +
                            RuleTable.RuleEntry.COLUMN_NAME_FILED + " TEXT," +
                            RuleTable.RuleEntry.COLUMN_NAME_CHECK + " TEXT," +
                            RuleTable.RuleEntry.COLUMN_NAME_VALUE + " TEXT," +
                            RuleTable.RuleEntry.COLUMN_NAME_SENDER_ID + " INTEGER," +
                            RuleTable.RuleEntry.COLUMN_NAME_TIME + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            RuleTable.RuleEntry.COLUMN_SMS_TEMPLATE + " TEXT NOT NULL DEFAULT ''," +
                            RuleTable.RuleEntry.COLUMN_REGEX_REPLACE + " TEXT NOT NULL DEFAULT ''," +
                            RuleTable.RuleEntry.COLUMN_NAME_STATUS + " INTEGER NOT NULL DEFAULT 1," +
                            RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT + " TEXT NOT NULL DEFAULT 'ALL')"
                    , "CREATE TABLE " + SenderTable.SenderEntry.TABLE_NAME + " (" +
                            SenderTable.SenderEntry._ID + " INTEGER PRIMARY KEY," +
                            SenderTable.SenderEntry.COLUMN_NAME_NAME + " TEXT," +
                            SenderTable.SenderEntry.COLUMN_NAME_STATUS + " INTEGER NOT NULL DEFAULT 1," +
                            SenderTable.SenderEntry.COLUMN_NAME_TYPE + " INTEGER NOT NULL DEFAULT 1," +
                            SenderTable.SenderEntry.COLUMN_NAME_JSON_SETTING + " TEXT," +
                            SenderTable.SenderEntry.COLUMN_NAME_TIME + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)"
            );

    private static final List<String> SQL_DELETE_ENTRIES =
            Arrays.asList(
                    "DROP TABLE IF EXISTS " + LogTable.LogEntry.TABLE_NAME + " ; "
                    , "DROP TABLE IF EXISTS " + RuleTable.RuleEntry.TABLE_NAME + " ; "
                    , "DROP TABLE IF EXISTS " + SenderTable.SenderEntry.TABLE_NAME + " ; "

            );


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        for (String createEntries : SQL_CREATE_ENTRIES
        ) {
            Log.d(TAG, "onCreate:createEntries " + createEntries);
            db.execSQL(createEntries);
        }
    }

    public void delCreateTable(SQLiteDatabase db) {
        for (String delCreateEntries : SQL_DELETE_ENTRIES
        ) {
            Log.d(TAG, "delCreateTable:delCreateEntries " + delCreateEntries);
            db.execSQL(delCreateEntries);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) { //转发日志添加SIM卡槽信息
            String sql = "Alter table " + LogTable.LogEntry.TABLE_NAME + " add column " + LogTable.LogEntry.COLUMN_NAME_SIM_INFO + " TEXT ";
            db.execSQL(sql);
        }
        if (oldVersion < 3) { //转发规则添加SIM卡槽信息
            String sql = "Alter table " + RuleTable.RuleEntry.TABLE_NAME + " add column " + RuleTable.RuleEntry.COLUMN_NAME_SIM_SLOT + " TEXT NOT NULL DEFAULT 'ALL' ";
            db.execSQL(sql);
        }
        if (oldVersion < 4) { //转发日志添加转发状态与返回信息
            String sql = "Alter table " + LogTable.LogEntry.TABLE_NAME + " add column " + LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS + " INTEGER NOT NULL DEFAULT 1 ";
            db.execSQL(sql);
            sql = "Alter table " + LogTable.LogEntry.TABLE_NAME + " add column " + LogTable.LogEntry.COLUMN_NAME_FORWARD_RESPONSE + " TEXT NOT NULL DEFAULT 'ok' ";
            db.execSQL(sql);
        }
        if (oldVersion < 5) { //转发规则添加规则自定义信息模板
            String sql = "Alter table " + RuleTable.RuleEntry.TABLE_NAME + " add column " + RuleTable.RuleEntry.COLUMN_SMS_TEMPLATE + " TEXT NOT NULL DEFAULT '' ";
            db.execSQL(sql);
        }
        if (oldVersion < 6) { //增加转发规则与日志的分类
            String sql = "Alter table " + RuleTable.RuleEntry.TABLE_NAME + " add column " + RuleTable.RuleEntry.COLUMN_NAME_TYPE + " TEXT NOT NULL DEFAULT 'sms' ";
            db.execSQL(sql);
            sql = "Alter table " + LogTable.LogEntry.TABLE_NAME + " add column " + RuleTable.RuleEntry.COLUMN_NAME_TYPE + " TEXT NOT NULL DEFAULT 'sms' ";
            db.execSQL(sql);
        }
        if (oldVersion < 7) { //转发规则添加正则替换内容
            String sql = "Alter table " + RuleTable.RuleEntry.TABLE_NAME + " add column " + RuleTable.RuleEntry.COLUMN_REGEX_REPLACE + " TEXT NOT NULL DEFAULT '' ";
            db.execSQL(sql);
        }
        if (oldVersion < 8) { //更新日志表状态：0=失败，1=待处理，2=成功
            String sql = "update " + LogTable.LogEntry.TABLE_NAME + " set " + LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS + " = 2 where " + LogTable.LogEntry.COLUMN_NAME_FORWARD_STATUS + " = 1 ";
            db.execSQL(sql);
        }
        if (oldVersion < 9) { //规则/通道状态：0=禁用，1=启用
            String sql = "Alter table " + RuleTable.RuleEntry.TABLE_NAME + " add column " + RuleTable.RuleEntry.COLUMN_NAME_STATUS + " INTEGER NOT NULL DEFAULT 1 ";
            db.execSQL(sql);
            sql = "update " + SenderTable.SenderEntry.TABLE_NAME + " set " + SenderTable.SenderEntry.COLUMN_NAME_STATUS + " = 1 ";
            db.execSQL(sql);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
