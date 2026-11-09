package com.itsoeh.sbsc.buti;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Ventana para elegir el avatar de perfil (dibujos en punto de cruz),
 * subir una foto de la galería o quitarla.
 *
 *   ElegirAvatar.mostrar(contexto, avatarActual, () -> selectorImagen.launch("image/*"));
 */
public final class ElegirAvatar {

    private ElegirAvatar() {
    }

    public static void mostrar(
            @NonNull Context context,
            @Nullable String avatarActual,
            @Nullable String nombreUsuario,
            @Nullable Runnable alSubirFoto) {

        Dialog dialogo = new Dialog(context);
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogo.setContentView(R.layout.dialog_elegir_avatar);
        dialogo.setCancelable(true);

        ImageView vista = dialogo.findViewById(R.id.imgVistaAvatar);
        TextView iniciales = dialogo.findViewById(R.id.txtInicialesVistaAvatar);
        TextView txtNombre = dialogo.findViewById(R.id.txtNombreAvatar);
        View contenedorVista = dialogo.findViewById(R.id.contenedorVistaAvatar);
        MaterialButton btnGuardar = dialogo.findViewById(R.id.btnGuardarAvatar);

        List<String[]> avatares = FotoPerfil.avataresDisponibles(context);

        // Vista previa inicial: el avatar actual o las iniciales.
        int actual = FotoPerfil.recursoAvatar(context, avatarActual);

        if (actual != 0) {
            vista.setImageResource(actual);
        } else {
            vista.setVisibility(View.GONE);
            iniciales.setVisibility(View.VISIBLE);
            iniciales.setText(FotoPerfil.iniciales(nombreUsuario));
        }

        final String[] elegido = {null};

        AdaptadorAvatares adaptador = new AdaptadorAvatares(avatares, avatarActual, avatar -> {

            elegido[0] = avatar[0];

            vista.setVisibility(View.VISIBLE);
            iniciales.setVisibility(View.GONE);
            vista.setImageResource(FotoPerfil.recursoAvatar(context, avatar[0]));
            txtNombre.setText(avatar[1]);

            // La vista previa "salta" al cambiar.
            contenedorVista.setScaleX(0.8f);
            contenedorVista.setScaleY(0.8f);
            contenedorVista.animate().scaleX(1f).scaleY(1f).setDuration(300)
                    .setInterpolator(new OvershootInterpolator(3f)).start();

            btnGuardar.setEnabled(!avatar[0].equals(avatarActual));

            LectorVoz.hablar(context, avatar[1]);
        });

        RecyclerView lista = dialogo.findViewById(R.id.rvAvatares);
        lista.setLayoutManager(new GridLayoutManager(context, 4));
        lista.setAdapter(adaptador);

        btnGuardar.setOnClickListener(v -> {

            if (elegido[0] == null) return;

            btnGuardar.setEnabled(false);

            FotoPerfil.guardarAvatar(elegido[0], exito -> {
                dialogo.dismiss();
                if (exito) {
                    new ButiDialogo(context).mostrarExito(
                            "¡Nuevo avatar!",
                            "Ya se ve en tu perfil, en el menú y en Inicio.",
                            null
                    );
                } else {
                    new ButiDialogo(context).mostrarError(
                            "No se pudo guardar",
                            "Revisa tu conexión a internet e inténtalo de nuevo."
                    );
                }
            });
        });

        dialogo.findViewById(R.id.btnSubirFotoAvatar).setOnClickListener(v -> {
            dialogo.dismiss();
            if (alSubirFoto != null) alSubirFoto.run();
        });

        dialogo.findViewById(R.id.btnQuitarAvatar).setOnClickListener(v ->
                FotoPerfil.quitarFotoYAvatar(exito -> dialogo.dismiss()));

        dialogo.findViewById(R.id.btnCancelarAvatar).setOnClickListener(v -> dialogo.dismiss());

        dialogo.show();

        Window ventana = dialogo.getWindow();
        if (ventana != null) {
            ventana.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ventana.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // =========================================
    // CUADRÍCULA
    // =========================================

    private interface AlElegir {
        void elegir(String[] avatar);
    }

    private static class AdaptadorAvatares extends RecyclerView.Adapter<AdaptadorAvatares.Celda> {

        private final List<String[]> avatares;
        private final AlElegir alElegir;
        private int seleccionado = -1;

        AdaptadorAvatares(List<String[]> avatares, @Nullable String actual, AlElegir alElegir) {
            this.avatares = avatares;
            this.alElegir = alElegir;

            for (int i = 0; i < avatares.size(); i++) {
                if (avatares.get(i)[0].equals(actual)) seleccionado = i;
            }
        }

        @NonNull
        @Override
        public Celda onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Celda(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_avatar, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Celda celda, int position) {

            String[] avatar = avatares.get(position);
            Context c = celda.itemView.getContext();
            boolean marcado = position == seleccionado;

            celda.imagen.setImageResource(FotoPerfil.recursoAvatar(c, avatar[0]));
            celda.palomita.setVisibility(marcado ? View.VISIBLE : View.GONE);

            // Fondo dorado cuando está elegido.
            celda.marco.setBackgroundTintList(marcado
                    ? ColorStateList.valueOf(ContextCompat.getColor(c, R.color.buti_dorado))
                    : null);

            celda.marco.setScaleX(marcado ? 1.08f : 1f);
            celda.marco.setScaleY(marcado ? 1.08f : 1f);

            celda.marco.setContentDescription("Avatar " + avatar[1] + (marcado ? ", elegido" : ""));

            celda.marco.setOnClickListener(v -> {

                int anterior = seleccionado;
                seleccionado = celda.getAdapterPosition();
                if (seleccionado == RecyclerView.NO_POSITION) return;

                if (anterior >= 0) notifyItemChanged(anterior);
                notifyItemChanged(seleccionado);

                alElegir.elegir(avatares.get(seleccionado));
            });
        }

        @Override
        public int getItemCount() {
            return avatares.size();
        }

        static class Celda extends RecyclerView.ViewHolder {

            final View marco;
            final ImageView imagen;
            final ImageView palomita;

            Celda(@NonNull View itemView) {
                super(itemView);
                marco = itemView.findViewById(R.id.marcoAvatar);
                imagen = itemView.findViewById(R.id.imgAvatarOpcion);
                palomita = itemView.findViewById(R.id.imgPalomitaAvatar);
            }
        }
    }
}
