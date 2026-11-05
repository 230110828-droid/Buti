package com.itsoeh.sbsc.buti.admin;

public class PalabraSimulada {

    private String leccion;
    private String palabraEspanol;
    private String palabraHnahnu;
    private String categoria;
    private String audioNombre;
    private String imagenNombre;

    public PalabraSimulada(String leccion, String palabraEspanol, String palabraHnahnu, String categoria, String audioNombre, String imagenNombre) {
        this.leccion = leccion;
        this.palabraEspanol = palabraEspanol;
        this.palabraHnahnu = palabraHnahnu;
        this.categoria = categoria;
        this.audioNombre = audioNombre;
        this.imagenNombre = imagenNombre;
    }

    public String getLeccion() {
        return leccion;
    }

    public String getPalabraEspanol() {
        return palabraEspanol;
    }

    public String getPalabraHnahnu() {
        return palabraHnahnu;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getAudioNombre() {
        return audioNombre;
    }

    public String getImagenNombre() {
        return imagenNombre;
    }
}
