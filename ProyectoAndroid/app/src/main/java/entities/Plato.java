package entities;

public class Plato {

    private String nombre, descripcion;

    //Precio --> Precio Original del plato
    //PrecioFinal --> Precio final si se ha compartido puede variar
    private double precio, precioFinal;
    private boolean compartido;

    public Plato (String nombre, String descripcion, double precio,double precioFinal , boolean compartido) {
        this.nombre=nombre;
        this.descripcion=descripcion;
        this.precio=precio;
        this.precioFinal=precioFinal;
        this.compartido=compartido;
    }

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
}
