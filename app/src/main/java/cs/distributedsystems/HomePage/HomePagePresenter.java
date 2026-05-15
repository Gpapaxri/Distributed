package cs.distributedsystems.HomePage;

import cs.distributedsystems.UserLogin.LoginPresenter;
import gr.softeng.distributedsystems.Entities.Player;

public class HomePagePresenter {
    private HomePageView view;
    private static Player loggedInPlayer;
    public HomePagePresenter(){ loggedInPlayer = LoginPresenter.getLoggedInPlayer(); }

    public Player getLoggedInPlayer() {
        return loggedInPlayer;
    }

    public void setView(HomePageView view){ this.view = view; }

}
