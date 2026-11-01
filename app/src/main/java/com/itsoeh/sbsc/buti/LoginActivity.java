package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText edtCorreoLogin;
    private EditText edtContrasenaLogin;

    // Ahora es una imagen (ojo abierto / ojo cerrado)
    private ImageView txtMostrarContrasena;
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
        txtOlvideContrasena.setOnClickListener(v -> recuperarContrasena());

        // Continuar con Google / Onboarding
        btnGoogleLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    GoogleOnboardingActivity.class
            );
            startActivity(intent);
        });

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

            // Ocultar contraseña: se muestra el ojo abierto
            edtContrasenaLogin.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            txtMostrarContrasena.setImageResource(R.drawable.ic_ojoabierto);
            contrasenaVisible = false;

        } else {

            // Mostrar contraseña: se muestra el ojo cerrado
            edtContrasenaLogin.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            txtMostrarContrasena.setImageResource(R.drawable.ic_ojocerrado);
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
        Intent intent = new Intent(
                LoginActivity.this,
                RecuperarContrasenaActivity.class
        );
        if (!correo.isEmpty()) {
            intent.putExtra("correo", correo);
        }
        startActivity(intent);
    }
}