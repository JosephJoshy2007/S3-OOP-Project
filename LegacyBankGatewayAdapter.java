import java.time.LocalDateTime;
import java.util.UUID;

// Module 4: Structural Adapter Pattern
public class LegacyBankGatewayAdapter implements PaymentGateway {
    private final LegacyBankGatewayApi legacyApi;

    public LegacyBankGatewayAdapter(LegacyBankGatewayApi legacyApi) {
        this.legacyApi = legacyApi;
    }

    @Override
    public PaymentTransaction processPayment(Booking booking, double amount, String notes)
            throws PaymentProcessingException {
        if (booking == null) {
            throw new PaymentProcessingException("Adapter failed: Booking is null.");
        }

        String simulatedAccount = "ACCT-987654321";
        LegacyBankResult result = legacyApi.chargeAccount(simulatedAccount, amount);

        if (!result.isSuccess()) {
            throw new PaymentProcessingException("Legacy bank declined transaction: " + result.getReference());
        }

        return new PaymentTransaction(
                "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                booking.getId(),
                "Bank DirectDebit (Adapter)",
                amount,
                LocalDateTime.now(),
                PaymentStatus.SUCCESS,
                result.getReference()
        );
    }
}
