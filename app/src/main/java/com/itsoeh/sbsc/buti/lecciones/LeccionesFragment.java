package com.itsoeh.sbsc.buti.lecciones;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.ButiDialogo;
import com.itsoeh.sbsc.buti.ProgresoButi;
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

        // Puntos totales del usuario (lecciones + práctica + juegos).
        cargarPuntos(view.findViewById(R.id.txtPuntosLecciones));
    }

    private void cargarPuntos(TextView txtPuntos) {

        if (txtPuntos == null) return;

        txtPuntos.setText("0");

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!isAdded() || getView() == null) return;

                        txtPuntos.setText(String.valueOf(
                                ProgresoButi.puntosTotales(snapshot)
                        ));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
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

            new ButiDialogo(requireContext()).mostrarConfirmacion(
                    "Nivel " + numeroNivel + " · " + titulo,
                    "5 etapas de aprendizaje.\n\n"
                            + "Completa los desafíos para avanzar "
                            + "en tu camino hñähñu.",
                    "Empezar",
                    "Ahora no",
                    () -> {

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
                    },
                    null
            );
        });
    }
}