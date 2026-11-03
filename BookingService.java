import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Module 3: BookingService with SQLite JDBC Persistence
public class BookingService {
    private final BookingManager manager;
    private final SeatManager seatManager;

    public BookingService(BookingManager manager, SeatManager seatManager) {
        this.manager = manager;
        this.seatManager = seatManager;
    }

    public synchronized Booking createPendingReservation(Customer customer, Show show, List<String> seatIds)
            throws SeatUnavailableException, ValidationException {
        if (customer == null) throw new ValidationException("A valid customer must be specified.");
        if (show == null) throw new ValidationException("A valid show must be selected.");
        if (seatIds == null || seatIds.isEmpty()) {
            throw new ValidationException("At least one seat must be selected to proceed.");
        }

        seatManager.expireOutdatedReservations(show);

        double total = 0.0;
        for (String seatId : seatIds) {
            Seat seat = show.findSeat(seatId);
            if (seat == null) throw new SeatUnavailableException("Seat ID " + seatId + " does not exist.");
            if (!seat.isAvailable()) {
                throw new SeatUnavailableException("Seat " + seatId + " is no longer available.");
            }
            total += show.calculateSeatPrice(seat);
        }

        String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        for (String seatId : seatIds) {
            Seat seat = show.findSeat(seatId);
            seat.reserve(bookingId, 5);
        }

        Booking booking = new Booking(bookingId, customer, show, seatIds, total, 5);
        manager.getBookings().add(booking);
        manager.logAudit("Created pending booking " + bookingId + " for " + customer.getName() + " (" + seatIds + ")");
        return booking;
    }

    public synchronized void cancelPendingReservation(Booking booking) {
        if (booking == null) return;
        if (booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT) {
            seatManager.releaseSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
            try {
                booking.cancel("Checkout abandoned by user.");
            } catch (InvalidBookingStateException ignored) {}
            manager.logAudit("Pending reservation " + booking.getId() + " released and cancelled.");
        }
    }

    public synchronized void confirmBooking(Booking booking) throws InvalidBookingStateException, SeatUnavailableException {
        if (booking == null) throw new InvalidBookingStateException("Null booking provided.");

        if (booking.isExpired()) {
            seatManager.releaseSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
            throw new InvalidBookingStateException("Booking has expired and cannot be confirmed.");
        }

        seatManager.confirmSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
        booking.confirm();

        // Persist confirmed booking to SQLite
        try {
            manager.getDbManager().insertBooking(booking);
        } catch (SQLException e) {
            System.err.println("[JDBC WARNING] Failed to persist booking to database: " + e.getMessage());
        }

        // Award customer loyalty points (e.g., 1 point per $1 spent)
        Customer customer = booking.getCustomer();
        if (customer != null) {
            int earnedPoints = (int) Math.round(booking.getTotalAmount());
            customer.addLoyaltyPoints(earnedPoints);
            manager.getDbManager().updateCustomerLoyalty(customer.getId(), customer.getLoyaltyPoints());
        }

        manager.logAudit("Confirmed booking " + booking.getId() + " for " + booking.getCustomer().getName());
    }

    public synchronized void cancelConfirmedBooking(String bookingId)
            throws BookingNotFoundException, InvalidBookingStateException {
        Booking booking = manager.findBookingById(bookingId);
        if (booking == null) {
            throw new BookingNotFoundException("Booking ID " + bookingId + " not found.");
        }

        if (!booking.isCancellable()) {
            throw new InvalidBookingStateException("Booking " + bookingId + " cannot be cancelled. Current status: " + booking.getBookingStatus());
        }

        seatManager.releaseSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
        booking.cancel("Cancelled by customer request");

        // Update booking status in SQLite
        manager.getDbManager().updateBookingStatus(bookingId, BookingStatus.CANCELLED, "Cancelled by customer request");

        PaymentTransaction refundTxn = new PaymentTransaction(
                "TXN-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                booking.getId(),
                "Simulated Refund",
                booking.getTotalAmount(),
                LocalDateTime.now(),
                PaymentStatus.REFUNDED,
                "REFUND-PROCESSED"
        );
        manager.getTransactions().add(refundTxn);

        try {
            manager.getDbManager().insertTransaction(refundTxn);
        } catch (SQLException e) {
            System.err.println("[JDBC WARNING] Failed to persist refund transaction: " + e.getMessage());
        }

        manager.logAudit("Cancelled booking " + booking.getId() + ". Seats released and simulated refund recorded.");
    }
}
