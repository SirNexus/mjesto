package com.example.mjesto;

import android.net.Uri;

import com.google.gson.Gson;

public class MjestoUtils {
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
    }

    public static String getMjestoLocationsUrl() {
        return Uri.parse(MJESTO_BASE_URL).buildUpon()
                .appendPath(MJESTO_LOCATIONS_URL)
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
}
