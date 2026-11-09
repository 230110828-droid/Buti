package com.itsoeh.sbsc.buti.admin;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.vocabulario.PalabraButi;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

import java.util.ArrayList;
import java.util.List;

/** Lista de palabras del panel de Admin (datos reales de Firebase). */
public class PalabrasAdapter extends RecyclerView.Adapter<PalabrasAdapter.PalabraViewHolder> {

    public interface Acciones {
        void escuchar(PalabraButi palabra);

        void editar(PalabraButi palabra);

        void eliminar(PalabraButi palabra);
    }

    private final List<PalabraButi> palabras = new ArrayList<>();
    private final Acciones acciones;

    public PalabrasAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    public void mostrar(List<PalabraButi> nuevas) {
        palabras.clear();
        palabras.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PalabraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_palabra_admin, parent, false);
        return new PalabraViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PalabraViewHolder holder, int position) {

        PalabraButi p = palabras.get(position);

        holder.tvHnahnu.setText(p.hnahnu);
        holder.tvEspanol.setText(p.espanol);

        String detalle = VocabularioNube.nombre(p.categoria)
                + (p.tieneImagen() ? " · con imagen" : " · sin imagen")
                + (p.tieneAudio() ? " · con audio" : " · sin audio");

        holder.tvDetalle.setText(detalle);

        // Imagen: la subida, la de la app o un ícono de "sin imagen".
        Bitmap subida = VocabularioNube.decodificarImagen(p);

        if (subida != null) {
            holder.img.setImageBitmap(subida);
        } else {
            int recurso = p.imagen.isEmpty() ? 0 : holder.itemView.getResources()
                    .getIdentifier(p.imagen, "drawable", holder.itemView.getContext().getPackageName());

            holder.img.setImageResource(recurso != 0 ? recurso : R.drawable.ic_cambiar_imagen);
        }

        holder.btnEscuchar.setVisibility(p.tieneAudio() ? View.VISIBLE : View.INVISIBLE);
        holder.btnEscuchar.setOnClickListener(v -> acciones.escuchar(p));
        holder.btnEditar.setOnClickListener(v -> acciones.editar(p));
        holder.btnEliminar.setOnClickListener(v -> acciones.eliminar(p));

        holder.itemView.setContentDescription(p.hnahnu + ", " + p.espanol);
    }

    @Override
    public int getItemCount() {
        return palabras.size();
    }

    static class PalabraViewHolder extends RecyclerView.ViewHolder {

        final ImageView img;
        final TextView tvHnahnu, tvEspanol, tvDetalle;
        final ImageButton btnEscuchar, btnEditar, btnEliminar;

        PalabraViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPalabraAdmin);
            tvHnahnu = itemView.findViewById(R.id.tvPalabraHnahnuAdmin);
            tvEspanol = itemView.findViewById(R.id.tvPalabraEspanolAdmin);
            tvDetalle = itemView.findViewById(R.id.tvDetallePalabraAdmin);
            btnEscuchar = itemView.findViewById(R.id.btnEscucharPalabraAdmin);
            btnEditar = itemView.findViewById(R.id.btnEditarPalabraAdmin);
            btnEliminar = itemView.findViewById(R.id.btnEliminarPalabraAdmin);
        }
    }
}