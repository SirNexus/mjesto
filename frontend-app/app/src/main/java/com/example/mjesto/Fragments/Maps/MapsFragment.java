package com.example.mjesto.Fragments.Maps;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mjesto.R;
import com.example.mjesto.Utils.MjestoUtils;
import com.example.mjesto.Utils.NetworkUtils;
import com.example.mjesto.Utils.UserUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.mjesto.Utils.MjestoUtils.getMjestoLocationsUrl;

public class MapsFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

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
    private Button mPopulateButton;
    private ProgressBar mPopulateProgress;
    private LinearLayout mSpotLimitedLL;
    private ArrayList<String> mSpotTypes;
    private static String mLimitedValue;
//    TODO: remove marker
    private Marker mCurMarker;
    private Dialog mCurDialog;
    private FrameLayout mMapsFL;

    private MjestoUtils.Location mCurLocation;
    private LatLng mCurLatLng;
    private String mParkedLocationID;
    private String mCurUser;
    private Map<String, Marker> mLocationsMap;

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
        mPopulateProgress = mView.findViewById(R.id.pb_load_populate);
        mPopulateButton = mView.findViewById(R.id.b_populate_map);
        mPopulateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doMjestoGetLocations();
            }
        });
        mSwipeButton = mView.findViewById(R.id.parked_swipe_ib);
        mMapsFL = mView.findViewById(R.id.fl_maps);
        ParkedFragment parkedFragment = new ParkedFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.parked_fragment, parkedFragment);
        fragmentTransaction.commit();

        mFirstClick = null;
//        TODO: remove marker
        mCurMarker = null;
        mCurLocation = null;
        mCurLatLng = null;
        mParkedLocationID = null;
        mCurDialog = null;
        mLocationsMap = new HashMap<>();

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
        String parked = preferences.getString(UserUtils.CUR_USER_PARKED, "");
        mCurUser = preferences.getString(UserUtils.CUR_USER, "");
        Log.d(TAG, "User pref: " + mCurUser);
        if (!parked.equals("")) {
            Log.d(TAG, "User parked");
            mParkedLocationID = parked;
            Marker marker = mLocationsMap.get(mParkedLocationID);
            if (marker != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            } else {
                doMjestoGetLocationById(mParkedLocationID);
            }
        } else {
            Log.d(TAG, "User not parked");
        }
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

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getActivity(), "Don't have permission", Toast.LENGTH_LONG).show();
        }

        mMap.setMaxZoomPreference(20);

        // Add a marker in Corvallis and move the camera
        LatLng corvallis = new LatLng(44.5646, -123.2620);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(corvallis, 14));
//        TODO: reenable this
//        mMap.setOnCameraIdleListener(this);

        TileProvider tileProvider = new SpotsTileProvider(getActivity());
        mTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

    }

    public void parkUser(MjestoUtils.Location location) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putString(UserUtils.CUR_USER_PARKED, location._id).commit();
        Marker marker =  mLocationsMap.get(location._id);
        if (marker != null) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }
        mParkedLocationID = location._id;
        Log.d(TAG, "location restriction: " + location.restriction);
        if (location.restriction.equals("limited")) {
            ParkedFragment.setTimer(location.limit);
        } else {
            ParkedFragment.setParked();
        }
