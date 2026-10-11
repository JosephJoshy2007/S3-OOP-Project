// Module 4: Concrete MockCardPayment Strategy
public class MockCardPayment extends PaymentMethod {
    private final String cardNumber;
    private final String cardHolder;

    public MockCardPayment(String cardNumber, String cardHolder) {
        super("Mock Credit/Debit Card");
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
    }

    public String getCardNumber() { return cardNumber; }
    public String getCardHolder() { return cardHolder; }

    @Override
    public boolean executeTransaction(double amount) throws PaymentProcessingException {
        if (cardNumber == null || cardNumber.trim().length() < 4) {
            throw new PaymentProcessingException("Card payment failed: Invalid card details provided.");
        }
        return true;
    }
}
