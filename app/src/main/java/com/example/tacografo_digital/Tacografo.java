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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private String fechaInicioJornada;
    private String fechaFinJornada;
    private List<Jornada> listaDeJornadas = new ArrayList<>();
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<Location> listaDeUbicaciones = new ArrayList<>();
    private static final String FILENAME = "jornadas.json"; // Cambiamos la extensión del archivo

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
                        btIniciarDescanso.setAlpha(0.5f);
                        btIniciarOtros.setAlpha(0.5f);
                    } else if (conduccionActiva) {
                        conduccionActiva = false;
                        btIniciarConduccion.setText("CONDUCCIÓN");
                        twLetreroConduccion.setTextColor(Color.parseColor("#FF8F00"));
                        btIniciarDescanso.setAlpha(1.0f);
                        btIniciarOtros.setAlpha(1.0f);
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
                        btIniciarDescanso.setAlpha(0.5f);
                        btIniciarConduccion.setAlpha(0.5f);
                    } else if (otrosActivos) {
                        otrosActivos = false;
                        btIniciarOtros.setText("OTROS");
                        twLetreroOtros.setTextColor(Color.parseColor("#FF8F00"));
                        btIniciarDescanso.setAlpha(1.0f);
                        btIniciarConduccion.setAlpha(1.0f);
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
                        btIniciarConduccion.setAlpha(0.5f);
                        btIniciarOtros.setAlpha(0.5f);
                    } else if (descansoActivo) {
                        descansoActivo = false;
                        btIniciarDescanso.setText("DESCANSO");
                        twLetreroDescanso.setTextColor(Color.parseColor("#FF8F00"));
                        btIniciarConduccion.setAlpha(1.0f);
                        btIniciarOtros.setAlpha(1.0f);
                    }
                }
            }
        });

        // Cargar la lista de jornadas desde JSON al iniciar la Activity
        cargarJornadasDesdeJSON();

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

                    btSalirTac.setEnabled(false);
                    btSalirTac.setAlpha(0.5f);
                } else {
                    iniciarJornada = false;
                    btInicioJornada.setText("INICIAR JORNADA");
                    handler.removeCallbacks(actualizarTiempoRunnable);
                    stopLocationUpdates(); // Detener las actualizaciones de ubicación al finalizar la jornada
                    fechaFinJornada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()); // Obtener fecha y hora de fin

                    Jornada jornada = new Jornada(fechaInicioJornada, fechaFinJornada ,new ArrayList<>(listaDeUbicaciones), tiempoConduccion, tiempoOtrosTrabajos, tiempoDescanso);
                    listaDeJornadas.add(jornada);
                    guardarJornadasEnJSON(); // Guardar la lista actualizada como JSON

                    Intent intent = new Intent(Tacografo.this, ResumenJornadas.class);
                    ArrayList<Jornada> ultimaJornadaLista = new ArrayList<>();
                    ultimaJornadaLista.add(jornada); // Creamos una nueva lista con solo la última jornada
                    intent.putParcelableArrayListExtra("jornadas",ultimaJornadaLista);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                    // Desbloquear y restaurar la opacidad del botón Historial al finalizar la jornada
                    btHistorial.setEnabled(true);
                    btHistorial.setAlpha(1.0f); // Opacidad completa

                    btSalirTac.setEnabled(true);
                    btSalirTac.setAlpha(1.0f);

                    textViewConduccion.setText("00:00:00");
                    textViewDescanso.setText("00:00:00");
                    textViewOtros.setText("00:00:00");


                }
            }
        });
        // Configurar el listener para el botón de historial
        btHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Tacografo.this, HistorialJornadas.class);
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

    private void cargarJornadasDesdeJSON() {
        listaDeJornadas = new ArrayList<>();
        try (FileInputStream fis = openFileInput(FILENAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                listaDeJornadas.add(Jornada.fromJson(jsonObject));
            }
            Log.i("Tacografo", "Jornadas cargadas desde JSON.");
        } catch (IOException | JSONException e) {
            Log.e("Tacografo", "Error al cargar jornadas desde JSON o archivo no encontrado: " + e.getMessage());
        }
    }

    private void guardarJornadasEnJSON() {
        JSONArray jsonArray = new JSONArray();
        for (Jornada jornada : listaDeJornadas) {
            jsonArray.put(jornada.toJson());
        }
        String jsonString = jsonArray.toString();
        try (FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            fos.write(jsonString.getBytes());
            Log.i("Tacografo", "Jornadas guardadas en JSON.");
        } catch (IOException e) {
            Log.e("Tacografo", "Error al guardar jornadas en JSON: " + e.getMessage());
        }
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

        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("fechaInicio", this.fechaInicio);
                jsonObject.put("fechaFin", this.fechaFin);
                JSONArray ubicacionesJsonArray = new JSONArray();
                if (this.ubicaciones != null) {
                    for (Location location : this.ubicaciones) {
                        JSONObject locationJson = new JSONObject();
                        locationJson.put("latitude", location.getLatitude());
                        locationJson.put("longitude", location.getLongitude());
                        ubicacionesJsonArray.put(locationJson);
                    }
                }
                jsonObject.put("ubicaciones", ubicacionesJsonArray);
                jsonObject.put("tiempoConduccion", this.tiempoConduccion);
                jsonObject.put("tiempoOtrosTrabajos", this.tiempoOtrosTrabajos);
                jsonObject.put("tiempoDescanso", this.tiempoDescanso);
            } catch (JSONException e) {
                Log.e("Jornada", "Error al convertir a JSON: " + e.getMessage());
            }
            return jsonObject;
        }

        public static Jornada fromJson(JSONObject jsonObject) {
            try {
                String fechaInicio = jsonObject.getString("fechaInicio");
                String fechaFin = jsonObject.getString("fechaFin");
                JSONArray ubicacionesJsonArray = jsonObject.getJSONArray("ubicaciones");
                List<Location> ubicaciones = new ArrayList<>();
                for (int i = 0; i < ubicacionesJsonArray.length(); i++) {
                    JSONObject locationJson = ubicacionesJsonArray.getJSONObject(i);
                    Location location = new Location("");
                    location.setLatitude(locationJson.getDouble("latitude"));
                    location.setLongitude(locationJson.getDouble("longitude"));
                    ubicaciones.add(location);
                }
                long tiempoConduccion = jsonObject.getLong("tiempoConduccion");
                long tiempoOtrosTrabajos = jsonObject.getLong("tiempoOtrosTrabajos");
                long tiempoDescanso = jsonObject.getLong("tiempoDescanso");
                return new Jornada(fechaInicio, fechaFin, ubicaciones, tiempoConduccion, tiempoOtrosTrabajos, tiempoDescanso);
            } catch (JSONException e) {
                Log.e("Jornada", "Error al crear desde JSON: " + e.getMessage());
                return null;
            }
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