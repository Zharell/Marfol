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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.tfg.marfol.R;
import java.util.ArrayList;
import entities.Plato;

public class CrudPlatosAdapter extends RecyclerView.Adapter<CrudPlatosAdapter.CrudPlatosAdapterResultHolder> {

    private ArrayList<Plato> resultsPlato = new ArrayList<>();
    private onItemClickListener mListener;

    @NonNull
    @Override
    public CrudPlatosAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_crud_platos, parent, false);

        return new CrudPlatosAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CrudPlatosAdapterResultHolder holder, int position) {
        holder.tvPlatoRow.setText(resultsPlato.get(position).getNombre()+"\n---------\n"+resultsPlato.get(position).getRestaurante());
        if(resultsPlato.get(position).getUrlImage()!=null) {
            Glide.with(holder.itemView)
                    .load(Uri.parse(resultsPlato.get(position).getUrlImage()))
                    .transform(new RoundedCorners(100)) // Especifica el radio de redondeo en píxeles
                    .into(holder.ivPlatoRow);
            if(!resultsPlato.get(position).getUrlImage().equalsIgnoreCase("")){
                holder.ivPlatoRow.setBackground(null);
            }

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

    class CrudPlatosAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPlatoRow;
        private ImageView ivPlatoRow;

        public CrudPlatosAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPlatoRow = itemView.findViewById(R.id.tvCrudPlatoNombre);
            ivPlatoRow = itemView.findViewById(R.id.ivCrudPlato);

        }

    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

}