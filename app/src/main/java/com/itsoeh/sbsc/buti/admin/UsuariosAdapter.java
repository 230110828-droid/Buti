package com.itsoeh.sbsc.buti.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itsoeh.sbsc.buti.R;

import java.util.ArrayList;
import java.util.List;

/** Lista de estudiantes del panel de Admin. */
public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    public interface AlTocar {
        void abrir(UsuarioAdmin usuario);
    }

    private final List<UsuarioAdmin> usuarios = new ArrayList<>();
    private final AlTocar alTocar;

    public UsuariosAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void mostrar(List<UsuarioAdmin> nuevos) {
        usuarios.clear();
        usuarios.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_admin, parent, false);
        return new UsuarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {

        UsuarioAdmin u = usuarios.get(position);

        holder.tvNombre.setText(u.nombre);
        holder.tvCorreo.setText(u.correo);
        holder.tvPuntos.setText(u.puntos + " pts");
        holder.tvRacha.setText("Racha " + u.racha);
        holder.tvNivel.setText("Nivel " + u.nivelActual);

        u.ponerAvatar(holder.imgFoto, holder.txtIniciales);

        holder.itemView.setContentDescription(u.nombre + ", " + u.puntos + " puntos, racha de "
                + u.racha + " días, nivel " + u.nivelActual);

        holder.itemView.setOnClickListener(v -> alTocar.abrir(u));
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        final ImageView imgFoto;
        final TextView txtIniciales, tvNombre, tvCorreo, tvPuntos, tvRacha, tvNivel;

        UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgFotoUsuarioAdmin);
            txtIniciales = itemView.findViewById(R.id.txtInicialesUsuarioAdmin);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuarioAdmin);
            tvCorreo = itemView.findViewById(R.id.tvCorreoUsuarioAdmin);
            tvPuntos = itemView.findViewById(R.id.tvPuntosUsuarioAdmin);
            tvRacha = itemView.findViewById(R.id.tvRachaUsuarioAdmin);
            tvNivel = itemView.findViewById(R.id.tvNivelUsuarioAdmin);
        }
    }
}