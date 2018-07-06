package net.nipa0711.photosns;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Hyunmin on 2015-06-05.
 * Start refactoring 2018-07-06.
 */
public class ShowGoogleMap extends AppCompatActivity
        implements OnMapReadyCallback {
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_map);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //final globalVar val = (globalVar) getApplicationContext();

        //map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();


        /*LatLng photoTakenLocation = new LatLng(val.Latitude, val.Longitude);
        Marker here = map.addMarker(new MarkerOptions().position(photoTakenLocation).title("here"));

        CameraPosition cp = new CameraPosition.Builder().target((photoTakenLocation)).zoom(15).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final globalVar val = (globalVar) getApplicationContext();
        LatLng photoTakenLocation = new LatLng(val.Latitude, val.Longitude);
        googleMap.addMarker(new MarkerOptions().position(photoTakenLocation).title("here")); //Marker here =

        CameraPosition cp = new CameraPosition.Builder().target((photoTakenLocation)).zoom(15).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }
}
