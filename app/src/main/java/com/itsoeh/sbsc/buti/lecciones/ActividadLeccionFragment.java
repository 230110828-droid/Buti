package com.itsoeh.sbsc.buti.lecciones;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ScrollView;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import com.google.firebase.auth.FirebaseAuth;

import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.ResultadoButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

import java.text.BreakIterator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ActividadLeccionFragment extends Fragment {

    // =========================================================
    // VISTAS
    // =========================================================

    private TextView txtEtapa, txtPuntos, txtNumeroPregunta;
    private TextView txtInstruccion, txtDescripcion;
    private TextView txtPalabra, txtTraduccion;
    private TextView txtPregunta, txtResultado, txtTemporizador;
    private TextView txtPistaReto;
    private LinearLayout contenedorEspacios;

    // Completar palabra: qué botón del banco llenó cada casilla,
    // la última casilla que se llenó (para animarla) y si ya se revisó.
    private final Map<Integer, View> botonPorPosicion = new HashMap<>();
    private int ultimaPosicionColocada = -1;
    private boolean palabraRevisada = false;

    private ImageView imgActividad, imgReto;
    private ImageView imgPersonajeActividad, imgColibriActividad;
    private TextView txtMensajeReaccionActividad;
    private FrameLayout contenedorProgresoColibri;
    private ScrollView scrollActividad;
    private View panelRespuesta;
    private int fondoPanelNormal;

    private ImageButton btnVolver, btnEscuchar;

    private MaterialButton btnContinuar;
    private MaterialButton btnHablar, btnValidarManual;

    private MaterialButton[] botonesOpciones = new MaterialButton[4];

    private ProgressBar barraProgreso;

    private LinearLayout contenedorOpciones;
    private LinearLayout contenedorCompletar;
    private LinearLayout contenedorLetras;
    private LinearLayout contenedorRelacionar;
    private LinearLayout columnaEspanol;
    private LinearLayout columnaHnahnu;

    private View cardRetoEspecial;

    // =========================================================
    // DATOS
    // =========================================================

    private int numeroNivel = 1;
    private int numeroEtapa = 1;

    private String nombreNivel = "Saludos";
    private int etapaPendiente = 0;
    private boolean navegacionAutomaticaRealizada = false;
    private String categoriaAudio;
    private String[][] vocabulario;

    private final List<Integer> preguntas = new ArrayList<>();

    private final List<Integer> preguntasDescubre = new ArrayList<>();

    private int indicePalabra = 0;
    private int indicePregunta = 0;
    private String respuestaCorrectaEvaluacion = "";
    private int aciertos = 0;
    private int puntosObtenidos = 0;
    private int totalDescubre = 0;

    private boolean respondida = false;
    private boolean guardando = false;
    private boolean traduccionVisible = false;
    private boolean esperandoRelacion = false;
    private boolean memoriaMostrada = false;

    private String palabraConstruida = "";
    private String palabraObjetivo = "";

    // Letras de la etapa 2
    private String[] letrasColocadas;
    private boolean[] posicionesFijas;

    private int seleccionEspanol = -1;
    private int seleccionHnahnu = -1;

    private final Set<Integer> parejasCompletadas = new HashSet<>();
    private final List<Integer> palabrasMemoria = new ArrayList<>();
    private final List<String> palabrasParaRepasar = new ArrayList<>();

    // Ronda de repaso: palabras falladas (una sola oportunidad, menos puntos)
    private final List<Integer> palabrasFallidas = new ArrayList<>();
    private boolean enRepaso = false;
    private int indiceRepaso = 0;
    private int aciertosRepaso = 0;
    private ResultadoButi pantallaResultado;

    private int indiceTarjetaMemoria = 0;
    private boolean fasePreguntasMemoria = false;
    private MediaPlayer reproductor;
    private CountDownTimer temporizador;
    private final Handler handlerReacciones = new Handler(Looper.getMainLooper());
    private boolean personajeNina = true;
    private int totalPreguntasVisual = 1;
    private int progresoVisual = 0;
    private int baseRepasoVisual = 0;
    private int respondidasRepaso = 0;
    private final Runnable limpiarReaccion = () -> {
        if (isAdded() && imgPersonajeActividad != null) {
            imgPersonajeActividad.setImageResource(
                    personajeNina ? R.drawable.ic_nina_pensando : R.drawable.ic_nino_pensando
            );
        }
    };

    // =========================================================
    // CONSTANTES
    // =========================================================

    // Puntos: 10 por respuesta correcta y 5 por acierto en el repaso.
    private static final int PUNTOS_ACIERTO = ProgresoButi.PUNTOS_ACIERTO;
    private static final int PUNTOS_REPASO = ProgresoButi.PUNTOS_REPASO;

    private static final int TOTAL_DESCUBRE = 6;
    private static final int TOTAL_RELACIONA = 6;
    private static final int TOTAL_MEMORIA = 4;
    private static final int TOTAL_EVALUACION = 10;

    private static final int DURACION_PREGUNTA = 20000;

    // Colores de la paleta B'ÜTI. Se cargan en cargarColores()
    // para que cambien solos entre modo claro y modo oscuro.
    private int VERDE;          // acierto
    private int ROSA;           // error
    private int CREMA;          // fondo de las letras
    private int CAFE;           // texto principal
    private int DORADO;         // opción seleccionada
    private int FONDO_OPCION;   // fondo normal de las opciones
    private int FONDO_ACIERTO;  // fondo de una opción correcta
    private int FONDO_ERROR;    // fondo de una opción incorrecta
    private int BORDE;          // borde de las tarjetas

    public ActividadLeccionFragment() {
        super(R.layout.fragment_actividad_leccion);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        cargarColores();
        inicializarVistas(view);
        recibirDatos();
        configurarBotonVolver();
        configurarActividad();

        ocultarBarraSuperior(true);
    }

    // En la actividad solo se usa la flecha de regresar: se esconden la cenefa,
    // la barra con el menú de hamburguesa y el menú lateral para ganar espacio.
    private void ocultarBarraSuperior(boolean ocultar) {

        if (getActivity() == null) return;

        View toolbar = getActivity().findViewById(R.id.toolbar);

        if (toolbar != null) {

            int visibilidad = ocultar ? View.GONE : View.VISIBLE;
            toolbar.setVisibility(visibilidad);

            // La cenefa bordada está justo antes de la barra.
            if (toolbar.getParent() instanceof ViewGroup) {
                ViewGroup padre = (ViewGroup) toolbar.getParent();
                int posicion = padre.indexOfChild(toolbar);
                if (posicion > 0) padre.getChildAt(posicion - 1).setVisibility(visibilidad);
            }
        }

        View cajon = getActivity().findViewById(R.id.drawer_layout);

        if (cajon instanceof DrawerLayout) {
            ((DrawerLayout) cajon).setDrawerLockMode(ocultar
                    ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                    : DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    // =========================================================
    // COLORES (CLARO / OSCURO)
    // =========================================================

    private void cargarColores() {

        boolean oscuro = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        VERDE = ContextCompat.getColor(requireContext(), R.color.buti_exito);
        ROSA = ContextCompat.getColor(requireContext(), R.color.nivel_rosa);
        CREMA = ContextCompat.getColor(requireContext(), R.color.buti_superficie_alt);
        CAFE = ContextCompat.getColor(requireContext(), R.color.buti_texto);
        FONDO_OPCION = ContextCompat.getColor(requireContext(), R.color.buti_superficie);
        BORDE = ContextCompat.getColor(requireContext(), R.color.buti_borde);

        DORADO = oscuro ? Color.rgb(122, 92, 20) : Color.rgb(242, 169, 0);
        FONDO_ACIERTO = oscuro ? Color.rgb(47, 74, 34) : Color.rgb(213, 229, 199);
        FONDO_ERROR = oscuro ? Color.rgb(90, 26, 42) : Color.rgb(255, 210, 210);
    }

    // =========================================================
    // INICIALIZACIÓN
    // =========================================================

    private void inicializarVistas(View view) {

        btnVolver = view.findViewById(R.id.btnVolverActividad);

        txtEtapa = view.findViewById(R.id.txtEtapaActividad);
        txtPuntos = view.findViewById(R.id.txtPuntosActividad);
        txtNumeroPregunta = view.findViewById(R.id.txtNumeroPregunta);

        txtInstruccion = view.findViewById(R.id.txtInstruccionActividad);
        txtDescripcion = view.findViewById(R.id.txtDescripcionActividad);

        imgActividad = view.findViewById(R.id.imgActividad);
        imgReto = view.findViewById(R.id.imgRetoActividad);
        imgPersonajeActividad = view.findViewById(R.id.imgPersonajeActividad);
        imgColibriActividad = view.findViewById(R.id.imgColibriActividad);
        txtMensajeReaccionActividad = view.findViewById(R.id.txtMensajeReaccionActividad);
        contenedorProgresoColibri = view.findViewById(R.id.contenedorProgresoColibri);
        scrollActividad = view.findViewById(R.id.scrollActividad);
        panelRespuesta = view.findViewById(R.id.panelRespuestaActividad);
        fondoPanelNormal = ContextCompat.getColor(requireContext(), R.color.buti_fondo);

        // Cuando ya se conoce el ancho de la barra, colocar el colibrí en su lugar.
        if (contenedorProgresoColibri != null) {
            contenedorProgresoColibri.post(() -> actualizarColibriActividad(false));
        }

        txtPalabra = view.findViewById(R.id.txtPalabraActividad);
        txtTraduccion = view.findViewById(R.id.txtTraduccionActividad);

        btnEscuchar = view.findViewById(R.id.btnEscucharActividad);
        btnHablar = view.findViewById(R.id.btnHablarActividad);
        btnValidarManual = view.findViewById(R.id.btnValidarManualActividad);

        txtPregunta = view.findViewById(R.id.txtPreguntaActividad);
        txtResultado = view.findViewById(R.id.txtResultadoActividad);
        txtTemporizador = view.findViewById(R.id.txtTemporizadorActividad);

        contenedorEspacios = view.findViewById(R.id.contenedorEspaciosPalabra);
        txtPistaReto = view.findViewById(R.id.txtPistaRetoActividad);

        barraProgreso = view.findViewById(R.id.barraProgresoActividad);

        botonesOpciones[0] = view.findViewById(R.id.btnOpcion1);
        botonesOpciones[1] = view.findViewById(R.id.btnOpcion2);
        botonesOpciones[2] = view.findViewById(R.id.btnOpcion3);
        botonesOpciones[3] = view.findViewById(R.id.btnOpcion4);

        contenedorOpciones = view.findViewById(R.id.contenedorOpcionesActividad);
        contenedorCompletar = view.findViewById(R.id.contenedorCompletarActividad);
        contenedorLetras = view.findViewById(R.id.contenedorLetrasActividad);

        contenedorRelacionar = view.findViewById(R.id.contenedorRelacionarActividad);
        columnaEspanol = view.findViewById(R.id.columnaEspanolActividad);
        columnaHnahnu = view.findViewById(R.id.columnaHnahnuActividad);

        cardRetoEspecial = view.findViewById(R.id.cardRetoEspecialActividad);

        btnContinuar = view.findViewById(R.id.btnContinuarActividad);
    }

    private void recibirDatos() {

        Bundle argumentos = getArguments();

        if (argumentos != null) {

            numeroNivel = argumentos.getInt("numeroNivel", 1);
            numeroEtapa = argumentos.getInt("etapaSeleccionada", 1);

            String nombre = argumentos.getString("nivelSeleccionado");

            if (nombre != null && !nombre.trim().isEmpty()) {
                nombreNivel = nombre;
            }
        }

        if (numeroNivel < 1 || numeroNivel > 11) {
            numeroNivel = 1;
        }

        if (numeroEtapa < 1 || numeroEtapa > 5) {
            numeroEtapa = 1;
        }
    }

    private void configurarBotonVolver() {

        btnVolver.setOnClickListener(v -> {
            detenerTemporizador();
            detenerAudio();
            Navigation.findNavController(v).popBackStack();
        });
    }

    // =========================================================
    // CONFIGURACIÓN GENERAL
    // =========================================================

    private void configurarActividad() {

        detenerTemporizador();
        detenerAudio();

        indicePregunta = 0;
        indicePalabra = 0;
        aciertos = 0;
        puntosObtenidos = 0;
        totalDescubre = 0;
        indiceTarjetaMemoria = 0;
        fasePreguntasMemoria = false;

        respondida = false;
        guardando = false;
        traduccionVisible = false;
        esperandoRelacion = false;
        memoriaMostrada = false;

        palabraConstruida = "";
        palabraObjetivo = "";

        letrasColocadas = null;
        posicionesFijas = null;

        seleccionEspanol = -1;
        seleccionHnahnu = -1;

        parejasCompletadas.clear();
        palabrasMemoria.clear();
        preguntasDescubre.clear();
        palabrasParaRepasar.clear();
        palabrasFallidas.clear();
        enRepaso = false;
        indiceRepaso = 0;
        aciertosRepaso = 0;

        if (pantallaResultado != null) {
            pantallaResultado.cerrar();
            pantallaResultado = null;
        }

        txtEtapa.setText("ETAPA " + numeroEtapa);
        txtPuntos.setText("0");
        personajeNina = (numeroNivel % 2 != 0);
        progresoVisual = 0;
        totalPreguntasVisual = obtenerTotalPreguntasEtapa();
        actualizarColibriActividad();
        mostrarPersonajePensando();

        reiniciarPanelRespuesta();

        txtTraduccion.setVisibility(View.GONE);
        txtTemporizador.setVisibility(View.GONE);

        txtPalabra.setText("¡Aprende algo nuevo!");

        imgActividad.setVisibility(View.GONE);
        imgReto.setVisibility(View.GONE);

        btnEscuchar.setVisibility(View.VISIBLE);
        btnHablar.setVisibility(View.GONE);
        btnValidarManual.setVisibility(View.GONE);

        contenedorOpciones.setVisibility(View.VISIBLE);
        contenedorCompletar.setVisibility(View.GONE);
        contenedorRelacionar.setVisibility(View.GONE);
        cardRetoEspecial.setVisibility(View.GONE);

        contenedorLetras.removeAllViews();
        columnaEspanol.removeAllViews();
        columnaHnahnu.removeAllViews();

        ocultarOpciones();

        btnContinuar.setEnabled(true);
        btnContinuar.setOnClickListener(null);

        if (!cargarVocabulario()) {
            mostrarSinVocabulario();
            return;
        }

        prepararIndices();
        totalPreguntasVisual = obtenerTotalPreguntasEtapa();
        progresoVisual = 0;
        actualizarColibriActividad();

        if (preguntas.size() < 4) {
            mostrarSinVocabulario();
            return;
        }

        switch (numeroEtapa) {

            case 1:
                iniciarDescubre();
                break;

            case 2:
                iniciarCompletar();
                break;

            case 3:
                iniciarRelaciona();
                break;

            case 4:
                iniciarRetoMemoria();
                break;

            case 5:
                iniciarEvaluacion();
                break;

            default:
                mostrarSinVocabulario();
                break;
        }
    }

    // =========================================================
    // VOCABULARIO
    // =========================================================

    private boolean cargarVocabulario() {

        switch (numeroNivel) {

            case 1:
                vocabulario = VocabularioNube.arreglo("saludos");
                categoriaAudio = "Saludos";
                break;

            case 2:
                vocabulario = VocabularioNube.arreglo("numeros");
                categoriaAudio = "Números";
                break;

            case 3:
                vocabulario = VocabularioNube.arreglo("colores");
                categoriaAudio = "Colores";
                break;

            case 4:
                vocabulario = VocabularioNube.arreglo("animales");
                categoriaAudio = "Animales";
                break;

            case 5:
                vocabulario = VocabularioNube.arreglo("plantas");
                categoriaAudio = "Plantas";
                break;

            case 6:
                vocabulario = VocabularioNube.arreglo("objetos");
                categoriaAudio = "Objetos";
                break;

            case 7:
                vocabulario = VocabularioNube.arreglo("cuerpo");
                categoriaAudio = "Cuerpo";
                break;

            case 8:
                vocabulario = VocabularioNube.arreglo("comida");
                categoriaAudio = "Comida";
                break;

            case 9:
                vocabulario = VocabularioNube.arreglo("tiempo");
                categoriaAudio = "Tiempo";
                break;

            case 10:
                vocabulario = VocabularioNube.arreglo("naturaleza");
                categoriaAudio = "Naturaleza";
                break;

            case 11:
                vocabulario = VocabularioNube.arreglo("lugares");
                categoriaAudio = "Lugares";
                break;

            default:
                vocabulario = null;
                categoriaAudio = null;
                return false;
        }

        return vocabulario != null;
    }

    private void prepararIndices() {

        preguntas.clear();

        if (vocabulario == null) return;

        for (int i = 0; i < vocabulario.length; i++) {

            if (vocabulario[i] != null
                    && vocabulario[i].length >= 2
                    && vocabulario[i][0] != null
                    && vocabulario[i][1] != null
                    && !vocabulario[i][0].trim().isEmpty()
                    && !vocabulario[i][1].trim().isEmpty()) {

                preguntas.add(i);
            }
        }

        Collections.shuffle(preguntas);
    }

    private void mostrarSinVocabulario() {

        detenerTemporizador();
        detenerAudio();

        txtInstruccion.setText("LECCIÓN NO DISPONIBLE");

        txtDescripcion.setText(
                "Todavía no tenemos suficiente vocabulario para esta categoría."
        );

        txtPalabra.setText(nombreNivel);
        txtTraduccion.setVisibility(View.GONE);

        imgActividad.setVisibility(View.GONE);
        btnEscuchar.setVisibility(View.GONE);
        btnHablar.setVisibility(View.GONE);
        btnValidarManual.setVisibility(View.GONE);

        contenedorOpciones.setVisibility(View.GONE);
        contenedorCompletar.setVisibility(View.GONE);
        contenedorRelacionar.setVisibility(View.GONE);
        cardRetoEspecial.setVisibility(View.GONE);

        txtPregunta.setText(
                "Esta actividad estará disponible próximamente."
        );

        mostrarResultado("Puedes regresar a las etapas.", CAFE);

        btnContinuar.setText("Volver");
        btnContinuar.setEnabled(true);
        btnContinuar.setOnClickListener(v -> volverAEtapas());
    }

    // =========================================================
    // HERRAMIENTAS DE INTERFAZ
    // =========================================================

    private void mostrarResultado(String mensaje, int color) {

        // Los mensajes en café son solo indicaciones: no son ni acierto ni error.
        if (color == CAFE) {
            mostrarResultadoDirecto(mensaje);
            return;
        }

        boolean acierto = color == VERDE;

        txtResultado.setVisibility(View.VISIBLE);
        txtResultado.setText(mensaje);
        txtResultado.setTextColor(color);

        // El panel de abajo se pinta verde (bien) o rosa (mal) y "sube".
        pintarPanelRespuesta(acierto ? FONDO_ACIERTO : FONDO_ERROR, true);

        mostrarReaccionActividad(acierto, mensaje);
    }

    // Panel fijo de abajo: color de fondo y pequeña animación de entrada.
    private void pintarPanelRespuesta(int colorFondo, boolean animar) {

        if (panelRespuesta == null) return;

        panelRespuesta.setBackgroundColor(colorFondo);

        if (animar) {
            panelRespuesta.animate().cancel();
            panelRespuesta.setTranslationY(dp(24));
            panelRespuesta.setAlpha(0.6f);
            panelRespuesta.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(280)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    // Deja el panel limpio y sube el contenido al inicio para la siguiente pregunta.
    private void reiniciarPanelRespuesta() {

        if (txtResultado != null) {
            txtResultado.setVisibility(View.GONE);
            txtResultado.setText("");
        }

        if (panelRespuesta != null) {
            panelRespuesta.animate().cancel();
            panelRespuesta.setTranslationY(0f);
            panelRespuesta.setAlpha(1f);
            panelRespuesta.setBackgroundColor(fondoPanelNormal);
        }

        if (scrollActividad != null) {
            scrollActividad.post(() -> scrollActividad.scrollTo(0, 0));
        }
    }

    // Guarda las palabras que presentaron dificultad durante la etapa.
    private void registrarPalabraParaRepasar(int indice) {

        if (indice < 0 || vocabulario == null || indice >= vocabulario.length) {
            return;
        }

        String espanol = vocabulario[indice][0];
        String hnahnu = primeraVariante(vocabulario[indice][1]);
        String registro = espanol + " — " + hnahnu;

        if (!palabrasParaRepasar.contains(registro)) {
            palabrasParaRepasar.add(registro);
        }

        // También entra a la ronda de repaso del final.
        if (!palabrasFallidas.contains(indice)) {
            palabrasFallidas.add(indice);
        }
    }

    // Suma puntos por una respuesta correcta y actualiza el marcador.
    private void sumarAcierto() {
        aciertos++;
        // El colibrí solo avanza cuando la respuesta es correcta.
        registrarAvanceVisual();
        puntosObtenidos += PUNTOS_ACIERTO;
        txtPuntos.setText(String.valueOf(puntosObtenidos));
        animarMarcador();
    }

    private void animarMarcador() {
        if (txtPuntos == null) return;
        txtPuntos.animate().cancel();
        txtPuntos.setScaleX(1.35f);
        txtPuntos.setScaleY(1.35f);
        txtPuntos.animate().scaleX(1f).scaleY(1f).setDuration(260).start();
    }

    // Avanza el colibrí únicamente cuando se completa una actividad.
    // Los mensajes informativos y los intentos incorrectos no cuentan como avance.
    private void registrarAvanceVisual() {
        int total = Math.max(1, totalPreguntasVisual);
        progresoVisual = Math.min(progresoVisual + 1, total);
        actualizarColibriActividad(true);
    }

    // Reparte lo que le falta al colibrí entre las palabras del repaso,
    // así con la última respuesta llega justo a la meta.
    private void avanzarRepasoVisual() {
        int total = Math.max(1, totalPreguntasVisual);
        int palabras = Math.max(1, palabrasFallidas.size());
        respondidasRepaso = Math.min(respondidasRepaso + 1, palabras);
        int faltante = Math.max(0, total - baseRepasoVisual);
        progresoVisual = baseRepasoVisual
                + Math.round(faltante * (respondidasRepaso / (float) palabras));
        progresoVisual = Math.min(progresoVisual, total);
        actualizarColibriActividad(true);
    }

    private int obtenerTotalPreguntasEtapa() {
        switch (numeroEtapa) {
            case 1:
                return Math.min(TOTAL_DESCUBRE, preguntas.size());

            case 2:
                return Math.min(TOTAL_DESCUBRE, preguntas.size());

            case 3:
                return Math.max(1, obtenerTotalRelacion());

            case 4:
                return Math.min(TOTAL_MEMORIA, preguntas.size());

            case 5:
                return TOTAL_EVALUACION;

            default:
                return 1;
        }
    }

    private void mostrarPersonajePensando() {
        if (imgPersonajeActividad == null) return;
        imgPersonajeActividad.setImageResource(
                personajeNina ? R.drawable.ic_nina_pensando : R.drawable.ic_nino_pensando
        );
        if (txtMensajeReaccionActividad != null) {
            txtMensajeReaccionActividad.setText("¡Vamos paso a paso! Escucha, observa y aprende.");
        }
    }

    private void mostrarReaccionActividad(boolean acierto, String mensaje) {
        if (imgPersonajeActividad == null) return;
        handlerReacciones.removeCallbacks(limpiarReaccion);
        imgPersonajeActividad.setImageResource(acierto
                ? (personajeNina ? R.drawable.ic_nina_feliz : R.drawable.ic_nino_feliz)
                : (personajeNina ? R.drawable.ic_nina_animando : R.drawable.ic_nino_animando));
        if (txtMensajeReaccionActividad != null) {
            txtMensajeReaccionActividad.setText(acierto
                    ? "¡Muy bien! Cada respuesta te acerca a la meta."
                    : "¡No te rindas! Equivocarse también ayuda a aprender.");
        }
        // La personita "brinca" de gusto o se sacude para animarte.
        imgPersonajeActividad.animate().cancel();
        imgPersonajeActividad.setTranslationX(0f);
        imgPersonajeActividad.setTranslationY(0f);
        imgPersonajeActividad.setRotation(0f);
        imgPersonajeActividad.setScaleX(0.75f);
        imgPersonajeActividad.setScaleY(0.75f);

        if (acierto) {
            imgPersonajeActividad.animate()
                    .scaleX(1.12f).scaleY(1.12f)
                    .translationY(-dp(10))
                    .setDuration(220)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> imgPersonajeActividad.animate()
                            .scaleX(1f).scaleY(1f)
                            .translationY(0f)
                            .setDuration(260)
                            .setInterpolator(new OvershootInterpolator(2.5f))
                            .start())
                    .start();
        } else {
            imgPersonajeActividad.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(180)
                    .withEndAction(() -> sacudir(imgPersonajeActividad, 0))
                    .start();
        }

        // La burbuja del mensaje también aparece con un pequeño salto.
        if (txtMensajeReaccionActividad != null) {
            txtMensajeReaccionActividad.setAlpha(0f);
            txtMensajeReaccionActividad.setTranslationX(dp(12));
            txtMensajeReaccionActividad.animate()
                    .alpha(1f).translationX(0f)
                    .setDuration(260)
                    .start();
        }

        // Se queda un rato para que se alcance a ver.
        handlerReacciones.postDelayed(limpiarReaccion, 3000);
    }

    private void sacudir(View vista, int paso) {
        if (vista == null || !isAdded()) return;
        float[] movimientos = {-dp(8), dp(8), -dp(5), dp(5), 0};
        if (paso >= movimientos.length) return;
        vista.animate()
                .translationX(movimientos[paso])
                .setDuration(60)
                .withEndAction(() -> sacudir(vista, paso + 1))
                .start();
    }

    private void actualizarColibriActividad() {
        actualizarColibriActividad(false);
    }

    private void actualizarColibriActividad(boolean animar) {
        if (barraProgreso == null || imgColibriActividad == null) return;
        int total = Math.max(1, totalPreguntasVisual);
        barraProgreso.setMax(total);
        barraProgreso.setProgress(Math.min(progresoVisual, total));
        int recurso;
        if (progresoVisual <= 0) recurso = R.drawable.ic_colibri_normal;
        else if (progresoVisual >= total) recurso = R.drawable.ic_colibri_meta;
        else recurso = R.drawable.ic_colibri_volando;
        imgColibriActividad.setImageResource(recurso);
        if (contenedorProgresoColibri != null) {
            float recorrido = Math.max(0, contenedorProgresoColibri.getWidth()
                    - imgColibriActividad.getWidth());
            float fraccion = total == 0 ? 0f : progresoVisual / (float) total;
            float destino = recorrido * fraccion;

            imgColibriActividad.animate().cancel();

            if (animar && recorrido > 0) {
                // El colibrí vuela hasta su nuevo lugar dando un saltito.
                imgColibriActividad.setTranslationY(0f);
                imgColibriActividad.animate()
                        .translationX(destino)
                        .translationY(-dp(8))
                        .scaleX(1.25f).scaleY(1.25f)
                        .setDuration(450)
                        .setInterpolator(new DecelerateInterpolator())
                        .withEndAction(() -> imgColibriActividad.animate()
                                .translationY(0f)
                                .scaleX(1f).scaleY(1f)
                                .setDuration(280)
                                .setInterpolator(new OvershootInterpolator(3f))
                                .start())
                        .start();
            } else {
                imgColibriActividad.setTranslationX(destino);
                imgColibriActividad.setTranslationY(0f);
                imgColibriActividad.setScaleX(1f);
                imgColibriActividad.setScaleY(1f);
            }
        }
    }

    private void ocultarOpciones() {

        for (MaterialButton boton : botonesOpciones) {

            if (boton != null) {
                boton.setVisibility(View.GONE);
                boton.setEnabled(false);
                boton.setOnClickListener(null);
            }
        }
    }

    private void mostrarOpcionesGenericas() {

        contenedorOpciones.setVisibility(View.VISIBLE);
        ocultarOpciones();
    }

    private void bloquearOpciones() {

        for (MaterialButton boton : botonesOpciones) {
            if (boton != null) {
                boton.setEnabled(false);
            }
        }
    }

    private void prepararBotonOpcion(
            MaterialButton boton,
            String texto,
            View.OnClickListener listener
    ) {

        boton.setText(texto);
        boton.setVisibility(View.VISIBLE);
        boton.setEnabled(true);

        boton.setBackgroundTintList(
                ColorStateList.valueOf(FONDO_OPCION)
        );

        boton.setTextColor(CAFE);
        boton.setOnClickListener(listener);
    }

    // =========================================================
    // ETAPA 1: ESCUCHA Y DESCUBRE
    // =========================================================

    private void iniciarDescubre() {

        txtInstruccion.setText("ESCUCHA Y DESCUBRE");

        txtDescripcion.setText(
                "Escucha la palabra en Hñähñu y selecciona la imagen que representa lo que escuchaste."
        );

        imgActividad.setVisibility(View.GONE);
        txtTraduccion.setVisibility(View.GONE);

        btnHablar.setVisibility(View.GONE);
        btnValidarManual.setVisibility(View.GONE);

        btnEscuchar.setVisibility(View.VISIBLE);

        contenedorOpciones.setVisibility(View.VISIBLE);

        for (int indice : preguntas) {

            String espanol = vocabulario[indice][0];

            int imagen = obtenerRecursoImagen(espanol);

            boolean audio = VocabularioNube.tieneAudio(
                    requireContext(),
                    categoriaAudio,
                    espanol
            );

            if (imagen != 0 && audio) {
                preguntasDescubre.add(indice);
            }
        }

        Collections.shuffle(preguntasDescubre);

        totalDescubre = Math.min(
                TOTAL_DESCUBRE,
                preguntasDescubre.size()
        );

        if (preguntasDescubre.size() < 4) {

            mostrarResultado(
                    "No hay suficientes imágenes y audios disponibles para esta actividad.",
                    ROSA
            );

            txtPalabra.setText("Actividad no disponible");

            txtPregunta.setText(
                    "Se necesitan al menos cuatro palabras con imagen y audio."
            );

            btnEscuchar.setVisibility(View.GONE);
            contenedorOpciones.setVisibility(View.GONE);

            btnContinuar.setText("Volver a etapas");
            btnContinuar.setEnabled(true);
            btnContinuar.setOnClickListener(v -> volverAEtapas());

            return;
        }

        mostrarPreguntaDescubre();
    }

    private void mostrarPreguntaDescubre() {

        reiniciarPanelRespuesta();

        detenerAudio();

        if (indicePregunta >= totalDescubre) {
            terminarEjercicio();
            return;
        }

        respondida = false;

        indicePalabra = preguntasDescubre.get(indicePregunta);

        txtNumeroPregunta.setText(
                "Pregunta " + (indicePregunta + 1)
                        + " de " + totalDescubre
        );

        barraProgreso.setMax(totalDescubre);
        barraProgreso.setProgress(indicePregunta);

        txtPalabra.setText("¿Qué imagen escuchas?");

        txtTraduccion.setVisibility(View.GONE);

        txtPregunta.setText(
                "Presiona el botón de audio y selecciona la imagen correcta."
        );

        mostrarResultado(
                "Escucha con atención antes de responder.",
                CAFE
        );

        btnEscuchar.setVisibility(View.VISIBLE);
        btnEscuchar.setOnClickListener(v -> reproducirAudio());

        crearOpcionesImagen(indicePalabra);

        btnContinuar.setText("Escucha y selecciona");
        btnContinuar.setEnabled(false);
    }

    private void crearOpcionesImagen(int indiceCorrecto) {

        contenedorOpciones.removeAllViews();
        contenedorOpciones.setVisibility(View.VISIBLE);

        List<Integer> opciones = new ArrayList<>();
        opciones.add(indiceCorrecto);

        List<Integer> candidatos = new ArrayList<>(preguntasDescubre);

        candidatos.remove(Integer.valueOf(indiceCorrecto));

        Collections.shuffle(candidatos);

        for (int indice : candidatos) {

            if (opciones.size() >= 4) break;

            int imagen = obtenerRecursoImagen(vocabulario[indice][0]);

            if (imagen != 0 && !opciones.contains(indice)) {
                opciones.add(indice);
            }
        }

        Collections.shuffle(opciones);

        LinearLayout fila = null;

        for (int i = 0; i < opciones.size(); i++) {

            if (i % 2 == 0) {

                fila = new LinearLayout(requireContext());

                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams parametrosFila =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                parametrosFila.setMargins(0, 5, 0, 5);

                fila.setLayoutParams(parametrosFila);

                contenedorOpciones.addView(fila);
            }

            final int opcion = opciones.get(i);

            int recurso = obtenerRecursoImagen(
                    vocabulario[opcion][0]
            );

            MaterialCardView tarjeta = new MaterialCardView(
                    requireContext()
            );

            LinearLayout.LayoutParams parametrosTarjeta =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(145),
                            1
                    );

            parametrosTarjeta.setMargins(
                    dp(5),
                    dp(4),
                    dp(5),
                    dp(4)
            );

            tarjeta.setLayoutParams(parametrosTarjeta);
            tarjeta.setRadius(dp(18));
            tarjeta.setCardElevation(dp(3));
            tarjeta.setStrokeWidth(dp(1));
            tarjeta.setStrokeColor(BORDE);
            tarjeta.setCardBackgroundColor(FONDO_OPCION);
            tarjeta.setClickable(true);
            tarjeta.setFocusable(true);

            ImageView imagen = new ImageView(requireContext());

            imagen.setLayoutParams(
                    new MaterialCardView.LayoutParams(
                            MaterialCardView.LayoutParams.MATCH_PARENT,
                            MaterialCardView.LayoutParams.MATCH_PARENT
                    )
            );

            imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imagen.setPadding(dp(10), dp(10), dp(10), dp(10));
            imagen.setImageResource(recurso);
            imagen.setContentDescription("Opción de imagen");

            tarjeta.addView(imagen);
            tarjeta.setTag(opcion);

            tarjeta.setOnClickListener(v ->
                    responderDescubre(opcion, indiceCorrecto, tarjeta)
            );

            fila.addView(tarjeta);
        }
    }

    private void responderDescubre(
            int opcion,
            int correcta,
            MaterialCardView tarjeta
    ) {

        if (respondida) return;

        // Una sola oportunidad: bien o mal, se pasa a la siguiente.
        respondida = true;

        String espanol = vocabulario[correcta][0];
        String hnahnu = primeraVariante(vocabulario[correcta][1]);

        if (opcion == correcta) {

            sumarAcierto();

            tarjeta.setStrokeColor(VERDE);
            tarjeta.setStrokeWidth(dp(3));

            mostrarResultado(
                    "¡Correcto! +" + PUNTOS_ACIERTO + " puntos. «" + hnahnu
                            + "» significa «" + espanol + "».",
                    VERDE
            );

        } else {

            registrarPalabraParaRepasar(correcta);

            tarjeta.setStrokeColor(ROSA);
            tarjeta.setStrokeWidth(dp(3));
            marcarTarjetaCorrecta(correcta);

            mostrarResultado(
                    "Incorrecto. Lo que escuchaste fue «" + hnahnu
                            + "», que significa «" + espanol
                            + "». La verás otra vez en el repaso.",
                    ROSA
            );
        }

        txtTraduccion.setText(hnahnu + " = " + espanol);
        txtTraduccion.setVisibility(View.VISIBLE);

        bloquearTarjetasImagen();

        btnContinuar.setText(
                indicePregunta == totalDescubre - 1
                        ? "Terminar etapa"
                        : "Siguiente palabra"
        );

        btnContinuar.setEnabled(true);

        btnContinuar.setOnClickListener(v -> {
            indicePregunta++;
            mostrarPreguntaDescubre();
        });
    }

    // Resalta en verde la imagen correcta cuando el usuario se equivoca.
    private void marcarTarjetaCorrecta(int correcta) {

        for (int i = 0; i < contenedorOpciones.getChildCount(); i++) {

            View fila = contenedorOpciones.getChildAt(i);

            if (!(fila instanceof LinearLayout)) continue;

            LinearLayout grupo = (LinearLayout) fila;

            for (int j = 0; j < grupo.getChildCount(); j++) {

                View hijo = grupo.getChildAt(j);

                if (hijo instanceof MaterialCardView
                        && Integer.valueOf(correcta).equals(hijo.getTag())) {

                    ((MaterialCardView) hijo).setStrokeColor(VERDE);
                    ((MaterialCardView) hijo).setStrokeWidth(dp(4));
                }
            }
        }
    }

    private void bloquearTarjetasImagen() {

        for (int i = 0; i < contenedorOpciones.getChildCount(); i++) {

            View fila = contenedorOpciones.getChildAt(i);

            if (fila instanceof LinearLayout) {

                LinearLayout grupo = (LinearLayout) fila;

                for (int j = 0; j < grupo.getChildCount(); j++) {
                    grupo.getChildAt(j).setEnabled(false);
                }
            }
        }
    }

    private int dp(float valor) {

        return (int) (
                valor * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    // =========================================================
    // ETAPA 2: COMPLETA PALABRAS
    // =========================================================

    private void iniciarCompletar() {

        txtInstruccion.setText("COMPLETA PALABRAS");

        txtDescripcion.setText(
                "Observa la palabra en español y construye su traducción en Hñähñu. Algunas letras ya estarán colocadas como ayuda."
        );

        btnEscuchar.setVisibility(View.VISIBLE);
        btnEscuchar.setOnClickListener(v -> reproducirAudio());

        contenedorOpciones.setVisibility(View.GONE);
        contenedorCompletar.setVisibility(View.VISIBLE);

        mostrarPalabraIncompleta();
    }

    private void mostrarPalabraIncompleta() {

        reiniciarPanelRespuesta();

        int total = Math.min(TOTAL_DESCUBRE, preguntas.size());

        if (indicePregunta >= total) {
            terminarEjercicio();
            return;
        }

        detenerAudio();

        indicePalabra = preguntas.get(indicePregunta);

        palabraObjetivo = primeraVariante(
                vocabulario[indicePalabra][1]
        );

        palabraObjetivo = Normalizer.normalize(
                palabraObjetivo,
                Normalizer.Form.NFC
        ).toLowerCase(Locale.ROOT).replace(" ", "");

        palabraConstruida = "";
        respondida = false;

        // Preparar las letras que aparecerán como ayuda.
        prepararLetrasConAyuda();

        txtNumeroPregunta.setText(
                "Palabra " + (indicePregunta + 1) + " de " + total
        );

        barraProgreso.setMax(total);
        barraProgreso.setProgress(indicePregunta);

        txtPalabra.setText(vocabulario[indicePalabra][0]);
        txtTraduccion.setVisibility(View.GONE);

        configurarImagen(vocabulario[indicePalabra][0]);

        txtPregunta.setText("Forma la palabra en Hñähñu.");

        actualizarEspacios();
        crearBancoLetras();

        mostrarResultado(
                "Selecciona las letras que faltan para completar la palabra.",
                CAFE
        );

        btnContinuar.setText("Comprobar palabra");
        btnContinuar.setEnabled(true);
        btnContinuar.setOnClickListener(v -> comprobarPalabra());
    }

    // Separa la palabra respetando los caracteres y acentos.
    private List<String> separarGrafemas(String texto) {

        List<String> resultado = new ArrayList<>();

        BreakIterator iterador =
                BreakIterator.getCharacterInstance(Locale.ROOT);

        iterador.setText(texto);

        int inicio = iterador.first();

        for (int fin = iterador.next();
             fin != BreakIterator.DONE;
             inicio = fin, fin = iterador.next()) {

            resultado.add(texto.substring(inicio, fin));
        }

        return resultado;
    }

    private String primeraVariante(String texto) {

        if (texto == null) return "";

        int coma = texto.indexOf(',');

        return (coma >= 0
                ? texto.substring(0, coma)
                : texto).trim();
    }

    // =========================================================
    // PREPARAR LETRAS DE AYUDA
    // =========================================================

    private void prepararLetrasConAyuda() {

        List<String> letras = separarGrafemas(palabraObjetivo);

        int cantidad = letras.size();

        palabraRevisada = false;
        ultimaPosicionColocada = -1;

        letrasColocadas = new String[cantidad];
        posicionesFijas = new boolean[cantidad];

        if (cantidad <= 1) {
            return;
        }

        // Aproximadamente el 30% de las letras aparecerán colocadas.
        int cantidadAyuda = Math.round(cantidad * 0.30f);

        cantidadAyuda = Math.max(1, cantidadAyuda);
        cantidadAyuda = Math.min(cantidad - 1, cantidadAyuda);

        // Mezclar posiciones para que las pistas no siempre
        // aparezcan al principio de la palabra.
        List<Integer> posiciones = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            posiciones.add(i);
        }

        Collections.shuffle(posiciones);

        // Colocar las letras de ayuda en su posición original.
        for (int i = 0; i < cantidadAyuda; i++) {

            int posicion = posiciones.get(i);

            letrasColocadas[posicion] = letras.get(posicion);
            posicionesFijas[posicion] = true;
        }
    }

    // =========================================================
    // MOSTRAR ESPACIOS Y LETRAS COLOCADAS
    // =========================================================

    private void actualizarEspacios() {

        if (letrasColocadas == null || contenedorEspacios == null) {
            return;
        }

        contenedorEspacios.removeAllViews();

        List<String> correctas = separarGrafemas(palabraObjetivo == null ? "" : palabraObjetivo);
        StringBuilder construida = new StringBuilder();

        int cantidad = letrasColocadas.length;
        int colocadas = 0;
        int siguienteVacia = -1;

        for (int i = 0; i < cantidad; i++) {
            if (letrasColocadas[i] != null) {
                construida.append(letrasColocadas[i]);
                colocadas++;
            } else if (siguienteVacia == -1) {
                siguienteVacia = i;
            }
        }

        palabraConstruida = construida.toString();

        // Repartir las casillas en filas parejas que quepan en la pantalla.
        int anchoCasilla = dp(42);
        int separacion = dp(3);
        int porFila = calcularPorFila(anchoCasilla + separacion * 2, dp(56));
        int filas = (int) Math.ceil(cantidad / (double) porFila);
        int enCadaFila = (int) Math.ceil(cantidad / (double) Math.max(1, filas));

        LinearLayout fila = null;

        for (int i = 0; i < cantidad; i++) {

            if (i % enCadaFila == 0) {
                fila = new LinearLayout(requireContext());
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(Gravity.CENTER);
                fila.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                contenedorEspacios.addView(fila);
            }

            String letra = letrasColocadas[i];
            boolean fija = posicionesFijas != null && posicionesFijas[i];
            Boolean bien = null;

            if (palabraRevisada && letra != null && i < correctas.size()) {
                bien = Normalizer.normalize(letra, Normalizer.Form.NFC)
                        .equalsIgnoreCase(Normalizer.normalize(correctas.get(i), Normalizer.Form.NFC));
            }

            TextView casilla = crearCasilla(letra, fija, i == siguienteVacia && !palabraRevisada, bien);

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(anchoCasilla, dp(52));
            parametros.setMargins(separacion, separacion, separacion, separacion);
            casilla.setLayoutParams(parametros);

            // Tocar una letra puesta por el alumno la regresa al banco.
            final int posicion = i;
            if (letra != null && !fija && !palabraRevisada) {
                casilla.setOnClickListener(v -> quitarLetra(posicion));
            }

            fila.addView(casilla);

            // La letra recién puesta "salta" a su casilla.
            if (i == ultimaPosicionColocada) {
                casilla.setScaleX(0.6f);
                casilla.setScaleY(0.6f);
                casilla.animate().scaleX(1f).scaleY(1f).setDuration(240)
                        .setInterpolator(new OvershootInterpolator(3f)).start();
            }
        }

        ultimaPosicionColocada = -1;

        txtPregunta.setText(
                "Letras colocadas: " + colocadas
                        + " de " + cantidad
        );
    }

    // Una casilla con diseño de ficha: pista, vacía, siguiente, colocada o revisada.
    private TextView crearCasilla(String letra, boolean fija, boolean siguiente, Boolean bien) {

        TextView casilla = new TextView(requireContext());
        casilla.setGravity(Gravity.CENTER);
        casilla.setTextSize(22);
        casilla.setTypeface(Typeface.DEFAULT_BOLD);
        casilla.setIncludeFontPadding(false);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setCornerRadius(dp(12));

        int texto = CAFE;

        if (bien != null) {
            // Después de comprobar: verde si va bien en ese lugar, rosa si no.
            fondo.setColor(bien ? FONDO_ACIERTO : FONDO_ERROR);
            fondo.setStroke(dp(2), bien ? VERDE : ROSA);
            texto = bien ? VERDE : ROSA;
            casilla.setText(letra);

        } else if (fija) {
            // Letra de pista: ya viene puesta, más tenue y sin borde.
            fondo.setColor(ContextCompat.getColor(requireContext(), R.color.buti_superficie_alt));
            texto = ContextCompat.getColor(requireContext(), R.color.buti_texto_secundario);
            casilla.setText(letra);

        } else if (letra != null) {
            // Letra que puso el alumno.
            fondo.setColor(FONDO_OPCION);
            fondo.setStroke(dp(2), doradoBorde());
            casilla.setText(letra.trim().isEmpty() ? "␣" : letra);

        } else if (siguiente) {
            // La casilla que se va a llenar: punteada en dorado.
            int dorado = doradoBorde();
            fondo.setColor(Color.argb(45, Color.red(dorado), Color.green(dorado), Color.blue(dorado)));
            fondo.setStroke(dp(2), dorado, dp(5), dp(4));
            casilla.setText("");

        } else {
            // Casilla vacía: solo un contorno punteado suave.
            fondo.setColor(Color.TRANSPARENT);
            fondo.setStroke(dp(2), BORDE, dp(5), dp(4));
            casilla.setText("");
        }

        casilla.setTextColor(texto);
        casilla.setBackground(fondo);
        casilla.setContentDescription(letra == null ? "Espacio vacío" : "Letra " + letra);

        return casilla;
    }

    // Dorado brillante para bordes (se ve bien en claro y en oscuro).
    private int doradoBorde() {
        return ContextCompat.getColor(requireContext(), R.color.buti_dorado);
    }

    // Regresa al banco la letra de una casilla.
    private void quitarLetra(int posicion) {

        if (respondida || letrasColocadas == null) return;
        if (posicionesFijas != null && posicionesFijas[posicion]) return;

        letrasColocadas[posicion] = null;

        View boton = botonPorPosicion.remove(posicion);
        if (boton != null) {
            boton.setEnabled(true);
            boton.setAlpha(1f);
            boton.setScaleX(0.7f);
            boton.setScaleY(0.7f);
            boton.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
        }

        actualizarEspacios();
    }

    // Cuántos elementos caben en una fila, según el ancho de la pantalla.
    private int calcularPorFila(int anchoElemento, int margenes) {
        int ancho = getResources().getDisplayMetrics().widthPixels - dp(40) - margenes;
        return Math.max(3, ancho / Math.max(1, anchoElemento));
    }

    // =========================================================
    // BANCO DE LETRAS
    // =========================================================

    private void crearBancoLetras() {

        contenedorLetras.removeAllViews();

        contenedorCompletar.setVisibility(View.VISIBLE);
        contenedorLetras.setVisibility(View.VISIBLE);

        if (palabraObjetivo == null
                || palabraObjetivo.trim().isEmpty()
                || letrasColocadas == null
                || posicionesFijas == null) {

            return;
        }

        botonPorPosicion.clear();

        List<String> letrasObjetivo = separarGrafemas(palabraObjetivo);

        // El banco solo tendrá las letras que todavía faltan.
        List<String> letrasFaltantes = new ArrayList<>();

        for (int i = 0; i < letrasObjetivo.size(); i++) {

            if (!posicionesFijas[i]) {
                letrasFaltantes.add(letrasObjetivo.get(i));
            }
        }

        // Mezclar las letras faltantes.
        Collections.shuffle(letrasFaltantes);

        contenedorLetras.setOrientation(LinearLayout.VERTICAL);

        LinearLayout fila = null;
        int contador = 0;

        // Filas parejas que quepan completas en la pantalla (ya no se cortan).
        int porFila = calcularPorFila(dp(52), dp(16));
        int filasBanco = (int) Math.ceil(letrasFaltantes.size() / (double) porFila);
        int enCadaFila = (int) Math.ceil(letrasFaltantes.size() / (double) Math.max(1, filasBanco));

        for (String letra : letrasFaltantes) {

            if (contador % Math.max(1, enCadaFila) == 0) {

                fila = new LinearLayout(requireContext());

                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(Gravity.CENTER);

                fila.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                );

                contenedorLetras.addView(fila);
            }

            MaterialButton boton = new MaterialButton(requireContext());

            boton.setText(letra.trim().isEmpty() ? "␣" : letra);
            boton.setTextSize(18);
            boton.setAllCaps(false);
            boton.setTextColor(CAFE);

            boton.setBackgroundTintList(
                    ColorStateList.valueOf(CREMA)
            );

            boton.setCornerRadius(dp(12));
            boton.setStrokeWidth(dp(2));
            boton.setTypeface(Typeface.DEFAULT_BOLD);

            boton.setStrokeColor(
                    ColorStateList.valueOf(doradoBorde())
            );

            boton.setInsetTop(0);
            boton.setInsetBottom(0);
            boton.setPadding(dp(4), 0, dp(4), 0);

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            dp(46),
                            dp(50)
                    );

            parametros.setMargins(
                    dp(3),
                    dp(4),
                    dp(3),
                    dp(4)
            );

            boton.setLayoutParams(parametros);

            String letraSeleccionada = letra;

            boton.setOnClickListener(v -> {

                if (respondida) {
                    return;
                }

                // Buscar el siguiente espacio que no tenga letra.
                int siguientePosicion = -1;

                for (int i = 0; i < letrasColocadas.length; i++) {

                    if (letrasColocadas[i] == null) {
                        siguientePosicion = i;
                        break;
                    }
                }

                if (siguientePosicion == -1) {
                    return;
                }

                // Colocar la letra en el siguiente espacio disponible.
                letrasColocadas[siguientePosicion] = letraSeleccionada;
                botonPorPosicion.put(siguientePosicion, boton);
                ultimaPosicionColocada = siguientePosicion;

                // Desactivar el botón para evitar reutilizar esa letra.
                boton.setEnabled(false);
                boton.setAlpha(0.40f);

                actualizarEspacios();
            });

            fila.addView(boton);

            contador++;
        }

        if (letrasFaltantes.isEmpty()) {

            mostrarResultado(
                    "Ya tienes todas las letras. Comprueba tu respuesta.",
                    VERDE
            );
        }
    }

    // =========================================================
    // COMPROBAR PALABRA
    // =========================================================

    private void comprobarPalabra() {

        if (respondida) return;

        if (letrasColocadas == null || posicionesFijas == null) {
            return;
        }

        StringBuilder construida = new StringBuilder();

        // Verificar que no queden espacios vacíos.
        for (String letra : letrasColocadas) {

            if (letra == null) {

                mostrarResultado(
                        "Todavía faltan caracteres para completar la palabra.",
                        ROSA
                );

                return;
            }

            construida.append(letra);
        }

        palabraConstruida = construida.toString();

        String construidaNormalizada = Normalizer.normalize(
                palabraConstruida,
                Normalizer.Form.NFC
        );

        String objetivoNormalizado = Normalizer.normalize(
                palabraObjetivo,
                Normalizer.Form.NFC
        );

        // Una sola oportunidad: bien o mal, se pasa a la siguiente.
        respondida = true;

        String espanol = vocabulario[indicePalabra][0];

        if (construidaNormalizada.equalsIgnoreCase(objetivoNormalizado)) {

            sumarAcierto();

            mostrarResultado(
                    "¡Excelente! +" + PUNTOS_ACIERTO + " puntos. Formaste «"
                            + palabraObjetivo + "» (" + espanol + ").",
                    VERDE
            );

            txtTraduccion.setText(palabraObjetivo + " = " + espanol);

        } else {

            registrarPalabraParaRepasar(indicePalabra);

            mostrarResultado(
                    "Incorrecto. Escribiste «" + palabraConstruida
                            + "», pero la palabra correcta es «" + palabraObjetivo
                            + "» (" + espanol + "). La verás otra vez en el repaso.",
                    ROSA
            );

            txtTraduccion.setText("Correcta: " + palabraObjetivo + " = " + espanol);
        }

        txtTraduccion.setVisibility(View.VISIBLE);

        // Pintar cada casilla: verde si la letra va ahí, rosa si no.
        palabraRevisada = true;
        actualizarEspacios();

        bloquearBancoLetras();

        indicePregunta++;

        int total = Math.min(TOTAL_DESCUBRE, preguntas.size());

        btnContinuar.setText(
                indicePregunta >= total
                        ? "Terminar etapa"
                        : "Siguiente palabra"
        );

        btnContinuar.setEnabled(true);

        btnContinuar.setOnClickListener(v ->
                mostrarPalabraIncompleta()
        );
    }

    private void bloquearBancoLetras() {

        for (int i = 0; i < contenedorLetras.getChildCount(); i++) {

            View fila = contenedorLetras.getChildAt(i);

            if (fila instanceof LinearLayout) {

                LinearLayout grupo = (LinearLayout) fila;

                for (int j = 0; j < grupo.getChildCount(); j++) {

                    grupo.getChildAt(j).setEnabled(false);
                }
            }
        }
    }

    // =========================================================
    // ETAPA 3: RELACIONA
    // =========================================================

    private void iniciarRelaciona() {

        txtInstruccion.setText("RELACIONA");

        txtDescripcion.setText(
                "Relaciona cada palabra en español con su traducción en Hñähñu."
        );

        btnEscuchar.setVisibility(View.GONE);
        imgActividad.setVisibility(View.GONE);

        contenedorOpciones.setVisibility(View.GONE);
        contenedorRelacionar.setVisibility(View.VISIBLE);

        prepararRelacion();
    }

    private int obtenerTotalRelacion() {

        int total = Math.min(TOTAL_RELACIONA, preguntas.size());

        return total - (total % 2);
    }

    private void prepararRelacion() {

        reiniciarPanelRespuesta();

        int total = obtenerTotalRelacion();

        if (indicePregunta >= total) {
            terminarEjercicio();
            return;
        }

        detenerAudio();

        columnaEspanol.removeAllViews();
        columnaHnahnu.removeAllViews();

        parejasCompletadas.clear();

        seleccionEspanol = -1;
        seleccionHnahnu = -1;
        esperandoRelacion = true;

        int indice1 = preguntas.get(indicePregunta);
        int indice2 = preguntas.get(indicePregunta + 1);

        List<Integer> parejas = new ArrayList<>();

        parejas.add(indice1);
        parejas.add(indice2);

        List<Integer> ladoHnahnu = new ArrayList<>(parejas);

        Collections.shuffle(ladoHnahnu);

        txtNumeroPregunta.setText(
                "Parejas " + (indicePregunta + 1)
                        + " y " + (indicePregunta + 2)
                        + " de " + total
        );

        barraProgreso.setMax(total);
        barraProgreso.setProgress(indicePregunta);

        txtPalabra.setText("Encuentra las parejas");
        txtTraduccion.setVisibility(View.GONE);

        txtPregunta.setText(
                "Toca una palabra en español y después su traducción."
        );

        mostrarResultado("Une las dos columnas.", CAFE);

        btnContinuar.setText("Siguientes parejas");
        btnContinuar.setEnabled(false);

        for (int indice : parejas) {

            MaterialButton boton = crearBotonRelacion(
                    vocabulario[indice][0]
            );

            boton.setOnClickListener(v ->
                    seleccionarEspanol(indice, boton)
            );

            columnaEspanol.addView(boton);
        }

        for (int indice : ladoHnahnu) {

            MaterialButton boton = crearBotonRelacion(
                    primeraVariante(vocabulario[indice][1])
            );

            boton.setOnClickListener(v ->
                    seleccionarHnahnu(indice, boton)
            );

            columnaHnahnu.addView(boton);
        }
    }

    private MaterialButton crearBotonRelacion(String texto) {

        MaterialButton boton = new MaterialButton(requireContext());

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(dp(2), dp(6), dp(2), dp(6));

        boton.setLayoutParams(parametros);
        boton.setMinHeight(dp(65));
        boton.setText(texto);
        boton.setTextSize(13);
        boton.setAllCaps(false);
        boton.setTextColor(CAFE);
        boton.setCornerRadius(dp(14));

        boton.setBackgroundTintList(
                ColorStateList.valueOf(FONDO_OPCION)
        );

        return boton;
    }

    private void seleccionarEspanol(
            int indice,
            MaterialButton boton
    ) {

        if (!esperandoRelacion || parejasCompletadas.contains(indice)) {
            return;
        }

        seleccionEspanol = indice;

        restaurarColoresRelacion(columnaEspanol);

        boton.setBackgroundTintList(
                ColorStateList.valueOf(DORADO)
        );

        comprobarSeleccionRelacion();
    }

    private void seleccionarHnahnu(
            int indice,
            MaterialButton boton
    ) {

        if (!esperandoRelacion || parejasCompletadas.contains(indice)) {
            return;
        }

        seleccionHnahnu = indice;

        restaurarColoresRelacion(columnaHnahnu);

        boton.setBackgroundTintList(
                ColorStateList.valueOf(DORADO)
        );

        comprobarSeleccionRelacion();
    }

    private void restaurarColoresRelacion(LinearLayout columna) {

        for (int i = 0; i < columna.getChildCount(); i++) {

            View vista = columna.getChildAt(i);

            if (vista instanceof MaterialButton) {

                MaterialButton boton = (MaterialButton) vista;

                if (boton.isEnabled()) {

                    boton.setBackgroundTintList(
                            ColorStateList.valueOf(FONDO_OPCION)
                    );
                }
            }
        }
    }

    private void comprobarSeleccionRelacion() {

        if (seleccionEspanol == -1 || seleccionHnahnu == -1) {
            return;
        }

        int espanol = seleccionEspanol;
        int hnahnu = seleccionHnahnu;

        String palabraEspanol = vocabulario[espanol][0];
        String palabraHnahnu = primeraVariante(vocabulario[espanol][1]);

        // Una sola oportunidad por palabra: bien o mal, la pareja se cierra.
        parejasCompletadas.add(espanol);

        restaurarColoresRelacion(columnaEspanol);
        restaurarColoresRelacion(columnaHnahnu);

        if (espanol == hnahnu) {

            sumarAcierto();
            marcarParejaRelacion(espanol, FONDO_ACIERTO);

            mostrarResultado(
                    "¡Muy bien! +" + PUNTOS_ACIERTO + " puntos. «" + palabraEspanol
                            + "» se dice «" + palabraHnahnu + "».",
                    VERDE
            );

        } else {

            registrarPalabraParaRepasar(espanol);
            marcarParejaRelacion(espanol, FONDO_ERROR);

            mostrarResultado(
                    "Incorrecto. «" + palabraEspanol + "» se dice «" + palabraHnahnu
                            + "». La verás otra vez en el repaso.",
                    ROSA
            );
        }

        if (parejasCompletadas.size() == 2) {

            esperandoRelacion = false;
            indicePregunta += 2;

            btnContinuar.setText(
                    indicePregunta >= obtenerTotalRelacion()
                            ? "Terminar etapa"
                            : "Siguientes parejas"
            );

            btnContinuar.setEnabled(true);

            btnContinuar.setOnClickListener(v ->
                    prepararRelacion()
            );
        }

        seleccionEspanol = -1;
        seleccionHnahnu = -1;
    }

    private void marcarParejaRelacion(int indice, int color) {

        for (int i = 0; i < columnaEspanol.getChildCount(); i++) {

            MaterialButton boton =
                    (MaterialButton) columnaEspanol.getChildAt(i);

            if (boton.getText().toString()
                    .equals(vocabulario[indice][0])) {

                boton.setEnabled(false);

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(color)
                );
            }
        }

        for (int i = 0; i < columnaHnahnu.getChildCount(); i++) {

            MaterialButton boton =
                    (MaterialButton) columnaHnahnu.getChildAt(i);

            if (boton.getText().toString()
                    .equalsIgnoreCase(
                            primeraVariante(vocabulario[indice][1])
                    )) {

                boton.setEnabled(false);

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(color)
                );
            }
        }
    }

    // =========================================================
// ETAPA 4: RETO DE MEMORIA
// =========================================================

    private void iniciarRetoMemoria() {

        txtInstruccion.setText("RETO DE MEMORIA");

        txtDescripcion.setText(
                "Observa cada tarjeta, memoriza las palabras y después demuestra cuánto recuerdas."
        );

        btnEscuchar.setVisibility(View.GONE);
        imgActividad.setVisibility(View.GONE);

        contenedorCompletar.setVisibility(View.GONE);
        contenedorRelacionar.setVisibility(View.GONE);

        contenedorOpciones.setVisibility(View.GONE);
        cardRetoEspecial.setVisibility(View.VISIBLE);

        indiceTarjetaMemoria = 0;
        indicePregunta = 0;
        fasePreguntasMemoria = false;

        prepararPalabrasMemoria();
        mostrarMemoria();
    }

    private void prepararPalabrasMemoria() {

        palabrasMemoria.clear();

        List<Integer> disponibles = new ArrayList<>(preguntas);

        Collections.shuffle(disponibles);

        int cantidad = Math.min(TOTAL_MEMORIA, disponibles.size());

        for (int i = 0; i < cantidad; i++) {
            palabrasMemoria.add(disponibles.get(i));
        }

        indiceTarjetaMemoria = 0;
        indicePregunta = 0;
    }

    private void mostrarMemoria() {

        detenerAudio();

        if (palabrasMemoria.isEmpty()) {
            terminarEjercicio();
            return;
        }

        fasePreguntasMemoria = false;

        if (indiceTarjetaMemoria >= palabrasMemoria.size()) {

            iniciarPreguntasMemoria();
            return;
        }

        int indice = palabrasMemoria.get(indiceTarjetaMemoria);

        String espanol = vocabulario[indice][0];

        String hnahnu = primeraVariante(vocabulario[indice][1]);

        txtNumeroPregunta.setText(
                "Memorización " + (indiceTarjetaMemoria + 1)
                        + " de " + palabrasMemoria.size()
        );

        txtPalabra.setText(espanol);

        txtPistaReto.setText(
                "Tarjeta " + (indiceTarjetaMemoria + 1)
                        + " de " + palabrasMemoria.size()
        );

        txtPregunta.setText(
                "En Hñähñu se dice:\n\n" + hnahnu
        );

        txtTraduccion.setVisibility(View.GONE);

        mostrarResultadoDirecto(
                "Observa bien la palabra y su significado para recordarlos."
        );

        ocultarOpciones();

        contenedorOpciones.setVisibility(View.GONE);

        btnContinuar.setEnabled(true);

        if (indiceTarjetaMemoria == palabrasMemoria.size() - 1) {
            btnContinuar.setText("Comenzar preguntas");
        } else {
            btnContinuar.setText("Siguiente tarjeta");
        }

        btnContinuar.setOnClickListener(v -> {

            indiceTarjetaMemoria++;

            if (indiceTarjetaMemoria >= palabrasMemoria.size()) {

                iniciarPreguntasMemoria();

            } else {

                mostrarMemoria();
            }
        });
    }

    private void iniciarPreguntasMemoria() {

        fasePreguntasMemoria = true;

        indicePregunta = 0;
        respuestaCorrectaEvaluacion = "";
        aciertos = 0;

        totalPreguntasVisual = palabrasMemoria.size();
        progresoVisual = 0;

        actualizarColibriActividad();

        txtDescripcion.setText(
                "Ahora demuestra lo que memorizaste. Selecciona el significado correcto de cada palabra en Hñähñu."
        );

        mostrarPreguntaMemoria();
    }

    private void mostrarPreguntaMemoria() {

        reiniciarPanelRespuesta();

        detenerAudio();

        if (indicePregunta >= palabrasMemoria.size()) {

            terminarEjercicio();
            return;
        }

        respondida = false;

        int indiceCorrecto = palabrasMemoria.get(indicePregunta);

        String hnahnu = primeraVariante(
                vocabulario[indiceCorrecto][1]
        );

        String correcta = vocabulario[indiceCorrecto][0];

        txtNumeroPregunta.setText(
                "Pregunta " + (indicePregunta + 1)
                        + " de " + palabrasMemoria.size()
        );

        txtPalabra.setText(hnahnu);

        txtPistaReto.setText(
                "Recuerda las tarjetas que acabas de observar."
        );

        txtPregunta.setText(
                "¿Qué significa esta palabra en español?"
        );

        txtTraduccion.setVisibility(View.GONE);

        mostrarResultadoDirecto(
                "Selecciona la traducción correcta."
        );

        // La respuesta correcta será la palabra en español.
        List<String> opciones = new ArrayList<>();
        opciones.add(correcta);

        // Primero usamos como distractores las otras palabras que el
        // usuario acaba de memorizar. Así siempre hay opciones suficientes
        // aunque la categoría tenga únicamente cuatro palabras.
        List<Integer> candidatos = new ArrayList<>(palabrasMemoria);
        candidatos.remove(Integer.valueOf(indiceCorrecto));
        Collections.shuffle(candidatos);

        // Si todavía faltan opciones, completamos con otras palabras
        // disponibles de la categoría.
        List<Integer> candidatosAdicionales = new ArrayList<>(preguntas);
        Collections.shuffle(candidatosAdicionales);
        candidatos.addAll(candidatosAdicionales);

        for (int indice : candidatos) {

            String candidata = vocabulario[indice][0];

            if (!opciones.contains(candidata)) {
                opciones.add(candidata);
            }

            if (opciones.size() >= 4) {
                break;
            }
        }

        Collections.shuffle(opciones);

        mostrarOpcionesGenericas();

        for (int i = 0; i < opciones.size(); i++) {

            final String opcion = opciones.get(i);

            prepararBotonOpcion(
                    botonesOpciones[i],
                    opcion,
                    v -> responderMemoria(opcion, correcta, indiceCorrecto)
            );
        }

        // Ocultar los botones que no tengan una opción disponible.
        for (int i = opciones.size(); i < botonesOpciones.length; i++) {

            botonesOpciones[i].setVisibility(View.GONE);
        }

        btnContinuar.setText("Selecciona una respuesta");
        btnContinuar.setEnabled(false);
    }

    private void responderMemoria(
            String opcion,
            String correcta,
            int indiceCorrecto
    ){

        if (respondida) {
            return;
        }

        respondida = true;

        boolean esCorrecta = opcion.equalsIgnoreCase(correcta);

        if (esCorrecta) {

            sumarAcierto();

            mostrarResultado(
                    "¡Excelente! +" + PUNTOS_ACIERTO + " puntos. Recordaste que significa «"
                            + correcta + "».",
                    VERDE
            );

        } else {
            registrarPalabraParaRepasar(indiceCorrecto);

            mostrarResultado(
                    "Incorrecto. La respuesta correcta era «" + correcta
                            + "». La verás otra vez en el repaso.",
                    ROSA
            );

            txtTraduccion.setText(
                    "Significado: " + correcta
            );

            txtTraduccion.setVisibility(View.VISIBLE);
        }

        // Igual que en la evaluación final, se distingue visualmente
        // la respuesta correcta y, si hubo error, la opción seleccionada.
        for (MaterialButton boton : botonesOpciones) {

            if (boton.getVisibility() != View.VISIBLE) {
                continue;
            }

            String textoOpcion = boton.getText().toString();

            if (textoOpcion.equalsIgnoreCase(correcta)) {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(FONDO_ACIERTO)
                );

            } else if (textoOpcion.equalsIgnoreCase(opcion)) {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(FONDO_ERROR)
                );
            }
        }

        bloquearOpciones();

        indicePregunta++;

        btnContinuar.setText(
                indicePregunta >= palabrasMemoria.size()
                        ? "Terminar reto"
                        : "Siguiente pregunta"
        );

        btnContinuar.setEnabled(true);

        btnContinuar.setOnClickListener(v ->
                mostrarPreguntaMemoria()
        );
    }

    // Mensajes informativos que no aumentan el progreso del colibrí.
    private void mostrarResultadoDirecto(String mensaje) {

        txtResultado.setVisibility(View.VISIBLE);
        txtResultado.setText(mensaje);
        txtResultado.setTextColor(CAFE);
        pintarPanelRespuesta(fondoPanelNormal, false);

        mostrarPersonajePensando();
    }

    // =========================================================
    // ETAPA 5: EVALUACIÓN FINAL
    // =========================================================

    private void iniciarEvaluacion() {

        txtInstruccion.setText("EVALUACIÓN FINAL");

        txtDescripcion.setText(
                "Responde las 10 preguntas antes de que termine el tiempo."
        );

        btnEscuchar.setVisibility(View.VISIBLE);
        contenedorOpciones.setVisibility(View.VISIBLE);

        mostrarPreguntaEvaluacion();
    }

    private void mostrarPreguntaEvaluacion() {

        reiniciarPanelRespuesta();

        detenerTemporizador();
        detenerAudio();

        if (indicePregunta >= TOTAL_EVALUACION) {
            terminarEjercicio();
            return;
        }

        indicePalabra = preguntas.get(
                indicePregunta % preguntas.size()
        );

        String espanol = vocabulario[indicePalabra][0];

        String hnahnu = primeraVariante(
                vocabulario[indicePalabra][1]
        );

        respondida = false;

        txtNumeroPregunta.setText(
                "Pregunta " + (indicePregunta + 1)
                        + " de " + TOTAL_EVALUACION
        );

        barraProgreso.setMax(TOTAL_EVALUACION);
        barraProgreso.setProgress(indicePregunta);

        txtTraduccion.setVisibility(View.GONE);

        configurarImagen(espanol);

        int tipo = indicePregunta % 3;

        if (tipo == 0) {

            txtPalabra.setText(espanol);

            txtPregunta.setText(
                    "¿Cómo se dice esta palabra en Hñähñu?"
            );

            btnEscuchar.setVisibility(View.VISIBLE);
            btnEscuchar.setOnClickListener(v -> reproducirAudio());

            configurarOpcionesEvaluacion(hnahnu, true);

        } else if (tipo == 1) {

            txtPalabra.setText(hnahnu);

            txtPregunta.setText(
                    "¿Qué significa esta palabra?"
            );

            btnEscuchar.setVisibility(View.GONE);

            configurarOpcionesEvaluacion(espanol, false);

        } else {

            txtPalabra.setText("Escucha y responde");

            txtPregunta.setText(
                    "¿Qué palabra escuchaste?"
            );

            btnEscuchar.setVisibility(View.VISIBLE);
            btnEscuchar.setOnClickListener(v -> reproducirAudio());

            configurarOpcionesEvaluacion(espanol, false);
        }

        btnContinuar.setText("Selecciona una respuesta");
        btnContinuar.setEnabled(false);

        iniciarTemporizador();
    }

    private void configurarOpcionesEvaluacion(
            String correcta,
            boolean respuestasHnahnu
    ) {

        respuestaCorrectaEvaluacion = correcta;

        List<String> opciones = new ArrayList<>();
        opciones.add(correcta);

        for (int indice : preguntas) {

            String candidata = respuestasHnahnu
                    ? primeraVariante(vocabulario[indice][1])
                    : vocabulario[indice][0];

            if (!opciones.contains(candidata)) {
                opciones.add(candidata);
            }

            if (opciones.size() >= 4) break;
        }

        Collections.shuffle(opciones);

        mostrarOpcionesGenericas();

        for (int i = 0; i < opciones.size(); i++) {

            final String opcion = opciones.get(i);

            prepararBotonOpcion(
                    botonesOpciones[i],
                    opcion,
                    v -> responderEvaluacion(opcion, correcta)
            );
        }

        for (int i = opciones.size(); i < 4; i++) {
            botonesOpciones[i].setVisibility(View.GONE);
        }
    }

    private void responderEvaluacion(
            String opcion,
            String correcta
    ) {

        if (respondida) return;

        respondida = true;

        detenerTemporizador();

        boolean esCorrecta = opcion.equalsIgnoreCase(correcta);

        if (esCorrecta) {

            sumarAcierto();

            mostrarResultado(
                    "¡Respuesta correcta! +" + PUNTOS_ACIERTO + " puntos.",
                    VERDE
            );

        } else {
            registrarPalabraParaRepasar(indicePalabra);

            mostrarResultado(
                    "Incorrecto. La respuesta correcta era «" + correcta
                            + "». La verás otra vez en el repaso.",
                    ROSA
            );

            txtTraduccion.setText(
                    "Respuesta correcta: " + correcta
            );

            txtTraduccion.setVisibility(View.VISIBLE);
        }

        for (MaterialButton boton : botonesOpciones) {

            if (boton.getVisibility() != View.VISIBLE) continue;

            if (boton.getText().toString().equalsIgnoreCase(correcta)) {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(FONDO_ACIERTO)
                );

            } else {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(
                                opcion.equalsIgnoreCase(
                                        boton.getText().toString()
                                )
                                        ? FONDO_ERROR
                                        : FONDO_OPCION
                        )
                );
            }
        }

        bloquearOpciones();

        indicePregunta++;

        btnContinuar.setText(
                indicePregunta >= TOTAL_EVALUACION
                        ? "Ver resultado"
                        : "Siguiente pregunta"
        );

        btnContinuar.setEnabled(true);

        btnContinuar.setOnClickListener(v ->
                mostrarPreguntaEvaluacion()
        );
    }

    // =========================================================
    // TEMPORIZADOR
    // =========================================================

    private void iniciarTemporizador() {

        detenerTemporizador();

        txtTemporizador.setVisibility(View.VISIBLE);

        temporizador = new CountDownTimer(
                DURACION_PREGUNTA,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                int segundos = (int) (millisUntilFinished / 1000);

                txtTemporizador.setText(segundos + " s");

                if (segundos <= 5) {
                    txtTemporizador.setTextColor(ROSA);
                } else {
                    txtTemporizador.setTextColor(CAFE);
                }
            }

            @Override
            public void onFinish() {

                temporizador = null;

                if (!isAdded() || getView() == null || respondida) {
                    return;
                }

                respondida = true;

                String correcta = respuestaCorrectaEvaluacion;
                registrarPalabraParaRepasar(indicePalabra);

                mostrarResultado(
                        "¡Se terminó el tiempo! La respuesta correcta era: " + correcta,
                        ROSA
                );

                txtTraduccion.setText(
                        "Respuesta correcta: " + correcta
                );
                txtTraduccion.setVisibility(View.VISIBLE);

                // Resaltar la respuesta correcta aunque el tiempo haya terminado.
                for (MaterialButton boton : botonesOpciones) {

                    if (boton.getVisibility() != View.VISIBLE) {
                        continue;
                    }

                    if (boton.getText().toString().equalsIgnoreCase(correcta)) {

                        boton.setBackgroundTintList(
                                ColorStateList.valueOf(FONDO_ACIERTO)
                        );
                    }
                }

                bloquearOpciones();

                indicePregunta++;

                btnContinuar.setText(
                        indicePregunta >= TOTAL_EVALUACION
                                ? "Ver resultado"
                                : "Siguiente pregunta"
                );

                btnContinuar.setEnabled(true);

                btnContinuar.setOnClickListener(v ->
                        mostrarPreguntaEvaluacion()
                );

                txtTemporizador.setText("Tiempo terminado");
            }

        }.start();
    }

    private void detenerTemporizador() {

        if (temporizador != null) {
            temporizador.cancel();
            temporizador = null;
        }

        if (txtTemporizador != null) {
            txtTemporizador.setVisibility(View.GONE);
        }
    }

    // =========================================================
    // RESULTADOS
    // =========================================================

    // Al terminar las preguntas: si hubo errores, primero viene la ronda de repaso.
    private void terminarEjercicio() {

        detenerTemporizador();
        detenerAudio();

        if (obtenerTotalEtapa() <= 0) {
            mostrarSinVocabulario();
            return;
        }

        if (!enRepaso && !palabrasFallidas.isEmpty()) {
            iniciarRepaso();
            return;
        }

        finalizarEtapa();
    }

    private int obtenerTotalEtapa() {

        switch (numeroEtapa) {
            case 1:
                return totalDescubre;
            case 2:
                return Math.min(TOTAL_DESCUBRE, preguntas.size());
            case 3:
                return obtenerTotalRelacion();
            case 4:
                return palabrasMemoria.size();
            case 5:
                return TOTAL_EVALUACION;
            default:
                return 1;
        }
    }

    // =========================================================
    // RONDA DE REPASO (una sola oportunidad, vale menos puntos)
    // =========================================================

    private void iniciarRepaso() {

        enRepaso = true;
        indiceRepaso = 0;
        aciertosRepaso = 0;

        Collections.shuffle(palabrasFallidas);

        // La pantalla vuelve a un formato sencillo de opciones.
        contenedorCompletar.setVisibility(View.GONE);
        contenedorRelacionar.setVisibility(View.GONE);
        cardRetoEspecial.setVisibility(View.GONE);
        btnHablar.setVisibility(View.GONE);
        btnValidarManual.setVisibility(View.GONE);
        restaurarBotonesOpciones();

        txtInstruccion.setText("RONDA DE REPASO");

        txtDescripcion.setText(
                "Vuelve a responder las palabras que fallaste. Solo tienes una oportunidad "
                        + "y cada acierto vale " + PUNTOS_REPASO + " puntos."
        );

        // El colibrí NO se reinicia: sigue desde donde iba y en el repaso avanza
        // con cada respuesta (buena o mala) hasta llegar a la meta al terminar.
        baseRepasoVisual = progresoVisual;
        respondidasRepaso = 0;

        mostrarPreguntaRepaso();

        // Se muestra después de preparar la pregunta para que no se borre.
        String avisoRepaso = "¡Segunda oportunidad! Tienes " + palabrasFallidas.size()
                + (palabrasFallidas.size() == 1 ? " palabra" : " palabras")
                + " para repasar.";
        mostrarResultadoDirecto(avisoRepaso);
        if (txtMensajeReaccionActividad != null) {
            txtMensajeReaccionActividad.setText(avisoRepaso);
        }
    }

    // La etapa 1 reemplaza los botones de opción por tarjetas con imagen;
    // aquí se vuelven a colocar para poder usarlos en el repaso.
    private void restaurarBotonesOpciones() {

        if (botonesOpciones[0] != null && botonesOpciones[0].getParent() != contenedorOpciones) {

            contenedorOpciones.removeAllViews();

            for (MaterialButton boton : botonesOpciones) {

                if (boton == null) continue;

                if (boton.getParent() instanceof android.view.ViewGroup) {
                    ((android.view.ViewGroup) boton.getParent()).removeView(boton);
                }

                contenedorOpciones.addView(boton);
            }
        }

        mostrarOpcionesGenericas();
    }

    private void mostrarPreguntaRepaso() {

        reiniciarPanelRespuesta();

        detenerAudio();

        if (indiceRepaso >= palabrasFallidas.size()) {
            finalizarEtapa();
            return;
        }

        indicePalabra = palabrasFallidas.get(indiceRepaso);

        String espanol = vocabulario[indicePalabra][0];
        String hnahnu = primeraVariante(vocabulario[indicePalabra][1]);

        respondida = false;

        txtNumeroPregunta.setText(
                "Repaso " + (indiceRepaso + 1) + " de " + palabrasFallidas.size()
        );

        txtTraduccion.setVisibility(View.GONE);
        configurarImagen(espanol);

        txtPalabra.setText(espanol);
        txtPregunta.setText("¿Cómo se dice «" + espanol + "» en hñähñu?");

        btnEscuchar.setVisibility(View.VISIBLE);
        btnEscuchar.setOnClickListener(v -> reproducirAudio());

        // Opciones: la correcta y otras palabras de la etapa.
        List<String> opciones = new ArrayList<>();
        opciones.add(hnahnu);

        List<Integer> candidatos = new ArrayList<>(preguntas);
        Collections.shuffle(candidatos);

        for (int indice : candidatos) {

            if (opciones.size() >= 4) break;

            String candidata = primeraVariante(vocabulario[indice][1]);

            if (!opciones.contains(candidata)) {
                opciones.add(candidata);
            }
        }

        Collections.shuffle(opciones);

        mostrarOpcionesGenericas();

        for (int i = 0; i < opciones.size() && i < 4; i++) {

            final String opcion = opciones.get(i);

            prepararBotonOpcion(
                    botonesOpciones[i],
                    opcion,
                    v -> responderRepaso(opcion, hnahnu, espanol)
            );
        }

        for (int i = opciones.size(); i < 4; i++) {
            botonesOpciones[i].setVisibility(View.GONE);
        }

        btnContinuar.setText("Selecciona una respuesta");
        btnContinuar.setEnabled(false);
    }

    private void responderRepaso(String opcion, String correcta, String espanol) {

        if (respondida) return;

        respondida = true;

        // En el repaso el colibrí avanza bien o mal: al terminar llega a la meta.
        avanzarRepasoVisual();

        if (opcion.equalsIgnoreCase(correcta)) {

            aciertosRepaso++;
            puntosObtenidos += PUNTOS_REPASO;
            txtPuntos.setText(String.valueOf(puntosObtenidos));
            animarMarcador();

            mostrarResultado(
                    "¡Ahora sí! +" + PUNTOS_REPASO + " puntos. «" + espanol
                            + "» se dice «" + correcta + "».",
                    VERDE
            );

        } else {

            mostrarResultado(
                    "Incorrecto. «" + espanol + "» se dice «" + correcta
                            + "». Sigue practicándola.",
                    ROSA
            );
        }

        txtTraduccion.setText(correcta + " = " + espanol);
        txtTraduccion.setVisibility(View.VISIBLE);

        for (MaterialButton boton : botonesOpciones) {

            if (boton.getVisibility() != View.VISIBLE) continue;

            String texto = boton.getText().toString();

            int fondo;
            if (texto.equalsIgnoreCase(correcta)) fondo = FONDO_ACIERTO;
            else if (texto.equalsIgnoreCase(opcion)) fondo = FONDO_ERROR;
            else fondo = FONDO_OPCION;

            boton.setBackgroundTintList(ColorStateList.valueOf(fondo));
        }

        bloquearOpciones();

        indiceRepaso++;

        btnContinuar.setText(
                indiceRepaso >= palabrasFallidas.size()
                        ? "Ver resultado"
                        : "Siguiente palabra"
        );

        btnContinuar.setEnabled(true);
        btnContinuar.setOnClickListener(v -> mostrarPreguntaRepaso());
    }

    // =========================================================
    // RESULTADO FINAL: se guarda solo y aparece el colibrí
    // =========================================================

    private void finalizarEtapa() {

        detenerTemporizador();
        detenerAudio();

        int total = obtenerTotalEtapa();
        int totalRepaso = enRepaso ? palabrasFallidas.size() : 0;

        // Aprobar: en la evaluación 7 de 10; en las demás, la mitad.
        // Los aciertos del repaso también cuentan para aprobar.
        int aciertosTotales = aciertos + aciertosRepaso;

        boolean aprobado = numeroEtapa == 5
                ? aciertosTotales >= 7
                : aciertosTotales >= Math.max(1, (total + 1) / 2);

        // Pantalla de fondo (por si se cierra el diálogo).
        txtNumeroPregunta.setText("RESULTADO FINAL");
        barraProgreso.setMax(Math.max(1, total));
        barraProgreso.setProgress(Math.max(1, total));

        txtPalabra.setText(aprobado ? "¡Lo lograste!" : "¡Sigue practicando!");
        txtTraduccion.setVisibility(View.GONE);
        imgActividad.setVisibility(View.GONE);

        btnEscuchar.setVisibility(View.GONE);
        btnHablar.setVisibility(View.GONE);
        btnValidarManual.setVisibility(View.GONE);

        contenedorOpciones.setVisibility(View.GONE);
        contenedorCompletar.setVisibility(View.GONE);
        contenedorRelacionar.setVisibility(View.GONE);
        cardRetoEspecial.setVisibility(View.GONE);

        StringBuilder resumen = new StringBuilder();
        resumen.append("Correctas: ").append(aciertos).append(" de ").append(total);

        if (totalRepaso > 0) {
            resumen.append("\nRepaso: ").append(aciertosRepaso).append(" de ").append(totalRepaso);
        }

        resumen.append("\nPuntos: ").append(puntosObtenidos);

        if (!palabrasParaRepasar.isEmpty()) {
            resumen.append("\n\nPALABRAS PARA REPASAR:\n");
            for (String palabra : palabrasParaRepasar) {
                resumen.append("• ").append(palabra).append("\n");
            }
        }

        txtPregunta.setText(resumen.toString());
        txtPuntos.setText(String.valueOf(puntosObtenidos));

        mostrarResultado(
                aprobado
                        ? "¡Excelente trabajo! Completaste esta etapa."
                        : (numeroEtapa == 5
                           ? "Necesitas al menos 7 respuestas correctas para aprobar."
                           : "Necesitas más aciertos. Vuelve a intentarlo."),
                aprobado ? VERDE : ROSA
        );

        btnContinuar.setText(aprobado ? "Volver a etapas" : "Reintentar");
        btnContinuar.setEnabled(true);
        btnContinuar.setOnClickListener(v -> {
            if (aprobado) volverAEtapas();
            else configurarActividad();
        });

        // Pantalla del colibrí.
        ResultadoButi.Datos datos = new ResultadoButi.Datos();
        datos.etiqueta = "ETAPA " + numeroEtapa + " · NIVEL " + numeroNivel;
        datos.titulo = aprobado ? "¡Lo lograste!" : "¡Sigue practicando!";
        datos.mensaje = aprobado
                ? (palabrasParaRepasar.isEmpty()
                   ? "¡Perfecto! No tienes palabras pendientes."
                   : "Completaste la etapa. Repasa las palabras que fallaste.")
                : (numeroEtapa == 5
                   ? "Necesitas al menos 7 respuestas correctas para aprobar."
                   : "Necesitas acertar al menos la mitad. ¡Tú puedes!");
        datos.correctas = aciertos;
        datos.total = total;
        datos.repasoCorrectas = aciertosRepaso;
        datos.repasoTotal = totalRepaso;
        datos.puntos = puntosObtenidos;
        datos.exito = aprobado;

        if (pantallaResultado != null) pantallaResultado.cerrar();

        pantallaResultado = aprobado
                ? ResultadoButi.mostrar(requireContext(), datos,
                "Volver a etapas", this::volverAEtapas,
                "Repetir etapa", this::configurarActividad)
                : ResultadoButi.mostrar(requireContext(), datos,
                "Reintentar", this::configurarActividad,
                "Volver a etapas", this::volverAEtapas);

        guardarProgreso(aprobado);
    }

    // =========================================================
    // AUDIO
    // =========================================================

    private void reproducirAudio() {

        if (!isAdded()
                || vocabulario == null
                || indicePalabra < 0
                || indicePalabra >= vocabulario.length
                || vocabulario[indicePalabra] == null) {

            return;
        }

        detenerAudio();

        String palabra = vocabulario[indicePalabra][0];

        // Audio subido desde el Admin o el que trae la app.
        MediaPlayer nuevo = VocabularioNube.crearReproductor(
                requireContext(),
                categoriaAudio,
                palabra
        );

        if (nuevo == null) {

            ButiAviso.mostrar(
                    this,
                    "No se encontró el audio de esta palabra.",
                    ButiAviso.Tipo.INFO
            );

            return;
        }

        try {

            if (nuevo != null) {

                reproductor = nuevo;

                nuevo.setOnCompletionListener(mp -> {

                    if (reproductor == mp) {
                        detenerAudio();
                    }
                });

                nuevo.start();

            } else {

                ButiAviso.mostrar(
                        this,
                        "No se pudo cargar el audio.",
                        ButiAviso.Tipo.ERROR
                );
            }

        } catch (Exception e) {

            detenerAudio();

            ButiAviso.mostrar(
                    this,
                    "No se pudo reproducir el audio.",
                    ButiAviso.Tipo.ERROR
            );
        }
    }

    private void detenerAudio() {

        if (reproductor != null) {

            MediaPlayer anterior = reproductor;
            reproductor = null;

            try {

                anterior.setOnCompletionListener(null);

                if (anterior.isPlaying()) {
                    anterior.stop();
                }

                anterior.release();

            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================
    // IMÁGENES
    // =========================================================

    private int obtenerRecursoImagen(String palabra) {

        if (!isAdded() || palabra == null) {
            return 0;
        }

        // Primero la imagen guardada en Firebase para esa palabra.
        int deNube = VocabularioNube.recursoImagen(requireContext(), categoriaAudio, palabra);

        if (deNube != 0) {
            return deNube;
        }

        String categoria = obtenerNombreCategoriaImagen();

        String nombre = "img_"
                + categoria
                + "_"
                + normalizar(palabra);

        return getResources().getIdentifier(
                nombre,
                "drawable",
                requireContext().getPackageName()
        );
    }

    private void configurarImagen(String palabra) {

        // Imagen subida desde el Admin.
        android.graphics.Bitmap subida = VocabularioNube.imagenSubida(categoriaAudio, palabra);

        if (subida != null) {
            imgActividad.setImageBitmap(subida);
            imgActividad.setVisibility(View.VISIBLE);
            return;
        }

        int recurso = obtenerRecursoImagen(palabra);

        if (recurso != 0) {

            imgActividad.setImageResource(recurso);
            imgActividad.setVisibility(View.VISIBLE);

        } else {

            imgActividad.setImageDrawable(null);
            imgActividad.setVisibility(View.GONE);
        }
    }

    private String obtenerNombreCategoriaImagen() {

        switch (numeroNivel) {

            case 1:
                return "saludos";

            case 2:
                return "numeros";

            case 3:
                return "colores";

            case 4:
                return "animales";

            case 5:
                return "plantas";

            case 6:
                return "objetos";

            case 7:
                return "cuerpo";

            case 8:
                return "comida";

            case 9:
                return "tiempo";

            case 10:
                return "naturaleza";

            case 11:
                return "lugares";

            default:
                return "saludos";
        }
    }

    private String normalizar(String texto) {

        if (texto == null) return "";

        String resultado = texto.toLowerCase(Locale.ROOT).trim();

        if (resultado.equals("a noche")) return "anoche";

        if (resultado.equals("pueblo, ciudad")) {
            return "pueblo_ciudad";
        }

        if (resultado.equals("gluteo, nalga")) {
            return "gluteo_nalga";
        }

        if (resultado.equals("dormitorio, recamara")) {
            return "dormitorio_recamara";
        }

        if (resultado.equals("organo, cactus")) {
            return "organo_cactus";
        }

        if (resultado.equals("vibora, culebra")) {
            return "vibora_culebra";
        }

        resultado = Normalizer.normalize(
                resultado,
                Normalizer.Form.NFD
        );

        resultado = resultado.replaceAll("\\p{M}", "");
        resultado = resultado.replace("ñ", "n");

        resultado = resultado.replaceAll(
                "[^a-z0-9]+",
                "_"
        );

        return resultado.replaceAll("^_+|_+$", "");
    }

    // =========================================================
    // FIREBASE
    // =========================================================

    // Guarda la etapa y la racha automáticamente (sin botón).
    private void guardarProgreso(boolean aprobado) {

        if (guardando) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            ButiAviso.mostrar(
                    this,
                    "Inicia sesión para guardar tus puntos y tu racha.",
                    ButiAviso.Tipo.INFO
            );
            if (pantallaResultado != null) pantallaResultado.ponerRacha(0);
            return;
        }

        guardando = true;

        ProgresoButi.guardarEtapa(
                numeroNivel,
                numeroEtapa,
                puntosObtenidos,
                aprobado,
                (puntosSumados, racha) -> {

                    guardando = false;

                    if (!isAdded() || getView() == null) return;

                    if (pantallaResultado != null) {
                        pantallaResultado.ponerRacha(racha);
                    }
                }
        );
    }

    // =========================================================
    // NAVEGACIÓN Y DESTRUCCIÓN
    // =========================================================

    private void volverAEtapas() {

        detenerTemporizador();
        detenerAudio();

        if (!isAdded() || getView() == null) {
            return;
        }

        Navigation.findNavController(requireView()).popBackStack();
    }

    @Override
    public void onDestroyView() {

        ocultarBarraSuperior(false);

        handlerReacciones.removeCallbacksAndMessages(null);

        if (pantallaResultado != null) {
            pantallaResultado.cerrar();
            pantallaResultado = null;
        }

        detenerTemporizador();
        detenerAudio();

        super.onDestroyView();
    }
}