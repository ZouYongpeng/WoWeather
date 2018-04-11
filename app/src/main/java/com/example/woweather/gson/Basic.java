package com.example.woweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 邹永鹏 on 2018/3/27.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String cityId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
    /*因为JSON某一些字段不太适合直接命名，所以使用@SerializedName注解来建立映射关系*/
}
