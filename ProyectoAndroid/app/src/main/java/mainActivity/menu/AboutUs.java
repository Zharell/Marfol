package mainActivity.menu;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tfg.marfol.R;

import mainActivity.IndexActivity;

public class AboutUs extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        TextView tvAboutUs = findViewById(R.id.tvAboutUs);

        String aboutUsText = "Sobre nosotros\n\n" +
                "Somos dos estudiantes de DAM que creamos la aplicación Marfol para simplificar la tarea de dividir la cuenta al comer fuera con amigos o familiares. Surge de la necesidad que experimentamos al no tener la asignatura FOL los martes durante nuestro primer año, por lo que solíamos ir a un restaurante a tomar algo. Al ser un grupo grande, se complicaba el momento de pagar y dividir la cuenta, ya que algunos platos eran compartidos entre tres personas y la misma persona compartía otros platos con diferentes personas. A partir de esta experiencia, decidimos crear una aplicación que resolviera este problema.\n\n" +
                "Marfol es una aplicación fácil y sencilla que permite al usuario dividir la cuenta sin tener que realizar cálculos manualmente.\n\n" +
                "Características principales:\n" +
                "1. División precisa: Nuestra aplicación tiene en cuenta los precios de los artículos y permite asignar diferentes porcentajes o cantidades a cada persona para una división justa.\n" +
                "2. Agregar artículos: Puedes agregar los artículos y sus precios para crear una lista detallada de lo consumido.\n" +
                "3. Guardar: Puedes guardar los registros para futuras referencias.\n" +
                "4. Interfaz intuitiva: Diseñamos una interfaz fácil de usar con un diseño limpio y atractivo para que puedas disfrutar de una experiencia fluida al utilizar nuestra aplicación.\n\n" +
                "Nuestro objetivo principal es hacer que la tarea de dividir la cuenta sea rápida, precisa y sin complicaciones.\n\n" +
                "¡Esperamos que disfrutes utilizando Marfol y que te ayude a simplificar la experiencia de salir a comer con tus amigos!\n\n" +
                "¡Gracias por tu apoyo!\n\n" +
                "Equipo de Marfol\n" +
                "Francisco Javier *Experto en adaptadores*.\n" +
                "Kayler *Experto en firebase*.";


        tvAboutUs.setText(aboutUsText);
    }

}
