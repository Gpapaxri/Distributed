package cs.distributedsystems.UserSignin;


import cs.distributedsystems.UserLogin.LoginPresenter;
import cs.distributedsystems.dao.PlayerDAO;
import gr.softeng.distributedsystems.Entities.Player;

public class SignInPresenter {
    private SignInView view;
    private static PlayerDAO playerDAO;
    public SignInPresenter() {playerDAO = LoginPresenter.getPlayerDAO();}

    public void setView(SignInView view){
        this.view = view;
    }

    public void signIn(String nameKeyword, String passwordKeyword) {

        Boolean nameEmpty = (nameKeyword == null || nameKeyword.isEmpty());
        Boolean passwordEmpty = (passwordKeyword == null || passwordKeyword.isEmpty());

        if(nameEmpty || passwordEmpty){
            view.showMessage("Please fill out the whole form");
            return;
        }

        Player player = new Player(nameKeyword, passwordKeyword);
        view.registerUser(player);
    }
}
