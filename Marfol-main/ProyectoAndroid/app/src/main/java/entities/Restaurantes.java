package entities;

import java.io.Serializable;

public class Restaurantes implements Serializable {
    private String nombreRestaurante;
    private String usuarioId;

    public Restaurantes(String nombreRestaurante, String usuarioId) {
        this.nombreRestaurante = nombreRestaurante;
        this.usuarioId = usuarioId;
    }

    public String getNombreRestaurante() {
        return nombreRestaurante;
    }

    public void setNombreRestaurante(String nombreRestaurante) {
        this.nombreRestaurante = nombreRestaurante;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
