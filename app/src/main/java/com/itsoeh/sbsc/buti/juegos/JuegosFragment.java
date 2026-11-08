package com.itsoeh.sbsc.buti.juegos;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.itsoeh.sbsc.buti.R;
import com.google.android.material.card.MaterialCardView;

public class JuegosFragment extends Fragment {

    private MaterialCardView cardMemorama;
    private MaterialCardView cardRompecabezas;
    private MaterialCardView cardAtrapaPalabras;
    private MaterialCardView cardDiferencias;

    public JuegosFragment() {
        super(R.layout.fragment_juegos);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        cardMemorama = view.findViewById(R.id.cardMemorama);
        cardRompecabezas = view.findViewById(R.id.cardRompecabezas);
        cardAtrapaPalabras = view.findViewById(R.id.cardAtrapaPalabras);
        cardDiferencias = view.findViewById(R.id.cardDiferencias);

        cardMemorama.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.memoramaFragment));

        cardRompecabezas.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.rompecabezasFragment));

        cardAtrapaPalabras.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.atrapaPalabraFragment));

        cardDiferencias.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.diferenciasFragment));
    }
}
