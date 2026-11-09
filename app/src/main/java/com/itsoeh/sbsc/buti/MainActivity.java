package com.itsoeh.sbsc.buti;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavController navController;
    private NavigationView navView;
    private BottomNavigationView bottomNav;

    private View contenidoNavegacion;
    private View logoToolbar;
    private View contenedorPrincipal;

    private boolean primeraCarga = true;

    // Lector de voz: dónde empezó el toque (para no leer al desplazarse).
    private float toqueInicioX;
    private float toqueInicioY;

    // Aplica el tamaño de letra elegido en Configuración.
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(PreferenciasButi.contextoConLetra(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activar Edge-to-Edge.
        EdgeToEdge.enable(this);

        // Accesibilidad: texto en negrita (antes de dibujar la pantalla).
        if (PreferenciasButi.negritaActiva(this)) {
            getTheme().applyStyle(R.style.ThemeOverlay_Buti_TextoNegrita, true);
        }

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

            ButiAviso.mostrar(
                    this,
                    "No se pudo cargar la pantalla principal.",
                    ButiAviso.Tipo.ERROR
            );

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

        // La barra inferior no suma el espacio
        // de los botones del sistema.
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

            ButiAviso.mostrar(
                    this,
                    "No se pudo cargar la navegación.",
                    ButiAviso.Tipo.ERROR
            );

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

        configurarEncabezadoMenu();

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
    // LECTOR DE VOZ: lee lo que se toca
    // =================================================

    @Override
    public boolean dispatchTouchEvent(MotionEvent evento) {

        if (PreferenciasButi.lectorActivo(this)) {

            int accion = evento.getActionMasked();

            if (accion == MotionEvent.ACTION_DOWN) {

                toqueInicioX = evento.getRawX();
                toqueInicioY = evento.getRawY();

            } else if (accion == MotionEvent.ACTION_UP) {

                float limite = 12 * getResources().getDisplayMetrics().density;

                boolean fueToque =
                        Math.abs(evento.getRawX() - toqueInicioX) < limite
                                && Math.abs(evento.getRawY() - toqueInicioY) < limite;

                if (fueToque) {

                    String texto = LectorVoz.textoEnPunto(
                            getWindow().getDecorView(),
                            (int) evento.getRawX(),
                            (int) evento.getRawY()
                    );

                    LectorVoz.hablar(this, texto);
                }
            }
        }

        // La acción normal del toque sigue funcionando.
        return super.dispatchTouchEvent(evento);
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

                drawerLayout.closeDrawer(GravityCompat.START);

                if (navController.getCurrentDestination() == null
                        || navController.getCurrentDestination().getId()
                        != R.id.configuracionFragment) {

                    navController.navigate(R.id.configuracionFragment);
                }

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
    // ENCABEZADO DEL MENÚ: FOTO, NOMBRE, PUNTOS Y RACHA
    // =================================================

    private void configurarEncabezadoMenu() {

        if (navView.getHeaderCount() == 0) return;

        View encabezado = navView.getHeaderView(0);

        ImageView imgFoto = encabezado.findViewById(R.id.imgFotoDrawer);
        TextView txtIniciales = encabezado.findViewById(R.id.txtInicialesDrawer);
        TextView txtNombre = encabezado.findViewById(R.id.txtNombreDrawer);
        TextView txtResumen = encabezado.findViewById(R.id.txtResumenDrawer);

        // Foto o iniciales y nombre (se actualizan solos si cambian).
        FotoPerfil.mostrar(this, imgFoto, txtIniciales, txtNombre);

        // Tocar la tarjeta abre "Mi perfil".
        View tarjeta = encabezado.findViewById(R.id.contenedorPerfilDrawer);

        if (tarjeta != null) {
            tarjeta.setOnClickListener(v -> abrirPerfil());
        }

        // Puntos y racha: se refrescan cada vez que se abre el menú.
        if (txtResumen != null) {

            actualizarResumenMenu(txtResumen);

            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                    actualizarResumenMenu(txtResumen);
                }
            });
        }
    }

    private void abrirPerfil() {

        drawerLayout.closeDrawer(GravityCompat.START);

        if (navController != null
                && (navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.perfilFragment)) {

            navController.navigate(R.id.perfilFragment);
        }
    }

    private void actualizarResumenMenu(TextView txtResumen) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (isFinishing() || isDestroyed()) return;

                        int puntos = ProgresoButi.puntosTotales(snapshot);
                        int racha = ProgresoButi.rachaVigente(snapshot);

                        txtResumen.setText(
                                puntos + " puntos · racha " + racha
                                        + (racha == 1 ? " día" : " días")
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // =================================================
    // DESTINOS DEL MENÚ LATERAL
    // =================================================

    private boolean esDestinoDelMenu(int id) {

        return id == R.id.inicioFragment
                || id == R.id.perfilFragment
                || id == R.id.culturaFragment
                || id == R.id.vocabularioFragment
                || id == R.id.practicarFragment
                || id == R.id.juegosFragment
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