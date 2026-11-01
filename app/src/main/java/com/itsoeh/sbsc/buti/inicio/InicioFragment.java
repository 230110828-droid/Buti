package com.itsoeh.sbsc.buti.inicio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.itsoeh.sbsc.buti.R;

public class InicioFragment extends Fragment {

    public InicioFragment() {
        super(R.layout.fragment_inicio);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtNombreUsuario = view.findViewById(R.id.txtNombreUsuarioInicio);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            txtNombreUsuario.setText("Hola, " + currentUser.getDisplayName());
        }

        NavController navController = Navigation.findNavController(view);

        Button btnContinuarLeccion = view.findViewById(R.id.btnContinuarLeccion);
        btnContinuarLeccion.setOnClickListener(v -> navController.navigate(R.id.practicarFragment));

        View cardIxmiquilpan = view.findViewById(R.id.cardNavIxmiquilpan);
        cardIxmiquilpan.setOnClickListener(v -> navController.navigate(R.id.culturaFragment));

        View cardVocabulario = view.findViewById(R.id.cardNavVocabulario);
        cardVocabulario.setOnClickListener(v -> navController.navigate(R.id.vocabularioFragment));

        View cardPracticar = view.findViewById(R.id.cardNavPracticar);
        cardPracticar.setOnClickListener(v -> navController.navigate(R.id.practicarFragment));

        View cardProgreso = view.findViewById(R.id.cardNavProgreso);
        cardProgreso.setOnClickListener(v -> navController.navigate(R.id.progresoFragment));
    }
}