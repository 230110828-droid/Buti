package com.itsoeh.sbsc.buti.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.itsoeh.sbsc.buti.FotoPerfil;
import com.itsoeh.sbsc.buti.ProgresoButi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Datos reales de un estudiante, leídos de usuarios/{uid} en Firebase.
 * Lo usan Reportes y Usuarios del panel de administrador.
 */
public class UsuarioAdmin {

    public static final int NIVELES = 11;
    public static final int ETAPAS = 5;

    /** Cuentas de administrador (no cuentan como estudiantes). */
    private static final String[] CORREOS_ADMIN = {
            "admin@buti.com",
            "yelitzagranados002@gmail.com"
    };

    public String uid = "";
    public String nombre = "";
    public String correo = "";
    public String fotoBase64 = "";
    public String avatar = "";
    public String ultimaFecha = "";

    public int puntos;
    public int racha;
    public int mejorRacha;
    public int etapasCompletadas;
    public int nivelActual = 1;
    public final int[] etapasPorNivel = new int[NIVELES];

    /** Días (yyyy-MM-dd) en los que practicó. */
    public final Set<String> diasActivos = new HashSet<>();

    // Fotos ya decodificadas (para no repetir el trabajo al hacer scroll).
    private static final Map<String, Bitmap> fotos = new HashMap<>();

    public static boolean esAdmin(@Nullable String correo) {

        if (correo == null) return false;

        for (String admin : CORREOS_ADMIN) {
            if (admin.equalsIgnoreCase(correo.trim())) return true;
        }

        return false;
    }

    /** Todos los estudiantes (sin administradores ni registros vacíos). */
    public static List<UsuarioAdmin> lista(@Nullable DataSnapshot usuarios) {

        List<UsuarioAdmin> lista = new ArrayList<>();

        if (usuarios == null) return lista;

        for (DataSnapshot dato : usuarios.getChildren()) {

            UsuarioAdmin u = desde(dato);

            if (u.correo.isEmpty() && u.nombre.isEmpty()) continue;
            if (esAdmin(u.correo)) continue;

            lista.add(u);
        }

        return lista;
    }

    public static UsuarioAdmin desde(@NonNull DataSnapshot dato) {

        UsuarioAdmin u = new UsuarioAdmin();

        u.uid = dato.getKey() == null ? "" : dato.getKey();
        u.nombre = texto(dato, "nombre");
        u.correo = texto(dato, "correo");

        if (u.correo.isEmpty()) u.correo = texto(dato, "email");
        if (u.nombre.isEmpty() && !u.correo.isEmpty()) u.nombre = u.correo.split("@")[0];

        u.fotoBase64 = texto(dato, "fotoPerfilBase64");
        u.avatar = texto(dato, "avatar");

        u.puntos = ProgresoButi.puntosTotales(dato);
        u.racha = ProgresoButi.rachaVigente(dato);

        Long mejor = dato.child("racha").child("mejorRacha").getValue(Long.class);
        u.mejorRacha = Math.max(u.racha, mejor == null ? 0 : mejor.intValue());

        u.ultimaFecha = texto(dato.child("racha"), "ultimaFecha");

        // Etapas completadas por nivel.
        DataSnapshot progreso = dato.child("progresoLecciones");
        boolean sigueCompleto = true;

        for (int n = 1; n <= NIVELES; n++) {

            int completas = 0;

            for (int e = 1; e <= ETAPAS; e++) {

                DataSnapshot etapa = progreso.child("nivel" + n).child("etapa" + e);

                if (Boolean.TRUE.equals(etapa.child("completada").getValue(Boolean.class))) {
                    completas++;
                }

                // Días de lecciones (por si es un usuario anterior a puntosPorDia).
                String fecha = etapa.child("fecha").getValue(String.class);
                if (fecha != null && fecha.length() >= 10) u.diasActivos.add(fecha.substring(0, 10));
            }

            u.etapasPorNivel[n - 1] = completas;
            u.etapasCompletadas += completas;

            if (sigueCompleto && completas == ETAPAS) {
                u.nivelActual = Math.min(NIVELES, n + 1);
            } else {
                sigueCompleto = false;
            }
        }

        // Días con puntos (lecciones, práctica y juegos).
        for (DataSnapshot dia : dato.child("puntosPorDia").getChildren()) {
            Long valor = dia.getValue(Long.class);
            if (dia.getKey() != null && valor != null && valor > 0) u.diasActivos.add(dia.getKey());
        }

        if (!u.ultimaFecha.isEmpty()) u.diasActivos.add(u.ultimaFecha);

        return u;
    }

    public boolean activoEl(String dia) {
        return diasActivos.contains(dia);
    }

    /** Coincide con lo que se escribió en el buscador. */
    public boolean coincide(String busqueda) {

        if (TextUtils.isEmpty(busqueda)) return true;

        String b = busqueda.toLowerCase(Locale.ROOT);

        return nombre.toLowerCase(Locale.ROOT).contains(b)
                || correo.toLowerCase(Locale.ROOT).contains(b);
    }

    /** Pone la foto (o las iniciales si no tiene). */
    public void ponerAvatar(ImageView foto, TextView iniciales) {

        Bitmap bitmap = foto();
        int dibujo = FotoPerfil.recursoAvatar(foto.getContext(), avatar);

        if (bitmap != null) {
            foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            foto.setPadding(0, 0, 0, 0);
            foto.setImageBitmap(bitmap);
            foto.setVisibility(View.VISIBLE);
            iniciales.setVisibility(View.GONE);
        } else if (dibujo != 0) {
            int margen = Math.round(foto.getResources().getDisplayMetrics().density * 4);
            foto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            foto.setPadding(margen, margen, margen, margen);
            foto.setImageResource(dibujo);
            foto.setVisibility(View.VISIBLE);
            iniciales.setVisibility(View.GONE);
        } else {
            foto.setVisibility(View.GONE);
            iniciales.setVisibility(View.VISIBLE);
            iniciales.setText(FotoPerfil.iniciales(nombre));
        }
    }

    @Nullable
    private Bitmap foto() {

        if (fotoBase64.isEmpty()) return null;

        String llave = uid + "_" + fotoBase64.hashCode();

        synchronized (fotos) {
            Bitmap guardada = fotos.get(llave);
            if (guardada != null) return guardada;
        }

        try {
            byte[] bytes = Base64.decode(fotoBase64, Base64.DEFAULT);

            // Leer pequeña: en la lista solo se ve de 52 dp.
            BitmapFactory.Options opciones = new BitmapFactory.Options();
            opciones.inSampleSize = 2;

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opciones);

            if (bitmap != null) {
                synchronized (fotos) {
                    fotos.put(llave, bitmap);
                }
            }

            return bitmap;

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String texto(DataSnapshot dato, String campo) {
        Object valor = dato.child(campo).getValue();
        return valor == null ? "" : String.valueOf(valor);
    }
}