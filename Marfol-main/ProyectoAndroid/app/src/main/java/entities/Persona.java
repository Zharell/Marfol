package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class Persona implements Serializable {

    private int comensalCode;
    private double monedero=0;
    private String nombre,descripcion, urlImage;
    private ArrayList<Plato> platos;

    public Persona () {}

    public Persona(String nombre, String descripcion, String urlImage) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlImage = urlImage;
    }

    public Persona (int comensalCode, String nombre, String descripcion, String urlImage, ArrayList<Plato> platos, double monedero) {
        this.comensalCode=comensalCode;
        this.nombre=nombre;
        this.descripcion=descripcion;
        this.urlImage=urlImage;
        this.platos=platos;
        this.monedero=monedero;
    }

    //Método devuelve el total a pagar
    public void asignarPrecio () {
        double precioTotal=0;
        for (int i=1;i<platos.size();i++) {
            if (!platos.get(i).isCompartido()) {
                monedero+=platos.get(i).getPrecio();
            }
        }
    }

    //Método que devuelve el número de platos añadidos, se resta 1 ya que es el plato botón (añadir plato)
    public int obtenerNumPlatos() {
        return (platos.size()-1);
    }

    //Método que comprueba si el comensal posee algún plato
    public boolean existePlato() {
        if (platos.size()>=2) {
            return true;
        }
        return false;
    }

    public void sumarMonedero(double nuevoPrecio) {
        monedero+=nuevoPrecio;
    }

    public double getMonedero() { return monedero; }

    public void setMonedero(double monedero) { this.monedero = monedero; }

    public int getComensalCode() { return comensalCode; }

    public void setComensalCode(int comensalCode) { this.comensalCode = comensalCode; }

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
