package com.itsoeh.sbsc.buti.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Base de datos local (SQLite) de B'üti.
 * Si más adelante deciden usar Firebase u otra, solo se cambian los
 * repositorios; las pantallas no deberían tocar esta clase directamente.
 */
public class BDHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "buti.db";
    private static final int VERSION_BD = 1;

    public static final String GRUPO_BASICO = "Lo básico";
    public static final String GRUPO_CULTURA = "Vida y cultura de Ixmiquilpan";

    public BDHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuario ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nombre TEXT NOT NULL,"
                + "correo TEXT NOT NULL UNIQUE,"
                + "contrasena TEXT NOT NULL,"
                + "motivo TEXT,"
                + "puntos INTEGER NOT NULL DEFAULT 0,"
                + "racha INTEGER NOT NULL DEFAULT 0,"
                + "nivel INTEGER NOT NULL DEFAULT 1,"
                + "ultima_fecha TEXT)");

        db.execSQL("CREATE TABLE categoria ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nombre TEXT NOT NULL,"
                + "grupo TEXT NOT NULL)");

        db.execSQL("CREATE TABLE palabra ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "categoria_id INTEGER NOT NULL,"
                + "espanol TEXT NOT NULL,"
                + "hnahnu TEXT NOT NULL,"
                + "audio TEXT,"
                + "FOREIGN KEY (categoria_id) REFERENCES categoria(id))");

        db.execSQL("CREATE TABLE nivel ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "numero INTEGER NOT NULL UNIQUE,"
                + "titulo TEXT NOT NULL,"
                + "puntos_max INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE progreso ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "usuario_id INTEGER NOT NULL,"
                + "nivel_id INTEGER NOT NULL,"
                + "correctas INTEGER NOT NULL,"
                + "puntos INTEGER NOT NULL,"
                + "fecha TEXT NOT NULL,"
                + "FOREIGN KEY (usuario_id) REFERENCES usuario(id),"
                + "FOREIGN KEY (nivel_id) REFERENCES nivel(id))");

        cargarDatosIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        // Mientras el proyecto está en desarrollo se recrea todo.
        db.execSQL("DROP TABLE IF EXISTS progreso");
        db.execSQL("DROP TABLE IF EXISTS palabra");
        db.execSQL("DROP TABLE IF EXISTS categoria");
        db.execSQL("DROP TABLE IF EXISTS nivel");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        onCreate(db);
    }

    // ---------- Datos iniciales ----------

    private void cargarDatosIniciales(SQLiteDatabase db) {
        // Niveles (según la pantalla "Tu camino hñáhñu")
        insertarNivel(db, 1, "Saludos", 60);
        insertarNivel(db, 2, "Números", 60);
        insertarNivel(db, 3, "Familia", 60);
        insertarNivel(db, 4, "Colores", 60);
        insertarNivel(db, 5, "Animales", 60);

        // Categorías (10 en total, según las pantallas de vocabulario)
        long colores = insertarCategoria(db, "Colores", GRUPO_BASICO);
        insertarCategoria(db, "Familia", GRUPO_BASICO);
        insertarCategoria(db, "Números", GRUPO_BASICO);
        insertarCategoria(db, "Saludos", GRUPO_BASICO);
        insertarCategoria(db, "Plantas", GRUPO_CULTURA);
        insertarCategoria(db, "Animales", GRUPO_CULTURA);
        insertarCategoria(db, "Objetos", GRUPO_CULTURA);
        insertarCategoria(db, "Cuerpo", GRUPO_CULTURA);
        insertarCategoria(db, "Comida", GRUPO_CULTURA);
        insertarCategoria(db, "Tiempo y celebraciones", GRUPO_CULTURA);

        // PENDIENTE: estas palabras en hñähñu son solo de relleno para probar
        // las pantallas. Reemplazarlas con el vocabulario revisado por el
        // hablante antes de la entrega.
        insertarPalabra(db, colores, "Rojo", "PENDIENTE-1", null);
        insertarPalabra(db, colores, "Azul", "PENDIENTE-2", null);
        insertarPalabra(db, colores, "Verde", "PENDIENTE-3", null);
        insertarPalabra(db, colores, "Amarillo", "PENDIENTE-4", null);
    }

    private long insertarCategoria(SQLiteDatabase db, String nombre, String grupo) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("grupo", grupo);
        return db.insert("categoria", null, v);
    }

    private void insertarNivel(SQLiteDatabase db, int numero, String titulo, int puntosMax) {
        ContentValues v = new ContentValues();
        v.put("numero", numero);
        v.put("titulo", titulo);
        v.put("puntos_max", puntosMax);
        db.insert("nivel", null, v);
    }

    private void insertarPalabra(SQLiteDatabase db, long categoriaId,
                                 String espanol, String hnahnu, String audio) {
        ContentValues v = new ContentValues();
        v.put("categoria_id", categoriaId);
        v.put("espanol", espanol);
        v.put("hnahnu", hnahnu);
        v.put("audio", audio);
        db.insert("palabra", null, v);
    }
}
