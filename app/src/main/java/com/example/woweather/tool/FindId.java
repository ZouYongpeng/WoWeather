package com.example.woweather.tool;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.woweather.MainActivity;
import com.example.woweather.MyApplication;
import com.example.woweather.db.City;
import com.example.woweather.db.County;
import com.example.woweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by 邹永鹏 on 2018/3/29.
 */

public class FindId {

    private ProgressDialog progressDialog;
    private static final String LOCAL="LOCAL";
    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表

    public static String localProvinceName;
    public static String localCityName;
    public static String localCountyName;
    public static int localProvinceId;
    public static int localCityId;
    public static String localWeatherId;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;



    public int getProvinceCodeFromInt(final String name, String address){
        Log.d(LOCAL,"进入getProvinceCodeFromInt");
        pref=PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
//        pref=getSharedPreferences("WoWeatherData",MODE_PRIVATE);
//        return 1;
//        showProgressDialog();
        //查找服務器
//        HttpUtil.sendOkHttpRequest(name,address, new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseText=response.body().string();
//                //判断数据是否为空
//                if (!TextUtils.isEmpty(responseText)) {
//                    //返回数据不为空
//                    try{
//                        JSONArray allpProvinces=new JSONArray(responseText);
//                        for (int i=0;i<allpProvinces.length();i++){
//                            //将所有省存入数据库sharePerences(name,id)
//                            JSONObject provinceObject=allpProvinces.getJSONObject(i);
//                            editor=pref.edit();
//                            editor.putInt(provinceObject.getString("name"),provinceObject.getInt("id"));
//                            Log.d(LOCAL,"保存"+provinceObject.getString("name")+" - "+provinceObject.getInt("id"));
//                            if (name.equals(provinceObject.getString("name"))){
//                                Log.d(LOCAL,"找到了"+name+" 对应的id为"+provinceObject.getInt("id"));
//                                localProvinceId=provinceObject.getInt("id");
//                            }
//                        }
//                    }catch (JSONException e){
//                        e.printStackTrace();
//                        Log.d(LOCAL,"无法解析资源");
//                    }
//                }
//            }
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//        });
//        closeProgressDialog();
        return localProvinceId;
    }

    //显示进度对话框
    public void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(MyApplication.getContext());
            progressDialog.setMessage("正在获取资源...");
            /*在loading的时候，如果你触摸屏幕其它区域，就会让这个progressDialog消失，然后可能出现崩溃问题
            * 所以在用ProgressDialog的地方，最好加下这个属性，防止4.0系统出问题*/
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    public void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
