package com.example.mjesto.Fragments.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.example.mjesto.R;
import com.example.mjesto.Utils.CanvasTileProvider;
import com.example.mjesto.Utils.MjestoUtils;
import com.example.mjesto.Utils.NetworkUtils;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpotsTileProvider extends CanvasTileProvider {
    private static String TAG = SpotsTileProvider.class.getSimpleName();

    private class LocationQuery {
        private String url;
        private Canvas canvas;
        private TileProjection projection;
        private int zoom;
    }

    private Context mContext;

    SpotsTileProvider(Context context) {
        mContext = context;

    }

    @Override
    public void onDraw(Canvas canvas, TileProjection projection, int zoom) {

        LatLngBounds bounds = projection.getTileBounds();
        Log.d(TAG, "Bounds: /" + bounds.southwest.longitude +
                "/" + bounds.southwest.latitude +
                "/" + bounds.northeast.longitude +
                "/" + bounds.northeast.latitude);
//        Rect rect = new Rect();
        LatLng corvallis = new LatLng(44.5646, -123.2620);

        TileProjection.DoublePoint doublePoint = projection.latLngToPoint(corvallis);


//        paint.setAlpha(50);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(getStrokeWidth(zoom));
        paint.setColor(ContextCompat.getColor(mContext, R.color.red));
        paint.setAntiAlias(true);
        canvas.drawLine((float) doublePoint.getX(), (float) doublePoint.getY(), (float) doublePoint.getX(), (float) doublePoint.getY() + 50, paint);


        String url = MjestoUtils.getMjestoLocationsUrl(
                Double.toString(bounds.southwest.longitude),
                Double.toString(bounds.southwest.latitude),
                Double.toString(bounds.northeast.longitude),
                Double.toString(bounds.northeast.latitude));

//        LocationQuery locationQuery = new LocationQuery();
//        locationQuery.url = url;
//        locationQuery.canvas = canvas;
//        locationQuery.projection = projection;
//        locationQuery.zoom = zoom;

//        TODO: move http query to own function if works without asynctask
//        String url = locationQuery.url;
//        mCanvas = locationQuery.canvas;
//        Log.d(TAG, "Canvas: " + mCanvas);
//        mProjection = locationQuery.projection;
//        mZoom = locationQuery.zoom;

        String results = null;
        try {
            results = NetworkUtils.doHttpGet(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Get response: " + results);

        if (results != null) {
            MjestoUtils.Location[] locations = MjestoUtils.parseLocationResults(results);
            if (locations != null) {
                drawLocations(locations, canvas, projection, zoom);
            }
        } else {
            Log.d(TAG, "Error Populating");
        }

//        new MjestoGetLocationsTask().execute(locationQuery);

    }

    private float getStrokeWidth(int zoom) {
        Double Scale = Math.pow(2, zoom - 14);
        return Scale.floatValue();
    }

    private void drawLocations(MjestoUtils.Location[] locations, Canvas canvas, TileProjection projection, int zoom) {
        for (MjestoUtils.Location location : locations) {
            Log.d(TAG, "Drawing Location: " + location._id);
            Log.d(TAG, "Canvas: " + canvas);
            LatLng beginLatLng = new LatLng(location.beginCoords.get(1), location.beginCoords.get(0));
            LatLng endLatLng = new LatLng(location.endCoords.get(1), location.endCoords.get(0));

            TileProjection.DoublePoint beginPoint = projection.latLngToPoint(beginLatLng);
            TileProjection.DoublePoint endPoint = projection.latLngToPoint(endLatLng);
            Log.d(TAG, "Point: " + beginPoint.getX() + " " + beginPoint.getY() + "end: " + endPoint.getX() + " " + endPoint.getY());

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(getStrokeWidth(zoom));
            paint.setStrokeWidth(5);
            paint.setColor(ContextCompat.getColor(mContext, R.color.red));
            paint.setAntiAlias(true);
            canvas.drawLine((float) beginPoint.getX(), (float) beginPoint.getY(), (float) endPoint.getX(), (float) endPoint.getY(), paint);
        }
    }

    class MjestoGetLocationsTask extends AsyncTask<LocationQuery, Void, String> {

        private Canvas mCanvas;
        private TileProjection mProjection;
        private int mZoom;

        @Override
        protected String doInBackground(LocationQuery... locationQueries) {
            LocationQuery locationQuery = locationQueries[0];
            String url = locationQuery.url;
            mCanvas = locationQuery.canvas;
            Log.d(TAG, "Canvas: " + mCanvas);
            mProjection = locationQuery.projection;
            mZoom = locationQuery.zoom;

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
                MjestoUtils.Location[] locations = MjestoUtils.parseLocationResults(s);
                if (locations != null) {
                    drawLocations(locations, mCanvas, mProjection, mZoom);
                }
            } else {
                Log.d(TAG, "Error Populating");
            }
        }
    }
}