package com.example.mjesto.Fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mjesto.Fragments.Maps.MapsFragment;
import com.example.mjesto.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ParkedFragment extends Fragment {

    private static final String TAG = ParkedFragment.class.getSimpleName();

    public View mView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_parked, container, false);

//        MapsFragment fragment = MapsFragment.getInstance();
//        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.fl_parked_map, fragment);
//        fragmentTransaction.commit();

        final TextView timeRemainingValue = mView.findViewById(R.id.time_remaining_value_tv);

        timeRemainingValue.setText("Test");

        CountDownTimer countDownTimer = new CountDownTimer(2 * 60 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int hours = (int) (TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
                int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1));
                int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));

                if (hours != 0) {
                    timeRemainingValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                } else if (minutes != 0) {
                    timeRemainingValue.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                } else if (seconds != 0) {
                    timeRemainingValue.setText(String.format(Locale.getDefault(), "%02d", seconds));
                }

                Log.d(TAG, "time remaining: " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timeRemainingValue.setText("0");
            }
        };

        countDownTimer.start();

        return mView;
    }
}
