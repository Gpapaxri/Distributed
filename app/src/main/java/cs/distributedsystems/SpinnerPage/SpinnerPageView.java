package cs.distributedsystems.SpinnerPage;

public interface SpinnerPageView {
    void spin(int targetCenterAdapterPosition);
    void set_PlayButtonEnabled(boolean enabled);
    void back();
    void showError(String message);
}