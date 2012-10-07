/*
 * source: http://robobunny.com/wp/2011/08/13/android-seekbar-preference/
 */

package com.bvalosek.cpuspy.realgpp;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class mySeekBarPreference extends Preference implements OnSeekBarChangeListener {
	
	private final String TAG = "realgpp";
	
	private static final String ANDROIDNS="http://schemas.android.com/apk/res/android";
	private static final String ROBOBUNNYNS="http://robobunny.com";
	private static final int DEFAULT_VALUE = 100;
	
	private int mMaxValue      = 100;
	private int mMinValue      = 0;
	private int mInterval      = 1;
	private int mCurrentValue;
	private String mUnitsLeft  = "";
	private String mUnitsRight = "";
	private SeekBar mSeekBar;
	
	private TextView mStatusText;

	public mySeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}

	public mySeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	private void initPreference(Context context, AttributeSet attrs) {
		setValuesFromXml(attrs);
		this.mSeekBar = new SeekBar(context, attrs);
		this.mSeekBar.setMax(this.mMaxValue - this.mMinValue);
		this.mSeekBar.setOnSeekBarChangeListener(this);
	}
	
	private void setValuesFromXml(AttributeSet attrs) {
		this.mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
		this.mMinValue = attrs.getAttributeIntValue(ROBOBUNNYNS, "min", 0);
		
		this.mUnitsLeft = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsLeft", "");
		String units = getAttributeStringValue(attrs, ROBOBUNNYNS, "units", "");
		this.mUnitsRight = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsRight", units);
		
		try {
			String newInterval = attrs.getAttributeValue(ROBOBUNNYNS, "interval");
			if(newInterval != null)
				this.mInterval = Integer.parseInt(newInterval);
		}
		catch(Exception e) {
			Log.e(this.TAG, "Invalid interval value", e);
		}
		
	}
	
	private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
		String value = attrs.getAttributeValue(namespace, name);
		if(value == null)
			value = defaultValue;
		
		return value;
	}
	
	@Override
	protected View onCreateView(ViewGroup parent){
		
		RelativeLayout layout =  null;
		
		try {
			LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			layout = (RelativeLayout)mInflater.inflate(R.layout.seek_bar_preference, parent, false);
		}
		catch(Exception e)
		{
			Log.e(this.TAG, "Error creating seek bar preference", e);
		}

		return layout;
		
	}
	
	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		try
		{
			// move our seekbar to the new view we've been given
	        ViewParent oldContainer = this.mSeekBar.getParent();
	        ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);
	        
	        if (oldContainer != newContainer) {
	        	// remove the seekbar from the old view
	            if (oldContainer != null) {
	                ((ViewGroup) oldContainer).removeView(this.mSeekBar);
	            }
	            // remove the existing seekbar (there may not be one) and add ours
	            newContainer.removeAllViews();
	            newContainer.addView(this.mSeekBar, ViewGroup.LayoutParams.FILL_PARENT,
	                    ViewGroup.LayoutParams.WRAP_CONTENT);
	        }
		}
		catch(Exception ex) {
			Log.e(this.TAG, "Error binding view: " + ex.toString());
		}

		updateView(view);
	}
    
	/**
	 * Update a mySeekBarPreference view with our current state
	 * @param view
	 */
	protected void updateView(View view) {

		try {
			RelativeLayout layout = (RelativeLayout)view;

			this.mStatusText = (TextView)layout.findViewById(R.id.seekBarPrefValue);
			this.mStatusText.setText(String.valueOf(this.mCurrentValue));
			this.mStatusText.setMinimumWidth(30);
			
			this.mSeekBar.setProgress(this.mCurrentValue - this.mMinValue);

			TextView unitsRight = (TextView)layout.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(this.mUnitsRight);
			
			TextView unitsLeft = (TextView)layout.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(this.mUnitsLeft);
			
		}
		catch(Exception e) {
			Log.e(this.TAG, "Error updating seek bar preference", e);
		}
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int newValue = progress + this.mMinValue;
		
		if(newValue > this.mMaxValue)
			newValue = this.mMaxValue;
		else if(newValue < this.mMinValue)
			newValue = this.mMinValue;
		else if(this.mInterval != 1 && newValue % this.mInterval != 0)
			newValue = Math.round(((float)newValue)/this.mInterval)*this.mInterval;  
		
		// change rejected, revert to the previous value
		if(!callChangeListener(newValue)){
			seekBar.setProgress(this.mCurrentValue - this.mMinValue); 
			return; 
		}

		// change accepted, store it
		this.mCurrentValue = newValue;
		this.mStatusText.setText(String.valueOf(newValue));
		persistInt(newValue);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		notifyChanged();
	}


	@Override 
	protected Object onGetDefaultValue(TypedArray ta, int index){
		
		int defaultValue = ta.getInt(index, DEFAULT_VALUE);
		return defaultValue;
		
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		if(restoreValue) {
			this.mCurrentValue = getPersistedInt(this.mCurrentValue);
		}
		else {
			int temp = 0;
			try {
				temp = (Integer)defaultValue;
			}
			catch(Exception ex) {
				Log.e(this.TAG, "Invalid default value: " + defaultValue.toString());
			}
			
			persistInt(temp);
			this.mCurrentValue = temp;
		}
		
	}
	
}

