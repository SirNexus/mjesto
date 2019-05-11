package com.example.mjesto.Utils;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MjestoUtils {
    private final static String TAG = MjestoUtils.class.getSimpleName();

    private final static String MJESTO_BASE_URL = "http://mjesto.io";
    private final static String MJESTO_LOCATIONS_URL = "locations";
    private final static String MJESTO_PARK_URL = "park";
    private final static String MJESTO_USERS_URL = "users";


    public static final String DEFAULT_RESTRICTION_TIME = "9:00AM - 5:00PM";
    public static final String DEFAULT_RESTRICTION_START = "9:00AM";
    public static final String DEFAULT_RESTRICTION_END = "5:00PM";

    public static class Location {
        public String _id;
        public ArrayList<Double> beginCoords;
        public ArrayList<Double> endCoords;
        public String restriction;
        public Boolean metered;
        public Boolean allDay;
        public String restrictionStart;
        public String restrictionEnd;
        public String limit;
        public JsonObject errors;

        public Location() {
            beginCoords = new ArrayList<>();
            endCoords = new ArrayList<>();
        }
    }

    public static class LocationsResult {
        public Location[] locations;
        public String message;
    }

    public static class Park {
        public String user;
        public String location;
        public String endDate;
    }

    public static class User {
        public String name;
        public Integer numParked;
    }

    public static String buildTime(int hour, int min) {
        String AM_PM = (hour <= 12) ? "AM" : "PM";
        String hourStr = (hour <= 12) ? String.valueOf(hour) : String.valueOf(hour - 12);
        String minStr = (String.valueOf(min).length() == 1) ? "0" + min : String.valueOf(min);

        return hourStr + ":" + minStr + AM_PM;
    }

//    take time of format "9:00PM" and return String array of format ["21", "0"]
    public static Integer[] decodeTime(String time) {
        Integer[] returnTimes = new Integer[2];
        String[] times = time.split(":");
        times[1] = times[1].trim();
        times[1].replaceAll("[^0-9]", "");
        Integer hourInt = Integer.valueOf(times[0].trim());
        returnTimes[0] = time.contains("PM") ? hourInt + 12 : hourInt;
        returnTimes[1] = Integer.valueOf(times[1]);
        return returnTimes;
    }

    public static Date addLimitWithDate(String limit) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String[] limitArr = limit.split(":");
        int hour = Integer.parseInt(limitArr[0]);
        int minute = Integer.parseInt(limitArr[1]);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);

        Log.d(TAG, "End time: " + calendar.getTime());
        return calendar.getTime();
    }

    public static LatLng getAverageLatLng(ArrayList<Double> beginCoords, ArrayList<Double> endCoords) {
        Double longitude = (beginCoords.get(0) + endCoords.get(0)) / 2;
        Double latitude = (beginCoords.get(1) + endCoords.get(1)) / 2;
        return new LatLng(latitude, longitude);
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

    public static String getMjestoLocationsUrl(String southwestLong, String southwestLat, String northeastLong, String northeastLat) {
        return Uri.parse(MJESTO_BASE_URL).buildUpon()
                .appendPath(MJESTO_LOCATIONS_URL)
                .appendPath(southwestLong)
                .appendPath(southwestLat)
                .appendPath(northeastLong)
                .appendPath(northeastLat)
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

    public static String getMjestoIncNumParkedUrlByUserID(String user) {
        return Uri.parse(getMjestoUserByIdUrl(user)).buildUpon()
                .appendPath("inc")
                .build()
                .toString();
    }

    public static Location[] parseLocationResults(String json) {
        Gson gson = new Gson();
        LocationsResult result = new LocationsResult();
        if (json.startsWith("{")) {
            result.message = json;
        } else if (json.startsWith("[")) {
            result.locations = gson.fromJson(json, Location[].class);
            Log.d(TAG, "locations:" + result.locations.length);
        }

        if (result.locations != null && result.message == null) {
            return result.locations;
        } else {
            if (result.message != null) {
                Log.d(TAG, result.message);
            }
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

    public static String buildJsonFromUser(User user) {
        Gson gson = new Gson();
        return gson.toJson(user, User.class);
    }

}
