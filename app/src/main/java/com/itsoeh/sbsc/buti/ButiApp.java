package com.itsoeh.sbsc.buti;

import android.app.Application;

import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

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

        // Vocabulario desde Firebase (con copia local para usarlo sin internet).
        VocabularioNube.iniciar(this);
    }
}