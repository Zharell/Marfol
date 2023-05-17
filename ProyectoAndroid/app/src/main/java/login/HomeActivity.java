package login;
import static android.content.ContentValues.TAG;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;
import java.util.HashMap;
import mainActivity.IndexActivity;
import mainActivity.MetodosGlobales;

enum ProviderType{
    BASIC,
    GOOGLE
}
public class HomeActivity extends AppCompatActivity {
    private Button btnLogoutHome, btnEditarBD;
    private TextView tvEmailHome;
    private TextView tvNombreUsuario, tvTelefonoUsuario;
    private FirebaseFirestore db;
    private HashMap<String, Object> map = new HashMap<>();
    private ProgressBar progressBar;
    private ActivityResultLauncher rLauncherHome;
    private String nombreEnviar,telefonoEnviar;
    private ImageView ivFotoPersonaHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //asignarId
        asignarId();
        if(MetodosGlobales.comprobarLogueado(this,ivFotoPersonaHome)){
            MetodosGlobales.cargarDatosEnHomeSiLogueado(tvEmailHome,tvNombreUsuario,tvTelefonoUsuario);
        }else{

        }

        setup( );

        // Guardado de datos
        rLauncherHome = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Aquí puedes obtener los datos de vuelta de la actividad de edición
                        Intent data = result.getData();
                        if (data != null) {
                            // Obtén los datos enviados desde la actividad de edición
                            String name = data.getStringExtra("name");
                            String phone = data.getStringExtra("phone");
                            String img = data.getStringExtra("img");
                            // Por ejemplo, actualiza los TextView en esta actividad
                            tvNombreUsuario.setText(name);
                            tvTelefonoUsuario.setText(phone);
                            Glide.with(HomeActivity.this).load(img).circleCrop().into(ivFotoPersonaHome);
                        }
                    }
                }
        );


    }

    private void asignarId() {
        db = FirebaseFirestore.getInstance();
        btnLogoutHome = findViewById(R.id.btnLogoutHome);
        btnEditarBD = findViewById(R.id.btnEditarBD);
        tvEmailHome = findViewById(R.id.tvEmailHome);
        tvNombreUsuario = findViewById(R.id.tvNombreApellido);
        tvTelefonoUsuario = findViewById(R.id.tvTelefono);
        ivFotoPersonaHome = findViewById(R.id.ivFotoPersonaHome);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void setup() {



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
            //aqui parseo los datos para quitar Nombre: y Teléfono: y quedarme solo con el valor de dentro
            nombreEnviar =  tvNombreUsuario.getText().toString();
            telefonoEnviar = tvTelefonoUsuario.getText().toString();
            editar.putExtra("name", nombreEnviar);
            editar.putExtra("phone", telefonoEnviar);
            setResult(RESULT_OK, editar);
            rLauncherHome.launch(editar);
        });

    }
}


