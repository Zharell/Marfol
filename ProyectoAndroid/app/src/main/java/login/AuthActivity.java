package login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.firestore.admin.v1.Index;
import com.tfg.marfol.R;

import java.util.HashMap;
import java.util.Map;

import mainActivity.IndexActivity;

public class AuthActivity extends AppCompatActivity {
    private Button btnRegistrarseLogin;
    private ImageButton btnGoogleLogin;
    private Button btnEntrarLogin;
    private EditText etEmailLogin;
    private EditText etPasswordLogin;

    private TextView tvContrasenaOlvidada;
    private final int GOOGLE_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        //variables
        asignarId();


        //Setup
        setup();


    }

    private void asignarId() {
        btnRegistrarseLogin=findViewById(R.id.btnRegistrarseLogin);
        btnEntrarLogin=findViewById(R.id.btnEntrarLogin);
        etEmailLogin=findViewById(R.id.etEmailLogin);
        etPasswordLogin=findViewById(R.id.etPasswordLogin);
        btnGoogleLogin=findViewById(R.id.btnGoogleLogin);
        tvContrasenaOlvidada=findViewById(R.id.tvContrasenaOlvidada);
    }


    //Este metodo crea un usuario con el correo pasandole las cajas y conectándose con firebase
    private void setup() {
        //Registro
        btnRegistrarseLogin.setOnClickListener(v ->{
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
            finish();
        });
        //Este metodo se conecta con la BD firebase y comprueba si el usuario y la contraseña existen, si son correctos se loguea
        btnEntrarLogin.setOnClickListener(v ->{
            if (!(etEmailLogin.getText().toString().equals("")||etPasswordLogin.getText().toString().equals(""))){
                FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(etEmailLogin.getText().toString(),
                                etPasswordLogin.getText().toString()).addOnCompleteListener(it->{
                            if(it.isSuccessful()){

                                showIndex();
                            }else{
                                showAlert();
                            }
                        });
            }
        });
        btnGoogleLogin.setOnClickListener(v ->{
            GoogleSignInOptions googleConf = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleClient = GoogleSignIn.getClient(this,googleConf);
            googleClient.signOut();
            startActivityForResult(googleClient.getSignInIntent(),GOOGLE_SIGN_IN);

        });
        tvContrasenaOlvidada.setOnClickListener(v -> {
            Intent recover = new Intent(this, RecoverActivity.class);
            startActivity(recover);
        });

    }
    private void showAlert(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Se produjo un error en la autentificaciónd el usuario");
        builder.setPositiveButton("Aceptar",null);
        AlertDialog dialog= builder.create();
        dialog.show();
    }
    private void showIndex(){
        Intent homeIntent= new Intent(this, IndexActivity.class);

        startActivity(homeIntent);
        finish();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    final String email = account.getEmail();
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(it -> {
                        if (it.isSuccessful() && email != null) {
                            // Verificar si el documento ya existe en la colección "users"
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(email)
                                    .get()
                                    .addOnCompleteListener(documentTask -> {
                                        if (documentTask.isSuccessful()) {
                                            DocumentSnapshot document = documentTask.getResult();
                                            if (document.exists()) {
                                                // El documento ya existe, continuar con el inicio de sesión
                                                showIndex();
                                            } else {
                                                // El documento no existe, crear uno nuevo con datos vacíos
                                                Map<String, Object> datosPersona = new HashMap<>();
                                                datosPersona.put("email", email);
                                                datosPersona.put("name", "");
                                                datosPersona.put("phone", "");
                                                datosPersona.put("imagen", "");

                                                FirebaseFirestore.getInstance().collection("users")
                                                        .document(email)
                                                        .set(datosPersona)
                                                        .addOnSuccessListener(anadido -> {
                                                            showIndex();
                                                        })
                                                        .addOnFailureListener(error -> {
                                                            showAlert();
                                                        });
                                            }
                                        } else {
                                            showAlert();
                                        }
                                    });
                        } else {
                            showAlert();
                        }
                    }).addOnFailureListener(e -> {
                        showAlert();
                    });
                }
            } catch (ApiException e) {
                showAlert();
            }
        }
    }




}