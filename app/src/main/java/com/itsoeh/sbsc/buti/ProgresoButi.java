package com.itsoeh.sbsc.buti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Lugar ÚNICO donde B'ÜTI guarda puntos y racha.
 *
 * Estructura en Firebase (usuarios/{uid}):
 *   progresoLecciones/nivelN/etapaM  -> completada, puntos (mejor), intentos, fecha
 *   puntosActividades                -> puntos de práctica y juegos
 *   puntosPorDia/{yyyy-MM-dd}         -> puntos ganados ese día (gráfica semanal)
 *   racha/diasActuales, ultimaFecha, mejorRacha
 *
 * Puntos totales = suma del mejor puntaje de cada etapa + puntosActividades.
 * Repetir una etapa solo suma si mejoras tu puntaje (no se pueden "inflar").
 */
public final class ProgresoButi {

    // Puntos por respuesta
    public static final int PUNTOS_ACIERTO = 10;   // respuesta correcta
    public static final int PUNTOS_REPASO = 5;     // acierto en la ronda de repaso

    // Práctica libre: vale menos que las lecciones
    public static final int PUNTOS_PRACTICA = 5;
    public static final int PUNTOS_PRACTICA_REPASO = 2;

    // Puntos de los juegos (al terminar una partida)
    public static final int PUNTOS_MEMORAMA = 15;
    public static final int PUNTOS_DIFERENCIAS = 15;
    public static final int PUNTOS_ROMPECABEZAS_POR_NIVEL = 10; // 3x3=10, 4x4=20, 5x5=30
    public static final int PUNTOS_ATRAPA_POR_ACIERTO = 5;

    /** Respuesta de Firebase: cuántos puntos se sumaron y la racha actual. */
    public interface AlGuardar {
        void listo(int puntosSumados, int racha);
    }

    private ProgresoButi() {
    }

    // =========================================
    // FECHAS
    // =========================================

    private static final String FORMATO_DIA = "yyyy-MM-dd";

    public static String hoy() {
        return new SimpleDateFormat(FORMATO_DIA, Locale.US).format(new Date());
    }

