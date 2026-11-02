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

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    public InicioFragment() {
        super(R.layout.fragment_inicio);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // -----------------------------------------
        // SALUDO DEL USUARIO
        // -----------------------------------------

        TextView txtNombreUsuario =
                view.findViewById(R.id.txtNombreUsuarioInicio);

        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null
                && currentUser.getDisplayName() != null
                && !currentUser.getDisplayName().trim().isEmpty()) {

            txtNombreUsuario.setText(
                    "Hola, " + currentUser.getDisplayName()
            );

        }

        // -----------------------------------------
        // CONTROLADOR DE NAVEGACIÓN
        // -----------------------------------------

        NavController navController =
                Navigation.findNavController(view);

        // -----------------------------------------
        // BOTÓN CONTINUAR LECCIÓN
        // -----------------------------------------

        Button btnContinuarLeccion =
                view.findViewById(R.id.btnContinuarLeccion);

        btnContinuarLeccion.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(navController, R.id.practicarFragment)
                )
        );

        // -----------------------------------------
        // TARJETA IXMIQUILPAN
        // -----------------------------------------

        View cardIxmiquilpan =
                view.findViewById(R.id.cardNavIxmiquilpan);

        cardIxmiquilpan.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(navController, R.id.culturaFragment)
                )
        );

        // -----------------------------------------
        // TARJETA VOCABULARIO
        // -----------------------------------------

        View cardVocabulario =
                view.findViewById(R.id.cardNavVocabulario);

        cardVocabulario.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(navController, R.id.vocabularioFragment)
                )
        );

        // -----------------------------------------
        // TARJETA PRACTICAR
        // -----------------------------------------

        View cardPracticar =
                view.findViewById(R.id.cardNavPracticar);

        cardPracticar.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(navController, R.id.practicarFragment)
                )
        );

        // -----------------------------------------
        // TARJETA MI PROGRESO
        // -----------------------------------------

        View cardProgreso =
                view.findViewById(R.id.cardNavProgreso);

        cardProgreso.setOnClickListener(v ->
                animarYCambiar(
                        v,
                        () -> navegar(navController, R.id.progresoFragment)
                )
        );

        // -----------------------------------------
        // ANIMACIONES DE ENTRADA
        // -----------------------------------------

        configurarAnimacionesEntrada(view);
    }

    // ---------------------------------------------
    // ANIMACIÓN AL ENTRAR A INICIO
    // ---------------------------------------------

    private void configurarAnimacionesEntrada(View root) {

        List<View> elementos = new ArrayList<>();

        elementos.add(
                root.findViewById(R.id.txtNombreUsuarioInicio)
        );

        elementos.add(
                root.findViewById(R.id.btnContinuarLeccion)
        );

        elementos.add(
                root.findViewById(R.id.cardNavIxmiquilpan)
        );

        elementos.add(
                root.findViewById(R.id.cardNavVocabulario)
        );

        elementos.add(
                root.findViewById(R.id.cardNavPracticar)
        );

        elementos.add(
                root.findViewById(R.id.cardNavProgreso)
        );

        long retraso = 0;

        for (View elemento : elementos) {

            if (elemento == null) continue;

            elemento.setAlpha(0f);
            elemento.setTranslationY(22f);
            elemento.setScaleX(0.97f);
            elemento.setScaleY(0.97f);

            elemento.post(() -> {

                elemento.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(430)
                        .setStartDelay(
                                elementos.indexOf(elemento) * 75L
                        )
                        .start();
            });

            retraso += 75;
        }
    }

    // ---------------------------------------------
    // ANIMACIÓN AL PULSAR UNA TARJETA
    // ---------------------------------------------

    private void animarYCambiar(
            View elemento,
            Runnable accion
    ) {

        elemento.animate().cancel();

        elemento.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(100)
                .withEndAction(() -> {

                    elemento.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(110)
                            .withEndAction(() -> {

                                if (isAdded()) {
                                    accion.run();
                                }

                            })
                            .start();

                })
                .start();
    }

    // ---------------------------------------------
    // NAVEGACIÓN SEGURA
    // ---------------------------------------------

    private void navegar(
            NavController navController,
            int destino
    ) {

        if (!isAdded()) return;

        if (navController.getCurrentDestination() == null) {
            return;
        }

        if (navController.getCurrentDestination().getId() != destino) {

            navController.navigate(destino);
        }
    }
}
