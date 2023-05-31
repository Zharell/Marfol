package entities;

import java.io.Serializable;

public class Historial implements Serializable {
    private String historial;
    private String fecha;
    private String restaurante;

    public Historial(String historial, String fecha, String restaurante) {
        this.historial = historial;
        this.fecha = fecha;
        this.restaurante = restaurante;
    }

    public String getHistorial() {
        return historial;
    }

    public void setHistorial(String historial) {
        this.historial = historial;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(String restaurante) {
        this.restaurante = restaurante;
    }
}
