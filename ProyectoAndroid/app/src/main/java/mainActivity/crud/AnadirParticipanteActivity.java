package mainActivity.crud;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tfg.marfol.R;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapters.AnadirPersonaAdapter;
import entities.Persona;
import entities.Plato;

public class AnadirParticipanteActivity extends AppCompatActivity implements AnadirPersonaAdapter.onItemClickListener {

    private RecyclerView rvPlatosAnadirParticipante;
    private TextView tvTitleAnadirP, tvSubTitP;
    private EditText etNombreAnadirP, etDescAnadirP;
    private Button btnContinuarAnadirP;
    private ImageView ivLoginAnadirParticipante, ivPlatoAnadirP;

    private AnadirPersonaAdapter anadirPAdapter;

    private ArrayList <Plato> platos;
    private ActivityResultLauncher <Intent> camaraLauncher;
    private ActivityResultLauncher rLauncherPlatos;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private String uriCapturada="";
    private ArrayList<Persona> comensales;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_participante);


        //Recibe la lista de comensales para empezar a añadir
        Intent intent = getIntent();
        comensales = (ArrayList<Persona>) intent.getSerializableExtra("arrayListComensales");

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();
        //test

        //Laucher Result - Se debe añadir el switch(code) que dependiendo de que actividad vuelva, haga una u otra cosa
        rLauncherPlatos = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        platos = (ArrayList<Plato>) data.getSerializableExtra("arrayListPlatos");
                        anadirPAdapter.setResultsPlato(platos);
                    }



                }
        );

        // Registrar el launcher para la cámara
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {

                // Si la foto se toma correctamente, mostrar la vista previa en el ImageView
                Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                ivPlatoAnadirP.setImageBitmap(photo);

                // Insertar la imagen en la galería y obtenemos la URI transformada en String para almacenar en la BD
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "personaMarfol.jpg");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try {

                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    //Obtenemos la ruta URI de la imagen seleccionada
                    uriCapturada = uri.toString();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        //Añadimos onClick en el ImageView para activar la imagen
        ivPlatoAnadirP.setOnClickListener(view -> {
            // Solicitar permiso para acceder a la cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                //Si no tenemos los permisos los obtenemos
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {

                // Si ya se tienen los permisos, abrir la cámara
                abrirCamara();
            }
        });

        btnContinuarAnadirP.setOnClickListener(view -> {

            //Método encargado de crear un comensal
            anadirComensal();

        });

        /*
        platos.add(new Plato( "pollito con papa", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "el pepe", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "kaylertragaSa", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "cachopo", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "cerveza viemfria", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "cocacolastic", "eso es lo que me gusta a mí",20,20,"",false));
        platos.add(new Plato( "juan", "eso es lo que me gusta a mí",20,20,"",false));
        anadirPAdapter.setResultsPlato(platos);
        */

    }

    public void anadirComensal() {

        boolean esValidado=true;
        String nombre = String.valueOf(etNombreAnadirP.getText());
        String descripcion = String.valueOf(etDescAnadirP.getText());
        anadirPersonaABd(nombre,descripcion);


        ArrayList <Plato> platos = new ArrayList<Plato>();

        //Comprueba si has añadido un nombre
        if (etNombreAnadirP.getText().toString().length() == 0) {
            Toast.makeText(this,"Debe introducir un nombre", Toast.LENGTH_SHORT).show();

            esValidado=false;
        }

        //Comprueba si ha validado, añade la persona, envía al padre el ArrayList y cierra la actividad
        if (esValidado) {

            //Añado a la lista la persona creada
            comensales.add(new Persona(nombre, descripcion, uriCapturada, platos));

            Intent intentComensal = new Intent();
            intentComensal.putExtra("arrayListComensales", comensales);
            setResult(Activity.RESULT_OK, intentComensal);
            finish();

        }


    }

    // Método para manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Si se conceden los permisos, abrir la cámara
                abrirCamara();

            } else {

                // Si se deniegan los permisos, mostrar un mensaje al usuario
                Toast.makeText(this, "Para almacenar la imagen debe otorgar los permisos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para abrir la cámara
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
    }

    //Se debe insertar el ArrayList vacío para que el adaptador inserte el objeto 0 ( añadir elemento )
    public void mostrarAdapter() {

        //Se añaden la lista de platos vacía para que aparezca el botón de añadir Plato
        platos = new ArrayList<Plato>();

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
        //Inserta Imagen photo
        ivPlatoAnadirP.setImageURI(Uri.parse("android.resource://com.tfg.marfol/"+R.drawable.camera));
        ivPlatoAnadirP.setPadding(30, 30, 30, 30);
    }
    public void asignarId() {
        //Asigna Ids a los elementos de la actividad
        rvPlatosAnadirParticipante = findViewById(R.id.rvPlatosAnadirPlato);
        tvTitleAnadirP = findViewById(R.id.tvTitleAnadirPlato);
        etNombreAnadirP = findViewById(R.id.etNombreAnadirPlato);
        etDescAnadirP = findViewById(R.id.etDescripcionAnadirPlato);
        tvSubTitP = findViewById(R.id.tvListaPlatosAnadirPlato);
        ivLoginAnadirParticipante = findViewById(R.id.ivLoginAnadirPlato);
        btnContinuarAnadirP = findViewById(R.id.btnPlatosAnadirPlato);
        ivPlatoAnadirP = findViewById(R.id.ivPlatoAnadirPlato);
    }
    @Override
    public void onItemClick(int position) {
        //Si pulsas "Añadir Persona" ( 0 ), accederás a la actividad añadir persona
        if (position>0) {
            Toast.makeText(this,"Pulsaste el campo: "+String.valueOf(position),Toast.LENGTH_SHORT).show();
        } else {
            //Accedemos a la actividad de añadir plato
            Intent intent = new Intent(this, AnadirPlatoActivity.class);
            intent.putExtra("arrayListPlatos", platos);
            rLauncherPlatos.launch(intent);
        }
    }
    private void anadirPersonaABd(String nombre,String descripcion) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!nombre.equals("")) {
            if (currentUser != null) {
                // el usuario está autenticado
                CollectionReference personaRef = db.collection("users").document(currentUser.getEmail()).collection("personas");

                // iterar sobre la lista de personas que deseas agregar

                Map<String, Object> nuevaPersona = new HashMap<>();
                nuevaPersona.put("nombre", nombre);
                nuevaPersona.put("descripcion", descripcion);

                personaRef.add(nuevaPersona)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                // la persona se agregó exitosamente
                                Toast.makeText(AnadirParticipanteActivity.this, "Se agregó la persona exitosamente", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // ocurrió un error al agregar la persona
                                Toast.makeText(AnadirParticipanteActivity.this, "Ocurrió un error al agregar la persona", Toast.LENGTH_SHORT).show();
                            }
                        });

            } else {
                // el usuario no está autenticado, muestra un mensaje o inicia sesión automáticamente
                Toast.makeText(this, "Inicia sesión para agregar una persona", Toast.LENGTH_SHORT).show();
            }

        }
    }

}