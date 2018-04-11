package com.example.woweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.example.woweather.db.City;
import com.example.woweather.db.Collection;
import com.example.woweather.db.County;
import com.example.woweather.db.Province;
import com.example.woweather.gson.Forecast;
import com.example.woweather.gson.Weather;
import com.example.woweather.tool.ActivityCollector;
import com.example.woweather.tool.CollectAdapter;
import com.example.woweather.tool.HttpUtil;
import com.example.woweather.tool.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String LOCAL="LOCAL-WoWeather";

    public SwipeRefreshLayout swipeRefresh;
    private ProgressDialog progressDialog;
    public LocationClient mLocationClient;

    private SharedPreferences pref;

    private static final int FIND_PRO=1;
    private static final int FIND_CITY=2;
    private static final int FIND_COUNTY=3;
    private static final int FIND_WEATHER=4;

    private List<Collection> collectList=new ArrayList<>();
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    private DrawerLayout drawerLayout;
    private CollectAdapter adapter;

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取一个全局的context参数并传入
        mLocationClient=new LocationClient(getApplicationContext());
        //注册一个定位监听器，当获取到位置信息时，就会回调这个定位监听器
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);
        //检查是否申请权限，如果成功获取权限则在内部执行requestLocation查找位置
        checkPemission();

        intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver=new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver,intentFilter);

        // 初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg=(ImageView) findViewById(R.id.bing_pic_img);
        FloatingActionButton returnChoose=(FloatingActionButton)findViewById(R.id.return_choose);
        returnChoose.setOnClickListener(this);

        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOCAL,"天气数据已更新");
                requestWeather(MyApplication.getNowWeatherId());
            }
        });

        FloatingActionButton collectButton=(FloatingActionButton)findViewById(R.id.collect_button);
        collectButton.setOnClickListener(this);

        drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);

    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
