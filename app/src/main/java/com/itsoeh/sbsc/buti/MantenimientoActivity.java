package com.itsoeh.sbsc.buti;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Pantalla de mantenimiento. Escucha Firebase en vivo:
 * en cuanto el admin lo desactiva, regresa sola a la app.
 */
public class MantenimientoActivity extends AppCompatActivity {

    private DatabaseReference refMantenimiento;
    private ValueEventListener escucha;
    private TextView txtEstado;
    private ObjectAnimator flotar;
    private boolean saliendo = false;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(PreferenciasButi.contextoConLetra(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (PreferenciasButi.negritaActiva(this)) {
            getTheme().applyStyle(R.style.ThemeOverlay_Buti_TextoNegrita, true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mantenimiento);

        txtEstado = findViewById(R.id.txtEstadoMantenimiento);

        // El colibrí "descansa" flotando.
        View colibri = findViewById(R.id.imgLogoMantenimiento);
        flotar = ObjectAnimator.ofFloat(colibri, View.TRANSLATION_Y, -10f, 10f);
        flotar.setDuration(1600);
        flotar.setRepeatMode(ValueAnimator.REVERSE);
        flotar.setRepeatCount(ValueAnimator.INFINITE);
        flotar.setInterpolator(new AccelerateDecelerateInterpolator());
        flotar.start();

        findViewById(R.id.btnVerificarEstadoMantenimiento).setOnClickListener(v -> verificarEstadoSistema());

        findViewById(R.id.btnCerrarSesionMantenimiento).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MantenimientoActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bloquear el botón atrás para no saltarse la pantalla de mantenimiento.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        // Escuchar en vivo: si el admin lo desactiva, entrar solo.
        refMantenimiento = FirebaseDatabase.getInstance().getReference("config/mantenimiento");

        escucha = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                    entrarALaApp();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        refMantenimiento.addValueEventListener(escucha);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (flotar != null) flotar.cancel();

        if (refMantenimiento != null && escucha != null) {
            refMantenimiento.removeEventListener(escucha);
        }
    }

    private void verificarEstadoSistema() {

        ponerEstado("Revisando…", R.color.buti_titulo);

        refMantenimiento.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                    ponerEstado("Seguimos trabajando. Gracias por tu paciencia.", R.color.buti_rosa);
                    LectorVoz.hablar(MantenimientoActivity.this,
                            "B'ÜTI sigue en mantenimiento. Gracias por tu paciencia.");
                } else {
                    entrarALaApp();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ponerEstado("No se pudo revisar. Revisa tu conexión a internet.", R.color.buti_rosa);
            }
        });
    }

    private void entrarALaApp() {

        if (saliendo || isFinishing()) return;
        saliendo = true;

        getSharedPreferences("buti_prefs", MODE_PRIVATE)
                .edit().putBoolean("modo_mantenimiento", false).apply();

        new ButiDialogo(this).mostrarExito(
                "¡B'ÜTI ya está lista!",
                "Terminamos el mantenimiento. Puedes seguir aprendiendo hñähñu.",
                () -> {
                    Intent intent = new Intent(MantenimientoActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
        );
    }

    private void ponerEstado(String texto, int color) {
        txtEstado.setText(texto);
        txtEstado.setTextColor(ContextCompat.getColor(this, color));
    }
}