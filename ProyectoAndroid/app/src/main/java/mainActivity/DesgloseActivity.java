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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.ArrayList;
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
    private ImageView ivMenuDesglose, ivDesgloseImagen;
    private DesgloseAdapter desgloseAdapter;

    private ArrayList<Persona> comensales, listaComensales;
    private ArrayList<Plato> platosABd;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Intent homeIntent,authIntent;
    private ActivityResultLauncher rLauncherLogin;
    private String nombreRestaurante,emailUsuario,restauranteId,platoExistenteId, rutaImagen,imagenUrl;
    private DocumentReference restauranteRef;
    private CollectionReference platosRef;
    private Query query;
    private QuerySnapshot querySnapshot;
    private QuerySnapshot platosSnapshot;
    private DocumentSnapshot restauranteSnapshot;
    private List<DocumentSnapshot> platosExistente;
    private Query platosQuery;
    private Intent intent;
    private boolean platoExistente;
    private StorageReference storageRef,imagenRef;
    private Uri imagenUri ;
    private UploadTask uploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desglose);

        //Recibe la lista de comensales para empezar a añadir
        intent = getIntent();
        comensales = (ArrayList<Persona>) intent.getSerializableExtra("envioDesglose");

        //Método que asigna IDs a los elementos
        asignarId();

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
                    finish();
                });
                //Guarda el restaurante en la BD :)
                btnConfirmarDesglose.setOnClickListener(v2 -> {
                    nombreRestaurante = String.valueOf(etNombrePopup.getText());
                    emailUsuario = currentUser.getEmail();
                    //compruebo si el nombre del restaurante está vacío
                    if (!(nombreRestaurante).equalsIgnoreCase("")) {
                        copiarPlatosEnNuevoArray();
                        guardarRestaurante(nombreRestaurante,emailUsuario);
                        Toast.makeText(this, "Se han guardado los platos", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        //si el nombre está vacío no va a dejar hacer nada
                        Toast.makeText(this, "Para guardar un restaurante necesitas un nombre", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                //si no estoy logueado no saldrá ningún pop up y se finalizará la actividad
                Toast.makeText(this, "Gracias por utilizar marfol", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //launcher para volver con datos desde el auth/home dependiendo si estoy logueado o no
        rLauncherLogin = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> comprobarLauncher()
        );


    }

    private void guardarRestaurante(String nombreRestaurante, String correoUsuario) {
        // Consultar si ya existe un restaurante con el mismo nombre y usuario
        query = db.collection("restaurantes")
                .whereEqualTo("nombreRestaurante", nombreRestaurante)
                .whereEqualTo("usuarioId", correoUsuario);

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
                    restauranteRef.set(restauranteData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Restaurante guardado correctamente", Toast.LENGTH_SHORT).show();
                                guardarPlatos(nombreRestaurante); // Llamar al método para guardar los platos
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al guardar el restaurante", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                } else {
                    // Ya existe un restaurante con el mismo nombre y usuario
                    restauranteSnapshot = querySnapshot.getDocuments().get(0);
                    Toast.makeText(this, "Ya existe un restaurante con el mismo nombre y usuario", Toast.LENGTH_SHORT).show();
                    guardarPlatos(nombreRestaurante); // Llamar al método para guardar los platos
                    finish();
                }
            } else {
                // Error al realizar la consulta
                Toast.makeText(this, "Error al consultar el restaurante existente", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void guardarPlatos(String restauranteNombre) {
        platosRef = db.collection("platos");

        // Consultar los platos existentes del restaurante
        platosQuery = platosRef
                .whereEqualTo("restaurante", restauranteNombre)
                .whereEqualTo("usuario", currentUser.getEmail());

        platosQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                platosSnapshot = task.getResult();
                platosExistente = platosSnapshot.getDocuments();

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
                        platosRef.document(platoExistenteId)
                                .set(platoData)
                                .addOnSuccessListener(aVoid -> {
                                    // Log.d(TAG, "Plato actualizado: " + platoExistenteId);
                                })
                                .addOnFailureListener(e -> {
                                    // Log.e(TAG, "Error al actualizar el plato", e);
                                });

                        // Subir la imagen del plato y actualizar los datos en Firestore
                        subirImagenPlato(platoExistenteId, plato.getUrlImage(), platoData);
                    } else {
                        // Guardar el nuevo plato
                        platosRef.add(platoData)
                                .addOnSuccessListener(documentReference -> {
                                    // Log.d(TAG, "Plato guardado: " + documentReference.getId());

                                    // Subir la imagen del plato y actualizar los datos en Firestore
                                    subirImagenPlato(documentReference.getId(), plato.getUrlImage(), platoData);
                                })
                                .addOnFailureListener(e -> {
                                    // Log.e(TAG, "Error al guardar el plato", e);
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
                // La imagen se subió exitosamente
                Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();

                // Obtiene la URL de descarga de la imagen
                imagenRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // La URL de descarga de la imagen está disponible
                    imagenUrl = uri.toString();
                    platoData.put("imagen", imagenUrl);

                    // Actualiza los datos del plato en Firestore
                    db.collection("platos").document(platoId)
                            .set(platoData)
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






    private void comprobarLauncher(){
        if(MetodosGlobales.comprobarLogueado(this,ivDesgloseImagen)){
            currentUser = mAuth.getCurrentUser();
            botonImagenLogueado();
        }else{
            Glide.with(this).load(R.drawable.nologinimg).into(ivDesgloseImagen);
            botonImagenNoLogueado();
        }
    }

    private void botonImagenLogueado() {
        //Puesto provisional para probar cosas
        ivDesgloseImagen.setOnClickListener(view -> {
            homeIntent = new Intent(this, login.HomeActivity.class);
            rLauncherLogin.launch(homeIntent);
        });
    }

    private void botonImagenNoLogueado() {
        //Puesto provisional para probar cosas
        ivDesgloseImagen.setOnClickListener(view -> {
            authIntent = new Intent(this, login.AuthActivity.class);
            rLauncherLogin.launch(authIntent);
        });
    }

    private void copiarPlatosEnNuevoArray() {
        platosABd.removeAll(platosABd);
        for (int a = 0; a < comensales.size(); a++) {
            for (int b = 0; b < comensales.get(a).getPlatos().size(); b++) {
                if (b > 0) {
                    platosABd.add(comensales.get(a).getPlatos().get(b));
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
        int[] colors = {getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };
        float[] positions = {0f, 0.2f};
        LinearGradient gradient = new LinearGradient(0, 0, 40,
                tvTitleDesglose.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);
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
        ivMenuDesglose = findViewById(R.id.ivMenuDesglose);
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
