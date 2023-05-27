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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import entities.Plato;

public class PlatoRecordarAdapter extends RecyclerView.Adapter<PlatoRecordarAdapter.PlatoRecordarAdapterResultHolder> {

    private ArrayList<Plato> resultsPlato = new ArrayList<>();
    private NumberFormat euroFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private onItemClickListener mListener;

    @NonNull
    @Override
    public PlatoRecordarAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_plato_recordar, parent, false);

        return new PlatoRecordarAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatoRecordarAdapterResultHolder holder, int position) {

        //Insertamos para cada plato en el Recycler su nombre
        if (position==0) {
            holder.tvPlatoRow.setText("NUEVO");
        } else {
            holder.tvPlatoRow.setText(resultsPlato.get(position).getNombre());
        }

        //Insertamos para cada plato en el Recycler su precio
        if (position==0) {
            holder.tvPrecioRow.setText("PLATO");
        } else {
            holder.tvPrecioRow.setText(String.valueOf(euroFormat.format(resultsPlato.get(position).getPrecio())));
        }

        //Insertamos para cada plato en el Recycler su imagen
        if (resultsPlato.get(position).getUrlImage() != null && !resultsPlato.get(position).getUrlImage().equalsIgnoreCase("")) {
            Glide.with(holder.itemView)
                    .load(resultsPlato.get(position).getUrlImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivPlatoRow);
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

    public void setResultsPlato(ArrayList<Plato> resultsPlato) {
        this.resultsPlato = resultsPlato;
        notifyDataSetChanged();
    }

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }

    class PlatoRecordarAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPlatoRow, tvPrecioRow;
        private ImageView ivPlatoRow;

        public PlatoRecordarAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPlatoRow = itemView.findViewById(R.id.tvPlatoRecordarRow);
            tvPrecioRow = itemView.findViewById(R.id.tvPrecioRecordarRow);
            ivPlatoRow = itemView.findViewById(R.id.ivPlatoRecordarRow);

        }

    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

}
