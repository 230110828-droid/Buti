package com.itsoeh.sbsc.buti.admin;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.itsoeh.sbsc.buti.AudioButi;
import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ButiDialogo;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.vocabulario.PalabraButi;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Admin · Vocabulario: agregar, editar, eliminar e importar palabras en Firebase. */
public class AdminVocabularioFragment extends Fragment {

    // Tamaños máximos de lo que se sube a Firebase
    private static final int AUDIO_MAXIMO_BYTES = 600 * 1024;
    private static final int IMAGEN_LADO_MAXIMO = 480;

    private RecyclerView rvPalabrasAdmin;
    private PalabrasAdapter palabrasAdapter;
    private ChipGroup chipsCategorias;
    private EditText edtBuscar;
    private TextView txtTotalVocab;
    private TextView txtVacioVocab;
    private View contenedorImportar;
    private Button btnImportar;
    private String categoriaActual = VocabularioNube.CATEGORIAS[0];

    // Formulario de palabra (agregar / editar)
    private Dialog dialogoPalabra;
    private PalabraButi palabraEnEdicion;
    private String categoriaAnterior;
    private ImageView imgVistaPrevia;
    private TextView tvEstadoImagen;
    private TextView tvEstadoAudio;
    private View btnProbarAudio;

    private ButiDialogo cargando;
    private MediaPlayer reproductor;

    private final VocabularioNube.AlCambiar alCambiarVocabulario = this::refrescarVocabulario;

