package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Splash animado de B'ÜTI.
 * Al terminar, muestra la pantalla de bienvenida.
 */
public class splash extends Fragment {

    // Momento del impacto del logo.
    private static final long MOMENTO_IMPACTO = 380;

    // Duración total del splash.
    private static final long DURACION_SPLASH = 3600;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable impacto;
    private Runnable irSiguiente;

    private Animator flotar;
    private ObjectAnimator cargaAnim;

    public splash() {
        // Constructor público vacío requerido.
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = requireContext();

        // 0 ms: el logo cae desde muy grande.
        animar(
                view.findViewById(R.id.imgLogoSplash),
                ctx,
                R.anim.slam,
                0
        );

        // Momento del impacto: efecto "pum".
        impacto = () -> {
            if (!isAdded() || getView() != view) {
                return;
            }

            haceElPum(view, ctx);
        };

        handler.postDelayed(impacto, MOMENTO_IMPACTO);

        // Textos decorativos.
        animar(view.findViewById(R.id.txtLema),
                ctx, R.anim.fade_up, 800);

        animar(view.findViewById(R.id.txtLugar),
                ctx, R.anim.fade_up, 950);

        animar(view.findViewById(R.id.filete),
                ctx, R.anim.fade_up, 1100);

        // Rombos decorativos.
        int[] rombos = {
                R.id.rombo1,
                R.id.rombo2,
                R.id.rombo3,
                R.id.rombo4
        };

        for (int i = 0; i < rombos.length; i++) {
            animar(
                    view.findViewById(rombos[i]),
                    ctx,
                    R.anim.pop_in,
                    1150 + i * 140L
            );
        }

        // Personajes.
        animar(view.findViewById(R.id.imgNino),
                ctx, R.anim.subir_rebote, 900);

        animar(view.findViewById(R.id.imgNina),
                ctx, R.anim.subir_rebote, 1050);

        // Barra de progreso.
        ProgressBar progreso =
                view.findViewById(R.id.progresoSplash);

        if (progreso != null) {
            cargaAnim = ObjectAnimator.ofInt(
                    progreso,
                    "progress",
                    0,
                    progreso.getMax()
            );

            cargaAnim.setStartDelay(900);
            cargaAnim.setDuration(DURACION_SPLASH - 1100);
            cargaAnim.setInterpolator(new LinearInterpolator());
            cargaAnim.start();
        }

        // Al terminar el splash, pasar a la pantalla de bienvenida.
        irSiguiente = () -> {
            if (!isAdded() || getView() != view) {
                return;
            }

            Intent intent = new Intent(
                    requireContext(),
                    BienvenidaActivity.class
            );

            startActivity(intent);

            requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );

            requireActivity().finish();
        };

