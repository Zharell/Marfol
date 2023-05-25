package mainActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.RestaurantesAdapter;
import entities.Restaurantes;
import mainActivity.API.API;
import mainActivity.menu.AboutUs;
import mainActivity.menu.ContactUs;
import mainActivity.menu.Preferences;
import mainActivity.menu.crudBd.detalle.DetalleEditarPlatosBd;

public class IndexActivity extends AppCompatActivity implements RestaurantesAdapter.onItemClickListenerRestaurantes {

    private Button btnApIndex;

    private Button btnApIndex2;
    private ImageView ivImagenLogin, ivMenuIndex;
    private RecyclerView rvRestaurantesUsuario;
    private Dialog puVolverIndex;
    private Button btnCancelarIndex, btnConfirmarIndex, btnProvisional;
    private TextView tvMessage1Popup, tvMessage2Popup, tvTitlePopup, btnAPIndex, tvTitleIndex;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ActivityResultLauncher rLauncherLogin;

    private TextView menuItemAboutUs;
    private TextView menuItemContactUs;
    private TextView menuItemPreferencias;
    private TextView menuItemHome;
    private TextView tvLogoutIndex;
    private PopupWindow popupWindow;
    private Intent homeIntent, authIntent,intent;
    private RestaurantesAdapter restaurantesAdapter;
    private ArrayList<Restaurantes> restaurantesBd;
    private boolean enviarRestaurante;

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
            intent = new Intent(this, ParticipantesActivity.class);

            startActivity(intent);

            //Aplica un efecto de desvanecimiento entre actividades y se cierra
            overridePendingTransition(androidx.navigation.ui.R.anim.nav_default_enter_anim, androidx.navigation.ui.R.anim.nav_default_exit_anim);
            finish();
        });

        btnApIndex2.setOnClickListener(view -> {
            intent = new Intent(this, API.class);
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
        btnProvisional.setOnClickListener(view -> {
            intent = new Intent(this, mainActivity.menu.crudBd.Seleccion.class);
            startActivity(intent);
        });
    }

    private void botonImagenNoLogueado() {
        //Puesto provisional para probar cosas
        ivImagenLogin.setOnClickListener(view -> {
            authIntent = new Intent(this, login.AuthActivity.class);
            rLauncherLogin.launch(authIntent);
        });


    }

    private void botonImagenLogueado() {
        //Puesto provisional para probar cosas
        ivImagenLogin.setOnClickListener(view -> {
            homeIntent = new Intent(this, login.HomeActivity.class);
            rLauncherLogin.launch(homeIntent);
        });
    }

    public void asignarId() {
        //Asigna Ids a los elementos de la actividad
        btnApIndex = findViewById(R.id.btnApIndex);
        btnApIndex2 = findViewById(R.id.btnAPIndex);
        ivImagenLogin = findViewById(R.id.ivImagenLogin);
        Glide.with(this).load(R.drawable.nologinimg).into(ivImagenLogin);
        ivMenuIndex = findViewById(R.id.ivMenuAnadirPlato);
        rvRestaurantesUsuario = findViewById(R.id.rvRestaurantesUsuario);
        tvTitleIndex = findViewById(R.id.tvTitleAnadirPlato);
        //Asigna IDs de los elementos del popup
        puVolverIndex = new Dialog(this);
        puVolverIndex.setContentView(R.layout.popup_confirmacion);
        btnCancelarIndex = puVolverIndex.findViewById(R.id.btnCancelarPopup);
        btnConfirmarIndex = puVolverIndex.findViewById(R.id.btnConfirmarPopup);
        tvMessage1Popup = puVolverIndex.findViewById(R.id.tvMessage1Popup);
        tvMessage2Popup = puVolverIndex.findViewById(R.id.tvMessage2Popup);
        tvTitlePopup = puVolverIndex.findViewById(R.id.tvTitlePopup);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        btnProvisional = findViewById(R.id.btnProvisional);
    }

    public void asignarEfectos() {

        //Ajusta el tamaño de la imagen del login
        ivImagenLogin.setPadding(20, 20, 20, 20);

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
        int height = getResources().getDisplayMetrics().heightPixels; // El 70% del alto de la pantalla

        popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        popupWindow.showAtLocation(view, Gravity.START, 0, 0);
        popupView.setOnTouchListener((v, event) -> {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            return true;
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

        menuItemAboutUs.setOnClickListener(v -> {
            // Acción al hacer clic en "AboutUs"
            intent = new Intent(IndexActivity.this, AboutUs.class);
            startActivity(intent);

            // Cerrar el menú emergente
            popupWindow.dismiss();
        });

        menuItemContactUs.setOnClickListener(v -> {
            // Acción al hacer clic en "ContactUs"
            intent = new Intent(IndexActivity.this, ContactUs.class);
            startActivity(intent);

            // Cerrar el menú emergente
            popupWindow.dismiss();
        });

        menuItemHome.setOnClickListener(v -> {
            // Cerrar el menú emergente
            popupWindow.dismiss();
        });

        menuItemPreferencias.setOnClickListener(v -> {
            // Acción al hacer clic en "Preferences"
            intent = new Intent(IndexActivity.this, Preferences.class);
            startActivity(intent);
            // Cerrar el menú emergente
            popupWindow.dismiss();
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            tvLogoutIndex.setVisibility(View.VISIBLE);
            tvLogoutIndex.setOnClickListener(v -> {
                SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor prefs = prefAux.edit();
                prefs.clear();
                prefs.apply();
                FirebaseAuth.getInstance()
                        .signOut();
                intent = new Intent(this, IndexActivity.class);
                startActivity(intent);
                finish();
            });
        }
        // Cerrar el PopupWindow cuando se destruya la actividad
    }

    private void comprobarLauncher() {
        if (MetodosGlobales.comprobarLogueado(IndexActivity.this, ivImagenLogin)) {
            botonImagenLogueado();
            mostrarAdapterRestaurantes();
        } else {
            Glide.with(this).load(R.drawable.nologinimg).into(ivImagenLogin);
            botonImagenNoLogueado();
        }
    }

    private void mostrarAdapterRestaurantes() {
        rvRestaurantesUsuario.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        restaurantesAdapter = new RestaurantesAdapter();
        rvRestaurantesUsuario.setAdapter(restaurantesAdapter);
        restaurantesAdapter.setmListener(this);
        if (currentUser != null) {
            cargarRestaurantesBd();
        }

    }

    private void cargarRestaurantesBd() {
        if (currentUser != null) {
            restaurantesBd = new ArrayList<>();
            String usuarioId = currentUser.getEmail();
            DocumentReference id = db.collection("users").document(usuarioId);

            CollectionReference restaurantesRef = db.collection("restaurantes");

            Query consulta = restaurantesRef.whereEqualTo("usuarioId", usuarioId);

            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        String nombreRestaurante = document.getString("nombreRestaurante");
                        Restaurantes restaurante = new Restaurantes(nombreRestaurante, "");
                        restaurantesBd.add(restaurante);
                    }
                    restaurantesAdapter.setResultsRestaurantes(restaurantesBd);
                }

            });

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


    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
        intent = new Intent(this, ParticipantesActivity.class);
        intent.putExtra("nombreRestaurante", restaurantesBd.get(position).getNombreRestaurante());
        intent.putExtra("enviarRestaurante",true);
        startActivity(intent);
        finish();
    }
}