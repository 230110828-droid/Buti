package com.itsoeh.sbsc.buti;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class ButiDialogo {

    private final Dialog dialog;

    private final TextView tvTitulo;
    private final TextView tvMensaje;
    private final ProgressBar progreso;
    private final MaterialButton btnDialogo;

    public ButiDialogo(Context context) {

        dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogo_buti);
        dialog.setCancelable(false);

        Window ventana = dialog.getWindow();

        if (ventana != null) {

            ventana.setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            ventana.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );

            ventana.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );

            WindowManager.LayoutParams parametros =
                    ventana.getAttributes();

            parametros.dimAmount = 0.65f;

            ventana.setAttributes(parametros);
        }

        tvTitulo = dialog.findViewById(R.id.tvTituloDialogo);
        tvMensaje = dialog.findViewById(R.id.tvMensajeDialogo);
        progreso = dialog.findViewById(R.id.progresoDialogo);
        btnDialogo = dialog.findViewById(R.id.btnDialogo);
    }

    // Mostrar mientras se realiza una operación
    public void mostrarCargando(
            String titulo,
            String mensaje
    ) {

        tvTitulo.setText(titulo);
        tvMensaje.setText(mensaje);

        progreso.setVisibility(View.VISIBLE);
        btnDialogo.setVisibility(View.GONE);

        dialog.setCancelable(false);

        mostrarDialogo();
    }

    // Mostrar cuando la operación fue exitosa
    public void mostrarExito(
            String titulo,
            String mensaje,
            Runnable accion
    ) {

        tvTitulo.setText(titulo);
        tvMensaje.setText(mensaje);

        progreso.setVisibility(View.GONE);
        btnDialogo.setVisibility(View.VISIBLE);

        btnDialogo.setText("Continuar");

        btnDialogo.setOnClickListener(v -> {

            animarBoton(v);

            dialog.dismiss();

            if (accion != null) {
                accion.run();
            }
        });

        dialog.setCancelable(false);

        mostrarDialogo();
    }

    // Mostrar cuando ocurre un error
    public void mostrarError(
            String titulo,
            String mensaje
    ) {

        tvTitulo.setText(titulo);
        tvMensaje.setText(mensaje);

        progreso.setVisibility(View.GONE);
        btnDialogo.setVisibility(View.VISIBLE);

        btnDialogo.setText("Entendido");

        btnDialogo.setOnClickListener(v -> {

            animarBoton(v);
            dialog.dismiss();
        });

        dialog.setCancelable(false);

        mostrarDialogo();
    }

    // Mostrar el diálogo con animación
    private void mostrarDialogo() {

        if (!dialog.isShowing()) {
            dialog.show();
        }

        Window ventana = dialog.getWindow();

        if (ventana != null) {

            ventana.setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            ventana.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        View contenido = dialog.findViewById(
                R.id.tvTituloDialogo
        ).getRootView();

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
}
