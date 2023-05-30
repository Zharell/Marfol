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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.CrudRestaurantesAdapter;
import entities.Restaurantes;
import mainActivity.menu.crudBd.detalle.DetalleEditarRestaurantesBd;

public class EditarRestaurantesBd extends AppCompatActivity implements CrudRestaurantesAdapter.onItemClickListenerRestaurantes {
    private RecyclerView rvCrudRestaurantes;
    private CrudRestaurantesAdapter crudRestaurantesAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ArrayList<Restaurantes> restaurantesBd;
    private Intent intent;
    private String email, nombreRestaurante;
    private CollectionReference restaurantesRef;
    private Query consulta;
    private Restaurantes restaurante;
    private ActivityResultLauncher rLauncherRestaurantes;
    private Handler handler;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_restaurantes_bd);
        asignarId();
        mostrarAdapterRestaurantes();
        //Laucher Result recibe el ArrayList con los restaurantes actualizados y los inserta en el adapter
        rLauncherRestaurantes = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    cargarRestaurantesBd();
                }
        );

    }

    private void asignarId() {
        rvCrudRestaurantes = findViewById(R.id.rvCrudRestaurantes);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void mostrarAdapterRestaurantes() {
        rvCrudRestaurantes.setLayoutManager(new GridLayoutManager(this, 1));
        crudRestaurantesAdapter = new CrudRestaurantesAdapter();
        rvCrudRestaurantes.setAdapter(crudRestaurantesAdapter);
        crudRestaurantesAdapter.setmListener(this);
        if (currentUser != null) {
            cargarRestaurantesBd();
        }
    }

    private void cargarRestaurantesBd() {
        if (currentUser != null) {  // Verifica si hay un usuario actualmente logueado
            restaurantesBd = new ArrayList<>();  // Crea una nueva lista para almacenar los restaurantes
            email = currentUser.getEmail();  // Obtiene el correo electrónico del usuario actual

            // Configura la referencia a la colección "restaurantes" en la base de datos
            restaurantesRef = db.collection("restaurantes");

            // Realiza una consulta a la colección "restaurantes" donde el campo "usuarioId" sea igual al correo electrónico del usuario actual
            consulta = restaurantesRef.whereEqualTo("usuarioId", email);

            // Ejecuta la consulta y agrega un listener para recibir el resultado
            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {  // Verifica si la consulta se completó con éxito
                    for (DocumentSnapshot document : task.getResult()) {
                        // Recorre los documentos resultantes de la consulta
                        nombreRestaurante = document.getString("nombreRestaurante");  // Obtiene el nombre del restaurante del documento
                        restaurante = new Restaurantes(nombreRestaurante, "");  // Crea un nuevo objeto Restaurantes con el nombre obtenido
                        restaurantesBd.add(restaurante);  // Agrega el objeto a la lista de restaurantes
                    }
                    crudRestaurantesAdapter.setResultsRestaurantes(restaurantesBd);  // Actualiza el adaptador con los resultados obtenidos
                }
            });
        }
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
        intent = new Intent(this, DetalleEditarRestaurantesBd.class);
        intent.putExtra("restauranteDetalle", restaurantesBd.get(position));
        intent.putExtra("restaurantesTotales", restaurantesBd);
        rLauncherRestaurantes.launch(intent);
    }
}