package cs.distributedsystems.HomePage;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs.distributedsystems.R;
import gr.softeng.distributedsystems.Entities.Game;


public class GameRecyclerViewAdapter extends RecyclerView.Adapter<GameRecyclerViewAdapter.ViewHolder> {
    private HomePageView view;
    private List<Game> games;

    public void setView(HomePageView view) {
        this.view = view;
    }
    public GameRecyclerViewAdapter(List<Game> games){
        this.games = games;
    }
    public void setGames(List<Game> games) {
        this.games = games;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Game dataItem = games.get(position);
        holder.gameName.setText(dataItem.getGameName());
        holder.risk.setText("Risk Level: " + dataItem.getRiskLevel());
        holder.range.setText("Bet Range: " + dataItem.getMinBet() + " - " + dataItem.getMaxBet());
        holder.jackpot.setText("Jackpot: " + dataItem.getJackpot());
        holder.playBt.setOnClickListener(view -> initPlayPage(dataItem));
        holder.rateBt.setOnClickListener( view -> initRateWindow(dataItem));

        setGameImage(holder.image, dataItem);
    }
    private void initPlayPage(Game game){
        this.view.createPlayPage(game);
    }

    private void initRateWindow(Game game){this.view.createRateWindow(game);}

    private void setGameImage(ImageView imageView, Game game) {

        view.getLogo(imageView, game.getGameLogo());
    }

    @Override
    public int getItemCount() {
        return games.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView gameName;
        public TextView risk;
        public TextView range;
        public TextView jackpot;
        public Button playBt;
        public Button rateBt;
        public ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            gameName = itemView.findViewById(R.id.gameName);
            risk = itemView.findViewById(R.id.risk);
            range = itemView.findViewById(R.id.range);
            jackpot = itemView.findViewById(R.id.jackpot);
            playBt = itemView.findViewById(R.id.playButton);
            rateBt = itemView.findViewById(R.id.rateButton);
            image = itemView.findViewById(R.id.imageView2);
        }
    }
}
