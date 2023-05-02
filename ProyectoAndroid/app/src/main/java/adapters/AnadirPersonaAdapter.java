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
import java.util.List;

import entities.Plato;

public class AnadirPersonaAdapter extends RecyclerView.Adapter<AnadirPersonaAdapter.AnadirPersonaAdapterResultHolder> {

    private List<Plato> resultsPlato = new ArrayList<>();

    //añadirPlato siempre se añade en la posición 0 ya que su función es redirigir a otra actividad distinta
    Plato anadirPlato = new Plato("", "" ,0, 0, "android.resource://com.tfg.marfol/2131230972",false);

    private PersonaAdapter.onItemClickListener mListener;

    @NonNull
    @Override
    public AnadirPersonaAdapter.AnadirPersonaAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_platoanadirpersona, parent, false);

        return new AnadirPersonaAdapter.AnadirPersonaAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AnadirPersonaAdapterResultHolder holder, int position) {

        //Insertamos para cada persona en el Recycler su nombre
        holder.tvPlatoRow.setText(resultsPlato.get(position).getNombre());

        //Únicamente insertará la imagén del primer Plato ya que se encarga de añadir
        if (position==0) {
            holder.ivPlatoRow.setImageURI(Uri.parse(resultsPlato.get(0).getUrlImage()));
        }

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
        return resultsPlato.size();
    }

    public void setResultsPlato(List<Plato> resultsPlato) {

        //Comprueba si está vacío para añadir el primer elemento ( Añadir Persona ) si no, no hace nada
        if (resultsPlato.size()==0) {
            resultsPlato.add(0, anadirPlato);
        }

        this.resultsPlato = resultsPlato;
        notifyDataSetChanged();

    }

    public void setmListener(PersonaAdapter.onItemClickListener mListener) {
        this.mListener = mListener;
    }

    class AnadirPersonaAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPlatoRow;
        private ImageView ivPlatoRow;

        public AnadirPersonaAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPlatoRow = itemView.findViewById(R.id.tvPlatoanadirPersonaRow);
            ivPlatoRow = itemView.findViewById(R.id.ivPlatoanadirPersonaRow);

        }

    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

}
