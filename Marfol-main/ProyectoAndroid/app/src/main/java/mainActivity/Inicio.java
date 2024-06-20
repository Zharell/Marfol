package mainActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.tfg.marfol.R;

public class Inicio extends AppCompatActivity {
    ImageView ivImagenInicio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        //busca la imagen en el xml
        ivImagenInicio = findViewById(R.id.ivImagenInicio);
        //con glide seteamos el gif a la imagen
        Glide.with(this)
                .asGif()
                .load(R.drawable.marfol_intro)
                .centerInside()
                .into(ivImagenInicio);

        // Mostrar la animación durante 3 segundos
        int segundosDuracion = 3000;
        new Handler().postDelayed(() -> {
            // Iniciar el index
            Intent intent = new Intent(Inicio.this, IndexActivity.class);
            startActivity(intent);
            //Aplica un efecto de desvanecimiento entre actividades y se cierra
            overridePendingTransition(androidx.navigation.ui.R.anim.nav_default_enter_anim, androidx.navigation.ui.R.anim.nav_default_exit_anim);
            // Cerrar la actividad actual
            finish();
        }, segundosDuracion);
    }
}