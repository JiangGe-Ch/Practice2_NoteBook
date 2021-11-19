package com.example.practice2_notebook;

import android.graphics.Bitmap;
import android.util.Log;

/*
内容中图片的实例对象
 */
public class ContentImgItem {
    private Bitmap imgBitmap;
    private String fileDir="";

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public ContentImgItem(){

    }

    public ContentImgItem(Bitmap imgId, String fileDir){
        this.imgBitmap =imgId;
        this.fileDir=fileDir;
        Log.d("debug", "ContentImgItem: fileDir="+fileDir);
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }
}
