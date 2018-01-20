package com.example.btzhang.goodweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by BTZHANG on 2018/1/19.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
