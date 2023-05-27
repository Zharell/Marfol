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
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.Map;

import entities.Persona;
import entities.Plato;

public class DetalleEditarPlatosBd extends AppCompatActivity {
    private Plato platoBd;
    private ArrayList<Plato> platosTotales;
    private EditText etDetalleNombreEditarPlato, etDetalleDescripcionEditarPlato, etDetallePrecioEditarPlato;
    private Dialog puElegirAccion;
    private Button btnUpCamara, btnUpGaleria;
    private static final int GALLERY_PERMISSION_CODE = 1001;
    private TextView tvNombreDeRestaurante;
    private ImageView ivDetalleFotoEditarPlato;
    private Button btnEditarDetallePlatos, btnBorrarDetallePlatos;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private String uriCapturada = "", nombreNuevo, imagen, descripcionNueva, personaId, rutaImagen, imagenUrl, email,nombreExistente,restauranteExistente;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Intent intent, cameraIntent;
    private CollectionReference platosRef;
    private DocumentSnapshot document;
    private StorageReference storageRef, imagenRef;
    private Uri imagenUri, uri;
    private UploadTask uploadTask;
    private ContentValues values;
    private boolean comprobarNombre = true;
    private double precioNuevo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_editar_platos_bd);
        asignarId();
        intent = getIntent();
        platoBd = (Plato) intent.getSerializableExtra("platoDetalle");
        platosTotales = (ArrayList<Plato>) intent.getSerializableExtra("platosTotales");
        mostrarDatos();
        //Añadimos onClick en el ImageView para activar la imagen
        ivDetalleFotoEditarPlato.setOnClickListener(view -> {
            // Solicitar permiso para acceder a la cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                //Si no tenemos los permisos los obtenemos
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                // Si ya se tienen los permisos, abrir la cámara
                abrirCamara();
            }
        });
        //Botón que vuelve a platos y además devuelve el plato modificado
        btnEditarDetallePlatos.setOnClickListener(view -> {
            editarPlato();
            finish();
        });
        //Botón de borrar, llama a un método que a además de borrar
        btnBorrarDetallePlatos.setOnClickListener(view -> {
            borrarPlato();
            finish();
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

        //Convocamos el PopUp para mostrar las acciones ( Galería, Cámara )
        ivDetalleFotoEditarPlato.setOnClickListener(view -> { puElegirAccion.show(); });

        // Registrar el launcher para la cámara
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {

                // Si la foto se toma correctamente, mostrar la vista previa en el ImageView
                Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                ivDetalleFotoEditarPlato.setImageBitmap(photo);

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
                    ivDetalleFotoEditarPlato.setBackground(null);
                    Glide.with(this).load(uriCapturada).circleCrop().into(ivDetalleFotoEditarPlato);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void borrarPlato() {
        // Eliminar el plato de la base de datos
        platosRef = db.collection("platos");

        platosRef.whereEqualTo("nombre", platoBd.getNombre())
                .whereEqualTo("restaurante", platoBd.getRestaurante())
                .whereEqualTo("usuario", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        document = querySnapshot.getDocuments().get(0);
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // La eliminación se realizó exitosamente
                                    Toast.makeText(DetalleEditarPlatosBd.this, "El plato se eliminó correctamente", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error al eliminar el plato
                                    Toast.makeText(DetalleEditarPlatosBd.this, "Error al eliminar el plato", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al buscar el plato en la base de datos
                    Toast.makeText(DetalleEditarPlatosBd.this, "Error al buscar el comensal en la base de datos", Toast.LENGTH_SHORT).show();
                });
        finish();
    }

    private void editarPlato() {
        // Comprobación del nombre y el restaurante del plato a editar
        nombreNuevo = String.valueOf(etDetalleNombreEditarPlato.getText());
        descripcionNueva = String.valueOf(etDetalleDescripcionEditarPlato.getText());
        precioNuevo = Double.parseDouble((etDetallePrecioEditarPlato.getText().toString()));

        // Comprobación del nombre y el restaurante del plato a editar
        for (int i = 0; i < platosTotales.size(); i++) {
             nombreExistente = platosTotales.get(i).getNombre();
             restauranteExistente = platosTotales.get(i).getRestaurante();

            // Ignorar el elemento "nombre" enviado desde otra actividad
            if (!nombreExistente.equalsIgnoreCase(platoBd.getNombre()) ||
                    !restauranteExistente.equalsIgnoreCase(platoBd.getRestaurante())) {
                // Verificar si el nombre y el restaurante coinciden con otro plato
                if (nombreNuevo.equalsIgnoreCase(nombreExistente) &&
                        restauranteExistente.equalsIgnoreCase(platoBd.getRestaurante())) {
                    comprobarNombre = false;
                    break;
                }
            }
        }

        if (nombreNuevo.equalsIgnoreCase("")) {
            // El campo de texto está vacío, mostrar un mensaje de error
            Toast.makeText(DetalleEditarPlatosBd.this, "El campo nombre no puede estar vacío, no se realizaron los cambios.", Toast.LENGTH_SHORT).show();
            return;
        }
        platosRef = db.collection("platos");
        platosRef.whereEqualTo("nombre", platoBd.getNombre())
                .whereEqualTo("restaurante", platoBd.getRestaurante())
                .whereEqualTo("usuario", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        document = querySnapshot.getDocuments().get(0);
                        if (comprobarNombre) {
                            document.getReference().update("nombre", nombreNuevo, "descripcion", descripcionNueva,"precio",precioNuevo)
                                    .addOnSuccessListener(aVoid -> {
                                        // La actualización se realizó exitosamente
                                        Toast.makeText(DetalleEditarPlatosBd.this, "Los datos se actualizaron correctamente", Toast.LENGTH_SHORT).show();
                                        personaId = document.getReference().getId();
                                        subirImagenPlato(personaId, uriCapturada);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Ocurrió un error al actualizar los datos
                                        Toast.makeText(DetalleEditarPlatosBd.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(DetalleEditarPlatosBd.this, "Ya existe una persona con ese nombre, no se realizaron los cambios.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al buscar el comensal en la base de datos
                    Toast.makeText(DetalleEditarPlatosBd.this, "Error al buscar el comensal en la base de datos", Toast.LENGTH_SHORT).show();
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

    private void abrirCamara() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
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
                ivDetalleFotoEditarPlato.setBackground(null);
                Glide.with(this).load(selectedImageUri).circleCrop().into(ivDetalleFotoEditarPlato);
            }
            puElegirAccion.dismiss();
        }
    }

    private void asignarId() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        email = currentUser.getEmail();
        etDetalleNombreEditarPlato = findViewById(R.id.etDetalleNombreEditarPlato);
        etDetalleDescripcionEditarPlato = findViewById(R.id.etDetalleDescripcionEditarPlato);
        etDetallePrecioEditarPlato = findViewById(R.id.etDetallePrecioEditarPlato);
        tvNombreDeRestaurante = findViewById(R.id.tvNombreDeRestaurante);
        btnBorrarDetallePlatos = findViewById(R.id.btnBorrarDetallePlatos);
        btnEditarDetallePlatos = findViewById(R.id.btnEditarDetallePlatos);
        ivDetalleFotoEditarPlato = findViewById(R.id.ivDetalleFotoEditarPlato);

        //Asigna IDs de los elementos del popup
        puElegirAccion = new Dialog(this);
        puElegirAccion.setContentView(R.layout.popup_accion);
        btnUpCamara = puElegirAccion.findViewById(R.id.btnCancelarPopup);
        btnUpGaleria = puElegirAccion.findViewById(R.id.btnConfirmarPopup);

    }

    private void mostrarDatos() {
        etDetalleNombreEditarPlato.setText(platoBd.getNombre());
        etDetalleDescripcionEditarPlato.setText(platoBd.getDescripcion());
        etDetallePrecioEditarPlato.setText(String.valueOf(platoBd.getPrecio()));
        tvNombreDeRestaurante.setText(platoBd.getRestaurante());
        imagen = platoBd.getUrlImage();
        if (imagen != null && !imagen.equalsIgnoreCase("")) {
            ivDetalleFotoEditarPlato.setBackground(null);
            Glide.with(DetalleEditarPlatosBd.this).load(imagen).circleCrop().into(ivDetalleFotoEditarPlato);
        } else {
            Glide.with(DetalleEditarPlatosBd.this).load(R.drawable.camera).circleCrop().into(ivDetalleFotoEditarPlato);
        }
    }

    private void subirImagenPlato(String platoId, String imagen) {
        if (platoId != null && !platoId.isEmpty()) {
            // Obtén una referencia al Storage de Firebase
            storageRef = FirebaseStorage.getInstance().getReference();

            // Define una referencia a la imagen en Storage utilizando el ID del plato
            rutaImagen = "platos/" + platoId + ".jpg";
            imagenRef = storageRef.child(rutaImagen);

            // Sube la imagen a Storage
            imagenUri = Uri.parse(imagen);
            uploadTask = imagenRef.putFile(imagenUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // La imagen se subió exitosamente
                Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();

                // Obtiene la URL de descarga de la imagen
                imagenRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // La URL de descarga de la imagen está disponible
                    imagenUrl = uri.toString();

                    // Actualiza los datos del plato en Firestore
                    db.collection("platos").document(platoId)
                            .update("imagen", imagenUrl)
                            .addOnSuccessListener(aVoid -> {
                                // Los datos del plato se actualizaron exitosamente en Firestore
                            })
                            .addOnFailureListener(e -> {
                                // Ocurrió un error al actualizar los datos del plato en Firestore
                            });
                });
            }).addOnFailureListener(e -> {
                // Ocurrió un error al subir la imagen
                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        }
    }
}