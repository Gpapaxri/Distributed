package cs.distributedsystems.PlayPage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cs.distributedsystems.HomePage.HomePageActivity;
import cs.distributedsystems.R;
import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;


public class PlayPage extends AppCompatActivity implements PlayPageView{
    private PopupWindow wonPopup;
    private LayoutInflater inflater;
    private PlayPageViewModel viewModel;
    private PlayPagePresenter presenter;
    private TextView betAmount;
    private String gameName;
    private Game game;
    private ActivityResultLauncher<Intent> playerHomePageLauncher;
    private ImageView ivWheel;
    private Button btnPlay;
    private int degreesTotal;
    //private final int[] degrees = {0, 38, 73, 109, 146, 183, 219, 254, 290, 325};
    private final int[] degrees = {10, 40, 75, 110, 145, 182, 218, 254, 287, 316, 343};
    private final Map<Double, Integer> lowMultipliers = new HashMap<>();
    private final Map<Double, Integer> mediumMultipliers = new HashMap<>();
    private final Map<Double, Integer> highMultipliers = new HashMap<>();
    private double result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play_page);

        viewModel = new ViewModelProvider(this).get(PlayPageViewModel.class);
        presenter = viewModel.getPresenter();
        presenter.setView(this);

        playerHomePageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {finish();});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inflater = getLayoutInflater();

        betAmount = findViewById(R.id.addBet);

        btnPlay = findViewById(R.id.playGame);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playGame();
                betAmount.setText("");
            }
        });

        TextView backButton = findViewById(R.id.BackPlayPage);
        backButton.setOnClickListener(view -> back());

        ivWheel = findViewById(R.id.ivWheel);

        game = (Game) getIntent().getSerializableExtra("Game");

        assert game != null;
        gameName = game.getGameName();

        ImageView target = findViewById(R.id.ivTarget);

        if(game.getRiskLevel().equals("Low")){
            ivWheel.setImageResource(R.drawable.wheel12);
            target.setImageResource(R.drawable.arrow);
        }else if(game.getRiskLevel().equals("Medium")){
            ivWheel.setImageResource(R.drawable.wheel22);
            target.setImageResource(R.drawable.arrowblack);
        }else{
            ivWheel.setImageResource(R.drawable.wheel32);
            target.setImageResource(R.drawable.arrowblack);
        }

        lowMultipliers.put(10.0d, 10);
        lowMultipliers.put(0.1d, 7);
        lowMultipliers.put(0.5d, 6);
        lowMultipliers.put(1.0d, 5);
        lowMultipliers.put(1.1d, 4);
        lowMultipliers.put(1.3d, 3);
        lowMultipliers.put(2.0d, 2);
        lowMultipliers.put(2.5d, 1);

        mediumMultipliers.put(3.5d, 2);
        mediumMultipliers.put(2.5d, 3);
        mediumMultipliers.put(1.5d, 4);
        mediumMultipliers.put(1.0d, 5);
        mediumMultipliers.put(0.5d, 6);
        mediumMultipliers.put(20.0d, 10);

        highMultipliers.put(6.5d, 3);
        highMultipliers.put(2.0d, 4);
        highMultipliers.put(1.0d, 5);
        highMultipliers.put(40.0d, 10);
    }
    @SuppressLint("SetTextI18n")
    private void createWonWindow(boolean isJackpot){
        View popupView;
        if (isJackpot) {
            popupView = inflater.inflate(R.layout.jackpot_popup_window, null);
        } else {
            popupView = inflater.inflate(R.layout.won_popup_window, null);
        }
        wonPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        TextView edt = popupView.findViewById(R.id.WonText);
        edt.setText("You won " + result + " FUN");

        TextView edt1 = popupView.findViewById(R.id.titleWon);
        if(result > 0.0){
            edt1.setText("Congratulations!");
        }else{
            edt1.setText("Unfortunately");
        }

        View btn = popupView.findViewById(R.id.Ok);
        btn.setOnClickListener(view -> wonPopup.dismiss());

        View rootView = getWindow().getDecorView().getRootView();
        wonPopup.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void back() {

        Intent intent = new Intent(this, HomePageActivity.class);
        playerHomePageLauncher.launch(intent);
    }

    private void spin(int number){

        Random random = new Random();

        int rotation;

        int circles = random.nextInt(1) + 5;

        if(degreesTotal > degrees[number]){
            rotation = (360 - degreesTotal + degrees[number]);
            degreesTotal += (360 - degreesTotal + degrees[number]) % 360;
        }else{
            rotation = degrees[number] - degreesTotal;
            degreesTotal = degrees[number];
        }

        rotation += circles * 360;

        ObjectAnimator animator = ObjectAnimator.ofFloat(
                ivWheel,
                "rotation",
                ivWheel.getRotation(),
                ivWheel.getRotation() + rotation
        );
        animator.setDuration(4000L);
        animator.start();

        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                btnPlay.setEnabled(true);
                createWonWindow(number == 10);
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }

    @SuppressLint("SetTextI18n")
    private void playGame() {
        String bet = betAmount.getText().toString();

        if(bet.isEmpty()){
            showMessage("No Betting Amount");
        }else{
            if(presenter.getLoggedInPlayer().getWallet().getBalance() < Double.parseDouble(bet)){
                showMessage("Not Enough funds");
            }else if (Double.parseDouble(bet) > game.getMaxBet() || Double.parseDouble(bet) < game.getMinBet()){
                showMessage("Please bet between " + game.getMinBet() + "-" + game.getMaxBet());
            }else{
                presenter.getLoggedInPlayer().getWallet().chargeWallet(Double.parseDouble(bet));

                updateBalance(presenter.getLoggedInPlayer().getUsername(), -Double.parseDouble(bet));

                String[] nub = {gameName, presenter.getLoggedInPlayer().getUsername(), bet};

                Message m = new Message(MessageCode.PlayGame, nub);

                btnPlay.setEnabled(false);

                new Thread(() -> {
                    try {
                        Socket master = new Socket("192.168.1.6", 1312);

                        ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

                        oss.writeObject(m);

                        oss.flush();

                        ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                        result = (double) ois.readObject();

                        double multiplier = result / Double.parseDouble(bet);

                        Random random = new Random();

                        int place;

                        if (game.getRiskLevel().equals("Low")) {
                            if (multiplier == 0.0) {
                                place = (random.nextInt(2) + 8) % 10;
                            } else {
                                place = lowMultipliers.get(multiplier);
                            }
                        } else if (game.getRiskLevel().equals("Medium")) {
                            if (multiplier == 0.0) {
                                place = (random.nextInt(4) + 7) % 10;
                            } else {
                                place = mediumMultipliers.get(multiplier);
                            }
                        } else {
                            if (multiplier == 0.0) {
                                place = (random.nextInt(6) + 6) % 10;
                            } else {
                                place = highMultipliers.get(multiplier);
                            }
                        }

                        runOnUiThread(() -> {
                            spin(place);
                        });


                        presenter.getLoggedInPlayer().getWallet().rechargeWallet(result);

                        updateBalance(presenter.getLoggedInPlayer().getUsername(), result);

                        master.close();

                    } catch (IOException | NumberFormatException | NullPointerException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    @Override
    public void showMessage(String msg) {
        View contextView = findViewById(android.R.id.content);
        com.google.android.material.snackbar.Snackbar snackbar =
                com.google.android.material.snackbar.Snackbar.make(contextView, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundResource(R.drawable.bg_container_border);

        int snackbarTextId = snackbarView.getResources().getIdentifier("snackbar_text", "id", getPackageName());
        TextView textView = snackbarView.findViewById(snackbarTextId);

        textView.setTextColor(getColor(R.color.gold));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);

        snackbar.show();
    }

    public void onBackBtnPressed() {
        Intent intent = new Intent(this, HomePageActivity.class);
        playerHomePageLauncher.launch(intent);
    }

    private void updateBalance(String username, double amount){

        String[] ua = {username, String.valueOf(amount)};

        Message m = new Message(MessageCode.UpdateWallet, ua);

        new Thread(() -> {
            try {
                Socket master = new Socket("192.168.1.6", 1312);

                ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

                oss.writeObject(m);

                oss.flush();

                ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                String answer = (String) ois.readObject();

                master.close();

            } catch (IOException | NumberFormatException | NullPointerException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }
}