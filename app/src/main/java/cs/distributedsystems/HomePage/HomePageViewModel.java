package cs.distributedsystems.HomePage;

import androidx.lifecycle.ViewModel;

public class HomePageViewModel extends ViewModel {
    private HomePagePresenter presenter;
    public HomePageViewModel() {this.presenter = new HomePagePresenter();}

    public HomePagePresenter getPresenter() {return presenter;}
}
