package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class Persona implements Serializable {

    private String nombre,descripcion, urlImage;
    private ArrayList<Plato> platos;

    public Persona () {}

    public Persona (String nombre, String descripcion, String urlImage, ArrayList<Plato> platos) {
        this.nombre=nombre;
        this.descripcion=descripcion;
        this.urlImage=urlImage;
        this.platos=platos;
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public ArrayList<Plato> getPlatos() { return platos; }

    public void setPlatos(ArrayList<Plato> platos) { this.platos = platos;}

}
