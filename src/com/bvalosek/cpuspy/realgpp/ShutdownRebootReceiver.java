package com.bvalosek.cpuspy.realgpp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ShutdownRebootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Thread.currentThread().setUncaughtExceptionHandler(
				new CustomExceptionHandler(context.getApplicationContext(),
						null));
		
		String action = intent.getAction();
		String msg = "";
		if (action != null)
			 msg = ".\nAction occurred: " + action;

		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent appIntent = new Intent("android.intent.action.MAIN");
			intent.setComponent(new ComponentName(
					"com.bvalosek.cpuspy.realgpp",
					"com.bvalosek.cpuspy.realgpp.ui.HomeActivity"));
			appIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
//			context.startActivity(appIntent);
		}
		SharedPreferences settings = context.getSharedPreferences(
				CommonClass.PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		
		CommonClass.myLog(settings, "ShutdownRebootReceiver.onReceive():\n"+msg,
				CommonClass.YES);


		editor.remove(CommonClass.PREF_BATTERY_RESET_TIME);
		editor.remove(CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET);
		editor.commit();

		 msg = "Timer stats variables have been deleted";
		
		CommonClass.myLog(settings, msg,
				CommonClass.YES);
		CommonClass.myLog(settings, "ShutdownRebootReceiver.onReceive(): End"+msg,
				CommonClass.YES);
	}

}
