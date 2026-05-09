package cs.distributedsystems.UserLogin;


import cs.distributedsystems.dao.PlayerDAO;
import cs.distributedsystems.memorydao.PlayerDAOMemory;
import gr.softeng.distributedsystems.Entities.Player;

public class LoginPresenter {
    private LoginView view;
    private static PlayerDAO playerDAO;
    private static Player loggedInPlayer;
    public LoginPresenter() {
        playerDAO = new PlayerDAOMemory();
    }

    public void setView(LoginView view) {
        this.view = view;
    }

    public void login(String username, String password) {
        boolean isUsernameEmpty = username == null || username.trim().isEmpty();
        boolean isPasswordEmpty = password == null || password.trim().isEmpty();

        if (isUsernameEmpty && isPasswordEmpty) {
            view.showMessage("Please fill out the log in credentials");
            return;
        }

        if (isUsernameEmpty) {
            view.showMessage("Please put in your username");
            return;
        }

        if (isPasswordEmpty) {
            view.showMessage("Please put in your password");
            return;
        }

        view.checkUserCredentials(username, password);

    }

    public void checkPassword(Player player, String password){
        if (player.getUsername().isEmpty()) {
            view.showMessage("User not found");
        } else if (player.getPassword().equals(password)) {

            loggedInPlayer = player;
            view.onLoginSuccess();

        } else {
            view.showMessage("Wrong password");
        }

    }
    public static Player getLoggedInPlayer() {
        return loggedInPlayer;
    }
    public static PlayerDAO getPlayerDAO() {
        return playerDAO;
    }
}
