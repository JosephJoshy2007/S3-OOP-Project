import java.sql.SQLException;
import java.util.List;

// Module 2: ShowService (Show Management with JDBC Persistence)
public class ShowService {
    private final BookingManager manager;

    public ShowService(BookingManager manager) {
        this.manager = manager;
    }

    public List<Show> getAllShows() {
        return manager.getShows();
    }

    public List<Show> getShowsForMovie(String movieId) {
        return manager.getShowsForMovie(movieId);
    }

    public synchronized void addShow(Show show) throws ValidationException {
        if (show == null) throw new ValidationException("Show cannot be null.");
        if (show.getBasePrice() <= 0) throw new ValidationException("Base price must be greater than zero.");

        try {
            manager.getDbManager().insertShow(show);
            manager.getShows().add(show);
            manager.logAudit("Added show " + show.getId() + " for movie " + show.getMovie().getTitle());
        } catch (SQLException e) {
            throw new ValidationException("Database error while adding show: " + e.getMessage());
        }
    }

    public synchronized void removeShow(String showId) throws ValidationException {
        for (Booking booking : manager.getBookings()) {
            if (booking.getShow().getId().equals(showId)) {
                throw new ValidationException("Cannot remove show: Booking records exist for Show ID " + showId);
            }
        }

        try {
            manager.getDbManager().deleteShow(showId);
            boolean removed = manager.getShows().removeIf(s -> s.getId().equals(showId));
            if (!removed) throw new ValidationException("Show ID not found: " + showId);
            manager.logAudit("Safely removed show ID: " + showId);
        } catch (SQLException e) {
            throw new ValidationException("Database error while removing show: " + e.getMessage());
        }
    }
}
