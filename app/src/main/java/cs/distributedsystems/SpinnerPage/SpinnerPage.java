package cs.distributedsystems.SpinnerPage;

import cs.distributedsystems.HomePage.HomePageActivity;
import cs.distributedsystems.R;
import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SpinnerPage extends AppCompatActivity implements SpinnerPageView {
    private PopupWindow wonPopup;
    private LayoutInflater inflater;
    private SpinnerPagePresenter presenter;
    private SpinnerPageViewModel viewModel;
    private static final int visible_multipliers = 5;
    private static final long spin_duration = 4_000L;
    private static final float deceleration = 2.5f;
    private RecyclerView wheelRecyclerView;
    private View centerIndicator;
    private View top_fade;
    private View bottom_fade;
    private Button play_button;
    private TextView btnBack;
    private LinearLayoutManager layoutManager;
    private SpinnerAdapter spinnerAdapter;
    private ActivityResultLauncher<Intent> playerHomePageLauncher;
    private int itemHeight = 0;
    private ValueAnimator spinAnimator;
    private static Game game;
    private TextView betAmount;
    private String gameName;
    private double result;
    private ObjectOutputStream oss;
    private ObjectInputStream ois;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner_page);

        game = (Game) getIntent().getSerializableExtra("Game");
        assert game != null;
        gameName = game.getGameName();

        viewModel = new SpinnerPageViewModel();
        presenter = new SpinnerPagePresenter(this, viewModel);

        inflater = getLayoutInflater();

        playerHomePageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {finish();});

        bindViews();
        setupWheel();
        betAmount = findViewById(R.id.addBet);

        play_button.setOnClickListener(v -> {
            playGame();
        });

        btnBack.setOnClickListener(v -> {
            back();
        });
    }

    private void bindViews() {
        wheelRecyclerView = findViewById(R.id.wheelRecyclerView);
        centerIndicator = findViewById(R.id.centerIndicator);
        top_fade = findViewById(R.id.topFade);
        bottom_fade = findViewById(R.id.bottomFade);
        play_button = findViewById(R.id.playButton);
        btnBack = findViewById(R.id.BackPlayPage);
        play_button.setEnabled(true);
    }

    private void setupWheel() {
        layoutManager = new LinearLayoutManager(this);
        wheelRecyclerView.setLayoutManager(layoutManager);

        spinnerAdapter = new SpinnerAdapter(viewModel.get_WheelMultipliers(), viewModel.get_TotalAdapterItems());
        wheelRecyclerView.setAdapter(spinnerAdapter);


        wheelRecyclerView.setOnTouchListener((v, e) -> true);


        wheelRecyclerView.post(this::finalizeWheelLayout);
    }

    @SuppressLint("SetTextI18n")
    private void playGame() {
        String bet = betAmount.getText().toString();

        if(bet.isEmpty()){
            showError("No betting amount");
        }else{
            if(presenter.getLoggedInPlayer().getWallet().getBalance() < Double.parseDouble(bet)){
                showError("Not enough funds");
            }else if (Double.parseDouble(bet) > game.getMaxBet() || Double.parseDouble(bet) < game.getMinBet()){
                showError("Please bet between " + game.getMinBet() + "-" + game.getMaxBet());
            }else{
                presenter.getLoggedInPlayer().getWallet().chargeWallet(Double.parseDouble(bet));

                updateBalance(presenter.getLoggedInPlayer().getUsername(), -Double.parseDouble(bet));

                String[] nub = {gameName, presenter.getLoggedInPlayer().getUsername(), bet};

                Message m = new Message(MessageCode.PlayGame, nub);

                new Thread(() -> {
                    try {
                        Socket master = new Socket("192.168.1.6", 1312);

                        oss = new ObjectOutputStream(master.getOutputStream());

                        oss.writeObject(m);

                        oss.flush();

                        ois = new ObjectInputStream(master.getInputStream());

                        result = (double) ois.readObject();

                        double multiplier = result / Double.parseDouble(bet);

                        runOnUiThread(() -> {
                            if(multiplier == game.getJackpot()){
                                createWonWindow();
                            }else{
                                presenter.onPlayButtonPressed(multiplier, game.getRiskLevel());
                            }
                        });

                        presenter.getLoggedInPlayer().getWallet().rechargeWallet(result);

                        updateBalance(presenter.getLoggedInPlayer().getUsername(), result);

                        master.close();

                    } catch (IOException | NumberFormatException | NullPointerException | ClassNotFoundException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            showError("Connection error. Please try again.");
                            play_button.setEnabled(true);
                            presenter.getLoggedInPlayer().getWallet().rechargeWallet(Double.parseDouble(bet));
                        });
                    }
                }).start();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void createWonWindow(){
        View popupView = inflater.inflate(R.layout.won_popup_window, null);
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


    private void finalizeWheelLayout() {
        int rvHeight = wheelRecyclerView.getHeight();
        if (rvHeight == 0) {

            wheelRecyclerView.post(this::finalizeWheelLayout);
            return;
        }

        itemHeight = rvHeight / visible_multipliers;

        setViewHeight(centerIndicator, itemHeight);
        setViewHeight(top_fade, (int)(itemHeight * 1.6f));
        setViewHeight(bottom_fade, (int)(itemHeight * 1.6f));

        layoutManager.scrollToPositionWithOffset(viewModel.get_CurrentCenterAdapterPosition(), 2 * itemHeight);   // slot 2 = κέντρο

        play_button.setEnabled(true);
    }

    @Override
    public void spinWheel(int targetCenterAdapterPosition) {
        if (spinAnimator != null && spinAnimator.isRunning()) {spinAnimator.cancel();}

        int currentCenter = viewModel.get_CurrentCenterAdapterPosition();
        int totalPixels = (targetCenterAdapterPosition - currentCenter) * itemHeight;

        spinAnimator = ValueAnimator.ofInt(0, totalPixels);
        spinAnimator.setDuration(spin_duration);
        spinAnimator.setInterpolator(new DecelerateInterpolator(deceleration));

        final int[] lastValue = {0};

        spinAnimator.addUpdateListener(anim -> {
            int current = (int) anim.getAnimatedValue();
            int delta = current - lastValue[0];
            lastValue[0] = current;
            wheelRecyclerView.scrollBy(0, delta);
        });

        spinAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;

            @Override
            public void onAnimationCancel(Animator a) {cancelled = true;}

            @Override
            public void onAnimationEnd(Animator a) {
                if (cancelled) return;
                layoutManager.scrollToPositionWithOffset(targetCenterAdapterPosition, 2 * itemHeight);
                presenter.onSpinComplete(targetCenterAdapterPosition);
                createWonWindow();
            }
        });

        spinAnimator.start();
    }

    public static Game get_game() {return game;}

    @Override
    public void set_PlayButtonEnabled(boolean enabled) {
        play_button.setEnabled(enabled);
        play_button.setAlpha(enabled ? 1f : 0.45f);
    }

    private void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    public void showError(String msg) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (spinAnimator != null) spinAnimator.cancel();
    }

    @Override
    public void back() {
        Intent intent = new Intent(this, HomePageActivity.class);
        playerHomePageLauncher.launch(intent);
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
