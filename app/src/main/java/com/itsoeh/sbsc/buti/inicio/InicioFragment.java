package com.itsoeh.sbsc.buti.inicio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.itsoeh.sbsc.buti.FotoPerfil;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.RachaMaguey;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    // Total de niveles y etapas
    private static final int TOTAL_NIVELES = 11;
    private static final int ETAPAS_POR_NIVEL = 5;
    private static final int TOTAL_ETAPAS =
            TOTAL_NIVELES * ETAPAS_POR_NIVEL;

    // Elementos de la pantalla
    private TextView txtNombreUsuario;
    private TextView txtLeccionActual;
    private TextView txtPorcentaje;
    private TextView txtRacha;
    private TextView txtPuntos;
    private TextView txtMensajeRacha;
    private ImageView imgRacha;

    private ProgressBar pbProgreso;

    // Firebase
    private DatabaseReference referenciaUsuario;
    private ValueEventListener listenerUsuario;

    public InicioFragment() {
        super(R.layout.fragment_inicio);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // -----------------------------------------
        // VINCULAR ELEMENTOS
        // -----------------------------------------

        txtNombreUsuario =
                view.findViewById(R.id.txtNombreUsuarioInicio);

        txtLeccionActual =
                view.findViewById(R.id.txtLeccionActualProgreso);

        txtPorcentaje =
                view.findViewById(R.id.txtPorcentajeProgreso);

        txtRacha =
                view.findViewById(R.id.txtRachaInicio);

        pbProgreso =
                view.findViewById(R.id.pbProgresoInicio);

        txtPuntos = view.findViewById(R.id.txtPuntosInicio);
        txtMensajeRacha = view.findViewById(R.id.txtMensajeRachaInicio);
        imgRacha = view.findViewById(R.id.imgRachaInicio);

        // Foto de perfil (o iniciales) en la tarjeta de saludo.
        FotoPerfil.mostrar(
                getViewLifecycleOwner(),
                view.findViewById(R.id.imgFotoInicio),
                view.findViewById(R.id.txtInicialesInicio),
                null
        );

        // -----------------------------------------
        // VALORES INICIALES
        // -----------------------------------------

        pbProgreso.setMax(100);
        pbProgreso.setProgress(0);

        txtPorcentaje.setText("0%");
        txtLeccionActual.setText("Lección 1 de 11 · Etapa 1 de 5");
        txtRacha.setText("Racha de 0 días");

        // -----------------------------------------
        // CARGAR DATOS DE FIREBASE
        // -----------------------------------------

        cargarDatosUsuario();

        // -----------------------------------------
        // CONTROLADOR DE NAVEGACIÓN
        // -----------------------------------------

        NavController navController =
                Navigation.findNavController(view);

        // -----------------------------------------
        // AVATAR: ABRE MI PERFIL
        // -----------------------------------------

        View avatar = view.findViewById(R.id.contenedorAvatarInicio);

        if (avatar != null) {
            avatar.setOnClickListener(v ->
                    animarYCambiar(
                            v,
                            () -> navegar(navController, R.id.perfilFragment)
                    )
            );
        }

        // -----------------------------------------
        // BOTÓN CONTINUAR LECCIÓN
        // -----------------------------------------

        Button btnContinuarLeccion =
                view.findViewById(R.id.btnContinuarLeccion);

        btnContinuarLeccion.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(
                                navController,
                                R.id.practicarFragment
                        )
                )
        );

        // -----------------------------------------
        // TARJETA IXMIQUILPAN
        // -----------------------------------------

        View cardIxmiquilpan =
                view.findViewById(R.id.cardNavIxmiquilpan);

        cardIxmiquilpan.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(
                                navController,
                                R.id.culturaFragment
                        )
                )
        );

        // -----------------------------------------
        // TARJETA VOCABULARIO
        // -----------------------------------------

        View cardVocabulario =
                view.findViewById(R.id.cardNavVocabulario);

        cardVocabulario.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(
                                navController,
                                R.id.vocabularioFragment
                        )
                )
        );

        // -----------------------------------------
        // TARJETA PRACTICAR
        // -----------------------------------------

        View cardPracticar =
                view.findViewById(R.id.cardNavPracticar);

        cardPracticar.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(
                                navController,
                                R.id.practicarFragment
                        )
                )
        );

        // -----------------------------------------
        // TARJETA MI PROGRESO
        // -----------------------------------------

        View cardProgreso =
                view.findViewById(R.id.cardNavProgreso);

        cardProgreso.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(
                                navController,
                                R.id.progresoFragment
                        )
                )
        );

        // -----------------------------------------
        // ANIMACIONES DE ENTRADA
        // -----------------------------------------

        configurarAnimacionesEntrada(view);
    }

    // ---------------------------------------------
    // CARGAR DATOS DEL USUARIO
    // ---------------------------------------------

    private void cargarDatosUsuario() {

        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            txtNombreUsuario.setText("Hola, aprendiz");
            return;
        }

        // Nombre inicial desde Firebase Authentication
        String nombreInicial = currentUser.getDisplayName();

        if (nombreInicial != null && !nombreInicial.trim().isEmpty()) {
            txtNombreUsuario.setText("Hola, " + nombreInicial);
        } else {
            txtNombreUsuario.setText("Hola, aprendiz");
        }

        String uid = currentUser.getUid();

        referenciaUsuario = FirebaseDatabase
                .getInstance()
                .getReference("usuarios")
                .child(uid);

        listenerUsuario = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!isAdded() || getView() == null) {
                    return;
                }

                // ---------------------------------
                // NOMBRE DEL USUARIO
                // ---------------------------------

                String nombre = snapshot.child("nombre")
                        .getValue(String.class);

                if (nombre != null && !nombre.trim().isEmpty()) {
                    txtNombreUsuario.setText("Hola, " + nombre);
                }

                // ---------------------------------
                // RACHA DEL USUARIO
                // ---------------------------------

                // Si dejó pasar un día sin practicar, la racha vuelve a 0.
                int racha = ProgresoButi.rachaVigente(snapshot);

                actualizarRacha(racha);

                // ---------------------------------
                // PUNTOS TOTALES
                // ---------------------------------

                int puntos = ProgresoButi.puntosTotales(snapshot);

                if (txtPuntos != null) {
                    txtPuntos.setText(puntos == 1 ? "1 punto" : puntos + " puntos");
                }

                // ¿Ya practicó hoy?
                if (txtMensajeRacha != null) {

                    boolean practicoHoy = ProgresoButi.hoy().equals(
                            snapshot.child("racha").child("ultimaFecha").getValue(String.class)
                    );

                    txtMensajeRacha.setText(
                            practicoHoy
                                    ? "¡Ya practicaste hoy! Vuelve mañana para que crezca."
                                    : racha > 0
                                      ? "Practica hoy para no perder tu racha."
                                      : "Completa una lección, práctica o juego para empezar."
                    );
                }

                // ---------------------------------
                // PROGRESO DE LAS LECCIONES
                // ---------------------------------

                DataSnapshot progreso =
                        snapshot.child("progresoLecciones");

                calcularProgreso(progreso);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Se mantienen los valores iniciales
            }
        };

        // Escucha cambios en tiempo real
        referenciaUsuario.addValueEventListener(listenerUsuario);
    }

    // ---------------------------------------------
    // CALCULAR PROGRESO DE LAS LECCIONES
    // ---------------------------------------------

    private void calcularProgreso(DataSnapshot progreso) {

        int etapasCompletadas = 0;

        int siguienteNivel = TOTAL_NIVELES;
        int siguienteEtapa = ETAPAS_POR_NIVEL;

        boolean encontroPendiente = false;

        for (int nivel = 1; nivel <= TOTAL_NIVELES; nivel++) {

            for (int etapa = 1; etapa <= ETAPAS_POR_NIVEL; etapa++) {

                DataSnapshot datosEtapa = progreso
                        .child("nivel" + nivel)
                        .child("etapa" + etapa);

                Boolean completada = datosEtapa
                        .child("completada")
                        .getValue(Boolean.class);

                boolean estaCompletada =
                        Boolean.TRUE.equals(completada);

                if (estaCompletada) {

                    etapasCompletadas++;

                } else if (!encontroPendiente) {

                    siguienteNivel = nivel;
                    siguienteEtapa = etapa;

                    encontroPendiente = true;
                }
            }
        }

        // -----------------------------------------
        // CALCULAR PORCENTAJE
        // -----------------------------------------

        int porcentaje = Math.round(
                (etapasCompletadas * 100f) / TOTAL_ETAPAS
        );

        // -----------------------------------------
        // ACTUALIZAR BARRA Y PORCENTAJE
        // -----------------------------------------

        pbProgreso.setMax(100);
        pbProgreso.setProgress(porcentaje);

        txtPorcentaje.setText(porcentaje + "%");

        // -----------------------------------------
        // ACTUALIZAR LECCIÓN ACTUAL
        // -----------------------------------------

        if (etapasCompletadas >= TOTAL_ETAPAS) {

            txtLeccionActual.setText(
                    "¡Completaste todas las lecciones!"
            );

        } else {

            txtLeccionActual.setText(
                    "Lección " + siguienteNivel +
                            " de " + TOTAL_NIVELES +
                            " · Etapa " + siguienteEtapa +
                            " de " + ETAPAS_POR_NIVEL
            );
        }
    }

    // ---------------------------------------------
    // ACTUALIZAR RACHA
    // ---------------------------------------------

    private void actualizarRacha(int dias) {

        if (dias < 0) {
            dias = 0;
        }

        if (dias == 1) {

            txtRacha.setText("Racha de 1 día");

        } else {

            txtRacha.setText("Racha de " + dias + " días");
        }

        // El maguey crece con la racha.
        if (imgRacha != null) {
            imgRacha.setImageResource(RachaMaguey.dibujoPorRacha(dias));
        }
    }

    // ---------------------------------------------
    // ANIMACIÓN AL ENTRAR A INICIO
    // ---------------------------------------------

    private void configurarAnimacionesEntrada(View root) {

        List<View> elementos = new ArrayList<>();

        elementos.add(
                root.findViewById(R.id.txtNombreUsuarioInicio)
        );

        elementos.add(
                root.findViewById(R.id.btnContinuarLeccion)
        );

        elementos.add(
                root.findViewById(R.id.cardNavIxmiquilpan)
        );

        elementos.add(
                root.findViewById(R.id.cardNavVocabulario)
        );

        elementos.add(
                root.findViewById(R.id.cardNavPracticar)
        );

        elementos.add(
                root.findViewById(R.id.cardNavProgreso)
        );

        long retraso = 0;

        for (View elemento : elementos) {

            if (elemento == null) {
                continue;
            }

            elemento.setAlpha(0f);
            elemento.setTranslationY(22f);
            elemento.setScaleX(0.97f);
            elemento.setScaleY(0.97f);

            long retrasoActual = retraso;

            elemento.post(() -> {

                elemento.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(430)
                        .setStartDelay(retrasoActual)
                        .start();
            });

            retraso += 75;
        }
    }

    // ---------------------------------------------
    // ANIMACIÓN AL PULSAR UNA TARJETA
    // ---------------------------------------------

    private void animarYCambiar(
            View elemento,
            Runnable accion
    ) {

        elemento.animate().cancel();

        elemento.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(100)
                .withEndAction(() -> {

                    elemento.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(110)
                            .withEndAction(() -> {

                                if (isAdded()) {
                                    accion.run();
                                }

                            })
                            .start();

                })
                .start();
    }

    // ---------------------------------------------
    // NAVEGACIÓN SEGURA
    // ---------------------------------------------

    private void navegar(
            NavController navController,
            int destino
    ) {

        if (!isAdded()) {
            return;
        }

        if (navController.getCurrentDestination() == null) {
            return;
        }

        if (navController.getCurrentDestination().getId() != destino) {
            navController.navigate(destino);
        }
    }

    // ---------------------------------------------
    // LIBERAR LISTENER DE FIREBASE
    // ---------------------------------------------

    @Override
    public void onDestroyView() {

        if (referenciaUsuario != null && listenerUsuario != null) {
            referenciaUsuario.removeEventListener(listenerUsuario);
        }

        referenciaUsuario = null;
        listenerUsuario = null;

        txtNombreUsuario = null;
        txtLeccionActual = null;
        txtPorcentaje = null;
        txtRacha = null;
        txtPuntos = null;
        txtMensajeRacha = null;
        imgRacha = null;
        pbProgreso = null;

        super.onDestroyView();
    }
}