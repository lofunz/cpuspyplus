package com.bvalosek.cpuspy.realgpp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.NumberFormatException;

public class SystemUtils {
	/**
	 * @return in kiloHertz.
	 */
	public static int getCPUFrequencyMinScaling() {
		int ret = 0;
		try {
			String valueStr = SystemUtils.readFile(
					CommonClass.PATH_SCALING_MIN_FREQ)
					.replace("\n", "");
			ret = Integer.valueOf(valueStr);
		} catch (NumberFormatException nfe) {
			ret = 0;
		}
		return ret;
	}

	/**
	 * @return in kiloHertz.
	 */
	public static int getCPUFrequencyMaxScaling() {
		int ret = 0;
		try {
			String valueStr = SystemUtils.readFile(
					CommonClass.PATH_SCALING_MAX_FREQ)
					.replace("\n", "");
			ret = Integer.valueOf(valueStr);
		} catch (NumberFormatException nfe) {
			ret = 0;
		}
		return ret;
	}

	/**
	 * @return in kiloHertz.
	 */
	public static int getCPUFrequencyMax() {
		int ret = 0;
		try {
			String valueStr = SystemUtils.readFile(
					CommonClass.PATH_CPUINFO_MAX_FREQ)
					.replace("\n", "");
			ret = Integer.valueOf(valueStr);
		} catch (NumberFormatException nfe) {
			ret = 0;
		}
		return ret;
	}

	/**
	 * @return in kiloHertz.
	 */
	public static int getCPUFrequencyMin() {
		int ret = 0;
		try {
			String valueStr = SystemUtils.readFile(
					CommonClass.PATH_CPUINFO_MIN_FREQ)
					.replace("\n", "");
			ret = Integer.valueOf(valueStr);
		} catch (NumberFormatException nfe) {
			ret = 0;
		}
		return ret;
	}

	public static String getGovernor(){
		String governorStr = SystemUtils
				.readFile(CommonClass.PATH_SCALING_GOVERNOR).replace("\n","");;
		return governorStr;
	}
	
	public static String getKernelInfo() {
		return SystemUtils.readFile(CommonClass.PATH_KERNEL_INFO).replace("\n","");
	}
	
	public static String getCPUInfo() {
		return SystemUtils.readFile(CommonClass.PATH_CPU_INFO);
	}

	static public String readFile(String filename) {
		String result = "";
		try {

			InputStream is = new FileInputStream(filename);
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);

			String line;
			while ((line = br.readLine()) != null) {
				result += line + "\n";
			}

			// close the file again
			is.close();
		} catch (java.io.FileNotFoundException e) {
			result = "File doesn't exist:\n" + filename;
		} catch (IOException exception) {
			result = "IO error:\n" + filename;
		}
		return result;
	}

}
