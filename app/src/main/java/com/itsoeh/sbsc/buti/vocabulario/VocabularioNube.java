package com.itsoeh.sbsc.buti.vocabulario;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Base64;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Vocabulario de B'ÜTI guardado en Firebase Realtime Database.
 *
 *   vocabulario/palabras/{categoria}/{id} = {espanol, hnahnu, orden,
 *                                           imagen, audio, audio2,
 *                                           imagenBase64, audioBase64}
 *
 * - La app lee de aquí (Vocabulario, Práctica y Lecciones).
 * - Si no hay internet, usa la última copia guardada en el celular.
 * - Si Firebase todavía está vacío, usa VocabularioDatos (lo que viene en la app).
 * - El Admin agrega, edita, elimina e importa desde aquí.
 *
 * Se inicia una sola vez en ButiApp: VocabularioNube.iniciar(this);
 */
public final class VocabularioNube {

    /** Claves de las 11 categorías (así se guardan en Firebase). */
    public static final String[] CATEGORIAS = {
            "saludos", "numeros", "colores", "animales", "plantas", "objetos",
            "cuerpo", "comida", "tiempo", "naturaleza", "lugares"
    };

    /** Nombres para mostrar, en el mismo orden que CATEGORIAS. */
    public static final String[] NOMBRES = {
            "Saludos", "Números", "Colores", "Animales", "Plantas", "Objetos",
            "Cuerpo", "Alimentos", "Tiempo", "Naturaleza", "Lugares"
    };

    public interface AlCambiar {
        void cambio();
    }

    public interface AlTerminar {
        void listo(boolean exito, @Nullable String error);
    }

    private static final String RUTA = "vocabulario/palabras";
    private static final String ARCHIVO_CACHE = "vocabulario_nube.json";

    private static final Map<String, List<PalabraButi>> datos = new HashMap<>();
    private static final Map<String, Bitmap> imagenes = new HashMap<>();
    private static final List<AlCambiar> escuchas = new CopyOnWriteArrayList<>();

    private static boolean iniciado = false;
    private static boolean llegoDeFirebase = false;
    private static boolean escuchando = false;
    private static ValueEventListener escuchaFirebase;
    private static File archivoCache;
    private static File carpetaAudios;

    private VocabularioNube() {
    }

    // =========================================================
    // INICIO
    // =========================================================

