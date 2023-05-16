package mainActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tfg.marfol.R;

import java.util.ArrayList;
import java.util.List;

import adapters.DesgloseAdapter;
import adapters.PersonaAdapter;
import entities.Persona;
import entities.Plato;

public class DesgloseActivity extends AppCompatActivity implements DesgloseAdapter.onItemClickListener {

    private Button btnGuardarRestaurante;
    private RecyclerView rvPersonaDesglose;
    private TextView tvTitleDesglose;
    private ImageView ivMenuDesglose, ivLoginDesglose;
    private DesgloseAdapter desgloseAdapter;

    private ArrayList<Persona> comensales, listaComensales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desglose);

        //Recibe la lista de comensales para empezar a añadir
        Intent intent = getIntent();
        comensales = (ArrayList<Persona>) intent.getSerializableExtra("envioDesglose");

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Reparte los platos compartidos entre los comensales
        repartirCompartido();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();




    }

    //Método que prepara el recycler y el adaptador para su uso
    public void mostrarAdapter() {

        //Se debe borrar la primera posición ya que es el elemento de añadir y no tiene valores
        listaComensales = comensales;
        listaComensales.remove(0);

        //Prepara el Adapter para su uso
        rvPersonaDesglose.setLayoutManager(new LinearLayoutManager(this));
        desgloseAdapter = new DesgloseAdapter();
        rvPersonaDesglose.setAdapter(desgloseAdapter);
        desgloseAdapter.setmListener(this);

        //Añade el contenido al adapter, si está vacío el propio Adapter añade el " Añadir Persona "
        desgloseAdapter.setResultsPersona(listaComensales);

    }

    public void repartirCompartido() {
        double precioPlato;
        for (int i=0;i<comensales.size();i++) {
            for (int j=0;j<comensales.get(i).getPlatos().size();j++) {
                if (comensales.get(i).getPlatos().get(j).isCompartido()) {
                    comensales.get(i).getPlatos().get(j).getPersonasCompartir().add(comensales.get(i)); // ---
                    precioPlato = comensales.get(i).getPlatos().get(j).getPrecio() / (comensales.get(i).getPlatos().get(j).getPersonasCompartir().size());
                    for (int h = 0; h < comensales.get(i).getPlatos().get(j).getPersonasCompartir().size(); h++) {
                        for (int m=0;m<comensales.size();m++) {
                            if (comensales.get(m).getComensalCode() == comensales.get(i).getPlatos().get(j).getPersonasCompartir().get(h).getComensalCode()) {
                                comensales.get(m).sumarMonedero(precioPlato);
                            }
                        }
                    }
                }
            }
            comensales.get(i).asignarPrecio();
        }
    }

    public void asignarEfectos() {
        //Ajusta el tamaño de la imagen del login
        ivLoginDesglose.setPadding(20, 20, 20, 20);

        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };
        float[] positions = {0f, 0.2f};
        LinearGradient gradient = new LinearGradient(0, 0, 40,
                tvTitleDesglose.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);
        tvTitleDesglose.getPaint().setShader(gradient);
        btnGuardarRestaurante.getPaint().setShader(gradient);
        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;
        tvTitleDesglose.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
    }

    public void asignarId() {
        btnGuardarRestaurante = findViewById(R.id.btnGuardarRestauranteDes);
        rvPersonaDesglose = findViewById(R.id.rvPersonaDesglose);
        tvTitleDesglose = findViewById(R.id.tvTitleDesglose);
        ivMenuDesglose = findViewById(R.id.ivMenuDesglose);
        ivLoginDesglose = findViewById(R.id.ivLoginDesglose);
    }

    @Override
    public void onItemClick(int position) {

    }
}
