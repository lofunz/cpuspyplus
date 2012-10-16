package com.bvalosek.cpuspy.realgpp;

import com.bvalosek.cpuspy.realgpp.CpuStateMonitor.CpuStateMonitorException;
import com.bvalosek.cpuspy.realgpp.ui.HomeActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.preference.PreferenceManager;

/**
 * This is the broadcast receiver that reacts to cable events: (un)plugging
 * 
 */
public class ChargerReceiver extends BroadcastReceiver {
	CpuSpyApp _app = null;

	@Override
	public void onReceive(Context context, Intent intent) {

		Thread.currentThread().setUncaughtExceptionHandler(
				new CustomExceptionHandler(context.getApplicationContext(),
						null));

		this._app = (CpuSpyApp) context.getApplicationContext();

		SharedPreferences settings = context.getSharedPreferences(
				CommonClass.PREF_NAME, Context.MODE_PRIVATE);

		CommonClass.myLog(settings, "\nChargeReceiver.onReceive() - Begin",
				CommonClass.YES);

		boolean cablePlugged = false;
		boolean cableUnplugged = false;


		String action = intent.getAction();
		if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
			CommonClass.myLog(settings, "\nACTION_POWER_CONNECTED received",
					CommonClass.NO);
			cablePlugged = true;
		} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
			CommonClass.myLog(settings, "\nACTION_POWER_DISCONNECTED received",
					CommonClass.NO);
			cableUnplugged = true;
		}

		if (settings.getInt(CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
				CommonClass.NO) == CommonClass.YES) {
			CommonClass.myLog(settings, "ChargerReceiver is enabled",
					CommonClass.NO);
			Editor editor = settings.edit();

			if (cablePlugged) {
				CommonClass.myLog(settings, "cablePlugged = " + cablePlugged,
						CommonClass.NO);

				/*
				 * This event can happen when the receiver is started before the
				 * HomeActivity can create this variable
				 */
				if (this._app == null) {
					CommonClass.myLog(settings, "this._app == " + this._app
							+ "\nClosing...", CommonClass.NO);
					CommonClass.myLog(settings,
							"ChargeReceiver.onReceive() - End\n",
							CommonClass.YES);
					return;
				}

				this._app.getCpuStateMonitor().removeOffsets();

				CommonClass.myLog(settings, "Offsets removed", CommonClass.NO);

				editor.remove(CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET);

				/*
				 * set the PREF_WAITING_DISCHARGING_EVENT to true
				 */
				editor.remove(CommonClass.PREF_WAITING_DISCHARGING_EVENT)
						.commit();

				CommonClass.myLog(settings, "Removed timers", CommonClass.NO);
				editor.putBoolean(CommonClass.PREF_WAITING_DISCHARGING_EVENT,
						true);
				editor.commit();

				CommonClass.myLog(settings,
						"Cable plugged. Timer's been deleted. Data refreshed.",
						CommonClass.NO);

			} else if (cableUnplugged) {
				CommonClass.myLog(settings, "cableUnplugged = "
						+ cableUnplugged, CommonClass.NO);

				int batt_level_before_reset = settings.getInt(
						CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET, -1);
				if (batt_level_before_reset != -1) {
					/*
					 * timer isn't set. Nothing to do here
					 */
					CommonClass
							.myLog(settings,
									"Cable unplugged. Reset timer already set. Nothing done.",
									CommonClass.NO);
				} else {
					CommonClass
							.myLog(settings,
									"Cable unplugged. Reset timer not set. Continue...",
									CommonClass.NO);

					int level = HomeActivity.getBatteryLevelStatic(context);
					int ActivatingThreshold = PreferenceManager
							.getDefaultSharedPreferences(context)
							.getInt(context.getResources().getString(
									R.string.ThresholdActivateTimerKey), 100);

					if (level >= ActivatingThreshold) {

						CommonClass.myLog(settings, "Battery level: " + level
								+ ". ActivatingThreshold: "
								+ ActivatingThreshold, CommonClass.NO);

						editor = settings.edit();
						editor.putInt(
								CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET,
								level);
						editor.remove(CommonClass.PREF_UPDATING_DATA);
						editor.putInt(CommonClass.PREF_UPDATING_DATA,
								CommonClass.YES).commit();

						/*
						 * set the PREF_WAITING_DISCHARGING_EVENT to false
						 */
						editor.remove(
								CommonClass.PREF_WAITING_DISCHARGING_EVENT)
								.commit();
						editor.putBoolean(
								CommonClass.PREF_WAITING_DISCHARGING_EVENT,
								false).commit();

						try {
							this._app.getCpuStateMonitor().setOffsets();
							CommonClass.myLog(settings,
									"Setting timer offsets.", CommonClass.NO);
						} catch (CpuStateMonitorException exception) {
							exception.printStackTrace();
						}

						this._app.saveOffsets();

						putBatteryLevelInSharedPrefs(settings, level);
						CommonClass.myLog(settings,
								"Battery level saved in SharedPrefs",
								CommonClass.NO);
						putTimeInSharedPrefs(settings);
						CommonClass.myLog(settings,
								"Time saved in SharedPrefs", CommonClass.NO);

						CommonClass.myLog(settings, "Timer's been reset.",
								CommonClass.NO);

					} else {
						CommonClass.myLog(settings,
								"Cable unplugged. Battery level: " + level
										+ ". ActivatingThreshold: "
										+ ActivatingThreshold
										+ ". Nothing done.", CommonClass.NO);
					}
				}
			}

			CommonClass.myLog(settings, "cableUnlugged block code - end",
					CommonClass.NO);
		} else {
			CommonClass.myLog(settings,
					" BroadcastReceiver preference is disabled. Nothing Done.",
					CommonClass.NO);
		}
		
		CommonClass.myLog(settings, "ChargeReceiver.onReceive() - End\n",
				CommonClass.YES);
	}

	public int getBatteryLevel(Context context) {

		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		double level = -1;
		if (rawlevel >= 0 && scale > 0) {
			level = rawlevel / scale * 100;
		}

		return (int) level;
	}

	public void putBatteryLevelInSharedPrefs(SharedPreferences settings,
			int level) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET, level);
		editor.commit();

		return;

	}

	public void putTimeInSharedPrefs(SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(CommonClass.PREF_BATTERY_RESET_TIME,
				SystemClock.elapsedRealtime());
		editor.commit();
		return;
	}

}
