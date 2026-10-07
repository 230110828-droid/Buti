package com.itsoeh.sbsc.buti.datos;

public class Usuario {
    public long id;
    public String nombre;
    public String correo;
    public String motivo;   // "Raíces familiares", "Curiosidad cultural", "Trabajo o escuela"
    public int puntos;
    public int racha;
    public int nivel;       // nivel actual desbloqueado (empieza en 1)

    public Usuario() { }
}
