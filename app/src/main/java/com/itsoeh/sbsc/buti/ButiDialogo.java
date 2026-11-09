package com.itsoeh.sbsc.buti;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

/**
 * Mensajes personalizados de B'ÜTI (reemplazan a los AlertDialog y Toast importantes).
 *
 *  mostrarCargando(...)      -> colibrí volando + círculo de carga
 *  mostrarExito(...)         -> colibrí feliz + botón verde "Continuar"
 *  mostrarError(...)         -> pájaro triste + botón vino "Entendido"
 *  mostrarInfo(...)          -> colibrí normal + botón vino "Entendido"   (nuevo)
 *  mostrarConfirmacion(...)  -> niña pensando + dos botones               (nuevo)
 */
public class ButiDialogo {

    private final Context context;
    private final Dialog dialog;

    private final TextView tvTitulo;
    private final TextView tvMensaje;
    private final ProgressBar progreso;
    private final MaterialButton btnDialogo;
    private final MaterialButton btnSecundario;
    private final ImageView imgPersonaje;

    public ButiDialogo(Context context) {

        this.context = context;

        dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogo_buti);
        dialog.setCancelable(false);

        Window ventana = dialog.getWindow();

        if (ventana != null) {

            ventana.setBackgroundDrawableResource(android.R.color.transparent);

            ventana.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );

            ventana.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            WindowManager.LayoutParams parametros = ventana.getAttributes();
            parametros.dimAmount = 0.65f;
            ventana.setAttributes(parametros);
        }

        tvTitulo = dialog.findViewById(R.id.tvTituloDialogo);
        tvMensaje = dialog.findViewById(R.id.tvMensajeDialogo);
        progreso = dialog.findViewById(R.id.progresoDialogo);
        btnDialogo = dialog.findViewById(R.id.btnDialogo);
        btnSecundario = dialog.findViewById(R.id.btnDialogoSecundario);
        imgPersonaje = dialog.findViewById(R.id.imgLogoDialogo);
    }

    // =========================================
    // CARGANDO
    // =========================================

    public void mostrarCargando(String titulo, String mensaje) {

        prepararTextos(titulo, mensaje, R.drawable.ic_colibri_volando);

        progreso.setVisibility(View.VISIBLE);
        btnDialogo.setVisibility(View.GONE);
        btnSecundario.setVisibility(View.GONE);

        dialog.setCancelable(false);
        mostrarDialogo();
    }

    // =========================================
    // ÉXITO
    // =========================================

    public void mostrarExito(String titulo, String mensaje, Runnable accion) {

        prepararTextos(titulo, mensaje, R.drawable.ic_colibri_feliz);

        configurarBotonPrincipal("Continuar", R.color.buti_verde, accion);
        btnSecundario.setVisibility(View.GONE);

        dialog.setCancelable(false);
        mostrarDialogo();
    }

    // =========================================
    // ERROR
    // =========================================

    public void mostrarError(String titulo, String mensaje) {

        prepararTextos(titulo, mensaje, R.drawable.ic_pajtriste);

        configurarBotonPrincipal("Entendido", R.color.buti_vino, null);
        btnSecundario.setVisibility(View.GONE);

        dialog.setCancelable(false);
        mostrarDialogo();
    }

    // =========================================
    // INFORMACIÓN (nuevo)
    // =========================================

    public void mostrarInfo(String titulo, String mensaje, Runnable accion) {

        prepararTextos(titulo, mensaje, R.drawable.ic_colibri_normal);

        configurarBotonPrincipal("Entendido", R.color.buti_vino, accion);
        btnSecundario.setVisibility(View.GONE);

        dialog.setCancelable(true);
        mostrarDialogo();
    }

    // =========================================
    // CONFIRMACIÓN CON DOS BOTONES (nuevo)
    // =========================================

    public void mostrarConfirmacion(
            String titulo,
            String mensaje,
            String textoAceptar,
            String textoCancelar,
            Runnable alAceptar,
            Runnable alCancelar) {

        prepararTextos(titulo, mensaje, R.drawable.ic_nina_pensando);

        configurarBotonPrincipal(textoAceptar, R.color.buti_vino, alAceptar);

        btnSecundario.setText(textoCancelar);
        btnSecundario.setVisibility(View.VISIBLE);
        btnSecundario.setOnClickListener(v -> {
            animarBoton(v);
            dialog.dismiss();
            if (alCancelar != null) alCancelar.run();
        });

        dialog.setCancelable(false);
        mostrarDialogo();
    }

    // =========================================
    // AYUDANTES
    // =========================================

    private void prepararTextos(String titulo, String mensaje, @DrawableRes int personaje) {

        tvTitulo.setText(titulo);
        tvMensaje.setText(mensaje);

        if (imgPersonaje != null) {
            imgPersonaje.setImageResource(personaje);
        }

        progreso.setVisibility(View.GONE);
    }

    private void configurarBotonPrincipal(String texto, int color, Runnable accion) {

        btnDialogo.setText(texto);
        btnDialogo.setVisibility(View.VISIBLE);
        btnDialogo.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(context, color)));

        btnDialogo.setOnClickListener(v -> {
            animarBoton(v);
            dialog.dismiss();
            if (accion != null) accion.run();
        });
    }

    // Mostrar el diálogo con animación
    private void mostrarDialogo() {

        if (!dialog.isShowing()) {
            dialog.show();
        }

        Window ventana = dialog.getWindow();

        if (ventana != null) {
            ventana.setBackgroundDrawableResource(android.R.color.transparent);
            ventana.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        View contenido = tvTitulo.getRootView();

        contenido.setAlpha(0f);
        contenido.setScaleX(0.92f);
        contenido.setScaleY(0.92f);

        contenido.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // El personaje "salta" un poco al aparecer
        if (imgPersonaje != null) {
            imgPersonaje.setScaleX(0.6f);
            imgPersonaje.setScaleY(0.6f);
            imgPersonaje.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(120)
                    .setDuration(380)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .start();
        }
    }

    // Cerrar el diálogo
    public void cerrar() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // Saber si está visible
    public boolean estaMostrando() {
        return dialog.isShowing();
    }

    private void animarBoton(View vista) {

        vista.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(90)
                .withEndAction(() ->
                        vista.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                )
                .start();
    }

    // =========================================
    // VICTORIA (para los juegos): pega este método en ButiDialogo.java,
    // justo debajo de mostrarConfirmacion(...)
    // =========================================

    public void mostrarVictoria(
            String titulo,
            String mensaje,
            String textoAceptar,
            String textoCancelar,
            Runnable alAceptar,
            Runnable alCancelar) {

        prepararTextos(titulo, mensaje, R.drawable.ic_nina_victoria);

        configurarBotonPrincipal(textoAceptar, R.color.buti_verde, alAceptar);

        btnSecundario.setText(textoCancelar);
        btnSecundario.setVisibility(View.VISIBLE);
        btnSecundario.setOnClickListener(v -> {
            animarBoton(v);
            dialog.dismiss();
            if (alCancelar != null) alCancelar.run();
        });

        dialog.setCancelable(false);
        mostrarDialogo();
    }
}