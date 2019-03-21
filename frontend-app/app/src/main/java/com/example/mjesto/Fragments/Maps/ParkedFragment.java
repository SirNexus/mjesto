package com.example.mjesto.Fragments.Maps;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mjesto.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ParkedFragment extends Fragment {

    private static final String TAG = ParkedFragment.class.getSimpleName();
    private static final String NOT_PARKED_STATE = "Not Parked";
    private static final String PARKED_STATE = "You're Parked!";

    public View mView;

    private static TextView mParkingStatusTV;
    private static TextView mTimeRemainingTV;
    private static TextView mTimeRemainingLabelTV;

    private static CountDownTimer mCountdownTimer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_parked, container, false);

        mParkingStatusTV = mView.findViewById(R.id.parking_status_tv);
        mTimeRemainingTV = mView.findViewById(R.id.time_remaining_value_tv);
        mTimeRemainingLabelTV = mView.findViewById(R.id.time_label_tv);

        mCountdownTimer = null;

        clearTimer();

        return mView;
    }

    public static void setParked() {
        mTimeRemainingLabelTV.setVisibility(View.INVISIBLE);
        mTimeRemainingTV.setVisibility(View.INVISIBLE);
        mParkingStatusTV.setText(PARKED_STATE);
    }

    public static void setTimer(int hours) {
        Log.d(TAG, "setTimer: " + hours);

        mTimeRemainingLabelTV.setVisibility(View.VISIBLE);
        mTimeRemainingTV.setVisibility(View.VISIBLE);
        mParkingStatusTV.setText(PARKED_STATE);

        mCountdownTimer = new CountDownTimer(hours * 60 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int hours = (int) (TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
                int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1));
                int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));

                if (hours != 0) {
                    mTimeRemainingTV.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                } else if (minutes != 0) {
                    mTimeRemainingTV.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                } else if (seconds != 0) {
                    mTimeRemainingTV.setText(String.format(Locale.getDefault(), "%02d", seconds));
                }

                Log.d(TAG, "time remaining: " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                mTimeRemainingTV.setText("0");
            }
        };

        mCountdownTimer.start();

    }

    public static void clearTimer() {
        Log.d(TAG, "clearTimer");
        mTimeRemainingLabelTV.setVisibility(View.INVISIBLE);
        mTimeRemainingTV.setVisibility(View.INVISIBLE);
        mParkingStatusTV.setText(NOT_PARKED_STATE);
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }
    }
}
