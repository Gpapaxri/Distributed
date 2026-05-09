package cs.distributedsystems.WalletPage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import cs.distributedsystems.HomePage.HomePageActivity;
import cs.distributedsystems.R;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;


public class WalletPage extends AppCompatActivity implements WalletPageView {
    private WalletPagePresenter presenter;
    private TextView txtMoney;
    private ActivityResultLauncher<Intent> walletPageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet_page);

        walletPageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {finish();});

        WalletPageViewModel viewModel = new ViewModelProvider(this).get(WalletPageViewModel.class);
        presenter = viewModel.getPresenter();
        presenter.setView(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtMoney = findViewById(R.id.amount2);
        updateBalance();

        Button btnAdd = findViewById(R.id.addMoney);
        btnAdd.setOnClickListener(v -> addMoney());

        TextView backButton = findViewById(R.id.btnBackWalletPage);
        backButton.setOnClickListener(view -> back());
    }

    public void addMoney(){

        EditText addAmount = findViewById(R.id.addAmount);
        String addedAmount = addAmount.getText().toString();

        presenter.addMoney(addedAmount);
        addAmount.setText(null);
    }

    public void onAddMoneySuccess(String msg){

        showMessage(msg);
        updateBalance();

        Intent intent = new Intent(this, HomePageActivity.class);
        walletPageLauncher.launch(intent);
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

    private void updateBalance(){

        String txt = WalletPagePresenter.getLoggedInPlayer().getWallet().getBalance() + " FUN";
        txtMoney.setText(txt);
    }

    private void back() {

        Intent intent = new Intent(this, HomePageActivity.class);
        walletPageLauncher.launch(intent);
    }

    @Override
    protected void onResume(){

        super.onResume();
        updateBalance();
    }
    @Override
    public void updateWallet(String username, double amount){

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