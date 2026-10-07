package com.itsoeh.sbsc.buti;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        // Respeta la barra de estado y la barra de navegación del teléfono.
        View contenido = findViewById(R.id.contenido);
        ViewCompat.setOnApplyWindowInsetsListener(contenido, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        NavHostFragment navHost = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host);
        navController = navHost.getNavController();

        // Botón de hamburguesa -> abre el menú lateral
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Barra inferior: Inicio / Lecciones / Perfil
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Menú lateral
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.itemConfiguracion) {
                // PENDIENTE: lo implementa la rama feature/progreso-perfil
                Toast.makeText(this, "Configuración: pendiente", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.itemCerrarSesion) {
                // PENDIENTE: lo implementa la rama feature/auth
                Toast.makeText(this, "Cerrar sesión: pendiente", Toast.LENGTH_SHORT).show();
            } else {
                NavigationUI.onNavDestinationSelected(item, navController);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Mantiene marcada en el menú lateral la pantalla actual
        navController.addOnDestinationChangedListener(
                (controller, destino, args) -> navView.setCheckedItem(destino.getId()));

        // El botón "atrás" cierra primero el menú lateral si está abierto
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
