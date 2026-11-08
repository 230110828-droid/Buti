package com.itsoeh.sbsc.buti.juegos;

import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.card.MaterialCardView;

import com.itsoeh.sbsc.buti.R;

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
            tarjeta.setRadius(dp(16));
            tarjeta.setCardElevation(dp(4));
            tarjeta.setCardBackgroundColor(
                    Color.rgb(255, 250, 240));

            tarjeta.setStrokeWidth(dp(1));
            tarjeta.setStrokeColor(
                    Color.rgb(201, 122, 43));

            LinearLayout contenido =
                    new LinearLayout(requireContext());

            contenido.setGravity(Gravity.CENTER_VERTICAL);
            contenido.setOrientation(
                    LinearLayout.HORIZONTAL);

            contenido.setPadding(
                    dp(10), dp(10), dp(10), dp(10));

            ImageView imagen =
                    new ImageView(requireContext());

            imagen.setImageResource(imagenes[i]);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams imagenParams =
                    new LinearLayout.LayoutParams(
                            dp(95), dp(95));

            imagen.setLayoutParams(imagenParams);

            TextView nombre =
                    new TextView(requireContext());

            nombre.setText(nombres[i]);
            nombre.setTextColor(
                    Color.rgb(79, 0, 11));

            nombre.setTextSize(15);
            nombre.setTypeface(null, 1);

            nombre.setPadding(dp(15), 0, 0, 0);

            contenido.addView(imagen);
            contenido.addView(nombre);

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

        LinearLayout opciones =
                new LinearLayout(requireContext());

        opciones.setOrientation(LinearLayout.VERTICAL);
        opciones.setPadding(dp(20), dp(15), dp(20), dp(10));

        TextView titulo =
                new TextView(requireContext());

        titulo.setText("Selecciona la dificultad");
        titulo.setTextSize(18);
        titulo.setTextColor(Color.rgb(79, 0, 11));
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null, 1);

        opciones.addView(titulo);

        String[] textos = {
                "Fácil · 9 piezas",
                "Intermedio · 16 piezas",
                "Difícil · 25 piezas"
        };

        int[] dificultades = {3, 4, 5};

        for (int i = 0; i < textos.length; i++) {

            final int nivel = dificultades[i];

            Button boton = new Button(requireContext());

            boton.setText(textos[i]);
            boton.setAllCaps(false);

            boton.setTextColor(Color.WHITE);
            boton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.rgb(79, 0, 11)));

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

            params.setMargins(0, dp(6), 0, dp(6));

            boton.setLayoutParams(params);

            boton.setOnClickListener(v -> {

                dificultad = nivel;

                iniciarJuego();

            });

            opciones.addView(boton);
        }

        new androidx.appcompat.app.AlertDialog.Builder(
                requireContext())
                .setView(opciones)
                .setNegativeButton("Cancelar", null)
                .show();
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

            Toast.makeText(
                    requireContext(),
                    "No se pudo cargar la imagen",
                    Toast.LENGTH_SHORT
            ).show();

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

        int margen = dp(36);

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

        new androidx.appcompat.app.AlertDialog.Builder(
                requireContext())
                .setTitle("¡Excelente trabajo! 🎉")
                .setMessage(
                        "¡Completaste el rompecabezas!\n\n"
                                + "Lugar: "
                                + nombres[imagenSeleccionada]
                                + "\nMovimientos: "
                                + movimientos
                                + "\nPiezas: "
                                + (dificultad * dificultad))
                .setCancelable(false)
                .setPositiveButton(
                        "Volver a jugar",
                        (dialog, which) -> iniciarJuego())
                .setNegativeButton(
                        "Elegir otro lugar",
                        (dialog, which) -> mostrarSeleccion())
                .show();
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

        fondo.setColor(Color.WHITE);

        fondo.setStroke(
                dp(1),
                Color.rgb(242, 169, 0));

        return fondo;
    }

    private int dp(int valor) {

        return (int) (
                valor * getResources()
                        .getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {

        if (imagenOriginal != null
                && !imagenOriginal.isRecycled()) {

            imagenOriginal.recycle();
            imagenOriginal = null;
        }

        super.onDestroyView();
    }
}
