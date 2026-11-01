package com.itsoeh.sbsc.buti.cultura;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.itsoeh.sbsc.buti.R;

public class CulturaFragment extends Fragment {

    public CulturaFragment() {
        super(R.layout.fragment_cultura);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Pueblos
        view.findViewById(R.id.cardPuebloDexthi).setOnClickListener(v ->
                mostrarDetalle("El Dexthi", "Tradición textil",
                        "Comunidad de Ixmiquilpan, Hidalgo. Se caracteriza por su clima semiseco, su flora y fauna adaptadas a la región, y por conservar tradiciones y costumbres locales.",
                        R.drawable.milpa_atardecer_nube));

        view.findViewById(R.id.cardPuebloNith).setOnClickListener(v ->
                mostrarDetalle("El Nith", "Idioma vivo",
                        "El Nith destaca por su paisaje de lomeríos, clima seco semicálido y ubicación dentro de las cuencas de los ríos Moctezuma y Tula, siendo una de las localidades importantes de Ixmiquilpan.",
                        R.drawable.nith_casas_ladera));

        view.findViewById(R.id.cardPuebloPanales).setOnClickListener(v ->
                mostrarDetalle("Panales", "Gastronomía local",
                        "Comunidad de Ixmiquilpan con rica tradición gastronómica y artesanal. Se caracteriza por sus fiestas comunitarias y arraigo de la cultura hñáhñu.",
                        R.drawable.panales_valle_agricola));

        // Temas distintivos
        view.findViewById(R.id.cardTemaMaguey).setOnClickListener(v ->
                mostrarDetalle("El maguey", "Lo que la distingue",
                        "El maguey acompaña la vida en el Valle del Mezquital. De la planta se obtiene el aguamiel, con el que se prepara el pulque, y la fibra de ixtle, que se usa para tejer.",
                        R.drawable.cerro_sendero_maguey));

        view.findViewById(R.id.cardTemaBordados).setOnClickListener(v ->
                mostrarDetalle("Bordados hñähñu", "Lo que la distingue",
                        "Los bordados hñahñu se hacen a mano, puntada por puntada. Los motivos más conocidos son las aves, las flores y los rombos. Cada figura cuenta una historia.",
                        R.drawable.cielo_atardecer_naranja));

        view.findViewById(R.id.cardTemaGastronomia).setOnClickListener(v ->
                mostrarDetalle("Gastronomía", "Lo que la distingue",
                        "La cocina de Ixmiquilpan se prueba en platillos como la barbacoa de hoyo, los mixiotes, los escamoles y los gusanos de maguey acompañados con tortillas hechas a mano.",
                        R.drawable.ixmiquilpan_tianguis_lonas));

        view.findViewById(R.id.cardTemaSanMiguel).setOnClickListener(v ->
                mostrarDetalle("San Miguel Arcángel", "Lo que la distingue",
                        "El templo y exconvento de San Miguel Arcángel es del siglo XVI. Dentro se conservan murales con guerreros indígenas y es el centro de la fiesta patronal a finales de septiembre.",
                        R.drawable.ixmiquilpan_templo_fachada));
    }

    private void mostrarDetalle(String titulo, String categoria, String descripcion,
                                @DrawableRes int foto) {
        if (getContext() == null) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);   // debe ir antes de setContentView
        dialog.setContentView(R.layout.dialog_detalle_cultura);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Fondo transparente con 24 px de margen por lado (sube o baja el 24 a tu gusto)
            dialog.getWindow().setBackgroundDrawable(
                    new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), 24));
        }

        TextView txtTitulo = dialog.findViewById(R.id.txtTituloDetalleCultura);
        TextView txtCategoria = dialog.findViewById(R.id.txtCategoriaDetalleCultura);
        TextView txtDescripcion = dialog.findViewById(R.id.txtDescripcionDetalleCultura);
        ImageView imgFoto = dialog.findViewById(R.id.imgDetalleCultura);
        ImageView btnCerrar = dialog.findViewById(R.id.btnCerrarDetalleCultura);
        Button btnEscuchar = dialog.findViewById(R.id.btnEscucharOtomi);

        txtTitulo.setText(titulo);
        txtCategoria.setText(categoria);
        txtDescripcion.setText(descripcion);
        imgFoto.setImageResource(foto);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnEscuchar.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Reproduciendo pronunciación en otomí...", Toast.LENGTH_SHORT).show());

        dialog.show();
    }
}