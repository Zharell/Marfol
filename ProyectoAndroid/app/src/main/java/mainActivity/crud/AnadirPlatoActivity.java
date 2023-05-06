package mainActivity.crud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.marfol.R;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class AnadirPlatoActivity extends AppCompatActivity {

    private Switch swCompartirPlato;
    private RecyclerView rvPlatosAnadirPlato;
    private TextView tvTitleAnadirP, etNombreAnadirP, etDescAnadirP, tvSubTitP;
    private Button btnContinuarAnadirP;
    private ImageView ivLoginAnadirPlato, ivPlatoAnadirP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_plato);

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Comprueba si el switch compartir está activo o no para mostrar su información
        swCompartirPlato.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    tvSubTitP.setVisibility(View.VISIBLE);
                    rvPlatosAnadirPlato.setVisibility(View.VISIBLE);
                } else {
                    tvSubTitP.setVisibility(View.INVISIBLE);
                    rvPlatosAnadirPlato.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    public void asignarEfectos() {

        //Ajusta el tamaño de la imagen del login
        ivLoginAnadirPlato.setPadding(20, 20, 20, 20);

        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };

        float[] positions = {0f, 0.2f};

        LinearGradient gradient = new LinearGradient(0, 0, 40,
                tvTitleAnadirP.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);

        tvTitleAnadirP.getPaint().setShader(gradient);
        tvSubTitP.getPaint().setShader(gradient);
        swCompartirPlato.getPaint().setShader(gradient);
        btnContinuarAnadirP.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;

        tvTitleAnadirP.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

        //Inserta Imagen photo
        ivPlatoAnadirP.setImageURI(Uri.parse("android.resource://com.tfg.marfol/"+R.drawable.camera));
        ivPlatoAnadirP.setPadding(30, 30, 30, 30);

    }

    public void asignarId() {

        //Asigna Ids a los elementos de la actividad
        rvPlatosAnadirPlato = findViewById(R.id.rvPlatosAnadirPlato);
        tvTitleAnadirP = findViewById(R.id.tvTitleAnadirPlato);
        etNombreAnadirP = findViewById(R.id.etNombreAnadirPlato);
        etDescAnadirP = findViewById(R.id.etDescripcionAnadirPlato);
        tvSubTitP = findViewById(R.id.tvListaPlatosAnadirPlato);
        ivLoginAnadirPlato = findViewById(R.id.ivLoginAnadirPlato);
        btnContinuarAnadirP = findViewById(R.id.btnPlatosAnadirPlato);
        ivPlatoAnadirP = findViewById(R.id.ivPlatoAnadirPlato);
        swCompartirPlato = findViewById(R.id.swCompartirAnadirPlato);

    }

}