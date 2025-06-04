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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.widget.Toast;

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
    Button btIniciarConduccion; // Botón para iniciar/detener el contador de conducción
    Button btIniciarOtros;      // Botón para iniciar/detener el contador de otros trabajos
    Button btIniciarDescanso;    // Botón para iniciar/detener el contador de descanso
    Button btHistorial;         // Botón para ir a la pantalla del historial de jornadas
    Button btSalirTac;          // Botón para volver a la pantalla de Loggin
    Button btInicioJornada;     // Botón para iniciar/finalizar la jornada laboral

    // TextViews para mostrar el tiempo transcurrido
    private TextView textViewConduccion; // TextView para mostrar el tiempo de conducción
    private TextView textViewOtros;      // TextView para mostrar el tiempo de otros trabajos
    private TextView textViewDescanso;   // TextView para mostrar el tiempo de descanso

    // TextViews para los letreros de los botones de contador
    private TextView twLetreroConduccion; // TextView para el letrero del botón de conducción
    private TextView twLetreroDescanso;  // TextView para el letrero del botón de descanso
    private TextView twLetreroOtros;     // TextView para el letrero del botón de otros trabajos

    // Variables para el seguimiento del estado y el tiempo
    private boolean iniciarJornada = false;   // Indica si la jornada laboral está activa
    private boolean conduccionActiva = false; // Indica si el contador de conducción está activo
    private boolean otrosActivos = false;      // Indica si el contador de otros trabajos está activo
    private boolean descansoActiva = false;   // Indica si el contador de descanso está activo
    private long tiempoConduccion = 0;        // Tiempo acumulado de conducción en milisegundos
    private long tiempoOtrosTrabajos = 0;     // Tiempo acumulado de otros trabajos en milisegundos
    private long tiempoDescanso = 0;          // Tiempo acumulado de descanso en milisegundos
    private Location ultimaUbicacion = null;   // Última ubicación GPS obtenida (actualmente no se usa directamente)
    private final Handler handler = new Handler(); // Handler para la ejecución de tareas en el hilo principal con retardo
    private Runnable actualizarTiempoRunnable; // Runnable para actualizar los TextViews de tiempo periódicamente
    private String fechaInicioJornada;       // Marca de tiempo del inicio de la jornada
    private String fechaFinJornada;         // Marca de tiempo del fin de la jornada
    private List<Jornada> listaDeJornadas = new ArrayList<>(); // Lista para almacenar las jornadas laborales
    private LocationManager locationManager; // Servicio del sistema para acceder a la ubicación
    private FusedLocationProviderClient fusedLocationClient; // Cliente para obtener la ubicación de manera más eficiente
    private LocationRequest locationRequest; // Objeto para configurar la solicitud de ubicación
    private LocationCallback locationCallback; // Callback para recibir los resultados de la solicitud de ubicación
    private List<Location> listaDeUbicaciones = new ArrayList<>(); // Lista para almacenar las ubicaciones GPS durante la jornada
    private static final String FILENAME = "jornadas.json"; // Nombre del archivo JSON para guardar las jornadas

    // VARIABLES PARA EL CONTROL DE DESCANSO OBLIGATORIO Y FIN DE JORNADA
    private long tiempoConduccionDesdeUltimoDescanso = 0; // Tiempo de conducción acumulado desde el último descanso reglamentario
    private long tiempoDescansoAcumuladoEnPeriodo = 0;   // Tiempo de descanso acumulado en el período de conducción actual
    private boolean descansoRequeridoAlertado = false;   // Aviso para evitar alertar repetidamente sobre el mismo descanso
    private boolean descansoCumplidoAlertado = false;    // Aviso para evitar alertar repetidamente sobre el descanso cumplido
    private boolean finJornadaAlertado = false;          // Aviso para evitar alertar repetidamente sobre el fin de jornada

    // VARIABLES CON TIEMPOS OBLIGATORIOS SEGUN CODIGO CIRCULACION
    private static final long LIMITE_CONDUCCION_MILLIS = 1 * 60 * 1000; // 1 minuto en milisegundos
    private static final long DURACION_DESCANSO_REQUERIDO_MILLIS = 30 * 1000; // 30 segundos en milisegundos
    private static final long LIMITE_JORNADA_MILLIS = 3 * 60 * 1000; // 3 minutos en milisegundos (Conducción + Descanso + Otros)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tacografo);

        // Se inicializan los botones de los contadores obteniendo sus referencias por ID
        btIniciarConduccion = findViewById(R.id.btConduccion);
        btIniciarOtros = findViewById(R.id.btOtros);
        btIniciarDescanso = findViewById(R.id.btDescanso);
        btHistorial = findViewById(R.id.btHistorial);
        btSalirTac = findViewById(R.id.btSalirTac);
        btInicioJornada = findViewById(R.id.btInicioJornada);
        twLetreroConduccion = findViewById(R.id.twLetreroConduccion);
        twLetreroDescanso = findViewById(R.id.twLetreroDescanso);
        twLetreroOtros = findViewById(R.id.twLetreroOtros);

        // Se inicializan los TextViews para mostrar el tiempo obteniendo sus referencias por ID
        textViewConduccion = findViewById(R.id.textViewConduccion);
        textViewOtros = findViewById(R.id.textViewOtros);
        textViewDescanso = findViewById(R.id.textViewDescanso);

        // Se obtiene el servicio de LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Se verifica y solicitan permisos de ubicación si no están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // No se inician las actualizaciones de ubicación hasta que se inicie la jornada
        }

        // Se inicializa el cliente de ubicación fusionada de Google Play Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest(); // Configurar la solicitud de ubicación
        createLocationCallback(); // Configurar el callback para recibir los resultados de la ubicación

        // OnClickListeners para los botones de los contadores
        btIniciarConduccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Solo permitir la acción si la jornada ha sido iniciada
                if (iniciarJornada) {
                    // Si ningún contador está activo, iniciar el de conducción
                    if (!conduccionActiva && !otrosActivos && !descansoActiva) {
                        conduccionActiva = true;
                        btIniciarConduccion.setText("DETENER CONDUCCIÓN");
                        twLetreroConduccion.setTextColor(Color.parseColor("#37ff21")); // Cambiar letrero de Conducción a verde
                        btIniciarDescanso.setAlpha(0.5f); // Reducir opacidad de otros botones
                        btIniciarOtros.setAlpha(0.5f);
                        descansoCumplidoAlertado = false; // Resetear la bandera de descanso cumplido al iniciar conducción
                    }
                    // Si el contador de conducción está activo, detenerlo, cambiar de vuelta el color y el botón
                    else if (conduccionActiva) {
                        conduccionActiva = false;
                        btIniciarConduccion.setText("CONDUCCIÓN");
                        twLetreroConduccion.setTextColor(Color.parseColor("#FF8F00")); // Cambiar color a naranja
                        btIniciarDescanso.setAlpha(1.0f); // Restaurar opacidad de otros botones
                        btIniciarOtros.setAlpha(1.0f);
                    }
                }
            }
        });

        btIniciarOtros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Solo permitir la acción si la jornada ha sido iniciada
                if (iniciarJornada) {
                    // Si ningún contador está activo, iniciar el de otros trabajos
                    if (!conduccionActiva && !otrosActivos && !descansoActiva) {
                        otrosActivos = true;
                        btIniciarOtros.setText("DETENER OTROS");
                        twLetreroOtros.setTextColor(Color.parseColor("#37ff21")); // Cambiar color a verde
                        btIniciarDescanso.setAlpha(0.5f); // Reducir opacidad de otros botones
                        btIniciarConduccion.setAlpha(0.5f);
                    }
                    // Si el contador de otros trabajos está activo, detenerlo
                    else if (otrosActivos) {
                        otrosActivos = false;
                        btIniciarOtros.setText("OTROS");
                        twLetreroOtros.setTextColor(Color.parseColor("#FF8F00")); // Cambiar color a naranja
                        btIniciarDescanso.setAlpha(1.0f); // Restaurar opacidad de otros botones
                        btIniciarConduccion.setAlpha(1.0f);
                    }
                }
            }
        });

        btIniciarDescanso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Solo permitir la acción si la jornada ha sido iniciada
                if (iniciarJornada) {
                    // Si ningún contador está activo, iniciar el de descanso
                    if (!conduccionActiva && !otrosActivos && !descansoActiva) {
                        descansoActiva = true;
                        btIniciarDescanso.setText("DETENER DESCANSO");
                        twLetreroDescanso.setTextColor(Color.parseColor("#37ff21")); // Cambiar color a verde
                        btIniciarConduccion.setAlpha(0.5f); // Reducir opacidad de otros botones
                        btIniciarOtros.setAlpha(0.5f);
                    }
                    // Si el contador de descanso está activo, detenerlo
                    else if (descansoActiva) {
                        descansoActiva = false;
                        btIniciarDescanso.setText("DESCANSO");
                        twLetreroDescanso.setTextColor(Color.parseColor("#FF8F00")); // Cambiar color a naranja
                        btIniciarConduccion.setAlpha(1.0f); // Restaurar opacidad de otros botones
                        btIniciarOtros.setAlpha(1.0f);

                        // Ahora solo verificamos si el descanso fue insuficiente al detenerlo,
                        // si ya se cumplió el aviso se dio automáticamente.
                        if (tiempoDescansoAcumuladoEnPeriodo < DURACION_DESCANSO_REQUERIDO_MILLIS && !descansoCumplidoAlertado) {
                            Toast.makeText(Tacografo.this, "Descanso insuficiente. Necesitas al menos " +
                                    (DURACION_DESCANSO_REQUERIDO_MILLIS / 1000) + " segundos de descanso.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        // Se carga la lista de jornadas desde el archivo JSON al iniciar la Activity
        cargarJornadasDesdeJSON();

        // OnClickListener para el botón de inicio/fin de jornada
        btInicioJornada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si la jornada no está iniciada, iniciarla
                if (!iniciarJornada) {
                    iniciarJornada = true;
                    btInicioJornada.setText("FINALIZAR JORNADA");
                    listaDeUbicaciones.clear(); // Limpia la lista de ubicaciones al iniciar una nueva jornada
                    fechaInicioJornada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()); // Obtener la fecha y hora de inicio

                    // Si los permisos de ubicación están concedidos, comenzar a obtener actualizaciones y el temporizador
                    if (ContextCompat.checkSelfPermission(Tacografo.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startLocationUpdates(); // Inicia las actualizaciones de ubicación
                        handler.postDelayed(actualizarTiempoRunnable, 1000); // Inicia el runnable para actualizar los tiempos cada segundo
                    } else {
                        // Si no hay permisos, solicitarlos
                        ActivityCompat.requestPermissions(Tacografo.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    // Reinicia los contadores de tiempo al iniciar una nueva jornada
                    tiempoConduccion = 0;
                    tiempoOtrosTrabajos = 0;
                    tiempoDescanso = 0;
                    tiempoConduccionDesdeUltimoDescanso = 0;
                    tiempoDescansoAcumuladoEnPeriodo = 0;
                    descansoRequeridoAlertado = false;
                    descansoCumplidoAlertado = false;
                    finJornadaAlertado = false;

                    actualizarTextViews(); // Actualiza la interfaz con los tiempos reiniciados
                    // Reestablece el texto de los botones de contador
                    btIniciarConduccion.setText("CONDUCCIÓN");
                    btIniciarOtros.setText("OTROS");
                    btIniciarDescanso.setText("DESCANSO");
                    conduccionActiva = false;
                    otrosActivos = false;
                    descansoActiva = false;

                    // Bloquea y hace semi-transparente el botón Historial durante la jornada
                    btHistorial.setEnabled(false);
                    btHistorial.setAlpha(0.5f);

                    // Bloquea y hacer semi-transparente el botón Salir durante la jornada
                    btSalirTac.setEnabled(false);
                    btSalirTac.setAlpha(0.5f);
                }
                // Si la jornada está iniciada, finalizarla
                else {
                    iniciarJornada = false;
                    btInicioJornada.setText("INICIAR JORNADA");
                    handler.removeCallbacks(actualizarTiempoRunnable); // Detiene el runnable de actualización de tiempo
                    stopLocationUpdates(); // Detiene las actualizaciones de ubicación
                    fechaFinJornada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()); // Obtiene la fecha y hora de fin

                    // Crea un objeto Jornada con los datos recopilados
                    Jornada jornada = new Jornada(fechaInicioJornada, fechaFinJornada, new ArrayList<>(listaDeUbicaciones), tiempoConduccion, tiempoOtrosTrabajos, tiempoDescanso);
                    listaDeJornadas.add(jornada); // Añadir la jornada a la lista
                    guardarJornadasEnJSON(); // Guardar la lista de jornadas actualizada en el archivo JSON

                    // Crea un Intent para ir a la pantalla de ResumenJornadas y pasar la última jornada
                    Intent intent = new Intent(Tacografo.this, ResumenJornadas.class);
                    ArrayList<Jornada> ultimaJornadaLista = new ArrayList<>();
                    ultimaJornadaLista.add(jornada); // Crea una nueva lista que contiene solo la última jornada
                    intent.putParcelableArrayListExtra("jornadas", ultimaJornadaLista);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                    // Desbloquea y restaura la opacidad del botón Historial al finalizar la jornada
                    btHistorial.setEnabled(true);
                    btHistorial.setAlpha(1.0f);

                    // Desbloquea y restaura la opacidad del botón Salir al finalizar la jornada
                    btSalirTac.setEnabled(true);
                    btSalirTac.setAlpha(1.0f);

                    // Reinicia la visualización de los tiempos a 00:00:00
                    textViewConduccion.setText("00:00:00");
                    textViewDescanso.setText("00:00:00");
                    textViewOtros.setText("00:00:00");

                    // REINICIA VARIABLES DE CONTROL DE DESCANSO Y JORNADA AL FINALIZAR JORNADA
                    tiempoConduccionDesdeUltimoDescanso = 0;
                    tiempoDescansoAcumuladoEnPeriodo = 0;
                    descansoRequeridoAlertado = false;
                    descansoCumplidoAlertado = false;
                    finJornadaAlertado = false;
                }
            }
        });

        // OnClickListener para el botón de historial
        btHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para iniciar la Activity HistorialJornadas
                Intent intent = new Intent(Tacografo.this, HistorialJornadas.class);
                // Inicia la Activity HistorialJornadas
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // OnClickListener para el botón de salir
        btSalirTac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para iniciar la Activity Loggin
                Intent intent = new Intent(Tacografo.this, Loggin.class);
                // Inicia la Activity Loggin
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // Runnable que se ejecuta cada segundo para actualizar los TextViews de tiempo
        actualizarTiempoRunnable = new Runnable() {
            @Override
            public void run() {
                // Solo actualizar si la jornada está activa
                if (iniciarJornada) {
                    // Incrementar el tiempo del contador activo
                    if (conduccionActiva) {
                        tiempoConduccion += 1000;
                        tiempoConduccionDesdeUltimoDescanso += 1000; // Incrementa el tiempo de conducción desde el último descanso
                        tiempoDescansoAcumuladoEnPeriodo = 0; // Si estás conduciendo, el descanso acumulado en el período se reinicia

                        // COMPROBAR LA REGLA DE LÍMITE DE CONDUCCIÓN
                        // La alerta solo se muestra si se ha superado el límite y no ha sido alertado previamente
                        if (tiempoConduccionDesdeUltimoDescanso >= LIMITE_CONDUCCION_MILLIS && !descansoRequeridoAlertado) {
                            mostrarAlertaDescansoRequerido();
                            descansoRequeridoAlertado = true; // Activar el asviso para no mostrar la alerta repetidamente
                        }

                    } else if (descansoActiva) {
                        tiempoDescanso += 1000;
                        tiempoDescansoAcumuladoEnPeriodo += 1000; // Acumula tiempo de descanso en el período

                        // Comprueba si el descanso obligatorio se ha cumplido y aún no se ha alertado
                        if (tiempoDescansoAcumuladoEnPeriodo >= DURACION_DESCANSO_REQUERIDO_MILLIS && !descansoCumplidoAlertado) {
                            tiempoConduccionDesdeUltimoDescanso = 0; // Reiniciar el contador de conducción
                            descansoRequeridoAlertado = false; // Resetea el aviso de alerta de "descanso requerido"
                            Toast.makeText(Tacografo.this, "¡Descanso completado! Puedes reanudar la conducción.", Toast.LENGTH_LONG).show();
                            descansoCumplidoAlertado = true; // Establece el aviso para evitar repeticiones de alerta
                        }

                    } else if (otrosActivos) {
                        tiempoOtrosTrabajos += 1000;
                        // Otros trabajos no contribuyen a la conducción ni al descanso para esta regla
                    }

                    // Si se supera el tiempo total de Jornada, solo se permite finalizar la misma
                    long tiempoTotalJornada = tiempoConduccion + tiempoDescanso + tiempoOtrosTrabajos;
                    if (tiempoTotalJornada >= LIMITE_JORNADA_MILLIS && !finJornadaAlertado) {
                        mostrarAlertaFinJornada();
                        finJornadaAlertado = true; // Establecer el aviso para evitar repeticiones
                        conduccionActiva = false;
                        otrosActivos = false;
                        descansoActiva = false;
                        btIniciarConduccion.setText("CONDUCCIÓN");
                        btIniciarOtros.setText("OTROS");
                        btIniciarDescanso.setText("DESCANSO");
                        twLetreroConduccion.setTextColor(Color.parseColor("#FF8F00"));
                        twLetreroDescanso.setTextColor(Color.parseColor("#FF8F00"));
                        twLetreroOtros.setTextColor(Color.parseColor("#FF8F00"));
                        btIniciarConduccion.setAlpha(1.0f);
                        btIniciarOtros.setAlpha(1.0f);
                        btIniciarDescanso.setAlpha(1.0f);


                        btIniciarConduccion.setEnabled(false);
                        btIniciarOtros.setEnabled(false);
                        btIniciarDescanso.setEnabled(false);
                    }

                    actualizarTextViews(); // Actualiza la interfaz de usuario con los nuevos tiempos
                }
                handler.postDelayed(this, 1000); // Programa la próxima ejecución del Runnable después de 1 segundo
            }
        };
    }


    // Método para guardar la lista de jornadas en un archivo JSON
    private void guardarJornadasEnJSON() {
        JSONArray jsonArray = new JSONArray();
        // Itera sobre la lista de jornadas y convierte cada una a un objeto JSON
        for (Jornada jornada : listaDeJornadas) {
            jsonArray.put(jornada.toJson());
        }
        String jsonString = jsonArray.toString();
        // Intenta abrir un FileOutputStream para escribir en el archivo especificado
        try (FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            fos.write(jsonString.getBytes()); // Escribe la cadena JSON en el archivo
            Log.i("Tacografo", "Jornadas guardadas en JSON.");
        } catch (IOException e) {
            Log.e("Tacografo", "Error al guardar jornadas en JSON: " + e.getMessage());
        }
    }

    // Método para cargar la lista de jornadas desde el archivo JSON
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

    // Método para actualizar los TextViews que muestran el tiempo en la interfaz de usuario
    private void actualizarTextViews() {
        // Ejecuta la actualización de la interfaz de usuario en el hilo principal
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Formatea el tiempo acumulado y lo establece en los TextViews correspondientes
                textViewConduccion.setText(formatearTiempo(tiempoConduccion));
                textViewOtros.setText(formatearTiempo(tiempoOtrosTrabajos));
                textViewDescanso.setText(formatearTiempo(tiempoDescanso));
            }
        });
    }

    // Método para crear y configurar la solicitud de ubicación
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        // Ajustado a un intervalo razonable para un tacógrafo si la conducción está activa
        locationRequest.setInterval(5000); // 5 segundos
        locationRequest.setFastestInterval(3000); // No menos de 3 segundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Establece la prioridad de la solicitud en alta precisión (usa GPS)
    }

    private Location currentLocation; // Variable para almacenar la ubicación actual

    // Método para crear el callback que recibe los resultados de la solicitud de ubicación
    private LocationCallback createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // Verifica si el resultado de la ubicación no es nulo
                if (locationResult == null) {
                    return;
                }
                // Itera sobre la lista de ubicaciones obtenidas en el resultado
                for (Location location : locationResult.getLocations()) {
                    // Solo guarda ubicación si la jornada está iniciada Y el contador de conducción está activo
                    if (location != null && iniciarJornada && conduccionActiva) {
                        currentLocation = location; // Guarda la ubicación actual
                        Log.d("UbicacionGuardada", "Latitud: " + currentLocation.getLatitude() + ", Longitud: " + currentLocation.getLongitude());
                        listaDeUbicaciones.add(currentLocation); // Añade la ubicación a la lista de ubicaciones de la jornada
                        // Si es la primera ubicación, la establece como la última conocida
                        if (ultimaUbicacion == null) {
                            ultimaUbicacion = location;
                        }
                    }
                }
            }
        };
        return locationCallback; // Retorna el callback creado
    }

    // Método para comenzar a recibir actualizaciones de ubicación
    private void startLocationUpdates() {
        try {
            // Verifica si se tienen los permisos de ubicación precisos
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Solicita las actualizaciones de ubicación al FusedLocationProviderClient
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        } catch (SecurityException e) { // Cambiado a SecurityException para ser más específico
            Log.e("Tacografo", "SecurityException al iniciar updates de ubicación: " + e.getMessage());
        }
    }

    // Método para detener la recepción de actualizaciones de ubicación
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates(); // Detiene las actualizaciones de ubicación cuando la actividad pasa a segundo plano
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si la jornada está iniciada y se tienen los permisos, vuelve a iniciar las actualizaciones al volver a la actividad
        if (iniciarJornada && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
            // También reanudar el handler si es necesario, aunque el handler.postDelayed se encargará de ello
            // si el runnable ya estaba programado. Es buena práctica revisar el estado.
            if (actualizarTiempoRunnable != null) {
                handler.removeCallbacks(actualizarTiempoRunnable); // Eliminar cualquier callback pendiente duplicado
                handler.postDelayed(actualizarTiempoRunnable, 1000); // Reprogramar
            }
        }
    }

    // Método para manejar la respuesta a la solicitud de permisos en tiempo de ejecución
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Verifica si el código de solicitud coincide con el de la solicitud de ubicación
        if (requestCode == 1) {
            // Verifica si se concedieron permisos y si la lista de resultados no está vacía y el primer resultado es PERMISSION_GRANTED
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si la jornada ya estaba iniciada cuando se solicitaron los permisos, comienza las actualizaciones y el temporizador
                if (iniciarJornada) {
                    startLocationUpdates();
                    handler.postDelayed(actualizarTiempoRunnable, 1000);
                }
            } else {
                Toast.makeText(this, "Permisos de ubicación denegados. Algunas funcionalidades no estarán disponibles.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Este callback no es necesario cuando se usa FusedLocationProviderClient con LocationCallback
    }

    // Método para formatear el tiempo en formato HH:MM:SS
    private String formatearTiempo(long tiempoMillis) {
        long segundos = tiempoMillis / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos % 60, segundos % 60);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Callback para cambios en el estado del proveedor de ubicación (no se usa directamente)
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Callback cuando el proveedor de ubicación está habilitado (no se usa directamente)
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Callback cuando el proveedor de ubicación está deshabilitado (no se usa directamente)
    }


    // MÉTODO PARA MOSTRAR LA ALERTA DE DESCANSO REQUERIDO
    private void mostrarAlertaDescansoRequerido() {
        // Ajusta el mensaje de la alerta para mostrar segundos o minutos
        String limiteConduccionStr;
        if (LIMITE_CONDUCCION_MILLIS < 60 * 1000) {
            limiteConduccionStr = (LIMITE_CONDUCCION_MILLIS / 1000) + " segundo" + (LIMITE_CONDUCCION_MILLIS / 1000 == 1 ? "" : "s");
        } else {
            limiteConduccionStr = (LIMITE_CONDUCCION_MILLIS / (60 * 1000)) + " minuto" + (LIMITE_CONDUCCION_MILLIS / (60 * 1000) == 1 ? "" : "s");
        }

        String duracionDescansoStr;
        if (DURACION_DESCANSO_REQUERIDO_MILLIS < 60 * 1000) {
            duracionDescansoStr = (DURACION_DESCANSO_REQUERIDO_MILLIS / 1000) + " segundo" + (DURACION_DESCANSO_REQUERIDO_MILLIS / 1000 == 1 ? "" : "s");
        } else {
            duracionDescansoStr = (DURACION_DESCANSO_REQUERIDO_MILLIS / (60 * 1000)) + " minuto" + (DURACION_DESCANSO_REQUERIDO_MILLIS / (60 * 1000) == 1 ? "" : "s");
        }

        new AlertDialog.Builder(this)
                .setTitle("¡ATENCIÓN: DESCANSO REQUERIDO!")
                .setMessage("Has acumulado " + limiteConduccionStr + " de conducción. Debes tomar un descanso de al menos " + duracionDescansoStr + ".")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    // MÉTODO PARA MOSTRAR LA ALERTA DE FIN DE JORNADA
    private void mostrarAlertaFinJornada() {
        String limiteJornadaStr;
        if (LIMITE_JORNADA_MILLIS < 60 * 1000) {
            limiteJornadaStr = (LIMITE_JORNADA_MILLIS / 1000) + " segundo" + (LIMITE_JORNADA_MILLIS / 1000 == 1 ? "" : "s");
        } else {
            limiteJornadaStr = (LIMITE_JORNADA_MILLIS / (60 * 1000)) + " minuto" + (LIMITE_JORNADA_MILLIS / (60 * 1000) == 1 ? "" : "s");
        }

        new AlertDialog.Builder(this)
                .setTitle("¡FIN DE JORNADA LABORAL!")
                .setMessage("Has alcanzado el límite de " + limiteJornadaStr + " para esta jornada (suma de conducción, descanso y otros trabajos). Debes finalizar la jornada.")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }


    // Clase interna estática que implementa Parcelable para representar una jornada laboral
    public static class Jornada implements android.os.Parcelable {
        public String fechaInicio; // Fecha y hora de inicio de la jornada
        public String fechaFin;    // Fecha y hora de fin de la jornada
        public List<Location> ubicaciones; // Lista de ubicaciones registradas durante la jornada
        public long tiempoConduccion;    // Tiempo total de conducción en milisegundos
        public long tiempoOtrosTrabajos; // Tiempo total de otros trabajos en milisegundos
        public long tiempoDescanso;      // Tiempo total de descanso en milisegundos

        // Constructor de la clase Jornada
        public Jornada(String fechaInicio, String fechaFin, List<Location> ubicaciones, long tiempoConduccion, long tiempoOtrosTrabajos, long tiempoDescanso) {
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.ubicaciones = ubicaciones;
            this.tiempoConduccion = tiempoConduccion;
            this.tiempoOtrosTrabajos = tiempoOtrosTrabajos;
            this.tiempoDescanso = tiempoDescanso;
        }

        // Constructor usado para recrear el objeto desde un Parcel (para que esté disponible desde otros componentes)
        protected Jornada(android.os.Parcel in) {
            fechaInicio = in.readString();
            fechaFin = in.readString();
            ubicaciones = in.createTypedArrayList(Location.CREATOR); // Lee la lista de ubicaciones
            tiempoConduccion = in.readLong();
            tiempoOtrosTrabajos = in.readLong();
            tiempoDescanso = in.readLong();
        }

        // Convierte el objeto Jornada a un JSONObject (para guardar en JSON)
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

        // Crea un objeto Jornada desde un JSONObject (para cargar desde JSON)
        public static Jornada fromJson(JSONObject jsonObject) {
            try {
                String fechaInicio = jsonObject.getString("fechaInicio");
                String fechaFin = jsonObject.getString("fechaFin");
                JSONArray ubicacionesJsonArray = jsonObject.getJSONArray("ubicaciones");
                List<Location> ubicaciones = new ArrayList<>();
                for (int i = 0; i < ubicacionesJsonArray.length(); i++) {
                    JSONObject locationJson = ubicacionesJsonArray.getJSONObject(i);
                    Location location = new Location(""); // Creamos un objeto Location
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

        // Objeto CREATOR requerido por Parcelable para recrear objetos
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

        // Método requerido por Parcelable, indica si hay objetos especiales
        @Override
        public int describeContents() {
            return 0; // No hay objetos especiales
        }

        // Escribe los datos del objeto en un Parcel para su serialización
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