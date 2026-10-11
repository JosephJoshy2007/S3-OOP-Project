// Module 4: PaymentGateway Interface
public interface PaymentGateway {
    PaymentTransaction processPayment(Booking booking, double amount, String notes)
            throws PaymentProcessingException;
}
