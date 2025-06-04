package com.example.tacografo_digital;

import android.content.Intent;
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
import com.google.android.gms.maps.model.PolylineOptions;
import android.util.Log;
import android.graphics.Color;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;


public class ResumenJornadas extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout linearLayoutJornadas; // Layout lineal para mostrar los datos de la jornada
    private List<Tacografo.Jornada> listaDeJornadas; // Lista que contiene los datos de la jornada a mostrar
    private MapView mapView; // Vista para mostrar el mapa
    private GoogleMap googleMap; // Objeto para interactuar con el mapa de Google
    private FloatingActionButton fabSalirPestanas; // Botón flotante para salir de esta pantalla

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_jornadas);

        // Obtiene la referencia al LinearLayout donde se mostrarán los datos
        linearLayoutJornadas = findViewById(R.id.linearLayoutJornadas);
        // Obtiene la lista de jornadas pasada como extra desde la Activity anterior
        listaDeJornadas = getIntent().getParcelableArrayListExtra("jornadas");

        // Inicializa la vista del mapa
        mapView = findViewById(R.id.mapView);
        // Asegura que mapView no sea nulo antes de continuar
        if (mapView != null) {
            mapView.onCreate(savedInstanceState); // Llama al método onCreate del del MapView
        }

        // Obtiene la referencia al FloatingActionButton para salir
        fabSalirPestanas = findViewById(R.id.fabSalirPestanas);

        // Solicita que el mapa esté listo de forma asíncrona, el callback se recibirá en onMapReady()
        mapView.getMapAsync(this);

        // Establece el OnClickListener para el FloatingActionButton de salir
        fabSalirPestanas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para volver a la Activity Tacografo
                Intent intent = new Intent(ResumenJornadas.this, Tacografo.class);
                // Inicia la Activity Tacografo
                startActivity(intent);
                finish(); // Opcional: finaliza la Activity actual para que no quede en la pila de actividades
            }
        });
    }

    // Callback que se ejecuta cuando el mapa de Google está listo para ser utilizado
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map; // Asigna la instancia del mapa de Google al objeto googleMap de la clase
        // Configura las opciones iniciales del mapa
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Establece el tipo de mapa a normal

        // Llama al método para mostrar los datos de las jornadas después de que el mapa esté listo
        mostrarDatosDeJornadas();
    }

    // Método para mostrar los datos de la jornada en la interfaz de usuario
    private void mostrarDatosDeJornadas() {
        // Verifica si la lista de jornadas no es nula y no está vacía
        if (listaDeJornadas != null && !listaDeJornadas.isEmpty()) {
            // Obtiene la primera jornada de la lista (actualmente solo se espera una jornada)
            Tacografo.Jornada jornada = listaDeJornadas.get(0);

            // Crea un TextView para mostrar la fecha de inicio de la jornada
            TextView fechaInicioTextView = new TextView(this);
            fechaInicioTextView.setText("Fecha de Inicio: " + jornada.fechaInicio);
            fechaInicioTextView.setTextSize(20);
            fechaInicioTextView.setTextAppearance(android.R.style.TextAppearance_Medium);
            fechaInicioTextView.setTextColor(Color.GREEN);
            fechaInicioTextView.setTextAlignment(fechaInicioTextView.TEXT_ALIGNMENT_CENTER);

            // Crea un TextView para mostrar el tiempo de conducción
            TextView conduccionTextView = new TextView(this);
            conduccionTextView.setText("Tiempo de Conducción: " + formatearTiempo(jornada.tiempoConduccion));
            conduccionTextView.setTextSize(18);
            conduccionTextView.setTextColor(Color.WHITE);
            conduccionTextView.setTextAlignment(fechaInicioTextView.TEXT_ALIGNMENT_CENTER);

            // Crea un TextView para mostrar el tiempo de descanso
            TextView descansoTextView = new TextView(this);
            descansoTextView.setText("Tiempo de Descanso: " + formatearTiempo(jornada.tiempoDescanso));
            descansoTextView.setTextSize(18);
            descansoTextView.setTextColor(Color.WHITE);
            descansoTextView.setTextAlignment(fechaInicioTextView.TEXT_ALIGNMENT_CENTER);

            // Crea un TextView para mostrar el tiempo de otros trabajos
            TextView otrosTextView = new TextView(this);
            otrosTextView.setText("Tiempo de Otros Trabajos: " + formatearTiempo(jornada.tiempoOtrosTrabajos));
            otrosTextView.setTextSize(18);
            otrosTextView.setTextColor(Color.WHITE);
            otrosTextView.setTextAlignment(fechaInicioTextView.TEXT_ALIGNMENT_CENTER);

            // Crea un TextView para mostrar la fecha de fin de la jornada
            TextView fechaFinTextView = new TextView(this);
            fechaFinTextView.setText("Fecha de Fin: " + jornada.fechaFin);
            fechaFinTextView.setTextSize(20);
            fechaFinTextView.setTextAppearance(android.R.style.TextAppearance_Medium);
            fechaFinTextView.setTextColor(Color.RED);
            fechaFinTextView.setTextAlignment(fechaInicioTextView.TEXT_ALIGNMENT_CENTER);

            // Agrega los TextViews al LinearLayout para mostrarlos en la pantalla
            linearLayoutJornadas.addView(fechaInicioTextView);
            linearLayoutJornadas.addView(conduccionTextView);
            linearLayoutJornadas.addView(descansoTextView);
            linearLayoutJornadas.addView(otrosTextView);
            linearLayoutJornadas.addView(fechaFinTextView);

            // Llama al método para mostrar las ubicaciones de la jornada en el mapa
            mostrarUbicacionesEnMapa(jornada.ubicaciones);
        }
    }

    // Método para mostrar las ubicaciones de una lista en el mapa
    private void mostrarUbicacionesEnMapa(List<Location> ubicaciones) {
        // Verifica si el mapa de Google está inicializado, la lista de ubicaciones no es nula y no está vacía
        if (googleMap != null && ubicaciones != null && !ubicaciones.isEmpty()) {
            // Limpia cualquier marcador o polilínea anterior del mapa
            googleMap.clear();
            // Crea un constructor de límites para ajustar la cámara del mapa para que incluya todas las ubicaciones
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            // Crea opciones para la polilínea que se dibujará en el mapa
            PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE).width(5);

            // Itera sobre la lista de ubicaciones
            for (Location location : ubicaciones) {
                // Crea un objeto LatLng a partir de la latitud y longitud de la ubicación
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                // Incluye este punto en los límites del mapa
                builder.include(latLng);
                // Añade este punto a la polilínea
                polylineOptions.add(latLng);
            }
            // Añade la polilínea al mapa
            googleMap.addPolyline(polylineOptions);

            // Construye los límites del mapa
            LatLngBounds bounds = builder.build();
            // Define un padding para que los puntos no estén pegados a los bordes del mapa
            int padding = 100; // Ajusta el padding según sea necesario
            // Intenta animar la cámara del mapa para mostrar todos los puntos dentro de los límites
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                // Captura la excepción si hay un error al actualizar la cámara (por ejemplo, si solo hay una ubicación)
                Log.e("CameraUpdate", "Error al actualizar la cámara: " + e.getMessage());
                // Si solo hay una ubicación, centra el mapa en esa ubicación con un nivel de zoom específico
                if (ubicaciones.size() == 1) {
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 15)); // Nivel de zoom 15
                } else if (ubicaciones.size() > 1){
                    // Si hay más de una ubicación pero la cámara no se actualiza, centra el mapa en la primera ubicación con un nivel de zoom menor
                    Location loc = ubicaciones.get(0);
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 10));
                }
            }
        } else {
            Log.w("mostrarUbicaciones", "GoogleMap es nulo o no hay ubicaciones para mostrar.");
        }
    }

    // Método para formatear el tiempo en formato HH:MM:SS
    private String formatearTiempo(long tiempoMillis) {
        long segundos = tiempoMillis / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos % 60, segundos % 60);
    }

    // Métodos del ciclo de vida del MapView que deben ser llamados para su correcto funcionamiento
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