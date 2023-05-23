package mainActivity.menu.crudBd;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.tfg.marfol.R;
public class Seleccion extends AppCompatActivity {
    private Button btnCrudPersona, btnCrudRestaurantes, btnCrudPlatos, btnCrudVolver;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion);
        assignarId();

        btnCrudPersona.setOnClickListener(v -> {
            intent = new Intent(Seleccion.this,EditarPersonasBd.class);
            startActivity(intent);
        });
        btnCrudRestaurantes.setOnClickListener(v ->{
            intent = new Intent(Seleccion.this, EditarRestaurantesBd.class);
            startActivity(intent);
        });

        btnCrudVolver.setOnClickListener(v -> finish());
    }

    private void assignarId() {
        btnCrudPersona = findViewById(R.id.btnCrudPersona);
        btnCrudRestaurantes = findViewById(R.id.btnCrudRestaurantes);
        btnCrudPlatos = findViewById(R.id.btnCrudPlatos);
        btnCrudVolver = findViewById(R.id.btnCrudVolver);

    }
}