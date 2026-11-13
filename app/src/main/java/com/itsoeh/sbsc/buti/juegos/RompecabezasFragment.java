package com.itsoeh.sbsc.buti.juegos;

import android.app.Dialog;
import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.card.MaterialCardView;

import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.SonidosJuego;
import com.itsoeh.sbsc.buti.ResultadoButi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RompecabezasFragment extends Fragment {

    private LinearLayout layoutSeleccion;
    private LinearLayout layoutJuego;
    private LinearLayout contenedorImagenes;

    private GridLayout gridRompecabezas;

    private TextView txtNombre;
    private TextView txtDificultad;
    private TextView txtMovimientos;

    private Button btnReiniciar;
    private Button btnCambiarImagen;

    private final int[] imagenes = {
            R.drawable.cerro_mirador_valle_punto_de_cruz,
            R.drawable.nith_iglesia_bordado_floral,
            R.drawable.ixmiquilpan_presidencia_bordado_tradicional,
            R.drawable.ixmiquilpan_templo_fachada_bordada,
            R.drawable.tapiz_bordado_panales_valle_agricola
    };

    private final String[] nombres = {
            "Cerro Mirador del Valle",
            "Iglesia de Nith",
            "Presidencia de Ixmiquilpan",
            "Templo de Ixmiquilpan",
            "Paisaje agrícola del Valle"
    };

    // Palabra en hñähñu que se dice al completar cada lugar
    // (categoría y español; null si no hay una palabra que le quede).
    private final String[][] palabrasLugar = {
            {"naturaleza", "Cerro"},
            {"lugares", "Iglesia"},
            null,
            {"lugares", "Iglesia"},
            null
    };

    private int imagenSeleccionada;
    private int dificultad = 3;
    private int movimientos = 0;

    private Bitmap imagenOriginal;

    private final List<Integer> piezas = new ArrayList<>();

    private int piezaArrastrada = -1;

    public RompecabezasFragment() {
        super(R.layout.fragment_rompecabezas);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Carga los sonidos para que suenen sin retraso.
        SonidosJuego.preparar(requireContext());

        layoutSeleccion = view.findViewById(
                R.id.layoutSeleccionRompecabezas);

        layoutJuego = view.findViewById(
                R.id.layoutJuegoRompecabezas);

        contenedorImagenes = view.findViewById(
                R.id.contenedorImagenesRompecabezas);

        gridRompecabezas = view.findViewById(
                R.id.gridRompecabezas);

        txtNombre = view.findViewById(
                R.id.txtNombreRompecabezas);

        txtDificultad = view.findViewById(
                R.id.txtDificultadRompecabezas);

        txtMovimientos = view.findViewById(
                R.id.txtMovimientosRompecabezas);

        btnReiniciar = view.findViewById(
                R.id.btnReiniciarRompecabezas);

        btnCambiarImagen = view.findViewById(
                R.id.btnCambiarImagenRompecabezas);

        view.findViewById(R.id.btnVolverRompecabezas)
                .setOnClickListener(v ->
                        NavHostFragment.findNavController(this)
                                .popBackStack());

        btnReiniciar.setOnClickListener(v -> iniciarJuego());

        btnCambiarImagen.setOnClickListener(v ->
                mostrarSeleccion());

        mostrarImagenes();

    }

    // Mostrar las cinco imágenes disponibles.

    private void mostrarImagenes() {

        contenedorImagenes.removeAllViews();

        for (int i = 0; i < imagenes.length; i++) {

            final int posicion = i;

            MaterialCardView tarjeta =
                    new MaterialCardView(requireContext());

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            parametros.setMargins(0, dp(7), 0, dp(7));

            tarjeta.setLayoutParams(parametros);
            tarjeta.setRadius(dp(20));
            tarjeta.setCardElevation(dp(3));
            tarjeta.setCardBackgroundColor(color(R.color.buti_superficie));

            tarjeta.setStrokeWidth(dp(1));
            tarjeta.setStrokeColor(color(R.color.buti_borde));
            tarjeta.setRippleColor(
                    android.content.res.ColorStateList.valueOf(
                            color(R.color.buti_dorado)));

            LinearLayout contenido =
                    new LinearLayout(requireContext());

            contenido.setGravity(Gravity.CENTER_VERTICAL);
            contenido.setOrientation(
                    LinearLayout.HORIZONTAL);

            contenido.setPadding(
                    dp(10), dp(10), dp(10), dp(10));

            // Foto con esquinas redondeadas.
            MaterialCardView marco =
                    new MaterialCardView(requireContext());

            marco.setRadius(dp(14));
            marco.setCardElevation(0);
            marco.setStrokeWidth(0);

            ImageView imagen =
                    new ImageView(requireContext());

            imagen.setImageResource(imagenes[i]);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

            marco.addView(imagen, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Nombre y etiqueta.
            LinearLayout textos =
                    new LinearLayout(requireContext());

            textos.setOrientation(LinearLayout.VERTICAL);
            textos.setPadding(dp(14), 0, dp(6), 0);

            TextView etiqueta =
                    new TextView(requireContext());

            etiqueta.setText(String.format(
                    java.util.Locale.getDefault(), "POSTAL %02d", i + 1));
            etiqueta.setTextColor(color(R.color.buti_resalte));
            etiqueta.setTextSize(10);
            etiqueta.setLetterSpacing(0.1f);
            etiqueta.setTypeface(null, 1);

            TextView nombre =
                    new TextView(requireContext());

            nombre.setText(nombres[i]);
            nombre.setTextColor(color(R.color.buti_titulo));
            nombre.setTextSize(16);
            nombre.setTypeface(null, 1);
            nombre.setPadding(0, dp(3), 0, 0);

            TextView jugar =
                    new TextView(requireContext());

            jugar.setText("Armar postal");
            jugar.setTextColor(color(R.color.nivel_verde));
            jugar.setTextSize(12);
            jugar.setTypeface(null, 1);
            jugar.setPadding(0, dp(6), 0, 0);
            jugar.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_flecha_mini, 0);
            jugar.setCompoundDrawableTintList(
                    android.content.res.ColorStateList.valueOf(
                            color(R.color.nivel_verde)));

            textos.addView(etiqueta);
            textos.addView(nombre);
            textos.addView(jugar);

            contenido.addView(marco, new LinearLayout.LayoutParams(dp(96), dp(96)));
            contenido.addView(textos, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            tarjeta.addView(contenido);

            tarjeta.setOnClickListener(v -> {

                imagenSeleccionada = posicion;

                mostrarDificultades();

            });

            contenedorImagenes.addView(tarjeta);
        }
    }

    // Elegir dificultad.

    private void mostrarDificultades() {

        if (!isAdded()) return;

        // Ventana propia de B'ÜTI (antes AlertDialog).
        Dialog ventana = new Dialog(requireContext());
        ventana.setContentView(R.layout.dialog_dificultad_rompecabezas);
        ventana.setCancelable(true);

        TextView txtLugar = ventana.findViewById(R.id.txtLugarDificultad);
        if (txtLugar != null) {
            txtLugar.setText(nombres[imagenSeleccionada].toUpperCase());
        }

        int[] botones = {
                R.id.btnDificultadFacil,
                R.id.btnDificultadIntermedio,
                R.id.btnDificultadDificil
        };

        int[] dificultades = {3, 4, 5};

        for (int i = 0; i < botones.length; i++) {

            final int nivel = dificultades[i];

            View boton = ventana.findViewById(botones[i]);

            if (boton != null) {
                boton.setOnClickListener(v -> {
                    ventana.dismiss();
                    dificultad = nivel;
                    iniciarJuego();
                });
            }
        }

        View cancelar = ventana.findViewById(R.id.btnDificultadCancelar);
        if (cancelar != null) {
            cancelar.setOnClickListener(v -> ventana.dismiss());
        }

        ventana.show();

        if (ventana.getWindow() != null) {
            ventana.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
            ventana.getWindow().setDimAmount(0.6f);
            ventana.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // Iniciar el rompecabezas.

    private void iniciarJuego() {

        if (imagenOriginal != null
                && !imagenOriginal.isRecycled()) {

            imagenOriginal.recycle();
        }

        BitmapFactory.Options opciones =
                new BitmapFactory.Options();

        opciones.inScaled = false;

        imagenOriginal = BitmapFactory.decodeResource(
                getResources(),
                imagenes[imagenSeleccionada],
                opciones);

        if (imagenOriginal == null) {

            ButiAviso.mostrar(
                    this,
                    "No se pudo cargar la imagen",
                    ButiAviso.Tipo.ERROR
            );

            return;
        }

        // Convertir la imagen en un cuadrado.

        int lado = Math.min(
                imagenOriginal.getWidth(),
                imagenOriginal.getHeight());

        int x = (imagenOriginal.getWidth() - lado) / 2;
        int y = (imagenOriginal.getHeight() - lado) / 2;

        Bitmap cuadrada = Bitmap.createBitmap(
                imagenOriginal, x, y, lado, lado);

        if (cuadrada != imagenOriginal) {

            imagenOriginal.recycle();
            imagenOriginal = cuadrada;
        }

        piezas.clear();

        int total = dificultad * dificultad;

        for (int i = 0; i < total; i++) {
            piezas.add(i);
        }

        // Mezclar sin dejar la imagen resuelta.

        do {
            Collections.shuffle(piezas);

        } while (estaResuelto());

        movimientos = 0;

        actualizarContadores();

        txtNombre.setText(nombres[imagenSeleccionada]);

        String nivel = dificultad == 3
                ? "Fácil · 9 piezas"
                : dificultad == 4
                  ? "Intermedio · 16 piezas"
                  : "Difícil · 25 piezas";

        txtDificultad.setText("Dificultad: " + nivel);

        layoutSeleccion.setVisibility(View.GONE);
        layoutJuego.setVisibility(View.VISIBLE);

        construirTablero();
    }

    // Crear las piezas de la imagen.

    private void construirTablero() {

        gridRompecabezas.removeAllViews();

        gridRompecabezas.setColumnCount(dificultad);
        gridRompecabezas.setRowCount(dificultad);

        int anchoPantalla =
                getResources().getDisplayMetrics().widthPixels;

        // Márgenes de la pantalla (32) + marco del tablero (16) + separación (12).
        int margen = dp(60);

        int espacio = Math.max(
                1,
                (anchoPantalla - margen) / dificultad);

        int lado = Math.min(
                espacio,
                dp(115));

        int ladoImagen = imagenOriginal.getWidth()
                / dificultad;

        for (int i = 0; i < piezas.size(); i++) {

            final int posicion = i;

            int piezaOriginal = piezas.get(i);

            int fila = piezaOriginal / dificultad;
            int columna = piezaOriginal % dificultad;

            Bitmap fragmento = Bitmap.createBitmap(
                    imagenOriginal,
                    columna * ladoImagen,
                    fila * ladoImagen,
                    ladoImagen,
                    ladoImagen);

            ImageView pieza =
                    new ImageView(requireContext());

            pieza.setImageBitmap(fragmento);

            pieza.setScaleType(
                    ImageView.ScaleType.FIT_XY);

            pieza.setTag(posicion);

            pieza.setBackground(
                    crearBorde());

            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams();

            params.width = lado;
            params.height = lado;

            params.setMargins(
                    dp(1), dp(1), dp(1), dp(1));

            pieza.setLayoutParams(params);

            // Permitir arrastrar una pieza.

            pieza.setOnLongClickListener(v -> {

                piezaArrastrada = posicion;

                ClipData datos = ClipData.newPlainText(
                        "pieza",
                        String.valueOf(posicion));

                View.DragShadowBuilder sombra =
                        new View.DragShadowBuilder(v);

                v.startDragAndDrop(
                        datos,
                        sombra,
                        null,
                        0);

                return true;
            });

            // Recibir otra pieza y realizar el intercambio.

            pieza.setOnDragListener((v, evento) -> {

                if (evento.getAction()
                        == DragEvent.ACTION_DROP) {

                    int destino = (int) v.getTag();

                    int origen = Integer.parseInt(
                            evento.getClipData()
                                    .getItemAt(0)
                                    .getText()
                                    .toString());

                    intercambiarPiezas(origen, destino);

                    return true;
                }

                return true;
            });

            gridRompecabezas.addView(pieza);
        }
    }

    // Intercambiar dos piezas.

    private void intercambiarPiezas(
            int origen,
            int destino) {

        if (origen < 0
                || destino < 0
                || origen >= piezas.size()
                || destino >= piezas.size()
                || origen == destino) {

            return;
        }

        Collections.swap(piezas, origen, destino);

        SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.TIC);

        movimientos++;

        actualizarContadores();

        if (estaResuelto()) {

            construirTablero();

            mostrarVictoria();

        } else {

            construirTablero();
        }
    }

    // Comprobar si todas las piezas están en orden.

    private boolean estaResuelto() {

        for (int i = 0; i < piezas.size(); i++) {

            if (piezas.get(i) != i) {
                return false;
            }
        }

        return true;
    }

    private void actualizarContadores() {

        txtMovimientos.setText(
                "Movimientos: " + movimientos);
    }

    // Felicitación al terminar.

    private void mostrarVictoria() {

        if (!isAdded()) return;

        // Melodía de victoria y, si el lugar tiene palabra, se dice en hñähñu.
        SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.VICTORIA);

        String[] palabra = imagenSeleccionada >= 0 && imagenSeleccionada < palabrasLugar.length
                ? palabrasLugar[imagenSeleccionada] : null;

        String extra = "";

        if (palabra != null) {
            SonidosJuego.decirPalabra(requireContext(), palabra[0], palabra[1], 1300);
            String hnahnu = SonidosJuego.hnahnu(palabra[0], palabra[1]);
            if (hnahnu != null) extra = "\n" + palabra[1] + " en hñähñu: " + hnahnu;
        }

        // Pantalla del colibrí: más piezas = más puntos (3x3=10, 4x4=20, 5x5=30).
        ResultadoButi.Datos datos = new ResultadoButi.Datos();
        datos.etiqueta = "ROMPECABEZAS " + dificultad + "×" + dificultad;
        datos.titulo = "¡Excelente trabajo!";
        datos.mensaje = "¡Completaste el rompecabezas!\n"
                + "Lugar: " + nombres[imagenSeleccionada]
                + " · Movimientos: " + movimientos
                + extra;
        datos.correctas = dificultad * dificultad;
        datos.total = dificultad * dificultad;
        datos.puntos = ProgresoButi.PUNTOS_ROMPECABEZAS_POR_NIVEL * Math.max(1, dificultad - 2);
        datos.exito = true;

        ResultadoButi.mostrarYGuardar(
                requireContext(),
                datos,
                "Volver a jugar",
                this::iniciarJuego,
                "Elegir otro lugar",
                this::mostrarSeleccion
        );
    }

    private void mostrarSeleccion() {

        layoutJuego.setVisibility(View.GONE);
        layoutSeleccion.setVisibility(View.VISIBLE);

        if (imagenOriginal != null
                && !imagenOriginal.isRecycled()) {

            imagenOriginal.recycle();
            imagenOriginal = null;
        }

        gridRompecabezas.removeAllViews();
    }

    private GradientDrawable crearBorde() {

        GradientDrawable fondo =
                new GradientDrawable();

        fondo.setColor(color(R.color.buti_superficie));

        fondo.setStroke(
                dp(1),
                color(R.color.buti_dorado));

        return fondo;
    }

    private int color(int recurso) {

        return ContextCompat.getColor(requireContext(), recurso);
    }

    private int dp(int valor) {

        return (int) (
                valor * getResources()
                        .getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {

        SonidosJuego.detener();

        if (imagenOriginal != null
                && !imagenOriginal.isRecycled()) {

            imagenOriginal.recycle();
            imagenOriginal = null;
        }

        super.onDestroyView();
    }
}