package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla completa de racha (estilo Duolingo).
 * Sale la primera vez del día que terminas una lección, práctica o juego:
 * el maguey crece, el número sube con animación, se marca el día en la
 * semana y cae confeti. Se cierra con "¡Seguir!", tocando la pantalla
 * o solita después de unos segundos.
 *
 *   RachaButi.mostrar(contexto, 5, () -> { ...lo que sigue... });
 *
 * Además tiene 3 versiones para el maguey congelado (1 día de gracia):
 *   · SALVADA   → practicó con la racha congelada: el hielo se derrite.
 *   · CONGELADA → aviso al entrar: faltó ayer, la racha está en pausa.
 *   · PERDIDA   → aviso al entrar: faltó 2 días, el maguey se secó.
 */
public final class RachaButi {

    private static final long CIERRE_AUTOMATICO = 7000;

    private enum Modo { NORMAL, SALVADA, CONGELADA, PERDIDA }

    private static final int AZUL_HIELO = Color.parseColor("#7FB3D5");

    private final Dialog dialog;
    private final Context context;
    private final int dias;
    private final Modo modo;
    private final int mejor;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Animator> animaciones = new ArrayList<>();
    private boolean puedeCerrar = false;

    private RachaButi(Context context, int dias, Modo modo, int mejor) {
        this.context = context;
        this.modo = modo;
        this.mejor = mejor;
        this.dias = modo == Modo.PERDIDA ? Math.max(0, dias) : Math.max(1, dias);
        // Con el tema de la app (necesario para los botones y tarjetas Material).
        this.dialog = new Dialog(context);
    }

    public static void mostrar(Context context, int dias, @Nullable Runnable alTerminar) {

        if (context instanceof Activity
                && (((Activity) context).isFinishing() || ((Activity) context).isDestroyed())) {
            if (alTerminar != null) alTerminar.run();
            return;
        }

        // Si la actividad de hoy salvó una racha congelada, se festeja distinto.
        Modo modo = ProgresoButi.tomarDescongelada() ? Modo.SALVADA : Modo.NORMAL;

        new RachaButi(context, dias, modo, 0).abrir(alTerminar);
    }

    /** Aviso al entrar: faltó ayer y el maguey se congeló (la racha sigue en pausa). */
    public static void mostrarCongelada(Context context, int dias, @Nullable Runnable alTerminar) {
        if (!sePuedeMostrar(context, alTerminar)) return;
        new RachaButi(context, dias, Modo.CONGELADA, 0).abrir(alTerminar);
    }

    /** Aviso al entrar: faltó 2 días o más y la racha se perdió. */
    public static void mostrarPerdida(Context context, int diasPerdidos, int mejor,
                                      @Nullable Runnable alTerminar) {
        if (!sePuedeMostrar(context, alTerminar)) return;
        new RachaButi(context, diasPerdidos, Modo.PERDIDA, mejor).abrir(alTerminar);
    }

    private static boolean sePuedeMostrar(Context context, @Nullable Runnable alTerminar) {
        if (context instanceof Activity
                && (((Activity) context).isFinishing() || ((Activity) context).isDestroyed())) {
            if (alTerminar != null) alTerminar.run();
            return false;
        }
        return true;
    }

    private void abrir(@Nullable Runnable alTerminar) {

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_racha_buti);
        dialog.setCancelable(true);

        Window ventana = dialog.getWindow();
        if (ventana != null) {
            ventana.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ventana.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ventana.setWindowAnimations(android.R.style.Animation_Dialog);
        }

        dialog.setOnDismissListener(d -> {
            handler.removeCallbacksAndMessages(null);
            for (Animator a : new ArrayList<>(animaciones)) a.cancel();
            animaciones.clear();
            ConfetiView confeti = dialog.findViewById(R.id.confetiRacha);
            if (confeti != null) confeti.detener();
            if (alTerminar != null) alTerminar.run();
        });

        llenar();

        View.OnClickListener cerrar = v -> {
            if (puedeCerrar) dialog.dismiss();
        };

