package mainActivity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;

public class MetodosGlobales {
    public static boolean comprobarLogueado(Context context, ImageView iVimagen) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String imagen = document.getString("imagen");
                        if (imagen != null&&!imagen.equalsIgnoreCase("")) {
                            iVimagen.setPadding(20,20,20,20);
                            iVimagen.setBackground(null);
                            try {
                                Glide.with(context)
                                        .load(imagen)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            } catch (IllegalArgumentException e) {
                            }
                        } else {
                            try {
                                Glide.with(context)
                                        .load(R.drawable.camera)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            } catch (IllegalArgumentException e) {
                            }
                        }
                    } else {
                        try {
                            Glide.with(context)
                                    .load(R.drawable.camera)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(iVimagen);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                } else {
                    Toast.makeText(context, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } else {
            Toast.makeText(context, "No estás logueado", Toast.LENGTH_SHORT).show();
            Glide.with(context)
                    .load(R.drawable.camera)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iVimagen);
            return false;
        }
    }


}
