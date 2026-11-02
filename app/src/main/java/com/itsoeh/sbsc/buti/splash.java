package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Splash animado de B'ÜTI.
 * Al terminar, muestra la pantalla de bienvenida.
 */
public class splash extends Fragment {

    // Momento del impacto del logo.
    private static final long MOMENTO_IMPACTO = 380;

    // Duración mínima del splash.
    private static final long DURACION_SPLASH = 3600;

    // Pausa que se queda el texto completo antes de pasar.
    private static final long PAUSA_FINAL = 900;

    // Tiempo máximo de respaldo.
    private static final long TIEMPO_MAXIMO = 8000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Animator> activeAnimators = new ArrayList<>();
    private final List<View> confettiViews = new ArrayList<>();

    private boolean minimoCumplido = false;
    private boolean lemaTerminado = false;
    private boolean lugarTerminado = false;
    private boolean navegacionProgramada = false;

    private Runnable impacto;
    private Runnable irSiguiente;
    private Runnable typewriterRunnable;
    private Runnable minimoRunnable;
    private Runnable seguroMaximoRunnable;

    public splash() {
        // Constructor público vacío requerido.
    }

    private synchronized void intentarContinuar() {
        if (!isAdded() || getView() == null) return;
        if (minimoCumplido && lemaTerminado && lugarTerminado && !navegacionProgramada) {
            navegacionProgramada = true;
            handler.postDelayed(irSiguiente, PAUSA_FINAL);
        }
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
        float density = ctx.getResources().getDisplayMetrics().density;

        // Definición de Runnables de navegación
        irSiguiente = () -> {
            if (!isAdded() || getView() != view) {
                return;
            }

            Intent intent = new Intent(requireContext(), BienvenidaActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            requireActivity().finish();
        };

        minimoRunnable = () -> {
            minimoCumplido = true;
            intentarContinuar();
        };

        seguroMaximoRunnable = () -> {
            if (!navegacionProgramada && isAdded() && getView() != null) {
                navegacionProgramada = true;
                irSiguiente.run();
            }
        };

        handler.postDelayed(minimoRunnable, DURACION_SPLASH);
        handler.postDelayed(seguroMaximoRunnable, TIEMPO_MAXIMO);

        // g. Márgenes superior e inferior entran deslizándose al inicio
        View margenSup = view.findViewById(R.id.margenSup);
        View margenInf = view.findViewById(R.id.margenInf);

        if (margenSup != null) {
            margenSup.setTranslationY(-60f * density);
            ObjectAnimator animSup = ObjectAnimator.ofFloat(margenSup, "translationY", -60f * density, 0f);
            animSup.setDuration(500);
            animSup.setInterpolator(new DecelerateInterpolator());
            registerAnimator(animSup);
            animSup.start();
        }

        if (margenInf != null) {
            margenInf.setTranslationY(60f * density);
            ObjectAnimator animInf = ObjectAnimator.ofFloat(margenInf, "translationY", 60f * density, 0f);
            animInf.setDuration(500);
            animInf.setInterpolator(new DecelerateInterpolator());
            registerAnimator(animInf);
            animInf.start();
        }

        // 0 ms: el logo cae desde muy grande (animación slam)
        animar(
                view.findViewById(R.id.imgLogoSplash),
                ctx,
                R.anim.slam,
                0
        );

        // Momento del impacto: efecto "pum"
        impacto = () -> {
            if (!isAdded() || getView() != view) {
                return;
            }
            haceElPum(view, ctx);
        };
        handler.postDelayed(impacto, MOMENTO_IMPACTO);

        // e. txtLema aparece letra por letra (~30 ms por letra) con rebote al final
        TextView txtLema = view.findViewById(R.id.txtLema);
        if (txtLema != null) {
            iniciarEfectoMecanografia(txtLema, getString(R.string.splash_lema), 700);
        } else {
            lemaTerminado = true;
            intentarContinuar();
        }

        // 7. txtLugar con animación y listener al terminar
        View txtLugar = view.findViewById(R.id.txtLugar);
        if (txtLugar != null) {
            Animation animLugar = AnimationUtils.loadAnimation(ctx, R.anim.fade_up);
            animLugar.setStartOffset(950);
            animLugar.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    lugarTerminado = true;
                    intentarContinuar();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            txtLugar.startAnimation(animLugar);
        } else {
            lugarTerminado = true;
            intentarContinuar();
        }

        animar(view.findViewById(R.id.filete), ctx, R.anim.fade_up, 1100);

        // f. Rombos decorativos en cascada con rebote y posterior ondulación continua
        int[] rombos = {R.id.rombo1, R.id.rombo2, R.id.rombo3, R.id.rombo4};
        for (int i = 0; i < rombos.length; i++) {
            final View rombo = view.findViewById(rombos[i]);
            if (rombo != null) {
                rombo.setScaleX(0f);
                rombo.setScaleY(0f);
                long delay = 1150 + i * 120L;

                ObjectAnimator scaleX = ObjectAnimator.ofFloat(rombo, "scaleX", 0f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(rombo, "scaleY", 0f, 1f);
                AnimatorSet set = new AnimatorSet();
                set.playTogether(scaleX, scaleY);
                set.setStartDelay(delay);
                set.setDuration(400);
                set.setInterpolator(new OvershootInterpolator(1.6f));

                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isAdded()) return;
                        ObjectAnimator ola = ObjectAnimator.ofFloat(rombo, "translationY", -4f * density, 4f * density);
                        ola.setDuration(1200);
                        ola.setRepeatMode(ValueAnimator.REVERSE);
                        ola.setRepeatCount(ValueAnimator.INFINITE);
                        ola.setInterpolator(new LinearInterpolator());
                        registerAnimator(ola);
                        ola.start();
                    }
                });

                registerAnimator(set);
                set.start();
            }
        }

        // Personajes entrando desde los lados
        ImageView imgNino = view.findViewById(R.id.imgNino);
        ImageView imgNina = view.findViewById(R.id.imgNina);

        if (imgNino != null) {
            imgNino.setTranslationX(-250f * density);
            ObjectAnimator animNino = ObjectAnimator.ofFloat(imgNino, "translationX", -250f * density, 0f);
            animNino.setStartDelay(850);
            animNino.setDuration(600);
            animNino.setInterpolator(new OvershootInterpolator(1.2f));

            animNino.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isAdded()) return;
                    iniciarBalanceoPersonaje(imgNino, 0L);
                }
            });
            registerAnimator(animNino);
            animNino.start();
        }

        if (imgNina != null) {
            imgNina.setTranslationX(250f * density);
            ObjectAnimator animNina = ObjectAnimator.ofFloat(imgNina, "translationX", 250f * density, 0f);
            animNina.setStartDelay(950);
            animNina.setDuration(600);
            animNina.setInterpolator(new OvershootInterpolator(1.2f));

            animNina.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isAdded()) return;
                    iniciarBalanceoPersonaje(imgNina, 300L);
                }
            });
            registerAnimator(animNina);
            animNina.start();
        }

        // h. Barra de progreso y texto de carga palpitante
        ProgressBar progreso = view.findViewById(R.id.progresoSplash);
        if (progreso != null) {
            ObjectAnimator cargaAnim = ObjectAnimator.ofInt(progreso, "progress", 0, progreso.getMax());
            cargaAnim.setStartDelay(800);
            cargaAnim.setDuration(DURACION_SPLASH - 1000);
            cargaAnim.setInterpolator(new LinearInterpolator());
            registerAnimator(cargaAnim);
            cargaAnim.start();
        }

        TextView txtCargando = view.findViewById(R.id.txtCargando);
        if (txtCargando != null) {
            ObjectAnimator pulseCargando = ObjectAnimator.ofFloat(txtCargando, "alpha", 0.3f, 1f);
            pulseCargando.setDuration(600);
            pulseCargando.setRepeatMode(ValueAnimator.REVERSE);
            pulseCargando.setRepeatCount(ValueAnimator.INFINITE);
            registerAnimator(pulseCargando);
            pulseCargando.start();
        }
    }

    /**
     * e, 6. Efecto mecanografía para txtLema basado en tiempo transcurrido
     */
    private void iniciarEfectoMecanografia(TextView textView, String textoCompleto, long delayInicial) {
        if (TextUtils.isEmpty(textoCompleto)) {
            lemaTerminado = true;
            intentarContinuar();
            return;
        }

        textView.setText("");
        typewriterRunnable = new Runnable() {
            private long startTime = -1;

            @Override
            public void run() {
                if (!isAdded() || getView() == null) return;
                if (startTime < 0) {
                    startTime = SystemClock.elapsedRealtime();
                }
                long ahora = SystemClock.elapsedRealtime();
                int index = (int) ((ahora - startTime) / 30);

                if (index <= textoCompleto.length()) {
                    textView.setText(textoCompleto.substring(0, index));
                    handler.postDelayed(this, 30);
                } else {
                    textView.setText(textoCompleto);
                    // Pequeño rebote al finalizar la escritura
                    ObjectAnimator popX = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.1f, 1f);
                    ObjectAnimator popY = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.1f, 1f);
                    AnimatorSet setPop = new AnimatorSet();
                    setPop.playTogether(popX, popY);
                    setPop.setDuration(250);
                    setPop.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            lemaTerminado = true;
                            intentarContinuar();
                        }
                    });
                    registerAnimator(setPop);
                    setPop.start();
                }
            }
        };
        handler.postDelayed(typewriterRunnable, delayInicial);
    }

    /**
     * a. Balanceo suave y respiración continua de los personajes
     */
    private void iniciarBalanceoPersonaje(View v, long delay) {
        if (v == null || !isAdded()) return;

        v.post(() -> {
            if (!isAdded()) return;
            v.setPivotX(v.getWidth() / 2f);
            v.setPivotY(v.getHeight());

            ObjectAnimator rotacion = ObjectAnimator.ofFloat(v, "rotation", -3f, 3f);
            rotacion.setDuration(1600);
            rotacion.setRepeatMode(ValueAnimator.REVERSE);
            rotacion.setRepeatCount(ValueAnimator.INFINITE);
            rotacion.setInterpolator(new LinearInterpolator());

            ObjectAnimator respiracion = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.03f);
            respiracion.setDuration(1600);
            respiracion.setRepeatMode(ValueAnimator.REVERSE);
            respiracion.setRepeatCount(ValueAnimator.INFINITE);
            respiracion.setInterpolator(new LinearInterpolator());

            AnimatorSet set = new AnimatorSet();
            set.playTogether(rotacion, respiracion);
            set.setStartDelay(delay);
            registerAnimator(set);
            set.start();
        });
    }

    /**
     * Efectos que ocurren en el instante del impacto.
     */
    private void haceElPum(View view, Context ctx) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

        View flash = view.findViewById(R.id.flash);
        if (flash != null) {
            flash.setVisibility(View.VISIBLE);
            flash.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.flash));
        }

        View velo = view.findViewById(R.id.velo);
        if (velo != null) {
            animar(velo, ctx, R.anim.velo_salir, 0);
        }

        View raiz = view.findViewById(R.id.raiz);
        if (raiz != null) {
            raiz.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.sacudir));
        }

        int[] ondas = {R.id.onda1, R.id.onda2, R.id.onda3};
        for (int i = 0; i < ondas.length; i++) {
            View onda = view.findViewById(ondas[i]);
            if (onda != null) {
                onda.setVisibility(View.VISIBLE);
                animar(onda, ctx, R.anim.onda, i * 140L);
            }
        }

        View rayos = view.findViewById(R.id.imgRayos);
        if (rayos != null) {
            rayos.setVisibility(View.VISIBLE);
            animar(rayos, ctx, R.anim.rayos_giro, 0);
        }

        View halo = view.findViewById(R.id.imgHalo);
        if (halo != null) {
            halo.setVisibility(View.VISIBLE);
            animar(halo, ctx, R.anim.latido_halo, 0);
        }

        int[] estrellas = {R.id.estrella1, R.id.estrella2, R.id.estrella3, R.id.estrella4};
        long[] delaysEstrellas = {100L, 350L, 600L, 850L};
        for (int i = 0; i < estrellas.length; i++) {
            View estrella = view.findViewById(estrellas[i]);
            if (estrella != null) {
                ObjectAnimator alphaEstrella = ObjectAnimator.ofFloat(estrella, "alpha", 0.4f, 1f);
                ObjectAnimator scaleXEstrella = ObjectAnimator.ofFloat(estrella, "scaleX", 0.8f, 1.2f);
                ObjectAnimator scaleYEstrella = ObjectAnimator.ofFloat(estrella, "scaleY", 0.8f, 1.2f);

                AnimatorSet setEstrella = new AnimatorSet();
                setEstrella.playTogether(alphaEstrella, scaleXEstrella, scaleYEstrella);
                setEstrella.setDuration(700);
                setEstrella.setStartDelay(delaysEstrellas[i]);
                setEstrella.setInterpolator(new LinearInterpolator());

                alphaEstrella.setRepeatMode(ValueAnimator.REVERSE);
                alphaEstrella.setRepeatCount(ValueAnimator.INFINITE);
                scaleXEstrella.setRepeatMode(ValueAnimator.REVERSE);
                scaleXEstrella.setRepeatCount(ValueAnimator.INFINITE);
                scaleYEstrella.setRepeatMode(ValueAnimator.REVERSE);
                scaleYEstrella.setRepeatCount(ValueAnimator.INFINITE);

                registerAnimator(setEstrella);
                setEstrella.start();
            }
        }

        FrameLayout raizLayout = view.findViewById(R.id.raiz);
        if (raizLayout != null) {
            lanzarConfeti(raizLayout, ctx);
        }

        FrameLayout contenedor = view.findViewById(R.id.logoContenedor);
        if (contenedor != null) {
            Animator flotar = AnimatorInflater.loadAnimator(ctx, R.animator.flotar);
            flotar.setTarget(contenedor);
            flotar.setStartDelay(700);
            registerAnimator(flotar);
            flotar.start();
        }
    }

    private void lanzarConfeti(FrameLayout raiz, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        int[] colores = {
                R.color.buti_rosa,
                R.color.buti_dorado,
                R.color.buti_naranja,
                R.color.buti_verde,
                R.color.buti_azul
        };

        Random random = new Random();
        int cantidad = 28;

        for (int i = 0; i < cantidad; i++) {
            View cuadrito = new View(ctx);
            int tamDp = 8 + random.nextInt(7);
            int tamPx = (int) (tamDp * density);

            int colorRes = colores[random.nextInt(colores.length)];
            cuadrito.setBackgroundColor(ContextCompat.getColor(ctx, colorRes));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(tamPx, tamPx, Gravity.CENTER);
            raiz.addView(cuadrito, params);
            confettiViews.add(cuadrito);

            double angulo = random.nextDouble() * 2 * Math.PI;
            float vel = (120f + random.nextFloat() * 140f) * density;
            float startX = (float) Math.cos(angulo) * vel;
            float startY = (float) Math.sin(angulo) * vel - (40f * density);

            cuadrito.setTranslationX(startX);
            cuadrito.setTranslationY(startY);
            cuadrito.setRotation(random.nextFloat() * 360f);

            float destY = startY + (180f + random.nextFloat() * 120f) * density;
            float destX = startX + (-30f + random.nextFloat() * 60f) * density;

            ValueAnimator animConfeti = ValueAnimator.ofFloat(0f, 1f);
            animConfeti.setDuration(1000 + random.nextInt(300));
            animConfeti.setInterpolator(new LinearInterpolator());

            float initialRot = cuadrito.getRotation();
            float rotSpeed = -360f + random.nextFloat() * 720f;

            animConfeti.addUpdateListener(animation -> {
                float val = (float) animation.getAnimatedValue();
                cuadrito.setTranslationX(startX + (destX - startX) * val);
                cuadrito.setTranslationY(startY + (destY - startY) * val * val);
                cuadrito.setRotation(initialRot + rotSpeed * val);
                cuadrito.setAlpha(1f - val);
            });

            animConfeti.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (cuadrito.getParent() == raiz) {
                        raiz.removeView(cuadrito);
                    }
                    confettiViews.remove(cuadrito);
                }
            });

            registerAnimator(animConfeti);
            animConfeti.start();
        }
    }

    private void animar(View v, Context ctx, int animRes, long retrasoMs) {
        if (v == null) return;
        Animation animacion = AnimationUtils.loadAnimation(ctx, animRes);
        animacion.setStartOffset(retrasoMs);
        v.startAnimation(animacion);
    }

    private void registerAnimator(Animator animator) {
        if (animator != null) {
            activeAnimators.add(animator);
        }
    }

    @Override
    public void onDestroyView() {
        if (impacto != null) handler.removeCallbacks(impacto);
        if (irSiguiente != null) handler.removeCallbacks(irSiguiente);
        if (typewriterRunnable != null) handler.removeCallbacks(typewriterRunnable);
        if (minimoRunnable != null) handler.removeCallbacks(minimoRunnable);
        if (seguroMaximoRunnable != null) handler.removeCallbacks(seguroMaximoRunnable);

        for (Animator anim : activeAnimators) {
            if (anim != null) anim.cancel();
        }
        activeAnimators.clear();

        View root = getView();
        if (root instanceof FrameLayout) {
            FrameLayout raiz = (FrameLayout) root;
            for (View confetti : confettiViews) {
                if (confetti != null && confetti.getParent() == raiz) {
                    raiz.removeView(confetti);
                }
            }
        }
        confettiViews.clear();

        super.onDestroyView();
    }
}