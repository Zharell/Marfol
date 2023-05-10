package adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tfg.marfol.R;

import java.util.ArrayList;
import java.util.List;

import entities.Persona;
import entities.Plato;

public class PersonaAdapter extends RecyclerView.Adapter<PersonaAdapter.PersonaAdapterResultHolder> {

    private ArrayList<Persona> resultsPersona = new ArrayList<>();

    //añadirPersona siempre se añade en la posición 0 ya que su función es redirigir a otra actividad distinta
    //Tiene la ruta URI de su imagen por defecto
    private Persona anadirPersona = new Persona("Añadir Persona", "", "android.resource://com.tfg.marfol/"+R.drawable.add_icon , new ArrayList<>());
    private onItemClickListener mListener;

    @NonNull
    @Override
    public PersonaAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_persona, parent, false);

        return new PersonaAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonaAdapterResultHolder holder, int position) {

        //Insertamos para cada persona en el Recycler su nombre
        holder.tvPersonaRow.setText(resultsPersona.get(position).getNombre());
        Log.d("Posiciones",String.valueOf(position));
        //Insertamos para cada persona en el Recycler su imagen
        holder.ivPersonaRow.setImageURI(Uri.parse(resultsPersona.get(position).getUrlImage()));

        //Método onclick
        holder.itemView.setOnClickListener(view -> {
            if (mListener != null) {
                int position1 = holder.getAdapterPosition();
                if (position1 != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position1);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultsPersona.size();
    }

    public void setResultsPersona(ArrayList<Persona> resultsPersona) {

        //Comprueba si está vacío para añadir el primer elemento ( Añadir Persona ) si no, no hace nada
        if (resultsPersona.size()==0) {
            resultsPersona.add(0, anadirPersona);
        }

        this.resultsPersona = resultsPersona;
        notifyDataSetChanged();

    }

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }

    class PersonaAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPersonaRow;
        private ImageView ivPersonaRow;

        public PersonaAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPersonaRow = itemView.findViewById(R.id.tvPersonaRow);
            ivPersonaRow = itemView.findViewById(R.id.ivPersonaRow);

        }

    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

}
