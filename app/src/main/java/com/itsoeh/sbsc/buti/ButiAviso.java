package com.itsoeh.sbsc.buti;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

/**
 * Aviso corto de B'ÜTI que aparece arriba y se va solo.
 * Reemplaza a los Toast.
 *
 * Desde una Activity:
 *     ButiAviso.mostrar(this, "Perfil actualizado", ButiAviso.Tipo.EXITO);
 *
 * Desde un Fragment:
 *     ButiAviso.mostrar(this, "Selecciona una opción", ButiAviso.Tipo.INFO);
 */
public final class ButiAviso {

    public enum Tipo { EXITO, ERROR, INFO }

    private static final String ETIQUETA = "buti_aviso";
    private static final long DURACION = 2600;

    private ButiAviso() {
    }

    public static void mostrar(Fragment fragment, String mensaje, Tipo tipo) {
        if (fragment == null || !fragment.isAdded() || fragment.getActivity() == null) {
            return;
        }
        mostrar(fragment.requireActivity(), mensaje, tipo);
    }

    public static void mostrar(Activity activity, String mensaje, Tipo tipo) {

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        ViewGroup raiz = activity.findViewById(android.R.id.content);
        if (raiz == null) return;

        // Si ya hay un aviso, se quita para mostrar el nuevo.
        View anterior = raiz.findViewWithTag(ETIQUETA);
        if (anterior != null) {
            raiz.removeView(anterior);
        }

        View aviso = LayoutInflater.from(activity)
                .inflate(R.layout.aviso_buti, raiz, false);
        aviso.setTag(ETIQUETA);

        TextView txt = aviso.findViewById(R.id.txtAvisoButi);
        ImageView img = aviso.findViewById(R.id.imgAvisoButi);
        View franja = aviso.findViewById(R.id.franjaAvisoButi);
        MaterialCardView card = (MaterialCardView) aviso;

        txt.setText(mensaje);

        // Colores y personaje según el tipo
        int color;
        int personaje;

        switch (tipo) {
            case EXITO:
                color = R.color.buti_verde;
                personaje = R.drawable.ic_colibri_feliz;
                break;
            case ERROR:
                color = R.color.buti_vino;
                personaje = R.drawable.ic_pajtriste;
                break;
            default:
                color = R.color.buti_dorado;
                personaje = R.drawable.ic_colibri_normal;
                break;
        }

        int colorReal = ContextCompat.getColor(activity, color);
        franja.setBackgroundColor(colorReal);
        card.setStrokeColor(colorReal);
        img.setImageResource(personaje);

        // Posición: arriba, debajo de la barra de estado
        float densidad = activity.getResources().getDisplayMetrics().density;
        int margen = (int) (16 * densidad);
        int arriba = margen;

        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(raiz);
        if (insets != null) {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            arriba += barras.top;
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP);
        params.setMargins(margen, arriba, margen, 0);

        raiz.addView(aviso, params);

        // Entrada
        aviso.setAlpha(0f);
        aviso.setTranslationY(-60 * densidad);
        aviso.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .start();

        // Tocar para cerrar antes
        aviso.setOnClickListener(v -> ocultar(raiz, aviso, densidad));

        // Se va solo
        aviso.postDelayed(() -> ocultar(raiz, aviso, densidad), DURACION);
    }

    private static void ocultar(ViewGroup raiz, View aviso, float densidad) {

        if (aviso.getParent() == null) return;

        aviso.animate()
                .alpha(0f)
                .translationY(-40 * densidad)
                .setDuration(240)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> raiz.removeView(aviso))
                .start();
    }
}
