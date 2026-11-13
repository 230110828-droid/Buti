package com.itsoeh.sbsc.buti;

import android.media.MediaPlayer;

import androidx.annotation.Nullable;

/**
 * Coordina los sonidos de B'ÜTI para que no se encimen:
 *  · Solo suena un audio de palabra a la vez.
 *  · Cuando empieza un audio en hñähñu, el lector de voz se calla.
 *  · Si tocas algo mientras suena el audio, el lector lo lee
 *    cuando el audio termina.
 *
 * Uso en cada pantalla que reproduce audio:
 *   AudioButi.empezar(reproductor);   // justo antes de reproductor.start()
 *   AudioButi.termino(reproductor);   // al terminar o al liberar el reproductor
 */
public final class AudioButi {

    @Nullable
    private static MediaPlayer actual;

    private AudioButi() {
    }

    /** Avisa que este audio va a sonar: calla al lector y pausa otro audio. */
    public static void empezar(@Nullable MediaPlayer reproductor) {

        if (reproductor == null) return;

        // El audio en hñähñu tiene prioridad sobre el lector de voz.
        LectorVoz.callarPorAudio();

        MediaPlayer anterior = actual;
        actual = reproductor;

        if (anterior != null && anterior != reproductor) {
            try {
                if (anterior.isPlaying()) anterior.pause();
            } catch (IllegalStateException ignored) {
                // Ya estaba liberado.
            }
        }
    }

    /** Avisa que este audio terminó o se detuvo. */
    public static void termino(@Nullable MediaPlayer reproductor) {

        if (reproductor == null || actual != reproductor) return;

        actual = null;

        // Si se tocó algo mientras sonaba, ahora sí se lee.
        LectorVoz.leerLoQueEsperaba();
    }

    /** ¿Está sonando un audio de palabra en este momento? */
    public static boolean sonando() {

        if (actual == null) return false;

        try {
            return actual.isPlaying();
        } catch (IllegalStateException e) {
            actual = null;
            return false;
        }
    }
}
