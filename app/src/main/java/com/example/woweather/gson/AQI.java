package com.example.woweather.gson;

/**
 * Created by 邹永鹏 on 2018/3/27.
 */

public class AQI {
    public AQPCity city;

    public class AQPCity{
        public String aqi;
        public String pm25;
    }
}