        dialog.findViewById(R.id.raizRacha).setOnClickListener(cerrar);
        dialog.findViewById(R.id.btnContinuarRacha).setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Ocupa toda la pantalla (se aplica después de show()).
        Window ventanaAbierta = dialog.getWindow();
        if (ventanaAbierta != null) {
            ventanaAbierta.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        dialog.findViewById(R.id.raizRacha).post(this::animar);

        handler.postDelayed(() -> {
            if (dialog.isShowing()) dialog.dismiss();
        }, CIERRE_AUTOMATICO);

        LectorVoz.hablar(context, titulo() + ". " + mensaje());
    }

    // =========================================
    // CONTENIDO
    // =========================================

    private void llenar() {

        TextView anterior = dialog.findViewById(R.id.txtNumeroAnteriorRacha);
        TextView numero = dialog.findViewById(R.id.txtNumeroRacha);
        TextView txtDias = dialog.findViewById(R.id.txtDiasRacha);
        TextView etiqueta = dialog.findViewById(R.id.txtEtiquetaRacha);
        TextView boton = dialog.findViewById(R.id.btnContinuarRacha);
        ImageView maguey = dialog.findViewById(R.id.imgMagueyRacha);

        anterior.setText(String.valueOf(Math.max(0, dias - 1)));
        numero.setText(String.valueOf(modo == Modo.PERDIDA ? 0 : dias));
        txtDias.setText(dias == 1 ? "día de racha" : "días de racha");
        ((TextView) dialog.findViewById(R.id.txtMensajeRacha)).setText(mensaje());

        switch (modo) {

            case SALVADA:
                etiqueta.setText("◆  ¡RACHA SALVADA!  ◆");
                // Empieza congelado; en la animación se derrite el hielo.
                maguey.setImageResource(R.drawable.ic_maguey_congelado);
                crearSemana(dias - 1, 2, 1);
                break;

            case CONGELADA:
                etiqueta.setText("◆  RACHA CONGELADA  ◆");
                etiqueta.setTextColor(AZUL_HIELO);
                maguey.setImageResource(R.drawable.ic_maguey_congelado);
                anterior.setVisibility(View.GONE);
                numero.setVisibility(View.VISIBLE);
                numero.setTextColor(AZUL_HIELO);
                txtDias.setText(dias == 1 ? "día en pausa" : "días en pausa");
                boton.setText("¡A salvarla!");
                crearSemana(dias, 2, 1);
                break;

            case PERDIDA:
                etiqueta.setText("◆  RACHA PERDIDA  ◆");
                maguey.setImageResource(R.drawable.ic_maguey_seco);
                anterior.setVisibility(View.GONE);
                numero.setVisibility(View.VISIBLE);
                numero.setTextColor(ContextCompat.getColor(context, R.color.buti_texto_secundario));
                txtDias.setText("días de racha");
                boton.setText("Empezar de nuevo");
                crearSemana(0, 1, -1);
                break;

            default:
                // Primero se ve el maguey de ayer (si va a crecer, luego cambia).
                maguey.setImageResource(RachaMaguey.dibujoPorRacha(Math.max(1, dias - 1)));
                crearSemana(dias - 1, 1, -1);
                break;
        }
    }

    private String titulo() {
        switch (modo) {
            case CONGELADA: return "Tu racha está congelada";
            case PERDIDA: return "Tu racha se terminó";
            case SALVADA: return "¡Racha salvada! " + dias + (dias == 1 ? " día" : " días");
            default: return "¡Racha de " + dias + (dias == 1 ? " día" : " días") + "!";
        }
    }

