package com.itsoeh.sbsc.buti.vocabulario;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Una palabra del vocabulario guardada en Firebase:
 *   vocabulario/palabras/{categoria}/{id}
 *
 * imagen / audio / audio2 -> nombre del archivo que ya viene dentro de la app
 *                            (drawable y res/raw), por ejemplo "img_colores_blanco".
 * imagenBase64 / audioBase64 -> archivo subido desde el panel de Admin.
 */
public class PalabraButi {

    public String id = "";
    public String categoria = "";
    public String espanol = "";
    public String hnahnu = "";
    public int orden = 0;

    public String imagen = "";
    public String audio = "";
    public String audio2 = "";

    public String imagenBase64 = "";
    public String audioBase64 = "";

    public PalabraButi() {
        // Requerido por Firebase
    }

    public PalabraButi(String categoria, String espanol, String hnahnu) {
        this.categoria = categoria;
        this.espanol = espanol;
        this.hnahnu = hnahnu;
    }

    public static PalabraButi desde(@NonNull DataSnapshot dato, String categoria) {

        PalabraButi p = new PalabraButi();

        p.id = dato.getKey() == null ? "" : dato.getKey();
        p.categoria = categoria;
        p.espanol = texto(dato, "espanol");
        p.hnahnu = texto(dato, "hnahnu");
        p.imagen = texto(dato, "imagen");
        p.audio = texto(dato, "audio");
        p.audio2 = texto(dato, "audio2");
        p.imagenBase64 = texto(dato, "imagenBase64");
        p.audioBase64 = texto(dato, "audioBase64");

        Long orden = dato.child("orden").getValue(Long.class);
        p.orden = orden == null ? 0 : orden.intValue();

        return p;
    }

    /** Lo que se guarda en Firebase (sin id ni categoría, que van en la ruta). */
    @Exclude
    public Map<String, Object> aMapa() {

        Map<String, Object> mapa = new HashMap<>();

        mapa.put("espanol", espanol);
        mapa.put("hnahnu", hnahnu);
        mapa.put("orden", orden);
        mapa.put("imagen", vacioANull(imagen));
        mapa.put("audio", vacioANull(audio));
        mapa.put("audio2", vacioANull(audio2));
        mapa.put("imagenBase64", vacioANull(imagenBase64));
        mapa.put("audioBase64", vacioANull(audioBase64));

        return mapa;
    }

    @Exclude
    public boolean tieneImagen() {
        return !imagen.isEmpty() || !imagenBase64.isEmpty();
    }

    @Exclude
    public boolean tieneAudio() {
        return !audio.isEmpty() || !audioBase64.isEmpty();
    }

    @Exclude
    public PalabraButi copia() {

        PalabraButi p = new PalabraButi();

        p.id = id;
        p.categoria = categoria;
        p.espanol = espanol;
        p.hnahnu = hnahnu;
        p.orden = orden;
        p.imagen = imagen;
        p.audio = audio;
        p.audio2 = audio2;
        p.imagenBase64 = imagenBase64;
        p.audioBase64 = audioBase64;

        return p;
    }

    private static String texto(DataSnapshot dato, String campo) {
        String valor = dato.child(campo).getValue(String.class);
        return valor == null ? "" : valor;
    }

    // En Firebase, guardar null borra el campo (no deja textos vacíos).
    private static Object vacioANull(String valor) {
        return valor == null || valor.isEmpty() ? null : valor;
    }
}
