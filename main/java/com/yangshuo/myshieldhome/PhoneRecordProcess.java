package com.yangshuo.myshieldhome;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.yangshuo.myshieldhome.speech.AudioDecode;
import com.yangshuo.myshieldhome.speech.FucUtil;
import com.yangshuo.myshieldhome.speech.JsonParser;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.iflytek.cloud.VerifierResult.TAG;

/**
 * Created by yangshuo on 2018/4/28.
 */

public class PhoneRecordProcess {
    private SpeechUtility mSpeech;
    // 用HashMap存储听写结果
    private HashMap<String,String> mIatResults = new LinkedHashMap<>();
    private SpeechRecognizer mIat;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private AudioDecode audioDecode;
    private Context mContext = null;
    private String strSourceFileName = "";
    private File outputFile;
    private OutputStream outputfos;
    private BufferedOutputStream bos;
    private boolean bIsWriteFile = false;
    private boolean bIsTestButton = false;
    private String strDecodeResult = "";
    private String strDecodeFileName = "";
    private String strMailTitle = "";
    private String strMailContent = "";

    private PowerManager.WakeLock gpsWakeLock = null;
    private void acquireWakeLock() {
        if (gpsWakeLock ==null) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            gpsWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            gpsWakeLock.acquire();
        }
    }
    private void releaseWakeLock() {
        if (gpsWakeLock !=null&& gpsWakeLock.isHeld()) {
            gpsWakeLock.release();
            gpsWakeLock =null;
        }
    }

    //听写监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        //volume音量值0~30，data音频数据
        @Override
        public void onVolumeChanged(int volume, byte[] bytes) {
            //       showTip("当前正在说话，音量大小：" + volume);
//            Log.d(TAG, "返回音频数据："+bytes.length);
        }
        //开始录音
        // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        @Override
        public void onBeginOfSpeech() {
            //     showTip("开始说话");
        }
        //结束录音
        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //   showTip("结束说话");
        }
        /**
         * 听写结果回调接口,返回Json格式结果
         * 一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加
         * isLast等于true时会话结束。
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {
            Log.d(TAG, recognizerResult.getResultString());
            printResult(recognizerResult);
            if(isLast)
            {
                printLog("识别结束","");
                strDecodeResult+="<br>\r\n识别结束";
                DecodeComplete(true);
            }
        }
        //会话发生错误回调接口
        // Tips：
        // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
        @Override
        public void onError(SpeechError speechError) {
            //打印错误码描述
            Log.d(TAG, "error:" + speechError.getPlainDescription(true));
//            showTip(speechError.getPlainDescription(true));
            printLog("error",speechError.getPlainDescription(true));
            if(mSpeech==null)
            {
                printLog("error.SpeechUtilityNull", "null");
            }
            if(mIat==null)
            {
                printLog("error.mIatNull", "null");
            }
            strDecodeResult+="<br>\r\n"+speechError.getPlainDescription(true);
            DecodeComplete(false);
        }
        //扩展用接口
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {
        }
    };
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
//                showTip("初始化失败，错误码：" + code);
                printLog("初始化失败", String.valueOf(code));
            }
        }
    };

    public PhoneRecordProcess(){
    }

    public void init(Context tmpContext, boolean bButton){
        bIsTestButton =bButton;
        mContext = tmpContext;
        //创建语音配置对象，换成自己的id，另外so文件也要换成自己的，AndroidManifest里面还有个统计分析的id也换成自己的
//        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID+"=5ae01196");

        mSpeech = SpeechUtility.createUtility(mContext, SpeechConstant.APPID+"="+mContext.getString(R.string.app_id));
        if(mSpeech==null)
        {
            printLog("SpeechUtilityNull", "null");
        }
        //1、创建SpeechRecognizer对象，第二个参数：本地识别时传InitListener
//        mIat = SpeechRecognizer.createRecognizer(mContext,null);
        mIat = SpeechRecognizer.createRecognizer(mContext,mInitListener);
        if(mIat==null)
        {
            printLog("mIatNull", "null");
        }
        setParam();
        if(bIsWriteFile)
        {
            try {
                // Make sure the directory exists.
                outputFile = new File(CommonParams.path, "1.wav");
                outputfos = new FileOutputStream(outputFile);
                bos = new BufferedOutputStream(outputfos);
            } catch (IOException e) {
            }
        }
        if(bIsTestButton)
        {
            startDecode("1.amr");
        }
    }

    public void VoiceDecode(String strFilename, String strTitle, String strContent){
        strDecodeFileName = strFilename;
        strMailTitle = strTitle;
        strMailContent = strContent;
        //decode结果跟在传入参数后面返回
        startDecode(strDecodeFileName);
    }

    public void clear()
    {
        if( null != mIat ){
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
        if( null != mSpeech ){
            mSpeech.destroy();
        }
    }

    private void DecodeComplete(boolean bIsDecodeSucceed)
    {
        clear();
        if(bIsTestButton)
        {
            return;
        }
        if(bIsDecodeSucceed)
        {

        }
        else
        {

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
        strCallContent += "<br>Call Record:"+strDecodeFileName;
        // 如果接受过更新设备名称的指令，那么在这里更新，确保设备名称正确显示
        strMailTitle = strMailTitle.replaceAll(CommonParams.deviceName, CfgParamMgr.getInstance().getMachineName());
        strMailContent = strMailContent.replaceAll(CommonParams.deviceName, CfgParamMgr.getInstance().getMachineName());
        strCallContent+= strDecodeResult;

        MailManager.getInstance().sendMailWithMultiFile(strMailTitle+ ".时间"+strDecodeFileName, strMailContent+strCallContent, pathList);
        if(CfgParamMgr.getInstance().getGPSserviceFlag()==false)
        {
            //如果没开GPS，需要检查命令。如果GPS已经开着，每次上报时会检查命令
            MailManager.getInstance().receiveCommandMail();
        }
        strDecodeFileName = "";
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

        releaseWakeLock();

    }

    // 参数设置
    private void setParam(){
        //参数设置
        /**
         * 应用领域 服务器为不同的应用领域，定制了不同的听写匹配引擎，使用对应的领域能获取更 高的匹配率
         * 应用领域用于听写和语音语义服务。当前支持的应用领域有：
         * 短信和日常用语：iat (默认)
         * 视频：video
         * 地图：poi
         * 音乐：music
         */
        mIat.setParameter(SpeechConstant.DOMAIN,"iat");
        /**
         * 在听写和语音语义理解时，可通过设置此参数，选择要使用的语言区域
         * 当前支持：
         * 简体中文：zh_cn（默认）
         * 美式英文：en_us
         */
        mIat.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
        /**
         * 每一种语言区域，一般还有不同的方言，通过此参数，在听写和语音语义理解时， 设置不同的方言参数。
         * 当前仅在LANGUAGE为简体中文时，支持方言选择，其他语言区域时， 请把此参数值设为null。
         * 普通话：mandarin(默认)
         * 粤 语：cantonese
         * 四川话：lmz
         * 河南话：henanese
         */
        mIat.setParameter(SpeechConstant.ACCENT,"mandarin");
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置语音前端点：静音超时时间，即用户多长时间不说话则当做超时处理
        //默认值：短信转写5000，其他4000
        mIat.setParameter(SpeechConstant.VAD_BOS,"30000");//4000
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS,"30000");//1000
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT,"1");
        // 设置音频保存路径，保存音频格式支持pcm、wav
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        //mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
        //文本，编码
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
    }

    /**
     * 工具类
     * @param audioPath
     */
    //语音转换
    private void audioDecodeFun(String audioPath){
        printLog("开始转换", "");
        acquireWakeLock();

        audioDecode = AudioDecode.newInstance();
        audioDecode.setFilePath(audioPath);
        audioDecode.prepare();
        audioDecode.setOnCompleteListener(new AudioDecode.OnCompleteListener() {
            @Override
            public void completed(final ArrayList<byte[]> pcmData) {
                if(pcmData!=null){
                    printLog("转换结束", "");
                    int ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        //        showTip("识别失败,错误码：" + ret);
                        printLog("识别失败", String.valueOf(ret));
                    } else {
                    }
                    //写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
                    //必须要先保存到本地，才能被讯飞识别
                    //为防止数据较长，多次写入,把一次写入的音频，限制到 64K 以下，然后循环的调用wirteAudio，直到把音频写完为止
                    //这里每次data.length都是320
                    for (byte[] data : pcmData){
//                        mIat.writeAudio(data, 0, data.length);
                        //写32或者64识别出的效果不一致,直接320则报网络错误
                        if(bIsWriteFile==false) {
                            ArrayList<byte[]> arrayAudioData = FucUtil.splitBuffer(data, data.length, 64000);
                            for (int i = 0; i < arrayAudioData.size(); i++) {
                                mIat.writeAudio(arrayAudioData.get(i), 0, arrayAudioData.get(i).length);
                            }
                        }
                        if(bIsWriteFile) {
                            try
                            {
                                bos.write(data);
                            } catch (IOException e) {
                            }
                        }
                    }

/*                    int startIdx = 0;
                    int sndTime = 0;
                    byte[] putData = new byte[64000];
                    for(int i=0;i<pcmData.size();i++)
                    {
                        byte[] data = pcmData.get(i);
                        for(int j=0;j<data.length;j++)
                        {
                            putData[startIdx+j] = data[j];
                        }
                        startIdx+=data.length;
                        if(startIdx>=63000)
                        {
                            mIat.writeAudio(putData, 0, startIdx);
                            startIdx = 0;
                            sndTime++;
                        }
                    }
                    printLog("write结束"+String.valueOf(sndTime), "");
*/
//                    Log.d("-----------stop",System.currentTimeMillis()+"");
                    mIat.stopListening();
                }else{
                    mIat.cancel();
//                    Log.d(TAG,"--->读取音频流失败");
                    printLog("读取数据失败", "");
                }
                if(bIsWriteFile) {
                    try {
                        bos.flush();
                        bos.close();
                        outputfos.close();
                    } catch (IOException e) {
                    }
                }
                audioDecode.release();
                if(bIsWriteFile) {
                    iatFun();
                }
            }
        });
        audioDecode.startAsync();
    }
    /**
     * 讯飞
     */
    private void iatFun(){
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            //        showTip("识别失败,错误码：" + ret);
            printLog("识别失败", String.valueOf(ret));
        } else {
        }
        byte[] audioData = FucUtil.readAudioFile(mContext, "1.wav");
        if (null != audioData) {
//            showTip("开始识别");
            printLog("IAT开始识别", "");
            // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
            // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
            // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
            // 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
//            mIat.writeAudio(audioData, 0, audioData.length);
            ArrayList<byte[]> arrayAudioData = FucUtil.splitBuffer(audioData,audioData.length,64000);
            for(int i=0;i<arrayAudioData.size();i++)
            {
                mIat.writeAudio(arrayAudioData.get(i), 0, arrayAudioData.get(i).length);
            }
            mIat.stopListening();
        } else {
            mIat.cancel();
//            showTip("读取音频流失败");
            printLog("IAT读取音频流失败", "");
        }
    }
    private void printResult(RecognizerResult recognizerResult) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        String sn = null;
        //读取Json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
            sn = resultJson.optString("sn");
        }catch (Exception e){
            printLog("fail", e.toString());
            e.printStackTrace();
        }
        mIatResults.put(sn,text);
        StringBuilder sb = new StringBuilder();
        for (String key:mIatResults.keySet()){
            sb.append(mIatResults.get(key));
        }
        strDecodeResult = sb.toString();
        printLog("speech", sb.toString());
