package mainActivity.menu;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tfg.marfol.R;

import mainActivity.IndexActivity;

public class ContactUs extends AppCompatActivity {

    private EditText etSubject, etMessage;
    private ImageView ivMenuIndex;
    private TextView menuItemAboutUs;
    private TextView menuItemContactUs;
    private TextView menuItemPreferencias;
    private TextView menuItemHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_us);

        etSubject = findViewById(R.id.etSubject);
        etMessage = findViewById(R.id.etMessage);

        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
    }

    private void sendEmail() {
        String[] to = {"fernando.vaz.carvalho@gmail.com"}; // Reemplaza con tu dirección de correo electrónico
        String subject = etSubject.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Choose an email app"));
        } else {
            Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
        }
    }
    public void showPopupMenu(View view) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_menu, null);
        menuItemPreferencias = popupView.findViewById(R.id.menu_item2);
        menuItemAboutUs = popupView.findViewById(R.id.menu_item3);
        menuItemContactUs = popupView.findViewById(R.id.menu_item4);
        // Ajustar el tamaño del menú según tus preferencias
        int width = getResources().getDisplayMetrics().widthPixels * 7 / 10; // El 70% del ancho de la pantalla
        int height = getResources().getDisplayMetrics().heightPixels ;


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
                Intent intent = new Intent(ContactUs.this, AboutUs.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
        menuItemContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
            }
        });
        menuItemHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Sobre nosotros"
                Intent intent = new Intent(ContactUs.this, IndexActivity.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
        menuItemPreferencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Sobre nosotros"
                Intent intent = new Intent(ContactUs.this, Preferences.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
    }
}