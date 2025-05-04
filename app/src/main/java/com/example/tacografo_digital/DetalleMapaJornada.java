package com.example.tacografo_digital;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;

import java.util.List;

public class DetalleMapaJornada extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapViewDetalle;
    private GoogleMap googleMap;
    private List<Location> listaUbicaciones;
    private FloatingActionButton fabSalirMapa; // Declarar el FloatingActionButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_mapa_jornada);

        mapViewDetalle = findViewById(R.id.mapViewDetalle);
        mapViewDetalle.onCreate(savedInstanceState);
        mapViewDetalle.getMapAsync(this);

        fabSalirMapa = findViewById(R.id.fabSalirMapa); // Obtener la referencia al FAB

        listaUbicaciones = getIntent().getParcelableArrayListExtra("ubicaciones");

        // Establecer el OnClickListener para el FAB
        fabSalirMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetalleMapaJornada.this, HistorialJornadas.class); // Reemplaza HistorialJornadas.class con la Activity a la que quieres ir
                startActivity(intent);
                finish(); // Opcional: finalizar la Activity actual
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (listaUbicaciones != null && !listaUbicaciones.isEmpty()) {
            mostrarUbicacionesEnMapa(listaUbicaciones);
        }
    }

    private void mostrarUbicacionesEnMapa(List<Location> ubicaciones) {
        if (googleMap != null && ubicaciones != null && !ubicaciones.isEmpty()) {
            googleMap.clear();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            PolylineOptions polylineOptions = new PolylineOptions().color(android.graphics.Color.BLUE).width(5);

            for (Location location : ubicaciones) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                builder.include(latLng);
                polylineOptions.add(latLng);
            }
            googleMap.addPolyline(polylineOptions);

            LatLngBounds bounds = builder.build();
            int padding = 100;
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e("DetalleMapa", "Error al actualizar la cámara: " + e.getMessage());
                if (ubicaciones.size() == 1) {
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 15));
                } else if (ubicaciones.size() > 1){
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 10));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapViewDetalle.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewDetalle.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewDetalle.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewDetalle.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapViewDetalle.onSaveInstanceState(outState);
    }
}