package com.itsoeh.sbsc.buti.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UsuarioRepositorio {

    private final BDHelper helper;

    public UsuarioRepositorio(Context context) {
        helper = new BDHelper(context.getApplicationContext());
    }

    /** Devuelve el id del usuario nuevo, o -1 si el correo ya existe. */
    public long registrar(String nombre, String correo, String contrasena) {
        String c = normalizar(correo);
        if (existeCorreo(c)) return -1;
        ContentValues v = new ContentValues();
        v.put("nombre", nombre.trim());
        v.put("correo", c);
        v.put("contrasena", hash(contrasena));
        return helper.getWritableDatabase().insert("usuario", null, v);
    }

    public boolean existeCorreo(String correo) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM usuario WHERE correo = ?",
                new String[]{normalizar(correo)})) {
            return c.moveToFirst();
        }
    }

    /** Devuelve el usuario si correo y contraseña coinciden, o null si no. */
    public Usuario iniciarSesion(String correo, String contrasena) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, nombre, correo, motivo, puntos, racha, nivel "
                        + "FROM usuario WHERE correo = ? AND contrasena = ?",
                new String[]{normalizar(correo), hash(contrasena)})) {
            return c.moveToFirst() ? aUsuario(c) : null;
        }
    }

    public Usuario obtener(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, nombre, correo, motivo, puntos, racha, nivel "
                        + "FROM usuario WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? aUsuario(c) : null;
        }
    }

    public void guardarMotivo(long id, String motivo) {
        ContentValues v = new ContentValues();
        v.put("motivo", motivo);
        helper.getWritableDatabase().update("usuario", v, "id = ?",
                new String[]{String.valueOf(id)});
    }

    public void actualizarNombre(long id, String nombre) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre.trim());
        helper.getWritableDatabase().update("usuario", v, "id = ?",
                new String[]{String.valueOf(id)});
    }

    /** Para "Cambiar contraseña" o el final de "Olvidaste tu contraseña". */
    public boolean cambiarContrasena(String correo, String nueva) {
        ContentValues v = new ContentValues();
        v.put("contrasena", hash(nueva));
        return helper.getWritableDatabase().update("usuario", v, "correo = ?",
                new String[]{normalizar(correo)}) > 0;
    }

    /**
     * Llamar una vez al día cuando el usuario practica.
     * Si ya practicó hoy no cambia nada; si practicó ayer suma 1 a la racha;
     * si pasó más tiempo, la racha vuelve a 1.
     */
    public void registrarActividadDiaria(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String hoy = fecha(0);
        String ayer = fecha(-1);
        String ultima = null;
        int racha = 0;
        try (Cursor c = db.rawQuery("SELECT ultima_fecha, racha FROM usuario WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            if (!c.moveToFirst()) return;
            ultima = c.getString(0);
            racha = c.getInt(1);
        }
        if (hoy.equals(ultima)) return;
        int nueva = ayer.equals(ultima) ? racha + 1 : 1;
        ContentValues v = new ContentValues();
        v.put("racha", nueva);
        v.put("ultima_fecha", hoy);
        db.update("usuario", v, "id = ?", new String[]{String.valueOf(id)});
    }

    // ---------- Auxiliares ----------

    private Usuario aUsuario(Cursor c) {
        Usuario u = new Usuario();
        u.id = c.getLong(0);
        u.nombre = c.getString(1);
        u.correo = c.getString(2);
        u.motivo = c.getString(3);
        u.puntos = c.getInt(4);
        u.racha = c.getInt(5);
        u.nivel = c.getInt(6);
        return u;
    }

    private String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    static String fecha(int diasDesdeHoy) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, diasDesdeHoy);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
    }

    /** SHA-256 sin sal: suficiente para un proyecto escolar local, no para producción. */
    private String hash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
