package com.bvalosek.cpuspy.realgpp.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.bvalosek.cpuspy.realgpp.R;

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
			+ CommonClass.LOG_FILE;

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
					tv.setText(readFile(LogFileActivity.this.filePath));
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
				tv.setText(readFile(LogFileActivity.this.filePath));
				Toast toast = Toast.makeText(getApplicationContext(),
						"Logfile reloaded", Toast.LENGTH_SHORT);
				toast.show();
			}
		});

		TextView tv = (TextView) findViewById(R.id.logfile_content);
		tv.setText(readFile(this.filePath));
	}

	private String readFile(String filename) {
		String result = "";
		try {

			InputStream is = new FileInputStream(filename);
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);

			String line;

			// read every line of the file into the line-variable, on line at
			// the time
			while ((line = br.readLine()) != null) {
				result += line + "\n";
			}

			// close the file again
			is.close();
		} catch (java.io.FileNotFoundException e) {

			result = "file doesn't exist:\n" + filename;
		} catch (IOException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
		}
		return result;
	}

}
