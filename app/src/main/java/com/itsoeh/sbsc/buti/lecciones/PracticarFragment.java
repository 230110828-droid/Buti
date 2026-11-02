package com.itsoeh.sbsc.buti.lecciones;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioDatos;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PracticarFragment extends Fragment {

    private LinearLayout layoutCuestionario;
    private LinearLayout layoutResultado;

    private ScrollView scrollPracticar;

    private ProgressBar pbPreguntas;

    private TextView txtContadorPreguntas;
    private TextView txtPreguntaQuiz;
    private TextView txtPuntosActuales;
    private TextView txtRetroalimentacion;

    private Button btnOpcionA;
    private Button btnOpcionB;
    private Button btnOpcionC;

    private ImageView btnCerrarQuiz;
    private ImageView btnAudioPregunta;
    private ImageView imgPersonaje;
    private ImageView imgColibri;
    private ImageView imgPersonajeResultado;

    private TextView txtCorrectasResultado;
    private TextView txtPuntosResultado;
    private TextView txtTituloResultado;
    private TextView txtSubtituloResultado;

    private Button btnContinuarRacha;
    private Button btnSeguirAprendiendo;

    private int preguntaActual = 0;

    private int correctasPrimeraRonda = 0;
    private int palabrasReforzadas = 0;

    private int puntos = 0;
    private int totalPreguntasIniciales = 0;

    private String opcionSeleccionada = "";
    private String temaSeleccionado = "Saludos";

    private boolean respuestaEvaluada = false;
    private boolean rondaRepaso = false;
    private boolean personajeNina = true;

    private String[][] preguntas = new String[0][0];

    private final List<String[]> preguntasRepaso = new ArrayList<>();

    private final Random random = new Random();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable avanzarRunnable;

    private Dialog dialogResultadoRespuesta;

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

    public PracticarFragment() {
        super(R.layout.fragment_practicar);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        enlazarVistas(view);

        NavController navController =
                Navigation.findNavController(view);

        Bundle argumentos = getArguments();

        if (argumentos != null) {
            temaSeleccionado = argumentos.getString(
                    "temaSeleccionado",
                    "Saludos"
            );
        }

        String[][] palabras = obtenerPalabras(temaSeleccionado);

        if (palabras == null || palabras.length < 3) {
            Toast.makeText(
                    requireContext(),
                    "No hay suficientes palabras para esta categoría.",
                    Toast.LENGTH_LONG
            ).show();

            navController.navigateUp();
            return;
        }

        preguntas = generarPreguntas(palabras);

        if (preguntas.length == 0) {
            Toast.makeText(
                    requireContext(),
                    "No se pudieron generar las preguntas.",
                    Toast.LENGTH_LONG
            ).show();

            navController.navigateUp();
            return;
        }

        totalPreguntasIniciales = preguntas.length;

        personajeNina = random.nextBoolean();

        btnCerrarQuiz.setOnClickListener(v -> {
            cancelarAvanceAutomatico();
            navController.navigateUp();
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

        btnContinuarRacha.setOnClickListener(v ->
                navController.navigate(R.id.inicioFragment)
        );

        btnSeguirAprendiendo.setOnClickListener(v ->
                navController.navigate(R.id.leccionesFragment)
        );

        btnAudioPregunta.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Próximamente: pronunciación grabada en Hñähñu.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        preguntaActual = 0;
        correctasPrimeraRonda = 0;
        palabrasReforzadas = 0;
        puntos = 0;

        opcionSeleccionada = "";
        respuestaEvaluada = false;
        rondaRepaso = false;

        preguntasRepaso.clear();

        cancelarAvanceAutomatico();

        layoutCuestionario.setVisibility(View.VISIBLE);
        layoutResultado.setVisibility(View.GONE);

        actualizarPuntos();
        cargarPregunta();
    }

    private void enlazarVistas(View view) {

        scrollPracticar = view.findViewById(R.id.scrollPracticar);

        layoutCuestionario =
                view.findViewById(R.id.layoutCuestionario);

        layoutResultado =
                view.findViewById(R.id.layoutResultado);

        pbPreguntas =
                view.findViewById(R.id.pbPreguntas);

        txtContadorPreguntas =
                view.findViewById(R.id.txtContadorPreguntas);

        txtPreguntaQuiz =
                view.findViewById(R.id.txtPreguntaQuiz);

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

    private String[][] obtenerPalabras(String tema) {

        String nombre = normalizar(tema);

        switch (nombre) {

            case "saludos":
            case "frases":
                return VocabularioDatos.SALUDOS;

            case "numeros":
                return VocabularioDatos.NUMEROS;

            case "colores":
                return VocabularioDatos.COLORES;

            case "animales":
                return VocabularioDatos.ANIMALES;

            case "plantas":
                return VocabularioDatos.PLANTAS;

            case "objetos":
                return VocabularioDatos.OBJETOS;

            case "alimento":
            case "alimentos":
            case "comida":
                return VocabularioDatos.COMIDA;

            case "cuerpo":
                return VocabularioDatos.CUERPO;

            case "tiempo":
                return VocabularioDatos.TIEMPO;

            case "naturaleza":
                return VocabularioDatos.NATURALEZA;

            case "lugares":
                return VocabularioDatos.LUGARES;

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

    private String[][] generarPreguntas(String[][] datos) {

        List<String[]> validas = new ArrayList<>();

        for (String[] fila : datos) {

            if (fila != null
                    && fila.length >= 2
                    && fila[0] != null
                    && fila[1] != null
                    && !fila[0].trim().isEmpty()
                    && !fila[1].trim().isEmpty()) {

                validas.add(new String[]{
                        fila[0].trim(),
                        fila[1].trim()
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
                    correcta
            });
        }

        return resultado.toArray(new String[0][0]);
    }

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

        String[] pregunta = preguntas[preguntaActual];

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

        pbPreguntas.setMax(total);
        pbPreguntas.setProgress(preguntaActual);

        imgPersonaje.setImageResource(
                personajesPensando[personajeNina ? 0 : 1]
        );

        actualizarColibri();

        if (scrollPracticar != null && preguntaActual == 0) {
            scrollPracticar.post(() ->
                    scrollPracticar.smoothScrollTo(0, 0)
            );
        }
    }

    private void seleccionarOpcion(Button boton) {

        if (respuestaEvaluada || !boton.isEnabled()) {
            return;
        }

        opcionSeleccionada = boton.getText().toString();

        evaluarRespuesta();
    }

    private void resetBoton(Button boton) {

        boton.setBackgroundTintList(
                ColorStateList.valueOf(Color.WHITE)
        );

        boton.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.buti_negro
                )
        );
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

                puntosGanados = 3;
                puntos += puntosGanados;
                palabrasReforzadas++;

                txtRetroalimentacion.setText(
                        "¡Muy bien! Reforzaste esta palabra. +3 puntos"
                );

            } else {

                puntosGanados = 10;
                puntos += puntosGanados;
                correctasPrimeraRonda++;

                txtRetroalimentacion.setText(
                        "¡Excelente! Tu respuesta es correcta. +10 puntos"
                );
            }

            txtRetroalimentacion.setTextColor(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.buti_verde
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
                            R.color.buti_naranja
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

        pbPreguntas.setProgress(preguntaActual + 1);

        // Mostrar ventana emergente con puntos y acumulado
        mostrarVentanaRespuesta(acierto, puntosGanados);

        // Avanzar automáticamente después de la animación
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

                boton.setTextColor(Color.WHITE);

            } else if (texto.equals(opcionSeleccionada)) {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        requireContext(),
                                        R.color.buti_naranja
                                )
                        )
                );

                boton.setTextColor(Color.WHITE);
            }
        }
    }

    // ------------------------------------------------
    // VENTANA EMERGENTE ANIMADA
    // ------------------------------------------------

    private void mostrarVentanaRespuesta(
            boolean acierto,
            int puntosGanados
    ) {

        if (!isAdded() || getContext() == null) {
            return;
        }

        Dialog dialog = new Dialog(requireContext());

        dialogResultadoRespuesta = dialog;

        LinearLayout tarjeta = new LinearLayout(requireContext());

        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setGravity(Gravity.CENTER);
        tarjeta.setPadding(
                dp(24),
                dp(25),
                dp(24),
                dp(25)
        );

        int verde = ContextCompat.getColor(
                requireContext(),
                R.color.buti_verde
        );

        int crema = ContextCompat.getColor(
                requireContext(),
                R.color.buti_crema
        );

        int naranja = ContextCompat.getColor(
                requireContext(),
                R.color.buti_naranja
        );

        int colorPrincipal = acierto ? verde : naranja;

        tarjeta.setBackground(
                crearFondo(Color.WHITE, 28, colorPrincipal)
        );

        // Icono animado
        TextView emoji = new TextView(requireContext());

        emoji.setText(acierto ? "🌟" : "💚");
        emoji.setTextSize(48);
        emoji.setGravity(Gravity.CENTER);

        tarjeta.addView(emoji);

        // Título
        TextView titulo = new TextView(requireContext());

        titulo.setText(
                acierto ? "¡Excelente!" : "¡Sigue intentando!"
        );

        titulo.setTextSize(25);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setTextColor(colorPrincipal);
        titulo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        tituloParams.topMargin = dp(8);

        tarjeta.addView(titulo, tituloParams);

        // Mensaje
        TextView mensaje = new TextView(requireContext());

        mensaje.setText(
                acierto
                        ? "¡Lo estás haciendo increíble! Sigue aprendiendo Hñähñu."
                        : "No te preocupes, equivocarse también es aprender. ¡Tú puedes!"
        );

        mensaje.setTextSize(15);
        mensaje.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.buti_negro
                )
        );

        mensaje.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams mensajeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        mensajeParams.topMargin = dp(12);

        tarjeta.addView(mensaje, mensajeParams);

        // Puntos ganados
        TextView puntosTexto = new TextView(requireContext());

        puntosTexto.setText(
                acierto
                        ? "+" + puntosGanados + " puntos"
                        : "+0 puntos"
        );

        puntosTexto.setTextSize(29);
        puntosTexto.setTypeface(null, Typeface.BOLD);
        puntosTexto.setTextColor(colorPrincipal);
        puntosTexto.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams puntosParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        puntosParams.topMargin = dp(18);

        tarjeta.addView(puntosTexto, puntosParams);

        // Total acumulado
        TextView acumulado = new TextView(requireContext());

        acumulado.setText(
                "⭐ Total acumulado: " + puntos + " puntos"
        );

        acumulado.setTextSize(17);
        acumulado.setTypeface(null, Typeface.BOLD);
        acumulado.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.buti_negro
                )
        );

        acumulado.setGravity(Gravity.CENTER);

        acumulado.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
        );

        acumulado.setBackground(
                crearFondo(crema, 16, 0)
        );

        LinearLayout.LayoutParams acumuladoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        acumuladoParams.topMargin = dp(18);

        tarjeta.addView(acumulado, acumuladoParams);

        // Mostrar la respuesta correcta cuando se equivoca
        if (!acierto) {

            TextView respuesta = new TextView(requireContext());

            respuesta.setText(
                    "La respuesta correcta era:\n"
                            + preguntas[preguntaActual][4]
            );

            respuesta.setTextSize(16);
            respuesta.setTypeface(null, Typeface.BOLD);
            respuesta.setTextColor(verde);
            respuesta.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams respuestaParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            respuestaParams.topMargin = dp(16);

            tarjeta.addView(respuesta, respuestaParams);
        }

        dialog.setContentView(tarjeta);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            dialog.getWindow().setLayout(
                    getResources().getDisplayMetrics().widthPixels - dp(40),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Animación de entrada
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

        escala.setDuration(350);

        AlphaAnimation aparecer =
                new AlphaAnimation(0f, 1f);

        aparecer.setDuration(350);

        animacion.addAnimation(escala);
        animacion.addAnimation(aparecer);

        tarjeta.startAnimation(animacion);

        if (acierto) {
            animarCelebracion(emoji);
            animarCelebracion(puntosTexto);
        } else {
            animarElemento(emoji);
        }
    }

    private GradientDrawable crearFondo(
            int color,
            int radio,
            int bordeColor
    ) {

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(color);
        fondo.setCornerRadius(dp(radio));

        if (bordeColor != 0) {
            fondo.setStroke(dp(2), bordeColor);
        }

        return fondo;
    }

    private int dp(float valor) {

        return (int) (
                valor * getResources().getDisplayMetrics().density
        );
    }

    // ------------------------------------------------
    // AVANCE AUTOMÁTICO Y DESPLAZAMIENTO
    // ------------------------------------------------

    private void programarAvanceAutomatico(boolean acierto) {

        cancelarAvanceAutomatico();

        long tiempo = acierto ? 1900 : 2900;

        avanzarRunnable = () -> {

            if (!isAdded() || getView() == null) {
                return;
            }

            if (dialogResultadoRespuesta != null
                    && dialogResultadoRespuesta.isShowing()) {

                dialogResultadoRespuesta.dismiss();
            }

            dialogResultadoRespuesta = null;

            // Regresar arriba para mostrar el progreso
            if (scrollPracticar != null) {

                scrollPracticar.smoothScrollTo(0, 0);
            }

            // Cargar la siguiente pregunta después del desplazamiento
            handler.postDelayed(() -> {

                if (isAdded() && getView() != null) {
                    avanzarPregunta();
                }

            }, 400);
        };

        handler.postDelayed(avanzarRunnable, tiempo);
    }

    private void cancelarAvanceAutomatico() {

        if (avanzarRunnable != null) {

            handler.removeCallbacks(avanzarRunnable);

            avanzarRunnable = null;
        }

        if (dialogResultadoRespuesta != null
                && dialogResultadoRespuesta.isShowing()) {

            dialogResultadoRespuesta.dismiss();
        }

        dialogResultadoRespuesta = null;
    }

    private void avanzarPregunta() {

        cancelarAvanceAutomatico();

        if (!isAdded() || getView() == null) {
            return;
        }

        preguntaActual++;

        cargarPregunta();
    }

    // ------------------------------------------------
    // REPASO
    // ------------------------------------------------

    private void iniciarRepaso() {

        rondaRepaso = true;

        preguntas = preguntasRepaso.toArray(new String[0][0]);

        preguntasRepaso.clear();

        preguntaActual = 0;

        txtRetroalimentacion.setVisibility(View.GONE);

        pbPreguntas.setMax(preguntas.length);
        pbPreguntas.setProgress(0);

        imgColibri.setImageResource(
                R.drawable.ic_colibri_volando
        );

        Toast.makeText(
                requireContext(),
                "¡Comienza el repaso! Vamos a reforzar las palabras "
                        + "que fueron más difíciles. Cada acierto vale 3 puntos.",
                Toast.LENGTH_LONG
        ).show();

        if (scrollPracticar != null) {
            scrollPracticar.smoothScrollTo(0, 0);
        }

        cargarPregunta();
    }

    // ------------------------------------------------
    // PUNTOS Y PERSONAJES
    // ------------------------------------------------

    private void actualizarPuntos() {

        txtPuntosActuales.setText(
                "★ " + puntos + " puntos"
        );

        animarElemento(txtPuntosActuales);
    }

    private void actualizarColibri() {

        int total = preguntas.length;

        if (rondaRepaso) {

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_volando
            );

        } else if (preguntaActual == 0) {

            imgColibri.setImageResource(
                    R.drawable.ic_colibri_normal
            );

        } else if (preguntaActual >= total - 1) {

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

        animarCelebracion(imgPersonaje);
    }

    private void mostrarPersonajeAnimando() {

        imgPersonaje.setImageResource(
                personajesAnimando[personajeNina ? 0 : 1]
        );

        animarElemento(imgPersonaje);
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

    // ------------------------------------------------
    // RESULTADO FINAL
    // ------------------------------------------------

    private void mostrarResultado() {

        cancelarAvanceAutomatico();

        layoutCuestionario.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.VISIBLE);

        imgPersonajeResultado.setImageResource(
                personajesVictoria[personajeNina ? 0 : 1]
        );

        animarCelebracion(imgPersonajeResultado);

        // Aciertos de la primera ronda, sin sumar los del repaso
        txtCorrectasResultado.setText(
                correctasPrimeraRonda + "/" + totalPreguntasIniciales
        );

        // Puntos de ambas rondas
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

        imgColibri.setImageResource(
                R.drawable.ic_colibri_meta
        );
    }

    @Override
    public void onDestroyView() {

        cancelarAvanceAutomatico();

        super.onDestroyView();
    }
}
