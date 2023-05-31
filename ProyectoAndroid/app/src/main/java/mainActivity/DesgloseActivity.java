package mainActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tfg.marfol.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapters.DesgloseAdapter;
import entities.Persona;
import entities.Plato;

public class DesgloseActivity extends AppCompatActivity implements DesgloseAdapter.onItemClickListener {

    private TextView tvMessage1Popup, tvMessage2Popup;
    private EditText etNombrePopup;
    private Dialog puGuardarDesglose;
    private Button btnNoGuardarDesglose, btnConfirmarDesglose;
    private Button btnGuardarRestaurante;
    private RecyclerView rvPersonaDesglose;
    private TextView tvTitleDesglose;
    private ImageView ivDesgloseImagen;
    private DesgloseAdapter desgloseAdapter;
    private ArrayList<Persona> comensales, listaComensales;
    private ArrayList<Plato> platosABd;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ActivityResultLauncher rLauncherLogin;
    private String nombreRestaurante="", email, platoExistenteId, rutaImagen, imagenUrl,historial="";
    private DocumentReference restauranteRef;
    private CollectionReference platosRef;
    private CollectionReference historialRef;
    private Query query;
    private QuerySnapshot querySnapshot;
    private List<DocumentSnapshot> platosExistente;
    private Query platosQuery;
    private Intent intent;
    private boolean platoExistente;
    private StorageReference storageRef, imagenRef;
    private Uri imagenUri;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desglose);

        //Recibe la lista de comensales para empezar a añadir
        intent = getIntent();
        comensales = (ArrayList<Persona>) intent.getSerializableExtra("envioDesglose");
        nombreRestaurante = intent.getStringExtra("nombreRestaurante");
        //Método que asigna IDs a los elementos
        asignarId();
        if (nombreRestaurante != null) {
            etNombrePopup.setText(nombreRestaurante);
            etNombrePopup.setFocusable(false);
            Toast.makeText(this, nombreRestaurante, Toast.LENGTH_SHORT).show();
        }


        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Comprobar si el usuario está logueado
        comprobarLauncher();

        //Reparte los platos compartidos entre los comensales
        repartirCompartido();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Botón para guardar restaurante, añadimos el texto deseado al pop up
        btnGuardarRestaurante.setOnClickListener(view -> {
            //comprueba si el usuario está logueado
            if (currentUser != null) {
                //si el usuario está logueado te muestra el pop up por si le interesa guardar el restaurante
                tvMessage1Popup.setText(getString(R.string.popup_text1_desglose));
                tvMessage2Popup.setText(getString(R.string.popup_text2_desglose));
                puGuardarDesglose.show();
                //Método cancelar para no guardar en la bd
                btnNoGuardarDesglose.setOnClickListener(v1 -> {
                    Toast.makeText(this, "Gracias por utilizar marfol", Toast.LENGTH_SHORT).show();
                    puGuardarDesglose.dismiss();
                    finish();
                });
                //Guarda el restaurante en la BD :)
                btnConfirmarDesglose.setOnClickListener(v2 -> {
                    nombreRestaurante = String.valueOf(etNombrePopup.getText());
                    email = currentUser.getEmail();
                    //compruebo si el nombre del restaurante está vacío
                    if (!(nombreRestaurante).equalsIgnoreCase("")) {
                        copiarPlatosEnNuevoArray();
                        guardarRestaurante(nombreRestaurante, email);
                        Toast.makeText(this, "Se han guardado los platos", Toast.LENGTH_SHORT).show();
                        puGuardarDesglose.dismiss();
                        finish();
                    } else {
                        //si el nombre está vacío no va a dejar hacer nada
                        Toast.makeText(this, "Para guardar un restaurante necesitas un nombre", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                //si no estoy logueado no saldrá ningún pop up y se finalizará la actividad
                Toast.makeText(this, "Regístrate para guardar el restaurante", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, login.AuthActivity.class);
                rLauncherLogin.launch(intent);
            }
        });
        //launcher para volver con datos desde el auth/home dependiendo si estoy logueado o no
        rLauncherLogin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> comprobarLauncher());


    }

    private void guardarRestaurante(String nombreRestaurante, String correoUsuario) {
        // Consultar si ya existe un restaurante con el mismo nombre y usuario
        query = db.collection("restaurantes").whereEqualTo("nombreRestaurante", nombreRestaurante).whereEqualTo("usuarioId", correoUsuario);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                querySnapshot = task.getResult();
                if (querySnapshot.isEmpty()) {
                    // No existe un restaurante con el mismo nombre y usuario, se guarda uno nuevo
                    // Crear un nuevo documento en la colección "restaurantes"
                    restauranteRef = db.collection("restaurantes").document();

                    // Crear un mapa con los datos del restaurante
                    Map<String, Object> restauranteData = new HashMap<>();
                    restauranteData.put("nombreRestaurante", nombreRestaurante);
                    restauranteData.put("usuarioId", correoUsuario);

                    // Guardar el restaurante en la base de datos
                    restauranteRef.set(restauranteData).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Restaurante guardado correctamente", Toast.LENGTH_SHORT).show();
                        guardarPlatos(nombreRestaurante); // Llamar al método para guardar los platos
                        guardarHistorial(nombreRestaurante,email);// Llamar al método para guardar un historial de la transaccion
                        puGuardarDesglose.dismiss();
                        finish();
                    }).addOnFailureListener(e -> {
                        puGuardarDesglose.dismiss();
                        finish();
                    });
                } else {
                    guardarHistorial(nombreRestaurante,email);// Llamar al método para guardar un historial de la transaccion
                    guardarPlatos(nombreRestaurante); // Llamar al método para guardar los platos
                    puGuardarDesglose.dismiss();
                    finish();
                }
            } else {
                // Error al realizar la consulta
                Toast.makeText(this, "Error al consultar el restaurante existente", Toast.LENGTH_SHORT).show();
                puGuardarDesglose.dismiss();
                finish();
            }
        });
    }

    private void guardarHistorial(String nombreRestaurante,String email) {
            if (currentUser != null) {
                // Obtén la colección "historial" en Firestore
                historialRef = db.collection("historial");
                cargarHistorial();
                // Crea un objeto HashMap para almacenar los datos del nuevo historial
                Map<String, Object> nuevoHistorial = new HashMap<>();
                nuevoHistorial.put("historial", historial);
                nuevoHistorial.put("restaurante",nombreRestaurante);
                // Obtiene la fecha actual en el formato deseado ("dd/MM/yyyy HH:mm")
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String fechaActual = sdf.format(new Date());
                nuevoHistorial.put("fecha", fechaActual);

                nuevoHistorial.put("usuario", email);

                // Agrega el nuevo historial con un ID único generado automáticamente
                historialRef.add(nuevoHistorial)
                        .addOnSuccessListener(documentReference -> {
                            // Éxito en la operación de guardado
                        })
                        .addOnFailureListener(e -> {
                            // Error en la operación de guardado
                        });
            }
        }

    private void cargarHistorial() {
        for (int i = 0; i < listaComensales.size(); i++) {
            historial+=listaComensales.get(i).getNombre()+": "+listaComensales.get(i).getMonedero()+" € \n \n";
        }
    }





    private void guardarPlatos(String restauranteNombre) {
        platosRef = db.collection("platos");

        // Consultar los platos existentes del restaurante
        platosQuery = platosRef.whereEqualTo("restaurante", restauranteNombre).whereEqualTo("usuario", email);

        platosQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                querySnapshot = task.getResult();
                platosExistente = querySnapshot.getDocuments();

                // Guardar los nuevos platos y actualizar los existentes
                for (Plato plato : platosABd) {
                    platoExistente = false;
                    platoExistenteId = "";

                    // Verificar si el plato ya existe en la base de datos
                    for (DocumentSnapshot platoSnapshot : platosExistente) {
                        if (plato.getNombre().equals(platoSnapshot.getString("nombre"))) {
                            platoExistente = true;
                            platoExistenteId = platoSnapshot.getId();
                            break;
                        }
                    }

                    // Crear un mapa con los datos del plato
                    Map<String, Object> platoData = new HashMap<>();
                    platoData.put("nombre", plato.getNombre());
                    platoData.put("descripcion", plato.getDescripcion());
                    platoData.put("precio", plato.getPrecio());
                    platoData.put("imagen", plato.getUrlImage() == null ? "" : plato.getUrlImage());
                    platoData.put("restaurante", restauranteNombre);
                    platoData.put("usuario", currentUser.getEmail());

                    if (platoExistente) {
                        // Actualizar el plato existente
                        platosRef.document(platoExistenteId).set(platoData).addOnSuccessListener(aVoid -> {
                        }).addOnFailureListener(e -> {
                        });
                        // Subir la imagen del plato y actualizar los datos en Firestore
                        subirImagenPlato(platoExistenteId, plato.getUrlImage(), platoData);
                    } else {
                        // Guardar el nuevo plato
                        platosRef.add(platoData).addOnSuccessListener(documentReference -> {
                            // Subir la imagen del plato y actualizar los datos en Firestore
                            subirImagenPlato(documentReference.getId(), plato.getUrlImage(), platoData);
                        }).addOnFailureListener(e -> {
                        });
                    }
                }
            }
        });
    }

    private void subirImagenPlato(String platoId, String imagen, Map<String, Object> platoData) {
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

                // Obtiene la URL de descarga de la imagen
                imagenRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // La URL de descarga de la imagen está disponible
                    imagenUrl = uri.toString();
                    platoData.put("imagen", imagenUrl);

                    // Actualiza los datos del plato en Firestore
                    db.collection("platos").document(platoId).set(platoData).addOnSuccessListener(aVoid -> {
                        // Los datos del plato se actualizaron exitosamente en Firestore
                    }).addOnFailureListener(e -> {
                        // Ocurrió un error al actualizar los datos del plato en Firestore
                    });
                });
            }).addOnFailureListener(e -> {
            });
        }
    }


    private void comprobarLauncher() {
        if (MetodosGlobales.comprobarLogueado(this, ivDesgloseImagen)) {
            currentUser = mAuth.getCurrentUser();
            botonImagenLogueado();
        } else {
            Glide.with(this)
                    .load(R.drawable.nologinimg)
                    .circleCrop()
                    .into(ivDesgloseImagen);
            botonImagenNoLogueado();
        }
    }

    private void botonImagenLogueado() {
        //Puesto provisional para probar cosas
        ivDesgloseImagen.setOnClickListener(view -> {
            intent = new Intent(this, login.HomeActivity.class);
            rLauncherLogin.launch(intent);
        });
    }

    private void botonImagenNoLogueado() {
        //Puesto provisional para probar cosas
        ivDesgloseImagen.setOnClickListener(view -> {
            intent = new Intent(this, login.AuthActivity.class);
            rLauncherLogin.launch(intent);
        });
    }

    private void copiarPlatosEnNuevoArray() {
        //añade los platos
        platosABd.removeAll(platosABd);
        for (int a = 0; a < comensales.size(); a++) {
            for (int b = 0; b < comensales.get(a).getPlatos().size(); b++) {
                if (b > 0) {
                    platosABd.add(comensales.get(a).getPlatos().get(b));
                }
            }
        }
        //comprueba si existen platos con el mismo nombre y los borra, solo guarda el ultimo nombre
        for (int i = 0; i < platosABd.size(); i++) {
            String nombreActual = platosABd.get(i).getNombre();
            for (int j = i + 1; j < platosABd.size(); j++) {
                if (nombreActual.equals(platosABd.get(j).getNombre())) {
                    platosABd.remove(i);
                    i--;
                    break;
                }
            }
        }

    }


    //Método que prepara el recycler y el adaptador para su uso
    public void mostrarAdapter() {
        //Se debe borrar la primera posición ya que es el elemento de añadir y no tiene valores
        listaComensales = comensales;
        listaComensales.remove(0);
        //Prepara el Adapter para su uso
        rvPersonaDesglose.setLayoutManager(new LinearLayoutManager(this));
        desgloseAdapter = new DesgloseAdapter();
        rvPersonaDesglose.setAdapter(desgloseAdapter);
        desgloseAdapter.setmListener(this);
        //Añade el contenido al adapter, si está vacío el propio Adapter añade el " Añadir Persona "
        desgloseAdapter.setResultsPersona(listaComensales);
    }

    public void repartirCompartido() {
        double precioPlato;
        for (int i = 0; i < comensales.size(); i++) {
            for (int j = 0; j < comensales.get(i).getPlatos().size(); j++) {
                if (comensales.get(i).getPlatos().get(j).isCompartido()) {
                    comensales.get(i).getPlatos().get(j).getPersonasCompartir().add(comensales.get(i)); // ---
                    precioPlato = comensales.get(i).getPlatos().get(j).getPrecio() / (comensales.get(i).getPlatos().get(j).getPersonasCompartir().size());
                    for (int h = 0; h < comensales.get(i).getPlatos().get(j).getPersonasCompartir().size(); h++) {
                        for (int m = 0; m < comensales.size(); m++) {
                            if (comensales.get(m).getComensalCode() == comensales.get(i).getPlatos().get(j).getPersonasCompartir().get(h).getComensalCode()) {
                                comensales.get(m).sumarMonedero(precioPlato);
                            }
                        }
                    }
                }
            }
            comensales.get(i).asignarPrecio();
        }
    }

    public void asignarEfectos() {
        //Ajusta el tamaño de la imagen del login
        ivDesgloseImagen.setPadding(20, 20, 20, 20);

        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder), getResources().getColor(R.color.redTitle)};
        float[] positions = {0f, 0.2f};
        LinearGradient gradient = new LinearGradient(0, 0, 40, tvTitleDesglose.getPaint().getTextSize(), colors, positions, Shader.TileMode.REPEAT);
        tvTitleDesglose.getPaint().setShader(gradient);
        btnGuardarRestaurante.getPaint().setShader(gradient);
        btnConfirmarDesglose.getPaint().setShader(gradient);
        btnNoGuardarDesglose.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;
        tvTitleDesglose.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

    }

    public void asignarId() {
        btnGuardarRestaurante = findViewById(R.id.btnGuardarRestauranteDes);
        rvPersonaDesglose = findViewById(R.id.rvPersonaDesglose);
        tvTitleDesglose = findViewById(R.id.tvTitleDesglose);
        ivDesgloseImagen = findViewById(R.id.ivDesgloseImagen);
        //Asigna IDs de los elementos del popup
        puGuardarDesglose = new Dialog(this);
        puGuardarDesglose.setContentView(R.layout.popup_confirmacion_guardar);
        btnNoGuardarDesglose = puGuardarDesglose.findViewById(R.id.btnCancelarPopup);
        btnConfirmarDesglose = puGuardarDesglose.findViewById(R.id.btnConfirmarPopup);
        tvMessage1Popup = puGuardarDesglose.findViewById(R.id.tvMessage1Popup);
        tvMessage2Popup = puGuardarDesglose.findViewById(R.id.tvMessage2Popup);
        etNombrePopup = puGuardarDesglose.findViewById(R.id.etNombrePopup);
        platosABd = new ArrayList<Plato>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


    }

    @Override
    public void onItemClick(int position) {

    }
}
