package com.example.tacografo_digital;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialJornadas extends AppCompatActivity {

    private RecyclerView recyclerViewJornadas; // RecyclerView para mostrar la lista de jornadas
    private JornadaAdapter jornadaAdapter; // Adaptador para el RecyclerView
    private List<Tacografo.Jornada> listaDeJornadas = new ArrayList<>(); // Lista para almacenar las jornadas cargadas
    private static final String FILENAME = "jornadas.json"; // Nombre del archivo JSON donde se guardan las jornadas
    private FloatingActionButton fabSalir; // Botón flotante para salir de la pantalla del historial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_jornadas);

        // Obtiene la referencia al RecyclerView desde el layout
        recyclerViewJornadas = findViewById(R.id.recyclerViewJornadas);
        // Establece el LayoutManager para el RecyclerView (en este caso, una lista vertical)
        recyclerViewJornadas.setLayoutManager(new LinearLayoutManager(this));

        // Obtiene la referencia al FloatingActionButton para salir
        fabSalir = findViewById(R.id.fabSalir);

        // Carga las jornadas guardadas desde el archivo JSON
        cargarJornadasDesdeJSON();
        // Crea una instancia del adaptador para el RecyclerView con la lista de jornadas
        jornadaAdapter = new JornadaAdapter(listaDeJornadas);
        // Establece el adaptador en el RecyclerView para mostrar los datos
        recyclerViewJornadas.setAdapter(jornadaAdapter);

        // Establece el OnClickListener para el FloatingActionButton de salir
        fabSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para volver a la Activity Tacografo
                Intent intent = new Intent(HistorialJornadas.this, Tacografo.class);
                // Inicia la Activity Tacografo
                startActivity(intent);
                finish(); // Finaliza la Activity actual para liber recursos
            }
        });
    }

    // Método para cargar las jornadas desde el archivo JSON
    private void cargarJornadasDesdeJSON() {
        listaDeJornadas.clear(); // Limpia la lista antes de cargar nuevas jornadas
        // Intenta abrir el archivo JSON y leer su contenido
        try (FileInputStream fis = openFileInput(FILENAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            // Lee cada línea del archivo y la añade al StringBuilder
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            // Crea un JSONArray a partir de la cadena JSON
            JSONArray jsonArray = new JSONArray(jsonString);
            // Itera sobre cada objeto JSON en el array y crea una instancia de Jornada
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                listaDeJornadas.add(Tacografo.Jornada.fromJson(jsonObject));
            }
            Log.i("HistorialJornadas", "Jornadas cargadas desde JSON.");
        } catch (IOException | JSONException e) {
            Log.e("HistorialJornadas", "Error al cargar jornadas desde JSON o archivo no encontrado: " + e.getMessage());
        }
    }

    // Método para guardar la lista de jornadas en el archivo JSON
    private void guardarJornadasEnJSON() {
        JSONArray jsonArray = new JSONArray();
        // Itera sobre la lista de jornadas y convierte cada una a un objeto JSON
        for (Tacografo.Jornada jornada : listaDeJornadas) {
            jsonArray.put(jornada.toJson());
        }
        String jsonString = jsonArray.toString();
        // Intenta abrir un FileOutputStream para escribir en el archivo especificado
        try (FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            fos.write(jsonString.getBytes()); // Escribe la cadena JSON en el archivo
            Log.i("HistorialJornadas", "Jornadas guardadas en JSON.");
        } catch (IOException e) {
            Log.e("HistorialJornadas", "Error al guardar jornadas en JSON: " + e.getMessage());
        }
    }

    // Método para formatear la parte de la hora de una fecha completa
    private String formatearHora(String fechaCompleta) {
        try {
            // Define el formato de la fecha completa
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            // Parsea la cadena de fecha completa a un objeto Date
            Date date = formatoCompleto.parse(fechaCompleta);
            // Define el formato para mostrar solo la hora
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            // Formatea la fecha para obtener solo la hora como una cadena
            return formatoHora.format(date);
        } catch (ParseException e) {
            Log.e("HistorialJornadas", "Error al formatear la hora: " + e.getMessage());
            return ""; // Devuelve una cadena vacía en caso de error
        }
    }

    // Adaptador para el RecyclerView que muestra la lista de jornadas
    private class JornadaAdapter extends RecyclerView.Adapter<JornadaAdapter.JornadaViewHolder> {

        private List<Tacografo.Jornada> jornadas; // Lista de jornadas a mostrar

        // Constructor del adaptador
        public JornadaAdapter(List<Tacografo.Jornada> jornadas) {
            this.jornadas = jornadas;
        }

        @NonNull
        @Override
        // Crea una nueva vista (invocado por el LayoutManager)
        public JornadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Infla el layout para un elemento de la lista desde item_jornada_historial.xml
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_jornada_historial, parent, false);
            // Crea y devuelve un nuevo ViewHolder que contiene la vista para ese elemento
            return new JornadaViewHolder(itemView);
        }

        @Override
        // Reemplaza el contenido de una vista (invocado por el LayoutManager)
        public void onBindViewHolder(@NonNull JornadaViewHolder holder, int position) {
            // Obtiene el elemento de datos (Jornada) en la posición especificada
            Tacografo.Jornada jornada = jornadas.get(position);
            // Establece los valores de los TextViews en el ViewHolder con los datos de la jornada
            holder.textViewFechaInicio.setText("Fecha Inicio: " + jornada.fechaInicio.split(" ")[0]); // Muestra solo la fecha
            holder.textViewHoraInicio.setText("Hora Inicio: " + formatearHora(jornada.fechaInicio)); // Muestra solo la hora
            holder.textViewHoraFin.setText("Hora Fin: " + formatearHora(jornada.fechaFin)); // Muestra solo la hora
            holder.textViewConduccion.setText("Conducción: " + formatearTiempo(jornada.tiempoConduccion));
            holder.textViewDescanso.setText("Descanso: " + formatearTiempo(jornada.tiempoDescanso));
            holder.textViewOtrosTrabajos.setText("Otros: " + formatearTiempo(jornada.tiempoOtrosTrabajos));
            holder.textViewFechaFin.setText("Fecha Fin: " + jornada.fechaFin.split(" ")[0]); // Muestra solo la fecha

            // Establece el OnClickListener para el botón "Ver Mapa"
            holder.buttonVerMapa.setOnClickListener(v -> {
                // Crea un Intent para iniciar la Activity DetalleMapaJornada
                Intent intent = new Intent(HistorialJornadas.this, DetalleMapaJornada.class);
                // Pasa la lista de ubicaciones de la jornada actual como un extra
                intent.putParcelableArrayListExtra("ubicaciones", (ArrayList<Location>) jornada.ubicaciones);
                // Inicia la Activity DetalleMapaJornada
                startActivity(intent);
            });

            // Establece el OnClickListener para el botón "Borrar Jornada"
            holder.buttonBorrarJornada.setOnClickListener(v -> {
                // Obtiene la posición del elemento en el adaptador
                int adapterPosition = holder.getAdapterPosition();
                // Verifica que la posición sea válida
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // Remueve la jornada de la lista
                    listaDeJornadas.remove(adapterPosition);
                    // Guarda la lista actualizada en el archivo JSON
                    guardarJornadasEnJSON();
                    // Notifica al adaptador que un elemento ha sido removido en esa posición
                    notifyItemRemoved(adapterPosition);
                }
            });
        }

        @Override
        // Devuelve el número total de elementos en el conjunto de datos
        public int getItemCount() {
            return jornadas.size();
        }

        // Clase interna que representa la vista de cada elemento de la lista
        public class JornadaViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewFechaInicio;
            public TextView textViewHoraInicio;
            public TextView textViewFechaFin;
            public TextView textViewHoraFin;
            public TextView textViewConduccion;
            public TextView textViewOtrosTrabajos;
            public TextView textViewDescanso;
            public Button buttonVerMapa;
            public Button buttonBorrarJornada;

            // Constructor del ViewHolder
            public JornadaViewHolder(@NonNull View itemView) {
                super(itemView);
                // Obtiene las referencias a las vistas dentro del layout del elemento
                textViewFechaInicio = itemView.findViewById(R.id.textViewFechaInicio);
                textViewHoraInicio = itemView.findViewById(R.id.textViewHoraInicio);
                textViewHoraFin = itemView.findViewById(R.id.textViewHoraFin);
                textViewConduccion = itemView.findViewById(R.id.textViewConduccion);
                textViewDescanso = itemView.findViewById(R.id.textViewDescanso);
                textViewOtrosTrabajos = itemView.findViewById(R.id.textViewOtrosTrabajos);
                textViewFechaFin = itemView.findViewById(R.id.textViewFechaFin);
                buttonVerMapa = itemView.findViewById(R.id.buttonVerMapa);
                buttonBorrarJornada = itemView.findViewById(R.id.buttonBorrarJornada);
            }
        }

        // Método privado para formatear el tiempo en HH:MM:SS
        private String formatearTiempo(long tiempoMillis) {
            long segundos = tiempoMillis / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos % 60, segundos % 60);
        }
    }
}