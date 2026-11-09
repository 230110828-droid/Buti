package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MantenimientoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mantenimiento);

        Button btnVerificarEstado = findViewById(R.id.btnVerificarEstadoMantenimiento);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesionMantenimiento);

        btnVerificarEstado.setOnClickListener(v -> verificarEstadoSistema());

        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MantenimientoActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bloquear el botón atrás para evitar omitir la pantalla de mantenimiento
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });
    }

    private void verificarEstadoSistema() {
        Toast.makeText(this, "Verificando disponibilidad del sistema...", Toast.LENGTH_SHORT).show();

        DatabaseReference refMantenimiento = FirebaseDatabase.getInstance().getReference("config/mantenimiento");
        refMantenimiento.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean enMantenimiento = snapshot.getValue(Boolean.class);
                if (enMantenimiento != null && !enMantenimiento) {
                    // Mantenimiento desactivado: redirigir a MainActivity
                    Toast.makeText(MantenimientoActivity.this, "¡El sistema ya se encuentra disponible!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MantenimientoActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MantenimientoActivity.this, "El sistema continúa en mantenimiento. Gracias por tu paciencia.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MantenimientoActivity.this, "El sistema continúa en mantenimiento.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
