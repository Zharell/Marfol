package login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.tfg.marfol.R;

import mainActivity.IndexActivity;

enum ProviderType{
    BASIC,
    GOOGLE
}
public class HomeActivity extends AppCompatActivity {
    Button btnLogoutHome;
    TextView etEmailHome;
    TextView etProviderHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnLogoutHome=findViewById(R.id.btnLogoutHome);
        etEmailHome=findViewById(R.id.etEmailHome);
        etProviderHome=findViewById(R.id.etProviderHome);
        Bundle extras= getIntent().getExtras();
        String email= extras.getString("EMAIL");
        String provider=extras.getString("PROVIDER");
        //setup
        setup(email,provider);

        // Guardado de datos
        SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefs = prefAux.edit();
        prefs.putString("email",email);
        prefs.putString("provider",provider);
        prefs.apply();




    }

    private void setup(String email, String provider) {
        String title= "Inicio";
        etEmailHome.setText(email);
        etProviderHome.setText(provider);
        btnLogoutHome.setOnClickListener(v -> {
            //Borrado de datos
            SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor prefs = prefAux.edit();
            prefs.clear();
            prefs.apply();
            FirebaseAuth.getInstance()
                    .signOut();
            onBackPressed();
        });
    }


}