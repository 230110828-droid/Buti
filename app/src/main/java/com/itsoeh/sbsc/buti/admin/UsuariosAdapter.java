package com.itsoeh.sbsc.buti.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itsoeh.sbsc.buti.R;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    private final List<UsuarioSimulado> listaUsuarios;

    public UsuariosAdapter(List<UsuarioSimulado> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_admin, parent, false);
        return new UsuarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        UsuarioSimulado u = listaUsuarios.get(position);
        holder.tvNombre.setText(u.getNombre());
        holder.tvCorreo.setText(u.getCorreo());
        holder.tvRacha.setText(u.getRachaDias() + " días de racha");
        holder.tvNivel.setText("Nivel " + u.getNivel());
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo, tvRacha, tvNivel;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuarioAdmin);
            tvCorreo = itemView.findViewById(R.id.tvCorreoUsuarioAdmin);
            tvRacha = itemView.findViewById(R.id.tvRachaUsuarioAdmin);
            tvNivel = itemView.findViewById(R.id.tvNivelUsuarioAdmin);
        }
    }
}