    private String mensaje() {

        if (modo == Modo.CONGELADA) {
            return "Ayer no practicaste y tu maguey se congeló.\n"
                    + "Haz una lección, práctica o juego hoy para salvar tu racha de "
                    + dias + (dias == 1 ? " día." : " días.");
        }

        if (modo == Modo.PERDIDA) {
            return "Tu racha de " + dias + (dias == 1 ? " día" : " días") + " se terminó y tu maguey se secó.\n"
                    + "Tu mejor racha: " + Math.max(mejor, dias) + (Math.max(mejor, dias) == 1 ? " día" : " días")
                    + ". ¡Hoy puedes empezar una nueva!";
        }

        if (modo == Modo.SALVADA) {
            return "¡Salvaste tu racha! Tu maguey se descongeló.\n"
                    + "Llevas " + dias + " días aprendiendo hñähñu.";
        }

        if (dias == 1) return "¡Empezaste tu racha! Tu maguey acaba de brotar.\nVuelve mañana para que crezca.";
        if (creceHoy()) return "¡Tu maguey creció!\nLlevas " + dias + " días seguidos aprendiendo hñähñu.";
        if (dias == 2) return "¡Dos días seguidos!\nTu maguey se pone más fuerte.";
        if (dias >= 30) return "¡Increíble! Tu maguey ya floreció.\nEres un ejemplo de constancia.";

        return "¡Sigue así!\nPractica mañana para no perder tu racha.";
    }

    /** ¿Hoy el maguey cambia de dibujo (3, 7, 14 o 30 días)? */
    private boolean creceHoy() {
        return dias > 1 && RachaMaguey.dibujoPorRacha(dias) != RachaMaguey.dibujoPorRacha(dias - 1);
    }

