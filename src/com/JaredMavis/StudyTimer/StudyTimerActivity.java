package com.JaredMavis.StudyTimer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	private final int ClockUpdateInterval = 1000; // ms

	private TextView statusDisplay;
	private TextView timeDisplay;
	private int timePerStudySession; // the amount of time to spend on every
	// session in minutes
	private int timePerBreakSession;// the amount of time to spend on every
	// break in minutes
	private int timePerLongBreakSession; // in minutes
	private Button startButton;

	private Part timer;

	private Session session;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        statusDisplay = (TextView) findViewById(R.id.SessionTitle);
		timeDisplay = (TextView) findViewById(R.id.TimeLeftInSession);

		timePerStudySession = 1;
		timePerBreakSession = 1;
		timePerLongBreakSession = 30;

		session = new Session();

		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(startButtonListener());
    }

    private View.OnClickListener startButtonListener(){
    	View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (session.isGoing) {

				} else {
					session.start();
					startButton.setText("Stop");
				}
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

	public class ShortBreak extends Part {
		ShortBreak(){
			super(MinToMilli(timePerBreakSession), ClockUpdateInterval, "Break");
		}
	}

	public class LongBreak extends Part {
		LongBreak(){
			super(MinToMilli(timePerLongBreakSession), ClockUpdateInterval, "Break");
		}
	}

    public class Part extends CountDownTimer {
		String type;

		Part(long startTime, long interval, String _type)
		{
			super(startTime, interval);
			type = _type;
		}

		public void startPart(){
			statusDisplay.setText(type);
			start();
		}

		@Override
		public void onFinish() {
			session.next();
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

    public class Session {
    	LinkedList<Part> sessionParts;
    	Iterator<Part> current;
    	boolean isGoing;

    	Session(){
    		isGoing = false;
    		sessionParts = new LinkedList<Part>();
    		sessionParts.add(new StudySession());
    		sessionParts.add(new ShortBreak());
    		current = sessionParts.iterator();
    	}

    	public void start(){
    		isGoing = true;
    		next();
    	}

    	public void next(){
    		if (current.hasNext()) {
    			current.next().startPart();
    		} else {
    			isGoing = false;
    			timeDisplay.setVisibility(View.INVISIBLE);
    			startButton.setText("Start");
    		}
    	}
    }

	void TRACE(String msg) {
		Log.d(TAG, msg);
	}
}