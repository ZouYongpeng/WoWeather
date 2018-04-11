package com.example.woweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 邹永鹏 on 2018/3/27.
 */

public class Forecast {
    public String date;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt_d")
        public String info;
    }

    @SerializedName("tmp")
    public Temperature temperature;

    public class Temperature{
        public String max;
        public String min;
    }
}
