package cs.distributedsystems.UserSignin;

import gr.softeng.distributedsystems.Entities.Player;

public class SignInPresenter {
    private SignInView view;
    public SignInPresenter() {}

    public void setView(SignInView view){
        this.view = view;
    }

    public void signIn(String nameKeyword, String passwordKeyword) {

        Boolean nameEmpty = (nameKeyword == null || nameKeyword.isEmpty());
        Boolean passwordEmpty = (passwordKeyword == null || passwordKeyword.isEmpty());

        if(nameEmpty || passwordEmpty){
            view.showMessage("Please Fill Out All Fields");
            return;
        }

        Player player = new Player(nameKeyword, passwordKeyword);
        view.registerUser(player);
    }
}
