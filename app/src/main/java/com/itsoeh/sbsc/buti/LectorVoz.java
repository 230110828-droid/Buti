package com.itsoeh.sbsc.buti;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

/**
 * Lector de voz de B'ÜTI.
 * Si está activo en Configuración, lee en voz alta lo que tocas
 * (botones, tarjetas, textos) y los mensajes de la app.
 * Usa la voz en español del celular (no necesita internet).
 */
public final class LectorVoz {

    private static TextToSpeech voz;
    private static boolean lista = false;
    private static String pendiente = null;

    // Para no encimarse con los audios en hñähñu.
    private static final Handler principal = new Handler(Looper.getMainLooper());
    private static Runnable programado = null;
    private static String esperaAudio = null;
    private static Context contextoApp = null;

    private LectorVoz() {
    }

    // =========================================
    // VOZ
    // =========================================

    private static void iniciar(Context context) {

        if (voz != null) return;

        voz = new TextToSpeech(context.getApplicationContext(), estado -> {

            if (estado != TextToSpeech.SUCCESS || voz == null) {
                return;
            }

            int resultado = voz.setLanguage(Locale.forLanguageTag("es-MX"));

            if (resultado == TextToSpeech.LANG_MISSING_DATA
                    || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                voz.setLanguage(Locale.forLanguageTag("es"));
            }

            voz.setSpeechRate(0.95f);
            lista = true;

            if (pendiente != null) {
                decir(pendiente);
                pendiente = null;
            }
        });
    }

    /**
     * Lee el texto solo si el lector está activo en Configuración.
     * Si está sonando un audio en hñähñu, espera a que termine.
     * Se lee con una pausa muy corta: si lo que se tocó es el botón de
     * audio, el audio empieza y esta lectura se cancela (no se enciman).
     */
    public static void hablar(Context context, CharSequence texto) {

        if (context == null || !PreferenciasButi.lectorActivo(context)
                || TextUtils.isEmpty(texto)) {
            return;
        }

        contextoApp = context.getApplicationContext();
        String limpio = limpiar(texto.toString());

        if (limpio.isEmpty()) return;

        cancelarProgramado();

        if (AudioButi.sonando()) {
            // Se lee cuando termine el audio (solo lo último que se tocó).
            esperaAudio = limpio;
            return;
        }

        programar(limpio, 180);
    }

    /** Lo llama AudioButi cuando empieza un audio: el lector se calla. */
    public static void callarPorAudio() {
        cancelarProgramado();
        esperaAudio = null;
        pendiente = null;
        detener();
    }

    /** Lo llama AudioButi cuando termina el audio. */
    public static void leerLoQueEsperaba() {

        if (esperaAudio == null || contextoApp == null
                || !PreferenciasButi.lectorActivo(contextoApp)) {
            esperaAudio = null;
            return;
        }

        String texto = esperaAudio;
        esperaAudio = null;
        programar(texto, 300);
    }

    private static void programar(String texto, long retraso) {

        cancelarProgramado();

        programado = () -> {
            programado = null;
            if (contextoApp != null && !AudioButi.sonando()) {
                hablarSiempre(contextoApp, texto);
            }
        };

        principal.postDelayed(programado, retraso);
    }

    private static void cancelarProgramado() {
        if (programado != null) {
            principal.removeCallbacks(programado);
            programado = null;
        }
    }

    /** Lee el texto aunque el lector esté apagado (para confirmar al activarlo). */
    public static void hablarSiempre(Context context, CharSequence texto) {

        if (context == null || TextUtils.isEmpty(texto)) return;

        String limpio = limpiar(texto.toString());

        if (limpio.isEmpty()) return;

        iniciar(context);

        if (!lista) {
            pendiente = limpio;
            return;
        }

        decir(limpio);
    }

