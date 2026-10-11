import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Module 3: Booking Model
public class Booking implements Cancellable {
    private final String id;
    private final Customer customer;
    private final Show show;
    private final List<String> seatIds;
    private final double totalAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private BookingStatus bookingStatus;
    private String cancellationReason;

    public Booking(String id, Customer customer, Show show, List<String> seatIds, double totalAmount, int expiryMinutes) {
        this.id = id;
        this.customer = customer;
        this.show = show;
        this.seatIds = new ArrayList<>(seatIds);
        this.totalAmount = Math.round(totalAmount * 100.0) / 100.0;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        this.bookingStatus = BookingStatus.PENDING_PAYMENT;
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Show getShow() { return show; }
    public List<String> getSeatIds() { return Collections.unmodifiableList(seatIds); }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public String getCancellationReason() { return cancellationReason; }

    public boolean isExpired() {
        return bookingStatus == BookingStatus.PENDING_PAYMENT && LocalDateTime.now().isAfter(expiresAt);
    }

    public void confirm() throws InvalidBookingStateException {
        if (bookingStatus == BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Booking " + id + " is already confirmed.");
        }
        if (bookingStatus == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Cannot confirm a cancelled booking.");
        }
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public void markPaymentFailed() {
        if (this.bookingStatus == BookingStatus.PENDING_PAYMENT) {
            this.bookingStatus = BookingStatus.PAYMENT_FAILED;
        }
    }

    @Override
    public boolean isCancellable() {
        if (bookingStatus != BookingStatus.CONFIRMED) return false;
        // Cancellable up to 2 hours before showtime
        return LocalDateTime.now().isBefore(show.getShowDateTime().minusHours(2));
    }

    @Override
    public void cancel(String reason) throws InvalidBookingStateException {
        if (bookingStatus == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking " + id + " is already cancelled.");
        }
        this.bookingStatus = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    @Override
    public String toString() {
        return "Booking #" + id + " [" + bookingStatus.getDisplayName() + "] - " +
                show.getMovie().getTitle() + " (" + String.join(", ", seatIds) + ") $" + String.format("%.2f", totalAmount);
    }
}
