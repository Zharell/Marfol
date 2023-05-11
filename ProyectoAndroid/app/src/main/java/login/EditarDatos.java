package login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;

import java.util.HashMap;
import java.util.Map;

import mainActivity.IndexActivity;


public class EditarDatos extends AppCompatActivity {
    private EditText etNombreUsuario, etTelefonoUsuario;
    private Button btnGuardarBD;
    private FirebaseFirestore db;
    private String email, provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_datos);

        // Obtenemos la instancia de Firebase
        db = FirebaseFirestore.getInstance();

        // Obtenemos las referencias a los elementos del layout
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etTelefonoUsuario = findViewById(R.id.etTelefonoUsuario);
        btnGuardarBD = findViewById(R.id.btnGuardarBD);

        // Obtenemos los datos del usuario
        SharedPreferences prefAux = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        email = prefAux.getString("email", "");
        provider = prefAux.getString("provider", "");
        etNombreUsuario.setText(prefAux.getString("name", ""));
        etTelefonoUsuario.setText(prefAux.getString("phone", ""));

        // Configuramos el botón de guardar
        btnGuardarBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarDatos();
            }
        });
    }

    private void actualizarDatos() {
        // Creamos el mapa con los datos a actualizar
        Map<String, Object> map = new HashMap<>();
        map.put("provider", provider);
        map.put("name", etNombreUsuario.getText().toString());
        map.put("phone", etTelefonoUsuario.getText().toString());

        // Verificamos si el documento del usuario ya existe en la base de datos
        db.collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // El documento del usuario ya existe, actualizamos sus datos
                                db.collection("users").document(email).update(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Mostramos el mensaje de actualizado
                                                Toast.makeText(EditarDatos.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                                finish();
                                                // Esperamos 2 segundos antes de finalizar la actividad
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Mostramos el mensaje de error
                                                Toast.makeText(EditarDatos.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // El documento del usuario no existe, creamos uno nuevo con sus datos
                                db.collection("users").document(email).set(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Mostramos el mensaje de actualizado
                                                Toast.makeText(EditarDatos.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                                // Esperamos 2 segundos antes de finalizar la actividad
                                                finish();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Mostramos el mensaje de error
                                                Toast.makeText(EditarDatos.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                        } else {
                            // Mostramos el mensaje de error
                            Toast.makeText(EditarDatos.this, "Error al verificar los datos del usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}



