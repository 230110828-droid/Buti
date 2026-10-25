package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText edtCorreoLogin;
    private EditText edtContrasenaLogin;

    private TextView txtMostrarContrasena;
    private TextView txtOlvideContrasena;
    private TextView txtRegistrate;

    private Button btnIniciarSesion;
    private Button btnGoogleLogin;

    private FirebaseAuth mAuth;

    private boolean contrasenaVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Relacionar los componentes del XML
        edtCorreoLogin = findViewById(R.id.edtCorreoLogin);
        edtContrasenaLogin = findViewById(R.id.edtContrasenaLogin);

        txtMostrarContrasena = findViewById(R.id.txtMostrarContrasena);
        txtOlvideContrasena = findViewById(R.id.txtOlvideContrasena);
        txtRegistrate = findViewById(R.id.txtRegistrate);

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // Iniciar sesión
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Mostrar u ocultar contraseña
        txtMostrarContrasena.setOnClickListener(v ->
                cambiarVisibilidadContrasena());

        // Recuperar contraseña
        txtOlvideContrasena.setOnClickListener(v ->
                recuperarContrasena());

        // Ir al registro
        txtRegistrate.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegistroActivity.class
            );
            startActivity(intent);
        });
    }

    private void iniciarSesion() {

        String correo = edtCorreoLogin.getText().toString().trim();
        String contrasena =
                edtContrasenaLogin.getText().toString();

        // Validar correo
        if (TextUtils.isEmpty(correo)) {
            edtCorreoLogin.setError("Ingresa tu correo electrónico");
            edtCorreoLogin.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreoLogin.setError("Ingresa un correo válido");
            edtCorreoLogin.requestFocus();
            return;
        }

        // Validar contraseña
        if (TextUtils.isEmpty(contrasena)) {
            edtContrasenaLogin.setError("Ingresa tu contraseña");
            edtContrasenaLogin.requestFocus();
            return;
        }

        // Evitar pulsaciones repetidas durante la petición
        btnIniciarSesion.setEnabled(false);

        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {

                    btnIniciarSesion.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "¡Bienvenido a B'ÜTI!",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Abrir la pantalla principal
                        Intent intent = new Intent(
                                LoginActivity.this,
                                MainActivity.class
                        );

                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        String mensaje =
                                "No se pudo iniciar sesión. Verifica tu correo y contraseña.";

                        if (task.getException() != null) {
                            String error = task.getException()
                                    .getMessage();

                            if (error != null
                                    && error.toLowerCase().contains(
                                    "network")) {
                                mensaje =
                                        "Comprueba tu conexión a internet.";
                            }
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                mensaje,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void cambiarVisibilidadContrasena() {

        int posicion = edtContrasenaLogin.getSelectionStart();

        if (contrasenaVisible) {

            edtContrasenaLogin.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            txtMostrarContrasena.setText("Mostrar");
            contrasenaVisible = false;

        } else {

            edtContrasenaLogin.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            txtMostrarContrasena.setText("Ocultar");
            contrasenaVisible = true;
        }

        // Mantener la posición del cursor
        if (posicion >= 0) {
            edtContrasenaLogin.setSelection(
                    Math.min(
                            posicion,
                            edtContrasenaLogin.length()
                    )
            );
        }
    }

    private void recuperarContrasena() {

        String correo = edtCorreoLogin.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            edtCorreoLogin.setError(
                    "Escribe tu correo para recuperar la contraseña"
            );
            edtCorreoLogin.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreoLogin.setError("Ingresa un correo válido");
            edtCorreoLogin.requestFocus();
            return;
        }

        txtOlvideContrasena.setEnabled(false);

        mAuth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {

                    txtOlvideContrasena.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Si el correo corresponde a una cuenta, recibirás instrucciones para restablecer la contraseña.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "No se pudo procesar la solicitud. Revisa tu conexión e inténtalo de nuevo.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
