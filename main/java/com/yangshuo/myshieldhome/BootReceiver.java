package com.yangshuo.myshieldhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * 监听android开机广播（只要手机一开机就开始监听）
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//	    CfgParamMgr.getInstance().readCfgFile();
        Bundle paramBundle = new Bundle();
//        paramBundle.putString(CommonParams.PARAM_TYPE, CfgParamMgr.getInstance().getMachineType());
//        paramBundle.putBoolean(CommonParams.PARAM_SYSRECORDER,CfgParamMgr.getInstance().getSysRecorderFlag());
        paramBundle.putBoolean(CommonParams.TXT_ONCREATE,true);
        Intent i = new Intent(context,MyShieldService.class);
        i.putExtras(paramBundle);
        context.startService(i);
  }
}
