package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

public class BienvenidaAnimador {

    private static final int[] TODAS = {
            R.id.margenSuperior,
            R.id.margenInferior,
            R.id.viewHalo,
            R.id.imgLogoBienvenida,
            R.id.tvTituloBienvenida,
            R.id.lineaAcento,
            R.id.tvDescripcionBienvenida,
            R.id.cardCultura,
            R.id.imgRombo1,
            R.id.imgRombo2,
            R.id.imgRombo3,
            R.id.tvLemaBienvenida,
            R.id.llIndicadores,
            R.id.indicador1,
            R.id.btnComenzar,
            R.id.tvPieBienvenida
    };

    private final View raiz;
    private final float dp;

    private final List<Animator> bucles = new ArrayList<>();

    private Animator pulsoBoton;

    public BienvenidaAnimador(View raiz) {
        this.raiz = raiz;

        this.dp = raiz.getResources()
                .getDisplayMetrics().density;

        configurarBoton();
    }

    private View v(int id) {
        return raiz.findViewById(id);
    }

    // =========================================
    // INICIO DE LAS ANIMACIONES
    // =========================================

    public void reproducir() {

        detener();

        // 1. Cenefas decorativas

        entrar(
                v(R.id.margenSuperior),
                -18, 0, 650
        );

        entrar(
                v(R.id.margenInferior),
                18, 0, 650
        );

        // 2. Halo del logotipo

        View halo = v(R.id.viewHalo);

        aparecerConRebote(
                halo, 150, 800, 0.5f
        );

        if (halo != null) {
            bucles.add(pulso(
                    halo, 1.025f, 2800, 1000
            ));
        }

        // 3. Logotipo principal

        View logo = v(R.id.imgLogoBienvenida);

        aparecerConRebote(
                logo, 250, 800, 0.4f
        );

        if (logo != null) {
            bucles.add(flotar(
                    logo, -4, 2600, 1200
            ));
        }

        // 4. Título

        entrar(
                v(R.id.tvTituloBienvenida),
                28, 550, 600
        );

        // 5. Línea decorativa

        crecerHorizontal(
                v(R.id.lineaAcento),
                850, 500, 0.5f
        );

        // 6. Descripción

        entrar(
                v(R.id.tvDescripcionBienvenida),
                24, 750, 600
        );

        // 7. Tarjeta cultural

        entrar(
                v(R.id.cardCultura),
                24, 900, 650
        );

        // 8. Rombos decorativos

        int[] rombos = {
                R.id.imgRombo1,
                R.id.imgRombo2,
                R.id.imgRombo3
        };

        for (int i = 0; i < rombos.length; i++) {

            View rombo = v(rombos[i]);

            aparecerConRebote(
                    rombo,
                    1050 + i * 130L,
                    650,
                    0f
            );

            if (rombo != null) {
                bucles.add(flotar(
                        rombo,
                        -2,
                        2200,
                        1900 + i * 250L
                ));
            }
        }

        // 9. Lema cultural

        entrar(
                v(R.id.tvLemaBienvenida),
                16, 1250, 550
        );

        // 10. Indicadores

        entrar(
                v(R.id.llIndicadores),
                12, 1400, 450
        );

        crecerHorizontal(
                v(R.id.indicador1),
                1700, 450, 0f
        );

        // 11. Botón comenzar

        entrar(
                v(R.id.btnComenzar),
                40, 1450, 650
        );

        iniciarPulsoBoton(2300);

        // 12. Pie de pantalla

        entrar(
                v(R.id.tvPieBienvenida),
                12, 1750, 500
        );
    }

    // =========================================
    // DETENER Y REINICIAR ANIMACIONES
    // =========================================

    public void detener() {

        // Detener animaciones infinitas

        for (Animator a : bucles) {
            a.cancel();
        }

        bucles.clear();

        // Detener pulso del botón

        if (pulsoBoton != null) {
            pulsoBoton.cancel();
            pulsoBoton = null;
        }

        // Reiniciar todas las vistas

        for (int id : TODAS) {

            View vista = v(id);

            if (vista != null) {

                vista.animate().cancel();

                vista.setAlpha(1f);
                vista.setScaleX(1f);
                vista.setScaleY(1f);

                vista.setTranslationX(0f);
                vista.setTranslationY(0f);
            }
        }
    }

    // =========================================
    // ANIMACIÓN DE ENTRADA
    // =========================================

