package mainActivity.menu.historial.detalle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.tfg.marfol.R;

import entities.Historial;

public class DetalleHistorialBd extends AppCompatActivity {
    private Intent intent;
    private Historial historialDet;

    private TextView tvHistorialNombreRestaurante, tvHistorialFactura, tvHistorialFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_historial_bd);
        asignarId();
        intent = getIntent();
        historialDet = (Historial) intent.getSerializableExtra("detalleHistorial");
        mostrarDatos();

    }

    private void mostrarDatos() {
        tvHistorialFecha.setText(historialDet.getFecha());
        tvHistorialNombreRestaurante.setText(historialDet.getRestaurante());
        tvHistorialFactura.setText(historialDet.getHistorial());
    }

    private void asignarId() {
        tvHistorialNombreRestaurante = findViewById(R.id.tvHistorialNombreRestaurante);
        tvHistorialFactura = findViewById(R.id.tvHistorialFactura);
        tvHistorialFecha = findViewById(R.id.tvHistorialFecha);
    }
}