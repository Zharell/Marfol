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
    private String nombre, telefono, email, password1, password2;
    private AlertDialog alerta;
    private final String REGEX = "^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$";

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

        // Registro
        btnRegistrarseRegistro.setOnClickListener(v -> {
            email = etRegistroEmailLogin.getText().toString();
            password1 = etRegistroPasswordLogin.getText().toString();
            password2 = etRegistroPasswordLogin2.getText().toString();
            if (email.isEmpty()) {
                // Validación de campo de correo electrónico vacío
                showAlert("El campo de correo electrónico está vacío");
                return;
            }

            if (!password1.equals(password2)) {
                // Validación de contraseñas distintas
                showAlert("Las contraseñas no coinciden");
                return;
            }

            if (password1.isEmpty()) {
                // Validación de campo de contraseña vacío
                showAlert("El campo de contraseña está vacío");
                return;
            }

            // Validación de contraseña con expresión regular
            if (!password1.matches(REGEX)) {
                showAlert("La contraseña debe tener al menos 8 caracteres, un número y un carácter especial");
                return;
            }

            // Resto del código para el registro exitoso
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(etRegistroEmailLogin.getText().toString(),
                            etRegistroPasswordLogin.getText().toString()).addOnCompleteListener(it -> {
                        if (it.isSuccessful()) {
                            email = etRegistroEmailLogin.getText().toString();
                            nombre = etNombreRegistro.getText().toString();
                            telefono = etTelefonoRegistro.getText().toString();
                            //mapeo de datos
                            Map<String, Object> datosPersona = new HashMap<>();
                            datosPersona.put("email", email);
                            datosPersona.put("name", nombre);
                            datosPersona.put("phone", telefono);
                            datosPersona.put("imagen", "");
                            db.collection("users").document(email).set(datosPersona)
                                    .addOnSuccessListener(anadido -> {
                                        finish();
                                    }).addOnFailureListener(error -> {
                                        showAlert("Error al guardar los datos del usuario");
                                    });
                        } else {
                            showAlert("El correo no coincide con el formato 'x'+'@'+'.'+'x'.");
                        }
                    });
        });
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, id) -> dialog.dismiss());
        alerta = builder.create();
        alerta.show();
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
}