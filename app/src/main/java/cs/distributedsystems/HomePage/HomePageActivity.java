package cs.distributedsystems.HomePage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cs.distributedsystems.PlayPage.PlayPage;
import cs.distributedsystems.R;
import cs.distributedsystems.SpinnerPage.SpinnerPage;
import cs.distributedsystems.UserLogin.LoginActivity;
import cs.distributedsystems.WalletPage.WalletPage;
import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;


public class HomePageActivity extends AppCompatActivity implements HomePageView {
    private PopupWindow playPopup;
    private PopupWindow ratePopup;
    private LayoutInflater inflater;
    private HomePageViewModel viewModel;
    private HomePagePresenter presenter;
    private ActivityResultLauncher<Intent> homePageLauncher;
    private RecyclerView gamesScroller;
    private int starCount;
    private String bet;
    private String risk;
    private GameRecyclerViewAdapter adapter;
    private List<Game> games = new ArrayList<>();
    private final boolean[] colored = new boolean[3];
    int[] colors = new int[11];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        viewModel = new ViewModelProvider(this).get(HomePageViewModel.class);
        presenter = viewModel.getPresenter();
        presenter.setView(this);

        homePageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {finish();});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inflater = getLayoutInflater();

        updateBalance();

        Button btnAdd = findViewById(R.id.add);
        btnAdd.setOnClickListener(v -> addMoney());

        Button btnSignOutHomePage = findViewById(R.id.btnSignOutCitizenHomePage);
        btnSignOutHomePage.setOnClickListener(v -> SignOut());

        Button btnFilters = findViewById(R.id.filters);
        btnFilters.setOnClickListener(v -> chooseFilters());

        createList(true);

        for(int i=0; i< 11; i++){
            colors[i] = getColor(R.color.gold);
        }
    }

    private void addMoney(){

        Intent intent = new Intent(this, WalletPage.class);
        homePageLauncher.launch(intent);
    }

    private void createList(boolean all){
        games = searchGames(all);

        gamesScroller = findViewById(R.id.GameRecycler);
        gamesScroller.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameRecyclerViewAdapter(games);
        adapter.setView(this);
        gamesScroller.setAdapter(adapter);
    }

    private void modifyList(boolean all){
        games = searchGames(all);

        assert gamesScroller.getAdapter() != null;
        ((GameRecyclerViewAdapter)gamesScroller.getAdapter()).setGames(games);

    }

    private void chooseFilters(){
        View popupView = inflater.inflate(R.layout.filters_popup_window, null);
        playPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        Button[] stars = {popupView.findViewById(R.id.Star1)
                        ,popupView.findViewById(R.id.Star2)
                        ,popupView.findViewById(R.id.Star3)
                        ,popupView.findViewById(R.id.Star4), popupView.findViewById(R.id.Star5) };

        Button[] bets = {popupView.findViewById(R.id.Bet1)
                        ,popupView.findViewById(R.id.Bet2), popupView.findViewById(R.id.Bet3)};

        Button[] risks = {popupView.findViewById(R.id.Risk1)
                        ,popupView.findViewById(R.id.Risk2), popupView.findViewById(R.id.Risk3)};

        Button apply = popupView.findViewById(R.id.Apply);
        Button clear = popupView.findViewById(R.id.Clear);
        Button close = popupView.findViewById(R.id.Close);

        close.setOnClickListener( view -> playPopup.dismiss());

        clear.setOnClickListener(view -> {
            Arrays.fill(colored, false);
            for(int i = 0; i < 11; i++){
                colors[i] = getColor(R.color.gold);
            }
            playPopup.dismiss();
            modifyList(true);
        });

        for(int i = 0; i < 5 ; i++){
            stars[i].setBackgroundTintList(null);
            int number = i;
            stars[i].setBackgroundColor(colors[i]);
            if (colors[i] == Color.parseColor("#1E1513")) {
                stars[i].setTextColor(Color.WHITE);
            } else {
                stars[i].setTextColor(Color.BLACK);
            }
            stars[i].setOnClickListener(view -> {
                starCount = number + 1;
                colored[0] = true;
                for (int j = 0; j < 5; j ++){
                    if (number == j){
                        stars[j].setBackgroundColor(Color.parseColor("#1E1513"));
                        stars[j].setTextColor(Color.WHITE);
                        colors[number] = Color.parseColor("#1E1513");
                        continue;
                    }
                    stars[j].setBackgroundColor(getColor(R.color.gold));
                    stars[j].setTextColor(Color.BLACK);
                    colors[j] = getColor(R.color.gold);
                }
            });
        }

        for(int i = 0; i < 3 ; i++){
            bets[i].setBackgroundTintList(null);
            int number = i;
            bets[i].setBackgroundColor(colors[i + 5]);
            if (colors[i+5] == Color.parseColor("#1E1513")) {
                bets[i].setTextColor(Color.WHITE);
            } else {
                bets[i].setTextColor(Color.BLACK);
            }
            bets[i].setOnClickListener( view -> {
                colored[1] = true;
                for (int j = 0; j < 3; j ++){
                    if (number == j){
                        bets[j].setBackgroundColor(Color.parseColor("#1E1513"));
                        bets[j].setTextColor(Color.WHITE);
                        colors[number + 5] = Color.parseColor("#1E1513");
                        bet = bets[j].getText().toString();
                        continue;
                    }
                    bets[j].setBackgroundColor(getColor(R.color.gold));
                    bets[j].setTextColor(Color.BLACK);
                    colors[j + 5] = getColor(R.color.gold);
                }

            });
        }

        for(int i = 0; i < 3 ; i++){
            risks[i].setBackgroundTintList(null);
            int number = i;
            risks[i].setBackgroundColor(colors[i + 8]);
            if (colors[i+8] == Color.parseColor("#1E1513")) {
                risks[i].setTextColor(Color.WHITE);
            } else {
                risks[i].setTextColor(Color.BLACK);
            }
            risks[i].setOnClickListener( view -> {
                colored[2] = true;
                for (int j = 0; j < 3; j ++){
                    if (number == j){
                        risks[j].setBackgroundColor(Color.parseColor("#1E1513"));
                        risks[j].setTextColor(Color.WHITE);
                        colors[number + 8] = Color.parseColor("#1E1513");
                        risk = risks[j].getText().toString();
                        continue;
                    }
                    risks[j].setBackgroundColor(getColor(R.color.gold));
                    risks[j].setTextColor(Color.BLACK);
                    colors[j + 8] = getColor(R.color.gold);
                }
            });
        }

        apply.setOnClickListener(view -> {
            if(colored[0] && colored[1] && colored[2]){
                modifyList(false);

                playPopup.dismiss();
            }else{
                showMessage("Please choose all the filters");
            }
        });

        View rootView = getWindow().getDecorView().getRootView();
        playPopup.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }

    public void showMessage(String msg) {
        View contextView = findViewById(android.R.id.content);
        com.google.android.material.snackbar.Snackbar snackbar =
                com.google.android.material.snackbar.Snackbar.make(contextView, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundResource(R.drawable.bg_container_border);

        int snackBarTextId = snackbarView.getResources().getIdentifier("snackbar_text", "id", getPackageName());
        TextView textView = snackbarView.findViewById(snackBarTextId);

        textView.setTextColor(getColor(R.color.gold));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);

        snackbar.show();
    }

    @Override
    public void createPlayPage(Game game) {
        Intent intent;

        if(game.getGameName().length() % 2 == 0){
            intent = new Intent(this, PlayPage.class);
        }else {
            intent = new Intent(this, SpinnerPage.class);
        }

        intent.putExtra("Game", game);
        homePageLauncher.launch(intent);
    }

    @Override
    public void createRateWindow(Game game) {
        View popupView = inflater.inflate(R.layout.rate_popup_window, null);
        ratePopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        Button[] stars = {popupView.findViewById(R.id.Star1)
                ,popupView.findViewById(R.id.Star2)
                ,popupView.findViewById(R.id.Star3)
                ,popupView.findViewById(R.id.Star4), popupView.findViewById(R.id.Star5) };

        Button apply = popupView.findViewById(R.id.rateButton);
        Button close = popupView.findViewById(R.id.Close);

        AtomicInteger rating = new AtomicInteger();

        apply.getBackground().getColorFilter();

        close.setOnClickListener( view -> ratePopup.dismiss());

        for(int i = 0; i < 5 ; i++){
            stars[i].setBackgroundTintList(null);
            int number = i;
            stars[i].setTextColor(Color.BLACK);
            stars[i].setOnClickListener(view -> {
                rating.set(number + 1);
                for (int j = 0; j <= number; j ++){
                    stars[j].setBackgroundColor(Color.parseColor("#1E1513"));
                    stars[j].setTextColor(Color.WHITE);
                    colors[number] = Color.parseColor("#1E1513");
                }
                for (int j = number + 1; j < 5; j ++){
                    stars[j].setBackgroundColor(getColor(R.color.gold));
                    stars[j].setTextColor(Color.BLACK);
                }

            });
        }

        apply.setOnClickListener(view -> {
            if(rating.get() != 0){

                String[] r = {game.getGameName(), presenter.getLoggedInPlayer().getUsername(), String.valueOf(rating.get())};

                Message m = new Message(MessageCode.Rating, r);

                new Thread(() -> {
                    try {
                        Socket master = new Socket("192.168.1.6", 1312);

                        ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

                        oss.writeObject(m);

                        oss.flush();

                        ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                        String answer = ois.readUTF();

                        master.close();

                    } catch (IOException e) {
                        ratePopup.dismiss();
                    }
                }).start();

                ratePopup.dismiss();
            }else{
                showMessage("Please choose a rating");
            }
        });

        View rootView = getWindow().getDecorView().getRootView();
        ratePopup.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }
    //////////////////////////
    @Override
    public void getLogo(ImageView imageView, String name) {
        new Thread(() -> {
            try {
                // 1. Connect to Master
                Socket socket = new Socket("192.168.1.6", 1312);   // your Master IP/port

                // 2. Send GetLogo request as a normal Message
                Message request = new Message(MessageCode.GetLogo, name);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(request);
                oos.flush();

                // 3. Read ALL raw bytes from the socket into a buffer
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                int length = dis.readInt();
                if (length <= 0) {
                    // Logo not found
                    socket.close();
                    return;
                }
                byte[] utf8Bytes = new byte[length];
                dis.readFully(utf8Bytes);
                socket.close();

                String base64 = new String(utf8Bytes, StandardCharsets.UTF_8);
                byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                if (bitmap != null) {
                    runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                } else {
                    Log.e("LOGO", "Bitmap null, data length: " + decoded.length);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private static String bytesToHex(byte[] bytes, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(count, bytes.length); i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }
    //////////////////////////////
    private void updateBalance(){

        Button btnAdd = findViewById(R.id.add);

        String txt = presenter.getLoggedInPlayer().getWallet().getBalance() + " FUN";
        btnAdd.setText(txt);
    }

    @Override
    protected void onResume(){

        super.onResume();
        updateBalance();
    }

    public void SignOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        homePageLauncher.launch(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private List<Game> searchGames(boolean all){

        List<Game> gs = new ArrayList<>() ;

        Message m = new Message(MessageCode.SearchGames, "all");

        if(!all){
            m.setContent(starCount + " " + bet + " " + risk);
        }

        new Thread(() -> {
            try {
                Socket master = new Socket("192.168.1.6", 1312);

                ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

                oss.writeObject(m);

                oss.flush();

                ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                Map<String, Object> games = (Map<String, Object>) ois.readObject();

                for(Object game : games.values()){
                    gs.add((Game) game);
                }

                master.close();

                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();

        return gs;
    }
}