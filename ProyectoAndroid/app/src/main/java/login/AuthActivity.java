package login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.tfg.marfol.R;
public class AuthActivity extends AppCompatActivity {
    Button btnRegistrarseLogin;
    Button btnEntrarLogin;
    EditText etEmailLogin;
    EditText etPasswordLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        btnRegistrarseLogin=findViewById(R.id.btnRegistrarseLogin);
        btnEntrarLogin=findViewById(R.id.btnEntrarLogin);
        etEmailLogin=findViewById(R.id.etEmailLogin);
        etPasswordLogin=findViewById(R.id.etPasswordLogin);
        setup();
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
}