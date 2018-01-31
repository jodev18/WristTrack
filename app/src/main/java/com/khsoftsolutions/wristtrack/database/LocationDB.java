package com.khsoftsolutions.wristtrack.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by myxroft2 on 8/12/17.
 */

public class LocationDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "LocationData.db";
    private static final int VERSION = 1;

    public LocationDB(Context ct){
        super(ct,DB_NAME,null,VERSION);
    }

    /**
     * Database Schema
     *
     * Locally stores all coordinates.
     */
    protected class LocationTable{

        public static final String TABLE_NAME = "tbl_location";

        public static final String ID = "loc_id";

        public static final String LOC_LAT = "loc_lat";

        public static final String LOC_LONG = "loc_long";

        public static final String LOC_TIMESTAMP = "loc_time";

        public static final String TABLE_CREATE = "CREATE TABLE " +  TABLE_NAME
                + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LOC_LAT + " TEXT,"
                + LOC_LONG + " TEXT,"
                + LOC_TIMESTAMP + " TEXT);";

    }

    /**
     * Basic kid information.
     *
     * For Identification, in case.
     */
    protected class KidTable{

        public static final String TABLE_NAME = "tbl_kid_info";

        public static final String ID = "kid_id";

        public static final String KID_FIRST_NAME = "kid_first_name";

        public static final String KID_MIDDLE_NAME = "kid_middle_name";

        public static final String KID_LAST_NAME = "kid_last_name";

        public static final String KID_AGE = "kid_age";

        public static final String KID_ADDRESS1 = "kid_address_1";

        //public static final String KID_ADDRESS2 = "kid_address_2";

        public static final String KID_GENDER = "kid_gender";

        public static final String KID_OBJ_ID = "kid_object_id";

        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
                + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KID_FIRST_NAME + " TEXT NOT NULL,"
                + KID_MIDDLE_NAME + " TEXT NOT NULL,"
                + KID_LAST_NAME + " TEXT NOT NULL,"
                + KID_AGE + " TEXT NOT NULL,"
                + KID_ADDRESS1 + " TEXT NOT NULL,"
                + KID_OBJ_ID + " TEXT NOT NULL,"
                //+ KID_ADDRESS2 + " TEXT NOT NULL,"
                + KID_GENDER + " TEXT NOT NULL);";

    }

    /**
     * Stores parent information.
     *
     * In case something happens.
     */
    protected class ParentTable{

        public static final String TABLE_NAME = "tbl_parent_info";

        public static final String ID = "parent_id";

        public static final String PARENT_FIRST_NAME = "parent_first_name";

        public static final String PARENT_MIDDLE_NAME = "parent_middle_name";

        public static final String PARENT_LAST_NAME = "parent_last_name";

        public static final String PARENT_AGE = "parent_age";

        //public static final String PARENT_POSITION = "parent_position";

        public static final String PARENT_CONTACT_NUMBER = "parent_contact_number";

        public static final String PARENT_ADDRESS = "parent_address";

        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
                + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PARENT_FIRST_NAME + " TEXT NOT NULL,"
                + PARENT_MIDDLE_NAME + " TEXT NOT NULL,"
                + PARENT_LAST_NAME + " TEXT NOT NULL,"
                + PARENT_AGE + " TEXT NOT NULL,"
                //+ PARENT_POSITION + " TEXT NOT NULL,"
                + PARENT_ADDRESS + " TEXT NOT NULL,"
                + PARENT_CONTACT_NUMBER + " TEXT NOT NULL);";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(LocationTable.TABLE_CREATE);
        sqLiteDatabase.execSQL(KidTable.TABLE_CREATE);
        sqLiteDatabase.execSQL(ParentTable.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Do nothing yet
    }
}
