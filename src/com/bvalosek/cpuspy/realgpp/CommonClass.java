package com.bvalosek.cpuspy.realgpp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.bvalosek.cpuspy.realgpp.ui.HomeActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 
 * @author realgpp. Created 20/giu/2012.
 */
public class CommonClass extends Activity {
	public static final String TAG = "CpuSpyRealgpp";
	public final static int YES = 1;
	public static final int NO = 0;
	public final static int YES2 = 0;
	public static final int NO2 = 1;
	
	public static final int OPT_DISABLE_BROADCAST_RECEIVER = YES;

	public static final String AFFECTED_CPUS_PATH_CPU0 = "/sys/devices/system/cpu/cpu0/cpufreq/affected_cpus";
	public static final String AFFECTED_CPUS_PATH_CPU1 = "/sys/devices/system/cpu/cpu1/cpufreq/affected_cpus";
	public static String TIME_IN_STATE_PATH_CORE0 = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
	public static String TIME_IN_STATE_PATH_CORE1 = "/sys/devices/system/cpu/cpu1/cpufreq/stats/time_in_state";
	public static final String LOG_FILE = "CpuSpy.log";

	public static final String PREF_NAME = "CpuSpyPreferences";
	public static final String PREF_ENABLE_BATTERY_EVENTS_RECEIVER = "pref_battery_events";
	public static final String PREF_UPDATING_DATA = "pref_updating_data";
	public static final String PREF_BATTERY_LEVEL_BEFORE_RESET = "pref_battery_before_reset";
	public static final String PREF_BATTERY_RESET_TIME = "pref_battery_reset_time";
	public static final String PREF_SHOW_1PERC_STAT = "pref_show_1perc_stat";
	public static final String PREF_APP_VERSION = "pref_version_app";
	public static final String PREF_SHOW_1PERC_IN_TIME_IN_STATE_LAYOUT = "pref_show_1perc_in_time_in_state_layout";
	public static final String PREF_SHOW_1PERC_STATES = "pref_show_1perc_states";

	/*
	 * Preference string used to get/understand if I'm waiting for charging
	 * event or not. And activate the right behaviour according to this.
	 */
	public static final String PREF_WAITING_DISCHARGING_EVENT = "pref_waiting_discharging_event";

	/*
	 * Strings to get values setted in Preference class
	 */
	public static final String PREF_THEME_SELECTION = "pref_theme_selection";
	public static final String THEME_NO_BUTTON = "Classic Theme";
	public static final String THEME_WITH_BUTTON = "New Theme";

	public final static String PREF_VERBOSE_KEY = "pref_verbose_key";
	public final static String PREF_LESS1PERC_BAR_GRAPH_TOP = "pref_less1perc_bar_graph_top";
	public final static String PREF_ENABLE_UI_SOUNDS = "pref_enable_ui_sounds";
	public final static String PREF_ENABLE_UI_PREF_SOUNDS = "pref_enable_ui_pref_sounds";
	public final static String PREF_ENABLE_MISC_ACTIVITY = "pref_enable_misc_activity";

	static public int VERBOSE = NO;

	@SuppressWarnings("unused")
	public static boolean existLogFile() {
		boolean logfile_exist = false;
		try {
			FileInputStream f = new FileInputStream(
					Environment.getExternalStorageDirectory() + "/"
							+ CommonClass.LOG_FILE);
			logfile_exist = true;
		} catch (FileNotFoundException exception) {
			Log.i("existLogFile()", "No logfile generated so on");
			logfile_exist = false;
		}
		return logfile_exist;
	}

	public static void myLog(SharedPreferences settings, String msg,
			int isDateOn) {
		if (settings.getBoolean(CommonClass.PREF_VERBOSE_KEY, false) == true) {
			Log.v(CommonClass.TAG, msg);

			PrintWriter pw;
			try {
				if (!existLogFile()) {
					pw = new PrintWriter(new FileWriter(
							Environment.getExternalStorageDirectory() + "/"
									+ CommonClass.LOG_FILE, true));

					StringBuilder message = new StringBuilder();
					addInformation(message);
					pw.write(message + "\n");
					pw.flush();
					pw.close();
				}

				pw = new PrintWriter(new FileWriter(
						Environment.getExternalStorageDirectory() + "/"
								+ CommonClass.LOG_FILE, true));
				/*
				 * LOG LINE TO FILE
				 */
				String date = "";
				if (isDateOn == CommonClass.YES) {
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat df = new SimpleDateFormat(
							"dd-MM-yyyy HH:mm:ss");
					date = df.format(cal.getTime()) + ": ";
				}

				pw.write(date + msg + "\n");
				pw.flush();
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static public void addInformation(StringBuilder message) {
		Context appContext = HomeActivity.getAppContext();
		if (appContext != null)
			try {
				PackageManager pm = appContext.getPackageManager();
				PackageInfo pi;
				pi = pm.getPackageInfo(appContext.getPackageName(), 0);
				message.append("Version: ").append(pi.versionName).append('\n');
				message.append("Package: ").append(pi.packageName).append('\n');
			} catch (Exception e) {
				Log.e("CustomExceptionHandler", "Error", e);
				message.append("Could not get Version information for ")
						.append(appContext.getPackageName());
			}

		message.append("Phone Model: ").append(android.os.Build.MODEL)
				.append('\n');
		message.append("Android Version: ")
				.append(android.os.Build.VERSION.RELEASE).append('\n');
		message.append("Board: ").append(android.os.Build.BOARD).append('\n');
		message.append("Brand: ").append(android.os.Build.BRAND).append('\n');
		message.append("Device: ").append(android.os.Build.DEVICE).append('\n');
		message.append("Host: ").append(android.os.Build.HOST).append('\n');
		message.append("ID: ").append(android.os.Build.ID).append('\n');
		message.append("Model: ").append(android.os.Build.MODEL).append('\n');
		message.append("Product: ").append(android.os.Build.PRODUCT)
				.append('\n');
		message.append("Type: ").append(android.os.Build.TYPE).append('\n');
		StatFs stat = getStatFs();
		message.append("Total Internal memory: ")
				.append(getTotalInternalMemorySize(stat)).append('\n');
		message.append("Available Internal memory: ")
				.append(getAvailableInternalMemorySize(stat)).append('\n');
	}

	static public StatFs getStatFs() {
		File path = Environment.getDataDirectory();
		return new StatFs(path.getPath());
	}

	static public long getAvailableInternalMemorySize(StatFs stat) {
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	static public long getTotalInternalMemorySize(StatFs stat) {
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

}
