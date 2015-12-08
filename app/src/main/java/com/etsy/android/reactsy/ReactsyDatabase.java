package com.etsy.android.reactsy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mhorowitz on 8/26/14.
 */
public class ReactsyDatabase extends SQLiteOpenHelper {
    protected static final String DB_NAME = "ReactsyDatabase";
    protected static final int DB_VERSION = 1;

    public ReactsyDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ReactsyTables.TestInstance.SQL_CREATE);
        db.execSQL(ReactsyTables.TrialResults.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing yet; no upgrades
    }
}
