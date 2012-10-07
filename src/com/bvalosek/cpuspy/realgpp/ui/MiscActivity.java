package com.bvalosek.cpuspy.realgpp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.bvalosek.cpuspy.realgpp.R;
import com.bvalosek.cpuspy.realgpp.SystemUtils;

public class MiscActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.misc_activity);

		String str = "";
		TextView tv = (TextView) findViewById(R.id.tv_misc_cpuInfo);
		try {
			str = "Freqs range: "
					+ String.valueOf(SystemUtils.getCPUFrequencyMin() / 1000)
					+ "MHz - "
					+ String.valueOf(SystemUtils.getCPUFrequencyMax() / 1000)
					+ "MHz\n";
			str += "Scaling range: "
					+ String.valueOf(SystemUtils.getCPUFrequencyMinScaling() / 1000)
					+ "MHz - "
					+ String.valueOf(SystemUtils.getCPUFrequencyMaxScaling() / 1000)
					+ "MHz\n";
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		tv.setText(str + fetch_cpu_info());
		
		
		tv = (TextView) findViewById(R.id.misc_tv_kernel_info);
		tv.setText(fetch_kernel_info());
	}

	public static String fetch_cpu_info() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
			result = cmdexe.run(args, "/system/bin/");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public static String fetch_kernel_info() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/proc/version" };
			result = cmdexe.run(args, "/system/bin/");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}
}

class CMDExecute {

	public synchronized String run(String[] cmd, String workdirectory)
			throws IOException {
		String result = "";

		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			// set working directory
			if (workdirectory != null)
				builder.directory(new File(workdirectory));
			builder.redirectErrorStream(true);
			Process process = builder.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while (in.read(re) != -1) {
				System.out.println(new String(re));
				result = result + new String(re);
			}
			in.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

}
