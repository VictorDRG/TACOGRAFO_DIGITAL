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

    private MapView mapViewDetalle; // Vista para mostrar el mapa de detalles de la jornada.
    private GoogleMap googleMap; // Objeto para interactuar con el mapa de Google.
    private List<Location> listaUbicaciones; // Lista que contiene las coordenadas de ubicación de la jornada seleccionada.
    private FloatingActionButton fabSalirMapa; // Botón flotante para permitir al usuario salir de esta pantalla y volver al historial.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_mapa_jornada);

        // Obtiene una referencia al MapView desde el layout utilizando su ID.
        mapViewDetalle = findViewById(R.id.mapViewDetalle);
        // Llama al método onCreate del ciclo de vida del MapView para inicializarlo correctamente.
        mapViewDetalle.onCreate(savedInstanceState);
        mapViewDetalle.getMapAsync(this);

        // Obtiene una referencia al FloatingActionButton para salir utilizando su ID.
        fabSalirMapa = findViewById(R.id.fabSalirMapa);

        // Obtiene la lista de objetos Location (parcelables) que fue pasada como extra desde la actividad HistorialJornadas.
        listaUbicaciones = getIntent().getParcelableArrayListExtra("ubicaciones");

        // Establece un OnClickListener para el FloatingActionButton de salir.
        fabSalirMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para volver a la actividad HistorialJornadas.
                Intent intent = new Intent(DetalleMapaJornada.this, HistorialJornadas.class);
                // Inicia la actividad HistorialJornadas.
                startActivity(intent);
                // Finaliza la actividad actual (DetalleMapaJornada) para liber recursos de memoria.
                finish();
            }
        });
    }

    // Callback que se ejecuta cuando la instancia del mapa de Google está lista para ser utilizada.
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map; // Asigna la instancia del mapa de Google al objeto googleMap de la clase.
        // Establece el tipo de mapa a normal
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Verifica si la lista de ubicaciones no es nula y contiene al menos un elemento.
        if (listaUbicaciones != null && !listaUbicaciones.isEmpty()) {
            // Llama al método para mostrar las ubicaciones en el mapa.
            mostrarUbicacionesEnMapa(listaUbicaciones);
        }
    }

    // Método para mostrar una lista de coordenadas de ubicación en el mapa.
    private void mostrarUbicacionesEnMapa(List<Location> ubicaciones) {
        // Verifica si el mapa de Google está inicializado, la lista de ubicaciones no es nula y no está vacía.
        if (googleMap != null && ubicaciones != null && !ubicaciones.isEmpty()) {
            // Limpia cualquier marcador o polilínea que pudiera haber estado previamente en el mapa.
            googleMap.clear();
            // Crea un constructor de límites para ajustar la cámara del mapa de manera que todas las ubicaciones sean visibles.
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            // Crea opciones para la polilínea que conectará las ubicaciones en el mapa, estableciendo su color a azul y su grosor a 5 píxeles.
            PolylineOptions polylineOptions = new PolylineOptions().color(android.graphics.Color.BLUE).width(5);

            // Itera sobre cada objeto Location en la lista de ubicaciones.
            for (Location location : ubicaciones) {
                // Crea un objeto LatLng (latitud y longitud) a partir de las coordenadas de la ubicación.
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                // Incluye este punto en los límites que se utilizarán para ajustar la cámara.
                builder.include(latLng);
                // Añade este punto a la polilínea que se dibujará en el mapa.
                polylineOptions.add(latLng);
            }
            // Añade la polilínea al mapa.
            googleMap.addPolyline(polylineOptions);

            // Construye los límites del mapa a partir de todas las ubicaciones incluidas.
            LatLngBounds bounds = builder.build();
            // Define un padding (espacio alrededor de los límites) para que los puntos no estén pegados a los bordes del mapa.
            int padding = 100;
            // Intenta animar la cámara del mapa para mostrar todos los puntos dentro de los límites definidos.
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                // Captura cualquier excepción que pueda ocurrir durante la actualización de la cámara (por ejemplo, si solo hay una ubicación).
                Log.e("DetalleMapa", "Error al actualizar la cámara: " + e.getMessage());
                // Si solo hay una ubicación, centra el mapa en esa ubicación con un nivel de zoom específico.
                if (ubicaciones.size() == 1) {
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 15)); // Nivel de zoom 15 (acercamiento significativo).
                } else if (ubicaciones.size() > 1){
                    // Si hay más de una ubicación pero la actualización automática de la cámara falla, centra el mapa en la primera ubicación con un nivel de zoom menor.
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 10)); // Nivel de zoom 10 (acercamiento moderado).
                }
            }
        }
    }

    // Métodos del ciclo de vida del MapView que deben ser llamados para asegurar su correcto funcionamiento.
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