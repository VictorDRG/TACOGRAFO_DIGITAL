package com.example.tacografo_digital;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class Tacografo extends AppCompatActivity implements LocationListener {

    // Botones de la interfaz de usuario para los contadores
    Button btIniciarConduccion;
    Button btIniciarOtros;
    Button btIniciarDescanso;
    Button btHistorial;
    Button btSalirTac;
    Button btInicioJornada;

    // TextViews para mostrar el tiempo
    private TextView textViewConduccion;
    private TextView textViewOtros;
    private TextView textViewDescanso;

    private TextView twLetreroConduccion;
    private TextView twLetreroDescanso;
    private TextView twLetreroOtros;

    // Variables para el seguimiento del tiempo y la ubicación
    private boolean iniciarJornada = false;
    private boolean conduccionActiva = false;
    private boolean otrosActivos = false;
    private boolean descansoActivo = false;
    private long tiempoConduccion = 0;
    private long tiempoOtrosTrabajos = 0;
    private long tiempoDescanso = 0;
    private Location ultimaUbicacion = null;
    private final Handler handler = new Handler();
    private Runnable actualizarTiempoRunnable;
    private String fechaInicioJornada; // Añadida fecha de inicio
    private String fechaFinJornada; // Añadida fecha de fin
    private List<Jornada> listaDeJornadas = new ArrayList<>(); // Lista para almacenar jornadas completas
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<Location> listaDeUbicaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tacografo);

        // Inicializar los botones de los contadores
        btIniciarConduccion = findViewById(R.id.btConduccion);
        btIniciarOtros = findViewById(R.id.btOtros);
        btIniciarDescanso = findViewById(R.id.btDescanso);
        btHistorial = findViewById(R.id.btHistorial);
        btSalirTac = findViewById(R.id.btSalirTac);
        btInicioJornada = findViewById(R.id.btInicioJornada);
        twLetreroConduccion = findViewById(R.id.twLetreroConduccion);
        twLetreroDescanso = findViewById(R.id.twLetreroDescanso);
        twLetreroOtros = findViewById(R.id.twLetreroOtros);

        // Inicializar las TextViews para mostrar el tiempo
        textViewConduccion = findViewById(R.id.textViewConduccion);
        textViewOtros = findViewById(R.id.textViewOtros);
        textViewDescanso = findViewById(R.id.textViewDescanso);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verificar y solicitar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // No iniciar las actualizaciones de ubicación hasta que se inicie la jornada
        }

        // Inicializar el cliente de ubicación fusionada
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest(); // Crear la solicitud de ubicación
        createLocationCallback(); // Crear el callback para recibir los resultados de la ubicación

        // Configurar listeners para los botones de los contadores
        btIniciarConduccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iniciarJornada) {
                    if (!conduccionActiva && !otrosActivos && !descansoActivo) {
                        conduccionActiva = true;
                        btIniciarConduccion.setText("DETENER CONDUCCIÓN");
                        twLetreroConduccion.setTextColor(Color.parseColor("#37ff21"));
                    } else if (conduccionActiva) {
                        conduccionActiva = false;
                        btIniciarConduccion.setText("CONDUCCIÓN");
                        twLetreroConduccion.setTextColor(Color.parseColor("#FF8F00"));
                    }
                }
            }
        });

        btIniciarOtros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iniciarJornada) {
                    if (!conduccionActiva && !otrosActivos && !descansoActivo) {
                        otrosActivos = true;
                        btIniciarOtros.setText("DETENER OTROS");
                        twLetreroOtros.setTextColor(Color.parseColor("#37ff21"));

                    } else if (otrosActivos) {
                        otrosActivos = false;
                        btIniciarOtros.setText("OTROS");
                        twLetreroOtros.setTextColor(Color.parseColor("#FF8F00"));
                    }
                }
            }
        });

        btIniciarDescanso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iniciarJornada) {
                    if (!conduccionActiva && !otrosActivos && !descansoActivo) {
                        descansoActivo = true;
                        btIniciarDescanso.setText("DETENER DESCANSO");
                        twLetreroDescanso.setTextColor(Color.parseColor("#37ff21"));
                    } else if (descansoActivo) {
                        descansoActivo = false;
                        btIniciarDescanso.setText("DESCANSO");
                        twLetreroDescanso.setTextColor(Color.parseColor("#FF8F00"));
                    }
                }
            }
        });

        // Configurar el listener para el botón de inicio/fin de jornada (solo gestiona el estado de la jornada y el GPS)
        btInicioJornada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!iniciarJornada) {
                    iniciarJornada = true;
                    btInicioJornada.setText("FINALIZAR JORNADA");
                    listaDeUbicaciones.clear();
                    fechaInicioJornada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

                    if (ContextCompat.checkSelfPermission(Tacografo.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startLocationUpdates(); // Comenzar las actualizaciones de ubicación al iniciar la jornada
                        handler.postDelayed(actualizarTiempoRunnable, 1000); // Iniciar el runnable para actualizar los tiempos
                    } else {
                        ActivityCompat.requestPermissions(Tacografo.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    // Reiniciar los tiempos al iniciar una nueva jornada
                    tiempoConduccion = 0;
                    tiempoOtrosTrabajos = 0;
                    tiempoDescanso = 0;
                    actualizarTextViews(); // Actualizar la vista con los tiempos reiniciados
                    // Establecer los botones de contador a "Iniciar" al inicio de la jornada
                    btIniciarConduccion.setText("CONDUCCIÓN");
                    btIniciarOtros.setText("OTROS");
                    btIniciarDescanso.setText("DESCANSO");
                    conduccionActiva = false;
                    otrosActivos = false;
                    descansoActivo = false;

                    // Bloquear y poner opaco el botón Historial al iniciar la jornada
                    btHistorial.setEnabled(false);
                    btHistorial.setAlpha(0.5f); // Opacidad al 50%
                } else {
                    iniciarJornada = false;
                    btInicioJornada.setText("INICIAR JORNADA");
                    handler.removeCallbacks(actualizarTiempoRunnable);
                    stopLocationUpdates(); // Detener las actualizaciones de ubicación al finalizar la jornada
                    fechaFinJornada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()); // Obtener fecha y hora de fin

                    Jornada jornada = new Jornada(fechaInicioJornada, fechaFinJornada ,new ArrayList<>(listaDeUbicaciones), tiempoConduccion, tiempoOtrosTrabajos, tiempoDescanso);
                    listaDeJornadas.add(jornada);

                    Intent intent = new Intent(Tacografo.this, Pestanas.class);
                    intent.putParcelableArrayListExtra("jornadas", (ArrayList<Jornada>) listaDeJornadas);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                    // Desbloquear y restaurar la opacidad del botón Historial al finalizar la jornada
                    btHistorial.setEnabled(true);
                    btHistorial.setAlpha(1.0f); // Opacidad completa
                }
            }
        });
        // Configurar el listener para el botón de historial
        btHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Tacografo.this, Pestanas.class);
                intent.putParcelableArrayListExtra("jornadas", (ArrayList<Jornada>) listaDeJornadas);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // Configurar el listener para el botón de salir
        btSalirTac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Tacografo.this, Loggin.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // Runnable para actualizar el tiempo
        actualizarTiempoRunnable = new Runnable() {
            @Override
            public void run() {
                if (iniciarJornada) {
                    if (conduccionActiva) {
                        tiempoConduccion += 1000;
                    }
                    if (otrosActivos) {
                        tiempoOtrosTrabajos += 1000;
                    }
                    if (descansoActivo) {
                        tiempoDescanso += 1000;
                    }
                    actualizarTextViews();
                }
                handler.postDelayed(this, 1000);
            }
        };
    }

    private void actualizarTextViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewConduccion.setText(formatearTiempo(tiempoConduccion));
                textViewOtros.setText(formatearTiempo(tiempoOtrosTrabajos));
                textViewDescanso.setText(formatearTiempo(tiempoDescanso));
            }
        });
    }

    // Método para crear la solicitud de ubicación
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000); // 5 segundos
        locationRequest.setFastestInterval(3000); // 3 segundos (opcional)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Alta precisión
    }

    private Location currentLocation;

    // Método para crear el callback de la ubicación
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null && iniciarJornada && conduccionActiva) { // Solo guardar si la conducción está activa
                        currentLocation = location;
                        Log.d("UbicacionGuardada", "Latitud: " + currentLocation.getLatitude() + ", Longitud: " + currentLocation.getLongitude());
                        listaDeUbicaciones.add(currentLocation);
                        if (ultimaUbicacion == null) {
                            ultimaUbicacion = location;
                        }
                    }
                }
            }
        };
    }

    // Método para comenzar las actualizaciones de ubicación
    private void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    // Método para detener las actualizaciones de ubicación
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (iniciarJornada && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    // Método para manejar los resultados de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (iniciarJornada) {
                    startLocationUpdates(); // Comenzar las actualizaciones de ubicación si la jornada ya está iniciada
                    handler.postDelayed(actualizarTiempoRunnable, 1000); // Iniciar el runnable si la jornada ya está iniciada
                }
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // No necesitamos esta callback directamente con FusedLocationProviderClient
    }

    // Método para formatear el tiempo en HH:MM:SS
    private String formatearTiempo(long tiempoMillis) {
        long segundos = tiempoMillis / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos % 60, segundos % 60);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    // Clase para representar una jornada
    public static class Jornada implements android.os.Parcelable {
        public String fechaInicio;
        public String fechaFin;
        public List<Location> ubicaciones;
        public long tiempoConduccion;
        public long tiempoOtrosTrabajos;
        public long tiempoDescanso;

        public Jornada(String fechaInicio, String fechaFin, List<Location> ubicaciones, long tiempoConduccion, long tiempoOtrosTrabajos, long tiempoDescanso) {
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.ubicaciones = ubicaciones;
            this.tiempoConduccion = tiempoConduccion;
            this.tiempoOtrosTrabajos = tiempoOtrosTrabajos;
            this.tiempoDescanso = tiempoDescanso;
        }

        protected Jornada(android.os.Parcel in) {
            fechaInicio = in.readString();
            fechaFin = in.readString();
            ubicaciones = in.createTypedArrayList(Location.CREATOR);
            tiempoConduccion = in.readLong();
            tiempoOtrosTrabajos = in.readLong();
            tiempoDescanso = in.readLong();
        }

        public static final android.os.Parcelable.Creator<Jornada> CREATOR = new android.os.Parcelable.Creator<Jornada>() {
            @Override
            public Jornada createFromParcel(android.os.Parcel in) {
                return new Jornada(in);
            }

            @Override
            public Jornada[] newArray(int size) {
                return new Jornada[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {
            dest.writeString(fechaInicio);
            dest.writeString(fechaFin);
            dest.writeTypedList(ubicaciones);
            dest.writeLong(tiempoConduccion);
            dest.writeLong(tiempoOtrosTrabajos);
            dest.writeLong(tiempoDescanso);
        }
    }
}