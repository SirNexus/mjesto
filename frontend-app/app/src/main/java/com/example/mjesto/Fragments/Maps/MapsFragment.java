package com.example.mjesto.Fragments.Maps;

import android.Manifest;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mjesto.R;
import com.example.mjesto.Utils.MjestoUtils;
import com.example.mjesto.Utils.NetworkUtils;
import com.example.mjesto.Utils.ViewModels.ParkedViewModel;
import com.example.mjesto.Utils.UserUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.example.mjesto.Utils.MjestoUtils.getMjestoLocationsUrl;

public class MapsFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    private static final String TAG = MapsFragment.class.getSimpleName();
    private static final String LOCATION_CLICK_FLEXIBILITY = "250";

    private static volatile MapsFragment mapsFragmentInstance;

//    TODO: remove temp variables
    private TextView mClickStatus;
    private LatLng mFirstClick;

    private View mView;
    private ImageButton mSwipeButton;
    private SpotPagerAdapter mAdapter;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private GoogleMap mMap;
    private TileOverlay mTileOverlay;
    private DrawerLayout mDrawyerLayout;
    private LinearLayout mSpotLimitedLL;
    private ArrayList<String> mSpotTypes;
    private static String mLimitedValue;
    private static Boolean mSpotMeteredB;
    private static Boolean mSpotAllDay;
    private static TextView mSpotStartTimeTV;
    private static TextView mSpotEndTimeTV;
    private static String mSpotStartTime;
    private static String mSpotEndTime;
    private Dialog mCurDialog;
    private FrameLayout mMapsFL;
    private FusedLocationProviderClient mFusedLocationClient;
    private ParkedViewModel mParkedViewModel;

    private MjestoUtils.Location mCurLocation;
    private LatLng mCurLatLng;
    private String mParkedLocationID;
    private String mCurUser;
    private Marker mParkedMarker;


    public static synchronized MapsFragment getInstance() {
        if (mapsFragmentInstance == null) {
            mapsFragmentInstance = new MapsFragment();
            Log.d(TAG, "Creating new maps instance");
        }
        return mapsFragmentInstance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_maps, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawyerLayout = mView.findViewById(R.id.drawer);
        mSwipeButton = mView.findViewById(R.id.parked_swipe_ib);
        mMapsFL = mView.findViewById(R.id.fl_maps);
        ParkedFragment parkedFragment = new ParkedFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.parked_fragment, parkedFragment);
        fragmentTransaction.commit();

        mFirstClick = null;
        mLimitedValue = null;
        mSpotMeteredB = null;
        mCurLocation = null;
        mCurLatLng = null;
        mParkedLocationID = null;
        mCurDialog = null;
        mParkedMarker = null;
        mParkedViewModel = null;

        mSpotTypes = new ArrayList<>();
        mSpotTypes.add("unlimited");
        mSpotTypes.add("limited");
        mSpotTypes.add("restricted");

        ParkedGestureListener touchListener = new ParkedGestureListener(mMapsFL);
        mSwipeButton.setOnTouchListener(touchListener);
        mSwipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Swipe Button clicked", Toast.LENGTH_LONG).show();
            }
        });

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String parked = preferences.getString(UserUtils.CUR_USER_PARKED_LOCATION, "");
        mCurUser = preferences.getString(UserUtils.CUR_USER, "");
        Log.d(TAG, "User pref: " + mCurUser);
        if (!parked.equals("")) {
            Log.d(TAG, "User parked");
            mParkedLocationID = parked;
            if (mParkedMarker == null) {
                doMjestoGetLocationById(mParkedLocationID);
            }
        } else {
            Log.d(TAG, "User not parked");
        }
        mParkedViewModel = ViewModelProviders.of(getActivity()).get(ParkedViewModel.class);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device,  the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        TypedValue tv = new TypedValue();
        // move my location button down under toolbar
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
        {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            mMap.setPadding(0, actionBarHeight, 0, 0);
        }

        if (mParkedMarker != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mParkedMarker.getPosition()));
        } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                            }
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Don't have permission", Toast.LENGTH_LONG).show();
        }


        mMap.setMaxZoomPreference(20);

        // Add a marker in Corvallis and move the camera
        LatLng corvallis = new LatLng(44.5646, -123.2620);

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(corvallis, 14));

        TileProvider tileProvider = new SpotsTileProvider(getActivity());
        mTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

    }

    public void parkUser(MjestoUtils.Location location) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putString(UserUtils.CUR_USER_PARKED_LOCATION, location._id).commit();
        if (location.restriction.equals("limited")) {
            Date endDate = MjestoUtils.addLimitWithDate(location.limit);
            Log.d(TAG, "Park date: " + endDate.toString());
            preferences.edit().putString(UserUtils.CUR_PARKED_END_DATE, endDate.toString()).commit();
        }
        LatLng coords = MjestoUtils.getAverageLatLng(location.beginCoords, location.endCoords);

        mParkedMarker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
        mParkedMarker.setTag(location);
        mParkedLocationID = location._id;

        if (mParkedMarker != null) {
            mParkedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        Log.d(TAG, "location restriction: " + location.restriction);
        if (location.restriction.equals("limited")) {
            mParkedViewModel.setEndDate(MjestoUtils.addLimitWithDate(location.limit));
        } else {
            mParkedViewModel.setParked(true);
        }
    }

    public void unparkUser(String locationID) {
        Log.d(TAG, "Unpark User");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putString(UserUtils.CUR_USER_PARKED_LOCATION, "").commit();
        preferences.edit().putString(UserUtils.CUR_PARKED_END_DATE, "").commit();
        if (mParkedMarker != null) {
            mParkedMarker.remove();
        }
        mParkedViewModel.setParked(false);
        mParkedLocationID = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawyerLayout.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void doMjestoGetLocationById(String locationID) {
        String url = MjestoUtils.getMjestoLocationsUrlWithID(locationID);
        Log.d(TAG, "With ID Querying URL: " + url);
        new MjestoGetLocationByIdTask().execute(url);
    }

    private void doMjestoGetLocationNearLatLng(LatLng latLng) {

        String url = getMjestoLocationsUrl(String.valueOf(latLng.longitude), String.valueOf(latLng.latitude), LOCATION_CLICK_FLEXIBILITY);
        new MjestoGetLocationNearLatLngTask().execute(url);
    }

    private void doMjestoPatchLocation(String id, MjestoUtils.Location location) {
        String body = MjestoUtils.buildJsonFromLocation(location);
        String url = MjestoUtils.getMjestoLocationsUrlWithID(id);
        new MjestoPatchLocationTask().execute(url, body);

    }

    private void doMjestoPostLocation(MjestoUtils.Location location) {
        String body = MjestoUtils.buildJsonFromLocation(location);
        String url = getMjestoLocationsUrl();
        new MjestoPostLocationTask().execute(url, body);
    }

    private void doMjestoDeleteLocation(MjestoUtils.Location location) {
        String url = MjestoUtils.getMjestoLocationsUrlWithID(location._id);
        new MjestoDeleteLocationTask().execute(url);
    }

    private void doMjestoGetParkedUser() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String user = preferences.getString(UserUtils.CUR_USER, "");
        Log.d(TAG, "User: " + user);
        String url = MjestoUtils.getMjestoParkedUserUrl(user);
        new MjestoGetParkedUserTask().execute(url);
    }

    private void doMjestoParkUser() {
        if (mCurUser == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mCurUser = preferences.getString(UserUtils.CUR_USER, "");
        }
        if (mParkedLocationID != null && mParkedMarker != null) {
            unparkUser(mParkedLocationID);
        }
        String url = MjestoUtils.getMjestoParkUrl();

        Log.d(TAG, "User: " + mCurUser);

        String endTime = null;

        if (mCurLocation.restriction.equals("limited")) {
            Date endDate = MjestoUtils.addLimitWithDate(mCurLocation.limit);
            endTime = String.valueOf(endDate);
        }

        new MjestoParkUserTask().execute(url, mCurUser, mCurLocation._id, endTime);
    }

    private void doMjestoIncParkedUser() {
        if (mCurUser == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mCurUser = preferences.getString(UserUtils.CUR_USER, "");
        }

        String url = MjestoUtils.getMjestoIncNumParkedUrlByUserID(mCurUser);

        new NetworkUtils.MjestoDoGetTask().execute(url);
    }

    private void doMjestoDeleteParkUser() {
        if (mCurUser == null) {
            Toast.makeText(getActivity(), "User is null", Toast.LENGTH_LONG);
        } else {
            String url = MjestoUtils.getMjestoParkedUserUrl(mCurUser);
            new MjestoDeleteParkedUserTask().execute(url);
        }
    }

    public static void setMLimitedValue(String limitedValue) {
        mLimitedValue = limitedValue;
    }

    public static void setmSpotMeteredB(Boolean meteredB) {
        mSpotMeteredB = meteredB;
    }

    public static void setmSpotStartTimeTVText(String startTime) {
        mSpotStartTimeTV.setText(startTime);
    }

    public static void setmSpotEndTimeTVText(String endTime) {
        mSpotEndTimeTV.setText(endTime);
    }

    public static void setmSpotAllDay(Boolean allDay) {
        mSpotAllDay = allDay;
    }

    public void findLocationNearLatLng(LatLng latLng, MjestoUtils.Location[] locations) {
        for (MjestoUtils.Location location : locations) {
//            if location has a greater longitude or latitude than the points on line, continue
            if (Math.abs(latLng.latitude) * 111000 > Math.max(Math.abs(location.beginCoords.get(1)), Math.abs(location.endCoords.get(1))) * 111000 + 30
                    || Math.abs(latLng.latitude) * 111000 < Math.min(Math.abs(location.beginCoords.get(1)), Math.abs(location.endCoords.get(1))) * 111000 - 30
                    || Math.abs(latLng.longitude) * 111000 > Math.max(Math.abs(location.beginCoords.get(0)), Math.abs(location.endCoords.get(0))) * 111000 + 30
                    || Math.abs(latLng.longitude) * 111000 < Math.min(Math.abs(location.beginCoords.get(0)), Math.abs(location.endCoords.get(0))) * 111000 - 30) {
                continue;
            }

            Double distance;
            distance = Math.abs(
//                  (y2 - y1)x0
                    (location.endCoords.get(1) - location.beginCoords.get(1)) * latLng.longitude
//                  - (x2 - x1)y0
                    - (location.endCoords.get(0) - location.beginCoords.get(0)) * latLng.latitude
//                  + x2y1
                    + location.endCoords.get(0) * location.beginCoords.get(1)
//                  - y2x1
                    - location.endCoords.get(1) * location.beginCoords.get(0)
            ) / Math.sqrt(
//                  (y2 - y1)^2
                    Math.pow(location.endCoords.get(1) - location.beginCoords.get(1), 2)
//                  (x2 - x1)^2
                    + Math.pow(location.endCoords.get(0) - location.beginCoords.get(0), 2)
            );

            Log.d(TAG, "Distance from latlng to line of location: " + distance * 111000);

            if (distance < 10) {
                openSpotDialog(location);
                break;
            }
        }
    }

    public void openNewSpotDialog(final LatLng latLng) {
        mClickStatus = mView.findViewById(R.id.tv_click_status);

        if (mFirstClick == null) {
            mFirstClick = latLng;
            mClickStatus.setText("Second Click");
            return;
        }

        mCurDialog = openDialog(R.layout.edit_spot, getActivity());

        mCurLocation = new MjestoUtils.Location();
        mSpotStartTime = MjestoUtils.DEFAULT_RESTRICTION_START;
        mSpotEndTime = MjestoUtils.DEFAULT_RESTRICTION_END;
        mSpotMeteredB = false;
        mSpotAllDay = false;

        mCurLocation.restrictionStart = mSpotStartTime;
        mCurLocation.restrictionEnd = mSpotEndTime;
        mCurLocation.metered = mSpotMeteredB;
        mCurLocation.allDay = mSpotAllDay;

        mAdapter = new SpotPagerAdapter(getActivity(), mCurLocation);
        mPager = mCurDialog.findViewById(R.id.edit_spot_vp);
        mPager.setAdapter(mAdapter);

        mTabLayout = mCurDialog.findViewById(R.id.edit_spot_tl);
        mTabLayout.setupWithViewPager(mPager);

        Button spotSubmitB = mCurDialog.findViewById(R.id.b_spot_submit);
        spotSubmitB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MjestoUtils.Location newLocation = new MjestoUtils.Location();
                String title = (String) mAdapter.getPageTitle(mPager.getCurrentItem());

                newLocation.beginCoords.add(mFirstClick.longitude);
                newLocation.beginCoords.add(mFirstClick.latitude);

                newLocation.endCoords.add(latLng.longitude);
                newLocation.endCoords.add(latLng.latitude);

                newLocation.restriction = title;
                newLocation.metered = mSpotMeteredB;
                newLocation.restrictionStart = mSpotStartTime;
                newLocation.restrictionEnd = mSpotEndTime;
                newLocation.allDay = mSpotAllDay;

                Log.d(TAG, "Restriction: " + mSpotStartTime + " - " + mSpotEndTime);

                if (title.equals("limited")) {
                    if (mLimitedValue.equals("") || mLimitedValue.equals("0:00")) {
                        Toast.makeText(getActivity(), "Must enter a time limit", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.d(TAG, "Limit: " + mLimitedValue);
                    newLocation.limit = mLimitedValue;
                }


                doMjestoPostLocation(newLocation);
            }
        });

        Button spotDeleteB = mCurDialog.findViewById(R.id.b_spot_delete);
        spotDeleteB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirstClick = null;
                mClickStatus.setText("First Click");
                mCurDialog.dismiss();
            }
        });
    }

    public void openSpotDialog(MjestoUtils.Location location) {
        mCurLocation = location;
        mCurDialog = openDialog(R.layout.view_spot, getActivity());

        mSpotLimitedLL = mCurDialog.findViewById(R.id.ll_spot_limited);
        TextView restriction_tv = mCurDialog.findViewById(R.id.s_spot_type);
        restriction_tv.setText(mCurLocation.restriction);
        TextView limit_tv = mCurDialog.findViewById(R.id.tv_spot_limit);
        LinearLayout hours_ll = mCurDialog.findViewById(R.id.ll_spot_hours);
        TextView hours_tv = mCurDialog.findViewById(R.id.tv_spot_hours);
        String hoursStr = mCurLocation.restrictionStart + " - " + mCurLocation.restrictionEnd;
        Button parkButton = mCurDialog.findViewById(R.id.b_spot_park);
        parkButton.setOnClickListener(this);

        Log.d(TAG, "mParkedLocationID: " + mParkedLocationID);

        if (mCurLocation._id.equals(mParkedLocationID)) {
            parkButton.setText("Leave Parking");
        }

        if (mCurLocation.restriction.equals("limited")) {
            mSpotLimitedLL.setVisibility(View.VISIBLE);
            hours_ll.setVisibility(View.VISIBLE);

            if (mCurLocation.limit != null) {
                limit_tv.setText(String.valueOf(mCurLocation.limit));
                hours_tv.setText(hoursStr);
            } else {
                limit_tv.setText(0);
                hours_tv.setText("NaN");
            }

        }

        TextView editSpot = mCurDialog.findViewById(R.id.tv_edit_spot);
        editSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditSpotDialog();
            }
        });

    }

    public void openEditSpotDialog() {
        if (mCurDialog != null) {
            mCurDialog.dismiss();
        }

        mCurDialog = openDialog(R.layout.edit_spot, getActivity());

        mAdapter = new SpotPagerAdapter(getActivity(), mCurLocation);
        mPager = mCurDialog.findViewById(R.id.edit_spot_vp);
        mPager.setAdapter(mAdapter);

        mTabLayout = mCurDialog.findViewById(R.id.edit_spot_tl);
        mTabLayout.setupWithViewPager(mPager);

        Button spotSubmitB = mCurDialog.findViewById(R.id.b_spot_submit);
        spotSubmitB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MjestoUtils.Location updateLocation = new MjestoUtils.Location();
                String title = (String) mAdapter.getPageTitle(mPager.getCurrentItem());

                // Set to null to prevent being added to json for PATCH
                updateLocation.restriction = title;

                updateLocation.beginCoords = null;
                updateLocation.endCoords = null;

                updateLocation.metered = mSpotMeteredB;
                updateLocation.restrictionStart = mSpotStartTime;
                updateLocation.restrictionEnd = mSpotEndTime;
                updateLocation.allDay = mSpotAllDay;

                if (title.equals("limited")) {
                    if (mLimitedValue.equals("0:00")) {
                        Toast.makeText(getActivity(), "Must enter a time limit", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.d(TAG, "Number of limit: " + mLimitedValue);
                    updateLocation.limit = mLimitedValue;
                }

                doMjestoPatchLocation(mCurLocation._id, updateLocation);
            }
        });

        Button spotDeleteB = mCurDialog.findViewById(R.id.b_spot_delete);
        spotDeleteB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                doMjestoDeleteLocation(mCurLocation);
            }
        });
    }

    public static Dialog openDialog(Integer view, Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(view);
        dialog.setTitle(R.string.spot_view_more);
        dialog.show();

        return dialog;

    }

    public static void openEditHoursDialog(Context context, View v) {
        Button hoursButton = (Button) v;
        String[] hoursArr = ((String) hoursButton.getText()).split("-");
        final Dialog dialog = openDialog(R.layout.edit_spot_hours, context);
        SpotHoursPagerAdapter adapter = new SpotHoursPagerAdapter(context, hoursArr[0], hoursArr[1]);
        ViewPager pager = dialog.findViewById(R.id.edit_hours_vp);
        pager.setAdapter(adapter);

        mSpotStartTimeTV = dialog.findViewById(R.id.tv_start_time);
        mSpotEndTimeTV = dialog.findViewById(R.id.tv_end_time);

        Button hoursSubmit = dialog.findViewById(R.id.b_hours_submit);
        hoursSubmit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotStartTime = (String) mSpotStartTimeTV.getText();
                mSpotEndTime = (String) mSpotEndTimeTV.getText();
                SpotPagerAdapter.setmSpotHoursBText(mSpotStartTime, mSpotEndTime);
                dialog.dismiss();
            }
        });

        Button hoursCancel = dialog.findViewById(R.id.b_hours_cancel);
        hoursCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mSpotStartTimeTV.setTypeface(null, Typeface.BOLD);
                    mSpotEndTimeTV.setTypeface(null, Typeface.NORMAL);
                } else if (position == 1) {
                    mSpotEndTimeTV.setTypeface(null, Typeface.BOLD);
                    mSpotStartTimeTV.setTypeface(null, Typeface.NORMAL);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        TabLayout tabLayout = dialog.findViewById(R.id.edit_hours_tl);
        tabLayout.setupWithViewPager(pager);


    }

    @Override
    public void onMapClick(final LatLng latLng) {
        openNewSpotDialog(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mCurLatLng = latLng;
        doMjestoGetLocationNearLatLng(mCurLatLng);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getItemAtPosition(i).toString() == "limited") {
            mSpotLimitedLL.setVisibility(View.VISIBLE);
        } else {
            mSpotLimitedLL.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public double getDistanceFromBounds(LatLngBounds latLngBounds) {
        return Math.sqrt(
                Math.abs((latLngBounds.northeast.latitude - latLngBounds.southwest.latitude)
                + (latLngBounds.northeast.longitude - latLngBounds.southwest.longitude)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_spot_park:
                mCurDialog.dismiss();
                if (mCurLocation._id.equals(mParkedLocationID)) {
                    doMjestoDeleteParkUser();
                } else {
                    doMjestoParkUser();
                }
                break;
        }
    }

    class MjestoGetLocationByIdTask extends AsyncTask<String, Void, String> {

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
                MjestoUtils.Location location = MjestoUtils.getLocationFromJson(s);
                if (location != null) {
                    parkUser(location);
                }
            }

        }
    }

    class MjestoGetLocationNearLatLngTask extends AsyncTask<String, Void, String> {

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
            Log.d(TAG, "Get response near latlng: " + s);

            if (s != null) {
                MjestoUtils.Location[] locations = MjestoUtils.parseLocationResults(s);
                if (locations != null) {
                    findLocationNearLatLng(mCurLatLng, locations);
                    mCurLatLng = null;
                }
            } else {
                Log.d(TAG, "Locations near latlng returned null");
            }

        }
    }

    class MjestoPatchLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String json = strings[1];
            String results = null;

            try {
                results = NetworkUtils.doHttpPatch(url, json);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Results of PATCH: " + results);

            return results;
        }

        @Override
        protected void onPostExecute(String s) {

            MjestoUtils.Location location = null;

            location = MjestoUtils.getLocationFromJson(s);
            if (location != null) {
                mCurLocation = location;
                mTileOverlay.clearTileCache();
                Toast.makeText(getActivity(), "Spot Updated Successfully", Toast.LENGTH_LONG).show();
                mCurDialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Spot Update Failed", Toast.LENGTH_LONG).show();
            }



        }
    }

    class MjestoPostLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String json = strings[1];
            String results = null;

            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            jsonObject.addProperty("type", "Point");
            json = jsonObject.toString();

            try {
                results = NetworkUtils.doHttpPost(url, json);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Results of POST: " + results);

            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            MjestoUtils.Location location = MjestoUtils.getLocationFromJson(s);
            if (location.errors != null) {
                Log.d(TAG, location.errors.toString());
                Toast.makeText(getActivity(), "Spot Update Failed", Toast.LENGTH_LONG).show();
            }
            else {
                mCurLocation = location;
                mFirstClick = null;
                mClickStatus.setText("First Click");
                mTileOverlay.clearTileCache();
                Toast.makeText(getActivity(), "Spot Created Successfully", Toast.LENGTH_LONG).show();
                mCurDialog.dismiss();
            }
        }
    }

    class MjestoDeleteLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String results = null;

            try {
                results = NetworkUtils.doHttpDelete(url);

            } catch (IOException e) {
                e.printStackTrace();
            }


            return results;
        }

        @Override
        protected void onPostExecute(String s) {


            Log.d(TAG, "Results of Delete: " + s);

            if (s.equals("\"Location Delete Successful\"")) {
                mCurDialog.dismiss();
                mTileOverlay.clearTileCache();
                Toast.makeText(getActivity(), "Location Deleted Successfully", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class MjestoGetParkedUserTask extends AsyncTask<String, Void, String> {

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
                MjestoUtils.Park park = MjestoUtils.getParkFromJson(s);
                Log.d(TAG, "GetParkedUser returned: " + park);
                if (park != null) {
                    doMjestoGetLocationById(park.location);
                }
            }
        }
    }

    public class MjestoParkUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String userID = strings[1];
            String locationID = strings[2];
            String endDate = strings[3];

            MjestoUtils.Park park = new MjestoUtils.Park();
            park.user = userID;
            park.location = locationID;
            park.endDate = endDate;

            String json = MjestoUtils.buildJsonFromPark(park);

            String results = null;
            try {
                results = NetworkUtils.doHttpPost(url, json);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "Park user results: " + s);
//            MjestoUtils.Location location = MjestoUtils.getLocationFromJson(s);
            if (s != null) {
                Log.d(TAG, "CurLocaiton:" + MjestoUtils.buildJsonFromLocation(mCurLocation));
                parkUser(mCurLocation);
                doMjestoIncParkedUser();
            }

        }
    }

    public class MjestoDeleteParkedUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            String results = null;
            try {
                results = NetworkUtils.doHttpDelete(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "Get response: " + s);

            if (s != null) {
                unparkUser(mParkedLocationID);
                if (s.equals("null")) {
                    Log.d(TAG, "No user to delete");
                }
                Log.d(TAG, "Parking deleted successfully");
            }
        }
    }


}
