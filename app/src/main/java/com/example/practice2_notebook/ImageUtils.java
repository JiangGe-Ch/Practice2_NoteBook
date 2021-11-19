package com.example.practice2_notebook;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static String getEncodedImage(Drawable drawable){
        Bitmap bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity()!= PixelFormat.OPAQUE?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        String imageBase64=new String(Base64.encodeToString(os.toByteArray(), Base64.DEFAULT));
        return imageBase64;
    }

    public static Drawable getDecodedImage(String base64){
        byte[] base64byte= Base64.decode(base64, Base64.DEFAULT);
        ByteArrayInputStream is=new ByteArrayInputStream(base64byte);
        Drawable drawable=Drawable.createFromStream(is, "image");
        return drawable;
    }
}
