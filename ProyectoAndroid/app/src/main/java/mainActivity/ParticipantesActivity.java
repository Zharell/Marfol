package mainActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tfg.marfol.R;


import java.util.ArrayList;


import adapters.PersonaAdapter;
import adapters.PersonaAdapterBd;
import entities.Persona;
import login.AuthActivity;
import mainActivity.crud.AnadirParticipanteActivity;
import mainActivity.detalle.DetallePersonaActivity;

public class ParticipantesActivity extends AppCompatActivity implements PersonaAdapter.onItemClickListener, PersonaAdapterBd.onItemClickListenerBd {

    private final int MINIMO_PLATOS = 1;
    private boolean borrarComensal = false;
    private int numPlatos = 0;
    private ImageView ivLoginParticipantes, ivMenuParticipantes;
    private TextView tvTitleParticipantes, tvRecorPar;
    private Dialog puVolverParticipantes;
    private Button btnCancelarParticipantes, btnConfirmarParticipantes, btnContinuarParticipantes;
    private Intent volverIndex;
    private RecyclerView rvPersonaParticipantes;
    private PersonaAdapter personaAdapter;
    private PersonaAdapterBd personaAdapterBd;
    private ActivityResultLauncher rLauncherAnadirComensal;
    private ActivityResultLauncher rLauncherDetalleComensal;
    private ActivityResultLauncher rLauncherDesglose;
    private ActivityResultLauncher rLauncherLogin;
    private ArrayList<Persona> comensales;
    private int comensalPosicion;
    private ArrayList<Persona> comensalesBd;
    private RecyclerView rvPersonaParticipantesBd;
    private Intent irLogin;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

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

        //comprobar si estoy logueado
        if (MetodosGlobales.comprobarLogueado(this, ivLoginParticipantes)) {
            botonImagenLogueado();
        } else {
            Glide.with(this).load(R.drawable.nologinimg).into(ivLoginParticipantes);
            botonImagenNoLogueado();
        }