    private static void decir(String texto) {
        if (voz != null) {
            voz.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "buti_lector");
        }
    }

    public static void detener() {
        if (voz != null) {
            voz.stop();
        }
    }

    private static String limpiar(String texto) {
        return texto
                .replace("\n", ". ")
                .replace("·", ",")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // =========================================
    // ¿QUÉ SE TOCÓ?
    // =========================================

    /**
     * Busca qué hay en el punto tocado (coordenadas de pantalla)
     * y devuelve el texto que se debe leer.
     */
    public static String textoEnPunto(View raiz, int x, int y) {

        View tocada = vistaEnPunto(raiz, x, y);

        if (tocada == null) return null;

        // Si lo tocado está dentro de algo que se puede tocar
        // (botón, tarjeta), se lee ese elemento completo.
        View actual = tocada;

        while (actual != null && actual != raiz) {

            if (actual.isClickable() || actual.isLongClickable()) {

                String texto = descripcion(actual);

                if (!TextUtils.isEmpty(texto)) {
                    return texto;
                }
                break;
            }

            if (!(actual.getParent() instanceof View)) break;
            actual = (View) actual.getParent();
        }

        // Si no, se lee el texto que se tocó.
        return descripcion(tocada);
    }

    private static View vistaEnPunto(View vista, int x, int y) {

        if (vista.getVisibility() != View.VISIBLE || vista.getAlpha() <= 0f) {
            return null;
        }

        int modo = vista.getImportantForAccessibility();

        if (modo == View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS) {
            return null;
        }

        int[] posicion = new int[2];
        vista.getLocationOnScreen(posicion);

        boolean dentro = x >= posicion[0]
                && x < posicion[0] + vista.getWidth()
                && y >= posicion[1]
                && y < posicion[1] + vista.getHeight();

        if (!dentro) return null;

        if (vista instanceof ViewGroup) {

            ViewGroup grupo = (ViewGroup) vista;

            // De arriba hacia abajo: lo último dibujado está encima.
            for (int i = grupo.getChildCount() - 1; i >= 0; i--) {

                View encontrada = vistaEnPunto(grupo.getChildAt(i), x, y);

                if (encontrada != null) {
                    return encontrada;
                }
            }
        }

        // Los iconos decorativos se saltan para leer lo que está debajo.
        if (modo == View.IMPORTANT_FOR_ACCESSIBILITY_NO) {
            return null;
        }

        return vista;
    }

    private static String descripcion(View vista) {

        CharSequence descripcion = vista.getContentDescription();

        if (!TextUtils.isEmpty(descripcion)) {
            return descripcion.toString();
        }

        if (vista instanceof EditText) {

            EditText campo = (EditText) vista;

            if (TextUtils.isEmpty(campo.getText())) {
                CharSequence pista = campo.getHint();
                return pista == null ? "Campo de texto" : "Campo: " + pista;
            }

            // No se leen contraseñas.
            int tipo = campo.getInputType();
            boolean esContrasena =
                    (tipo & 0xFFF) == (android.text.InputType.TYPE_CLASS_TEXT
                            | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            return esContrasena ? "Contraseña escrita" : campo.getText().toString();
        }

        if (vista instanceof TextView) {
            CharSequence texto = ((TextView) vista).getText();
            return texto == null ? null : texto.toString();
        }

        if (vista instanceof ViewGroup) {
            StringBuilder juntos = new StringBuilder();
            juntarTextos((ViewGroup) vista, juntos);
            return juntos.toString();
        }

        return null;
    }

    private static void juntarTextos(ViewGroup grupo, StringBuilder juntos) {

        for (int i = 0; i < grupo.getChildCount(); i++) {

            if (juntos.length() > 160) return;

            View hijo = grupo.getChildAt(i);

            if (hijo.getVisibility() != View.VISIBLE) continue;

            if (hijo instanceof TextView && !(hijo instanceof EditText)) {

                CharSequence texto = ((TextView) hijo).getText();

                if (!TextUtils.isEmpty(texto)) {
                    if (juntos.length() > 0) juntos.append(". ");
                    juntos.append(texto);
                }

            } else if (hijo instanceof ViewGroup) {
                juntarTextos((ViewGroup) hijo, juntos);
            }
        }
    }
}