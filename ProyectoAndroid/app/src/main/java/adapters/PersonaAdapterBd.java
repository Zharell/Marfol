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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tfg.marfol.R;

import java.util.ArrayList;
import java.util.List;

import entities.Persona;

public class PersonaAdapterBd extends RecyclerView.Adapter<PersonaAdapterBd.PersonaBdAdapterResultHolder> {

    private List<Persona> resultsPersonaBd = new ArrayList<>();

    private onItemClickListenerBd mListener;

    @NonNull
    @Override
    public PersonaBdAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_persona_bd, parent, false);

        return new PersonaBdAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonaBdAdapterResultHolder holder, int position) {

        //Insertamos para cada persona en el Recycler su nombre
        holder.tvPersonaRow.setText(resultsPersonaBd.get(position).getNombre());

        //holder.ivPersonaRow.setImageURI(Uri.parse(resultsPersona.get(position).getUrlImage()));
        if(resultsPersonaBd.get(position).getUrlImage()!=null && !resultsPersonaBd.get(position).getUrlImage().equalsIgnoreCase("")) {
            Glide.with(holder.itemView)
                    .load(Uri.parse(resultsPersonaBd.get(position).getUrlImage()))
                    .circleCrop().into(holder.ivPersonaRow);
        } else {
            //Limpia en el caso de que no haya cargado la imagen
            Glide.with(holder.itemView).clear(holder.ivPersonaRow);
        }

        //Método onclick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClickBd(position);
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultsPersonaBd.size();
    }

    public void setResultsPersonaBd(List<Persona> resultsPersona) {
        this.resultsPersonaBd = resultsPersona;
        notifyDataSetChanged();
    }

    public void setmListener(onItemClickListenerBd mListener) {
        this.mListener = mListener;
    }

    class PersonaBdAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPersonaRow;
        private ImageView ivPersonaRow;

        public PersonaBdAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPersonaRow = itemView.findViewById(R.id.tvPersonaRow);
            ivPersonaRow = itemView.findViewById(R.id.ivPersonaRow);

        }

    }

    public interface onItemClickListenerBd{
        void onItemClickBd(int position);
    }

}
