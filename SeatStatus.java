// Module 2: SeatStatus Enum
public enum SeatStatus {
    AVAILABLE("Available"),
    SELECTED("Selected"),
    RESERVED("Reserved"),
    BOOKED("Booked");

    private final String displayName;

    SeatStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
