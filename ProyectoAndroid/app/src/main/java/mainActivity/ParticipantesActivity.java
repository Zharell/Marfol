package mainActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.tfg.marfol.R;

public class ParticipantesActivity extends AppCompatActivity {

    ImageView ivLoginParticipantes, ivMenuParticipantes;
    ActivityResultLauncher resultLauncher;
    private Dialog puVolverParticipantes;
    Button btnCancelarParticipantes, btnConfirmarParticipantes;
    private Intent volverIndex;

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

        //Botones para el popup de confirmación
        //Confirmar, retrocede, cierra la actividad y pierde los datos introducidos
        btnConfirmarParticipantes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(volverIndex);
                finish();
            }
        });

        //Cancela, desaparece el popup y continúa en la actividad
        btnCancelarParticipantes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                puVolverParticipantes.dismiss();
            }
        });

    }

    //Método que al pulsar el botón de volver redirige a la pantalla Index sin perder información
    @Override
    public void onBackPressed() {

        //Pregunta si realmente quieres salir
        puVolverParticipantes.show();

    }


    public void asignarId () {

        //Asigna Ids a los elementos de la actividad
        ivMenuParticipantes = findViewById(R.id.ivMenuParticipantes);
        ivLoginParticipantes = findViewById(R.id.ivLoginParticipantes);

        //Asigna IDs de los elementos del popup
        puVolverParticipantes = new Dialog(this);
        volverIndex = new Intent(this, IndexActivity.class);
        puVolverParticipantes.setContentView(R.layout.popup_confirmacion);
        btnCancelarParticipantes = puVolverParticipantes.findViewById(R.id.btnCancelarParticipantes);
        btnConfirmarParticipantes = puVolverParticipantes.findViewById(R.id.btnConfirmarParticipantes);


        //Ajusta el tamaño de la imagen del login
        ivLoginParticipantes.setPadding(20, 20, 20, 20);

    }

}