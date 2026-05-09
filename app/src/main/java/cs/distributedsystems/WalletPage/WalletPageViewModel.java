package cs.distributedsystems.WalletPage;

import androidx.lifecycle.ViewModel;

public class WalletPageViewModel extends ViewModel {
    private WalletPagePresenter presenter;
    public WalletPageViewModel() { this.presenter = new WalletPagePresenter();}

    public WalletPagePresenter getPresenter() {return presenter;}
}
