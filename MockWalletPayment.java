// Module 4: Concrete MockWalletPayment Strategy
public class MockWalletPayment extends PaymentMethod {
    private final String walletName;
    private final double walletBalance;

    public MockWalletPayment(String walletName, double walletBalance) {
        super("Mock Digital Wallet (" + walletName + ")");
        this.walletName = walletName;
        this.walletBalance = walletBalance;
    }

    public String getWalletName() { return walletName; }
    public double getWalletBalance() { return walletBalance; }

    @Override
    public boolean executeTransaction(double amount) throws PaymentProcessingException {
        if (walletBalance < amount) {
            throw new PaymentProcessingException(
                    String.format("Wallet payment failed: Insufficient balance ($%.2f available, $%.2f required).",
                            walletBalance, amount));
        }
        return true;
    }
}
