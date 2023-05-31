package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.marfol.R;

import java.util.ArrayList;

import entities.Historial;
public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialAdapterResultHolder> {
    private ArrayList<Historial> resultsHistorial = new ArrayList<>();
    private onItemClickListener mListener;


    @NonNull
    @Override
    public HistorialAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_historial, parent, false);
        return new HistorialAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialAdapterResultHolder holder, int position) {
        holder.tvHistorial.setText(resultsHistorial.get(position).getFecha()+"\n---------\n"+resultsHistorial.get(position).getRestaurante());
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
        return resultsHistorial.size();
    }

    public void setResultsHistorial(ArrayList<Historial> resultsHistorial) {

        this.resultsHistorial = resultsHistorial;
        notifyDataSetChanged();
    }

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }


    class HistorialAdapterResultHolder extends RecyclerView.ViewHolder {
        private TextView tvHistorial;

        public HistorialAdapterResultHolder(@NonNull View itemView) {
            super(itemView);
            tvHistorial = itemView.findViewById(R.id.tvHistorial);

        }
    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }


}
