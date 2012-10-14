package com.bvalosek.cpuspy.realgpp.ui;

import java.io.File;
import com.bvalosek.cpuspy.realgpp.R;
import com.bvalosek.cpuspy.realgpp.SystemUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bvalosek.cpuspy.realgpp.CommonClass;

public class LogFileActivity extends Activity {

	String filePath = Environment.getExternalStorageDirectory() + "/"
			+ CommonClass.PATH_LOG_FILE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_log_file);		

		Button delet = (Button) findViewById(R.id.buttonDeleteLogfile);
		delet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(LogFileActivity.this.filePath);
				boolean deleted = file.delete();
				if (deleted) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"Logfile deleted", Toast.LENGTH_SHORT);
					toast.show();
					TextView tv = (TextView) findViewById(R.id.logfile_content);
					tv.setText(SystemUtils.readFile(LogFileActivity.this.filePath));
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"Logfile NOT deleted", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});

		Button refresh = (Button) findViewById(R.id.buttonReloadLogFile);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView tv = (TextView) findViewById(R.id.logfile_content);
				tv.setText(SystemUtils.readFile(LogFileActivity.this.filePath));
				Toast toast = Toast.makeText(getApplicationContext(),
						"Logfile reloaded", Toast.LENGTH_SHORT);
				toast.show();
			}
		});

		TextView tv = (TextView) findViewById(R.id.logfile_content);
		tv.setText(SystemUtils.readFile(this.filePath));
	}
}
