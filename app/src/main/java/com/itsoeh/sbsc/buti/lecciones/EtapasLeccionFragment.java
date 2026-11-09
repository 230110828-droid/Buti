package com.itsoeh.sbsc.buti.lecciones;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.R;

public class EtapasLeccionFragment extends Fragment {

    private final MaterialCardView[] tarjetas = new MaterialCardView[5];
    private final TextView[] estados = new TextView[5];

    private TextView txtTituloNivel;
    private TextView txtPuntosEtapas;
    private TextView txtProgresoEtapas;
    private ProgressBar barraProgreso;

    private int numeroNivel = 1;
    private String nombreNivel = "Saludos";
    private int etapaPendiente = 0;
    private boolean navegacionAutomaticaRealizada = false;

    private DatabaseReference referenciaProgreso;
    private ValueEventListener listenerProgreso;

    public EtapasLeccionFragment() {
        super(R.layout.fragment_etapas_leccion);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        inicializarVistas(view);
        recibirDatos();
        configurarBotonVolver(view);
        configurarTarjetas();

        cargarProgreso();
    }

    private void inicializarVistas(View view) {

        txtTituloNivel = view.findViewById(R.id.txtTituloNivel);
        txtPuntosEtapas = view.findViewById(R.id.txtPuntosEtapas);
        txtProgresoEtapas = view.findViewById(R.id.txtProgresoEtapas);
        barraProgreso = view.findViewById(R.id.barraProgresoEtapas);

        tarjetas[0] = view.findViewById(R.id.cardEtapa1);
        tarjetas[1] = view.findViewById(R.id.cardEtapa2);
        tarjetas[2] = view.findViewById(R.id.cardEtapa3);
        tarjetas[3] = view.findViewById(R.id.cardEtapa4);
        tarjetas[4] = view.findViewById(R.id.cardEtapa5);

        estados[0] = view.findViewById(R.id.txtEstadoEtapa1);
        estados[1] = view.findViewById(R.id.txtEstadoEtapa2);
        estados[2] = view.findViewById(R.id.txtEstadoEtapa3);
        estados[3] = view.findViewById(R.id.txtEstadoEtapa4);
        estados[4] = view.findViewById(R.id.txtEstadoEtapa5);
    }

    private void recibirDatos() {

        Bundle argumentos = getArguments();

        if (argumentos != null) {

            String nombreRecibido =
                    argumentos.getString("nivelSeleccionado");

            if (nombreRecibido != null &&
                    !nombreRecibido.trim().isEmpty()) {
                nombreNivel = nombreRecibido;
            }

            numeroNivel = argumentos.getInt("numeroNivel", 1);
            etapaPendiente = argumentos.getInt("etapaPendiente", 0);
        }

        if (numeroNivel < 1 || numeroNivel > 11) {
            numeroNivel = 1;
        }

        txtTituloNivel.setText(
                "Nivel " + numeroNivel + ": " + nombreNivel
        );
    }

    private void configurarBotonVolver(View view) {

        ImageButton btnVolver =
                view.findViewById(R.id.btnVolverEtapas);

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> {
                Navigation.findNavController(v).popBackStack();
            });
        }
    }

    private void configurarTarjetas() {

        for (int i = 0; i < tarjetas.length; i++) {

            final int numeroEtapa = i + 1;

            if (tarjetas[i] == null) {
                continue;
            }

            tarjetas[i].setOnClickListener(v -> {

                if (!tarjetas[numeroEtapa - 1].isEnabled()) {
                    ButiAviso.mostrar(
                            this,
                            "Primero completa la etapa anterior.",
                            ButiAviso.Tipo.INFO
                    );
                    return;
                }

                abrirActividad(numeroEtapa, v);
            });
        }
    }

    private void cargarProgreso() {

        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            ButiAviso.mostrar(
                    this,
                    "Inicia sesión para guardar tu progreso.",
                    ButiAviso.Tipo.INFO
            );

            actualizarEtapas(new boolean[5], 0);
            return;
        }

        referenciaProgreso = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .child("progresoLecciones");

        referenciaProgreso.child("nivel" + numeroNivel)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot nivelSnapshot
                            ) {

                                if (!isAdded()) {
                                    return;
                                }

                                boolean[] completadas = new boolean[5];
                                int puntos = 0;

                                for (int i = 0; i < 5; i++) {

                                    DataSnapshot etapaSnapshot =
                                            nivelSnapshot.child("etapa" + (i + 1));

                                    Boolean completada =
                                            etapaSnapshot.child("completada")
                                                    .getValue(Boolean.class);

                                    completadas[i] =
                                            completada != null && completada;

                                    Integer puntosEtapa =
                                            etapaSnapshot.child("puntos")
                                                    .getValue(Integer.class);

                                    if (puntosEtapa != null) {
                                        puntos += puntosEtapa;
                                    }
                                }

                                actualizarEtapas(completadas, puntos);
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {

                                if (isAdded()) {
                                    ButiAviso.mostrar(
                                            EtapasLeccionFragment.this,
                                            "No se pudo cargar tu progreso.",
                                            ButiAviso.Tipo.ERROR
                                    );
                                }
                            }
                        }
                );
    }

    private void actualizarEtapas(
            boolean[] completadas,
            int puntos
    ) {

        if (!isAdded()) {
            return;
        }

        int totalCompletadas = 0;

        for (int i = 0; i < 5; i++) {

            boolean completada = completadas[i];

            // La primera etapa siempre está disponible.
            // Las demás requieren completar la etapa anterior.
            boolean desbloqueada =
                    i == 0 || completadas[i - 1];

            if (completada) {
                totalCompletadas++;
            }

            if (tarjetas[i] == null || estados[i] == null) {
                continue;
            }

            tarjetas[i].setEnabled(desbloqueada);
            tarjetas[i].setAlpha(
                    desbloqueada ? 1.0f : 0.55f
            );

            // Iconos reales en lugar de símbolos de texto
            int icono;

            if (completada) {
                icono = R.drawable.ic_estado_completada;
            } else if (desbloqueada) {
                icono = R.drawable.ic_estado_jugar;
            } else {
                icono = R.drawable.ic_estado_bloqueada;
            }

            estados[i].setText("");
            estados[i].setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, icono, 0
            );
        }

        txtProgresoEtapas.setText(
                totalCompletadas + " de 5 etapas completadas"
        );

        barraProgreso.setMax(5);
        barraProgreso.setProgress(totalCompletadas);

        txtPuntosEtapas.setText(String.valueOf(puntos));
        // Abrir automáticamente la primera etapa pendiente
// cuando se llega desde el botón Continuar lección.
        if (etapaPendiente >= 1
                && etapaPendiente <= 5
                && !navegacionAutomaticaRealizada
                && tarjetas[etapaPendiente - 1] != null
                && tarjetas[etapaPendiente - 1].isEnabled()
                && !completadas[etapaPendiente - 1]) {

            navegacionAutomaticaRealizada = true;

            View vista = getView();

            if (vista != null) {
                abrirActividad(etapaPendiente, vista);
            }
        }
    }

    private void abrirActividad(
            int numeroEtapa,
            View view
    ) {

        Bundle argumentos = new Bundle();

        argumentos.putInt("numeroNivel", numeroNivel);
        argumentos.putString("nivelSeleccionado", nombreNivel);
        argumentos.putInt("etapaSeleccionada", numeroEtapa);

        NavController navController =
                Navigation.findNavController(view);

        if (navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId()
                        == R.id.etapasLeccionFragment) {

            navController.navigate(
                    R.id.actividadLeccionFragment,
                    argumentos
            );
        }
    }
}