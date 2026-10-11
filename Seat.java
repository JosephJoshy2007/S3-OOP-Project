import java.time.LocalDateTime;

// Module 2: Seat Model
public class Seat {
    private final String id;
    private final int rowNumber;
    private final int colNumber;
    private final SeatCategory category;
    private SeatStatus status;
    private LocalDateTime reservedUntil;
    private String reservationBookingId;

    public Seat(String id, int rowNumber, int colNumber, SeatCategory category) {
        this.id = id;
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        this.category = category;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getId() { return id; }
    public int getRowNumber() { return rowNumber; }
    public int getColNumber() { return colNumber; }
    public SeatCategory getCategory() { return category; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public LocalDateTime getReservedUntil() { return reservedUntil; }
    public String getReservationBookingId() { return reservationBookingId; }

    public synchronized boolean isAvailable() {
        if (status == SeatStatus.RESERVED && reservedUntil != null && LocalDateTime.now().isAfter(reservedUntil)) {
            this.status = SeatStatus.AVAILABLE;
            this.reservedUntil = null;
            this.reservationBookingId = null;
            return true;
        }
        return status == SeatStatus.AVAILABLE;
    }

    public synchronized void reserve(String bookingId, int durationMinutes) throws SeatUnavailableException {
        if (!isAvailable()) {
            throw new SeatUnavailableException("Seat " + id + " is not available for reservation.");
        }
        this.status = SeatStatus.RESERVED;
        this.reservedUntil = LocalDateTime.now().plusMinutes(durationMinutes);
        this.reservationBookingId = bookingId;
    }

    public synchronized void confirmBooking(String bookingId) throws SeatUnavailableException {
        if (status == SeatStatus.BOOKED) {
            throw new SeatUnavailableException("Seat " + id + " is already booked.");
        }
        this.status = SeatStatus.BOOKED;
        this.reservationBookingId = bookingId;
        this.reservedUntil = null;
    }

    public synchronized void release(String bookingId) {
        if (this.reservationBookingId == null || this.reservationBookingId.equals(bookingId) || this.status != SeatStatus.BOOKED) {
            this.status = SeatStatus.AVAILABLE;
            this.reservedUntil = null;
            this.reservationBookingId = null;
        }
    }

    @Override
    public String toString() {
        return id + " (" + category.getLabel() + " - " + status.getDisplayName() + ")";
    }
}
