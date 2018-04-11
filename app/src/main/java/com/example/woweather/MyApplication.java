package com.example.woweather;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.litepal.LitePalApplication;

/**
 * Created by 邹永鹏 on 2018/3/30.
 */

public class MyApplication extends Application {
    private static Context context;

    private static String localProvinceName;
    private static String localCityName;
    private static String localCountyName;

    private static int localProvinceId;
    private static int localCityId;
    private static int localWeatherId;

    private static String nowWeatherId;
    private static String nowCountyName;
//    private static String nowWeatherDegree;
    @Override
    public void onCreate() {
        context=getApplicationContext();
        LitePalApplication.initialize(context);
    }

    public static Context getContext(){
        return context;
    }

    public static void setLocalProvinceName(String localProvinceName) {
        MyApplication.localProvinceName = localProvinceName;
    }

    public static void setLocalCityName(String localCityName) {
        MyApplication.localCityName = localCityName;
    }

    public static void setLocalCountyName(String localCountyName) {
        MyApplication.localCountyName = localCountyName;
    }

    public static void setLocalProvinceId(int localProvinceId) {
        MyApplication.localProvinceId = localProvinceId;
    }

    public static void setLocalCityId(int localCityId) {
        MyApplication.localCityId = localCityId;
    }

    public static void setLocalWeatherId(int localWeatherId) {
        MyApplication.localWeatherId = localWeatherId;
    }

    public static void setNowWeatherId(String nowWeatherId) {
        MyApplication.nowWeatherId = nowWeatherId;
    }

    public static void setNowCountyName(String nowCountyName) {
        MyApplication.nowCountyName = nowCountyName;
    }

//    public static void setNowWeatherDegree(String nowWeatherDegree) {
//        MyApplication.nowWeatherDegree = nowWeatherDegree;
//    }

    public static String getLocalProvinceName() {
        return localProvinceName;
    }

    public static String getLocalCityName() {
        return localCityName;
    }

    public static String getLocalCountyName() {
        return localCountyName;
    }

    public static int getLocalProvinceId() {
        return localProvinceId;
    }

    public static int getLocalCityId() {
        return localCityId;
    }

    public static int getLocalWeatherId() {
        return localWeatherId;
    }

    public static String getNowWeatherId() {
        return nowWeatherId;
    }

    public static String getNowCountyName() {
        return nowCountyName;
    }

//    public static String getNowWeatherDegree() {
//        return nowWeatherDegree;
//    }

    public static void showLocal(){
        Log.d("local",localProvinceName+" "+localCityName+" "+localCountyName);
    }
}
