package com.bvalosek.cpuspy.realgpp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.bvalosek.cpuspy.realgpp.CustomExceptionHandler;
import com.bvalosek.cpuspy.realgpp.R;
import com.bvalosek.cpuspy.realgpp.SystemUtils;

public class MiscActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Thread.currentThread().setUncaughtExceptionHandler(
				new CustomExceptionHandler(getApplicationContext(), null));

		super.onCreate(savedInstanceState);
		setContentView(R.layout.misc_activity);

		String str = "";
		TextView tv = (TextView) findViewById(R.id.tv_misc_cpuInfo);

		str = getResources().getString(R.string.ui_misc_freqs_range)+" " 
				+ String.valueOf(SystemUtils.getCPUFrequencyMin() / 1000)
				+ "MHz - "
				+ String.valueOf(SystemUtils.getCPUFrequencyMax() / 1000)
				+ "MHz\n";
		str += getResources().getString(R.string.ui_misc_scaling_range) +" " 
				+ String.valueOf(SystemUtils.getCPUFrequencyMinScaling() / 1000)
				+ "MHz - "
				+ String.valueOf(SystemUtils.getCPUFrequencyMaxScaling() / 1000)
				+ "MHz\n";

		tv.setText(str + SystemUtils.getCPUInfo());

		tv = (TextView) findViewById(R.id.misc_tv_kernel_info);
		tv.setText(SystemUtils.getKernelInfo());
	}

}
