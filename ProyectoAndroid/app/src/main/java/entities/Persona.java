package entities;

import java.io.Serializable;
import java.util.List;

public class Persona implements Serializable {

    private String nombre,descripcion, urlImage;
    private List<Plato> platos;

    public Persona () {}

    public Persona (String nombre, String descripcion, String urlImage, List<Plato> platos) {
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

    public List<Plato> getPlatos() { return platos; }

    public void setPlatos(List<Plato> platos) { this.platos = platos;}

}
