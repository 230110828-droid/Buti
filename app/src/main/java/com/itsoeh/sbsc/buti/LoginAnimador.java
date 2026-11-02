package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class LoginAnimador {

    private AnimatorSet animaciones;

    public void reproducir(View... vistas) {

        detener();

        List<Animator> lista = new ArrayList<>();

        long retraso = 0;

        for (View vista : vistas) {

            if (vista == null) {
                continue;
            }

            vista.setAlpha(0f);
            vista.setTranslationY(25f);

            ObjectAnimator aparecer =
                    ObjectAnimator.ofFloat(
                            vista,
                            View.ALPHA,
                            0f,
                            1f
                    );

            ObjectAnimator deslizar =
                    ObjectAnimator.ofFloat(
                            vista,
                            View.TRANSLATION_Y,
                            25f,
                            0f
                    );

            AnimatorSet entrada = new AnimatorSet();

            entrada.playTogether(aparecer, deslizar);
            entrada.setDuration(450);
            entrada.setStartDelay(retraso);
            entrada.setInterpolator(
                    new AccelerateDecelerateInterpolator()
            );

            lista.add(entrada);

            retraso += 90;
        }

        animaciones = new AnimatorSet();
        animaciones.playTogether(lista);
        animaciones.start();
    }

    public void detener() {

        if (animaciones != null) {
            animaciones.cancel();
            animaciones = null;
        }
    }

    // Efecto al presionar botones
    public static void animarPresion(View vista) {

        vista.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(100)
                .withEndAction(() -> {

                    vista.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start();

                })
                .start();
    }
}
