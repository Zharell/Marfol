package adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.marfol.R;

import java.util.ArrayList;

import entities.Persona;

public class PersonaCompartirAdapter extends RecyclerView.Adapter<PersonaCompartirAdapter.PersonaCompartirAdapterResultHolder> {

    private ArrayList<Persona> resultsPersonaCom = new ArrayList<>();

    //añadirPersona siempre se añade en la posición 0 ya que su función es redirigir a otra actividad distinta
    //Tiene la ruta URI de su imagen por defecto
    private Persona anadirPersona = new Persona(0, "", "", "android.resource://com.tfg.marfol/"+R.drawable.add_icon , new ArrayList<>(),0);
    private onItemClickListener mListener;

    @NonNull
    @Override
    public PersonaCompartirAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_platoanadirpersona, parent, false);

        return new PersonaCompartirAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonaCompartirAdapterResultHolder holder, int position) {

        //Insertamos para cada persona en el Recycler su nombre
        holder.tvPersonaRow.setText(resultsPersonaCom.get(position).getNombre());

        //Insertamos precio para cada persona a compartir
        //holder.tvPrecioRow.setText();

        //Insertamos para cada persona en el Recycler su imagen
        holder.ivPersonaRow.setImageURI(Uri.parse(resultsPersonaCom.get(position).getUrlImage()));

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

    public void setResultsPersonaCom(ArrayList<Persona> resultsPersonaCom) {
        //Comprueba si está vacío para añadir el primer elemento ( Añadir Persona ) si no, no hace nada
        if (resultsPersonaCom.size()==0) {
            resultsPersonaCom.add(0, anadirPersona);
        }
        this.resultsPersonaCom = resultsPersonaCom;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return resultsPersonaCom.size();
    }

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }

    class PersonaCompartirAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPersonaRow, tvPrecioRow;
        private ImageView ivPersonaRow;

        public PersonaCompartirAdapterResultHolder(@NonNull View itemView) {
            super(itemView);
            tvPersonaRow = itemView.findViewById(R.id.tvPlatoanadirPersonaRow);
            ivPersonaRow = itemView.findViewById(R.id.ivPlatoanadirPersonaRow);
        }

    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

}
