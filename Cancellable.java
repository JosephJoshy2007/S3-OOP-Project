// Module 3: Cancellable Interface
public interface Cancellable {
    boolean isCancellable();
    void cancel(String reason) throws InvalidBookingStateException;
}
