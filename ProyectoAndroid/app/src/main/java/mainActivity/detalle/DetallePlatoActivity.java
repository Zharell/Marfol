package mainActivity.detalle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.PersonaDetalleAdapter;
import entities.Persona;
import entities.Plato;

public class DetallePlatoActivity extends AppCompatActivity {

    private ImageView ivFotoDetalle;
    private EditText etTitleDetalle, etDescripcionDetalle;
    private RecyclerView rvAnadirPlatoDetalle;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private ActivityResultLauncher rLauncherPlatos;
    private Button btnContinuarDetalle, btnBorrarDetalle;
    private String uriCapturada = "";
    private Plato plato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_plato);

        //Recibe la lista de comensales para empezar a añadir
        Intent intent = getIntent();
        plato = (Plato) intent.getSerializableExtra("platoDetalle");

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Método que inserta la información a los comensales
        insertarComensal();

    }

    public void insertarComensal() {
        etTitleDetalle.setText(plato.getNombre());
        etDescripcionDetalle.setText(plato.getDescripcion());
        uriCapturada  = plato.getUrlImage();
        if (!plato.getUrlImage().equalsIgnoreCase("")) {
            ivFotoDetalle.setImageURI(Uri.parse(plato.getUrlImage()));
        } else {
            //Inserta Imagen photo
            ivFotoDetalle.setImageURI(Uri.parse("android.resource://com.tfg.marfol/"+R.drawable.camera));
            ivFotoDetalle.setPadding(30, 30, 30, 30);
        }
    }

    public void asignarEfectos() {

        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };

        float[] positions = {0f, 0.2f};

        LinearGradient gradient = new LinearGradient(0, 0, 40,
                etTitleDetalle.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);

        etTitleDetalle.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;

        etTitleDetalle.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

    }

    public void mostrarAdapter() {
        if (plato.isCompartido()) {
            rvAnadirPlatoDetalle.setVisibility(View.VISIBLE);
            //Métodos del adapter solo si es compartido
        }
    }

    public void asignarId() {
        ivFotoDetalle = findViewById(R.id.ivFotoPersonaPlato);
        etTitleDetalle = findViewById(R.id.etTitleDetallePlato);
        etDescripcionDetalle = findViewById(R.id.etDescripcionDetallePlato);
        rvAnadirPlatoDetalle = findViewById(R.id.rvAnadirDetallePlato);
        btnContinuarDetalle = findViewById(R.id.btnEditarPlato);
        btnBorrarDetalle = findViewById(R.id.btnBorrarPlato);
    }

}