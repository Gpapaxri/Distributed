package cs.distributedsystems.PlayPage;

import androidx.lifecycle.ViewModel;

public class PlayPageViewModel extends ViewModel {
    private PlayPagePresenter presenter;

    public PlayPageViewModel(){
        this.presenter = new PlayPagePresenter();
    }

    public PlayPagePresenter getPresenter(){
        return presenter;
    }
}
