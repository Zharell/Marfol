package mainActivity.menu.crudBd;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tfg.marfol.R;
import java.util.ArrayList;
import adapters.CrudRestaurantesAdapter;
import adapters.RestaurantesAdapter;
import entities.Restaurantes;
import mainActivity.menu.crudBd.detalle.DetalleEditarPersonaBd;
import mainActivity.menu.crudBd.detalle.DetalleEditarRestaurantesBd;

public class EditarRestaurantesBd extends AppCompatActivity implements CrudRestaurantesAdapter.onItemClickListenerRestaurantes{
    private RecyclerView rvCrudRestaurantes;
    private CrudRestaurantesAdapter crudRestaurantesAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ArrayList<Restaurantes> restaurantesBd;
    private Intent intent;
    private String usuarioId,nombreRestaurante;
    private CollectionReference restaurantesRef;
    private Query consulta;
    private Restaurantes restaurante;
    private ActivityResultLauncher rLauncherRestaurantes;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_restaurantes_bd);
        asignarId();
        mostrarAdapterRestaurantes();
        //Laucher Result recibe el ArrayList con los restaurantes actualizados y los inserta en el adapter
        rLauncherRestaurantes = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    //si no hago que se espere un poco, va mas rapida la petición que la actualización
                    handler = new Handler();
                    handler.postDelayed(() -> cargarRestaurantesBd(), 2000);


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
        if (currentUser != null) {
            restaurantesBd = new ArrayList<>();
            usuarioId = currentUser.getEmail();
            restaurantesRef = db.collection("restaurantes");
            consulta = restaurantesRef.whereEqualTo("usuarioId", usuarioId);
            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        nombreRestaurante = document.getString("nombreRestaurante");
                        restaurante = new Restaurantes(nombreRestaurante, "");
                        restaurantesBd.add(restaurante);
                    }
                    crudRestaurantesAdapter.setResultsRestaurantes(restaurantesBd);
                }

            });

        }
    }
    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
        intent = new Intent(this, DetalleEditarRestaurantesBd.class);
        intent.putExtra("restauranteDetalle", restaurantesBd.get(position));
        rLauncherRestaurantes.launch(intent);
    }
}