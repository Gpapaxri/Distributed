package cs.distributedsystems.HomePage;

import android.widget.ImageView;

import gr.softeng.distributedsystems.Entities.Game;

public interface HomePageView {
    void showMessage(String msg);

    void createPlayPage(Game game);

    void createRateWindow(Game game);

    /// /////////////////
    void getLogo(ImageView imageView, String name);
}
