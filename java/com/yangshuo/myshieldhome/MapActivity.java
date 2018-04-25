package com.yangshuo.myshieldhome;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private TextView txtMessage;
    private String strMessage;
    private Button btnPrevDay;
    private Button btnNextDay;
    private MapView mMapView = null;
    private AMap aMap = null;
    private ArrayList<MarkerOptions> pointList = new ArrayList<MarkerOptions>();
    private int currentDateIndex = 0;
    private String strDeviceID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);

        currentDateIndex = CfgParamMgr.getInstance().dateIndex;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strDeviceID = bundle.getString(CommonParams.PARAM_NAME);
        loadGPSdata();
    }
    @Override
    protected void onStart() {
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        super.onStart();
    }
    @Override
    protected void onResume(){
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public void prevDay(View view) {
        finish();
        CfgParamMgr.getInstance().dateIndex = currentDateIndex - 1;
        Intent intent = new Intent(MapActivity.this, MapActivity.class);
        Bundle paramBundle = new Bundle();
        paramBundle.putString(CommonParams.PARAM_NAME, strDeviceID);
        intent.putExtras(paramBundle);
        startActivity(intent);
    }
    public void nextDay(View view) {
        finish();
        CfgParamMgr.getInstance().dateIndex = currentDateIndex + 1;
        Intent intent = new Intent(MapActivity.this, MapActivity.class);
        Bundle paramBundle = new Bundle();
        paramBundle.putString(CommonParams.PARAM_NAME, strDeviceID);
        intent.putExtras(paramBundle);
        startActivity(intent);
    }

    private void loadGPSdata(){
        readGPSinfoFile();
        if(pointList.size()<=0)
        {
            return;
        }
//        aMap.addMarkers(pointList,true);// true是放大图，false是全市图
        ArrayList<Marker> markList = aMap.addMarkers(pointList,true);// true是放大图，false是全市图
        for (int i=0;i<markList.size();i++)
        {
            Marker mark = markList.get(i);
            mark.setObject(String.valueOf(i+1));
        }
    }

    private void readGPSinfoFile(){
        strMessage = strDeviceID+"\r\n";
        txtMessage.setText(strMessage);
        pointList.clear();
        List<String> pathList = new ArrayList<String>();
        File[] files = new File(CommonParams.path.getAbsolutePath()).listFiles();
        for (File file : files) {
            if((file.getName().endsWith(CommonParams.GPSinfoFileName))&&(file.getName().startsWith(strDeviceID)))
            {
                if(file.isFile()) {
                    String tmpFilename = file.getName();
                    pathList.add(tmpFilename);
                }
            }
        }
        if(pathList.size()<=0)
        {
            Button btnTmp = (Button)findViewById(R.id.btnPrevDay);
            btnTmp.setEnabled(false);
            btnTmp = (Button)findViewById(R.id.btnNextDay);
            btnTmp.setEnabled(false);
            strMessage += "没有文件";
            txtMessage.setText(strMessage);
            return;
        }
        Collections.sort(pathList);
        int currentFileIndex = pathList.size()-1+currentDateIndex;
        if(currentFileIndex>=pathList.size())
        {
            currentFileIndex = pathList.size()-1;
        }
        else if(currentFileIndex<0)
        {
            currentFileIndex = 0;
        }

        if(currentFileIndex==0)
        {
            Button btnTmp = (Button)findViewById(R.id.btnPrevDay);
            btnTmp.setEnabled(false);
        }
        if(currentFileIndex==pathList.size()-1)
        {
            Button btnTmp = (Button)findViewById(R.id.btnNextDay);
            btnTmp.setEnabled(false);
        }
        try {
            File GPSinfoFile=new File(CommonParams.path,pathList.get(currentFileIndex));
            if(GPSinfoFile.exists()==false)
            {
                strMessage += "没有文件";
                txtMessage.setText(strMessage);
                return;
            }

            FileInputStream fis=new FileInputStream(GPSinfoFile);
            InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
            BufferedReader bfr=new BufferedReader(isr);
            String in="";
            int iBegin=0, iEnd=0;
            String strTime = "";
            String strLongitude = "";
            String strLatitude = "";
            int idx = 0;
            while((in=bfr.readLine())!=null)//readLine不会读出\r\n
            {
//                String[] strParam = in.split(CommonParams.PATTERN_COMMA_SPLIT);
//                for(int i=0; i<strParam.length;i++)
//                {}
                if(in.contains(CommonParams.SUB_PARAM_TIME))
                {
                    iBegin = in.indexOf(CommonParams.SUB_PARAM_TIME)+CommonParams.SUB_PARAM_TIME.length();
                    iEnd = in.indexOf(CommonParams.PATTERN_COMMA_SPLIT, iBegin);
                    strTime = in.substring(iBegin, iEnd);
                }
                if(in.contains(CommonParams.SUB_PARAM_LONGITUDE))
                {
                    iBegin = in.indexOf(CommonParams.SUB_PARAM_LONGITUDE)+CommonParams.SUB_PARAM_LONGITUDE.length();
                    iEnd = in.indexOf(CommonParams.PATTERN_COMMA_SPLIT, iBegin);
                    strLongitude = in.substring(iBegin, iEnd);
                }
                if(in.contains(CommonParams.SUB_PARAM_LATITUDE))
                {
                    iBegin = in.indexOf(CommonParams.SUB_PARAM_LATITUDE)+CommonParams.SUB_PARAM_LATITUDE.length();
                    strLatitude = in.substring(iBegin);
                }
                LatLng latLng = new LatLng(Double.valueOf(strLatitude),Double.valueOf(strLongitude));
                idx++;
//                MarkerOptions tmpMarker = new MarkerOptions().position(latLng).title(String.valueOf(idx)).snippet(strTime);
                //TODO:这里应该写上具体地址，需要用到逆地理编码  http://lbs.amap.com/api/android-sdk/guide/map-data/geo
                MarkerOptions tmpMarker = new MarkerOptions().position(latLng).title(strTime).snippet("").icon(BitmapDescriptorFactory.fromBitmap(getMyBitmap(String.valueOf(idx))));
//                https://www.cnblogs.com/gisxs/p/3732060.html
                tmpMarker.infoWindowEnable(true);//没用，不是持续显示
                pointList.add(tmpMarker);
            }
            isr.close();
            fis.close();

            strTime = strTime.substring(0, strTime.indexOf(" "));//yyyy-mm-dd hh:mm:ss
            strMessage += strTime+"\r\n";
            txtMessage.setText(strMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Bitmap getMyBitmap(String pm_val) {
        Bitmap bitmap = BitmapDescriptorFactory.fromResource(
                R.drawable.marker_60).getBitmap();
        bitmap = Bitmap.createBitmap(bitmap, 0 ,0, bitmap.getWidth(),
                bitmap.getHeight());
//        bitmap.setHeight(200);
//        bitmap.setWidth(200);
        Canvas canvas = new Canvas(bitmap);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40f);
        textPaint.setColor(getResources().getColor(R.color.white));
        https://blog.csdn.net/harvic880925/article/details/50423762
        //这里图片是60X93，，y是基准线，不是左上角，y以上，x以右写文字
        canvas.drawText(pm_val, 10, 50 ,textPaint);// 设置bitmap上面的文字位置
        return bitmap;
    }
}
