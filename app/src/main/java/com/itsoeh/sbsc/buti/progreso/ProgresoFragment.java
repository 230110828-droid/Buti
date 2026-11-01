package com.itsoeh.sbsc.buti.progreso;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.RachaMaguey;

public class ProgresoFragment extends Fragment {

    public ProgresoFragment() {
        super(R.layout.fragment_progreso);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtNombre = view.findViewById(R.id.txtNombreProgreso);
        TextView txtIniciales = view.findViewById(R.id.txtInicialesProgreso);
        TextView txtRacha = view.findViewById(R.id.txtRachaProgreso);
        ImageView imgRacha = view.findViewById(R.id.imgRachaMaguey);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            String name = user.getDisplayName();
            txtNombre.setText(name);

            String[] partes = name.trim().split("\\s+");
            if (partes.length >= 2) {
                txtIniciales.setText(("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase());
            } else if (name.length() > 0) {
                txtIniciales.setText(name.substring(0, Math.min(2, name.length())).toUpperCase());
            }
        }

        // Por ahora la racha es de muestra. Luego la cambias por el dato real.
        int racha = 12;
        txtRacha.setText(String.valueOf(racha));
        imgRacha.setImageResource(RachaMaguey.dibujoPorRacha(racha));
    }
}