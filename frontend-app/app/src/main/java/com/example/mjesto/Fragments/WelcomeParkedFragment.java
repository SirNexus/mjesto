package com.example.mjesto.Fragments;

import android.arch.lifecycle.ViewModelProviders;
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
import com.example.mjesto.Fragments.Maps.ParkedFragment;
import com.example.mjesto.MainActivity;
import com.example.mjesto.R;
import com.example.mjesto.Utils.ViewModels.ParkedViewModel;
import com.example.mjesto.Utils.UserUtils;

import java.util.Date;

public class WelcomeParkedFragment extends Fragment implements View.OnClickListener {

    private static String TAG = WelcomeParkedFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_parked, container, false);
        Button parkButton = view.findViewById(R.id.b_park);
        parkButton.setOnClickListener(this);

        ParkedFragment parkedFragment = new ParkedFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_parked_fragment, parkedFragment);
        fragmentTransaction.commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Check if user is actually not parked, change fragment
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.getString(UserUtils.CUR_USER_PARKED_LOCATION, "").equals("")) {
            MainActivity.updateFragmentWithoutBackstack(new WelcomeFragment());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String endDate = preferences.getString(UserUtils.CUR_PARKED_END_DATE, "");
        ParkedViewModel viewModel = ViewModelProviders.of(getActivity()).get(ParkedViewModel.class);
        if (endDate == "") {
            viewModel.setParked(true);
        } else {
            viewModel.setEndDate(new Date(endDate));
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
