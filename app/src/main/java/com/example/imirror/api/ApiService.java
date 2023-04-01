package com.example.imirror.api;

import static com.example.imirror.Constant.API_KEY;

import com.example.imirror.bean.DailyResponse;
import com.example.imirror.bean.NowResponse;
import com.example.imirror.bean.SearchCityResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    /**
     * 搜索城市  模糊搜索，国内范围 返回10条数据
     *
     * @param location 城市名
     * @return NewSearchCityResponse 搜索城市数据返回
     */
    @GET("/v2/city/lookup?key=" + API_KEY + "&range=cn")
    Observable<SearchCityResponse> searchCity(@Query("location") String location);

    /**
     * 实况天气
     *
     * @param location 城市ID
     * @return 返回实况天气数据 NowResponse
     */
    @GET("/v7/weather/now?key=" + API_KEY)
    Observable<NowResponse> nowWeather(@Query("location") String location);

    /**
     * 天气预报  (免费订阅)最多可以获得3天的数据
     *
     * @param location 城市id
     * @return 返回天气预报数据 DailyResponse
     */
    @GET("/v7/weather/3d?key=" + API_KEY)
    Observable<DailyResponse> dailyWeather(@Query("location") String location);
}
