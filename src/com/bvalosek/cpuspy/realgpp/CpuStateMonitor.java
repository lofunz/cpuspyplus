//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
//
//-----------------------------------------------------------------------------

package com.bvalosek.cpuspy.realgpp;

// imports
import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import com.bvalosek.cpuspy.realgpp.CommonClass;

/**
 * CpuStateMonitor is a class responsible for querying the system and getting
 * the time-in-state information, as well as allowing the user to set/reset
 * offsets to "restart" the state timers
 */
public class CpuStateMonitor extends Activity {

	private List<CpuState> _states = new ArrayList<CpuState>();
	private Map<Integer, Long> _offsets = new HashMap<Integer, Long>();

	/** exception class */
	public class CpuStateMonitorException extends Exception {

		public CpuStateMonitorException(String s) {
			super(s);
		}
	}

	/** @return List of CpuState with the offsets applied */
	public List<CpuState> getStates() {
		List<CpuState> states = new ArrayList<CpuState>();
		/*
		 * check for an existing offset, and if it's not too big, subtract it
		 * from the duration, otherwise just add it to the return List
		 */

		for (int i = 0; i < this._states.size(); i++) {
			CpuState state = this._states.get(i);
			long duration = state.duration;
			if (this._offsets.containsKey(state.freq)) {
				long offset = this._offsets.get(state.freq);
				if (offset <= duration) {
					duration -= offset;
				} else {
					/*
					 * offset > duration implies our offsets are now invalid, so
					 * clear and recall this function
					 */
					this._offsets.clear();
					return getStates();
				}
			}

			states.add(new CpuState(state.freq, duration));
		}

		if (states.size() == 0)
			Log.e(CommonClass.TAG, "getStates(): size(List<CpuState>) = 0 !");
		return states;
	}

	/**
	 * @return Sum of all state durations including deep sleep, accounting for
	 *         offsets
	 */
	public long getTotalStateTime() {
		long sum = 0;

		for (CpuState state : this._states)
			sum += state.duration;

		long offset = 0;

		for (Map.Entry<Integer, Long> entry : this._offsets.entrySet()) {
			offset += entry.getValue();
		}

		return sum - offset;
	}

	/**
	 * @return Map of freq->duration of all the offsets
	 */
	public Map<Integer, Long> getOffsets() {
		return this._offsets;
	}

	/** Sets the offset map (freq->duration offset) */
	public void setOffsets(Map<Integer, Long> offsets) {
		this._offsets = offsets;
	}

	/**
	 * Updates the current time in states and then sets the offset map to the
	 * current duration, effectively "zeroing out" the timers
	 */
	public void setOffsets() throws CpuStateMonitorException {
		this._offsets.clear();
		updateStates();

		for (CpuState state : this._states) {
			this._offsets.put(state.freq, state.duration);
		}
	}

	/** removes state offsets */
	public void removeOffsets() {
		this._offsets.clear();
	}

	/**
	 * @return a list of all the CPU frequency states, which contains both a
	 *         frequency and a duration (time spent in that state
	 */
	public List<CpuState> updateStates() {

		/*
		 * attempt to create a buffered reader to the time in state file and
		 * read in the states to the class
		 */
		try {

			InputStream is = new FileInputStream(
					CommonClass.PATH_TIME_IN_STATE_CORE0);
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);
			this._states.clear();
			String line;
			while ((line = br.readLine()) != null) {

				String[] nums = line.split(" ");
				this._states.add(new CpuState(Integer.parseInt(nums[0]), Long
						.parseLong(nums[1])));

			}

			// close the file
			is.close();

			/*
			 * deep sleep time determined by difference between elapsed (total)
			 * boot time and the system uptime (awake)
			 */
			long sleepTime = (SystemClock.elapsedRealtime() - SystemClock
					.uptimeMillis()) / 10;
			this._states.add(new CpuState(0, sleepTime));
		} catch (java.io.FileNotFoundException e) {
			Log.v(CommonClass.TAG, "time_in_state: File Not Found");
		} catch (IOException exception) {
			Log.v(CommonClass.TAG, "time_in_state: Opening Error");
		}

		Collections.sort(this._states, Collections.reverseOrder());

		return this._states;
	}
}
