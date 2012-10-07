package com.bvalosek.cpuspy.realgpp.ui;

/**
 * TODO Put here a description of what this class does.
 *
 * @author realgpp.
 *         Created 24/giu/2012.
 */

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

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

		TextView tv = (TextView) findViewById(R.id.textView_xda_url);

		String url = "http://forum.xda-developers.com/showthread.php?p=28045183";
		Pattern pattern = Pattern.compile(url);
		Linkify.addLinks(tv, pattern, "http://");
		tv.setText(url);
		CommonClass.myLog(this.settings, "InformationActivity - End",
				CommonClass.YES);
	}

}
