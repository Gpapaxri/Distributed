package cs.distributedsystems.SpinnerPage;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import cs.distributedsystems.R;

public class SpinnerAdapter extends RecyclerView.Adapter<SpinnerViewHolder> {

    private static final int visible_items = 5;
    private final List<SpinnerMultiplier> multipliers;
    private final int totalMultipliers;


    public SpinnerAdapter(List<SpinnerMultiplier> multipliers, int totalMultipliers) {
        this.multipliers = multipliers;
        this.totalMultipliers = totalMultipliers;
    }

    @NonNull
    @Override
    public SpinnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner, parent, false);

        int rvHeight = parent.getHeight();
        int itemHeight = rvHeight > 0 ? rvHeight / visible_items : fallbackItemHeight(parent);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
        } else {
            params.height = itemHeight;
        }
        view.setLayoutParams(params);

        return new SpinnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpinnerViewHolder holder, int position) {
        holder.bind(multipliers.get(position % multipliers.size()));
    }

    @Override
    public int getItemCount() {return totalMultipliers;}

    private int fallbackItemHeight(ViewGroup parent) {
        DisplayMetrics dm = parent.getResources().getDisplayMetrics();
        return (int)(dm.heightPixels * 0.14f);
    }
}