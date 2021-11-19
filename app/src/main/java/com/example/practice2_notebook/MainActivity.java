package com.example.practice2_notebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<ListItem> itemList=new ArrayList<>();
    SQLiteDatabase db;
    ItemAdapter adapter;

    final String TAG="debug";


    /*
    主活动右上角菜单选择动作
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.editAuthor:Intent intent=new Intent(MainActivity.this, EditAuthor.class);
            startActivity(intent);break;
            case R.id.quit:finish();break;
//            case R.id.quit:Intent intent2=new Intent(MainActivity.this, LookUpNote.class);
//            intent2.putExtra("id", "1");
//            startActivity(intent2);
            case R.id.addNote:Intent intent1=new Intent(MainActivity.this, EditNote.class);
            startActivity(intent1);break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    添加右上角菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        创建数据库操作对象
         */
        DataBaseHelper dbHelper=new DataBaseHelper(MainActivity.this, "noteBook.db", null, 1);
        db=dbHelper.getReadableDatabase();

        this.initList();
        TextView text_tips=(TextView) findViewById(R.id.text_tips);
        if(itemList.isEmpty()){
            text_tips.setVisibility(View.VISIBLE);
            Log.d(TAG, "onCreate: itemList is empty");
        }else {
            text_tips.setVisibility(View.INVISIBLE);
            Log.d(TAG, "onCreate: itemList is not empty");
        }
        adapter=new ItemAdapter(MainActivity.this, R.layout.diary_listitem, itemList);
        ListView listView=(ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        /*
        设置列表项点击的监听器
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item=itemList.get(position);
                String itemId=item.getId();
                Intent intent=new Intent(MainActivity.this, LookNote.class);
                intent.putExtra("id", itemId);
                startActivity(intent);
            }
        });

    }

    /*
    当活动重新开始运行时刷新列表
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
        itemList.clear();
        this.initList();
        TextView text_tips=(TextView) findViewById(R.id.text_tips);
        if(itemList.isEmpty()){
            text_tips.setVisibility(View.VISIBLE);
            Log.d(TAG, "onRestart: itemList is empty");
        }else {
            text_tips.setVisibility(View.GONE);
            Log.d(TAG, "onRestart: itemList is not empty");
        }
        adapter.notifyDataSetChanged();
    }


    /*
    活动重新可交互时刷新列表
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onPostResume: ");
        itemList.clear();
        this.initList();
        TextView text_tips=(TextView) findViewById(R.id.text_tips);
        if(itemList.isEmpty()){
            text_tips.setVisibility(View.VISIBLE);
            Log.d(TAG, "onPostResume: itemList is empty");
        }else {
            Log.d(TAG, "onPostResume: itemList is not empty");
            text_tips.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    protected void initList(){
        /*
        根据数据库数据实例化列表
         */
        ListItem item;
        Cursor cursor=db.query("notebook", null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                @SuppressLint("Range") String id=cursor.getString(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String title=cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range") String author=cursor.getString(cursor.getColumnIndex("author"));
                @SuppressLint("Range") String date=cursor.getString(cursor.getColumnIndex("cdate"));
                @SuppressLint("Range") String content=cursor.getString(cursor.getColumnIndex("content"));
                item=new ListItem(id, title, author, date, content);
                itemList.add(item);
            }while (cursor.moveToNext());
        }

//        final String title="test title";
//        final String author="test author";
//        final String date="test date";
//        final String content="test content dfaasdgdasndakfjdasghuerhundknjakdskfjghdshkacn";
//        SharedPreferences pref=getSharedPreferences("author", MODE_PRIVATE);
//        for (int i=0;i<20;i++){
//            item=new ListItem(String.valueOf(i), title, pref.getString("name", "null"), date, content);
//            itemList.add(item);
//        }
    }
}