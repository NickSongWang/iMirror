package com.example.imirror.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.example.imirror.bean.SearchCityResponse;
import com.example.imirror.repository.SearchCityRepository;
import com.example.library.base.BaseViewModel;

public class MainViewModel extends BaseViewModel {

    public MutableLiveData<SearchCityResponse> searchCityResponseMutableLiveData = new MutableLiveData<>();

    /**
     * 搜索成功
     *
     * @param cityName 城市名称
     */
    public void searchCity(String cityName) {
        new SearchCityRepository().searchCity(searchCityResponseMutableLiveData, failed, cityName);
    }
}

