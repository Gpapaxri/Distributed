package cs.distributedsystems.UserSignin;

import gr.softeng.distributedsystems.Entities.Player;

public interface SignInView {
    void showMessage(String msg);
    void onSignInSuccess(String message);
    void registerUser(Player player);
}