//        et_voice_text.setText(sb.toString());
//        et_voice_text.setSelection(et_voice_text.length());
    }


    private void startDecode(String srcFileName) {
        if(srcFileName.isEmpty())
        {
            return;
        }
        strSourceFileName = srcFileName;
        mIatResults.clear();
        setParam();
        // 设置音频来源为外部文件
        String audioPath = CommonParams.path.toString() + File.separator + strSourceFileName; //***.amr
//        String mFileName2 = CommonParams.path.toString() + File.separator + "test_temp.wav";
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        mIat.setParameter(SpeechConstant.SAMPLE_RATE, "8000");//设置正确的采样率
//        mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, mFileName2);
//        mIat.startListening(mRecognizerListener);
        //主要参数： -i 设定输入流 -f 设定输出格式 -ss 开始时间
        // 视频参数：
        // -b 设定视频流量，默认为200Kbit/s -r 设定帧速率，
        // 默认为25 -s 设定画面的宽与高 -aspect 设定画面的比例
        // -vn 不处理视频 -vcodec 设定视频编解码器，未设定时则使用与输入流相同的编解码器
        // 音频参数：
        // -ar 设定采样率 -ac 设定声音的Channel数 -acodec 设定声音编解码器，未设定时则使用与输入流相同的编解码器 -an 不处理音频
        /*
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            //        showTip("识别失败,错误码：" + ret);
            printLog("识别失败", String.valueOf(ret));
        } else {
//            iatFun();//讯飞demo里面的方法
            audioDecodeFun(audioPath);
        }
        */
        audioDecodeFun(audioPath);
    }
    private void printLog(String strTitle, String strText)
    {
        try {
            // Make sure the directory exists.
            File file = new File(CommonParams.path, CfgParamMgr.getInstance().getCurrentTime()+strTitle+".txt");
            OutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos);
            osw.write(strText);
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e) {
        }
    }

}
