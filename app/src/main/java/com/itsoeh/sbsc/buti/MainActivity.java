package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavController navController;
    private NavigationView navView;
    private BottomNavigationView bottomNav;

    private View contenidoNavegacion;
    private View logoToolbar;
    private View contenedorPrincipal;

    private boolean primeraCarga = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activar Edge-to-Edge.
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // ---------------------------------------------
        // REFERENCIAS
        // ---------------------------------------------

        drawerLayout = findViewById(R.id.drawer_layout);

        contenedorPrincipal = findViewById(R.id.contenedorPrincipal);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        bottomNav = findViewById(R.id.bottom_nav);

        navView = findViewById(R.id.nav_view);

        contenidoNavegacion = findViewById(R.id.nav_host);

        logoToolbar = findViewById(R.id.imgLogoToolbar);

        // ---------------------------------------------
        // COMPROBAR COMPONENTES
        // ---------------------------------------------

        if (drawerLayout == null
                || contenedorPrincipal == null
                || toolbar == null
                || bottomNav == null
                || navView == null
                || contenidoNavegacion == null) {

            Toast.makeText(
                    this,
                    "Revisa los IDs de activity_main.xml",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ---------------------------------------------
        // CONFIGURAR ESPACIOS DE PANTALLA
        // ---------------------------------------------

        ViewCompat.setOnApplyWindowInsetsListener(
                contenedorPrincipal,
                (v, insets) -> {

                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            0
                    );

                    return insets;
                }
        );



        ViewCompat.requestApplyInsets(contenedorPrincipal);

        // La barra inferior no debe sumar el espacio de los botones del sistema
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            v.setPadding(0, 0, 0, 0);
            return insets;
        });
        bottomNav.post(() -> bottomNav.setPadding(0, 0, 0, 0));

        // ---------------------------------------------
        // CONTROLADOR DE NAVEGACIÓN
        // ---------------------------------------------

        NavHostFragment navHost =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host);

        if (navHost == null) {

            Toast.makeText(
                    this,
                    "No se encontró el contenedor de navegación.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        navController = navHost.getNavController();

        // ---------------------------------------------
        // BOTÓN DEL MENÚ LATERAL
        // ---------------------------------------------

        toolbar.setNavigationOnClickListener(v -> {

            v.animate()
                    .scaleX(0.90f)
                    .scaleY(0.90f)
                    .setDuration(90)
                    .withEndAction(() ->
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                                    .start()
                    )
                    .start();

            drawerLayout.openDrawer(GravityCompat.START);
        });

        // ---------------------------------------------
        // BARRA INFERIOR
        // ---------------------------------------------

        NavigationUI.setupWithNavController(
                bottomNav,
                navController
        );

        bottomNav.setItemHorizontalTranslationEnabled(false);

        // ---------------------------------------------
        // MENÚ LATERAL
        // ---------------------------------------------

        configurarMenuLateral();

        // ---------------------------------------------
        // CAMBIOS DE DESTINO
        // ---------------------------------------------

        navController.addOnDestinationChangedListener(
                (controller, destino, args) -> {

                    int id = destino.getId();

                    boolean mostrarBarra =
                            esDestinoBarraInferior(id);

                    actualizarBarraInferior(
                            mostrarBarra,
                            !primeraCarga
                    );

                    if (esDestinoDelMenu(id)) {
                        navView.setCheckedItem(id);
                    }

                    if (!primeraCarga) {
                        animarCambioPantalla();
                    }

                    primeraCarga = false;
                }
        );

        // ---------------------------------------------
        // ANIMACIONES INICIALES
        // ---------------------------------------------

        iniciarAnimacionesEntrada();

        // ---------------------------------------------
        // BOTÓN ATRÁS
        // ---------------------------------------------

        configurarBotonAtras();
    }

    // =================================================
    // ANIMACIONES DE ENTRADA
    // =================================================

    private void iniciarAnimacionesEntrada() {

        View toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {

            toolbar.setAlpha(0f);
            toolbar.setTranslationY(-25f);

            toolbar.post(() -> {

                toolbar.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(550)
                        .setInterpolator(
                                new DecelerateInterpolator()
                        )
                        .start();
            });
        }

        // Logo.
        if (logoToolbar != null) {

            logoToolbar.setAlpha(0f);
            logoToolbar.setScaleX(0.65f);
            logoToolbar.setScaleY(0.65f);

            logoToolbar.post(() -> {

                logoToolbar.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(650)
                        .setStartDelay(180)
                        .setInterpolator(
                                new OvershootInterpolator(1.2f)
                        )
                        .start();
            });
        }

        // Contenido.
        if (contenidoNavegacion != null) {

            contenidoNavegacion.setAlpha(0f);
            contenidoNavegacion.setTranslationY(20f);

            contenidoNavegacion.post(() -> {

                contenidoNavegacion.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(650)
                        .setStartDelay(250)
                        .setInterpolator(
                                new DecelerateInterpolator()
                        )
                        .start();
            });
        }

        // Barra inferior.
        if (bottomNav != null
                && bottomNav.getVisibility() == View.VISIBLE) {

            bottomNav.setAlpha(0f);
            bottomNav.setTranslationY(25f);

            bottomNav.post(() -> {

                if (bottomNav.getVisibility() == View.VISIBLE) {

                    bottomNav.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(550)
                            .setStartDelay(350)
                            .setInterpolator(
                                    new DecelerateInterpolator()
                            )
                            .start();
                }
            });
        }
    }

    // =================================================
    // TRANSICIONES ENTRE PANTALLAS
    // =================================================

    private void animarCambioPantalla() {

        if (contenidoNavegacion == null) return;

        contenidoNavegacion.animate().cancel();

        contenidoNavegacion.setAlpha(0.75f);
        contenidoNavegacion.setTranslationY(12f);

        contenidoNavegacion.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .start();
    }

    // =================================================
    // MOSTRAR Y OCULTAR BARRA INFERIOR
    // =================================================

    private void actualizarBarraInferior(
            boolean mostrar,
            boolean animar
    ) {

        if (bottomNav == null) return;

        bottomNav.animate().cancel();

        if (mostrar) {

            bottomNav.setVisibility(View.VISIBLE);

            if (animar) {

                bottomNav.setAlpha(0f);
                bottomNav.setTranslationY(20f);

                bottomNav.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .setInterpolator(
                                new DecelerateInterpolator()
                        )
                        .start();

            } else {

                bottomNav.setAlpha(1f);
                bottomNav.setTranslationY(0f);
            }

        } else {

            if (bottomNav.getVisibility() == View.VISIBLE) {

                if (animar) {

                    bottomNav.animate()
                            .alpha(0f)
                            .translationY(20f)
                            .setDuration(220)
                            .withEndAction(() -> {

                                bottomNav.setVisibility(View.GONE);

                            })
                            .start();

                } else {

                    bottomNav.setVisibility(View.GONE);
                }
            }
        }
    }

    // =================================================
    // CONFIGURAR MENÚ LATERAL
    // =================================================

    private void configurarMenuLateral() {

        navView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            // CONFIGURACIÓN
            if (id == R.id.itemConfiguracion) {

                Toast.makeText(
                        MainActivity.this,
                        "Configuración: versión 1.0.0",
                        Toast.LENGTH_SHORT
                ).show();

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }

            // CERRAR SESIÓN
            if (id == R.id.itemCerrarSesion) {

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(
                        MainActivity.this,
                        LoginActivity.class
                );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);

                finish();

                return true;
            }

            // NAVEGACIÓN
            boolean navegacionRealizada =
                    NavigationUI.onNavDestinationSelected(
                            item,
                            navController
                    );

            if (navegacionRealizada) {

                drawerLayout.closeDrawer(GravityCompat.START);
            }

            return navegacionRealizada;
        });
    }

    // =================================================
    // DESTINOS DEL MENÚ LATERAL
    // =================================================

    private boolean esDestinoDelMenu(int id) {

        return id == R.id.inicioFragment
                || id == R.id.culturaFragment
                || id == R.id.vocabularioFragment
                || id == R.id.practicarFragment
                || id == R.id.progresoFragment;
    }

    // =================================================
    // DESTINOS DE LA BARRA INFERIOR
    // =================================================

    private boolean esDestinoBarraInferior(int id) {

        return id == R.id.inicioFragment
                || id == R.id.leccionesFragment
                || id == R.id.perfilFragment;
    }

    // =================================================
    // BOTÓN ATRÁS
    // =================================================

    private void configurarBotonAtras() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (drawerLayout.isDrawerOpen(
                                GravityCompat.START
                        )) {

                            drawerLayout.closeDrawer(
                                    GravityCompat.START
                            );

                        } else {

                            setEnabled(false);

                            getOnBackPressedDispatcher()
                                    .onBackPressed();
                        }
                    }
                }
        );
    }
}
