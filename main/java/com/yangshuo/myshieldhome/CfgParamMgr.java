package com.yangshuo.myshieldhome;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by yangshuo on 2018/2/4.
 */

public class CfgParamMgr {
    private Context mContext = null;
    private boolean bIsServerMode = false;
    private String strMachineName = CommonParams.deviceName;
    private String strDeviceID = "";
    private String strPhoneNumber = "";
    private String strIMEI = "";
    private String strIMSI = "";
    //机器类型,X900,360等等
    private String strMachineType = CommonParams.type360;
    //是否使用系统录音机
    private boolean bUseSysRecorder = false;
    //是否开启定位
    private boolean bStartGPS = true;
    private String strGPSinfoFilename = "";
    private String strGPSmode = CommonParams.TXT_GPS_MODE_DEFAULT;
    private String strGPSmodeFlag = CommonParams.TXT_GPS_MODE_DEFAULT;

    public int dateIndex = 0;
    //当前已报告时间
    public long reportDate = 0L;
    //定位间隔
    private int iGPSinterval = CommonParams.GPSinterval;
    //当前已报告坐标经度
    private double reportLongitude = CommonParams.INVALID_LOCATION;
    //当前已报告坐标纬度
    private double reportLatitude = CommonParams.INVALID_LOCATION;
    private boolean bGPSreportModeDefault = true;
    //TreeMap默认按照升序排列，HashMap不排序　　　https://www.cnblogs.com/avivahe/p/5657070.html
    private Map<Integer, Integer> GPSreportTimeMap = new TreeMap<Integer, Integer>();
    private static CfgParamMgr instance = null;

    public static CfgParamMgr getInstance() {
        if(instance==null)
        {
            instance = new CfgParamMgr();
        }
//        return InstanceHolder.instance;
        return instance;
    }

    private CfgParamMgr() {
        //不能在这里写配置文件，因为这时候还没有mContext,还无法确定DeviceID,DeviceName
//        readCfgFile();
    }

    private static class InstanceHolder {
        private static CfgParamMgr instance = new CfgParamMgr();
    }

    public String getCurrentTime()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH点mm分ss秒");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }
    public String getCurrentDay()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public double getReportLatitude() {
        return reportLatitude;
    }

    public double getReportLongitude() {
        return reportLongitude;
    }

    public void setReportLocation(double tmpLatitude, double tmpLongitude) {
        reportLatitude = tmpLatitude;
        reportLongitude = tmpLongitude;
    }

    public void setServerFlag(boolean bFlag){
        bIsServerMode = bFlag;
    }

    public boolean getServerFlag(){
        return bIsServerMode;
    }

    public void setMachineType(String strType){
        strMachineType = strType;
    }

    public String getMachineType(){
        return strMachineType;
    }

    public void setMachineName(String strName){
        strMachineName = strName;
    }

    public String getMachineName(){
        return strMachineName;
    }

    public String getIMEI() {
        if(strIMEI.isEmpty())
        {
            checkDeviceID();
        }
        return strIMEI;
    }
    public String getIMSI() {
        if(strIMSI.isEmpty()) {
            checkDeviceID();
        }
        return strIMSI;
    }
    public String getPhoneNumber() {
        if(strPhoneNumber.isEmpty()) {
            checkDeviceID();
        }
        return strPhoneNumber;
    }
    public String getDeviceID(){
        if(strDeviceID.isEmpty()) {
            checkDeviceID();
        }
        return strDeviceID;
    }
    private void checkDeviceID(){
        boolean bGranted = false;
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == mContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
                bGranted = true;
            }
        }
        else
        {
            bGranted = true;
        }
