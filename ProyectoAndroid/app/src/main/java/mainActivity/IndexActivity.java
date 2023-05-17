package mainActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;


import mainActivity.API.API;
import mainActivity.menu.AboutUs;
import mainActivity.menu.ContactUs;
import mainActivity.menu.Preferences;


public class IndexActivity extends AppCompatActivity {

    private Button btnApIndex;

    private Button btnApIndex2;
    private ImageView ivLoginIndex, ivMenuIndex;
    private RecyclerView rvPresetsIndex;
    private Dialog puVolverIndex;
    private Button btnCancelarIndex, btnConfirmarIndex;
    private TextView tvMessage1Popup, tvMessage2Popup, tvTitlePopup, btnAPIndex, tvTitleIndex;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ActivityResultLauncher rLauncherLogin;

    private TextView menuItemAboutUs;
    private TextView menuItemContactUs;
    private TextView menuItemPreferencias;
    private TextView menuItemHome;
    private TextView tvLogoutIndex;
    private PopupWindow popupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna efectos a los elementos (colores, etc)
        asignarEfectos();
        //si uno está logueado se comporta de una manera o otra
        comprobarLauncher();
        rLauncherLogin = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    comprobarLauncher();
                }
        );
        //Botón que accede a la gestión de participantes
        btnApIndex.setOnClickListener(view -> {
            Intent intent = new Intent(this, ParticipantesActivity.class);

            startActivity(intent);

            //Aplica un efecto de desvanecimiento entre actividades y se cierra
            overridePendingTransition(androidx.navigation.ui.R.anim.nav_default_enter_anim, androidx.navigation.ui.R.anim.nav_default_exit_anim);
            finish();
        });

        btnApIndex2.setOnClickListener(view -> {
            Intent intent = new Intent(this, API.class);
            startActivity(intent);
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
    private void botonImagenLogueado(){
        //Puesto provisional para probar cosas
        ivLoginIndex.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.AuthActivity.class);
            rLauncherLogin.launch(intent);
        });


    }
    private void botonImagenNoLogueado(){
        //Puesto provisional para probar cosas
        ivLoginIndex.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.HomeActivity.class);
            rLauncherLogin.launch(intent);
        });
    }

    public void asignarId() {
        //Asigna Ids a los elementos de la actividad
        btnApIndex = findViewById(R.id.btnApIndex);
        btnApIndex2 = findViewById(R.id.btnAPIndex);
        ivLoginIndex = findViewById(R.id.ivAnadirPlatoImagen);
        Glide.with(this).load(R.drawable.nologinimg).into(ivLoginIndex);
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
        btnApIndex2.getPaint().setShader(gradient);
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
    public void showPopupMenu(View view) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_menu, null);
        menuItemHome = popupView.findViewById(R.id.menu_item1);
        menuItemPreferencias = popupView.findViewById(R.id.menu_item2);
        menuItemAboutUs = popupView.findViewById(R.id.menu_item3);
        menuItemContactUs = popupView.findViewById(R.id.menu_item4);
        tvLogoutIndex = popupView.findViewById(R.id.tvLogoutIndex);
        tvLogoutIndex.setVisibility(View.INVISIBLE);
        // Ajustar el tamaño del menú según tus preferencias
        int width = getResources().getDisplayMetrics().widthPixels * 7 / 10; // El 70% del ancho de la pantalla
        int height = getResources().getDisplayMetrics().heightPixels ; // El 70% del alto de la pantalla

        popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        popupWindow.showAtLocation(view, Gravity.START, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return true;
            }
        });

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
                // Acción al hacer clic en "AboutUs"
                Intent intent = new Intent(IndexActivity.this, AboutUs.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });

        menuItemContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "ContactUs"
                Intent intent = new Intent(IndexActivity.this, ContactUs.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });

        menuItemHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Home" (IndexActivity)
                // No es necesario iniciar una nueva actividad, ya que ya estás en IndexActivity

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });

        menuItemPreferencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción al hacer clic en "Preferences"
                Intent intent = new Intent(IndexActivity.this, Preferences.class);
                startActivity(intent);

                // Cerrar el menú emergente
                popupWindow.dismiss();
            }
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
        tvLogoutIndex.setVisibility(View.VISIBLE);
        tvLogoutIndex.setOnClickListener(v->{
            SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor prefs = prefAux.edit();
            prefs.clear();
            prefs.apply();
            FirebaseAuth.getInstance()
                    .signOut();
            Intent in = new Intent(this, IndexActivity.class);
            startActivity(in);
            finish();
        });
        }


        // Cerrar el PopupWindow cuando se destruya la actividad


    }
    private void comprobarLauncher(){
        if(MetodosGlobales.comprobarLogueado(IndexActivity.this,ivLoginIndex)){
            botonImagenNoLogueado();
        }else{
            Glide.with(this).load(R.drawable.nologinimg).into(ivLoginIndex);
            botonImagenLogueado();

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }



}