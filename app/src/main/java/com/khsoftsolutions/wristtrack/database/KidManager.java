package com.khsoftsolutions.wristtrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.khsoftsolutions.wristtrack.objects.ChildObject;

/**
 * Created by myxroft2 on 8/13/17.
 */

public class KidManager extends LocationDB {

    private SQLiteDatabase sq;
    private Context ctx;
    private Cursor c;

    private ContentValues cv;

    public KidManager(Context ct) {
        super(ct);

        this.sq = getWritableDatabase();
        this.cv = new ContentValues();
        this.ctx = ct;
    }

    public ChildObject getChildInfo(){

        String sql =  "SELECT * FROM " + KidTable.TABLE_NAME;

        this.c = this.sq.rawQuery(sql,null);

        if(this.c.getCount() > 0){

            ChildObject childObject = new ChildObject();

            while(c.moveToNext()){

                childObject.CHILD_ADDRESS = c.getString(c.getColumnIndex(KidTable.KID_ADDRESS1));
                childObject.CHILD_AGE = c.getString(c.getColumnIndex(KidTable.KID_AGE));
                childObject.CHILD_FIRST_NAME = c.getString(c.getColumnIndex(KidTable.KID_FIRST_NAME));
                childObject.CHILD_MIDDLE_NAME = c.getString(c.getColumnIndex(KidTable.KID_MIDDLE_NAME));
                childObject.CHILD_LAST_NAME = c.getString(c.getColumnIndex(KidTable.KID_LAST_NAME));
                childObject.CHILD_GENDER = c.getString(c.getColumnIndex(KidTable.KID_GENDER));
                childObject.OBJECT_ID = c.getString(c.getColumnIndex(KidTable.KID_OBJ_ID));

            }

            return childObject;

        }
        else{
            return null;
        }
    }

    public long addChildInfo(ChildObject child){

        //clear first entries, just in case
        this.cv.clear();

        this.cv.put(KidTable.KID_ADDRESS1,child.CHILD_ADDRESS);
        this.cv.put(KidTable.KID_AGE,child.CHILD_AGE);
        this.cv.put(KidTable.KID_FIRST_NAME,child.CHILD_FIRST_NAME);
        this.cv.put(KidTable.KID_MIDDLE_NAME,child.CHILD_MIDDLE_NAME);
        this.cv.put(KidTable.KID_LAST_NAME,child.CHILD_LAST_NAME);
        this.cv.put(KidTable.KID_GENDER,child.CHILD_GENDER);
        this.cv.put(KidTable.KID_OBJ_ID,child.OBJECT_ID);

        return this.sq.insert(KidTable.TABLE_NAME,KidTable.ID,this.cv);

    }

    public void cleanUp(){

        if(this.sq != null){
            if(this.sq.isOpen()){
                this.sq.close();
            }
        }

        if(this.cv != null){
            this.cv.clear();
        }
    }
}
