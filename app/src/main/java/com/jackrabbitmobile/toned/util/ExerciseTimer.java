package com.jackrabbitmobile.toned.util;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.jackrabbitmobile.toned.R;

public class ExerciseTimer extends RelativeLayout {

    private static final long COUNTDOWN_INTERVAL = 1000;
    private static final long SECOND_IN_MILLISECONDS = 1000;
    public static final int SECOND = 60;
    private static final long VIBRATE_DURATION = 1000;

    private final View mView;
    private static LayoutInflater mInflater;
    private final ImageView mCheck;
    private final View mContainer;
    private ProgressBar mProgressBar;
    private RobotoLightTextView mTimerText;
    private CountDownTimer mCountDownTimer;

    private final Context mContext;
    private Vibrator mVibrator;
    private int duration;

    @Override
    public View getRootView() {
        return mView;
    }

    public ExerciseTimer(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        if (!isInEditMode()) mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        if (mInflater == null) mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.exercise_timer, this);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progress);
        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.exercise_timer_progress_background));
        mTimerText = (RobotoLightTextView) mView.findViewById(R.id.exercise_timer_text);
        mTimerText.setTextColor(getResources().getColor(R.color.grey));
        mContainer = findViewById(R.id.timer_container);
        mCheck = (ImageView) findViewById(R.id.exercise_complete_check);
    }

    private void setTimer() {
        mCountDownTimer = new CountDownTimer(duration * SECOND_IN_MILLISECONDS, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / COUNTDOWN_INTERVAL;
                mProgressBar.setProgress(Integer.parseInt(String.valueOf(seconds)));
                mTimerText.setText(String.format("%02d", seconds % SECOND));
            }

            @Override
            public void onFinish() {
                stop();

                if (!isInEditMode()) mVibrator.vibrate(VIBRATE_DURATION);

                mContainer.setVisibility(INVISIBLE);
                mCheck.setVisibility(VISIBLE);
                mCheck.setImageLevel(1);
                mCheck.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reset();
                        start();
                    }
                });
            }
        };
    }

    public void start() {
        mContainer.setVisibility(VISIBLE);
        mCheck.setVisibility(GONE);
        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.exercise_timer_progress_bar));
        mTimerText.setTextColor(getResources().getColor(R.color.toned_pink));
        mCountDownTimer.cancel();
        reset();
        mCountDownTimer.start();
    }

    public void stop() {
        mCountDownTimer.cancel();
        mProgressBar.setProgress(0);
        mTimerText.setText(String.format("%02d", duration));
    }

    public void reset() {
        mContainer.setVisibility((isDurationValid()) ? View.VISIBLE : View.GONE);
        mCheck.setVisibility((isDurationValid()) ? View.GONE : View.VISIBLE);

        mProgressBar.setProgress(duration);
        mProgressBar.setMax(duration);
        mTimerText.setText(String.format("%02d", duration));
        setTimer();
    }

    public void setDuration(int duration) {
        this.duration = duration;
        reset();
    }

    public void toggleChecked() {
        boolean state = !mCheck.isActivated();
        mCheck.setActivated(state);
        mCheck.setImageLevel((state) ? 1 : 0);
    }

    public boolean isDurationValid() {
        return duration > 0;
    }
}
