package com.itsoeh.sbsc.buti.juegos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.SonidosJuego;
import com.itsoeh.sbsc.buti.ResultadoButi;

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

    // View: así funcionan con el botón Material y la flecha redonda.
    private View btnReiniciar;
    private View btnVolver;

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

    // Palabra de cada imagen (mismo orden): categoría y español,
    // para decirla en hñähñu al encontrar la pareja.
    private final String[][] palabras = {
            {"animales", "Zorro"},
            {"animales", "Conejo"},
            {"plantas", "Maguey"},
            {"plantas", "Nopal"},
            {"objetos", "Olla"},
            {"objetos", "Molcajete"}
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

        // Carga los sonidos para que suenen sin retraso.
        SonidosJuego.preparar(requireContext());

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

        contenedor.setBackgroundResource(R.drawable.bg_carta_frente);

        contenedor.setElevation(dp(3));

        ImageView imagen = new ImageView(requireContext());

        imagen.setImageResource(carta.imagen);
        imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imagen.setPadding(dp(7), dp(7), dp(7), dp(7));

        imagen.setVisibility(View.GONE);

        // Reverso: tu diseño en punto de cruz, con borde dorado y esquinas redondeadas.
        ImageView reverso = new ImageView(requireContext());

        reverso.setImageResource(R.drawable.img_reverso_memorama);
        reverso.setScaleType(ImageView.ScaleType.CENTER_CROP);
        reverso.setPadding(dp(3), dp(3), dp(3), dp(3));
        reverso.setBackgroundResource(R.drawable.bg_carta_reverso);
        reverso.setClipToOutline(true);

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
        SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.TIC);

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

            // Borde verde para las parejas encontradas.
            primeraCarta.contenedor.setBackgroundResource(R.drawable.bg_carta_encontrada);
            segundaCarta.contenedor.setBackgroundResource(R.drawable.bg_carta_encontrada);

            parejasEncontradas++;

            txtParejas.setText(
                    parejasEncontradas + " / " + TOTAL_PAREJAS
            );

            // Premio: campanita y la palabra en hñähñu.
            String[] palabra = palabraDe(primeraCarta.imagen);
            SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.ACIERTO);

            if (palabra != null) {
                String hnahnu = SonidosJuego.hnahnu(palabra[0], palabra[1]);
                txtMensaje.setText(hnahnu != null
                        ? "¡" + palabra[1] + "! En hñähñu: " + hnahnu
                        : "¡Encontraste una pareja!");
                SonidosJuego.decirPalabra(requireContext(), palabra[0], palabra[1], 450);
            } else {
                txtMensaje.setText("¡Encontraste una pareja!");
            }

            primeraCarta = null;
            segundaCarta = null;

            if (parejasEncontradas == TOTAL_PAREJAS) {

                // Se espera a que se escuche la última palabra.
                bloqueado = true;
                handler.removeCallbacks(actualizarTiempo);
                handler.postDelayed(() -> {
                    // Esa espera no cuenta en el tiempo del juego.
                    tiempoInicial += 1800;
                    if (isAdded()) terminarJuego();
                }, 1800);
            }

        } else {

            // Las imágenes son diferentes.
            bloqueado = true;

            SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.ERROR);

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

        SonidosJuego.tocar(requireContext(), SonidosJuego.Efecto.VICTORIA);

        // Pantalla del colibrí: muestra el resultado y guarda puntos y racha.
        ResultadoButi.Datos datos = new ResultadoButi.Datos();
        datos.etiqueta = "MEMORAMA";
        datos.titulo = "¡Excelente!";
        datos.mensaje = "¡Encontraste todas las parejas!\n"
                + "Tiempo: " + tiempoFinal + " · Movimientos: " + movimientos;
        datos.correctas = parejasEncontradas;
        datos.total = parejasEncontradas;
        datos.puntos = ProgresoButi.PUNTOS_MEMORAMA;
        datos.exito = true;

        ResultadoButi.mostrarYGuardar(
                requireContext(),
                datos,
                "Jugar otra vez",
                this::reiniciarJuego,
                "Salir",
                () -> {
                    if (isAdded()) {
                        NavHostFragment.findNavController(this).popBackStack();
                    }
                }
        );
    }

    private String[] palabraDe(int imagen) {
        for (int i = 0; i < imagenes.length && i < palabras.length; i++) {
            if (imagenes[i] == imagen) return palabras[i];
        }
        return null;
    }

    private void reiniciarJuego() {

        SonidosJuego.detener();

        handler.removeCallbacks(actualizarTiempo);

        handler.removeCallbacksAndMessages(null);

        iniciarJuego();
    }

    private int dp(float valor) {

        return (int) (valor *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroyView() {

        handler.removeCallbacksAndMessages(null);
        SonidosJuego.detener();

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
        ImageView reversoVista;

        Carta(int imagen) {
            this.imagen = imagen;
        }
    }
}