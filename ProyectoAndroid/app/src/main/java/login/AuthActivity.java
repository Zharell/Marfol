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
    private ImageButton btnGoogleLogin;
    private Button btnEntrarLogin;
    private EditText etEmailLogin;
    private int susCont=0;
    private ImageView imagenLogoAuth;
    private EditText etPasswordLogin;
    private TextView tvContrasenaOlvidada;
    private final int GOOGLE_SIGN_IN = 100;
    private Intent intent, recover;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AuthCredential credential;
    private GoogleSignInOptions googleConf;
    private GoogleSignInClient googleClient;
    private GoogleSignInAccount account;
    private DocumentSnapshot document;
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
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        imagenLogoAuth = findViewById(R.id.imagenLogoAuth);
        tvContrasenaOlvidada = findViewById(R.id.tvContrasenaOlvidada);
        db = FirebaseFirestore.getInstance();
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
            String email = etEmailLogin.getText().toString();
            String password = etPasswordLogin.getText().toString();
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
        // Iniciar sesión con Google
        btnGoogleLogin.setOnClickListener(v -> {
            googleConf = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleClient = GoogleSignIn.getClient(this, googleConf);
            googleClient.signOut();
            startActivityForResult(googleClient.getSignInIntent(), GOOGLE_SIGN_IN);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                account = task.getResult(ApiException.class);
                if (account != null) {
                    final String email = account.getEmail();
                    credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(it -> {
                        if (it.isSuccessful() && email != null) {
                            // Verificar si el documento ya existe en la colección "users"
                            db.collection("users").document(email).get().addOnCompleteListener(documentTask -> {
                                if (documentTask.isSuccessful()) {
                                    document = documentTask.getResult();
                                    if (document.exists()) {
                                        // El documento ya existe, continuar con el inicio de sesión
                                        finish();
                                    } else {
                                        // El documento no existe, crear uno nuevo con datos vacíos
                                        Map<String, Object> datosPersona = new HashMap<>();
                                        datosPersona.put("email", email);
                                        datosPersona.put("name", "");
                                        datosPersona.put("phone", "");
                                        datosPersona.put("imagen", "");
                                        db.collection("users").document(email).set(datosPersona).addOnSuccessListener(anadido -> {
                                            finish();
                                        }).addOnFailureListener(error -> {
                                            showAlert("Error", "Se produjo un error al crear el documento del usuario");
                                        });
                                    }
                                } else {
                                    showAlert("Error", "Se produjo un error al obtener los datos del usuario");
                                }
                            });
                        } else {
                            showAlert("Error", "Se produjo un error en la autenticación del usuario");
                        }
                    }).addOnFailureListener(e -> {
                        showAlert("Error", "Se produjo un error en la autenticación del usuario");
                    });
                }
            } catch (ApiException e) {
                showAlert("Error", "Se produjo un error en la autenticación del usuario");
            }
        }
    }
}
