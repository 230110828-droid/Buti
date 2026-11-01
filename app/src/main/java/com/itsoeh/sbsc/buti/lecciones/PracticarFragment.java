package com.itsoeh.sbsc.buti.lecciones;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

public class PracticarFragment extends Fragment {

    private LinearLayout layoutCuestionario;
    private LinearLayout layoutResultado;
    private ProgressBar pbPreguntas;
    private TextView txtContadorPreguntas;
    private TextView txtPreguntaQuiz;

    private Button btnOpcionA;
    private Button btnOpcionB;
    private Button btnOpcionC;
    private Button btnSiguientePregunta;

    private ImageView btnCerrarQuiz;
    private ImageView btnAudioPregunta;

    private TextView txtCorrectasResultado;
    private TextView txtPuntosResultado;
    private TextView txtTituloResultado;
    private TextView txtSubtituloResultado;

    private Button btnContinuarRacha;
    private Button btnSeguirAprendiendo;

    private int preguntaActual = 0;
    private int correctas = 0;

    private String opcionSeleccionada = "";
    private String temaSeleccionado = "Saludos";

    // Pregunta, opción A, opción B, opción C, respuesta correcta.
    private String[][] preguntas = new String[0][0];

    public PracticarFragment() {
        super(R.layout.fragment_practicar);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController =
                Navigation.findNavController(view);

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

        btnOpcionA = view.findViewById(R.id.btnOpcionA);
        btnOpcionB = view.findViewById(R.id.btnOpcionB);
        btnOpcionC = view.findViewById(R.id.btnOpcionC);

        btnSiguientePregunta =
                view.findViewById(R.id.btnSiguientePregunta);

        btnCerrarQuiz =
                view.findViewById(R.id.btnCerrarQuiz);

        btnAudioPregunta =
                view.findViewById(R.id.btnAudioPregunta);

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

        Bundle argumentos = getArguments();

        if (argumentos != null) {
            temaSeleccionado = argumentos.getString(
                    "temaSeleccionado",
                    "Saludos"
            );
        }

        // Obtener los datos directamente de VocabularioDatos.java.
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

        btnCerrarQuiz.setOnClickListener(v ->
                navController.navigateUp()
        );

        btnOpcionA.setOnClickListener(v ->
                seleccionarOpcion(btnOpcionA)
        );

        btnOpcionB.setOnClickListener(v ->
                seleccionarOpcion(btnOpcionB)
        );

        btnOpcionC.setOnClickListener(v ->
                seleccionarOpcion(btnOpcionC)
        );

        btnSiguientePregunta.setOnClickListener(v ->
                evaluarYSiguiente()
        );

        btnContinuarRacha.setOnClickListener(v ->
                navController.navigate(R.id.inicioFragment)
        );

        btnSeguirAprendiendo.setOnClickListener(v ->
                navController.navigate(R.id.leccionesFragment)
        );

        // El botón de audio se conserva visualmente.
        // La pronunciación requiere configurar un motor de voz
        // compatible con hñähñu.

        preguntaActual = 0;
        correctas = 0;
        opcionSeleccionada = "";

        layoutCuestionario.setVisibility(View.VISIBLE);
        layoutResultado.setVisibility(View.GONE);

        cargarPregunta();
    }

    /**
     * Relaciona el nombre de la categoría con los datos originales.
     */
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

    /**
     * Genera preguntas usando únicamente los pares de palabras
     * que existen en VocabularioDatos.java.
     */
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

        // Diez preguntas como máximo por práctica.
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

        if (preguntaActual >= preguntas.length) {
            mostrarResultado();
            return;
        }

        resetBoton(btnOpcionA);
        resetBoton(btnOpcionB);
        resetBoton(btnOpcionC);

        opcionSeleccionada = "";

        String[] pregunta = preguntas[preguntaActual];

        txtPreguntaQuiz.setText(pregunta[0]);

        btnOpcionA.setText(pregunta[1]);
        btnOpcionB.setText(pregunta[2]);
        btnOpcionC.setText(pregunta[3]);

        pbPreguntas.setMax(preguntas.length);
        pbPreguntas.setProgress(preguntaActual + 1);

        txtContadorPreguntas.setText(
                (preguntaActual + 1) + "/" + preguntas.length
        );

        btnSiguientePregunta.setText(
                preguntaActual == preguntas.length - 1
                        ? "Ver resultados"
                        : "Siguiente"
        );
    }

    private void seleccionarOpcion(Button boton) {

        opcionSeleccionada = boton.getText().toString();

        resetBoton(btnOpcionA);
        resetBoton(btnOpcionB);
        resetBoton(btnOpcionC);

        // Guardar la respuesta seleccionada antes de cambiar
        // el aspecto visual de los botones.
        boton.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(
                                requireContext(),
                                R.color.buti_verde
                        )
                )
        );

        boton.setTextColor(Color.WHITE);
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

    private void evaluarYSiguiente() {

        if (opcionSeleccionada.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Selecciona una respuesta primero.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String correcta = preguntas[preguntaActual][4];

        if (opcionSeleccionada.equals(correcta)) {
            correctas++;
        }

        preguntaActual++;
        opcionSeleccionada = "";

        cargarPregunta();
    }

    private void mostrarResultado() {

        layoutCuestionario.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.VISIBLE);

        txtCorrectasResultado.setText(
                correctas + "/" + preguntas.length
        );

        txtPuntosResultado.setText(
                "+" + (correctas * 10)
        );

        txtTituloResultado.setText(
                correctas == preguntas.length
                        ? "¡Excelente!"
                        : correctas >= preguntas.length / 2
                          ? "¡Muy bien!"
                          : "¡Sigue practicando!"
        );

        txtSubtituloResultado.setText(
                "Terminaste la práctica de " + temaSeleccionado
        );
    }
}
