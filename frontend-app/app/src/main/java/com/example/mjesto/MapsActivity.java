package com.example.mjesto;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MapsActivity.class.getSimpleName();


    private GoogleMap mMap;
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private DrawerLayout mDrawyerLayout;
    private Button mPopulateButton;
    private ProgressBar mPopulateProgress;
    private Spinner mSpotTypeSpinner;
    private LinearLayout mSpotLimitedLL;
    private ArrayList<String> mSpotTypes;
    private EditText mSpotLimitedET;
    private MjestoUtils.Location mCurLocation;
    private Marker mCurMarker;
    private Dialog mCurDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawyerLayout = findViewById(R.id.drawer);
        mPopulateProgress = findViewById(R.id.pb_load_populate);
        mPopulateButton = findViewById(R.id.b_populate_map);
        mPopulateButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               doMjestoGetLocations();
           }
       });
        mCurMarker = null;
        mCurDialog = null;

        mSpotTypes = new ArrayList<>();
        mSpotTypes.add("unlimited");
        mSpotTypes.add("limited");
        mSpotTypes.add("restricted");

//        DrawerLayout drawerLayout = findViewById(R.id.maps_drawer);

        Toolbar toolbar = findViewById(R.id.maps_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayShowTitleEnabled(false);

        NavigationView navigationView = findViewById(R.id.maps_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this, "Don't have permission", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    FINE_LOCATION_PERMISSION_REQUEST );
        }
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case FINE_LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
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
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void doMjestoGetLocations() {
        String url = MjestoUtils.getMjestoLocationsUrl();
        Log.d(TAG, "Querying URL: " + url);
        new MjestoGetLocationsTask().execute(url);
    }

    private void doMjestoPatchLocation(String id, MjestoUtils.Location location) {
        String body = MjestoUtils.buildJsonFromLocation(location);
        String url = MjestoUtils.getMjestoLocationsUrlWithID(id);
        Log.d(TAG, "path url: " + url);
        new MjestoPatchLocationTask().execute(url, body);

    }

    private void doMjestoPostLocation(MjestoUtils.Location location) {
        String body = MjestoUtils.buildJsonFromLocation(location);
        String url = MjestoUtils.getMjestoLocationsUrl();
        new MjestoPostLocationTask().execute(url, body);
    }

    private void populateMap(MjestoUtils.Location[] locations) {
        for (MjestoUtils.Location location : locations) {
            LatLng coords = new LatLng(location.coordinates.lat, location.coordinates.lng);

            Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
            marker.setTag(location);

        }
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

        mSpotTypeSpinner = mCurDialog.findViewById(R.id.s_spot_type);
        mSpotLimitedLL = mCurDialog.findViewById(R.id.ll_spot_limited);
        mSpotLimitedET = mCurDialog.findViewById(R.id.et_spot);
        Button spotSubmitB = mCurDialog.findViewById(R.id.b_spot_submit);
        spotSubmitB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MjestoUtils.Location updateLocation = new MjestoUtils.Location();

                updateLocation.restriction = mSpotTypeSpinner.getSelectedItem().toString();


                if (updateLocation.restriction.equals("limited")) {
                    String limitString = mSpotLimitedET.getText().toString();
                    if (limitString.equals("") || limitString.equals("0")) {
                        Toast.makeText(MapsActivity.this, "Must enter a time limit", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Integer limit = Integer.parseInt(limitString);
                    Log.d(TAG, "Number of limit: " + limit);
                    updateLocation.limit = limit;
                }


                doMjestoPatchLocation(mCurLocation._id, updateLocation);
            }
        });

        setSpotDialogOptions();
    }

    public Dialog openDialog(Integer view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        dialog.setTitle(R.string.spot_view_more);
        dialog.show();

        return dialog;

    }

    public void setSpotDialogOptions() {
        mSpotTypeSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, mSpotTypes));
        mSpotTypeSpinner.setOnItemSelectedListener(this);

        if (mCurLocation != null) {
            if (mCurLocation.restriction != null &&
                    mSpotTypes != null &&
                    mSpotTypes.contains(mCurLocation.restriction)) {
                mSpotTypeSpinner.setSelection(mSpotTypes.indexOf(mCurLocation.restriction));
            }

            if (mCurLocation.restriction.equals("limited") && mCurLocation.limit != null) {
                mSpotLimitedLL.setVisibility(View.VISIBLE);
                mSpotLimitedET.setText(String.valueOf(mCurLocation.limit));
            }
        }
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        mCurDialog = openDialog(R.layout.edit_spot);

        mCurLocation = null;
        mSpotTypeSpinner = mCurDialog.findViewById(R.id.s_spot_type);
        mSpotLimitedLL = mCurDialog.findViewById(R.id.ll_spot_limited);
        mSpotLimitedET = mCurDialog.findViewById(R.id.et_spot);
        Button spotSubmitB = mCurDialog.findViewById(R.id.b_spot_submit);
        spotSubmitB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MjestoUtils.Location newLocation = new MjestoUtils.Location();

                newLocation.coordinates.lng = latLng.longitude;
                newLocation.coordinates.lat = latLng.latitude;

                newLocation.restriction = mSpotTypeSpinner.getSelectedItem().toString();


                if (newLocation.restriction.equals("limited")) {
                    String limitString = mSpotLimitedET.getText().toString();
                    if (limitString.equals("") || limitString.equals("0")) {
                        Toast.makeText(MapsActivity.this, "Must enter a time limit", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Integer limit = Integer.parseInt(limitString);
                    Log.d(TAG, "Number of limit: " + limit);
                    newLocation.limit = limit;
                }


                doMjestoPostLocation(newLocation);
            }
        });

        setSpotDialogOptions();


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(this, "Item selected: " + adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_LONG).show();


        if (adapterView.getItemAtPosition(i).toString() == "limited") {
            mSpotLimitedLL.setVisibility(View.VISIBLE);
        } else {
            mSpotLimitedLL.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return NavigationViewUtils.handleOnNavigationItemSelected(menuItem, this);
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

    class MjestoPatchLocationTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

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
                Toast.makeText(MapsActivity.this, "Spot Updated Successfully", Toast.LENGTH_LONG).show();
                mCurDialog.dismiss();
            } else {
                Toast.makeText(MapsActivity.this, "Spot Update Failed", Toast.LENGTH_LONG).show();
            }



        }
    }

    class MjestoPostLocationTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

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
                Toast.makeText(MapsActivity.this, "Spot Update Failed", Toast.LENGTH_LONG).show();
            }
            else {
                mCurLocation = location;
                LatLng coords = new LatLng(location.coordinates.lat, location.coordinates.lng);
                Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
                marker.setTag(location);
                Toast.makeText(MapsActivity.this, "Spot Created Successfully", Toast.LENGTH_LONG).show();
                mCurDialog.dismiss();
            }
        }
    }




}
