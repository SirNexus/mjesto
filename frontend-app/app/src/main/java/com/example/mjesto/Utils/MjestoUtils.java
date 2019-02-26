package com.example.mjesto.Utils;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MjestoUtils {
    private final static String TAG = MjestoUtils.class.getSimpleName();

    private final static String MJESTO_BASE_URL = "http://mjesto.io";
    private final static String MJESTO_LOCATIONS_URL = "locations";

    public static class LatLngCustom {
        public double lat;
        public double lng;
    }

    public static class Location {
        public String _id;
        public LatLngCustom coordinates;
        public String restriction;
        public Integer limit;
        public JsonObject errors;

        public Location() {
            coordinates = new LatLngCustom();
        }
    }


    public static String getMjestoLocationsUrl() {
        return Uri.parse(MJESTO_BASE_URL).buildUpon()
                .appendPath(MJESTO_LOCATIONS_URL)
                .build()
                .toString();
    }

    public static String getMjestoLocationsUrlWithID(String id) {
        return Uri.parse(getMjestoLocationsUrl()).buildUpon()
                .appendPath(id)
                .build()
                .toString();
    }

    public static Location[] parseLocationResults(String json) {
        Gson gson = new Gson();
        Location[] locations = gson.fromJson(json, Location[].class);

        if (locations != null) {
            return locations;
        } else {
            return null;
        }
    }

    public static String buildJsonFromLocation(Location location){
        Gson gson = new Gson();
        String json = gson.toJson(location);
        Log.d(TAG, "json from location: " + json);
        return json;
    }

    public static Location getLocationFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Location.class);
    }
}
