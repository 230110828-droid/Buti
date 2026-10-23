package com.itsoeh.sbsc.buti;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Activity del splash: pantalla completa, sin menú ni barra de navegación.
 * Solo aloja el fragment "splash".
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pantalla inmersiva: oculta barra de estado y de navegación del sistema
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat control =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        control.hide(WindowInsetsCompat.Type.systemBars());
        control.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}