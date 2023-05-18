package login;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tfg.marfol.R;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import mainActivity.MetodosGlobales;

public class EditarDatos extends AppCompatActivity {
    private EditText etNombreUsuario, etTelefonoUsuario;
    private Button btnGuardarBD;
    private ImageView ivPlatoAnadirP;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private StorageReference storageReference;
    private Uri uri;
    private ContentValues values;
    private OutputStream outputStream;
    private String imagen,rute_storage_photo,userNombre,userTelefono,email,uriCapturada = "", storage_path = "users/*", photo = "photo", download_uri;
    private StorageReference reference;
    private DocumentSnapshot document;
    private DocumentReference userRef;
    private Intent homeAv,cameraIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_datos);
        asignarId();
        if (MetodosGlobales.comprobarLogueadoEditar(EditarDatos.this, ivPlatoAnadirP)) {
            //este metodo va a firebase y carga los datos del usuario en esta clase
            cargarDatosDesdeBD(etNombreUsuario, etTelefonoUsuario);

            // Configuramos el botón de guardar
            btnGuardarBD.setOnClickListener(v -> actualizarDatos());
            // Registrar el launcher para la cámara
            camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Si la foto se toma correctamente, mostrar la vista previa en el ImageView
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    // Insertar la imagen en la galería y obtenemos la URI transformada en String para almacenar en la BD
                    values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "personaMarfol.jpg");
                    uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    try {
                        outputStream = getContentResolver().openOutputStream(uri);
                        photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                        //Obtenemos la ruta URI de la imagen seleccionada
                        uriCapturada = uri.toString();
                        Glide.with(EditarDatos.this).load(uriCapturada).circleCrop().into(ivPlatoAnadirP);
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
        }
    }
    private void asignarId() {
        // Obtenemos la instancia de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        // Obtenemos las referencias a los elementos del layout
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etTelefonoUsuario = findViewById(R.id.etTelefonoUsuario);
        ivPlatoAnadirP = findViewById(R.id.ivAnadirFotoPersona);
        btnGuardarBD = findViewById(R.id.btnGuardarBD);
        if(mAuth.getCurrentUser()!=null)email = mAuth.getCurrentUser().getEmail();
    }
    private void actualizarDatos() {
        //METERLO EN STORAGE
        rute_storage_photo = storage_path + "" + photo + "" + mAuth.getUid();
        reference = storageReference.child(rute_storage_photo);
        // Creamos el mapa con los datos a actualizar
        Map<String, Object> map = new HashMap<>();
        map.put("name", etNombreUsuario.getText().toString());
        map.put("phone", etTelefonoUsuario.getText().toString());
        // Verificamos si el documento del usuario ya existe en la base de datos
        db.collection("users").document(email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        document = task.getResult();
                        if (document.exists()) {
                            actualizarImagenBd(reference, Uri.parse(uriCapturada));
                            // Verificar si no se seleccionó una nueva imagen y conservar el valor existente
                            if (uriCapturada.isEmpty()) {
                                map.put("imagen", document.getString("imagen"));
                            } else {
                                map.put("imagen", uriCapturada);
                            }
                            // El documento del usuario ya existe, actualizamos sus datos
                            db.collection("users").document(email).update(map)
                                    .addOnSuccessListener(unused -> {
                                        // Mostramos el mensaje de actualizado
                                        Toast.makeText(EditarDatos.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                        homeAv = new Intent(EditarDatos.this, HomeActivity.class);
                                        startActivity(homeAv);
                                        // Crear el Intent con los datos a enviar
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Mostramos el mensaje de error
                                        Toast.makeText(EditarDatos.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // El documento del usuario no existe, creamos uno nuevo con sus datos
                            db.collection("users").document(email).set(map)
                                    .addOnSuccessListener(unused -> {
                                        // Mostramos el mensaje de actualizado
                                        Toast.makeText(EditarDatos.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                        // Crear el Intent con los datos a enviar
                                        Intent i = new Intent(EditarDatos.this, HomeActivity.class);
                                        finish();

                                    })
                                    .addOnFailureListener(e -> {
                                        // Mostramos el mensaje de error
                                        Toast.makeText(EditarDatos.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                    });
                        }

                    } else {
                        // Mostramos el mensaje de error
                        Toast.makeText(EditarDatos.this, "Error al verificar los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void abrirCamara() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
    }
    private void actualizarImagenBd(StorageReference reference, Uri uri) {
        reference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            uriTask.addOnSuccessListener(uri1 -> {
                download_uri = uri1.toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("imagen", download_uri);
                db.collection("users").document(email).set(map, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> Toast.makeText(EditarDatos.this, "¡Foto actualizada!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(EditarDatos.this, "Error al actualizar la foto", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(EditarDatos.this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show());
        });
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
    private void cargarDatosDesdeBD(EditText nombre, EditText telefono) {
        if (mAuth.getCurrentUser() != null && email != null && !email.isEmpty()) {
            userRef = db.collection("users").document(email);
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        userNombre = document.getString("name");
                        userTelefono = document.getString("phone");
                        imagen = document.getString("imagen");
                        if (userNombre != null) {
                            nombre.setText(userNombre);
                        }
                        if (userTelefono != null) {
                            telefono.setText(userTelefono);
                        }
                        // Si la imagen existe, mostrarla en el ImageView
                        if (imagen != null) {
                            Glide.with(EditarDatos.this).load(imagen).circleCrop().into(ivPlatoAnadirP);
                        } else {
                            // Manejar el caso en el que no haya imagen
                            Glide.with(EditarDatos.this).load(R.drawable.nologinimg).circleCrop().into(ivPlatoAnadirP);
                        }
                    }
                } else {
                    // Mostrar el mensaje de error
                    Toast.makeText(EditarDatos.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}



