package com.itsoeh.sbsc.buti;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.itsoeh.sbsc.buti.vocabulario.PalabraButi;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

/**
 * Sonidos de los juegos de B'ÜTI.
 *  · Efectos cortos (res/raw): tic, acierto, error y victoria.
 *  · La palabra en hñähñu como premio al acertar.
 * Se pueden apagar en Configuración → «Sonidos de los juegos».
 * La palabra pasa por AudioButi para no encimarse con el lector de voz.
 *
 *   SonidosJuego.preparar(contexto);                 // al abrir el juego
 *   SonidosJuego.tocar(contexto, SonidosJuego.Efecto.ACIERTO);
 *   SonidosJuego.decirPalabra(contexto, "animales", "Zorro", 450);
 *   SonidosJuego.detener();                           // al salir del juego
 */
public final class SonidosJuego {

    public enum Efecto { TIC, ACIERTO, ERROR, VICTORIA }

    private static final String ARCHIVO = "buti_prefs";
    private static final String CLAVE = "sonidos_juegos";

    private static SoundPool efectos;
    private static final int[] ids = new int[4];
    private static final boolean[] listos = new boolean[4];

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable palabraProgramada;
    private static MediaPlayer palabra;

    private SonidosJuego() {
    }

    // =========================================
    // CONFIGURACIÓN
    // =========================================

    public static boolean activos(Context context) {
        return preferencias(context).getBoolean(CLAVE, true);
    }

    public static void guardarActivos(Context context, boolean activos) {
        preferencias(context).edit().putBoolean(CLAVE, activos).apply();
        if (!activos) detener();
    }

    private static SharedPreferences preferencias(Context context) {
        return context.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    // =========================================
    // EFECTOS
    // =========================================

    /** Carga los efectos (llámalo al abrir un juego para que suenen sin retraso). */
    public static synchronized void preparar(Context context) {

        if (efectos != null) return;

        Context app = context.getApplicationContext();

        efectos = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();

        efectos.setOnLoadCompleteListener((pool, id, estado) -> {
            for (int i = 0; i < ids.length; i++) {
                if (ids[i] == id) listos[i] = estado == 0;
            }
        });

        ids[Efecto.TIC.ordinal()] = efectos.load(app, R.raw.juego_tic, 1);
        ids[Efecto.ACIERTO.ordinal()] = efectos.load(app, R.raw.juego_acierto, 1);
        ids[Efecto.ERROR.ordinal()] = efectos.load(app, R.raw.juego_error, 1);
        ids[Efecto.VICTORIA.ordinal()] = efectos.load(app, R.raw.juego_victoria, 1);
    }

    public static void tocar(Context context, Efecto efecto) {

        if (context == null || !activos(context)) return;

        preparar(context);

        int i = efecto.ordinal();

        if (efectos != null && listos[i]) {
            float volumen = efecto == Efecto.TIC ? 0.6f : 0.9f;
            efectos.play(ids[i], volumen, volumen, 1, 0, 1f);
        }
    }

    // =========================================
    // PALABRA EN HÑÄHÑU
    // =========================================

    /** La palabra escrita en hñähñu (o null si no está en el vocabulario). */
    @Nullable
    public static String hnahnu(@Nullable String categoria, @Nullable String espanol) {
        PalabraButi p = VocabularioNube.buscar(categoria, espanol);
        return p == null || p.hnahnu == null || p.hnahnu.trim().isEmpty() ? null : p.hnahnu;
    }

    /** Pronuncia la palabra en hñähñu después de un pequeño retraso (para que antes suene el efecto). */
    public static void decirPalabra(Context context, @Nullable String categoria,
                                    @Nullable String espanol, long retraso) {

        if (context == null || espanol == null || !activos(context)) return;

        Context app = context.getApplicationContext();

        cancelarProgramada();

        palabraProgramada = () -> {

            palabraProgramada = null;
            liberarPalabra();

            MediaPlayer nuevo = VocabularioNube.crearReproductor(app, categoria, espanol);
            if (nuevo == null) return;

            palabra = nuevo;

            nuevo.setOnCompletionListener(mp -> {
                if (palabra == mp) liberarPalabra();
            });

            try {
                AudioButi.empezar(nuevo);
                nuevo.start();
            } catch (IllegalStateException e) {
                liberarPalabra();
            }
        };

        handler.postDelayed(palabraProgramada, Math.max(0, retraso));
    }

    /** Detiene la palabra pendiente o sonando (al salir o reiniciar el juego). */
    public static void detener() {
        cancelarProgramada();
        liberarPalabra();
    }

    private static void cancelarProgramada() {
        if (palabraProgramada != null) {
            handler.removeCallbacks(palabraProgramada);
            palabraProgramada = null;
        }
    }

    private static void liberarPalabra() {

        if (palabra == null) return;

        MediaPlayer anterior = palabra;
        palabra = null;

        AudioButi.termino(anterior);

        try {
            anterior.setOnCompletionListener(null);
            if (anterior.isPlaying()) anterior.stop();
            anterior.release();
        } catch (Exception ignored) {
        }
    }
}