    // Elegir archivos del celular
    private final ActivityResultLauncher<String> elegirImagen =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::alElegirImagen);

    private final ActivityResultLauncher<String> elegirAudio =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::alElegirAudio);

    public AdminVocabularioFragment() {
        super(R.layout.fragment_admin_vocabulario);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cargando = new ButiDialogo(requireContext());
        rvPalabrasAdmin = view.findViewById(R.id.rvPalabrasAdmin);

        configurarVocabulario(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        VocabularioNube.dejarDeEscuchar(alCambiarVocabulario);
        detenerAudio();

        if (dialogoPalabra != null && dialogoPalabra.isShowing()) dialogoPalabra.dismiss();
        if (cargando != null) cargando.cerrar();
    }

    // =========================================
    // VOCABULARIO (Firebase)
    // =========================================

    private void configurarVocabulario(View vista) {

        chipsCategorias = vista.findViewById(R.id.chipsCategoriasAdmin);
        edtBuscar = vista.findViewById(R.id.edtBuscarPalabraAdmin);
        txtTotalVocab = vista.findViewById(R.id.txtTotalVocabAdmin);
        txtVacioVocab = vista.findViewById(R.id.txtVacioVocabAdmin);
        contenedorImportar = vista.findViewById(R.id.contenedorImportarAdmin);
        btnImportar = vista.findViewById(R.id.btnImportarVocabAdmin);

        palabrasAdapter = new PalabrasAdapter(new PalabrasAdapter.Acciones() {
            @Override
            public void escuchar(PalabraButi palabra) {
                reproducir(VocabularioNube.crearReproductor(requireContext(),
                        palabra.categoria, palabra.espanol));
            }

            @Override
            public void editar(PalabraButi palabra) {
                abrirFormulario(palabra);
            }

            @Override
            public void eliminar(PalabraButi palabra) {
                confirmarEliminar(palabra);
            }
        });

        rvPalabrasAdmin.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPalabrasAdmin.setAdapter(palabrasAdapter);

        crearChipsCategorias();

        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarPalabras();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        vista.findViewById(R.id.btnAgregarPalabraAdmin).setOnClickListener(v -> abrirFormulario(null));
        btnImportar.setOnClickListener(v -> confirmarImportar());

        VocabularioNube.escuchar(alCambiarVocabulario);
        refrescarVocabulario();
    }

    private void crearChipsCategorias() {

        chipsCategorias.removeAllViews();

        for (int i = 0; i < VocabularioNube.CATEGORIAS.length; i++) {

            String clave = VocabularioNube.CATEGORIAS[i];

            Chip chip = new Chip(requireContext());
            chip.setId(View.generateViewId());
            chip.setTag(clave);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setText(VocabularioNube.NOMBRES[i]);
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), R.color.buti_superficie_alt));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.buti_titulo));
            chip.setChipStrokeColor(ContextCompat.getColorStateList(requireContext(), R.color.buti_borde));
            chip.setChipStrokeWidth(2f);

            chip.setOnCheckedChangeListener((boton, marcado) -> {
                if (marcado) {
                    categoriaActual = clave;
                    boton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buti_sobre_acento));
                    ((Chip) boton).setChipBackgroundColor(
                            ContextCompat.getColorStateList(requireContext(), R.color.buti_acento));
                    edtBuscar.setText("");
                    mostrarPalabras();
                } else {
                    boton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buti_titulo));
                    ((Chip) boton).setChipBackgroundColor(
                            ContextCompat.getColorStateList(requireContext(), R.color.buti_superficie_alt));
                }
            });

            chipsCategorias.addView(chip);

            if (clave.equals(categoriaActual)) chip.setChecked(true);
        }
    }

    private void refrescarVocabulario() {

        if (!isAdded() || getView() == null) return;

        int total = VocabularioNube.total();

        String error = VocabularioNube.ultimoError();

        txtTotalVocab.setText(total == 0
                ? (VocabularioNube.listoDesdeFirebase()
                   ? "Firebase todavía no tiene palabras"
                   : error != null
                     ? "Firebase no respondió: " + error
                     : "Conectando con Firebase…")
                : total + " palabras en Firebase");

        // Contador en cada categoría.
        for (int i = 0; i < chipsCategorias.getChildCount(); i++) {
            View hijo = chipsCategorias.getChildAt(i);
            if (hijo instanceof Chip && hijo.getTag() != null) {
                String clave = hijo.getTag().toString();
                int cantidad = VocabularioNube.palabras(clave).size();
                ((Chip) hijo).setText(VocabularioNube.nombre(clave) + " (" + cantidad + ")");
            }
        }

        contenedorImportar.setVisibility(
                VocabularioNube.listoDesdeFirebase() && VocabularioNube.faltanCategorias()
                        ? View.VISIBLE
                        : View.GONE
        );

        mostrarPalabras();
    }

    private void mostrarPalabras() {

        String busqueda = comparable(edtBuscar.getText().toString());
        List<PalabraButi> resultado = new ArrayList<>();

        if (busqueda.isEmpty()) {

            resultado.addAll(VocabularioNube.palabras(categoriaActual));

        } else {

            // Con texto: busca en todas las categorías (máximo 60 resultados).
            for (String clave : VocabularioNube.CATEGORIAS) {
                for (PalabraButi p : VocabularioNube.palabras(clave)) {
                    if (comparable(p.espanol).contains(busqueda) || comparable(p.hnahnu).contains(busqueda)) {
                        resultado.add(p);
                        if (resultado.size() >= 60) break;
                    }
                }
                if (resultado.size() >= 60) break;
            }
        }

        palabrasAdapter.mostrar(resultado);

        txtVacioVocab.setVisibility(resultado.isEmpty() ? View.VISIBLE : View.GONE);
        rvPalabrasAdmin.setVisibility(resultado.isEmpty() ? View.GONE : View.VISIBLE);
        txtVacioVocab.setText(busqueda.isEmpty()
                ? (VocabularioNube.total() == 0
                   ? "Firebase está vacío. Usa «Importar vocabulario de la app»."
                   : "No hay palabras en esta categoría. Toca «+ Agregar».")
                : "No se encontró ninguna palabra con ese texto.");
    }

    // ---------- Importar ----------

    private void confirmarImportar() {

        new ButiDialogo(requireContext()).mostrarConfirmacion(
                "Importar vocabulario",
                "Se subirán a Firebase todas las palabras que ya vienen en la app, "
                        + "con el nombre de su imagen y su audio.\n\n"
                        + "Solo se suben las categorías vacías, así que no se duplica nada.",
                "Importar",
                "Cancelar",
                this::importar,
                null
        );
    }

    private void importar() {

        cargando.mostrarCargando("Importando vocabulario", "Subiendo las palabras a Firebase…");

        VocabularioNube.importarDesdeApp(requireContext(), (exito, mensaje) -> {

            if (!isAdded() || getView() == null) return;

            cargando.cerrar();

            if (exito) {
                new ButiDialogo(requireContext()).mostrarExito(
                        "¡Vocabulario importado!",
                        "Ya puedes editar, eliminar y agregar palabras desde aquí. "
                                + "Los cambios se ven al instante en la app.",
                        null
                );
            } else {
                new ButiDialogo(requireContext()).mostrarError(
                        "No se pudo importar",
                        mensajeError(mensaje)
                );
            }
        });
    }

    // ---------- Eliminar ----------

    private void confirmarEliminar(PalabraButi palabra) {

        new ButiDialogo(requireContext()).mostrarConfirmacion(
                "¿Eliminar palabra?",
                "«" + palabra.hnahnu + "» (" + palabra.espanol + ") se quitará de "
                        + VocabularioNube.nombre(palabra.categoria)
                        + " y ya no aparecerá en la app.",
                "Eliminar",
                "Cancelar",
                () -> VocabularioNube.eliminar(palabra, (exito, error) -> {
                    if (!isAdded() || getView() == null) return;
                    if (exito) {
                        ButiAviso.mostrar(this, "Palabra eliminada.", ButiAviso.Tipo.EXITO);
                    } else {
                        new ButiDialogo(requireContext()).mostrarError("No se pudo eliminar", mensajeError(error));
                    }
                }),
                null
        );
    }

    // ---------- Formulario agregar / editar ----------

    private void abrirFormulario(@Nullable PalabraButi existente) {

        palabraEnEdicion = existente == null ? new PalabraButi() : existente.copia();
        categoriaAnterior = existente == null ? null : existente.categoria;

        if (existente == null) palabraEnEdicion.categoria = categoriaActual;

        dialogoPalabra = new Dialog(requireContext());
        dialogoPalabra.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoPalabra.setContentView(R.layout.dialogo_agregar_palabra_admin);
        dialogoPalabra.setCancelable(true);
        dialogoPalabra.setOnDismissListener(d -> detenerAudio());

        TextView titulo = dialogoPalabra.findViewById(R.id.txtTituloDialogoPalabra);
        AutoCompleteTextView campoCategoria = dialogoPalabra.findViewById(R.id.actCategoriaAdmin);
        TextInputLayout tilHnahnu = dialogoPalabra.findViewById(R.id.tilHnahnuAdmin);
        TextInputLayout tilEspanol = dialogoPalabra.findViewById(R.id.tilEspanolAdmin);
        TextInputEditText edtHnahnu = dialogoPalabra.findViewById(R.id.edtPalabraHnahnuAdmin);
        TextInputEditText edtEspanol = dialogoPalabra.findViewById(R.id.edtPalabraEspanolAdmin);

        imgVistaPrevia = dialogoPalabra.findViewById(R.id.imgVistaPreviaAdmin);
        tvEstadoImagen = dialogoPalabra.findViewById(R.id.tvEstadoImagenAdmin);
        tvEstadoAudio = dialogoPalabra.findViewById(R.id.tvEstadoAudioAdmin);
        btnProbarAudio = dialogoPalabra.findViewById(R.id.btnProbarAudioAdmin);

        titulo.setText(existente == null ? "Agregar palabra" : "Editar palabra");

        // Categorías en una lista desplegable.
        campoCategoria.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, Arrays.asList(VocabularioNube.NOMBRES)));
        campoCategoria.setText(VocabularioNube.nombre(palabraEnEdicion.categoria), false);
        campoCategoria.setOnItemClickListener((parent, view, posicion, id) ->
                palabraEnEdicion.categoria = VocabularioNube.CATEGORIAS[posicion]);

        edtHnahnu.setText(palabraEnEdicion.hnahnu);
        edtEspanol.setText(palabraEnEdicion.espanol);

        actualizarVistaImagen();
        actualizarVistaAudio();

        dialogoPalabra.findViewById(R.id.btnSeleccionarImagenAdmin)
                .setOnClickListener(v -> elegirImagen.launch("image/*"));

        dialogoPalabra.findViewById(R.id.btnQuitarImagenAdmin).setOnClickListener(v -> {
            palabraEnEdicion.imagen = "";
            palabraEnEdicion.imagenBase64 = "";
            actualizarVistaImagen();
        });

        dialogoPalabra.findViewById(R.id.btnSeleccionarAudioAdmin)
                .setOnClickListener(v -> elegirAudio.launch("audio/*"));

        btnProbarAudio.setOnClickListener(v -> probarAudioDelFormulario());

        dialogoPalabra.findViewById(R.id.btnQuitarAudioAdmin).setOnClickListener(v -> {
            detenerAudio();
            palabraEnEdicion.audio = "";
            palabraEnEdicion.audio2 = "";
            palabraEnEdicion.audioBase64 = "";
            actualizarVistaAudio();
        });

        dialogoPalabra.findViewById(R.id.btnCancelarPalabraAdmin)
                .setOnClickListener(v -> dialogoPalabra.dismiss());

        dialogoPalabra.findViewById(R.id.btnGuardarPalabraAdmin).setOnClickListener(v -> {

            String hnahnu = texto(edtHnahnu);
            String espanol = texto(edtEspanol);

            tilHnahnu.setError(hnahnu.isEmpty() ? "Escribe la palabra en hñähñu" : null);
            tilEspanol.setError(espanol.isEmpty() ? "Escribe el significado en español" : null);

            if (hnahnu.isEmpty() || espanol.isEmpty()) return;

            // Evitar repetidos en la misma categoría.
            PalabraButi repetida = VocabularioNube.buscar(palabraEnEdicion.categoria, espanol);

            if (repetida != null && !repetida.id.equals(palabraEnEdicion.id)) {
                tilEspanol.setError("Ya existe «" + espanol + "» en "
                        + VocabularioNube.nombre(palabraEnEdicion.categoria));
                return;
            }

            palabraEnEdicion.hnahnu = hnahnu;
            palabraEnEdicion.espanol = espanol;

            guardarPalabra();
        });

        dialogoPalabra.show();

        Window ventana = dialogoPalabra.getWindow();
        if (ventana != null) {
            ventana.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ventana.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void guardarPalabra() {

        boolean esNueva = palabraEnEdicion.id == null || palabraEnEdicion.id.isEmpty();

        cargando.mostrarCargando("Guardando", "Subiendo la palabra a Firebase…");

        VocabularioNube.guardar(palabraEnEdicion, categoriaAnterior, (exito, error) -> {

            if (!isAdded() || getView() == null) return;

            cargando.cerrar();

            if (exito) {
                if (dialogoPalabra != null) dialogoPalabra.dismiss();

                ButiAviso.mostrar(
                        this,
                        esNueva ? "Palabra agregada." : "Cambios guardados.",
                        ButiAviso.Tipo.EXITO
                );
            } else {
                new ButiDialogo(requireContext()).mostrarError("No se pudo guardar", mensajeError(error));
            }
        });
    }

    private void actualizarVistaImagen() {

        if (imgVistaPrevia == null || palabraEnEdicion == null) return;

        Bitmap subida = VocabularioNube.decodificarImagen(palabraEnEdicion);

        if (subida != null) {
            imgVistaPrevia.setImageBitmap(subida);
            tvEstadoImagen.setText("Imagen subida");
            return;
        }

        int recurso = palabraEnEdicion.imagen.isEmpty() ? 0
                : getResources().getIdentifier(palabraEnEdicion.imagen, "drawable", requireContext().getPackageName());

        if (recurso != 0) {
            imgVistaPrevia.setImageResource(recurso);
            tvEstadoImagen.setText("Imagen de la app");
        } else {
            imgVistaPrevia.setImageResource(R.drawable.ic_cambiar_imagen);
            tvEstadoImagen.setText("Sin imagen");
        }
    }

    private void actualizarVistaAudio() {

        if (tvEstadoAudio == null || palabraEnEdicion == null) return;

        if (!palabraEnEdicion.audioBase64.isEmpty()) {
            int kb = palabraEnEdicion.audioBase64.length() * 3 / 4 / 1024;
            tvEstadoAudio.setText("Audio subido (" + kb + " KB)");
        } else if (!palabraEnEdicion.audio.isEmpty()) {
            tvEstadoAudio.setText("Audio de la app: " + palabraEnEdicion.audio);
        } else {
            tvEstadoAudio.setText("Sin audio");
        }

        btnProbarAudio.setVisibility(palabraEnEdicion.tieneAudio() ? View.VISIBLE : View.GONE);
    }

    // ---------- Archivos elegidos ----------

    private void alElegirImagen(@Nullable Uri uri) {

        if (uri == null || palabraEnEdicion == null) return;

        try {
            // Leer en tamaño reducido para no gastar memoria.
            BitmapFactory.Options medidas = new BitmapFactory.Options();
            medidas.inJustDecodeBounds = true;

            try (InputStream entrada = requireContext().getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(entrada, null, medidas);
            }

            int escala = 1;
            while (Math.max(medidas.outWidth, medidas.outHeight) / (escala * 2) >= IMAGEN_LADO_MAXIMO) {
                escala *= 2;
            }

            BitmapFactory.Options opciones = new BitmapFactory.Options();
            opciones.inSampleSize = escala;

            Bitmap original;
            try (InputStream entrada = requireContext().getContentResolver().openInputStream(uri)) {
                original = BitmapFactory.decodeStream(entrada, null, opciones);
            }

            if (original == null) throw new IllegalStateException();

            float factor = Math.min(1f,
                    (float) IMAGEN_LADO_MAXIMO / Math.max(original.getWidth(), original.getHeight()));

            Bitmap reducida = factor < 1f
                    ? Bitmap.createScaledBitmap(original,
                    Math.round(original.getWidth() * factor),
                    Math.round(original.getHeight() * factor), true)
                    : original;

            // PNG si tiene transparencia (ilustraciones); JPEG si es foto.
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            if (reducida.hasAlpha()) {
                reducida.compress(Bitmap.CompressFormat.PNG, 100, salida);
            } else {
                reducida.compress(Bitmap.CompressFormat.JPEG, 80, salida);
            }

            palabraEnEdicion.imagenBase64 = Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP);
            palabraEnEdicion.imagen = "";

            actualizarVistaImagen();

        } catch (Exception e) {
            ButiAviso.mostrar(this, "No se pudo leer esa imagen.", ButiAviso.Tipo.ERROR);
        }
    }

    private void alElegirAudio(@Nullable Uri uri) {

        if (uri == null || palabraEnEdicion == null) return;

        try (InputStream entrada = requireContext().getContentResolver().openInputStream(uri)) {

            if (entrada == null) throw new IllegalStateException();

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            byte[] bloque = new byte[8192];
            int leidos;

            while ((leidos = entrada.read(bloque)) != -1) {
                salida.write(bloque, 0, leidos);

                if (salida.size() > AUDIO_MAXIMO_BYTES) {
                    new ButiDialogo(requireContext()).mostrarError(
                            "Audio muy grande",
                            "El audio debe pesar máximo 600 KB. Usa una grabación corta "
                                    + "(solo la palabra) en mp3 o m4a."
                    );
                    return;
                }
            }

            detenerAudio();

            palabraEnEdicion.audioBase64 = Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP);
            palabraEnEdicion.audio = "";
            palabraEnEdicion.audio2 = "";

            actualizarVistaAudio();

        } catch (Exception e) {
            ButiAviso.mostrar(this, "No se pudo leer ese audio.", ButiAviso.Tipo.ERROR);
        }
    }

    private void probarAudioDelFormulario() {

        if (palabraEnEdicion == null) return;

        if (!palabraEnEdicion.audioBase64.isEmpty()) {

            PalabraButi prueba = palabraEnEdicion.copia();
            prueba.id = "vista_previa";

            try {
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(VocabularioNube.archivoAudio(prueba).getAbsolutePath());
                mp.prepare();
                reproducir(mp);
            } catch (Exception e) {
                ButiAviso.mostrar(this, "Ese archivo no se puede reproducir.", ButiAviso.Tipo.ERROR);
            }

        } else if (!palabraEnEdicion.audio.isEmpty()) {

            int id = getResources().getIdentifier(palabraEnEdicion.audio, "raw", requireContext().getPackageName());
            reproducir(id == 0 ? null : MediaPlayer.create(requireContext(), id));
        }
    }

    // ---------- Audio ----------

    private void reproducir(@Nullable MediaPlayer nuevo) {

        detenerAudio();

        if (nuevo == null) {
            ButiAviso.mostrar(this, "Esta palabra no tiene audio.", ButiAviso.Tipo.INFO);
            return;
        }

        reproductor = nuevo;
        reproductor.setOnCompletionListener(mp -> detenerAudio());
        AudioButi.empezar(reproductor);
        reproductor.start();
    }

    private void detenerAudio() {

        if (reproductor == null) return;

        AudioButi.termino(reproductor);

        try {
            if (reproductor.isPlaying()) reproductor.stop();
            reproductor.release();
        } catch (Exception ignored) {
        }

        reproductor = null;
    }

    // ---------- Ayudantes ----------

    private static String texto(@Nullable EditText campo) {
        return campo == null || campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    private static String comparable(@Nullable String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static String mensajeError(@Nullable String error) {

        if (error != null && error.toLowerCase(Locale.ROOT).contains("permission")) {
            return "Firebase no dio permiso. Revisa que entraste con la cuenta de administrador "
                    + "y que las reglas de la base de datos permiten escribir en «vocabulario».";
        }

        return error == null ? "Revisa tu conexión a internet e inténtalo de nuevo." : error;
    }
}