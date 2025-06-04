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

    // Declaración de los elementos de la interfaz de usuario
    Button btRegistrarse; // Botón para ir a la pantalla de registro
    Button btLoggin;      // Botón para intentar iniciar sesión
    EditText eTUsuario;   // Campo de texto para ingresar el nombre de usuario
    EditText eTContrasena;// Campo de texto para ingresar la contraseña
    Button btAcercaDe;   // Botón para ir a la pantalla "Acerca De"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggin);

        // Obtiene referencias a los elementos de la interfaz de usuario mediante sus IDs
        eTUsuario = (EditText) findViewById(R.id.eTUsuario);
        eTContrasena = (EditText) findViewById(R.id.eTContrasena);
        btLoggin = (Button) findViewById(R.id.btLoggin);
        btRegistrarse = (Button) findViewById(R.id.btRegistrarse);
        btAcercaDe = (Button) findViewById(R.id.btAcercaDe);

        // Configura un OnClickListener para el botón de Loggin
        btLoggin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtiene el nombre de usuario y la contraseña ingresados por el usuario
                String nombreUsuario = eTUsuario.getText().toString();
                String contrasena = eTContrasena.getText().toString();

                // Obtiene una instancia de SharedPreferences para acceder a los datos de usuario guardados
                SharedPreferences sharedPreferences = getSharedPreferences("usuarios", Context.MODE_PRIVATE);
                // Busca la contraseña almacenada para el nombre de usuario ingresado.
                // Si no se encuentra el usuario, contrasenaAlmacenada será null.
                String contrasenaAlmacenada = sharedPreferences.getString(nombreUsuario, null);

                // Verifica si se encontró una contraseña almacenada para el usuario
                // y si coincide con la contraseña ingresada
                if (contrasenaAlmacenada != null && contrasenaAlmacenada.equals(contrasena)) {

                    // Si el inicio de sesión es correcto crea un Intent para iniciar la Activity Tacografo
                    Intent intent = new Intent(Loggin.this, Tacografo.class);
                    // Inicia la Activity
                    startActivity(intent);
                    // Desactiva la animación de transición entre actividades
                    overridePendingTransition(0, 0);
                    // Muestra un mensaje de bienvenida al usuario mediante un TOAST
                    Toast.makeText(Loggin.this, "Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();
                } else {
                    // Si el inicio de sesión es erróneo muestra un mensaje de error al usuario
                    Toast.makeText(Loggin.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configura un OnClickListener para el botón de Registrarse
        btRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para iniciar la Activity Register
                Intent intent = new Intent(Loggin.this, Register.class);
                // Inicia la Activity
                startActivity(intent);
                // Desactiva la animación de transición entre actividades
                overridePendingTransition(0, 0);
            }
        });

        // Configura un OnClickListener para el botón de Acerca De
        btAcercaDe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para iniciar la Activity AcercaDe
                Intent intent = new Intent(Loggin.this, AcercaDe.class);
                // Inicia la Activity
                startActivity(intent);
                // Desactiva la animación de transición entre actividades
                overridePendingTransition(0, 0);
            }
        });
    }
}