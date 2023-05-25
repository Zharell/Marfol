package mainActivity.menu.crudBd.detalle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;

import entities.Restaurantes;

public class DetalleEditarRestaurantesBd extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String email,nombreNuevo,nombreAntiguo;
    private Button btnBorrarDetalleRestaurantes,btnEditarDetalleRestaurantes;
    private EditText etDetalleRestaurantes;
    private Restaurantes restaurantesBd;
    private Intent intent;
    private CollectionReference restaurantesRef;
    private DocumentSnapshot document;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_editar_restaurantes_bd);
        asignarId();
        intent = getIntent();
        restaurantesBd = (Restaurantes) intent.getSerializableExtra("restauranteDetalle");
        mostrarDatos();
        btnEditarDetalleRestaurantes.setOnClickListener(view -> {
            editarRestaurante();
            finish();
        });
        btnBorrarDetalleRestaurantes.setOnClickListener(view -> {
            borrarRestaurante();
            finish();
        });
    }
    private void editarRestaurante() {
        nombreAntiguo = restaurantesBd.getNombreRestaurante();
        nombreNuevo = etDetalleRestaurantes.getText().toString();
        if (nombreNuevo.isEmpty()) {
            // El campo de texto está vacío, mostrar un mensaje de error
            Toast.makeText(DetalleEditarRestaurantesBd.this, "Debes ingresar un nombre para el restaurante", Toast.LENGTH_SHORT).show();
            return;
        }

        // Realizar la actualización sin verificar si existe previamente en la base de datos
        restaurantesRef = db.collection("restaurantes");
        restaurantesRef.whereEqualTo("usuarioId", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    if (document != null) {
                        document.getReference().update("nombreRestaurante", nombreNuevo)
                                .addOnSuccessListener(aVoid -> {
                                    // La actualización se realizó exitosamente
                                    Toast.makeText(DetalleEditarRestaurantesBd.this, "Los datos se actualizaron correctamente", Toast.LENGTH_SHORT).show();
                                    // Actualizar las referencias en la colección "Platos"
                                    actualizarReferencias(nombreAntiguo, nombreNuevo);
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error al actualizar los datos
                                    Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // El documento es nulo, mostrar un mensaje de error
                        Toast.makeText(DetalleEditarRestaurantesBd.this, "No se encontró el restaurante en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al buscar el restaurante en la base de datos
                    Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al buscar el restaurante en la base de datos", Toast.LENGTH_SHORT).show();
                });
    }



    private void actualizarReferencias(String nombreAntiguo, String nombreNuevo) {
        // Obtener la referencia a la colección "Platos"
        CollectionReference platosRef = db.collection("platos");

        // Realizar una consulta para obtener los platos con el nombre antiguo
        platosRef.whereEqualTo("restaurante", nombreAntiguo)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Actualizar cada documento que coincida con el nombre antiguo
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            document.getReference().update("restaurante", nombreNuevo)
                                    .addOnSuccessListener(aVoid -> {
                                        // La actualización se realizó exitosamente para el documento actual
                                        // Puedes realizar alguna acción adicional si es necesario
                                    })
                                    .addOnFailureListener(e -> {
                                        // Ocurrió un error al actualizar el documento
                                        // Puedes manejar el error según tus necesidades
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al realizar la consulta
                    // Puedes manejar el error según tus necesidades
                });
    }

    private void borrarRestaurante() {
        // Eliminar el comensal de la base de datos
        restaurantesRef = db.collection("restaurantes");

        restaurantesRef.whereEqualTo("nombreRestaurante", restaurantesBd.getNombreRestaurante())
                .whereEqualTo("usuarioId", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        document = querySnapshot.getDocuments().get(0);
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // La eliminación se realizó exitosamente
                                    Toast.makeText(DetalleEditarRestaurantesBd.this, "El restaurante se eliminó correctamente", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error al eliminar el comensal
                                    Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al eliminar el comensal", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // No se encontró el comensal en la base de datos
                        Toast.makeText(DetalleEditarRestaurantesBd.this, "El comensal no existe en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al buscar el comensal en la base de datos
                    Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al buscar el comensal en la base de datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDatos() {
        etDetalleRestaurantes.setText(restaurantesBd.getNombreRestaurante());
    }

    private void asignarId() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        email = currentUser.getEmail();
        btnBorrarDetalleRestaurantes = findViewById(R.id.btnBorrarDetalleRestaurantes);
        btnEditarDetalleRestaurantes = findViewById(R.id.btnEditarDetalleRestaurantes);
        etDetalleRestaurantes = findViewById(R.id.etDetalleRestaurantes);
    }
}