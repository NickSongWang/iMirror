package com.example.imirror;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.app.ActionBar;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.imirror.bean.DailyResponse;
import com.example.imirror.bean.NowResponse;
import com.example.imirror.bean.SearchCityResponse;
import com.example.imirror.databinding.ActivityMainBinding;
import com.example.imirror.databinding.ImirrorMainLayoutBinding;
import com.example.imirror.location.LocationCallback;
import com.example.imirror.location.MyLocationListener;
import com.example.imirror.repository.adapter.DailyAdapter;
import com.example.imirror.utils.WeatherUtil;
import com.example.imirror.viewmodel.MainViewModel;
import com.example.library.base.NetworkActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends NetworkActivity<ImirrorMainLayoutBinding> implements LocationCallback {

    //权限数组
    private final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求权限意图
    private ActivityResultLauncher<String[]> requestPermissionIntent;

    public LocationClient mLocationClient = null;
    private final MyLocationListener myListener = new MyLocationListener();

    private MainViewModel viewModel;

    private final List<DailyResponse.DailyBean> dailyBeanList = new ArrayList<>();
    private final DailyAdapter dailyAdapter = new DailyAdapter(dailyBeanList);

    /**
     * 注册意图
     */
    @Override
    public void onRegister() {
        //请求权限意图
        requestPermissionIntent = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean fineLocation = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
            boolean writeStorage = Boolean.TRUE.equals(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE));
            if (fineLocation && writeStorage) {
                //权限已经获取到，开始定位
                startLocation();
            }
        });
    }




    @Override
    protected void onCreate() {
        initLocation();
        requestPermission();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        * 全屏显示，隐藏状态栏、导航栏*/
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }




    /**
     * 数据观察
     */
    @Override
    protected void onObserveData() {
        if (viewModel != null) {
            //城市数据返回
            viewModel.searchCityResponseMutableLiveData.observe(this, searchCityResponse -> {
                List<SearchCityResponse.LocationBean> location = searchCityResponse.getLocation();
                if (location != null && location.size() > 0) {
                    String id = location.get(0).getId();
                    Log.d("TAG", "城市ID: " + id);
                    //获取到城市的ID
                    if (id != null) {
                        //通过城市ID查询城市实时天气
                        viewModel.nowWeather(id);
                        //通过城市ID查询天气预报
                        viewModel.dailyWeather(id);
                    }
                }
            });
            //实况天气返回
            viewModel.nowResponseMutableLiveData.observe(this, nowResponse -> {
                NowResponse.NowBean now = nowResponse.getNow();
                if (now != null) {
                    binding.weatherTemp.setText(now.getTemp());
                }
            });
            //天气预报返回
            viewModel.dailyResponseMutableLiveData.observe(this, dailyResponse -> {
                List<DailyResponse.DailyBean> daily = dailyResponse.getDaily();
                if (daily != null) {
                    binding.weatherLowtemp.setText(daily.get(0).getTempMin());
                    binding.weatherHightemp.setText(daily.get(0).getTempMax());
                    WeatherUtil.changeIcon(binding.weatherStatus, Integer.parseInt(daily.get(0).getIconDay()));
//                    if (dailyBeanList.size() > 0) {
//                        dailyBeanList.clear();
//                    }
//                    dailyBeanList.addAll(daily);
//                    dailyAdapter.notifyDataSetChanged();
                }
            });
            //错误信息返回
            viewModel.failed.observe(this, this::showLongMsg);
        }
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        //因为项目的最低版本API是23，所以肯定需要动态请求危险权限，只需要判断权限是否拥有即可
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //开始权限请求
            requestPermissionIntent.launch(permissions);
            return;
        }
        //开始定位
        startLocation();
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            myListener.setCallback(this);
            //注册定位监听
            mLocationClient.registerLocationListener(myListener);
            LocationClientOption option = new LocationClientOption();
            //如果开发者需要获得当前点的地址信息，此处必须为true
            option.setIsNeedAddress(true);
            //可选，设置是否需要最新版本的地址信息。默认不需要，即参数为false
            option.setNeedNewVersionRgc(true);
            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            mLocationClient.setLocOption(option);
        }
    }
    /**
     * 开始定位
     */
    private void startLocation() {
        if (mLocationClient != null) {
            mLocationClient.start();
        }
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
//        double latitude = bdLocation.getLatitude();    //获取纬度信息
//        double longitude = bdLocation.getLongitude();    //获取经度信息
//        float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f
//        String coorType = bdLocation.getCoorType();
//        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
//        int errorCode = bdLocation.getLocType();//161  表示网络定位结果
//        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
//        String addr = bdLocation.getAddrStr();    //获取详细地址信息
//        String country = bdLocation.getCountry();    //获取国家
//        String province = bdLocation.getProvince();    //获取省份
//        String city = bdLocation.getCity();    //获取城市
        String district = bdLocation.getDistrict();    //获取区县
//        String street = bdLocation.getStreet();    //获取街道信息
//        String locationDescribe = bdLocation.getLocationDescribe();    //获取位置描述信息
        binding.weatherLocation.setText(district);//设置文本显示

        if (viewModel != null && district != null) {
            //搜索城市
            viewModel.searchCity(district);
        } else {
            Log.e("TAG", "district: " + district);
        }
    }





}

