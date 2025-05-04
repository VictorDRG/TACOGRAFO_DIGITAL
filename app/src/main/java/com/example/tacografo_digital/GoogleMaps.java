package com.example.tacografo_digital;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.tacografo_digital.databinding.ActivityGoogleMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;
import android.graphics.Color;
import android.Manifest;

import java.util.ArrayList;
import java.util.List;

public class GoogleMaps extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap; // Objeto GoogleMap para interactuar con el mapa
    private ActivityGoogleMapsBinding binding;  // Objeto de enlace de vistas para la actividad
    private LocationManager locationManager;    // Objeto para gestionar la ubicación
    private static final long LOCATION_REFRESH_TIME = 2000; // Tiempo de actualización de ubicación (2 segundos)
    private static final float LOCATION_REFRESH_DISTANCE = 0;   // Distancia mínima para la actualización (0 metros)
    List<LatLng> ruta = new ArrayList<>();  // Lista para almacenar las coordenadas de la ruta

    // Método para iniciar las actualizaciones de ubicación
    private void startLocationUpdates() {
        // Verifica si los permisos de ubicación están concedidos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Solicita actualizaciones de ubicación usando NETWORK_PROVIDER
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Infla el diseño de la actividad usando View Binding
        binding = ActivityGoogleMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtiene el SupportMapFragment y notifica cuando el mapa está listo para ser usado
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Obtiene el servicio LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verifica y solicita permisos de ubicación si no están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Si los permisos están concedidos, inicia las actualizaciones de ubicación
            startLocationUpdates();
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Se llama cuando se recibe una nueva ubicación
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        ruta.add(latLng);   // Agrega la ubicación a la lista de ruta

        if (mMap != null) {
            mMap.clear();   // Limpia el mapa
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(ruta);   // Agrega todas las coordenadas de la ruta a la polilínea
            polylineOptions.width(10);  // Establece el ancho de la polilínea
            polylineOptions.color(Color.BLUE);  // Establece el color de la polilínea
            mMap.addPolyline(polylineOptions);  // Agrega la polilínea al mapa
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); // Mueve la cámara a la ubicación actual
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Se llama cuando el usuario responde a la solicitud de permisos
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si los permisos son concedidos, inicia las actualizaciones de ubicación
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Se llama cuando el mapa está listo para ser usado
        mMap = googleMap;

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Se llama cuando el estado del proveedor de ubicación cambia
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Se llama cuando el proveedor de ubicación está habilitado
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Se llama cuando el proveedor de ubicación está deshabilitado
    }
}