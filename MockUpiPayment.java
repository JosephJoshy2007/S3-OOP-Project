// Module 4: Concrete MockUpiPayment Strategy
public class MockUpiPayment extends PaymentMethod {
    private final String upiId;

    public MockUpiPayment(String upiId) {
        super("Mock UPI Instant Pay");
        this.upiId = upiId;
    }

    public String getUpiId() { return upiId; }

    @Override
    public boolean executeTransaction(double amount) throws PaymentProcessingException {
        if (upiId == null || !upiId.contains("@")) {
            throw new PaymentProcessingException("UPI payment failed: Invalid virtual payment address (VPA).");
        }
        return true;
    }
}
