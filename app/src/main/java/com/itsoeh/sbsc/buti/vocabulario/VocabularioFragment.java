package com.itsoeh.sbsc.buti.vocabulario;

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.itsoeh.sbsc.buti.R;

public class VocabularioFragment extends Fragment {

    private boolean masCategoriasVisibles = false;

    public VocabularioFragment() {
        super(R.layout.fragment_vocabulario);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        TextView btnVerMas =
                view.findViewById(R.id.btnVerMasCategorias);

        TextView tituloGrupo2 =
                view.findViewById(R.id.txtTituloGrupo2);

        GridLayout gridGrupo2 =
                view.findViewById(R.id.gridGrupo2);

        // Mostrar u ocultar las categorías adicionales.
        if (btnVerMas != null
                && tituloGrupo2 != null
                && gridGrupo2 != null) {

            btnVerMas.setOnClickListener(v -> {

                masCategoriasVisibles = !masCategoriasVisibles;

                int visibilidad = masCategoriasVisibles
                        ? View.VISIBLE
                        : View.GONE;

                tituloGrupo2.setVisibility(visibilidad);
                gridGrupo2.setVisibility(visibilidad);

                // Texto sin flechas: la flecha ahora es un icono real.
                btnVerMas.setText(
                        masCategoriasVisibles
                                ? "Ver menos categorías"
                                : "Ver más categorías"
                );

                btnVerMas.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        masCategoriasVisibles
                                ? R.drawable.ic_contraer
                                : R.drawable.ic_expandir,
                        0
                );
            });
        }

        // Categorías principales.
        configurarTarjeta(view, R.id.cardCatColores, "Colores");
        configurarTarjeta(view, R.id.cardCatNumeros, "Números");
        configurarTarjeta(view, R.id.cardCatSaludos, "Saludos");
        configurarTarjeta(view, R.id.cardCatTiempo, "Tiempo");

        // Categorías adicionales.
        configurarTarjeta(view, R.id.cardCatPlantas, "Plantas");
        configurarTarjeta(view, R.id.cardCatAnimales, "Animales");
        configurarTarjeta(view, R.id.cardCatObjetos, "Objetos");
        configurarTarjeta(view, R.id.cardCatCuerpo, "Cuerpo");
        configurarTarjeta(view, R.id.cardCatComida, "Comida");
        configurarTarjeta(view, R.id.cardCatNaturaleza, "Naturaleza");
        configurarTarjeta(view, R.id.cardCatLugares, "Lugares");
    }

    /**
     * Abre la pantalla de palabras de la categoría seleccionada.
     */
    private void configurarTarjeta(
            View view,
            int idTarjeta,
            String categoria
    ) {

        MaterialCardView tarjeta =
                view.findViewById(idTarjeta);

        if (tarjeta == null) {
            return;
        }

        tarjeta.setOnClickListener(v -> {

            Bundle argumentos = new Bundle();

            argumentos.putString("temaSeleccionado", categoria);
            argumentos.putString("categoriaSeleccionada", categoria);

            NavController navController =
                    Navigation.findNavController(v);

            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId()
                    == R.id.vocabularioFragment) {

                navController.navigate(
                        R.id.palabrasVocabularioFragment,
                        argumentos
                );
            }
        });
    }
}