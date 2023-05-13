package login;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tfg.marfol.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import mainActivity.IndexActivity;


public class EditarDatos extends AppCompatActivity {
    private EditText etNombreUsuario, etTelefonoUsuario;
    private Button btnGuardarBD;
    private FirebaseFirestore db;

    private String email, provider;
    private ImageView ivPlatoAnadirP;
    private ActivityResultLauncher rLauncherEditar;
    private ActivityResultLauncher <Intent> camaraLauncher;
    private FirebaseAuth mAuth;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private String uriCapturada="",storage_path="users/*",photo="photo";

    private StorageReference storageReference;
    private static final int COD_SEL_IMAGE = 400;
    private static final int COD_SEL_STORAGE = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_datos);

        // Obtenemos la instancia de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Obtenemos las referencias a los elementos del layout
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etTelefonoUsuario = findViewById(R.id.etTelefonoUsuario);
        ivPlatoAnadirP = findViewById(R.id.ivAnadirFotoPersona);
        btnGuardarBD = findViewById(R.id.btnGuardarBD);
        Bundle extras = getIntent().getExtras();
        String name = extras.getString("name");
        String phone = extras.getString("phone");
        etNombreUsuario.setText(name);
        etTelefonoUsuario.setText(phone);

        // Obtenemos los datos del usuario
        SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        email = prefAux.getString("email", "");
        provider = prefAux.getString("provider", "");
        imagenDeBd();

        // Configuramos el botón de guardar
        btnGuardarBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarDatos();
            }
        });

        // Registrar el launcher para la cámara
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {

                // Si la foto se toma correctamente, mostrar la vista previa en el ImageView
                Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                // Insertar la imagen en la galería y obtenemos la URI transformada en String para almacenar en la BD
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "personaMarfol.jpg");
                Uri uri = getContentResolver(). insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try {

                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    //Obtenemos la ruta URI de la imagen seleccionada
                    uriCapturada = uri.toString();
                    Glide.with(EditarDatos.this).load(uriCapturada).into(ivPlatoAnadirP);
                    Log.d("AAAAAAAAAAAAAA",uriCapturada);

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

    }
    private void imagenDeBd(){
        // Realizar consulta al documento del usuario
        if (email != null && !email.isEmpty()) {
            db.collection("users").document(email).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // El documento del usuario existe
                                    String imagen = document.getString("imagen");
                                    // Si la imagen existe, mostrarla en el ImageView
                                    if (imagen != null) {
                                        Glide.with(EditarDatos.this).load(imagen).into(ivPlatoAnadirP);
                                    }
                                } else {
                                    // El documento del usuario no existe
                                    // Manejar el caso según tus necesidades
                                    Glide.with(EditarDatos.this).load("").into(ivPlatoAnadirP);
                                }
                            } else {
                                // Mostrar el mensaje de error
                                Toast.makeText(EditarDatos.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // Mostrar el mensaje de error o manejar el caso cuando email es nulo o vacío
        }

    }
    private void actualizarDatos() {
        //METERLO EN STORAGE
        String rute_storage_photo = storage_path+""+photo+""+mAuth.getUid();
        StorageReference reference = storageReference.child(rute_storage_photo);
        // Creamos el mapa con los datos a actualizar
        Map<String, Object> map = new HashMap<>();
        map.put("provider", provider);
        map.put("name", etNombreUsuario.getText().toString());
        map.put("phone", etTelefonoUsuario.getText().toString());

        // Verificamos si el documento del usuario ya existe en la base de datos
        db.collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                imagenEnBd(reference,Uri.parse(uriCapturada));

                                // Verificar si no se seleccionó una nueva imagen y conservar el valor existente
                                if (uriCapturada.isEmpty()) {
                                    map.put("imagen", document.getString("imagen"));
                                } else {
                                    map.put("imagen", uriCapturada);
                                }

                                // El documento del usuario ya existe, actualizamos sus datos
                                db.collection("users").document(email).update(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Mostramos el mensaje de actualizado
                                                Toast.makeText(EditarDatos.this, "Datos actualizados", Toast.LENGTH_SHORT).show();

                                                // Crear el Intent con los datos a enviar
                                                Intent resultIntent = new Intent();
                                                resultIntent.putExtra("name", etNombreUsuario.getText().toString());
                                                resultIntent.putExtra("phone", etTelefonoUsuario.getText().toString());
                                                if (uriCapturada.isEmpty()) {
                                                    resultIntent.putExtra("img", document.getString("imagen"));
                                                } else {
                                                    resultIntent.putExtra("img", uriCapturada);
                                                }

                                                // Establecer el resultado y finalizar la actividad
                                                setResult(RESULT_OK, resultIntent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Mostramos el mensaje de error
                                                Toast.makeText(EditarDatos.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // El documento del usuario no existe, creamos uno nuevo con sus datos
                                db.collection("users").document(email).set(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Mostramos el mensaje de actualizado
                                                Toast.makeText(EditarDatos.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                                // Crear el Intent con los datos a enviar
                                                Intent resultIntent = new Intent();
                                                resultIntent.putExtra("name", etNombreUsuario.getText().toString());
                                                resultIntent.putExtra("phone", etTelefonoUsuario.getText().toString());
                                                resultIntent.putExtra("imagen", uriCapturada);
                                                // Establecer el resultado y finalizar la actividad
                                                setResult(RESULT_OK, resultIntent);
                                                finish();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Mostramos el mensaje de error
                                                Toast.makeText(EditarDatos.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                        } else {
                            // Mostramos el mensaje de error
                            Toast.makeText(EditarDatos.this, "Error al verificar los datos del usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
    }
    private void imagenEnBd(StorageReference reference, Uri uri) {
        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String download_uri = uri.toString();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imagen", download_uri);
                        db.collection("users").document(email).set(map, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EditarDatos.this, "¡Foto actualizada!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditarDatos.this, "Error al actualizar la foto", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditarDatos.this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
}



