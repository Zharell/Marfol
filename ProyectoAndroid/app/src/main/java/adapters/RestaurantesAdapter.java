package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.marfol.R;
import java.util.ArrayList;
import entities.Restaurantes;

public class RestaurantesAdapter extends RecyclerView.Adapter<RestaurantesAdapter.RestaurantesAdapterResultHolder> {
    private ArrayList<Restaurantes> resultsRestaurantes = new ArrayList<>();
    private onItemClickListenerRestaurantes mListener;



    @NonNull
    @Override
    public RestaurantesAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_restaurantes,parent,false);
        return new RestaurantesAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantesAdapterResultHolder holder, int position) {
        holder.tvRestauranteUsuario.setText(resultsRestaurantes.get(position).getNombreRestaurante());

        holder.itemView.setOnClickListener(view -> {
            if(mListener != null) {
                int position1 = holder.getAdapterPosition();
                if(position1 != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position1);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultsRestaurantes.size();
    }

    public void setResultsRestaurantes(ArrayList<Restaurantes> resultsRestaurantes){

        this.resultsRestaurantes = resultsRestaurantes;
        notifyDataSetChanged();
    }

    public void setmListener(onItemClickListenerRestaurantes mListener){
        this.mListener = mListener;
    }

    class RestaurantesAdapterResultHolder extends RecyclerView.ViewHolder {
        private TextView tvRestauranteUsuario;
        public RestaurantesAdapterResultHolder(@NonNull View itemView) {
            super(itemView);
            tvRestauranteUsuario = itemView.findViewById(R.id.tvRestauranteUsuario);
        }
    }


    public interface onItemClickListenerRestaurantes{
        void onItemClick(int position);
    }
}
