package mainActivity.crud;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.tfg.marfol.R;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import adapters.PersonaCompartirAdapter;
import entities.Persona;
import entities.Plato;
import mainActivity.detalle.CompartirListaActivity;

public class AnadirPlatoActivity extends AppCompatActivity implements PersonaCompartirAdapter.onItemClickListener {

    private Switch swCompartirPlato;
    private RecyclerView rvPlatosAnadirPlato;
    private TextView tvTitleAnadirP, tvSubTitP;
    private Dialog puElegirAccion;
    private Button btnUpCamara, btnUpGaleria;
    private static final int GALLERY_PERMISSION_CODE = 1001;
    private PersonaCompartirAdapter anadirPAdapter;
    private EditText  etNombreAnadirP, etDescAnadirP, etPrecioAnadirP;
    private Button btnContinuarAnadirP;
    private ImageView ivPlatoAnadirP;
    private final int CAMERA_PERMISSION_CODE = 100;
    private ArrayList <Plato> platos;
    private ArrayList<Persona> nombreCompartir;
    private ArrayList <Persona> comensalCompartirList;
    private ArrayList <Persona> noRepCompartirList;
    private boolean esCompartido=false;
    private ActivityResultLauncher rLauncherComp;
    private String uriCapturada ="android.resource://com.tfg.marfol/"+R.drawable.logo_marfol_azul;
    private int personaCode;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private Intent intent;
    private String nombre, descripcion;
    private double precio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_plato);

        //Recibe la lista de comensales para empezar a añadir
        intent = getIntent();
        platos = (ArrayList<Plato>) intent.getSerializableExtra("arrayListPlatos");

        //Recibo desde Editar o Añadir
        nombreCompartir = (ArrayList<Persona>) intent.getSerializableExtra("arrayListComenComp");
        personaCode = intent.getIntExtra("comensalCode",0);

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Botón encargado de añadir el plato
        btnContinuarAnadirP.setOnClickListener(view -> { anadirPlato(); });

        //Comprueba si el switch compartir está activo o no para mostrar su información
        swCompartirPlato.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                tvSubTitP.setVisibility(View.VISIBLE);
                rvPlatosAnadirPlato.setVisibility(View.VISIBLE);
                esCompartido=true;
            } else {
                tvSubTitP.setVisibility(View.INVISIBLE);
                rvPlatosAnadirPlato.setVisibility(View.INVISIBLE);
                esCompartido=false;
            }

        });

        //Laucher Result recibe las personas de la listas que van a compartir
        rLauncherComp = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Método de cálculo aquí también
                        Intent data = result.getData();
                        comensalCompartirList.add((Persona) data.getSerializableExtra("personaCompartir"));
                        anadirPAdapter.setResultsPersonaCom(comensalCompartirList);
                    }
                }
        );

        //Convocamos el PopUp para mostrar las acciones ( Galería, Cámara )
        ivPlatoAnadirP.setOnClickListener(view -> { puElegirAccion.show(); });

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
                ivPlatoAnadirP.setImageBitmap(photo);

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

                } catch (IOException e) {
                    e.printStackTrace();
                }
                puElegirAccion.dismiss();
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
        comensalCompartirList = new ArrayList<Persona>();

        //Prepara el Adapter para su uso
        rvPlatosAnadirPlato.setLayoutManager(new LinearLayoutManager(this));
        anadirPAdapter = new PersonaCompartirAdapter();
        rvPlatosAnadirPlato.setAdapter(anadirPAdapter);
        anadirPAdapter.setmListener(this::onItemClick);
        anadirPAdapter.setResultsPersonaCom(comensalCompartirList);
    }

    public void anadirPlato () {

        boolean esValidado=true;
        nombre = String.valueOf(etNombreAnadirP.getText());
        descripcion = String.valueOf(etDescAnadirP.getText());
        //Comprueba si el editText está vacío, de estarlo el programa lo entiende como un 0, además, remplaza <,> por <.> para evitar errores
        precio = etPrecioAnadirP.getText().toString().equalsIgnoreCase("") ? 0 : Double.parseDouble(etPrecioAnadirP.getText().toString().replace(",","."));

        //Comprueba si has añadido un nombre
        if (etNombreAnadirP.getText().toString().length() == 0) {
            Toast.makeText(this,"Debe introducir un nombre", Toast.LENGTH_SHORT).show();
            esValidado=false;
        }

        if (precio <= 0) {
            Toast.makeText(this,"Debe introducir un precio mayor a 0", Toast.LENGTH_SHORT).show();
            esValidado=false;
        }

        //Comprueba si ha validado, añade la persona, envía al padre el ArrayList de comensales, platos y cierra la actividad
        if (esValidado) {

            //Dependiendo si es compartido o no, sigue un
            if (esCompartido) {
                //Eliminamos la posición 0 ya que es un botón
                comensalCompartirList.remove(0);

                //Hacemos una última comprobación si ha añadido personas en compartir, si no ha añadido se añade un plato NO compartido
                if (comensalCompartirList.size()>0) {
                    //Añado a la lista la persona creada                                           - lista de personas que van a compartir el plato
                    platos.add(new Plato(nombre,descripcion,precio,precio,uriCapturada,esCompartido, comensalCompartirList));
                } else {
                    //Añado a la lista la persona creada                                          //Se añade vacío ya que no es compartido
                    esCompartido=false;
                    platos.add(new Plato(nombre,descripcion,precio,precio,uriCapturada,esCompartido, new ArrayList<>()));
                }
            } else {
                //Añado a la lista la persona creada                                          //Se crea el plato si en el compartido
                platos.add(new Plato(nombre,descripcion,precio,precio,uriCapturada,esCompartido, new ArrayList<>()));
            }
            intent = new Intent();
            intent.putExtra("arrayListPlatos", platos);
            setResult(Activity.RESULT_OK, intent);
            finish();

        }

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
                ivPlatoAnadirP.setBackground(null);
                Glide.with(this)
                        .load(selectedImageUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(ivPlatoAnadirP);
            }
            puElegirAccion.dismiss();
        }
    }

    // Método para abrir la cámara
    private void abrirCamara() {
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(intent);
    }

    public void asignarEfectos() {

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
        swCompartirPlato.getPaint().setShader(gradient);
        btnContinuarAnadirP.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;

        tvTitleAnadirP.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

        //Inserta Imagen photo
        ivPlatoAnadirP.setImageURI(Uri.parse("android.resource://com.tfg.marfol/"+R.drawable.camera));
        ivPlatoAnadirP.setPadding(30, 30, 30, 30);

    }

    public void asignarId() {
        //Asigna Ids a los elementos de la actividad
        rvPlatosAnadirPlato = findViewById(R.id.rvPlatosAnadirPlato);
        tvTitleAnadirP = findViewById(R.id.tvTitleAnadirPlato);
        etNombreAnadirP = findViewById(R.id.etNombreAnadirPlato);
        etDescAnadirP = findViewById(R.id.etDescripcionAnadirPlato);
        etPrecioAnadirP = findViewById(R.id.etPlatoPrecio);
        tvSubTitP = findViewById(R.id.tvListaPlatosAnadirPlato);
        btnContinuarAnadirP = findViewById(R.id.btnPlatosAnadirPlato);
        ivPlatoAnadirP = findViewById(R.id.ivPlatoAnadirPlato);
        swCompartirPlato = findViewById(R.id.swCompartirAnadirPlato);

        //Asigna IDs de los elementos del popup
        puElegirAccion = new Dialog(this);
        puElegirAccion.setContentView(R.layout.popup_accion);
        btnUpCamara = puElegirAccion.findViewById(R.id.btnCancelarPopup);
        btnUpGaleria = puElegirAccion.findViewById(R.id.btnConfirmarPopup);
    }

    @Override
    public void onItemClick(int position) {
        if (position==0) {
            //Método que comprueba si hay repetidos en la lista de compartidos (no aparezcan de nuevo)
            //Además, comprueba que no esté el propio usuario a la hora de repartir
            noRepCompartirList = nombreCompartir;

            for (Persona p : comensalCompartirList) {
                noRepCompartirList.removeIf(persona -> persona.getComensalCode()==p.getComensalCode());
                noRepCompartirList.removeIf(persona -> persona.getComensalCode()==personaCode);
            }

            //Accedemos a la actividad de compartir plato
            intent = new Intent(this, CompartirListaActivity.class);
            intent.putExtra("arrayListComenComp", noRepCompartirList);
            rLauncherComp.launch(intent);
        }
    }


}