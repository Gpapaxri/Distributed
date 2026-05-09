package cs.distributedsystems.SpinnerPage;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinnerPageViewModel {

    private Map<Double, Integer> lowMultipliers = new HashMap<>();
    private Map<Double, Integer> mediumMultipliers = new HashMap<>();
    private Map<Double, Integer> highMultipliers = new HashMap<>();
    private static final int total_adapter_items = 10_000;
    private static final int INITIAL_CENTER_POSITION = 5_000;
    private final List<SpinnerMultiplier> spinnerMultipliers;
    private boolean isSpinning;
    private int currentCenterAdapterPosition;


    public SpinnerPageViewModel() {
        spinnerMultipliers = init_WheelMultipliers();
        isSpinning = false;
        currentCenterAdapterPosition = INITIAL_CENTER_POSITION;
    }

    private List<SpinnerMultiplier> init_WheelMultipliers() {
        lowMultipliers.put(0.0d, 0);
        lowMultipliers.put(0.1d, 3);
        lowMultipliers.put(0.5d, 4);
        lowMultipliers.put(1.0d, 5);
        lowMultipliers.put(1.1d, 6);
        lowMultipliers.put(1.3d, 7);
        lowMultipliers.put(2.0d, 8);
        lowMultipliers.put(2.5d, 9);

        mediumMultipliers.put(0.0d, 0);
        mediumMultipliers.put(3.5d, 9);
        mediumMultipliers.put(2.5d, 8);
        mediumMultipliers.put(1.5d, 7);
        mediumMultipliers.put(1.0d, 6);
        mediumMultipliers.put(0.5d, 5);

        highMultipliers.put(0.0d, 0);
        highMultipliers.put(6.5d, 9);
        highMultipliers.put(2.0d, 8);
        highMultipliers.put(1.0d, 7);
        List<SpinnerMultiplier> multipliers = new ArrayList<>();

        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[0] + "x",SpinnerPage.get_game().getMultipliers()[0],  Color.parseColor("#3E2723"), Color.WHITE));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[1] + "x",SpinnerPage.get_game().getMultipliers()[1],  Color.parseColor("#4E342E"), Color.WHITE));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[2] + "x",SpinnerPage.get_game().getMultipliers()[2],  Color.parseColor("#BF360C"), Color.WHITE));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[3] + "x",SpinnerPage.get_game().getMultipliers()[3],  Color.parseColor("#D84315"), Color.WHITE));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[4] + "x",SpinnerPage.get_game().getMultipliers()[4],  Color.parseColor("#E64A19"), Color.WHITE));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[5] + "x",SpinnerPage.get_game().getMultipliers()[5],  Color.parseColor("#F4511E"), Color.WHITE));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[6] + "x",SpinnerPage.get_game().getMultipliers()[6],  Color.parseColor("#FF7043"), Color.BLACK));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[7] + "x",SpinnerPage.get_game().getMultipliers()[7],  Color.parseColor("#FF8A65"), Color.BLACK));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[8] + "x",SpinnerPage.get_game().getMultipliers()[8],  Color.parseColor("#A0522D"), Color.WHITE));
        multipliers.add(new SpinnerMultiplier(SpinnerPage.get_game().getMultipliers()[9] + "x",SpinnerPage.get_game().getMultipliers()[9],  Color.parseColor("#E5B814"), Color.BLACK));
        return multipliers;
    }

    public List<SpinnerMultiplier> get_WheelMultipliers() {return spinnerMultipliers;}
    public int get_TotalAdapterItems() {return total_adapter_items;}
    public boolean isSpinning() {return isSpinning;}
    public void set_isSpinning(boolean spinning) {isSpinning = spinning;}
    public int get_CurrentCenterAdapterPosition() {return currentCenterAdapterPosition;}
    public void set_CurrentCenterAdapterPosition(int pos) {currentCenterAdapterPosition = pos;}
    public Map<Double, Integer> getLowMultipliers() {return lowMultipliers;}
    public Map<Double, Integer> getMediumMultipliers() {return mediumMultipliers;}
    public Map<Double, Integer> getHighMultipliers() {return highMultipliers;}
    public SpinnerMultiplier get_PredeterminedItem(double multiplier, String riskLevel) {
        if (riskLevel.equals("Low")) {
            return spinnerMultipliers.get(lowMultipliers.get(multiplier));
        } else if (riskLevel.equals("Medium")) {
            return spinnerMultipliers.get(mediumMultipliers.get(multiplier));
        } else {
            return spinnerMultipliers.get(highMultipliers.get(multiplier));
        }
    }
}
