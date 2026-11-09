package com.itsoeh.sbsc.buti.configuracion;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.itsoeh.sbsc.buti.LectorVoz;
import com.itsoeh.sbsc.buti.PreferenciasButi;
import com.itsoeh.sbsc.buti.R;

/**
 * Configuración: tema (según el celular, claro u oscuro)
 * tamaño de letra (normal, grande o muy grande)
 * y accesibilidad (leer en voz alta y texto en negrita).
 */
public class ConfiguracionFragment extends Fragment {

    public ConfiguracionFragment() {
        super(R.layout.fragment_configuracion);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Funciona igual en la app y en el panel de administrador.
        view.findViewById(R.id.btnVolverConfiguracion)
                .setOnClickListener(v ->
                        requireActivity().getOnBackPressedDispatcher().onBackPressed());

        RadioGroup rgTema = view.findViewById(R.id.rgTemaConfiguracion);
        RadioGroup rgLetra = view.findViewById(R.id.rgLetraConfiguracion);

        // Iconos de las opciones de tema (tamaño fijo, sirven PNG o vector).
        ponerIcono(view.findViewById(R.id.rbTemaSistema), R.drawable.ic_solluna);
        ponerIcono(view.findViewById(R.id.rbTemaClaro), R.drawable.ic_tema_claro);
        ponerIcono(view.findViewById(R.id.rbTemaOscuro), R.drawable.ic_tema_oscuro);

        // Marcar lo que ya está guardado (antes de escuchar cambios).
        int tema = PreferenciasButi.obtenerTema(requireContext());

        rgTema.check(
                tema == PreferenciasButi.TEMA_CLARO ? R.id.rbTemaClaro
                        : tema == PreferenciasButi.TEMA_OSCURO ? R.id.rbTemaOscuro
                          : R.id.rbTemaSistema);

        int letra = PreferenciasButi.obtenerLetra(requireContext());

        rgLetra.check(
                letra == PreferenciasButi.LETRA_GRANDE ? R.id.rbLetraGrande
                        : letra == PreferenciasButi.LETRA_MUY_GRANDE ? R.id.rbLetraMuyGrande
                          : R.id.rbLetraNormal);

        // TEMA: se aplica al instante (Android redibuja la pantalla).
        rgTema.setOnCheckedChangeListener((grupo, id) -> {

            int nuevoTema;

            if (id == R.id.rbTemaClaro) {
                nuevoTema = PreferenciasButi.TEMA_CLARO;
            } else if (id == R.id.rbTemaOscuro) {
                nuevoTema = PreferenciasButi.TEMA_OSCURO;
            } else {
                nuevoTema = PreferenciasButi.TEMA_SISTEMA;
            }

            if (nuevoTema != PreferenciasButi.obtenerTema(requireContext())) {
                PreferenciasButi.guardarTema(requireContext(), nuevoTema);
            }
        });

        configurarAccesibilidad(view);

        // LETRA: se guarda y se vuelve a abrir la pantalla con el tamaño nuevo.
        rgLetra.setOnCheckedChangeListener((grupo, id) -> {

            int nuevaLetra;

            if (id == R.id.rbLetraGrande) {
                nuevaLetra = PreferenciasButi.LETRA_GRANDE;
            } else if (id == R.id.rbLetraMuyGrande) {
                nuevaLetra = PreferenciasButi.LETRA_MUY_GRANDE;
            } else {
                nuevaLetra = PreferenciasButi.LETRA_NORMAL;
            }

            if (nuevaLetra != PreferenciasButi.obtenerLetra(requireContext())) {
                PreferenciasButi.guardarLetra(requireContext(), nuevaLetra);
                requireActivity().recreate();
            }
        });
    }

    private void configurarAccesibilidad(View view) {

        SwitchMaterial swLector = view.findViewById(R.id.swLectorVoz);
        SwitchMaterial swNegrita = view.findViewById(R.id.swTextoNegrita);

        swLector.setChecked(PreferenciasButi.lectorActivo(requireContext()));
        swNegrita.setChecked(PreferenciasButi.negritaActiva(requireContext()));

        // LECTOR DE VOZ: se activa al instante y lo confirma en voz alta.
        swLector.setOnCheckedChangeListener((boton, activo) -> {

            PreferenciasButi.guardarLector(requireContext(), activo);

            if (activo) {
                LectorVoz.hablarSiempre(requireContext(),
                        "Lector de voz activado. Toca cualquier botón o texto para escucharlo.");
            } else {
                LectorVoz.detener();
            }
        });

        // TEXTO EN NEGRITA: se guarda y se vuelve a abrir la pantalla.
        swNegrita.setOnCheckedChangeListener((boton, activo) -> {

            if (activo != PreferenciasButi.negritaActiva(requireContext())) {
                PreferenciasButi.guardarNegrita(requireContext(), activo);
                requireActivity().recreate();
            }
        });
    }

    private void ponerIcono(RadioButton boton, @DrawableRes int icono) {

        if (boton == null) return;

        Drawable dibujo = ContextCompat.getDrawable(requireContext(), icono);

        if (dibujo == null) return;

        int lado = (int) (28 * getResources().getDisplayMetrics().density);
        dibujo.setBounds(0, 0, lado, lado);

        boton.setCompoundDrawablesRelative(dibujo, null, null, null);
    }
}