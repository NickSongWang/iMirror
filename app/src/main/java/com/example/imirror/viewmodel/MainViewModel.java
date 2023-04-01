package com.example.imirror.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.example.imirror.bean.NowResponse;
import com.example.imirror.bean.SearchCityResponse;
import com.example.imirror.repository.SearchCityRepository;
import com.example.imirror.repository.WeatherRepository;
import com.example.library.base.BaseViewModel;

public class MainViewModel extends BaseViewModel {

    public MutableLiveData<SearchCityResponse> searchCityResponseMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<NowResponse> nowResponseMutableLiveData = new MutableLiveData<>();

    /**
     * 搜索城市
     *
     * @param cityName 城市名称
     */
    public void searchCity(String cityName) {
        new SearchCityRepository().searchCity(searchCityResponseMutableLiveData, failed, cityName);
    }
    /**
     * 实况天气
     *
     * @param cityId 城市ID
     */
    public void nowWeather(String cityId) {
        new WeatherRepository().nowWeather(nowResponseMutableLiveData,failed, cityId);
    }
}

