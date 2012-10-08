//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
//
//-----------------------------------------------------------------------------

package com.bvalosek.cpuspy.realgpp.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.BatteryManager;

import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bvalosek.cpuspy.realgpp.*;
import com.bvalosek.cpuspy.realgpp.CpuStateMonitor.CpuStateMonitorException;

import android.util.Log;

/** main activity class */
public class HomeActivity extends Activity {

	public static CpuSpyApp _app = null;

	private LinearLayout _uiStatesView = null;
	private LinearLayout _uiStatesViewTop = null;
	private LinearLayout _uiStatesViewBottom = null;
	private TextView _uiAdditionalStates = null;
	private TextView _uiTotalStateTime = null;
	private LinearLayout _uiLayoutCpuInfo = null;
	private LinearLayout _uiLayoutLess1Perc = null;
	private LinearLayout _uiHeaderAdditionalStates = null;
	private TextView _uiTextViewTotalStateTime = null;
	private TextView _uiTotalSinceBootTime = null;
	private TextView _uiStatesWarning = null;
	private TextView _uiKernelString = null;

	private TextView _uiHeaderLessThan1PercStates_HeaderTextView = null;
	private TextView _uiLessThan1PercStates = null;
	private LinearLayout _uiHeaderLessThan1PercStates_LinearLayout = null;
	private LinearLayout _uiBatteryStats = null;
	private LinearLayout _uiLayoutUnusedStats = null;
	private LinearLayout _uiLayoutTotalStateTime = null;
	private LinearLayout _uiLayoutSubMenuTotalStates = null;
	private TextView _uiTextView_CpuInfoString = null;

	private boolean is_mute = false;
	public BroadcastReceiver batteryLevelReceiver;
	ChargerReceiver chargerReceiver;
	SharedPreferences settings;

	private static Context context;
	
	public static Context getAppContext() {
		return HomeActivity.context;
	}

	MediaPlayer button_clicked_sound;
	MediaPlayer header_clicked_sound;
	/** whether or not we're updating the data in the background */
	private boolean _updatingData = false;

	public void setUpdatingData(boolean b) {
		this._updatingData = b;
	}

	/** Initialize the Activity */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Thread.currentThread().setUncaughtExceptionHandler(
				new CustomExceptionHandler(getApplicationContext(), null));

		super.onCreate(savedInstanceState);
		HomeActivity.context = getApplicationContext();
		HomeActivity._app = (CpuSpyApp) getApplicationContext();

		this.settings = getSharedPreferences(CommonClass.PREF_NAME,
				MODE_PRIVATE);
		Editor editor = this.settings.edit();
		
		

		/*
		 * NON CANCELLARE
		 * 
		 * Code executed only on the first execution of the app for example when
		 * just installed or updated //
		 */
		String actualVersion = this.settings.getString(
				CommonClass.PREF_APP_VERSION, "- realgpp 0.4.00");
		if (!actualVersion.equals(getResources().getString(
				R.string.version_name))) {

			editor.putString(CommonClass.PREF_APP_VERSION, getResources()
					.getString(R.string.version_name));

			/*
			 * REMOVE TIMER VARIABLES
			 */
			editor.remove(CommonClass.PREF_BATTERY_RESET_TIME);
			editor.remove(CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET);
			editor.commit();
		}

		CommonClass.myLog(this.settings, "HomeActivity.onCreate - Begin",
				CommonClass.YES);

		if (this.settings.getString(CommonClass.PREF_THEME_SELECTION,
				CommonClass.THEME_NO_BUTTON)
				.equals(CommonClass.THEME_NO_BUTTON))
			setContentView(R.layout.no_button_layout);
		else
			setContentView(R.layout.button_layout);		

		/** Broadcast Receiver */
		if (isCharging()) {
			editor.putBoolean(CommonClass.PREF_WAITING_DISCHARGING_EVENT, true);
		} else {
			editor.putBoolean(CommonClass.PREF_WAITING_DISCHARGING_EVENT, false);
		}

		findViews();

		/** see if we're updating data during a config change (rotate screen) */
		if (savedInstanceState != null) {
			this._updatingData = savedInstanceState.getBoolean("updatingData");
		}

		/*
		 * UI Sounds
		 */
		this.button_clicked_sound = MediaPlayer.create(this,
				R.raw.button_clicked);
		this.header_clicked_sound = MediaPlayer.create(this,
				R.raw.expandible_view);

