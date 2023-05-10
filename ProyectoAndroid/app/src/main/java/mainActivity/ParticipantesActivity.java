package mainActivity;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;

import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.PersonaAdapter;
import adapters.PersonaBdAdapter;
import entities.Persona;
import mainActivity.crud.AnadirParticipanteActivity;
import mainActivity.detalle.DetallePersonaActivity;

public class ParticipantesActivity extends AppCompatActivity implements PersonaAdapter.onItemClickListener {

    private ImageView ivLoginParticipantes, ivMenuParticipantes;
    private TextView tvTitleParticipantes;
    private Dialog puVolverParticipantes;
    private Button btnCancelarParticipantes, btnConfirmarParticipantes, btnContinuarParticipantes;
    private Intent volverIndex;
    private RecyclerView rvPersonaParticipantes;
    private PersonaAdapter personaAdapter;
    private PersonaBdAdapter personaAdapterBd;
    private ActivityResultLauncher rLauncherAnadirComensal;
    private ActivityResultLauncher rLauncherDetalleComensal;
    private ArrayList<Persona> comensales;
    private int comensalPosicion;

    private ArrayList<Persona> comensalesBd;
    private TextView tvLlenarTexto;

    private RecyclerView rvPersonaParticipantesBd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participantes);

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();
        llenarDatos();


        //Método BD
        llenarDatos();

        //Laucher Result recibe el ArrayList con los nuevos comensales y los inserta en el adapter
        rLauncherAnadirComensal = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        comensales = (ArrayList<Persona>) data.getSerializableExtra("arrayListComensales");
                        personaAdapter.setResultsPersona(comensales);
                    }
                }
        );

        //Laucher Result recibe la persona y actualiza de ser necesario
        rLauncherDetalleComensal = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        comensales.set(comensalPosicion,(Persona) data.getSerializableExtra("detalleComensal"));
                        personaAdapter.setResultsPersona(comensales);
                    }
                }
        );

        //Botones para el popup de confirmación
        //Confirmar, retrocede, cierra la actividad y pierde los datos introducidos - se debe cerrar con dismiss() para evitar fugas de memoria
        btnConfirmarParticipantes.setOnClickListener(view -> {
            startActivity(volverIndex);
            puVolverParticipantes.dismiss();
            finish();
        });

        //Cancela, desaparece el popup y continúa en la actividad
        btnCancelarParticipantes.setOnClickListener(view -> puVolverParticipantes.dismiss());


    }

    //Método que al pulsar el botón de volver redirige a la pantalla Index sin perder información
    @Override
    public void onBackPressed() {

        //Pregunta si realmente quieres salir
        puVolverParticipantes.show();

    }


    public void asignarId () {

        //Asigna Ids a los elementos de la actividad
        ivMenuParticipantes = findViewById(R.id.ivMenuAnadirPlato);
        ivLoginParticipantes = findViewById(R.id.ivLoginAnadirPlato);
        rvPersonaParticipantes = findViewById(R.id.rvPersonaParticipantes);
        tvTitleParticipantes = findViewById(R.id.tvTitleAnadirPlato);
        btnContinuarParticipantes = findViewById(R.id.btnContinuarParticipantes);
        tvLlenarTexto = findViewById(R.id.tvLlenarTexto);

        //Asigna IDs de los elementos del popup
        puVolverParticipantes = new Dialog(this);
        volverIndex = new Intent(this, IndexActivity.class);
        puVolverParticipantes.setContentView(R.layout.popup_confirmacion);
        btnCancelarParticipantes = puVolverParticipantes.findViewById(R.id.btnCancelarPopup);
        btnConfirmarParticipantes = puVolverParticipantes.findViewById(R.id.btnConfirmarPopup);

    }

    public void asignarEfectos() {

        //Ajusta el tamaño de la imagen del login
        ivLoginParticipantes.setPadding(20, 20, 20, 20);

        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };

        float[] positions = {0f, 0.2f};

        LinearGradient gradient = new LinearGradient(0, 0, 40,
                tvTitleParticipantes.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);

        tvTitleParticipantes.getPaint().setShader(gradient);
        btnConfirmarParticipantes.getPaint().setShader(gradient);
        btnCancelarParticipantes.getPaint().setShader(gradient);
        btnContinuarParticipantes.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;

        tvTitleParticipantes.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
    }

    //Método que prepara el recycler y el adaptador para su uso
    public void mostrarAdapter() {

        //ArrayList creado para probar adapter
        comensales = new ArrayList<>();

        //Prepara el Adapter para su uso
        rvPersonaParticipantes.setLayoutManager(new GridLayoutManager(this,2));
        personaAdapter = new PersonaAdapter();
        rvPersonaParticipantes.setAdapter(personaAdapter);
        personaAdapter.setmListener(this);

        //Añade el contenido al adapter, si está vacío el propio Adapter añade el " Añadir Persona "
        personaAdapter.setResultsPersona(comensales);

    }

    @Override
    public void onItemClick(int position) {
        comensalPosicion = position;
        //Si pulsas "Añadir Persona" ( 0 ), accederás a la actividad añadir persona
        if (position>0) {

            //Accede a la actividad detalle de una persona
            Intent intentDetalle = new Intent(this, DetallePersonaActivity.class);
            intentDetalle.putExtra("comensalDetalle", comensales.get(position));
            rLauncherDetalleComensal.launch(intentDetalle);

        } else {

            //Accede a la actividad para añadir nuevos comensales
            Intent intent = new Intent(this, AnadirParticipanteActivity.class);
            intent.putExtra("arrayListComensales", comensales);
            rLauncherAnadirComensal.launch(intent);


            /*
            comensales.add(new Persona("Juan", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Gayler", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Fer", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Javier", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Juan", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Gayler", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Fer", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Javier", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Juan", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Gayler", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Fer", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Javier", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Juan", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Gayler", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Fer", "Le gusta comer", "no hay URL", new ArrayList<>()));
            comensales.add(new Persona("Javier", "Le gusta comer", "no hay URL", new ArrayList<>()));


            //Añade el contenido al adapter, si está vacío el propio Adapter añade el " Añadir Persona "
            personaAdapter.setResultsPersona(comensales);
            */


        }

    }
    private void llenarDatos(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            CollectionReference personaRef = db.collection("users").document(currentUser.getEmail()).collection("personas");
            personaRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        String datos = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Persona persona = document.toObject(Persona.class);
                            datos += persona.getNombre() + " - " + persona.getDescripcion() + "\n";
                        }
                        tvLlenarTexto.setText(datos);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        } else {
            // el usuario no está autenticado, muestra un mensaje o inicia sesión automáticamente
            Toast.makeText(this, "Inicia sesión para ver los datos", Toast.LENGTH_SHORT).show();
        }


    }

}
