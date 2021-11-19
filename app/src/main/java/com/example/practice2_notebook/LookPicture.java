package com.example.practice2_notebook;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.List;


/*
放大查看图片
 */
public class LookPicture extends AppCompatActivity {
    final String TAG="debug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_picture);
        Intent intent=getIntent();
        String imagePath=intent.getStringExtra("FileDir");
        Log.d(TAG, "onCreate: FileDir:"+imagePath);
        Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
        ImageView imageView=(ImageView) findViewById(R.id.look_picture_picture);
        imageView.setImageBitmap(bitmap);

        Button bt_delete_picture=(Button) findViewById(R.id.look_picture_delete);
        bt_delete_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(LookPicture.this);
                dialog.setTitle("注意！");
                dialog.setMessage("确定删除该图片吗？");
                dialog.setCancelable(true);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            File file=new File(imagePath);
                            file.delete();
                        }catch (Exception e){
                            Toast.makeText(LookPicture.this, "删除失败", Toast.LENGTH_SHORT).show();
                            Log.d("debug", "onClick: delete error---"+e.getMessage());
                        }
                        finish();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
    }
}