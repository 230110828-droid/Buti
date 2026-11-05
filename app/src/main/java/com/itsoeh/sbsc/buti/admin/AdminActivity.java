package com.itsoeh.sbsc.buti.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.LoginActivity;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.datos.Usuario;
import com.itsoeh.sbsc.buti.datos.UsuarioRepositorio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    private SwitchMaterial switchMantenimiento;
    private TextView tvEstadoMantenimiento;
    private RecyclerView rvUsuariosAdmin;
    private RecyclerView rvPalabrasAdmin;
    private Button btnAgregarPalabraAdmin;

    private List<UsuarioSimulado> listaUsuarios;
    private List<PalabraSimulada> listaPalabras;

    private UsuariosAdapter usuariosAdapter;
    private PalabrasAdapter palabrasAdapter;

    // Variables de estado temporal para el diálogo de agregar palabra
    private String audioSeleccionado = "Sin audio";
    private String imagenSeleccionada = "Sin imagen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Relacionar vistas
        switchMantenimiento = findViewById(R.id.switchMantenimiento);
        tvEstadoMantenimiento = findViewById(R.id.tvEstadoMantenimiento);
        rvUsuariosAdmin = findViewById(R.id.rvUsuariosAdmin);
        rvPalabrasAdmin = findViewById(R.id.rvPalabrasAdmin);
        btnAgregarPalabraAdmin = findViewById(R.id.btnAgregarPalabraAdmin);
        Button btnCerrarSesionAdmin = findViewById(R.id.btnCerrarSesionAdmin);

        // Configurar Modo Mantenimiento
        configurarMantenimiento();

        // Configurar Lista de Usuarios Registrados (Firebase Realtime DB + Datos base)
        configurarUsuarios();

        // Configurar Lista y Gestor de Lecciones / Vocabulario
        configurarVocabulario();

        // Configurar Botón Salir
        if (btnCerrarSesionAdmin != null) {
            btnCerrarSesionAdmin.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    // =========================================
    // 1. MODO MANTENIMIENTO
    // =========================================
    private void configurarMantenimiento() {
        switchMantenimiento.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvEstadoMantenimiento.setText("Estado: MODO MANTENIMIENTO ACTIVADO");
                tvEstadoMantenimiento.setTextColor(ContextCompat.getColor(this, R.color.buti_rosa));
                Toast.makeText(this, "Modo mantenimiento activado", Toast.LENGTH_SHORT).show();
            } else {
                tvEstadoMantenimiento.setText("Estado: Sistema Operativo (Normal)");
                tvEstadoMantenimiento.setTextColor(ContextCompat.getColor(this, R.color.buti_verde));
                Toast.makeText(this, "Modo mantenimiento desactivado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================
    // 2. USUARIOS REGISTRADOS (DESDE FIREBASE)
    // =========================================
    private void configurarUsuarios() {
        listaUsuarios = new ArrayList<>();
        usuariosAdapter = new UsuariosAdapter(listaUsuarios);
        rvUsuariosAdmin.setLayoutManager(new LinearLayoutManager(this));
        rvUsuariosAdmin.setAdapter(usuariosAdapter);

        // Cargar únicamente los usuarios reales de Firebase Realtime Database
        DatabaseReference refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        refUsuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaUsuarios.clear();
                Set<String> correosProcesados = new HashSet<>();

                // 1. Cargar usuarios reales de la base de datos de Firebase
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String nombre = ds.child("nombre").getValue(String.class);
                    String correo = ds.child("correo").getValue(String.class);
                    if (TextUtils.isEmpty(correo)) {
                        correo = ds.child("email").getValue(String.class);
                    }

                    if (TextUtils.isEmpty(nombre)) {
                        if (!TextUtils.isEmpty(correo) && correo.contains("@")) {
                            nombre = correo.split("@")[0];
                        } else {
                            nombre = "Usuario Registrado";
                        }
                    }

                    if (!TextUtils.isEmpty(correo) && !correosProcesados.contains(correo.toLowerCase())) {
                        correosProcesados.add(correo.toLowerCase());
                        
                        // Racha y nivel simulados basados en el usuario real
                        int rachaSimulada = Math.abs((correo.hashCode() % 15)) + 1;
                        int nivelSimulado = Math.abs((correo.hashCode() % 5)) + 1;

                        listaUsuarios.add(new UsuarioSimulado(nombre, correo, rachaSimulada, nivelSimulado));
                    }
                }

                // 2. Si el usuario actual de Auth no estaba en el nodo "usuarios", incluirlo
                FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
                if (userAuth != null && userAuth.getEmail() != null) {
                    String correoAuth = userAuth.getEmail();
                    if (!correosProcesados.contains(correoAuth.toLowerCase())) {
                        String nombreAuth = userAuth.getDisplayName();
                        if (TextUtils.isEmpty(nombreAuth)) {
                            nombreAuth = correoAuth.split("@")[0];
                        }
                        int rachaSimulada = Math.abs((correoAuth.hashCode() % 15)) + 1;
                        int nivelSimulado = Math.abs((correoAuth.hashCode() % 5)) + 1;

                        listaUsuarios.add(new UsuarioSimulado(nombreAuth, correoAuth, rachaSimulada, nivelSimulado));
                        correosProcesados.add(correoAuth.toLowerCase());
                    }
                }

                usuariosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Error al cargar usuarios de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================
    // 3. GESTOR DE LECCIONES / VOCABULARIO
    // =========================================
    private void configurarVocabulario() {
        listaPalabras = new ArrayList<>();
        listaPalabras.add(new PalabraSimulada("Lección 1: Saludos", "Hola", "Gui", "Saludos", "audio_gui.mp3", "img_gui.png"));
        listaPalabras.add(new PalabraSimulada("Lección 1: Saludos", "Gracias", "Jamädi", "Saludos", "audio_jamadi.mp3", "img_jamadi.png"));
        listaPalabras.add(new PalabraSimulada("Lección 2: La Familia", "Papá", "Mä dada", "Familia", "audio_dada.mp3", "img_dada.png"));
        listaPalabras.add(new PalabraSimulada("Lección 3: Cultura", "Maguey", "ꞌUada", "Cultura", "audio_uada.mp3", "img_uada.png"));
        listaPalabras.add(new PalabraSimulada("Lección 3: Cultura", "Flor de maguey", "Ndo̱mbo", "Cultura", "audio_ndombo.mp3", "img_ndombo.png"));

        palabrasAdapter = new PalabrasAdapter(listaPalabras);
        rvPalabrasAdmin.setLayoutManager(new LinearLayoutManager(this));
        rvPalabrasAdmin.setAdapter(palabrasAdapter);

        btnAgregarPalabraAdmin.setOnClickListener(v -> mostrarDialogoAgregarPalabra());
    }

    // Diálogo para agregar nueva palabra y asignarla a una lección con audio e imagen
    private void mostrarDialogoAgregarPalabra() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_palabra_admin, null);
        EditText edtLeccion = dialogView.findViewById(R.id.edtLeccionAdmin);
        EditText edtHnahnu = dialogView.findViewById(R.id.edtPalabraHnahnuAdmin);
        EditText edtEspanol = dialogView.findViewById(R.id.edtPalabraEspanolAdmin);
        EditText edtCategoria = dialogView.findViewById(R.id.edtCategoriaAdmin);

        Button btnAudio = dialogView.findViewById(R.id.btnSeleccionarAudioAdmin);
        TextView tvEstadoAudio = dialogView.findViewById(R.id.tvEstadoAudioAdmin);

        Button btnImagen = dialogView.findViewById(R.id.btnSeleccionarImagenAdmin);
        TextView tvEstadoImagen = dialogView.findViewById(R.id.tvEstadoImagenAdmin);

        audioSeleccionado = "No adjuntado";
        imagenSeleccionada = "No adjuntada";

        // Simular selección de audio
        btnAudio.setOnClickListener(v -> {
            audioSeleccionado = "pronunciacion_" + System.currentTimeMillis() % 1000 + ".mp3";
            tvEstadoAudio.setText(audioSeleccionado);
            tvEstadoAudio.setTextColor(ContextCompat.getColor(this, R.color.buti_verde));
            Toast.makeText(this, "Audio adjuntado correctamente", Toast.LENGTH_SHORT).show();
        });

        // Simular selección de imagen
        btnImagen.setOnClickListener(v -> {
            imagenSeleccionada = "ilustracion_" + System.currentTimeMillis() % 1000 + ".png";
            tvEstadoImagen.setText(imagenSeleccionada);
            tvEstadoImagen.setTextColor(ContextCompat.getColor(this, R.color.buti_naranja));
            Toast.makeText(this, "Imagen adjuntada correctamente", Toast.LENGTH_SHORT).show();
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Agregar Palabra a Lección")
                .setMessage("Asigna la palabra a una lección e ingresa la traducción y archivos adjuntos")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String leccion = edtLeccion.getText().toString().trim();
                    String hnahnu = edtHnahnu.getText().toString().trim();
                    String espanol = edtEspanol.getText().toString().trim();
                    String categoria = edtCategoria.getText().toString().trim();

                    if (TextUtils.isEmpty(hnahnu) || TextUtils.isEmpty(espanol)) {
                        Toast.makeText(this, "Por favor llena los campos de Hñähñu y Español", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(leccion)) {
                        leccion = "Lección General";
                    }
                    if (TextUtils.isEmpty(categoria)) {
                        categoria = "Vocabulario";
                    }

                    listaPalabras.add(0, new PalabraSimulada(leccion, espanol, hnahnu, categoria, audioSeleccionado, imagenSeleccionada));
                    palabrasAdapter.notifyItemInserted(0);
                    rvPalabrasAdmin.scrollToPosition(0);

                    Toast.makeText(this, "Palabra agregada a " + leccion, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
