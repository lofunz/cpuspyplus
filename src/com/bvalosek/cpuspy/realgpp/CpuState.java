package com.bvalosek.cpuspy.realgpp;

/**
 * simple struct for states/time
 */
public class CpuState implements Comparable<CpuState> {
	/** init with freq and duration */
	public CpuState(int a, long b) {
		this.freq = a;
		this.duration = b;
	}

	public int freq = 0;
	public long duration = 0;

	/** for sorting, compare the freqs */
	@Override
	public int compareTo(CpuState state) {
		Integer a = new Integer(this.freq);
		Integer b = new Integer(state.freq);
		return a.compareTo(b);
	}
}