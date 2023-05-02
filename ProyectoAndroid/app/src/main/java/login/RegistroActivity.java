package login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.tfg.marfol.R;

import mainActivity.IndexActivity;

public class RegistroActivity extends AppCompatActivity {

    Button btnRegistrarseRegistro;
    EditText etRegistroEmailLogin;
    EditText etRegistroPasswordLogin,etRegistroPasswordLogin2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        btnRegistrarseRegistro=findViewById(R.id.btnRegistrarseRegistro);
        etRegistroEmailLogin=findViewById(R.id.etRegistroEmailLogin);
        etRegistroPasswordLogin=findViewById(R.id.etRegistroPasswordLogin);
        etRegistroPasswordLogin2=findViewById(R.id.etRegistroPasswordLogin2);


        //Setup
        setup();
    }

    //Este metodo crea un usuario con el correo pasandole las cajas y conectándose con firebase
    private void setup() {
        String titulo = "Autentificación";
        //Registro
        btnRegistrarseRegistro.setOnClickListener(v -> {
            if (!(etRegistroEmailLogin.getText().toString().equals("") || etRegistroPasswordLogin.getText().toString().equals(""))) {
                if(!(etRegistroPasswordLogin.getText().toString().equals(etRegistroPasswordLogin2.getText().toString()))){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Los campos contraseña son distintos")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alerta = builder.create();
                    alerta.show();
                }
                    FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(etRegistroEmailLogin.getText().toString(),
                                    etRegistroPasswordLogin.getText().toString()).addOnCompleteListener(it -> {
                                if (it.isSuccessful()) {
                                    showHome(it.getResult().getUser().getEmail(), ProviderType.BASIC);
                                } else {
                                    showAlert();
                                }
                            });
                } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Los campos de correo electrónico y contraseña no pueden estar vacíos")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alerta = builder.create();
                alerta.show();
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
        finish();

    }

    //Método que al pulsar el botón de volver redirige a la pantalla Index
    // (Se debe añadir Launcher Result) Por si el usuario pulsa sin querer volver
    // No pierda los participantes añadidos
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}