package com.example.woweather.tool;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by 邹永鹏 on 2018/3/27.
 */

public class HttpUtil {

    //向服务器发送请求
    public static void sendOkHttpRequest(String name,String address,int findType,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
