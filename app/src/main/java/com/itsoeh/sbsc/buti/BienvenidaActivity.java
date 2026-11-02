package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class BienvenidaActivity extends AppCompatActivity {

    private BienvenidaAnimador animador;

    private Button btnComenzar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bienvenida);

        // Obtener la raíz de la pantalla
        View raiz = findViewById(R.id.rootBienvenida);

        // Inicializar el animador
        animador = new BienvenidaAnimador(raiz);

        // Obtener el botón
        btnComenzar = findViewById(R.id.btnComenzar);

        // Acción del botón Comenzar
        btnComenzar.setOnClickListener(v -> {

            // Ir a la pantalla de inicio de sesión
            Intent intent = new Intent(
                    BienvenidaActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

            // Transición suave entre pantallas
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );

            // Finalizar bienvenida
            finish();
        });
    }

    // =========================================
    // REPRODUCIR ANIMACIONES
    // =========================================

    @Override
    protected void onResume() {
        super.onResume();

        if (animador != null) {
            animador.reproducir();
        }
    }

    // =========================================
    // DETENER ANIMACIONES
    // =========================================

    @Override
    protected void onPause() {

        if (animador != null) {
            animador.detener();
        }

        super.onPause();
    }

    // =========================================
    // LIBERAR RECURSOS
    // =========================================

    @Override
    protected void onDestroy() {

        if (animador != null) {
            animador.detener();
            animador = null;
        }

        super.onDestroy();
    }
}
