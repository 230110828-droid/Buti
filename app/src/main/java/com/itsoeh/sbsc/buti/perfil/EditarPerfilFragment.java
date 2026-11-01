
package com.itsoeh.sbsc.buti.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
            Toast.makeText(
                    requireContext(),
                    "Inicia sesión para editar tu perfil.",
                    Toast.LENGTH_LONG
            ).show();

            nav.popBackStack();
            return;
        }

        referenciaUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(user.getUid());

        cargarDatosIniciales();
        escucharNombreGuardado();
        configurarIniciales();

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

            Toast.makeText(
                    requireContext(),
                    "Esta cuenta no usa contraseña de correo. " +
                            "Debes volver a autenticarte con tu proveedor " +
                            "de inicio de sesión para cambiar el correo.",
                    Toast.LENGTH_LONG
            ).show();

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

        EditText campoContrasena = new EditText(requireContext());

        campoContrasena.setHint("Contraseña actual");
        campoContrasena.setSingleLine(true);
        campoContrasena.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        int margen = (int) (24 * getResources()
                .getDisplayMetrics().density);

        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(margen, 0, margen, 0);
        contenedor.addView(campoContrasena);

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle("Confirma tu identidad")
                .setMessage(
                        "Para cambiar tu correo electrónico, " +
                                "escribe tu contraseña actual."
                )
                .setView(contenedor)
                .setPositiveButton("Continuar", null)
                .setNegativeButton("Cancelar", (dialog, which) ->
                        restaurarBoton())
                .create();

        dialogo.setOnShowListener(dialog -> {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String contrasena =
                                campoContrasena.getText()
                                        .toString();

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

                        dialogo.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setEnabled(false);

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

                                    dialogo.getButton(
                                            AlertDialog.BUTTON_POSITIVE
                                    ).setEnabled(true);

                                    Toast.makeText(
                                            requireContext(),
                                            "No se pudo confirmar tu identidad. " +
                                                    "Verifica tu contraseña actual.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                    });
        });

        dialogo.setOnCancelListener(dialog ->
                restaurarBoton());

        dialogo.show();
    }

    private void cambiarCorreoConfirmado(
            String nombre,
            String nuevoCorreo) {

        if (!isAdded() || user == null) {
            restaurarBoton();
            return;
        }

        user.updateEmail(nuevoCorreo)
                .addOnSuccessListener(resultado -> {
                    if (!isAdded()) {
                        return;
                    }

                    // Authentication cambió el correo.
                    // Ahora guardamos nombre y correo en la base de datos.
                    actualizarNombre(nombre, nuevoCorreo);
                })
                .addOnFailureListener(error -> {
                    if (!isAdded()) {
                        return;
                    }

                    restaurarBoton();

                    String detalle = error.getLocalizedMessage();

                    if (TextUtils.isEmpty(detalle)) {
                        detalle = "Inténtalo de nuevo.";
                    }

                    Toast.makeText(
                            requireContext(),
                            "No se pudo cambiar el correo: " + detalle,
                            Toast.LENGTH_LONG
                    ).show();
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

                        Toast.makeText(
                                requireContext(),
                                "No se pudo actualizar el nombre " +
                                        "de la cuenta. Revisa tu conexión.",
                                Toast.LENGTH_LONG
                        ).show();

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
                                    Toast.makeText(
                                            requireContext(),
                                            "Perfil actualizado correctamente.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    NavController nav =
                                            Navigation.findNavController(
                                                    requireView()
                                            );

                                    nav.popBackStack();

                                } else {
                                    restaurarBoton();

                                    Toast.makeText(
                                            requireContext(),
                                            "La cuenta se actualizó, pero " +
                                                    "no se pudieron guardar " +
                                                    "los datos en Realtime " +
                                                    "Database. Revisa los " +
                                                    "permisos de Firebase.",
                                            Toast.LENGTH_LONG
                                    ).show();
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
