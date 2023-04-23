package mainActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.tfg.marfol.R;


public class IndexActivity extends AppCompatActivity {

    Button btnApIndex;
    ImageView ivLoginIndex, ivMenuIndex;
    RecyclerView rvPresetsIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        //Método que asigna IDs a los elementos
        asignarId();



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

    }

    public void asignarId() {

        //Asigna Ids a los elementos de la actividad
        btnApIndex = findViewById(R.id.btnApIndex);
        ivLoginIndex = findViewById(R.id.ivLoginParticipantes);
        ivMenuIndex = findViewById(R.id.ivMenuParticipantes);
        rvPresetsIndex = findViewById(R.id.rvPresetsIndex);

        //Ajusta el tamaño de la imagen del login
        ivLoginIndex.setPadding(20, 20, 20, 20);

    }

    //Método que al pulsar el botón de volver te pregunta si deseas cerrar la app
    @Override
    public void onBackPressed() {

        finishAffinity();

    }




}