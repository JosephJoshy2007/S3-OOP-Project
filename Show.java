import java.time.LocalDateTime;

// Module 1: Show Model
public class Show {
    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime showDateTime;
    private final double basePrice;
    private final Seat[][] seats;

    public Show(String id, Movie movie, Screen screen, LocalDateTime showDateTime, double basePrice) {
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.showDateTime = showDateTime;
        this.basePrice = basePrice;
        this.seats = new Seat[screen.getRows()][screen.getColumns()];
        initializeSeats();
    }

    private void initializeSeats() {
        for (int r = 0; r < screen.getRows(); r++) {
            char rowLetter = (char) ('A' + r);
            SeatCategory cat;
            if (r < 2) {
                cat = SeatCategory.STANDARD;
            } else if (r < 4) {
                cat = SeatCategory.PREMIUM;
            } else {
                cat = SeatCategory.VIP;
            }

            for (int c = 0; c < screen.getColumns(); c++) {
                String seatId = "" + rowLetter + (c + 1);
                seats[r][c] = new Seat(seatId, r + 1, c + 1, cat);
            }
        }
    }

    public String getId() { return id; }
    public Movie getMovie() { return movie; }
    public Screen getScreen() { return screen; }
    public LocalDateTime getShowDateTime() { return showDateTime; }
    public double getBasePrice() { return basePrice; }
    public Seat[][] getSeats() { return seats; }

    public Seat findSeat(String seatId) {
        if (seatId == null) return null;
        for (Seat[] row : seats) {
            for (Seat seat : row) {
                if (seat.getId().equalsIgnoreCase(seatId.trim())) {
                    return seat;
                }
            }
        }
        return null;
    }

    public double calculateSeatPrice(Seat seat) {
        if (seat == null) return basePrice;
        return Math.round(basePrice * seat.getCategory().getPriceMultiplier() * 100.0) / 100.0;
    }

    public int getAvailableSeatsCount() {
        int count = 0;
        for (Seat[] row : seats) {
            for (Seat seat : row) {
                if (seat.isAvailable()) count++;
            }
        }
        return count;
    }

    public int getTotalSeatsCount() {
        return screen.getTotalCapacity();
    }

    @Override
    public String toString() {
        return movie.getTitle() + " @ " + screen.getName() + " (" + showDateTime.format(UIConstants.DATE_TIME_FORMATTER) + ")";
    }
}
