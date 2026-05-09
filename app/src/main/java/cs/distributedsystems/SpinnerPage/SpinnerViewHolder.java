package cs.distributedsystems.SpinnerPage;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cs.distributedsystems.R;

public class SpinnerViewHolder extends RecyclerView.ViewHolder {

    private final View colorCard;
    private final TextView multiplierText;

    public SpinnerViewHolder(@NonNull View itemView) {
        super(itemView);
        colorCard = itemView.findViewById(R.id.colorCard);
        multiplierText = itemView.findViewById(R.id.multiplierText);
    }

    void bind(SpinnerMultiplier item) {
        multiplierText.setText(item.get_Multiplier());
        multiplierText.setTextColor(item.get_TextColor());

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        float radius = itemView.getResources().getDisplayMetrics().density * 10f;
        bg.setCornerRadius(radius);
        bg.setColor(item.get_BackgroundColor());
        colorCard.setBackground(bg);
    }
}