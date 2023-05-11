package mainActivity.detalle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tfg.marfol.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import adapters.AnadirPersonaAdapter;
import adapters.PersonaDetalleAdapter;
import entities.Persona;
import entities.Plato;
import mainActivity.crud.AnadirPlatoActivity;

public class DetallePersonaActivity extends AppCompatActivity implements PersonaDetalleAdapter.onItemClickListener {

    private Persona comensal,comensalBd;
    private ImageView ivLoginDetalle, ivMenuDetalle, ivFotoDetalle;
    private EditText etTitleDetalle, etDescripcionDetalle;
    private RecyclerView rvAnadirPlatoDetalle;
    private PersonaDetalleAdapter adapterDetalle;
    private ArrayList<Plato> platos;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher <Intent> camaraLauncher;
    private ActivityResultLauncher rLauncherPlatos;
    private Button btnContinuarDetalle;
    private String uriCapturada="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_persona);

        //Recibe la lista de comensales para empezar a añadir
        Intent intent = getIntent();
        comensal = (Persona) intent.getSerializableExtra("comensalDetalle");
        comensalBd=comensal;

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Método que inserta la información a los comensales
        insertarComensal();

        //Laucher Result - recibe los platos del usuario creado
        rLauncherPlatos = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        platos = (ArrayList<Plato>) data.getSerializableExtra("arrayListPlatos");
                        adapterDetalle.setResultsPlato(platos);
                    }


                }
        );

        //Añadimos onClick en el ImageView para activar la imagen
        ivFotoDetalle.setOnClickListener(view -> {
            // Solicitar permiso para acceder a la cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                //Si no tenemos los permisos los obtenemos
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                // Si ya se tienen los permisos, abrir la cámara
                abrirCamara();
            }
        });

        //Botón que vuelve a participantes y además devuelve el comensal modificado
        btnContinuarDetalle.setOnClickListener(view -> {
            editarComensalBd();
            editarComensal();
        });

        // Registrar el launcher para la cámara
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {

                // Si la foto se toma correctamente, mostrar la vista previa en el ImageView
                Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                ivFotoDetalle.setImageBitmap(photo);

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



    }

    public void editarComensal() {
        boolean esValidado=true;
        String nombre = String.valueOf(etTitleDetalle.getText());
        String descripcion = String.valueOf(etDescripcionDetalle.getText());

        //Comprueba si has añadido un nombre
        if (etTitleDetalle.getText().toString().length() == 0) {
            Toast.makeText(this,"Debe introducir un nombre", Toast.LENGTH_SHORT).show();
            esValidado=false;
        }
        //Comprueba si ha validado, añade la persona, envía al padre el ArrayList y cierra la actividad
        if (esValidado) {
            //Añado a la lista la persona creada
            Persona personaEditada = new Persona(nombre, descripcion, uriCapturada, platos);

            Intent intentComensal = new Intent();
            intentComensal.putExtra("detalleComensal", personaEditada);
            setResult(Activity.RESULT_OK, intentComensal);
            finish();
        }
    }
    private void editarComensalBd() {
        String nombreNuevo = String.valueOf(etTitleDetalle.getText());
        String descripcionNueva = String.valueOf(etDescripcionDetalle.getText());
        String nombreAntiguo = comensalBd.getNombre();
        String descripcionAntigua = comensalBd.getDescripcion();
        String imagenAntigua = comensalBd.getUrlImage();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String usuarioId = currentUser.getEmail();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference personasRef = db.collection("personas");

        Query query = personasRef.whereEqualTo("nombre", nombreNuevo);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        // No existe ninguna persona con el mismo nombre, procede a actualizar los datos
                        personasRef.whereEqualTo("usuarioId", usuarioId)
                                .whereEqualTo("nombre", nombreAntiguo)
                                .whereEqualTo("descripcion", descripcionAntigua)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String personaId = document.getId(); // Obtiene el ID del documento de la persona
                                                DocumentReference personaRef = personasRef.document(personaId);

                                                personaRef.update("nombre", nombreNuevo, "descripcion", descripcionNueva)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // La actualización se realizó exitosamente
                                                                Toast.makeText(DetallePersonaActivity.this, "Los datos se actualizaron correctamente", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Ocurrió un error al actualizar los datos
                                                                Toast.makeText(DetallePersonaActivity.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                break; // Si se encuentra una persona, se actualiza solo el primer documento y se sale del bucle
                                            }
                                        } else {
                                            // Ocurrió un error al obtener los documentos
                                            Toast.makeText(DetallePersonaActivity.this, "Error al obtener los documentos", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        // Ya existe una persona con el mismo nombre, no se realiza ninguna acción
                        Toast.makeText(DetallePersonaActivity.this, "Ya existe una persona con el mismo nombre", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Ocurrió un error al obtener los documentos
                    Toast.makeText(DetallePersonaActivity.this, "Error al obtener los documentos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



        // Método para abrir la cámara
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
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

    //Se debe insertar el ArrayList vacío para que el adaptador inserte el objeto 0 ( añadir elemento )
    public void mostrarAdapter() {

        //Se añaden la lista de platos vacía para que aparezca el botón de añadir Plato
        platos = comensal.getPlatos();

        //Prepara el Adapter para su uso
        rvAnadirPlatoDetalle.setLayoutManager(new LinearLayoutManager(this));
        adapterDetalle = new PersonaDetalleAdapter();
        rvAnadirPlatoDetalle.setAdapter(adapterDetalle);
        adapterDetalle.setmListener(this::onItemClick);
        adapterDetalle.setResultsPlato(platos);

    }

    public void asignarEfectos() {

        //Ajusta el tamaño de la imagen del login
        ivLoginDetalle.setPadding(20, 20, 20, 20);

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

    public void insertarComensal() {
        etTitleDetalle.setText(comensal.getNombre());
        etDescripcionDetalle.setText(comensal.getDescripcion());
        uriCapturada  = comensal.getUrlImage();
        if (!comensal.getUrlImage().equalsIgnoreCase("")) {
            ivFotoDetalle.setImageURI(Uri.parse(comensal.getUrlImage()));
        } else {
            //Inserta Imagen photo
            ivFotoDetalle.setImageURI(Uri.parse("android.resource://com.tfg.marfol/"+R.drawable.camera));
            ivFotoDetalle.setPadding(30, 30, 30, 30);
        }
    }

    public void asignarId() {

        ivLoginDetalle = findViewById(R.id.ivLoginDetallePersona);
        ivMenuDetalle = findViewById(R.id.ivMenuDetallePersona);
        ivFotoDetalle = findViewById(R.id.ivFotoPersonaDetalle);
        etTitleDetalle = findViewById(R.id.etTitleDetallePersona);
        etDescripcionDetalle = findViewById(R.id.etDescripcionDetallePersona);
        rvAnadirPlatoDetalle = findViewById(R.id.rvAnadirPlatoDetalle);
        btnContinuarDetalle = findViewById(R.id.btnEditarDetalle);
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
}