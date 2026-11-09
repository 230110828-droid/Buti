package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla final de B'ÜTI al estilo Duolingo: el colibrí llega volando,
 * aterriza con rebote, festeja con saltitos y explota el confeti.
 * Si no aprobó, llega despacito y te anima a seguir.
 *
 * Opcional: si agregas a drawable ic_colibri_aleteo1, ic_colibri_aleteo2,
 * ic_colibri_celebra o ic_colibri_animando, se usan solos.
 *
 * También muestra
 * las respuestas correctas, el repaso, los puntos y la racha.
 *
 * Uso:
 *   ResultadoButi.Datos d = new ResultadoButi.Datos();
 *   d.titulo = "¡Lo lograste!"; d.correctas = 5; d.total = 6; d.puntos = 55; ...
 *   ResultadoButi r = ResultadoButi.mostrar(contexto, d,
 *           "Volver a etapas", () -> ..., "Repetir", () -> ...);
 *   r.ponerRacha(3);   // cuando Firebase responde
 */
public final class ResultadoButi {

    /** Datos que se muestran en la pantalla final. */
    public static class Datos {
        public String etiqueta = "RESULTADO";
        public String titulo = "¡Lo lograste!";
        public String mensaje = "";
        public int correctas = 0;
        public int total = 0;
        public int repasoCorrectas = 0;
        public int repasoTotal = 0;
        public int puntos = 0;
        public boolean exito = true;
    }

    private final Dialog dialog;
    private Context contexto;

    // Racha nueva del día: se muestra a pantalla completa al cerrar el resultado.
    private boolean mostrarRachaAlCerrar = false;
    private int diasRacha = 0;
    private final List<Animator> animaciones = new ArrayList<>();
    private TextView txtRacha;
    private ImageView imgRacha;

    private ResultadoButi(Dialog dialog) {
        this.dialog = dialog;
    }

    public static ResultadoButi mostrar(
            Context context,
            Datos datos,
            String textoPrincipal,
            @Nullable Runnable alPrincipal,
            @Nullable String textoSecundario,
            @Nullable Runnable alSecundario) {

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_resultado_buti);
        dialog.setCancelable(false);

        ResultadoButi resultado = new ResultadoButi(dialog);
        resultado.contexto = context;
        resultado.llenar(context, datos, textoPrincipal, alPrincipal, textoSecundario, alSecundario);

        dialog.setOnDismissListener(d -> resultado.detenerAnimaciones());
        dialog.show();

        Window ventana = dialog.getWindow();
        if (ventana != null) {
            ventana.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ventana.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ventana.setDimAmount(0.72f);
        }

        resultado.animar(datos);

        LectorVoz.hablar(context, datos.titulo + ". " + datos.mensaje
                + ". Respuestas correctas: " + datos.correctas + " de " + datos.total
                + ". Puntos: " + datos.puntos);

