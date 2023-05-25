package mainActivity.crud;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tfg.marfol.R;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import adapters.AnadirPersonaAdapter;
import entities.Persona;
import entities.Plato;
import login.AuthActivity;
import mainActivity.MetodosGlobales;
import mainActivity.detalle.DetallePlatoActivity;
import mainActivity.detalle.RecordarPlatoActivity;

public class AnadirParticipanteActivity extends AppCompatActivity implements AnadirPersonaAdapter.onItemClickListener {

    private RecyclerView rvPlatosAnadirParticipante;
    private TextView tvTitleAnadirP, tvSubTitP;
    private EditText etNombreAnadirP, etDescAnadirP;
    private Button btnContinuarAnadirP;
    private ImageView ivAnadirPlatoImagen, ivPlatoAnadirP;
    private AnadirPersonaAdapter anadirPAdapter;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private ActivityResultLauncher rLauncherPlatos;
    private ActivityResultLauncher rLauncherLogin;
    private ActivityResultLauncher rLauncherDetallePlato;
    private ActivityResultLauncher rLauncherRecordarPlato;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private String uriCapturada = "";
    private ArrayList<Persona> comensales;
    private boolean borrarPlato;
    private boolean nuevoPlato;
    private int platoPosicion;
    private ArrayList<Plato> platos;
    private Intent intentComensal;
    private Intent intent;
    private Intent data;
    private Intent cameraIntent;
    private Intent inLogin;
    private String storage_path = "personas/*", photo = "photo";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private boolean importado;
    private Persona personaBd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_participante);

        //Recibe la lista de comensales para empezar a añadir
        intent = getIntent();
        comensales = (ArrayList<Persona>) intent.getSerializableExtra("arrayListComensales");
        importado = intent.getBooleanExtra("importado",false);
        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Comprobar usuario si está logueado o no
        comprobarLauncher();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Trae desde el clic de personas ya añadidas recientemente los participantes para añadirlos
        insertarDesdeBd();

        //Launcher que da la opción a recordar platos o añadir nuevos
        rLauncherRecordarPlato = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        nuevoPlato = data.getBooleanExtra("nuevoPlato", false);
                        if (nuevoPlato) {
                            //Abre la actividad para añadir nuevo plato
                            intent = new Intent(this, AnadirPlatoActivity.class);
                            intent.putExtra("arrayListPlatos", platos);
                            intent.putExtra("arrayListComenComp", comensales);
                            rLauncherPlatos.launch(intent);
                        } else {
                            //recibe el plato que anteriormente se había utilizado
                            platos.add((Plato) data.getSerializableExtra("recordarNuevo"));
                            anadirPAdapter.setResultsPlato(platos);
                        }
                    }
                }
        );

        //Launcher detallePlato se actualiza al recibir la modificación
        rLauncherDetallePlato = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        borrarPlato = data.getBooleanExtra("borrarPlato", false);
                        if (!borrarPlato) {
                            //Sustituye el plato de la posición elegida anteriormente ( EDITAR PLATO )
                            platos.set(platoPosicion, (Plato) data.getSerializableExtra("detallePlato"));
                            anadirPAdapter.setResultsPlato(platos);
                        } else {
                            //Borra el plato de la posición elegida anteriormente ( BORRAR PLATO )
                            platos.remove(platoPosicion);
                            anadirPAdapter.setResultsPlato(platos);
                        }
                    }
                }
        );

        //Laucher Result - recibe los platos del usuario creado
        rLauncherPlatos = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        data = result.getData();
                        platos = (ArrayList<Plato>) data.getSerializableExtra("arrayListPlatos");
                        anadirPAdapter.setResultsPlato(platos);
                        comprobarLauncher();
                    }
                }
        );

        // Registrar el launcher para la cámara
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {

                // Si la foto se toma correctamente, mostrar la vista previa en el ImageView
                Bitmap photo = (Bitmap) result.getData().getExtras().get("data");


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
                    ivPlatoAnadirP.setBackground(null);
                    Glide.with(this).load(uriCapturada).circleCrop().into(ivPlatoAnadirP);

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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                // Si ya se tienen los permisos, abrir la cámara
                abrirCamara();
            }
        });

        btnContinuarAnadirP.setOnClickListener(view -> {
            //Método encargado de crear un comensal
            anadirComensal();
        });
        rLauncherLogin = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    comprobarLauncher();
                }
        );
    }

    private void insertarDesdeBd() {
        if(importado){
            personaBd=(Persona)intent.getSerializableExtra("comensalesBd");
            etNombreAnadirP.setText(personaBd.getNombre());
            etDescAnadirP.setText(personaBd.getDescripcion());
            if (personaBd.getUrlImage() != null) {
                Glide.with(this).load(personaBd.getUrlImage()).into(ivPlatoAnadirP);
                uriCapturada=personaBd.getUrlImage();
            }
        }
    }

    private void comprobarLauncher(){
        if(MetodosGlobales.comprobarLogueado(this,ivAnadirPlatoImagen)){
            currentUser = mAuth.getCurrentUser();
            botonImagenLogueado();
        } else {
            Glide.with(this).load(R.drawable.nologinimg).into(ivAnadirPlatoImagen);
            botonImagenNoLogueado();
        }
    }

    public void anadirComensal() {
        boolean esValidado = true;
        String nombre = String.valueOf(etNombreAnadirP.getText());
        String descripcion = String.valueOf(etDescAnadirP.getText());

        if (etNombreAnadirP.getText().toString().length() == 0) {
            Toast.makeText(this, "Debe introducir un nombre", Toast.LENGTH_SHORT).show();
            esValidado = false;
        }

        if (esValidado) {
            // Añade la persona en la base de datos
            anadirPersonaABd(nombre, descripcion, uriCapturada);

            // Añade la persona localmente
            comensales.add(new Persona(comensales.size(), nombre, descripcion, uriCapturada, platos, 0));

            intentComensal = new Intent();
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
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
        ivAnadirPlatoImagen.setPadding(20, 20, 20, 20);

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
        ivPlatoAnadirP.setImageURI(Uri.parse("android.resource://com.tfg.marfol/" + R.drawable.camera));
        ivPlatoAnadirP.setPadding(30, 30, 30, 30);
    }

    public void asignarId() {
        //Asigna Ids a los elementos de la actividad
        rvPlatosAnadirParticipante = findViewById(R.id.rvPlatosAnadirPlato);
        tvTitleAnadirP = findViewById(R.id.tvTitleAnadirPlato);
        etNombreAnadirP = findViewById(R.id.etNombreAnadirPlato);
        etDescAnadirP = findViewById(R.id.etDescripcionAnadirPlato);
        tvSubTitP = findViewById(R.id.tvListaPlatosAnadirPlato);
        ivAnadirPlatoImagen = findViewById(R.id.ivAnadirPlatoImagen);
        btnContinuarAnadirP = findViewById(R.id.btnPlatosAnadirPlato);
        ivPlatoAnadirP = findViewById(R.id.ivPlatoAnadirPlato);
        inLogin = new Intent(this, AuthActivity.class);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onItemClick(int position) {
        platoPosicion = position;
        //Si pulsas "Añadir Plato" ( 0 ), accederás a la actividad añadir plato
        if (position > 0) {
            Intent intentDetalle = new Intent(this, DetallePlatoActivity.class);
            intentDetalle.putExtra("platoDetalle", platos.get(position));
            intentDetalle.putExtra("arrayListComenComp", comensales);
            rLauncherDetallePlato.launch(intentDetalle);
        } else {
            intent = new Intent(this, RecordarPlatoActivity.class);
            intent.putExtra("arrayListPlatos", platos);
            intent.putExtra("arrayListComenComp", comensales);
            rLauncherRecordarPlato.launch(intent);
        }
    }

    private void anadirPersonaABd(String nombre, String descripcion, String imagen) {
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
                            nuevaPersona.put("imagen", "");
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
                                            Toast.makeText(AnadirParticipanteActivity.this, "Se agregó la persona exitosamente", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Ocurrió un error al agregar la persona
                                            Toast.makeText(AnadirParticipanteActivity.this, "Ocurrió un error al agregar la persona", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Ya existe una persona con los mismos valores de nombre y descripción
                            Toast.makeText(AnadirParticipanteActivity.this, "Ya existe una persona con ese nombre", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Ocurrió un error al obtener los documentos
                        Toast.makeText(AnadirParticipanteActivity.this, "Error al obtener los documentos", Toast.LENGTH_SHORT).show();
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
            if (!imagen.equalsIgnoreCase("")) {
                Uri imagenUri = Uri.parse(imagen);
                UploadTask uploadTask = imagenRef.putFile(imagenUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {            // La imagen se subió exitosamente
                        Toast.makeText(AnadirParticipanteActivity.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();

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
                }).addOnFailureListener(e -> {
                    // Ocurrió un error al subir la imagen
                    Toast.makeText(AnadirParticipanteActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void botonImagenNoLogueado() {
        //Puesto provisional para probar cosas
        ivAnadirPlatoImagen.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.AuthActivity.class);
            rLauncherLogin.launch(intent);
        });


    }

    private void botonImagenLogueado() {
        //Puesto provisional para probar cosas
        ivAnadirPlatoImagen.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.HomeActivity.class);
            rLauncherLogin.launch(intent);
        });
    }


}