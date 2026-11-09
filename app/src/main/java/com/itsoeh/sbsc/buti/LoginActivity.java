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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.admin.AdminActivity;

public class LoginActivity extends AppCompatActivity {

    // Campos
    private EditText edtCorreoLogin;
    private EditText edtContrasenaLogin;

    // Controles
    private ImageView txtMostrarContrasena;
    private TextView txtOlvideContrasena;
    private TextView txtRegistrate;

    private Button btnIniciarSesion;
    private Button btnGoogleLogin;

    // Firebase
    private FirebaseAuth mAuth;

    // Estado de contraseña
    private boolean contrasenaVisible = false;

    // Animaciones
    private LoginAnimador loginAnimador;

    // Mensajes personalizados
    private ButiDialogo butiDialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inicializar animadores
        loginAnimador = new LoginAnimador();

        // Inicializar mensajes personalizados
        butiDialogo = new ButiDialogo(this);

        // Relacionar campos
        edtCorreoLogin = findViewById(R.id.edtCorreoLogin);
        edtContrasenaLogin = findViewById(R.id.edtContrasenaLogin);

        // Relacionar controles
        txtMostrarContrasena = findViewById(
                R.id.txtMostrarContrasena
        );

        txtOlvideContrasena = findViewById(
                R.id.txtOlvideContrasena
        );

        txtRegistrate = findViewById(
                R.id.txtRegistrate
        );

        btnIniciarSesion = findViewById(
                R.id.btnIniciarSesion
        );

        btnGoogleLogin = findViewById(
                R.id.btnGoogleLogin
        );

        // Reproducir animaciones de entrada
        reproducirAnimaciones();

        // Iniciar sesión
        btnIniciarSesion.setOnClickListener(v -> {

            LoginAnimador.animarPresion(v);
            iniciarSesion();
        });

        // Mostrar u ocultar contraseña
        txtMostrarContrasena.setOnClickListener(v -> {

            LoginAnimador.animarPresion(v);
            cambiarVisibilidadContrasena();
        });

        // Recuperar contraseña
        txtOlvideContrasena.setOnClickListener(v ->
                recuperarContrasena()
        );

        // Continuar con Google / Onboarding
        btnGoogleLogin.setOnClickListener(v -> {

            LoginAnimador.animarPresion(v);

            Intent intent = new Intent(
                    LoginActivity.this,
                    GoogleOnboardingActivity.class
            );

            startActivity(intent);
        });