        return resultado;
    }

    /**
     * Para práctica y juegos: muestra el colibrí, suma los puntos
     * a "puntosActividades" y actualiza la racha del día.
     */
    public static ResultadoButi mostrarYGuardar(
            Context context,
            Datos datos,
            String textoPrincipal,
            @Nullable Runnable alPrincipal,
            @Nullable String textoSecundario,
            @Nullable Runnable alSecundario) {

        ResultadoButi resultado = mostrar(context, datos,
                textoPrincipal, alPrincipal, textoSecundario, alSecundario);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            resultado.ponerRacha(0);
            return resultado;
        }

        ProgresoButi.sumarPuntosActividad(datos.puntos,
                (sumados, racha) -> resultado.ponerRacha(racha));

        return resultado;
    }

    private void llenar(Context context, Datos datos,
                        String textoPrincipal, @Nullable Runnable alPrincipal,
                        @Nullable String textoSecundario, @Nullable Runnable alSecundario) {

        ((TextView) dialog.findViewById(R.id.txtEtiquetaResultado)).setText(datos.etiqueta);
        ((TextView) dialog.findViewById(R.id.txtTituloResultadoFinal)).setText(datos.titulo);

        TextView mensaje = dialog.findViewById(R.id.txtMensajeResultadoFinal);
        mensaje.setText(datos.mensaje);
        mensaje.setVisibility(datos.mensaje == null || datos.mensaje.isEmpty() ? View.GONE : View.VISIBLE);

        ((TextView) dialog.findViewById(R.id.txtCorrectasResultadoFinal))
                .setText(datos.correctas + "/" + datos.total);

        if (datos.repasoTotal > 0) {
            ((TextView) dialog.findViewById(R.id.txtRepasoResultadoFinal))
                    .setText(datos.repasoCorrectas + "/" + datos.repasoTotal);
        } else {
            ((TextView) dialog.findViewById(R.id.txtRepasoResultadoFinal)).setText("—");
        }

        txtRacha = dialog.findViewById(R.id.txtRachaResultadoFinal);
        imgRacha = dialog.findViewById(R.id.imgRachaResultadoFinal);
        txtRacha.setText("…");

        MaterialButton principal = dialog.findViewById(R.id.btnPrincipalResultado);
        principal.setText(textoPrincipal);
        principal.setOnClickListener(v -> {
            dialog.dismiss();
            continuar(alPrincipal);
        });

        MaterialButton secundario = dialog.findViewById(R.id.btnSecundarioResultado);
        if (textoSecundario != null) {
            secundario.setVisibility(View.VISIBLE);
            secundario.setText(textoSecundario);
            secundario.setOnClickListener(v -> {
                dialog.dismiss();
                continuar(alSecundario);
            });
        }
    }

    /** Primero la pantalla de racha (si es la primera actividad del día) y luego la acción. */
    private void continuar(@Nullable Runnable accion) {

        if (mostrarRachaAlCerrar) {
            mostrarRachaAlCerrar = false;
            RachaButi.mostrar(contexto, diasRacha, accion);
        } else if (accion != null) {
            accion.run();
        }
    }

    /** Muestra la racha cuando Firebase responde. */
    public void ponerRacha(int dias) {

        // ¿Es la primera actividad de hoy? Entonces toca la pantalla de racha.
        if (ProgresoButi.tomarRachaNueva() && dias > 0) {

            if (dialog.isShowing()) {
                mostrarRachaAlCerrar = true;
                diasRacha = dias;
            } else {
                // El usuario ya cerró el resultado: se muestra de una vez.
                RachaButi.mostrar(contexto, dias, null);
            }
        }

        if (txtRacha == null || !dialog.isShowing()) return;
        txtRacha.setText(String.valueOf(Math.max(0, dias)));
        if (imgRacha != null) imgRacha.setImageResource(RachaMaguey.dibujoPorRacha(dias));

        txtRacha.setScaleX(0.6f);
        txtRacha.setScaleY(0.6f);
        txtRacha.animate().scaleX(1f).scaleY(1f).setDuration(350)
                .setInterpolator(new OvershootInterpolator(2f)).start();
    }

    public void cerrar() {
        if (dialog.isShowing()) dialog.dismiss();
    }

    // =========================================
    // ANIMACIONES (estilo Duolingo)
    // =========================================

    private final Handler handler = new Handler(Looper.getMainLooper());

    private View escenario;
    private View contenedor;
    private ImageView colibri;
    private ConfetiView confeti;
    private View halo;
    private View sombra;
    private AnimationDrawable aleteo;
    private int poseReposo = R.drawable.ic_colibri_feliz;

    private void animar(Datos datos) {

        escenario = dialog.findViewById(R.id.escenarioResultado);
        contenedor = dialog.findViewById(R.id.contenedorLogoResultado);
        colibri = dialog.findViewById(R.id.imgLogoResultado);
        confeti = dialog.findViewById(R.id.confetiResultado);
        halo = dialog.findViewById(R.id.viewHaloResultado);
        sombra = dialog.findViewById(R.id.viewSombraResultado);

        // Esperar a que el diálogo tenga medidas para calcular el vuelo.
        escenario.post(() -> {

            if (!dialog.isShowing()) return;

            if (datos.exito) {
                entradaFeliz();
            } else {
                entradaAnimando();
            }

            animarEstrellas(datos);
            animarTarjetas(datos);
        });
    }

    // ---------- ¡Lo lograste! ----------

    /** 1) Llega volando en curva aleteando, desde la izquierda. */
    private void entradaFeliz() {

        float inicioX = -(escenario.getWidth() / 2f + contenedor.getWidth());

        contenedor.setTranslationX(inicioX);
        contenedor.setTranslationY(dp(50));
        contenedor.setRotation(-18f);
        contenedor.setScaleX(0.7f);
        contenedor.setScaleY(0.7f);
        contenedor.setVisibility(View.VISIBLE);

        empezarAleteo();

        ValueAnimator vuelo = ValueAnimator.ofFloat(0f, 1f);
        vuelo.setDuration(1050);
        vuelo.setInterpolator(new DecelerateInterpolator(1.4f));
        vuelo.addUpdateListener(a -> {

            float t = (float) a.getAnimatedValue();
            float arco = (float) Math.sin(Math.PI * t);

            contenedor.setTranslationX(inicioX * (1 - t));
            contenedor.setTranslationY(dp(50) * (1 - t) - dp(70) * arco);
            contenedor.setRotation(-18f * (1 - t) + 12f * arco);
            contenedor.setScaleX(0.7f + 0.3f * t);
            contenedor.setScaleY(0.7f + 0.3f * t);
        });
        vuelo.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dialog.isShowing()) aterrizar();
            }
        });

        guardar(vuelo);
    }

    /** 2) Aterriza: se aplasta y rebota, explota el confeti y late el halo. */
    private void aterrizar() {

        detenerAleteo();
        colibri.setImageResource(R.drawable.ic_colibri_feliz);

        aplastar(1.28f, 0.76f);

        contenedor.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

        sombra.animate().alpha(0.55f).setDuration(250).start();

        confeti.lanzar(escenario.getWidth() / 2f, escenario.getHeight() * 0.55f, 90);

        halo.animate().alpha(1f).setDuration(400).start();

        ObjectAnimator latido = ObjectAnimator.ofFloat(halo, View.SCALE_X, 0.9f, 1.1f);
        latido.setDuration(1000);
        latido.setRepeatMode(ValueAnimator.REVERSE);
        latido.setRepeatCount(ValueAnimator.INFINITE);
        latido.addUpdateListener(a -> halo.setScaleY((float) a.getAnimatedValue()));
        guardar(latido);

        handler.postDelayed(this::festejar, 650);
    }

    /** 3) Festeja con dos saltitos (con la pose de celebrar si existe). */
    private void festejar() {

        if (!dialog.isShowing()) return;

        int celebra = recurso("ic_colibri_celebra");
        if (celebra != 0) colibri.setImageResource(celebra);

        saltar(0, () -> saltar(1, () -> {
            colibri.setImageResource(R.drawable.ic_colibri_feliz);
            confeti.llover(30);
            reposar();
        }));
    }

    private void saltar(int numero, Runnable alTerminar) {

        if (!dialog.isShowing()) return;

        float giro = numero % 2 == 0 ? 10f : -10f;

        AnimatorSet salto = new AnimatorSet();

        ObjectAnimator subir = ObjectAnimator.ofFloat(contenedor, View.TRANSLATION_Y, 0f, -dp(38));
        subir.setDuration(230);
        subir.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator girar = ObjectAnimator.ofFloat(contenedor, View.ROTATION, 0f, giro, 0f);
        girar.setDuration(460);

        ObjectAnimator bajar = ObjectAnimator.ofFloat(contenedor, View.TRANSLATION_Y, -dp(38), 0f);
        bajar.setDuration(230);
        bajar.setInterpolator(new AccelerateInterpolator());

        AnimatorSet subeYBaja = new AnimatorSet();
        subeYBaja.playSequentially(subir, bajar);

        salto.playTogether(subeYBaja, girar);
        salto.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!dialog.isShowing()) return;
                aplastar(1.15f, 0.86f);
                confeti.lanzar(escenario.getWidth() / 2f, escenario.getHeight() * 0.7f, 18);
                handler.postDelayed(alTerminar, 160);
            }
        });

        guardar(salto);
    }

    // ---------- ¡Sigue practicando! ----------

    /** Llega despacito, descansa y luego te anima a seguir. */
    private void entradaAnimando() {

        colibri.setImageResource(R.drawable.ic_colibri_descansando);

        contenedor.setTranslationY(-escenario.getHeight());
        contenedor.setVisibility(View.VISIBLE);

        ObjectAnimator caer = ObjectAnimator.ofFloat(contenedor, View.TRANSLATION_Y,
                -escenario.getHeight(), 0f);
        caer.setDuration(950);
        caer.setInterpolator(new BounceInterpolator());
        caer.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                if (!dialog.isShowing()) return;

                sombra.animate().alpha(0.4f).setDuration(250).start();
                halo.animate().alpha(0.6f).setDuration(500).start();

                // Despierta y asiente: "¡tú puedes!".
                handler.postDelayed(() -> {

                    if (!dialog.isShowing()) return;

                    int animando = recurso("ic_colibri_animando");
                    poseReposo = animando != 0 ? animando : R.drawable.ic_colibri_normal;
                    colibri.setImageResource(poseReposo);

                    aplastar(1.1f, 0.9f);

                    ObjectAnimator asentir = ObjectAnimator.ofFloat(contenedor, View.ROTATION,
                            0f, -9f, 9f, -5f, 0f);
                    asentir.setDuration(700);
                    asentir.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (dialog.isShowing()) reposar();
                        }
                    });
                    guardar(asentir);

                }, 500);
            }
        });

        guardar(caer);
    }

    // ---------- Movimientos compartidos ----------

    /** Squash & stretch: se aplasta contra el piso y regresa rebotando. */
    private void aplastar(float ancho, float alto) {

        contenedor.setPivotX(contenedor.getWidth() / 2f);
        contenedor.setPivotY(contenedor.getHeight());

        contenedor.animate().cancel();
        contenedor.setScaleX(ancho);
        contenedor.setScaleY(alto);
        contenedor.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(420)
                .setInterpolator(new OvershootInterpolator(3f))
                .start();
    }

    /** Se queda flotando suavecito (y aletea de vez en cuando). */
    private void reposar() {

        contenedor.setPivotY(contenedor.getHeight() / 2f);

        ObjectAnimator flotar = ObjectAnimator.ofFloat(contenedor, View.TRANSLATION_Y, -dp(6), dp(6));
        flotar.setDuration(1400);
        flotar.setRepeatMode(ValueAnimator.REVERSE);
        flotar.setRepeatCount(ValueAnimator.INFINITE);
        flotar.setInterpolator(new AccelerateDecelerateInterpolator());
        guardar(flotar);

        ObjectAnimator mecer = ObjectAnimator.ofFloat(contenedor, View.ROTATION, -3f, 3f);
        mecer.setDuration(1900);
        mecer.setRepeatMode(ValueAnimator.REVERSE);
        mecer.setRepeatCount(ValueAnimator.INFINITE);
        mecer.setInterpolator(new AccelerateDecelerateInterpolator());
        guardar(mecer);

        // Cada ~3 segundos, un aleteo rápido.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!dialog.isShowing()) return;
                empezarAleteo();
                handler.postDelayed(() -> {
                    if (!dialog.isShowing()) return;
                    detenerAleteo();
                    colibri.setImageResource(poseReposo);
                }, 650);
                handler.postDelayed(this, 3200);
            }
        }, 1800);
    }

    /**
     * Aleteo: si existen ic_colibri_aleteo1 y ic_colibri_aleteo2 se
     * alternan como animación cuadro por cuadro. Si no, usa
     * ic_colibri_volando con un zumbido rápido.
     */
    private void empezarAleteo() {

        int cuadro1 = recurso("ic_colibri_aleteo1");
        int cuadro2 = recurso("ic_colibri_aleteo2");

        if (cuadro1 != 0 && cuadro2 != 0) {

            aleteo = new AnimationDrawable();
            aleteo.addFrame(ContextCompat.getDrawable(colibri.getContext(), cuadro1), 70);
            aleteo.addFrame(ContextCompat.getDrawable(colibri.getContext(), cuadro2), 70);
            aleteo.setOneShot(false);

            colibri.setImageDrawable(aleteo);
            aleteo.start();

        } else {

            colibri.setImageResource(R.drawable.ic_colibri_volando);

            ObjectAnimator zumbido = ObjectAnimator.ofFloat(colibri, View.TRANSLATION_Y, -dp(2), dp(2));
            zumbido.setDuration(60);
            zumbido.setRepeatMode(ValueAnimator.REVERSE);
            zumbido.setRepeatCount(ValueAnimator.INFINITE);
            colibri.setTag(zumbido);
            guardar(zumbido);
        }
    }

    private void detenerAleteo() {

        if (aleteo != null) {
            aleteo.stop();
            aleteo = null;
        }

        Object zumbido = colibri.getTag();

        if (zumbido instanceof Animator) {
            ((Animator) zumbido).cancel();
            animaciones.remove(zumbido);
            colibri.setTag(null);
            colibri.setTranslationY(0f);
        }
    }

    // ---------- Estrellas y tarjetas ----------

    private void animarEstrellas(Datos datos) {

        int[] estrellas = {
                R.id.imgEstrellaResultado1, R.id.imgEstrellaResultado2,
                R.id.imgEstrellaResultado3, R.id.imgEstrellaResultado4
        };

        long inicio = datos.exito ? 1150 : 1100;

        for (int i = 0; i < estrellas.length; i++) {

            View estrella = dialog.findViewById(estrellas[i]);

            if (!datos.exito && i >= 2) {
                estrella.setVisibility(View.GONE);
                continue;
            }

            estrella.setRotation(-120f);
            estrella.animate()
                    .scaleX(1f).scaleY(1f).rotation(0f)
                    .setStartDelay(inicio + i * 140L)
                    .setDuration(450)
                    .setInterpolator(new OvershootInterpolator(3f))
                    .withEndAction(() -> brillar(estrella))
                    .start();
        }
    }

    private void animarTarjetas(Datos datos) {

        int[] stats = {
                R.id.txtCorrectasResultadoFinal, R.id.txtRepasoResultadoFinal,
                R.id.txtPuntosResultadoFinal, R.id.txtRachaResultadoFinal
        };

        for (int i = 0; i < stats.length; i++) {
            View tarjeta = (View) dialog.findViewById(stats[i]).getParent();
            tarjeta.setTranslationY(dp(24));
            tarjeta.animate().alpha(1f).translationY(0f)
                    .setStartDelay(1300 + i * 120L)
                    .setDuration(380)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Los puntos cuentan hacia arriba.
        TextView txtPuntos = dialog.findViewById(R.id.txtPuntosResultadoFinal);
        ValueAnimator conteo = ValueAnimator.ofInt(0, Math.max(0, datos.puntos));
        conteo.setDuration(1200);
        conteo.setStartDelay(1500);
        conteo.setInterpolator(new DecelerateInterpolator());
        conteo.addUpdateListener(a -> txtPuntos.setText("+" + a.getAnimatedValue()));
        guardar(conteo);
    }

    // Las estrellas titilan.
    private void brillar(View estrella) {
        ObjectAnimator brillo = ObjectAnimator.ofFloat(estrella, View.ALPHA, 1f, 0.45f);
        brillo.setDuration(700 + (long) (Math.random() * 500));
        brillo.setRepeatMode(ValueAnimator.REVERSE);
        brillo.setRepeatCount(ValueAnimator.INFINITE);
        guardar(brillo);
    }

    // ---------- Ayudantes ----------

    /** Busca un dibujo por nombre (0 si todavía no lo agregaste a drawable). */
    private int recurso(String nombre) {
        Context c = colibri.getContext();
        return c.getResources().getIdentifier(nombre, "drawable", c.getPackageName());
    }

    private float dp(float valor) {
        return valor * colibri.getResources().getDisplayMetrics().density;
    }

    private void guardar(Animator animacion) {
        if (!dialog.isShowing()) return;
        animaciones.add(animacion);
        animacion.start();
    }

    private void detenerAnimaciones() {

        handler.removeCallbacksAndMessages(null);

        for (Animator a : new ArrayList<>(animaciones)) a.cancel();
        animaciones.clear();

        if (aleteo != null) aleteo.stop();
        if (confeti != null) confeti.detener();
    }
}