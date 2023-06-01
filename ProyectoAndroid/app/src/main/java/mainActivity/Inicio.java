package mainActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tfg.marfol.R;

public class Inicio extends AppCompatActivity {
    ImageView ivImagenInicio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        ivImagenInicio = findViewById(R.id.ivImagenInicio);
        Glide.with(this)
                .asGif()
                .load(R.drawable.marfol_intro)
                .into(ivImagenInicio);

    // Mostrar el logotipo durante 3 segundos
        int segundosDuracion = 3000;
        new Handler().postDelayed(() -> {
            // Iniciar la siguiente actividad
            Intent intent = new Intent(Inicio.this, IndexActivity.class);
            startActivity(intent);

            // Cerrar la actividad actual
            finish();
        }, segundosDuracion);
    }
}