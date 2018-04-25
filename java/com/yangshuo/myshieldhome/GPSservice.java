package com.yangshuo.myshieldhome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yangshuo on 2018/2/11.
 */

public class GPSservice  extends JobService implements AMapLocationListener{
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    private AMapLocationClientOption mLocationOption = null;
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }

    /**
     * 服务创建的时候调用的方法
     */
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }

    JobParameters mParams = null;
    @Override
    public boolean onStartJob(JobParameters params) {
        mParams = params;
        startGPS();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
    private void startGPS() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
//        MailManager.getInstance().sendMail("定位开始."+CommonParams.deviceName+df.format(date), CommonParams.deviceName);
        initLocation();
        // 启动定位
        mLocationClient.startLocation();
        //       return super.onStartCommand(intent, flags, startId);
        //使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
//        return START_REDELIVER_INTENT;
    }
    private void startLocation(){
        //根据参数选择，重新设置定位参数
        getDefaultOption();
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }
    private void stopLocation(){
        if(mLocationClient!=null)
        {
            mLocationClient.stopLocation();//停止定位
        }
    }

    private void destroyLocation(){
        if (mLocationClient != null) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationOption = null;
        }
    }
    /**
     * 服务销毁的时候调用的方法
     */
/*    @Override
    public void onDestroy() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        MailManager.getInstance().sendMail("定位销毁onDestroy："+df.format(date), df.format(date));
        stopLocation();
        destroyLocation();
        super.onDestroy();
    }
    */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            StringBuffer sb = new StringBuffer();
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            if (amapLocation.getErrorCode() == 0) {
                sb.append("定位成功." + CommonParams.deviceName+ "<br>\n");
                sb.append("时间间隔: " + mLocationOption.getInterval()/60000 + "分钟<br>\n");
                sb.append("定位类型: " + amapLocation.getLocationType() + "<br>\n");//获取当前定位结果来源，如网络定位结果，详见定位类型表
                sb.append("经    度    : " + amapLocation.getLongitude() + "<br>\n");
                sb.append("纬    度    : " + amapLocation.getLatitude() + "<br>\n");
                sb.append("精    度    : " + amapLocation.getAccuracy() + "米" + "<br>\n");
                sb.append("提供者    : " + amapLocation.getProvider() + "<br>\n");
                sb.append("速    度    : " + amapLocation.getSpeed() + "米/秒" + "<br>\n");
                sb.append("角    度    : " + amapLocation.getBearing() + "<br>\n");
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : " + amapLocation.getSatellites() + "<br>\n");
                sb.append("国    家    : " + amapLocation.getCountry() + "<br>\n");
                sb.append("省            : " + amapLocation.getProvince() + "<br>\n");
                sb.append("市            : " + amapLocation.getCity() + "<br>\n");
                sb.append("城市编码 : " + amapLocation.getCityCode() + "<br>\n");
                sb.append("区            : " + amapLocation.getDistrict() + "<br>\n");
                sb.append("区域 码   : " + amapLocation.getAdCode() + "<br>\n");
                sb.append("地    址    : " + amapLocation.getAddress() + "<br>\n");
                sb.append("兴趣点    : " + amapLocation.getPoiName() + "<br>\n");
                //定位完成的时间
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                sb.append("定位时间: " + df.format(date) + "<br>\n");
            } else {
                //定位失败
                sb.append("定位失败" + "<br>\n");
                sb.append("错误码:" + amapLocation.getErrorCode() + "<br>\n");
                sb.append("错误信息:" + amapLocation.getErrorInfo() + "<br>\n");
                sb.append("错误描述:" + amapLocation.getLocationDetail() + "<br>\n");
            }
            sb.append("***定位质量报告***").append("<br>\n");
            sb.append("* WIFI开关：").append(amapLocation.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("<br>\n");
            sb.append("* GPS状态：").append(getGPSStatusString(amapLocation.getLocationQualityReport().getGPSStatus())).append("<br>\n");
            sb.append("* GPS星数：").append(amapLocation.getLocationQualityReport().getGPSSatellites()).append("<br>\n");
            sb.append("****************").append("<br>\n");
            //定位之后的回调时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(amapLocation.getTime());
            sb.append("回调时间: " + df.format(date) + "<br>\n");
            //解析定位结果，
            String result = sb.toString();
            /*检测围栏状态*/
            if(checkFence(amapLocation.getLongitude(), amapLocation.getLatitude())==true)
            {
                MailManager.getInstance().sendMail(CommonParams.MAIL_TITLE_GPS_LOCATE_SUCCEED+CfgParamMgr.getInstance().getMachineName()+".设备ID." + CfgParamMgr.getInstance().getDeviceID()+".时间"+ df.format(date), result);
            }
        } else {
            MailManager.getInstance().sendMail(CommonParams.MAIL_TITLE_GPS_LOCATE_FAILED+CfgParamMgr.getInstance().getMachineName()+".设备ID." + CfgParamMgr.getInstance().getDeviceID(), CfgParamMgr.getInstance().getMachineName());
        }
        //TODO: 接收命令，调整时间间隔。由于线程因素，可能这次无法调整，但下次应该能调整过来
        //stopLocation();
        MailManager.getInstance().receiveCommandMail();
        if(CfgParamMgr.getInstance().getGPSinterval()!=CommonParams.GPSinterval)
        {
            CommonParams.GPSinterval = CfgParamMgr.getInstance().getGPSinterval();
            GPSscheduler.getInstance().stopScheduler();
            GPSscheduler.getInstance().startScheduler();
        }
        jobFinished( mParams/*(JobParameters) mJobMsg.obj */, false );
