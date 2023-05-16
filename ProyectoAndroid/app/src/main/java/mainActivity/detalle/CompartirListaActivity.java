package mainActivity.detalle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.ListaCompartirAdapter;
import adapters.PersonaAdapter;
import entities.Persona;
import mainActivity.crud.AnadirPlatoActivity;

public class CompartirListaActivity extends AppCompatActivity implements ListaCompartirAdapter.onItemClickListener {

    private ArrayList<Persona> nombreCompartir;
    private ImageView ivLoginListComp, ivMenuListComp;
    private TextView tvTitleListComp;
    private ListaCompartirAdapter listaAdapter;
    private RecyclerView rvPersonaCompartir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compartir_lista);

        Intent intentObtener = getIntent();
        nombreCompartir = (ArrayList<Persona>) intentObtener.getSerializableExtra("arrayListComenComp");

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();




    }

    public void mostrarAdapter() {
        //Prepara el Adapter para su uso
        rvPersonaCompartir.setLayoutManager(new GridLayoutManager(this,2));
        listaAdapter = new ListaCompartirAdapter();
        rvPersonaCompartir.setAdapter(listaAdapter);
        listaAdapter.setmListener(this);
        listaAdapter.setResultsListCompartir(nombreCompartir);
    }

    public void asignarEfectos() {
        //Ajusta el tamaño de la imagen del login
        ivLoginListComp.setPadding(20, 20, 20, 20);

        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };
        float[] positions = {0f, 0.2f};
        LinearGradient gradient = new LinearGradient(0, 0, 40,
                tvTitleListComp.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);
        tvTitleListComp.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;
        tvTitleListComp.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
    }

    public void asignarId() {
        //Asigna Ids a los elementos de la actividad
        ivLoginListComp = findViewById(R.id.ivLoginCompartir);
        ivMenuListComp = findViewById(R.id.ivMenuCompartir);
        tvTitleListComp = findViewById(R.id.tvTitleCompartir);
        rvPersonaCompartir = findViewById(R.id.rvPersonaCompartir);
    }

    @Override
    public void onItemClick(int position) {
        //Devuelve la persona que va a compartir
        Intent intentPersonaCom = new Intent();
        intentPersonaCom.putExtra("personaCompartir", nombreCompartir.get(position));
        setResult(Activity.RESULT_OK, intentPersonaCom);
        finish();

    }
}