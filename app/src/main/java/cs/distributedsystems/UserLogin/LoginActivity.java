package cs.distributedsystems.UserLogin;

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
import cs.distributedsystems.UserSignin.SignInActivity;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;
import gr.softeng.distributedsystems.Entities.Player;


public class LoginActivity extends AppCompatActivity implements LoginView {
    private LoginViewModel viewModel;
    private LoginPresenter presenter;
    private ActivityResultLauncher<Intent> citizenMainPageLauncher;
    private ActivityResultLauncher<Intent> citizenSignUpLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        presenter = viewModel.getPresenter();
        presenter.setView(this);

        citizenMainPageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {finish();
                });

        citizenSignUpLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.Login);
        button.setOnClickListener(v -> login());

        TextView signup = findViewById(R.id.SignUpText);

        Intent intent = new Intent(this, SignInActivity.class);

        signup.setOnClickListener(v -> citizenSignUpLauncher.launch(intent));

        String successMessage = getIntent().getStringExtra("SUCCESS_MESSAGE");
        if (successMessage != null) {
            showMessage(successMessage);
        }
    }

    public void login() {
        EditText edtUsername = findViewById(R.id.Username);
        String username = edtUsername.getText().toString();

        EditText edtPassword = findViewById(R.id.Password);
        String password = edtPassword.getText().toString();

        presenter.login(username, password);
    }

    @Override
    public void checkUserCredentials(String username, String password){

        Message m = new Message(MessageCode.LogIn, username);

        new Thread(() -> {
            try {
                Socket master = new Socket("192.168.1.6", 1312);

                ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

                oss.writeObject(m);

                oss.flush();

                ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                Player player = (Player) ois.readObject();

                master.close();

                presenter.checkPassword(player, password);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();

    }

    @Override
    public void onLoginSuccess() {
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.putExtra("SUCCESS_MESSAGE", "Successful Login");
        citizenMainPageLauncher.launch(intent);
    }

    @Override
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
}