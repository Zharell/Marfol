package mainActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.tfg.marfol.R;


public class IndexActivity extends AppCompatActivity {

    private Button btnApIndex;
    private TextView tvTitleIndex;
    private ImageView ivLoginIndex, ivMenuIndex;
    private RecyclerView rvPresetsIndex;
    private Dialog puVolverIndex;
    private Button btnCancelarIndex, btnConfirmarIndex;
    private TextView tvMessage1Popup, tvMessage2Popup, tvTitlePopup;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna efectos a los elementos (colores, etc)
        asignarEfectos();

        //Comprueba si estás logueado
        mAuth = FirebaseAuth.getInstance();

        //Comprueba si estás logueado o no
        if (mAuth.getCurrentUser() != null) {

            Toast.makeText(this, "Estás logueado bro ",Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(this, "No estás logueado pendejo ",Toast.LENGTH_SHORT).show();

        }

        //Puesto provisional para probar cosas
        ivLoginIndex.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.AuthActivity.class);
            startActivity(intent);
            finish();
        });

        //Botón que accede a la gestión de participantes
        btnApIndex.setOnClickListener(view -> {
            Intent intent = new Intent(this, ParticipantesActivity.class);
            startActivity(intent);
            finish();
        });

        //Botones para el popup de confirmación
        //Confirmar cierra la APP
        btnConfirmarIndex.setOnClickListener(view -> {
            puVolverIndex.dismiss();
            finishAffinity();
        });

        //Cancela, desaparece el popup y continúa en la actividad
        btnCancelarIndex.setOnClickListener(view -> puVolverIndex.dismiss());


    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.overflow, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.testMenu1){
            Toast.makeText(this, "Pursaste el primer item",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.testMenu2) {
            Toast.makeText(this, "Pursaste el segundo item",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.testMenu3) {
            Toast.makeText(this, "Pursaste el tercer item",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    public void asignarId() {

        //Asigna Ids a los elementos de la actividad
        btnApIndex = findViewById(R.id.btnApIndex);
        ivLoginIndex = findViewById(R.id.ivLoginAnadirPlato);
        ivMenuIndex = findViewById(R.id.ivMenuAnadirPlato);
        rvPresetsIndex = findViewById(R.id.rvPresetsIndex);
        tvTitleIndex = findViewById(R.id.tvTitleAnadirPlato);

        //Asigna IDs de los elementos del popup
        puVolverIndex = new Dialog(this);
        puVolverIndex.setContentView(R.layout.popup_confirmacion);
        btnCancelarIndex = puVolverIndex.findViewById(R.id.btnCancelarPopup);
        btnConfirmarIndex = puVolverIndex.findViewById(R.id.btnConfirmarPopup);
        tvMessage1Popup = puVolverIndex.findViewById(R.id.tvMessage1Popup);
        tvMessage2Popup = puVolverIndex.findViewById(R.id.tvMessage2Popup);
        tvTitlePopup = puVolverIndex.findViewById(R.id.tvTitlePopup);

    }

    public void asignarEfectos() {

        //Ajusta el tamaño de la imagen del login
        ivLoginIndex.setPadding(20, 20, 20, 20);

        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder),
                        getResources().getColor(R.color.redTitle)
        };

        float[] positions = {0f, 0.2f};

        LinearGradient gradient = new LinearGradient(0, 0, 40,
                tvTitleIndex.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);

        tvTitleIndex.getPaint().setShader(gradient);
        btnApIndex.getPaint().setShader(gradient);
        btnConfirmarIndex.getPaint().setShader(gradient);
        btnCancelarIndex.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;

        tvTitleIndex.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
        btnApIndex.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

    }

    //Método que al pulsar el botón de volver te pregunta si deseas cerrar la app
    @Override
    public void onBackPressed() {

        //Pregunta si realmente quieres salir
        tvTitlePopup.setText("Salir");
        tvMessage1Popup.setText("Salir de Marfol, se cerrará la aplicación");
        tvMessage2Popup.setText("¿ Estás seguro ?");
        puVolverIndex.show();

    }
    //desplegable



}