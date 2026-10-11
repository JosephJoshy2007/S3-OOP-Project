// Module 2: SeatCategory Enum
public enum SeatCategory {
    STANDARD("Standard", 1.0),
    PREMIUM("Premium", 1.3),
    VIP("VIP", 1.6);

    private final String label;
    private final double priceMultiplier;

    SeatCategory(String label, double priceMultiplier) {
        this.label = label;
        this.priceMultiplier = priceMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}
