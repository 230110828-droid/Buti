package com.itsoeh.sbsc.buti.perfil;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ButiDialogo;
import com.itsoeh.sbsc.buti.FotoPerfil;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.RecuperarContrasenaActivity;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilFragment extends Fragment {

    private EditText etNombre;
    private EditText etCorreo;
    private TextView txtIniciales;
    private MaterialButton btnGuardar;

    private FirebaseUser user;
    private DatabaseReference referenciaUsuario;
    private TextWatcher watcherNombre;

    public EditarPerfilFragment() {
        super(R.layout.fragment_editar_perfil);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        NavController nav = Navigation.findNavController(view);

        etNombre = view.findViewById(R.id.etNombrePerfil);
        etCorreo = view.findViewById(R.id.etCorreoPerfil);
        txtIniciales = view.findViewById(R.id.txtInicialesEditar);
        btnGuardar = view.findViewById(R.id.btnGuardarPerfil);

        MaterialButton btnCancelar =
                view.findViewById(R.id.btnCancelarPerfil);

        MaterialButton btnPass =
                view.findViewById(R.id.btnCambiarContrasena);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            ButiAviso.mostrar(
                    EditarPerfilFragment.this,
                    "Inicia sesión para editar tu perfil.",
                    ButiAviso.Tipo.INFO
            );

            nav.popBackStack();
            return;
        }

        referenciaUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(user.getUid());

        cargarDatosIniciales();
        escucharNombreGuardado();
        configurarIniciales();

        // Foto de perfil (si no hay, se ven las iniciales).
        FotoPerfil.mostrar(
                getViewLifecycleOwner(),
                view.findViewById(R.id.imgFotoEditar),
                txtIniciales,
                null
        );

        // Mostrar la opción de contraseña solo para cuentas
        // que usan correo y contraseña.
        boolean usaContrasena = tieneProveedorCorreoContrasena();

        if (btnPass != null) {
            if (usaContrasena) {
                btnPass.setVisibility(View.VISIBLE);

                btnPass.setOnClickListener(v -> {
                    Intent intent = new Intent(
                            requireContext(),
                            RecuperarContrasenaActivity.class
                    );
                    startActivity(intent);
                });
            } else {
                btnPass.setVisibility(View.GONE);
            }
        }

        // Las cuentas de Google usan el correo de Google:
        // ese correo no se puede cambiar desde la app.
        if (!usaContrasena) {
            etCorreo.setEnabled(false);
            etCorreo.setAlpha(0.6f);
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> nav.popBackStack());
        }

        btnGuardar.setOnClickListener(v -> validarYGuardar());
    }

    private boolean tieneProveedorCorreoContrasena() {
        if (user == null) {
            return false;
        }

        for (UserInfo info : user.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(
                    info.getProviderId())) {
                return true;
            }
        }

        return false;
    }

    private void cargarDatosIniciales() {
        String nombre = user.getDisplayName();

        if (TextUtils.isEmpty(nombre)) {
            nombre = nombreDesdeCorreo(user.getEmail());
        }

        etNombre.setText(nombre);

        etCorreo.setText(
                user.getEmail() != null ? user.getEmail() : ""
        );

        etNombre.setSelection(etNombre.getText().length());

        actualizarIniciales(nombre);
    }

    private void escucharNombreGuardado() {
        final String nombreInicial =
                etNombre.getText().toString();

        referenciaUsuario.child("nombre")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                if (!isAdded() || etNombre == null) {
                                    return;
                                }

                                String nombreBD =
                                        snapshot.getValue(String.class);

                                // No sobrescribir el nombre si el usuario
                                // ya empezó a escribir uno nuevo.
                                boolean noHaEditado =
                                        etNombre.getText().toString()
                                                .equals(nombreInicial);

                                if (!TextUtils.isEmpty(nombreBD)
                                        && noHaEditado) {

                                    etNombre.setText(nombreBD);

                                    etNombre.setSelection(
                                            etNombre.getText().length()
                                    );

                                    actualizarIniciales(nombreBD);
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {
                                // Se conserva el nombre de Authentication.
                            }
                        }
                );
    }

    private void configurarIniciales() {
        watcherNombre = new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                actualizarIniciales(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etNombre.addTextChangedListener(watcherNombre);
    }

    private void validarYGuardar() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();

        if (nombre.length() < 2) {
            etNombre.setError(
                    "Escribe tu nombre (mínimo 2 caracteres)."
            );
            etNombre.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(correo)
                || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            etCorreo.setError(
                    "Escribe un correo electrónico válido."
            );
            etCorreo.requestFocus();
            return;
        }

        guardarCambios(nombre, correo);
    }

    private void guardarCambios(String nombre, String correo) {
        if (user == null || btnGuardar == null) {
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        String correoActual = user.getEmail();

        // Si el correo no cambió, solamente guardamos el nombre.
        if (correoActual != null
                && correo.equalsIgnoreCase(correoActual)) {

            actualizarNombre(nombre, correoActual);
            return;
        }

        // Para cambiar el correo necesitamos una cuenta que use
        // correo y contraseña para poder volver a autenticarla.
        if (!tieneProveedorCorreoContrasena()) {
            restaurarBoton();

            ButiAviso.mostrar(
                    EditarPerfilFragment.this,
                    "Esta cuenta no usa contraseña de correo. " +
                            "Debes volver a autenticarte con tu proveedor " +
                            "de inicio de sesión para cambiar el correo.",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        solicitarContrasenaParaCambiarCorreo(nombre, correo);
    }

    private void solicitarContrasenaParaCambiarCorreo(
            String nombre,
            String nuevoCorreo) {

        if (!isAdded() || user == null || user.getEmail() == null) {
            restaurarBoton();
            return;
        }

        // Ventana propia de B'ÜTI (antes AlertDialog).
        Dialog dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_confirmar_contrasena);
        dialogo.setCancelable(true);

        EditText campoContrasena =
                dialogo.findViewById(R.id.etContrasenaConfirmar);

        MaterialButton btnContinuar =
                dialogo.findViewById(R.id.btnConfirmarContrasena);

        MaterialButton btnCancelarDialogo =
                dialogo.findViewById(R.id.btnCancelarContrasena);

        btnCancelarDialogo.setOnClickListener(v -> {
            dialogo.dismiss();
            restaurarBoton();
        });

        btnContinuar.setOnClickListener(v -> {

            String contrasena =
                    campoContrasena.getText().toString();

            if (contrasena.trim().isEmpty()) {
                campoContrasena.setError(
                        "Escribe tu contraseña actual."
                );
                campoContrasena.requestFocus();
                return;
            }

            // Ocultar el teclado antes de autenticar.
            InputMethodManager teclado =
                    (InputMethodManager) requireContext()
                            .getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (teclado != null) {
                teclado.hideSoftInputFromWindow(
                        campoContrasena.getWindowToken(), 0
                );
            }

            btnContinuar.setEnabled(false);
            btnContinuar.setText("Verificando...");

            AuthCredential credencial =
                    EmailAuthProvider.getCredential(
                            user.getEmail(),
                            contrasena
                    );

            user.reauthenticate(credencial)
                    .addOnSuccessListener(resultado -> {
                        if (!isAdded()) {
                            return;
                        }

                        dialogo.dismiss();

                        cambiarCorreoConfirmado(
                                nombre, nuevoCorreo
                        );
                    })
                    .addOnFailureListener(error -> {
                        if (!isAdded()) {
                            return;
                        }

                        btnContinuar.setEnabled(true);
                        btnContinuar.setText("Continuar");

                        ButiAviso.mostrar(
                                EditarPerfilFragment.this,
                                "No se pudo confirmar tu identidad. " +
                                        "Verifica tu contraseña actual.",
                                ButiAviso.Tipo.ERROR
                        );
                    });
        });

        dialogo.setOnCancelListener(dialog ->
                restaurarBoton());

        dialogo.show();

        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
            dialogo.getWindow().setDimAmount(0.6f);
            dialogo.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void cambiarCorreoConfirmado(
            String nombre,
            String nuevoCorreo) {

        if (!isAdded() || user == null) {
            restaurarBoton();
            return;
        }

        // Firebase ya no permite cambiar el correo directamente
        // (updateEmail). Ahora envía un enlace al correo nuevo y
        // el cambio se hace cuando la persona lo abre.
        user.verifyBeforeUpdateEmail(nuevoCorreo)
                .addOnSuccessListener(resultado -> {
                    if (!isAdded()) {
                        return;
                    }

                    // El nombre sí se guarda de inmediato.
                    // El correo se actualiza en la base de datos
                    // la próxima vez que inicie sesión.
                    String correoActual = user.getEmail() != null
                            ? user.getEmail()
                            : nuevoCorreo;

                    // Mensaje personalizado de B'ÜTI (antes AlertDialog).
                    new ButiDialogo(requireContext()).mostrarExito(
                            "Revisa tu correo nuevo",
                            "Te enviamos un enlace a:\n" + nuevoCorreo
                                    + "\n\nÁbrelo para confirmar el cambio. "
                                    + "Después vuelve a iniciar sesión "
                                    + "con tu correo nuevo.\n\n"
                                    + "Si no lo ves, revisa la carpeta de spam.",
                            () -> actualizarNombre(nombre, correoActual)
                    );
                })
                .addOnFailureListener(error -> {
                    if (!isAdded()) {
                        return;
                    }

                    restaurarBoton();

                    String detalle = error.getMessage() == null
                            ? ""
                            : error.getMessage().toLowerCase();

                    String mensaje;

                    if (detalle.contains("already in use")
                            || detalle.contains("email-already-in-use")) {
                        mensaje = "Ese correo ya está registrado en otra cuenta.";
                    } else if (detalle.contains("network")) {
                        mensaje = "Revisa tu conexión a internet e inténtalo de nuevo.";
                    } else if (detalle.contains("recent")) {
                        mensaje = "Por seguridad, cierra sesión, vuelve a entrar e inténtalo de nuevo.";
                    } else {
                        mensaje = "No se pudo cambiar el correo. Inténtalo de nuevo.";
                    }

                    android.util.Log.e("EditarPerfil",
                            "Error al cambiar correo", error);

                    ButiAviso.mostrar(
                            EditarPerfilFragment.this,
                            mensaje,
                            ButiAviso.Tipo.ERROR
                    );
                });
    }

    private void actualizarNombre(String nombre, String correo) {
        if (!isAdded() || user == null) {
            restaurarBoton();
            return;
        }

        UserProfileChangeRequest cambios =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build();

        user.updateProfile(cambios)
                .addOnCompleteListener(tarea -> {
                    if (!isAdded() || btnGuardar == null) {
                        return;
                    }

                    if (!tarea.isSuccessful()) {
                        restaurarBoton();

                        ButiAviso.mostrar(
                                EditarPerfilFragment.this,
                                "No se pudo actualizar el nombre " +
                                        "de la cuenta. Revisa tu conexión.",
                                ButiAviso.Tipo.ERROR
                        );

                        return;
                    }

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("nombre", nombre);
                    datos.put("correo", correo);

                    referenciaUsuario.updateChildren(datos)
                            .addOnCompleteListener(tareaBD -> {
                                if (!isAdded() || btnGuardar == null) {
                                    return;
                                }

                                if (tareaBD.isSuccessful()) {
                                    ButiAviso.mostrar(
                                            EditarPerfilFragment.this,
                                            "Perfil actualizado correctamente.",
                                            ButiAviso.Tipo.EXITO
                                    );

                                    NavController nav =
                                            Navigation.findNavController(
                                                    requireView()
                                            );

                                    nav.popBackStack();

                                } else {
                                    restaurarBoton();

                                    ButiAviso.mostrar(
                                            EditarPerfilFragment.this,
                                            "La cuenta se actualizó, pero " +
                                                    "no se pudieron guardar " +
                                                    "los datos en Realtime " +
                                                    "Database. Revisa los " +
                                                    "permisos de Firebase.",
                                            ButiAviso.Tipo.ERROR
                                    );
                                }
                            });
                });
    }

    private void restaurarBoton() {
        if (btnGuardar != null) {
            btnGuardar.setEnabled(true);
            btnGuardar.setText("Guardar cambios");
        }
    }

    private String nombreDesdeCorreo(String correo) {
        if (TextUtils.isEmpty(correo)) {
            return "Usuario";
        }

        int arroba = correo.indexOf("@");

        return arroba > 0
                ? correo.substring(0, arroba)
                : correo;
    }

    private String iniciales(String nombre) {
        String n = nombre == null ? "" : nombre.trim();

        if (n.isEmpty()) {
            return "?";
        }

        String[] partes = n.split("\\s+");

        if (partes.length >= 2) {
            return (
                    partes[0].substring(0, 1)
                            + partes[1].substring(0, 1)
            ).toUpperCase();
        }

        return n.substring(0, Math.min(2, n.length()))
                .toUpperCase();
    }

    private void actualizarIniciales(String nombre) {
        if (txtIniciales != null) {
            txtIniciales.setText(iniciales(nombre));
        }
    }

    @Override
    public void onDestroyView() {
        if (etNombre != null && watcherNombre != null) {
            etNombre.removeTextChangedListener(watcherNombre);
        }

        etNombre = null;
        etCorreo = null;
        txtIniciales = null;
        btnGuardar = null;
        watcherNombre = null;

        super.onDestroyView();
    }
}