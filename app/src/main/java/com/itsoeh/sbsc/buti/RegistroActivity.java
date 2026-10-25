package com.itsoeh.sbsc.buti;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
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

    private FirebaseAuth mAuth;
    private DatabaseReference referenciaUsuarios;

    private boolean longitudValida;
    private boolean mayusculaValida;
    private boolean minusculaValida;
    private boolean numeroValido;
    private boolean especialValido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        referenciaUsuarios = FirebaseDatabase.getInstance()
                .getReference("usuarios");

        edtNombre = findViewById(R.id.edtNombreRegistro);
        edtCorreo = findViewById(R.id.edtCorreoRegistro);
        edtContrasena = findViewById(R.id.edtContrasenaRegistro);
        edtConfirmarContrasena =
                findViewById(R.id.edtConfirmarContrasenaRegistro);

        chkTerminos = findViewById(R.id.chkTerminos);
        btnCrearCuenta = findViewById(R.id.btnCrearCuentaRegistro);

        TextView txtIniciarSesion =
                findViewById(R.id.txtIniciarSesionRegistro);

        txtNivelContrasena = findViewById(R.id.txtNivelContrasena);
        barraSeguridad = findViewById(R.id.barraSeguridad);

        txtReqLongitud = findViewById(R.id.txtReqLongitud);
        txtReqMayuscula = findViewById(R.id.txtReqMayuscula);
        txtReqMinuscula = findViewById(R.id.txtReqMinuscula);
        txtReqNumero = findViewById(R.id.txtReqNumero);
        txtReqEspecial = findViewById(R.id.txtReqEspecial);

        edtContrasena.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                actualizarSeguridad(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        actualizarSeguridad("");

        btnCrearCuenta.setOnClickListener(v -> crearCuenta());
        txtIniciarSesion.setOnClickListener(v -> finish());
    }

    private void actualizarSeguridad(String contrasena) {
        longitudValida = contrasena.length() >= 8;
        mayusculaValida = contrasena.matches("(?s).*\\p{Lu}.*");
        minusculaValida = contrasena.matches("(?s).*\\p{Ll}.*");
        numeroValido = contrasena.matches("(?s).*\\d.*");
        especialValido =
                contrasena.matches("(?s).*[\\p{P}\\p{S}].*");

        actualizarRequisito(
                txtReqLongitud, longitudValida, "Mínimo 8 caracteres");

        actualizarRequisito(
                txtReqMayuscula, mayusculaValida,
                "Una letra mayúscula (A-Z)");

        actualizarRequisito(
                txtReqMinuscula, minusculaValida,
                "Una letra minúscula (a-z)");

        actualizarRequisito(
                txtReqNumero, numeroValido, "Un número (0-9)");

        actualizarRequisito(
                txtReqEspecial, especialValido,
                "Un carácter especial (!@#$...)");

        int puntos = 0;
        if (longitudValida) puntos++;
        if (mayusculaValida) puntos++;
        if (minusculaValida) puntos++;
        if (numeroValido) puntos++;
        if (especialValido) puntos++;

        barraSeguridad.setProgress(puntos);

        int color;
        String nivel;

        if (contrasena.isEmpty()) {
            nivel = "Seguridad de la contraseña";
            color = Color.GRAY;
        } else if (puntos <= 2) {
            nivel = "Seguridad: débil";
            color = Color.rgb(217, 83, 79);
        } else if (puntos <= 4) {
            nivel = "Seguridad: media";
            color = Color.rgb(230, 160, 40);
        } else {
            nivel = "Seguridad: fuerte";
            color = Color.rgb(46, 160, 95);
        }

        txtNivelContrasena.setText(nivel);
        txtNivelContrasena.setTextColor(color);
        barraSeguridad.setProgressTintList(
                ColorStateList.valueOf(color));
    }

    private void actualizarRequisito(
            TextView vista, boolean cumplido, String texto) {

        if (cumplido) {
            vista.setText("✓ " + texto);
            vista.setTextColor(Color.rgb(46, 160, 95));
        } else {
            vista.setText("○ " + texto);
            vista.setTextColor(Color.GRAY);
        }
    }

    private boolean contrasenaSegura() {
        return longitudValida
                && mayusculaValida
                && minusculaValida
                && numeroValido
                && especialValido;
    }

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
                    "Completa todos los requisitos de seguridad");
            edtContrasena.requestFocus();

            Toast.makeText(this,
                    "Tu contraseña debe cumplir los 5 requisitos.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!contrasena.equals(confirmar)) {
            edtConfirmarContrasena.setError(
                    "Las contraseñas no coinciden");
            edtConfirmarContrasena.requestFocus();
            return;
        }

        if (!chkTerminos.isChecked()) {
            Toast.makeText(this,
                    "Debes aceptar los términos y condiciones",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnCrearCuenta.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {
                        btnCrearCuenta.setEnabled(true);

                        Exception error = task.getException();
                        String mensaje = obtenerMensajeError(error);

                        Log.e(TAG,
                                "Error al crear la cuenta. Correo: "
                                        + correo,
                                error);

                        Toast.makeText(
                                RegistroActivity.this,
                                mensaje,
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    FirebaseUser usuario = mAuth.getCurrentUser();

                    if (usuario == null) {
                        btnCrearCuenta.setEnabled(true);
                        Log.e(TAG,
                                "La autenticación fue exitosa, "
                                        + "pero getCurrentUser() devolvió null.");

                        Toast.makeText(
                                RegistroActivity.this,
                                "No se pudo obtener el usuario creado.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    guardarDatosUsuario(usuario, nombre, correo);
                });
    }

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

        if (error != null && error.getMessage() != null) {
            return "No se pudo crear la cuenta: "
                    + error.getMessage();
        }

        return "No se pudo crear la cuenta. "
                + "Revisa Logcat para ver el error.";
    }


    private void guardarDatosUsuario(
            FirebaseUser usuario, String nombre, String correo) {

        btnCrearCuenta.setEnabled(false);

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("uid", usuario.getUid());
        datosUsuario.put("nombre", nombre);
        datosUsuario.put("correo", correo);
        datosUsuario.put("fechaRegistro", System.currentTimeMillis());

        referenciaUsuarios.child(usuario.getUid())
                .setValue(datosUsuario)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Log.d(TAG,
                                "Usuario guardado en Realtime Database.");

                        UserProfileChangeRequest perfil =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nombre)
                                        .build();

                        usuario.updateProfile(perfil)
                                .addOnFailureListener(e ->
                                        Log.w(TAG,
                                                "No se pudo actualizar el perfil.",
                                                e));

                        Toast.makeText(
                                RegistroActivity.this,
                                "¡Cuenta creada y datos guardados!",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                    } else {
                        btnCrearCuenta.setEnabled(true);

                        Exception error = task.getException();

                        Log.e(TAG,
                                "Error al guardar en Realtime Database.",
                                error);

                        String mensaje =
                                "La cuenta existe, pero no se guardaron los datos.";

                        if (error != null
                                && error.getLocalizedMessage() != null) {
                            mensaje += " " + error.getLocalizedMessage();
                        }

                        Toast.makeText(
                                RegistroActivity.this,
                                mensaje,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

}
