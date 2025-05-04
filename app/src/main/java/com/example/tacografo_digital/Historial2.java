package com.example.tacografo_digital;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.util.Log;
import android.location.Location;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;

public class Historial2 extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Location> listaDeUbicaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial2);

        // Obtener la lista de ubicaciones del Intent
        listaDeUbicaciones = getIntent().getParcelableArrayListExtra("ubicaciones");

        if (listaDeUbicaciones == null || listaDeUbicaciones.isEmpty()) {
            // Manejar el caso donde no hay ubicaciones
            Log.e("HistorialRutaActivity", "No se recibieron ubicaciones para mostrar la ruta.");
            finish(); // Cerrar la actividad si no hay datos
            return;
        }

        // Obtener el SupportMapFragment y configurar el callback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Crear opciones de polilínea para la ruta
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE); // Color de la ruta
        polylineOptions.width(10);       // Ancho de la ruta

        // Crear límites para la cámara del mapa
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // Iterar sobre la lista de ubicaciones
        for (Location location : listaDeUbicaciones) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            polylineOptions.add(latLng); // Añadir punto a la polilínea
            builder.include(latLng);    // Incluir punto en los límites
        }
        if(listaDeUbicaciones != null && listaDeUbicaciones.size() > 0){
            // Mover la cámara para mostrar la ruta completa
            LatLngBounds bounds = builder.build();
            int padding = 100; // Ajusta el padding según sea necesario
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            mMap.addPolyline(polylineOptions); // Dibujar la ruta en el mapa
        }


    }
}

