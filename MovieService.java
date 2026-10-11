import java.util.ArrayList;
import java.util.List;

// Module 1: MovieService (Movie Management)
public class MovieService implements Searchable<Movie> {
    private final BookingManager manager;

    public MovieService(BookingManager manager) {
        this.manager = manager;
    }

    @Override
    public List<Movie> search(String query) {
        List<Movie> results = new ArrayList<>();
        for (Movie movie : manager.getMovies()) {
            if (movie.matches(query)) results.add(movie);
        }
        return results;
    }

    public List<Movie> search(String title, String genre) {
        List<Movie> results = new ArrayList<>();
        for (Movie movie : manager.getMovies()) {
            if (movie.matches(title, genre)) results.add(movie);
        }
        return results;
    }

    public synchronized void addMovie(Movie movie) throws ValidationException {
        if (movie == null) throw new ValidationException("Movie cannot be null.");
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new ValidationException("Movie title cannot be empty.");
        }
        if (movie.getDurationMinutes() <= 0) {
            throw new ValidationException("Duration must be a positive integer.");
        }
        manager.getMovies().add(movie);
        manager.logAudit("Added movie: " + movie.getTitle() + " (" + movie.getId() + ")");
    }

    public synchronized void updateMovie(String id, String title, String genre,
                                  int duration, String language, String cert, String desc)
            throws ValidationException {
        Movie movie = null;
        for (Movie m : manager.getMovies()) {
            if (m.getId().equalsIgnoreCase(id)) {
                movie = m;
                break;
            }
        }
        if (movie == null) throw new ValidationException("Movie with ID " + id + " not found.");
        if (title == null || title.trim().isEmpty()) throw new ValidationException("Title cannot be empty.");
        if (duration <= 0) throw new ValidationException("Duration must be positive.");

        movie.setTitle(title.trim());
        movie.setGenre(genre.trim());
        movie.setDurationMinutes(duration);
        movie.setLanguage(language.trim());
        movie.setCertification(cert.trim());
        movie.setDescription(desc.trim());
        manager.logAudit("Updated movie: " + movie.getTitle() + " (" + id + ")");
    }

    public synchronized void removeMovie(String movieId) throws ValidationException {
        for (Show show : manager.getShowsForMovie(movieId)) {
            for (Booking booking : manager.getBookings()) {
                if (booking.getShow().getId().equals(show.getId())) {
                    throw new ValidationException("Cannot remove movie: Shows associated with this movie have active or past booking records.");
                }
            }
        }

        manager.getShows().removeIf(s -> s.getMovie().getId().equals(movieId));
        boolean removed = manager.getMovies().removeIf(m -> m.getId().equals(movieId));
        if (!removed) {
            throw new ValidationException("Movie ID not found: " + movieId);
        }
        manager.logAudit("Safely removed movie ID: " + movieId);
    }
}
