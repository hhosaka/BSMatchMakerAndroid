package com.nag.android.bs_match_maker;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;


public class TimerView extends TextView {
	private final Handler handler=new Handler();
	private Timer timer = null;

	public TimerView(Context context) {
		super(context);
	}
	public TimerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TimerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    public void start(){
        start(System.currentTimeMillis());
    }

	public void start(final long initial){
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				handler.post(new Runnable(){
				@Override
				public void run() {
					long t = (System.currentTimeMillis()-initial)/1000;
					setText(String.format("%1$02d:%2$02d:%3$02d", t/(60*60), (t/60)%60, t%60));
				}});
			}
		},0,50);
			
	}
	public void stop(){
		timer.cancel();
		timer = null;
	}
}
