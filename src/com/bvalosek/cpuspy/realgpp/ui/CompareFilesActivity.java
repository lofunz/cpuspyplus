package com.bvalosek.cpuspy.realgpp.ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.bvalosek.cpuspy.realgpp.R;

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
		tv.setText(readFile(CommonClass.AFFECTED_CPUS_PATH_CPU0));
		
		tv = (TextView) findViewById(R.id.textViewCpu1AffectedCpus);
		tv.setText(readFile(CommonClass.AFFECTED_CPUS_PATH_CPU1));

		tv = (TextView) findViewById(R.id.textViewCpu0);
		tv.setText(readFile(CommonClass.TIME_IN_STATE_PATH_CORE0));

		tv = (TextView) findViewById(R.id.textViewCpu1);
		tv.setText(ls(CommonClass.TIME_IN_STATE_PATH_CORE1));
		
		CommonClass.myLog(this.settings, "CompareFilesActivity - End", CommonClass.YES);
	}

	String readFile(String filename) {
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

	String ls(String filename) {
		try {
			// Executes the command.
			Process process = Runtime.getRuntime().exec(
					"/system/bin/cat " + filename);

			// Reads stdout.
			// NOTE: You can write to stdin of the command using
			// process.getOutputStream().
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();

			// Waits for the command to finish.
			process.waitFor();
			if ( output.length() == 0)
				return "file doesn't exist:\n" + filename;
			return output.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
