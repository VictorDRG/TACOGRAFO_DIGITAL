package com.example.tacografo_digital;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import android.util.Log;
import com.google.android.gms.maps.model.PolylineOptions;
import android.graphics.Color;


public class Pestanas extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout linearLayoutJornadas;
    private List<Tacografo.Jornada> listaDeJornadas;
    private MapView mapView;
    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pestanas);

        linearLayoutJornadas = findViewById(R.id.linearLayoutJornadas);
        listaDeJornadas = getIntent().getParcelableArrayListExtra("jornadas");

        // Inicializar el MapView
        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }

        mapView.getMapAsync(this); // Obtener el mapa de forma asíncrona
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map; // Asignar el mapa cuando esté listo
        // Configurar el mapa aquí (tipo, centro, etc.)
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mostrarDatosDeJornadas(); // Llamar a este método aquí para que se ejecute después de que el mapa esté listo
    }

    private void mostrarDatosDeJornadas() {
        if (listaDeJornadas != null) {
            for (int i = 0; i < listaDeJornadas.size(); i++) {
                Tacografo.Jornada jornada = listaDeJornadas.get(i);

                TextView fechaInicioTextView = new TextView(this);
                fechaInicioTextView.setText("Fecha de Inicio: " + jornada.fechaInicio);
                fechaInicioTextView.setTextSize(18);
                fechaInicioTextView.setTextAppearance(android.R.style.TextAppearance_Medium);

                TextView fechaFinTextView = new TextView(this);
                fechaFinTextView.setText("Fecha de Fin: " + jornada.fechaFin);
                fechaFinTextView.setTextSize(18);
                fechaFinTextView.setTextAppearance(android.R.style.TextAppearance_Medium);

                TextView conduccionTextView = new TextView(this);
                conduccionTextView.setText("Tiempo de Conducción: " + formatearTiempo(jornada.tiempoConduccion));
                conduccionTextView.setTextSize(16);

                TextView otrosTextView = new TextView(this);
                otrosTextView.setText("Tiempo de Otros Trabajos: " + formatearTiempo(jornada.tiempoOtrosTrabajos));
                otrosTextView.setTextSize(16);

                TextView descansoTextView = new TextView(this);
                descansoTextView.setText("Tiempo de Descanso: " + formatearTiempo(jornada.tiempoDescanso));
                descansoTextView.setTextSize(16);


                linearLayoutJornadas.addView(fechaInicioTextView);
                linearLayoutJornadas.addView(fechaFinTextView);
                linearLayoutJornadas.addView(conduccionTextView);
                linearLayoutJornadas.addView(otrosTextView);
                linearLayoutJornadas.addView(descansoTextView);
                // Mostrar ubicaciones en el mapa
                mostrarUbicacionesEnMapa(jornada.ubicaciones);

            }
        }
    }

    private void mostrarUbicacionesEnMapa(List<Location> ubicaciones) {
        if (googleMap != null && ubicaciones != null && !ubicaciones.isEmpty()) {
            // Limpiar el mapa antes de agregar nuevas ubicaciones
            googleMap.clear();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE).width(5); // Opciones de la polilínea

            for (Location location : ubicaciones) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                // Añadir un marcador para cada ubicación
                builder.include(latLng); // Incluir la ubicación en los límites del mapa
                polylineOptions.add(latLng); // Añadir punto a la polilínea
            }
            googleMap.addPolyline(polylineOptions); // Añadir la polilínea al mapa

            // Centrar y hacer zoom al mapa para mostrar todas las ubicaciones
            LatLngBounds bounds = builder.build();
            int padding = 100; // Ajusta el padding según sea necesario
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e("CameraUpdate", "Error al actualizar la cámara: " + e.getMessage());
                if (ubicaciones.size() == 1) {
                    //Si solo hay una ubicación, centrar el mapa en esa ubicación
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 15)); // Zoom level 15
                } else if (ubicaciones.size() > 1){
                    //Si hay mas de una ubicación pero la camara no se actualiza, centrar en la primera.
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 10));
                }
            }
        } else {
            Log.w("mostrarUbicaciones", "GoogleMap es nulo o no hay ubicaciones para mostrar.");
        }
    }

    private String formatearTiempo(long tiempoMillis) {
        long segundos = tiempoMillis / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos % 60, segundos % 60);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}
