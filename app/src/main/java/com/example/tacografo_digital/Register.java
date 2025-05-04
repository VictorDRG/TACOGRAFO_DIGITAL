package com.example.tacografo_digital;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.Context;

public class Register extends AppCompatActivity {

    Button btAtras;
    EditText editTextUsuarioNuevo;
    EditText editTextTextContrasenaNueva;
    Button btRegistrarUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btAtras = (Button) findViewById(R.id.btAtras);
        editTextUsuarioNuevo = (EditText) findViewById(R.id.editTextUsuarioNuevo);
        editTextTextContrasenaNueva = (EditText) findViewById(R.id.editTextTextContrasenaNueva);
        btRegistrarUsuario = (Button) findViewById(R.id.btRegistrarUsuario);

        btAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Loggin.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        btRegistrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreUsuario = editTextUsuarioNuevo.getText().toString();
                String contraseña = editTextTextContrasenaNueva.getText().toString();

                SharedPreferences sharedPreferences = getSharedPreferences("usuarios", Context.MODE_PRIVATE);

                if (sharedPreferences.contains(nombreUsuario)) {
                    Toast.makeText(Register.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(nombreUsuario, contraseña);
                    editor.apply();

                    Toast.makeText(Register.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}