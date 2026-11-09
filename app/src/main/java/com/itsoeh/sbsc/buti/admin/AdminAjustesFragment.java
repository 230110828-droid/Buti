package com.itsoeh.sbsc.buti.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ButiDialogo;
import com.itsoeh.sbsc.buti.LoginActivity;
import com.itsoeh.sbsc.buti.MainActivity;
import com.itsoeh.sbsc.buti.R;

/** Admin · Ajustes: modo mantenimiento, apariencia, ver la app y cuenta. */
public class AdminAjustesFragment extends Fragment {

    private SwitchMaterial switchMantenimiento;
    private TextView tvEstado;

    private DatabaseReference refMantenimiento;
    private ValueEventListener escucha;

    public AdminAjustesFragment() {
        super(R.layout.fragment_admin_ajustes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchMantenimiento = view.findViewById(R.id.switchMantenimiento);
        tvEstado = view.findViewById(R.id.tvEstadoMantenimiento);

        configurarMantenimiento();

        // Apariencia y accesibilidad: la misma Configuración de la app.
        view.findViewById(R.id.filaAparienciaAdmin).setOnClickListener(v -> {
            if (getActivity() instanceof AdminActivity) {
                ((AdminActivity) getActivity()).abrirApariencia();
            }
        });

        // Ver la app como estudiante (el admin no se bloquea por mantenimiento).
        view.findViewById(R.id.filaVerAppAdmin).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MainActivity.class)));

        // Cuenta.
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        ((TextView) view.findViewById(R.id.txtCorreoAdmin)).setText(
                usuario != null && usuario.getEmail() != null
                        ? "Sesión iniciada como " + usuario.getEmail()
                        : "Sin sesión");

        view.findViewById(R.id.btnCerrarSesionAdmin).setOnClickListener(v ->
                new ButiDialogo(requireContext()).mostrarConfirmacion(
                        "¿Cerrar sesión?",
                        "Saldrás del panel de administrador.",
                        "Cerrar sesión",
                        "Cancelar",
                        this::cerrarSesion,
                        null
                ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refMantenimiento != null && escucha != null) refMantenimiento.removeEventListener(escucha);
    }

    // =========================================
    // MODO MANTENIMIENTO
    // =========================================

    private void configurarMantenimiento() {

        refMantenimiento = FirebaseDatabase.getInstance().getReference("config/mantenimiento");

        escucha = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!isAdded() || getView() == null) return;

                boolean activo = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));

                // Cambiar el switch sin disparar el aviso.
                switchMantenimiento.setOnCheckedChangeListener(null);
                switchMantenimiento.setChecked(activo);
                switchMantenimiento.setOnCheckedChangeListener(alCambiarSwitch);

                ponerEstado(activo);
                guardarLocal(activo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && tvEstado != null) {
                    tvEstado.setText("No se pudo leer el estado: " + error.getMessage());
                }
            }
        };

        refMantenimiento.addValueEventListener(escucha);
        switchMantenimiento.setOnCheckedChangeListener(alCambiarSwitch);
    }

    private final CompoundButton.OnCheckedChangeListener alCambiarSwitch = (boton, activar) -> {

        if (activar) {
            // Pedir confirmación: deja a todos los estudiantes fuera de la app.
            boton.setOnCheckedChangeListener(null);
            boton.setChecked(false);
            boton.setOnCheckedChangeListener(this.alCambiarSwitchSeguro());

            new ButiDialogo(requireContext()).mostrarConfirmacion(
                    "¿Activar mantenimiento?",
                    "Los estudiantes verán la pantalla de mantenimiento y no podrán usar B'ÜTI "
                            + "hasta que lo desactives.",
                    "Activar",
                    "Cancelar",
                    () -> cambiarMantenimiento(true),
                    null
            );
        } else {
            cambiarMantenimiento(false);
        }
    };

    // Evita referenciar el campo antes de que exista (inicialización de Java).
    private CompoundButton.OnCheckedChangeListener alCambiarSwitchSeguro() {
        return alCambiarSwitch;
    }

    private void cambiarMantenimiento(boolean activar) {

        refMantenimiento.setValue(activar).addOnCompleteListener(t -> {

            if (!isAdded()) return;

            if (t.isSuccessful()) {
                ButiAviso.mostrar(
                        this,
                        activar
                                ? "Modo mantenimiento activado para todos los estudiantes."
                                : "Modo mantenimiento desactivado. B'ÜTI ya está disponible.",
                        activar ? ButiAviso.Tipo.INFO : ButiAviso.Tipo.EXITO
                );
            } else {
                new ButiDialogo(requireContext()).mostrarError(
                        "No se pudo cambiar",
                        "Revisa tu conexión y que las reglas de Firebase permitan escribir en «config»."
                );
            }
        });
    }

    private void ponerEstado(boolean activo) {
        tvEstado.setText(activo
                ? "ACTIVADO · los estudiantes no pueden entrar"
                : "La app está disponible");
        tvEstado.setTextColor(ContextCompat.getColor(requireContext(),
                activo ? R.color.buti_rosa : R.color.buti_exito));
    }

    private void guardarLocal(boolean activo) {
        requireContext().getSharedPreferences("buti_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("modo_mantenimiento", activo)
                .apply();
    }

    // =========================================
    // CUENTA
    // =========================================

    private void cerrarSesion() {

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
