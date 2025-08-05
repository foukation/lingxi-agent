package com.fxzs.lingxiagent.util.ZUtil;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class ImageUtil {

    public static String TAG = "ImageUtil";
    public static void load(Context context, int resID, ImageView imageView){
        Glide.with(context) // 使用 Activity 或 Fragment 的 Context
                .load(resID)
                .into(imageView);
    }
    public static void net(Context context, String imageUrl, ImageView imageView){
        Glide.with(context) // 使用 Activity 或 Fragment 的 Context
                .load(imageUrl)
                .into(imageView);
    }
    public static void loadUriRadius(Context context, Uri imageUrl, ImageView imageView){
        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(16));
        Glide.with(context) // 使用 Activity 或 Fragment 的 Context
                .load(imageUrl)
                .apply(options)
                .into(imageView);
    }
    public static void netRadius(Context context, String imageUrl, ImageView imageView){
//        Log.e(TAG,"imageUrl = "+imageUrl);

        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(16));

//        GlideUrl path = new GlideUrl(imageUrl, new LazyHeaders.Builder()
//                .addHeader("device-type", "android")
////                .addHeader("Cookie", setCookie!!)
//        .build());
        Glide.with(context) // 使用 Activity 或 Fragment 的 Context
                .load(imageUrl)
                .apply(options)
                .into(imageView);
    }

    public static void netCircle(Context context, String imageUrl, ImageView imageView){
        Log.e(TAG,"imageUrl = "+imageUrl);

        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop(), new CircleCrop());

//        GlideUrl path = new GlideUrl(imageUrl, new LazyHeaders.Builder()
//                .addHeader("device-type", "android")
////                .addHeader("Cookie", setCookie!!)
//        .build());
        Glide.with(context) // 使用 Activity 或 Fragment 的 Context
                .load(imageUrl)
                .apply(options)
                .into(imageView);
    }

    public static void loadGif(Context context, int resId, ImageView imageView) {
        Glide.with(context)
                .asGif()
                .load(resId)
                .into(imageView);
    }
}
