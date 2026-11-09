package com.itsoeh.sbsc.buti;

import android.app.Application;

/**
 * Clase de la aplicación: aplica el tema guardado (claro, oscuro o
 * según el celular) antes de que se abra cualquier pantalla.
 *
 * IMPORTANTE: en AndroidManifest.xml agrega dentro de <application ...>
 *     android:name=".ButiApp"
 */
public class ButiApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenciasButi.aplicarTemaGuardado(this);
    }
}
