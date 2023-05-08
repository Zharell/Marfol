package login;
import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;

import java.util.HashMap;

import firebaseData.EditarDatos;
import mainActivity.IndexActivity;

enum ProviderType{
    BASIC,
    GOOGLE
}
public class HomeActivity extends AppCompatActivity {
    private Button btnLogoutHome, btnEditarBD;
    private TextView tvEmailHome;
    private TextView tvProviderHome;
    private TextView tvNombreUsuario, tvTelefonoUsuario;
    private FirebaseFirestore db;
    private HashMap<String, Object> map = new HashMap<>();
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        btnLogoutHome = findViewById(R.id.btnLogoutHome);
        btnEditarBD = findViewById(R.id.btnEditarBD);
        tvEmailHome = findViewById(R.id.tvEmailHome);
        tvProviderHome = findViewById(R.id.tvProviderHome);
        tvNombreUsuario = findViewById(R.id.tvNombreApellido);
        tvTelefonoUsuario = findViewById(R.id.tvTelefono);
        Bundle extras = getIntent().getExtras();
        String email = extras.getString("EMAIL");
        String provider = extras.getString("PROVIDER");
        //setup
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        setup(email, provider);

        // Guardado de datos
        SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefs = prefAux.edit();
        prefs.putString("email", email);
        prefs.putString("provider", provider);
        prefs.apply();


    }

    private void setup(String email, String provider) {
        tvEmailHome.setText(email);
        tvProviderHome.setText(provider);
        DocumentReference id = db.collection("users").document(email);
        progressBar.setVisibility(View.VISIBLE);

        id.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String phone = documentSnapshot.getString("phone");

                    // Si no hay datos de nombre o teléfono, ocultar la progress bar
                    if (name == null || phone == null) {
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    // Actualizar los EditText con los datos recuperados
                    tvNombreUsuario.setText("Nombre: "+name);
                    tvTelefonoUsuario.setText("Teléfono: "+phone);

                    progressBar.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "No se encontró el documento");
                    progressBar.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });


        btnLogoutHome.setOnClickListener(v -> {
            //Borrado de datos
            SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor prefs = prefAux.edit();
            prefs.clear();
            prefs.apply();
            FirebaseAuth.getInstance()
                    .signOut();
            Intent in = new Intent(this, IndexActivity.class);
            startActivity(in);
            finish();
        });
        btnEditarBD.setOnClickListener(v -> {
            Intent editar = new Intent(HomeActivity.this, EditarDatos.class);
            startActivity(editar);
        });

    }
}


