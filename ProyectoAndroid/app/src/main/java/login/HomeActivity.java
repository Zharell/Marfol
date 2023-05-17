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
    private Button btnEditarBD;
    private TextView tvEmailHome;
    private TextView tvNombreUsuario, tvTelefonoUsuario;
    private FirebaseFirestore db;
    private HashMap<String, Object> map = new HashMap<>();
    private ProgressBar progressBar;

    private ImageView ivFotoPersonaHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //asignarId
        asignarId();
        if(MetodosGlobales.comprobarLogueado(this,ivFotoPersonaHome)){

            MetodosGlobales.cargarDatosEnHomeSiLogueado(tvEmailHome,tvNombreUsuario,tvTelefonoUsuario);
            setup( );
        }else{

            finish();
        }



    }

    private void asignarId() {
        db = FirebaseFirestore.getInstance();
        btnEditarBD = findViewById(R.id.btnEditarBD);
        tvEmailHome = findViewById(R.id.tvEmailHome);
        tvNombreUsuario = findViewById(R.id.tvNombreApellido);
        tvTelefonoUsuario = findViewById(R.id.tvTelefono);
        ivFotoPersonaHome = findViewById(R.id.ivFotoPersonaHome);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void setup() {

        btnEditarBD.setOnClickListener(v -> {
            Intent editar = new Intent(HomeActivity.this, EditarDatos.class);
            startActivity(editar);
            finish();
        });

    }
}


