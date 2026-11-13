package com.itsoeh.sbsc.buti.juegos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.itsoeh.sbsc.buti.ProgresoButi;
import androidx.annotation.NonNull;
import com.itsoeh.sbsc.buti.ResultadoButi;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.SonidosJuego;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioDatos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AtrapaPalabraFragment extends Fragment {

    private ScrollView layoutSeleccion;
    private LinearLayout layoutPartida;
    private LinearLayout layoutResultado;
    private GridLayout gridCategorias;

    private TextView txtRonda;
    private TextView txtPuntos;
    private TextView txtTiempo;
    private TextView txtCategoria;
    private TextView txtPregunta;
    private TextView txtVidas;
    private TextView txtMensaje;
    private TextView txtResultado;
    private TextView txtDetalleResultado;

    private FrameLayout areaPalabras;

    private Button btnRepetir;
    private Button btnCategorias;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable temporizador;
    private Runnable siguienteRondaRunnable;

    private final List<ObjectAnimator> animaciones = new ArrayList<>();

    private Categoria categoriaActual;
    private String[] preguntaActual;

    private final List<String[]> palabrasPendientes = new ArrayList<>();

    private int ronda;
    private int puntos;
    private int vidas;
    private int tiempoRestante;
    private int burbujasRestantes;

    private boolean rondaActiva = false;

    // Evita descontar dos vidas en una misma ronda.
    private boolean vidaDescontadaEnRonda = false;

    private static class Categoria {

        String nombre;
        String[][] palabras;
        int imagen;

        Categoria(String nombre, String[][] palabras, int imagen) {
            this.nombre = nombre;
            this.palabras = palabras;
            this.imagen = imagen;
        }
    }

    private final Categoria[] categorias = {

            new Categoria("Animales", VocabularioDatos.ANIMALES, R.drawable.img_cat_animales),
            new Categoria("Plantas", VocabularioDatos.PLANTAS, R.drawable.img_cat_plantas),
            new Categoria("Números", VocabularioDatos.NUMEROS, R.drawable.img_cat_numeros),
            new Categoria("Colores", VocabularioDatos.COLORES, R.drawable.img_cat_colores),
            new Categoria("Saludos", VocabularioDatos.SALUDOS, R.drawable.img_cat_saludos),
            new Categoria("Tiempo", VocabularioDatos.TIEMPO, R.drawable.img_cat_tiempo),
            new Categoria("Objetos", VocabularioDatos.OBJETOS, R.drawable.img_cat_objetos),
            new Categoria("Cuerpo", VocabularioDatos.CUERPO, R.drawable.img_cat_cuerpo),
            new Categoria("Comida", VocabularioDatos.COMIDA, R.drawable.img_cat_comida),
            new Categoria("Naturaleza", VocabularioDatos.NATURALEZA, R.drawable.img_cat_naturaleza),
            new Categoria("Lugares", VocabularioDatos.LUGARES, R.drawable.img_cat_lugares)
    };

    public AtrapaPalabraFragment() {
        super(R.layout.fragment_atrapa_palabra);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Carga los sonidos para que suenen sin retraso.
        SonidosJuego.preparar(requireContext());

        layoutSeleccion = view.findViewById(R.id.layoutSeleccionAtrapa);
        layoutPartida = view.findViewById(R.id.layoutPartidaAtrapa);
        layoutResultado = view.findViewById(R.id.layoutResultadoAtrapa);

        gridCategorias = view.findViewById(R.id.gridCategoriasAtrapa);

        txtRonda = view.findViewById(R.id.txtRondaAtrapa);
        txtPuntos = view.findViewById(R.id.txtPuntosAtrapa);
        txtTiempo = view.findViewById(R.id.txtTiempoAtrapa);
        txtCategoria = view.findViewById(R.id.txtCategoriaActual);
        txtPregunta = view.findViewById(R.id.txtPreguntaAtrapa);
        txtVidas = view.findViewById(R.id.txtVidasAtrapa);
        txtMensaje = view.findViewById(R.id.txtMensajeAtrapa);

        txtResultado = view.findViewById(R.id.txtResultadoAtrapa);
        txtDetalleResultado = view.findViewById(R.id.txtDetalleResultadoAtrapa);

        areaPalabras = view.findViewById(R.id.areaPalabrasAtrapa);

        btnRepetir = view.findViewById(R.id.btnRepetirAtrapa);
        btnCategorias = view.findViewById(R.id.btnCategoriasAtrapa);

        View btnVolver = view.findViewById(R.id.btnVolverAtrapa);

        btnVolver.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        btnRepetir.setOnClickListener(v -> {

            if (categoriaActual != null) {
                iniciarPartida(categoriaActual);
            }

        });

        btnCategorias.setOnClickListener(v -> mostrarCategorias());

        crearBotonesCategorias();

        mostrarCategorias();
    }

    // ------------------------------------------------
    // CATEGORÍAS
    // ------------------------------------------------

    private void crearBotonesCategorias() {

        gridCategorias.removeAllViews();

        for (Categoria categoria : categorias) {

            if (categoria.palabras == null ||
                    categoria.palabras.length < 4) {
                continue;
            }

            // Tarjeta con la ilustración de la categoría.
            LinearLayout boton = new LinearLayout(requireContext());

            boton.setOrientation(LinearLayout.VERTICAL);
            boton.setGravity(Gravity.CENTER);
            boton.setBackgroundResource(R.drawable.bg_reto_tarjeta);
            boton.setElevation(dp(2));
            boton.setPadding(dp(8), dp(10), dp(8), dp(10));
            boton.setClickable(true);
            boton.setFocusable(true);

            ImageView icono = new ImageView(requireContext());

            icono.setImageResource(categoria.imagen);
            icono.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icono.setBackgroundResource(R.drawable.bg_icono_reto);
            icono.setPadding(dp(4), dp(4), dp(4), dp(4));

            boton.addView(icono, new LinearLayout.LayoutParams(dp(56), dp(56)));

            TextView nombre = new TextView(requireContext());

            nombre.setText(categoria.nombre);
            nombre.setTextColor(color(R.color.buti_titulo));
            nombre.setTextSize(15);
            nombre.setTypeface(null, Typeface.BOLD);
            nombre.setGravity(Gravity.CENTER);
            nombre.setPadding(0, dp(6), 0, 0);

            boton.addView(nombre);

            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams();

            params.width = 0;
            params.height = dp(112);

            params.columnSpec =
                    GridLayout.spec(GridLayout.UNDEFINED, 1f);

            params.setMargins(dp(5), dp(5), dp(5), dp(5));

            gridCategorias.addView(boton, params);

            boton.setOnClickListener(v ->
                    iniciarPartida(categoria));
        }
    }

    private void mostrarCategorias() {

        detenerTemporizador();
        cancelarAnimaciones();

        if (siguienteRondaRunnable != null) {
            handler.removeCallbacks(siguienteRondaRunnable);
            siguienteRondaRunnable = null;
        }

        rondaActiva = false;

        areaPalabras.removeAllViews();

        layoutSeleccion.setVisibility(View.VISIBLE);
        layoutPartida.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.GONE);
    }

    // ------------------------------------------------
    // INICIAR PARTIDA
    // ------------------------------------------------

    private void iniciarPartida(Categoria categoria) {

        detenerTemporizador();
        cancelarAnimaciones();

        if (siguienteRondaRunnable != null) {
            handler.removeCallbacks(siguienteRondaRunnable);
            siguienteRondaRunnable = null;
        }

        rondaActiva = false;

        categoriaActual = categoria;

        ronda = 0;
        puntos = 0;
        vidas = 3;
        tiempoRestante = 60;

        vidaDescontadaEnRonda = false;

        palabrasPendientes.clear();

        Collections.addAll(
                palabrasPendientes,
                categoriaActual.palabras.clone());

        Collections.shuffle(palabrasPendientes);

        layoutSeleccion.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.GONE);
        layoutPartida.setVisibility(View.VISIBLE);

        txtCategoria.setText(categoria.nombre.toUpperCase());

        actualizarPuntos();
        actualizarVidas();
        actualizarTiempo();

        iniciarTemporizador();

        siguienteRonda();
    }

    // ------------------------------------------------
    // TEMPORIZADOR
    // ------------------------------------------------

    private void iniciarTemporizador() {

        temporizador = new Runnable() {

            @Override
            public void run() {

                if (!isAdded() ||
                        layoutPartida == null ||
                        layoutPartida.getVisibility() != View.VISIBLE) {
                    return;
                }

                tiempoRestante--;

                actualizarTiempo();

                if (tiempoRestante <= 0) {

                    finalizarPartida();

                } else {

                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.postDelayed(temporizador, 1000);
    }

    private void detenerTemporizador() {

        if (temporizador != null) {

            handler.removeCallbacks(temporizador);

            temporizador = null;
        }
    }

    private void actualizarTiempo() {

        txtTiempo.setText(tiempoRestante + " s");

        if (tiempoRestante <= 10) {

            txtTiempo.setTextColor(color(R.color.nivel_rosa));

        } else {

            txtTiempo.setTextColor(color(R.color.buti_titulo));
        }
    }

    // ------------------------------------------------
    // PUNTOS Y VIDAS
    // ------------------------------------------------

    private void actualizarPuntos() {

        // La estrella ahora es un icono (drawableStart).
        txtPuntos.setText(String.valueOf(puntos));
    }

    private void actualizarVidas() {

        // Corazones reales (antes eran los símbolos ♥ y ♡).
        SpannableStringBuilder corazones = new SpannableStringBuilder();

        int tamano = dp(26);

        for (int i = 0; i < 3; i++) {

            Drawable corazon = ContextCompat.getDrawable(
                    requireContext(),
                    i < vidas ? R.drawable.ic_corazon : R.drawable.ic_corazon_vacio);

            if (corazon == null) continue;

            corazon.setBounds(0, 0, tamano, tamano);

            int inicio = corazones.length();
            corazones.append("♥");
            corazones.setSpan(
                    new ImageSpan(corazon, ImageSpan.ALIGN_BOTTOM),
                    inicio,
                    inicio + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (i < 2) corazones.append("   ");
        }

        txtVidas.setText(corazones);
    }

    // Descuenta una sola vida por ronda.
    private void descontarVida() {

        if (vidaDescontadaEnRonda) {
            return;
        }

        vidaDescontadaEnRonda = true;

        vidas--;

        actualizarVidas();
    }

    // ------------------------------------------------
    // RONDAS
    // ------------------------------------------------

    private void siguienteRonda() {

        if (!isAdded() ||
                tiempoRestante <= 0 ||
                vidas <= 0) {
            return;
        }

        if (ronda >= 10) {

            finalizarPartida();

            return;
        }

        cancelarAnimaciones();

        ronda++;

        rondaActiva = true;

        // Reinicia el control de vidas de esta ronda.
        vidaDescontadaEnRonda = false;

        txtRonda.setText("RONDA " + ronda + "/10");

        preguntaActual = obtenerPalabra();

        txtPregunta.setText(
                "¿Cómo se dice " +
                        preguntaActual[0].toUpperCase() +
                        " en Hñähñu?");

        txtMensaje.setText("¡Atrapa la traducción correcta!");

        areaPalabras.removeAllViews();

        crearBurbujas();
    }

    private String[] obtenerPalabra() {

        if (palabrasPendientes.isEmpty()) {

            Collections.addAll(
                    palabrasPendientes,
                    categoriaActual.palabras.clone());

            Collections.shuffle(palabrasPendientes);
        }

        return palabrasPendientes.remove(0);
    }

    // ------------------------------------------------
    // OPCIONES
    // ------------------------------------------------

    private List<String> obtenerOpciones() {

        List<String> opciones = new ArrayList<>();

        opciones.add(preguntaActual[1]);

        List<String> distractores = new ArrayList<>();

        for (String[] palabra : categoriaActual.palabras) {

            if (!palabra[1].equalsIgnoreCase(preguntaActual[1]) &&
                    !distractores.contains(palabra[1])) {

                distractores.add(palabra[1]);
            }
        }

        Collections.shuffle(distractores);

        for (int i = 0;
             i < distractores.size() && opciones.size() < 4;
             i++) {

            opciones.add(distractores.get(i));
        }

        Collections.shuffle(opciones);

        return opciones;
    }

    // ------------------------------------------------
    // CREAR PALABRAS QUE CAEN
    // ------------------------------------------------

    private void crearBurbujas() {

        areaPalabras.post(() -> {

            if (!isAdded() || !rondaActiva) {
                return;
            }

            areaPalabras.removeAllViews();

            cancelarAnimaciones();

            List<String> opciones = obtenerOpciones();

            burbujasRestantes = opciones.size();

            int anchoArea = areaPalabras.getWidth();

            int anchoBurbuja = (anchoArea - dp(28)) / 2;

            int altoArea = areaPalabras.getHeight();

            for (int i = 0; i < opciones.size(); i++) {

                final String palabra = opciones.get(i);

                TextView burbuja = new TextView(requireContext());

                burbuja.setText(palabra);
                burbuja.setTextColor(color(R.color.buti_titulo));
                burbuja.setTextSize(15);
                burbuja.setTypeface(null, Typeface.BOLD);
                burbuja.setGravity(Gravity.CENTER);
                burbuja.setPadding(dp(5), dp(5), dp(5), dp(5));
                burbuja.setMaxLines(2);

                burbuja.setBackground(crearFondo(
                        i % 2 == 0
                                ? color(R.color.buti_superficie)
                                : color(R.color.buti_superficie_alt),
                        color(R.color.buti_dorado)));

                burbuja.setElevation(dp(5));

                burbuja.setTag(Boolean.FALSE);

                int columna = i % 2;
                int fila = i / 2;

                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(
                                anchoBurbuja,
                                dp(64));

                params.leftMargin = dp(8)
                        + columna * (anchoBurbuja + dp(8));

                params.topMargin = 0;

                float posicionInicial = -dp(75 + fila * 100);

                burbuja.setTranslationY(posicionInicial);

                areaPalabras.addView(burbuja, params);

                // EVENTO AL TOCAR UNA PALABRA
                burbuja.setOnClickListener(v -> {

                    if (!rondaActiva ||
                            Boolean.TRUE.equals(burbuja.getTag())) {
                        return;
                    }

                    burbuja.setTag(Boolean.TRUE);

                    if (palabra.equals(preguntaActual[1])) {

                        // Respuesta correcta.
                        resolverRonda(true);

                    } else {

                        // Respuesta incorrecta.
                        SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.ERROR);
                        descontarVida();

                        txtMensaje.setText(
                                "¡Casi! Busca otra palabra.");

                        burbujasRestantes--;

                        burbuja.animate()
                                .alpha(0f)
                                .scaleX(0.7f)
                                .scaleY(0.7f)
                                .setDuration(180)
                                .withEndAction(() ->
                                        areaPalabras.removeView(burbuja))
                                .start();

                        if (vidas <= 0) {

                            finalizarPartida();

                        } else if (burbujasRestantes <= 0) {

                            // Si ya se descontó una vida por el toque
                            // incorrecto, no se vuelve a descontar.
                            resolverRonda(false);
                        }
                    }
                });

                // ANIMACIÓN DE CAÍDA
                ObjectAnimator animacion =
                        ObjectAnimator.ofFloat(
                                burbuja,
                                View.TRANSLATION_Y,
                                posicionInicial,
                                altoArea + dp(80));

                animacion.setDuration(7500);

                animacion.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        if (!rondaActiva ||
                                Boolean.TRUE.equals(burbuja.getTag())) {
                            return;
                        }

                        burbuja.setTag(Boolean.TRUE);

                        burbujasRestantes--;

                        // Cuando todas las palabras terminan de caer,
                        // se resuelve la ronda y se pierde una vida.
                        if (burbujasRestantes <= 0) {

                            resolverRonda(false);
                        }
                    }
                });

                animaciones.add(animacion);

                animacion.start();
            }
        });
    }

    // ------------------------------------------------
    // CANCELAR ANIMACIONES
    // ------------------------------------------------

    private void cancelarAnimaciones() {

        for (ObjectAnimator animacion : animaciones) {

            if (animacion != null && animacion.isRunning()) {
                animacion.cancel();
            }
        }

        animaciones.clear();
    }

    // ------------------------------------------------
    // RESOLVER RONDA
    // ------------------------------------------------

    private void resolverRonda(boolean correcta) {

        if (!rondaActiva) {
            return;
        }

        rondaActiva = false;

        cancelarAnimaciones();

        areaPalabras.removeAllViews();

        if (correcta) {

            puntos += ProgresoButi.PUNTOS_ATRAPA_POR_ACIERTO;

            actualizarPuntos();

            txtMensaje.setText("¡Muy bien! " + preguntaActual[0] + " = " + preguntaActual[1]
                    + "  +" + ProgresoButi.PUNTOS_ATRAPA_POR_ACIERTO + " puntos");

            // Premio: campanita y la palabra en hñähñu.
            SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.ACIERTO);
            SonidosJuego.decirPalabra(requireContext(), categoriaActual.nombre, preguntaActual[0], 400);

        } else {

            // Si no se descontó una vida por una respuesta incorrecta,
            // significa que todas las palabras cayeron sin seleccionarse.
            descontarVida();

            SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.ERROR);
            txtMensaje.setText("¡Se escapó la palabra!");

            if (vidas <= 0) {

                finalizarPartida();

                return;
            }
        }

        if (ronda >= 10) {

            siguienteRondaRunnable = this::finalizarPartida;

        } else {

            siguienteRondaRunnable = this::siguienteRonda;
        }

        // Si acertó, se espera a que se escuche la palabra en hñähñu.
        handler.postDelayed(siguienteRondaRunnable, correcta ? 1500 : 500);
    }

    // ------------------------------------------------
    // RESULTADOS
    // ------------------------------------------------

    private void finalizarPartida() {

        if (!isAdded() ||
                layoutPartida == null ||
                layoutPartida.getVisibility() != View.VISIBLE) {
            return;
        }

        rondaActiva = false;

        detenerTemporizador();

        if (siguienteRondaRunnable != null) {

            handler.removeCallbacks(siguienteRondaRunnable);

            siguienteRondaRunnable = null;
        }

        cancelarAnimaciones();

        areaPalabras.removeAllViews();

        layoutPartida.setVisibility(View.GONE);

        layoutResultado.setVisibility(View.VISIBLE);

        int acertadas = puntos / ProgresoButi.PUNTOS_ATRAPA_POR_ACIERTO;

        if (acertadas >= 8) {

            txtResultado.setText("¡Excelente trabajo!");

        } else if (acertadas >= 5) {

            txtResultado.setText("¡Muy bien!");

        } else {

            txtResultado.setText("¡Sigue practicando!");
        }


        txtDetalleResultado.setText(
                "Categoría: " + categoriaActual.nombre +
                        "\n\nPuntos: " + puntos +
                        "\nPalabras acertadas: " + acertadas + "/10" +
                        "\nVidas restantes: " + vidas +
                        "\nTiempo restante: " + tiempoRestante +
                        " segundos");

        // Pantalla del colibrí: guarda puntos (5 por palabra) y la racha.
        ResultadoButi.Datos datos = new ResultadoButi.Datos();
        datos.etiqueta = "ATRAPA LA PALABRA · " + categoriaActual.nombre.toUpperCase();
        datos.titulo = txtResultado.getText().toString();
        datos.mensaje = acertadas == 0
                ? "¡Inténtalo otra vez, tú puedes!"
                : "Atrapaste " + acertadas + (acertadas == 1 ? " palabra." : " palabras.");
        datos.correctas = acertadas;
        datos.total = 10;
        datos.puntos = puntos;
        datos.exito = acertadas >= 5;

        if (datos.exito) SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.VICTORIA);

        ResultadoButi.mostrarYGuardar(
                requireContext(),
                datos,
                "Ver resumen",
                null,
                null,
                null
        );
    }

    // ------------------------------------------------
    // DISEÑO
    // ------------------------------------------------

    private GradientDrawable crearFondo(int color, int borde) {

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(color);

        fondo.setCornerRadius(dp(18));

        fondo.setStroke(dp(2), borde);

        return fondo;
    }

    private int color(int recurso) {

        return ContextCompat.getColor(requireContext(), recurso);
    }

    private int dp(float valor) {

        return (int) (valor *
                getResources().getDisplayMetrics().density);
    }

    // ------------------------------------------------
    // DESTRUIR VISTA
    // ------------------------------------------------

    @Override
    public void onDestroyView() {

        SonidosJuego.detener();
        detenerTemporizador();

        handler.removeCallbacksAndMessages(null);

        cancelarAnimaciones();

        rondaActiva = false;

        if (areaPalabras != null) {
            areaPalabras.removeAllViews();
        }

        super.onDestroyView();
    }
}