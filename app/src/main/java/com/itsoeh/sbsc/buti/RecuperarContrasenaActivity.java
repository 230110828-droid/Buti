package com.itsoeh.sbsc.buti;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Recuperar contraseña con Firebase Authentication.
 *
 * Firebase envía un ENLACE (no un código) al correo.
 * La persona abre el enlace, escribe su nueva contraseña
 * en la página de Firebase y regresa a la app a iniciar sesión.
 */
public class RecuperarContrasenaActivity extends AppCompatActivity {

    private static final int SEGUNDOS_ESPERA_REENVIO = 60;

    private LinearLayout layoutPaso1, layoutPaso2, layoutPaso4;

    // Paso 1
    private EditText edtCorreoRecuperar;
    private Button btnObtenerCodigo;

    // Paso 2
    private TextView txtMensajeCodigoEnviado, txtReenviarCodigo;
    private Button btnVerificarCodigo;

    // Paso 4
    private Button btnIrIniciarSesion;

    private TextView txtRegresarLogin;

    private FirebaseAuth mAuth;
    private String correoUsuario = "";
    private CountDownTimer temporizadorReenvio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        mAuth = FirebaseAuth.getInstance();

        // El correo de Firebase llegará en español.
        mAuth.setLanguageCode("es");

        layoutPaso1 = findViewById(R.id.layoutPaso1);
        layoutPaso2 = findViewById(R.id.layoutPaso2);
        layoutPaso4 = findViewById(R.id.layoutPaso4);

        edtCorreoRecuperar = findViewById(R.id.edtCorreoRecuperar);
        btnObtenerCodigo = findViewById(R.id.btnObtenerCodigo);

        txtMensajeCodigoEnviado = findViewById(R.id.txtMensajeCodigoEnviado);
        txtReenviarCodigo = findViewById(R.id.txtReenviarCodigo);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo);

        btnIrIniciarSesion = findViewById(R.id.btnIrIniciarSesion);
        txtRegresarLogin = findViewById(R.id.txtRegresarLogin);

        // Pre-cargar correo si vino de LoginActivity
        String correoExtra = getIntent().getStringExtra("correo");
        if (!TextUtils.isEmpty(correoExtra)) {
            edtCorreoRecuperar.setText(correoExtra);
        }

        configurarPasos();
    }

    private void configurarPasos() {

        // Paso 1 -> Enviar enlace
        btnObtenerCodigo.setOnClickListener(v -> {

            correoUsuario = edtCorreoRecuperar.getText().toString().trim();

            if (TextUtils.isEmpty(correoUsuario)) {
                edtCorreoRecuperar.setError("Ingresa tu correo electrónico");
                edtCorreoRecuperar.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correoUsuario).matches()) {
                edtCorreoRecuperar.setError("Ingresa un correo válido");
                edtCorreoRecuperar.requestFocus();
                return;
            }

            enviarEnlace(true);
        });

        // Paso 2 -> Reenviar enlace
        txtReenviarCodigo.setOnClickListener(v -> {
            if (!correoUsuario.isEmpty()) {
                enviarEnlace(false);
            }
        });

        // Paso 2 -> Ya cambió su contraseña en la página de Firebase
        btnVerificarCodigo.setOnClickListener(v -> {
            layoutPaso2.setVisibility(View.GONE);
            layoutPaso4.setVisibility(View.VISIBLE);
        });

        // Paso 4 -> Volver a iniciar sesión
        btnIrIniciarSesion.setOnClickListener(v -> finish());
        txtRegresarLogin.setOnClickListener(v -> finish());
    }

    private void enviarEnlace(boolean primeraVez) {

        btnObtenerCodigo.setEnabled(false);
        txtReenviarCodigo.setEnabled(false);

        mAuth.sendPasswordResetEmail(correoUsuario)
                .addOnCompleteListener(task -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    btnObtenerCodigo.setEnabled(true);

                    if (!task.isSuccessful()) {

                        txtReenviarCodigo.setEnabled(true);

                        ButiAviso.mostrar(
                                this,
                                obtenerMensajeError(task.getException()),
                                ButiAviso.Tipo.ERROR
                        );
                        return;
                    }

                    // Por seguridad, Firebase no dice si el correo existe,
                    // así que el mensaje es el mismo en ambos casos.
                    txtMensajeCodigoEnviado.setText(
                            "Si " + correoUsuario + " está registrado en B'ÜTI, "
                                    + "te llegará un enlace para crear tu nueva contraseña."
                    );

                    if (primeraVez) {
                        layoutPaso1.setVisibility(View.GONE);
                        layoutPaso2.setVisibility(View.VISIBLE);
                    } else {
                        ButiAviso.mostrar(this, "Te enviamos el enlace otra vez.", ButiAviso.Tipo.EXITO);
                    }

                    iniciarEsperaReenvio();
                });
    }

    // Evita que se envíen muchos correos seguidos.
    private void iniciarEsperaReenvio() {

        if (temporizadorReenvio != null) {
            temporizadorReenvio.cancel();
        }

        txtReenviarCodigo.setEnabled(false);

        temporizadorReenvio = new CountDownTimer(
                SEGUNDOS_ESPERA_REENVIO * 1000L, 1000) {

            @Override
            public void onTick(long restante) {
                txtReenviarCodigo.setText(
                        "Reenviar enlace en " + (restante / 1000) + " s");
            }

            @Override
            public void onFinish() {
                txtReenviarCodigo.setText("Reenviar enlace");
                txtReenviarCodigo.setEnabled(true);
            }
        }.start();
    }

    private String obtenerMensajeError(Exception error) {

        String detalle = error == null || error.getMessage() == null
                ? ""
                : error.getMessage().toLowerCase();

        if (detalle.contains("network")) {
            return "Revisa tu conexión a internet e inténtalo de nuevo.";
        }

        if (detalle.contains("too-many-requests") || detalle.contains("blocked")) {
            return "Se hicieron demasiados intentos. Espera unos minutos.";
        }

        if (detalle.contains("invalid-email") || detalle.contains("badly formatted")) {
            return "El correo no es válido.";
        }

        return "No pudimos enviar el enlace. Inténtalo de nuevo.";
    }

    @Override
    protected void onDestroy() {
        if (temporizadorReenvio != null) {
            temporizadorReenvio.cancel();
        }
        super.onDestroy();
    }
}