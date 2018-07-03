package net.nipa0711.photosns;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Hyunmin on 2015-06-05.
 */
public class ShowGoogleMap extends Activity {
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_map);

        final globalVar val = (globalVar) getApplicationContext();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        LatLng photoTakenLocation = new LatLng(val.Latitude, val.Longitude);
        Marker here = map.addMarker(new MarkerOptions().position(photoTakenLocation).title("here"));

        CameraPosition cp = new CameraPosition.Builder().target((photoTakenLocation)).zoom(15).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }
}
