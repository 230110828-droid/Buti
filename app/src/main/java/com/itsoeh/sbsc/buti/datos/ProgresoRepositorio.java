package com.itsoeh.sbsc.buti.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProgresoRepositorio {

    private final BDHelper helper;

    public ProgresoRepositorio(Context context) {
        helper = new BDHelper(context.getApplicationContext());
    }

    public List<Nivel> listarNiveles() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Nivel> lista = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, numero, titulo, puntos_max FROM nivel ORDER BY numero", null)) {
            while (c.moveToNext()) {
                Nivel n = new Nivel();
                n.id = c.getLong(0);
                n.numero = c.getInt(1);
                n.titulo = c.getString(2);
                n.puntosMax = c.getInt(3);
                lista.add(n);
            }
        }
        return lista;
    }

    /**
     * Guarda el resultado de una lección o nivel: registra el intento,
     * suma los puntos al usuario y, si el nivel se superó, desbloquea el siguiente.
     */
    public void guardarResultado(long usuarioId, int nivelNumero,
                                 int correctas, int puntos, boolean superado) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            long nivelId = -1;
            try (Cursor c = db.rawQuery("SELECT id FROM nivel WHERE numero = ?",
                    new String[]{String.valueOf(nivelNumero)})) {
                if (c.moveToFirst()) nivelId = c.getLong(0);
            }
            if (nivelId == -1) return;

            ContentValues v = new ContentValues();
            v.put("usuario_id", usuarioId);
            v.put("nivel_id", nivelId);
            v.put("correctas", correctas);
            v.put("puntos", puntos);
            v.put("fecha", UsuarioRepositorio.fecha(0));
            db.insert("progreso", null, v);

            db.execSQL("UPDATE usuario SET puntos = puntos + ? WHERE id = ?",
                    new Object[]{puntos, usuarioId});

            if (superado) {
                // Solo avanza si este nivel es el último que tenía desbloqueado.
                db.execSQL("UPDATE usuario SET nivel = ? WHERE id = ? AND nivel <= ?",
                        new Object[]{nivelNumero + 1, usuarioId, nivelNumero});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
