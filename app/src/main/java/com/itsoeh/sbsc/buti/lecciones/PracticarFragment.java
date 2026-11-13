package com.itsoeh.sbsc.buti.lecciones;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.itsoeh.sbsc.buti.AudioButi;
import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.ResultadoButi;
import com.itsoeh.sbsc.buti.MainActivity;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PracticarFragment extends Fragment {

    // =========================================
    // PANTALLAS
    // =========================================

    private LinearLayout layoutSeleccionPractica;
    private LinearLayout layoutCategoriasPractica;
    private LinearLayout contenedorCategoriasPractica;

    private LinearLayout layoutCuestionario;
    private LinearLayout layoutResultado;

    private LinearLayout btnRetoGeneral;
    private LinearLayout btnRetoCategoria;
    private LinearLayout btnAprenderImagenes;
    private LinearLayout btnEscucharResponder;
    private ImageView imgImagenPregunta;

    private ScrollView scrollPracticar;

    // =========================================
    // ELEMENTOS DEL CUESTIONARIO
    // =========================================

    private ProgressBar pbPreguntas;
    private FrameLayout contenedorColibri;

    // Avance del colibrí: en la ronda normal solo con aciertos;
    // en el repaso avanza con cada respuesta hasta llegar a la meta.
    private int progresoVisual = 0;
    private int totalVisual = 1;
    private int baseRepasoVisual = 0;
    private int respondidasRepaso = 0;

    private TextView txtContadorPreguntas;
    private TextView txtPreguntaQuiz;
    private TextView txtIndicacionPregunta;
    private TextView txtPuntosActuales;
    private TextView txtRetroalimentacion;

    private Button btnOpcionA;
    private Button btnOpcionB;
    private Button btnOpcionC;

    private ImageView btnCerrarQuiz;
    private ImageView btnAudioPregunta;
    private ImageView imgPersonaje;
    private TextView txtMensajePersonaje;
    private View encabezadoCuestionario;
    private ImageView imgColibri;
    private ImageView imgPersonajeResultado;

    // =========================================
    // RESULTADOS
    // =========================================

    private TextView txtCorrectasResultado;
    private TextView txtPuntosResultado;
    private TextView txtTituloResultado;
    private TextView txtSubtituloResultado;

    private Button btnContinuarRacha;
    private Button btnSeguirAprendiendo;

    // =========================================
    // VARIABLES DE LA PRÁCTICA
    // =========================================

    private int preguntaActual = 0;

    private int correctasPrimeraRonda = 0;
    private int palabrasReforzadas = 0;

    private int puntos = 0;
    private int totalPreguntasIniciales = 0;

    private String opcionSeleccionada = "";

    private String temaSeleccionado = "Reto general";

    private String palabraActualAudio = "";
    private String categoriaAudioActual = "";

    private MediaPlayer reproductorAudio;

    private boolean respuestaEvaluada = false;
    private boolean rondaRepaso = false;
    private boolean personajeNina = true;
    private boolean modoImagenes = false;
    private boolean modoEscuchar = false;

    private String[][] preguntas = new String[0][0];

    private final List<String[]> preguntasRepaso =
            new ArrayList<>();

    private final Random random = new Random();

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private Runnable avanzarRunnable;
    private Runnable siguientePreguntaRunnable;
    private Runnable ventanaRunnable;

    private Dialog dialogResultadoRespuesta;

    // =========================================
    // PERSONAJES
    // =========================================

    private final int[] personajesPensando = {
            R.drawable.ic_nina_pensando,
            R.drawable.ic_nino_pensando
    };

    private final int[] personajesFelices = {
            R.drawable.ic_nina_feliz,
            R.drawable.ic_nino_feliz
    };

    private final int[] personajesAnimando = {
            R.drawable.ic_nina_animando,
            R.drawable.ic_nino_animando
    };

    private final int[] personajesVictoria = {
            R.drawable.ic_nina_victoria,
            R.drawable.ic_nino_victoria
    };

    // =========================================
    // CONSTRUCTOR
    // =========================================

    public PracticarFragment() {
        super(R.layout.fragment_practicar);
    }

    // =========================================
    // INICIO
    // =========================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        enlazarVistas(view);

        configurarSeleccionRetos();

        NavController navController =
                Navigation.findNavController(view);

        btnCerrarQuiz.setOnClickListener(v -> {
            cancelarAvanceAutomatico();
            liberarReproductor();
            mostrarSeleccionPractica();
        });

        btnOpcionA.setOnClickListener(v ->
                seleccionarOpcion(btnOpcionA)
        );

        btnOpcionB.setOnClickListener(v ->
                seleccionarOpcion(btnOpcionB)
        );

        btnOpcionC.setOnClickListener(v ->
                seleccionarOpcion(btnOpcionC)
        );

        btnContinuarRacha.setOnClickListener(v -> {
            cancelarAvanceAutomatico();
            liberarReproductor();
            irASeccion(R.id.inicioFragment);
        });

        btnSeguirAprendiendo.setOnClickListener(v -> {
            cancelarAvanceAutomatico();
            liberarReproductor();
            irASeccion(R.id.leccionesFragment);
        });

        btnAudioPregunta.setOnClickListener(v ->
                reproducirAudioPregunta()
        );

        Bundle argumentos = getArguments();

        if (argumentos != null) {

            String temaRecibido = argumentos.getString(
                    "temaSeleccionado",
                    ""
            );

            if (temaRecibido != null
                    && !temaRecibido.trim().isEmpty()) {

                iniciarPracticaCategoria(temaRecibido);
                return;
            }
        }

        mostrarSeleccionPractica();
    }

    // =========================================
    // ENLAZAR VISTAS
    // =========================================

    private void enlazarVistas(View view) {

        scrollPracticar =
                view.findViewById(R.id.scrollPracticar);

        layoutSeleccionPractica =
                view.findViewById(R.id.layoutSeleccionPractica);

        layoutCategoriasPractica =
                view.findViewById(R.id.layoutCategoriasPractica);

        contenedorCategoriasPractica =
                view.findViewById(R.id.contenedorCategoriasPractica);

        btnRetoGeneral =
                view.findViewById(R.id.btnRetoGeneral);

        btnRetoCategoria =
                view.findViewById(R.id.btnRetoCategoria);

        btnAprenderImagenes =
                view.findViewById(R.id.btnAprenderImagenes);

        btnEscucharResponder =
                view.findViewById(R.id.btnEscucharResponder);

        imgImagenPregunta =
                view.findViewById(R.id.imgImagenPregunta);

        layoutCuestionario =
                view.findViewById(R.id.layoutCuestionario);

        layoutResultado =
                view.findViewById(R.id.layoutResultado);

        contenedorColibri =
                view.findViewById(R.id.contenedorColibriPractica);

        pbPreguntas =
                view.findViewById(R.id.pbPreguntas);

        txtContadorPreguntas =
                view.findViewById(R.id.txtContadorPreguntas);

        txtPreguntaQuiz =
                view.findViewById(R.id.txtPreguntaQuiz);

        txtIndicacionPregunta =
                view.findViewById(R.id.txtIndicacionPregunta);

        txtPuntosActuales =
                view.findViewById(R.id.txtPuntosActuales);

        txtRetroalimentacion =
                view.findViewById(R.id.txtRetroalimentacion);

        btnOpcionA =
                view.findViewById(R.id.btnOpcionA);

        btnOpcionB =
                view.findViewById(R.id.btnOpcionB);

        btnOpcionC =
                view.findViewById(R.id.btnOpcionC);

        btnCerrarQuiz =
                view.findViewById(R.id.btnCerrarQuiz);

        btnAudioPregunta =
                view.findViewById(R.id.btnAudioPregunta);

        imgPersonaje =
                view.findViewById(R.id.imgPersonaje);

        txtMensajePersonaje =
                view.findViewById(R.id.txtMensajePersonajePractica);

        encabezadoCuestionario =
                view.findViewById(R.id.encabezadoCuestionario);

        imgColibri =
                view.findViewById(R.id.imgColibri);

        imgPersonajeResultado =
                view.findViewById(R.id.imgPersonajeResultado);

        txtCorrectasResultado =
                view.findViewById(R.id.txtCorrectasResultado);

        txtPuntosResultado =
                view.findViewById(R.id.txtPuntosResultado);

        txtTituloResultado =
                view.findViewById(R.id.txtTituloResultado);

        txtSubtituloResultado =
                view.findViewById(R.id.txtSubtituloResultado);

        btnContinuarRacha =
                view.findViewById(R.id.btnContinuarRacha);

        btnSeguirAprendiendo =
                view.findViewById(R.id.btnSeguirAprendiendo);
    }

    // =========================================
    // SELECCIÓN DE RETOS
    // =========================================

    private void configurarSeleccionRetos() {

        btnRetoGeneral.setOnClickListener(v ->
                iniciarPracticaGeneral()
        );

        btnAprenderImagenes.setOnClickListener(v ->
                iniciarAprenderImagenes()
        );

        btnEscucharResponder.setOnClickListener(v ->
                iniciarEscucharResponder()
        );

        btnRetoCategoria.setOnClickListener(v -> {

            if (layoutCategoriasPractica.getVisibility()
                    == View.VISIBLE) {

                layoutCategoriasPractica.setVisibility(View.GONE);

            } else {

                crearCategorias();

                layoutCategoriasPractica.setVisibility(View.VISIBLE);

                scrollPracticar.post(() ->
                        scrollPracticar.smoothScrollTo(
                                0,
                                layoutCategoriasPractica.getTop()
                        )
                );
            }
        });
    }

    private void mostrarSeleccionPractica() {

        cancelarAvanceAutomatico();
        liberarReproductor();

        layoutSeleccionPractica.setVisibility(View.VISIBLE);
        layoutCuestionario.setVisibility(View.GONE);
        if (encabezadoCuestionario != null) encabezadoCuestionario.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.GONE);

        ocultarBarraSuperior(false);

        layoutCategoriasPractica.setVisibility(View.GONE);

        modoImagenes = false;
        modoEscuchar = false;
        if (imgPersonaje != null) imgPersonaje.setVisibility(View.VISIBLE);
        if (imgImagenPregunta != null) imgImagenPregunta.setVisibility(View.GONE);
        if (btnAudioPregunta != null) btnAudioPregunta.setVisibility(View.VISIBLE);

        temaSeleccionado = "Reto general";

        palabraActualAudio = "";
        categoriaAudioActual = "";

        if (scrollPracticar != null) {
            scrollPracticar.post(() ->
                    scrollPracticar.smoothScrollTo(0, 0)
            );
        }
    }

    // =========================================
    // CREAR CATEGORÍAS
    // =========================================

    private void crearCategorias() {

        contenedorCategoriasPractica.removeAllViews();

        agregarTarjetaCategoria(
                "Saludos",
                R.drawable.img_cat_saludos,
                "Palabras y expresiones para saludar."
        );

        agregarTarjetaCategoria(
                "Números",
                R.drawable.img_cat_numeros,
                "Aprende a contar en Hñähñu."
        );

        agregarTarjetaCategoria(
                "Colores",
                R.drawable.img_cat_colores,
                "Identifica los colores."
        );

        agregarTarjetaCategoria(
                "Animales",
                R.drawable.img_cat_animales,
                "Conoce los nombres de los animales."
        );

        agregarTarjetaCategoria(
                "Plantas",
                R.drawable.img_cat_plantas,
                "Descubre palabras relacionadas con plantas."
        );

        agregarTarjetaCategoria(
                "Objetos",
                R.drawable.img_cat_objetos,
                "Aprende los nombres de objetos."
        );

        agregarTarjetaCategoria(
                "Alimentos",
                R.drawable.img_cat_comida,
                "Practica palabras de comida."
        );

        agregarTarjetaCategoria(
                "Cuerpo",
                R.drawable.img_cat_cuerpo,
                "Conoce las partes del cuerpo."
        );

        agregarTarjetaCategoria(
                "Tiempo",
                R.drawable.img_cat_tiempo,
                "Aprende palabras relacionadas con el tiempo."
        );

        agregarTarjetaCategoria(
                "Naturaleza",
                R.drawable.img_cat_naturaleza,
                "Descubre palabras de nuestro entorno."
        );

        agregarTarjetaCategoria(
                "Lugares",
                R.drawable.img_cat_lugares,
                "Conoce diferentes lugares."
        );
    }

    // =========================================
    // TARJETAS DE CATEGORÍAS CON ILUSTRACIONES
    // =========================================

    private void agregarTarjetaCategoria(
            String nombre,
            int imagen,
            String descripcion
    ) {

        LinearLayout tarjeta = new LinearLayout(requireContext());

        tarjeta.setOrientation(LinearLayout.HORIZONTAL);
        tarjeta.setGravity(Gravity.CENTER_VERTICAL);

        tarjeta.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        // Mismo estilo que las tarjetas de retos (claro y oscuro).
        tarjeta.setBackgroundResource(R.drawable.bg_reto_tarjeta);
        tarjeta.setElevation(dp(2));

        tarjeta.setClickable(true);
        tarjeta.setFocusable(true);

        LinearLayout.LayoutParams tarjetaParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        tarjetaParams.bottomMargin = dp(10);

        // ILUSTRACIÓN DE CATEGORÍA

        ImageView icono = new ImageView(requireContext());

        icono.setImageResource(imagen);
        icono.setScaleType(ImageView.ScaleType.FIT_CENTER);

        icono.setBackgroundResource(R.drawable.bg_icono_reto);

        icono.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        LinearLayout.LayoutParams iconoParams =
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(60)
                );

        tarjeta.addView(icono, iconoParams);

        // CONTENIDO

        LinearLayout contenido =
                new LinearLayout(requireContext());

        contenido.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams contenidoParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        contenidoParams.leftMargin = dp(14);

        TextView titulo = new TextView(requireContext());

        titulo.setText(nombre);
        titulo.setTextColor(color(R.color.buti_titulo));
        titulo.setTextSize(16);
        titulo.setTypeface(null, Typeface.BOLD);

        TextView subtitulo = new TextView(requireContext());

        subtitulo.setText(descripcion);
        subtitulo.setTextColor(color(R.color.buti_texto_secundario));
        subtitulo.setTextSize(12);

        LinearLayout.LayoutParams subtituloParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtituloParams.topMargin = dp(4);

        contenido.addView(titulo);
        contenido.addView(subtitulo, subtituloParams);

        tarjeta.addView(contenido, contenidoParams);

        // FLECHA

        ImageView flecha = new ImageView(requireContext());

        flecha.setImageResource(R.drawable.ic_flederecha);
        flecha.setScaleType(ImageView.ScaleType.FIT_CENTER);
        flecha.setBackgroundResource(R.drawable.bg_flecha_reto);
        flecha.setPadding(dp(8), dp(8), dp(8), dp(8));
        flecha.setImageTintList(
                ColorStateList.valueOf(color(R.color.buti_sobre_acento))
        );

        LinearLayout.LayoutParams flechaParams =
                new LinearLayout.LayoutParams(
                        dp(34),
                        dp(34)
                );

        flechaParams.leftMargin = dp(8);

        tarjeta.addView(flecha, flechaParams);

        // ACCIÓN

        tarjeta.setOnClickListener(v ->
                iniciarPracticaCategoria(nombre)
        );

        contenedorCategoriasPractica.addView(
                tarjeta,
                tarjetaParams
        );
    }

    // =========================================
    // INICIAR RETO GENERAL
    // =========================================

    private void iniciarPracticaGeneral() {

        modoImagenes = false;
        modoEscuchar = false;

        List<String[]> palabras = new ArrayList<>();

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("saludos"),
                "Saludos"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("numeros"),
                "Números"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("colores"),
                "Colores"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("animales"),
                "Animales"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("plantas"),
                "Plantas"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("objetos"),
                "Objetos"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("comida"),
                "Alimentos"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("cuerpo"),
                "Cuerpo"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("tiempo"),
                "Tiempo"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("naturaleza"),
                "Naturaleza"
        );

        agregarPalabras(
                palabras,
                VocabularioNube.arreglo("lugares"),
                "Lugares"
        );

        temaSeleccionado = "Reto general";

        iniciarCuestionario(palabras);
    }

    // =========================================
    // APRENDER CON IMÁGENES
    // =========================================

    private void iniciarAprenderImagenes() {

        List<String[]> palabras = new ArrayList<>();

        agregarPalabras(palabras, VocabularioNube.arreglo("colores"), "Colores");
        agregarPalabras(palabras, VocabularioNube.arreglo("numeros"), "Números");
        agregarPalabras(palabras, VocabularioNube.arreglo("saludos"), "Saludos");
        agregarPalabras(palabras, VocabularioNube.arreglo("tiempo"), "Tiempo");
        agregarPalabras(palabras, VocabularioNube.arreglo("plantas"), "Plantas");
        agregarPalabras(palabras, VocabularioNube.arreglo("animales"), "Animales");
        agregarPalabras(palabras, VocabularioNube.arreglo("objetos"), "Objetos");
        agregarPalabras(palabras, VocabularioNube.arreglo("cuerpo"), "Cuerpo");
        agregarPalabras(palabras, VocabularioNube.arreglo("comida"), "Alimentos");
        agregarPalabras(palabras, VocabularioNube.arreglo("naturaleza"), "Naturaleza");
        agregarPalabras(palabras, VocabularioNube.arreglo("lugares"), "Lugares");

        modoImagenes = true;
        modoEscuchar = false;
        temaSeleccionado = "Aprender con imágenes";

        iniciarCuestionario(palabras);
    }

    // =========================================
    // ESCUCHAR Y RESPONDER
    // =========================================

    private void iniciarEscucharResponder() {

        List<String[]> palabras = new ArrayList<>();

        agregarPalabras(palabras, VocabularioNube.arreglo("colores"), "Colores");
        agregarPalabras(palabras, VocabularioNube.arreglo("numeros"), "Números");
        agregarPalabras(palabras, VocabularioNube.arreglo("saludos"), "Saludos");
        agregarPalabras(palabras, VocabularioNube.arreglo("tiempo"), "Tiempo");
        agregarPalabras(palabras, VocabularioNube.arreglo("plantas"), "Plantas");
        agregarPalabras(palabras, VocabularioNube.arreglo("animales"), "Animales");
        agregarPalabras(palabras, VocabularioNube.arreglo("objetos"), "Objetos");
        agregarPalabras(palabras, VocabularioNube.arreglo("cuerpo"), "Cuerpo");
        agregarPalabras(palabras, VocabularioNube.arreglo("comida"), "Alimentos");
        agregarPalabras(palabras, VocabularioNube.arreglo("naturaleza"), "Naturaleza");
        agregarPalabras(palabras, VocabularioNube.arreglo("lugares"), "Lugares");

        modoImagenes = false;
        modoEscuchar = true;
        temaSeleccionado = "Escuchar y responder";

        iniciarCuestionario(palabras);
    }

    // =========================================
    // GENERAR PREGUNTAS DE ESCUCHA
    // =========================================

    private String[][] generarPreguntasEscuchar(List<String[]> datos) {

        List<String[]> validas = new ArrayList<>();

        for (String[] fila : datos) {
            if (fila != null
                    && fila.length >= 3
                    && fila[0] != null
                    && fila[1] != null
                    && fila[2] != null
                    && !fila[0].trim().isEmpty()
                    && !fila[1].trim().isEmpty()
                    && !fila[2].trim().isEmpty()) {

                validas.add(new String[]{
                        fila[0].trim(),
                        fila[1].trim(),
                        fila[2].trim()
                });
            }
        }

        if (validas.size() < 3) {
            return new String[0][0];
        }

        Collections.shuffle(validas);

        List<String[]> resultado = new ArrayList<>();
        int limite = Math.min(10, validas.size());

        for (int i = 0; i < limite; i++) {

            String[] palabra = validas.get(i);
            String espanolCorrecto = palabra[0];
            String categoria = palabra[2];

            List<String> opciones = new ArrayList<>();
            opciones.add(espanolCorrecto);

            List<String> distractores = new ArrayList<>();

            for (String[] otra : validas) {
                String opcion = otra[0];

                if (!opcion.equals(espanolCorrecto)
                        && !distractores.contains(opcion)) {
                    distractores.add(opcion);
                }
            }

            Collections.shuffle(distractores);

            for (String distractor : distractores) {
                if (opciones.size() == 3) {
                    break;
                }
                opciones.add(distractor);
            }

            if (opciones.size() < 3) {
                continue;
            }

            Collections.shuffle(opciones);

            // pregunta[5] conserva la palabra en español porque
            // VocabularioNube usa esa palabra para localizar su audio hñähñu.
            resultado.add(new String[]{
                    "Escucha la palabra en hñähñu. ¿Qué significa?",
                    opciones.get(0),
                    opciones.get(1),
                    opciones.get(2),
                    espanolCorrecto,
                    espanolCorrecto,
                    categoria
            });
        }

        return resultado.toArray(new String[0][0]);
    }

    private int obtenerImagenPalabra(String espanol, String categoria) {

        // Primero la imagen guardada en Firebase para esa palabra.
        int deNube = VocabularioNube.recursoImagen(requireContext(), categoria, espanol);

        if (deNube != 0) {
            return deNube;
        }

        String prefijo;

        switch (normalizar(categoria)) {
            case "alimentos":
            case "comida":
                prefijo = "img_comida_";
                break;
            default:
                prefijo = "img_" + normalizar(categoria) + "_";
                break;
        }

        String palabra = normalizar(espanol)
                .replace("¿", "")
                .replace("?", "")
                .replace("¡", "")
                .replace("!", "")
                .replace(",", "")
                .replace(".", "")
                .replace("  ", " ")
                .trim()
                .replace(" ", "_");

        // Nombres especiales usados por los archivos de ilustraciones.
        if (palabra.equals("bienvenidos_as")) palabra = "bienvenidos_as";
        if (palabra.equals("me_da_permiso_pas ar")) palabra = "permiso_para_pasar";
        if (palabra.equals("me_da_permiso_pasar")) palabra = "permiso_para_pasar";
        if (palabra.equals("como_estas")) palabra = "como_estas";
        if (palabra.equals("estoy_triste_estoy_preocupado")) palabra = "estoy_triste_preocupado";
        if (palabra.equals("nos_vemos_manana")) palabra = "nos_vemos_manana";
        if (palabra.equals("hasta_luego_hasta_pronto")) palabra = "hasta_luego";
        if (palabra.equals("hablas_hnahnu")) palabra = "hablas_hnahnu";
        if (palabra.equals("de_donde_eres")) palabra = "de_donde_eres";
        if (palabra.equals("cuantos_anos_tienes")) palabra = "cuantos_anos_tienes";
        if (palabra.equals("como_te_llamas")) palabra = "como_te_llamas";
        if (palabra.equals("donde_estas_donde_andas")) palabra = "donde_estas";
        if (palabra.equals("guajolota")) palabra = "guajolota";
        if (palabra.equals("borrego_oveja")) palabra = "borrego_oveja";
        if (palabra.equals("chivo_cabra")) palabra = "chivo_cabra";
        if (palabra.equals("vibora_culebra")) palabra = "vibora_culebra";
        if (palabra.equals("gluteo_nalga")) palabra = "gluteo_nalga";
        if (palabra.equals("organo_cactus")) palabra = "organo_cactus";
        if (palabra.equals("flor_de_maguey")) palabra = "flor_de_maguey";
        if (palabra.equals("hace_ano")) palabra = "hace_ano";
        if (palabra.equals("a_noche")) palabra = "anoche";
        if (palabra.equals("anoche")) palabra = "anoche";
        if (palabra.equals("pasado_manana")) palabra = "pasado_manana";
        if (palabra.equals("manana_por_la_manana")) palabra = "manana_por_la_manana";
        if (palabra.equals("manana_por_la_noche")) palabra = "manana_por_la_noche";
        if (palabra.equals("manana_temprano")) palabra = "manana_temprano";
        if (palabra.equals("dedo_mano")) palabra = "dedo_mano";
        if (palabra.equals("dedo_pie")) palabra = "dedo_pie";
        if (palabra.equals("llave_puerta")) palabra = "llave_puerta";
        if (palabra.equals("dormitorio_recamara")) palabra = "dormitorio_recamara";
        if (palabra.equals("servilleta_de_tortillas")
                || palabra.equals("servilleta_(de_tortillas)")) {
            palabra = "servilleta_tortillas";
        }
        if (palabra.equals("pueblo_ciudad")) palabra = "pueblo_ciudad";
        if (palabra.equals("carcel")) palabra = "carcel";
        if (palabra.equals("pena")) palabra = "pena";
        if (palabra.equals("rio")) palabra = "rio";
        if (palabra.equals("ar ana")) palabra = "arana";

        String nombre = prefijo + palabra;

        int recurso = getResources().getIdentifier(
                nombre,
                "drawable",
                requireContext().getPackageName()
        );

        return recurso;
    }

    // =========================================
    // INICIAR RETO POR CATEGORÍA
    // =========================================

    private void iniciarPracticaCategoria(String categoria) {

        modoImagenes = false;
        modoEscuchar = false;

        String[][] datos = obtenerPalabras(categoria);

        if (datos == null || datos.length < 3) {

            ButiAviso.mostrar(
                    this,
                    "No hay suficientes palabras para esta categoría.",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        List<String[]> palabras = new ArrayList<>();

        agregarPalabras(
                palabras,
                datos,
                categoria
        );

        temaSeleccionado = categoria;

        iniciarCuestionario(palabras);
    }

    // =========================================
    // PREPARAR CUESTIONARIO
    // =========================================

    private void iniciarCuestionario(List<String[]> datos) {

        if (datos == null || datos.size() < 3) {

            ButiAviso.mostrar(
                    this,
                    "No hay suficientes palabras para crear el reto.",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        cancelarAvanceAutomatico();
        liberarReproductor();

        preguntas = modoEscuchar
                ? generarPreguntasEscuchar(datos)
                : generarPreguntas(datos);

        if (preguntas.length == 0) {

            ButiAviso.mostrar(
                    this,
                    "No se pudieron generar las preguntas.",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        totalPreguntasIniciales = preguntas.length;

        progresoVisual = 0;
        totalVisual = Math.max(1, preguntas.length);
        baseRepasoVisual = 0;
        respondidasRepaso = 0;

        personajeNina = random.nextBoolean();

        preguntaActual = 0;

        correctasPrimeraRonda = 0;
        palabrasReforzadas = 0;

        puntos = 0;

        opcionSeleccionada = "";
        palabraActualAudio = "";
        categoriaAudioActual = "";

        respuestaEvaluada = false;
        rondaRepaso = false;

        preguntasRepaso.clear();

        layoutSeleccionPractica.setVisibility(View.GONE);
        layoutCuestionario.setVisibility(View.VISIBLE);
        if (encabezadoCuestionario != null) encabezadoCuestionario.setVisibility(View.VISIBLE);
        layoutResultado.setVisibility(View.GONE);

        // Durante el reto solo se usa la X para salir: se esconde el menú de arriba.
        ocultarBarraSuperior(true);

        actualizarPuntos();

        cargarPregunta();

        if (scrollPracticar != null) {
            scrollPracticar.post(() ->
                    scrollPracticar.smoothScrollTo(0, 0)
            );
        }
    }

    // =========================================
    // AGREGAR PALABRAS CON SU CATEGORÍA
    // =========================================

    private void agregarPalabras(
            List<String[]> destino,
            String[][] datos,
            String categoria
    ) {

        if (datos == null) {
            return;
        }

        for (String[] fila : datos) {

            if (fila != null
                    && fila.length >= 2
                    && fila[0] != null
                    && fila[1] != null
                    && !fila[0].trim().isEmpty()
                    && !fila[1].trim().isEmpty()) {

                destino.add(new String[]{
                        fila[0].trim(),
                        fila[1].trim(),
                        categoria
                });
            }
        }
    }

    // =========================================
    // OBTENER PALABRAS POR CATEGORÍA
    // =========================================

    private String[][] obtenerPalabras(String tema) {

        String nombre = normalizar(tema);

        switch (nombre) {

            case "saludos":
            case "frases":
                return VocabularioNube.arreglo("saludos");

            case "numeros":
                return VocabularioNube.arreglo("numeros");

            case "colores":
                return VocabularioNube.arreglo("colores");

            case "animales":
                return VocabularioNube.arreglo("animales");

            case "plantas":
                return VocabularioNube.arreglo("plantas");

            case "objetos":
                return VocabularioNube.arreglo("objetos");

            case "alimento":
            case "alimentos":
            case "comida":
                return VocabularioNube.arreglo("comida");

            case "cuerpo":
                return VocabularioNube.arreglo("cuerpo");

            case "tiempo":
                return VocabularioNube.arreglo("tiempo");

            case "naturaleza":
                return VocabularioNube.arreglo("naturaleza");

            case "lugares":
                return VocabularioNube.arreglo("lugares");

            default:
                return null;
        }
    }

    private String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return Normalizer.normalize(
                        texto.trim(),
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    // =========================================
    // GENERAR PREGUNTAS
    // =========================================

    private String[][] generarPreguntas(List<String[]> datos) {

        List<String[]> validas = new ArrayList<>();

        for (String[] fila : datos) {

            if (fila != null
                    && fila.length >= 3
                    && fila[0] != null
                    && fila[1] != null
                    && fila[2] != null
                    && !fila[0].trim().isEmpty()
                    && !fila[1].trim().isEmpty()
                    && !fila[2].trim().isEmpty()) {

                validas.add(new String[]{
                        fila[0].trim(),
                        fila[1].trim(),
                        fila[2].trim()
                });
            }
        }

        if (validas.size() < 3) {
            return new String[0][0];
        }

        Collections.shuffle(validas);

        List<String[]> resultado = new ArrayList<>();

        int limite = Math.min(10, validas.size());

        for (int i = 0; i < limite; i++) {

            String[] palabra = validas.get(i);

            String espanol = palabra[0];
            String correcta = palabra[1];
            String categoria = palabra[2];

            List<String> opciones = new ArrayList<>();

            opciones.add(correcta);

            List<String> distractores = new ArrayList<>();

            for (String[] otra : validas) {

                String traduccion = otra[1];

                if (!traduccion.equals(correcta)
                        && !distractores.contains(traduccion)) {

                    distractores.add(traduccion);
                }
            }

            Collections.shuffle(distractores);

            for (String distractor : distractores) {

                if (opciones.size() == 3) {
                    break;
                }

                opciones.add(distractor);
            }

            if (opciones.size() < 3) {
                continue;
            }

            Collections.shuffle(opciones);

            resultado.add(new String[]{
                    "¿Cómo se dice \"" + espanol + "\"?",
                    opciones.get(0),
                    opciones.get(1),
                    opciones.get(2),
                    correcta,
                    espanol,
                    categoria
            });
        }

        return resultado.toArray(new String[0][0]);
    }

    // =========================================
    // CARGAR PREGUNTA
    // =========================================

    private void cargarPregunta() {

        if (!isAdded() || getView() == null) {
            return;
        }

        if (preguntaActual >= preguntas.length) {

            if (!rondaRepaso && !preguntasRepaso.isEmpty()) {
                iniciarRepaso();
                return;
            }

            mostrarResultado();
            return;
        }

        respuestaEvaluada = false;
        opcionSeleccionada = "";

        resetBoton(btnOpcionA);
        resetBoton(btnOpcionB);
        resetBoton(btnOpcionC);

        btnOpcionA.setEnabled(true);
        btnOpcionB.setEnabled(true);
        btnOpcionC.setEnabled(true);

        txtRetroalimentacion.setVisibility(View.GONE);

        liberarReproductor();

        String[] pregunta = preguntas[preguntaActual];

        palabraActualAudio = pregunta.length > 5
                ? pregunta[5]
                : "";

        categoriaAudioActual = pregunta.length > 6
                ? pregunta[6]
                : temaSeleccionado;

        txtPreguntaQuiz.setText(pregunta[0]);

        btnOpcionA.setText(pregunta[1]);
        btnOpcionB.setText(pregunta[2]);
        btnOpcionC.setText(pregunta[3]);

        int total = preguntas.length;

        if (rondaRepaso) {

            txtContadorPreguntas.setText(
                    "Repaso " + (preguntaActual + 1) + "/" + total
            );

        } else {

            txtContadorPreguntas.setText(
                    "Pregunta " + (preguntaActual + 1) + "/" + total
            );
        }

        pintarProgreso(false);

        if (modoImagenes) {

            imgPersonaje.setVisibility(View.VISIBLE);
            imgPersonaje.setImageResource(
                    personajesPensando[personajeNina ? 0 : 1]
            );
            imgImagenPregunta.setVisibility(View.VISIBLE);

            int imagen = obtenerImagenPalabra(
                    pregunta[5],
                    pregunta[6]
            );

            android.graphics.Bitmap imagenSubida =
                    VocabularioNube.imagenSubida(pregunta[6], pregunta[5]);

            if (imagenSubida != null) {
                imgImagenPregunta.setImageBitmap(imagenSubida);
            } else if (imagen != 0) {
                imgImagenPregunta.setImageResource(imagen);
            } else {
                imgImagenPregunta.setImageResource(R.drawable.ic_practicar_categoria);
            }

            btnAudioPregunta.setVisibility(View.GONE);
            txtIndicacionPregunta.setText(
                    "Observa la imagen y elige su traducción en hñähñu."
            );

        } else {

            imgPersonaje.setVisibility(View.VISIBLE);
            imgImagenPregunta.setVisibility(View.GONE);
            btnAudioPregunta.setVisibility(View.VISIBLE);
            if (modoEscuchar) {
                txtIndicacionPregunta.setText(
                        "Escucha el audio y elige su significado en español. "
                                + "Puedes tocar el botón para repetirlo."
                );
            } else {
                txtIndicacionPregunta.setText(
                        "Escucha la pronunciación y elige."
                );
            }

            imgPersonaje.setImageResource(
                    personajesPensando[personajeNina ? 0 : 1]
            );
        }

        ponerMensajePersonaje(rondaRepaso
                ? "¡Vamos a reforzar esta palabra!"
                : "¿Cuál es la respuesta? ¡Tú puedes!");

        actualizarColibri();

        if (modoEscuchar) {
            reproducirAudioPregunta();
        }

        if (scrollPracticar != null && preguntaActual == 0) {

            scrollPracticar.post(() ->
                    scrollPracticar.smoothScrollTo(0, 0)
            );
        }
    }

    // =========================================
    // REPRODUCCIÓN DE AUDIO
    // =========================================

    private void reproducirAudioPregunta() {

        if (!isAdded() || getContext() == null) {
            return;
        }

        if (palabraActualAudio == null
                || palabraActualAudio.trim().isEmpty()) {

            ButiAviso.mostrar(
                    this,
                    "No hay una palabra para reproducir.",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        liberarReproductor();

        // Audio subido desde el Admin o el que trae la app.
        MediaPlayer nuevoReproductor = VocabularioNube.crearReproductor(
                requireContext(),
                categoriaAudioActual,
                palabraActualAudio
        );

        if (nuevoReproductor == null) {

            ButiAviso.mostrar(
                    this,
                    "No se encontró el audio de: " + palabraActualAudio,
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        try {

            reproductorAudio = nuevoReproductor;

            if (reproductorAudio != null) {

                MediaPlayer reproductorActual = reproductorAudio;

                reproductorActual.setOnCompletionListener(mp -> {

                    AudioButi.termino(mp);
                    mp.release();

                    if (reproductorAudio == mp) {
                        reproductorAudio = null;
                    }
                });

                AudioButi.empezar(reproductorActual);
                reproductorActual.start();

            } else {

                ButiAviso.mostrar(
                        this,
                        "No se pudo reproducir el audio.",
                        ButiAviso.Tipo.ERROR
                );
            }

        } catch (Exception e) {

            liberarReproductor();

            ButiAviso.mostrar(
                    this,
                    "Ocurrió un error al reproducir el audio.",
                    ButiAviso.Tipo.ERROR
            );
        }
    }

    private void liberarReproductor() {

        if (reproductorAudio != null) {

            AudioButi.termino(reproductorAudio);

            try {

                reproductorAudio.setOnCompletionListener(null);

                if (reproductorAudio.isPlaying()) {
                    reproductorAudio.stop();
                }

                reproductorAudio.release();

            } catch (Exception e) {
                // Evitar errores al liberar el reproductor.
            }

            reproductorAudio = null;
        }
    }

    // =========================================
    // SELECCIONAR Y EVALUAR RESPUESTA
    // =========================================

    private void seleccionarOpcion(Button boton) {

        if (respuestaEvaluada || !boton.isEnabled()) {
            return;
        }

        opcionSeleccionada = boton.getText().toString();

        evaluarRespuesta();
    }

    private void resetBoton(Button boton) {

        // Sin tinte: se ve el fondo bg_practicar_opcion (claro u oscuro).
        boton.setBackgroundTintList(null);

        boton.setTextColor(color(R.color.buti_texto));
    }

    private void evaluarRespuesta() {

        if (respuestaEvaluada || opcionSeleccionada.isEmpty()) {
            return;
        }

        String[] pregunta = preguntas[preguntaActual];

        String correcta = pregunta[4];

        respuestaEvaluada = true;

        boolean acierto = opcionSeleccionada.equals(correcta);

        int puntosGanados = 0;

        btnOpcionA.setEnabled(false);
        btnOpcionB.setEnabled(false);
        btnOpcionC.setEnabled(false);

        if (acierto) {

            if (rondaRepaso) {

                puntosGanados = ProgresoButi.PUNTOS_PRACTICA_REPASO;
                puntos += puntosGanados;
                palabrasReforzadas++;

                txtRetroalimentacion.setText(
                        "¡Muy bien! Reforzaste esta palabra. +" + puntosGanados + " puntos"
                );

            } else {

                puntosGanados = ProgresoButi.PUNTOS_PRACTICA;
                puntos += puntosGanados;
                correctasPrimeraRonda++;

                txtRetroalimentacion.setText(
                        "¡Excelente! Tu respuesta es correcta. +" + puntosGanados + " puntos"
                );
            }

            txtRetroalimentacion.setTextColor(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.buti_exito
                    )
            );

            mostrarPersonajeFeliz();

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_feliz
            );

        } else {

            if (!rondaRepaso) {
                preguntasRepaso.add(pregunta);
            }

            String mensaje;

            if (rondaRepaso) {

                mensaje = "¡No te preocupes! Este era el último repaso "
                        + "de esta palabra. ";

            } else {

                String[] mensajes = {
                        "¡No te rindas! Cada intento te ayuda a aprender. ",
                        "¡Sigue practicando! Vas por buen camino. ",
                        "¡Equivocarse también es parte de aprender! ",
                        "¡Ánimo! Poco a poco recordarás todas las palabras. "
                };

                mensaje = mensajes[random.nextInt(mensajes.length)];
            }

            txtRetroalimentacion.setText(
                    mensaje + "La respuesta correcta es: " + correcta
            );

            txtRetroalimentacion.setTextColor(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.buti_resalte
                    )
            );

            mostrarPersonajeAnimando();

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_descansando
            );
        }

        mostrarRespuestaCorrecta(correcta);

        txtRetroalimentacion.setVisibility(View.VISIBLE);

        animarElemento(txtRetroalimentacion);

        actualizarPuntos();

        if (rondaRepaso) {
            // En el repaso avanza bien o mal: al terminar llega a la meta.
            avanzarRepasoVisual();
        } else if (acierto) {
            // En la ronda normal solo avanza si la respuesta es correcta.
            progresoVisual = Math.min(progresoVisual + 1, totalVisual);
            pintarProgreso(true);
        }

        // Primero se ve al colibrí volar y al personaje reaccionar;
        // después aparece la ventana con el resultado.
        final boolean aciertoFinal = acierto;
        final int puntosFinal = puntosGanados;

        ventanaRunnable = () -> {
            ventanaRunnable = null;
            if (isAdded() && getView() != null) {
                mostrarVentanaRespuesta(aciertoFinal, puntosFinal);
            }
        };
        handler.postDelayed(ventanaRunnable, 1000);

        programarAvanceAutomatico(acierto);
    }

    private void mostrarRespuestaCorrecta(String correcta) {

        Button[] botones = {
                btnOpcionA,
                btnOpcionB,
                btnOpcionC
        };

        for (Button boton : botones) {

            String texto = boton.getText().toString();

            if (texto.equals(correcta)) {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        requireContext(),
                                        R.color.buti_verde
                                )
                        )
                );

                boton.setTextColor(color(R.color.buti_crema));

            } else if (texto.equals(opcionSeleccionada)) {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        requireContext(),
                                        R.color.buti_naranja
                                )
                        )
                );

                boton.setTextColor(color(R.color.buti_crema));
            }
        }
    }

    // =========================================
    // VENTANA EMERGENTE DE RESPUESTA
    // =========================================

    private void mostrarVentanaRespuesta(
            boolean acierto,
            int puntosGanados
    ) {

        if (!isAdded() || getContext() == null) {
            return;
        }

        Dialog dialog = new Dialog(requireContext());

        dialogResultadoRespuesta = dialog;

        // Encabezado: siempre vino con letras crema (claro y oscuro).
        int vino = Color.parseColor("#4F000B");
        int vinoClaro = Color.parseColor("#812B36");
        int crema = Color.parseColor("#F5EFE3");
        int dorado = Color.parseColor("#F2A900");

        // Colores que cambian solos en modo oscuro.
        int blanco = color(R.color.buti_superficie);
        int fondoCajaPuntos = color(R.color.buti_superficie_alt);
        int tituloColor = color(R.color.buti_titulo);
        int naranja = color(R.color.buti_resalte);
        int verde = color(R.color.buti_exito);
        int cafe = color(R.color.buti_texto);

        int colorPrincipal = acierto ? verde : naranja;

        LinearLayout tarjeta = new LinearLayout(requireContext());

        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setGravity(Gravity.CENTER);

        tarjeta.setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(22)
        );

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(blanco);
        fondo.setCornerRadius(dp(25));
        fondo.setStroke(dp(2), dorado);

        tarjeta.setBackground(fondo);
        tarjeta.setElevation(dp(8));

        // ENCABEZADO

        LinearLayout encabezado = new LinearLayout(requireContext());

        encabezado.setOrientation(LinearLayout.VERTICAL);
        encabezado.setGravity(Gravity.CENTER);

        encabezado.setPadding(
                dp(15),
                dp(17),
                dp(15),
                dp(17)
        );

        GradientDrawable fondoEncabezado =
                new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{vino, vinoClaro}
                );

        fondoEncabezado.setCornerRadius(dp(19));

        encabezado.setBackground(fondoEncabezado);

        TextView etiqueta = new TextView(requireContext());

        etiqueta.setText("RETO HÑÄHÑU");
        etiqueta.setTextSize(11);
        etiqueta.setTypeface(null, Typeface.BOLD);
        etiqueta.setTextColor(dorado);
        etiqueta.setGravity(Gravity.CENTER);

        encabezado.addView(etiqueta);

        // PERSONAJE DE LA RESPUESTA

        ImageView ilustracion = new ImageView(requireContext());

        ilustracion.setImageResource(
                acierto
                        ? personajesFelices[personajeNina ? 0 : 1]
                        : personajesAnimando[personajeNina ? 0 : 1]
        );

        ilustracion.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout.LayoutParams ilustracionParams =
                new LinearLayout.LayoutParams(
                        dp(115),
                        dp(115)
                );

        ilustracionParams.topMargin = dp(5);

        encabezado.addView(
                ilustracion,
                ilustracionParams
        );

        // TÍTULO

        TextView titulo = new TextView(requireContext());

        titulo.setText(
                acierto ? "¡Excelente!" : "¡Sigue intentando!"
        );

        titulo.setTextSize(23);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setTextColor(crema);
        titulo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        tituloParams.topMargin = dp(5);

        encabezado.addView(titulo, tituloParams);

        tarjeta.addView(encabezado);

        // MENSAJE

        TextView mensaje = new TextView(requireContext());

        mensaje.setText(
                acierto
                        ? "¡Lo estás haciendo increíble! Sigue aprendiendo Hñähñu."
                        : "No te preocupes, equivocarse también es aprender. ¡Tú puedes!"
        );

        mensaje.setTextSize(15);
        mensaje.setTextColor(cafe);
        mensaje.setGravity(Gravity.CENTER);
        mensaje.setLineSpacing(dp(4), 1f);

        mensaje.setPadding(
                dp(5),
                dp(17),
                dp(5),
                dp(5)
        );

        tarjeta.addView(mensaje);

        // PUNTOS

        LinearLayout tarjetaPuntos =
                new LinearLayout(requireContext());

        tarjetaPuntos.setOrientation(LinearLayout.VERTICAL);
        tarjetaPuntos.setGravity(Gravity.CENTER);

        tarjetaPuntos.setPadding(
                dp(15),
                dp(14),
                dp(15),
                dp(14)
        );

        GradientDrawable fondoPuntos = new GradientDrawable();

        fondoPuntos.setColor(fondoCajaPuntos);
        fondoPuntos.setCornerRadius(dp(18));
        fondoPuntos.setStroke(dp(1), dorado);

        tarjetaPuntos.setBackground(fondoPuntos);

        LinearLayout.LayoutParams puntosContenedorParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        puntosContenedorParams.topMargin = dp(17);

        TextView puntosTexto = new TextView(requireContext());

        puntosTexto.setText(
                acierto
                        ? "+" + puntosGanados + " PUNTOS"
                        : "+0 PUNTOS"
        );

        puntosTexto.setTextSize(27);
        puntosTexto.setTypeface(null, Typeface.BOLD);
        puntosTexto.setTextColor(colorPrincipal);
        puntosTexto.setGravity(Gravity.CENTER);

        tarjetaPuntos.addView(puntosTexto);

        TextView acumulado = new TextView(requireContext());

        acumulado.setText(
                "Total acumulado: " + puntos + " puntos"
        );

        acumulado.setTextSize(14);
        acumulado.setTypeface(null, Typeface.BOLD);
        acumulado.setTextColor(tituloColor);
        acumulado.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams acumuladoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        acumuladoParams.topMargin = dp(8);

        tarjetaPuntos.addView(acumulado, acumuladoParams);

        tarjeta.addView(tarjetaPuntos, puntosContenedorParams);

        // RESPUESTA CORRECTA

        if (!acierto) {

            TextView respuesta = new TextView(requireContext());

            respuesta.setText(
                    "LA RESPUESTA CORRECTA ERA\n\n"
                            + preguntas[preguntaActual][4]
            );

            respuesta.setTextSize(15);
            respuesta.setTypeface(null, Typeface.BOLD);
            respuesta.setTextColor(verde);
            respuesta.setGravity(Gravity.CENTER);
            respuesta.setLineSpacing(dp(3), 1f);

            respuesta.setPadding(
                    dp(10),
                    dp(17),
                    dp(10),
                    dp(5)
            );

            tarjeta.addView(respuesta);
        }

        // DETALLE

        TextView detalle = new TextView(requireContext());

        detalle.setText(
                acierto
                        ? "¡Una palabra más aprendida!"
                        : "Cada intento te hace mejorar"
        );

        detalle.setTextSize(12);
        detalle.setTextColor(cafe);
        detalle.setGravity(Gravity.CENTER);
        detalle.setTypeface(null, Typeface.ITALIC);

        LinearLayout.LayoutParams detalleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        detalleParams.topMargin = dp(17);

        tarjeta.addView(detalle, detalleParams);

        // CONFIGURAR DIÁLOGO

        dialog.setContentView(tarjeta);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            dialog.getWindow().setDimAmount(0.35f);

            dialog.getWindow().setGravity(Gravity.BOTTOM);

            dialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );

            dialog.getWindow().setLayout(
                    getResources().getDisplayMetrics().widthPixels
                            - dp(36),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // ANIMACIÓN DE ENTRADA

        AnimationSet animacion = new AnimationSet(true);

        ScaleAnimation escala = new ScaleAnimation(
                0.65f,
                1f,
                0.65f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        escala.setDuration(400);

        AlphaAnimation aparecer = new AlphaAnimation(0f, 1f);

        aparecer.setDuration(400);

        animacion.addAnimation(escala);
        animacion.addAnimation(aparecer);

        tarjeta.startAnimation(animacion);

        if (acierto) {

            animarCelebracion(ilustracion);
            animarCelebracion(puntosTexto);

        } else {

            animarElemento(ilustracion);
        }
    }

    // =========================================
    // FONDOS Y DP
    // =========================================

    private GradientDrawable crearFondo(
            int color,
            int radio,
            int bordeColor
    ) {

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(color);
        fondo.setCornerRadius(dp(radio));

        if (bordeColor != 0) {
            fondo.setStroke(dp(1), bordeColor);
        }

        return fondo;
    }

    private int color(int recurso) {

        return ContextCompat.getColor(requireContext(), recurso);
    }

    private int dp(float valor) {

        return (int) (
                valor * getResources().getDisplayMetrics().density
        );
    }

    // =========================================
    // AVANCE AUTOMÁTICO
    // =========================================

    private void programarAvanceAutomatico(boolean acierto) {

        cancelarAvanceAutomatico();

        long tiempo = acierto ? 2900 : 3900;

        avanzarRunnable = () -> {

            if (!isAdded() || getView() == null) {
                return;
            }

            if (dialogResultadoRespuesta != null
                    && dialogResultadoRespuesta.isShowing()) {

                dialogResultadoRespuesta.dismiss();
            }

            dialogResultadoRespuesta = null;

            if (scrollPracticar != null) {
                scrollPracticar.smoothScrollTo(0, 0);
            }

            siguientePreguntaRunnable = () -> {

                if (isAdded() && getView() != null) {
                    avanzarPregunta();
                }
            };

            handler.postDelayed(
                    siguientePreguntaRunnable,
                    400
            );
        };

        handler.postDelayed(avanzarRunnable, tiempo);
    }

    private void cancelarAvanceAutomatico() {

        if (ventanaRunnable != null) {
            handler.removeCallbacks(ventanaRunnable);
            ventanaRunnable = null;
        }

        if (avanzarRunnable != null) {

            handler.removeCallbacks(avanzarRunnable);

            avanzarRunnable = null;
        }

        if (siguientePreguntaRunnable != null) {

            handler.removeCallbacks(siguientePreguntaRunnable);

            siguientePreguntaRunnable = null;
        }

        if (dialogResultadoRespuesta != null
                && dialogResultadoRespuesta.isShowing()) {

            dialogResultadoRespuesta.dismiss();
        }

        dialogResultadoRespuesta = null;
    }

    private void avanzarPregunta() {

        if (!isAdded() || getView() == null) {
            return;
        }

        preguntaActual++;

        cargarPregunta();
    }

    // =========================================
    // REPASO
    // =========================================

    private void iniciarRepaso() {

        rondaRepaso = true;

        preguntas = preguntasRepaso.toArray(new String[0][0]);

        preguntasRepaso.clear();

        preguntaActual = 0;

        txtRetroalimentacion.setVisibility(View.GONE);

        // El colibrí NO se reinicia: sigue desde donde iba.
        baseRepasoVisual = progresoVisual;
        respondidasRepaso = 0;
        pintarProgreso(false);

        imgColibri.setImageResource(
                R.drawable.ic_colibri_volando
        );

        ButiAviso.mostrar(
                this,
                "¡Comienza el repaso! Vamos a reforzar las palabras "
                        + "que fueron más difíciles. Cada acierto vale "
                        + ProgresoButi.PUNTOS_PRACTICA_REPASO + " puntos.",
                ButiAviso.Tipo.INFO
        );

        if (scrollPracticar != null) {
            scrollPracticar.smoothScrollTo(0, 0);
        }

        cargarPregunta();
    }

    // =========================================
    // PUNTOS Y PERSONAJES
    // =========================================

    // Reparte lo que le falta al colibrí entre las preguntas del repaso.
    private void avanzarRepasoVisual() {
        int palabras = Math.max(1, preguntas.length);
        respondidasRepaso = Math.min(respondidasRepaso + 1, palabras);
        int faltante = Math.max(0, totalVisual - baseRepasoVisual);
        progresoVisual = Math.min(totalVisual, baseRepasoVisual
                + Math.round(faltante * (respondidasRepaso / (float) palabras)));
        pintarProgreso(true);
    }

    // Barra y colibrí: el colibrí vuela sobre la barra hasta donde vas.
    private void pintarProgreso(boolean animar) {

        if (pbPreguntas == null || imgColibri == null) return;

        pbPreguntas.setMax(totalVisual);
        pbPreguntas.setProgress(Math.min(progresoVisual, totalVisual));

        if (contenedorColibri == null) return;

        if (contenedorColibri.getWidth() == 0) {
            contenedorColibri.post(() -> pintarProgreso(false));
            return;
        }

        float recorrido = Math.max(0, contenedorColibri.getWidth() - imgColibri.getWidth());
        float destino = recorrido * (progresoVisual / (float) Math.max(1, totalVisual));

        imgColibri.animate().cancel();

        if (animar) {
            imgColibri.animate()
                    .translationX(destino)
                    .translationY(-dp(8))
                    .scaleX(1.25f).scaleY(1.25f)
                    .setDuration(450)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> imgColibri.animate()
                            .translationY(0f)
                            .scaleX(1f).scaleY(1f)
                            .setDuration(280)
                            .setInterpolator(new OvershootInterpolator(3f))
                            .start())
                    .start();
        } else {
            imgColibri.setTranslationX(destino);
            imgColibri.setTranslationY(0f);
            imgColibri.setScaleX(1f);
            imgColibri.setScaleY(1f);
        }
    }

    // Esconde la cenefa, la barra con el menú de hamburguesa y bloquea el menú lateral.
    private void ocultarBarraSuperior(boolean ocultar) {

        if (getActivity() == null) return;

        View toolbar = getActivity().findViewById(R.id.toolbar);

        if (toolbar != null) {

            int visibilidad = ocultar ? View.GONE : View.VISIBLE;
            toolbar.setVisibility(visibilidad);

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

    // Ir a Inicio o a Lecciones sin amontonar pantallas.
    private void irASeccion(int destino) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).irASeccion(destino);
        } else if (getView() != null) {
            androidx.navigation.Navigation.findNavController(getView()).navigate(destino);
        }
    }

    private void actualizarPuntos() {

        txtPuntosActuales.setText(
                puntos + " puntos"
        );

        animarElemento(txtPuntosActuales);
    }

    private void actualizarColibri() {

        int total = preguntas.length;

        if (rondaRepaso) {

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_volando
            );

        } else if (progresoVisual <= 0) {

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_normal
            );

        } else if (progresoVisual >= totalVisual) {

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_meta
            );

        } else {

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_volando
            );
        }

        animarElemento(imgColibri);
    }

    private void mostrarPersonajeFeliz() {

        imgPersonaje.setImageResource(
                personajesFelices[personajeNina ? 0 : 1]
        );

        ponerMensajePersonaje("¡Muy bien! El colibrí avanza contigo.");

        animarCelebracion(imgPersonaje);
    }

    private void mostrarPersonajeAnimando() {

        imgPersonaje.setImageResource(
                personajesAnimando[personajeNina ? 0 : 1]
        );

        ponerMensajePersonaje(rondaRepaso
                ? "¡Ya casi! La seguiremos practicando."
                : "¡No te rindas! La repasaremos al final.");

        animarElemento(imgPersonaje);
        sacudir(imgPersonaje);
    }

    private void ponerMensajePersonaje(String mensaje) {
        if (txtMensajePersonaje == null) return;
        txtMensajePersonaje.setText(mensaje);
        txtMensajePersonaje.setAlpha(0f);
        txtMensajePersonaje.setTranslationX(dp(12));
        txtMensajePersonaje.animate().alpha(1f).translationX(0f).setDuration(260).start();
    }

    // Sacudida suave del personaje cuando la respuesta no fue correcta.
    private void sacudir(View vista) {
        if (vista == null) return;
        vista.animate().cancel();
        vista.setTranslationX(0f);
        android.animation.ObjectAnimator.ofFloat(vista, View.TRANSLATION_X,
                        0, -dp(8), dp(8), -dp(5), dp(5), 0)
                .setDuration(380)
                .start();
    }

    private void animarElemento(View elemento) {

        ScaleAnimation animacion = new ScaleAnimation(
                0.85f,
                1.05f,
                0.85f,
                1.05f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        animacion.setDuration(250);
        animacion.setRepeatCount(1);
        animacion.setRepeatMode(Animation.REVERSE);

        elemento.startAnimation(animacion);
    }

    private void animarCelebracion(View elemento) {

        ScaleAnimation animacion = new ScaleAnimation(
                0.7f,
                1.15f,
                0.7f,
                1.15f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        animacion.setDuration(350);
        animacion.setRepeatCount(1);
        animacion.setRepeatMode(Animation.REVERSE);

        elemento.startAnimation(animacion);
    }

    // =========================================
    // RESULTADO FINAL
    // =========================================

    private void mostrarResultado() {

        cancelarAvanceAutomatico();
        liberarReproductor();

        layoutSeleccionPractica.setVisibility(View.GONE);
        layoutCuestionario.setVisibility(View.GONE);
        if (encabezadoCuestionario != null) encabezadoCuestionario.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.VISIBLE);

        imgPersonajeResultado.setImageResource(
                personajesVictoria[personajeNina ? 0 : 1]
        );

        animarCelebracion(imgPersonajeResultado);

        txtCorrectasResultado.setText(
                correctasPrimeraRonda + "/" + totalPreguntasIniciales
        );

        txtPuntosResultado.setText(
                "+" + puntos
        );

        if (correctasPrimeraRonda == totalPreguntasIniciales) {

            txtTituloResultado.setText("¡Increíble!");

        } else if (correctasPrimeraRonda >= totalPreguntasIniciales / 2) {

            txtTituloResultado.setText("¡Muy bien!");

        } else {

            txtTituloResultado.setText("¡Buen esfuerzo!");
        }

        txtSubtituloResultado.setText(
                "Terminaste la práctica de " + temaSeleccionado
                        + ".\n\n"
                        + "Aciertos iniciales: "
                        + correctasPrimeraRonda
                        + " de " + totalPreguntasIniciales
                        + ".\n"
                        + "Palabras reforzadas: "
                        + palabrasReforzadas
                        + ".\n\n"
                        + "¡Cada palabra que aprendes cuenta!"
        );

        progresoVisual = totalVisual;
        pintarProgreso(false);

        imgColibri.setImageResource(
                R.drawable.ic_colibri_meta
        );

        if (scrollPracticar != null) {
            scrollPracticar.post(() ->
                    scrollPracticar.smoothScrollTo(0, 0)
            );
        }

        mostrarPantallaColibri();
    }

    // =========================================
    // PANTALLA DEL COLIBRÍ Y GUARDADO
    // =========================================

    private ResultadoButi pantallaResultado;

    private void mostrarPantallaColibri() {

        int totalRepaso = totalPreguntasIniciales - correctasPrimeraRonda;
        boolean bien = correctasPrimeraRonda >= Math.max(1, (totalPreguntasIniciales + 1) / 2);

        ResultadoButi.Datos datos = new ResultadoButi.Datos();
        datos.etiqueta = "PRÁCTICA · " + temaSeleccionado.toUpperCase();
        datos.titulo = txtTituloResultado.getText().toString();
        datos.mensaje = "¡Cada palabra que aprendes cuenta!";
        datos.correctas = correctasPrimeraRonda;
        datos.total = totalPreguntasIniciales;
        datos.repasoCorrectas = palabrasReforzadas;
        datos.repasoTotal = Math.max(0, totalRepaso);
        datos.puntos = puntos;
        datos.exito = bien;

        if (pantallaResultado != null) pantallaResultado.cerrar();

        pantallaResultado = ResultadoButi.mostrar(
                requireContext(), datos,
                "Ver resumen", null,
                null, null
        );

        // Guardar los puntos y la racha del día.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            pantallaResultado.ponerRacha(0);
            return;
        }

        ProgresoButi.sumarPuntosActividad(puntos, (sumados, racha) -> {
            if (isAdded() && pantallaResultado != null) {
                pantallaResultado.ponerRacha(racha);
            }
        });
    }

    // =========================================
    // DESTRUIR VISTA
    // =========================================

    @Override
    public void onDestroyView() {

        ocultarBarraSuperior(false);

        cancelarAvanceAutomatico();

        liberarReproductor();

        if (pantallaResultado != null) {
            pantallaResultado.cerrar();
            pantallaResultado = null;
        }

        palabraActualAudio = "";
        categoriaAudioActual = "";

        super.onDestroyView();
    }
}