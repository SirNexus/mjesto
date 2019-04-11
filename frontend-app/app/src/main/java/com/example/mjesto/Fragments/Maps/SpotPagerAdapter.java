package com.example.mjesto.Fragments.Maps.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.mjesto.Fragments.Maps.MapsFragment;
import com.example.mjesto.R;
import com.example.mjesto.Utils.MjestoUtils;

public class SpotPagerAdapter extends PagerAdapter implements TextWatcher {
    private static final String TAG = SpotPagerAdapter.class.getSimpleName();
    private static final int NUM_PAGES = 3;

    private Context mContext;
    private MjestoUtils.Location mCurLocation;

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
            layout = (ViewGroup) inflater.inflate(R.layout.edit_spot_limited, container, false);
            EditText limitET = layout.findViewById(R.id.et_spot);
            limitET.addTextChangedListener(this);
            MapsFragment.setMLimitedValue("0");
            container.addView(layout);
        } else {
            layout = (ViewGroup) inflater.inflate(R.layout.edit_spot_default, container, false);
            container.addView(layout);
        }

        return layout;
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        MapsFragment.setMLimitedValue(s.toString());

    }
}
