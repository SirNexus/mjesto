package com.example.mjesto.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mjesto.Fragments.Maps.MapsFragment;
import com.example.mjesto.MainActivity;
import com.example.mjesto.R;

import java.util.Map;

public class WelcomeFragment extends Fragment implements View.OnClickListener {

    private View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_welcome, container, false);
        Button parkButton = mView.findViewById(R.id.b_park);
        parkButton.setOnClickListener(this);
        return mView;
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