		editor.commit();
		CommonClass.myLog(this.settings, "HomeActivity.onCreate - End",
				CommonClass.YES);

	}
	
	/** When the activity is about to change orientation */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("updatingData", this._updatingData);
	}

	@Override
	public void onBackPressed() {
		CommonClass.myLog(this.settings, "onBackPressed()", CommonClass.YES);
		moveTaskToBack(true);
	}

	/** Update the view when the application regains focus */
	@Override
	public void onResume() {
		CommonClass.myLog(this.settings, "HomeActivity.onResume() - Begin",
				CommonClass.YES);
		super.onResume();

		/*
		 * UI Sounds
		 */
		if (this.settings.getBoolean(CommonClass.PREF_ENABLE_UI_SOUNDS, false)) {
			this.is_mute = true;
		} else {
			this.is_mute = false;
		}

		refreshData();

		LinearLayout ll_cpu_info = (LinearLayout) findViewById(R.id.header_cpu_info_container);
		LinearLayout ll_kernel_info = (LinearLayout) findViewById(R.id.header_kernel_info_container);
		if (this.settings.getBoolean(CommonClass.PREF_ENABLE_MISC_ACTIVITY,
				false) == false) {

			ll_cpu_info.setVisibility(View.VISIBLE);
			ll_kernel_info.setVisibility(View.VISIBLE);
		} else {
			ll_cpu_info.setVisibility(View.GONE);
			ll_kernel_info.setVisibility(View.GONE);
		}

		CommonClass.myLog(this.settings, "HomeActivity.onResume() - End",
				CommonClass.YES);
	}

	/** Map all of the UI elements to member variables */
	private void findViews() {

		CommonClass.myLog(this.settings, "HomeActivity.findViews() - Begin",
				CommonClass.YES);
		this._uiStatesView = (LinearLayout) findViewById(R.id.ui_states_view);

		this._uiLayoutCpuInfo = (LinearLayout) findViewById(R.id.header_cpu_info);
		this._uiLayoutLess1Perc = (LinearLayout) findViewById(R.id.header_less1perc_states);
		this._uiLayoutUnusedStats = (LinearLayout) findViewById(R.id.header_unused_cpu_states);
		this._uiLayoutTotalStateTime = (LinearLayout) findViewById(R.id.header_tot_states);
		this._uiLayoutSubMenuTotalStates = (LinearLayout) findViewById(R.id.ui_submenu_tot_states);
		this._uiStatesViewTop = (LinearLayout) findViewById(R.id.ui_states_view_top);
		this._uiStatesViewBottom = (LinearLayout) findViewById(R.id.ui_states_view_bottom);
		this._uiKernelString = (TextView) findViewById(R.id.TextViewKernelInfoString);
		this._uiAdditionalStates = (TextView) findViewById(R.id.ui_additional_states);
		this._uiHeaderAdditionalStates = (LinearLayout) findViewById(R.id.header_unused_cpu_states);
		this._uiTextViewTotalStateTime = (TextView) findViewById(R.id.ui_header_total_state_time);
		this._uiStatesWarning = (TextView) findViewById(R.id.ui_states_warning);
		this._uiTotalStateTime = (TextView) findViewById(R.id.ui_total_state_time);
		this._uiTotalSinceBootTime = (TextView) findViewById(R.id.ui_total_since_boot_time);

		this._uiHeaderLessThan1PercStates_HeaderTextView = (TextView) findViewById(R.id.TextView_less1perc_states);
		this._uiLessThan1PercStates = (TextView) findViewById(R.id.ui_less_1_percentage_states);
		this._uiHeaderLessThan1PercStates_LinearLayout = (LinearLayout) findViewById(R.id.header_less1perc_states);
		this._uiBatteryStats = (LinearLayout) findViewById(R.id.ui_battery_stats);

		this._uiTextView_CpuInfoString = (TextView) findViewById(R.id.TextViewCpuInfoString);

		/** CPU INFO Header */
		LinearLayout ll = (LinearLayout) findViewById(R.id.header_cpu_info);
		ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				playHeaderClickedSound();
				ImageView iv = (ImageView) findViewById(R.id.ImageView_arrow_cpu_info);
				TextView tv = (TextView) findViewById(R.id.TextViewCpuInfoString);
				int visibility = tv.getVisibility();
				if (visibility == View.INVISIBLE || visibility == View.GONE) {
					iv.setImageResource(R.drawable.arrow_up_float);
					tv.setVisibility(View.VISIBLE);
				} else {
					iv.setImageResource(R.drawable.arrow_down_float);
					tv.setVisibility(View.GONE);

				}
				CommonClass.myLog(HomeActivity.this.settings,
						"findViews() - cpu_info_header clicked",
						CommonClass.YES);
			}
		});

		/** KERNEL INFO Header */
		ll = (LinearLayout) findViewById(R.id.header_kernel_info);

		ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				playHeaderClickedSound();
				ImageView arrow_iv = (ImageView) findViewById(R.id.ImageView_arrow_kernel_info);
				TextView tv = (TextView) findViewById(R.id.TextViewKernelInfoString);
				int visibility = tv.getVisibility();
				if (visibility == View.INVISIBLE || visibility == View.GONE) {
					arrow_iv.setImageResource(R.drawable.arrow_up_float);
					tv.setVisibility(View.VISIBLE);
				} else {
					arrow_iv.setImageResource(R.drawable.arrow_down_float);
					tv.setVisibility(View.GONE);
				}
				CommonClass.myLog(HomeActivity.this.settings,
						"findViews() - kernel_info_header clicked",
						CommonClass.YES);
			}
		});

		/** UNUSED CPU STATES Header */
		ll = (LinearLayout) findViewById(R.id.header_unused_cpu_states);
		ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				playHeaderClickedSound();
				ImageView arrow_iv = (ImageView) findViewById(R.id.ImageView_arrow_unused_cpu_states);
				TextView tv = (TextView) findViewById(R.id.ui_additional_states);
				int visibility = tv.getVisibility();
				if (visibility == View.INVISIBLE || visibility == View.GONE) {
					arrow_iv.setImageResource(R.drawable.arrow_up_float);
					tv.setVisibility(View.VISIBLE);
				} else {
					arrow_iv.setImageResource(R.drawable.arrow_down_float);
					tv.setVisibility(View.GONE);
				}
				CommonClass.myLog(HomeActivity.this.settings,
						"findViews() - additional_states_header clicked",
						CommonClass.YES);
			}
		});

		/** LESS 1% STATES Header */
		ImageView iv = (ImageView) findViewById(R.id.ImageView_arrow_less1perc_states);
		iv.setImageResource(R.drawable.arrow_down_float);
		ll = (LinearLayout) findViewById(R.id.header_less1perc_states);
		ll.setVisibility(View.GONE);
		ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				playHeaderClickedSound();
				ImageView arrow_iv = (ImageView) findViewById(R.id.ImageView_arrow_less1perc_states);

				TextView tv = (TextView) findViewById(R.id.ui_less_1_percentage_states);
				int visibility = tv.getVisibility();
				if (visibility == View.INVISIBLE || visibility == View.GONE) {
					arrow_iv.setImageResource(R.drawable.arrow_up_float);
					tv.setVisibility(View.VISIBLE);
				} else {
					tv.setVisibility(View.GONE);
					arrow_iv.setImageResource(R.drawable.arrow_down_float);
				}
				CommonClass.myLog(HomeActivity.this.settings,
						"findViews() - less1perc_states_header clicked",
						CommonClass.YES);
			}
		});

		/** TIME IN STATE Header */
		ll = (LinearLayout) findViewById(R.id.header_time_in_state);
		ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ImageView arrow_iv = (ImageView) findViewById(R.id.ImageView_arrow_time_in_state_header);

				LinearLayout ll = (LinearLayout) findViewById(R.id.ui_states_view_bottom);
				int numChildBottom = ll.getChildCount();
				ll = (LinearLayout) findViewById(R.id.ui_states_view_top);
				int numcChildTop = ll.getChildCount();

				if (numChildBottom == 0 && numcChildTop == 0) {
					arrow_iv.setVisibility(View.GONE);

				} else if (numChildBottom > 0 || numcChildTop > 0) {
					playHeaderClickedSound();
					arrow_iv.setVisibility(View.VISIBLE);
					int visibility = HomeActivity.this._uiStatesViewTop
							.getVisibility();
					if ((visibility == View.INVISIBLE || visibility == View.GONE)
							&& HomeActivity.this._uiStatesViewTop
									.getChildCount() > 0) {
						arrow_iv.setImageResource(R.drawable.arrow_down_float);

						HomeActivity.this._uiStatesViewTop
								.setVisibility(View.VISIBLE);
						HomeActivity.this.settings
								.edit()
								.putBoolean(
										CommonClass.PREF_SHOW_1PERC_IN_TIME_IN_STATE_LAYOUT,
										true);
					} else {
						HomeActivity.this._uiStatesViewTop
								.setVisibility(View.GONE);
						arrow_iv.setImageResource(R.drawable.arrow_up_float);
						HomeActivity.this.settings
								.edit()
								.putBoolean(
										CommonClass.PREF_SHOW_1PERC_IN_TIME_IN_STATE_LAYOUT,
										false);
					}

					visibility = HomeActivity.this._uiStatesViewBottom
							.getVisibility();
					if (visibility == View.INVISIBLE || visibility == View.GONE) {
						arrow_iv.setImageResource(R.drawable.arrow_up_float);
						HomeActivity.this._uiStatesViewBottom
								.setVisibility(View.VISIBLE);
						HomeActivity.this.settings
								.edit()
								.putBoolean(
										CommonClass.PREF_SHOW_1PERC_IN_TIME_IN_STATE_LAYOUT,
										true);
					} else {
						HomeActivity.this._uiStatesViewBottom
								.setVisibility(View.GONE);
						arrow_iv.setImageResource(R.drawable.arrow_down_float);
						HomeActivity.this.settings
								.edit()
								.putBoolean(
										CommonClass.PREF_SHOW_1PERC_IN_TIME_IN_STATE_LAYOUT,
										false);
					}

					HomeActivity.this.settings.edit().commit();
				}

				CommonClass.myLog(HomeActivity.this.settings,
						"findViews() - header_time_in_state clicked",
						CommonClass.YES);
			}
		});

		/*
		 * BUTTONS
		 */
		if (this.settings.getString(CommonClass.PREF_THEME_SELECTION,
				CommonClass.THEME_NO_BUTTON).equals(
				CommonClass.THEME_WITH_BUTTON)) {

			int screenWidth = getWindowManager().getDefaultDisplay().getWidth() - 10;
			int buttonWidth = screenWidth / 4;

			ImageButton ib = (ImageButton) findViewById(R.id.imageButton_Reset_timer);
			RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) ib
					.getLayoutParams();
			rl.width = buttonWidth;
			ib.setLayoutParams(rl);
			ib.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					playButtonClickedSound();
					function_ResetTimer();
					CommonClass.myLog(HomeActivity.this.settings,
							"findViews() - Reset timer button clicked",
							CommonClass.YES);
				}
			});

			ib = (ImageButton) findViewById(R.id.imageButton_1perc_freq);
			rl = (RelativeLayout.LayoutParams) ib.getLayoutParams();
			rl.width = buttonWidth;
			ib.setLayoutParams(rl);

			boolean show_1perc_states = this.settings.getBoolean(
					CommonClass.PREF_SHOW_1PERC_STAT, false);

			Editor editor = this.settings.edit();

			if (show_1perc_states) {
				editor.putBoolean(CommonClass.PREF_SHOW_1PERC_STAT, false);
				ib.setImageResource(R.drawable.ic_menu_view);
			} else {
				editor.putBoolean(CommonClass.PREF_SHOW_1PERC_STAT, true);
				ib.setImageResource(R.drawable.ic_menu_view_hide);
			}

			editor.commit();

			ib.setOnClickListener(new OnClickListener() {
				ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton_1perc_freq);

				@Override
				public void onClick(View v) {
					playButtonClickedSound();
					SharedPreferences settings;
					SharedPreferences.Editor editor;
					settings = getSharedPreferences(CommonClass.PREF_NAME,
							MODE_PRIVATE);
					editor = settings.edit();
					boolean show_1perc_states = settings.getBoolean(
							CommonClass.PREF_SHOW_1PERC_STAT, false);

					if (show_1perc_states) {
						editor.putBoolean(CommonClass.PREF_SHOW_1PERC_STAT,
								false);
						this.imageButton
								.setImageResource(R.drawable.ic_menu_view);
					} else {
						editor.putBoolean(CommonClass.PREF_SHOW_1PERC_STAT,
								true);
						this.imageButton
								.setImageResource(R.drawable.ic_menu_view_hide);
					}

					editor.commit();
					refreshData();
					CommonClass.myLog(HomeActivity.this.settings,
							"findViews() - Hide 1% button clicked",
							CommonClass.YES);
				}
			});

			ib = (ImageButton) findViewById(R.id.imageButton_Refresh);
			rl = (RelativeLayout.LayoutParams) ib.getLayoutParams();
			rl.width = buttonWidth;
			ib.setLayoutParams(rl);

			ib.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					playButtonClickedSound();

					refreshData();
					CommonClass.myLog(HomeActivity.this.settings,
							"findViews() - Refresh button clicked",
							CommonClass.YES);
				}
			});

			ib = (ImageButton) findViewById(R.id.imageButton_Restore_timer);
			rl = (RelativeLayout.LayoutParams) ib.getLayoutParams();
			rl.width = buttonWidth;
			ib.setLayoutParams(rl);
			ib.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					playButtonClickedSound();
					function_RestoreTimer();
					CommonClass.myLog(HomeActivity.this.settings,
							"findViews() - Restore timer button clicked",
							CommonClass.YES);
				}

			});
		}
		CommonClass.myLog(this.settings, "findViews() - End", CommonClass.YES);
	}

	/** called when we want to infalte the menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/** request inflater from activity and inflate into its menu */
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		playButtonClickedSound();

		if (this.settings.getBoolean(CommonClass.PREF_ENABLE_MISC_ACTIVITY,
				false) == false) {
			menu.getItem(6).setVisible(false);// misc info
		} else
			menu.getItem(6).setVisible(true);// misc info

		menu.getItem(0).setVisible(true); // cpu files
		menu.getItem(1).setVisible(true);// log file
		menu.getItem(7).setVisible(true);// settings

		if (this.settings.getString(CommonClass.PREF_THEME_SELECTION,
				CommonClass.THEME_NO_BUTTON).equals(
				CommonClass.THEME_WITH_BUTTON)) {
			menu.getItem(2).setVisible(false); // refresh
			menu.getItem(3).setVisible(false); // reset
			menu.getItem(4).setVisible(false); // restore
			menu.getItem(5).setVisible(false); // view 1%

		} else {
			menu.getItem(2).setVisible(true); // refresh
			menu.getItem(3).setVisible(true); // reset
			menu.getItem(4).setVisible(true); // restore

			int percetage2hideStates = PreferenceManager
					.getDefaultSharedPreferences(context).getInt(
							context.getResources().getString(
									R.string.ThresholdFreqsToHide), 1);
			String title = "Show/Hide ";
			title += Integer.toString(percetage2hideStates) + "% used stats";
			menu.getItem(5).setTitle(title);
			menu.getItem(5).setVisible(true); // view 1%

		}
		return true;
	}

	/** called to handle a menu event */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		SharedPreferences settings = getSharedPreferences(
				CommonClass.PREF_NAME, MODE_PRIVATE);
		Editor editor = settings.edit();
		switch (item.getItemId()) {
		case R.id.menu_log_file:
			Intent logfileIntent = new Intent(getApplicationContext(),
					LogFileActivity.class);
			startActivity(logfileIntent);
			break;
		case R.id.menu_impostazioni:
			playButtonClickedSound();
			Intent settingsActivit = new Intent(getApplicationContext(),
					Preferences.class);
			startActivity(settingsActivit);
			break;
		case R.id.menu_1perc:
			editor = this.settings.edit();
			if (settings.getBoolean(CommonClass.PREF_SHOW_1PERC_STAT, false) == false) {
				editor.putBoolean(CommonClass.PREF_SHOW_1PERC_STAT, true);

			} else {
				editor.putBoolean(CommonClass.PREF_SHOW_1PERC_STAT, false);
			}

			editor.commit();
			updateView();
			break;

		case R.id.menu_refresh:
			refreshData();
			break;
		case R.id.menu_reset:
			function_ResetTimer();
			break;
		case R.id.menu_restore:
			function_RestoreTimer();
			break;
		case R.id.menu_compare_files:
			playButtonClickedSound();
			Intent ii = new Intent(getBaseContext(), CompareFilesActivity.class);
			ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(ii);
			break;

		case R.id.menu_misc_info:
			Intent misc = new Intent(getBaseContext(), MiscActivity.class);
			misc.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(misc);
			break;
		}
		return true;
	}

	/** Generate and update all UI elements */
	public void updateView() {
		int isBrOn = this.settings.getInt(
				CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
				CommonClass.YES);

		// header text at the bottom of the layout - PLUGGING RECEIVER
		TextView tv = (TextView) findViewById(R.id.ui_header_broadcast_receiver);
		if (isBrOn == CommonClass.YES)

			tv.setText(getResources().getString(
					R.string.ui_header_broadcast_receiver)
					+ " ON");
		else
			tv.setText(getResources().getString(
					R.string.ui_header_broadcast_receiver)
					+ " OFF");

		/**
		 * Get the CpuStateMonitor from the app, and iterate over all states,
		 * creating a row if the duration is > 0 or otherwise marking it in
		 * extraStates (missing)
		 */

		CpuStateMonitor monitor_cpu = HomeActivity._app.getCpuStateMonitor();

		this._uiStatesView.removeAllViews();

		List<String> extraStates = new ArrayList<String>();

		List<String> less1States = new ArrayList<String>();

		boolean show_1perc_states = this.settings.getBoolean(
				CommonClass.PREF_SHOW_1PERC_STAT, false);

		CommonClass.myLog(this.settings, "updateView: PREF_SHOW_1PERC_STAT = "
				+ show_1perc_states, CommonClass.NO);

		CpuState sumTotalStateTimeOfLess1PercentageState = new CpuState(-99, 0);
		int percetage2hideStates = PreferenceManager
				.getDefaultSharedPreferences(context).getInt(
						context.getResources().getString(
								R.string.ThresholdFreqsToHide), 1);
		for (CpuState state : monitor_cpu.getStates()) {
			if (state.duration > 0) {
				if (!show_1perc_states) {

					float percen = (float) state.duration * 100
							/ monitor_cpu.getTotalStateTime();
					if (percen < percetage2hideStates) {
						sumTotalStateTimeOfLess1PercentageState.duration += state.duration;
						less1States.add(state.freq / 1000 + " MHz");

					} else {
						generateSingleStateRow(state, this._uiStatesView);
					}
				} else {
					generateSingleStateRow(state, this._uiStatesView);
				}

			} else {
				if (state.freq == 0) {
					extraStates.add("Deep Sleep");
				} else {
					extraStates.add(state.freq / 1000 + " MHz");
				}
			}
		}

		// show the red warning label if no states found
		int dim = 0;
		for (int i = 0; i < 4; i++) {
			dim = monitor_cpu.getStates().toArray().length;
			monitor_cpu.getStates().isEmpty();
			if (dim != 0)
				break;
		}

		if (dim == 0) {
			findViews();

			this._uiTextViewTotalStateTime.setVisibility(View.GONE);
			this._uiTotalStateTime.setVisibility(View.GONE);
			this._uiStatesView.setVisibility(View.GONE);
			this._uiBatteryStats.setVisibility(View.GONE);

			this._uiAdditionalStates.setVisibility(View.GONE);
			this._uiAdditionalStates.setVisibility(View.GONE);

			this._uiHeaderLessThan1PercStates_LinearLayout
					.setVisibility(View.GONE);
			this._uiLessThan1PercStates.setVisibility(View.GONE);

			this._uiLayoutCpuInfo.setVisibility(View.GONE);
			this._uiTextView_CpuInfoString.setVisibility(View.GONE);
			this._uiLayoutUnusedStats.setVisibility(View.GONE);
			this._uiLayoutSubMenuTotalStates.setVisibility(View.GONE);
			this._uiLayoutTotalStateTime.setVisibility(View.GONE);
			this._uiLayoutLess1Perc.setVisibility(View.GONE);

			// kernel line
			this._uiKernelString.setText(HomeActivity._app.getKernelVersion());

			this._uiStatesWarning.setVisibility(View.VISIBLE);
			return;
		} else {

			// update the total state time
			long totTime = SystemClock.elapsedRealtime() / 1000;
			this._uiTotalSinceBootTime.setText(sToString(totTime));

			totTime = monitor_cpu.getTotalStateTime() / 100;

			/*
			 * connection usage layout - TOTAL PART -
			 */
			ImageView iv;

			this._uiTotalStateTime.setText(sToString(totTime));

			// for all the 0 duration states, add the the Unused State area
			if (extraStates.size() > 0) {
				int n = 0;
				String str = "";

				for (String s : extraStates) {
					if (n++ > 0)
						str += ", ";
					str += s;
				}

				this._uiAdditionalStates.setVisibility(View.VISIBLE);
				this._uiHeaderAdditionalStates.setVisibility(View.VISIBLE);
				this._uiAdditionalStates.setText(str);

			} else {
				this._uiAdditionalStates.setVisibility(View.GONE);
				this._uiHeaderAdditionalStates.setVisibility(View.GONE);

			}

			/*
			 * for all the <1% usage duration states, add the the Unused State
			 * area
			 */

			String header;

			int percHideStats = PreferenceManager.getDefaultSharedPreferences(
					context).getInt(
					context.getResources().getString(
							R.string.ThresholdFreqsToHide), 1);
			long time = monitor_cpu.getTotalStateTime() / 10000 * percHideStats;
			header = getString(R.string.ui_header_less_1st_part) + " "
					+ percHideStats;
			header += getString(R.string.ui_header_less_2nd_part);
			header += " (i.e. " + sToString(time) + ")";

			this._uiHeaderLessThan1PercStates_HeaderTextView.setText(header);
			if (less1States.size() > 0) {
				int n = 0;
				String str = "";

				for (String s : less1States) {
					if (n++ > 0)
						str += ", ";
					str += s;
				}

				this._uiLessThan1PercStates.setVisibility(View.VISIBLE);
				this._uiHeaderLessThan1PercStates_LinearLayout
						.setVisibility(View.VISIBLE);
				this._uiLessThan1PercStates.setText(str);

				/*
				 * add the row that represent the total sum of the "less <1%"
				 * states
				 */
				boolean less1perc_bar_on_top = this.settings.getBoolean(
						CommonClass.PREF_LESS1PERC_BAR_GRAPH_TOP, false);
				if (less1perc_bar_on_top) {
					this._uiStatesViewTop.removeAllViews();
					generateSingleStateRow(
							sumTotalStateTimeOfLess1PercentageState,
							this._uiStatesViewTop);
					this._uiStatesViewBottom.removeAllViews();
				} else {
					this._uiStatesViewBottom.removeAllViews();
					generateSingleStateRow(
							sumTotalStateTimeOfLess1PercentageState,
							this._uiStatesViewBottom);
					this._uiStatesViewTop.removeAllViews();
				}
				iv = (ImageView) findViewById(R.id.ImageView_arrow_time_in_state_header);
				iv.setVisibility(View.VISIBLE);
			} else {

				this._uiLessThan1PercStates.setVisibility(View.GONE);
				this._uiHeaderLessThan1PercStates_LinearLayout
						.setVisibility(View.GONE);

				this._uiStatesViewTop.removeAllViews();
				this._uiStatesViewBottom.removeAllViews();
				iv = (ImageView) findViewById(R.id.ImageView_arrow_time_in_state_header);
				iv.setVisibility(View.GONE);
			}

			/*
			 * CPU Info textview
			 */
			String cpuInfo = "Freqs range: ";
			try {
				cpuInfo += String
						.valueOf(SystemUtils.getCPUFrequencyMin() / 1000)
						+ "MHz - "
						+ String.valueOf(SystemUtils.getCPUFrequencyMax() / 1000)
						+ "MHz";

			} catch (Exception exception) {
				cpuInfo += "Error";
				CommonClass.myLog(this.settings, exception.getMessage(),
						CommonClass.YES);
			}

			cpuInfo += "\nScaling range: ";

			try {
				cpuInfo += String.valueOf(SystemUtils
						.getCPUFrequencyMinScaling() / 1000)
						+ "MHz - "
						+ String.valueOf(SystemUtils
								.getCPUFrequencyMaxScaling() / 1000) + "MHz";
			} catch (Exception exception) {
				cpuInfo += "Error";
				CommonClass.myLog(this.settings, exception.getMessage(),
						CommonClass.YES);
			}

			cpuInfo += "\nGovernor: ";
			try {
				cpuInfo += SystemUtils.getGovernor();
			} catch (Exception exception) {
				cpuInfo += "Error";
				CommonClass.myLog(this.settings, exception.getMessage(),
						CommonClass.YES);
			}
			this._uiTextView_CpuInfoString.setText(cpuInfo);

			/*
			 * kernel line
			 */
			this._uiKernelString.setText(HomeActivity._app.getKernelVersion());

			SharedPreferences settings = getSharedPreferences(
					CommonClass.PREF_NAME, MODE_PRIVATE);
			/*
			 * battery level line
			 */
			Integer levelBatteryBeforeReset = settings.getInt(
					CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET, -1);
			Long deltaResetTime = this.settings.getLong(
					CommonClass.PREF_BATTERY_RESET_TIME,
					SystemClock.elapsedRealtime());
			deltaResetTime = SystemClock.elapsedRealtime() - deltaResetTime;

			/*
			 * 
			 * Timer header: battery statistics layout
			 */
			LinearLayout ll = (LinearLayout) findViewById(R.id.ui_battery_stats);
			if (levelBatteryBeforeReset != -1) {
				/*
				 * battery statistics layout
				 */

				TextView tvTime = (TextView) findViewById(R.id.ui_battery_level_percentage_start);
				tvTime.setText(levelBatteryBeforeReset.toString() + "%");

				tvTime = (TextView) findViewById(R.id.ui_battery_level_percentage_now);
				tvTime.setText(String.valueOf(levelBatteryBeforeReset
						- getBatteryLevel())
						+ "%");

				tvTime = (TextView) findViewById(R.id.ui_battery_time_spent);
				tvTime.setText(sToString(deltaResetTime / 1000));

				ll.setVisibility(View.VISIBLE);
			} else {
				ll.setVisibility(View.GONE);
			}

		}
	}

	/** Attempt to update the time-in-state info */
	public void refreshData() {
		if (!this._updatingData) {
			new RefreshStateDataTask().execute((Void) null);
		}
	}

	/** @return A nicely formatted String representing tSec seconds */
	private static String sToString(long tSec) {
		long h = (long) Math.floor(tSec / (60 * 60));
		long m = (long) Math.floor((tSec - h * 60 * 60) / 60);
		long s = tSec % 60;
		String sDur;
		sDur = h + ":";
		if (m < 10)
			sDur += "0";
		sDur += m + ":";
		if (s < 10)
			sDur += "0";
		sDur += s;

		return sDur;
	}

	/**
	 * @return a View that correpsonds to a CPU freq state row as specified by
	 *         the state parameter
	 */
	private View generateSingleStateRow(CpuState state, ViewGroup parent) {
		// inflate the XML into a view in the parent
		LayoutInflater inf = LayoutInflater.from(HomeActivity._app);
		LinearLayout theRow = (LinearLayout) inf.inflate(R.layout.state_row,
				parent, false);

		// what percetnage we've got
		CpuStateMonitor monitor_cpu0 = HomeActivity._app.getCpuStateMonitor();
		float per = (float) state.duration * 100
				/ monitor_cpu0.getTotalStateTime();

		String sPer = String.format("%d", (int) per) + "%";

		// state name
		String sFreq;
		if (state.freq == -99) {
			sPer = String.format("%.2f", per) + "%";
			int percetage2hideStates = PreferenceManager
					.getDefaultSharedPreferences(context).getInt(
							context.getResources().getString(
									R.string.ThresholdFreqsToHide), 1);
			sFreq = "All states < " + Integer.toString(percetage2hideStates)
					+ "%";
		} else if (state.freq == 0) {
			sFreq = "Deep Sleep";
		} else {
			sFreq = state.freq / 1000 + " MHz";
		}

		// duration
		long tSec = state.duration / 100;
		String sDur = sToString(tSec);

		// map UI elements to objects
		TextView freqText = (TextView) theRow.findViewById(R.id.ui_freq_text);
		TextView durText = (TextView) theRow
				.findViewById(R.id.ui_duration_text);
		TextView perText = (TextView) theRow
				.findViewById(R.id.ui_percentage_text);
		ProgressBar bar = (ProgressBar) theRow.findViewById(R.id.ui_bar);

		// modify the row
		freqText.setText(sFreq);
		perText.setText(sPer);
		durText.setText(sDur);
		bar.setProgress((int) per);

		// add it to parent and return
		parent.addView(theRow);
		return theRow;
	}

	/** Keep updating the state data off the UI thread for slow devices */
	public class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {

		/** Stuff to do on a seperate thread */
		@Override
		protected Void doInBackground(Void... v) {
			CpuStateMonitor monitor = HomeActivity._app.getCpuStateMonitor();
			try {
				monitor.updateStates();
			} catch (CpuStateMonitorException e) {
				Log.e(CommonClass.TAG, "Problem getting CPU states");
			}

			return null;
		}

		/** Executed on the UI thread right before starting the task */
		@Override
		protected void onPreExecute() {
			log("starting data update");
			HomeActivity.this._updatingData = true;
		}

		/** Executed on UI thread after task */
		@Override
		protected void onPostExecute(Void v) {

			log("finished data update");
			HomeActivity.this._updatingData = false;
			updateView();
		}
	}

	/** logging */
	private void log(String s) {
		Log.d(CommonClass.TAG, s);
	}

	public int getBatteryLevel() {

		Intent batteryIntent = this.getApplicationContext().registerReceiver(
				null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		double level = -1;
		if (rawlevel >= 0 && scale > 0) {
			level = rawlevel / scale * 100;
		}

		return (int) level;
	}

	static public int getBatteryLevelStatic(Context context) {

		Intent batteryIntent = context.getApplicationContext()
				.registerReceiver(null,
						new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		double level = -1;
		if (rawlevel >= 0 && scale > 0) {
			level = rawlevel / scale * 100;
		}

		return (int) level;
	}

	private boolean isCharging() {
		Intent batteryIntent = this.getApplicationContext().registerReceiver(
				null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		int eventReceived = batteryIntent.getIntExtra(
				BatteryManager.EXTRA_STATUS, -1);
		if (eventReceived == BatteryManager.BATTERY_STATUS_CHARGING)
			return true;
		else
			return false;
	}

	public void putBatteryLevelInSharedPrefs() {

		int level = getBatteryLevel();
		SharedPreferences.Editor editor = this.settings.edit();
		editor.putInt(CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET, level);
		editor.commit();

		return;
	}

	public void putTimeInSharedPrefs() {
		SharedPreferences.Editor editor = this.settings.edit();
		editor.putLong(CommonClass.PREF_BATTERY_RESET_TIME,
				SystemClock.elapsedRealtime());
		editor.commit();
		return;
	}

	public void printToast(String output) {
		Toast toast = Toast.makeText(this.getApplicationContext(), output,
				Toast.LENGTH_SHORT);
		toast.show();
	}

	void function_ChangeBRState() {
		SharedPreferences settings = getSharedPreferences(
				CommonClass.PREF_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		int isBRenabled = 0;
		try {
			isBRenabled = settings.getInt(
					CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
					CommonClass.NO);
		} catch (Exception e) {
			editor.putInt(CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
					CommonClass.YES);
		}

		if (isBRenabled == CommonClass.YES) {

			editor.putInt(CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
					CommonClass.NO);
		} else {
			editor.putInt(CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
					CommonClass.YES);
		}
		editor.commit();
		Intent i = new Intent(getBaseContext(), HomeActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		updateView();
	}

	void function_RestoreTimer() {
		Editor editor = this.settings.edit();

		HomeActivity._app.getCpuStateMonitor().removeOffsets();
		HomeActivity._app.saveOffsets();

		editor.remove(CommonClass.PREF_BATTERY_LEVEL_BEFORE_RESET);
		editor.commit();

		refreshData();
		CommonClass.myLog(this.settings, "RestoreTimer(): Timer restored.\n",
				CommonClass.YES);
	}

	void function_ResetTimer() {
		Intent batteryIntent = (getApplicationContext().registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
		int isDischarging = batteryIntent.getIntExtra("status", -1);

		int isBRon = HomeActivity.this.settings
				.getInt(CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
						CommonClass.NO);

		// create toast if cable is connected to the phone
		if (isDischarging == BatteryManager.BATTERY_STATUS_CHARGING
				&& isBRon == CommonClass.YES) {
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
			alertDialog.setTitle("Attention");
			alertDialog
					.setMessage("Phone's connected to cable. Unplug it to reset timer.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					playButtonClickedSound();
					dialog.dismiss();
				}
			});
			alertDialog.show();
			return;
		}

		try {
			HomeActivity._app.getCpuStateMonitor().setOffsets();
		} catch (CpuStateMonitorException e) {
			Log.e(CommonClass.TAG, e.getMessage());
		}

		HomeActivity._app.saveOffsets();
		putBatteryLevelInSharedPrefs();
		putTimeInSharedPrefs();

		refreshData();
		CommonClass.myLog(this.settings, "ResetTimer(): Timer reset.\n",
				CommonClass.YES);
	}

	private void playButtonClickedSound() {
		if (this.is_mute) {
			HomeActivity.this.button_clicked_sound.start();
		}
	}

	private void playHeaderClickedSound() {
		if (this.is_mute)
			HomeActivity.this.header_clicked_sound.start();
	}
}
