package com.example.mjesto.Fragments.Maps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;

import com.example.mjesto.R;
import com.example.mjesto.Utils.MjestoUtils;

public class SpotPagerAdapter extends PagerAdapter implements NumberPicker.OnValueChangeListener {
    private static final String TAG = SpotPagerAdapter.class.getSimpleName();
    private static final int NUM_PAGES = 3;

    private Context mContext;
    private MjestoUtils.Location mCurLocation;
    private NumberPicker mLimitHour;
    private NumberPicker mLimitMin;
    private static Button mSpotHoursB;

    SpotPagerAdapter(Context context, MjestoUtils.Location location) {
        mContext = context;
        mCurLocation = location;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout;
        Log.d(TAG, "viewpager at position: " + position);

        if (position == 1) {
            layout = (ViewGroup) inflater.inflate(R.layout.edit_spot_limited_page, container, false);
            mLimitHour = layout.findViewById(R.id.np_limit_hours);
            mLimitMin = layout.findViewById(R.id.np_limit_min);
            mLimitHour.setMaxValue(9);
            mLimitHour.setMinValue(0);
            mLimitHour.setOnValueChangedListener(this);
            mLimitMin.setMaxValue(59);
            mLimitMin.setMinValue(0);
            mLimitMin.setOnValueChangedListener(this);

            mSpotHoursB = layout.findViewById(R.id.b_hours);
            mSpotHoursB.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    MapsFragment.openEditHoursDialog(mContext, v);
                }
            });

            CheckBox meteredCB = layout.findViewById(R.id.cb_metered);
            meteredCB.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MapsFragment.setmSpotMeteredB(isChecked);
                }
            });

            if (mCurLocation != null) {
                if (mCurLocation.limit != null) {
                    MapsFragment.setMLimitedValue(mCurLocation.limit);
                    String[] curLocationLimitArr = mCurLocation.limit.split(":");
                    mLimitHour.setValue(Integer.valueOf(curLocationLimitArr[0]));
                    mLimitMin.setValue(Integer.valueOf(curLocationLimitArr[1]));
                } else {
                    MapsFragment.setMLimitedValue("0:00");
                }
                String spotHours = mCurLocation.restrictionStart + " - " + mCurLocation.restrictionEnd;
                mSpotHoursB.setText(spotHours);
                meteredCB.setChecked(mCurLocation.allDay);
            } else {
                mSpotHoursB.setText(MjestoUtils.DEFAULT_RESTRICTION_TIME);
            }



            container.addView(layout);
        } else {
            layout = (ViewGroup) inflater.inflate(R.layout.edit_spot_default, container, false);
            container.addView(layout);
        }

        return layout;
    }

    public static void setmSpotHoursBText(String start, String end) {
        if (mSpotHoursB != null) {
            String hours = start + " - " + end;
            mSpotHoursB.setText(hours);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
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
                return "no restriction";
            case 1:
                return "limited";
            case 2:
                return "restricted";
        }
        return "";
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        String hourStr = String.valueOf(mLimitHour.getValue());
        String minStr = String.valueOf(mLimitMin.getValue());
        if (minStr.length() == 1) {
            minStr = "0" + minStr;
        }
        Log.d(TAG, "Limit: " + hourStr + ":" + minStr);
        MapsFragment.setMLimitedValue(hourStr + ":" + minStr);
    }
}
