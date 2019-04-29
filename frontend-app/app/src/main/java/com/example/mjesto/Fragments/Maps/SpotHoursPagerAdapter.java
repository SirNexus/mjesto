package com.example.mjesto.Fragments.Maps;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.example.mjesto.R;
import com.example.mjesto.Utils.MjestoUtils;

public class SpotHoursPagerAdapter extends PagerAdapter {
    private String TAG = SpotHoursPagerAdapter.class.getSimpleName();
    private Integer NUM_COUNT = 2;

    private Context mContext;
    private String mStartTime;
    private String mEndTime;

    SpotHoursPagerAdapter(Context context, String startTime, String endTime) {
        mContext = context;
        mStartTime = startTime;
        mEndTime = endTime;
    }

    @Override
    public int getCount() {
        return NUM_COUNT;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.edit_spot_hours_page, container, false);

        if (position == 0) {
            TimePicker timePicker = layout.findViewById(R.id.hours_tp);
            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    MapsFragment.setmSpotStartTimeTVText(MjestoUtils.buildTime(hourOfDay, minute));
                }
            });
            timePicker.setHour(9);
            timePicker.setMinute(0);

        } else if (position == 1) {
            TimePicker timePicker = layout.findViewById(R.id.hours_tp);
            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    MapsFragment.setmSpotEndTimeTVText(MjestoUtils.buildTime(hourOfDay, minute));
                }
            });
            timePicker.setHour(17);
            timePicker.setMinute(0);

        }

        container.addView(layout);

        return layout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Start Time";
            case 1:
                return "End Time";
        }
        return "";
    }
}
