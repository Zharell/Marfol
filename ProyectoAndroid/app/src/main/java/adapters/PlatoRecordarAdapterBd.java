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

public class PlatoRecordarAdapterBd extends RecyclerView.Adapter<PlatoRecordarAdapterBd.PlatoRecordarAdapterBdResultHolder> {

    private ArrayList<Plato> resultsPlatoBd = new ArrayList<>();
    private NumberFormat euroFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private onItemClickListenerBd mListenerBd;

    @NonNull
    @Override
    public PlatoRecordarAdapterBdResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_plato_recordar_bd, parent, false);

        return new PlatoRecordarAdapterBdResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatoRecordarAdapterBdResultHolder holder, int position) {

        holder.tvPlatoRow.setText(resultsPlatoBd.get(position).getNombre());


        holder.tvPrecioRow.setText(String.valueOf(euroFormat.format(resultsPlatoBd.get(position).getPrecio())));

        //Insertamos para cada persona en el Recycler su imagen
        if (resultsPlatoBd.get(position).getUrlImage() != null && !resultsPlatoBd.get(position).getUrlImage().equalsIgnoreCase("")) {
            Glide.with(holder.itemView)
                    .load(resultsPlatoBd.get(position).getUrlImage())
                    .into(holder.ivPlatoRow);
        } else {
            Glide.with(holder.itemView).clear(holder.ivPlatoRow);
        }

        //Método onclick
        holder.itemView.setOnClickListener(view -> {
            if (mListenerBd != null) {
                int position1 = holder.getAdapterPosition();
                if (position1 != RecyclerView.NO_POSITION) {
                    mListenerBd.onItemClickBd(position1);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultsPlatoBd.size();
    }

    public void setResultsPlatoBd(ArrayList<Plato> resultsPlatoBd) {
        this.resultsPlatoBd = resultsPlatoBd;
        notifyDataSetChanged();
    }

    public void setmListenerBd(onItemClickListenerBd mListenerBd) {
        this.mListenerBd = mListenerBd;
    }

    class PlatoRecordarAdapterBdResultHolder extends RecyclerView.ViewHolder {
        private TextView tvPlatoRow, tvPrecioRow;
        private ImageView ivPlatoRow;

        public PlatoRecordarAdapterBdResultHolder(@NonNull View itemView) {
            super(itemView);
            tvPlatoRow = itemView.findViewById(R.id.tvPlatoRecordarRow);
            tvPrecioRow = itemView.findViewById(R.id.tvPrecioRecordarRow);
            ivPlatoRow = itemView.findViewById(R.id.ivPlatoRecordarRow);
        }

    }

    public interface onItemClickListenerBd {
        void onItemClickBd(int position);
    }

}
