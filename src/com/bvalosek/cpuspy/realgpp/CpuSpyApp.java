//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
//
//-----------------------------------------------------------------------------

package com.bvalosek.cpuspy.realgpp;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;

/** main application class */
public class CpuSpyApp extends Application {

	private static final String PREF_NAME = "CpuSpyPreferences";
	private static final String PREF_OFFSETS = "offsets";
	/** the long-living object used to monitor the system frequency states */
	private CpuStateMonitor _monitor_cpu = new CpuStateMonitor();

	private String _kernelVersion = "";

	/**
	 * On application start, load the saved offsets and stash the current kernel
	 * version string
	 */
	@Override
	public void onCreate() {
		loadOffsets();
		updateKernelVersion();
	}

	/** @return the kernel version string */
	public String getKernelVersion() {
		return this._kernelVersion;
	}

	/** @return the internal CpuStateMonitor object */
	public CpuStateMonitor getCpuStateMonitor() {
			return this._monitor_cpu;
	}

	/**
	 * Load the saved string of offsets from preferences and put it into the
	 * state monitor
	 */
	public void loadOffsets() {
		SharedPreferences settings = getSharedPreferences(PREF_NAME,
				MODE_PRIVATE);
		String prefs = settings.getString(PREF_OFFSETS, "");

		if (prefs == null || prefs.length() < 1) {
			return;
		}

		// split the string by peroids and then the info by commas and load
		Map<Integer, Long> offsets = new HashMap<Integer, Long>();
		String[] sOffsets = prefs.split(",");
		for (String offset : sOffsets) {
			String[] parts = offset.split(" ");
			offsets.put(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
		}

		this._monitor_cpu.setOffsets(offsets);
	}

	/**
	 * Save the state-time offsets as a string e.g. "100 24, 200 251, 500 124
	 * etc
	 */
	public void saveOffsets() {
		SharedPreferences settings = getSharedPreferences(PREF_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		// build the string by iterating over the freq->duration map
		String str = "";
		for (Map.Entry<Integer, Long> entry : this._monitor_cpu.getOffsets().entrySet()) {
			str += entry.getKey() + " " + entry.getValue() + ",";
		}

		editor.putString(PREF_OFFSETS, str);

		editor.commit();
	}

	/** Try to read the kernel version string from the proc fileystem */
	public String updateKernelVersion() {
		this._kernelVersion = SystemUtils.getKernelInfo();
		return this._kernelVersion;
	}
	
	
}
