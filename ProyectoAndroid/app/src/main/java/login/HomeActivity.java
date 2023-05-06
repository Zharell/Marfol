package login;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;

import java.util.HashMap;

import mainActivity.IndexActivity;

enum ProviderType{
    BASIC,
    GOOGLE
}
public class HomeActivity extends AppCompatActivity {
    Button btnLogoutHome,btnGuardarBD,btnBorrarBD,btnRecuperarBD;
    TextView etEmailHome;
    TextView etProviderHome;
    EditText etNombreUsuario,etTelefonoUsuario;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseFirestore db ;
    private HashMap<String, Object> map = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db= FirebaseFirestore.getInstance();
        btnLogoutHome=findViewById(R.id.btnLogoutHome);
        btnGuardarBD=findViewById(R.id.btnGuardarBD);
        btnBorrarBD=findViewById(R.id.btnBorrarBD);
        btnRecuperarBD=findViewById(R.id.btnRecuperarBD);
        etEmailHome=findViewById(R.id.etEmailHome);
        etProviderHome=findViewById(R.id.etProviderHome);
        etNombreUsuario=findViewById(R.id.etNombreUsuario);
        etTelefonoUsuario=findViewById(R.id.etTelefonoUsuario);
        Bundle extras= getIntent().getExtras();
        String email= extras.getString("EMAIL");
        String provider=extras.getString("PROVIDER");
        //setup
        setup(email,provider);

        // Guardado de datos
        SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefs = prefAux.edit();
        prefs.putString("email",email);
        prefs.putString("provider",provider);
        prefs.apply();




    }

    private void setup(String email, String provider) {
        etEmailHome.setText(email);
        etProviderHome.setText(provider);
        String idUser=mAuth.getCurrentUser().getUid();
        DocumentReference id = db.collection("users").document();
        map.put("id_user",idUser);
        map.put("id",id.getId());
        map.put("nombre",etNombreUsuario.getText().toString());
        map.put("telefono",etTelefonoUsuario.getText().toString());

        btnLogoutHome.setOnClickListener(v -> {
            //Borrado de datos
            SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor prefs = prefAux.edit();
            prefs.clear();
            prefs.apply();
            FirebaseAuth.getInstance()
                    .signOut();
            Intent in =new Intent(this, IndexActivity.class);
            startActivity(in);
            finish();
        });
        btnGuardarBD.setOnClickListener(v -> {
            //crear coleccion email es el id
            db.collection("users").document(email).set(
                            new HashMap<String, Object>() {{
                                put("provider", provider);
                                put("name", etNombreUsuario.getText().toString());
                                put("phone", etTelefonoUsuario.getText().toString());
                            }})
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(HomeActivity.this, "Creado exitosamente", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this, "Error al ingresar", Toast.LENGTH_SHORT).show();
                        }
                    });
        });




        btnBorrarBD.setOnClickListener(v -> {
            db.collection("users").document(email).collection("personas").add(new HashMap<String, Object>() {{
                put("name", "Persona 1");
                put("age", 30);
            }});
        });
        btnRecuperarBD.setOnClickListener(v -> {});
        }
    }


