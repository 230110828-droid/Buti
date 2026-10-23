package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class BienvenidaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        Button btnComenzar = findViewById(R.id.btnComenzar);

        btnComenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Abrir la pantalla de inicio de sesión.
                Intent intent = new Intent(
                        BienvenidaActivity.this,
                        LoginActivity.class
                );

                startActivity(intent);

                // Transición suave entre pantallas.
                overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );

                // Cerrar la bienvenida para no regresar a ella
                // al pulsar el botón Atrás desde el login.
                finish();
            }
        });
    }
}
