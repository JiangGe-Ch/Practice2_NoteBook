package com.example.practice2_notebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class EditNote extends AppCompatActivity {
    final String TAG="debug";

    private final int TAKE_PHOTO=1;
    private final int CHOOSE_PHOTO=2;

    Uri imageUri;

    List<ContentImgItem> list=new ArrayList<>();

    ContentImgAdapter adapter;
    SQLiteDatabase db;

    /*
    是否进行过编辑的标记
     */
    private boolean isEdit;

    /*
    是否由LookNote活动的”编辑“按钮启动的标记
     */
    private boolean edit;

    /*
    用于从LookNote活动“编辑”按钮启动时，存储当前编辑的笔记记录对应数据库的id
     */
    private String id;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: EditNote");
        setContentView(R.layout.activity_edit_note);
        Intent intent=getIntent();
        edit=intent.getBooleanExtra("edit", false);
        id=intent.getStringExtra("id");
        isEdit=false;
        DataBaseHelper dbHelper=new DataBaseHelper(EditNote.this, "noteBook.db", null, 1);
        db=dbHelper.getReadableDatabase();
        String title="";
        String content="";
        EditText titleView=(EditText) findViewById(R.id.text_content_title);
        EditText contentView=(EditText) findViewById(R.id.text_content);
        Log.d(TAG, "onCreate: edit:"+edit+" id:"+id);
        /*
        如果是从LookNote“编辑”按钮启动，则从数据库中查询对应记录，初始化UI数据，以供编辑
         */
        if(edit){
            Cursor cursor=db.query("notebook", null, "id="+id, null, null,null, null);
            if(cursor.moveToFirst()){
                title=cursor.getString(cursor.getColumnIndex("title"));
                content=cursor.getString(cursor.getColumnIndex("content"));
                CharSequence titleCs=title;
                CharSequence contentCs=content;
                titleView.setText(titleCs);
                contentView.setText(contentCs);
                String imageStrs=cursor.getString(cursor.getColumnIndex("images"));
                String[] images=imageStrs.split("/");
                FileInputStream is=null;
                for(String image:images){
                    try{
                        is=openFileInput(image);
                        Bitmap bitmap= BitmapFactory.decodeStream(is);
                        addContentImg(bitmap, image);
                    }catch (Exception e){
                        Log.d(TAG, "onCreate: "+e.getMessage());
                    }
                }
            }
        }

        Log.d(TAG, "onCreate: NewNote");

        /*
        图片列表（横向滚动）
         */
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.recycleview_content_img);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new ContentImgAdapter(list);
        recyclerView.setAdapter(adapter);

        /*
        取消提示动作
         */
        Button bt_cancel=(Button) findViewById(R.id.bt_addNote_cancel);
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(EditNote.this);
                dialog.setTitle("请注意！！！");
                dialog.setMessage("您所做的修改将不会被保存！");
                dialog.setCancelable(true);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
        保存提示动作
         */
        Button bt_ok=(Button) findViewById(R.id.bt_addNote_ok);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(EditNote.this);
                dialog.setTitle("注意！");
                dialog.setMessage("确定保存吗？");
                dialog.setCancelable(true);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TO_DO确定保存
                        List<String> names=saveContentImage(list);
                        saveNote(names);
                        isEdit=false;
                        Toast.makeText(EditNote.this, "保存成功！", Toast.LENGTH_SHORT).show();
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

        Button bt_addImage=(Button) findViewById(R.id.bt_addNote_addImg);
        bt_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });


        /*
        编辑框获得焦点时更改是否编辑过的标志
         */
        titleView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!isEdit){
                    isEdit=true;
                    Log.d(TAG, "title onFocusChange: isEdit="+isEdit);
                }
            }
        });
        contentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!isEdit){
                    isEdit=true;
                    Log.d(TAG, "contentView onFocusChange: isEdit="+isEdit);
                }
            }
        });
    }

    /*
    按下“返回”按钮时，根据isEdit标志做出对应提示或操作
     */
    @Override
    public void onBackPressed() {
        /*
        如果进行过编辑
         */
        if(isEdit){
            AlertDialog.Builder dialog=new AlertDialog.Builder(EditNote.this);
            dialog.setTitle("注意！");
            dialog.setMessage("您所做的更改将不会被保存！！！");
            dialog.setCancelable(true);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditNote.super.onBackPressed();
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }else {
            super.onBackPressed();
        }
    }

    /*
        保存笔记内容到数据库
         */
    private void saveNote(List<String> names){
        Log.d(TAG, "saveNote: enter");
        EditText text_title=(EditText) findViewById(R.id.text_content_title);
        EditText text_content=(EditText) findViewById(R.id.text_content);
        String title=text_title.getText().toString();
        String content=text_content.getText().toString();
        SharedPreferences pref=getSharedPreferences("author", MODE_PRIVATE);
        /*
        根据图片存储的名字、日期，生成唯一的图片存储名称
         */
        String name=pref.getString("name", "admin");
        Date date=new Date(System.currentTimeMillis());
        String dateStr=date.toString();
        String images="";
        for(String Imgname:names){
            images=images+"/"+Imgname;
        }
        try{
            Log.d(TAG, "saveNote: 尝试存入数据库");
            ContentValues values=new ContentValues();
            values.put("title", title);
            values.put("author", name);
            values.put("cdate", dateStr);
            values.put("content", content);
            values.put("images", images);
            if(edit){
                db.update("notebook", values, "id="+id, null);
            }else{
                db.insert("notebook", null, values);
            }
        }catch (Exception e){
            Log.d(TAG, "saveNote: "+e.getMessage());
            Toast.makeText(EditNote.this, "数据库写入出错！",Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "saveNote: 保存完成");
        Toast.makeText(EditNote.this, "保存成功！", Toast.LENGTH_SHORT).show();
    }

    /*
    为列表适配器添加数据项
     */
    private void addContentImg(Bitmap bitmap, String fileName){
            String fileDir=getFilesDir()+"/"+fileName;
            ContentImgItem item=new ContentImgItem(bitmap, fileDir);
            list.add(item);
            /*
            通知适配器数据更新
             */
            adapter.notifyDataSetChanged();
            Log.d(TAG, "addContentImg: success!");
    }

    /*
    显示图片选择对话框
     */
    private void showDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(EditNote.this);
        View view_choose_dialog= LayoutInflater.from(EditNote.this).inflate(R.layout.dialog_choose_picture, null);
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
                isEdit=true;
                dialog.cancel();
                if(ContextCompat.checkSelfPermission(EditNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(EditNote.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else{
                    openAlbum();
                }
            }
        });
        bt_openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit=true;
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
                    imageUri = FileProvider.getUriForFile(EditNote.this,"com.example.practice2_notebook.fileprovider",outputImage);
                }else {
                    imageUri= Uri.fromFile(outputImage);
                }
                openCamera();
            }
        });
    }

    /*
读写权限申请结果处理
 */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this, "请授予读写权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: enter");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                Log.d(TAG, "onActivityResult: CASE TAKE_PHOTO");
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        bitmap=Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                        addContentImg(bitmap, imageUri.toString());
                    } catch (Exception e) {
                        Log.d(TAG, "onActivityResult: "+e.getMessage());
                        Toast.makeText(EditNote.this, "添加图片错误！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
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
        Log.d(TAG, "handleImageOnKitKat: path:"+imagePath);
        try{
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            bitmap=Bitmap.createScaledBitmap(bitmap, 100, 100, true);
            addContentImg(bitmap, imagePath);
        }catch (Exception e){
            Log.d(TAG, "handleImageOnKitKat: "+e.getMessage());
            Toast.makeText(EditNote.this, "添加图片错误！", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    老版本图片路径解析
     */
    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri, null);
        try{
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            bitmap=Bitmap.createScaledBitmap(bitmap, 100, 100, true);
            addContentImg(bitmap, imagePath);
        }catch (Exception e){
            Log.d(TAG, "handleImageOnKitKat: "+e.getMessage());
            Toast.makeText(EditNote.this, "添加图片错误！", Toast.LENGTH_SHORT).show();
        }
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
    图片持久化存储
    @return 存储的图片的名称链表
     */
    private List<String> saveContentImage(List<ContentImgItem> list){
        List<String> imageNames=new ArrayList<String>();
        EditText text=(EditText) findViewById(R.id.text_content_title);
        String title=text.getText().toString();
        Date date=new Date(System.currentTimeMillis());
        FileOutputStream os=null;
        BufferedWriter writer=null;
        String preName=title+date.toString();
        Log.d(TAG, "saveContentImage: prepare success,title: "+title+" date: "+date+" prename:"+preName);
        int count=0;
        for(ContentImgItem item:list){
            Log.d(TAG, "saveContentImage: ");
            count++;
            Bitmap itemBitmap=item.getImgBitmap();
            String name=preName+String.valueOf(count);
            try{
                os=openFileOutput(name, Context.MODE_PRIVATE);
                itemBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
                imageNames.add(name);
            }catch (Exception e){
                Log.d(TAG, "saveContentImage: "+e.getMessage());
            }
        }
        return imageNames;
    }



//    protected void initContentImg(){
//        for(int i=0;i<10;i++){
//            ContentImgItem item=new ContentImgItem(R.drawable.authorimg_default);
//            list.add(item);
//        }
//    }
}