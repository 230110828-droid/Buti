package com.itsoeh.sbsc.buti.juegos;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.itsoeh.sbsc.buti.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MemoramaFragment extends Fragment {

    private GridLayout gridMemorama;

    private TextView txtTiempo;
    private TextView txtMovimientos;
    private TextView txtParejas;
    private TextView txtMensaje;

    private TextView btnReiniciar;
    private TextView btnVolver;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final List<Carta> cartas = new ArrayList<>();

    private Carta primeraCarta = null;
    private Carta segundaCarta = null;

    private int movimientos = 0;
    private int parejasEncontradas = 0;

    private boolean bloqueado = false;
    private boolean temporizadorIniciado = false;

    private long tiempoInicial = 0;

    private static final int TOTAL_PAREJAS = 6;

    private final int[] imagenes = {
            R.drawable.img_animales_zorro,
            R.drawable.img_animales_conejo,
            R.drawable.img_plantas_maguey,
            R.drawable.img_plantas_nopal,
            R.drawable.img_objetos_olla,
            R.drawable.img_objetos_molcajete
    };

    private final Runnable actualizarTiempo = new Runnable() {
        @Override
        public void run() {

            if (!isAdded() || txtTiempo == null) {
                return;
            }

            long segundos = (SystemClock.elapsedRealtime() - tiempoInicial) / 1000;

            int minutos = (int) (segundos / 60);
            int restantes = (int) (segundos % 60);

            txtTiempo.setText(
                    String.format(Locale.getDefault(), "%02d:%02d",
                            minutos, restantes)
            );

            handler.postDelayed(this, 1000);
        }
    };

    public MemoramaFragment() {
        super(R.layout.fragment_memorama);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        gridMemorama = view.findViewById(R.id.gridMemorama);

        txtTiempo = view.findViewById(R.id.txtTiempoMemorama);
        txtMovimientos = view.findViewById(R.id.txtMovimientosMemorama);
        txtParejas = view.findViewById(R.id.txtParejasMemorama);
        txtMensaje = view.findViewById(R.id.txtMensajeMemorama);

        btnReiniciar = view.findViewById(R.id.btnReiniciarMemorama);
        btnVolver = view.findViewById(R.id.btnVolverMemorama);

        btnReiniciar.setOnClickListener(v -> reiniciarJuego());

        btnVolver.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });

        iniciarJuego();
    }

    private void iniciarJuego() {

        handler.removeCallbacks(actualizarTiempo);

        cartas.clear();

        primeraCarta = null;
        segundaCarta = null;

        movimientos = 0;
        parejasEncontradas = 0;

        bloqueado = false;
        temporizadorIniciado = false;

        tiempoInicial = 0;

        txtTiempo.setText("00:00");
        txtMovimientos.setText("0");
        txtParejas.setText("0 / " + TOTAL_PAREJAS);

        txtMensaje.setText("¡Encuentra las imágenes iguales!");

        gridMemorama.removeAllViews();

        // Crear dos cartas de cada imagen.
        for (int imagen : imagenes) {

            cartas.add(new Carta(imagen));
            cartas.add(new Carta(imagen));
        }

        // Mezclar las cartas.
        Collections.shuffle(cartas);

        // Crear las 12 cartas visuales.
        for (int i = 0; i < cartas.size(); i++) {

            Carta carta = cartas.get(i);

            crearVistaCarta(carta, i);
        }
    }

    private void crearVistaCarta(Carta carta, int posicion) {

        FrameLayout contenedor = new FrameLayout(requireContext());

        contenedor.setBackground(crearFondo(
                Color.WHITE,
                Color.parseColor("#D6A43B"),
                16
        ));

        contenedor.setElevation(dp(3));

        ImageView imagen = new ImageView(requireContext());

        imagen.setImageResource(carta.imagen);
        imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imagen.setPadding(dp(7), dp(7), dp(7), dp(7));

        imagen.setBackground(crearFondo(
                Color.WHITE,
                Color.parseColor("#D6A43B"),
                16
        ));

        imagen.setVisibility(View.GONE);

        TextView reverso = new TextView(requireContext());

        reverso.setText("✿");
        reverso.setTextSize(34);
        reverso.setTextColor(Color.parseColor("#F2A900"));
        reverso.setGravity(Gravity.CENTER);

        reverso.setBackground(crearFondo(
                Color.parseColor("#4F000B"),
                Color.parseColor("#F2A900"),
                16
        ));

        FrameLayout.LayoutParams parametros =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        contenedor.addView(imagen, parametros);
        contenedor.addView(reverso, parametros);

        carta.contenedor = contenedor;
        carta.imagenVista = imagen;
        carta.reversoVista = reverso;

        contenedor.setOnClickListener(v -> seleccionarCarta(carta));

        GridLayout.LayoutParams parametrosGrid =
                new GridLayout.LayoutParams(
                        GridLayout.spec(posicion / 3, 1, 1f),
                        GridLayout.spec(posicion % 3, 1, 1f)
                );

        parametrosGrid.width = 0;
        parametrosGrid.height = 0;

        parametrosGrid.setMargins(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        gridMemorama.addView(contenedor, parametrosGrid);
    }

    private void seleccionarCarta(Carta carta) {

        if (bloqueado || carta.descubierta || carta.encontrada) {
            return;
        }

        iniciarTemporizador();

        mostrarCarta(carta);

        if (primeraCarta == null) {

            primeraCarta = carta;

            txtMensaje.setText("¡Busca su pareja!");

            return;
        }

        segundaCarta = carta;

        movimientos++;

        txtMovimientos.setText(String.valueOf(movimientos));

        comprobarPareja();
    }

    private void mostrarCarta(Carta carta) {

        carta.descubierta = true;

        carta.imagenVista.setVisibility(View.VISIBLE);
        carta.reversoVista.setVisibility(View.GONE);
    }

    private void ocultarCarta(Carta carta) {

        carta.descubierta = false;

        carta.imagenVista.setVisibility(View.GONE);
        carta.reversoVista.setVisibility(View.VISIBLE);
    }

    private void comprobarPareja() {

        if (primeraCarta.imagen == segundaCarta.imagen) {

            // Las imágenes son iguales.
            primeraCarta.encontrada = true;
            segundaCarta.encontrada = true;

            parejasEncontradas++;

            txtParejas.setText(
                    parejasEncontradas + " / " + TOTAL_PAREJAS
            );

            txtMensaje.setText("¡Encontraste una pareja!");

            primeraCarta = null;
            segundaCarta = null;

            if (parejasEncontradas == TOTAL_PAREJAS) {

                terminarJuego();
            }

        } else {

            // Las imágenes son diferentes.
            bloqueado = true;

            txtMensaje.setText("¡Inténtalo nuevamente!");

            handler.postDelayed(() -> {

                if (!isAdded()) {
                    return;
                }

                ocultarCarta(primeraCarta);
                ocultarCarta(segundaCarta);

                primeraCarta = null;
                segundaCarta = null;

                bloqueado = false;

                txtMensaje.setText("¡Encuentra las imágenes iguales!");

            }, 900);
        }
    }

    private void iniciarTemporizador() {

        if (temporizadorIniciado) {
            return;
        }

        temporizadorIniciado = true;

        tiempoInicial = SystemClock.elapsedRealtime();

        handler.post(actualizarTiempo);
    }

    private void terminarJuego() {

        handler.removeCallbacks(actualizarTiempo);

        bloqueado = true;

        long segundos = (SystemClock.elapsedRealtime() - tiempoInicial) / 1000;

        int minutos = (int) (segundos / 60);
        int restantes = (int) (segundos % 60);

        String tiempoFinal = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutos,
                restantes
        );

        txtMensaje.setText("¡Completaste el memorama!");

        new AlertDialog.Builder(requireContext())
                .setTitle("¡Excelente!")
                .setMessage(
                        "¡Encontraste todas las parejas!\n\n" +
                                "Tiempo: " + tiempoFinal + "\n" +
                                "Movimientos: " + movimientos + "\n" +
                                "Parejas: " + parejasEncontradas
                )
                .setCancelable(false)
                .setPositiveButton("Jugar otra vez", (dialog, which) -> {
                    reiniciarJuego();
                })
                .setNegativeButton("Salir", (dialog, which) -> {
                    NavHostFragment.findNavController(this).popBackStack();
                })
                .show();
    }

    private void reiniciarJuego() {

        handler.removeCallbacks(actualizarTiempo);

        handler.removeCallbacksAndMessages(null);

        iniciarJuego();
    }

    private GradientDrawable crearFondo(
            int color,
            int borde,
            int radio) {

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(color);
        fondo.setCornerRadius(dp(radio));

        fondo.setStroke(
                dp(2),
                borde
        );

        return fondo;
    }

    private int dp(float valor) {

        return (int) (valor *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroyView() {

        handler.removeCallbacksAndMessages(null);

        super.onDestroyView();

        gridMemorama = null;
        txtTiempo = null;
        txtMovimientos = null;
        txtParejas = null;
        txtMensaje = null;

        btnReiniciar = null;
        btnVolver = null;
    }

    private static class Carta {

        int imagen;

        boolean descubierta = false;
        boolean encontrada = false;

        FrameLayout contenedor;
        ImageView imagenVista;
        TextView reversoVista;

        Carta(int imagen) {
            this.imagen = imagen;
        }
    }
}
