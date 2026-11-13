package com.itsoeh.sbsc.buti.inicio;

import android.content.Context;
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
import com.itsoeh.sbsc.buti.MainActivity;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.RachaButi;
import com.itsoeh.sbsc.buti.RachaMaguey;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

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

    // Lección pendiente (la siguiente que toca).
    private int nivelPendiente = 1;
    private int etapaPendiente = 1;
    private boolean todoCompleto = false;

    // {nombre en Lecciones, nombre en Vocabulario, descripción, imagen}
    private static final String[][] TEMAS = {
            {"Saludos", "Saludos", "Aprende a saludar y responder de forma amable.", "img_cat_saludos"},
            {"Números", "Números", "Aprende a contar en hñähñu.", "img_cat_numeros"},
            {"Colores", "Colores", "Nombra los colores que ves a tu alrededor.", "img_cat_colores"},
            {"Animales", "Animales", "Conoce los animales del Valle del Mezquital.", "img_cat_animales"},
            {"Plantas", "Plantas", "Maguey, nopal, maíz y más plantas de la región.", "img_cat_plantas"},
            {"Objetos", "Objetos", "Objetos de la casa y de la cocina tradicional.", "img_cat_objetos"},
            {"Cuerpo", "Cuerpo", "Aprende las partes del cuerpo.", "img_cat_cuerpo"},
            {"Comida", "Comida", "Tortillas, tamales, barbacoa y más.", "img_cat_comida"},
            {"Tiempo y celebraciones", "Tiempo", "Hoy, mañana, ayer: habla del tiempo.", "img_cat_tiempo"},
            {"Naturaleza", "Naturaleza", "El sol, la luna, el agua y los cerros.", "img_cat_naturaleza"},
            {"Lugares", "Lugares", "El mercado, la escuela y otros lugares de la comunidad.", "img_cat_lugares"}
    };

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

        // Abre las etapas del nivel en el que va el usuario.
        btnContinuarLeccion.setOnClickListener(v ->
                animarYCambiar(v, () -> abrirLeccionPendiente(navController))
        );

        // -----------------------------------------
        // APRENDE HOY: vocabulario del tema que sigue
        // -----------------------------------------

        View cardAprendeHoy = view.findViewById(R.id.cardAprendeHoy);

        if (cardAprendeHoy != null) {
            cardAprendeHoy.setOnClickListener(v ->
                    animarYCambiar(v, () -> abrirVocabularioDelTema(navController))
            );
        }

        mostrarAprendeHoy();

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

                // Racha con 1 día de gracia: activa, congelada o perdida.
                ProgresoButi.EstadoRacha estadoRacha = ProgresoButi.estadoRacha(snapshot);
                int racha = estadoRacha.dias;

                actualizarRacha(estadoRacha);
                avisarEstadoRacha(estadoRacha);

                // ---------------------------------
                // PUNTOS TOTALES
                // ---------------------------------

                int puntos = ProgresoButi.puntosTotales(snapshot);

                if (txtPuntos != null) {
                    txtPuntos.setText(puntos == 1 ? "1 punto" : puntos + " puntos");
                }

                // Mensaje según cómo está el maguey.
                if (txtMensajeRacha != null) {

                    String mensajeRacha;

                    switch (estadoRacha.estado) {
                        case ACTIVA:
                            mensajeRacha = "¡Ya practicaste hoy! Vuelve mañana para que crezca.";
                            break;
                        case PENDIENTE:
                            mensajeRacha = "Practica hoy para no perder tu racha.";
                            break;
                        case CONGELADA:
                            mensajeRacha = "❄ Tu maguey se congeló. Practica hoy para salvar tu racha.";
                            break;
                        case PERDIDA:
                            mensajeRacha = estadoRacha.diasPerdidos > 0
                                    ? "Tu maguey se secó. ¡Haz una actividad para empezar una racha nueva!"
                                    : "Completa una lección, práctica o juego para empezar.";
                            break;
                        default:
                            mensajeRacha = "Completa una lección, práctica o juego para empezar.";
                            break;
                    }

                    txtMensajeRacha.setText(mensajeRacha);
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

        nivelPendiente = siguienteNivel;
        etapaPendiente = siguienteEtapa;
        todoCompleto = etapasCompletadas >= TOTAL_ETAPAS;

        mostrarAprendeHoy();

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
    // CONTINUAR LECCIÓN Y APRENDE HOY
    // ---------------------------------------------

    private void abrirLeccionPendiente(NavController navController) {

        if (!isAdded() || navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.inicioFragment) {
            return;
        }

        // Si ya terminó todo, se muestran todas las lecciones.
        if (todoCompleto) {
            navController.navigate(R.id.leccionesFragment);
            return;
        }

        String nombre = TEMAS[nivelPendiente - 1][0];

        Bundle argumentos = new Bundle();
        argumentos.putInt("numeroNivel", nivelPendiente);
        argumentos.putString("nivelSeleccionado", nombre);
        argumentos.putString("temaSeleccionado", nombre);
        argumentos.putInt("etapaPendiente", etapaPendiente);

        navController.navigate(R.id.etapasLeccionFragment, argumentos);
    }

    private void abrirVocabularioDelTema(NavController navController) {

        if (!isAdded() || navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.inicioFragment) {
            return;
        }

        String categoria = TEMAS[nivelPendiente - 1][1];

        Bundle argumentos = new Bundle();
        argumentos.putString("temaSeleccionado", categoria);
        argumentos.putString("categoriaSeleccionada", categoria);

        navController.navigate(R.id.palabrasVocabularioFragment, argumentos);
    }

    /** La tarjeta "Aprende hoy" muestra el tema de la lección que sigue. */
    private void mostrarAprendeHoy() {

        View raiz = getView();

        if (!isAdded() || raiz == null) return;

        String[] tema = TEMAS[Math.max(0, Math.min(TEMAS.length - 1, nivelPendiente - 1))];

        TextView titulo = raiz.findViewById(R.id.txtTituloAprendeHoy);
        TextView descripcion = raiz.findViewById(R.id.txtDescripcionAprendeHoy);
        TextView duracion = raiz.findViewById(R.id.txtDuracionAprendeHoy);
        ImageView imagen = raiz.findViewById(R.id.imgAprendeHoy);
        View tarjeta = raiz.findViewById(R.id.cardAprendeHoy);

        if (titulo != null) titulo.setText(tema[1] + " en hñähñu");
        if (descripcion != null) descripcion.setText(tema[2]);

        int palabras = VocabularioNube.arreglo(tema[1]).length;

        if (duracion != null) {
            duracion.setText(palabras > 0
                    ? palabras + " palabras · " + Math.max(3, palabras / 4) + " min"
                    : "5 min");
        }

        if (imagen != null) {
            int recurso = getResources().getIdentifier(tema[3], "drawable", requireContext().getPackageName());
            imagen.setImageResource(recurso != 0 ? recurso : R.drawable.ic_manitas);
        }

        if (tarjeta != null) {
            tarjeta.setContentDescription("Aprende hoy: " + tema[1]
                    + " en hñähñu. " + tema[2] + " Toca para ver las palabras.");
        }
    }

    // ---------------------------------------------
    // ACTUALIZAR RACHA
    // ---------------------------------------------

    private void actualizarRacha(ProgresoButi.EstadoRacha estado) {

        int dias = Math.max(0, estado.dias);

        if (dias == 1) {

            txtRacha.setText("Racha de 1 día");

        } else {

            txtRacha.setText("Racha de " + dias + " días");
        }

        if (estado.estado == ProgresoButi.Estado.CONGELADA) {
            txtRacha.setText(txtRacha.getText() + " · en pausa");
        }

        // El maguey crece con la racha; congelado o seco si toca.
        if (imgRacha != null) {
            imgRacha.setImageResource(RachaMaguey.dibujo(estado));
        }
    }

    // ---------------------------------------------
    // AVISO DE RACHA CONGELADA O PERDIDA (una vez)
    // ---------------------------------------------

    private void avisarEstadoRacha(ProgresoButi.EstadoRacha estado) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null || !isAdded()) return;

        String clave;

        if (estado.estado == ProgresoButi.Estado.CONGELADA) {
            // Una vez al día mientras está congelada.
            clave = "aviso_congelada_" + usuario.getUid() + "_" + ProgresoButi.hoy();
        } else if (estado.estado == ProgresoButi.Estado.PERDIDA && estado.diasPerdidos > 0) {
            // Una sola vez por cada racha perdida.
            clave = "aviso_perdida_" + usuario.getUid() + "_" + estado.ultimaFecha;
        } else {
            return;
        }

        android.content.SharedPreferences preferencias = requireContext()
                .getSharedPreferences("buti_prefs", Context.MODE_PRIVATE);

        if (preferencias.getBoolean(clave, false)) return;

        preferencias.edit().putBoolean(clave, true).apply();

        if (estado.estado == ProgresoButi.Estado.CONGELADA) {
            RachaButi.mostrarCongelada(requireContext(), estado.dias, null);
        } else {
            RachaButi.mostrarPerdida(requireContext(), estado.diasPerdidos, estado.mejor, null);
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

        if (navController.getCurrentDestination().getId() == destino) {
            return;
        }

        // Las secciones principales se abren igual que desde el menú.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).irASeccion(destino);
        } else {
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