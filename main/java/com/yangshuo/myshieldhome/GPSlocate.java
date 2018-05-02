package com.yangshuo.myshieldhome;

import android.content.Context;
import android.os.PowerManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by yangshuo on 2018/4/3
 */

public class GPSlocate implements AMapLocationListener{
    private PowerManager.WakeLock gpsWakeLock = null;
    private Context mContext = null;
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    private AMapLocationClientOption mLocationOption = null;
    private static GPSlocate instance = null;

    public static GPSlocate getInstance() {
        if(instance==null)
        {
            instance = new GPSlocate();
            //初始化,这样重启后上线就会报告
//            CfgParamMgr.getInstance().reportLatitude = 0D;
//            CfgParamMgr.getInstance().reportLongitude = 0D;
        }
//        return InstanceHolder.instance;
        return instance;
    }

    //TODO: 没找到在哪里调用合适?
    public void destroyLocation(){
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

    private void acquireWakeLock() {
        if (gpsWakeLock ==null) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            gpsWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            gpsWakeLock.acquire();
        }
    }
    public void releaseWakeLock() {
        if (gpsWakeLock !=null&& gpsWakeLock.isHeld()) {
            gpsWakeLock.release();
            gpsWakeLock =null;
        }
    }

    public void startGPS(Context context) {
        if(mContext == null)
        {
            mContext = context;
        }
        acquireWakeLock();
        initLocation();
        // 启动定位
        mLocationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        boolean bRecvCommand = false;
        if (amapLocation != null) {
            StringBuffer sb = new StringBuffer();
            StringBuffer sbGPS = new StringBuffer();
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            if (amapLocation.getErrorCode() == 0) {
                sb.append("定位成功." + CfgParamMgr.getInstance().getMachineName()+ "<br>\n");
                sb.append("时间间隔: " + mLocationOption.getInterval()/60000 + "分钟    ");
                sb.append("定位类型: " + amapLocation.getLocationType() + "<br>\n");//获取当前定位结果来源，如网络定位结果，详见定位类型表
                sb.append("经    度    : " + amapLocation.getLongitude() + "    ");
                sb.append("纬    度    : " + amapLocation.getLatitude() + "<br>\n");
                sb.append("精    度    : " + amapLocation.getAccuracy() + "米" + "    ");
                sb.append("提供者    : " + amapLocation.getProvider() + "<br>\n");
                sb.append("速    度    : " + amapLocation.getSpeed() + "米/秒" + "    ");
                sb.append("角    度    : " + amapLocation.getBearing() + "    ");
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : " + amapLocation.getSatellites() + "<br>\n");
                sb.append("国    家    : " + amapLocation.getCountry() + "    ");
                sb.append("省            : " + amapLocation.getProvince() + "    ");
                sb.append("市            : " + amapLocation.getCity() + "    ");
                sb.append("城市编码 : " + amapLocation.getCityCode() + "<br>\n");
                sb.append("区            : " + amapLocation.getDistrict() + "    ");
                sb.append("区域 码   : " + amapLocation.getAdCode() + "<br>\n");
                sb.append("地    址    : " + amapLocation.getAddress() + "<br>\n");
                sb.append("兴趣点    : " + amapLocation.getPoiName() + "<br>\n");
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
            //定位完成的时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(amapLocation.getTime());
            sb.append("定位时间: " + df.format(date) + "<br>\n");
            sb.append("****************").append("<br>\n");
            //解析定位结果，
            String result = sb.toString();
            /*检测围栏状态*/
            //这里读配置文件，只是为了调试时，可以让时间表有更新的机会，否则会一直用内存里的时间表
            CfgParamMgr.getInstance().readCfgFile();//读进状态
            boolean bEscapeFence = checkFence(amapLocation.getLongitude(), amapLocation.getLatitude());
            if(bEscapeFence) {
                //TODO: 写在文件里
                sbGPS.append(CommonParams.SUB_PARAM_TIME + getCurrentDate() + CommonParams.PATTERN_COMMA_SPLIT + CommonParams.SUB_PARAM_LONGITUDE + amapLocation.getLongitude() + CommonParams.PATTERN_COMMA_SPLIT + CommonParams.SUB_PARAM_LATITUDE + amapLocation.getLatitude() + "\r\n");
                writeGPSinfoFile(sbGPS.toString());
            }
            if(CfgParamMgr.getInstance().getGPSreportFlag(bEscapeFence))
            {
                result+=CfgParamMgr.getInstance().getGPSreport();
                String strMailTitle = CommonParams.MAIL_TITLE_GPS_LOCATE_SUCCEED+CfgParamMgr.getInstance().getMachineName()+".设备ID." + CfgParamMgr.getInstance().getDeviceID()+".时间"+ df.format(date);
                List<String> pathList = getPathList();
                if(pathList.size()>1)
                {
                    strMailTitle+=CommonParams.MAIL_TITLE_GPS_INFO_MULTI;
                }
                MailManager.getInstance().sendGPSinfoMailWithMultiFile(strMailTitle, result, pathList);
                bRecvCommand = true;
            }
        } else {
            MailManager.getInstance().sendMail(CommonParams.MAIL_TITLE_GPS_LOCATE_FAILED+CfgParamMgr.getInstance().getMachineName()+".设备ID." + CfgParamMgr.getInstance().getDeviceID(), CfgParamMgr.getInstance().getMachineName());
        }
        if(bRecvCommand)
        {
            MailManager.getInstance().receiveCommandMail();
        }
        else
        {
            releaseWakeLock();
        }
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
            mLocationClient = new AMapLocationClient(mContext);
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
        if((tmpLongitude==0)||(tmpLatitude==0))
        {
            return false;
        }
        boolean bIsGPSupdated = false;
        double reportLatitude = CfgParamMgr.getInstance().getReportLatitude();
        double reportLongitude = CfgParamMgr.getInstance().getReportLongitude();
        if((reportLatitude!=0)&&(reportLongitude!=0)&&(reportLatitude!=CommonParams.INVALID_LOCATION)&&(reportLongitude!=CommonParams.INVALID_LOCATION))
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
        }
        else
        {   //initialize
            CfgParamMgr.getInstance().setReportLocation(tmpLatitude, tmpLongitude);
            bIsGPSupdated = true;
        }
        return bIsGPSupdated;
    }

