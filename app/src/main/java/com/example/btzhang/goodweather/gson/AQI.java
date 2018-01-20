package com.example.btzhang.goodweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by BTZHANG on 2018/1/19.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        @SerializedName("aqi")
        public String aqi;
        @SerializedName("pm25")
        public String pm25;
    }
}
