package com.itsoeh.sbsc.buti.lecciones;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import androidx.core.os.BundleCompat;

import com.itsoeh.sbsc.buti.R;

public class LeccionesFragment extends Fragment {

    public LeccionesFragment() {
        super(R.layout.fragment_lecciones);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController =
                Navigation.findNavController(view);

        setupNivelCard(
                view.findViewById(R.id.cardNivel1),
                "Nivel 1",
                "Saludos",
                navController
        );

        setupNivelCard(
                view.findViewById(R.id.cardNivel2),
                "Nivel 2",
                "Números",
                navController
        );

        setupNivelCard(
                view.findViewById(R.id.cardNivel3),
                "Nivel 3",
                "Familia",
                navController
        );

        setupNivelCard(
                view.findViewById(R.id.cardNivel4),
                "Nivel 4",
                "Colores",
                navController
        );

        setupNivelCard(
                view.findViewById(R.id.cardNivel5),
                "Nivel 5",
                "Animales",
                navController
        );
    }

    private void setupNivelCard(
            View cardView,
            String nivel,
            String titulo,
            NavController navController
    ) {
        if (cardView == null) {
            return;
        }

        cardView.setOnClickListener(v -> {

            if (!isAdded()) {
                return;
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle(nivel + " · " + titulo)
                    .setMessage(
                            "Tema: " + titulo
                                    + "\n\n10 preguntas"
                                    + "\n\nGanarás hasta 60 puntos"
                    )
                    .setPositiveButton("Empezar", (dialog, which) -> {

                        Bundle argumentos = new Bundle();
                        argumentos.putString("temaSeleccionado", titulo);
                        argumentos.putString("nivelSeleccionado", nivel);

                        navController.navigate(
                                R.id.practicarFragment,
                                argumentos
                        );
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}
