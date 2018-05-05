package com.yangshuo.myshieldhome.speech;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.yangshuo.myshieldhome.CommonParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 功能性函数扩展类
 */
public class FucUtil {

    public static String readFile(Context mContext, String file, String code) {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try {
            InputStream in = new FileInputStream(CommonParams.path.toString() + File.separator + "official.wav");
//            InputStream in = mContext.getClass().getClassLoader().getResourceAsStream("assets/"+file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取asset目录下音频文件。
     *
     * @return 二进制文件数据
     */
    public static byte[] readAudioFile(Context context, String filename) {
        try {
//            InputStream ins = context.getAssets().open(filename);
            InputStream ins = new FileInputStream(CommonParams.path.toString() + File.separator + filename);
            byte[] data = new byte[ins.available()];

            ins.read(data);
            ins.close();

            return data;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
    /**
     * 将字节缓冲区按照固定大小进行分割成数组
     * @param buffer 缓冲区
     * @param length 缓冲区大小
     * @param spsize 切割块大小
     * @return
     */
    public static ArrayList<byte[]> splitBuffer(byte[] buffer, int length, int spsize)
    {
        ArrayList<byte[]> array = new ArrayList<byte[]>();
        if(spsize <= 0 || length <= 0 || buffer == null || buffer.length < length)
            return array;
        int size = 0;
        while(size < length)
        {
            int left = length - size;
            if(spsize < left)
            {
                byte[] sdata = new byte[spsize];
                System.arraycopy(buffer,size,sdata,0,spsize);
                array.add(sdata);
                size += spsize;
            }else
            {
                byte[] sdata = new byte[left];
                System.arraycopy(buffer,size,sdata,0,left);
                array.add(sdata);
                size += left;
            }
        }
        return array;
    }
}
