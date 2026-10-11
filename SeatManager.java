import java.time.LocalDateTime;
import java.util.List;

// Module 2: SeatManager
public class SeatManager {
    public synchronized void expireOutdatedReservations(Show show) {
        if (show == null) return;
        for (Seat[] row : show.getSeats()) {
            for (Seat seat : row) {
                if (seat.getStatus() == SeatStatus.RESERVED &&
                        seat.getReservedUntil() != null &&
                        LocalDateTime.now().isAfter(seat.getReservedUntil())) {
                    seat.release(seat.getReservationBookingId());
                }
            }
        }
    }

    public synchronized void releaseSeats(Show show, List<String> seatIds, String bookingId) {
        if (show == null || seatIds == null) return;
        for (String seatId : seatIds) {
            Seat seat = show.findSeat(seatId);
            if (seat != null) {
                seat.release(bookingId);
            }
        }
    }

    public synchronized void confirmSeats(Show show, List<String> seatIds, String bookingId)
            throws SeatUnavailableException {
        for (String seatId : seatIds) {
            Seat seat = show.findSeat(seatId);
            if (seat == null) throw new SeatUnavailableException("Seat " + seatId + " not found.");
            seat.confirmBooking(bookingId);
        }
    }
}
