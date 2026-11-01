package com.itsoeh.sbsc.buti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Referencias principales.
        drawerLayout = findViewById(R.id.drawer_layout);

        View contenido = findViewById(R.id.contenido);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        bottomNav = findViewById(R.id.bottom_nav);

        navView = findViewById(R.id.nav_view);

        // Comprobar que los componentes principales existan.
        if (drawerLayout == null
                || contenido == null
                || toolbar == null
                || bottomNav == null
                || navView == null) {

            Toast.makeText(
                    this,
                    "Revisa los IDs de activity_main.xml",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Ajustar el contenido a las barras del sistema.
        ViewCompat.setOnApplyWindowInsetsListener(
                contenido,
                (v, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            barras.bottom
                    );

                    return insets;
                }
        );

        // Obtener el controlador de navegación.
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

        // Botón de hamburguesa: abrir el menú lateral.
        toolbar.setNavigationOnClickListener(
                v -> drawerLayout.openDrawer(GravityCompat.START)
        );

        // Barra inferior: navegar a los destinos correspondientes.
        NavigationUI.setupWithNavController(
                bottomNav,
                navController
        );

        // Configurar el menú lateral.
        configurarMenuLateral();

        // Mantener sincronizada la selección del menú lateral.


        navController.addOnDestinationChangedListener(
                (controller, destino, args) -> {

                    int id = destino.getId();

                    // Mostrar la barra inferior solo en las pantallas principales.
                    boolean mostrarBarra =
                            id == R.id.inicioFragment
                                    || id == R.id.leccionesFragment
                                    || id == R.id.perfilFragment;

                    bottomNav.setVisibility(
                            mostrarBarra ? View.VISIBLE : View.GONE
                    );

                    // Mantener sincronizada la selección del menú lateral.
                    if (esDestinoDelMenu(id)) {
                        navView.setCheckedItem(id);
                    }

                    // Comprobar si el destino pertenece a la barra inferior.
                    android.view.MenuItem elemento =
                            bottomNav.getMenu().findItem(id);

                    if (elemento != null) {
                        elemento.setChecked(true);
                    }
                }
        );



        // Botón atrás: cerrar primero el menú lateral.
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (drawerLayout.isDrawerOpen(
                                GravityCompat.START)) {

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

    /**
     * Configura los clics del menú lateral.
     */
    private void configurarMenuLateral() {

        navView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            // Configuración.
            if (id == R.id.itemConfiguracion) {

                Toast.makeText(
                        MainActivity.this,
                        "Configuración: versión 1.0.0",
                        Toast.LENGTH_SHORT
                ).show();

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Cerrar sesión de Firebase.
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

            // Navegar a los destinos del nav_graph.
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

    /**
     * Indica qué destinos se muestran en el menú lateral.
     */
    private boolean esDestinoDelMenu(int id) {

        return id == R.id.inicioFragment
                || id == R.id.culturaFragment
                || id == R.id.vocabularioFragment
                || id == R.id.practicarFragment
                || id == R.id.progresoFragment;
    }

    /**
     * Indica qué destinos aparecen en la barra inferior.
     */
    private boolean esDestinoBarraInferior(int id) {

        return id == R.id.inicioFragment
                || id == R.id.leccionesFragment
                || id == R.id.perfilFragment;
    }
}