//        if(bGranted==true)
        {
            strDeviceID = tm.getDeviceId();//获取智能设备唯一编号
            if(strDeviceID.contains("868062020476223"))
            {
                strMachineName = "乐Max";
                strMachineType = CommonParams.typeX900;
                bIsServerMode = true;
            }
            else if(strDeviceID.contains("867662020947601"))
            {
                strMachineName = "360极客@家";
                strMachineType = CommonParams.type360;
                bUseSysRecorder = true;//TODO: 采用系统录音机
            }
            else if(strDeviceID.contains("868062022152970"))
            {
                strMachineName = "工体乐视";
                strMachineType = CommonParams.typeX900;
            }
            else if(strDeviceID.contains("866647021482345"))
            {
                strMachineName = "备用乐Max";
                strMachineType = CommonParams.typeX900;
                bIsServerMode = true;
            }
            else if(strDeviceID.contains("867602026685227"))
            {
                strMachineName = "荣耀4X";
                strMachineType = "荣耀4X";
            }
            strPhoneNumber = tm.getLine1Number();//获取本机号码
            strIMEI = tm.getSimSerialNumber();//获得SIM卡的序号
            strIMSI = tm.getSubscriberId();//得到用户Id
//            writeCfgFile();
        }
    }

    public void setSysRecorderFlag(boolean bFlag){
        bUseSysRecorder = bFlag;
    }

    public boolean getSysRecorderFlag(){
        return bUseSysRecorder;
    }

    public void setGPSserviceFlag(boolean bFlag){
        bStartGPS = bFlag;
    }

    public boolean getGPSserviceFlag(){
        return bStartGPS;
    }

    public int getGPSinterval(){
        return iGPSinterval;
    }

    public void checkAlarmGPS(boolean bFirst)
    {
        if(bStartGPS)
        {
            CfgParamMgr.getInstance().startAlarm(bFirst);
        }
/*        else
        {
            CfgParamMgr.getInstance().stopAlarm();
        }*/
    }

    public void updateCfgFile(String strCmd){
        //TODO: UPDATE.XXXX
        if(strCmd.contains(CommonParams.CMD_UPDATE))
        {
            String strCfgParam = strCmd.substring(strCmd.indexOf(CommonParams.CMD_UPDATE)+CommonParams.CMD_UPDATE.length()+1);
            if(strCfgParam.contains(CommonParams.PARAM_TYPE))
            {
                strMachineType = strCfgParam.substring(strCfgParam.indexOf(CommonParams.PARAM_TYPE)+CommonParams.PARAM_TYPE.length());
            }
            else if(strCfgParam.contains(CommonParams.PARAM_SYSRECORDER))
            {
                bUseSysRecorder = Boolean.valueOf(strCfgParam.substring(strCfgParam.indexOf(CommonParams.PARAM_SYSRECORDER)+CommonParams.PARAM_SYSRECORDER.length()));
            }
            else if(strCfgParam.contains(CommonParams.PARAM_GPSSERVICE))
            {
                bStartGPS = Boolean.valueOf(strCfgParam.substring(strCfgParam.indexOf(CommonParams.PARAM_GPSSERVICE)+CommonParams.PARAM_GPSSERVICE.length()));
            }
            else if(strCfgParam.contains(CommonParams.PARAM_GPS_INTERVAL))
            {
                iGPSinterval = Integer.valueOf(strCfgParam.substring(strCfgParam.indexOf(CommonParams.PARAM_GPS_INTERVAL)+CommonParams.PARAM_GPS_INTERVAL.length()));
            }
            else if(strCfgParam.contains(CommonParams.PARAM_NAME))
            {
                strMachineName = strCfgParam.substring(strCfgParam.indexOf(CommonParams.PARAM_NAME)+CommonParams.PARAM_NAME.length());
            }
            else if(strCfgParam.contains(CommonParams.PARAM_GPS_MODE))
            {
                strGPSmode = strCfgParam.substring(strCfgParam.indexOf(CommonParams.PARAM_GPS_MODE)+CommonParams.PARAM_GPS_MODE.length());
                parseGPSreportMode(true);
            }
        }
        writeCfgFile();
        String strContent = "command received:<br>";
        strContent += strCmd+"<br>";
        strContent += "current configuration:<br>";
        strContent += CommonParams.PARAM_NAME+strMachineName+"<br>";
        strContent += CommonParams.PARAM_TYPE+strMachineType+"<br>";
        if(bUseSysRecorder)
        {
            strContent += CommonParams.PARAM_SYSRECORDER+"true<br>";
        }
        else
        {
            strContent += CommonParams.PARAM_SYSRECORDER+"false<br>";
        }
        if(bStartGPS)
        {
            strContent += CommonParams.PARAM_GPSSERVICE+"true<br>";
        }
        else
        {
            strContent += CommonParams.PARAM_GPSSERVICE+"false<br>";
        }
        strContent += CommonParams.PARAM_GPS_INTERVAL+String.valueOf(iGPSinterval)+"<br>";
        strContent += CommonParams.PARAM_GPS_MODE+strGPSmode+"<br>";
        strContent += CommonParams.PARAM_GPS_MODE_FLAG+strGPSmodeFlag+"<br>";
        MailManager.getInstance().sendMail(CommonParams.MAIL_TITLE_COMMAND_RECV+getDeviceID()+strMachineName+strCmd, strContent);
    }
    public void writeCfgFile() {
        try {
            File cfgFile = new File(CommonParams.path, CommonParams.cfgFileName);
            OutputStream cfgfos = new FileOutputStream(cfgFile);//默认为覆盖，FileOutputStream(cfgFile,true)是追加
            OutputStreamWriter cfgosw=new OutputStreamWriter(cfgfos);
            cfgosw.write(CommonParams.PARAM_NAME+strMachineName+"\r\n");
            cfgosw.write(CommonParams.PARAM_TYPE+strMachineType+"\r\n");
            if(bUseSysRecorder)
            {
                cfgosw.write(CommonParams.PARAM_SYSRECORDER+"true\r\n");
            }
            else
            {
                cfgosw.write(CommonParams.PARAM_SYSRECORDER+"false\r\n");
            }
            if(bStartGPS)
            {
                cfgosw.write(CommonParams.PARAM_GPSSERVICE+"true\r\n");
            }
            else
            {
                cfgosw.write(CommonParams.PARAM_GPSSERVICE+"false\r\n");
            }
            cfgosw.write(CommonParams.PARAM_GPS_INTERVAL+String.valueOf(iGPSinterval)+"\r\n");
            //写GPSmode
            cfgosw.write(CommonParams.PARAM_GPS_MODE+strGPSmode+"\r\n");
            cfgosw.write(CommonParams.PARAM_GPS_MODE_FLAG+strGPSmodeFlag+"\r\n");
            cfgosw.write(CommonParams.PARAM_REPORT_LATITUDE+String.valueOf(reportLatitude)+"\r\n");
            cfgosw.write(CommonParams.PARAM_REPORT_LONGITUDE+String.valueOf(reportLongitude)+"\r\n");
            cfgosw.flush();
            cfgosw.close();
            cfgfos.close();
        } catch (IOException e) {
            printErrorLog(e);
        }
    }

    public void readCfgFile(){
        if(reportLongitude!=CommonParams.INVALID_LOCATION)
        {
            return;
        }
        try {
            File cfgFile=new File(CommonParams.path,CommonParams.cfgFileName);
            if(cfgFile.exists()==false)
            {
                writeCfgFile();
                return;
            }

            FileInputStream fis=new FileInputStream(cfgFile);
            InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
            BufferedReader bfr=new BufferedReader(isr);
            String in="";
            while((in=bfr.readLine())!=null)//readLine不会读出\r\n
            {
                if(in.contains(CommonParams.PARAM_TYPE))
                {
                    strMachineType = in.substring(in.indexOf("=")+1);
                }
                else if(in.contains(CommonParams.PARAM_NAME))
                {
                    strMachineName = in.substring(in.indexOf("=")+1);
                }
                else if(in.contains(CommonParams.PARAM_SYSRECORDER))
                {
                    if(in.contains("true"))
                    {
                        bUseSysRecorder = true;
                    }
                    else
                    {
                        bUseSysRecorder = false;
                    }
                }
                else if(in.contains(CommonParams.PARAM_GPSSERVICE))
                {
                    if(in.contains("true"))
                    {
                        bStartGPS = true;
                    }
                    else
                    {
                        bStartGPS = false;
                    }
                }
                else if(in.contains(CommonParams.PARAM_GPS_INTERVAL))
                {
                    iGPSinterval = Integer.parseInt(in.substring(in.indexOf("=")+1));
                }
                else if(in.contains(CommonParams.PARAM_GPS_MODE))
                {
                    strGPSmode = in.substring(in.indexOf("=")+1);
                }
                else if(in.contains(CommonParams.PARAM_GPS_MODE_FLAG))
                {
                    strGPSmodeFlag = in.substring(in.indexOf("=")+1);
                    parseGPSreportMode(false);
                }
                else if(in.contains(CommonParams.PARAM_REPORT_LATITUDE))
                {
                    reportLatitude = Double.valueOf(in.substring(in.indexOf("=")+1));
                }
                else if(in.contains(CommonParams.PARAM_REPORT_LONGITUDE))
                {
                    reportLongitude = Double.valueOf(in.substring(in.indexOf("=")+1));
                }
            }
            isr.close();
            fis.close();
        } catch (IOException e) {
            printErrorLog(e);
        }
    }

    public void initContext(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        readCfgFile();
        checkDeviceID();
    }
