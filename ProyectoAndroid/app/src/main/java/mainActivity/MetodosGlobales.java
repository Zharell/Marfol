package mainActivity;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;

import org.w3c.dom.Text;

public class MetodosGlobales {
    public static void cambiarImagenSiLogueado(Context context, ImageView iVimagen) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String imagen = document.getString("imagen");
                            if (imagen != null) {
                                iVimagen.setClickable(false);  // Deshabilitar el clic en la imagen
                                iVimagen.setFocusable(false);
                                iVimagen.setPadding(20, 20, 20, 20);
                                Glide.with(context)
                                        .load(imagen)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            } else {
                                Glide.with(context)
                                        .load(R.drawable.nologinimg)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            }
                        } else {
                            Glide.with(context).load(R.drawable.nologinimg).into(iVimagen);
                        }
                    } else {
                        Toast.makeText(context, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(context, "No estás logueado", Toast.LENGTH_SHORT).show();
            Glide.with(context).load(R.drawable.nologinimg).into(iVimagen);
        }
    }

    public static boolean comprobarLogueado(Context context, ImageView iVimagen) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String imagen = document.getString("imagen");
                            if (imagen != null) {
                                iVimagen.setPadding(20, 20, 20, 20);
                                Glide.with(context)
                                        .load(imagen)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            } else {
                                Glide.with(context)
                                        .load(R.drawable.nologinimg)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            }
                        } else {
                            Glide.with(context).load(R.drawable.nologinimg).into(iVimagen);
                        }
                    } else {
                        Toast.makeText(context, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return true;
        } else {
            Toast.makeText(context, "No estás logueado", Toast.LENGTH_SHORT).show();
            Glide.with(context).load(R.drawable.nologinimg).into(iVimagen);
            return false;
        }
    }
    public static boolean comprobarLogueadoEditar(Context context, ImageView iVimagen) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String imagen = document.getString("imagen");
                            if (imagen != null) {
                                iVimagen.setPadding(20, 20, 20, 20);
                                Glide.with(context)
                                        .load(imagen)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            } else {
                                Glide.with(context)
                                        .load(R.drawable.nologinimg)
                                        .circleCrop() // Aplica el formato redondeado
                                        .into(iVimagen);
                            }
                        } else {
                            Glide.with(context).load(R.drawable.nologinimg).into(iVimagen);
                        }
                    } else {
                        Toast.makeText(context, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return true;
        } else {
            Toast.makeText(context, "No estás logueado", Toast.LENGTH_SHORT).show();
            Glide.with(context).load(R.drawable.nologinimg).into(iVimagen);
            return false;
        }
    }

    public static void cargarDatosEnHomeSiLogueado(TextView email, TextView nombre, TextView telefono) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String userEmail = document.getString("email");
                            String userNombre = document.getString("name");
                            String userTelefono = document.getString("phone");

                            if (userEmail != null) {
                                email.setText(userEmail);
                            }
                            if (userNombre != null) {
                                nombre.setText(userNombre);
                            }
                            if (userTelefono != null) {
                                telefono.setText(userTelefono);
                            }
                        }
                    }
                }
            });
        }
    }
    public static void cargarDatosEnEditarSiLogueado(EditText nombre, EditText telefono) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String userNombre = document.getString("name");
                            String userTelefono = document.getString("phone");

                            if (userNombre != null) {
                                nombre.setText(userNombre);
                            }
                            if (userTelefono != null) {
                                telefono.setText(userTelefono);
                            }
                        }
                    }
                }
            });
        }
    }
}
