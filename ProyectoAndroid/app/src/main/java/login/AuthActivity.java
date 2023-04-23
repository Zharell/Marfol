package login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tfg.marfol.R;

import mainActivity.IndexActivity;

public class AuthActivity extends AppCompatActivity {
    Button btnRegistrarseLogin;
    Button btnGoogleLogin;
    Button btnEntrarLogin;
    EditText etEmailLogin;
    EditText etPasswordLogin;
    private final int GOOGLE_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        //variables
        btnRegistrarseLogin=findViewById(R.id.btnRegistrarseLogin);
        btnEntrarLogin=findViewById(R.id.btnEntrarLogin);
        etEmailLogin=findViewById(R.id.etEmailLogin);
        etPasswordLogin=findViewById(R.id.etPasswordLogin);
        btnGoogleLogin=findViewById(R.id.btnGoogleLogin);
        //Setup
        setup();
        session();
    }

    private void session() {
        SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        String email = prefAux.getString("email",null);
        String provider = prefAux.getString("provider",null);
        if(email != null && provider != null){

            showHome(email,ProviderType.valueOf(provider));
        }
    }

    private void setup() {
        String titulo= "Autentificación";
        //Registro
        btnRegistrarseLogin.setOnClickListener(v ->{
            if (!(etEmailLogin.getText().equals("")&&etPasswordLogin.equals(""))){
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(etEmailLogin.getText().toString(),
                        etPasswordLogin.getText().toString()).addOnCompleteListener(it->{
                        if(it.isSuccessful()){
                            showHome(it.getResult().getUser().getEmail(),ProviderType.BASIC);
                        }else{
                            showAlert();
                        }
                        });
            }
        });
        btnEntrarLogin.setOnClickListener(v ->{
            if (!(etEmailLogin.getText().equals("")&&etPasswordLogin.equals(""))){
                FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(etEmailLogin.getText().toString(),
                                etPasswordLogin.getText().toString()).addOnCompleteListener(it->{
                            if(it.isSuccessful()){
                                showHome(it.getResult().getUser().getEmail(),ProviderType.BASIC);
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

    }
    private void showAlert(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Se produjo un error en la autentificaciónd el usuario");
        builder.setPositiveButton("Aceptar",null);
        AlertDialog dialog= builder.create();
        dialog.show();
    }
    private void showHome(String email, ProviderType provider ){
        Intent homeIntent= new Intent(this,HomeActivity.class);
        homeIntent.putExtra("EMAIL",email);
        homeIntent.putExtra("PROVIDER",provider.name());
        startActivity(homeIntent);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == GOOGLE_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account != null){
                    final String email=account.getEmail();
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(it->{
                        if (it.isSuccessful() && email != null) {
                            showHome(email, ProviderType.GOOGLE);
                        } else {
                            showAlert();
                        }



                    });

                }
            } catch (ApiException e) {
                showAlert();
            }


        }
    }

    //Método que al pulsar el botón de volver redirige a la pantalla Index
    // (Se debe añadir Launcher Result) Por si el usuario pulsa sin querer volver
    // No pierda los participantes añadidos
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, IndexActivity.class);
        startActivity(intent);
        finish();
    }

}