package com.example.tacografo_digital;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.Context;

public class Loggin extends AppCompatActivity {

    Button btRegistrarse;
    Button btLoggin;
    EditText eTUsuario;
    EditText eTContrasena;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggin);

        btRegistrarse = (Button) findViewById(R.id.btRegistrarse);
        eTUsuario = (EditText) findViewById(R.id.eTUsuario);
        eTContrasena = (EditText) findViewById(R.id.eTContrasena);
        btLoggin = (Button) findViewById(R.id.btLoggin);


        btLoggin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreUsuario = eTUsuario.getText().toString();
                String contrasena = eTContrasena.getText().toString();

                SharedPreferences sharedPreferences = getSharedPreferences("usuarios", Context.MODE_PRIVATE);
                String contrasenaAlmacenada = sharedPreferences.getString(nombreUsuario, null);

                if (contrasenaAlmacenada != null && contrasenaAlmacenada.equals(contrasena)) {

                    // Inicio de sesión exitoso, inicia la siguiente Activity
                    Intent intent = new Intent(Loggin.this, Tacografo.class); // Reemplaza MainActivity.class con la Activity que deseas iniciar
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    Toast.makeText(Loggin.this, "Bienvenido "+nombreUsuario, Toast.LENGTH_SHORT).show();
                } else {
                    // Inicio de sesión fallido
                    Toast.makeText(Loggin.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Loggin.this, Register.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

}