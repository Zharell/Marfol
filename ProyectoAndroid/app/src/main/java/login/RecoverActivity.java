package login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.tfg.marfol.R;

public class RecoverActivity extends AppCompatActivity {
    EditText etContrasenaOlvidadaRecover;
    Button btnEnviarRecover;
    private FirebaseAuth mAuth;
    private String email="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);
        mAuth=FirebaseAuth.getInstance();
        etContrasenaOlvidadaRecover=findViewById(R.id.etContrasenaOlvidadaRecover);
        btnEnviarRecover=findViewById(R.id.btnEnviarRecover);
        btnEnviarRecover.setOnClickListener(v->{
            email= etContrasenaOlvidadaRecover.getText().toString();
            if(!email.isEmpty()){
                resetPassword();
                finish();
            }else {
                Toast.makeText(this, "Debe ingresar email",Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void resetPassword() {
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(l->{
            if(l.isSuccessful()){
                Toast.makeText(this, "Se ha enviado un correo para restablecer la contraseña",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "No se pudo enviar el email",Toast.LENGTH_SHORT).show();
            }
        });
    }
}