package com.example.mjesto.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mjesto.Fragments.Maps.MapsFragment;
import com.example.mjesto.MainActivity;
import com.example.mjesto.R;
import com.example.mjesto.Utils.UserUtils;

import java.util.Map;

public class WelcomeFragment extends Fragment implements View.OnClickListener {

    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_welcome, container, false);
        Button parkButton = mView.findViewById(R.id.b_park);
        parkButton.setOnClickListener(this);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Check if user  is actually parked, change fragment
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!preferences.getString(UserUtils.CUR_USER_PARKED_LOCATION, "").equals("")) {
            MainActivity.updateFragmentWithoutBackstack(new WelcomeParkedFragment());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_park:
                openMap();
                return;
        }
    }

    public void openMap() {
        MapsFragment mapsFragment = MapsFragment.getInstance();
        MainActivity.updateFragment(mapsFragment, "maps");
    }
}
