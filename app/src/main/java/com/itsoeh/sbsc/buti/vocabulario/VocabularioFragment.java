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

        // Referencias a los elementos para mostrar más categorías.
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

                btnVerMas.setText(
                        masCategoriasVisibles
                                ? "Ver menos categorías ↑"
                                : "Ver más categorías ↓"
                );
            });
        }

        // Categorías principales.
        configurarTarjeta(
                view,
                R.id.cardCatColores,
                "Colores"
        );

        configurarTarjeta(
                view,
                R.id.cardCatNumeros,
                "Números"
        );

        configurarTarjeta(
                view,
                R.id.cardCatSaludos,
                "Saludos"
        );

        configurarTarjeta(
                view,
                R.id.cardCatTiempo,
                "Tiempo"
        );

        // Categorías adicionales.
        configurarTarjeta(
                view,
                R.id.cardCatPlantas,
                "Plantas"
        );

        configurarTarjeta(
                view,
                R.id.cardCatAnimales,
                "Animales"
        );

        configurarTarjeta(
                view,
                R.id.cardCatObjetos,
                "Objetos"
        );

        configurarTarjeta(
                view,
                R.id.cardCatCuerpo,
                "Cuerpo"
        );

        configurarTarjeta(
                view,
                R.id.cardCatComida,
                "Comida"
        );

        configurarTarjeta(
                view,
                R.id.cardCatNaturaleza,
                "Naturaleza"
        );

        configurarTarjeta(
                view,
                R.id.cardCatLugares,
                "Lugares"
        );
    }

    /**
     * Configura una tarjeta para abrir el cuestionario
     * con la categoría que seleccionó el usuario.
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

            // Enviar la categoría seleccionada.
            Bundle argumentos = new Bundle();
            argumentos.putString(
                    "temaSeleccionado",
                    categoria
            );

            // Obtener el controlador de navegación.
            NavController navController =
                    Navigation.findNavController(v);

            // Evitar navegar si el destino no existe
            // desde el destino actual.
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination()
                    .getId() == R.id.vocabularioFragment) {

                navController.navigate(
                        R.id.practicarFragment,
                        argumentos
                );
            }
        });
    }
}
