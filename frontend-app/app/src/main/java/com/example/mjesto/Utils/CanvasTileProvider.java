package com.example.mjesto.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

// All credit for this code goes to Stack Overflow post:
// https://stackoverflow.com/questions/23936780/polylines-performance-issues-on-googlemaps-api-v2

/* imports should be obvious */
public abstract class CanvasTileProvider implements TileProvider {
    private static int TILE_SIZE = 512

            ;

    private BitMapThreadLocal tlBitmap;

    @SuppressWarnings("unused")
    private static final String TAG = CanvasTileProvider.class.getSimpleName();

    public CanvasTileProvider() {
        super();
        tlBitmap = new BitMapThreadLocal();
    }

    @Override
// Warning: Must be threadsafe. To still avoid creation of lot of bitmaps,
// I use a subclass of ThreadLocal !!!
    public Tile getTile(int x, int y, int zoom) {
        TileProjection projection = new TileProjection(TILE_SIZE,
                x, y, zoom);

        Log.d(CanvasTileProvider.class.getSimpleName(), "Test getTile");
        byte[] data;
        Bitmap image = getNewBitmap();
        Canvas canvas = new Canvas(image);
        onDraw(canvas, projection, zoom);
        data = bitmapToByteArray(image);
        Tile tile = new Tile(TILE_SIZE, TILE_SIZE, data);
        return tile;
    }

    public abstract void onDraw(Canvas canvas, TileProjection projection, int zoom);

    /**
     * Get an empty bitmap, which may however be reused from a previous call in
     * the same thread.
     *
     * @return
     */
    private Bitmap getNewBitmap() {
        Bitmap bitmap = tlBitmap.get();
        // Clear the previous bitmap
        bitmap.eraseColor(Color.TRANSPARENT);
        return bitmap;
    }

    private static byte[] bitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] data = bos.toByteArray();
        return data;
    }

    class BitMapThreadLocal extends ThreadLocal<Bitmap> {
        @Override
        protected Bitmap initialValue() {
            Bitmap image = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE,
                    Bitmap.Config.ARGB_8888);
            return image;
        }
    }

    /** Converts between LatLng coordinates and the pixels inside a tile. */
    public class TileProjection {

        private int x;
        private int y;
        private int zoom;
        private int TILE_SIZE;

        private DoublePoint pixelOrigin_;
        private double pixelsPerLonDegree_;
        private double pixelsPerLonRadian_;

        TileProjection(int tileSize, int x, int y, int zoom) {
            this.TILE_SIZE = tileSize;
            this.x = x;
            this.y = y;
            this.zoom = zoom;
            pixelOrigin_ = new DoublePoint(TILE_SIZE / 2, TILE_SIZE / 2);
            pixelsPerLonDegree_ = TILE_SIZE / 360d;
            pixelsPerLonRadian_ = TILE_SIZE / (2 * Math.PI);
        }

        /** Get the dimensions of the Tile in LatLng coordinates */
        public LatLngBounds getTileBounds() {
            DoublePoint tileSW = new DoublePoint(x * TILE_SIZE, (y + 1) * TILE_SIZE);
            DoublePoint worldSW = pixelToWorldCoordinates(tileSW);
            LatLng SW = worldCoordToLatLng(worldSW);
            DoublePoint tileNE = new DoublePoint((x + 1) * TILE_SIZE, y * TILE_SIZE);
            DoublePoint worldNE = pixelToWorldCoordinates(tileNE);
            LatLng NE = worldCoordToLatLng(worldNE);
            return new LatLngBounds(SW, NE);
        }

        /**
         * Calculate the pixel coordinates inside a tile, relative to the left upper
         * corner (origin) of the tile.
         */
        public DoublePoint latLngToPoint(LatLng latLng) {
            DoublePoint result = new DoublePoint(0, 0);
            latLngToWorldCoordinates(latLng, result);
            worldToPixelCoordinates(result, result);
            result.x -= x * TILE_SIZE;
            result.y -= y * TILE_SIZE;
            return result;
        }


        private DoublePoint pixelToWorldCoordinates(DoublePoint pixelCoord) {
            int numTiles = 1 << zoom;
            DoublePoint worldCoordinate = new DoublePoint(pixelCoord.x / numTiles,
                    pixelCoord.y / numTiles);
            return worldCoordinate;
        }

        /**
         * Transform the world coordinates into pixel-coordinates relative to the
         * whole tile-area. (i.e. the coordinate system that spans all tiles.)
         *
         *
         * Takes the resulting point as parameter, to avoid creation of new objects.
         */
        private void worldToPixelCoordinates(DoublePoint worldCoord, DoublePoint result) {
            int numTiles = 1 << zoom;
            result.x = worldCoord.x * numTiles;
            result.y = worldCoord.y * numTiles;
        }

        private LatLng worldCoordToLatLng(DoublePoint worldCoordinate) {
            DoublePoint origin = pixelOrigin_;
            double lng = (worldCoordinate.x - origin.x) / pixelsPerLonDegree_;
            double latRadians = (worldCoordinate.y - origin.y)
                    / -pixelsPerLonRadian_;
            double lat = Math.toDegrees(2 * Math.atan(Math.exp(latRadians))
                    - Math.PI / 2);
            return new LatLng(lat, lng);
        }

        /**
         * Get the coordinates in a system describing the whole globe in a
         * coordinate range from 0 to TILE_SIZE (type double).
         *
         * Takes the resulting point as parameter, to avoid creation of new objects.
         */
        private void latLngToWorldCoordinates(LatLng latLng, DoublePoint result) {
            DoublePoint origin = pixelOrigin_;

            result.x = origin.x + latLng.longitude * pixelsPerLonDegree_;

            // Truncating to 0.9999 effectively limits latitude to 89.189. This is
            // about a third of a tile past the edge of the world tile.
            double siny = bound(Math.sin(Math.toRadians(latLng.latitude)), -0.9999,
                    0.9999);
            result.y = origin.y + 0.5 * Math.log((1 + siny) / (1 - siny))
                    * -pixelsPerLonRadian_;
        };

        /** Return value reduced to min and max if outside one of these bounds. */
        private double bound(double value, double min, double max) {
            value = Math.max(value, min);
            value = Math.min(value, max);
            return value;
        }

        /** A Point in an x/y coordinate system with coordinates of type double */
        public class DoublePoint {
            double x;
            double y;

            public DoublePoint(double x, double y) {
                this.x = x;
                this.y = y;
            }

            public double getX() {
                return x;
            }

            public double getY() {
                return y;
            }
        }

    }
}