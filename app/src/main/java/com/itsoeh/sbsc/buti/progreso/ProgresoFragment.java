package com.itsoeh.sbsc.buti.progreso;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.itsoeh.sbsc.buti.FotoPerfil;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.RachaMaguey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgresoFragment extends Fragment {

    // DATOS DEL USUARIO
    private TextView txtNombre;
    private TextView txtIniciales;
    private TextView txtRacha;
    private TextView txtPuntos;
    private TextView txtNivel;
    private TextView txtActividadReciente;

    // IMAGEN DE RACHA
    private ImageView imgRacha;

    // TARJETAS DE INSIGNIAS
    private View cardInsignia1;
    private View cardInsignia2;
    private View cardInsignia3;
    private View cardInsignia4;

    // IMÁGENES DE INSIGNIAS
    private ImageView imgInsignia1;
    private ImageView imgInsignia2;
    private ImageView imgInsignia3;
    private ImageView imgInsignia4;

    // ESTADOS DE INSIGNIAS
    private TextView txtEstadoInsignia1;
    private TextView txtEstadoInsignia2;
    private TextView txtEstadoInsignia3;
    private TextView txtEstadoInsignia4;

    // GRÁFICAS
    private GraficaBarrasView graficaSemana;
    private GraficaBarrasView graficaNiveles;
    private TextView txtResumenSemana;
    private TextView txtResumenNiveles;

    private static final int TOTAL_NIVELES = 11;
    private static final int ETAPAS_POR_NIVEL = 5;

    // FIREBASE
    private DatabaseReference referenciaUsuario;
    private ValueEventListener listenerProgreso;

    public ProgresoFragment() {
        super(R.layout.fragment_progreso);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // DATOS DEL USUARIO
        txtNombre = view.findViewById(R.id.txtNombreProgreso);
        txtIniciales = view.findViewById(R.id.txtInicialesProgreso);

        // Foto de perfil (si no hay, se ven las iniciales).
        FotoPerfil.mostrar(
                getViewLifecycleOwner(),
                view.findViewById(R.id.imgFotoProgreso),
                txtIniciales,
                null
        );
        txtRacha = view.findViewById(R.id.txtRachaProgreso);
        txtPuntos = view.findViewById(R.id.txtPuntosProgreso);
        txtNivel = view.findViewById(R.id.txtNivelProgreso);

        // RACHA
        imgRacha = view.findViewById(R.id.imgRachaMaguey);

        // ACTIVIDAD RECIENTE
        txtActividadReciente =
                view.findViewById(R.id.txtActividadReciente);

        // TARJETAS
        cardInsignia1 = view.findViewById(R.id.cardInsignia1);
        cardInsignia2 = view.findViewById(R.id.cardInsignia2);
        cardInsignia3 = view.findViewById(R.id.cardInsignia3);
        cardInsignia4 = view.findViewById(R.id.cardInsignia4);

        // IMÁGENES
        imgInsignia1 = view.findViewById(R.id.imgInsignia1);
        imgInsignia2 = view.findViewById(R.id.imgInsignia2);
        imgInsignia3 = view.findViewById(R.id.imgInsignia3);
        imgInsignia4 = view.findViewById(R.id.imgInsignia4);

        // TEXTOS DE ESTADO
        txtEstadoInsignia1 =
                view.findViewById(R.id.txtEstadoInsignia1);

        txtEstadoInsignia2 =
                view.findViewById(R.id.txtEstadoInsignia2);

        txtEstadoInsignia3 =
                view.findViewById(R.id.txtEstadoInsignia3);

        txtEstadoInsignia4 =
                view.findViewById(R.id.txtEstadoInsignia4);

        // GRÁFICAS
        graficaSemana = view.findViewById(R.id.graficaSemanaProgreso);
        graficaNiveles = view.findViewById(R.id.graficaNivelesProgreso);
        txtResumenSemana = view.findViewById(R.id.txtResumenSemanaProgreso);
        txtResumenNiveles = view.findViewById(R.id.txtResumenNivelesProgreso);

        cargarProgreso();
    }

    // =====================================================
    // CARGAR INFORMACIÓN DEL USUARIO
    // =====================================================

    private void cargarProgreso() {

        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {

            txtNombre.setText("Usuario");
            txtIniciales.setText("U");
            txtRacha.setText("0");
            txtPuntos.setText("0");
            txtNivel.setText("Nv. 1");

            imgRacha.setImageResource(
                    RachaMaguey.dibujoPorRacha(0)
            );

            actualizarInsignias(0);

            cargarGraficas(null, null);

            txtActividadReciente.setText(
                    "Todavía no tienes actividades registradas."
            );

            return;
        }

        referenciaUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid());

        String nombreInicial = usuario.getDisplayName();

        if (TextUtils.isEmpty(nombreInicial)) {
            nombreInicial = "Usuario";
        }

        mostrarNombre(nombreInicial);

        listenerProgreso = new ValueEventListener() {

            @Override
            public void onDataChange(
                    @NonNull DataSnapshot snapshot) {

                if (!isAdded() || getView() == null) {
                    return;
                }

                // -----------------------------------------
                // NOMBRE
                // -----------------------------------------

                String nombre = snapshot.child("nombre")
                        .getValue(String.class);

                if (TextUtils.isEmpty(nombre)) {
                    nombre = usuario.getDisplayName();
                }

                if (TextUtils.isEmpty(nombre)) {
                    nombre = "Usuario";
                }

                mostrarNombre(nombre);

                // -----------------------------------------
                // RACHA
                // -----------------------------------------

                // Si dejó pasar un día sin practicar, la racha vuelve a 0.
                int diasRacha = ProgresoButi.rachaVigente(snapshot);

                txtRacha.setText(String.valueOf(diasRacha));

                imgRacha.setImageResource(
                        RachaMaguey.dibujoPorRacha(diasRacha)
                );

                // -----------------------------------------
                // PROGRESO DE LECCIONES
                // -----------------------------------------

                DataSnapshot progreso =
                        snapshot.child("progresoLecciones");

                calcularEstadisticas(progreso);

                // Puntos: lecciones (mejor puntaje) + práctica + juegos.
                txtPuntos.setText(String.valueOf(ProgresoButi.puntosTotales(snapshot)));

                // -----------------------------------------
                // INSIGNIAS
                // -----------------------------------------

                calcularInsignias(progreso);

                // -----------------------------------------
                // ACTIVIDAD RECIENTE
                // -----------------------------------------

                cargarActividadReciente(progreso);

                // -----------------------------------------
                // GRÁFICAS
                // -----------------------------------------

                cargarGraficas(progreso, snapshot);
            }

            @Override
            public void onCancelled(
                    @NonNull DatabaseError error) {

                android.util.Log.e(
                        "ProgresoFragment",
                        "Error al cargar progreso: "
                                + error.getMessage()
                );
            }
        };

        referenciaUsuario.addValueEventListener(listenerProgreso);
    }

    // =====================================================
    // MOSTRAR NOMBRE E INICIALES
    // =====================================================

    private void mostrarNombre(String nombre) {

        txtNombre.setText(nombre);

        String[] partes = nombre.trim().split("\\s+");

        String iniciales;

        if (partes.length >= 2) {

            iniciales = partes[0].substring(0, 1)
                    + partes[1].substring(0, 1);

        } else if (!nombre.isEmpty()) {

            iniciales = nombre.substring(
                    0,
                    Math.min(2, nombre.length())
            );

        } else {

            iniciales = "U";
        }

        txtIniciales.setText(iniciales.toUpperCase());
    }

    // =====================================================
    // CALCULAR PUNTOS Y NIVEL
    // =====================================================

    private void calcularEstadisticas(DataSnapshot progreso) {

        int puntosTotales = 0;
        int nivelActual = 1;

        for (DataSnapshot nivelSnapshot : progreso.getChildren()) {

            String nombreNivel = nivelSnapshot.getKey();

            if (nombreNivel == null
                    || !nombreNivel.startsWith("nivel")) {
                continue;
            }

            int numeroNivel;

            try {

                numeroNivel = Integer.parseInt(
                        nombreNivel.replace("nivel", "")
                );

            } catch (NumberFormatException e) {
                continue;
            }

            boolean tieneEtapasCompletadas = false;

            for (DataSnapshot etapa : nivelSnapshot.getChildren()) {

                Boolean completada = etapa.child("completada")
                        .getValue(Boolean.class);

                if (completada != null && completada) {

                    tieneEtapasCompletadas = true;

                    Long puntosEtapa = etapa.child("puntos")
                            .getValue(Long.class);

                    if (puntosEtapa != null) {
                        puntosTotales += puntosEtapa.intValue();
                    }
                }
            }

            if (tieneEtapasCompletadas) {

                nivelActual = Math.max(
                        nivelActual,
                        numeroNivel
                );
            }
        }

        txtNivel.setText("Nv. " + nivelActual);
    }

    // =====================================================
    // CALCULAR INSIGNIAS
    // =====================================================

    private void calcularInsignias(DataSnapshot progreso) {

        int etapasCompletadas = 0;

        for (DataSnapshot nivel : progreso.getChildren()) {

            String nombreNivel = nivel.getKey();

            if (nombreNivel == null
                    || !nombreNivel.startsWith("nivel")) {
                continue;
            }

            for (DataSnapshot etapa : nivel.getChildren()) {

                Boolean completada = etapa.child("completada")
                        .getValue(Boolean.class);

                if (completada != null && completada) {
                    etapasCompletadas++;
                }
            }
        }

        actualizarInsignias(etapasCompletadas);
    }

    // =====================================================
    // ACTUALIZAR ESTADO DE INSIGNIAS
    // =====================================================

    private void actualizarInsignias(int etapasCompletadas) {

        // INSIGNIA 1: PRIMERA LECCIÓN
        actualizarEstadoInsignia(
                cardInsignia1,
                imgInsignia1,
                txtEstadoInsignia1,
                etapasCompletadas >= 1,
                R.drawable.ic_cirpajaro
        );

        // INSIGNIA 2: APRENDIZ CONSTANTE
        actualizarEstadoInsignia(
                cardInsignia2,
                imgInsignia2,
                txtEstadoInsignia2,
                etapasCompletadas >= 5,
                R.drawable.ic_libro1
        );

        // INSIGNIA 3: CONOCIMIENTO
        actualizarEstadoInsignia(
                cardInsignia3,
                imgInsignia3,
                txtEstadoInsignia3,
                etapasCompletadas >= 15,
                R.drawable.ic_nopalverde
        );

        // INSIGNIA 4: MAESTRO BUTI
        actualizarEstadoInsignia(
                cardInsignia4,
                imgInsignia4,
                txtEstadoInsignia4,
                etapasCompletadas >= 55,
                R.drawable.ic_cirestrella
        );
    }

    private void actualizarEstadoInsignia(
            View tarjeta,
            ImageView imagen,
            TextView estado,
            boolean desbloqueada,
            int imagenDesbloqueada) {

        if (tarjeta == null || imagen == null || estado == null) {
            return;
        }

        if (desbloqueada) {

            imagen.setImageResource(imagenDesbloqueada);
            imagen.setAlpha(1.0f);

            tarjeta.setAlpha(1.0f);

            estado.setText("Desbloqueada");
            estado.setTextColor(ContextCompat.getColor(requireContext(), R.color.buti_exito));

            // Aro dorado cuando la insignia ya es tuya.
            if (tarjeta instanceof MaterialCardView) {
                ((MaterialCardView) tarjeta).setStrokeColor(
                        ContextCompat.getColor(requireContext(), R.color.buti_dorado));
                ((MaterialCardView) tarjeta).setStrokeWidth(dp(2));
            }

        } else {

            imagen.setImageResource(R.drawable.ic_candado3);
            imagen.setAlpha(0.65f);

            tarjeta.setAlpha(0.75f);

            estado.setText("Pendiente");
            estado.setTextColor(ContextCompat.getColor(requireContext(), R.color.buti_texto_secundario));

            if (tarjeta instanceof MaterialCardView) {
                ((MaterialCardView) tarjeta).setStrokeColor(
                        ContextCompat.getColor(requireContext(), R.color.buti_borde));
                ((MaterialCardView) tarjeta).setStrokeWidth(dp(1));
            }
        }
    }

    // =====================================================
    // ACTIVIDAD RECIENTE
    // =====================================================

    private void cargarActividadReciente(DataSnapshot progreso) {

        List<Actividad> actividades = new ArrayList<>();

        SimpleDateFormat formatoFirebase =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                );

        SimpleDateFormat formatoMostrar =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                );

        for (DataSnapshot nivel : progreso.getChildren()) {

            String nombreNivel = nivel.getKey();

            if (nombreNivel == null
                    || !nombreNivel.startsWith("nivel")) {
                continue;
            }

            for (DataSnapshot etapa : nivel.getChildren()) {

                Boolean completada = etapa.child("completada")
                        .getValue(Boolean.class);

                if (completada == null || !completada) {
                    continue;
                }

                String nombreEtapa = etapa.getKey();

                if (nombreEtapa == null) {
                    continue;
                }

                String fechaTexto = etapa.child("fecha")
                        .getValue(String.class);

                Date fecha = null;

                if (!TextUtils.isEmpty(fechaTexto)) {

                    try {

                        fecha = formatoFirebase.parse(fechaTexto);

                    } catch (ParseException e) {

                        android.util.Log.e(
                                "ProgresoFragment",
                                "Error al interpretar fecha: " + fechaTexto,
                                e
                        );
                    }
                }

                actividades.add(
                        new Actividad(
                                nombreNivel,
                                nombreEtapa,
                                fecha
                        )
                );
            }
        }

        // ORDENAR ACTIVIDADES POR FECHA

        Collections.sort(
                actividades,
                new Comparator<Actividad>() {

                    @Override
                    public int compare(Actividad a, Actividad b) {

                        if (a.fecha == null && b.fecha == null) {
                            return 0;
                        }

                        if (a.fecha == null) {
                            return 1;
                        }

                        if (b.fecha == null) {
                            return -1;
                        }

                        return b.fecha.compareTo(a.fecha);
                    }
                }
        );

        // SI NO EXISTEN ACTIVIDADES

        if (actividades.isEmpty()) {

            txtActividadReciente.setText(
                    "Todavía no tienes actividades registradas."
            );

            return;
        }

        // MOSTRAR LAS TRES MÁS RECIENTES

        // Palomita real (icono) en lugar del símbolo "✓".
        SpannableStringBuilder resultado = new SpannableStringBuilder();

        Drawable palomita = ContextCompat.getDrawable(requireContext(), R.drawable.ic_req_ok);

        if (palomita != null) {
            palomita = palomita.mutate();
            palomita.setTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.buti_exito)));
            palomita.setBounds(0, 0, dp(16), dp(16));
        }

        int limite = Math.min(3, actividades.size());

        for (int i = 0; i < limite; i++) {

            Actividad actividad = actividades.get(i);

            String nivel = actividad.nivel.replace("nivel", "");
            String etapa = actividad.etapa.replace("etapa", "");

            if (palomita != null) {
                int inicio = resultado.length();
                resultado.append("✓");
                resultado.setSpan(new ImageSpan(palomita, ImageSpan.ALIGN_BASELINE),
                        inicio, inicio + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                resultado.append("  ");
            }

            resultado.append("Nivel ")
                    .append(nivel)
                    .append(" · Etapa ")
                    .append(etapa);

            if (actividad.fecha != null) {

                resultado.append("\n      ")
                        .append(formatoMostrar.format(actividad.fecha));

            } else {

                resultado.append("\n      Fecha no disponible");
            }

            if (i < limite - 1) {
                resultado.append("\n\n");
            }
        }

        txtActividadReciente.setText(resultado);
    }

    // =====================================================
    // GRÁFICAS
    // =====================================================

    private void cargarGraficas(@Nullable DataSnapshot progreso,
                                @Nullable DataSnapshot usuario) {

        // ---------- Puntos de los últimos 7 días ----------

        String[] dias = new String[7];
        int[] puntosPorDia = new int[7];
        String[] inicialDia = {"D", "L", "M", "X", "J", "V", "S"};

        String[] claves = new String[7];

        // Datos nuevos: puntos guardados por día (lecciones, práctica y juegos).
        boolean hayPuntosPorDia = usuario != null && usuario.hasChild("puntosPorDia");

        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {

            claves[i] = ProgresoButi.diaDe(calendario);

            if (hayPuntosPorDia) {
                puntosPorDia[i] = ProgresoButi.puntosDelDia(usuario, claves[i]);
            }

            dias[i] = (i == 6)
                    ? "Hoy"
                    : inicialDia[calendario.get(Calendar.DAY_OF_WEEK) - 1];

            calendario.add(Calendar.DAY_OF_YEAR, 1);
        }

        // ---------- Etapas completadas por nivel ----------

        String[] nombresNiveles = new String[TOTAL_NIVELES];
        int[] etapasPorNivel = new int[TOTAL_NIVELES];

        for (int i = 0; i < TOTAL_NIVELES; i++) {
            nombresNiveles[i] = String.valueOf(i + 1);
        }

        if (progreso != null) {

            for (DataSnapshot nivel : progreso.getChildren()) {

                String nombreNivel = nivel.getKey();

                if (nombreNivel == null
                        || !nombreNivel.startsWith("nivel")) {
                    continue;
                }

                int numeroNivel;

                try {
                    numeroNivel = Integer.parseInt(
                            nombreNivel.replace("nivel", ""));
                } catch (NumberFormatException e) {
                    continue;
                }

                for (DataSnapshot etapa : nivel.getChildren()) {

                    Boolean completada = etapa.child("completada")
                            .getValue(Boolean.class);

                    if (completada == null || !completada) {
                        continue;
                    }

                    // Avance por nivel
                    if (numeroNivel >= 1 && numeroNivel <= TOTAL_NIVELES) {
                        etapasPorNivel[numeroNivel - 1]++;
                    }

                    // Puntos por día (la fecha se guarda al completar la etapa)
                    String fecha = etapa.child("fecha").getValue(String.class);
                    Long puntos = etapa.child("puntos").getValue(Long.class);

                    if (hayPuntosPorDia || fecha == null || fecha.length() < 10) {
                        continue;
                    }

                    String dia = fecha.substring(0, 10);

                    for (int i = 0; i < 7; i++) {
                        if (claves[i].equals(dia)) {
                            puntosPorDia[i] += puntos == null ? 0 : puntos.intValue();
                            break;
                        }
                    }
                }
            }
        }

        // ---------- Mostrar ----------

        int totalSemana = 0;
        for (int p : puntosPorDia) totalSemana += p;

        if (graficaSemana != null) {
            graficaSemana.setDatos(dias, puntosPorDia, 6, 0);
        }

        if (txtResumenSemana != null) {
            txtResumenSemana.setText(totalSemana == 0
                    ? "Aún no tienes puntos esta semana. ¡Completa una lección, práctica o juego!"
                    : "Ganaste " + totalSemana + " puntos en los últimos 7 días.");
        }

        int etapasTotales = 0;
        for (int e : etapasPorNivel) etapasTotales += e;

        if (graficaNiveles != null) {
            graficaNiveles.setDatos(nombresNiveles, etapasPorNivel, -1, ETAPAS_POR_NIVEL);
        }

        if (txtResumenNiveles != null) {
            txtResumenNiveles.setText("Etapas completadas: " + etapasTotales
                    + " de " + (TOTAL_NIVELES * ETAPAS_POR_NIVEL));
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    // =====================================================
    // MODELO DE ACTIVIDAD
    // =====================================================

    private static class Actividad {

        String nivel;
        String etapa;
        Date fecha;

        Actividad(
                String nivel,
                String etapa,
                Date fecha) {

            this.nivel = nivel;
            this.etapa = etapa;
            this.fecha = fecha;
        }
    }

    // =====================================================
    // LIMPIAR LISTENER
    // =====================================================

    @Override
    public void onDestroyView() {

        if (referenciaUsuario != null
                && listenerProgreso != null) {

            referenciaUsuario.removeEventListener(listenerProgreso);
        }

        referenciaUsuario = null;
        listenerProgreso = null;

        super.onDestroyView();
    }
}