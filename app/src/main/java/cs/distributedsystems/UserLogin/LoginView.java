package cs.distributedsystems.UserLogin;

import gr.softeng.distributedsystems.Entities.Player;

public interface LoginView {
     void showMessage(String msg);
     void onLoginSuccess();
     void checkUserCredentials(String username, String password);
}
