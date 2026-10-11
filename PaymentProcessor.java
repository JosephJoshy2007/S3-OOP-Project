import java.time.LocalDateTime;
import java.util.UUID;

// Module 4: PaymentProcessor
public class PaymentProcessor {
    private final BookingManager manager;
    private final BookingService bookingService;

    public PaymentProcessor(BookingManager manager, BookingService bookingService) {
        this.manager = manager;
        this.bookingService = bookingService;
    }

    public synchronized PaymentTransaction processSimulatedPayment(
            Booking booking,
            PaymentMethod paymentMethod,
            String simulationMode
    ) throws PaymentProcessingException, InvalidBookingStateException, SeatUnavailableException {

        if (booking == null) {
            throw new PaymentProcessingException("No booking associated with payment.");
        }

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Booking " + booking.getId() + " is already confirmed. Duplicate payment prevented.");
        }

        if (booking.isExpired()) {
            bookingService.cancelPendingReservation(booking);
            throw new PaymentProcessingException("Checkout failed: Your 5-minute reservation window has expired. Please select seats again.");
        }

        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if ("USER_CANCEL".equalsIgnoreCase(simulationMode)) {
            bookingService.cancelPendingReservation(booking);
            PaymentTransaction txn = new PaymentTransaction(
                    txnId, booking.getId(), paymentMethod.getMethodName(),
                    booking.getTotalAmount(), LocalDateTime.now(),
                    PaymentStatus.CANCELLED, "USER-ABORT"
            );
            manager.getTransactions().add(txn);
            manager.logAudit("Payment cancelled by user for booking " + booking.getId());
            return txn;
        }

        if ("FORCE_FAIL".equalsIgnoreCase(simulationMode)) {
            booking.markPaymentFailed();
            bookingService.cancelPendingReservation(booking);
            PaymentTransaction txn = new PaymentTransaction(
                    txnId, booking.getId(), paymentMethod.getMethodName(),
                    booking.getTotalAmount(), LocalDateTime.now(),
                    PaymentStatus.FAILED, "ERR-DECLINED"
            );
            manager.getTransactions().add(txn);
            manager.logAudit("Payment forcefully failed for booking " + booking.getId());
            throw new PaymentProcessingException("Simulated payment was declined by the issuer.");
        }

        boolean authorized = paymentMethod.executeTransaction(booking.getTotalAmount());
        if (!authorized) {
            booking.markPaymentFailed();
            bookingService.cancelPendingReservation(booking);
            throw new PaymentProcessingException("Payment authorization failed.");
        }

        bookingService.confirmBooking(booking);

        PaymentTransaction successTxn = new PaymentTransaction(
                txnId, booking.getId(), paymentMethod.getMethodName(),
                booking.getTotalAmount(), LocalDateTime.now(),
                PaymentStatus.SUCCESS, "AUTH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()
        );
        manager.getTransactions().add(successTxn);
        manager.logAudit("Payment successful (" + successTxn.getId() + ") for booking " + booking.getId());
        return successTxn;
    }
}
