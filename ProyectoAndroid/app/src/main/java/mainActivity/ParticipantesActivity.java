package mainActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.tfg.marfol.R;

public class ParticipantesActivity extends AppCompatActivity {

    ImageView ivLoginParticipantes, ivMenuParticipantes;
    ActivityResultLauncher resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participantes);

        //Método que asigna IDs a los elementos
        asignarId();


        //Laucher Result
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {

                    Toast.makeText(this, "Juan", Toast.LENGTH_SHORT).show();
                }
        );


    }

    //Método que al pulsar el botón de volver redirige a la pantalla Index sin perder información
    @Override
    public void onBackPressed() {

        //Abre una nueva actividad a la espera de nuevos resulset
        Intent intent = new Intent(this, IndexActivity.class);
        resultLauncher.launch(intent);


    }


    public void asignarId () {

        //Asigna Ids a los elementos de la actividad
        ivMenuParticipantes = findViewById(R.id.ivMenuParticipantes);
        ivLoginParticipantes = findViewById(R.id.ivLoginParticipantes);

        //Ajusta el tamaño de la imagen del login
        ivLoginParticipantes.setPadding(20, 20, 20, 20);

    }

}