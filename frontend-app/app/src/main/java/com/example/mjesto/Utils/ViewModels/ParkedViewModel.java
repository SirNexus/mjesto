package com.example.mjesto.Utils.ViewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ParkedViewModel extends ViewModel {

    private final String TAG = ParkedViewModel.class.getSimpleName();

    MutableLiveData<Boolean> vm_parked;
    MutableLiveData<String> vm_time_remaining;

    Date mEndDate;
    CountDownTimer mCountdownTimer;

    public MutableLiveData<Boolean> getParked() {
        if (vm_parked == null) {
            vm_parked = new MutableLiveData<>();
            vm_parked.setValue(false);
        }
        return vm_parked;
    }

    public MutableLiveData<String> getTimeRemaining() {
        if (vm_time_remaining == null) {
            vm_time_remaining = new MutableLiveData<>();
            vm_time_remaining.setValue(null);
        }
        return vm_time_remaining;
    }

    public void setParked(Boolean bool) {
        if (vm_parked == null) {
            vm_parked = new MutableLiveData<>();
        }
        vm_parked.setValue(bool);
    }

    public void setEndDate(Date endDate) {
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }
        if (vm_time_remaining == null) {
            vm_time_remaining = new MutableLiveData<>();
        }
        if (endDate == null) {
            vm_time_remaining.setValue(null);
        } else {
            mEndDate = endDate;

            long millis = endDate.getTime() - (new Date()).getTime();
            int hours = (int) millis / (1000 * 60 * 60);
            int minutes = ((int) millis / (1000 * 60)) - hours * 60 + 1;

            Log.d(TAG, "Time to set: " + hours + ":" + minutes);

            setTimer(hours, minutes);
        }

    }

    public void setTimer(int hours, int minutes) {
        if (mCountdownTimer == null) {
            mCountdownTimer = new CountDownTimer(hours * 60 * 60 * 1000 + minutes * 60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int hours = (int) (TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
                    int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1));
                    int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));

                    if (hours != 0) {
                        vm_time_remaining.setValue(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                    } else if (minutes != 0) {
                        vm_time_remaining.setValue(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                    } else if (seconds != 0) {
                        vm_time_remaining.setValue(String.format(Locale.getDefault(), "%02d", seconds));
                    }
                }

                @Override
                public void onFinish() {
                    vm_time_remaining.setValue("0");
                }
            };

            mCountdownTimer.start();
        }
    }
}