    /* 记录GPS定位信息 */
    private void writeGPSinfoFile(String strGPSinfo){
        boolean bIsAppend = true;
        try {
            File GPSinfoFile = new File(CommonParams.path, CfgParamMgr.getInstance().getGPSinfoFilename());
            if(GPSinfoFile.exists()==true)
            {
                String strDate = "";
                FileInputStream fis=new FileInputStream(GPSinfoFile);
                InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
                BufferedReader bfr=new BufferedReader(isr);
                String in="";
                while((in=bfr.readLine())!=null)//readLine不会读出\r\n
                {
                    if(in.contains(CommonParams.SUB_PARAM_TIME))
                    {
                        strDate = in.substring(in.indexOf("=")+1, in.indexOf(","));
                        break;
                    }
                }
                isr.close();
                fis.close();
                if(CfgParamMgr.getInstance().IsYesterday(strDate))
                {
                    bIsAppend = false;
                }
            }
            /* comment out on 2018.4.29
            else
            {// this is a new file, in new date, reset all reportFlag to 0000
                CfgParamMgr.getInstance().resetGPSmodeFlag();

            }
            */
            OutputStream cfgfos = new FileOutputStream(GPSinfoFile, bIsAppend);//默认为覆盖，FileOutputStream(cfgFile,true)是追加
            OutputStreamWriter cfgosw=new OutputStreamWriter(cfgfos);
            cfgosw.write(strGPSinfo);
            cfgosw.flush();
            cfgosw.close();
            cfgfos.close();

        } catch (Exception e) {
            CfgParamMgr.getInstance().printErrorLog(e);
        }
    }

    private List<String> getPathList()
    {
        List<String> pathList = new ArrayList<String>();
        File[] files = new File(CommonParams.path.getAbsolutePath()).listFiles();
        for (File file : files) {
            if((file.getName().endsWith(CommonParams.GPSinfoFileName))&&(file.getName().startsWith(CfgParamMgr.getInstance().getDeviceID())))
            {
                if(file.isFile()) {
                    String tmpFilename = CommonParams.path.getAbsolutePath() + File.separator + file.getName();
                    pathList.add(tmpFilename);

                }
            }
        }
        //所有GPS文件都发，除今天的GPS文件以外，都清除
        try {
            File file = new File(CommonParams.path, CommonParams.GPSinfoPendingListName);
            OutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos);
            for(int i=0;i<pathList.size();i++)
            {
                String tmpFilename = pathList.get(i);
                if(tmpFilename.contains(CfgParamMgr.getInstance().getGPSinfoFilename()))
                {
                    //今天的新文件不删
                    continue;
                }
                osw.write(tmpFilename);
                osw.write("\r\n");
            }
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e) {
        }
        return pathList;
    }

    private String getCurrentDate()
    {
        SimpleDateFormat formatter = new SimpleDateFormat(CommonParams.PATTERN_DATE_FORMAT);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }
}
