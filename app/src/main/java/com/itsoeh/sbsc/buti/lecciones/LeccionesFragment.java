package com.itsoeh.sbsc.buti.lecciones;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

        // Nivel 1
        setupNivelCard(view.findViewById(R.id.cardNivel1),
                1, "Saludos", navController);

        // Nivel 2
        setupNivelCard(view.findViewById(R.id.cardNivel2),
                2, "Números", navController);

        // Nivel 3
        setupNivelCard(view.findViewById(R.id.cardNivel3),
                3, "Colores", navController);

        // Nivel 4
        setupNivelCard(view.findViewById(R.id.cardNivel4),
                4, "Animales", navController);

        // Nivel 5
        setupNivelCard(view.findViewById(R.id.cardNivel5),
                5, "Plantas", navController);

        // Nivel 6
        setupNivelCard(view.findViewById(R.id.cardNivel6),
                6, "Objetos", navController);

        // Nivel 7
        setupNivelCard(view.findViewById(R.id.cardNivel7),
                7, "Cuerpo", navController);

        // Nivel 8
        setupNivelCard(view.findViewById(R.id.cardNivel8),
                8, "Comida", navController);

        // Nivel 9
        setupNivelCard(view.findViewById(R.id.cardNivel9),
                9, "Tiempo y celebraciones", navController);

        // Nivel 10
        setupNivelCard(view.findViewById(R.id.cardNivel10),
                10, "Naturaleza", navController);

        // Nivel 11
        setupNivelCard(view.findViewById(R.id.cardNivel11),
                11, "Lugares", navController);
    }

    private void setupNivelCard(
            View cardView,
            int numeroNivel,
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
                    .setTitle("Nivel " + numeroNivel + " · " + titulo)
                    .setMessage(
                            "Tema: " + titulo
                                    + "\n\n5 etapas de aprendizaje"
                                    + "\n\nCompleta los desafíos para avanzar."
                    )
                    .setPositiveButton("Empezar", (dialog, which) -> {

                        Bundle argumentos = new Bundle();

                        argumentos.putInt(
                                "numeroNivel",
                                numeroNivel
                        );

                        argumentos.putString(
                                "nivelSeleccionado",
                                titulo
                        );

                        argumentos.putString(
                                "temaSeleccionado",
                                titulo
                        );

                        if (navController.getCurrentDestination() != null
                                && navController.getCurrentDestination().getId()
                                == R.id.leccionesFragment) {

                            navController.navigate(
                                    R.id.etapasLeccionFragment,
                                    argumentos
                            );
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}