/* comment out on 2018.4.29
    public void resetGPSmodeFlag(){
        //clear strGPSmodeFlag
        parseGPSreportMode(true);
        writeCfgFile();
    }
*/
    private void startAlarm(boolean bIsFirst){
        if(mContext == null)
        {
            return;
        }
        // 重复定时任务
        // Intent local = new Intent(context, KeepAliveReceiver.class);
        Intent intentAlarm = new Intent("MyShield GPS service");
        intentAlarm.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);// 表示包含未启动的App
        PendingIntent piAlarm = PendingIntent.getBroadcast(mContext, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        am.cancel(piAlarm);
        // 此处必须使用SystemClock.elapsedRealtime，否则闹钟无法接收
        long triggerAtMillis = SystemClock.elapsedRealtime();
        if(bIsFirst==false)
        {
            triggerAtMillis += CfgParamMgr.getInstance().getGPSinterval()*60*1000;
        }
        // pendingIntent 为发送广播
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, piAlarm);
        }
        else
        {
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, piAlarm);
        }
    }
/*
    private void stopAlarm(){
        if(mContext == null)
        {
            return;
        }
        Intent intentAlarm = new Intent("MyShield GPS service");
        intentAlarm.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);// 表示包含未启动的App
        PendingIntent piAlarm = PendingIntent.getBroadcast(mContext, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        am.cancel(piAlarm);
    }
    */

    private int getTimeInteger()
    {
        //取得当前时间
        Calendar cal = Calendar.getInstance();
//        Date date = new Date(System.currentTimeMillis());
//        cal.setTime(date);
//        String year = String.valueOf(cal.get(Calendar.YEAR));
//        String month = String.valueOf(cal.get(Calendar.MONTH))+1;
//        String day = String.valueOf(cal.get(Calendar.DATE));
//        String curSecond = String.valueOf(cal.get(Calendar.SECOND));
        int curHour = 0;
        if (cal.get(Calendar.AM_PM) == 0)
            curHour = cal.get(Calendar.HOUR);
        else
            curHour = cal.get(Calendar.HOUR)+12;
        int curMinute = cal.get(Calendar.MINUTE);
        return curHour*100+curMinute;
    }
    private void parseGPSreportMode(boolean bIsFromCommand)
    {
        //初始化,插入0表示还未使用,1表示已过
        bGPSreportModeDefault = false;
        GPSreportTimeMap.clear();
        if(strGPSmode.isEmpty()||strGPSmode.contains(CommonParams.TXT_GPS_MODE_DEFAULT))
        {
            bGPSreportModeDefault = true;
            strGPSmode = CommonParams.TXT_GPS_MODE_DEFAULT;
            strGPSmodeFlag = CommonParams.TXT_GPS_MODE_DEFAULT;
            return;
        }
        ArrayList<Integer> flagList = new ArrayList<Integer>();
        if(bIsFromCommand==false)
        {
            if(strGPSmodeFlag.isEmpty()==false)
            {
                String[] strArrayFlag = strGPSmodeFlag.split(CommonParams.PATTERN_COMMA_SPLIT);
                for(int i=0;i<strArrayFlag.length;i++)
                {
                    if(strArrayFlag[i].contains("0"))
                    {
                        flagList.add(0);
                    }
                    else
                    {
                        flagList.add(1);
                    }
                }
            }
        }

        //取得设置参数中的时间
//        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
//        Date tmpTime;
        int tmpHour=0, tmpMinute=0;
        ArrayList<Integer> timeList = new ArrayList<Integer>();
        String[] strArray = strGPSmode.split(CommonParams.PATTERN_COMMA_SPLIT);
        for(int i=0; i<strArray.length;i++) {
            if(bIsFromCommand)
            {
                //处理时间如12:30
                if (strArray[i].contains(":")) {
                    String[] strHourArray = strArray[i].split(":");
                    tmpHour = Integer.valueOf(strHourArray[0]);
                    tmpMinute = 0;
                    if (strHourArray.length >= 2) {
                        tmpMinute = Integer.valueOf(strHourArray[1]);
                    }
                } else {
                    tmpHour = Integer.valueOf(strArray[i]);
                    tmpMinute = 0;
                }
                if (tmpHour < 0) {
                    tmpHour = 0;
                } else if (tmpHour > 23) {
                    tmpHour = 23;
                }
                if (tmpMinute < 0) {
                    tmpMinute = 0;
                } else if (tmpMinute > 59) {
                    tmpMinute = 59;
                }
                timeList.add(tmpHour*100+tmpMinute);//2030 means 20:30
            }
            else
            {
                timeList.add(Integer.valueOf(strArray[i]));//2030 means 20:30
            }
        }
        //排序
        Collections.sort(timeList);
        strGPSmode = "";
        strGPSmodeFlag = "";
        // strGPSmode and strGPSmodeFlag are calculated to put in config file
        int curTime = getTimeInteger();
        int tmpTime;
        for(int i=0;i<timeList.size();i++)
        {
            tmpTime = timeList.get(i);
            if(tmpTime<1000)
            {
                strGPSmode+="0";
            }
            strGPSmode+=String.valueOf(tmpTime)+CommonParams.PATTERN_COMMA_SPLIT;
            if(curTime>tmpTime)
            {
                //已经过去的时间窗口，如果是从命令来，过去就不发
                if((flagList.size()==0)||(flagList.size()<=i))
                {
                    GPSreportTimeMap.put(tmpTime, 1);
                    strGPSmodeFlag+="1111"+CommonParams.PATTERN_COMMA_SPLIT;
                }
                else
                {
                    //已经过去的时间窗口，如果是从配置文件来，那么由配置文件决定发不发
                    int tmpValue = flagList.get(i);
                    GPSreportTimeMap.put(tmpTime, tmpValue);
                    if(tmpValue==0)
                    {
                        strGPSmodeFlag+="0000"+CommonParams.PATTERN_COMMA_SPLIT;
                    }
                    else
                    {
                        strGPSmodeFlag+="1111"+CommonParams.PATTERN_COMMA_SPLIT;
                    }
                }
            }
            else
            {
                //还没到的时间窗口，不论从哪里来，都还没发
                GPSreportTimeMap.put(tmpTime, 0);
                strGPSmodeFlag+="0000"+CommonParams.PATTERN_COMMA_SPLIT;
            }
        }
        if(strGPSmode.endsWith(CommonParams.PATTERN_COMMA_SPLIT))
        {//去掉最后一个逗号
            strGPSmode = strGPSmode.substring(0, strGPSmode.lastIndexOf(CommonParams.PATTERN_COMMA_SPLIT));
        }
        if(strGPSmodeFlag.endsWith(CommonParams.PATTERN_COMMA_SPLIT))
        {//去掉最后一个逗号
            strGPSmodeFlag = strGPSmodeFlag.substring(0, strGPSmodeFlag.lastIndexOf(CommonParams.PATTERN_COMMA_SPLIT));
        }
    }

    public boolean getGPSreportFlag(boolean bEscapeFence){
        /*
        String strLog = "enter getGPSreportFlag\r\n" + String.valueOf(bGPSreportModeDefault);
        for (Map.Entry<Integer, Integer> entry : GPSreportTimeMap.entrySet()) {
            strLog += ";"+String.valueOf(entry.getKey());
            strLog += ","+String.valueOf(entry.getValue());
        }
        strLog+= "\r\n";
        */
        //随时报模式时，越界才会报
        if(bGPSreportModeDefault)
        {
            /*
            strLog+= "exit getGPSreportFlag with default\r\n";
            printLog("getGPSreportFlag", strLog);
            */
            return bEscapeFence;
        }
//        strGPSmodeFlag = ""; // comment out on 5.2
        int curTime = getTimeInteger();
        int tmpTime;
        boolean bIsReport = false;
        for (Map.Entry<Integer, Integer> entry : GPSreportTimeMap.entrySet()) {
            /* comment out on 2018.4.29
            if(entry.getValue()!=0)//"1111"
            {//已经报过
                strGPSmodeFlag+="1111"+CommonParams.PATTERN_COMMA_SPLIT;
                continue;
            }
            */
            tmpTime = entry.getKey();
            if(tmpTime<=curTime)
            {
                if(entry.getValue()!=0)//"1111"
                {//已经报过
                    /*
                    strLog+= "already reported: "+ String.valueOf(entry.getKey())+"."+String.valueOf(entry.getValue())+"\r\n";
                    */
                }
                else
                {
                    bIsReport = true;
                /* comment out on 5.2
                    entry.setValue(1);
                */
                /*
                    strLog+= "will report: "+ String.valueOf(entry.getKey())+"."+String.valueOf(entry.getValue())+"\r\n";
                    */
                }
                /* comment out on 5.2
                strGPSmodeFlag+="1111"+CommonParams.PATTERN_COMMA_SPLIT;
                */
            }
            else
            {
                /* comment out on 5.2
                strGPSmodeFlag+="0000"+CommonParams.PATTERN_COMMA_SPLIT;
                */
            }
        }
                /* comment out on 5.2
        if(strGPSmodeFlag.endsWith(CommonParams.PATTERN_COMMA_SPLIT))
        {//去掉最后一个逗号
            strGPSmodeFlag = strGPSmodeFlag.substring(0, strGPSmodeFlag.lastIndexOf(CommonParams.PATTERN_COMMA_SPLIT));
        }
        writeCfgFile();
        */
        /*
        strLog += "prepare to exit getGPSreportFlag\r\n"+String.valueOf(bGPSreportModeDefault);
        for (Map.Entry<Integer, Integer> entry : GPSreportTimeMap.entrySet()) {
            strLog += ";"+String.valueOf(entry.getKey());
            strLog += ","+String.valueOf(entry.getValue());
        }
        strLog += "\r\nstrGPSmodeFlag: "+strGPSmodeFlag+"\r\n";
        printLog("getGPSreportFlag", strLog);
        */
        return bIsReport;
    }

    public void setGPSreportFlag(){
        /*
        String strLog = "enter setGPSreportFlag\r\n" + String.valueOf(bGPSreportModeDefault);
        for (Map.Entry<Integer, Integer> entry : GPSreportTimeMap.entrySet()) {
            strLog += ";"+String.valueOf(entry.getKey());
            strLog += ","+String.valueOf(entry.getValue());
        }
        strLog+= "\r\n";
        */
        if(bGPSreportModeDefault)
        {
            /*
            strLog+= "exit setGPSreportFlag with default\r\n";
            printLog("sent", strLog);
            */
            return;
        }
        strGPSmodeFlag = "";
        int curTime = getTimeInteger();
        int tmpTime;
        for (Map.Entry<Integer, Integer> entry : GPSreportTimeMap.entrySet()) {
            if(entry.getValue()!=0)//"1111"
            {//已经报过
                strGPSmodeFlag+="1111"+CommonParams.PATTERN_COMMA_SPLIT;
            }
            else
            {
                tmpTime = entry.getKey();
                if(tmpTime<=curTime)
                {
                    strGPSmodeFlag+="1111"+CommonParams.PATTERN_COMMA_SPLIT;
                    entry.setValue(1);
                }
                else
                {
                    strGPSmodeFlag+="0000"+CommonParams.PATTERN_COMMA_SPLIT;
                }
            }
        }
        if(strGPSmodeFlag.endsWith(CommonParams.PATTERN_COMMA_SPLIT))
        {//去掉最后一个逗号
            strGPSmodeFlag = strGPSmodeFlag.substring(0, strGPSmodeFlag.lastIndexOf(CommonParams.PATTERN_COMMA_SPLIT));
        }
        writeCfgFile();
        /*
        strLog += "prepare to exit setGPSreportFlag\r\n"+String.valueOf(bGPSreportModeDefault);
        for (Map.Entry<Integer, Integer> entry : GPSreportTimeMap.entrySet()) {
            strLog += ";"+String.valueOf(entry.getKey());
            strLog += ","+String.valueOf(entry.getValue());
        }
        strLog += "\r\nstrGPSmodeFlag: "+strGPSmodeFlag+"\r\n";
        printLog("sent", strLog);
        */
    }
    public String getGPSinfoFilename(){
        strGPSinfoFilename = getDeviceID()+"."+getCurrentDay()+"."+CommonParams.GPSinfoFileName;
        return strGPSinfoFilename;
    }
    public String getGPSreport() {
        String result = CommonParams.TXT_GPS_REPORT_HEADER+"<br>";
        try {
            if(strGPSinfoFilename=="")
            {
                getGPSinfoFilename();
            }
            File GPSinfoFile = new File(CommonParams.path, strGPSinfoFilename);
            if(GPSinfoFile.exists()==false)
            {
                result+=CommonParams.TXT_GPS_REPORT_FOOTER+"<br>";
                return result;
            }

            FileInputStream fis=new FileInputStream(GPSinfoFile);
            InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
            BufferedReader bfr=new BufferedReader(isr);
            String in="";
            while((in=bfr.readLine())!=null)//readLine不会读出\r\n
            {
                result += in+"\n<br>";
            }
            isr.close();
            fis.close();
        } catch (IOException e) {
            printErrorLog(e);
        }
        result+=CommonParams.TXT_GPS_REPORT_FOOTER+"<br>";
        return result;
    }
    public void printLog(String strTitle, String strText)
    {
        try {
            // Make sure the directory exists.
            File file = new File(CommonParams.path, getCurrentTime()+"."+strTitle+".txt");
            OutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos);
            osw.write(strText);
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e) {
        }
    }

    public void printErrorLog(Exception e){
        e.printStackTrace();
        File file = new File(CommonParams.path, getCurrentTime()+"writeFileErrorLog.txt");
        try {
            OutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write("\r\n e.toString()\r\n");
            osw.write(e.toString());
            osw.write("\r\n e.getMessage()\r\n");
            osw.write(e.getMessage());
            osw.write("\r\n e.getLocalizedMessage()\r\n");
            osw.write(e.getLocalizedMessage());
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    /**
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @throws ParseException
     */
    public static boolean IsYesterday(String day) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(CommonParams.PATTERN_DATE_FORMAT);
        Date date = df.parse(day);
        return IsYesterday(date);
    }
    public static boolean IsYesterday(Date date) throws ParseException {

        Calendar pre = Calendar.getInstance();
//        Date predate = new Date(System.currentTimeMillis());
//        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay <= -1) {
                return true;
            }
        }
        return false;
    }
}
