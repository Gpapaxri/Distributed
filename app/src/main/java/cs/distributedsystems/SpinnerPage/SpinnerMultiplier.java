package cs.distributedsystems.SpinnerPage;

public class SpinnerMultiplier {
    private final String multiplier;
    private final double multiplierValue;
    private final int backgroundColor;
    private final int textColor;

    public SpinnerMultiplier(String multiplier, double multiplierValue, int backgroundColor, int textColor) {
        this.multiplier = multiplier;
        this.multiplierValue = multiplierValue;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    public String get_Multiplier() {return multiplier;}
    public double get_MultiplierValue() {return multiplierValue;}
    public int get_BackgroundColor() {return backgroundColor;}
    public int get_TextColor() {return textColor;}
}