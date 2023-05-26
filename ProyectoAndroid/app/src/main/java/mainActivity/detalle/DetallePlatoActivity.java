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
import android.app.Dialog;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tfg.marfol.R;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import adapters.ListaCompartirAdapter;
import adapters.PersonaCompartirAdapter;
import adapters.PersonaDetalleAdapter;
import entities.Persona;
import entities.Plato;

public class DetallePlatoActivity extends AppCompatActivity implements PersonaCompartirAdapter.onItemClickListener {

    private Switch swCompartirPlato;
    private ImageView ivFotoDetalle;
    private ActivityResultLauncher rLauncherComp;
    private TextView tvListaEditarPlato;
    private EditText etTitleDetalle, etDescripcionDetalle, etPrecioDetalle;
    private Dialog puElegirAccion;
    private ActivityResultLauncher<Intent> galeriaLauncher;
    private Button btnUpCamara, btnUpGaleria;
    private static final int GALLERY_PERMISSION_CODE = 1001;
    private RecyclerView rvAnadirPlatoDetalle;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private int personaCode;
    private ArrayList<Persona> noRepComList;
    private ArrayList<Persona> nombreCompartir;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private PersonaCompartirAdapter anadirPAdapter;
    private boolean esCompartido;
    private Button btnEditarDetalle, btnBorrarDetalle;
    private String uriCapturada = "";
    private Plato plato;
    private Intent intent;
    private String nombre,descripcion;
    private double precio;
    private Plato platoEditado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_plato);

        //Recibe la lista de comensales para empezar a añadir
        intent = getIntent();
        plato = (Plato) intent.getSerializableExtra("platoDetalle");
        nombreCompartir = (ArrayList<Persona>) intent.getSerializableExtra("arrayListComenComp");
        personaCode = intent.getIntExtra("comensalCode", 0);
        esCompartido = plato.isCompartido();

        for (Persona e : nombreCompartir) {
            Toast.makeText(this, String.valueOf(e.getNombre()), Toast.LENGTH_SHORT).show();
        }

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Método que inserta la información a los comensales
        insertarComensal();

        //Botón de borrar, envía a la actividad padre un boolean indicando si debe borrar
        btnBorrarDetalle.setOnClickListener(view -> {
            borrarPlato();
        });

        //Botón de editar, envía un nuevo plato tras ser validado y sustituye el antiguo
        btnEditarDetalle.setOnClickListener(view -> {
            editarPlato();
        });

        //Comprueba si el switch compartir está activo o no para mostrar su información
        swCompartirPlato.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                tvListaEditarPlato.setVisibility(View.VISIBLE);
                rvAnadirPlatoDetalle.setVisibility(View.VISIBLE);
                esCompartido = true;
            } else {
                tvListaEditarPlato.setVisibility(View.INVISIBLE);
                rvAnadirPlatoDetalle.setVisibility(View.INVISIBLE);
                esCompartido = false;
            }
        });

        //Laucher Result recibe las personas de la listas que van a compartir
        rLauncherComp = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Método de cálculo aquí también
                        intent = result.getData();
                        plato.getPersonasCompartir().add((Persona) intent.getSerializableExtra("personaCompartir"));
                        anadirPAdapter.setResultsPersonaCom(plato.getPersonasCompartir());
                    }
                }
        );

        // Registrar el launcher para la galería
        galeriaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    uriCapturada = selectedImageUri.toString();

                    //Cargar imagen seleccionada
                    ivFotoDetalle.setBackground(null);
                    ivFotoDetalle.setImageURI(selectedImageUri);
                }
                puElegirAccion.dismiss();
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
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    //Obtenemos la ruta URI de la imagen seleccionada
                    uriCapturada = uri.toString();
                    ivFotoDetalle.setBackground(null);
                    Glide.with(this).load(uriCapturada).into(ivFotoDetalle);

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

    }

    public void editarPlato() {
        boolean esValidado = true;
        nombre = String.valueOf(etTitleDetalle.getText());
        descripcion = String.valueOf(etDescripcionDetalle.getText());

        //Comprueba si el editText está vacío, de estarlo el programa lo entiende como un 0, además, remplaza <,> por <.> para evitar errores
        precio = etPrecioDetalle.getText().toString().equalsIgnoreCase("") ? 0 : Double.parseDouble(etPrecioDetalle.getText().toString().replace(",", "."));

        //Comprueba si has añadido un nombre
        if (etTitleDetalle.getText().toString().length() == 0) {
            Toast.makeText(this, "Debe introducir un nombre", Toast.LENGTH_SHORT).show();
            esValidado = false;
        }

        if (precio <= 0) {
            Toast.makeText(this, "Debe introducir un precio mayor a 0", Toast.LENGTH_SHORT).show();
            esValidado = false;
        }

        //Comprueba si ha validado, añade la persona, envía al padre el ArrayList de comensales, platos y cierra la actividad
        if (esValidado) {
            //Dependiendo si es compartido o no
            if (esCompartido) {
                //Eliminamos la posición 0 ya que es un botón
                plato.getPersonasCompartir().remove(0);

                //Hacemos una última comprobación si ha añadido personas en compartir, si no ha añadido se añade un plato NO compartido
                if (plato.getPersonasCompartir().size() > 0) {
                    //Añado a la lista la persona creada                                           - lista de personas que van a compartir el plato
                    platoEditado = new Plato(nombre, descripcion, precio, precio, uriCapturada, esCompartido, plato.getPersonasCompartir());
                } else {
                    //Añado a la lista la persona creada
                    esCompartido = false;                                                         //Solo se crea el plato si en el compartido
                    platoEditado = new Plato(nombre, descripcion, precio, precio, uriCapturada, esCompartido, new ArrayList<>());
                }
            } else {
                //Añado a la lista la persona creada                                       //Solo se crea el plato si en el compartido
                platoEditado = new Plato(nombre, descripcion, precio, precio, uriCapturada, esCompartido, new ArrayList<>());

            }

            Intent intentComensal = new Intent();
            intentComensal.putExtra("detallePlato", platoEditado);
            setResult(Activity.RESULT_OK, intentComensal);
            finish();

        }

    }

    public void borrarPlato() {
        Intent borrarPlato = new Intent();
        borrarPlato.putExtra("borrarPlato", true);
        setResult(Activity.RESULT_OK, borrarPlato);
        finish();
    }

    public void insertarComensal() {
        etTitleDetalle.setText(plato.getNombre());
        etDescripcionDetalle.setText(plato.getDescripcion());
        etPrecioDetalle.setText(String.valueOf(plato.getPrecio()));
        uriCapturada = plato.getUrlImage();
        if (!plato.getUrlImage().equalsIgnoreCase("")) {
            ivFotoDetalle.setImageURI(Uri.parse(plato.getUrlImage()));
        } else {
            //Inserta Imagen photo
            ivFotoDetalle.setImageURI(Uri.parse("android.resource://com.tfg.marfol/" + R.drawable.camera));
            ivFotoDetalle.setPadding(30, 30, 30, 30);
        }
    }

    public void asignarEfectos() {

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

    public void mostrarAdapter() {
        //Se añade la posición 0 el objeto para añadir personas ( compartir )
        //Se añade el objeto Persona, " Añadir Persona " por si el usuario desea compartir en editar
        plato.getPersonasCompartir().add(0, new Persona(0, "", "", "android.resource://com.tfg.marfol/" + R.drawable.add_icon, new ArrayList<>(), 0));

        //Prepara el Adapter para su uso
        rvAnadirPlatoDetalle.setLayoutManager(new LinearLayoutManager(this));
        anadirPAdapter = new PersonaCompartirAdapter();
        rvAnadirPlatoDetalle.setAdapter(anadirPAdapter);
        anadirPAdapter.setmListener(this::onItemClick);
        anadirPAdapter.setResultsPersonaCom(plato.getPersonasCompartir());

        //Solo aparece activado si es compartido
        if (plato.isCompartido()) {
            //Si el plato es compartido, activamos el switch y los elementos de compartir
            rvAnadirPlatoDetalle.setVisibility(View.VISIBLE);
            tvListaEditarPlato.setVisibility(View.VISIBLE);
            swCompartirPlato.setChecked(true);
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

    // Método para abrir la galería
    private void abrirGaleria() {
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
    }

    // Método para abrir la cámara
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
    }

    public void asignarId() {
        ivFotoDetalle = findViewById(R.id.ivFotoPersonaPlato);
        etTitleDetalle = findViewById(R.id.etTitleDetallePlato);
        etDescripcionDetalle = findViewById(R.id.etDescripcionDetallePlato);
        rvAnadirPlatoDetalle = findViewById(R.id.rvAnadirDetallePlato);
        btnEditarDetalle = findViewById(R.id.btnEditarPlato);
        btnBorrarDetalle = findViewById(R.id.btnBorrarPlato);
        etPrecioDetalle = findViewById(R.id.etPrecioEditarPlato);
        swCompartirPlato = findViewById(R.id.swCompartirEditarPlato);
        tvListaEditarPlato = findViewById(R.id.tvListaEditarPlato);

        //Asigna IDs de los elementos del popup
        puElegirAccion = new Dialog(this);
        puElegirAccion.setContentView(R.layout.popup_accion);
        btnUpCamara = puElegirAccion.findViewById(R.id.btnCancelarPopup);
        btnUpGaleria = puElegirAccion.findViewById(R.id.btnConfirmarPopup);

    }

    @Override
    public void onItemClick(int position) {
        if (position == 0) {
            //Método que comprueba si hay repetidos en la lista de compartidos (no aparezcan de nuevo)
            //Además, comprueba que no esté el propio usuario a la hora de repartir
            noRepComList = nombreCompartir;

            for (Persona p : plato.getPersonasCompartir()) {
                noRepComList.removeIf(persona -> persona.getComensalCode() == p.getComensalCode());
                noRepComList.removeIf(persona -> persona.getComensalCode() == personaCode);
            }

            //Accedemos a la actividad de compartir plato
            intent = new Intent(this, CompartirListaActivity.class);
            intent.putExtra("arrayListComenComp", noRepComList);
            rLauncherComp.launch(intent);

        }
    }
}