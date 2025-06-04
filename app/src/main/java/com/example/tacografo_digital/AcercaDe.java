package com.example.tacografo_digital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Button;

public class AcercaDe extends AppCompatActivity {

    // Declaración de los elementos de la interfaz de usuario
    private TextView twAcercaDe;   // TextView para mostrar la información "Acerca De"
    Button btSalirAcerca;         // Botón para volver a la pantalla de Loggin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca_de);

        // Obtiene referencias a los elementos de la interfaz de usuario mediante sus IDs
        twAcercaDe = (TextView) findViewById(R.id.twAcercaDe);
        btSalirAcerca = (Button) findViewById(R.id.btSalirAcerca);

        // Establece el texto para el TextView "Acerca De"
        twAcercaDe.setText("Proyecto realizado por" +
                "\nVíctor del Río Gago" +
                "\n con DNI: 71030778-Q" +
                "\n" +
                "\n Este proyecto se realiza con fines académicos como proyecto de fin del Ciclo Superior de DAM" +
                "\n(DESARROLLO DE APLICACIONES MULTIPLATAFORMA)" +
                "\n realizado en el centro FOC (FOMENTO OCUPACIONAL), en Granada");

        // Configura un OnClickListener para el botón "Salir"
        btSalirAcerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para iniciar la Activity Loggin
                Intent intent = new Intent(AcercaDe.this, Loggin.class);
                // Inicia la Activity Loggin
                startActivity(intent);
                // Desactiva la animación de transición entre actividades
                overridePendingTransition(0, 0);
            }
        });
    }
}