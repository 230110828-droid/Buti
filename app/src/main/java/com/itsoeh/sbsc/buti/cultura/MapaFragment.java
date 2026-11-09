package com.itsoeh.sbsc.buti.cultura;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapa de Ixmiquilpan dentro de la app (OpenStreetMap, gratis y sin clave).
 * Muestra Ixmiquilpan, El Dexthi, El Nith y Panales.
 * Al tocar un lugar aparece su información y el botón "Cómo llegar".
 */
public class MapaFragment extends Fragment {

    // ===== Lugares =====
    // Coordenadas aproximadas del centro de cada localidad.
    private final String[] nombres = {
            "Ixmiquilpan",
            "El Dexthi",
            "El Nith",
            "Panales"
    };

    private final String[] tipos = {
            "MUNICIPIO",
            "COMUNIDAD · TRADICIÓN TEXTIL",
            "COMUNIDAD · ARTESANÍA EN MADERA",
            "COMUNIDAD · ORGANIZACIÓN COMUNITARIA"
    };

    private final String[] descripciones = {
            "Corazón del Valle del Mezquital y hogar de comunidades hñähñu que conservan su lengua, sus artesanías y sus tradiciones.",
            "El Dexthi San Juanico conserva conocimientos artesanales del maguey y la elaboración de productos de ixtle.",
            "Reconocido por su trabajo artesanal con madera de enebro y concha de abulón.",
            "Comunidad donde la organización comunitaria y la fiesta de San Miguel Arcángel forman parte de su identidad."
    };

    private final double[][] coordenadas = {
            {20.48194, -99.21806},  // Ixmiquilpan (centro)
            {20.56800, -99.21800},  // El Dexthi San Juanico (aprox.)
            {20.48909, -99.19407},  // El Nith
            {20.46899, -99.26771}   // Panales
    };

    private static final double ZOOM_GENERAL = 11.6;
    private static final double ZOOM_LUGAR = 14.5;

    // ===== Vistas =====
    private MapView mapa;
    private TextView txtTipo, txtNombre, txtDescripcion;
    private LinearLayout contenedorLugares;

    private final List<Marker> marcadores = new ArrayList<>();
    private final List<TextView> botonesLugar = new ArrayList<>();

    private int lugarSeleccionado = 0;

    public MapaFragment() {
        super(R.layout.fragment_mapa);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de osmdroid: identificar la app y guardar
        // los mapas descargados en la carpeta interna (sin permisos).
        Context contexto = requireContext().getApplicationContext();

        Configuration.getInstance().setUserAgentValue(contexto.getPackageName());

        File base = new File(contexto.getCacheDir(), "osmdroid");
        Configuration.getInstance().setOsmdroidBasePath(base);
        Configuration.getInstance().setOsmdroidTileCache(new File(base, "tiles"));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapa = view.findViewById(R.id.mapaIxmiquilpan);
        txtTipo = view.findViewById(R.id.txtTipoLugarMapa);
        txtNombre = view.findViewById(R.id.txtNombreLugarMapa);
        txtDescripcion = view.findViewById(R.id.txtDescripcionLugarMapa);
        contenedorLugares = view.findViewById(R.id.contenedorLugaresMapa);

        MaterialButton btnComoLlegar = view.findViewById(R.id.btnComoLlegarMapa);

        view.findViewById(R.id.btnVolverMapa).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        configurarMapa();
        crearMarcadores();
        crearBotonesLugares();

        btnComoLlegar.setOnClickListener(v -> abrirComoLlegar());

        // Vista inicial: los cuatro lugares a la vez.
        seleccionarLugar(0, false);
        mapa.getController().setZoom(ZOOM_GENERAL);
        mapa.getController().setCenter(new GeoPoint(20.5150, -99.2300));
    }

    // ===== Mapa =====

    private void configurarMapa() {

        mapa.setTileSource(TileSourceFactory.MAPNIK);
        mapa.setMultiTouchControls(true);   // zoom con dos dedos
        mapa.setTilesScaledToDpi(true);     // texto del mapa legible
        mapa.setMinZoomLevel(9.0);
        mapa.setMaxZoomLevel(18.0);

        // Modo oscuro: el mapa también se oscurece para que no deslumbre.
        int modo = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if (modo == android.content.res.Configuration.UI_MODE_NIGHT_YES) {

            // Invierte los colores del mapa (fondo oscuro, calles claras).
            android.graphics.ColorMatrix inversion = new android.graphics.ColorMatrix(new float[]{
                    -1f,  0f,  0f, 0f, 255f,
                    0f, -1f,  0f, 0f, 255f,
                    0f,  0f, -1f, 0f, 255f,
                    0f,  0f,  0f, 1f,   0f
            });

            mapa.getOverlayManager().getTilesOverlay().setColorFilter(
                    new android.graphics.ColorMatrixColorFilter(inversion));
        }

        // Ocultar los botones +/- (se usa el zoom con los dedos)
        mapa.getZoomController().setVisibility(
                org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER);
    }

