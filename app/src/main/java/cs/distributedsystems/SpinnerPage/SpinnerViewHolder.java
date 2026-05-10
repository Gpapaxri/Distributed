package cs.distributedsystems.SpinnerPage;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
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

        float density = itemView.getResources().getDisplayMetrics().density;
        float radius  = density * 10f;

        if (item.get_Multiplier().equals("JACKPOT")) {

            GradientDrawable bg = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{
                            Color.parseColor("#FFD700"),
                            Color.parseColor("#FFA500"),
                            Color.parseColor("#FFD700")
                    }
            );
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(radius);
            bg.setStroke((int)(density * 2), Color.parseColor("#FF8C00"));
            colorCard.setBackground(bg);

            multiplierText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            multiplierText.setTypeface(null, android.graphics.Typeface.BOLD);

        } else {
            // ── Κανονικό flat χρώμα για τους υπόλοιπους ──────────────────
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(radius);
            bg.setColor(item.get_BackgroundColor());
            colorCard.setBackground(bg);

            multiplierText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            multiplierText.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
}