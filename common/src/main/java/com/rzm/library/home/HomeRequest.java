package com.rzm.library.home;

import com.rzm.arouter_api.Call;

public interface HomeRequest extends Call {
    //home模块有一个请求天气的接口，暴露粗来供其他模块调用
    String getWeatherInfo(String city);
}
