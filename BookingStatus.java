// Module 3: BookingStatus Enum
public enum BookingStatus {
    PENDING_PAYMENT("Pending Payment"),
    CONFIRMED("Confirmed"),
    PAYMENT_FAILED("Payment Failed"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
