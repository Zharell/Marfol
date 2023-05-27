package login;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;
import java.util.HashMap;
import java.util.Map;
public class AuthActivity extends AppCompatActivity {
    private Button btnRegistrarseLogin;
    private Button btnEntrarLogin;
    private EditText etEmailLogin;
    private int susCont=0;
    private ImageView imagenLogoAuth;
    private EditText etPasswordLogin;
    private TextView tvContrasenaOlvidada;
    private Intent intent, recover;
    private FirebaseAuth mAuth;
    private String password,email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        // Variables
        asignarId();

        // Setup
        setup();

        //Método que utiliza el verySusyItem
        imagenLogoAuth.setOnClickListener(view -> { utilizarSusy(); });

    }

    public void utilizarSusy() {
        susCont++;
        if (susCont==8) {
            Toast.makeText(this,"tututu tu... tururú",Toast.LENGTH_SHORT).show();
        } else {
            if (susCont==16) {
                Toast.makeText(this,"mMmMm, very SUS touch",Toast.LENGTH_SHORT).show();
            } else {
                if (susCont==30) {
                    imagenLogoAuth.setBackground(null);
                    imagenLogoAuth.setImageDrawable(getDrawable(R.drawable.verysusyitem));
                }
            }
        }
    }

    private void asignarId() {
        // Asignar IDs de las vistas
        btnRegistrarseLogin = findViewById(R.id.btnRegistrarseLogin);
        btnEntrarLogin = findViewById(R.id.btnEntrarLogin);
        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        imagenLogoAuth = findViewById(R.id.imagenLogoAuth);
        tvContrasenaOlvidada = findViewById(R.id.tvContrasenaOlvidada);
        mAuth = FirebaseAuth.getInstance();
    }
    // Este método configura los listeners y acciones de los botones
    private void setup() {
        // Registro
        btnRegistrarseLogin.setOnClickListener(v -> {
            intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
            finish();
        });
        // Iniciar sesión con correo y contraseña
        btnEntrarLogin.setOnClickListener(v -> {
            email = etEmailLogin.getText().toString();
            password = etPasswordLogin.getText().toString();
            if (!email.isEmpty() && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(it -> {
                    if (it.isSuccessful()) {
                        finish();
                    } else {
                        showAlert("Error", "Se produjo un error en la autenticación del usuario");
                    }
                });
            }
        });
        // Recuperar contraseña
        tvContrasenaOlvidada.setOnClickListener(v -> {
            recover = new Intent(this, RecoverActivity.class);
            startActivity(recover);
        });
    }
    //mensaje de error
    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
