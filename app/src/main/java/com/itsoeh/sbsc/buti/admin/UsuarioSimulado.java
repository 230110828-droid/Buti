package com.itsoeh.sbsc.buti.admin;

public class UsuarioSimulado {

    private String nombre;
    private String correo;
    private int rachaDias;
    private int nivel;

    public UsuarioSimulado(String nombre, String correo, int rachaDias, int nivel) {
        this.nombre = nombre;
        this.correo = correo;
        this.rachaDias = rachaDias;
        this.nivel = nivel;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public int getRachaDias() {
        return rachaDias;
    }

    public int getNivel() {
        return nivel;
    }
}
