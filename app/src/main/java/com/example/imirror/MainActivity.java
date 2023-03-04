package com.example.imirror;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;

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
import com.example.imirror.bean.SearchCityResponse;
import com.example.imirror.databinding.ActivityMainBinding;
import com.example.imirror.databinding.ImirrorMainLayoutBinding;
import com.example.imirror.location.LocationCallback;
import com.example.imirror.location.MyLocationListener;
import com.example.imirror.viewmodel.MainViewModel;
import com.example.library.base.NetworkActivity;

import java.io.IOException;
import java.util.List;

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;


public class MainActivity extends NetworkActivity<ImirrorMainLayoutBinding> implements LocationCallback {

    //权限数组
    private final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求权限意图
    private ActivityResultLauncher<String[]> requestPermissionIntent;

    public LocationClient mLocationClient = null;
    private final MyLocationListener myListener = new MyLocationListener();

    private MainViewModel viewModel;

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
            viewModel.searchCityResponseMutableLiveData.observe(this, searchCityResponse -> {
                List<SearchCityResponse.LocationBean> location = searchCityResponse.getLocation();
                if (location != null && location.size() > 0) {
                    String id = location.get(0).getId();
                    Log.d("TAG", "城市ID: " + id);
                }
            });
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
        double latitude = bdLocation.getLatitude();    //获取纬度信息
        double longitude = bdLocation.getLongitude();    //获取经度信息
        float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f
        String coorType = bdLocation.getCoorType();
        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
        int errorCode = bdLocation.getLocType();//161  表示网络定位结果
        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
        String addr = bdLocation.getAddrStr();    //获取详细地址信息
        String country = bdLocation.getCountry();    //获取国家
        String province = bdLocation.getProvince();    //获取省份
        String city = bdLocation.getCity();    //获取城市
        String district = bdLocation.getDistrict();    //获取区县
        String street = bdLocation.getStreet();    //获取街道信息
        String locationDescribe = bdLocation.getLocationDescribe();    //获取位置描述信息
        binding.weatherLocation.setText(district);//设置文本显示

        if (viewModel != null && district != null) {
            //搜索城市
            viewModel.searchCity(district);
        } else {
            Log.e("TAG", "district: " + district);
        }
//        //调用和风天气查找城市API的方法
//        searchCity(district);
    }





//    //使用OKHttp请求url
//    private void searchCity(String district) {
//        //使用Get异步请求
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                //拼接访问地址
//                .url("https://geoapi.qweather.com/v2/city/lookup?key=c24af8f8985544128271bb85028b696a&location="+district)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//            }
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if(response.isSuccessful()){//回调的方法执行在子线程。
//                    Log.d("a","获取数据成功了");
//                    Log.d("a","response.code()=="+response.code());
//                    Log.d("a","response.body().string()=="+response.body().string());
//                }
//            }
//        });
//    }


}

