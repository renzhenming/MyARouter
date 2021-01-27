package com.rzm.home;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.library.home.HomeRequest;

@ARouter(path = "/home/HomeRequestApi")
public class HomeRequestApi implements HomeRequest {
    @Override
    public String getWeatherInfo(String city) {
        return "home 模块请求天气接口，收到返回结果 city = " + city;
    }
}
