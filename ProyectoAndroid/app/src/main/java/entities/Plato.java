package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class Plato implements Serializable {

    private String nombre, descripcion, urlImage;

    //Precio --> Precio Original del plato
    //PrecioFinal --> Precio final si se ha compartido puede variar
    private double precio, precioFinal;
    private boolean compartido;

    private ArrayList<Persona> personasCompartir;

    

    public Plato (String nombre, String descripcion, double precio,double precioFinal,String urlImage , boolean compartido, ArrayList<Persona> personasCompartir) {
        this.nombre=nombre;
        this.descripcion=descripcion;
        this.precio=precio;
        this.precioFinal=precioFinal;
        this.urlImage=urlImage;
        this.compartido=compartido;
        this.personasCompartir=personasCompartir;
    }



    public ArrayList<Persona> getPersonasCompartir() { return personasCompartir;}

    public void setPersonasCompartir(ArrayList<Persona> personasCompartir) { this.personasCompartir = personasCompartir; }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getPrecioFinal() { return precioFinal; }

    public void setPrecioFinal(double precioFinal) { this.precioFinal = precioFinal;}

    public boolean isCompartido() { return compartido; }

    public void setCompartido(boolean compartido) { this.compartido = compartido; }

    public String getUrlImage() { return urlImage; }

    public void setUrlImage(String urlImage) { this.urlImage = urlImage; }
}
