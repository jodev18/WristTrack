package com.khsoftsolutions.wristtrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.khsoftsolutions.wristtrack.objects.ParentObject;

/**
 * Created by myxroft2 on 8/13/17.
 */

public class ParentManager extends LocationDB {

    private SQLiteDatabase sq;
    private Context ctx;
    private Cursor c;

    private ContentValues cv;

    public ParentManager(Context ct) {
        super(ct);

        this.sq = getWritableDatabase();
        this.ctx = ct;
        this.cv = new ContentValues();
    }

    public ParentObject getParentInfo(){

        String q = "SELECT * FROM " + ParentTable.TABLE_NAME;

        this.c = this.sq.rawQuery(q,null);

        if(this.c.getCount() > 0){

            ParentObject parentObject = new ParentObject();

            while(c.moveToNext()){
                parentObject.PARENT_ADDRESS
                        = c.getString(c.getColumnIndex(ParentTable.PARENT_ADDRESS));
                parentObject.PARENT_AGE = c.getString(c.getColumnIndex(ParentTable.PARENT_AGE));
                parentObject.PARENT_FIRST_NAME = c.getString(c.getColumnIndex(ParentTable.PARENT_FIRST_NAME));
                parentObject.PARENT_MID_NAME = c.getString(c.getColumnIndex(ParentTable.PARENT_MIDDLE_NAME));
                parentObject.PARENT_LAST_NAME = c.getString(c.getColumnIndex(ParentTable.PARENT_LAST_NAME));
                parentObject.PARENT_PHONE_NUMBER = c.getString(c.getColumnIndex(ParentTable.PARENT_CONTACT_NUMBER));
            }

            return parentObject;
        }
        else{
            return null;
        }
    }

    public long insertParent(ParentObject parent){

        //clear previous items
        this.cv.clear();

        this.cv.put(ParentTable.PARENT_ADDRESS, parent.PARENT_ADDRESS);
        this.cv.put(ParentTable.PARENT_AGE, parent.PARENT_AGE);
        this.cv.put(ParentTable.PARENT_CONTACT_NUMBER, parent.PARENT_PHONE_NUMBER);
        this.cv.put(ParentTable.PARENT_FIRST_NAME,parent.PARENT_FIRST_NAME);
        this.cv.put(ParentTable.PARENT_LAST_NAME,parent.PARENT_LAST_NAME);
        this.cv.put(ParentTable.PARENT_MIDDLE_NAME,parent.PARENT_MID_NAME);

        return this.sq.insert(ParentTable.TABLE_NAME,ParentTable.ID,this.cv);
    }

    public void cleanUp(){

        this.cv.clear();

        if(this.sq != null){
            if(this.sq.isOpen()){
                this.sq.close();
            }
        }


        if(this.c != null){
            if(!this.c.isClosed()){
                this.c.close();
            }
        }

        this.c = null;
        this.sq = null;
        this.c = null;
    }



}
