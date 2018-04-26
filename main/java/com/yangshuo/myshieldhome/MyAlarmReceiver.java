package com.yangshuo.myshieldhome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by yangshuo on 2018/4/3.
 */

public class MyAlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_SEND = "MyShield GPS service";
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if(action.equals("MyShield GPS service")){
                    // 定时闹钟发送的保活心跳
                    // 再次发送闹钟
                    setKeepAliveAlarm(context);
                    CfgParamMgr.getInstance().initContext(context);
                    CfgParamMgr.getInstance().checkAlarmGPS(false);
                    // 处理自己的逻辑
                    GPSlocate.getInstance().startGPS(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setKeepAliveAlarm(Context context) {
    }
}
