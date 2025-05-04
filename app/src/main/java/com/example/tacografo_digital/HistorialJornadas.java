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

    private RecyclerView recyclerViewJornadas;
    private JornadaAdapter jornadaAdapter;
    private List<Tacografo.Jornada> listaDeJornadas = new ArrayList<>();
    private static final String FILENAME = "jornadas.json";
    private FloatingActionButton fabSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_jornadas);

        recyclerViewJornadas = findViewById(R.id.recyclerViewJornadas);
        recyclerViewJornadas.setLayoutManager(new LinearLayoutManager(this));

        fabSalir = findViewById(R.id.fabSalir); // Obtener la referencia al FAB

        cargarJornadasDesdeJSON();
        jornadaAdapter = new JornadaAdapter(listaDeJornadas);
        recyclerViewJornadas.setAdapter(jornadaAdapter);

        // Establecer el OnClickListener para el FAB
        fabSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistorialJornadas.this, Tacografo.class); // Reemplaza ResumenJornadas.class con la Activity a la que quieres ir
                startActivity(intent);
                finish(); // Opcional: finalizar la Activity actual para que no quede en la pila de retroceso
            }
        });
    }

    private void cargarJornadasDesdeJSON() {
        listaDeJornadas.clear();
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
                listaDeJornadas.add(Tacografo.Jornada.fromJson(jsonObject));
            }
            Log.i("HistorialJornadas", "Jornadas cargadas desde JSON.");
        } catch (IOException | JSONException e) {
            Log.e("HistorialJornadas", "Error al cargar jornadas desde JSON o archivo no encontrado: " + e.getMessage());
        }
    }

    private void guardarJornadasEnJSON() {
        JSONArray jsonArray = new JSONArray();
        for (Tacografo.Jornada jornada : listaDeJornadas) {
            jsonArray.put(jornada.toJson());
        }
        String jsonString = jsonArray.toString();
        try (FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            fos.write(jsonString.getBytes());
            Log.i("HistorialJornadas", "Jornadas guardadas en JSON.");
        } catch (IOException e) {
            Log.e("HistorialJornadas", "Error al guardar jornadas en JSON: " + e.getMessage());
        }
    }

    private String formatearHora(String fechaCompleta) {
        try {
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = formatoCompleto.parse(fechaCompleta);
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            return formatoHora.format(date);
        } catch (ParseException e) {
            Log.e("HistorialJornadas", "Error al formatear la hora: " + e.getMessage());
            return "";
        }
    }

    // Adapter para el RecyclerView
    private class JornadaAdapter extends RecyclerView.Adapter<JornadaAdapter.JornadaViewHolder> {

        private List<Tacografo.Jornada> jornadas;

        public JornadaAdapter(List<Tacografo.Jornada> jornadas) {
            this.jornadas = jornadas;
        }

        @NonNull
        @Override
        public JornadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_jornada_historial, parent, false);
            return new JornadaViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull JornadaViewHolder holder, int position) {
            Tacografo.Jornada jornada = jornadas.get(position);
            holder.textViewFechaInicio.setText("Fecha Inicio: " + jornada.fechaInicio.split(" ")[0]);
            holder.textViewHoraInicio.setText("Hora Inicio: " + formatearHora(jornada.fechaInicio));
            holder.textViewHoraFin.setText("Hora Fin: " + formatearHora(jornada.fechaFin));
            holder.textViewConduccion.setText("Conducción: " + formatearTiempo(jornada.tiempoConduccion));
            holder.textViewDescanso.setText("Descanso: " + formatearTiempo(jornada.tiempoDescanso));
            holder.textViewOtrosTrabajos.setText("Otros: " + formatearTiempo(jornada.tiempoOtrosTrabajos));
            holder.textViewFechaFin.setText("Fecha Fin: " + jornada.fechaFin.split(" ")[0]);

            holder.buttonVerMapa.setOnClickListener(v -> {
                Intent intent = new Intent(HistorialJornadas.this, DetalleMapaJornada.class);
                intent.putParcelableArrayListExtra("ubicaciones", (ArrayList<Location>) jornada.ubicaciones);
                startActivity(intent);
            });

            holder.buttonBorrarJornada.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listaDeJornadas.remove(adapterPosition);
                    guardarJornadasEnJSON(); // Guardar la lista actualizada en el archivo
                    notifyItemRemoved(adapterPosition); // Notificar al adapter que un elemento ha sido removido
                }
            });
        }

        @Override
        public int getItemCount() {
            return jornadas.size();
        }

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

            public JornadaViewHolder(@NonNull View itemView) {
                super(itemView);
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

        private String formatearTiempo(long tiempoMillis) {
            long segundos = tiempoMillis / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos % 60, segundos % 60);
        }
    }
}