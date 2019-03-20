package com.example.mjesto.Utils;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class MjestoUtils {
    private final static String TAG = MjestoUtils.class.getSimpleName();

    private final static String MJESTO_BASE_URL = "http://mjesto.io";
    private final static String MJESTO_LOCATIONS_URL = "locations";
    private final static String MJESTO_PARK_URL = "park";
    private final static String MJESTO_USERS_URL = "users";

    public static class Location {
        public String _id;
        public ArrayList<Double> coordinates;
        public String restriction;
        public Integer limit;
        public JsonObject errors;

        public Location() {
            coordinates = new ArrayList<>();
        }
    }

    public static class Park {
        public String user;
        public String location;
    }

    public static class User {
        public String name;
    }


    public static String getMjestoLocationsUrl() {
        return Uri.parse(MJESTO_BASE_URL).buildUpon()
                .appendPath(MJESTO_LOCATIONS_URL)
                .build()
                .toString();
    }

    public static String getMjestoLocationsUrl(String longitude, String latitude, String distance) {
        return Uri.parse(MJESTO_BASE_URL).buildUpon()
                .appendPath(MJESTO_LOCATIONS_URL)
                .appendPath(longitude)
                .appendPath(latitude)
                .appendPath(distance)
                .build()
                .toString();

    }

    public static String getMjestoLocationsUrlWithID(String id) {
        return Uri.parse(getMjestoLocationsUrl()).buildUpon()
                .appendPath(id)
                .build()
                .toString();
    }

    public static String getMjestoParkUrl() {
        return Uri.parse(MJESTO_BASE_URL).buildUpon()
                .appendPath(MJESTO_PARK_URL)
                .build()
                .toString();
    }

    public static String getMjestoParkedUserUrl(String userID) {
        return Uri.parse(getMjestoParkUrl()).buildUpon()
                .appendPath(userID)
                .build()
                .toString();
    }

    public static String getMjestoUsersUrl() {
        return Uri.parse(MJESTO_BASE_URL).buildUpon()
                .appendPath(MJESTO_USERS_URL)
                .build()
                .toString();
    }

    public static String getMjestoUserByIdUrl(String user) {
        return Uri.parse(getMjestoUsersUrl()).buildUpon()
                .appendPath(user)
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
        String json = gson.toJson(location, Location.class);
        Log.d(TAG, "json from location: " + json);
        return json;
    }

    public static Location getLocationFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Location.class);
    }

    public static String buildJsonFromPark(Park park) {
        Gson gson = new Gson();
        String json = gson.toJson(park, Park.class);
        Log.d(TAG, "json from park: " + json);
        return json;
    }

    public static Park getParkFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Park.class);

    }

    public static User getUserFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, User.class);
    }

}
