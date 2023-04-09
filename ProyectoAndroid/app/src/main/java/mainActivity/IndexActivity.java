package mainActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.tfg.marfol.R;


public class IndexActivity extends AppCompatActivity {

    Button btnApIndex;
    ImageView ivLoginIndex, ivMenuIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        //Método que asigna IDs a los elementos
        asignarId();

        ivLoginIndex.setPadding(10, 10, 10, 10);

        //Puesto provisional para probar cosas
        ivLoginIndex.setOnClickListener(view -> {
            Intent intent = new Intent(this, login.AuthActivity.class);
            startActivity(intent);
        });

    }

    public void asignarId() {

        btnApIndex = findViewById(R.id.btnApIndex);
        ivLoginIndex = findViewById(R.id.ivLoginIndex);
        ivMenuIndex = findViewById(R.id.ivMenuIndex);

    }


}