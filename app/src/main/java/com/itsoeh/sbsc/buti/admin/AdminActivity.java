package com.itsoeh.sbsc.buti.admin;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itsoeh.sbsc.buti.PreferenciasButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.configuracion.ConfiguracionFragment;
import com.itsoeh.sbsc.buti.vocabulario.VocabularioNube;

/**
 * Panel de administrador de B'ÜTI, separado por secciones:
 * Reportes · Usuarios · Vocabulario · Ajustes
 */
public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView navAdmin;
    private TextView txtSeccion;
    private TextView txtDescripcion;

    // Tamaño de letra elegido en Configuración.
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(PreferenciasButi.contextoConLetra(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Texto en negrita (accesibilidad), antes de dibujar la pantalla.
        if (PreferenciasButi.negritaActiva(this)) {
            getTheme().applyStyle(R.style.ThemeOverlay_Buti_TextoNegrita, true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // El vocabulario se carga desde Firebase (por si no pasó por ButiApp).
        VocabularioNube.iniciar(this);
        VocabularioNube.conectar();

        navAdmin = findViewById(R.id.navAdmin);
        txtSeccion = findViewById(R.id.txtSeccionAdmin);
        txtDescripcion = findViewById(R.id.txtDescripcionSeccionAdmin);

        navAdmin.setOnItemSelectedListener(item -> {
            mostrarSeccion(item.getItemId());
            return true;
        });

        // Volver a tocar la misma sección cierra la subpantalla (ej. Apariencia).
        navAdmin.setOnItemReselectedListener(item ->
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE));

        getSupportFragmentManager().addOnBackStackChangedListener(this::actualizarEncabezado);

        if (savedInstanceState == null) {
            navAdmin.setSelectedItemId(R.id.adminReportes);
        } else {
            actualizarEncabezado();
        }

        // Atrás: cierra la subpantalla; si no hay, regresa a Reportes; luego sale.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                FragmentManager fm = getSupportFragmentManager();

                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else if (navAdmin.getSelectedItemId() != R.id.adminReportes) {
                    navAdmin.setSelectedItemId(R.id.adminReportes);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /** Cambia a otra sección del menú inferior (lo usan los fragments). */
    public void irA(@IdRes int seccion) {
        navAdmin.setSelectedItemId(seccion);
    }

    /** Abre Apariencia y accesibilidad (la misma Configuración de la app). */
    public void abrirApariencia() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contenedorAdmin, new ConfiguracionFragment())
                .addToBackStack("apariencia")
                .commit();
    }

    private void mostrarSeccion(@IdRes int seccion) {

        FragmentManager fm = getSupportFragmentManager();

        // Cerrar subpantallas abiertas antes de cambiar de sección.
        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment fragment;

        if (seccion == R.id.adminUsuarios) {
            fragment = new AdminUsuariosFragment();
        } else if (seccion == R.id.adminVocabulario) {
            fragment = new AdminVocabularioFragment();
        } else if (seccion == R.id.adminAjustes) {
            fragment = new AdminAjustesFragment();
        } else {
            fragment = new AdminResumenFragment();
        }

        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contenedorAdmin, fragment)
                .commit();

        ponerEncabezado(seccion);
    }

    private void actualizarEncabezado() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            txtSeccion.setText("Apariencia");
            txtDescripcion.setText("Tema, letra y accesibilidad");
        } else {
            ponerEncabezado(navAdmin.getSelectedItemId());
        }
    }

    private void ponerEncabezado(@IdRes int seccion) {

        if (seccion == R.id.adminUsuarios) {
            txtSeccion.setText("Usuarios");
            txtDescripcion.setText("Estudiantes registrados y su avance");
        } else if (seccion == R.id.adminVocabulario) {
            txtSeccion.setText("Vocabulario");
            txtDescripcion.setText("Agrega, edita y elimina palabras");
        } else if (seccion == R.id.adminAjustes) {
            txtSeccion.setText("Ajustes");
            txtDescripcion.setText("Mantenimiento, apariencia y cuenta");
        } else {
            txtSeccion.setText("Reportes");
            txtDescripcion.setText("Así va B'ÜTI");
        }
    }
}