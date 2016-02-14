package com.example.vijay.trackmymobile;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdate;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import android.os.Handler;


import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import java.util.HashMap;
import java.util.Map;

public class TrackMobile extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final String TAG = "TrackMobileActivity";
    private AsyncHttpClient mClient = new AsyncHttpClient();
    Handler mHandler = new Handler();
    private LatLng latlong;
    private static final long FIVE_SECONDS = 5000;
    private static final String GET_URL = "http://52.16.146.63/vehicle/location/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_mobile);
        scheduleGetLocations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
//            if (mMap != null) {
//                latlong = new LatLng(49, 79);
//                setUpMap(latlong);
//            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng latlong) {
        mMap.addMarker(new MarkerOptions()
                .position(latlong)
                .title("225D")
                .snippet("Lingampally to Dilshuknagar")
                .draggable(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.setMyLocationEnabled(false); // false to disable
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlong, 18);
        mMap.animateCamera(cameraUpdate);

    }

    public void scheduleGetLocations() {

        mHandler.postDelayed(new Runnable() {
            public void run() {
                getLocation();          // this method will contain your almost-finished HTTP calls
                mHandler.postDelayed(this, FIVE_SECONDS);
            }
        }, FIVE_SECONDS);
    }

    private void getLocation() {
        RequestParams mParams = new RequestParams();

        mClient.get(GET_URL, mParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                return response;
                try {

                    Double prev_lat = new Double(response.get("prev_lat").toString());
                    Double prev_long = new Double(response.get("prev_long").toString());
                    Double current_lat = new Double(response.get("current_lat").toString());
                    Double current_long = new Double(response.get("current_long").toString());
                    Double velocity = new Double(response.get("velocity").toString());
                    LatLng prev_latlong = new LatLng(prev_lat, prev_long);
                    LatLng current_latlong = new LatLng(current_lat, current_long);
                    moveVehicle(prev_latlong, current_latlong, velocity);
                } catch (Exception e) {
                    Log.d(TAG, "exception catch" + e);
                }


                Log.d(TAG, "Success get to remote api ........." + statusCode);
                Log.d(TAG, "Success get to remote api ........." + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d(TAG, "Failure to get api ...." + statusCode);
            }
        });
    }

    public void moveVehicle(LatLng prev_lat, LatLng current_lat, Double velocity) {
        setUpMap(current_lat);

    }
}


