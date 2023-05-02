package mainActivity.crud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.marfol.R;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import adapters.AnadirPersonaAdapter;
import entities.Plato;

public class AnadirParticipanteActivity extends AppCompatActivity implements AnadirPersonaAdapter.onItemClickListener {

    private RecyclerView rvPlatosAnadirParticipante;
    private TextView tvTitleAnadirP, etNombreAnadirP, etDescAnadirP, tvSubTitP;
    private Button btnContinuarAnadirP;
    private ImageView ivLoginAnadirParticipante;

    private AnadirPersonaAdapter anadirPAdapter;

    private ArrayList <Plato> platos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_participante);

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();


        platos.add(new Plato( "pollito con papa", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "el pepe", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "kaylertragaSa", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "cachopo", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "cerveza viemfria", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "cocacolastic", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "juan", "eso es lo que me gusta a mí",20,20,"",false));
        anadirPAdapter.setResultsPlato(platos);

    }

    public void mostrarAdapter() {

        //ArrayList creado para probar adapter
        platos = new ArrayList<>();

        //Prepara el Adapter para su uso
        rvPlatosAnadirParticipante.setLayoutManager(new LinearLayoutManager(this));
        anadirPAdapter = new AnadirPersonaAdapter();
        rvPlatosAnadirParticipante.setAdapter(anadirPAdapter);
        anadirPAdapter.setmListener(this::onItemClick);

        anadirPAdapter.setResultsPlato(platos);

    }

    public void asignarEfectos() {

        //Ajusta el tamaño de la imagen del login
        ivLoginAnadirParticipante.setPadding(20, 20, 20, 20);

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
        btnContinuarAnadirP.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;

        tvTitleAnadirP.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

    }

    public void asignarId() {

        //Asigna Ids a los elementos de la actividad
        rvPlatosAnadirParticipante = findViewById(R.id.rvPlatosAnadirParticipante);
        tvTitleAnadirP = findViewById(R.id.tvTitleAnadirParticipante);
        etNombreAnadirP = findViewById(R.id.etNombreAnadirParticipante);
        etDescAnadirP = findViewById(R.id.etDescripcionAnadirParticipante);
        tvSubTitP = findViewById(R.id.tvListaPlatosAnadirParticipante);
        ivLoginAnadirParticipante = findViewById(R.id.ivLoginAnadirParticipante);
        btnContinuarAnadirP = findViewById(R.id.btnPlatosAnadirParticipante);

    }


    @Override
    public void onItemClick(int position) {

        //Si pulsas "Añadir Persona" ( 0 ), accederás a la actividad añadir persona
        if (position>0) {

            Toast.makeText(this,"Pulsaste el campo: "+String.valueOf(position),Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(this,"Pulsaste el campo: "+String.valueOf(position),Toast.LENGTH_SHORT).show();

        }

    }
}