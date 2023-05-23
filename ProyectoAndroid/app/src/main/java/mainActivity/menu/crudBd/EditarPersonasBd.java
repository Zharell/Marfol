package mainActivity.menu.crudBd;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import adapters.CrudPersonaAdapter;
import adapters.PersonaAdapterBd;
import entities.Persona;
import mainActivity.menu.crudBd.detalle.DetalleEditarPersonaBd;

public class EditarPersonasBd extends AppCompatActivity implements CrudPersonaAdapter.onItemClickListenerCrudPersona {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView rvCrudPersonas;
    private ArrayList<Persona> comensalesBd;
    private CrudPersonaAdapter crudPersonaAdapter;
    private Intent intentDetalle;
    private ActivityResultLauncher rLauncherPersonas;
    private String usuarioId, nombre, descripcion, imagen;
    private CollectionReference personasRef;
    private Query consulta;
    private Persona persona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_personas_db);
        asignarId();
        rvCrudPersonas.setLayoutManager(new GridLayoutManager(this, 1));
        crudPersonaAdapter = new CrudPersonaAdapter();
        rvCrudPersonas.setAdapter(crudPersonaAdapter);
        crudPersonaAdapter.setmListener(this);
        if (currentUser != null) {
            cargarDatosBd();
        }
        //Laucher Result recibe el ArrayList con los nuevos comensales y los inserta en el adapter
        rLauncherPersonas = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        comensalesBd = (ArrayList<Persona>) data.getSerializableExtra("detalleComensal");
                        cargarDatosBd();
                        crudPersonaAdapter.setResultsCrudPersona(comensalesBd);
                    }
                }
        );

    }

    private void asignarId() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rvCrudPersonas = findViewById(R.id.rvCrudPersonas);

    }

    private void cargarDatosBd() {
        if (currentUser != null) {
            comensalesBd = new ArrayList<>();
            usuarioId = currentUser.getEmail(); // Utiliza el email como ID único del usuario
            // Obtén la colección "personas" en Firestore
            personasRef = db.collection("personas");
            // Realiza la consulta para obtener todas las personas del usuario actual
            consulta = personasRef.whereEqualTo("usuarioId", usuarioId);
            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Recorrer los documentos obtenidos y agregar los datos al ArrayList
                    for (DocumentSnapshot document : task.getResult()) {
                        nombre = document.getString("nombre");
                        descripcion = document.getString("descripcion");
                        imagen = document.getString("imagen");
                        persona = new Persona(nombre, descripcion, imagen);
                        comensalesBd.add(persona);
                    }
                    // Notificar al adapter que los datos han cambiado
                    crudPersonaAdapter.setResultsCrudPersona(comensalesBd);
                }
            });
        } else {
            // El usuario no está autenticado, muestra un mensaje o inicia sesión automáticamente
            Toast.makeText(this, "Inicia sesión para cargar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClickCrudPersona(int position) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
        intentDetalle = new Intent(this, DetalleEditarPersonaBd.class);
        intentDetalle.putExtra("comensalDetalle", comensalesBd.get(position));
        rLauncherPersonas.launch(intentDetalle);
    }
}