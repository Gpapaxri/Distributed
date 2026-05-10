package cs.distributedsystems.WalletPage;

import cs.distributedsystems.UserLogin.LoginPresenter;
import gr.softeng.distributedsystems.Entities.Player;

public class WalletPagePresenter {
    private WalletPageView view;
    private static Player loggedInPlayer;

    public static Player getLoggedInPlayer() {
        return loggedInPlayer;
    }

    public WalletPagePresenter(){ loggedInPlayer = LoginPresenter.getLoggedInPlayer(); }

    public void setView(WalletPageView view) { this.view = view; }

    public void addMoney(String addedAmount){

        if (addedAmount.isEmpty()){

            view.showMessage("Please Insert Recharge Amount");
            return;
        }

        try{
            double amount = Double.parseDouble(addedAmount.replace(",","."));
            loggedInPlayer.getWallet().rechargeWallet(amount);
            view.updateWallet(loggedInPlayer.getUsername(), amount);
            view.onAddMoneySuccess("Balance Updated Successfully");

        } catch (NumberFormatException e){

            view.showMessage("Invalid Amount");
        }
    }
}
