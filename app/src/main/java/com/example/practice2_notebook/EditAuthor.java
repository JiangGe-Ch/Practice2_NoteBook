package com.example.practice2_notebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditAuthor extends AppCompatActivity {

    private final String TAG="debug";
    private final int TAKE_PHOTO=1;
    private final int CHOOSE_PHOTO=2;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_author);
        SharedPreferences pref=getSharedPreferences("author", MODE_PRIVATE);
        String authorImage=pref.getString("headImage", "");
        String name=pref.getString("name", "");
        String sex=pref.getString("sex", "");
        if(name.equals("")){
            SharedPreferences.Editor editor=pref.edit();
            editor.putString("name", "admin");
            editor.apply();
            name="admin";
        }
        if(sex.equals("")){
            SharedPreferences.Editor editor=pref.edit();
            editor.putString("sex", "男");
            editor.apply();
            sex="男";
        }
        EditText editText_name=(EditText) findViewById(R.id.input_authorName);
        EditText editText_sex=(EditText) findViewById(R.id.input_authorSex);
        ImageView authorImg=(ImageView) findViewById(R.id.authorImage);
        editText_name.setText(name);
        editText_sex.setText(sex);
        authorImg.setImageResource(R.drawable.authorimg_default);
        if(!authorImage.equals("")){
            authorImg.setImageDrawable(ImageUtils.getDecodedImage(authorImage));
        }
        Button bt_submit=(Button) findViewById(R.id.bt_editAuthor_submit);
        Button bt_cancel=(Button) findViewById(R.id.bt_editAuthor_cancel);


        /*
        提交按钮监听器
        保存编辑的作者信息（头像、姓名、性别）到sharedPreference
         */
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=editText_name.getText().toString();
                String sex=editText_sex.getText().toString();
                SharedPreferences.Editor editor=pref.edit();
                editor.putString("headImage", ImageUtils.getEncodedImage(authorImg.getDrawable()));
                editor.putString("name", name);
                editor.putString("sex", sex);
                editor.apply();
                Toast.makeText(EditAuthor.this, "信息已保存", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: name:"+pref.getString("name", ""));
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*
        头像的点击监听器
        调用图片选择对话框选择图片设置头像
         */
        authorImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }


    /*
    打开相机拍照
    */
    private void openCamera(){
        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }


    /*
    打开相册选择图片
     */
    private void openAlbum(){
        Log.d(TAG, "openAlbum: enter");
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }


    /*
    读写权限申请结果处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                /*
                如果成功授予了读写存储权限
                 */
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this, "请授予读写权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: enter");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            /*
            拍照结果返回处理
             */
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        bitmap=Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                        ImageView imageView = (ImageView) findViewById(R.id.authorImage);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
                /*
                相册选择结果返回处理
                 */
            case CHOOSE_PHOTO:
                Log.d(TAG, "onActivityResult: case choose photo");
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult: case choose photo result ok");
                    if (Build.VERSION.SDK_INT >= 19) {
                        Log.d(TAG, "onActivityResult: handleImageOnKitKat  call");
                        handleImageOnKitKat(data);
                    } else {
                        Log.d(TAG, "onActivityResult: handleImageBeforeKitKat call");
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    /*
    图片选择对话框
    从相册选择、相机拍照、取消
     */
    private void showDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(EditAuthor.this);
        View view_choose_dialog= LayoutInflater.from(EditAuthor.this).inflate(R.layout.dialog_choose_picture, null);
        dialog.setContentView(view_choose_dialog);
        dialog.show();
        Button bt_cancel=(Button) view_choose_dialog.findViewById(R.id.choose_cancel);
        Button bt_openAlbum=(Button) view_choose_dialog.findViewById(R.id.choose_from_album);
        Button bt_openCamera=(Button) view_choose_dialog.findViewById(R.id.choose_from_camera);
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                dialog.cancel();
            }
        });
        bt_openAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                if(ContextCompat.checkSelfPermission(EditAuthor.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(EditAuthor.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else{
                    openAlbum();
                }
            }
        });
        bt_openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                File outputImage=new File(getExternalCacheDir(), "output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(EditAuthor.this,"com.example.practice2_notebook.fileprovider",outputImage);
                }else {
                    imageUri=Uri.fromFile(outputImage);
                }
                openCamera();
            }
        });
    }


    /*
    图片路径解析
     */
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        Log.d(TAG, "handleImageOnKitKat: enter");
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this, uri)){
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath=getImagePath(contentUri, null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath=getImagePath(uri, null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath=uri.getPath();
        }
        displayImage(imagePath);
    }

    /*
    老版本图片路径解析
     */
    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri, null);
        displayImage(imagePath);
    }

    /*
    获取图片路径
     */
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection){
        String path=null;
        Cursor cursor=getContentResolver().query(uri, null, selection, null, null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /*
    获取到图片后显示图片
     */
    private void displayImage(String imagePath){
        Log.d(TAG, "displayImage: enter");
        if(imagePath!=null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            bitmap=Bitmap.createScaledBitmap(bitmap, 100, 100, true);
            ImageView imageView=(ImageView) findViewById(R.id.authorImage);
            imageView.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this, "错误！",Toast.LENGTH_SHORT).show();
        }
    }
}