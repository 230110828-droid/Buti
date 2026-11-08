package com.itsoeh.sbsc.buti.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BDHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "buti.db";
    private static final int VERSION_BD = 2;

    public static final String GRUPO_BASICO = "Lo básico";
    public static final String GRUPO_CULTURA =
            "Vida y cultura de Ixmiquilpan";

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

        // Usuarios locales existentes
        db.execSQL("CREATE TABLE usuario (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "correo TEXT NOT NULL UNIQUE," +
                "contrasena TEXT NOT NULL," +
                "motivo TEXT," +
                "puntos INTEGER NOT NULL DEFAULT 0," +
                "racha INTEGER NOT NULL DEFAULT 0," +
                "nivel INTEGER NOT NULL DEFAULT 1," +
                "ultima_fecha TEXT)");

        // Categorías
        db.execSQL("CREATE TABLE categoria (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "grupo TEXT NOT NULL)");

        // Vocabulario
        db.execSQL("CREATE TABLE palabra (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "categoria_id INTEGER NOT NULL," +
                "espanol TEXT NOT NULL," +
                "hnahnu TEXT NOT NULL," +
                "audio TEXT," +
                "FOREIGN KEY (categoria_id) " +
                "REFERENCES categoria(id))");

        // Niveles de aprendizaje
        db.execSQL("CREATE TABLE nivel (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "numero INTEGER NOT NULL UNIQUE," +
                "titulo TEXT NOT NULL," +
                "puntos_max INTEGER NOT NULL," +
                "categoria_id INTEGER," +
                "FOREIGN KEY (categoria_id) " +
                "REFERENCES categoria(id))");

        // Progreso antiguo: se conserva para no afectar funciones existentes
        db.execSQL("CREATE TABLE progreso (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario_id INTEGER NOT NULL," +
                "nivel_id INTEGER NOT NULL," +
                "correctas INTEGER NOT NULL," +
                "puntos INTEGER NOT NULL," +
                "fecha TEXT NOT NULL," +
                "FOREIGN KEY (usuario_id) REFERENCES usuario(id)," +
                "FOREIGN KEY (nivel_id) REFERENCES nivel(id))");

        crearTablaEtapas(db);
        crearTablaProgresoEtapas(db);

        cargarDatosIniciales(db);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int anterior,
            int nueva
    ) {

        // Migración de versión 1 a versión 2.
        // No elimina usuarios, categorías, palabras ni progreso.

        if (anterior < 2) {

            if (!columnaExiste(db, "nivel", "categoria_id")) {
                db.execSQL(
                        "ALTER TABLE nivel ADD COLUMN categoria_id INTEGER"
                );
            }

            crearTablaEtapas(db);
            crearTablaProgresoEtapas(db);

            asegurarDatosLecciones(db);
        }
    }

    // ==========================================
    // TABLA DE ETAPAS
    // ==========================================

    private void crearTablaEtapas(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS etapa (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nivel_id INTEGER NOT NULL," +
                "numero INTEGER NOT NULL," +
                "titulo TEXT NOT NULL," +
                "tipo_actividad TEXT NOT NULL," +
                "UNIQUE(nivel_id, numero)," +
                "FOREIGN KEY (nivel_id) REFERENCES nivel(id))");
    }

    // ==========================================
    // PROGRESO INDIVIDUAL DE CADA ETAPA
    // ==========================================

    private void crearTablaProgresoEtapas(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS progreso_etapa (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario_uid TEXT NOT NULL," +
                "etapa_id INTEGER NOT NULL," +
                "completada INTEGER NOT NULL DEFAULT 0," +
                "mejor_puntuacion INTEGER NOT NULL DEFAULT 0," +
                "intentos INTEGER NOT NULL DEFAULT 0," +
                "puntos_otorgados INTEGER NOT NULL DEFAULT 0," +
                "fecha TEXT," +
                "UNIQUE(usuario_uid, etapa_id)," +
                "FOREIGN KEY (etapa_id) REFERENCES etapa(id))");

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_progreso_uid " +
                        "ON progreso_etapa(usuario_uid)"
        );
    }

    // ==========================================
    // DATOS INICIALES
    // ==========================================

    private void cargarDatosIniciales(SQLiteDatabase db) {

        asegurarDatosLecciones(db);

        // Vocabulario provisional que ya tenía el proyecto.
        // No representa traducciones verificadas.

        long colores = obtenerCategoriaId(db, "Colores");

        if (colores != -1) {
            insertarPalabraSiNoExiste(
                    db, colores, "Rojo", "PENDIENTE-1"
            );

            insertarPalabraSiNoExiste(
                    db, colores, "Azul", "PENDIENTE-2"
            );

            insertarPalabraSiNoExiste(
                    db, colores, "Verde", "PENDIENTE-3"
            );

            insertarPalabraSiNoExiste(
                    db, colores, "Amarillo", "PENDIENTE-4"
            );
        }
    }

    private void asegurarDatosLecciones(SQLiteDatabase db) {

        // --------------------------------------
        // 1. CATEGORÍAS EXISTENTES
        // --------------------------------------

        asegurarCategoria(db, "Colores", GRUPO_BASICO);
        asegurarCategoria(db, "Familia", GRUPO_BASICO);
        asegurarCategoria(db, "Números", GRUPO_BASICO);
        asegurarCategoria(db, "Saludos", GRUPO_BASICO);

        asegurarCategoria(db, "Plantas", GRUPO_CULTURA);
        asegurarCategoria(db, "Animales", GRUPO_CULTURA);
        asegurarCategoria(db, "Objetos", GRUPO_CULTURA);
        asegurarCategoria(db, "Cuerpo", GRUPO_CULTURA);
        asegurarCategoria(db, "Comida", GRUPO_CULTURA);
        asegurarCategoria(
                db, "Tiempo y celebraciones", GRUPO_CULTURA
        );

        // --------------------------------------
        // 2. DIEZ NIVELES
        // --------------------------------------

        asegurarNivel(db, 1, "Saludos", "Saludos");
        asegurarNivel(db, 2, "Números", "Números");
        asegurarNivel(db, 3, "Familia", "Familia");
        asegurarNivel(db, 4, "Colores", "Colores");
        asegurarNivel(db, 5, "Animales", "Animales");
        asegurarNivel(db, 6, "Plantas", "Plantas");
        asegurarNivel(db, 7, "Objetos", "Objetos");
        asegurarNivel(db, 8, "Cuerpo", "Cuerpo");
        asegurarNivel(db, 9, "Comida", "Comida");
        asegurarNivel(
                db, 10, "Tiempo y celebraciones",
                "Tiempo y celebraciones"
        );

        // --------------------------------------
        // 3. CINCO ETAPAS POR NIVEL
        // --------------------------------------

        asegurarEtapas(db);
    }

    // ==========================================
    // CATEGORÍAS
    // ==========================================

    private long asegurarCategoria(
            SQLiteDatabase db,
            String nombre,
            String grupo
    ) {

        long id = obtenerCategoriaId(db, nombre);

        if (id != -1) {
            return id;
        }

        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("grupo", grupo);

        return db.insert("categoria", null, valores);
    }

    private long obtenerCategoriaId(
            SQLiteDatabase db,
            String nombre
    ) {

        try (Cursor c = db.rawQuery(
                "SELECT id FROM categoria WHERE nombre = ? LIMIT 1",
                new String[]{nombre}
        )) {

            if (c.moveToFirst()) {
                return c.getLong(0);
            }
        }

        return -1;
    }

    // ==========================================
    // NIVELES
    // ==========================================

    private void asegurarNivel(
            SQLiteDatabase db,
            int numero,
            String titulo,
            String categoria
    ) {

        long categoriaId = obtenerCategoriaId(db, categoria);

        if (categoriaId == -1) {
            return;
        }

        ContentValues valores = new ContentValues();
        valores.put("numero", numero);
        valores.put("titulo", titulo);
        valores.put("puntos_max", 60);
        valores.put("categoria_id", categoriaId);

        db.insertWithOnConflict(
                "nivel",
                null,
                valores,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        // También actualiza la relación de los niveles anteriores.
        db.update(
                "nivel",
                valores,
                "numero = ?",
                new String[]{String.valueOf(numero)}
        );
    }

    // ==========================================
    // ETAPAS
    // ==========================================

    private void asegurarEtapas(SQLiteDatabase db) {

        String[] titulos = {
                "Descubre",
                "Completa palabras",
                "Relaciona",
                "Reto especial",
                "Evaluación final"
        };

        String[] tipos = {
                "descubre",
                "completa_palabras",
                "relaciona",
                "reto_categoria",
                "evaluacion"
        };

        try (Cursor c = db.rawQuery(
                "SELECT id, numero FROM nivel ORDER BY numero",
                null
        )) {

            while (c.moveToNext()) {

                long nivelId = c.getLong(0);

                for (int i = 0; i < 5; i++) {

                    ContentValues valores = new ContentValues();

                    valores.put("nivel_id", nivelId);
                    valores.put("numero", i + 1);
                    valores.put("titulo", titulos[i]);
                    valores.put("tipo_actividad", tipos[i]);

                    db.insertWithOnConflict(
                            "etapa",
                            null,
                            valores,
                            SQLiteDatabase.CONFLICT_IGNORE
                    );
                }
            }
        }
    }

    // ==========================================
    // PALABRAS
    // ==========================================

    private void insertarPalabraSiNoExiste(
            SQLiteDatabase db,
            long categoriaId,
            String espanol,
            String hnahnu
    ) {

        try (Cursor c = db.rawQuery(
                "SELECT id FROM palabra " +
                        "WHERE categoria_id = ? AND espanol = ? LIMIT 1",
                new String[]{
                        String.valueOf(categoriaId),
                        espanol
                }
        )) {

            if (c.moveToFirst()) {
                return;
            }
        }

        ContentValues valores = new ContentValues();

        valores.put("categoria_id", categoriaId);
        valores.put("espanol", espanol);
        valores.put("hnahnu", hnahnu);
        valores.putNull("audio");

        db.insert("palabra", null, valores);
    }

    // ==========================================
    // VERIFICAR COLUMNAS
    // ==========================================

    private boolean columnaExiste(
            SQLiteDatabase db,
            String tabla,
            String columna
    ) {

        try (Cursor c = db.rawQuery(
                "PRAGMA table_info(" + tabla + ")",
                null
        )) {

            while (c.moveToNext()) {

                String nombre = c.getString(1);

                if (columna.equals(nombre)) {
                    return true;
                }
            }
        }

        return false;
    }
}
