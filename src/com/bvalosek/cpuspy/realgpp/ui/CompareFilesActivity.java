package com.bvalosek.cpuspy.realgpp.ui;

import com.bvalosek.cpuspy.realgpp.R;
import com.bvalosek.cpuspy.realgpp.SystemUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.bvalosek.cpuspy.realgpp.CommonClass;

public class CompareFilesActivity extends Activity {
	SharedPreferences settings;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compare_state_files);
		
		this.settings = getSharedPreferences(CommonClass.PREF_NAME,
				MODE_PRIVATE);
		CommonClass.myLog(this.settings, "CompareFilesActivity - Begin", CommonClass.YES);

		TextView tv = (TextView) findViewById(R.id.textViewCpu0AffectedCpus);
		tv.setText(SystemUtils.readFile(CommonClass.AFFECTED_CPUS_PATH_CPU0));
		
		tv = (TextView) findViewById(R.id.textViewCpu1AffectedCpus);
		tv.setText(SystemUtils.readFile(CommonClass.AFFECTED_CPUS_PATH_CPU1));

		tv = (TextView) findViewById(R.id.textViewCpu0);
		tv.setText(SystemUtils.readFile(CommonClass.PATH_TIME_IN_STATE_CORE0));

		tv = (TextView) findViewById(R.id.textViewCpu1);
		tv.setText(SystemUtils.readFile(CommonClass.PATH_TIME_IN_STATE_CORE1));
		
		CommonClass.myLog(this.settings, "CompareFilesActivity - End", CommonClass.YES);
	}
}
