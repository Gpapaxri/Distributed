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
import android.widget.ProgressBar;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    private final String tag = "HomePageActivity";
    private final String masterIP = "192.168.1.6";
    private final int masterPort = 1312;
    private final int attempts = 10;
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
    private final Map<String, Bitmap> savedLogos = new HashMap<>();
    private File logoDir;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        viewModel = new ViewModelProvider(this).get(HomePageViewModel.class);
        presenter  = viewModel.getPresenter();
        presenter.setView(this);

        homePageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> finish()
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inflater = getLayoutInflater();

        logoDir = new File(getFilesDir(), "logos");
        if (!logoDir.exists()) logoDir.mkdirs();

        progressBar = findViewById(R.id.progressBar);

        updateBalance();

        Button btnAdd = findViewById(R.id.add);
        btnAdd.setOnClickListener(v -> addMoney());

        Button btnSignOut = findViewById(R.id.btnSignOutCitizenHomePage);
        btnSignOut.setOnClickListener(v -> SignOut());

        Button btnFilters = findViewById(R.id.filters);
        btnFilters.setOnClickListener(v -> chooseFilters());

        gamesScroller = findViewById(R.id.GameRecycler);
        gamesScroller.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameRecyclerViewAdapter(new ArrayList<>());
        adapter.setView(this);
        gamesScroller.setAdapter(adapter);
        gamesScroller.setVisibility(View.INVISIBLE);

        for (int i = 0; i < 11; i++) colors[i] = getColor(R.color.gold);

        loadGamesAndLogos(true);
    }

    private void loadGamesAndLogos(boolean all) {
        setLoading(true);

        new Thread(() -> {
            List<Game> fetchedGames = fetchGamesSync(all);

            if (fetchedGames.isEmpty()) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showMessage("No games available");
                });
                return;
            }

            for (Game game : fetchedGames) {
                String logoName = game.getGameLogo();
                File cacheFile = new File(logoDir, logoName);

                if (!cacheFile.exists()) {
                    boolean ok = fetchLogoWithRetry(logoName, cacheFile);
                    if (!ok) {
                        Log.e(tag, "Αδυναμία φόρτωσης logo για: " + logoName);
                    }
                } else {
                    Log.d(tag, "Logo από cache: " + logoName);
                }
            }

            runOnUiThread(() -> {
                games.clear();
                games.addAll(fetchedGames);
                adapter.setGames(games);
                adapter.notifyDataSetChanged();
                gamesScroller.setVisibility(View.VISIBLE);
                setLoading(false);
            });

        }).start();
    }

    private List<Game> fetchGamesSync(boolean all) {
        List<Game> result = new ArrayList<>();
        try {
            Socket master = new Socket(masterIP, masterPort);

            Message m = all
                    ? new Message(MessageCode.SearchGames, "all")
                    : new Message(MessageCode.SearchGames, starCount + " " + bet + " " + risk);

            ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());
            oss.writeObject(m);
            oss.flush();

            ObjectInputStream ois = new ObjectInputStream(master.getInputStream());
            Map<String, Object> gamesMap = (Map<String, Object>) ois.readObject();

            for (Object game : gamesMap.values()) {
                result.add((Game) game);
            }

            master.close();
        } catch (Exception e) {
            Log.e(tag, "Σφάλμα φόρτωσης παιχνιδιών: " + e.getMessage());
        }
        return result;
    }

    /**
     * Προσπαθεί να κατεβάσει το logo έως MAX_RETRIES φορές.
     * Αποθηκεύει τα bytes απευθείας στο disk (cacheFile).
     */
    private boolean fetchLogoWithRetry(String name, File cacheFile) {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            Log.d(tag, "Προσπάθεια " + attempt + "/" + attempts + " για: " + name);
            try {
                Socket socket = new Socket(masterIP, masterPort);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(new Message(MessageCode.GetLogo, name));
                oos.flush();

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message response = (Message) ois.readObject();
                socket.close();

                byte[] imageBytes = (byte[]) response.getContent();

                if (imageBytes != null && imageBytes.length > 0) {
                    // Αποθήκευσε στο disk
                    try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                        fos.write(imageBytes);
                    }
                    Log.d(tag, "Logo αποθηκεύτηκε: " + name + " (" + imageBytes.length + " bytes)");
                    return true;
                } else {
                    Log.w(tag, "Κενό response για: " + name + ", attempt " + attempt);
                }

            } catch (Exception e) {
                Log.w(tag, "Σφάλμα attempt " + attempt + " για " + name + ": " + e.getMessage());
            }

            // Αναμονή πριν το επόμενο retry
            if (attempt < attempts) {
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            }
        }
        return false;
    }

    @Override
    public void getLogo(ImageView imageView, String name) {
        // Έλεγξε in-memory cache (γρήγορος)
        Bitmap cached = savedLogos.get(name);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        imageView.setTag(name);
        final String requestedName = name;

        new Thread(() -> {
            File cacheFile = new File(logoDir, name);
            Bitmap bitmap = null;

            if (cacheFile.exists()) {

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), opts);

                int reqW = 200, reqH = 200, sample = 1;
                if (opts.outWidth > reqW || opts.outHeight > reqH) {
                    int hw = opts.outHeight / 2, ww = opts.outWidth / 2;
                    while ((hw / sample) >= reqH && (ww / sample) >= reqW) sample *= 2;
                }
                opts.inSampleSize = sample;
                opts.inJustDecodeBounds = false;

                bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), opts);
            }

            if (bitmap != null) {
                final Bitmap finalBitmap = bitmap;
                savedLogos.put(name, finalBitmap);
                runOnUiThread(() -> {
                    if (requestedName.equals(imageView.getTag())) {
                        imageView.setImageBitmap(finalBitmap);
                    }
                });
            } else {
                Log.w(tag, "Logo δεν βρέθηκε στο disk για: " + name);
            }
        }).start();
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void addMoney() {
        homePageLauncher.launch(new Intent(this, WalletPage.class));
    }

    private void createList(boolean all) {
        loadGamesAndLogos(all);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void modifyList(boolean all) {
        loadGamesAndLogos(all);
    }

    private void chooseFilters() {
        View popupView = inflater.inflate(R.layout.filters_popup_window, null);
        playPopup = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        Button[] stars = {
            popupView.findViewById(R.id.Star1), popupView.findViewById(R.id.Star2),
            popupView.findViewById(R.id.Star3), popupView.findViewById(R.id.Star4),
            popupView.findViewById(R.id.Star5)
        };
        Button[] bets = {
            popupView.findViewById(R.id.Bet1), popupView.findViewById(R.id.Bet2),
            popupView.findViewById(R.id.Bet3)
        };
        Button[] risks = {
            popupView.findViewById(R.id.Risk1), popupView.findViewById(R.id.Risk2),
            popupView.findViewById(R.id.Risk3)
        };

        Button apply = popupView.findViewById(R.id.Apply);
        Button clear  = popupView.findViewById(R.id.Clear);
        Button close  = popupView.findViewById(R.id.Close);

        close.setOnClickListener(v -> playPopup.dismiss());

        clear.setOnClickListener(v -> {
            Arrays.fill(colored, false);
            for (int i = 0; i < 11; i++) colors[i] = getColor(R.color.gold);
            playPopup.dismiss();
            modifyList(true);
        });

        for (int i = 0; i < 5; i++) {
            stars[i].setBackgroundTintList(null);
            int number = i;
            stars[i].setBackgroundColor(colors[i]);
            stars[i].setTextColor(colors[i] == Color.parseColor("#1E1513") ? Color.WHITE : Color.BLACK);
            stars[i].setOnClickListener(v -> {
                starCount = number + 1;
                colored[0] = true;
                for (int j = 0; j < 5; j++) {
                    boolean sel = (j == number);
                    stars[j].setBackgroundColor(sel ? Color.parseColor("#1E1513") : getColor(R.color.gold));
                    stars[j].setTextColor(sel ? Color.WHITE : Color.BLACK);
                    colors[j] = sel ? Color.parseColor("#1E1513") : getColor(R.color.gold);
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            bets[i].setBackgroundTintList(null);
            int number = i;
            bets[i].setBackgroundColor(colors[i + 5]);
            bets[i].setTextColor(colors[i + 5] == Color.parseColor("#1E1513") ? Color.WHITE : Color.BLACK);
            bets[i].setOnClickListener(v -> {
                colored[1] = true;
                for (int j = 0; j < 3; j++) {
                    boolean sel = (j == number);
                    bets[j].setBackgroundColor(sel ? Color.parseColor("#1E1513") : getColor(R.color.gold));
                    bets[j].setTextColor(sel ? Color.WHITE : Color.BLACK);
                    colors[j + 5] = sel ? Color.parseColor("#1E1513") : getColor(R.color.gold);
                }
                bet = bets[number].getText().toString();
            });
        }

        for (int i = 0; i < 3; i++) {
            risks[i].setBackgroundTintList(null);
            int number = i;
            risks[i].setBackgroundColor(colors[i + 8]);
            risks[i].setTextColor(colors[i + 8] == Color.parseColor("#1E1513") ? Color.WHITE : Color.BLACK);
            risks[i].setOnClickListener(v -> {
                colored[2] = true;
                for (int j = 0; j < 3; j++) {
                    boolean sel = (j == number);
                    risks[j].setBackgroundColor(sel ? Color.parseColor("#1E1513") : getColor(R.color.gold));
                    risks[j].setTextColor(sel ? Color.WHITE : Color.BLACK);
                    colors[j + 8] = sel ? Color.parseColor("#1E1513") : getColor(R.color.gold);
                }
                risk = risks[number].getText().toString();
            });
        }

        apply.setOnClickListener(v -> {
            if (colored[0] && colored[1] && colored[2]) {
                modifyList(false);
                playPopup.dismiss();
            } else {
                showMessage("Please choose all the filters");
            }
        });

        playPopup.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);
    }

    @Override
    public void showMessage(String msg) {
        View contextView = findViewById(android.R.id.content);
        com.google.android.material.snackbar.Snackbar snackbar =
                com.google.android.material.snackbar.Snackbar.make(
                        contextView, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundResource(R.drawable.bg_container_border);

        int id = snackbarView.getResources().getIdentifier(
                "snackbar_text", "id", getPackageName());
        TextView textView = snackbarView.findViewById(id);
        textView.setTextColor(getColor(R.color.gold));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);

        snackbar.show();
    }

    @Override
    public void createPlayPage(Game game) {
        Intent intent = (game.getGameName().length() % 2 == 0) ? new Intent(this, PlayPage.class) : new Intent(this, SpinnerPage.class);
        intent.putExtra("Game", game);
        homePageLauncher.launch(intent);
    }

    @Override
    public void createRateWindow(Game game) {
        View popupView = inflater.inflate(R.layout.rate_popup_window, null);
        ratePopup = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        Button[] stars = {
            popupView.findViewById(R.id.Star1), popupView.findViewById(R.id.Star2),
            popupView.findViewById(R.id.Star3), popupView.findViewById(R.id.Star4),
            popupView.findViewById(R.id.Star5)
        };
        Button apply = popupView.findViewById(R.id.rateButton);
        Button close  = popupView.findViewById(R.id.Close);
        AtomicInteger rating = new AtomicInteger();

        close.setOnClickListener(v -> ratePopup.dismiss());

        for (int i = 0; i < 5; i++) {
            stars[i].setBackgroundTintList(null);
            int number = i;
            stars[i].setTextColor(Color.BLACK);
            stars[i].setOnClickListener(v -> {
                rating.set(number + 1);
                for (int j = 0; j < 5; j++) {
                    boolean sel = (j <= number);
                    stars[j].setBackgroundColor(sel ? Color.parseColor("#1E1513") : getColor(R.color.gold));
                    stars[j].setTextColor(sel ? Color.WHITE : Color.BLACK);
                }
            });
        }

        apply.setOnClickListener(v -> {
            if (rating.get() != 0) {
                String[] r = {game.getGameName(),
                              presenter.getLoggedInPlayer().getUsername(),
                              String.valueOf(rating.get())};
                new Thread(() -> {
                    try {
                        Socket master = new Socket(masterIP, masterPort);
                        ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());
                        oss.writeObject(new Message(MessageCode.Rating, r));
                        oss.flush();
                        ObjectInputStream ois = new ObjectInputStream(master.getInputStream());
                        ois.readUTF();
                        master.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                ratePopup.dismiss();
            } else {
                showMessage("Please choose a rating");
            }
        });

        ratePopup.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);
    }

    private void updateBalance() {
        Button btnAdd = findViewById(R.id.add);
        btnAdd.setText(presenter.getLoggedInPlayer().getWallet().getBalance() + " FUN");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBalance();
    }

    public void SignOut() {
        homePageLauncher.launch(new Intent(this, LoginActivity.class));
    }
}
