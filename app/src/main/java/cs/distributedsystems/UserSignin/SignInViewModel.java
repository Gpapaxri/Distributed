package cs.distributedsystems.UserSignin;

import androidx.lifecycle.ViewModel;

public class SignInViewModel extends ViewModel {

    private SignInPresenter presenter;
    public SignInViewModel(){
         this.presenter = new SignInPresenter();
     }

    public SignInPresenter getPresenter(){
         return presenter;
     }
}
