package com.yangshuo.myshieldhome;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

/**
 * Created by yangshuo on 2018/2/16.
 */

// JobScheduler  http://blog.csdn.net/bboyfeiyu/article/details/44809395
// 安卓6.0注意事项  https://www.jianshu.com/p/1f919c6eeff6
public class GPSscheduler {
    private JobScheduler mJobScheduler = null;
    private Context mContext = null;
    private static GPSscheduler instance = null;
    private boolean bIsJobStarted = false;

    public static GPSscheduler getInstance() {
        if(instance==null)
        {
            instance = new GPSscheduler();
        }
//        return InstanceHolder.instance;
        return instance;
    }
    public void setContext(Context context){
        if(mContext == null)
        {
            mContext = context;
        }
    }

    public boolean getJobStatus(){
        return bIsJobStarted;
    }

    public void startScheduler(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //高于安卓5.0，使用JobScheduler
            mJobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1,
                    new ComponentName(mContext.getPackageName(), GPSservice.class.getName()));

            builder.setPeriodic(CfgParamMgr.getInstance().getGPSinterval()*60*1000);
            //builder.setRequiresCharging(true);//只有充电时才会调用
            builder.setPersisted(true);  //设置设备重启后，是否重新执行任务
            //builder.setRequiresDeviceIdle(true);//只有闲置时才会调用

            if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
                //If something goes wrong
            }
            bIsJobStarted = true;
        }
    }

    public void stopScheduler(){
        mJobScheduler.cancelAll();
        bIsJobStarted = false;
    }
}