        //Laucher Result recibe el ArrayList con los nuevos comensales y los inserta en el adapter
        rLauncherAnadirComensal = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        comensales = (ArrayList<Persona>) data.getSerializableExtra("arrayListComensales");
                        personaAdapter.setResultsPersona(comensales);
                        comprobarLauncher();
                    } else {
                        comprobarLauncher();
                    }
                }
        );

        //Laucher Result recibe la persona y actualiza de ser necesario
        rLauncherDetalleComensal = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        comprobarLauncher();
                        Intent data = result.getData();
                        borrarComensal = data.getBooleanExtra("borrarComensal", false);
                        if (!borrarComensal) {
                            comensales.set(comensalPosicion, (Persona) data.getSerializableExtra("detalleComensal"));
                            personaAdapter.setResultsPersona(comensales);
                        } else {
                            //Método que comprueba con quien has compartido y lo borra de ser necesario
                            borrarCompartidos();
                            comensales.remove(comensalPosicion);
                            personaAdapter.setResultsPersona(comensales);
                            //Importantísimo, reorganiza los ComensalCodes al borrar un comensal
                            for (int i = 1; i < comensales.size(); i++) {
                                comensales.get(i).setComensalCode(i);
                            }
                        }
                    } else {
                        comprobarLauncher();
                    }
                }
        );

        //Laucher Result recibe comensales desde Desglose por si se ha modificado
        rLauncherDesglose = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        comprobarLauncher();
                        Intent data = result.getData();
                        comensales = (ArrayList<Persona>) data.getSerializableExtra("arrayListDesglose");
                        personaAdapter.setResultsPersona(comensales);
                    } else {
                        comprobarLauncher();
                    }
                }
        );

        //Comprueba si estás logueado
        rLauncherLogin = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    comprobarLauncher();
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

        //Botón de continuar, avanza a la siguiente actividad
        // 1 - Debe haber mínimo un comensal
        // 2 - Debe haber mínimo un plato ( la primera vez )
        btnContinuarParticipantes.setOnClickListener(view -> {
            //Obtenemos todos los platos
            int numPlatos = obtenerPlatos();
            if (comensales.size() > 1 && numPlatos >= MINIMO_PLATOS) {
                //Accede a la actividad detalle de una persona
                Intent intentDesglose = new Intent(this, DesgloseActivity.class);
                intentDesglose.putExtra("envioDesglose", comensales);
                rLauncherDesglose.launch(intentDesglose);
            } else {
                //Comprobamos qué elemento nos falta para avanzar al desglose
                if (comensales.size() < 2) {
                    Toast.makeText(this, "Debe añadir al menos un comensal", Toast.LENGTH_SHORT).show();
                }
                if (numPlatos < MINIMO_PLATOS) {
                    Toast.makeText(this, "Debe añadir al menos un plato", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void botonImagenNoLogueado() {
        //Puesto provisional para probar cosas
        ivLoginParticipantes.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.AuthActivity.class);
            rLauncherLogin.launch(intent);
        });
    }

    private void botonImagenLogueado() {
        //Puesto provisional para probar cosas
        ivLoginParticipantes.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.HomeActivity.class);
            rLauncherLogin.launch(intent);
        });
    }

    public void borrarCompartidos() {
        for (int i = 1; i < comensales.size(); i++) {
            for (int j = 1; j < comensales.get(i).getPlatos().size(); j++) {
                if (comensales.get(i).getPlatos().get(j).isCompartido()) {
                    for (int h = 0; h < comensales.get(i).getPlatos().get(j).getPersonasCompartir().size(); h++) {
                        if (comensales.get(i).getPlatos().get(j).getPersonasCompartir().get(h).getComensalCode() == comensales.get(comensalPosicion).getComensalCode()) {
                            comensales.get(i).getPlatos().remove(j);
                            break;
                        }
                    }
                }
            }
        }
    }

    //Comprueba si se han añadido platos para avanzar al Desglose
    public int obtenerPlatos() {
        for (int i = 1; i < comensales.size(); i++) {
            numPlatos += comensales.get(i).obtenerNumPlatos();
        }
        return numPlatos;
    }

    //Método que al pulsar el botón de volver redirige a la pantalla Index sin perder información
    @Override
    public void onBackPressed() {
        //Pregunta si realmente quieres salir
        puVolverParticipantes.show();
    }


    public void asignarId() {

        //Asigna Ids a los elementos de la actividad
        ivMenuParticipantes = findViewById(R.id.ivMenuAnadirPlato);
        ivLoginParticipantes = findViewById(R.id.ivParticipantesImagen);
        rvPersonaParticipantes = findViewById(R.id.rvPersonaParticipantes);
        rvPersonaParticipantesBd = findViewById(R.id.rvPersonaParticipantesBd);
        tvTitleParticipantes = findViewById(R.id.tvTitleAnadirPlato);
        btnContinuarParticipantes = findViewById(R.id.btnContinuarParticipantes);
        tvRecorPar = findViewById(R.id.tvRecordarParticipantesPar);
        volverIndex = new Intent(this, IndexActivity.class);
        irLogin = new Intent(this, AuthActivity.class);

        //Asigna IDs de los elementos del popup
        puVolverParticipantes = new Dialog(this);
        puVolverParticipantes.setContentView(R.layout.popup_confirmacion);
        btnCancelarParticipantes = puVolverParticipantes.findViewById(R.id.btnCancelarPopup);
        btnConfirmarParticipantes = puVolverParticipantes.findViewById(R.id.btnConfirmarPopup);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        comensales = new ArrayList<>();
        comensalesBd = new ArrayList<>();
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

        //Colores rojizos
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

        //Prepara el Adapter para su uso
        rvPersonaParticipantes.setLayoutManager(new GridLayoutManager(this, 2));
        personaAdapter = new PersonaAdapter();
        rvPersonaParticipantes.setAdapter(personaAdapter);
        personaAdapter.setmListener(this);
        //Añade el contenido al adapter, si está vacío el propio Adapter añade el " Añadir Persona "
        personaAdapter.setResultsPersona(comensales);

        //aqui datos de la base de datos
        rvPersonaParticipantesBd.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        personaAdapterBd = new PersonaAdapterBd();
        rvPersonaParticipantesBd.setAdapter(personaAdapterBd);
        personaAdapterBd.setmListener(this);
        if (currentUser != null) {
            cargarDatosBd();
        }
    }

    private void cargarDatosBd() {
        if (currentUser != null) {
            comensalesBd = new ArrayList<>();
            String usuarioId = currentUser.getEmail(); // Utiliza el email como ID único del usuario
            DocumentReference id = db.collection("users").document(usuarioId);

            // Obtén la colección "personas" en Firestore
            CollectionReference personasRef = db.collection("personas");

            // Realiza la consulta para obtener todas las personas del usuario actual
            Query consulta = personasRef.whereEqualTo("usuarioId", usuarioId);

            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Limpiar el ArrayList antes de agregar los nuevos datos
                    int incremento = 1;

                    // Recorrer los documentos obtenidos y agregar los datos al ArrayList
                    for (DocumentSnapshot document : task.getResult()) {

                        String nombre = document.getString("nombre");
                        String descripcion = document.getString("descripcion");
                        String imagen = document.getString("imagen");
                        Persona persona = new Persona(incremento, nombre, descripcion, imagen, new ArrayList<>(), 0);
                        comensalesBd.add(persona);
                        incremento++;
                    }

                    // Notificar al adapter que los datos han cambiado
                    personaAdapterBd.setResultsPersonaBd(comensalesBd);

                } else {
                    // Ocurrió un error al obtener los documentos
                }
            });
        } else {
            // El usuario no está autenticado, muestra un mensaje o inicia sesión automáticamente
            Toast.makeText(this, "Inicia sesión para cargar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(int position) {
        comensalPosicion = position;
        //Si pulsas "Añadir Persona" ( 0 ), accederás a la actividad añadir persona
        if (position > 0) {
            //Accede a la actividad detalle de una persona
            Intent intentDetalle = new Intent(this, DetallePersonaActivity.class);
            intentDetalle.putExtra("comensalDetalle", comensales.get(position));
            intentDetalle.putExtra("arrayListComenComp", comensales);
            rLauncherDetalleComensal.launch(intentDetalle);

        } else {
            //Accede a la actividad para añadir nuevos comensales
            Intent intent = new Intent(this, AnadirParticipanteActivity.class);
            intent.putExtra("arrayListComensales", comensales);
            rLauncherAnadirComensal.launch(intent);

        }

    }

    @Override
    public void onItemClickBd(int position) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, AnadirParticipanteActivity.class);
        i.putExtra("importado", true);
        i.putExtra("arrayListComensales", comensales);
        i.putExtra("comensalesBd", comensalesBd.get(position));
        rLauncherAnadirComensal.launch(i);

    }

    private void comprobarLauncher() {
        if (MetodosGlobales.comprobarLogueado(this, ivLoginParticipantes)) {
            currentUser = mAuth.getCurrentUser();
            cargarDatosBd();
            botonImagenLogueado();
        } else {
            Glide.with(this).load(R.drawable.nologinimg).into(ivLoginParticipantes);
            botonImagenNoLogueado();
        }
    }
}
