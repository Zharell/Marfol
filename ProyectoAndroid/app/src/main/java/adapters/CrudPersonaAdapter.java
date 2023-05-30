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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.tfg.marfol.R;

import java.util.ArrayList;
import java.util.List;

import entities.Persona;

public class CrudPersonaAdapter extends RecyclerView.Adapter<CrudPersonaAdapter.CrudPersonaAdapterResultHolder> {

    private List<Persona> resultsCrudPersona = new ArrayList<>();

    private onItemClickListenerCrudPersona mListener;

    @NonNull
    @Override
    public CrudPersonaAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_crud_persona, parent, false);

        return new CrudPersonaAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CrudPersonaAdapterResultHolder holder, int position) {

        //Insertamos para cada persona en el Recycler su nombre
        holder.tvPersonaRow.setText(resultsCrudPersona.get(position).getNombre());

        //Insertamos para cada persona en el Recycler su imagen
        if(resultsCrudPersona.get(position).getUrlImage()!=null) {
            Glide.with(holder.itemView)
                    .load(Uri.parse(resultsCrudPersona.get(position).getUrlImage()))
                    .circleCrop() // Especifica el radio de redondeo en píxeles
                    .into(holder.ivPersonaRow);
            if(!resultsCrudPersona.get(position).getUrlImage().equalsIgnoreCase("")){
                holder.ivPersonaRow.setBackground(null);
            }

        } else {
            Glide.with(holder.itemView).clear(holder.ivPersonaRow);
        }

        //Método onclick
        holder.itemView.setOnClickListener(view -> {
            if (mListener != null) {
                int position1 = holder.getAdapterPosition();
                if (position1 != RecyclerView.NO_POSITION) {
                    mListener.onItemClickCrudPersona(position1);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultsCrudPersona.size();
    }

    public void setResultsCrudPersona(List<Persona> resultsPersona) {
        this.resultsCrudPersona = resultsPersona;
        notifyDataSetChanged();
    }

    public void setmListener(onItemClickListenerCrudPersona mListener) {
        this.mListener = mListener;
    }

    class CrudPersonaAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPersonaRow;
        private ImageView ivPersonaRow;

        public CrudPersonaAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPersonaRow = itemView.findViewById(R.id.tvCrudPersona);
            ivPersonaRow = itemView.findViewById(R.id.ivCrudPersona);

        }

    }

    public interface onItemClickListenerCrudPersona{
        void onItemClickCrudPersona(int position);
    }

}
