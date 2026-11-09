package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Bienvenida para quien entra por primera vez con Google.
 * Pregunta el motivo, guarda al usuario en Realtime Database
 * (igual que el registro normal) y abre la app.
 */
public class GoogleOnboardingActivity extends AppCompatActivity {

    private LinearLayout layoutGooglePaso1, layoutGooglePaso2, layoutGooglePaso3, layoutGooglePaso4;

    private RadioGroup rgMotivoAprendizaje;
    private Button btnContinuarMotivo;
    private Button btnComenzarLeccion;

    private FirebaseUser usuario;
    private String nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_onboarding);

        usuario = FirebaseAuth.getInstance().getCurrentUser();

        // Solo se llega aquí después de elegir una cuenta de Google.
        if (usuario == null) {
            finish();
            return;
        }

        layoutGooglePaso1 = findViewById(R.id.layoutGooglePaso1);
        layoutGooglePaso2 = findViewById(R.id.layoutGooglePaso2);
        layoutGooglePaso3 = findViewById(R.id.layoutGooglePaso3);
        layoutGooglePaso4 = findViewById(R.id.layoutGooglePaso4);

        rgMotivoAprendizaje = findViewById(R.id.rgMotivoAprendizaje);
        btnContinuarMotivo = findViewById(R.id.btnContinuarMotivo);
        btnComenzarLeccion = findViewById(R.id.btnComenzarLeccion);

        // El selector real de Google reemplaza a los pasos 1 y 2.
        layoutGooglePaso1.setVisibility(View.GONE);
        layoutGooglePaso2.setVisibility(View.GONE);
        layoutGooglePaso3.setVisibility(View.VISIBLE);
        layoutGooglePaso4.setVisibility(View.GONE);

        // Nombre e iniciales reales de la cuenta de Google.
        nombre = usuario.getDisplayName();
        if (TextUtils.isEmpty(nombre)) {
            nombre = "Usuario";
        }

        String primerNombre = nombre.trim().split("\\s+")[0];
        String iniciales = obtenerIniciales(nombre);

        ((TextView) findViewById(R.id.txtInicialesPaso3)).setText(iniciales);
        ((TextView) findViewById(R.id.txtSaludoPaso3)).setText("¡Ya casi, " + primerNombre + "!");

        ((TextView) findViewById(R.id.txtInicialesPaso4)).setText(iniciales);
        ((TextView) findViewById(R.id.txtSaludoPaso4)).setText("¡Te damos la bienvenida, " + primerNombre + "!");

        // Paso 3 -> Guardar motivo y datos del usuario
        btnContinuarMotivo.setOnClickListener(v -> {

            int seleccion = rgMotivoAprendizaje.getCheckedRadioButtonId();

            if (seleccion == -1) {
                ButiAviso.mostrar(this, "Elige una opción para continuar.", ButiAviso.Tipo.INFO);
                return;
            }

            guardarUsuario(obtenerMotivo(seleccion));
        });

        // Paso 4 -> Abrir MainActivity
        btnComenzarLeccion.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Si sale sin terminar, se cierra la sesión para que
        // no entre a la app sin datos guardados.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (layoutGooglePaso4.getVisibility() != View.VISIBLE) {
                    FirebaseAuth.getInstance().signOut();
                }
                finish();
            }
        });
    }

    private void guardarUsuario(String motivo) {

        btnContinuarMotivo.setEnabled(false);

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("uid", usuario.getUid());
        datosUsuario.put("nombre", nombre);
        datosUsuario.put("correo", usuario.getEmail());
        datosUsuario.put("fechaRegistro", System.currentTimeMillis());
        datosUsuario.put("motivo", motivo);
        datosUsuario.put("proveedor", "google");

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .updateChildren(datosUsuario)
                .addOnCompleteListener(task -> {

                    btnContinuarMotivo.setEnabled(true);

                    if (task.isSuccessful()) {

                        layoutGooglePaso3.setVisibility(View.GONE);
                        layoutGooglePaso4.setVisibility(View.VISIBLE);

                    } else {

                        android.util.Log.e("GoogleOnboarding",
                                "No se pudo guardar el usuario", task.getException());

                        ButiAviso.mostrar(this,
                                "No pudimos guardar tus datos. Revisa tu internet e inténtalo de nuevo.",
                                ButiAviso.Tipo.ERROR);
                    }
                });
    }

    private String obtenerMotivo(int idSeleccion) {

        if (idSeleccion == R.id.rbRaices) return "Raíces familiares";
        if (idSeleccion == R.id.rbCuriosidad) return "Curiosidad cultural";
        if (idSeleccion == R.id.rbTrabajo) return "Trabajo o escuela";
        return "Otro";
    }

    private String obtenerIniciales(String texto) {

        String[] partes = texto.trim().split("\\s+");

        if (partes.length >= 2) {
            return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
        }

        return texto.substring(0, Math.min(2, texto.length())).toUpperCase();
    }
}