//            Log.d(LOCAL,""+"本地省号为"+MyApplication.getLocalProvinceId());
            if (MyApplication.getLocalProvinceId()==0) {//第一次启动
                //获得目前所在省市
                MyApplication.setLocalProvinceName(bdLocation.getProvince().replace("省", ""));
                MyApplication.setLocalCityName(bdLocation.getCity().replace("市", ""));
                MyApplication.setLocalCountyName(bdLocation.getDistrict().replace("区", ""));
//                Log.d(LOCAL, "baiduLBS获取位置：" +MyApplication.getLocalProvinceName() + " " + MyApplication.getLocalCityName() + " " + MyApplication.getLocalCountyName());
                //获取当前省的id
                findId(MyApplication.getLocalProvinceName(), FIND_PRO);
            }else if (!getIntent().getStringExtra("weather_id").isEmpty()){//
                //获取点击列表返回的数据
                String weather_id=getIntent().getStringExtra("weather_id");
//                Log.d(LOCAL,"接收到来自chooseArea的数据"+getIntent().getStringExtra("weather_id"));
                requestWeather(weather_id);
            }
        }
    }

    public void findId(String name,int findType){
        Log.d(LOCAL,"开始寻找 "+name+" 的id");
        String address;
        //尝试使用litepal数据库
        switch (findType){
            case FIND_PRO:
                List<Province> provinces= DataSupport.where("provinceName = ?",name).find(Province.class);
                Log.d(LOCAL,"成功获取数据库");
                if (!provinces.isEmpty()){
                    Log.d(LOCAL,"数据库找到了 "+name+" id:"+provinces.get(0).getId());
                    //数据库找到了，取出来
                    MyApplication.setLocalProvinceId(provinces.get(0).getId());
                    findId(MyApplication.getLocalCityName(), FIND_CITY);
                }else{
                    //数据库找不到，就到服务器中寻找
                    address="http://guolin.tech/api/china";
                    Log.d(LOCAL,"address is "+address+" ,and type is "+findType);
                    getCodeFromInt(name,address,findType);
                }
                break;
            case FIND_CITY:
                List<City> citys= DataSupport.where("cityName = ?",name).find(City.class);
                if (!citys.isEmpty()){
                    //数据库找到了，取出来
                    Log.d(LOCAL,"数据库找到了 "+name+" id:"+citys.get(0).getId());
                    MyApplication.setLocalCityId(citys.get(0).getId());
                    findId(MyApplication.getLocalCountyName(), FIND_COUNTY);
                }else{
                    //数据库找不到，就到服务器中寻找
                    address="http://guolin.tech/api/china/"+MyApplication.getLocalProvinceId();
                    Log.d(LOCAL,"address is "+address+" ,and type is "+findType);
                    getCodeFromInt(name,address,findType);
                }
                break;
            case FIND_COUNTY:
                List<County> countys= DataSupport.where("countyName = ?",name).find(County.class);
                if (!countys.isEmpty()){
                    //数据库找到了，取出来
                    Log.d(LOCAL,"数据库找到了 "+name+" id:"+countys.get(0).getId());
                    int weatherIid = Integer.parseInt(countys.get(0).getWeatherId().replace("CN", ""));
                    MyApplication.setLocalWeatherId(weatherIid);
                    findId("CN" + MyApplication.getLocalWeatherId(), FIND_WEATHER);
                }else{
                    //数据库找不到，就到服务器中寻找
                    address="http://guolin.tech/api/china/"+MyApplication.getLocalProvinceId()+"/"+MyApplication.getLocalCityId();
                    Log.d(LOCAL,"address is "+address+" ,and type is "+findType);
                    getCodeFromInt(name,address,findType);
                }
                break;
            case FIND_WEATHER:
                requestWeather(name);
                Log.d(LOCAL,"开始查找weather_id:"+name);
            default:
                break;
        }
    }

    //根据天气ID请求城市天气信息
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid=" +weatherId
                +"&key=a41e6909fcad45289af37f6344d0580e";
        Log.d(LOCAL,"正在访问"+weatherUrl);
        HttpUtil.sendOkHttpRequest("",weatherUrl,1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"无法获取天气信息",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //将返回数据转换为文本
                final String responseText=response.body().string();
                final Weather weather= Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null && weather.status.equals("ok")){
                            showWeatherInfo(weatherId,weather);
                        }else {
                            Toast.makeText(MainActivity.this,"无法获取天气信息",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(String weatherId,Weather weather){
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.trim();
        String degree=weather.now.temperature+"`C";
        String weatherInfo=weather.now.more.info;
//        Log.d(LOCAL,"读取参数 "+cityName+" "+updateTime+" "+degree+" "+weatherInfo);
        //将当前地址名存入My
        MyApplication.setNowWeatherId(weatherId);
        MyApplication.setNowCountyName(cityName);

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //显示收藏按钮

        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView) view.findViewById(R.id.date_text);
            TextView infoText=(TextView) view.findViewById(R.id.info_text);
            TextView maxText=(TextView) view.findViewById(R.id.max_text);
            TextView minText=(TextView) view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carwash.info;
        String sport="运动建议："+weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        //根据天气加载图片
        int weatherPicUrl=R.drawable.default_weather;
        switch (weatherInfo){
            case "多云":
                weatherPicUrl=R.drawable.cloud;
                break;
            case "晴":
                weatherPicUrl=R.drawable.sunny;
                break;
            case "小雨":
                weatherPicUrl=R.drawable.rain;
                break;
            case "阵雨":
                weatherPicUrl=R.drawable.rain;
                break;
            case "晴间多云":
                weatherPicUrl=R.drawable.sunny_cloud;
                break;
            case "阴":
                weatherPicUrl=R.drawable.overcast;
                break;
            case "雾":
                weatherPicUrl=R.drawable.fog;
                break;
            default:
                break;
        }
        Glide.with(MainActivity.this).load(weatherPicUrl).into(bingPicImg);
        //初始化收藏列表
        initCollections();
    }

    //在服务器查找id
    public void getCodeFromInt(final String name, String address, final int findType) {
//        Log.d(LOCAL, "进入getProvinceCodeFromInt");
        pref = getSharedPreferences("WoWeatherData", MODE_PRIVATE);
        //查找服務器
        HttpUtil.sendOkHttpRequest(name, address, findType, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //判断数据是否为空
                if (!TextUtils.isEmpty(responseText)) {
                    //返回数据不为空,开始解析数据并将数据存储
                    try {
                        JSONArray all = new JSONArray(responseText);
                        for (int i = 0; i < all.length(); i++) {
                            JSONObject Object = all.getJSONObject(i);
                            //尝试将数据存入litepal
                            switch (findType){
                                case FIND_PRO:
                                    //存入
                                    Province province=new Province();
                                    province.setProvinceCode(Object.getInt("id"));
                                    province.setProvinceName(Object.getString("name"));
//                                    Log.d(LOCAL,"开始储存数据: "+Object.getString("name")+" - "+Object.getInt("id"));
                                    province.save();
                                    //并设置本地
                                    if (name.equals(Object.getString("name"))) {
//                                        Log.d(LOCAL, "在服务器找到了要查找的 " + name + " 对应的id为" + Object.getInt("id"));
                                        MyApplication.setLocalProvinceId(Object.getInt("id"));
                                    }
                                    break;
                                case FIND_CITY:
                                    //存入
                                    City city=new City();
                                    city.setCityCode(Object.getInt("id"));
                                    city.setCityName(Object.getString("name"));
//                                    Log.d(LOCAL,"开始储存数据: "+Object.getString("name")+" - "+Object.getInt("id"));
                                    city.save();
                                    //并设置本地
                                    if (name.equals(Object.getString("name"))) {
//                                        Log.d(LOCAL, "在服务器找到了要查找的 " + name + " 对应的id为" + Object.getInt("id"));
                                        MyApplication.setLocalCityId(Object.getInt("id"));
                                    }
                                    break;
                                case FIND_COUNTY:
                                    //存入
                                    County county=new County();
                                    county.setId(Object.getInt("id"));
                                    county.setCountyName(Object.getString("name"));
                                    county.setWeatherId(Object.getString("weather_id"));
//                                    Log.d(LOCAL,"开始储存数据: "+Object.getString("name")+" - "+Object.getInt("id")+" - "+Object.getString("weather_id"));
                                    county.save();
                                    //并设置本地
                                    if (name.equals(Object.getString("name"))) {
//                                        Log.d(LOCAL, "在服务器找到了要查找的 " + name + " 对应的id为" + Object.getInt("id"));
//                                        Log.d(LOCAL, "在服务器找到了要查找的 " + name + " 对应的weatherid为" + Object.getString("weather_id"));
                                        int weatherId=Integer.parseInt(Object.getString("weather_id").replace("CN",""));
                                        MyApplication.setLocalWeatherId(weatherId);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Log.d(LOCAL, "解析、读取省的数据时出现了错误");
                    }
                    switch (findType) {
                        case FIND_PRO:
//                            Log.d(LOCAL, "再次调用findId（）查找" + MyApplication.getLocalCityName() + " 的id");
                            findId(MyApplication.getLocalCityName(), FIND_CITY);
                            break;
                        case FIND_CITY:
//                            Log.d(LOCAL, "再次调用findId（）查找" + MyApplication.getLocalCountyName() + " 的id");
                            findId(MyApplication.getLocalCountyName(), FIND_COUNTY);
                            break;
                        case FIND_COUNTY:
//                            Log.d(LOCAL, "再次调用findId（）查找  CN" + MyApplication.getLocalWeatherId() + " 的天气");
                            findId("CN" + MyApplication.getLocalWeatherId(), FIND_WEATHER);
                            break;
                        default:
                            break;
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    //检查是否申请权限
    private void checkPemission(){
        //创建一个新的list集合，以此判断这三个权限有没有授权。没授权就添加
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            //将未授权的权限一次性申请
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            //开始地理位置定位，定位结果会回到定位监听器mLocationClient
            requestLocation();
        }
    }

    //若未同意权限则再次询问
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            //有一个权限不同意就会退出
                            Toast.makeText(MainActivity.this,"必须同意所有权限才能使用本程序",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        requestLocation();
                    }
                }else {
                    Toast.makeText(MainActivity.this,"发生未知错误",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void requestLocation(){
        LocationClientOption option=new LocationClientOption();
//        option.setScanSpan(100);//时间间隔为1小时
        option.setIsNeedAddress(true);//设置需要获取详细地址信息
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d(LOCAL,"MainActivity:onDestroy");
        mLocationClient.stop();//停止定位
        unregisterReceiver(networkChangeReceiver);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.return_choose:
                //返回选中列表
                Log.d(LOCAL,"返回选择列表");
                Intent intent=new Intent(this,ChooseArea.class);
                startActivity(intent);
                MainActivity.this.finish();
                break;
            case R.id.collect_button:
                //返回选中列表
                Log.d(LOCAL,"收藏当前");
                collectNow();
                break;
            default:
                break;
        }
    }

    private void initCollections(){
        //先读取收藏的内容
        Log.d(LOCAL,"当前收藏列表有");
        final List<Collection> collections=DataSupport.findAll(Collection.class);
        collectList.clear();
        for (Collection collection1:collections){
            Log.d(LOCAL,"地点："+collection1.getCollectName()+" weatherID:"+collection1.getCollectId());
            collectList.add(collection1);
        }
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.collect_weather_list);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        CollectAdapter adapter=new CollectAdapter(collectList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CollectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String weatherId) {
                Log.d(LOCAL,"you click"+weatherId);
                //跳转到点击的界面
                requestWeather(weatherId);
                //将滑动菜单关闭
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            @Override
            public void onItemDelete(String weatherId) {
                Log.d(LOCAL,"you delete"+weatherId);
                //将数据库中的该收藏删除
                DataSupport.deleteAll(Collection.class,"collectId=?",weatherId);
                //再刷新
                initCollections();
            }
        });
    }

    private void collectNow(){
        Log.d(LOCAL,"当前界面"+MyApplication.getNowCountyName()+" - "+MyApplication.getNowWeatherId());
        //判断是否已收藏
        List<Collection> collections=DataSupport.where("collectId = ?",MyApplication.getNowWeatherId()).find(Collection.class);
        if (collections.isEmpty()){
            //将当前界面的weather_id、countyName保存
            Collection collection=new Collection();
            Log.d(LOCAL,"保存当前："+MyApplication.getNowCountyName()+" - "+MyApplication.getNowWeatherId());
            collection.setCollectName(MyApplication.getNowCountyName());
            collection.setCollectId(MyApplication.getNowWeatherId());
            collection.save();
            Toast.makeText(this,"已成功添加至收藏夹，右滑可查看",Toast.LENGTH_SHORT).show();
            initCollections();
        }else {
            Toast.makeText(this,"已经在你的收藏夹里啦，右滑可查看",Toast.LENGTH_SHORT).show();
            initCollections();
        }

    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if (networkInfo!=null && networkInfo.isAvailable()){
                Toast.makeText(context,"网络已连接",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context,"网络连接失败",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
