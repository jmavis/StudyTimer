package com.JaredMavis.StudyTimer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class StudyTimerActivity extends Activity {
	private static final String TAG = "StudyTimerActivity";
	private final int ClockUpdateInterval = 1000; // ms
	private final int study = 0, shortBreak = 1, longBreak = 2;

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
	private PartFactory partFactory;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        statusDisplay = (TextView) findViewById(R.id.SessionTitle);
		timeDisplay = (TextView) findViewById(R.id.TimeLeftInSession);

		timePerStudySession = 25;
		timePerBreakSession = 5;
		timePerLongBreakSession = 35;

		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(startButtonListener());

		pauseButton = (Button) findViewById(R.id.pauseButton);
		pauseButton.setOnClickListener(pauseButtonListener());

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		partFactory = new PartFactory();

		session = new Session();
    }

    private View.OnClickListener startButtonListener(){
    	View.OnClickListener listener = new View.OnClickListener() {
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
		StudySession(ProgressBar _bar){
			super(MinToMilli(timePerStudySession), ClockUpdateInterval, "Study", _bar);
			long millis = MinToMilli(timePerStudySession);

			bar = _bar;

			bar.setMax((int) millis/1000);
		}

		@Override
		public void onTick(long millisTillFinished) {
			super.onTick(millisTillFinished);
			bar.setProgress(1+bar.getProgress());
		}
	}

	public class ShortBreak extends Part {
		ShortBreak(ProgressBar _bar){
			super(MinToMilli(timePerBreakSession), ClockUpdateInterval, "Break", _bar);
			long millis = MinToMilli(timePerStudySession);

			bar = _bar;

			bar.setMax((int) millis/1000);
		}

		@Override
		public void onTick(long millisTillFinished) {
			super.onTick(millisTillFinished);
			bar.setProgress(1+bar.getProgress());
		}
	}

	public class LongBreak extends Part {
		LongBreak(ProgressBar _bar){
			super(MinToMilli(timePerLongBreakSession), ClockUpdateInterval, "Break", _bar);
			long millis = MinToMilli(timePerStudySession);


			bar.setMax((int) millis/60);
		}

		@Override
		public void onTick(long millisTillFinished) {
			super.onTick(millisTillFinished);
			bar.setProgress(1+bar.getProgress());
		}
	}

    public class Part extends CountDownTimer {
		String type;
		int seconds;
		Boolean isPaused;
		ProgressBar bar;

		Part(long startTime, long interval, String _type, ProgressBar _bar)
		{
			super(startTime, interval);
			seconds = (int) (startTime/1000);
			type = _type;
			isPaused = false;
			bar = _bar;
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

    		sessionParts.add(partFactory.makePart(study));
    		sessionParts.add(partFactory.makePart(shortBreak));
    		sessionParts.add(partFactory.makePart(study));
    		sessionParts.add(partFactory.makePart(longBreak));
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
			itor = sessionParts.iterator();
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

    public class PartFactory{
    	PartFactory(){
    		
    	}
    	
    	Part makePart(int type) {
    		switch (type) {
				case study:
					return (new StudySession(progressBar));
				case shortBreak:
					return (new ShortBreak(progressBar));
				case longBreak:
					return (new LongBreak(progressBar));
				default:
					Log.e(TAG, Integer.toString(type) + ": no type matching has been set.");
					return (null);
    		}
    	}
    }
    
	void TRACE(String msg) {
		Log.d(TAG, msg);
	}
}