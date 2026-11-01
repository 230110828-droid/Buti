package com.itsoeh.sbsc.buti;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.itsoeh.sbsc.buti.datos.UsuarioRepositorio;

public class RecuperarContrasenaActivity extends AppCompatActivity {

    private LinearLayout layoutPaso1, layoutPaso2, layoutPaso3, layoutPaso4;

    // Paso 1
    private EditText edtCorreoRecuperar;
    private Button btnObtenerCodigo;

    // Paso 2
    private EditText edtCodigo1, edtCodigo2, edtCodigo3, edtCodigo4;
    private TextView txtMensajeCodigoEnviado, txtReenviarCodigo;
    private Button btnVerificarCodigo;

    // Paso 3
    private EditText edtNuevaContrasena, edtConfirmarNuevaContrasena;
    private Button btnGuardarContrasena;

    // Paso 4
    private Button btnIrIniciarSesion;

    private TextView txtRegresarLogin;

    private FirebaseAuth mAuth;
    private UsuarioRepositorio usuarioRepositorio;
    private String correoUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        mAuth = FirebaseAuth.getInstance();
        usuarioRepositorio = new UsuarioRepositorio(this);

        layoutPaso1 = findViewById(R.id.layoutPaso1);
        layoutPaso2 = findViewById(R.id.layoutPaso2);
        layoutPaso3 = findViewById(R.id.layoutPaso3);
        layoutPaso4 = findViewById(R.id.layoutPaso4);

        edtCorreoRecuperar = findViewById(R.id.edtCorreoRecuperar);
        btnObtenerCodigo = findViewById(R.id.btnObtenerCodigo);

        edtCodigo1 = findViewById(R.id.edtCodigo1);
        edtCodigo2 = findViewById(R.id.edtCodigo2);
        edtCodigo3 = findViewById(R.id.edtCodigo3);
        edtCodigo4 = findViewById(R.id.edtCodigo4);
        txtMensajeCodigoEnviado = findViewById(R.id.txtMensajeCodigoEnviado);
        txtReenviarCodigo = findViewById(R.id.txtReenviarCodigo);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo);

        edtNuevaContrasena = findViewById(R.id.edtNuevaContrasena);
        edtConfirmarNuevaContrasena = findViewById(R.id.edtConfirmarNuevaContrasena);
        btnGuardarContrasena = findViewById(R.id.btnGuardarContrasena);

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
        // Paso 1 -> Solicitar código
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

            btnObtenerCodigo.setEnabled(false);

            mAuth.sendPasswordResetEmail(correoUsuario)
                    .addOnCompleteListener(task -> {
                        btnObtenerCodigo.setEnabled(true);
                        txtMensajeCodigoEnviado.setText("Enviamos un código de 4 dígitos a " + correoUsuario);
                        layoutPaso1.setVisibility(View.GONE);
                        layoutPaso2.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Código enviado a tu correo", Toast.LENGTH_SHORT).show();
                    });
        });

        // Configuración auto-focus en código
        configurarFocusCodigo();

        // Paso 2 -> Verificar código
        btnVerificarCodigo.setOnClickListener(v -> {
            String c1 = edtCodigo1.getText().toString().trim();
            String c2 = edtCodigo2.getText().toString().trim();
            String c3 = edtCodigo3.getText().toString().trim();
            String c4 = edtCodigo4.getText().toString().trim();

            if (c1.isEmpty() || c2.isEmpty() || c3.isEmpty() || c4.isEmpty()) {
                Toast.makeText(this, "Ingresa el código completo de 4 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutPaso2.setVisibility(View.GONE);
            layoutPaso3.setVisibility(View.VISIBLE);
        });

        txtReenviarCodigo.setOnClickListener(v -> {
            if (!correoUsuario.isEmpty()) {
                mAuth.sendPasswordResetEmail(correoUsuario);
                Toast.makeText(this, "Código reenviado", Toast.LENGTH_SHORT).show();
            }
        });

        // Paso 3 -> Guardar contraseña
        btnGuardarContrasena.setOnClickListener(v -> {
            String pass = edtNuevaContrasena.getText().toString();
            String confirm = edtConfirmarNuevaContrasena.getText().toString();

            if (pass.length() < 8) {
                edtNuevaContrasena.setError("Debe tener al menos 8 caracteres");
                edtNuevaContrasena.requestFocus();
                return;
            }

            if (!pass.equals(confirm)) {
                edtConfirmarNuevaContrasena.setError("Las contraseñas no coinciden");
                edtConfirmarNuevaContrasena.requestFocus();
                return;
            }

            // Actualizar contraseña en base de datos local
            usuarioRepositorio.cambiarContrasena(correoUsuario, pass);

            layoutPaso3.setVisibility(View.GONE);
            layoutPaso4.setVisibility(View.VISIBLE);
        });

        // Paso 4 -> Iniciar sesión
        btnIrIniciarSesion.setOnClickListener(v -> finish());
        txtRegresarLogin.setOnClickListener(v -> finish());
    }

    private void configurarFocusCodigo() {
        edtCodigo1.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) edtCodigo2.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        edtCodigo2.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) edtCodigo3.requestFocus();
                else if (s.length() == 0) edtCodigo1.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        edtCodigo3.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) edtCodigo4.requestFocus();
                else if (s.length() == 0) edtCodigo2.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        edtCodigo4.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) edtCodigo3.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}