package cs.distributedsystems.WalletPage;

public interface WalletPageView {
    void showMessage(String msg);
    void onAddMoneySuccess(String msg);
    void updateWallet(String username, double amount);
}
