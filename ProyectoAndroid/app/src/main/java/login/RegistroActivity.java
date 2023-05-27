package login;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;
import java.util.HashMap;
import java.util.Map;
public class RegistroActivity extends AppCompatActivity {
    private Button btnRegistrarseRegistro;
    private EditText etRegistroEmailLogin;
    private EditText etTelefonoRegistro;
    private EditText etNombreRegistro;
    private EditText etRegistroPasswordLogin;
    private EditText etRegistroPasswordLogin2;
    private FirebaseFirestore db;
    private String nombre, telefono,email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        //Método que asigna los ids a todas las variables
        asignarId();
        //Setup
        setup();
    }
    //Este metodo crea un usuario con el correo pasandole las cajas y conectándose con firebase
    private void setup() {
        //Registro
        btnRegistrarseRegistro.setOnClickListener(v -> {
            if (!(etRegistroEmailLogin.getText().toString().equals("") || etRegistroPasswordLogin.getText().toString().equals(""))) {
                if (!(etRegistroPasswordLogin.getText().toString().equalsIgnoreCase(etRegistroPasswordLogin2.getText().toString()))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Los campos contraseña son distintos")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", (dialog, id) -> dialog.dismiss());

                    AlertDialog alerta = builder.create();
                    alerta.show();
                    return;
                }
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(etRegistroEmailLogin.getText().toString(),
                                etRegistroPasswordLogin.getText().toString()).addOnCompleteListener(it -> {
                            if (it.isSuccessful()) {
                                email = etRegistroEmailLogin.getText().toString();
                                nombre = etNombreRegistro.getText().toString();
                                telefono = etTelefonoRegistro.getText().toString();
                                //mapeo de datos
                                Map<String, Object> datosPersona = new HashMap<>();
                                datosPersona.put("email",email);
                                datosPersona.put("name", nombre);
                                datosPersona.put("phone", telefono);
                                datosPersona.put("imagen","");
                                db.collection("users").document(email).set(datosPersona)
                                        .addOnSuccessListener(anadido -> {
                                            finish();
                                        }).addOnFailureListener(error -> {
                                            showAlert();
                                        });
                            } else {
                                showAlert();
                            }
                        });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Los campos de correo electrónico y contraseña no pueden estar vacíos")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", (dialog, id) -> dialog.dismiss());
                AlertDialog alerta = builder.create();
                alerta.show();
            }
        });
    }
    private void asignarId() {
        btnRegistrarseRegistro = findViewById(R.id.btnRegistrarseRegistro);
        etRegistroEmailLogin = findViewById(R.id.etRegistroEmailLogin);
        etRegistroPasswordLogin = findViewById(R.id.etRegistroPasswordLogin);
        etRegistroPasswordLogin2 = findViewById(R.id.etRegistroPasswordLogin2);
        etTelefonoRegistro = findViewById(R.id.etTelefonoRegistro);
        etNombreRegistro = findViewById(R.id.etNombreRegistro);
        db = FirebaseFirestore.getInstance();
    }
    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Se produjo un error en la autentificaciónd el usuario");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}