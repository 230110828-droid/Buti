package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GoogleOnboardingActivity extends AppCompatActivity {

    private LinearLayout layoutGooglePaso1, layoutGooglePaso2, layoutGooglePaso3, layoutGooglePaso4;
    private LinearLayout cardCuentaGoogle;
    private TextView txtUsarOtraCuenta;

    private RadioGroup rgMotivoAprendizaje;
    private Button btnContinuarMotivo;

    private Button btnComenzarLeccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_onboarding);

        layoutGooglePaso1 = findViewById(R.id.layoutGooglePaso1);
        layoutGooglePaso2 = findViewById(R.id.layoutGooglePaso2);
        layoutGooglePaso3 = findViewById(R.id.layoutGooglePaso3);
        layoutGooglePaso4 = findViewById(R.id.layoutGooglePaso4);

        cardCuentaGoogle = findViewById(R.id.cardCuentaGoogle);
        txtUsarOtraCuenta = findViewById(R.id.txtUsarOtraCuenta);

        rgMotivoAprendizaje = findViewById(R.id.rgMotivoAprendizaje);
        btnContinuarMotivo = findViewById(R.id.btnContinuarMotivo);

        btnComenzarLeccion = findViewById(R.id.btnComenzarLeccion);

        // Paso 1 -> Selección de cuenta
        cardCuentaGoogle.setOnClickListener(v -> simularConexion());
        txtUsarOtraCuenta.setOnClickListener(v -> finish());

        // Paso 3 -> Guardar motivo y avanzar
        btnContinuarMotivo.setOnClickListener(v -> {
            int selectedId = rgMotivoAprendizaje.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Por favor selecciona una opción", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutGooglePaso3.setVisibility(View.GONE);
            layoutGooglePaso4.setVisibility(View.VISIBLE);
        });

        // Paso 4 -> Abrir MainActivity
        btnComenzarLeccion.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void simularConexion() {
        layoutGooglePaso1.setVisibility(View.GONE);
        layoutGooglePaso2.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            layoutGooglePaso2.setVisibility(View.GONE);
            layoutGooglePaso3.setVisibility(View.VISIBLE);
        }, 1500);
    }
}