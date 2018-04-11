package com.example.woweather.tool;

import android.text.TextUtils;
import android.util.Log;

import com.example.woweather.db.City;
import com.example.woweather.db.County;
import com.example.woweather.db.Province;
import com.example.woweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 邹永鹏 on 2018/3/27.
 */

public class Utility {
    /*解析和处理服务器返回的省级数据*/
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){//返回数据不为空
            try{
                JSONArray allpProvinces=new JSONArray(response);
                for (int i=0;i<allpProvinces.length();i++){
                    JSONObject provinceObject=allpProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                    Log.d("local",province.getProvinceName());
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /*解析和处理服务器返回的市级数据*/
//    public static boolean handleCityResponse(String response,int provinceId){
    public static boolean handleCityResponse(String response,int provinceId,String provinceName){
        if (!TextUtils.isEmpty(response)){//返回数据不为空
            try{
                JSONArray allpCities=new JSONArray(response);
                for (int i=0;i<allpCities.length();i++){
                    JSONObject cityObject=allpCities.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    /*```````````````````````````````````*/
                    city.setProvinceName(provinceName);
                    Log.d("local",city.getCityName());
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /*解析和处理服务器返回的县级数据*/
    public static boolean handleCountyResponse(String response,int cityId,String cityName){
        if (!TextUtils.isEmpty(response)){//返回数据不为空
            try{
                JSONArray allpCounties=new JSONArray(response);
                for (int i=0;i<allpCounties.length();i++){
                    JSONObject countyObject=allpCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    //
                    county.setCityName(cityName);
                    county.save();
                    Log.d("local",county.getCountyName());
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //将返回的json数据解析成weather实体类
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            Log.d("success:","JSON解析："+weatherContent);
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
