package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtCorreo;
    private EditText edtContrasena;
    private TextView txtMostrarContrasena;

    private boolean contrasenaVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtCorreo = findViewById(R.id.edtCorreoLogin);
        edtContrasena = findViewById(R.id.edtContrasenaLogin);
        txtMostrarContrasena =
                findViewById(R.id.txtMostrarContrasena);

        Button btnIniciarSesion =
                findViewById(R.id.btnIniciarSesion);

        Button btnGoogleLogin =
                findViewById(R.id.btnGoogleLogin);

        TextView txtOlvideContrasena =
                findViewById(R.id.txtOlvideContrasena);

        TextView txtRegistrate =
                findViewById(R.id.txtRegistrate);

        // Mostrar u ocultar la contraseña.
        txtMostrarContrasena.setOnClickListener(v -> {
            int posicion = edtContrasena.getSelectionStart();

            if (contrasenaVisible) {
                edtContrasena.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );
                contrasenaVisible = false;
            } else {
                edtContrasena.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );
                contrasenaVisible = true;
            }

            edtContrasena.setSelection(
                    Math.max(0, posicion)
            );
        });

        // Validación básica del formulario.
        btnIniciarSesion.setOnClickListener(v -> {
            String correo = edtCorreo.getText()
                    .toString().trim();

            String contrasena = edtContrasena.getText()
                    .toString();

            if (correo.isEmpty()) {
                edtCorreo.setError(
                        "Escribe tu correo electrónico"
                );
                edtCorreo.requestFocus();
                return;
            }

            if (contrasena.isEmpty()) {
                edtContrasena.setError(
                        "Escribe tu contraseña"
                );
                edtContrasena.requestFocus();
                return;
            }

            Toast.makeText(
                    this,
                    "La conexión de inicio de sesión está pendiente.",
                    Toast.LENGTH_LONG
            ).show();

            // Cuando configuremos la autenticación,
            // aquí se abrirá MainActivity al validar la cuenta.
        });

        // Estas pantallas se conectarán cuando las creemos.
        txtOlvideContrasena.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Próximamente: recuperar contraseña",
                        Toast.LENGTH_SHORT
                ).show()
        );

        txtRegistrate.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Próximamente: crear cuenta",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Google necesita configurar autenticación.
        btnGoogleLogin.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Primero debemos configurar el acceso con Google.",
                        Toast.LENGTH_LONG
                ).show()
        );
    }
}

