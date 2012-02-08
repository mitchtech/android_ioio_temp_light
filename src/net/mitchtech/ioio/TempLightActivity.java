package net.mitchtech.ioio;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import net.mitchtech.ioio.templight.R;
import android.os.Bundle;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

public class TempLightActivity extends AbstractIOIOActivity {
	
	private final int TMP36_PIN = 34;
	private final int PHOTOCELL_PIN = 35;
	
	TextView mTempTextView;
	TextView mLightTextView;

	SeekBar mLightSeekBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mTempTextView = (TextView) findViewById(R.id.tvTemp);
		
		mLightTextView = (TextView) findViewById(R.id.tvLight);
		mLightSeekBar = (SeekBar) findViewById(R.id.sbLight);

		enableUi(false);
	}

	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		private AnalogInput mTempInput;
		private AnalogInput mLightInput;

		@Override
		public void setup() throws ConnectionLostException {
			try {
				mTempInput = ioio_.openAnalogInput(TMP36_PIN);
				mLightInput = ioio_.openAnalogInput(PHOTOCELL_PIN);
				enableUi(true);
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
			}
		}

		@Override
		public void loop() throws ConnectionLostException {
			try {
				final float lightReading = mLightInput.read();
				setSeekBar((int) (lightReading * 100));

				final float voltage = mTempInput.getVoltage();
				float raw = ((voltage * 1024) - 500) / 10;
				int celsius = Math.round(raw);
				celsius -= 4;
				int fahrenheit = (int) ((celsius * (9.0 / 5.0)) + 32.0);

				String tempString = Integer.toString(fahrenheit) + " F / "
						+ Integer.toString(celsius) + " C";
				setText(Float.toString((lightReading * 100)), tempString);
				sleep(10);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
			}
		}
	}

	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLightSeekBar.setEnabled(enable);
			}
		});
	}

	private void setSeekBar(final int value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLightSeekBar.setProgress(value);
			}
		});
	}

	private void setText(final String lightStr, final String tempStr) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLightTextView.setText(lightStr);
				mTempTextView.setText(tempStr);
			}
		});
	}
}