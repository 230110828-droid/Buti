package com.itsoeh.sbsc.buti;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Guarda y aplica las preferencias de B'ÜTI:
 *  - Tema: según el celular, claro u oscuro.
 *  - Tamaño de letra: normal, grande o muy grande.
 *  - Accesibilidad: lector de voz y texto en negrita.
 */
public final class PreferenciasButi {

    private static final String ARCHIVO = "ButiPreferencias";
    private static final String CLAVE_TEMA = "tema";
    private static final String CLAVE_LETRA = "tamanoLetra";
    private static final String CLAVE_LECTOR = "lectorVoz";
    private static final String CLAVE_NEGRITA = "textoNegrita";

    public static final int TEMA_SISTEMA = 0;
    public static final int TEMA_CLARO = 1;
    public static final int TEMA_OSCURO = 2;

    public static final int LETRA_NORMAL = 0;
    public static final int LETRA_GRANDE = 1;
    public static final int LETRA_MUY_GRANDE = 2;

    // Cuánto crece la letra en cada opción.
    private static final float[] ESCALAS = {1.0f, 1.15f, 1.30f};

    private PreferenciasButi() {
    }

    private static SharedPreferences preferencias(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    // =========================================
    // TEMA
    // =========================================

    public static int obtenerTema(Context context) {
        return preferencias(context).getInt(CLAVE_TEMA, TEMA_SISTEMA);
    }

    public static void guardarTema(Context context, int tema) {
        preferencias(context).edit().putInt(CLAVE_TEMA, tema).apply();
        aplicarTema(tema);
    }

    /** Se llama al abrir la app (ButiApp). */
    public static void aplicarTemaGuardado(Context context) {
        aplicarTema(obtenerTema(context));
    }

    private static void aplicarTema(int tema) {

        int modo;

        if (tema == TEMA_CLARO) {
            modo = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (tema == TEMA_OSCURO) {
            modo = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            modo = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        // Android vuelve a dibujar las pantallas abiertas con el tema nuevo.
        if (AppCompatDelegate.getDefaultNightMode() != modo) {
            AppCompatDelegate.setDefaultNightMode(modo);
        }
    }

    // =========================================
    // TAMAÑO DE LETRA
    // =========================================

    public static int obtenerLetra(Context context) {
        int letra = preferencias(context).getInt(CLAVE_LETRA, LETRA_NORMAL);
        return (letra < 0 || letra >= ESCALAS.length) ? LETRA_NORMAL : letra;
    }

    public static void guardarLetra(Context context, int letra) {
        preferencias(context).edit().putInt(CLAVE_LETRA, letra).apply();
    }

    // =========================================
    // ACCESIBILIDAD
    // =========================================

    /** Lector de voz: lee en voz alta lo que tocas. */
    public static boolean lectorActivo(Context context) {
        return preferencias(context).getBoolean(CLAVE_LECTOR, false);
    }

    public static void guardarLector(Context context, boolean activo) {
        preferencias(context).edit().putBoolean(CLAVE_LECTOR, activo).apply();
    }

    /** Texto en negrita: todo el texto se ve más marcado. */
    public static boolean negritaActiva(Context context) {
        return preferencias(context).getBoolean(CLAVE_NEGRITA, false);
    }

    public static void guardarNegrita(Context context, boolean activo) {
        preferencias(context).edit().putBoolean(CLAVE_NEGRITA, activo).apply();
    }

    /**
     * Devuelve un contexto con la letra más grande o más chica.
     * Se usa en attachBaseContext de MainActivity.
     * Respeta también el tamaño de letra que la persona tiene en su celular.
     */
    public static Context contextoConLetra(Context base) {

        float escala = ESCALAS[obtenerLetra(base)];

        if (escala == 1.0f) {
            return base;
        }

        Configuration configuracion =
                new Configuration(base.getResources().getConfiguration());

        configuracion.fontScale = configuracion.fontScale * escala;

        return base.createConfigurationContext(configuracion);
    }
}