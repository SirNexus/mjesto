package com.example.mjesto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

public class MapsActivity extends FragmentActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();


    private GoogleMap mMap;
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private Button mPopulateButton;
    private ProgressBar mPopulateProgress;
    private FrameLayout mSpot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSpot = findViewById(R.id.fl_spot);
        mPopulateProgress = findViewById(R.id.pb_load_populate);
        mPopulateButton = findViewById(R.id.b_populate_map);
        mPopulateButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               doMjestoGetLocations();
           }
       });
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
        Marker marker = mMap.addMarker(new MarkerOptions().position(corvallis).title("Marker in corvallis"));
        marker.setTag(null);

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

    private void populateMap(MjestoUtils.Location[] locations) {
        for (MjestoUtils.Location location : locations) {
            LatLng coords = new LatLng(location.coordinates.lat, location.coordinates.lng);

            Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title("Parking Spot"));
            marker.setTag(location);

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MjestoUtils.Location location = (MjestoUtils.Location) marker.getTag();

        if (location == null) {
            Toast.makeText(this, "Null location", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Type: " + location.restriction, Toast.LENGTH_LONG).show();
        }

        mSpot.setVisibility(View.VISIBLE);
        mSpot.setClickable(false);

        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mSpot.setVisibility(View.INVISIBLE);
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

//            Toast.makeText(getApplication().getBaseContext(), "id: " + locations[0]._id, Toast.LENGTH_LONG).show();

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

}
