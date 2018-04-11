package com.example.woweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woweather.db.City;
import com.example.woweather.db.County;
import com.example.woweather.db.Province;
import com.example.woweather.tool.ActivityCollector;
import com.example.woweather.tool.HttpUtil;
import com.example.woweather.tool.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseArea extends AppCompatActivity {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();


    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表

    private Province selectedProvince;//选中的省
    private City selectedCity;//选中的市
    private int currentLevel;//当前选中的级别
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);

        //获取控件的实例
        titleText=(TextView) findViewById(R.id.title_text);
        backButton=(Button) findViewById(R.id.back_button);
        listView=(ListView) findViewById(R.id.list_view);

        //初始化适配器
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    //当前选中的是省时，获取省列表的该位置的值
                    selectedProvince=provinceList.get(position);
//                    Log.d("success:","you click "+selectedProvince.getProvinceName());
                    //查询属于该省的市
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    //当前选中的是市时，获取市列表的该位置的值
                    selectedCity=cityList.get(position);
                    //查询属于该市的县
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY){
                    //当前选中的是县时，跳转界面
                    String weatherId=countyList.get(position).getWeatherId();
                    Intent intent=new Intent(ChooseArea.this,MainActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
//                    Log.d("success:","将weather_id"+weatherId+"传给显示界面");
                    ChooseArea.this.finish();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTY){
                    //从县列表选中返回时，返回市
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    //从县列表选中返回时，返回省
                    queryProvinces();
                }else if (currentLevel==LEVEL_PROVINCE){
                    //从省列表选中返回时，返回主页
                    String weatherId="CN"+MyApplication.getLocalWeatherId();
                    Intent intent=new Intent(ChooseArea.this,MainActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
//                    Log.d("success:","将weather_id"+weatherId+"传给显示界面");
                    ChooseArea.this.finish();
                }
            }
        });
        queryProvinces();//显示省列表，一开始从这里加载
    }

    //查询全国所有的省
    private void queryProvinces(){
        titleText.setText("中国");
        //先在数据库中查询
        Log.d("success","进入queryProvinces");
        provinceList= DataSupport.findAll(Province.class);//获取指定类型的数据集合
        if (provinceList.size()>0){
            Log.d("success:","在数据库上查询省");
            dataList.clear();//先清空当前列表
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
            listView.setSelection(0);//将第position个item显示在listView的最上面一项
            currentLevel=LEVEL_PROVINCE;//当前选中级别为省
        }
        //数据库没有时就到服务器上查询
        else {
            Log.d("success:","在服务器上查询省");
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }

    }

    //查询全省所有的市
    private void queryCities(){
        Log.d("success:","中国 - "+selectedProvince.getProvinceName());
        titleText.setText("中国 - "+selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);//按钮可见
        //先在数据库中查询
        cityList= DataSupport
                .where("provincename = ?",String.valueOf(selectedProvince.getProvinceName()))//获取符合该名字的省
                .find(City.class);//获取指定类型的数据集合

        if (cityList.size()>0){
            Log.d("success:","在数据库上查询市");
            dataList.clear();//先清空当前列表
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();//如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
            listView.setSelection(0);//将第position个item显示在listView的最上面一项
            currentLevel=LEVEL_CITY;//当前选中级别为市
        }
        //数据库没有时就到服务器上查询
        else {
            Log.d("success:","在服务器上查询市");
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    //查询全市所有的县
    private void queryCounties(){
        titleText.setText("中国 - "+selectedProvince.getProvinceName()+" - "+selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);//按钮可见
        //先在数据库中查询
        countyList= DataSupport
                .where("cityName = ?",String.valueOf(selectedCity.getCityName()))//获取符合该ID的省
                .find(County.class);//获取指定类型的数据集合
        if (countyList.size()>0){
            Log.d("success:","在数据库上查询县");
            dataList.clear();//先清空当前列表
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();//如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
            listView.setSelection(0);//将第position个item显示在listView的最上面一项
            currentLevel=LEVEL_COUNTY;//当前选中级别为县
        }
        //数据库没有时就到服务器上查询
        else {
            Log.d("success:","在服务器上查询县");
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //在服务器上查询
    public void queryFromServer(String address,final String type){
        HttpUtil.sendOkHttpRequest("",address,1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread回到主线程
                ChooseArea.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChooseArea.this,"获取资源失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if (type.equals("province")){
                    //返回解析得到的数据
                    result= Utility.handleProvinceResponse(responseText);
                }else if (type.equals("city")){
                    //返回解析得到的数据
                    result= Utility.handleCityResponse(responseText,selectedProvince.getId(),selectedProvince.getProvinceName());
                }else if (type.equals("county")){
                    //返回解析得到的数据
                    result= Utility.handleCountyResponse(responseText,selectedCity.getId(),selectedCity.getCityName());
                }
                if (result){
                    ChooseArea.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (type.equals("province")){
                                queryProvinces();
                            }else if (type.equals("city")){
                                queryCities();
                            }else if (type.equals("county")){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d("LOCAL","ChooseArea:onDestory");
//    }
}
