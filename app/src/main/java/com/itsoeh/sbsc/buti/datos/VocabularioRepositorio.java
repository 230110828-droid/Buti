package com.itsoeh.sbsc.buti.datos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class VocabularioRepositorio {

    private final BDHelper helper;

    public VocabularioRepositorio(Context context) {
        helper = new BDHelper(context.getApplicationContext());
    }

    /** grupo = BDHelper.GRUPO_BASICO, BDHelper.GRUPO_CULTURA, o null para todas. */
    public List<Categoria> listarCategorias(String grupo) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT id, nombre, grupo FROM categoria"
                + (grupo == null ? "" : " WHERE grupo = ?") + " ORDER BY id";
        String[] args = grupo == null ? null : new String[]{grupo};
        List<Categoria> lista = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                Categoria cat = new Categoria();
                cat.id = c.getLong(0);
                cat.nombre = c.getString(1);
                cat.grupo = c.getString(2);
                lista.add(cat);
            }
        }
        return lista;
    }

    public List<Palabra> listarPalabras(long categoriaId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Palabra> lista = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, categoria_id, espanol, hnahnu, audio FROM palabra "
                        + "WHERE categoria_id = ? ORDER BY id",
                new String[]{String.valueOf(categoriaId)})) {
            while (c.moveToNext()) {
                Palabra p = new Palabra();
                p.id = c.getLong(0);
                p.categoriaId = c.getLong(1);
                p.espanol = c.getString(2);
                p.hnahnu = c.getString(3);
                p.audio = c.getString(4);
                lista.add(p);
            }
        }
        return lista;
    }
}
