import java.time.LocalDateTime;

// Module 4: PaymentTransaction Model
public class PaymentTransaction {
    private final String id;
    private final String bookingId;
    private final String paymentMethodName;
    private final double amount;
    private final LocalDateTime timestamp;
    private final PaymentStatus paymentStatus;
    private final String gatewayReference;

    public PaymentTransaction(String id, String bookingId, String paymentMethodName,
                              double amount, LocalDateTime timestamp,
                              PaymentStatus paymentStatus, String gatewayReference) {
        this.id = id;
        this.bookingId = bookingId;
        this.paymentMethodName = paymentMethodName;
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.timestamp = timestamp;
        this.paymentStatus = paymentStatus;
        this.gatewayReference = gatewayReference;
    }

    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public String getPaymentMethodName() { return paymentMethodName; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getGatewayReference() { return gatewayReference; }

    @Override
    public String toString() {
        return "Txn #" + id + " (" + paymentStatus.getDisplayName() + ") $" +
                String.format("%.2f", amount) + " via " + paymentMethodName + " [" + gatewayReference + "]";
    }
}
