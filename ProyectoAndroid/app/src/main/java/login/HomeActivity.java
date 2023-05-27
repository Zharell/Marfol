package login;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;
import mainActivity.MetodosGlobales;
public class HomeActivity extends AppCompatActivity {
    private Button btnEditarBD;
    private TextView tvEmailHome;
    private TextView tvNombreUsuario, tvTelefonoUsuario;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DocumentReference userRef;
    private DocumentSnapshot document;
    private ProgressBar progressBar;
    private ImageView ivFotoPersonaHome;
    private String emailId,userEmail,userNombre,userTelefono;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Asignar ID
        asignarId();
        // Verificar si el usuario está logueado
        if (MetodosGlobales.comprobarLogueado(this, ivFotoPersonaHome)) {
            cargarDatosEnHomeSiLogueado(tvEmailHome, tvNombreUsuario, tvTelefonoUsuario);
            setup();

        } else {
            finish();
        }
    }
    private void asignarId() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        btnEditarBD = findViewById(R.id.btnEditarBD);
        tvEmailHome = findViewById(R.id.tvEmailHome);
        tvNombreUsuario = findViewById(R.id.tvNombreApellido);
        tvTelefonoUsuario = findViewById(R.id.tvTelefono);
        ivFotoPersonaHome = findViewById(R.id.ivFotoPersonaHome);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }
    private void setup() {
        btnEditarBD.setOnClickListener(v -> {
            Intent editar = new Intent(HomeActivity.this, EditarDatos.class);
            startActivity(editar);
            finish();
        });
    }
    private void cargarDatosEnHomeSiLogueado(TextView email, TextView nombre, TextView telefono) {
        if (mAuth.getCurrentUser() != null) {
            // Obtener el usuario actualmente logueado
            currentUser = mAuth.getCurrentUser();
            // Obtener el correo electrónico del usuario
            emailId = currentUser.getEmail();
            // Obtener la referencia al documento del usuario en la colección "users"
            userRef = db.collection("users").document(emailId);
            // Obtener los datos del documento del usuario
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Obtener el documento
                    document = task.getResult();
                    if (document.exists()) {
                        // Obtener los valores de los campos del documento
                        userEmail = document.getString("email");
                        userNombre = document.getString("name");
                        userTelefono = document.getString("phone");
                        // Establecer los valores en los TextView correspondientes
                        if (userEmail != null) {
                            email.setText(userEmail);
                        }
                        if (userNombre != null) {
                            nombre.setText(userNombre);
                        }
                        if (userTelefono != null) {
                            telefono.setText(userTelefono);
                        }
                    }
                }
            });
        }
    }
}


