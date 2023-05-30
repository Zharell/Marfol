package login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private String nombre, telefono, email, password1, password2,acuerdos;
    private AlertDialog alerta;
    private CheckBox cbAcuerdoUsuario;
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
            if (!cbAcuerdoUsuario.isChecked()) {
                // Mostrar mensaje de error
                showAlert("Debes aceptar los acuerdos de usuario");
                // Mostrar cuadro de diálogo con los acuerdos
                showAgreementDialog(cbAcuerdoUsuario);
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
        cbAcuerdoUsuario.setOnClickListener(vb ->{
            showAgreementDialog(cbAcuerdoUsuario);
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
        cbAcuerdoUsuario = findViewById(R.id.cbAcuerdoUsuario);
        db = FirebaseFirestore.getInstance();
        acuerdos = "Al registrarte, aceptas los siguientes términos y condiciones de uso:\n\n" +
                "Aceptación de los términos: Al utilizar esta aplicación, aceptas cumplir con los términos y condiciones establecidos en este acuerdo.\n\n" +
                "Privacidad y protección de datos: Reconoces y aceptas que esta aplicación recopila y almacena información personal, como tu nombre y dirección de correo electrónico, para fines de registro y autenticación. También aceptas que la aplicación puede utilizar cookies y tecnologías similares para mejorar tu experiencia de uso.\n\n" +
                "Responsabilidad del usuario: Eres responsable de mantener la confidencialidad de tus credenciales de inicio de sesión y de cualquier actividad que ocurra en tu cuenta. También aceptas ser responsable de cualquier contenido que publiques o compartas a través de la aplicación.\n\n" +
                "Uso adecuado de la aplicación: Te comprometes a utilizar la aplicación de manera adecuada y legal, sin infringir los derechos de terceros ni realizar actividades que puedan dañar la integridad de la aplicación o su funcionalidad.\n\n" +
                "Propiedad intelectual: Reconoces que todos los derechos de propiedad intelectual relacionados con la aplicación y su contenido (incluyendo imágenes, logotipos y nombres de platos) pertenecen al titular de la aplicación. No se te otorga ningún derecho o licencia sobre dicha propiedad intelectual.\n\n" +
                "Al marcar la casilla de aceptación, confirmas que has leído y comprendido los términos y condiciones establecidos anteriormente, y que estás de acuerdo en cumplir con ellos.";

    }
    private void showAgreementDialog(final CheckBox checkBox) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acuerdos de usuario");
        builder.setMessage(acuerdos);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            // Marcar el CheckBox cuando se hace clic en "Aceptar"
            checkBox.setChecked(true);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // Poner el CheckBox en false cuando se hace clic en "Cancelar"
            checkBox.setChecked(false);
        });

        builder.setOnCancelListener(dialog -> {
            // Poner el CheckBox en false cuando se cancela el cuadro de diálogo
            checkBox.setChecked(false);
        });


        builder.show();
    }
}




