package cs.distributedsystems.PlayPage;


import cs.distributedsystems.UserLogin.LoginPresenter;
import gr.softeng.distributedsystems.Entities.Player;

public class PlayPagePresenter {
    private PlayPageView view;
    private static Player loggedInPlayer;
    public PlayPagePresenter() {
        loggedInPlayer = LoginPresenter.getLoggedInPlayer();
    }

    public static Player getLoggedInPlayer() {
        return loggedInPlayer;
    }

    public void setView(PlayPageView view) {this.view = view;}
}
