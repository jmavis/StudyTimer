package com.JaredMavis.StudyTimer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	private Button pauseButton;
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

		pauseButton = (Button) findViewById(R.id.pauseButton);
		pauseButton.setOnClickListener(pauseButtonListener());

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(session.calcTotalSeconds());
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

    private View.OnClickListener pauseButtonListener(){
    	View.OnClickListener listener = new View.OnClickListener() {
    		@Override
    		public void onClick(View v){

    		}
    	};

    	return listener;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return (true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()){
    		case R.id.edit:
    			break;
    		default:

    	}
    	return super.onOptionsItemSelected(item);
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
		Boolean isPaused;

		Part(long startTime, long interval, String _type)
		{
			super(startTime, interval);
			seconds = (int) (startTime/1000);
			type = _type;
			isPaused = false;
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
			String text;
			if (minutes != 0) {
				text = String.format("%d min, %d sec",
						minutes,
					    TimeUnit.MILLISECONDS.toSeconds(millisTillFinished) -
					    minutes * 60
					);
			} else{
				text = String.format("%d sec",
					    TimeUnit.MILLISECONDS.toSeconds(millisTillFinished) -
					    minutes * 60
					);
			}
			timeDisplay.setText(text);
			progressBar.setProgress(1+progressBar.getProgress());
			TRACE("Tick");
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

    	public void pause(){
    		current.isPaused = true;
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