        handler.postDelayed(irSiguiente, DURACION_SPLASH);
    }

    /**
     * Efectos que ocurren en el instante del impacto.
     */
    private void haceElPum(View view, Context ctx) {

        // Vibración corta, si el dispositivo lo permite.
        view.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS
        );

        // Destello de pantalla completa.
        View flash = view.findViewById(R.id.flash);

        if (flash != null) {
            flash.setVisibility(View.VISIBLE);
            flash.startAnimation(
                    AnimationUtils.loadAnimation(ctx, R.anim.flash)
            );
        }

        // El velo se desvanece.
        View velo = view.findViewById(R.id.velo);

        if (velo != null) {
            animar(velo, ctx, R.anim.velo_salir, 0);
        }

        // Temblor de toda la pantalla.
        View raiz = view.findViewById(R.id.raiz);

        if (raiz != null) {
            raiz.startAnimation(
                    AnimationUtils.loadAnimation(ctx, R.anim.sacudir)
            );
        }

        // Ondas expansivas.
        int[] ondas = {
                R.id.onda1,
                R.id.onda2,
                R.id.onda3
        };

        for (int i = 0; i < ondas.length; i++) {
            View onda = view.findViewById(ondas[i]);

            if (onda != null) {
                onda.setVisibility(View.VISIBLE);

                animar(
                        onda,
                        ctx,
                        R.anim.onda,
                        i * 140L
                );
            }
        }

        // Rayos de sol.
        View rayos = view.findViewById(R.id.imgRayos);

        if (rayos != null) {
            rayos.setVisibility(View.VISIBLE);
            animar(rayos, ctx, R.anim.rayos_giro, 0);
        }

        // Halo.
        View halo = view.findViewById(R.id.imgHalo);

        if (halo != null) {
            halo.setVisibility(View.VISIBLE);
            animar(halo, ctx, R.anim.latido_halo, 0);
        }

        // Estrellas decorativas.
        animar(view.findViewById(R.id.estrella1),
                ctx, R.anim.pulso, 300);

        animar(view.findViewById(R.id.estrella2),
                ctx, R.anim.pulso, 700);

        animar(view.findViewById(R.id.estrella3),
                ctx, R.anim.girar_lento, 0);

        animar(view.findViewById(R.id.estrella4),
                ctx, R.anim.pulso, 1100);

        // Estrellas que salen disparadas desde el logo.
        FrameLayout contenedor =
                view.findViewById(R.id.logoContenedor);

        if (contenedor != null) {
            lanzarEstrellas(contenedor, ctx);

            // El logo comienza a flotar suavemente.
            flotar = AnimatorInflater.loadAnimator(
                    ctx,
                    R.animator.flotar
            );

            flotar.setTarget(contenedor);
            flotar.setStartDelay(700);
            flotar.start();
        }
    }

    /**
     * Lanza estrellas de colores en distintas direcciones.
     */
    private void lanzarEstrellas(
            FrameLayout contenedor,
            Context ctx
    ) {
        float d = getResources()
                .getDisplayMetrics().density;

        int[] colores = {
                R.color.buti_dorado,
                R.color.buti_rosa,
                R.color.buti_naranja,
                R.color.buti_verde,
                R.color.buti_dorado,
                R.color.buti_siena
        };

        int cantidad = 16;

        for (int i = 0; i < cantidad; i++) {
            ImageView estrella = new ImageView(ctx);

            estrella.setImageResource(R.drawable.deco_estrella);

            estrella.setImageTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    ctx,
                                    colores[i % colores.length]
                            )
                    )
            );

            boolean grande = i % 2 == 0;

            int tam = (int) (
                    (grande ? 24 : 14) * d
            );

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            tam,
                            tam,
                            Gravity.CENTER
                    );

            contenedor.addView(estrella, params);

            double angulo =
                    (2 * Math.PI * i / cantidad)
                            + (grande ? 0 : 0.2);

            float distancia =
                    (grande ? 230 : 160) * d;

            estrella.setScaleX(0.3f);
            estrella.setScaleY(0.3f);

            estrella.animate()
                    .translationX(
                            (float) Math.cos(angulo) * distancia
                    )
                    .translationY(
                            (float) Math.sin(angulo) * distancia
                    )
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .rotation(grande ? 180f : -180f)
                    .alpha(0f)
                    .setDuration(950)
                    .setInterpolator(
                            new DecelerateInterpolator()
                    )
                    .withEndAction(() -> {
                        if (estrella.getParent() == contenedor) {
                            contenedor.removeView(estrella);
                        }
                    })
                    .start();
        }
    }

    /**
     * Carga una animación XML y la inicia con un retraso.
     */
    private void animar(
            View v,
            Context ctx,
            int animRes,
            long retrasoMs
    ) {
        if (v == null) {
            return;
        }

        Animation animacion =
                AnimationUtils.loadAnimation(ctx, animRes);

        animacion.setStartOffset(retrasoMs);
        v.startAnimation(animacion);
    }

    @Override
    public void onDestroyView() {

        // Evita que se ejecuten tareas pendientes.
        if (impacto != null) {
            handler.removeCallbacks(impacto);
        }

        if (irSiguiente != null) {
            handler.removeCallbacks(irSiguiente);
        }

        // Cancela animaciones activas.
        if (flotar != null) {
            flotar.cancel();
            flotar = null;
        }

        if (cargaAnim != null) {
            cargaAnim.cancel();
            cargaAnim = null;
        }

        super.onDestroyView();
    }
}
