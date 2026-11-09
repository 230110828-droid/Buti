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
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

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

    // Google (selector de cuentas del celular)
    private CredentialManager credentialManager;

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

        // Inicializar selector de cuentas de Google
        credentialManager = CredentialManager.create(this);

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

        // Continuar con Google
        btnGoogleLogin.setOnClickListener(v -> {

            LoginAnimador.animarPresion(v);
            iniciarSesionConGoogle();
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

                        // Mantener el correo actualizado en la base de datos
                        // (por si lo cambió desde Editar perfil).
                        FirebaseUser usuarioActual = mAuth.getCurrentUser();

                        if (usuarioActual != null && usuarioActual.getEmail() != null) {
                            FirebaseDatabase.getInstance()
                                    .getReference("usuarios")
                                    .child(usuarioActual.getUid())
                                    .child("correo")
                                    .setValue(usuarioActual.getEmail());
                        }

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
    // INICIAR SESIÓN CON GOOGLE
    // =========================================

    private void iniciarSesionConGoogle() {

        btnGoogleLogin.setEnabled(false);

        // Opción "Iniciar sesión con Google": siempre muestra
        // el selector con las cuentas Google del celular.
        GetSignInWithGoogleOption opcionGoogle =
                new GetSignInWithGoogleOption.Builder(
                        getString(R.string.default_web_client_id)
                ).build();

        GetCredentialRequest solicitud =
                new GetCredentialRequest.Builder()
                        .addCredentialOption(opcionGoogle)
                        .build();

        credentialManager.getCredentialAsync(
                this,
                solicitud,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse respuesta) {
                        procesarCuentaGoogle(respuesta.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException error) {

                        btnGoogleLogin.setEnabled(true);

                        // La persona cerró el selector: no mostramos error.
                        if (error instanceof GetCredentialCancellationException) {
                            return;
                        }

                        android.util.Log.e(
                                "LoginActivity",
                                "Error con Google: " + error.getMessage(),
                                error
                        );

                        if (error instanceof NoCredentialException) {

                            butiDialogo.mostrarError(
                                    "No encontramos cuentas de Google",
                                    "Agrega una cuenta de Google en tu celular "
                                            + "(Ajustes > Cuentas) e inténtalo de nuevo."
                            );
                            return;
                        }

                        butiDialogo.mostrarError(
                                "No pudimos conectar con Google",
                                "Revisa tu conexión a internet e inténtalo nuevamente."
                        );
                    }
                }
        );
    }

    // Recibe la cuenta elegida y entra a Firebase con ella.
    private void procesarCuentaGoogle(Credential credencial) {

        if (!(credencial instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(credencial.getType())) {

            btnGoogleLogin.setEnabled(true);

            butiDialogo.mostrarError(
                    "No pudimos conectar con Google",
                    "La cuenta seleccionada no es válida. Inténtalo nuevamente."
            );
            return;
        }

        GoogleIdTokenCredential cuentaGoogle =
                GoogleIdTokenCredential.createFrom(
                        ((CustomCredential) credencial).getData()
                );

        AuthCredential credencialFirebase =
                GoogleAuthProvider.getCredential(
                        cuentaGoogle.getIdToken(),
                        null
                );

        butiDialogo.mostrarCargando(
                "¡Un momento!",
                "Conectando tu cuenta de Google con B'ÜTI..."
        );

        mAuth.signInWithCredential(credencialFirebase)
                .addOnCompleteListener(this, task -> {

                    if (isFinishing() || isDestroyed()) {
                        butiDialogo.cerrar();
                        return;
                    }

                    if (!task.isSuccessful() || mAuth.getCurrentUser() == null) {

                        btnGoogleLogin.setEnabled(true);

                        android.util.Log.e(
                                "LoginActivity",
                                "Error al entrar a Firebase con Google",
                                task.getException()
                        );

                        butiDialogo.mostrarError(
                                "No pudimos iniciar sesión",
                                obtenerMensajeError(task.getException())
                        );
                        return;
                    }

                    revisarSiEsNuevo(mAuth.getCurrentUser());
                });
    }

    // Si ya tiene datos en la base de datos entra directo;
    // si es nuevo, pasa a las preguntas de bienvenida.
    private void revisarSiEsNuevo(FirebaseUser usuario) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .get()
                .addOnCompleteListener(task -> {

                    btnGoogleLogin.setEnabled(true);

                    if (isFinishing() || isDestroyed()) {
                        butiDialogo.cerrar();
                        return;
                    }

                    DataSnapshot datos = task.isSuccessful()
                            ? task.getResult()
                            : null;

                    boolean yaRegistrado = datos != null
                            && datos.child("nombre").exists();

                    if (yaRegistrado) {

                        butiDialogo.mostrarExito(
                                "¡Qué alegría tenerte de vuelta!",
                                "Has iniciado sesión con Google.\n\n"
                                        + "Tu camino para aprender hñähñu "
                                        + "continúa aquí.",
                                this::abrirPantallaPrincipal
                        );

                    } else {

                        butiDialogo.cerrar();

                        Intent intent = new Intent(
                                LoginActivity.this,
                                GoogleOnboardingActivity.class
                        );

                        startActivity(intent);
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