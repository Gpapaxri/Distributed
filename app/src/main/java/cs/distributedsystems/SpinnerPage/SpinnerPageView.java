package cs.distributedsystems.SpinnerPage;

public interface SpinnerPageView {
    void spinWheel(int targetCenterAdapterPosition);
    void set_PlayButtonEnabled(boolean enabled);
    void back();
    void showError(String message);
}