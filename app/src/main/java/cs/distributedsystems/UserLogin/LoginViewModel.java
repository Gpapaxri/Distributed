package cs.distributedsystems.UserLogin;

import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {
    private LoginPresenter loginPresenter;
    public LoginViewModel() {
        this.loginPresenter = new LoginPresenter();
    }

    public LoginPresenter getPresenter() {
        return loginPresenter;
    }
}
