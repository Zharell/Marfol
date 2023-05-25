package adapters;

import android.net.Uri;
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

import entities.Persona;

public class ListaCompartirAdapter extends RecyclerView.Adapter<ListaCompartirAdapter.ListaCompartirAdapterResultHolder> {

    private ArrayList<Persona> resultsListCompartir = new ArrayList<>();

    private onItemClickListener mListener;

    @NonNull
    @Override
    public ListaCompartirAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_listacompartir, parent, false);

        return new ListaCompartirAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListaCompartirAdapterResultHolder holder, int position) {

        //Insertamos para cada persona en el Recycler su nombre
        holder.tvPersonaRow.setText(resultsListCompartir.get(position).getNombre());

        //Insertamos para cada persona en el Recycler su imagen
        if (resultsListCompartir.get(position).getUrlImage() != null && !resultsListCompartir.get(position).getUrlImage().equalsIgnoreCase("")) {
            Glide.with(holder.itemView).load(resultsListCompartir.get(position).getUrlImage()).into(holder.ivPersonaRow);
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
        return resultsListCompartir.size();
    }

    public void setResultsListCompartir(ArrayList<Persona> resultsListCompartir) {
        this.resultsListCompartir = resultsListCompartir;
    }

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }

    class ListaCompartirAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPersonaRow;
        private ImageView ivPersonaRow;

        public ListaCompartirAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPersonaRow = itemView.findViewById(R.id.tvListaCompartirRow);
            ivPersonaRow = itemView.findViewById(R.id.ivListaCompartirRow);

        }

    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

}