    public static String ayer() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1);
        return new SimpleDateFormat(FORMATO_DIA, Locale.US).format(c.getTime());
    }

    public static String diaDe(Calendar calendario) {
        return new SimpleDateFormat(FORMATO_DIA, Locale.US).format(calendario.getTime());
    }

    @Nullable
    private static DatabaseReference usuario() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) return null;
        return FirebaseDatabase.getInstance().getReference("usuarios").child(u.getUid());
    }

    // =========================================
    // GUARDAR: LECCIONES
    // =========================================

    /**
     * Guarda el resultado de una etapa.
     * - "puntos" guarda tu MEJOR puntaje de esa etapa.
     * - Solo se suma a tu total la mejora (si repites y sacas menos, no resta ni suma).
     * - La etapa queda completada si la aprobaste alguna vez.
     * - Siempre cuenta para la racha del día.
     */
    public static void guardarEtapa(
            int nivel,
            int etapa,
            int puntos,
            boolean aprobada,
            @Nullable AlGuardar alGuardar) {

        DatabaseReference ref = usuario();

        if (ref == null) {
            if (alGuardar != null) alGuardar.listo(0, 0);
            return;
        }

        final int[] mejora = {0};

        ref.child("progresoLecciones")
                .child("nivel" + nivel)
                .child("etapa" + etapa)
                .runTransaction(new Transaction.Handler() {

                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData datos) {

                        Boolean completada = datos.child("completada").getValue(Boolean.class);
                        Integer anterior = datos.child("puntos").getValue(Integer.class);
                        Integer intentos = datos.child("intentos").getValue(Integer.class);

                        int mejorAnterior = anterior == null ? 0 : anterior;
                        int mejorNuevo = Math.max(mejorAnterior, puntos);

                        mejora[0] = mejorNuevo - mejorAnterior;

                        datos.child("puntos").setValue(mejorNuevo);
                        datos.child("mejorPuntuacion").setValue(mejorNuevo);
                        datos.child("intentos").setValue(intentos == null ? 1 : intentos + 1);
                        datos.child("completada").setValue(Boolean.TRUE.equals(completada) || aprobada);

                        if (aprobada) {
                            datos.child("fecha").setValue(
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                                            .format(new Date()));
                        }

                        return Transaction.success(datos);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error,
                                           boolean confirmado,
                                           @Nullable DataSnapshot snapshot) {

                        int sumados = (error == null && confirmado) ? mejora[0] : 0;

                        sumarAlDia(ref, sumados);
                        registrarRacha(ref, racha -> {
                            if (alGuardar != null) alGuardar.listo(sumados, racha);
                        });
                    }
                });
    }

    // =========================================
    // GUARDAR: PRÁCTICA Y JUEGOS
    // =========================================

    /** Suma puntos de práctica o de un juego y cuenta para la racha. */
    public static void sumarPuntosActividad(int puntos, @Nullable AlGuardar alGuardar) {

        DatabaseReference ref = usuario();

        if (ref == null) {
            if (alGuardar != null) alGuardar.listo(0, 0);
            return;
        }

        final int suma = Math.max(0, puntos);

        ref.child("puntosActividades").runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData datos) {
                Integer actual = datos.getValue(Integer.class);
                datos.setValue((actual == null ? 0 : actual) + suma);
                return Transaction.success(datos);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error,
                                   boolean confirmado,
                                   @Nullable DataSnapshot snapshot) {

                int sumados = (error == null && confirmado) ? suma : 0;

                sumarAlDia(ref, sumados);
                registrarRacha(ref, racha -> {
                    if (alGuardar != null) alGuardar.listo(sumados, racha);
                });
            }
        });
    }

    // =========================================
    // PUNTOS DEL DÍA (gráfica semanal)
    // =========================================

    private static void sumarAlDia(DatabaseReference ref, int puntos) {

        if (puntos <= 0) return;

        ref.child("puntosPorDia").child(hoy()).runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData datos) {
                Integer actual = datos.getValue(Integer.class);
                datos.setValue((actual == null ? 0 : actual) + puntos);
                return Transaction.success(datos);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean c,
                                   @Nullable DataSnapshot s) {
            }
        });
    }

    // =========================================
    // RACHA: sube una vez por día
    // =========================================

    private interface AlRacha {
        void listo(int racha);
    }

    // true cuando la racha se acaba de registrar por primera vez hoy.
    private static volatile boolean rachaNuevaHoy = false;

    /**
     * ¿La última actividad fue la primera del día? (para mostrar la
     * pantalla de racha). Se "consume": solo devuelve true una vez.
     */
    public static boolean tomarRachaNueva() {
        boolean nueva = rachaNuevaHoy;
        rachaNuevaHoy = false;
        return nueva;
    }

    private static void registrarRacha(DatabaseReference ref, AlRacha alRacha) {

        final String hoy = hoy();
        final String ayer = ayer();
        // {racha, 1 si es la primera actividad de hoy}
        final int[] resultado = {0, 0};

        ref.child("racha").runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData datos) {

                String ultima = datos.child("ultimaFecha").getValue(String.class);
                Integer dias = datos.child("diasActuales").getValue(Integer.class);
                Integer mejor = datos.child("mejorRacha").getValue(Integer.class);

                int actuales = dias == null ? 0 : dias;

                // Ya contaba hoy: no sube otra vez.
                if (hoy.equals(ultima)) {
                    resultado[0] = Math.max(1, actuales);
                    resultado[1] = 0;
                    return Transaction.success(datos);
                }

                // Ayer sí practicó: sigue la racha. Si no, empieza de nuevo.
                int nueva = ayer.equals(ultima) ? actuales + 1 : 1;

                datos.child("diasActuales").setValue(nueva);
                datos.child("ultimaFecha").setValue(hoy);
                datos.child("mejorRacha").setValue(Math.max(mejor == null ? 0 : mejor, nueva));

                resultado[0] = nueva;
                resultado[1] = 1;
                return Transaction.success(datos);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error,
                                   boolean confirmado,
                                   @Nullable DataSnapshot snapshot) {
                if (error == null && confirmado && resultado[1] == 1) {
                    rachaNuevaHoy = true;
                }
                alRacha.listo(resultado[0]);
            }
        });
    }

    // =========================================
    // LEER (Inicio, Lecciones, Progreso, Perfil)
    // =========================================

    /** Puntos totales: mejor puntaje de cada etapa + práctica y juegos. */
    public static int puntosTotales(DataSnapshot usuario) {

        int total = 0;

        for (DataSnapshot nivel : usuario.child("progresoLecciones").getChildren()) {

            String clave = nivel.getKey();
            if (clave == null || !clave.startsWith("nivel")) continue;

            for (DataSnapshot etapa : nivel.getChildren()) {
                Long puntos = etapa.child("puntos").getValue(Long.class);
                if (puntos != null && puntos > 0) total += puntos.intValue();
            }
        }

        Long actividades = usuario.child("puntosActividades").getValue(Long.class);
        if (actividades != null && actividades > 0) total += actividades.intValue();

        return total;
    }

    /**
     * Racha que se debe mostrar: si el último día con actividad
     * no fue hoy ni ayer, la racha ya se rompió y se muestra 0.
     */
    public static int rachaVigente(DataSnapshot usuario) {

        DataSnapshot racha = usuario.child("racha");

        String ultima = racha.child("ultimaFecha").getValue(String.class);
        Long dias = racha.child("diasActuales").getValue(Long.class);

        if (dias == null || ultima == null) return 0;

        if (ultima.equals(hoy()) || ultima.equals(ayer())) {
            return dias.intValue();
        }

        return 0;
    }

    /** Puntos ganados en un día (yyyy-MM-dd). */
    public static int puntosDelDia(DataSnapshot usuario, String dia) {
        Long puntos = usuario.child("puntosPorDia").child(dia).getValue(Long.class);
        return puntos == null ? 0 : puntos.intValue();
    }
}