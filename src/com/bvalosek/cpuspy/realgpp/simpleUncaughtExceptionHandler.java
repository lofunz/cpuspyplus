package com.bvalosek.cpuspy.realgpp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Environment;
import android.util.Log;

/**
 * TODO Put here a description of what this class does.
 * 
 * @author realgpp. Created 23/giu/2012.
 */
public class simpleUncaughtExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String date = df.format(cal.getTime()) + ":\n";

		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(
					Environment.getExternalStorageDirectory() + "/"
							+ CommonClass.PATH_LOG_FILE, true));
			// Log.e(commonsVars.TAG, ex.getMessage());
			Log.e("HomeActivity", "uncaughtException: must check "
					+ CommonClass.PATH_LOG_FILE + " file. " + thread.getName()
					+ " : " + ex.getMessage());

			ex.printStackTrace(pw);
			pw.write("########\n" + date + "\nException Error Message:\n"
					+ ex.getMessage());
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