    private void entrar(
            View vista,
            float desplazamientoDp,
            long retraso,
            long duracion
    ) {

        if (vista == null) return;

        vista.animate().cancel();

        vista.setAlpha(0f);

        vista.setTranslationY(
                desplazamientoDp * dp
        );

        vista.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(retraso)
                .setDuration(duracion)
                .setInterpolator(
                        new DecelerateInterpolator(2f)
                )
                .start();
    }

    // =========================================
    // APARICIÓN CON REBOTE
    // =========================================

    private void aparecerConRebote(
            View vista,
            long retraso,
            long duracion,
            float escalaInicial
    ) {

        if (vista == null) return;

        vista.animate().cancel();

        vista.setAlpha(0f);
        vista.setScaleX(escalaInicial);
        vista.setScaleY(escalaInicial);

        vista.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(retraso)
                .setDuration(duracion)
                .setInterpolator(
                        new OvershootInterpolator(1.6f)
                )
                .start();
    }

    // =========================================
    // CRECIMIENTO HORIZONTAL
    // =========================================

    private void crecerHorizontal(
            View vista,
            long retraso,
            long duracion,
            float pivoteX
    ) {

        if (vista == null) return;

        vista.animate().cancel();

        vista.setPivotX(
                vista.getWidth() * pivoteX
        );

        vista.setAlpha(1f);
        vista.setScaleX(0f);

        vista.animate()
                .scaleX(1f)
                .setStartDelay(retraso)
                .setDuration(duracion)
                .setInterpolator(
                        new DecelerateInterpolator(2f)
                )
                .start();
    }

    // =========================================
    // ANIMACIÓN DE FLOTACIÓN
    // =========================================

    private Animator flotar(
            View vista,
            float altoDp,
            long duracion,
            long retraso
    ) {

        if (vista == null) return null;

        ObjectAnimator a = ObjectAnimator.ofFloat(
                vista,
                View.TRANSLATION_Y,
                0f,
                altoDp * dp
        );

        configurarBucle(
                a, duracion, retraso
        );

        a.start();

        return a;
    }

    // =========================================
    // ANIMACIÓN DE PULSO
    // =========================================

    private Animator pulso(
            View vista,
            float escala,
            long duracion,
            long retraso
    ) {

        if (vista == null) return null;

        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(
                vista,

                PropertyValuesHolder.ofFloat(
                        View.SCALE_X, 1f, escala
                ),

                PropertyValuesHolder.ofFloat(
                        View.SCALE_Y, 1f, escala
                )
        );

        configurarBucle(
                a, duracion, retraso
        );

        a.start();

        return a;
    }

    // =========================================
    // CONFIGURACIÓN DE BUCLES INFINITOS
    // =========================================

    private void configurarBucle(
            ObjectAnimator a,
            long duracion,
            long retraso
    ) {

        a.setDuration(duracion);
        a.setStartDelay(retraso);

        a.setRepeatCount(
                ObjectAnimator.INFINITE
        );

        a.setRepeatMode(
                ObjectAnimator.REVERSE
        );

        a.setInterpolator(
                new AccelerateDecelerateInterpolator()
        );
    }

    // =========================================
    // PULSO DEL BOTÓN
    // =========================================

    private void iniciarPulsoBoton(long retraso) {

        View boton = v(R.id.btnComenzar);

        if (boton == null) return;

        if (pulsoBoton != null) {
            pulsoBoton.cancel();
        }

        pulsoBoton = pulso(
                boton, 1.035f, 1200, retraso
        );
    }

    // =========================================
    // EFECTO TÁCTIL DEL BOTÓN
    // =========================================

    @SuppressLint("ClickableViewAccessibility")
    private void configurarBoton() {

        View boton = v(R.id.btnComenzar);

        if (boton == null) return;

        boton.setOnTouchListener((vista, evento) -> {

            switch (evento.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    // Pausar pulso al presionar

                    if (pulsoBoton != null) {
                        pulsoBoton.cancel();
                        pulsoBoton = null;
                    }

                    vista.animate().cancel();

                    vista.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setStartDelay(0)
                            .setDuration(90)
                            .start();

                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    // Recuperar tamaño original

                    vista.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setStartDelay(0)
                            .setDuration(140)
                            .start();

                    // Reanudar pulso

                    iniciarPulsoBoton(500);

                    break;
            }

            // Permite conservar el clic original

            return false;
        });
    }
}
