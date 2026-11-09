package com.itsoeh.sbsc.buti;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Muestra la foto de perfil (o las iniciales) en cualquier pantalla.
 *
 * Escucha Firebase en vivo: si el usuario cambia su foto o su nombre,
 * se actualiza solo en el menú lateral, Inicio, Progreso, etc.
 * El listener se quita automáticamente cuando la pantalla se destruye.
 *
 * Uso en un Fragment:
 *   FotoPerfil.mostrar(getViewLifecycleOwner(), imgFoto, txtIniciales, null);
 */
public final class FotoPerfil {

    // Caché: la misma foto no se decodifica dos veces.
    private static String ultimoBase64;
    private static Bitmap ultimoBitmap;

    private FotoPerfil() {
    }

    public static void mostrar(
            @NonNull LifecycleOwner duenio,
            @Nullable ImageView foto,
            @Nullable TextView iniciales,
            @Nullable TextView nombre) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            ponerIniciales(foto, iniciales, nombre, "Usuario");
            return;
        }

        // Mientras responde Firebase: nombre de la cuenta y foto en caché.
        String nombreCuenta = TextUtils.isEmpty(usuario.getDisplayName())
                ? "Usuario"
                : usuario.getDisplayName();

        if (ultimoBitmap != null) {
            ponerFoto(foto, iniciales, ultimoBitmap);
            if (nombre != null) nombre.setText(nombreCuenta);
        } else {
            ponerIniciales(foto, iniciales, nombre, nombreCuenta);
        }

        DatabaseReference referencia = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid());

        DatabaseReference refFoto = referencia.child("fotoPerfilBase64");
        DatabaseReference refNombre = referencia.child("nombre");

        final String[] estado = {nombreCuenta, null};

        ValueEventListener escuchaNombre = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String valor = snapshot.getValue(String.class);
                if (!TextUtils.isEmpty(valor)) estado[0] = valor;
                aplicar(foto, iniciales, nombre, estado[0], estado[1]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        ValueEventListener escuchaFoto = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                estado[1] = snapshot.getValue(String.class);
                aplicar(foto, iniciales, nombre, estado[0], estado[1]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        if (duenio.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }

        refNombre.addValueEventListener(escuchaNombre);
        refFoto.addValueEventListener(escuchaFoto);

        duenio.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                refNombre.removeEventListener(escuchaNombre);
                refFoto.removeEventListener(escuchaFoto);
            }
        });
    }

    /** Iniciales a partir del nombre: "Ana López" -> "AL". */
    public static String iniciales(@Nullable String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) return "U";

        String[] partes = nombre.trim().split("\\s+");

        String resultado = partes.length >= 2
                ? partes[0].substring(0, 1) + partes[1].substring(0, 1)
                : partes[0].substring(0, Math.min(2, partes[0].length()));

        return resultado.toUpperCase();
    }

    // =========================================
    // AYUDANTES
    // =========================================

    private static void aplicar(
            @Nullable ImageView foto,
            @Nullable TextView iniciales,
            @Nullable TextView nombre,
            String textoNombre,
            @Nullable String base64) {

        if (nombre != null) nombre.setText(textoNombre);

        Bitmap bitmap = decodificar(base64);

        if (bitmap != null) {
            ponerFoto(foto, iniciales, bitmap);
        } else {
            ponerIniciales(foto, iniciales, null, textoNombre);
        }
    }

    @Nullable
    private static Bitmap decodificar(@Nullable String base64) {

        if (TextUtils.isEmpty(base64)) {
            ultimoBase64 = null;
            ultimoBitmap = null;
            return null;
        }

        if (base64.equals(ultimoBase64) && ultimoBitmap != null) {
            return ultimoBitmap;
        }

        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            if (bitmap != null) {
                ultimoBase64 = base64;
                ultimoBitmap = bitmap;
            }

            return bitmap;

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void ponerFoto(
            @Nullable ImageView foto,
            @Nullable TextView iniciales,
            Bitmap bitmap) {

        if (foto == null) return;

        // Recorte circular para que se vea igual en cualquier pantalla.
        RoundedBitmapDrawable circular =
                RoundedBitmapDrawableFactory.create(foto.getResources(), recortarCuadrado(bitmap));
        circular.setCircular(true);

        foto.setImageDrawable(circular);
        foto.setVisibility(View.VISIBLE);

        if (iniciales != null) iniciales.setVisibility(View.GONE);
    }

    private static void ponerIniciales(
            @Nullable ImageView foto,
            @Nullable TextView iniciales,
            @Nullable TextView nombre,
            String textoNombre) {

        if (foto != null) foto.setVisibility(View.GONE);

        if (iniciales != null) {
            iniciales.setText(iniciales(textoNombre));
            iniciales.setVisibility(View.VISIBLE);
        }

        if (nombre != null) nombre.setText(textoNombre);
    }

    private static Bitmap recortarCuadrado(Bitmap bitmap) {

        int lado = Math.min(bitmap.getWidth(), bitmap.getHeight());

        if (bitmap.getWidth() == bitmap.getHeight()) return bitmap;

        int x = (bitmap.getWidth() - lado) / 2;
        int y = (bitmap.getHeight() - lado) / 2;

        return Bitmap.createBitmap(bitmap, x, y, lado, lado);
    }
}