    public static synchronized void iniciar(@NonNull Context context) {

        if (iniciado) return;
        iniciado = true;

        Context app = context.getApplicationContext();

        archivoCache = new File(app.getFilesDir(), ARCHIVO_CACHE);
        carpetaAudios = new File(app.getCacheDir(), "audios_vocabulario");

        // 1) Lo último que se descargó (sirve sin internet).
        leerCache();

        // 2) Escuchar Firebase en vivo. Las reglas piden sesión iniciada,
        //    así que se conecta cuando el usuario entra (y se reconecta
        //    si cambia de cuenta o si Firebase negó el permiso antes).
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            if (auth.getCurrentUser() != null) {
                conectar();
            }
        });
    }

    /** Vuelve a pedir el vocabulario a Firebase si no se está escuchando. */
    public static synchronized void conectar() {

        if (escuchando) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        escuchando = true;

        escuchaFirebase = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                llegoDeFirebase = true;
                ultimoError = null;
                cargarDesde(snapshot);
                guardarCache();
                avisar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Sin permiso: Firebase quita el listener. Se reintenta
                // la próxima vez que se llame a conectar().
                synchronized (VocabularioNube.class) {
                    escuchando = false;
                }
                ultimoError = error.getMessage();
                avisar();
            }
        };

        FirebaseDatabase.getInstance().getReference(RUTA).addValueEventListener(escuchaFirebase);
    }

    @Nullable
    private static String ultimoError;

    /** Último error de Firebase (por ejemplo, permiso denegado). */
    @Nullable
    public static String ultimoError() {
        return ultimoError;
    }

    /** true cuando ya respondió Firebase (no solo la copia local). */
    public static boolean listoDesdeFirebase() {
        return llegoDeFirebase;
    }

    public static void escuchar(AlCambiar escucha) {
        if (escucha != null && !escuchas.contains(escucha)) escuchas.add(escucha);
    }

    public static void dejarDeEscuchar(AlCambiar escucha) {
        escuchas.remove(escucha);
    }

    // =========================================================
    // LECTURA PARA LAS PANTALLAS
    // =========================================================

    /** Convierte "Alimentos", "Números", "COMIDA"... a la clave de Firebase. */
    public static String clave(@Nullable String categoria) {

        String c = quitarAcentos(categoria).toLowerCase(Locale.ROOT).trim();

        if (c.equals("alimentos")) return "comida";

        return c;
    }

    public static String nombre(@Nullable String clave) {

        String c = clave(clave);

        for (int i = 0; i < CATEGORIAS.length; i++) {
            if (CATEGORIAS[i].equals(c)) return NOMBRES[i];
        }

        return clave == null ? "" : clave;
    }

    /**
     * Palabras de una categoría como {español, hñähñu}.
     * Mismo formato que VocabularioDatos, para no cambiar las pantallas.
     */
    public static String[][] arreglo(@Nullable String categoria) {

        List<PalabraButi> lista = palabras(categoria);

        if (lista.isEmpty()) {
            return respaldo(categoria);
        }

        String[][] resultado = new String[lista.size()][2];

        for (int i = 0; i < lista.size(); i++) {
            resultado[i][0] = lista.get(i).espanol;
            resultado[i][1] = lista.get(i).hnahnu;
        }

        return resultado;
    }

    /** Copia de las palabras de una categoría, en orden. */
    public static synchronized List<PalabraButi> palabras(@Nullable String categoria) {

        List<PalabraButi> lista = datos.get(clave(categoria));

        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    public static synchronized int total() {

        int total = 0;

        for (List<PalabraButi> lista : datos.values()) total += lista.size();

        return total;
    }

    /** ¿Hay alguna categoría que todavía no está en Firebase? */
    public static synchronized boolean faltanCategorias() {

        for (String c : CATEGORIAS) {
            List<PalabraButi> lista = datos.get(c);
            if (lista == null || lista.isEmpty()) return true;
        }

        return false;
    }

    @Nullable
    public static synchronized PalabraButi buscar(@Nullable String categoria, @Nullable String espanol) {

        if (espanol == null) return null;

        List<PalabraButi> lista = datos.get(clave(categoria));

        if (lista == null) return null;

        String buscada = comparable(espanol);

        for (PalabraButi p : lista) {
            if (comparable(p.espanol).equals(buscada)) return p;
        }

        return null;
    }

    // =========================================================
    // IMÁGENES
    // =========================================================

    /** Imagen de la app (drawable) guardada para esa palabra; 0 si no hay. */
    public static int recursoImagen(Context context, @Nullable String categoria, @Nullable String espanol) {

        PalabraButi p = buscar(categoria, espanol);

        if (context == null || p == null || p.imagen.isEmpty()) return 0;

        return context.getResources().getIdentifier(p.imagen, "drawable", context.getPackageName());
    }

    /** Imagen subida desde el Admin (Base64); null si no hay. */
    @Nullable
    public static Bitmap imagenSubida(@Nullable String categoria, @Nullable String espanol) {

        PalabraButi p = buscar(categoria, espanol);

        return p == null ? null : decodificarImagen(p);
    }

    @Nullable
    public static synchronized Bitmap decodificarImagen(@NonNull PalabraButi p) {

        if (p.imagenBase64.isEmpty()) return null;

        String llave = p.id + "_" + p.imagenBase64.hashCode();

        Bitmap guardada = imagenes.get(llave);

        if (guardada != null) return guardada;

        try {
            byte[] bytes = Base64.decode(p.imagenBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            if (bitmap != null) imagenes.put(llave, bitmap);

            return bitmap;

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Pone la imagen de la palabra (primero la subida, luego la de la app).
     * Devuelve false si la palabra no tiene imagen.
     */
    public static boolean ponerImagen(ImageView vista, @Nullable String categoria, @Nullable String espanol) {

        if (vista == null) return false;

        Bitmap subida = imagenSubida(categoria, espanol);

        if (subida != null) {
            vista.setImageBitmap(subida);
            return true;
        }

        int recurso = recursoImagen(vista.getContext(), categoria, espanol);

        if (recurso != 0) {
            vista.setImageResource(recurso);
            return true;
        }

        return false;
    }

    // =========================================================
    // AUDIO
    // =========================================================

    /**
     * Reproductor listo para la palabra: primero el audio subido,
     * luego el de res/raw. Devuelve null si no tiene audio.
     */
    @Nullable
    public static MediaPlayer crearReproductor(Context context, @Nullable String categoria, @Nullable String espanol) {

        if (context == null) return null;

        PalabraButi p = buscar(categoria, espanol);

        if (p != null && !p.audioBase64.isEmpty()) {

            File archivo = archivoAudio(p);

            if (archivo != null) {
                try {
                    MediaPlayer reproductor = new MediaPlayer();
                    reproductor.setDataSource(archivo.getAbsolutePath());
                    reproductor.prepare();
                    return reproductor;
                } catch (Exception ignored) {
                    // Si falla, intenta con el audio de la app.
                }
            }
        }

        int audio = AudiosVocabulario.obtenerAudio(context, categoria, espanol);

        return audio == 0 ? null : MediaPlayer.create(context, audio);
    }

    /** ¿La palabra tiene algún audio (subido o de la app)? */
    public static boolean tieneAudio(Context context, @Nullable String categoria, @Nullable String espanol) {

        PalabraButi p = buscar(categoria, espanol);

        if (p != null && !p.audioBase64.isEmpty()) return true;

        return AudiosVocabulario.obtenerAudio(context, categoria, espanol) != 0;
    }

    /** Escribe el audio Base64 en un archivo temporal (una sola vez). */
    @Nullable
    public static File archivoAudio(@NonNull PalabraButi p) {

        if (p.audioBase64.isEmpty() || carpetaAudios == null) return null;

        if (!carpetaAudios.exists() && !carpetaAudios.mkdirs()) return null;

        File archivo = new File(carpetaAudios,
                "audio_" + p.id + "_" + Integer.toHexString(p.audioBase64.hashCode()));

        if (archivo.exists()) return archivo;

        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            salida.write(Base64.decode(p.audioBase64, Base64.DEFAULT));
            return archivo;
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================
    // ADMIN: AGREGAR, EDITAR, ELIMINAR, IMPORTAR
    // =========================================================

    /**
     * Guarda una palabra nueva o editada.
     * Si cambió de categoría, se borra de la anterior en el mismo paso.
     */
    public static void guardar(@NonNull PalabraButi palabra,
                               @Nullable String categoriaAnterior,
                               @Nullable AlTerminar alTerminar) {

        DatabaseReference raiz = FirebaseDatabase.getInstance().getReference(RUTA);

        String categoria = clave(palabra.categoria);
        palabra.categoria = categoria;

        if (palabra.id == null || palabra.id.isEmpty()) {
            palabra.id = raiz.child(categoria).push().getKey();
            palabra.orden = siguienteOrden(categoria);
        }

        Map<String, Object> cambios = new HashMap<>();
        cambios.put(categoria + "/" + palabra.id, palabra.aMapa());

        String anterior = clave(categoriaAnterior);

        if (!anterior.isEmpty() && !anterior.equals(categoria)) {
            cambios.put(anterior + "/" + palabra.id, null);
            palabra.orden = siguienteOrden(categoria);
            cambios.put(categoria + "/" + palabra.id, palabra.aMapa());
        }

        raiz.updateChildren(cambios).addOnCompleteListener(t -> {
            if (alTerminar != null) {
                alTerminar.listo(t.isSuccessful(),
                        t.getException() == null ? null : t.getException().getMessage());
            }
        });
    }

    public static void eliminar(@NonNull PalabraButi palabra, @Nullable AlTerminar alTerminar) {

        FirebaseDatabase.getInstance()
                .getReference(RUTA)
                .child(clave(palabra.categoria))
                .child(palabra.id)
                .removeValue()
                .addOnCompleteListener(t -> {
                    if (alTerminar != null) {
                        alTerminar.listo(t.isSuccessful(),
                                t.getException() == null ? null : t.getException().getMessage());
                    }
                });
    }

    /**
     * Sube a Firebase todo el vocabulario que ya viene en la app
     * (VocabularioDatos + nombres de imágenes y audios).
     * Solo sube las categorías que todavía están vacías, así que
     * presionarlo dos veces no duplica nada.
     */
    public static void importarDesdeApp(@NonNull Context context, @Nullable AlTerminar alTerminar) {

        // Sin la respuesta de Firebase no se sabe qué ya existe: no importar a ciegas.
        if (!llegoDeFirebase) {
            if (alTerminar != null) {
                alTerminar.listo(false, "Todavía no hay conexión con Firebase. Revisa tu internet.");
            }
            return;
        }

        Map<String, Object> cambios = new HashMap<>();
        DatabaseReference raiz = FirebaseDatabase.getInstance().getReference(RUTA);

        for (String categoria : CATEGORIAS) {

            if (!palabras(categoria).isEmpty()) continue;

            String[][] lista = respaldo(categoria);

            for (int i = 0; i < lista.length; i++) {

                if (lista[i] == null || lista[i].length < 2) continue;

                PalabraButi p = new PalabraButi(categoria, lista[i][0], lista[i][1]);
                p.orden = i + 1;
                p.imagen = buscarImagenDeLaApp(context, categoria, p.espanol);

                List<String> audios = AudiosVocabulario.nombresDeLaApp(context, categoria, p.espanol);
                if (audios.size() > 0) p.audio = audios.get(0);
                if (audios.size() > 1) p.audio2 = audios.get(1);

                String id = raiz.child(categoria).push().getKey();
                cambios.put(categoria + "/" + id, p.aMapa());
            }
        }

        if (cambios.isEmpty()) {
            if (alTerminar != null) alTerminar.listo(true, "Ya estaba todo importado.");
            return;
        }

        raiz.updateChildren(cambios).addOnCompleteListener(t -> {
            if (alTerminar != null) {
                alTerminar.listo(t.isSuccessful(),
                        t.isSuccessful()
                                ? String.valueOf(cambios.size())
                                : (t.getException() == null ? null : t.getException().getMessage()));
            }
        });
    }

    /** Palabras que trae la app de fábrica (por si Firebase está vacío). */
    public static String[][] respaldo(@Nullable String categoria) {

        switch (clave(categoria)) {
            case "saludos":
                return VocabularioDatos.SALUDOS;
            case "numeros":
                return VocabularioDatos.NUMEROS;
            case "colores":
                return VocabularioDatos.COLORES;
            case "animales":
                return VocabularioDatos.ANIMALES;
            case "plantas":
                return VocabularioDatos.PLANTAS;
            case "objetos":
                return VocabularioDatos.OBJETOS;
            case "cuerpo":
                return VocabularioDatos.CUERPO;
            case "comida":
                return VocabularioDatos.COMIDA;
            case "tiempo":
                return VocabularioDatos.TIEMPO;
            case "naturaleza":
                return VocabularioDatos.NATURALEZA;
            case "lugares":
                return VocabularioDatos.LUGARES;
            default:
                return new String[0][0];
        }
    }

    // =========================================================
    // AYUDANTES INTERNOS
    // =========================================================

    private static synchronized int siguienteOrden(String categoria) {

        int mayor = 0;

        List<PalabraButi> lista = datos.get(categoria);

        if (lista != null) {
            for (PalabraButi p : lista) mayor = Math.max(mayor, p.orden);
        }

        return mayor + 1;
    }

    private static synchronized void cargarDesde(DataSnapshot snapshot) {

        datos.clear();

        for (DataSnapshot categoria : snapshot.getChildren()) {

            String clave = categoria.getKey();
            if (clave == null) continue;

            List<PalabraButi> lista = new ArrayList<>();

            for (DataSnapshot palabra : categoria.getChildren()) {
                PalabraButi p = PalabraButi.desde(palabra, clave);
                if (!p.espanol.isEmpty()) lista.add(p);
            }

            ordenar(lista);
            datos.put(clave, lista);
        }
    }

    private static void ordenar(List<PalabraButi> lista) {
        Collections.sort(lista, (a, b) -> Integer.compare(a.orden, b.orden));
    }

    private static void avisar() {
        for (AlCambiar escucha : escuchas) {
            try {
                escucha.cambio();
            } catch (Exception ignored) {
            }
        }
    }

    // ---------- copia local en el celular ----------

    private static synchronized void guardarCache() {

        if (archivoCache == null) return;

        try {
            JSONObject raiz = new JSONObject();

            for (Map.Entry<String, List<PalabraButi>> entrada : datos.entrySet()) {

                JSONArray arreglo = new JSONArray();

                for (PalabraButi p : entrada.getValue()) {
                    JSONObject o = new JSONObject();
                    o.put("id", p.id);
                    o.put("espanol", p.espanol);
                    o.put("hnahnu", p.hnahnu);
                    o.put("orden", p.orden);
                    o.put("imagen", p.imagen);
                    o.put("audio", p.audio);
                    o.put("audio2", p.audio2);
                    o.put("imagenBase64", p.imagenBase64);
                    o.put("audioBase64", p.audioBase64);
                    arreglo.put(o);
                }

                raiz.put(entrada.getKey(), arreglo);
            }

            try (FileOutputStream salida = new FileOutputStream(archivoCache)) {
                salida.write(raiz.toString().getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception ignored) {
        }
    }

    private static synchronized void leerCache() {

        if (archivoCache == null || !archivoCache.exists()) return;

        try {
            String texto = new String(Files.readAllBytes(archivoCache.toPath()), StandardCharsets.UTF_8);
            JSONObject raiz = new JSONObject(texto);

            datos.clear();

            JSONArray nombres = raiz.names();
            if (nombres == null) return;

            for (int i = 0; i < nombres.length(); i++) {

                String clave = nombres.getString(i);
                JSONArray arreglo = raiz.getJSONArray(clave);
                List<PalabraButi> lista = new ArrayList<>();

                for (int j = 0; j < arreglo.length(); j++) {
                    JSONObject o = arreglo.getJSONObject(j);
                    PalabraButi p = new PalabraButi();
                    p.id = o.optString("id");
                    p.categoria = clave;
                    p.espanol = o.optString("espanol");
                    p.hnahnu = o.optString("hnahnu");
                    p.orden = o.optInt("orden");
                    p.imagen = o.optString("imagen");
                    p.audio = o.optString("audio");
                    p.audio2 = o.optString("audio2");
                    p.imagenBase64 = o.optString("imagenBase64");
                    p.audioBase64 = o.optString("audioBase64");
                    lista.add(p);
                }

                ordenar(lista);
                datos.put(clave, lista);
            }

        } catch (Exception ignored) {
            datos.clear();
        }
    }

    // ---------- textos ----------

    private static String quitarAcentos(@Nullable String texto) {

        if (texto == null) return "";

        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    /** Para comparar palabras sin importar acentos, mayúsculas ni signos. */
    private static String comparable(@Nullable String texto) {

        return quitarAcentos(texto)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    // ---------- nombres de las imágenes que ya vienen en la app ----------

    /** Busca la ilustración img_<categoria>_<palabra> en drawable. */
    public static String buscarImagenDeLaApp(Context context, String categoria, String espanol) {

        String palabra = quitarAcentos(espanol)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[¿?¡!.()]", "")
                .replace(",", "")
                .replaceAll("\\s+", " ")
                .trim()
                .replace(" ", "_");

        // Nombres especiales de algunos archivos.
        switch (palabra) {
            case "me_da_permiso_pasar":
                palabra = "permiso_para_pasar";
                break;
            case "estoy_triste_estoy_preocupado":
                palabra = "estoy_triste_preocupado";
                break;
            case "hasta_luego_hasta_pronto":
                palabra = "hasta_luego";
                break;
            case "donde_estas_donde_andas":
                palabra = "donde_estas";
                break;
            case "a_noche":
                palabra = "anoche";
                break;
            case "servilleta_de_tortillas":
                palabra = "servilleta_tortillas";
                break;
            default:
                break;
        }

        String[] candidatos = {
                "img_" + clave(categoria) + "_" + palabra,
                "img_" + clave(categoria) + "_" + palabra.replace("_", "")
        };

        for (String nombre : candidatos) {
            if (context.getResources().getIdentifier(nombre, "drawable", context.getPackageName()) != 0) {
                return nombre;
            }
        }

        return "";
    }
}