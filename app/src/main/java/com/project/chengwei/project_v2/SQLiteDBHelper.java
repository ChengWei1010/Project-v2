package com.project.chengwei.project_v2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by chengwei on 2017/7/6.
 */

public class SQLiteDBHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "profile_database";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "profile_tbl";
    private final static String FEILD_ID = "_id";
    private final static String FEILD_NAME = "name";
    private final static String FEILD_PHONE = "phone";
    private final static String FEILD_ADDRESS = "address";
    private final static String FEILD_BIRTHDAY = "birthday";
    private static final String IMAGE = "image";

    public SQLiteDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sql_createTbl);
        db.execSQL(sql_firstRow);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        Log.w(SQLiteDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);
    }

    //SQL : create a table
    private String sql_createTbl;{
        sql_createTbl = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(" + FEILD_ID + " INTEGER PRIMARY KEY autoincrement, " +
                FEILD_NAME + " TEXT, " +
                FEILD_PHONE + " TEXT, " +
                FEILD_ADDRESS + " TEXT, " +
                FEILD_BIRTHDAY + " TEXT, " +
                IMAGE + " BLOB )";
    }
    //SQL : create first row
    byte[] ByteExample = "abc".getBytes();
    private String sql_firstRow;{
        sql_firstRow = ("INSERT INTO " + TABLE_NAME + " (name, phone, address, birthday,image) VALUES ('姓名', '電話', '住址', '2000-01-01', '"+ByteExample+"')");
    }

    //Database : updateProfileData to the table
    public long editProfileData(String name, String phone, String address, String birthday){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FEILD_NAME, name);
        cv.put(FEILD_PHONE, phone);
        cv.put(FEILD_ADDRESS, address);
        cv.put(FEILD_BIRTHDAY, birthday);

        return db.update(TABLE_NAME, cv, "_id=1", null);
    }

    //Database : cursor pointer to the table
    public Cursor getProfileData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[] {FEILD_NAME, FEILD_PHONE, FEILD_ADDRESS, FEILD_BIRTHDAY};
        Cursor mCursor = db.query(true,TABLE_NAME,cols, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }

    //-------------------------------------------------------------------------------------//
    //---------------------------------About Profile Photo---------------------------------//
    //-------------------------------------------------------------------------------------//

    // Database : insertImage
    public void addImageByte( byte[] imageBytes) throws SQLiteException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(IMAGE, imageBytes);

        db.update(TABLE_NAME, cv, "_id=1", null);
    }
    // Database : retrieve image from database
    //We will just get the last image we just saved for convenience...

    public byte[] retrieveImageFromDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(true, TABLE_NAME, new String[]{IMAGE,}, null, null, null, null, null + " DESC", "1");
        if (cur.moveToFirst()) {
            byte[] blob = cur.getBlob(cur.getColumnIndex(IMAGE));
            cur.close();
            return blob;
        }
        cur.close();
        return null;
    }

    //-------------------------------------------------------------------------------------//
    //-----------------------------------Profile Photo End---------------------------------//
    //-------------------------------------------------------------------------------------//

    //Database : insert to the table
//    public long insertProfileData(String name, String phone, String address, String birthday)
//    {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(FEILD_NAME, name);
//        cv.put(FEILD_PHONE, phone);
//        cv.put(FEILD_ADDRESS, address);
//        cv.put(FEILD_BIRTHDAY, birthday);
//
//        return db.insert(TABLE_NAME, null, cv);
//        //Log.d("success insert:", name + "," + phone + "," + address + "," + birthday);
//    }

    //-------------------------------------------------------------------------------------//
    //--------------------------------------About Contacts---------------------------------//
    //-------------------------------------------------------------------------------------//
    public void queryContactData(String sql){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public void insertContactData(String name, String phone, String image){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO PERSON VALUES (NULL, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1, name);
        statement.bindString(2, phone);
        statement.bindString(3, image);

        statement.executeInsert();
        database.close();
    }

    public void updateContactData(String name, String phone, String image, int id) {
        SQLiteDatabase database = getWritableDatabase();

        String sql = "UPDATE PERSON SET name = ?, phone = ?, image = ? WHERE id = ?";
        SQLiteStatement statement = database.compileStatement(sql);

        statement.bindString(1, name);
        statement.bindString(2, phone);
        statement.bindString(3, image);
        statement.bindDouble(4, (double)id);

        statement.execute();
        database.close();
    }

    public  void deleteContactData(int id) {
        SQLiteDatabase database = getWritableDatabase();

        String sql = "DELETE FROM PERSON WHERE id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1, (double)id);

        statement.execute();
        database.close();
    }

    public Cursor getContactData(String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql, null);
    }
    //The database manager tool for android from github
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
    public void close(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.close();
    }
}
