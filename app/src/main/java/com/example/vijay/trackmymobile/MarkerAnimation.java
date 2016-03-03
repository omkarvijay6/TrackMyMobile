package com.example.vijay.trackmymobile;

/**
 * Created by bhavani on 15/02/16.
 */
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MarkerAnimation {
    private static final String TAG = "MarkerAnimation";

    public static void animateMarkerToGB(final Marker marker, final LatLng finalPosition,
                                          final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 5000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));
                BitmapDescriptor icon = BitmapDescriptorFactory.fromAsset("images/vehicle_marker.png");
                marker.setIcon(icon);

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static void animateMarkerToHC(final Marker marker, final LatLng finalPosition,
                                          final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                marker.setPosition(newPosition);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromAsset("images/vehicle_marker.png");
                marker.setIcon(icon);
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(5000);
        valueAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void animateMarkerToICS(Marker marker, LatLng finalPosition,
                                           final LatLngInterpolator latLngInterpolator) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromAsset("images/vehicle_marker.png");
        marker.setIcon(icon);
        Double lat1 = Math.toRadians(marker.getPosition().latitude);
        Double lng1 = Math.toRadians(marker.getPosition().longitude);
        Double lat2 = Math.toRadians(finalPosition.latitude);
        Double lng2 = Math.toRadians(finalPosition.longitude);
        Double dPhi = Math.log(Math.tan(lat2/2.0+Math.PI/4.0)/Math.tan(lat1/2.0+Math.PI/4.0));
        double dLong = (lng2-lng1);
        if (Math.abs(dLong) > Math.PI){
            if (dLong > 0.0)
                dLong = -(2.0 * Math.PI - dLong);
            else
                dLong = (2.0 * Math.PI + dLong);
        }
        float brng = (float) ((Math.toDegrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0);
//        marker.setRotation(brng);
        rotateMarker(marker, brng);
        animator.setDuration(5000);
        animator.start();
    }

    static public void rotateMarker(final Marker marker, final float brng) {
    final Handler handler = new Handler();
    final long start = SystemClock.uptimeMillis();
    final float startRotation = marker.getRotation();
    final long duration = 355;

    final Interpolator interpolator = new LinearInterpolator();

    handler.post(new Runnable() {
        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - start;
            float t = interpolator.getInterpolation((float) elapsed / duration);

            float rot = t * brng + (1 -t) * startRotation;

            marker.setRotation(-rot > 180 ? rot/2 : rot);
            if (t < 1.0) {
                // Post again 16ms later.
                handler.postDelayed(this, 16);
            }
        }
    });
    }

}


