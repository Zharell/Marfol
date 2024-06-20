package mainActivity.menu.historial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.CrudPlatosAdapter;
import adapters.HistorialAdapter;
import entities.Historial;
import entities.Plato;
import mainActivity.menu.historial.detalle.DetalleHistorialBd;

public class HistorialBd extends AppCompatActivity implements HistorialAdapter.onItemClickListener{
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView rvHistorial;
    private HistorialAdapter historialAdapter;
    private ArrayList<Historial> historialBd;
    private Historial hist;
    private String email,historial,fecha,restaurante;
    private CollectionReference historialRef;
    private Query consulta;
    private Intent intent;
    private ActivityResultLauncher rLauncherHistorial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_bd);
        asignarId();
        mostrarAdapter();
        if (currentUser != null) {
            cargarDatosBd();
        }

        rLauncherHistorial = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                       cargarDatosBd();
                    }
                }
        );
    }

    private void mostrarAdapter() {
        rvHistorial.setLayoutManager(new GridLayoutManager(this, 1));
        historialAdapter = new HistorialAdapter();
        rvHistorial.setAdapter(historialAdapter);
        historialAdapter.setmListener(this);
    }

    private void cargarDatosBd() {
        if (currentUser != null) {
            historialBd = new ArrayList<>();
            email = currentUser.getEmail(); // Utiliza el email como ID único del usuario
            // Obtén la colección "personas" en Firestore
            historialRef = db.collection("historial");
            // Realiza la consulta para obtener todas las personas del usuario actual
            consulta = historialRef.whereEqualTo("usuario", email);
            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Recorrer los documentos obtenidos y agregar los datos al ArrayList
                    for (DocumentSnapshot document : task.getResult()) {
                        historial = document.getString("historial");
                        fecha = document.getString("fecha");
                        restaurante = document.getString("restaurante");

                        hist = new Historial(historial, fecha, restaurante);
                        historialBd.add(hist);
                    }
                    // Notificar al adapter que los datos han cambiado
                    historialAdapter.setResultsHistorial(historialBd);
                }
            });
        }
    }

    private void asignarId() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rvHistorial = findViewById(R.id.rvHistorial);
    }

    @Override
    public void onItemClick(int position) {
        intent = new Intent(this, DetalleHistorialBd.class);
        intent.putExtra("detalleHistorial",historialBd.get(position));
        rLauncherHistorial.launch(intent);
    }
}