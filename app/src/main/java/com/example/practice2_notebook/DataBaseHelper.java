package com.example.practice2_notebook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String TAG="debug";

    public static final String CREATE_TABLE="create table notebook(" +
            "id integer primary key autoincrement," +
            "title text," +
            "author text," +
            "cdate text," +
            "content text,"+
            "images text)";
    private Context mContext;

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        Log.d(TAG, "DataBaseHelper: construct");
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: DatabaseHelper");
        try{
            db.execSQL(CREATE_TABLE);
        }catch (Exception e){
            Log.d(TAG, "onCreate: 数据库已存在！");
        }
        Toast.makeText(mContext, "数据库创建成功！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
