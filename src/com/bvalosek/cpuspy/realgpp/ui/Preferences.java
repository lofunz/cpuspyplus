package com.bvalosek.cpuspy.realgpp.ui;

import com.bvalosek.cpuspy.realgpp.CommonClass;
import com.bvalosek.cpuspy.realgpp.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	SharedPreferences settings;
	MediaPlayer checked_sound;
	MediaPlayer unchecked_sound;
	MediaPlayer click_sound;
	private boolean is_mute = true;

	private void playClickedSound() {
		if (this.is_mute) {
			this.click_sound.start();
		}
	}

	private void playCheckedSound() {
		if (this.is_mute) {
			this.checked_sound.start();
		}
	}

	private void playUncheckedSound() {
		if (this.is_mute) {
			this.unchecked_sound.start();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.settings.getBoolean(CommonClass.PREF_ENABLE_UI_PREF_SOUNDS,
				false)) {
			this.is_mute = true;
		} else {
			this.is_mute = false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.checked_sound = MediaPlayer.create(this, R.raw.checked);
		this.unchecked_sound = MediaPlayer.create(this, R.raw.unchecked);
		this.click_sound = MediaPlayer.create(this, R.raw.click);
		this.settings = getSharedPreferences(CommonClass.PREF_NAME,
				MODE_PRIVATE);

		if (this.settings.getBoolean(CommonClass.PREF_ENABLE_UI_PREF_SOUNDS,
				false)) {
			this.is_mute = true;
		} else {
			this.is_mute = false;
		}

		CommonClass
				.myLog(this.settings, "Preferences - Begin", CommonClass.YES);

		addPreferencesFromResource(R.xml.dev_preferences);
		
		String originalTitle = getTitle().toString();

		setTitle(originalTitle + " "
				+ getResources().getText(R.string.version_name));

		CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference("verbose_key");
		mCheckBoxPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (preference instanceof CheckBoxPreference) {
							SharedPreferences.Editor editor = getSharedPreferences(
									CommonClass.PREF_NAME, MODE_PRIVATE).edit();
							CheckBoxPreference check = (CheckBoxPreference) preference;
							if (check.isChecked()) {
								playCheckedSound();
								editor.putBoolean(CommonClass.PREF_VERBOSE_KEY,
										true);
							} else {
								playUncheckedSound();
								editor.putBoolean(CommonClass.PREF_VERBOSE_KEY,
										false);
							}
							editor.commit();
						}
						return false;
					}
				});

		ListPreference lp = (ListPreference) findPreference(CommonClass.PREF_THEME_SELECTION);
		lp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				playClickedSound();
				return true;
			}
		});

		lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (preference instanceof ListPreference) {
					ListPreference lp = (ListPreference) preference;
					String nv = (String) newValue;
					String[] array = getResources().getStringArray(R.array.all_theme_name);
					
					lp.setValue((String) newValue);

					if (nv.equals(array[0])) {
						// layout without buttons
						Preferences.this.settings
								.edit()
								.putString(CommonClass.PREF_THEME_SELECTION,
										CommonClass.THEME_NO_BUTTON).commit();
					} else {
						Preferences.this.settings
								.edit()
								.putString(CommonClass.PREF_THEME_SELECTION,
										CommonClass.THEME_WITH_BUTTON).commit();
					}
				}
				Intent i = new Intent(getBaseContext(), HomeActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(i);

				return false;
			}
		});

		mCheckBoxPref = (CheckBoxPreference) findPreference("pref_disable_batt_br");
		if (this.settings.getInt(
				CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
				CommonClass.YES) == CommonClass.YES)
			mCheckBoxPref.setChecked(true);
		else
			mCheckBoxPref.setChecked(false);
		mCheckBoxPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (preference instanceof CheckBoxPreference) {
							SharedPreferences.Editor editor = getSharedPreferences(
									CommonClass.PREF_NAME, MODE_PRIVATE).edit();
							CheckBoxPreference check = (CheckBoxPreference) preference;
							CommonClass.myLog(Preferences.this.settings,
									"disable_battery_br clicked",
									CommonClass.YES);
							if (check.isChecked()) {
								playCheckedSound();
								CommonClass.myLog(Preferences.this.settings,
										"disable_battery_br checked",
										CommonClass.NO);
								editor.putInt(

										CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
										CommonClass.YES);
							} else {
								playUncheckedSound();
								editor.putInt(
										CommonClass.PREF_ENABLE_BATTERY_EVENTS_RECEIVER,
										CommonClass.NO);
								CommonClass.myLog(Preferences.this.settings,
										"disable_battery_br is NOT checked",
										CommonClass.NO);
							}
							editor.commit();
						}
						return false;
					}
				});

		mCheckBoxPref = (CheckBoxPreference) findPreference("pref_hide_kernel_cpu_info_key");
		mCheckBoxPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (preference instanceof CheckBoxPreference) {
							SharedPreferences.Editor editor = getSharedPreferences(
									CommonClass.PREF_NAME, MODE_PRIVATE).edit();
							CheckBoxPreference check = (CheckBoxPreference) preference;
							if (check.isChecked()) {
								playCheckedSound();
								editor.putBoolean(
										CommonClass.PREF_ENABLE_MISC_ACTIVITY,
										true);
							} else {
								playUncheckedSound();
								editor.putBoolean(

								CommonClass.PREF_ENABLE_MISC_ACTIVITY, false);
							}
							editor.commit();
						}
						return false;
					}
				});

		mCheckBoxPref = (CheckBoxPreference) findPreference("pref_less1perc_top_key");
		mCheckBoxPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						CommonClass.myLog(Preferences.this.settings,
								"pref_less1perc_top clicked", CommonClass.YES);
						if (preference instanceof CheckBoxPreference) {
							SharedPreferences settings = getSharedPreferences(
									CommonClass.PREF_NAME, MODE_PRIVATE);
							SharedPreferences.Editor editor = settings.edit();
							CheckBoxPreference check = (CheckBoxPreference) preference;
							if (check.isChecked()) {
								playCheckedSound();
								CommonClass.myLog(Preferences.this.settings,
										"pref_less1perc_top is checked_sound",
										CommonClass.YES);
								editor.putBoolean(
										CommonClass.PREF_LESS1PERC_BAR_GRAPH_TOP,
										true);

							} else {
								playUncheckedSound();
								CommonClass
										.myLog(Preferences.this.settings,
												"pref_less1perc_top is NOT checked_sound",
												CommonClass.YES);
								editor.putBoolean(
										CommonClass.PREF_LESS1PERC_BAR_GRAPH_TOP,
										false);

							}
							editor.commit();
						}
						return false;
					}
				});

		mCheckBoxPref = (CheckBoxPreference) findPreference("pref_enable_ui_sounds");
		mCheckBoxPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences settings = getSharedPreferences(
								CommonClass.PREF_NAME, MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.remove(CommonClass.PREF_ENABLE_UI_SOUNDS)
								.commit();
						CommonClass.myLog(Preferences.this.settings,
								"pref_enable_ui_sounds clicked",
								CommonClass.YES);
						if (preference instanceof CheckBoxPreference) {

							CheckBoxPreference check = (CheckBoxPreference) preference;
							if (check.isChecked()) {
								playCheckedSound();
								CommonClass.myLog(Preferences.this.settings,
										"pref_less1perc_top is checked_sound",
										CommonClass.YES);
								editor.putBoolean(
										CommonClass.PREF_ENABLE_UI_SOUNDS, true)
										.commit();
								Preferences.this.is_mute = false;

							} else {
								playUncheckedSound();
								CommonClass
										.myLog(Preferences.this.settings,
												"pref_less1perc_top is NOT checked_sound",
												CommonClass.YES);
								editor.putBoolean(
										CommonClass.PREF_ENABLE_UI_SOUNDS,
										false).commit();
								Preferences.this.is_mute = true;

							}
							editor.commit();
						}
						return false;
					}
				});

		mCheckBoxPref = (CheckBoxPreference) findPreference("pref_enable_ui_pref_sounds");
		mCheckBoxPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						CommonClass.myLog(Preferences.this.settings,
								"pref_enable_ui_sounds clicked",
								CommonClass.YES);
						if (preference instanceof CheckBoxPreference) {
							SharedPreferences settings = getSharedPreferences(
									CommonClass.PREF_NAME, MODE_PRIVATE);
							SharedPreferences.Editor editor = settings.edit();
							CheckBoxPreference check = (CheckBoxPreference) preference;
							if (check.isChecked()) {
								playCheckedSound();
								CommonClass.myLog(Preferences.this.settings,
										"pref_less1perc_top is checked_sound",
										CommonClass.YES);
								editor.putBoolean(
										CommonClass.PREF_ENABLE_UI_PREF_SOUNDS,
										true).commit();
								Preferences.this.is_mute = false;

							} else {
								playUncheckedSound();
								CommonClass
										.myLog(Preferences.this.settings,
												"pref_less1perc_top is NOT checked_sound",
												CommonClass.YES);
								editor.putBoolean(
										CommonClass.PREF_ENABLE_UI_PREF_SOUNDS,
										false).commit();
								Preferences.this.is_mute = true;

							}
							editor.commit();
						}
						Intent i = new Intent(getBaseContext(),
								Preferences.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						return false;
					}
				});

		Preference preference = findPreference("pref_show_info_key");
		preference
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						playClickedSound();
						Intent i = new Intent(getBaseContext(),
								InformationActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						CommonClass.myLog(Preferences.this.settings,
								"show_info_key clicked", CommonClass.YES);
						return false;
					}
				});

		preference = findPreference("pref_write_email_key");
		preference
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						playClickedSound();
						Intent i = new Intent(Intent.ACTION_SEND);
						i.setType("message/rfc822");
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { "cpuspy.realgpp@gmail.com" });
						i.putExtra(Intent.EXTRA_SUBJECT,
								"Cpuspy Plus - Message for you");

						try {
							startActivity(Intent.createChooser(i,
									"Send mail..."));
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(HomeActivity.getAppContext(),
									"There are no email clients installed.",
									Toast.LENGTH_SHORT).show();
						}
						return false;
					}
				});

		
		preference = findPreference("pref_xda_thread_key");
		if (originalTitle.endsWith("XDA")) {

			preference
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							playClickedSound();
							Intent viewIntent = new Intent(
									"android.intent.action.VIEW",
									Uri.parse("http://forum.xda-developers.com/showthread.php?t=1740622"));
							startActivity(viewIntent);
							return false;
						}
					});
		} else {
			PreferenceCategory pc = (PreferenceCategory) findPreference("info_contacts");
			pc.removePreference(preference);

		}

		PreferenceScreen prefScreen = (PreferenceScreen) findPreference("layout_options");
		prefScreen
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						playClickedSound();
						return true;
					}
				});

		prefScreen = (PreferenceScreen) findPreference("audio_options");
		prefScreen
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						playClickedSound();
						return true;
					}
				});

		prefScreen = (PreferenceScreen) findPreference("cable_options");
		prefScreen
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						playClickedSound();
						return true;
					}
				});

		CommonClass.myLog(this.settings, "Preferences - End", CommonClass.YES);
	}
}
