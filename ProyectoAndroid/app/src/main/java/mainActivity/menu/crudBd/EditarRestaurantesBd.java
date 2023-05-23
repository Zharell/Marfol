package mainActivity.menu.crudBd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
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

public class EditarRestaurantesBd extends AppCompatActivity implements CrudRestaurantesAdapter.onItemClickListenerRestaurantes{
    private RecyclerView rvCrudRestaurantes;
    private CrudRestaurantesAdapter crudRestaurantesAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ArrayList<Restaurantes> restaurantesBd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_restaurantes_bd);
        asignarId();
        mostrarAdapterRestaurantes();
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
            String usuarioId = currentUser.getEmail();
            DocumentReference id = db.collection("users").document(usuarioId);

            CollectionReference restaurantesRef = db.collection("restaurantes");

            Query consulta = restaurantesRef.whereEqualTo("usuarioId", usuarioId);

            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        String nombreRestaurante = document.getString("nombreRestaurante");
                        Restaurantes restaurante = new Restaurantes(nombreRestaurante, "");
                        restaurantesBd.add(restaurante);
                    }
                    crudRestaurantesAdapter.setResultsRestaurantes(restaurantesBd);
                }

            });

        }
    }

    @Override
    public void onItemClick(int position) {

    }
}