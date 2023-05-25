package mainActivity.menu.crudBd.detalle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.tfg.marfol.R;

import java.util.ArrayList;

import entities.Persona;
import entities.Plato;

public class DetalleEditarPlatosBd extends AppCompatActivity {
    private Plato platoBd;
    private ArrayList<Plato> platosTotales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_editar_platos_bd);
    }
}