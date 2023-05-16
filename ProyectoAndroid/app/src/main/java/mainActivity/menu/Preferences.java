package mainActivity.menu;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tfg.marfol.R;

import mainActivity.IndexActivity;

public class Preferences extends AppCompatActivity {
    private ImageView ivMenuIndex;
    private TextView menuItemAboutUs;
    private TextView menuItemContactUs;
    private TextView menuItemPreferencias;
    private TextView menuItemHome;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferencias);
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
                // Acción al hacer clic en "Sobre nosotros"
                Intent intent = new Intent(Preferences.this, AboutUs.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
        menuItemContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Sobre nosotros"
                Intent intent = new Intent(Preferences.this, ContactUs.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
        menuItemHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Sobre nosotros"
                Intent intent = new Intent(Preferences.this, IndexActivity.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
        menuItemPreferencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
}
