package cn.ui;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.gddisplaymap.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bean.UserBean;
import cn.server.Send;

/****
 * 讲诉了高德地图定位和3D地图显示
 *
 * 打包和未打包的情况是不一样的，高德配置是可以配置调试版和发布版
 *
 */
public class MainActivity extends Activity implements LocationSource,AMapLocationListener,View.OnClickListener {

    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;// 高德相关
    private boolean isFirst = true;
    private static double lat,lng;//实时定位的经纬度
    private Button start;//共享位置按钮
    private Send send = new Send(MainActivity.this);
    private double latitude,longitude;//接收共享位置的经纬度
    private List<Marker> list;//存放共享位置的list
    private Marker marker,markerOwner;//接收的marker,自己位置的marker
    private boolean isClick = false;//判断是否点击了共享位置按钮,同时显示自己的位置信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        initMap();
        initView();
    }

    private void initView(){
        start = (Button)findViewById(R.id.start);
        start.setOnClickListener(this);

        list = new ArrayList<>();
    }
    private void initMap(){
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        setUpMap();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start:
                new Thread(send).start();
                isClick = true;
                break;
        }
    }

    /**
     * 设置地图属性
     */
    private void setUpMap(){
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);// 跟随模式
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null){
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0){
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                if (isFirst){
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 18));//定位成功移到当前定位点
                    isFirst = false;
                }
                lat = aMapLocation.getLatitude();
                lng = aMapLocation.getLongitude();
                if (isClick){
                    if (markerOwner != null ){
                        markerOwner.remove();//每次定位发生改变的时候,把自己的marker先移除再添加
                    }
                    markerOwner = (Marker)aMap.addMarker((help_add_icon(new LatLng(lat,lng), R.mipmap.icon_myp)));
                }
            }else{
                Log.i("123",aMapLocation.getErrorCode()+"错误码"+aMapLocation.getErrorInfo()+"错误信息");
            }
        }
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null){
            mlocationClient = new AMapLocationClient(MainActivity.this);
            mLocationOption = new AMapLocationClientOption();
            mlocationClient.setLocationListener(this);// 设置定位监听
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mlocationClient.setLocationOption(mLocationOption);// 设置为高精度定位模式
            mLocationOption.setInterval(1000);
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    public static double getLat(){
        return  lat;
    }
    public static double getLng(){
        return lng;
    }

    /**
     * 添加所接收到的共享位置信息
     * @param jsonArray
     */
    public void allLatLng(JSONArray jsonArray){
        try{
            if (list.size() != 0){
                Remove(list);
            }
            for (int i = 0;i<jsonArray.length();i++){
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                latitude = jsonObject.getDouble("lat");
                longitude = jsonObject.getDouble("lng");
                LatLng latLng = new LatLng(latitude,longitude);
                marker = (Marker) (aMap.addMarker(help_add_icon(latLng, R.mipmap.icon_tourist)));
                list.add(marker);
            }
        }catch (Exception e){
            Log.i("130","解析出错"+e.getMessage());
        }
    }
    /**
     * 手机上显示共享位置的图标
     * @param latLng
     * @param id
     * @return
     */
    public static MarkerOptions help_add_icon(LatLng latLng,int id){
        MarkerOptions markOptiopns = new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(id));
        return markOptiopns;
    }

    /**
     * 移除
     * @param list
     */
    public static void Remove(List<Marker> list){
        if (list != null) {
            for (Marker marker : list) {
                marker.remove();
            }
        }
    }
}
