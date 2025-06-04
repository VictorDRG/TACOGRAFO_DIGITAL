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

    // Declaración de los elementos de la interfaz de usuario
    Button btAtras;                // Botón para volver a la pantalla de Loggin
    EditText editTextUsuarioNuevo; // Campo de texto para ingresar el nuevo nombre de usuario
    EditText editTextTextContrasenaNueva; // Campo de texto para ingresar la nueva contraseña
    Button btRegistrarUsuario;     // Botón para registrar el nuevo usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Obtiene referencias a los elementos de la interfaz de usuario mediante sus IDs
        btAtras = (Button) findViewById(R.id.btAtras);
        editTextUsuarioNuevo = (EditText) findViewById(R.id.editTextUsuarioNuevo);
        editTextTextContrasenaNueva = (EditText) findViewById(R.id.editTextTextContrasenaNueva);
        btRegistrarUsuario = (Button) findViewById(R.id.btRegistrarUsuario);


        // OnClickListener para el botón "Registrar Usuario"
        btRegistrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtiene el nuevo nombre de usuario y la contraseña ingresados por el usuario
                String nombreUsuario = editTextUsuarioNuevo.getText().toString();
                String contraseña = editTextTextContrasenaNueva.getText().toString();

                // Obtiene una instancia de SharedPreferences para acceder a los datos de usuario guardados
                SharedPreferences sharedPreferences = getSharedPreferences("usuarios", Context.MODE_PRIVATE);

                // Verifica si el nombre de usuario ya existe en SharedPreferences
                if (sharedPreferences.contains(nombreUsuario)) {
                    // Si el usuario ya existe, muestra un mensaje
                    Toast.makeText(Register.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                } else {
                    // Si el usuario no existe, crea un Editor para guardar el nuevo usuario y contraseña
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    // Guarda el nombre de usuario como clave y la contraseña como valor
                    editor.putString(nombreUsuario, contraseña);
                    // Aplica los cambios de forma asíncrona
                    editor.apply();

                    // Muestra un mensaje de éxito al registrar el usuario
                    Toast.makeText(Register.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // OnClickListener para el botón "Atrás"
        btAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para iniciar la Activity Loggin
                Intent intent = new Intent(Register.this, Loggin.class);
                // Inicia la Activity Loggin
                startActivity(intent);
                // Desactiva la animación de transición entre actividades
                overridePendingTransition(0, 0);
            }
        });
    }
}