    /**
     * Los 7 días de la semana; los que entran en la racha llevan palomita.
     *  cuenta      = cuántos días con palomita
     *  fin         = cuántos días atrás termina esa racha (1 = ayer, 2 = antier)
     *  congeladoEn = día que se marca con hielo (1 = ayer; -1 = ninguno)
     * Hoy se marca aparte (con animación) solo en NORMAL y SALVADA.
     */
    private void crearSemana(int cuenta, int fin, int congeladoEn) {

        LinearLayout semana = dialog.findViewById(R.id.contenedorSemanaRacha);
        semana.removeAllViews();

        SimpleDateFormat formato = new SimpleDateFormat("EEEEE", new Locale("es", "MX"));

        Calendar dia = Calendar.getInstance();
        dia.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {

            int atras = 6 - i;
            boolean esHoy = atras == 0;
            boolean enRacha = !esHoy && atras >= fin && atras < fin + cuenta;
            boolean congelado = atras == congeladoEn;

            LinearLayout columna = new LinearLayout(context);
            columna.setOrientation(LinearLayout.VERTICAL);
            columna.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView letra = new TextView(context);
            letra.setText(formato.format(dia.getTime()).toUpperCase(Locale.ROOT));
            letra.setTextSize(13);
            letra.setTypeface(letra.getTypeface(), Typeface.BOLD);
            letra.setTextColor(ContextCompat.getColor(context,
                    esHoy ? R.color.buti_titulo : R.color.buti_texto_secundario));
            letra.setGravity(Gravity.CENTER);
            columna.addView(letra);

            FrameLayout circulo = new FrameLayout(context);
            GradientDrawable fondo = new GradientDrawable();
            fondo.setShape(GradientDrawable.OVAL);
            fondo.setColor(congelado
                    ? Color.argb(70, Color.red(AZUL_HIELO), Color.green(AZUL_HIELO), Color.blue(AZUL_HIELO))
                    : ContextCompat.getColor(context,
                    enRacha ? R.color.nivel_naranja : R.color.buti_superficie_alt));
            fondo.setStroke(dp(2), congelado ? AZUL_HIELO : ContextCompat.getColor(context,
                    esHoy ? R.color.nivel_naranja : R.color.buti_borde));
            circulo.setBackground(fondo);
            circulo.setTag(esHoy ? "hoy" : null);

            ImageView palomita = new ImageView(context);
            palomita.setImageResource(R.drawable.ic_req_ok);
            palomita.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            palomita.setPadding(dp(6), dp(6), dp(6), dp(6));
            palomita.setVisibility(enRacha ? View.VISIBLE : View.INVISIBLE);
            palomita.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            circulo.addView(palomita, new FrameLayout.LayoutParams(-1, -1));

            if (congelado) {
                // Copo de nieve en el día que faltó.
                TextView copo = new TextView(context);
                copo.setText("❄");
                copo.setTextSize(15);
                copo.setTextColor(AZUL_HIELO);
                copo.setGravity(Gravity.CENTER);
                copo.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                circulo.addView(copo, new FrameLayout.LayoutParams(-1, -1));
            }

            LinearLayout.LayoutParams lpCirculo = new LinearLayout.LayoutParams(dp(34), dp(34));
            lpCirculo.topMargin = dp(6);
            columna.addView(circulo, lpCirculo);

            columna.setContentDescription(formato.format(dia.getTime())
                    + (enRacha ? ", practicaste" : congelado ? ", día congelado" : ", sin práctica"));

            semana.addView(columna, new LinearLayout.LayoutParams(0, -2, 1f));

            dia.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    // =========================================
    // ANIMACIONES
    // =========================================

    private void animar() {

        if (!dialog.isShowing()) return;

        if (modo == Modo.CONGELADA || modo == Modo.PERDIDA) {
            animarAviso();
            return;
        }

        View contenido = dialog.findViewById(R.id.contenidoRacha);
        View halo = dialog.findViewById(R.id.haloRacha);
        ImageView maguey = dialog.findViewById(R.id.imgMagueyRacha);
        TextView anterior = dialog.findViewById(R.id.txtNumeroAnteriorRacha);
        TextView nuevo = dialog.findViewById(R.id.txtNumeroRacha);
        View btn = dialog.findViewById(R.id.btnContinuarRacha);
        ConfetiView confeti = dialog.findViewById(R.id.confetiRacha);

        // 1) Todo entra suavecito desde abajo.
        contenido.setTranslationY(dp(40));
        contenido.setAlpha(0f);
        contenido.animate().translationY(0).alpha(1f).setDuration(400)
                .setInterpolator(new DecelerateInterpolator()).start();

        // 2) El maguey brota desde la tierra (se estira desde abajo).
        maguey.setPivotX(dp(100));
        maguey.setPivotY(dp(200));
        maguey.setScaleX(0.3f);
        maguey.setScaleY(0f);
        maguey.animate().scaleX(1f).scaleY(1f).setStartDelay(250).setDuration(700)
                .setInterpolator(new OvershootInterpolator(2.2f)).start();

        halo.animate().alpha(1f).setStartDelay(400).setDuration(500).start();

        ObjectAnimator latido = ObjectAnimator.ofFloat(halo, View.SCALE_X, 0.88f, 1.1f);
        latido.setDuration(1100);
        latido.setRepeatMode(ValueAnimator.REVERSE);
        latido.setRepeatCount(ValueAnimator.INFINITE);
        latido.addUpdateListener(a -> halo.setScaleY((float) a.getAnimatedValue()));
        latido.setStartDelay(700);
        guardar(latido);

        // 2b) Racha salvada: el hielo se derrite y el maguey reverdece.
        if (modo == Modo.SALVADA) handler.postDelayed(() -> derretir(maguey), 800);

        // 3) El número viejo sale hacia arriba y el nuevo cae rebotando.
        handler.postDelayed(() -> cambiarNumero(anterior, nuevo, confeti, maguey),
                modo == Modo.SALVADA ? 1600 : 1100);

        // 4) Si el maguey sube de etapa, "evoluciona" con un destello.
        if (creceHoy()) handler.postDelayed(() -> evolucionar(maguey, confeti),
                modo == Modo.SALVADA ? 2300 : 1700);

        // 5) Se mece con el viento.
        handler.postDelayed(() -> {
            if (!dialog.isShowing()) return;
            ObjectAnimator mecer = ObjectAnimator.ofFloat(maguey, View.ROTATION, -3f, 3f);
            mecer.setDuration(1600);
            mecer.setRepeatMode(ValueAnimator.REVERSE);
            mecer.setRepeatCount(ValueAnimator.INFINITE);
            mecer.setInterpolator(new AccelerateDecelerateInterpolator());
            guardar(mecer);
        }, 2400);

        // 6) Aparece el botón y ya se puede cerrar tocando la pantalla.
        handler.postDelayed(() -> {
            puedeCerrar = true;
            btn.setTranslationY(dp(20));
            btn.animate().alpha(1f).translationY(0).setDuration(350)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }, 2300);
    }

    /** Aviso de racha congelada o perdida: sin confeti, el maguey tirita o se marchita. */
    private void animarAviso() {

        View contenido = dialog.findViewById(R.id.contenidoRacha);
        View halo = dialog.findViewById(R.id.haloRacha);
        ImageView maguey = dialog.findViewById(R.id.imgMagueyRacha);
        TextView numero = dialog.findViewById(R.id.txtNumeroRacha);
        View btn = dialog.findViewById(R.id.btnContinuarRacha);

        contenido.setTranslationY(dp(40));
        contenido.setAlpha(0f);
        contenido.animate().translationY(0).alpha(1f).setDuration(400)
                .setInterpolator(new DecelerateInterpolator()).start();

        maguey.setAlpha(0f);
        maguey.setScaleX(0.85f);
        maguey.setScaleY(0.85f);
        maguey.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(250).setDuration(600)
                .setInterpolator(new DecelerateInterpolator()).start();

        numero.setAlpha(0f);
        numero.animate().alpha(1f).setStartDelay(600).setDuration(500).start();

        if (modo == Modo.CONGELADA) {

            // Halo frío y el maguey "tirita" de frío.
            halo.setBackgroundTintList(ColorStateList.valueOf(AZUL_HIELO));
            halo.animate().alpha(0.6f).setStartDelay(400).setDuration(500).start();

            handler.postDelayed(() -> {
                if (!dialog.isShowing()) return;
                ObjectAnimator tiritar = ObjectAnimator.ofFloat(maguey, View.TRANSLATION_X,
                        0, -dp(3), dp(3), -dp(2), dp(2), 0);
                tiritar.setDuration(500);
                tiritar.setRepeatCount(ValueAnimator.INFINITE);
                tiritar.setStartDelay(900);
                guardar(tiritar);
            }, 900);

        } else {

            // El maguey seco se inclina despacio.
            maguey.setPivotX(dp(100));
            maguey.setPivotY(dp(200));
            maguey.animate().rotation(-6f).setStartDelay(900).setDuration(1400)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }

        handler.postDelayed(() -> {
            puedeCerrar = true;
            btn.setTranslationY(dp(20));
            btn.animate().alpha(1f).translationY(0).setDuration(350)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }, 1500);
    }

    /** Racha salvada: destello y el maguey congelado vuelve a verse vivo. */
    private void derretir(ImageView maguey) {

        if (!dialog.isShowing()) return;

        View destello = dialog.findViewById(R.id.destelloRacha);
        destello.setBackgroundTintList(ColorStateList.valueOf(AZUL_HIELO));
        destello.setScaleX(0.5f);
        destello.setScaleY(0.5f);
        destello.setAlpha(1f);
        destello.animate().scaleX(1.6f).scaleY(1.6f).alpha(0f).setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> destello.setBackgroundTintList(null))
                .start();

        maguey.animate().alpha(0.2f).setDuration(250)
                .withEndAction(() -> {
                    if (!dialog.isShowing()) return;
                    maguey.setImageResource(RachaMaguey.dibujoPorRacha(Math.max(1, dias - 1)));
                    maguey.animate().alpha(1f).setDuration(450).start();
                })
                .start();
    }

    private void cambiarNumero(TextView anterior, TextView nuevo, ConfetiView confeti, View maguey) {

        if (!dialog.isShowing()) return;

        float alto = dp(110);

        anterior.animate().translationY(-alto).alpha(0f).setDuration(300)
                .setInterpolator(new AccelerateInterpolator()).start();

        nuevo.setVisibility(View.VISIBLE);
        nuevo.setTranslationY(alto);
        nuevo.setScaleX(0.6f);
        nuevo.setScaleY(0.6f);
        nuevo.animate().translationY(0).scaleX(1f).scaleY(1f).setStartDelay(150).setDuration(550)
                .setInterpolator(new OvershootInterpolator(2.5f))
                .withEndAction(() -> {

                    if (!dialog.isShowing()) return;

                    nuevo.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

                    // Pulso del número.
                    ObjectAnimator pulso = ObjectAnimator.ofFloat(nuevo, View.SCALE_X, 1f, 1.08f);
                    pulso.setDuration(700);
                    pulso.setRepeatMode(ValueAnimator.REVERSE);
                    pulso.setRepeatCount(ValueAnimator.INFINITE);
                    pulso.addUpdateListener(a -> nuevo.setScaleY((float) a.getAnimatedValue()));
                    guardar(pulso);

                    // Confeti desde el maguey.
                    int[] posicion = new int[2];
                    int[] raiz = new int[2];
                    maguey.getLocationOnScreen(posicion);
                    confeti.getLocationOnScreen(raiz);

                    confeti.lanzar(
                            posicion[0] - raiz[0] + maguey.getWidth() / 2f,
                            posicion[1] - raiz[1] + maguey.getHeight() / 2f,
                            110);

                    handler.postDelayed(() -> {
                        if (dialog.isShowing()) confeti.llover(40);
                    }, 500);

                    marcarHoy();
                })
                .start();
    }

    /** El círculo de hoy se llena con palomita. */
    private void marcarHoy() {

        LinearLayout semana = dialog.findViewById(R.id.contenedorSemanaRacha);

        for (int i = 0; i < semana.getChildCount(); i++) {

            LinearLayout columna = (LinearLayout) semana.getChildAt(i);
            View circulo = columna.getChildAt(1);

            if (!"hoy".equals(circulo.getTag())) continue;

            GradientDrawable fondo = (GradientDrawable) circulo.getBackground();
            fondo.setColor(ContextCompat.getColor(context, R.color.nivel_naranja));

            View palomita = ((FrameLayout) circulo).getChildAt(0);
            palomita.setVisibility(View.VISIBLE);
            palomita.setScaleX(0f);
            palomita.setScaleY(0f);
            palomita.animate().scaleX(1f).scaleY(1f).setDuration(400)
                    .setInterpolator(new OvershootInterpolator(3f)).start();

            circulo.setScaleX(1.35f);
            circulo.setScaleY(1.35f);
            circulo.animate().scaleX(1f).scaleY(1f).setDuration(450)
                    .setInterpolator(new OvershootInterpolator(2f)).start();
        }
    }

    /** El maguey cambia al dibujo más grande con un destello. */
    private void evolucionar(ImageView maguey, ConfetiView confeti) {

        if (!dialog.isShowing()) return;

        View destello = dialog.findViewById(R.id.destelloRacha);

        destello.setScaleX(0.5f);
        destello.setScaleY(0.5f);
        destello.setAlpha(1f);
        destello.animate().scaleX(1.6f).scaleY(1.6f).alpha(0f).setDuration(700)
                .setInterpolator(new DecelerateInterpolator()).start();

        maguey.animate().scaleX(0.2f).scaleY(0.2f).setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {

                    if (!dialog.isShowing()) return;

                    maguey.setImageResource(RachaMaguey.dibujoPorRacha(dias));
                    maguey.animate().scaleX(1f).scaleY(1f).setDuration(550)
                            .setInterpolator(new OvershootInterpolator(3f)).start();

                    confeti.lanzar(confeti.getWidth() / 2f, confeti.getHeight() * 0.35f, 60);
                })
                .start();
    }

    // =========================================
    // AYUDANTES
    // =========================================

    private void guardar(Animator animacion) {

        if (!dialog.isShowing()) return;

        animaciones.add(animacion);
        animacion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animaciones.remove(animation);
            }
        });
        animacion.start();
    }

    private int dp(int valor) {
        return Math.round(valor * context.getResources().getDisplayMetrics().density);
    }
}