//            startLocation();
    }

    /**
     * 获取GPS状态的字符串
     *
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode) {
        String str = "";
        switch (statusCode) {
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }

    private void initLocation(){
        //初始化client
        if(mLocationClient == null)
        {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        }
        if(mLocationOption == null)
        {
            mLocationOption = new AMapLocationClientOption();
        }
        getDefaultOption();
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 设置定位监听
        mLocationClient.setLocationListener(this);
    }
    /**
     * 默认的定位参数
     */
    private void getDefaultOption(){
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mLocationOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mLocationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mLocationOption.setInterval(CfgParamMgr.getInstance().getGPSinterval()*60*1000);//可选，设置定位间隔。默认单位为1/1000秒
        mLocationOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mLocationOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mLocationOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mLocationOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mLocationOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mLocationOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
    }
    /*检测围栏状态*/
    private boolean checkFence(double tmpLongitude, double tmpLatitude){
        boolean bIsGPSupdated = false;
        double reportLatitude = CfgParamMgr.getInstance().getReportLatitude();
        double reportLongitude = CfgParamMgr.getInstance().getReportLongitude();
        if((reportLatitude!=0)&&(reportLongitude!=0))
        {
            //出界，更新位置
            if((reportLongitude - tmpLongitude >= CommonParams.fenceScope)||
                    (tmpLongitude - reportLongitude >= CommonParams.fenceScope)||
                    (reportLatitude - tmpLatitude >= CommonParams.fenceScope)||
                    (tmpLatitude - reportLatitude >= CommonParams.fenceScope))
            {   //出界，更新位置
                CfgParamMgr.getInstance().setReportLocation(tmpLatitude, tmpLongitude);
                bIsGPSupdated = true;
            }
            else
            {  /* //没出界，但位置变化了，报不报？可能是快要进门回家，也可能是在家里基站变化
                if((reportLongitude != tmpLongitude)||(reportLatitude != tmpLatitude))
                {
                    if((lastLongitude != tmpLongitude)||(lastLatitude != tmpLatitude))
                    { //这次位置和上次位置不一样，暂时先不报
                        CfgParamMgr.getInstance().lastLatitude = tmpLatitude;
                        CfgParamMgr.getInstance().lastLongitude = tmpLongitude;
                    }
                    else
                    {//这次位置和上次位置一样，可以报告新位置了
                        CfgParamMgr.getInstance().lastLatitude = tmpLatitude;
                        CfgParamMgr.getInstance().lastLongitude = tmpLongitude;
                        CfgParamMgr.getInstance().reportLatitude = tmpLatitude;
                        CfgParamMgr.getInstance().reportLongitude = tmpLongitude;
                        bIsGPSupdated = true;
                    }
                }*/
            }
        }
        else
        {   //initialize
            CfgParamMgr.getInstance().setReportLocation(tmpLatitude, tmpLongitude);
            bIsGPSupdated = true;
        }
        return bIsGPSupdated;
    }
}
