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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tfg.marfol.R;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private ArrayList<Persona> nombreCompartir;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher <Intent> camaraLauncher;
    private ActivityResultLauncher rLauncherPlatos;
    private Button btnContinuarDetalle, btnBorrarDetalle;
    private String uriCapturada="";
    private FirebaseFirestore db;
    private FirebaseAuth auth ;
    private FirebaseUser currentUser;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_persona);

        //Recibe la lista de comensales para empezar a añadir
        Intent intent = getIntent();
        comensal = (Persona) intent.getSerializableExtra("comensalDetalle");
        nombreCompartir = (ArrayList<Persona>) intent.getSerializableExtra("arrayListComenComp");
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

        //Botón de borrar, llama a un método que a además de borrar, reordena los comensalID
        btnBorrarDetalle.setOnClickListener(view -> {
            borrarComensal();
        });

    }

    public void borrarComensal(){
        Intent borrarComensal = new Intent();
        borrarComensal.putExtra("borrarComensal", true);
        setResult(Activity.RESULT_OK, borrarComensal);
        finish();
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
            //AÑADIR A BD
            editarComensalBd();
            //Añado a la lista la persona creada
            Persona personaEditada = new Persona(comensal.getComensalCode(),nombre, descripcion, uriCapturada, platos, 0);
            Intent intentComensal = new Intent();
            intentComensal.putExtra("detalleComensal", personaEditada);
            setResult(Activity.RESULT_OK, intentComensal);
            finish();
        }
    }
    private void editarComensalBd() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null){
            Toast.makeText(DetallePersonaActivity.this, "Los datos no se guardarán en bd", Toast.LENGTH_SHORT).show();
            return;
        }
        String usuarioId = currentUser.getEmail();
        String nombreNuevo = String.valueOf(etTitleDetalle.getText());
        String descripcionNueva = String.valueOf(etDescripcionDetalle.getText());
        String nombreAntiguo = comensalBd.getNombre();
        String imagenAntigua = comensalBd.getUrlImage();
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
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String personaId = document.getId(); // Obtiene el ID del documento de la persona
                                                DocumentReference personaRef = personasRef.document(personaId);
                                                    personaRef.update("nombre", nombreNuevo, "descripcion", descripcionNueva, "imagen", uriCapturada)
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
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String personaId = document.getId(); // Obtiene el ID del documento de la persona
                            DocumentReference personaRef = personasRef.document(personaId);
                            personaRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // La actualización se realizó exitosamente
                                            anadirPersonaABd(nombreNuevo,descripcionNueva,uriCapturada);
                                            Toast.makeText(DetallePersonaActivity.this, "Se actualizaron nuevos datos", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Ocurrió un error al actualizar los datos
                                            Toast.makeText(DetallePersonaActivity.this, "No se borró", Toast.LENGTH_SHORT).show();
                                        }
                                    });;
                        }
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
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() != null) {
            email = currentUser.getEmail();
        }
        btnBorrarDetalle = findViewById(R.id.btnBorrarDetalle);
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
            intent.putExtra("arrayListComenComp", nombreCompartir);
            intent.putExtra("comensalCode", comensal.getComensalCode());
            rLauncherPlatos.launch(intent);

        }
    }
    private void anadirPersonaABd(String nombre, String descripcion,String imagen) {


        if (currentUser != null) {
            // El usuario está autenticado
            String usuarioId = currentUser.getEmail(); // Utiliza el email como ID único del usuario

            // Obtén la colección "personas" en Firestore
            CollectionReference personasRef = db.collection("personas");

            // Realiza la consulta para verificar si ya existe una persona con los mismos valores de nombre y descripción
            Query consulta = personasRef.whereEqualTo("nombre", nombre)
                    .whereEqualTo("usuarioId", usuarioId);

            consulta.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // No existe una persona con los mismos valores de nombre y descripción, procede a agregarla

                            // Crea un objeto HashMap para almacenar los datos de la nueva persona
                            Map<String, Object> nuevaPersona = new HashMap<>();
                            nuevaPersona.put("nombre", nombre);
                            nuevaPersona.put("descripcion", descripcion);
                            nuevaPersona.put("usuarioId", usuarioId);

                            // Agrega la nueva persona con un ID único generado automáticamente
                            personasRef.add(nuevaPersona)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            // La persona se agregó exitosamente
                                            String personaId = documentReference.getId();
                                            subirImagenPersona(personaId, imagen);
                                            Toast.makeText(DetallePersonaActivity.this, "Se agregó la persona exitosamente", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Ocurrió un error al agregar la persona
                                            Toast.makeText(DetallePersonaActivity.this, "Ocurrió un error al agregar la persona", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Ya existe una persona con los mismos valores de nombre y descripción
                            Toast.makeText(DetallePersonaActivity.this, "Ya existe una persona con ese nombre", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Ocurrió un error al obtener los documentos
                        Toast.makeText(DetallePersonaActivity.this, "Error al obtener los documentos", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // El usuario no está autenticado, muestra un mensaje o inicia sesión automáticamente
            Toast.makeText(this, "Inicia sesión para agregar una persona", Toast.LENGTH_SHORT).show();
        }
    }
    private void subirImagenPersona(String personaId, String imagen) {
        if (personaId != null && !personaId.isEmpty()) {
            // Obtiene una referencia al Storage de Firebase
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            // Define una referencia a la imagen en Storage utilizando el ID de la persona
            String rutaImagen = "personas/" + personaId + ".jpg";
            StorageReference imagenRef = storageRef.child(rutaImagen);

            // Sube la imagen a Storage
            Uri imagenUri = Uri.parse(imagen);
            UploadTask uploadTask = imagenRef.putFile(imagenUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {            // La imagen se subió exitosamente
                    Toast.makeText(DetallePersonaActivity.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();

                    // Obtiene la URL de descarga de la imagen
                    imagenRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // La URL de descarga de la imagen está disponible
                            String imagenUrl = uri.toString();

                            // Actualiza el campo "imagen" en Firestore con la URL de descarga de la imagen
                            db.collection("personas").document(personaId)
                                    .update("imagen", imagenUrl)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // La URL de la imagen se actualizó exitosamente en Firestore
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Ocurrió un error al actualizar el campo "imagen" en Firestore
                                        }
                                    });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Ocurrió un error al subir la imagen
                    Toast.makeText(DetallePersonaActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // El ID de la persona es nulo o vacío, muestra un mensaje de error
            Toast.makeText(DetallePersonaActivity.this, "ID de persona inválido", Toast.LENGTH_SHORT).show();
        }
    }
}