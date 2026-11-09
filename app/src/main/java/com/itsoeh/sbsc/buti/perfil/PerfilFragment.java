package com.itsoeh.sbsc.buti.perfil;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ButiDialogo;
import com.itsoeh.sbsc.buti.LoginActivity;
import com.itsoeh.sbsc.buti.ElegirAvatar;
import com.itsoeh.sbsc.buti.FotoPerfil;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.RecuperarContrasenaActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class PerfilFragment extends Fragment {

    private TextView txtNombrePerfil;
    private TextView txtInicialesPerfil;
    private ImageView imgFotoPerfil;
    private FrameLayout contenedorAvatarPerfil;

    private DatabaseReference referenciaUsuario;
    private ValueEventListener listenerUsuario;

    private ActivityResultLauncher<String> selectorImagen;

    // Avatar elegido (nombre del dibujo) y nombre para las iniciales.
    private String avatarActual;
    private String nombreActual = "Usuario";

    public PerfilFragment() {
        super(R.layout.fragment_perfil);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectorImagen = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && isAdded()) {
                        // Mostrar inmediatamente la foto elegida.
                        mostrarFoto(uri);

                        // Guardar la imagen en Firebase como Base64.
                        guardarFotoEnFirebase(uri);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        txtNombrePerfil =
                view.findViewById(R.id.txtNombrePerfil);

        txtInicialesPerfil =
                view.findViewById(R.id.txtInicialesPerfil);

        imgFotoPerfil =
                view.findViewById(R.id.imgFotoPerfil);

        contenedorAvatarPerfil =
                view.findViewById(R.id.contenedorAvatarPerfil);

        Button btnEditarPerfil =
                view.findViewById(R.id.btnEditarPerfil);

        Button btnCambiarContrasena =
                view.findViewById(R.id.btnCambiarContrasenaPerfil);

        Button btnConfiguracion =
                view.findViewById(R.id.btnConfiguracionPerfil);

        Button btnCerrarSesion =
                view.findViewById(R.id.btnCerrarSesionPerfil);

        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario != null) {
            referenciaUsuario = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(usuario.getUid());
        }

        cargarDatosUsuario();

        // Al tocar la foto: elegir avatar, subir foto o quitarla.
        contenedorAvatarPerfil.setOnClickListener(v ->
                ElegirAvatar.mostrar(
                        requireContext(),
                        avatarActual,
                        nombreActual,
                        () -> selectorImagen.launch("image/*")
                ));

        // Editar perfil.
        btnEditarPerfil.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.editarPerfilFragment));

        // Cambiar contraseña.
        btnCambiarContrasena.setOnClickListener(v -> {
            Intent intent = new Intent(
                    requireContext(),
                    RecuperarContrasenaActivity.class
            );
            startActivity(intent);
        });

        // Configuración.
        btnConfiguracion.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.configuracionFragment));

        // Cerrar sesión.
        btnCerrarSesion.setOnClickListener(v ->
                confirmarCerrarSesion());
    }

    private void cargarDatosUsuario() {

        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            mostrarNombre("Usuario");
            mostrarIniciales();
            return;
        }

        String nombreInicial = usuario.getDisplayName();

        if (TextUtils.isEmpty(nombreInicial)) {
            nombreInicial = nombreDesdeCorreo(usuario.getEmail());
        }

        mostrarNombre(nombreInicial);

        if (referenciaUsuario == null) {
            return;
        }

        listenerUsuario = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!isAdded() || txtNombrePerfil == null) {
                    return;
                }

                // Recuperar nombre desde Realtime Database.
                String nombreBD = snapshot.child("nombre")
                        .getValue(String.class);

                if (!TextUtils.isEmpty(nombreBD)) {
                    mostrarNombre(nombreBD);
                } else {
                    String nombreAuth = usuario.getDisplayName();

                    if (TextUtils.isEmpty(nombreAuth)) {
                        nombreAuth =
                                nombreDesdeCorreo(usuario.getEmail());
                    }

                    mostrarNombre(nombreAuth);
                }

                // Foto de la galería, avatar elegido o iniciales.
                String fotoBase64 = snapshot.child("fotoPerfilBase64")
                        .getValue(String.class);

                avatarActual = snapshot.child("avatar").getValue(String.class);

                FotoPerfil.ponerEn(
                        imgFotoPerfil,
                        txtInicialesPerfil,
                        nombreActual,
                        fotoBase64,
                        avatarActual
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) {
                    return;
                }

                // Si la sesión ya terminó, no mostrar mensajes del perfil.
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    return;
                }

                ButiAviso.mostrar(
                        PerfilFragment.this,
                        "No se pudieron cargar los datos del perfil.",
                        ButiAviso.Tipo.ERROR
                );

                android.util.Log.e(
                        "PerfilFragment",
                        "Error al consultar Firebase: " + error.getMessage()
                );
            }
        };

        referenciaUsuario.addValueEventListener(listenerUsuario);
    }

    private String nombreDesdeCorreo(String correo) {

        if (TextUtils.isEmpty(correo)) {
            return "Usuario";
        }

        int posicionArroba = correo.indexOf("@");

        if (posicionArroba > 0) {
            return correo.substring(0, posicionArroba);
        }

        return correo;
    }

    private void mostrarNombre(String nombre) {

        if (!TextUtils.isEmpty(nombre)) nombreActual = nombre;


        if (txtNombrePerfil == null) {
            return;
        }

        if (TextUtils.isEmpty(nombre)) {
            nombre = "Usuario";
        }

        txtNombrePerfil.setText(nombre);

        String nombreLimpio = nombre.trim();

        if (nombreLimpio.isEmpty()) {
            txtInicialesPerfil.setText("U");
            return;
        }

        String[] partes = nombreLimpio.split("\\s+");
        String iniciales;

        if (partes.length >= 2) {
            iniciales = partes[0].substring(0, 1)
                    + partes[1].substring(0, 1);
        } else {
            iniciales = nombreLimpio.substring(
                    0,
                    Math.min(2, nombreLimpio.length())
            );
        }

        txtInicialesPerfil.setText(iniciales.toUpperCase());
    }

    // Convertir y guardar la foto en Firebase Realtime Database.
    private void guardarFotoEnFirebase(Uri uri) {

        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null || referenciaUsuario == null || !isAdded()) {
            return;
        }

        ContentResolver resolver =
                requireContext().getContentResolver();

        DatabaseReference referenciaFoto =
                referenciaUsuario.child("fotoPerfilBase64");

        ButiAviso.mostrar(
                PerfilFragment.this,
                "Guardando fotografía...",
                ButiAviso.Tipo.INFO
        );

        // La conversión se realiza fuera del hilo principal.
        new Thread(() -> {
            String fotoBase64 = convertirImagenABase64(resolver, uri);

            if (fotoBase64 == null) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            ButiAviso.mostrar(
                                    PerfilFragment.this,
                                    "No se pudo procesar la fotografía.",
                                    ButiAviso.Tipo.ERROR
                            );
                        }
                    });
                }
                return;
            }

            // La foto reemplaza al avatar.
            referenciaUsuario.child("avatar").removeValue();

            referenciaFoto.setValue(fotoBase64)
                    .addOnCompleteListener(task -> {
                        if (!isAdded()) {
                            return;
                        }

                        if (task.isSuccessful()) {
                            ButiAviso.mostrar(
                                    PerfilFragment.this,
                                    "Fotografía guardada en Firebase.",
                                    ButiAviso.Tipo.EXITO
                            );
                        } else {
                            ButiAviso.mostrar(
                                    PerfilFragment.this,
                                    "No se pudo guardar. Revisa tu conexión y las reglas de Firebase.",
                                    ButiAviso.Tipo.ERROR
                            );
                        }
                    });
        }).start();
    }

    // Reducir el tamaño de la imagen y convertirla a Base64.
    private String convertirImagenABase64(
            ContentResolver resolver,
            Uri uri) {

        Bitmap bitmap = null;

        try {
            // Leer dimensiones sin cargar toda la imagen.
            BitmapFactory.Options opciones =
                    new BitmapFactory.Options();

            opciones.inJustDecodeBounds = true;

            try (InputStream entrada =
                         resolver.openInputStream(uri)) {
                if (entrada == null) {
                    return null;
                }

                BitmapFactory.decodeStream(
                        entrada,
                        null,
                        opciones
                );
            }

            if (opciones.outWidth <= 0 || opciones.outHeight <= 0) {
                return null;
            }

            // Evitar cargar fotografías demasiado grandes en memoria.
            opciones.inSampleSize = 1;

            while (opciones.outWidth / opciones.inSampleSize > 800
                    || opciones.outHeight / opciones.inSampleSize > 800) {
                opciones.inSampleSize *= 2;
            }

            opciones.inJustDecodeBounds = false;
            opciones.inPreferredConfig = Bitmap.Config.RGB_565;

            try (InputStream entrada =
                         resolver.openInputStream(uri)) {
                if (entrada == null) {
                    return null;
                }

                bitmap = BitmapFactory.decodeStream(
                        entrada,
                        null,
                        opciones
                );
            }

            if (bitmap == null) {
                return null;
            }

            // Limitar el resultado a un máximo de 400 x 400 píxeles.
            int ancho = bitmap.getWidth();
            int alto = bitmap.getHeight();

            float escala = Math.min(
                    1f,
                    400f / Math.max(ancho, alto)
            );

            int nuevoAncho = Math.max(1, Math.round(ancho * escala));
            int nuevoAlto = Math.max(1, Math.round(alto * escala));

            Bitmap imagenReducida = bitmap;

            if (nuevoAncho != ancho || nuevoAlto != alto) {
                imagenReducida = Bitmap.createScaledBitmap(
                        bitmap,
                        nuevoAncho,
                        nuevoAlto,
                        true
                );

                bitmap.recycle();
                bitmap = imagenReducida;
            }

            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            boolean comprimida = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    70,
                    salida
            );

            if (!comprimida) {
                return null;
            }

            byte[] bytesImagen = salida.toByteArray();

            return Base64.encodeToString(
                    bytesImagen,
                    Base64.NO_WRAP
            );

        } catch (IOException | OutOfMemoryError e) {
            return null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    // Decodificar Base64 y mostrar la foto recuperada.
    private void mostrarFotoBase64(String fotoBase64) {

        if (!isAdded()
                || imgFotoPerfil == null
                || txtInicialesPerfil == null) {
            return;
        }

        try {
            byte[] bytesImagen =
                    Base64.decode(fotoBase64, Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    bytesImagen,
                    0,
                    bytesImagen.length
            );

            if (bitmap != null) {
                imgFotoPerfil.setImageBitmap(bitmap);
                imgFotoPerfil.setVisibility(View.VISIBLE);
                txtInicialesPerfil.setVisibility(View.GONE);
            } else {
                mostrarIniciales();
            }

        } catch (IllegalArgumentException e) {
            mostrarIniciales();
        }
    }

    // Mostrar una foto recién elegida antes de terminar el guardado.
    private void mostrarFoto(Uri uri) {

        if (!isAdded()
                || imgFotoPerfil == null
                || txtInicialesPerfil == null) {
            return;
        }

        try {
            imgFotoPerfil.setPadding(0, 0, 0, 0);
            imgFotoPerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgFotoPerfil.setImageURI(uri);

            if (imgFotoPerfil.getDrawable() != null) {
                imgFotoPerfil.setVisibility(View.VISIBLE);
                txtInicialesPerfil.setVisibility(View.GONE);
            } else {
                mostrarIniciales();
            }

        } catch (Exception e) {
            mostrarIniciales();
        }
    }

    // Mostrar iniciales cuando el usuario no tiene fotografía.
    private void mostrarIniciales() {

        if (imgFotoPerfil != null) {
            imgFotoPerfil.setImageDrawable(null);
            imgFotoPerfil.setVisibility(View.GONE);
        }

        if (txtInicialesPerfil != null) {
            txtInicialesPerfil.setVisibility(View.VISIBLE);
        }
    }

    private void confirmarCerrarSesion() {

        // Mensaje personalizado de B'ÜTI (antes AlertDialog).
        new ButiDialogo(requireContext()).mostrarConfirmacion(
                "Cerrar sesión",
                "¿Seguro que quieres salir de tu cuenta?",
                "Sí, cerrar sesión",
                "Cancelar",
                this::cerrarSesion,
                null
        );
    }

    private void cerrarSesion() {

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(
                requireContext(),
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {

        if (referenciaUsuario != null && listenerUsuario != null) {
            referenciaUsuario.removeEventListener(listenerUsuario);
            listenerUsuario = null;
        }

        txtNombrePerfil = null;
        txtInicialesPerfil = null;
        imgFotoPerfil = null;
        contenedorAvatarPerfil = null;
        referenciaUsuario = null;

        super.onDestroyView();
    }
}