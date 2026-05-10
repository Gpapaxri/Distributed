package cs.distributedsystems.SpinnerPage;



import cs.distributedsystems.UserLogin.LoginPresenter;
import gr.softeng.distributedsystems.Entities.Player;

public class SpinnerPagePresenter {

    private final SpinnerPageView view;
    private final SpinnerPageViewModel viewModel;
    private static Player loggedInPlayer;
    private static final int rotations = 5;
    private double multiplier;
    private String riskLevel;

    public SpinnerPagePresenter(SpinnerPageView view, SpinnerPageViewModel viewModel) {
        this.view = view;
        this.viewModel = viewModel;

        loggedInPlayer = LoginPresenter.getLoggedInPlayer();
    }

    public void onPlayButtonPressed(double multiplier, String riskLevel) {
        this.multiplier = multiplier;
        this.riskLevel = riskLevel;
        if (viewModel.isSpinning()) {return;}

        viewModel.set_isSpinning(true);
        view.set_PlayButtonEnabled(false);

        int current_center = viewModel.get_CurrentCenterAdapterPosition();
        int current_index = current_center % 11;
        int target_index;


        if (riskLevel.equals("Low")) {
            target_index = viewModel.getLowMultipliers().get(multiplier);
        } else if (riskLevel.equals("Medium")) {
            target_index = viewModel.getMediumMultipliers().get(multiplier);
        } else {
            target_index = viewModel.getHighMultipliers().get(multiplier);
        }



        // Πόσες θέσεις πρέπει να προχωρήσουμε μέσα στις 10 για να αλιγκάρουμε.
        int shift = (target_index - current_index + 11) % 11;
        // Αν shift=0 → τουλάχιστον μία πλήρης μικρή περιστροφή παραπάνω.
        if (shift == 0) {shift = 11;}

        int targetCenterPosition = current_center + (rotations * 11) + shift;
        view.spinWheel(targetCenterPosition);
    }

    public void onSpinComplete(int finalCenterAdapterPosition) {
        viewModel.set_CurrentCenterAdapterPosition(finalCenterAdapterPosition);
        viewModel.set_isSpinning(false);

        view.set_PlayButtonEnabled(true);
    }

    public static Player getLoggedInPlayer() {return loggedInPlayer;}

    public SpinnerPageViewModel get_ViewModel() {return viewModel;}
}
