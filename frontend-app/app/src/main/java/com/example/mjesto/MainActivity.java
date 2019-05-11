package com.example.mjesto;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mjesto.Fragments.ProfileFragment;
import com.example.mjesto.Fragments.WelcomeFragment;
import com.example.mjesto.Fragments.WelcomeParkedFragment;
import com.example.mjesto.Utils.UserUtils;
import com.example.mjesto.Utils.ViewModels.MainViewModel;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        Serializable {

    private final String TAG = MainActivity.class.getSimpleName();
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;

    private DrawerLayout mDrawerLayout;
    private static MainViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_base);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mDrawerLayout = findViewById(R.id.drawer);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getString(UserUtils.CUR_USER_PARKED_LOCATION, "").equals("")) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new WelcomeParkedFragment());
            fragmentTransaction.commit();
        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new WelcomeFragment());
            fragmentTransaction.commit();

        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayShowTitleEnabled(false);

        NavigationView navigationView = findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        mViewModel.getFragment().observe(this, new Observer<Fragment>() {
            @Override
            public void onChanged(@Nullable Fragment fragment) {
                if (fragment == null) {
                    Log.d(TAG, "Fragment is NULL");
                    return;
                }
                else {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.addToBackStack("New Fragment");
                    fragmentTransaction.commit();
                }
            }
        });

        mViewModel.getFragmentNoBackstack().observe(this, new Observer<Fragment>() {
            @Override
            public void onChanged(@Nullable Fragment fragment) {
                if (fragment == null) {
                    return;
                }
                else {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.commit();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    FINE_LOCATION_PERMISSION_REQUEST );
        }


        preferences.edit().putString(UserUtils.CUR_USER, UserUtils.user).commit();
        Log.d(TAG, "User: " + preferences.getString(UserUtils.CUR_USER, "No user found"));
        Log.d(TAG, "Parked ID: " + preferences.getString(UserUtils.CUR_USER_PARKED_LOCATION, "Not parked"));

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mViewModel.setTAG("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_search:
                Toast.makeText(this, "Profile Clicked", Toast.LENGTH_LONG).show();
                mViewModel.setFragment(new ProfileFragment(), "profile");
                return true;
            default:
                return false;
        }
    }

    public static void updateFragment(Fragment fragment, String tag) {
        mViewModel.setFragment(fragment, tag);
    }

    public static void updateFragmentWithoutBackstack(Fragment fragment) {
        mViewModel.setFragmentNoBackstack(fragment);
    }
}
