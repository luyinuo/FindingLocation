package com.example.findinglocation;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;


/** 
 * 创建Activity（继承com.baidu.mapapi.MapActivity） 
 *  
 * @author lyn 
 * @date 2011-05-02 
 */  
public class MainActivity extends MapActivity implements com.baidu.mapapi.LocationListener{  
    private BMapManager mapManager;  
    private MapView mapView;  
    private MapController mapController;  
    private List<SmsInfo> infos;
    private EditText numberText;
    //定位信息  
    private LocationManager locationManager; 
    private SharedPreferences sp;
    private Location location;  
    //经纬度  
    private double latitude,longitude;  
    private MKLocationManager mLocationManager = null;
    private MyLocationOverlay myLocationOverlay;
    
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
    	
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);//获取手机位置信息
        List<String> providers = locationManager.getAllProviders();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//获取精准位置
        criteria.setCostAllowed(true);//允许产生开销
        criteria.setPowerRequirement(Criteria.POWER_HIGH);//消耗大的话，获取的频率高
        criteria.setSpeedRequired(true);//手机位置移动
        criteria.setAltitudeRequired(true);//海拔
        //获取最佳provider: 手机或者模拟器上均为gps
        String bestProvider = locationManager.getBestProvider(criteria, true);//使用GPS卫星
        System.out.println("最好的位置提供者是"+bestProvider);
        //location= locationManager.getLastKnownLocation(bestProvider);  
        
        //sp = getSharedPreferences("config",MODE_PRIVATE);
        //parameter: 1. provider 2. 每隔多少时间获取一次  3.每隔多少米  4.监听器触发回调函数
        //locationManager.requestLocationUpdates(bestProvider,60000,100, new MyLocationListener());
        
        //获得送达短信的电话号码
    	numberText = (EditText)this.findViewById(R.id.send_phone);
    	Button button = (Button)this.findViewById(R.id.send_button);
    	//添加监听器
    	button.setOnClickListener(new ButtonClickListener());
    	
    	//读取短信
        Uri uri = Uri.parse(AllFinalInfo.SMS_URI_INBOX);
        SmsContent sc = new SmsContent(this, uri);
        infos = sc.getSmsInfo();
        for(SmsInfo info:infos){
        	Log.i("MessageContent",info.getSmsbody());
        }
  
        /** 
         * 初始化MapActivity 
         */  
        mapManager = new BMapManager(getApplication());  
        // init方法的第一个参数需填入申请的API Key  
        mapManager.init("0334524162BC632CB0E5B3D64DD5534976FFE4ED", null);  
        super.initMapActivity(mapManager);  
  
        mLocationManager = mapManager.getLocationManager();  
        // 注册位置更新事件  
        mLocationManager.requestLocationUpdates(this);
        // 使用GPS定位  
        mLocationManager.enableProvider((int) MKLocationManager.MK_GPS_PROVIDER);
        
        mapView = (MapView) findViewById(R.id.map_View);  
        // 设置地图模式为交通地图  
        mapView.setTraffic(true);  
        // 设置启用内置的缩放控件  
        mapView.setBuiltInZoomControls(true);  
        
        // 构造一个经纬度点  
        GeoPoint point = new GeoPoint((int) (26.597239 * 1E6), (int) (106.720397 * 1E6));
  
        /** 
         * 创建图标资源（用于显示在overlayItem所标记的位置） 
         */  
        Drawable marker = this.getResources().getDrawable(R.drawable.marker_red_sprite);  
        // 为maker定义位置和边界  
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());  
  
        /** 
         * 创建自定义的ItemizedOverlay 
         */  
        CustomItemizedOverlay overlay = new CustomItemizedOverlay(marker, this);  
  
        
      
         
        OverlayItem overlayItem = new OverlayItem(point, "目标位置", "目标位置");  
        // 将标记添加到图层中（可添加多个OverlayItem）  
        overlay.addOverlay(overlayItem);  
  
        /** 
         * 往地图上添加自定义的ItemizedOverlay 
         */  
        List<Overlay> mapOverlays = mapView.getOverlays();  
        mapOverlays.add(overlay);  
  
        /** 
         * 取得地图控制器对象，用于控制MapView 
         */  
        mapController = mapView.getController();  
        // 设置地图的中心  
        mapController.setCenter(point);  
        // 设置地图默认的缩放级别  
        mapController.setZoom(9);  
        
        // 添加定位图层  
        myLocationOverlay = new MyLocationOverlay(this, mapView);  
        // 注册GPS位置更新的事件,让地图能实时显示当前位置  
        myLocationOverlay.enableMyLocation();  
        // 开启磁场感应传感器  
        myLocationOverlay.enableCompass();  
        mapView.getOverlays().add(myLocationOverlay); 
    }  
  
    @Override  
    protected boolean isRouteDisplayed() {  
        return false;  
    }  
  
    @Override  
    protected void onDestroy() {  
        if (mapManager != null) {  
            mapManager.destroy();  
            mapManager = null;  
        }  
        super.onDestroy();  
    }  
  
    @Override  
    protected void onPause() {  
        if (mapManager != null) {  
            mapManager.stop();  
        }  
        super.onPause();  
    }  
  
    @Override  
    protected void onResume() {  
        if (mapManager != null) {  
            mapManager.start();  
        }  
        super.onResume();  
    }  
    /** 
     * 根据MyLocationOverlay配置的属性确定是否在地图上显示当前位置 
     */  
    @Override  
    protected boolean isLocationDisplayed() {  
        return myLocationOverlay.isMyLocationEnabled();  
    }  
      
    /** 
     * 当位置发生变化时触发此方法 
     *  
     * @param location 当前位置 
     */  
    @Override  
    public void onLocationChanged(Location location) {  
        if (location != null) {  
            // 将当前位置转换成地理坐标点  
            final GeoPoint pt = new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000));  
            // 将当前位置设置为地图的中心  
            mapController.setCenter(pt);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }  
    }  
    
	
	
	//发送短信按钮监听
	public final class ButtonClickListener implements View.OnClickListener{

		@Override
		public void onClick(View arg0) {
			String number = numberText.getText().toString();
			if(location != null){
				location.getAccuracy();//精确度
	        	latitude = location.getLatitude();//经度
	        	longitude = location.getLongitude();//纬度
			}
			String content = "104.2090,30.7292";
			if(latitude>0 && longitude>0){
				content = "FindingLocation-target,"+latitude+","+longitude;
			}
			SmsManager manager = SmsManager.getDefault();
			ArrayList<String> texts = manager.divideMessage(content);
			for(String text:texts){
				manager.sendTextMessage(number, null, text, null, null);
			}
			Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();
		}
		
	}
	 
}  
