package com.itsoeh.sbsc.buti;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla final de B'ÜTI: el colibrí (logo) en grande con animación,
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
            if (alPrincipal != null) alPrincipal.run();
        });

        MaterialButton secundario = dialog.findViewById(R.id.btnSecundarioResultado);
        if (textoSecundario != null) {
            secundario.setVisibility(View.VISIBLE);
            secundario.setText(textoSecundario);
            secundario.setOnClickListener(v -> {
                dialog.dismiss();
                if (alSecundario != null) alSecundario.run();
            });
        }
    }

    /** Muestra la racha cuando Firebase responde. */
    public void ponerRacha(int dias) {
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
    // ANIMACIONES
    // =========================================

    private void animar(Datos datos) {

        View logo = dialog.findViewById(R.id.contenedorLogoResultado);
        View halo = dialog.findViewById(R.id.viewHaloResultado);

        // 1) El colibrí aparece girando y "rebota".
        logo.setRotation(-25f);
        logo.animate()
                .scaleX(1f).scaleY(1f).rotation(0f)
                .setStartDelay(150)
                .setDuration(750)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .withEndAction(() -> flotar(logo))
                .start();

        // 2) El halo dorado late detrás del colibrí.
        halo.animate().alpha(1f).setStartDelay(300).setDuration(500).start();
        ObjectAnimator latido = ObjectAnimator.ofFloat(halo, View.SCALE_X, 0.92f, 1.08f);
        latido.setDuration(1100);
        latido.setRepeatMode(ValueAnimator.REVERSE);
        latido.setRepeatCount(ValueAnimator.INFINITE);
        latido.setStartDelay(800);
        latido.addUpdateListener(a -> halo.setScaleY((float) a.getAnimatedValue()));
        guardar(latido);

        // 3) Las estrellas brotan una por una.
        int[] estrellas = {
                R.id.imgEstrellaResultado1, R.id.imgEstrellaResultado2,
                R.id.imgEstrellaResultado3, R.id.imgEstrellaResultado4
        };

        for (int i = 0; i < estrellas.length; i++) {
            View estrella = dialog.findViewById(estrellas[i]);
            if (!datos.exito && i >= 2) {
                estrella.setVisibility(View.GONE);   // menos estrellas si no aprobó
                continue;
            }
            estrella.setRotation(-90f);
            estrella.animate()
                    .scaleX(1f).scaleY(1f).rotation(0f)
                    .setStartDelay(700 + i * 160L)
                    .setDuration(420)
                    .setInterpolator(new OvershootInterpolator(2.5f))
                    .withEndAction(() -> brillar(estrella))
                    .start();
        }

        // 4) Las tarjetas de datos aparecen en cascada.
        int[] stats = {
                R.id.txtCorrectasResultadoFinal, R.id.txtRepasoResultadoFinal,
                R.id.txtPuntosResultadoFinal, R.id.txtRachaResultadoFinal
        };

        for (int i = 0; i < stats.length; i++) {
            View tarjeta = (View) dialog.findViewById(stats[i]).getParent();
            tarjeta.setTranslationY(30f);
            tarjeta.animate().alpha(1f).translationY(0f)
                    .setStartDelay(900 + i * 120L)
                    .setDuration(380)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // 5) Los puntos cuentan hacia arriba.
        TextView txtPuntos = dialog.findViewById(R.id.txtPuntosResultadoFinal);
        ValueAnimator conteo = ValueAnimator.ofInt(0, Math.max(0, datos.puntos));
        conteo.setDuration(1200);
        conteo.setStartDelay(1100);
        conteo.setInterpolator(new DecelerateInterpolator());
        conteo.addUpdateListener(a -> txtPuntos.setText("+" + a.getAnimatedValue()));
        guardar(conteo);
    }

    // El colibrí flota suavemente arriba y abajo.
    private void flotar(View logo) {
        ObjectAnimator flotar = ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, -8f, 8f);
        flotar.setDuration(1400);
        flotar.setRepeatMode(ValueAnimator.REVERSE);
        flotar.setRepeatCount(ValueAnimator.INFINITE);
        flotar.setInterpolator(new AccelerateDecelerateInterpolator());
        guardar(flotar);
    }

    // Las estrellas titilan.
    private void brillar(View estrella) {
        ObjectAnimator brillo = ObjectAnimator.ofFloat(estrella, View.ALPHA, 1f, 0.45f);
        brillo.setDuration(700 + (long) (Math.random() * 500));
        brillo.setRepeatMode(ValueAnimator.REVERSE);
        brillo.setRepeatCount(ValueAnimator.INFINITE);
        guardar(brillo);
    }

    private void guardar(Animator animacion) {
        if (!dialog.isShowing()) return;
        animaciones.add(animacion);
        animacion.start();
    }

    private void detenerAnimaciones() {
        for (Animator a : animaciones) a.cancel();
        animaciones.clear();
    }
}
