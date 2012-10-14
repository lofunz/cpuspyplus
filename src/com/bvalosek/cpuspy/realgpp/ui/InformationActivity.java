package com.bvalosek.cpuspy.realgpp.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bvalosek.cpuspy.realgpp.CommonClass;
import com.bvalosek.cpuspy.realgpp.R;

public class InformationActivity extends Activity {

	SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.settings = getSharedPreferences(CommonClass.PREF_NAME,
				MODE_PRIVATE);
		CommonClass.myLog(this.settings, "InformationActivity - Begin",
				CommonClass.YES);

		setContentView(R.layout.information);

		CommonClass.myLog(this.settings, "InformationActivity - End",
				CommonClass.YES);
	}

}