    private void crearMarcadores() {

        for (int i = 0; i < nombres.length; i++) {

            final int indice = i;

            Marker marcador = new Marker(mapa);
            marcador.setPosition(new GeoPoint(coordenadas[i][0], coordenadas[i][1]));
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marcador.setTitle(nombres[i]);
            marcador.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_mapa));

            marcador.setOnMarkerClickListener((m, mapView) -> {
                seleccionarLugar(indice, true);
                return true;
            });

            mapa.getOverlays().add(marcador);
            marcadores.add(marcador);
        }
    }

    // Botones tipo "chip" para saltar a cada lugar.
    private void crearBotonesLugares() {

        float densidad = getResources().getDisplayMetrics().density;

        for (int i = 0; i < nombres.length; i++) {

            final int indice = i;

            TextView chip = new TextView(requireContext());
            chip.setText(nombres[i]);
            chip.setTextSize(13);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setPadding((int) (16 * densidad), (int) (9 * densidad),
                    (int) (16 * densidad), (int) (9 * densidad));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd((int) (8 * densidad));
            chip.setLayoutParams(params);
            chip.setElevation(3 * densidad);

            chip.setOnClickListener(v -> seleccionarLugar(indice, true));

            contenedorLugares.addView(chip);
            botonesLugar.add(chip);
        }
    }

    private void seleccionarLugar(int indice, boolean animar) {

        lugarSeleccionado = indice;

        txtTipo.setText(tipos[indice]);
        txtNombre.setText(nombres[indice]);
        txtDescripcion.setText(descripciones[indice]);

        GeoPoint punto = new GeoPoint(coordenadas[indice][0], coordenadas[indice][1]);

        if (animar) {
            mapa.getController().animateTo(punto, ZOOM_LUGAR, 800L);
        } else {
            mapa.getController().setCenter(punto);
        }

        actualizarBotones();
    }

    private void actualizarBotones() {

        float densidad = getResources().getDisplayMetrics().density;

        // Colores que cambian solos en modo oscuro.
        int fondoActivo = ContextCompat.getColor(requireContext(), R.color.buti_acento);
        int textoActivo = ContextCompat.getColor(requireContext(), R.color.buti_sobre_acento);
        int fondoNormal = ContextCompat.getColor(requireContext(), R.color.buti_superficie);
        int textoNormal = ContextCompat.getColor(requireContext(), R.color.buti_titulo);
        int dorado = ContextCompat.getColor(requireContext(), R.color.buti_dorado);

        for (int i = 0; i < botonesLugar.size(); i++) {

            TextView chip = botonesLugar.get(i);
            boolean activo = i == lugarSeleccionado;

            android.graphics.drawable.GradientDrawable fondo =
                    new android.graphics.drawable.GradientDrawable();
            fondo.setCornerRadius(20 * densidad);
            fondo.setColor(activo ? fondoActivo : fondoNormal);
            fondo.setStroke((int) ((activo ? 2 : 1) * densidad), dorado);

            chip.setBackground(fondo);
            chip.setTextColor(activo ? textoActivo : textoNormal);
        }
    }

    // ===== Cómo llegar (Google Maps) =====

    private void abrirComoLlegar() {

        double lat = coordenadas[lugarSeleccionado][0];
        double lng = coordenadas[lugarSeleccionado][1];

        Uri ruta = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                + lat + "," + lng);

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, ruta));
        } catch (ActivityNotFoundException e) {
            ButiAviso.mostrar(
                    this,
                    "No se encontró una app para abrir el mapa.",
                    ButiAviso.Tipo.ERROR
            );
        }
    }

    // ===== Ciclo de vida que osmdroid necesita =====

    @Override
    public void onResume() {
        super.onResume();
        if (mapa != null) mapa.onResume();
    }

    @Override
    public void onPause() {
        if (mapa != null) mapa.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mapa != null) mapa.onDetach();
        marcadores.clear();
        botonesLugar.clear();
        mapa = null;
        super.onDestroyView();
    }
}