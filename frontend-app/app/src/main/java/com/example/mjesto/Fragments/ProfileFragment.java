package com.example.mjesto.Fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mjesto.Fragments.Maps.MapsFragment;
import com.example.mjesto.MainActivity;
import com.example.mjesto.R;
import com.example.mjesto.Utils.MjestoUtils;
import com.example.mjesto.Utils.NetworkUtils;
import com.example.mjesto.Utils.UserUtils;

import java.io.IOException;

public class ProfileFragment extends Fragment implements
    View.OnClickListener {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private Button mParkingButton;
    private TextView mUserInfoTV;
    private TextView mParkingStatsTV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mParkingButton = view.findViewById(R.id.parking_b);
        mParkingButton.setOnClickListener(this);
        mUserInfoTV = view.findViewById(R.id.user_name_tv);
        mParkingStatsTV = view.findViewById(R.id.parking_stats_tv);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String parkingStatus = preferences.getString(UserUtils.CUR_USER_PARKED_LOCATION, "");
        String user = preferences.getString(UserUtils.CUR_USER, "");

        Log.d(TAG, "user: " + user);
        if (!user.equals("")) {
            doMjestoGetUserForProfile(user);
        }


        Log.d(TAG, "parking status: " + parkingStatus);
        if (!parkingStatus.equals("")) {
            mParkingButton.setText("You're Parked!");
        } else {
            mParkingButton.setText("Park");
        }

    }

    public void setUserDetails(MjestoUtils.User user) {
        Log.d(TAG, "user: " + MjestoUtils.buildJsonFromUser(user));
        mUserInfoTV.setText("Hello, " + user.name);
        mParkingStatsTV.setText(user.numParked.toString());
    }

    private void doMjestoGetUserForProfile(String user) {
        String url = MjestoUtils.getMjestoUserByIdUrl(user);
        Log.d(TAG, "Querying URL: " + url);
        new MjestoGetUserAndSetProfileTask().execute(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.parking_b:
                MapsFragment fragment = MapsFragment.getInstance();
                MainActivity.updateFragment(fragment, "maps");
        }
    }

    class MjestoGetUserAndSetProfileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            String results = null;
            try {
                results = NetworkUtils.doHttpGet(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "Get response: " + s);

            if (s != null) {
                MjestoUtils.User user = MjestoUtils.getUserFromJson(s);
                if (user != null) {
                    setUserDetails(user);
                }
            }

        }
    }
}
