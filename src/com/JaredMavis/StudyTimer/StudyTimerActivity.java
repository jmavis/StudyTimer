package com.JaredMavis.StudyTimer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
	private Session session;
	private ProgressBar progressBar;

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

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(2*60);
		TRACE("ProgressBar Max = " + Integer.toString(progressBar.getMax()));
    }

    private View.OnClickListener startButtonListener(){
    	View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (session.isGoing) {
					session.stop();
					startButton.setText("Start");
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
		int seconds;

		Part(long startTime, long interval, String _type)
		{
			super(startTime, interval);
			seconds = (int) (startTime/1000);
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
			progressBar.setProgress(1+progressBar.getProgress());

			TRACE("ProgressBar Progress = "  + Integer.toString(progressBar.getProgress()));
		}
	}

    public class Session {
    	LinkedList<Part> sessionParts;
    	Iterator<Part> itor;
    	Part current;
    	boolean isGoing;

    	Session(){
    		isGoing = false;
    		sessionParts = new LinkedList<Part>();
    		sessionParts.add(new StudySession());
    		sessionParts.add(new ShortBreak());
    		itor = sessionParts.iterator();
    	}

    	public void start(){
    		isGoing = true;
    		next();
    		timeDisplay.setVisibility(View.VISIBLE);
    		progressBar.setVisibility(View.VISIBLE);
    	}

    	public void stop(){
    		current.cancel();
    		isGoing = false;
			timeDisplay.setVisibility(View.INVISIBLE);
			startButton.setText("Start");
			statusDisplay.setText("Welcome to Study Timer");
			progressBar.setVisibility(View.INVISIBLE);
    	}

    	public void next(){
    		if (itor.hasNext()) {
    			current = itor.next();
    			current.startPart();
    		} else {
    			stop();
    		}
    	}

    	public int calcTotalSeconds(){
    		Iterator<Part> iterator = sessionParts.iterator();
    		int total = 0;

    		while (iterator.hasNext()){
    			total += iterator.next().seconds;
    		}

    		return (total);
    	}
    }

	void TRACE(String msg) {
		Log.d(TAG, msg);
	}
}