package mainActivity.menu.detalle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.dhaval2404.imagepicker.ImagePicker;
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
import mainActivity.crud.RecordarPlatoActivity;

public class DetallePersonaActivity extends AppCompatActivity implements PersonaDetalleAdapter.onItemClickListener {

    private Persona comensal, comensalBd;
    private ProgressBar progressBar;
    private ImageView ivLoginDetalle, ivFotoDetalle;
    private EditText etTitleDetalle, etDescripcionDetalle;
    private RecyclerView rvAnadirPlatoDetalle;
    private Dialog puElegirAccion;
    private Button btnUpCamara, btnUpGaleria;
    private static final int GALLERY_PERMISSION_CODE = 1001;
    private PersonaDetalleAdapter adapterDetalle;
    private ArrayList<Plato> platos;
    private ArrayList<Persona> nombreCompartir;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private boolean borrarPlato;
    private int platoPosicion;
    private boolean nuevoPlato;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private ActivityResultLauncher rLauncherPlatos;
    private ActivityResultLauncher rLauncherDetallePlato;
    private ActivityResultLauncher rLauncherRecordarPlato;
    private Button btnContinuarDetalle, btnBorrarDetalle;
    private String uriCapturada = "android.resource://com.tfg.marfol/"+R.drawable.logo_marfol_amarillo;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String email,nombreRestaurante;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_persona);

        //Recibe la lista de comensales para empezar a añadir
        Intent intent = getIntent();
        comensal = (Persona) intent.getSerializableExtra("comensalDetalle");
        nombreCompartir = (ArrayList<Persona>) intent.getSerializableExtra("arrayListComenComp");
        comensalBd = comensal;
        nombreRestaurante = intent.getStringExtra("nombreRestaurante");
        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Método que inserta la información a los comensales
        insertarComensal();

        //Launcher detallePlato se actualiza al recibir la modificación
        rLauncherDetallePlato = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        borrarPlato = data.getBooleanExtra("borrarPlato", false);
                        if (!borrarPlato) {
                            //Sustituye el plato de la posición elegida anteriormente ( EDITAR PLATO )
                            platos.set(platoPosicion, (Plato) data.getSerializableExtra("detallePlato"));
                            adapterDetalle.setResultsPlato(platos);
                        } else {
                            //Borra el plato de la posición elegida anteriormente ( BORRAR PLATO )
                            platos.remove(platoPosicion);
                            adapterDetalle.setResultsPlato(platos);
                        }
                    }
                }
        );

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

        //Botón que vuelve a participantes y además devuelve el comensal modificado
        btnContinuarDetalle.setOnClickListener(view -> {
            editarComensal();
        });

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
                    ivFotoDetalle.setBackground(null);
                    Glide.with(this)
                            .load(uriCapturada)
                            .circleCrop()
                            .into(ivFotoDetalle);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                puElegirAccion.dismiss();
            }
        });

        //Convocamos el PopUp para mostrar las acciones ( Galería, Cámara )
        ivFotoDetalle.setOnClickListener(view -> { puElegirAccion.show(); });

        //Añadimos onClick en el ImageView para activar la imagen
        btnUpCamara.setOnClickListener(view -> {
            // Solicitar permiso para acceder a la cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                //Si no tenemos los permisos los obtenemos
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                // Si ya se tienen los permisos, abrir la cámara
                abrirCamara();
            }
        });

        //Añadimos onClick en el ImageView para activar la imagen
        btnUpGaleria.setOnClickListener(view -> {
            // Solicitar permiso para acceder a la galería
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                // Si no tenemos los permisos, los solicitamos
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_CODE);
            } else {
                // Si ya se tienen los permisos, abrir la galería
                abrirGaleria();
            }
        });

        //Botón de borrar, llama a un método que a además de borrar, reordena los comensalID
        btnBorrarDetalle.setOnClickListener(view -> {
            borrarComensal();
        });

        //Launcher que da la opción a recordar platos o añadir nuevos
        rLauncherRecordarPlato = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        nuevoPlato = data.getBooleanExtra("nuevoPlato", false);
                        if (nuevoPlato) {
                            //Accedemos a la actividad de añadir plato
                            Intent intentRec = new Intent(this, AnadirPlatoActivity.class);
                            intentRec.putExtra("arrayListPlatos", platos);
                            intentRec.putExtra("arrayListComenComp", nombreCompartir);
                            intentRec.putExtra("comensalCode", comensal.getComensalCode());
                            rLauncherPlatos.launch(intentRec);
                        } else {
                            //recibe el plato que anteriormente se había utilizado
                            platos.add((Plato) data.getSerializableExtra("recordarNuevo"));
                            adapterDetalle.setResultsPlato(platos);
                        }
                    }
                }
        );

    }

    // Método para abrir la galería
    private void abrirGaleria() {
        //Librería que accede a la galería del dispositivo (OBLIGATORIO USAR LIBRERÍAS MIUI BLOQUEA LO DEMÁS)
        ImagePicker.with(this)
                .galleryOnly()
                .start();
    }

    //Método que accede a la galería sin que los permisos restrictivos MIUI afecten
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // La Uri de la imagen no será nula para RESULT_OK
            Uri uri = data.getData();
            if (uri != null) {
                Uri selectedImageUri = data.getData();
                uriCapturada = selectedImageUri.toString();

                //Cargar imagen seleccionada
                ivFotoDetalle.setBackground(null);
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .into(ivFotoDetalle);
            }
            puElegirAccion.dismiss();
        }
    }


    // Método para abrir la cámara
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
    }

    public void borrarComensal() {
        Intent borrarComensal = new Intent();
        borrarComensal.putExtra("borrarComensal", true);
        setResult(Activity.RESULT_OK, borrarComensal);
        finish();
    }

    public void editarComensal() {
        boolean esValidado = true;
        String nombre = String.valueOf(etTitleDetalle.getText());
        String descripcion = String.valueOf(etDescripcionDetalle.getText());

        //Comprueba si has añadido un nombre
        if (etTitleDetalle.getText().toString().length() == 0) {
            Toast.makeText(this, "Debe introducir un nombre", Toast.LENGTH_SHORT).show();
            esValidado = false;
        }
        //Comprueba si ha validado, añade la persona, envía al padre el ArrayList y cierra la actividad
        if (esValidado) {

            //Añado a la lista la persona creada
            Persona personaEditada = new Persona(comensal.getComensalCode(), nombre, descripcion, uriCapturada, platos, 0);
            Intent intentComensal = new Intent();
            intentComensal.putExtra("detalleComensal", personaEditada);
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
        if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si se conceden los permisos, abrir la galería
                abrirGaleria();
            } else {
                // Si se deniegan los permisos, mostrar un mensaje al usuario
                Toast.makeText(this, "Para acceder a la galería debe otorgar los permisos", Toast.LENGTH_SHORT).show();
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
        uriCapturada = comensal.getUrlImage();

        if (comensal.getUrlImage() != null && !comensal.getUrlImage().equalsIgnoreCase("")) {

            //Asignamos un color rojizo característico de la APP
            progressBar.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.redSLight),
                    PorterDuff.Mode.SRC_IN
            );

            Glide.with(this)
                    .load(comensal.getUrlImage())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .error(uriCapturada)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivFotoDetalle);
        } else {
            //Inserta Imagen photo
            ivFotoDetalle.setImageURI(Uri.parse("android.resource://com.tfg.marfol/" + R.drawable.camera));
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
        ivFotoDetalle = findViewById(R.id.ivFotoPersonaDetalle);
        etTitleDetalle = findViewById(R.id.etTitleDetallePersona);
        etDescripcionDetalle = findViewById(R.id.etDescripcionDetallePersona);
        rvAnadirPlatoDetalle = findViewById(R.id.rvAnadirPlatoDetalle);
        btnContinuarDetalle = findViewById(R.id.btnEditarDetalle);
        progressBar = findViewById(R.id.pgImagenDetalle);

        //Asigna IDs de los elementos del popup
        puElegirAccion = new Dialog(this);
        puElegirAccion.setContentView(R.layout.popup_accion);
        btnUpCamara = puElegirAccion.findViewById(R.id.btnCancelarPopup);
        btnUpGaleria = puElegirAccion.findViewById(R.id.btnConfirmarPopup);

    }

    @Override
    public void onItemClick(int position) {
        platoPosicion = position;
        //Si pulsas "Añadir Persona" ( 0 ), accederás a la actividad añadir persona
        if (position > 0) {
            Intent intentDetalle = new Intent(this, DetallePlatoActivity.class);
            intentDetalle.putExtra("platoDetalle", platos.get(position));
            intentDetalle.putExtra("arrayListComenComp", nombreCompartir);
            intentDetalle.putExtra("comensalCode", comensal.getComensalCode());
            rLauncherDetallePlato.launch(intentDetalle);
        } else {
            Intent intent = new Intent(this, RecordarPlatoActivity.class);
            intent.putExtra("arrayListPlatos", platos);
            intent.putExtra("arrayListComenComp", nombreCompartir);
            intent.putExtra("nombreRestaurante", nombreRestaurante);
            intent.putExtra("enviarRestaurante",true);
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

            UploadTask uploadTask = null;
            // Sube la imagen a Storage si tiene valor
            if (!imagen.equalsIgnoreCase("")) {
                Uri imagenUri = Uri.parse(imagen);
                uploadTask = imagenRef.putFile(imagenUri);

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
            }
        } else {
            // El ID de la persona es nulo o vacío, muestra un mensaje de error
            Toast.makeText(DetallePersonaActivity.this, "ID de persona inválido", Toast.LENGTH_SHORT).show();
        }
    }
}