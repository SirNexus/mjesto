package com.example.mjesto.Fragments.Maps;

import android.Manifest;
import android.app.Dialog;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MapsFragment.class.getSimpleName();

    private static volatile MapsFragment mapsFragmentInstance;

    private View mView;
    private ImageButton mSwipeButton;
    private SpotPagerAdapter mAdapter;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private GoogleMap mMap;
    private DrawerLayout mDrawyerLayout;
    private Button mPopulateButton;
    private ProgressBar mPopulateProgress;
    private LinearLayout mSpotLimitedLL;
    private ArrayList<String> mSpotTypes;
    private static String mLimitedValue;
    private Marker mCurMarker;
    private Dialog mCurDialog;
    private FrameLayout mMapsFL;

    private MjestoUtils.Location mCurLocation;
    private String mParkedLocationID;
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

        mCurMarker = null;
        mCurLocation = null;
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
        String user = preferences.getString(UserUtils.CUR_USER, "");
        Log.d(TAG, "User pref: " + user);
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

        // Add a marker in Sydney and move the camera
        LatLng corvallis = new LatLng(44.5646, -123.2620);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(corvallis, 14));
        mMap.setOnCameraIdleListener(this);


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getActivity(), "Don't have permission", Toast.LENGTH_LONG).show();
        }
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
//        MainActivity.updateFragment(new ParkedFragment(), "parked");
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
        String url = MjestoUtils.getMjestoLocationsUrl();
        Log.d(TAG, "Querying URL: " + url);
        new MjestoGetLocationsTask().execute(url);
    }

    private void doMjestoGetLocationById(String locationID) {
        String url = MjestoUtils.getMjestoLocationsUrlWithID(locationID);
        Log.d(TAG, "With ID Querying URL: " + url);
        new MjestoGetLocationByIdTask().execute(url);
    }

    private void doMjestoPatchLocation(String id, MjestoUtils.Location location) {
        String body = MjestoUtils.buildJsonFromLocation(location);
        String url = MjestoUtils.getMjestoLocationsUrlWithID(id);
        new MjestoPatchLocationTask().execute(url, body);

    }

    private void doMjestoPostLocation(MjestoUtils.Location location) {
        String body = MjestoUtils.buildJsonFromLocation(location);
        String url = MjestoUtils.getMjestoLocationsUrl();
        new MjestoPostLocationTask().execute(url, body);
    }

    private void doMjestoGetParkedUser() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String user = preferences.getString(UserUtils.CUR_USER, "");
        Log.d(TAG, "User: " + user);
        String url = MjestoUtils.getMjestoParkedUserUrl(user);

        new MjestoGetParkedUserTask().execute(url);
    }

    private void doMjestoParkUser() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String user = preferences.getString(UserUtils.CUR_USER, "");
        Log.d(TAG, "User: " + user);

        new MjestoParkUserTask().execute(user, mCurLocation._id);
    }

    private void populateMap(MjestoUtils.Location[] locations) {
        for (MjestoUtils.Location location : locations) {
            if (mLocationsMap.get(location._id) == null) {
                LatLng coords = new LatLng(location.coordinates.get(1), location.coordinates.get(0));

                Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
                marker.setTag(location);

                if (mParkedLocationID != null && mParkedLocationID.equals(location._id)) {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }

                mLocationsMap.put(location._id, marker);
            }
        }
    }

    public static void setMLimitedValue(String limitedValue) {
        Log.d(TAG, "setting limited value: " + limitedValue);
        mLimitedValue = limitedValue;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mCurMarker = marker;
        mCurLocation = (MjestoUtils.Location) marker.getTag();
        mCurDialog = openDialog(R.layout.view_spot);

        mSpotLimitedLL = mCurDialog.findViewById(R.id.ll_spot_limited);
        TextView restriction_tv = mCurDialog.findViewById(R.id.s_spot_type);
        restriction_tv.setText(mCurLocation.restriction);
        TextView limit_tv = mCurDialog.findViewById(R.id.tv_spot_limit);
        Button parkButton = mCurDialog.findViewById(R.id.b_spot_park);
        parkButton.setOnClickListener(this);


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
                updateLocation.coordinates = null;


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

                newLocation.coordinates.add(latLng.longitude);
                newLocation.coordinates.add(latLng.latitude);

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
        Toast.makeText(getActivity(), "Camera Idle", Toast.LENGTH_SHORT).show();
        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        Double distance = getDistanceFromBounds(latLngBounds);

        LatLng position = mMap.getCameraPosition().target;
        // 111,111 used as dirty conversion from coordinates to meters
        String url = MjestoUtils.getMjestoLocationsUrl(
                Double.toString(position.longitude),
                Double.toString(position.latitude),
                Double.toString(distance / 2 * 25000));

        Log.d(TAG, "Geoquery: " + url);
        new MjestoGetLocationsTask().execute(url);
    }

    public double getDistanceFromBounds(LatLngBounds latLngBounds) {
        return Math.sqrt(
                (latLngBounds.northeast.latitude - latLngBounds.southwest.latitude)
                + (latLngBounds.northeast.longitude - latLngBounds.southwest.longitude));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_spot_park:
                mCurDialog.dismiss();
                doMjestoParkUser();
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
                mCurMarker.setTag(location);
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
                LatLng coords = new LatLng(location.coordinates.get(1), location.coordinates.get(0));
                Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
                marker.setTag(location);
                Toast.makeText(getActivity(), "Spot Created Successfully", Toast.LENGTH_LONG).show();
                mCurDialog.dismiss();
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
            String userID = strings[0];
            String locationID = strings[1];
            String url = MjestoUtils.getMjestoParkUrl();

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
            if (s != null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                preferences.edit().putString(UserUtils.CUR_USER_PARKED, mCurLocation._id).commit();
                parkUser(mCurLocation);
            }

        }
    }


}
