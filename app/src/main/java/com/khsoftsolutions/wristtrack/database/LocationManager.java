package com.khsoftsolutions.wristtrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by myxroft2 on 8/13/17.
 */

public class LocationManager extends LocationDB {

    private SQLiteDatabase sq;
    private Context ctx;
    private Cursor c;

    private ContentValues cv;

    public LocationManager(Context ct) {
        super(ct);

        this.ctx = ct;
        this.sq = getWritableDatabase();
    }
}
