package com.example.btzhang.goodweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by BTZHANG on 2018/1/19.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }

}
