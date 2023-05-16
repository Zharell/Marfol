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
    private ImageView ivMenuIndex;
    private TextView menuItemAboutUs;
    private TextView menuItemContactUs;
    private TextView menuItemPreferencias;
    private TextView menuItemHome;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        TextView tvAboutUs = findViewById(R.id.tvAboutUs);

        String aboutUsText = "Sobre nosotros\n\n" +
                "¡Bienvenido a nuestra aplicación de División de Cuentas!\n\n" +
                "Somos un equipo de 3 estudiantes de DAW comprometidos en simplificar la tarea de dividir la cuenta al comer fuera con amigos o familiares. Nuestra aplicación te permite calcular fácilmente cuánto debe pagar cada persona según lo que consumió.\n\n" +
                "Características principales:\n" +
                "1. División precisa: Nuestra aplicación tiene en cuenta los precios de los artículos y permite asignar diferentes porcentajes o cantidades a cada persona para una división justa.\n" +
                "2. Agregar artículos: Puedes agregar los artículos y sus precios para crear una lista detallada de lo consumido.\n" +
                "3. Compartir y guardar: Puedes compartir los resultados del cálculo de la cuenta con tus amigos a través de mensajes o redes sociales. También puedes guardar los registros para futuras referencias.\n" +
                "4. Interfaz intuitiva: Diseñamos una interfaz fácil de usar con un diseño limpio y atractivo para que puedas disfrutar de una experiencia fluida al utilizar nuestra aplicación.\n\n" +
                "Nuestro objetivo principal es hacer que la tarea de dividir la cuenta sea rápida, precisa y sin complicaciones. Valoramos tus comentarios y sugerencias para mejorar continuamente nuestra aplicación.\n\n" +
                "¡Esperamos que disfrutes utilizando nuestra aplicación de División de Cuentas y que te ayude a simplificar la experiencia de salir a comer con tus amigos!\n\n" +
                "Si tienes alguna pregunta o sugerencia, no dudes en ponerte en contacto con nosotros a través de la sección \"Contacto\" en la aplicación.\n\n" +
                "¡Gracias por tu apoyo!\n\n" +
                "Equipo de División de Cuentas\n" +
                "Estudiantes de DAW";

        tvAboutUs.setText(aboutUsText);
    }
    public void showPopupMenu(View view) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_menu, null);
        menuItemHome = popupView.findViewById(R.id.menu_item1);
        menuItemPreferencias = popupView.findViewById(R.id.menu_item2);
        menuItemAboutUs = popupView.findViewById(R.id.menu_item3);
        menuItemContactUs = popupView.findViewById(R.id.menu_item4);

        // Ajustar el tamaño del menú según tus preferencias
        int width = getResources().getDisplayMetrics().widthPixels * 7 / 10; // El 70% del ancho de la pantalla
        int height = getResources().getDisplayMetrics().heightPixels ; // El 70% del alto de la pantalla

        PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        popupWindow.showAtLocation(view, Gravity.START, 0, 0);

        // Aplicar el degradado de colores a los textos del menú
        int[] colors = {
                getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };

        float[] positions = {0f, 0.2f};

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, menuItemHome.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT
        );

        menuItemHome.getPaint().setShader(gradient);
        menuItemPreferencias.getPaint().setShader(gradient);
        menuItemAboutUs.getPaint().setShader(gradient);
        menuItemContactUs.getPaint().setShader(gradient);

        menuItemAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
            }
        });

        menuItemContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "ContactUs"
                Intent intent = new Intent(AboutUs.this, ContactUs.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });

        menuItemHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Home" (IndexActivity)
                Intent intent = new Intent(AboutUs.this, IndexActivity.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });

        menuItemPreferencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Preferences"
                Intent intent = new Intent(AboutUs.this, Preferences.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
    }
}
