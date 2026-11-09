package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private static final String TAG = "REGISTRO_FIREBASE";

    private EditText edtNombre;
    private EditText edtCorreo;
    private TextInputEditText edtContrasena;
    private TextInputEditText edtConfirmarContrasena;
    private CheckBox chkTerminos;

    private TextView txtNivelContrasena;
    private TextView txtReqLongitud;
    private TextView txtReqMayuscula;
    private TextView txtReqMinuscula;
    private TextView txtReqNumero;
    private TextView txtReqEspecial;

    private ProgressBar barraSeguridad;
    private Button btnCrearCuenta;
    private TextView txtIniciarSesion;

    private FirebaseAuth mAuth;
    private DatabaseReference referenciaUsuarios;

    private boolean longitudValida;
    private boolean mayusculaValida;
    private boolean minusculaValida;
    private boolean numeroValido;
    private boolean especialValido;

    // Animaciones
    private RegistroAnimador registroAnimador;

    // Mensajes personalizados
    private ButiDialogo butiDialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        referenciaUsuarios = FirebaseDatabase.getInstance()
                .getReference("usuarios");

        // Inicializar animador
        registroAnimador = new RegistroAnimador();

        // Inicializar mensajes personalizados
        butiDialogo = new ButiDialogo(this);

        // Campos del formulario
        edtNombre = findViewById(R.id.edtNombreRegistro);
        edtCorreo = findViewById(R.id.edtCorreoRegistro);

        edtContrasena = findViewById(
                R.id.edtContrasenaRegistro
        );

        edtConfirmarContrasena = findViewById(
                R.id.edtConfirmarContrasenaRegistro
        );

        chkTerminos = findViewById(R.id.chkTerminos);

        btnCrearCuenta = findViewById(
                R.id.btnCrearCuentaRegistro
        );

        txtIniciarSesion = findViewById(
                R.id.txtIniciarSesionRegistro
        );

        // Seguridad de contraseña
        txtNivelContrasena = findViewById(
                R.id.txtNivelContrasena
        );

        barraSeguridad = findViewById(
                R.id.barraSeguridad
        );

        txtReqLongitud = findViewById(
                R.id.txtReqLongitud
        );

        txtReqMayuscula = findViewById(
                R.id.txtReqMayuscula
        );

        txtReqMinuscula = findViewById(
                R.id.txtReqMinuscula
        );

        txtReqNumero = findViewById(
                R.id.txtReqNumero
        );

        txtReqEspecial = findViewById(
                R.id.txtReqEspecial
        );

        // Reproducir animaciones de entrada
        reproducirAnimaciones();

        // Observar cambios en la contraseña
        edtContrasena.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                actualizarSeguridad(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        actualizarSeguridad("");

        // Crear cuenta
        btnCrearCuenta.setOnClickListener(v -> {
            RegistroAnimador.animarPresion(v);
            crearCuenta();
        });

        // Regresar al inicio de sesión
        txtIniciarSesion.setOnClickListener(v -> {
            RegistroAnimador.animarPresion(v);
            finish();
        });
    }

    // ------------------------------------------------
    // ANIMACIONES DE LA PANTALLA
    // ------------------------------------------------

    private void reproducirAnimaciones() {

        View imgLogo = findViewById(R.id.imgLogoRegistro);
        View tvNombre = findViewById(R.id.tvNombreRegistro);
        View tvTitulo = findViewById(R.id.tvTituloRegistro);

        View contenedorSubtitulo = findViewById(
                R.id.contenedorSubtituloRegistro
        );

        View tvSubtitulo = findViewById(
                R.id.tvSubtituloRegistro
        );

        View cardRegistro = findViewById(
                R.id.cardRegistro
        );

        View contenedorNombre = findViewById(
                R.id.contenedorNombreRegistro
        );

        View contenedorCorreo = findViewById(
                R.id.contenedorCorreoRegistro
        );

        View contenedorContrasena = findViewById(
                R.id.contenedorContrasenaRegistro
        );

        View cardSeguridad = findViewById(
                R.id.cardSeguridadRegistro
        );

        View contenedorConfirmar = findViewById(
                R.id.contenedorConfirmarContrasenaRegistro
        );

        View contenedorLogin = findViewById(
                R.id.contenedorLoginRegistro
        );

        View tvFrase = findViewById(
                R.id.tvFraseRegistro
        );

        registroAnimador.reproducir(
                imgLogo,
                tvNombre,
                tvTitulo,
                contenedorSubtitulo,
                tvSubtitulo,
                cardRegistro,
                contenedorNombre,
                contenedorCorreo,
                contenedorContrasena,
                cardSeguridad,
                contenedorConfirmar,
                chkTerminos,
                btnCrearCuenta,
                contenedorLogin,
                tvFrase
        );
    }

    // ------------------------------------------------
    // SEGURIDAD DE CONTRASEÑA
    // ------------------------------------------------

    private void actualizarSeguridad(String contrasena) {

        longitudValida = contrasena.length() >= 8;

        mayusculaValida = contrasena.matches(
                "(?s).*\\p{Lu}.*"
        );

        minusculaValida = contrasena.matches(
                "(?s).*\\p{Ll}.*"
        );

        numeroValido = contrasena.matches(
                "(?s).*\\d.*"
        );

        especialValido = contrasena.matches(
                "(?s).*[\\p{P}\\p{S}].*"
        );

        actualizarRequisito(
                txtReqLongitud,
                longitudValida,
                "Mínimo 8 caracteres"
        );

        actualizarRequisito(
                txtReqMayuscula,
                mayusculaValida,
                "Una letra mayúscula (A-Z)"
        );

        actualizarRequisito(
                txtReqMinuscula,
                minusculaValida,
                "Una letra minúscula (a-z)"
        );

        actualizarRequisito(
                txtReqNumero,
                numeroValido,
                "Un número (0-9)"
        );

        actualizarRequisito(
                txtReqEspecial,
                especialValido,
                "Un carácter especial (!@#$...)"
        );

        int puntos = 0;

        if (longitudValida) puntos++;
        if (mayusculaValida) puntos++;
        if (minusculaValida) puntos++;
        if (numeroValido) puntos++;
        if (especialValido) puntos++;

        barraSeguridad.setProgress(puntos);

        int color;
        String nivel;

        // Colores de la paleta B'ÜTI (cambian solos en modo oscuro)
        if (contrasena.isEmpty()) {

            nivel = "Seguridad de la contraseña";
            color = ContextCompat.getColor(this, R.color.buti_texto);

        } else if (puntos <= 2) {

            nivel = "Seguridad: débil";
            color = ContextCompat.getColor(this, R.color.buti_rosa);

        } else if (puntos <= 4) {

            nivel = "Seguridad: media";
            color = ContextCompat.getColor(this, R.color.buti_naranja);

        } else {

            nivel = "Seguridad: fuerte";
            color = ContextCompat.getColor(this, R.color.buti_exito);
        }

        txtNivelContrasena.setText(nivel);
        txtNivelContrasena.setTextColor(color);

        barraSeguridad.setProgressTintList(
                ColorStateList.valueOf(color)
        );
    }

    private void actualizarRequisito(
            TextView vista,
            boolean cumplido,
            String texto
    ) {

        vista.setText(texto);

        int color = ContextCompat.getColor(
                this,
                cumplido ? R.color.buti_exito : R.color.buti_texto_secundario
        );

        // Icono real en lugar de los símbolos de texto
        vista.setCompoundDrawablesRelativeWithIntrinsicBounds(
                cumplido ? R.drawable.ic_req_ok : R.drawable.ic_req_pendiente,
                0, 0, 0
        );

        vista.setCompoundDrawableTintList(ColorStateList.valueOf(color));
        vista.setTextColor(color);
    }

    private boolean contrasenaSegura() {

        return longitudValida
                && mayusculaValida
                && minusculaValida
                && numeroValido
                && especialValido;
    }

    // ------------------------------------------------
    // CREAR CUENTA
    // ------------------------------------------------

    private void crearCuenta() {

        String nombre = edtNombre.getText().toString().trim();

        String correo = edtCorreo.getText().toString().trim();

        String contrasena = edtContrasena.getText().toString();

        String confirmar = edtConfirmarContrasena.getText().toString();

        if (nombre.isEmpty()) {

            edtNombre.setError("Escribe tu nombre completo");
            edtNombre.requestFocus();
            return;
        }

        if (correo.isEmpty()) {

            edtCorreo.setError("Escribe tu correo electrónico");
            edtCorreo.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            edtCorreo.setError("Escribe un correo válido");
            edtCorreo.requestFocus();
            return;
        }

        if (!contrasenaSegura()) {

            edtContrasena.setError(
                    "Completa todos los requisitos de seguridad"
            );

            edtContrasena.requestFocus();

            ButiAviso.mostrar(
                    this,
                    "Tu contraseña debe cumplir los 5 requisitos.",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        if (!contrasena.equals(confirmar)) {

            edtConfirmarContrasena.setError(
                    "Las contraseñas no coinciden"
            );

            edtConfirmarContrasena.requestFocus();
            return;
        }

        if (!chkTerminos.isChecked()) {

            ButiAviso.mostrar(
                    this,
                    "Debes aceptar los términos y condiciones.",
                    ButiAviso.Tipo.INFO
            );

            return;
        }

        btnCrearCuenta.setEnabled(false);

        butiDialogo.mostrarCargando(
                "Creando tu cuenta",
                "Estamos preparando tu camino para aprender hñähñu..."
        );

        mAuth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {

                        btnCrearCuenta.setEnabled(true);

                        Exception error = task.getException();

                        String mensaje = obtenerMensajeError(error);

                        Log.e(
                                TAG,
                                "Error al crear la cuenta. Correo: " + correo,
                                error
                        );

                        butiDialogo.mostrarError(
                                "No pudimos crear tu cuenta",
                                mensaje
                        );

                        return;
                    }

                    FirebaseUser usuario = mAuth.getCurrentUser();

                    if (usuario == null) {

                        btnCrearCuenta.setEnabled(true);

                        Log.e(
                                TAG,
                                "La autenticación fue exitosa, "
                                        + "pero getCurrentUser() devolvió null."
                        );

                        butiDialogo.mostrarError(
                                "Algo salió mal",
                                "Tu cuenta se creó, pero no pudimos abrirla. "
                                        + "Intenta iniciar sesión."
                        );

                        return;
                    }

                    guardarDatosUsuario(usuario, nombre, correo);
                });
    }

    // ------------------------------------------------
    // MENSAJES DE ERROR DE FIREBASE
    // ------------------------------------------------

    private String obtenerMensajeError(Exception error) {

        if (error instanceof FirebaseAuthUserCollisionException) {

            return "Este correo ya tiene una cuenta. "
                    + "Intenta iniciar sesión.";
        }

        if (error instanceof FirebaseAuthInvalidCredentialsException) {

            return "Firebase rechazó los datos de acceso. "
                    + "Verifica el correo y vuelve a intentarlo.";
        }

        if (error instanceof FirebaseNetworkException) {

            return "No hay conexión estable. "
                    + "Revisa tu internet e inténtalo otra vez.";
        }

        return "Ocurrió un problema al crear tu cuenta.\n\n"
                + "Inténtalo de nuevo en un momento.";
    }

    // ------------------------------------------------
    // GUARDAR DATOS EN FIREBASE
    // ------------------------------------------------

    private void guardarDatosUsuario(
            FirebaseUser usuario,
            String nombre,
            String correo
    ) {

        btnCrearCuenta.setEnabled(false);

        Map<String, Object> datosUsuario = new HashMap<>();

        datosUsuario.put("uid", usuario.getUid());
        datosUsuario.put("nombre", nombre);
        datosUsuario.put("correo", correo);
        datosUsuario.put(
                "fechaRegistro",
                System.currentTimeMillis()
        );

        referenciaUsuarios.child(usuario.getUid())
                .setValue(datosUsuario)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Log.d(
                                TAG,
                                "Usuario guardado en Realtime Database."
                        );

                        UserProfileChangeRequest perfil =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nombre)
                                        .build();

                        usuario.updateProfile(perfil)
                                .addOnFailureListener(e ->
                                        Log.w(
                                                TAG,
                                                "No se pudo actualizar el perfil.",
                                                e
                                        )
                                );

                        butiDialogo.mostrarExito(
                                "¡Cuenta creada!",
                                "Bienvenido a B'ÜTI, " + nombre + ".\n\n"
                                        + "Ya puedes iniciar sesión y comenzar "
                                        + "a aprender hñähñu.",
                                this::finish
                        );

                    } else {

                        btnCrearCuenta.setEnabled(true);

                        Exception error = task.getException();

                        Log.e(
                                TAG,
                                "Error al guardar en Realtime Database.",
                                error
                        );

                        butiDialogo.mostrarError(
                                "No guardamos tus datos",
                                "Tu cuenta se creó, pero no pudimos guardar "
                                        + "tu información. Revisa tu internet "
                                        + "e inténtalo de nuevo."
                        );
                    }
                });
    }

    // ------------------------------------------------
    // DETENER ANIMACIONES
    // ------------------------------------------------

    @Override
    protected void onDestroy() {

        if (registroAnimador != null) {
            registroAnimador.detener();
        }

        if (butiDialogo != null) {
            butiDialogo.cerrar();
        }

        super.onDestroy();
    }

    // ------------------------------------------------
    // CLASE INTERNA PARA LAS ANIMACIONES
    // ------------------------------------------------

    public static class RegistroAnimador {

        private AnimatorSet animaciones;

        public void reproducir(View... vistas) {

            detener();

            List<Animator> lista = new ArrayList<>();

            long retraso = 0;

            for (View vista : vistas) {

                if (vista == null) {
                    continue;
                }

                vista.setAlpha(0f);
                vista.setTranslationY(22f);

                ObjectAnimator aparecer = ObjectAnimator.ofFloat(
                        vista,
                        View.ALPHA,
                        0f,
                        1f
                );

                ObjectAnimator deslizar = ObjectAnimator.ofFloat(
                        vista,
                        View.TRANSLATION_Y,
                        22f,
                        0f
                );

                AnimatorSet entrada = new AnimatorSet();

                entrada.playTogether(aparecer, deslizar);
                entrada.setDuration(400);
                entrada.setStartDelay(retraso);

                entrada.setInterpolator(
                        new AccelerateDecelerateInterpolator()
                );

                lista.add(entrada);

                retraso += 100;
            }

            animaciones = new AnimatorSet();
            animaciones.playTogether(lista);
            animaciones.start();
        }

        public void detener() {

            if (animaciones != null) {

                animaciones.cancel();
                animaciones = null;
            }
        }

        public static void animarPresion(View vista) {

            vista.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(100)
                    .withEndAction(() ->
                            vista.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                                    .start()
                    )
                    .start();
        }
    }
}