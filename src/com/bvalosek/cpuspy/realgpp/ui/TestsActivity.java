package com.bvalosek.cpuspy.realgpp.ui;

import com.bvalosek.cpuspy.realgpp.CommonClass;
import com.bvalosek.cpuspy.realgpp.CustomExceptionHandler;
import com.bvalosek.cpuspy.realgpp.R;
import com.bvalosek.cpuspy.realgpp.SystemUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/*
 * It tries to execute all the file system reads necessary to the app
 */

public class TestsActivity extends Activity {
	int MAX_VALUE_BAR;
	ProgressBar progressBar;
	TextView progressStat, logOperations;
	Button btt_start_tests;
	
	String[] all_tests;
	
	public class BackgroundAsyncTask extends AsyncTask<Void, String, Void> {

		@Override
		protected void onPostExecute(Void result) {
			TestsActivity.this.btt_start_tests.setClickable(true);
		}

		@Override
		protected void onPreExecute() {
			TestsActivity.this.progressBar
					.setMax(TestsActivity.this.MAX_VALUE_BAR);
	
			TestsActivity.this.logOperations.setText("");
		}

		@Override
		protected Void doInBackground(Void... params) {
			String progress[] = { "", "","" };
			/*
			 * Read state_in_time file
			 */
			SystemClock.sleep(150);
			progress[0] = "1";
			progress[1] = TestsActivity.this.all_tests[0];
			String str = SystemUtils
					.readFile(CommonClass.PATH_TIME_IN_STATE_CORE0);

			
			if (str.startsWith("File doesn't exist:\n")
					|| str.startsWith("IO error:\n")) {
				progress[1] += " FAILED\n";
			} else {
				progress[1] += " PASSED\n";
			}
			
			progress[2] = progress[0] + " / " + String.valueOf(TestsActivity.this.MAX_VALUE_BAR);

			publishProgress(progress);
			
			/*
			 * Read kernel info
			 */
			
			SystemClock.sleep(150);
			progress[0] = "2";
			progress[1] = TestsActivity.this.all_tests[1];
			str = SystemUtils
					.getKernelInfo();
			
			if (str.startsWith("File doesn't exist:\n")
					|| str.startsWith("IO error:\n")) {
				progress[1] += " FAILED\n";
			} else {
				progress[1] += " PASSED\n";
			}
			
			progress[2] = progress[0] + " / " + String.valueOf(TestsActivity.this.MAX_VALUE_BAR);

			publishProgress(progress);
			
			/*
			 * Read CPU infos
			 */
			
			SystemClock.sleep(150);
			progress[0] = "3";
			progress[1] = TestsActivity.this.all_tests[2];
			str = SystemUtils.getCPUInfo();
			
			if (str.startsWith("File doesn't exist:\n")
					|| str.startsWith("IO error:\n")) {
				progress[1] += " FAILED\n";
			} else {
				progress[1] += " PASSED\n";
			}
			
			progress[2] = progress[0] + " / " + String.valueOf(TestsActivity.this.MAX_VALUE_BAR);

			publishProgress(progress);
			
			/*
			 * Reading CPU freqs range
			 */
			
			SystemClock.sleep(150);
			progress[0] = "4";
			progress[1] = TestsActivity.this.all_tests[3];
			int max = SystemUtils.getCPUFrequencyMax();
			int min = SystemUtils.getCPUFrequencyMin();
			
			if ( max == 0 && min == 0) {
				progress[1] += " FAILED\n";
			} else {
				progress[1] += " PASSED\n";
			}
			
			progress[2] = progress[0] + " / " + String.valueOf(TestsActivity.this.MAX_VALUE_BAR);

			publishProgress(progress);
			
			
			/*
			 * Reading CPU freqs scaling
			 */
			
			SystemClock.sleep(150);
			progress[0] = "5";
			progress[1] = TestsActivity.this.all_tests[4];
			 max = SystemUtils.getCPUFrequencyMaxScaling();
			 min = SystemUtils.getCPUFrequencyMinScaling();
			
			if ( max == 0 && min == 0) {
				progress[1] += " FAILED\n";
			} else {
				progress[1] += " PASSED\n";
			}
			
			progress[2] = progress[0] + " / " + String.valueOf(TestsActivity.this.MAX_VALUE_BAR);

			publishProgress(progress);
			
			/*
			 * Read CPU infos
			 */
			
			SystemClock.sleep(150);
			progress[0] = "6";
			progress[1] = TestsActivity.this.all_tests[5];
			str = SystemUtils.getGovernor();
			
			if (str.startsWith("File doesn't exist:\n")
					|| str.startsWith("IO error:\n")) {
				progress[1] += " FAILED\n";
			} else {
				progress[1] += " PASSED\n";
			}
			
			progress[2] = progress[0] + " / " + String.valueOf(TestsActivity.this.MAX_VALUE_BAR);

			publishProgress(progress);

			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			TestsActivity.this.progressBar.setProgress(Integer
					.valueOf(values[0]));
			TestsActivity.this.logOperations.append(values[1]);
			TestsActivity.this.progressStat.setText(values[2]);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Thread.currentThread().setUncaughtExceptionHandler(
				new CustomExceptionHandler(getApplicationContext(), null));

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests);
		
		this.all_tests = getResources().getStringArray(R.array.all_tests_name);
		
		this.MAX_VALUE_BAR = this.all_tests.length;
		
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		this.logOperations = ( TextView) findViewById(R.id.textView_testlog);
		this.progressStat  = ( TextView) findViewById(R.id.textView_progress_stats);
		
		this.progressBar.setProgress(0);
		
		this.btt_start_tests = (Button) findViewById(R.id.button_start_tests);
		this.btt_start_tests.setOnClickListener(new Button.OnClickListener(){

			   @Override
			   public void onClick(View v) {
			    // TODO Auto-generated method stub
			    new BackgroundAsyncTask().execute();
			    v.setClickable(false);
			   }});
	}
}
