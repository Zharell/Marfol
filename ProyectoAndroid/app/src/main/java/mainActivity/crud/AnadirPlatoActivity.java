package mainActivity.crud;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.marfol.R;

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

import adapters.AnadirPersonaAdapter;
import adapters.PersonaCompartirAdapter;
import entities.Persona;
import entities.Plato;

public class AnadirPlatoActivity extends AppCompatActivity implements PersonaCompartirAdapter.onItemClickListener {

    private Switch swCompartirPlato;
    private RecyclerView rvPlatosAnadirPlato;
    private TextView tvTitleAnadirP, tvSubTitP;
    private PersonaCompartirAdapter anadirPAdapter;
    private EditText  etNombreAnadirP, etDescAnadirP, etPrecioAnadirP;
    private Button btnContinuarAnadirP;
    private ImageView ivLoginAnadirPlato, ivPlatoAnadirP;
    private final int CAMERA_PERMISSION_CODE = 100;
    private ArrayList <Plato> platos;
    private ArrayList <Persona> comensalCompartir;
    private boolean esCompartido=false;
    private String uriCapturada="";
    private ActivityResultLauncher<Intent> camaraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_plato);

        //Recibe la lista de comensales para empezar a añadir
        Intent intent = getIntent();
        platos = (ArrayList<Plato>) intent.getSerializableExtra("arrayListPlatos");

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

            }
        });

    }

    //Se debe insertar el ArrayList vacío para que el adaptador inserte el objeto 0 ( añadir elemento )
    public void mostrarAdapter() {

        //Se añaden la lista de platos vacía para que aparezca el botón de añadir Plato
        comensalCompartir = new ArrayList<Persona>();

        //Prepara el Adapter para su uso
        rvPlatosAnadirPlato.setLayoutManager(new LinearLayoutManager(this));
        anadirPAdapter = new PersonaCompartirAdapter();
        rvPlatosAnadirPlato.setAdapter(anadirPAdapter);
        anadirPAdapter.setmListener(this::onItemClick);
        anadirPAdapter.setResultsPersonaCom(comensalCompartir);
    }

    public void anadirPlato () {

        boolean esValidado=true;
        String nombre = String.valueOf(etNombreAnadirP.getText());
        String descripcion = String.valueOf(etDescAnadirP.getText());
        double precio;

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

            //Añado a la lista la persona creada
            platos.add(new Plato(nombre,descripcion,precio,precio,uriCapturada,esCompartido, new ArrayList<>()));

            Intent intentPlato = new Intent();
            intentPlato.putExtra("arrayListPlatos", platos);
            setResult(Activity.RESULT_OK, intentPlato);
            finish();

        }

    }

    // Método para abrir la cámara
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraLauncher.launch(cameraIntent);
    }

    public void asignarEfectos() {

        //Ajusta el tamaño de la imagen del login
        ivLoginAnadirPlato.setPadding(20, 20, 20, 20);

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
        ivLoginAnadirPlato = findViewById(R.id.ivLoginAnadirPlato);
        btnContinuarAnadirP = findViewById(R.id.btnPlatosAnadirPlato);
        ivPlatoAnadirP = findViewById(R.id.ivPlatoAnadirPlato);
        swCompartirPlato = findViewById(R.id.swCompartirAnadirPlato);

    }

    @Override
    public void onItemClick(int position) {
        //Si pulsas "Añadir Persona" ( 0 ), accederás a la actividad añadir persona
        if (position>0) {
            Toast.makeText(this,"Pulsaste el campo: "+String.valueOf(position),Toast.LENGTH_SHORT).show();
        } else {
            //Accedemos a la actividad de añadir plato
            Intent intent = new Intent(this, AnadirPlatoActivity.class);
            intent.putExtra("arrayListPlatos", platos);
            //rLauncherComp.launch(intent);
        }
    }
}