package com.example.vijay.trackmymobile;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.CameraUpdate;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import android.os.Handler;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;
import com.google.android.gms.maps.model.Marker;


public class TrackMobile extends FragmentActivity {

    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    private static final String TAG = "TrackMobileActivity";
    private AsyncHttpClient mClient = new AsyncHttpClient();
    Handler mHandler = new Handler();
    private Marker marker;


    private static final long FIVE_SECONDS = 5000;
    private static final String GET_URL = "http://52.16.146.63/vehicle/location/";
    //initializing custom Marker
    //TODO : Need to call the constructor instead of initializing it here
    CustomMarker myMarker = new CustomMarker("",(double)0,(double)0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_mobile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleGetLocations();
        setUpMapIfNeeded();
        Log.d(TAG, "Activity resume.....");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Activity paused.....");
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #//setUpMapMarker()} once when {@link #googleMap} is not null.
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
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            //TODO: Need to remove the hacks
            // Check if we were successful in obtaining the map.
//            if (googleMap != null) {
//                latlong = new LatLng(49, 79);
//                setUpMap(latlong);
//            }
        }
    }

    /*This method is used to add a new marker if it does not exisit
     */

    public void addMarker(CustomMarker customMarker){
        LatLng latlng = new LatLng(customMarker.getCustomMarkerLatitude(),
                         customMarker.getCustomMarkerLongitude());
        Log.d(TAG,"Success addMarker Latitude  "  + latlng.latitude);
        Log.d(TAG,"Success addMarker Longitude "  + latlng.longitude);
        marker = googleMap.addMarker(new MarkerOptions()
                .position(latlng)
                .title("225D")
                .snippet("Lingampally to Dilshuknagar")
                .draggable(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.setMyLocationEnabled(false); // false to disable
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 18);
        googleMap.animateCamera(cameraUpdate);
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
                    //TODO : Need to write a function which returns boolean values
                    if ((myMarker.getCustomMarkerLatitude() == 0) &&
                            (myMarker.getCustomMarkerLongitude()== 0)){
                        myMarker.setCustomMarkerId("myMarker");
                        myMarker.setCustomMarkerLatitude(current_lat);
                        myMarker.setCustomMarkerLongitude(current_long);
                        findMarker(myMarker);
                        addMarker(myMarker);
                    } else {
                        moveVehicle(prev_latlong, current_latlong, velocity);
                    }
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
        Log.d(TAG, "Success moveVehicle");
        animateMarker(myMarker, current_lat);
    }

    //this is method to help us find a Marker that is stored into the hashmap

    public Marker findMarker(CustomMarker customMarker) {
        Log.d(TAG,"Success Calling findMarker");
        Log.d(TAG,"Success findMarker Markerid " + customMarker.getCustomMarkerId());
        Log.d(TAG,"Success findMarker latitude " + customMarker.getCustomMarkerLatitude());
        Log.d(TAG,"Success findMarker longitude " + customMarker.getCustomMarkerLongitude());
        if((customMarker.getCustomMarkerLatitude() != 0) &&
                (customMarker.getCustomMarkerLongitude() != 0)) {
            Log.d(TAG,"Success finding marker ");
            return marker;
        }
        else {
            return null;
        }
    }

    /*The function is used to animate the Marker
      Different implementations of the function are invoked depending on the
      Android device.
     */
    public void animateMarker(CustomMarker customMarker, LatLng latlng) {
        if (findMarker(customMarker) != null) {
            Log.d(TAG,"Success animateMarker");
            LatLngInterpolator latlngInter = new LatLngInterpolator.LinearFixed();
            latlngInter.interpolate(20,
            new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), latlng);
            customMarker.setCustomMarkerLatitude(latlng.latitude);
            customMarker.setCustomMarkerLongitude(latlng.longitude);

            if (android.os.Build.VERSION.SDK_INT >= 14) {
                Log.d(TAG,"Animating marker for ICS Android");
                MarkerAnimation.animateMarkerToICS(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
                        customMarker.getCustomMarkerLongitude()), latlngInter);
            } else if (android.os.Build.VERSION.SDK_INT >= 11) {
                MarkerAnimation.animateMarkerToHC(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
                        customMarker.getCustomMarkerLongitude()), latlngInter);
            } else {
                MarkerAnimation.animateMarkerToGB(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
                        customMarker.getCustomMarkerLongitude()), latlngInter);
            }
        }
    }

}


