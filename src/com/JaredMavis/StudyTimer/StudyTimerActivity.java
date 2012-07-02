package com.JaredMavis.StudyTimer;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class StudyTimerActivity extends Activity {
	private static final String TAG = "StudyTimerActivity";
	private final int ClockUpdateInterval = 750; // ms

	private TextView statusDisplay;
	private TextView timeDisplay;
	private int timePerStudySession; // the amount of time to spend on every
	// session in minutes
	private int timePerBreakSession;// the amount of time to spend on every
	// break in minutes
	private int timePerLongBreakSession; // in minutes
	private int studySessionNumber; // the current session that is being worked
	// on
	private Button startButton;

	private Part timer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        statusDisplay = (TextView) findViewById(R.id.SessionTitle);
		timeDisplay = (TextView) findViewById(R.id.TimeLeftInSession);

		timePerStudySession = 25;

		timer = new StudySession();


		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(startButtonListener());
    }

    private View.OnClickListener startButtonListener(){
    	View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				timer.start();
			}
		};

		return listener;
    }

    public int MinToMilli(int mins){
		return (mins * 60 * 1000);
	}

	public class StudySession extends Part {
		StudySession(){
			super(MinToMilli(timePerStudySession), ClockUpdateInterval, "Study");
		}
	}

    public class Part extends CountDownTimer {
		String type;

		Part(long startTime, long interval, String _type)
		{
			super(startTime, interval);
			statusDisplay.setText(type);
			TRACE(Long.toString(startTime));
		}
		@Override
		public void onFinish() {
			Log.d(TAG, "Clock.onFinish");
		}

		@Override
		public void onTick(long millisTillFinished) {
			long minutes = TimeUnit.MILLISECONDS.toSeconds(millisTillFinished)/60;
			String text = String.format("%d min, %d sec",
					minutes,
				    TimeUnit.MILLISECONDS.toSeconds(millisTillFinished) -
				    minutes * 60
				);

			timeDisplay.setText(text);
		}
	}

	void TRACE(String msg) {
		Log.d(TAG, msg);
	}
}