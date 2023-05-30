package mainActivity.menu.crudBd.detalle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tfg.marfol.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import entities.Persona;

public class DetalleEditarPersonaBd extends AppCompatActivity {
    private Persona comensalBd;
    private ArrayList<Persona> comensalesTotales;
    private ImageView ivDetalleFotoEditarPersona;
    private EditText etDetalleNombreEditarPersona, etDetalleDescripcionEditarPersona;
    private Dialog puElegirAccion;
    private Button btnUpCamara, btnUpGaleria;
    private static final int GALLERY_PERMISSION_CODE = 1001;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private Button btnBorrarDetalleEditarPersona, btnEditarDetalleEditarPersona;
    private String uriCapturada = "", nombreNuevo, imagen, descripcionNueva, personaId, rutaImagen, imagenUrl, email;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Intent intent, cameraIntent;
    private CollectionReference personasRef;
    private DocumentSnapshot document;
    private StorageReference storageRef, imagenRef;
    private Uri imagenUri, uri;
    private UploadTask uploadTask;
    private ContentValues values;
    private boolean comprobarNombre = true;
    private ProgressDialog progressDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_editar_persona_bd);
        asignarId();
        intent = getIntent();
        comensalBd = (Persona) intent.getSerializableExtra("comensalDetalle");
        comensalesTotales = (ArrayList<Persona>) intent.getSerializableExtra("comensalesTotales");
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

        //Botón que vuelve a comensales y además devuelve el comensal modificado
        btnEditarDetalleEditarPersona.setOnClickListener(view -> {
            editarComensal();
            progressDialog = ProgressDialog.show(this, "", "Actualización en curso...", true);
            handler = new Handler();
            handler.postDelayed(() -> {
                // Quitar el ProgressDialog después de 500 milisegundos adicionales
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finish();
            }, 2000);
        });

        //Botón de borrar, llama a un método que a además de borrar
        btnBorrarDetalleEditarPersona.setOnClickListener(view -> {
            borrarComensal();
            progressDialog = ProgressDialog.show(this, "", "Borrado en curso...", true);
            handler = new Handler();
            handler.postDelayed(() -> {
                // Quitar el ProgressDialog después de 500 milisegundos adicionales
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finish();
            }, 2000);


        });

        //Convocamos el PopUp para mostrar las acciones ( Galería, Cámara )
        ivDetalleFotoEditarPersona.setOnClickListener(view -> {
            puElegirAccion.show();
        });

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
                    ivDetalleFotoEditarPersona.setBackground(null);
                    Glide.with(this)
                            .load(uriCapturada)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop()
                            .into(ivDetalleFotoEditarPersona);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    // Método para abrir la galería
    private void abrirGaleria() {
        //Librería que accede a la galería del dispositivo (OBLIGATORIO USAR LIBRERÍAS MIUI BLOQUEA LO DEMÁS)
        ImagePicker.with(this)
                .galleryOnly()
                .start();
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
                ivDetalleFotoEditarPersona.setBackground(null);
                Glide.with(this)
                        .load(selectedImageUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(ivDetalleFotoEditarPersona);
            }
            puElegirAccion.dismiss();
        }
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

        //Asigna IDs de los elementos del popup
        puElegirAccion = new Dialog(this);
        puElegirAccion.setContentView(R.layout.popup_accion);
        btnUpCamara = puElegirAccion.findViewById(R.id.btnCancelarPopup);
        btnUpGaleria = puElegirAccion.findViewById(R.id.btnConfirmarPopup);

    }

    private void mostrarDatos() {
        etDetalleNombreEditarPersona.setText(comensalBd.getNombre());
        etDetalleDescripcionEditarPersona.setText(comensalBd.getDescripcion());
        imagen = comensalBd.getUrlImage();
        if (imagen != null && !imagen.equalsIgnoreCase("")) {
            ivDetalleFotoEditarPersona.setBackground(null);
            Glide.with(DetalleEditarPersonaBd.this)
                    .load(imagen)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(ivDetalleFotoEditarPersona);

        } else {
            Glide.with(DetalleEditarPersonaBd.this)
                    .load(R.drawable.camera)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(ivDetalleFotoEditarPersona);
        }

    }

    // Método para abrir la cámara
    private void abrirCamara() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
    }

    private void editarComensal() {
        nombreNuevo = String.valueOf(etDetalleNombreEditarPersona.getText());
        descripcionNueva = String.valueOf(etDetalleDescripcionEditarPersona.getText());
        // Actualizar el comensal en la base de datos
        for (int i = 0; i < comensalesTotales.size(); i++) {
            if (nombreNuevo.equalsIgnoreCase(comensalesTotales.get(i).getNombre())) {
                // Ignorar el elemento "nombre" enviado desde otra actividad
                if (!comensalesTotales.get(i).getNombre().equalsIgnoreCase(comensalBd.getNombre())) {
                    comprobarNombre = false;
                    break;
                }
            }
        }
        if (nombreNuevo.equalsIgnoreCase("")) {
            // El campo de texto está vacío, mostrar un mensaje de error
            Toast.makeText(DetalleEditarPersonaBd.this, "El campo nombre no puede estar vacío, no se realizaron los cambios.", Toast.LENGTH_SHORT).show();
            return;
        }
        personasRef = db.collection("personas");
        personasRef.whereEqualTo("nombre", comensalBd.getNombre())
                .whereEqualTo("usuarioId", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        document = querySnapshot.getDocuments().get(0);
                        if (comprobarNombre) {
                            document.getReference().update("nombre", nombreNuevo, "descripcion", descripcionNueva)
                                    .addOnSuccessListener(aVoid -> {
                                        // La actualización se realizó exitosamente
                                        personaId = document.getReference().getId();
                                        subirImagenPersona(personaId, uriCapturada);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Ocurrió un error al actualizar los datos
                                        Toast.makeText(DetalleEditarPersonaBd.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(DetalleEditarPersonaBd.this, "Ya existe una persona con ese nombre, no se realizaron los cambios.", Toast.LENGTH_SHORT).show();
                        }
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
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error al eliminar el comensal
                                    Toast.makeText(DetalleEditarPersonaBd.this, "Error al eliminar el comensal", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al buscar el comensal en la base de datos
                    Toast.makeText(DetalleEditarPersonaBd.this, "Error al buscar el comensal en la base de datos", Toast.LENGTH_SHORT).show();
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
            });
        }
    }
}





