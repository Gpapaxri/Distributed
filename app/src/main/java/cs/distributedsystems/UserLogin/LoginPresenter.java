package cs.distributedsystems.UserLogin;

import gr.softeng.distributedsystems.Entities.Player;

public class LoginPresenter {
    private LoginView view;
    private static Player loggedInPlayer;
    public LoginPresenter() {}

    public void setView(LoginView view) {
        this.view = view;
    }

    public void login(String username, String password) {
        boolean isUsernameEmpty = username == null || username.trim().isEmpty();
        boolean isPasswordEmpty = password == null || password.trim().isEmpty();

        if (isUsernameEmpty && isPasswordEmpty) {
            view.showMessage("Please Fill Out All Fields");
            return;
        }

        if (isUsernameEmpty) {
            view.showMessage("Please Insert Your Username");
            return;
        }

        if (isPasswordEmpty) {
            view.showMessage("Please Insert your Password");
            return;
        }

        view.checkUserCredentials(username, password);

    }

    public void checkPassword(Player player, String password){
        if (player.getUsername().isEmpty()) {
            view.showMessage("User Not Found");
        } else if (player.getPassword().equals(password)) {

            loggedInPlayer = player;
            view.onLoginSuccess();

        } else {
            view.showMessage("Wrong Password");
        }

    }
    public static Player getLoggedInPlayer() {
        return loggedInPlayer;
    }
}
