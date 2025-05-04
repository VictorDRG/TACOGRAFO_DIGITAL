package com.example.tacografo_digital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Button;

public class AcercaDe extends AppCompatActivity {

    private TextView twAcercaDe;
    Button btSalirAcerca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca_de);

        twAcercaDe = (TextView) findViewById(R.id.twAcercaDe);
        btSalirAcerca = (Button) findViewById(R.id.btSalirAcerca);

        twAcercaDe.setText("Proyecto realizado por"+
                "\nVíctor del Río Gago"+
                "\n con DNI: 71030778-Q"+
                "\n"+
                "\n Este proyecto se realiza con fines académicos como proyecto de fin del Ciclo Superior de DAM"+
                "\n(DESARROLLO DE APLICACIONES PLATAFORMA)"+
                "\n realizado en el centro de granad FOC (FOMENTO OCUPACIONAL)");

        btSalirAcerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AcercaDe.this, Loggin.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

}