package com.example.mjesto.Fragments.Maps;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
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

import com.example.mjesto.MainViewModel;
import com.example.mjesto.R;
import com.example.mjesto.Utils.ParkedViewModel;

import java.time.Period;
import java.util.Date;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ParkedViewModel viewModel = ViewModelProviders.of(getActivity()).get(ParkedViewModel.class);
        viewModel.getParked().observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean bool) {
                Log.d(TAG, "Parked? " + bool);
                if (bool != null) {
                    if (!bool) {
                        clearTimer();
                        clearParked();
                    } else {
                        setParked();
                    }

                }
            }
        });
        viewModel.getTimeRemaining().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String timeRemaining) {
                Log.d(TAG, "Time remaining: " + timeRemaining);
                if (timeRemaining == null) {
                    clearTimer();
                } else {
                    setTimer(timeRemaining);
                }
            }
        });
    }

    public static void setParked() {
        mTimeRemainingLabelTV.setVisibility(View.INVISIBLE);
        mTimeRemainingTV.setVisibility(View.INVISIBLE);
        mParkingStatusTV.setText(PARKED_STATE);
    }

    public static void clearParked() {
        mParkingStatusTV.setText(NOT_PARKED_STATE);
    }

    public static void setTimer(String time) {
        mTimeRemainingLabelTV.setVisibility(View.VISIBLE);
        mTimeRemainingTV.setVisibility(View.VISIBLE);
        mTimeRemainingTV.setText(time);
        mParkingStatusTV.setText(PARKED_STATE);
    }

    public static void clearTimer() {
        Log.d(TAG, "clearTimer");
        mTimeRemainingLabelTV.setVisibility(View.INVISIBLE);
        mTimeRemainingTV.setVisibility(View.INVISIBLE);
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }
    }
}
