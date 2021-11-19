package com.example.practice2_notebook;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class LookNote extends AppCompatActivity {
    SQLiteDatabase db;
    ContentImgAdapter adapter;
    List<ContentImgItem> list=new ArrayList<>();
    private String[] imageStrs;
    private String id;

    public static final String TAG="debug";

    @Override
    protected void onPostResume() {
        super.onPostResume();
        list.clear();
        initContentImg();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        list.clear();
        initContentImg();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: look");
        setContentView(R.layout.activity_look_up_note);
        Intent intent=getIntent();
        id=intent.getStringExtra("id");
//        String id="1";
                /*
        图片列表
         */
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.look_recycleview_content_img);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new ContentImgAdapter(list);
        adapter.setOnItemClickListener(new ContentImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: itemClick");
                ContentImgItem imgItem=list.get(position);
                String fileDir=imgItem.getFileDir();
                Intent intent1=new Intent(LookNote.this, LookPicture.class);
                intent1.putExtra("FileDir", fileDir);
                Log.d(TAG, "onItemClick: startActivity  fileDir="+fileDir);
                startActivity(intent1);
            }
        });
        recyclerView.setAdapter(adapter);


        DataBaseHelper dbHelper=new DataBaseHelper(LookNote.this, "noteBook.db", null, 1);
        db=dbHelper.getReadableDatabase();
        initContentImg();

        /*
        “删除”按钮的监听器
         */
        Button bt_delete=(Button) findViewById(R.id.bt_look_note_delete);
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(LookNote.this);
                dialog.setTitle("请注意！！！");
                dialog.setMessage("确定要删除此条笔记吗？");
                dialog.setCancelable(true);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try{
                            db.delete("notebook", "id=?", new String[]{id});
                            /*
                            删除笔记数据库记录时同时删除存储的内容图片
                             */
                            for(String image:imageStrs){
                                File file=new File(getFilesDir()+"/"+image);
                                Log.d(TAG, "onClick: getFileDir+image: "+getFilesDir()+image);
                                file.delete();
                            }
                        }catch (Exception e){
                            Toast.makeText(LookNote.this, "删除出错！", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(LookNote.this, "删除成功！", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        /*
        编辑按钮监听器
         */
        Button bt_edit=(Button) findViewById(R.id.bt_look_note_edit);
        bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(LookNote.this, EditNote.class);
                intent1.putExtra("edit", true);
                intent1.putExtra("id", id);
                startActivity(intent1);
                finish();
            }
        });
    }

    /*
为列表适配器添加数据项
 */
    private void addContentImg(Bitmap bitmap, String fileName){
        String fileDir=getFilesDir()+"/"+fileName;
        ContentImgItem item=new ContentImgItem(bitmap, fileDir);
        list.add(item);
        Log.d(TAG, "addContentImg: success!");
    }

    /*
    初始化内容图片设配器数据
     */
    private void initContentImg(){
        Cursor cursor=db.query("notebook", null, "id="+id, null, null, null, null);
        if(cursor.moveToFirst()){
            @SuppressLint("Range") String title=cursor.getString(cursor.getColumnIndex("title"));
            @SuppressLint("Range") String content=cursor.getString(cursor.getColumnIndex("content"));
            TextView titleView=(TextView) findViewById(R.id.look_text_content_title);
            TextView contentView=(TextView) findViewById(R.id.look_text_content);
            CharSequence titleCs=title;
            CharSequence contentCs=content;
            titleView.setText(titleCs);
            contentView.setText(contentCs);
            @SuppressLint("Range") String images=cursor.getString(cursor.getColumnIndex("images"));
            imageStrs=images.split("/");
            FileInputStream is=null;
            for(String image:imageStrs){
                try{
                    is=openFileInput(image);
                    Bitmap bitmap= BitmapFactory.decodeStream(is);
                    addContentImg(bitmap, image);
                }catch (Exception e){
                    Log.d(TAG, "onCreate: "+e.getMessage());
                }
            }
                        /*
            通知适配器数据更新
             */
            adapter.notifyDataSetChanged();
        }
    }
}