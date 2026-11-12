package com.itsoeh.sbsc.buti.cultura;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.itsoeh.sbsc.buti.R;

public class CulturaFragment extends Fragment {

    public CulturaFragment() {
        super(R.layout.fragment_cultura);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ANIMACIONES DE ENTRADA
        animarEntrada(view.findViewById(R.id.cardExploraIxmiquilpan), 60);
        animarEntrada(view.findViewById(R.id.cardPuebloDexthi), 150);
        animarEntrada(view.findViewById(R.id.cardPuebloNith), 280);
        animarEntrada(view.findViewById(R.id.cardPuebloPanales), 410);

        animarEntrada(view.findViewById(R.id.cardTemaMaguey), 540);
        animarEntrada(view.findViewById(R.id.cardTemaBordados), 650);
        animarEntrada(view.findViewById(R.id.cardTemaGastronomia), 760);
        animarEntrada(view.findViewById(R.id.cardTemaSanMiguel), 870);

        // PUEBLO: EL DEXTHI
        view.findViewById(R.id.cardPuebloDexthi).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "El Dexthi",
                    "Tradición artesanal",
                    "El Dexthi San Juanico es una comunidad de Ixmiquilpan que conserva conocimientos artesanales relacionados con el aprovechamiento del maguey y la elaboración de productos de ixtle.",
                    R.drawable.milpa_atardecer_nube
            );
        });

        // PUEBLO: EL NITH
        view.findViewById(R.id.cardPuebloNith).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "El Nith",
                    "Artesanía en madera",
                    "El Nith destaca por su trabajo artesanal con madera de enebro y concha de abulón. Sus artesanos elaboran piezas decorativas mediante técnicas de tallado e incrustación.",
                    R.drawable.nith_casas_ladera
            );
        });

        // PUEBLO: PANALES
        view.findViewById(R.id.cardPuebloPanales).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "Panales",
                    "Organización comunitaria",
                    "Panales es una comunidad de Ixmiquilpan donde las celebraciones religiosas y la participación de sus habitantes forman parte de su identidad. Destaca su fiesta patronal en honor a San Miguel Arcángel durante septiembre.",
                    R.drawable.panales_valle_agricola
            );
        });

        // TEMA: MAGUEY
        view.findViewById(R.id.cardTemaMaguey).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "El maguey",
                    "Aprovechamiento tradicional",
                    "El maguey acompaña la vida en el Valle del Mezquital. De esta planta se obtiene el aguamiel, con el que se prepara el pulque, y la fibra de ixtle, que se utiliza para elaborar artesanías y tejidos.",
                    R.drawable.cerro_sendero_maguey
            );
        });

        // TEMA: BORDADOS
        view.findViewById(R.id.cardTemaBordados).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "Bordados hñähñu",
                    "Arte e identidad",
                    "Los bordados hñähñu son una expresión artesanal realizada a mano. Sus diseños incluyen aves, flores, figuras geométricas y elementos de la naturaleza que representan parte de la identidad cultural de la región.",
                    R.drawable.cielo_atardecer_naranja
            );
        });

        // TEMA: GASTRONOMÍA
        view.findViewById(R.id.cardTemaGastronomia).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "Gastronomía",
                    "Sabores del Valle",
                    "La cocina de Ixmiquilpan refleja los ingredientes y conocimientos tradicionales de la región. Entre sus preparaciones destacan la barbacoa, los mixiotes, las gorditas de quelites, los escamoles y los gusanos de maguey.",
                    R.drawable.ixmiquilpan_tianguis_lonas
            );
        });

        // TEMA: SAN MIGUEL
        view.findViewById(R.id.cardTemaSanMiguel).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "San Miguel Arcángel",
                    "Patrimonio histórico",
                    "El templo y exconvento de San Miguel Arcángel es uno de los monumentos históricos más representativos de Ixmiquilpan. Su construcción data del siglo XVI y conserva murales de gran valor histórico y artístico.",
                    R.drawable.ixmiquilpan_templo_fachada
            );
        });

        // UBICACIÓN: EXPLORA IXMIQUILPAN
        view.findViewById(R.id.cardExploraIxmiquilpan).setOnClickListener(v -> {
            animarToque(v);

            mostrarDetalle(
                    "Ixmiquilpan",
                    "Corazón del Valle del Mezquital",
                    "Ixmiquilpan es un municipio del estado de Hidalgo y uno de los centros más importantes del Valle del Mezquital. Es hogar de comunidades hñähñu que conservan su lengua, sus artesanías y sus tradiciones, como El Dexthi, El Nith y Panales.",
                    R.drawable.ixmiquilpan_templo_fachada
            );
        });

        // PIN: ABRIR EL MAPA DENTRO DE LA APP
        view.findViewById(R.id.btnMapaIxmiquilpan).setOnClickListener(v -> {
            animarToque(v);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.mapaFragment);
        });
    }

    // ANIMACIÓN DE ENTRADA
    private void animarEntrada(View vista, long retraso) {
        if (vista == null) return;

        vista.setAlpha(0f);
        vista.setTranslationY(35f);

        vista.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(retraso)
                .setDuration(550)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    // ANIMACIÓN AL TOCAR UNA TARJETA
    private void animarToque(View vista) {
        if (vista == null) return;

        vista.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(100)
                .withEndAction(() ->
                        vista.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start()
                )
                .start();
    }

    // MOSTRAR DETALLE DE CADA COMUNIDAD O TEMA
    private void mostrarDetalle(
            String titulo,
            String categoria,
            String descripcion,
            @DrawableRes int foto
    ) {

        if (!isAdded()) return;

        Dialog dialog = new Dialog(requireContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_detalle_cultura);
        dialog.setCancelable(true);

        // REFERENCIAS DE LOS ELEMENTOS
        TextView txtTitulo = dialog.findViewById(R.id.txtTituloDetalleCultura);
        TextView txtCategoria = dialog.findViewById(R.id.txtCategoriaDetalleCultura);
        TextView txtDescripcion = dialog.findViewById(R.id.txtDescripcionDetalleCultura);

        ImageView imgFoto = dialog.findViewById(R.id.imgDetalleCultura);
        ImageView btnCerrar = dialog.findViewById(R.id.btnCerrarDetalleCultura);

        // COLOCAR INFORMACIÓN
        if (txtTitulo != null) txtTitulo.setText(titulo);
        if (txtCategoria != null) txtCategoria.setText(categoria);
        if (txtDescripcion != null) txtDescripcion.setText(descripcion);
        if (imgFoto != null) imgFoto.setImageResource(foto);

        // CERRAR DETALLES
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        // REFERENCIAS DE LOS BOTONES INFORMATIVOS
        TextView tagCultura = dialog.findViewById(R.id.tagCultura);
        TextView tagTradiciones = dialog.findViewById(R.id.tagTradiciones);
        TextView tagUbicacion = dialog.findViewById(R.id.tagUbicacion);

        // INFORMACIÓN DE CULTURA
        if (tagCultura != null) {
            tagCultura.setOnClickListener(v -> {
                animarToque(v);

                mostrarInfoFlotante(
                        "Cultura de " + titulo,
                        R.drawable.ic_tema_cultura_grande,
                        R.drawable.bg_tag_naranja,
                        obtenerInformacion(titulo, "cultura")
                );
            });
        }

        // INFORMACIÓN DE TRADICIONES
        if (tagTradiciones != null) {
            tagTradiciones.setOnClickListener(v -> {
                animarToque(v);

                mostrarInfoFlotante(
                        "Tradiciones de " + titulo,
                        R.drawable.ic_tema_tradiciones_grande,
                        R.drawable.bg_tag_verde,
                        obtenerInformacion(titulo, "tradiciones")
                );
            });
        }

        // INFORMACIÓN DE UBICACIÓN
        if (tagUbicacion != null) {
            tagUbicacion.setOnClickListener(v -> {
                animarToque(v);

                mostrarInfoFlotante(
                        "Ubicación de " + titulo,
                        R.drawable.ic_tema_ubicacion_grande,
                        R.drawable.bg_tag_siena,
                        obtenerInformacion(titulo, "ubicacion")
                );
            });
        }

        // MOSTRAR VENTANA DE DETALLES
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            dialog.getWindow().setDimAmount(0.55f);

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // INFORMACIÓN DIFERENTE SEGÚN LA COMUNIDAD O EL TEMA
    private String obtenerInformacion(String titulo, String tipo) {

        switch (titulo) {

            case "El Dexthi":

                switch (tipo) {
                    case "cultura":
                        return "El Dexthi San Juanico conserva una tradición artesanal relacionada con el aprovechamiento del maguey y la elaboración de productos de ixtle. Estas actividades forman parte de los conocimientos tradicionales de sus habitantes.";

                    case "tradiciones":
                        return "El trabajo del ixtle se transmite mediante la práctica artesanal. Con esta fibra se elaboran ayates, bolsas, monederos y otros accesorios. Su elaboración requiere paciencia y trabajo manual.";

                    case "ubicacion":
                        return "El Dexthi San Juanico pertenece al municipio de Ixmiquilpan, Hidalgo, en la región del Valle del Mezquital.";
                }
                break;

            case "El Nith":

                switch (tipo) {
                    case "cultura":
                        return "El Nith es reconocido por su trabajo artesanal con madera de enebro y concha de abulón. Esta actividad representa una expresión importante de la creatividad y los conocimientos de sus artesanos.";

                    case "tradiciones":
                        return "Los artesanos trabajan la madera mediante técnicas de tallado e incrustación de concha. Elaboran piezas decorativas, miniaturas, accesorios y otros objetos artesanales.";

                    case "ubicacion":
                        return "El Nith se localiza en el municipio de Ixmiquilpan, Hidalgo, dentro de la región del Valle del Mezquital.";
                }
                break;

            case "Panales":

                switch (tipo) {
                    case "cultura":
                        return "Panales es una comunidad de Ixmiquilpan donde la organización comunitaria y las celebraciones religiosas forman parte importante de su identidad.";

                    case "tradiciones":
                        return "La comunidad celebra su fiesta patronal en honor a San Miguel Arcángel durante septiembre. En 2024, sus habitantes decidieron no utilizar pirotecnia y destinar esos recursos a la restauración de su templo.";

                    case "ubicacion":
                        return "Panales pertenece al municipio de Ixmiquilpan, Hidalgo, en la región del Valle del Mezquital.";
                }
                break;

            case "El maguey":

                switch (tipo) {
                    case "cultura":
                        return "El maguey es una planta representativa del Valle del Mezquital. Durante generaciones ha sido aprovechado por las comunidades como parte de sus actividades y conocimientos tradicionales.";

                    case "tradiciones":
                        return "De esta planta se obtiene el aguamiel para elaborar pulque. También se aprovecha su fibra, conocida como ixtle, que se utiliza para crear tejidos, ayates y artesanías.";

                    case "ubicacion":
                        return "El maguey forma parte del paisaje del Valle del Mezquital, una región del estado de Hidalgo donde sus habitantes aprovechan sus recursos naturales.";
                }
                break;

            case "Bordados hñähñu":

                switch (tipo) {
                    case "cultura":
                        return "Los bordados hñähñu son una expresión artística de las comunidades del Valle del Mezquital. Sus figuras representan elementos de la naturaleza y expresiones de la identidad cultural.";

                    case "tradiciones":
                        return "Mediante técnicas de bordado como el hilván se crean figuras de aves, flores, caminos y formas geométricas. Los diseños conservan conocimientos que se transmiten entre generaciones.";

                    case "ubicacion":
                        return "Esta tradición artesanal está presente en las comunidades hñähñu del Valle del Mezquital, especialmente en Ixmiquilpan y otras localidades de Hidalgo.";
                }
                break;

            case "Gastronomía":

                switch (tipo) {
                    case "cultura":
                        return "La gastronomía de Ixmiquilpan refleja la relación de sus habitantes con la tierra, los cultivos y los ingredientes naturales de la región.";

                    case "tradiciones":
                        return "Entre los alimentos tradicionales destacan las gorditas de quelites, los escamoles, los chinicuiles, los xamues, los gualumbos y la barbacoa. Estos ingredientes forman parte de la cocina regional.";

                    case "ubicacion":
                        return "Estos sabores forman parte de la gastronomía de Ixmiquilpan y de otras comunidades del Valle del Mezquital, en el estado de Hidalgo.";
                }
                break;

            case "San Miguel Arcángel":

                switch (tipo) {
                    case "cultura":
                        return "El templo y exconvento de San Miguel Arcángel es uno de los monumentos históricos más representativos de Ixmiquilpan. Su arquitectura y sus murales forman parte del patrimonio cultural de la región.";

                    case "tradiciones":
                        return "Las celebraciones religiosas en honor a San Miguel Arcángel forman parte de las tradiciones de la comunidad. Estas fiestas reúnen actividades religiosas y de convivencia comunitaria.";

                    case "ubicacion":
                        return "El templo de San Miguel Arcángel se encuentra en el centro de Ixmiquilpan, Hidalgo, dentro de la región del Valle del Mezquital.";
                }
                break;

            case "Ixmiquilpan":

                switch (tipo) {
                    case "cultura":
                        return "Ixmiquilpan es uno de los principales centros de la cultura hñähñu. En sus comunidades se conservan la lengua, la artesanía de ixtle, los bordados y el trabajo en madera con concha de abulón.";

                    case "tradiciones":
                        return "Entre sus tradiciones destacan las fiestas en honor a San Miguel Arcángel, el tianguis, la gastronomía regional como la barbacoa y el pulque, y los oficios artesanales que se transmiten de generación en generación.";

                    case "ubicacion":
                        return "Ixmiquilpan se encuentra en el estado de Hidalgo, en la región del Valle del Mezquital. Desde aquí se llega a comunidades como El Dexthi, El Nith y Panales.";
                }
                break;
        }

        return "Esta expresión forma parte de la riqueza cultural de Ixmiquilpan y del Valle del Mezquital, Hidalgo.";
    }

    // MOSTRAR VENTANA FLOTANTE DE INFORMACIÓN
    private void mostrarInfoFlotante(
            String titulo,
            @DrawableRes int icono,
            @DrawableRes int fondoIcono,
            String descripcion
    ) {

        if (!isAdded()) return;

        Dialog ventana = new Dialog(requireContext());

        ventana.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ventana.setContentView(R.layout.dialog_info_cultura);
        ventana.setCancelable(true);

        // REFERENCIAS DE LOS ELEMENTOS
        TextView txtTitulo = ventana.findViewById(R.id.txtTituloInfo);
        TextView txtIcono = ventana.findViewById(R.id.txtIconoInfo);
        TextView txtDescripcion = ventana.findViewById(R.id.txtDescripcionInfo);

        Button btnCerrar = ventana.findViewById(R.id.btnCerrarInfo);

        // COLOCAR INFORMACIÓN
        if (txtTitulo != null) txtTitulo.setText(titulo);
        // Icono real (en lugar de símbolos de texto) sobre el color de la etiqueta.
        if (txtIcono != null) {
            txtIcono.setText("");
            txtIcono.setBackgroundResource(fondoIcono);
            txtIcono.setCompoundDrawablesRelativeWithIntrinsicBounds(0, icono, 0, 0);
        }
        if (txtDescripcion != null) txtDescripcion.setText(descripcion);

        // CERRAR VENTANA
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> ventana.dismiss());
        }

        // MOSTRAR VENTANA
        ventana.show();

        if (ventana.getWindow() != null) {

            ventana.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            ventana.getWindow().setDimAmount(0.55f);

            int ancho = (int) (
                    getResources().getDisplayMetrics().widthPixels * 0.88f
            );

            ventana.getWindow().setLayout(
                    ancho,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}