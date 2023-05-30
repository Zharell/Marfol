package mainActivity.menu.crudBd;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.CrudPlatosAdapter;
import entities.Plato;
import mainActivity.menu.crudBd.detalle.DetalleEditarPlatosBd;

public class EditarPlatosBd extends AppCompatActivity implements CrudPlatosAdapter.onItemClickListener {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView rvCrudPlatos;
    private CrudPlatosAdapter crudPlatosAdapter;
    private ArrayList<Plato> platosBd;
    private String email, nombre, descripcion, imagen, restaurante;
    private double precio;
    private CollectionReference platosRef;
    private Query consulta;
    private Plato plato;
    private Intent intentDetalle;
    private ActivityResultLauncher rLauncherPlatos;
    private Handler handler;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_platos_bd);
        asignarId();
        mostrarAdapterPlatos();
        //Laucher Result recibe el ArrayList con los nuevos comensales y los inserta en el adapter
        rLauncherPlatos = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    cargarDatosBd();
                }
        );

    }

    private void asignarId() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rvCrudPlatos = findViewById(R.id.rvCrudPlatos);

    }

    private void mostrarAdapterPlatos() {
        rvCrudPlatos.setLayoutManager(new GridLayoutManager(this, 1));
        crudPlatosAdapter = new CrudPlatosAdapter();
        rvCrudPlatos.setAdapter(crudPlatosAdapter);
        crudPlatosAdapter.setmListener(this);
        if (currentUser != null) {
            cargarDatosBd();
        }
    }

    private void cargarDatosBd() {

        if (currentUser != null) {
            platosBd = new ArrayList<>();
            email = currentUser.getEmail(); // Utiliza el email como ID único del usuario
            // Obtén la colección "personas" en Firestore
            platosRef = db.collection("platos");
            // Realiza la consulta para obtener todas las personas del usuario actual
            consulta = platosRef.whereEqualTo("usuario", email);
            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Recorrer los documentos obtenidos y agregar los datos al ArrayList
                    for (DocumentSnapshot document : task.getResult()) {
                        nombre = document.getString("nombre");
                        descripcion = document.getString("descripcion");
                        imagen = document.getString("imagen");
                        restaurante = document.getString("restaurante");
                        precio = document.getDouble("precio");
                        plato = new Plato(nombre, descripcion, imagen, restaurante, precio);
                        platosBd.add(plato);
                    }
                    // Notificar al adapter que los datos han cambiado
                    crudPlatosAdapter.setResultsPlato(platosBd);
                }
            });
        }
    }

    @Override
    public void onItemClick(int position) {
        intentDetalle = new Intent(this, DetalleEditarPlatosBd.class);
        intentDetalle.putExtra("platoDetalle", platosBd.get(position));
        intentDetalle.putExtra("platosTotales", platosBd);
        rLauncherPlatos.launch(intentDetalle);
    }
}