//        MainActivity.updateFragment(new ParkedFragment(), "parked");
    }

    public void unparkUser(String locationID) {
        Log.d(TAG, "Unpark User");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putString(UserUtils.CUR_USER_PARKED, "").commit();
        Marker marker = mLocationsMap.get(locationID);
        if (marker != null) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker());
        }
        ParkedFragment.clearTimer();
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

    private void doMjestoGetLocations() {
        String url = getMjestoLocationsUrl();
        Log.d(TAG, "Querying URL: " + url);
        new MjestoGetLocationsTask().execute(url);
    }

    private void doMjestoGetLocationById(String locationID) {
        String url = MjestoUtils.getMjestoLocationsUrlWithID(locationID);
        Log.d(TAG, "With ID Querying URL: " + url);
        new MjestoGetLocationByIdTask().execute(url);
    }

    private void doMjestoGetLocationNearLatLng(LatLng latLng) {

        String url = getMjestoLocationsUrl(String.valueOf(latLng.longitude), String.valueOf(latLng.latitude), LOCATION_CLICK_FLEXIBILITY);
        new MjestoGetLocationNearLatLng().execute(url);
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
        if (mParkedLocationID != null) {
            unparkUser(mParkedLocationID);
        }
        String url = MjestoUtils.getMjestoParkUrl();

        Log.d(TAG, "User: " + mCurUser);

        new MjestoParkUserTask().execute(url, mCurUser, mCurLocation._id);
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

    private void populateMap(MjestoUtils.Location[] locations) {
        for (MjestoUtils.Location location : locations) {
            if (mLocationsMap.get(location._id) == null) {
//                TODO: replace with drawing on canvas
//                LatLng coords = new LatLng(location.coordinates.get(1), location.coordinates.get(0));

//                Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
//                marker.setTag(location);

//                if (mParkedLocationID != null && mParkedLocationID.equals(location._id)) {
//                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
//                }
//
//                mLocationsMap.put(location._id, marker);
            }
        }
    }

    public static void setMLimitedValue(String limitedValue) {
        Log.d(TAG, "setting limited value: " + limitedValue);
        mLimitedValue = limitedValue;
    }

    public void findLocationNearLatLng(LatLng latLng, MjestoUtils.Location[] locations) {
        for (MjestoUtils.Location location : locations) {
//            if location has a greater longitude or latitude than the points on line, continue
            if (Math.abs(latLng.latitude) * 111000 > Math.max(Math.abs(location.beginCoords.get(1)), Math.abs(location.endCoords.get(1))) * 111000 + 5
                    || Math.abs(latLng.latitude) * 111000 < Math.min(Math.abs(location.beginCoords.get(1)), Math.abs(location.endCoords.get(1))) * 111000 - 5
                    || Math.abs(latLng.longitude) * 111000 > Math.max(Math.abs(location.beginCoords.get(0)), Math.abs(location.endCoords.get(0))) * 111000 + 5
                    || Math.abs(latLng.longitude) * 111000 < Math.min(Math.abs(location.beginCoords.get(0)), Math.abs(location.endCoords.get(0))) * 111000 - 5) {
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

    public void openSpotDialog(MjestoUtils.Location location) {
        mCurLocation = location;
        mCurDialog = openDialog(R.layout.view_spot);

        mSpotLimitedLL = mCurDialog.findViewById(R.id.ll_spot_limited);
        TextView restriction_tv = mCurDialog.findViewById(R.id.s_spot_type);
        restriction_tv.setText(mCurLocation.restriction);
        TextView limit_tv = mCurDialog.findViewById(R.id.tv_spot_limit);
        Button parkButton = mCurDialog.findViewById(R.id.b_spot_park);
        parkButton.setOnClickListener(this);

        Log.d(TAG, "mParkedLocationID: " + mParkedLocationID);

        if (mCurLocation._id.equals(mParkedLocationID)) {
            parkButton.setText("Leave Parking");
        }

        if (mCurLocation.restriction.equals("limited")) {
            mSpotLimitedLL.setVisibility(View.VISIBLE);
            if (mCurLocation.limit != null) {
                limit_tv.setText(String.valueOf(mCurLocation.limit));
            } else {
                limit_tv.setText(0);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
//        TODO: remove marker listener
        mCurMarker = marker;
        mCurLocation = (MjestoUtils.Location) marker.getTag();
        mCurDialog = openDialog(R.layout.view_spot);

        mSpotLimitedLL = mCurDialog.findViewById(R.id.ll_spot_limited);
        TextView restriction_tv = mCurDialog.findViewById(R.id.s_spot_type);
        restriction_tv.setText(mCurLocation.restriction);
        TextView limit_tv = mCurDialog.findViewById(R.id.tv_spot_limit);
        Button parkButton = mCurDialog.findViewById(R.id.b_spot_park);
        parkButton.setOnClickListener(this);

        Log.d(TAG, "mParkedLocationID: " + mParkedLocationID);

        if (mCurLocation._id.equals(mParkedLocationID)) {
            parkButton.setText("Leave Parking");
        }

        if (mCurLocation.restriction.equals("limited")) {
            mSpotLimitedLL.setVisibility(View.VISIBLE);
            if (mCurLocation.limit != null) {
                limit_tv.setText(String.valueOf(mCurLocation.limit));
            } else {
                limit_tv.setText(0);
            }

        }

        TextView editSpot = mCurDialog.findViewById(R.id.tv_edit_spot);
        editSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditSpotDialog();
            }
        });

        return true;
    }

    public void openEditSpotDialog() {

        if (mCurDialog != null) {
            mCurDialog.dismiss();
        }

        mCurDialog = openDialog(R.layout.edit_spot);

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


                if (title.equals("limited")) {
                    if (mLimitedValue.equals("") || mLimitedValue.equals("0")) {
                        Toast.makeText(getActivity(), "Must enter a time limit", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Integer limit = Integer.parseInt(mLimitedValue);
                    Log.d(TAG, "Number of limit: " + limit);
                    updateLocation.limit = limit;
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

    public Dialog openDialog(Integer view) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(view);
        dialog.setTitle(R.string.spot_view_more);
        dialog.show();

        return dialog;

    }

    @Override
    public void onMapClick(final LatLng latLng) {

        mClickStatus = mView.findViewById(R.id.tv_click_status);

        if (mFirstClick == null) {
            mFirstClick = latLng;
            mClickStatus.setText("Second Click");
            return;
        }

//        TODO: implement first and second click and then upload line parking spot

        mCurDialog = openDialog(R.layout.edit_spot);

        mCurLocation = null;

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


                if (title.equals("limited")) {
                    String limitString = mLimitedValue;
                    if (limitString.equals("") || limitString.equals("0")) {
                        Toast.makeText(getActivity(), "Must enter a time limit", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Integer limit = Integer.parseInt(limitString);
                    Log.d(TAG, "Number of limit: " + limit);
                    newLocation.limit = limit;
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

    @Override
    public void onCameraIdle() {
        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        Double distance = getDistanceFromBounds(latLngBounds);

        LatLng position = mMap.getCameraPosition().target;
        if (position != null) {
            // 111,111 used as dirty conversion from coordinates to meters
            String url = getMjestoLocationsUrl(
                    Double.toString(position.longitude),
                    Double.toString(position.latitude),
                    Double.toString(distance / 2 * 25000));

            new MjestoGetLocationsTask().execute(url);
        }
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(UserUtils.CUR_USER_PARKED)) {
            String parked = sharedPreferences.getString(key, "");
            if (!parked.equals("")) {

            }
        }
    }




    class MjestoGetLocationsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPopulateButton.setVisibility(View.INVISIBLE);
            mPopulateProgress.setVisibility(View.VISIBLE);
        }

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
                mPopulateButton.setText("Populate Map");
                MjestoUtils.Location[] locations = MjestoUtils.parseLocationResults(s);
                if (locations != null) {
                    populateMap(locations);
                }
            } else {
                mPopulateButton.setText("Error Populating");
            }
            mPopulateButton.setVisibility(View.VISIBLE);
            mPopulateProgress.setVisibility(View.INVISIBLE);

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

    class MjestoGetLocationNearLatLng extends AsyncTask<String, Void, String> {

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
//                TODO: remove marker
//                mCurMarker.setTag(location);
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

            MjestoUtils.Location location = null;

            location = MjestoUtils.getLocationFromJson(s);
            if (location.errors != null) {
                Log.d(TAG, location.errors.toString());
                Toast.makeText(getActivity(), "Spot Update Failed", Toast.LENGTH_LONG).show();
            }
            else {
                mCurLocation = location;
//                TODO: implement canvas drawing to replace this
//                LatLng coords = new LatLng(location.coordinates.get(1), location.coordinates.get(0));
//                Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
//                mLocationsMap.put(mCurLocation._id, marker);
//                marker.setTag(location);
//
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
                mLocationsMap.remove(mCurLocation._id);
//                TODO: remove marker
//                mCurMarker.remove();
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
            } else {
                mPopulateButton.setText("Error Populating");
            }
        }
    }

    public class MjestoParkUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String userID = strings[1];
            String locationID = strings[2];

            MjestoUtils.Park park = new MjestoUtils.Park();
            park.user = userID;
            park.location = locationID;

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
