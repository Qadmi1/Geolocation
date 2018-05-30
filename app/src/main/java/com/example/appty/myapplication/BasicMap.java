package com.example.appty.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by appty on 28/05/18.
 */

public class BasicMap extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;

    ImageButton m1, m2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bmap);

        mapFragment.getMapAsync(this);

        m1 = findViewById(R.id.imageButton);
        m2 = findViewById(R.id.imageButton2);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        m1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LatLng Sligo = new LatLng(54.3434, -8.2313);

                mMap.addMarker(new MarkerOptions().position(Sligo).title("Sligo"));

                mMap.moveCamera(CameraUpdateFactory.newLatLng(Sligo));

                mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));

            }
        });


        m2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });
    }
}
