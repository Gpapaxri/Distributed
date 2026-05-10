package cs.distributedsystems.UserSignin;

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

import cs.distributedsystems.R;
import cs.distributedsystems.UserLogin.LoginActivity;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;
import gr.softeng.distributedsystems.Entities.Player;


public class SignInActivity extends AppCompatActivity implements SignInView {

    private SignInViewModel viewModel;
    private SignInPresenter presenter;
    private ActivityResultLauncher<Intent> LogInPageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        viewModel = new ViewModelProvider(this).get(SignInViewModel.class);
        presenter = viewModel.getPresenter();
        presenter.setView(this);

        LogInPageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {finish();});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSignIn = findViewById(R.id.Signin);
        btnSignIn.setOnClickListener(v -> signIn());

        TextView btnBackSignInPage = findViewById(R.id.btnBackSignInPage);
        btnBackSignInPage.setOnClickListener(v -> onBackBtnPressed());
    }

    private void signIn(){

        EditText name = findViewById(R.id.name);
        String nameKeyword = name.getText().toString();

        EditText password = findViewById(R.id.Code);
        String passwordKeyword = password.getText().toString();

        presenter.signIn(nameKeyword, passwordKeyword);
    }

    @Override
    public void onSignInSuccess(String message){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("SUCCESS_MESSAGE", message);
        LogInPageLauncher.launch(intent);
    }

    @Override
    public void registerUser(Player player) {
        Message m = new Message(MessageCode.SignIn, player);

        new Thread(() -> {
            try {
                Socket master = new Socket("192.168.1.6", 1312);

                ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

                oss.writeObject(m);

                oss.flush();

                ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                String answer = (String) ois.readObject();

                master.close();

                runOnUiThread(() ->{
                    if(answer.equals("User already exists")){
                        showMessage(answer);
                    }else {
                        onSignInSuccess(answer);
                    }
                });

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                onSignInSuccess("Successful SignIn");
            }
        }).start();
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

    public void onBackBtnPressed() {
        finish();
    }
}