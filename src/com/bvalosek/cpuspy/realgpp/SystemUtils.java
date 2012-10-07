package com.bvalosek.cpuspy.realgpp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;



public class SystemUtils {
	/**
	 * @return in kiloHertz.
	 * @throws SystemUtilsException
	 */
	public static int getCPUFrequencyMinScaling() throws Exception {
		return SystemUtils
				.readSystemFileAsInt("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
	}

	public static int getCPUFrequencyMaxScaling() throws Exception {
		return SystemUtils
				.readSystemFileAsInt("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
	}

	/**
	 * @return in kiloHertz.
	 * @throws SystemUtilsException
	 */
	public static int getCPUFrequencyMax() throws Exception {
		return SystemUtils
				.readSystemFileAsInt("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
	}

	/**
	 * @return in kiloHertz.
	 * @throws SystemUtilsException
	 */
	public static int getCPUFrequencyMin() throws Exception {
		return SystemUtils
				.readSystemFileAsInt("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");
	}
	
	public static String getGovernor()
			throws Exception {
		String pSystemFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
		InputStream in = null;
		try {
			final Process process = new ProcessBuilder(new String[] {
					"/system/bin/cat", pSystemFile }).start();

			in = process.getInputStream();
			final String content = readFully(in);
			return content;
		} catch (final Exception e) {
			throw new Exception(e);
		}
	}

	

	private static int readSystemFileAsInt(final String pSystemFile)
			throws Exception {
		InputStream in = null;
		try {
			final Process process = new ProcessBuilder(new String[] {
					"/system/bin/cat", pSystemFile }).start();

			in = process.getInputStream();
			final String content = readFully(in);
			return Integer.parseInt(content);
		} catch (final Exception e) {
			throw new Exception(e);
		}
	}

	public static final String readFully(final InputStream pInputStream)
			throws IOException {
		final StringBuilder sb = new StringBuilder();
		final Scanner sc = new Scanner(pInputStream);
		while (sc.hasNextLine()) {
			sb.append(sc.nextLine());
		}
		return sb.toString();
	}

}
