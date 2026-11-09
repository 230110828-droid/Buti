package com.itsoeh.sbsc.buti.admin;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.progreso.GraficaBarrasView;
import com.itsoeh.sbsc.buti.vocabulario.PalabraButi;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Admin · Reportes: estudiantes, actividad, avance por nivel y vocabulario. */
public class AdminResumenFragment extends Fragment {

    private static final String[] NOMBRES_NIVELES = {
            "Saludos", "Números", "Colores", "Animales", "Plantas", "Objetos",
            "Cuerpo", "Alimentos", "Tiempo", "Naturaleza", "Lugares"
    };

    private TextView txtActualizado, txtUsuarios, txtHoy, txtSemana, txtPuntos;
    private TextView txtResumenActividad, txtVocab;
    private GraficaBarrasView grafica;
    private LinearLayout contenedorNiveles, contenedorTop;

    private DatabaseReference refUsuarios;
    private ValueEventListener escuchaUsuarios;

    private final VocabularioNube.AlCambiar alCambiarVocabulario = this::mostrarVocabulario;

    public AdminResumenFragment() {
        super(R.layout.fragment_admin_resumen);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtActualizado = view.findViewById(R.id.txtActualizadoAdmin);
        txtUsuarios = view.findViewById(R.id.txtStatUsuariosAdmin);
        txtHoy = view.findViewById(R.id.txtStatHoyAdmin);
        txtSemana = view.findViewById(R.id.txtStatSemanaAdmin);
        txtPuntos = view.findViewById(R.id.txtStatPuntosAdmin);
        txtResumenActividad = view.findViewById(R.id.txtResumenActividadAdmin);
        txtVocab = view.findViewById(R.id.txtVocabResumenAdmin);
        grafica = view.findViewById(R.id.graficaActividadAdmin);
        contenedorNiveles = view.findViewById(R.id.contenedorNivelesAdmin);
        contenedorTop = view.findViewById(R.id.contenedorTopAdmin);

        view.findViewById(R.id.btnIrVocabularioAdmin).setOnClickListener(v -> {
            if (getActivity() instanceof AdminActivity) {
                ((AdminActivity) getActivity()).irA(R.id.adminVocabulario);
            }
        });

        // Usuarios en vivo.
        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        escuchaUsuarios = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded() && getView() != null) mostrarReportes(UsuarioAdmin.lista(snapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && txtActualizado != null) {
                    txtActualizado.setText("No se pudieron leer los usuarios: " + error.getMessage());
                }
            }
        };

        refUsuarios.addValueEventListener(escuchaUsuarios);

        VocabularioNube.escuchar(alCambiarVocabulario);
        mostrarVocabulario();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (refUsuarios != null && escuchaUsuarios != null) {
            refUsuarios.removeEventListener(escuchaUsuarios);
        }

        VocabularioNube.dejarDeEscuchar(alCambiarVocabulario);
    }

    // =========================================
    // REPORTES
    // =========================================

    private void mostrarReportes(List<UsuarioAdmin> usuarios) {

        String hoy = ProgresoButi.hoy();

        // Últimos 7 días (el último es hoy).
        String[] claves = new String[7];
        String[] etiquetas = new String[7];
        SimpleDateFormat formatoEtiqueta = new SimpleDateFormat("EEE", new Locale("es", "MX"));

        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            claves[i] = ProgresoButi.diaDe(calendario);
            String dia = formatoEtiqueta.format(calendario.getTime()).replace(".", "");
            etiquetas[i] = i == 6 ? "Hoy" : dia.substring(0, 1).toUpperCase() + dia.substring(1);
            calendario.add(Calendar.DAY_OF_YEAR, 1);
        }

        int activosHoy = 0;
        int activosSemana = 0;
        int puntosTotales = 0;
        int[] porDia = new int[7];

        for (UsuarioAdmin u : usuarios) {

            puntosTotales += u.puntos;

            if (u.activoEl(hoy)) activosHoy++;

            boolean enLaSemana = false;

            for (int i = 0; i < 7; i++) {
                if (u.activoEl(claves[i])) {
                    porDia[i]++;
                    enLaSemana = true;
                }
            }

            if (enLaSemana) activosSemana++;
        }

        txtUsuarios.setText(String.valueOf(usuarios.size()));
        txtHoy.setText(String.valueOf(activosHoy));
        txtSemana.setText(String.valueOf(activosSemana));
        txtPuntos.setText(String.valueOf(puntosTotales));

        txtActualizado.setText("Actualizado en vivo · "
                + new SimpleDateFormat("d 'de' MMMM, HH:mm", new Locale("es", "MX")).format(new Date()));

        // Gráfica de la semana.
        grafica.setDatos(etiquetas, porDia, 6, 0);

        txtResumenActividad.setText(usuarios.isEmpty()
                ? "Todavía no hay estudiantes registrados."
                : activosSemana + " de " + usuarios.size()
                + " estudiantes practicaron esta semana ("
                + Math.round(activosSemana * 100f / usuarios.size()) + " %).");

        mostrarNiveles(usuarios);
        mostrarTop(usuarios);
    }

    private void mostrarNiveles(List<UsuarioAdmin> usuarios) {

        contenedorNiveles.removeAllViews();

        for (int n = 0; n < UsuarioAdmin.NIVELES; n++) {

            int etapas = 0;
            int terminaron = 0;

            for (UsuarioAdmin u : usuarios) {
                etapas += u.etapasPorNivel[n];
                if (u.etapasPorNivel[n] == UsuarioAdmin.ETAPAS) terminaron++;
            }

            int porcentaje = usuarios.isEmpty() ? 0
                    : Math.round(etapas * 100f / (usuarios.size() * UsuarioAdmin.ETAPAS));

            contenedorNiveles.addView(filaNivel(n + 1, NOMBRES_NIVELES[n], porcentaje, terminaron));
        }
    }

    private View filaNivel(int numero, String nombre, int porcentaje, int terminaron) {

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.VERTICAL);
        fila.setPadding(0, dp(6), 0, dp(6));

        LinearLayout textos = new LinearLayout(requireContext());
        textos.setOrientation(LinearLayout.HORIZONTAL);

        TextView titulo = texto(numero + ". " + nombre, 14, R.color.buti_texto, true);
        textos.addView(titulo, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView valor = texto(porcentaje + " %  ·  " + terminaron
                + (terminaron == 1 ? " lo terminó" : " lo terminaron"), 12, R.color.buti_texto_secundario, false);
        textos.addView(valor);

        fila.addView(textos);

        LinearProgressIndicator barra = new LinearProgressIndicator(requireContext());
        barra.setMax(100);
        barra.setProgressCompat(porcentaje, false);
        barra.setTrackThickness(dp(8));
        barra.setTrackCornerRadius(dp(4));
        barra.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.buti_acento));
        barra.setTrackColor(ContextCompat.getColor(requireContext(), R.color.buti_superficie_alt));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(4);
        fila.addView(barra, lp);

        fila.setContentDescription("Nivel " + numero + ", " + nombre + ": " + porcentaje
                + " por ciento de avance, " + terminaron + " lo terminaron");
        fila.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        return fila;
    }

    private void mostrarTop(List<UsuarioAdmin> usuarios) {

        contenedorTop.removeAllViews();

        List<UsuarioAdmin> orden = new ArrayList<>(usuarios);
        Collections.sort(orden, (a, b) -> Integer.compare(b.puntos, a.puntos));

        if (orden.isEmpty()) {
            contenedorTop.addView(texto("Todavía no hay estudiantes.", 14, R.color.buti_texto_secundario, false));
            return;
        }

        for (int i = 0; i < Math.min(5, orden.size()); i++) {

            UsuarioAdmin u = orden.get(i);

            LinearLayout fila = new LinearLayout(requireContext());
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setPadding(0, dp(6), 0, dp(6));

            TextView lugar = texto(String.valueOf(i + 1), 15, R.color.buti_sobre_acento, true);
            lugar.setGravity(Gravity.CENTER);
            lugar.setBackgroundResource(R.drawable.bg_numero_paso);
            lugar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    i == 0 ? R.color.buti_dorado : R.color.buti_acento)));
            fila.addView(lugar, new LinearLayout.LayoutParams(dp(30), dp(30)));

            TextView nombre = texto(u.nombre, 15, R.color.buti_texto, true);
            LinearLayout.LayoutParams lpNombre = new LinearLayout.LayoutParams(0, -2, 1f);
            lpNombre.setMarginStart(dp(10));
            fila.addView(nombre, lpNombre);

            fila.addView(texto(u.puntos + " pts", 14, R.color.buti_titulo, true));

            contenedorTop.addView(fila);
        }
    }

    // =========================================
    // VOCABULARIO
    // =========================================

    private void mostrarVocabulario() {

        if (!isAdded() || txtVocab == null) return;

        int total = 0;
        int sinAudio = 0;
        int sinImagen = 0;
        int subidas = 0;

        for (String categoria : VocabularioNube.CATEGORIAS) {
            for (PalabraButi p : VocabularioNube.palabras(categoria)) {
                total++;
                if (!p.tieneAudio()) sinAudio++;
                if (!p.tieneImagen()) sinImagen++;
                if (!p.imagenBase64.isEmpty() || !p.audioBase64.isEmpty()) subidas++;
            }
        }

        if (total == 0) {
            txtVocab.setText("Firebase todavía no tiene palabras. Entra a Vocabulario y presiona «Importar».");
            return;
        }

        txtVocab.setText(total + " palabras en " + VocabularioNube.CATEGORIAS.length + " categorías\n"
                + sinAudio + " sin audio · " + sinImagen + " sin imagen\n"
                + subidas + " con archivos subidos desde el Admin");
    }

    // =========================================
    // AYUDANTES
    // =========================================

    private TextView texto(String contenido, int sp, int color, boolean negrita) {

        TextView t = new TextView(requireContext());
        t.setText(contenido);
        t.setTextSize(sp);
        t.setTextColor(ContextCompat.getColor(requireContext(), color));

        if (negrita) t.setTypeface(t.getTypeface(), Typeface.BOLD);

        return t;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
