package com.itsoeh.sbsc.buti.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itsoeh.sbsc.buti.R;

import java.util.List;

public class PalabrasAdapter extends RecyclerView.Adapter<PalabrasAdapter.PalabraViewHolder> {

    private final List<PalabraSimulada> listaPalabras;

    public PalabrasAdapter(List<PalabraSimulada> listaPalabras) {
        this.listaPalabras = listaPalabras;
    }

    @NonNull
    @Override
    public PalabraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_palabra_admin, parent, false);
        return new PalabraViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PalabraViewHolder holder, int position) {
        PalabraSimulada p = listaPalabras.get(position);
        holder.tvLeccion.setText(p.getLeccion());
        holder.tvHnahnu.setText(p.getPalabraHnahnu());
        holder.tvEspanol.setText(p.getPalabraEspanol());
        holder.tvCategoria.setText(p.getCategoria());
        holder.tvAudio.setText("Audio: " + p.getAudioNombre());
        holder.tvImagen.setText("Imagen: " + p.getImagenNombre());
    }

    @Override
    public int getItemCount() {
        return listaPalabras.size();
    }

    static class PalabraViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeccion, tvHnahnu, tvEspanol, tvCategoria, tvAudio, tvImagen;

        public PalabraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLeccion = itemView.findViewById(R.id.tvLeccionAdmin);
            tvHnahnu = itemView.findViewById(R.id.tvPalabraHnahnuAdmin);
            tvEspanol = itemView.findViewById(R.id.tvPalabraEspanolAdmin);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaAdmin);
            tvAudio = itemView.findViewById(R.id.tvAudioAdmin);
            tvImagen = itemView.findViewById(R.id.tvImagenAdmin);
        }
    }
}
