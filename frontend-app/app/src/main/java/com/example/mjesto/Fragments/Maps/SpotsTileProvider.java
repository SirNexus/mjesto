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
//        LatLng corvallis = new LatLng(44.5646, -123.2620);



        String url = MjestoUtils.getMjestoLocationsUrl(
                Double.toString(bounds.southwest.longitude),
                Double.toString(bounds.southwest.latitude),
                Double.toString(bounds.northeast.longitude),
                Double.toString(bounds.northeast.latitude));

        MjestoUtils.Location[] locations;
        locations = queryLocations(url, canvas, projection, zoom);
        if (locations != null) {
            drawLocations(locations, canvas, projection, zoom);
        }

    }

    private float getStrokeWidth(int zoom) {
        Double Scale = Math.pow(2, zoom - 13);
        return Scale.floatValue();
    }

    private MjestoUtils.Location[] queryLocations(String url, Canvas canvas, TileProjection projection, int zoom) {
        String results = null;
        try {
            results = NetworkUtils.doHttpGet(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Get response: " + results);

        if (results != null) {
            MjestoUtils.Location[] locations = MjestoUtils.parseLocationResults(results);
            return locations;
        } else {
            Log.d(TAG, "Error converting response to Location[]");
            return null;
        }

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
            paint.setStrokeWidth(getStrokeWidth(zoom));

            switch (location.restriction) {
                case "restricted":
                    paint.setColor(ContextCompat.getColor(mContext, R.color.red));
                    break;
                case "limited":
                    if (location.metered) {
                        paint.setColor(ContextCompat.getColor(mContext, R.color.orange));
                    } else {
                        paint.setColor(ContextCompat.getColor(mContext, R.color.yellow));
                    }
                    break;
                case "no restriction":
                    paint.setColor(ContextCompat.getColor(mContext, R.color.green));
                    break;
                default:
                    paint.setColor(Color.BLACK);
            }

            paint.setAntiAlias(true);
            canvas.drawLine((float) beginPoint.getX(), (float) beginPoint.getY(), (float) endPoint.getX(), (float) endPoint.getY(), paint);
        }
    }
}