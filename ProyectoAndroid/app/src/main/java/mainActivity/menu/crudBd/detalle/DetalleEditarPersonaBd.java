package mainActivity.menu.crudBd.detalle;

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

import com.bumptech.glide.Glide;
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
import login.EditarDatos;
import mainActivity.crud.AnadirPlatoActivity;
import mainActivity.detalle.DetallePersonaActivity;

public class DetalleEditarPersonaBd extends AppCompatActivity {
    private Persona comensalBd;
    private ImageView ivDetalleFotoEditarPersona;
    private EditText etDetalleNombreEditarPersona, etDetalleDescripcionEditarPersona;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private Button btnBorrarDetalleEditarPersona, btnEditarDetalleEditarPersona;
    private String uriCapturada = "",nombreNuevo,imagen,descripcionNueva,personaId,rutaImagen,imagenUrl;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String email;
    private Intent intent,cameraIntent;
    private CollectionReference personasRef;
    private DocumentSnapshot document;
    private StorageReference storageRef,imagenRef;
    private Uri imagenUri,uri;
    private UploadTask uploadTask;
    private ContentValues values;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_editar_persona_bd);
        asignarId();
        intent = getIntent();
        comensalBd = (Persona) intent.getSerializableExtra("comensalDetalle");
        mostrarDatos();
        //Añadimos onClick en el ImageView para activar la imagen
        ivDetalleFotoEditarPersona.setOnClickListener(view -> {
            // Solicitar permiso para acceder a la cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                //Si no tenemos los permisos los obtenemos
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                // Si ya se tienen los permisos, abrir la cámara
                abrirCamara();
            }
        });

        //Botón que vuelve a participantes y además devuelve el comensal modificado
        btnEditarDetalleEditarPersona.setOnClickListener(view -> {
            editarComensal();
            finish();
        });
        //Botón de borrar, llama a un método que a además de borrar, reordena los comensalID
        btnBorrarDetalleEditarPersona.setOnClickListener(view -> {
            borrarComensal();
        });

        // Registrar el launcher para la cámara
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {

                // Si la foto se toma correctamente, mostrar la vista previa en el ImageView
                Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                ivDetalleFotoEditarPersona.setImageBitmap(photo);

                // Insertar la imagen en la galería y obtenemos la URI transformada en String para almacenar en la BD
                values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "personaMarfol.jpg");
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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



    public void asignarId() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        email = currentUser.getEmail();
        etDetalleNombreEditarPersona = findViewById(R.id.etDetalleNombreEditarPersona);
        etDetalleDescripcionEditarPersona = findViewById(R.id.etDetalleDescripcionEditarPersona);
        btnBorrarDetalleEditarPersona = findViewById(R.id.btnBorrarDetalleEditarPersona);
        btnEditarDetalleEditarPersona = findViewById(R.id.btnEditarDetalleEditarPersona);
        ivDetalleFotoEditarPersona = findViewById(R.id.ivDetalleFotoEditarPersona);

    }
    private void mostrarDatos() {
        etDetalleNombreEditarPersona.setText(comensalBd.getNombre());
        etDetalleDescripcionEditarPersona.setText(comensalBd.getDescripcion());
        imagen = comensalBd.getUrlImage();
        if (imagen != null) {
            Glide.with(DetalleEditarPersonaBd.this).load(imagen).circleCrop().into(ivDetalleFotoEditarPersona);
        }else{
            Glide.with(DetalleEditarPersonaBd.this).load(R.drawable.nologinimg).circleCrop().into(ivDetalleFotoEditarPersona);
        }

    }

    // Método para abrir la cámara
    private void abrirCamara() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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


    private void editarComensal() {
        nombreNuevo = String.valueOf(etDetalleNombreEditarPersona.getText());
        descripcionNueva = String.valueOf(etDetalleDescripcionEditarPersona.getText());
        // Actualizar el comensal en la base de datos
        personasRef = db.collection("personas");
        personasRef.whereEqualTo("nombre", comensalBd.getNombre())
                .whereEqualTo("usuarioId", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        document = querySnapshot.getDocuments().get(0);
                        document.getReference().update("nombre", nombreNuevo, "descripcion", descripcionNueva)
                                .addOnSuccessListener(aVoid -> {
                                    // La actualización se realizó exitosamente
                                    Toast.makeText(DetalleEditarPersonaBd.this, "Los datos se actualizaron correctamente", Toast.LENGTH_SHORT).show();
                                    personaId = document.getReference().getId();
                                    subirImagenPersona(personaId, uriCapturada);
                                    // Aquí puedes llamar a un método para actualizar la vista con el comensal modificado
                                    // Por ejemplo: actualizarVista(comensalBd);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error al actualizar los datos
                                    Toast.makeText(DetalleEditarPersonaBd.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // No se encontró el comensal en la base de datos
                        Toast.makeText(DetalleEditarPersonaBd.this, "El comensal no existe en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al buscar el comensal en la base de datos
                    Toast.makeText(DetalleEditarPersonaBd.this, "Error al buscar el comensal en la base de datos", Toast.LENGTH_SHORT).show();
                });
    }


    private void borrarComensal() {
        // Eliminar el comensal de la base de datos
        personasRef = db.collection("personas");

        personasRef.whereEqualTo("nombre", comensalBd.getNombre())
                .whereEqualTo("usuarioId", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        document = querySnapshot.getDocuments().get(0);
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // La eliminación se realizó exitosamente
                                    Toast.makeText(DetalleEditarPersonaBd.this, "El comensal se eliminó correctamente", Toast.LENGTH_SHORT).show();

                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error al eliminar el comensal
                                    Toast.makeText(DetalleEditarPersonaBd.this, "Error al eliminar el comensal", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // No se encontró el comensal en la base de datos
                        Toast.makeText(DetalleEditarPersonaBd.this, "El comensal no existe en la base de datos", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al buscar el comensal en la base de datos
                    Toast.makeText(DetalleEditarPersonaBd.this, "Error al buscar el comensal en la base de datos", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
    private void subirImagenPersona(String personaId, String imagen) {
        if (personaId != null && !personaId.isEmpty()) {
            // Obtiene una referencia al Storage de Firebase
            storageRef = FirebaseStorage.getInstance().getReference();

            // Define una referencia a la imagen en Storage utilizando el ID de la persona
            rutaImagen = "personas/" + personaId + ".jpg";
            imagenRef = storageRef.child(rutaImagen);

            // Sube la imagen a Storage
            imagenUri = Uri.parse(imagen);
            uploadTask = imagenRef.putFile(imagenUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // La imagen se subió exitosamente
                // Obtiene la URL de descarga de la imagen
                imagenRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // La URL de descarga de la imagen está disponible
                    imagenUrl = uri.toString();

                    // Actualiza el campo "imagen" en Firestore con la URL de descarga de la imagen
                    db.collection("personas").document(personaId)
                            .update("imagen", imagenUrl)
                            .addOnSuccessListener(aVoid -> {
                                // La URL de la imagen se actualizó exitosamente en Firestore
                            })
                            .addOnFailureListener(e -> {
                                // Ocurrió un error al actualizar el campo "imagen" en Firestore
                            });
                });
            }).addOnFailureListener(e -> {
                // Ocurrió un error al subir la imagen
                Toast.makeText(DetalleEditarPersonaBd.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        }
    }
}





