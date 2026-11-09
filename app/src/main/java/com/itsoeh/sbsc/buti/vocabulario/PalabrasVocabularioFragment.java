package com.itsoeh.sbsc.buti.vocabulario;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PalabrasVocabularioFragment extends Fragment {

    private LinearLayout layoutListaPalabras;
    private LinearLayout btnMostrarFavoritos;

    private ImageView btnRegresar;
    private ImageView imgIconoCategoria;

    private TextView txtTituloCategoria;
    private TextView txtDescripcionCategoria;
    private TextView txtCantidadPalabras;
    private TextView txtTextoFavoritos;

    private EditText edtBuscarPalabra;

    private String categoriaSeleccionada = "Saludos";

    private boolean mostrandoFavoritos = false;

    private SharedPreferences preferencias;
    private Set<String> favoritos;

    private MediaPlayer reproductor;

    private ArrayList<String[]> palabrasCategoria = new ArrayList<>();

    public PalabrasVocabularioFragment() {
        super(R.layout.fragment_palabras_vocabulario);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        layoutListaPalabras = view.findViewById(R.id.layoutListaPalabras);
        btnRegresar = view.findViewById(R.id.btnRegresarPalabras);

        imgIconoCategoria = view.findViewById(R.id.imgIconoCategoria);

        txtTituloCategoria = view.findViewById(R.id.txtTituloCategoria);
        txtDescripcionCategoria = view.findViewById(R.id.txtDescripcionCategoria);
        txtCantidadPalabras = view.findViewById(R.id.txtCantidadPalabras);
        txtTextoFavoritos = view.findViewById(R.id.txtTextoFavoritos);

        edtBuscarPalabra = view.findViewById(R.id.edtBuscarPalabra);
        btnMostrarFavoritos = view.findViewById(R.id.btnMostrarFavoritos);

        preferencias = requireContext().getSharedPreferences(
                "ButiFavoritos",
                Context.MODE_PRIVATE
        );

        favoritos = new HashSet<>(
                preferencias.getStringSet("palabras", new HashSet<>())
        );

        if (getArguments() != null) {
            categoriaSeleccionada = getArguments().getString(
                    "categoriaSeleccionada",
                    "Saludos"
            );
        }

        btnRegresar.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        btnMostrarFavoritos.setOnClickListener(v -> {

            mostrandoFavoritos = !mostrandoFavoritos;

            txtTextoFavoritos.setText(
                    mostrandoFavoritos
                            ? "Ver todas las palabras"
                            : "Mis palabras favoritas"
            );

            actualizarLista();
        });

        edtBuscarPalabra.addTextChangedListener(new TextWatcher() {

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

                actualizarLista();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        cargarCategoria();
    }

    // CONFIGURAR ENCABEZADO Y CATEGORÍA
    private void cargarCategoria() {

        palabrasCategoria = obtenerPalabras(categoriaSeleccionada);

        txtTituloCategoria.setText(categoriaSeleccionada);

        txtDescripcionCategoria.setText(
                obtenerDescripcion(categoriaSeleccionada)
        );

        imgIconoCategoria.setImageResource(
                obtenerIconoCategoria(categoriaSeleccionada)
        );

        actualizarLista();
    }

    private String obtenerDescripcion(String categoria) {

        switch (categoria) {

            case "Colores":
                return "Descubre los colores que dan vida a nuestra cultura.";

            case "Números":
            case "Numeros":
                return "Cuenta, aprende y descubre nuevas formas de expresarte.";

            case "Saludos":
                return "Cada saludo es una forma de compartir nuestra identidad.";

            case "Tiempo":
                return "Aprende a nombrar los momentos que forman nuestros días.";

            case "Plantas":
                return "Conoce las palabras de la naturaleza que nos rodea.";

            case "Animales":
                return "Descubre cómo nombramos a los animales en Hñähñu.";

            case "Objetos":
                return "Las cosas de todos los días también cuentan historias.";

            case "Cuerpo":
                return "Conoce y aprende las palabras de nuestro cuerpo.";

            case "Comida":
                return "Sabores y palabras que forman parte de nuestras raíces.";

            case "Naturaleza":
                return "Explora las palabras de la tierra, el cielo y la vida.";

            case "Lugares":
                return "Aprende a nombrar los espacios que nos rodean.";

            default:
                return "Cada palabra guarda una parte de nuestra historia.";
        }
    }

    // ICONOS REPRESENTATIVOS DE LAS CATEGORÍAS
    private int obtenerIconoCategoria(String categoria) {

        switch (categoria) {

            case "Colores":
                return R.drawable.ic_rombo7;

            case "Números":
            case "Numeros":
                return R.drawable.ic_numeros;

            case "Saludos":
                return R.drawable.ic_manitas;

            case "Tiempo":
                return R.drawable.ic_reloj;

            case "Plantas":
                return R.drawable.ic_nopal;

            case "Animales":
                return R.drawable.ic_venado;

            case "Objetos":
                return R.drawable.ic_jarronnaranja;

            case "Cuerpo":
                return R.drawable.ic_humano;

            case "Comida":
                return R.drawable.ic_teflauta;

            case "Naturaleza":
                return R.drawable.ic_solluna;

            case "Lugares":
                return R.drawable.ic_ubicacion;

            default:
                return R.drawable.ic_rombo7;
        }
    }

    // ACTUALIZAR LISTA
    private void actualizarLista() {

        if (layoutListaPalabras == null ||
                edtBuscarPalabra == null) {
            return;
        }

        layoutListaPalabras.removeAllViews();

        String busqueda = edtBuscarPalabra.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        int cantidad = 0;

        for (String[] palabra : palabrasCategoria) {

            String espanol = palabra[0];
            String hnahnu = palabra[1];

            String clave = crearClave(espanol);

            boolean coincideBusqueda =
                    espanol.toLowerCase(Locale.ROOT).contains(busqueda) ||
                            hnahnu.toLowerCase(Locale.ROOT).contains(busqueda);

            boolean esFavorito = favoritos.contains(clave);

            if (!coincideBusqueda) {
                continue;
            }

            if (mostrandoFavoritos && !esFavorito) {
                continue;
            }

            agregarTarjetaPalabra(
                    espanol,
                    hnahnu,
                    esFavorito,
                    cantidad + 1
            );

            cantidad++;
        }

        txtCantidadPalabras.setText(
                cantidad + (cantidad == 1
                        ? " PALABRA"
                        : " PALABRAS")
        );

        if (cantidad == 0) {
            mostrarMensajeVacio();
        }
    }

    // DISEÑO DE CADA PALABRA
    private void agregarTarjetaPalabra(
            String espanol,
            String hnahnu,
            boolean esFavorito,
            int numero) {

        Context contexto = requireContext();

        LinearLayout tarjeta = new LinearLayout(contexto);

        tarjeta.setOrientation(LinearLayout.HORIZONTAL);
        tarjeta.setGravity(Gravity.CENTER_VERTICAL);
        tarjeta.setPadding(dp(16), dp(13), dp(12), dp(13));

        tarjeta.setBackgroundResource(R.drawable.bg_palabra_tarjeta);
        tarjeta.setElevation(dp(2));

        LinearLayout.LayoutParams parametrosTarjeta =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosTarjeta.setMargins(0, 0, 0, dp(12));

        tarjeta.setLayoutParams(parametrosTarjeta);

        // Número decorativo de la palabra
        TextView numeroPalabra = new TextView(contexto);

        numeroPalabra.setText(String.format(Locale.getDefault(), "%02d", numero));
        numeroPalabra.setTextColor(color(R.color.buti_crema));
        numeroPalabra.setTextSize(13);
        numeroPalabra.setTypeface(null, Typeface.BOLD);
        numeroPalabra.setGravity(Gravity.CENTER);

        numeroPalabra.setBackgroundResource(R.drawable.bg_numero_palabra);

        tarjeta.addView(numeroPalabra,
                new LinearLayout.LayoutParams(dp(39), dp(39)));

        // Contenido de la palabra
        LinearLayout contenido = new LinearLayout(contexto);

        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(12), 0, dp(5), 0);

        tarjeta.addView(contenido,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                ));

        // Etiqueta de español
        TextView etiquetaEspanol = new TextView(contexto);

        etiquetaEspanol.setText("ESPAÑOL");
        etiquetaEspanol.setTextColor(color(R.color.buti_resalte));
        etiquetaEspanol.setTextSize(9);
        etiquetaEspanol.setTypeface(null, Typeface.BOLD);
        etiquetaEspanol.setLetterSpacing(0.12f);

        contenido.addView(etiquetaEspanol);

        // Palabra en español
        TextView textoEspanol = new TextView(contexto);

        textoEspanol.setText(espanol);
        textoEspanol.setTextColor(color(R.color.buti_titulo));
        textoEspanol.setTextSize(19);
        textoEspanol.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams margenEspanol =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        margenEspanol.setMargins(0, dp(2), 0, dp(8));

        contenido.addView(textoEspanol, margenEspanol);

        // Separador decorativo
        View separador = new View(contexto);

        separador.setBackgroundColor(color(R.color.buti_borde));

        contenido.addView(separador,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                ));

        // Palabra Hñähñu
        TextView textoHnahnu = new TextView(contexto);

        textoHnahnu.setText(hnahnu);
        // El hñähñu resalta en rosa (más brillante en modo oscuro).
        textoHnahnu.setTextColor(color(R.color.nivel_rosa));
        textoHnahnu.setTextSize(17);
        textoHnahnu.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams margenHnahnu =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        margenHnahnu.setMargins(0, dp(8), 0, 0);

        contenido.addView(textoHnahnu, margenHnahnu);

        // Controles bordados
        LinearLayout controles = new LinearLayout(contexto);

        controles.setOrientation(LinearLayout.VERTICAL);
        controles.setGravity(Gravity.CENTER);

        tarjeta.addView(controles,
                new LinearLayout.LayoutParams(
                        dp(44),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

        // Favorito
        ImageView botonFavorito = new ImageView(contexto);

        botonFavorito.setImageResource(R.drawable.ic_favorito_bordado);
        botonFavorito.setScaleType(ImageView.ScaleType.FIT_CENTER);
        botonFavorito.setContentDescription("Agregar a favoritos");
        botonFavorito.setBackgroundResource(R.drawable.bg_control_palabra);
        botonFavorito.setPadding(dp(6), dp(6), dp(6), dp(6));

        botonFavorito.setAlpha(esFavorito ? 1f : 0.55f);

        controles.addView(botonFavorito,
                new LinearLayout.LayoutParams(dp(40), dp(40)));

        botonFavorito.setOnClickListener(v -> {

            String clave = crearClave(espanol);

            Set<String> nuevosFavoritos = new HashSet<>(
                    preferencias.getStringSet(
                            "palabras",
                            new HashSet<>()
                    )
            );

            if (nuevosFavoritos.contains(clave)) {
                nuevosFavoritos.remove(clave);
            } else {
                nuevosFavoritos.add(clave);
            }

            preferencias.edit()
                    .putStringSet("palabras", nuevosFavoritos)
                    .apply();

            favoritos = nuevosFavoritos;

            actualizarLista();
        });

        // Audio bordado
        ImageView botonAudio = new ImageView(contexto);

        botonAudio.setImageResource(R.drawable.ic_audio_bordado);
        botonAudio.setScaleType(ImageView.ScaleType.FIT_CENTER);
        botonAudio.setContentDescription("Escuchar pronunciación");
        botonAudio.setBackgroundResource(R.drawable.bg_control_palabra);
        botonAudio.setPadding(dp(6), dp(6), dp(6), dp(6));

        LinearLayout.LayoutParams margenAudio =
                new LinearLayout.LayoutParams(dp(40), dp(40));

        margenAudio.setMargins(0, dp(8), 0, 0);

        controles.addView(botonAudio, margenAudio);

        botonAudio.setOnClickListener(v ->
                reproducirAudio(espanol)
        );

        layoutListaPalabras.addView(tarjeta);
    }

    // MENSAJE CUANDO NO HAY RESULTADOS
    private void mostrarMensajeVacio() {

        LinearLayout contenedor = new LinearLayout(requireContext());

        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.CENTER);
        contenedor.setPadding(dp(20), dp(28), dp(20), dp(28));

        contenedor.setBackgroundResource(R.drawable.bg_practicar_tarjeta);

        // Colibrí descansando para el mensaje vacío.
        ImageView colibri = new ImageView(requireContext());
        colibri.setImageResource(R.drawable.ic_colibri_descansando);
        colibri.setScaleType(ImageView.ScaleType.FIT_CENTER);
        colibri.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        LinearLayout.LayoutParams margenColibri =
                new LinearLayout.LayoutParams(dp(84), dp(72));
        margenColibri.setMargins(0, 0, 0, dp(10));

        contenedor.addView(colibri, margenColibri);

        TextView titulo = new TextView(requireContext());

        titulo.setText(
                mostrandoFavoritos
                        ? "¡Tus favoritas te esperan!"
                        : "¡Sigamos buscando!"
        );

        titulo.setTextColor(color(R.color.buti_titulo));
        titulo.setTextSize(18);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);

        contenedor.addView(titulo);

        TextView mensaje = new TextView(requireContext());

        mensaje.setText(
                mostrandoFavoritos
                        ? "Guarda las palabras que más te gusten para encontrarlas aquí."
                        : "Prueba con otra palabra o escríbela en Hñähñu."
        );

        mensaje.setTextColor(color(R.color.buti_texto_secundario));
        mensaje.setTextSize(13);
        mensaje.setGravity(Gravity.CENTER);
        mensaje.setPadding(0, dp(8), 0, 0);

        contenedor.addView(mensaje);

        layoutListaPalabras.addView(contenedor);
    }

    // CLAVE PARA GUARDAR FAVORITOS
    private String crearClave(String palabra) {

        return categoriaSeleccionada + "_" +
                palabra.trim().toLowerCase(Locale.ROOT);
    }

    // OBTENER PALABRAS DE CADA CATEGORÍA
    private ArrayList<String[]> obtenerPalabras(String categoria) {

        switch (categoria) {

            case "Colores":
                return convertir(VocabularioDatos.COLORES);

            case "Números":
            case "Numeros":
                return convertir(VocabularioDatos.NUMEROS);

            case "Saludos":
                return convertir(VocabularioDatos.SALUDOS);

            case "Tiempo":
                return convertir(VocabularioDatos.TIEMPO);

            case "Plantas":
                return convertir(VocabularioDatos.PLANTAS);

            case "Animales":
                return convertir(VocabularioDatos.ANIMALES);

            case "Objetos":
                return convertir(VocabularioDatos.OBJETOS);

            case "Cuerpo":
                return convertir(VocabularioDatos.CUERPO);

            case "Comida":
                return convertir(VocabularioDatos.COMIDA);

            case "Naturaleza":
                return convertir(VocabularioDatos.NATURALEZA);

            case "Lugares":
                return convertir(VocabularioDatos.LUGARES);

            default:
                return convertir(VocabularioDatos.SALUDOS);
        }
    }

    private ArrayList<String[]> convertir(String[][] datos) {

        ArrayList<String[]> lista = new ArrayList<>();

        if (datos != null) {
            for (String[] palabra : datos) {
                if (palabra != null && palabra.length >= 2) {
                    lista.add(new String[]{
                            palabra[0],
                            palabra[1]
                    });
                }
            }
        }

        return lista;
    }

    // REPRODUCIR AUDIO
    private void reproducirAudio(String palabra) {

        int audio = AudiosVocabulario.obtenerAudio(
                requireContext(),
                categoriaSeleccionada,
                palabra
        );

        if (audio == 0) {
            ButiAviso.mostrar(
                    this,
                    "Audio no disponible para esta palabra",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        liberarReproductor();

        reproductor = MediaPlayer.create(requireContext(), audio);

        if (reproductor != null) {

            reproductor.setOnCompletionListener(mp -> {
                mp.release();

                if (reproductor == mp) {
                    reproductor = null;
                }
            });

            reproductor.start();

        } else {

            ButiAviso.mostrar(
                    this,
                    "No se pudo reproducir el audio",
                    ButiAviso.Tipo.ERROR
            );
        }
    }

    private void liberarReproductor() {

        if (reproductor != null) {

            try {
                if (reproductor.isPlaying()) {
                    reproductor.stop();
                }

                reproductor.release();

            } catch (Exception ignored) {
            }

            reproductor = null;
        }
    }

    private int color(int recurso) {

        return ContextCompat.getColor(requireContext(), recurso);
    }

    private int dp(int valor) {

        return (int) (
                valor * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    public void onDestroyView() {

        liberarReproductor();

        layoutListaPalabras = null;
        btnMostrarFavoritos = null;
        btnRegresar = null;
        imgIconoCategoria = null;

        txtTituloCategoria = null;
        txtDescripcionCategoria = null;
        txtCantidadPalabras = null;
        txtTextoFavoritos = null;

        edtBuscarPalabra = null;

        super.onDestroyView();
    }
}