        // Ir al registro
        txtRegistrate.setOnClickListener(v -> {

            LoginAnimador.animarPresion(v);

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegistroActivity.class
            );

            startActivity(intent);
        });
    }

    // =========================================
    // ANIMACIONES DE ENTRADA
    // =========================================

    private void reproducirAnimaciones() {

        loginAnimador.reproducir(

                findViewById(R.id.imgLogoLogin),
                findViewById(R.id.tvNombreLogin),
                findViewById(R.id.tvBienvenidaLogin),

                findViewById(R.id.cardLogin),
                findViewById(R.id.tvTituloLogin),
                findViewById(R.id.tvDescripcionLogin),

                findViewById(R.id.contenedorCorreoLogin),
                findViewById(R.id.contenedorContrasenaLogin),

                findViewById(R.id.txtOlvideContrasena),
                findViewById(R.id.btnIniciarSesion),

                findViewById(R.id.separadorLogin),
                findViewById(R.id.btnGoogleLogin),
                findViewById(R.id.contenedorRegistroLogin)
        );
    }

    // =========================================
    // INICIAR SESIÓN CON FIREBASE
    // =========================================

    private void iniciarSesion() {

        String correo = edtCorreoLogin.getText()
                .toString()
                .trim();

        String contrasena = edtContrasenaLogin.getText()
                .toString();

        // Validar correo vacío
        if (TextUtils.isEmpty(correo)) {

            edtCorreoLogin.setError(
                    "Ingresa tu correo electrónico"
            );

            edtCorreoLogin.requestFocus();
            return;
        }

        // Validar formato de correo
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            edtCorreoLogin.setError(
                    "Ingresa un correo válido"
            );

            edtCorreoLogin.requestFocus();
            return;
        }

        // Validar contraseña vacía
        if (TextUtils.isEmpty(contrasena)) {

            edtContrasenaLogin.setError(
                    "Ingresa tu contraseña"
            );

            edtContrasenaLogin.requestFocus();
            return;
        }

        // Evitar pulsaciones repetidas
        btnIniciarSesion.setEnabled(false);

        // Mostrar mensaje de carga personalizado
        butiDialogo.mostrarCargando(
                "¡Un momento!",
                "Estamos preparando tu camino para aprender hñähñu.\n\n"
                        + "Verificando tus datos..."
        );

        // Verificar cuenta con Firebase
        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {

                    btnIniciarSesion.setEnabled(true);

                    if (isFinishing() || isDestroyed()) {
                        butiDialogo.cerrar();
                        return;
                    }

                    if (task.isSuccessful()) {

                        // Mensaje personalizado de bienvenida
                        butiDialogo.mostrarExito(
                                "¡Qué alegría tenerte de vuelta!",
                                "Has iniciado sesión correctamente.\n\n"
                                        + "Tu camino para aprender hñähñu "
                                        + "y descubrir nuestra cultura "
                                        + "continúa aquí.",
                                this::abrirPantallaPrincipal
                        );

                    } else {

                        String mensaje = obtenerMensajeError(
                                task.getException()
                        );

                        butiDialogo.mostrarError(
                                "No pudimos iniciar sesión",
                                mensaje
                        );
                    }
                });
    }

    // =========================================
    // MENSAJES DE ERROR
    // =========================================

    private String obtenerMensajeError(Exception error) {

        if (error == null) {

            return "Ocurrió un problema inesperado. "
                    + "Inténtalo nuevamente.";
        }

        String codigo = error.getMessage() == null
                ? ""
                : error.getMessage().toLowerCase();

        if (codigo.contains("network")) {

            return "Parece que no tienes una conexión estable.\n\n"
                    + "Revisa tu internet e inténtalo nuevamente.";
        }

        if (codigo.contains("user-not-found")
                || codigo.contains("invalid-credential")
                || codigo.contains("wrong-password")
                || codigo.contains("invalid-login-credentials")) {

            return "El correo o la contraseña no son correctos.\n\n"
                    + "Verifica tus datos e inténtalo nuevamente.";
        }

        if (codigo.contains("too-many-requests")) {

            return "Se han realizado demasiados intentos.\n\n"
                    + "Espera un momento antes de volver a intentarlo.";
        }

        if (codigo.contains("user-disabled")) {

            return "Esta cuenta se encuentra deshabilitada.\n\n"
                    + "Comunícate con el administrador.";
        }

        return "No pudimos verificar tus datos en este momento.\n\n"
                + "Inténtalo nuevamente.";
    }

    // =========================================
    // ABRIR PANTALLA PRINCIPAL
    // =========================================

    private void abrirPantallaPrincipal() {

        String correoAdmin1 = "yelitzagranados002@gmail.com";
        String correoAdmin2 = "admin@buti.com";
        String correoIngresado = "";

        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioActual != null && usuarioActual.getEmail() != null) {
            correoIngresado = usuarioActual.getEmail().trim();
        } else if (edtCorreoLogin != null) {
            correoIngresado = edtCorreoLogin.getText().toString().trim();
        }

        boolean esAdmin = !correoIngresado.isEmpty() && (
                correoIngresado.equalsIgnoreCase(correoAdmin1) ||
                correoIngresado.equalsIgnoreCase(correoAdmin2)
        );

        if (esAdmin) {
            Intent intent = new Intent(
                    LoginActivity.this,
                    AdminActivity.class
            );
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
            return;
        }

        // Si es usuario normal, verificar si el sistema está en modo mantenimiento
        verificarMantenimientoYNavegar();
    }

    private void verificarMantenimientoYNavegar() {
        // Doble verificación por seguridad: si es admin, nunca mandar a mantenimiento
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioActual != null && usuarioActual.getEmail() != null) {
            String email = usuarioActual.getEmail().trim();
            if (email.equalsIgnoreCase("yelitzagranados002@gmail.com") || email.equalsIgnoreCase("admin@buti.com")) {
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        boolean mantenimientoLocal = getSharedPreferences("buti_prefs", MODE_PRIVATE)
                .getBoolean("modo_mantenimiento", false);

        if (mantenimientoLocal) {
            navegarAMantenimiento();
            return;
        }

        FirebaseDatabase.getInstance().getReference("config/mantenimiento")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean enMantenimiento = snapshot.getValue(Boolean.class);
                        if (enMantenimiento != null && enMantenimiento) {
                            getSharedPreferences("buti_prefs", MODE_PRIVATE)
                                    .edit().putBoolean("modo_mantenimiento", true).apply();
                            navegarAMantenimiento();
                        } else {
                            navegarAMainNormal();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        navegarAMainNormal();
                    }
                });
    }

    private void navegarAMantenimiento() {
        Intent intent = new Intent(LoginActivity.this, MantenimientoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navegarAMainNormal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // =========================================
    // MOSTRAR U OCULTAR CONTRASEÑA
    // =========================================

    private void cambiarVisibilidadContrasena() {

        int posicion = edtContrasenaLogin.getSelectionStart();

        if (contrasenaVisible) {

            // Ocultar contraseña
            edtContrasenaLogin.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            txtMostrarContrasena.setImageResource(
                    R.drawable.ic_ojocerrado
            );

            contrasenaVisible = false;

        } else {

            // Mostrar contraseña
            edtContrasenaLogin.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            txtMostrarContrasena.setImageResource(
                    R.drawable.ic_ojoabierto
            );

            contrasenaVisible = true;
        }

        // Mantener posición del cursor
        if (posicion >= 0) {

            edtContrasenaLogin.setSelection(
                    Math.min(
                            posicion,
                            edtContrasenaLogin.length()
                    )
            );
        }
    }

    // =========================================
    // RECUPERAR CONTRASEÑA
    // =========================================

    private void recuperarContrasena() {

        String correo = edtCorreoLogin.getText()
                .toString()
                .trim();

        Intent intent = new Intent(
                LoginActivity.this,
                RecuperarContrasenaActivity.class
        );

        if (!correo.isEmpty()) {
            intent.putExtra("correo", correo);
        }

        startActivity(intent);
    }

    // =========================================
    // DETENER ANIMACIONES
    // =========================================

    @Override
    protected void onDestroy() {

        if (loginAnimador != null) {
            loginAnimador.detener();
        }

        if (butiDialogo != null) {
            butiDialogo.cerrar();
        }

        super.onDestroy();
    }
}
