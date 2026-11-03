import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Module 5: Application Entry Point, Verification Suite & Authentication Flow
public class CineBookApp {

    public static void runAutomatedCoreVerification() {
        System.out.println(">>> RUNNING CINEBOOK AUTOMATED SYSTEM VERIFICATION <<<");
        BookingManager mgr = BookingManager.getInstance();
        SeatManager seatMgr = new SeatManager();
        MovieService movieSvc = new MovieService(mgr);
        BookingService bookingSvc = new BookingService(mgr, seatMgr);
        PaymentProcessor payProc = new PaymentProcessor(mgr, bookingSvc);

        int passCount = 0;
        int testCount = 0;

        // Test 1: Sample movie catalogue loaded
        testCount++;
        if (mgr.getMovies().size() >= 6) {
            passCount++;
            System.out.println("[PASS] Test 1: Sample movie catalogue loaded (" + mgr.getMovies().size() + " movies from SQLite).");
        } else {
            System.err.println("[FAIL] Test 1: Movies not loaded properly from database.");
        }

        // Test 2: Shows maintain independent seat object instances
        testCount++;
        Show s1 = mgr.getShows().get(0);
        Show s2 = mgr.getShows().get(1);
        Seat seatA1_s1 = s1.findSeat("A3");
        Seat seatA1_s2 = s2.findSeat("A3");
        if (seatA1_s1 != null && seatA1_s2 != null && seatA1_s1 != seatA1_s2) {
            passCount++;
            System.out.println("[PASS] Test 2: Shows maintain independent seat object instances.");
        } else {
            System.err.println("[FAIL] Test 2: Shows share seat instances.");
        }

        // Test 3: Temporary reservation -> Payment -> Confirmed booking completed
        testCount++;
        try {
            Customer cust = mgr.getCustomers().get(0);

            // Select 2 available seats dynamically so test is idempotent across database runs
            List<String> targetSeats = new ArrayList<>();
            for (Seat[] row : s2.getSeats()) {
                for (Seat seat : row) {
                    if (seat.isAvailable()) {
                        targetSeats.add(seat.getId());
                        if (targetSeats.size() == 2) break;
                    }
                }
                if (targetSeats.size() == 2) break;
            }

            if (targetSeats.size() < 2) {
                // If s2 has fewer than 2 available seats, pick from s3
                Show fallbackShow = mgr.getShows().get(2);
                for (Seat[] row : fallbackShow.getSeats()) {
                    for (Seat seat : row) {
                        if (seat.isAvailable()) {
                            targetSeats.add(seat.getId());
                            if (targetSeats.size() == 2) break;
                        }
                    }
                    if (targetSeats.size() == 2) break;
                }
                s2 = fallbackShow;
            }

            Booking bkg = bookingSvc.createPendingReservation(cust, s2, targetSeats);
            PaymentMethod card = new MockCardPayment("1111222233334444", "Test User");
            PaymentTransaction txn = payProc.processSimulatedPayment(bkg, card, "SUCCESS");

            if (bkg.getBookingStatus() == BookingStatus.CONFIRMED &&
                    txn.getPaymentStatus() == PaymentStatus.SUCCESS &&
                    s2.findSeat(targetSeats.get(0)).getStatus() == SeatStatus.BOOKED) {
                passCount++;
                System.out.println("[PASS] Test 3: Temporary reservation -> Payment -> Confirmed booking completed (" + targetSeats + ").");
            } else {
                System.err.println("[FAIL] Test 3: Booking confirmation state incorrect.");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3 encountered exception: " + e.getMessage());
        }

        // Test 4: Double confirmation prevented
        testCount++;
        try {
            Booking bkg = mgr.getBookings().get(mgr.getBookings().size() - 1);
            bkg.confirm();
            System.err.println("[FAIL] Test 4: Double confirmation was not prevented!");
        } catch (InvalidBookingStateException expected) {
            passCount++;
            System.out.println("[PASS] Test 4: Double confirmation prevented: " + expected.getMessage());
        } catch (Exception other) {
            System.err.println("[FAIL] Test 4: Unexpected exception type: " + other.getMessage());
        }

        // Test 5: Safe deletion guard prevented deleting movie with bookings
        testCount++;
        try {
            movieSvc.removeMovie("MOV-01");
            System.err.println("[FAIL] Test 5: Deletion guard did not trigger!");
        } catch (ValidationException expected) {
            passCount++;
            System.out.println("[PASS] Test 5: Safe deletion guard prevented deleting movie with bookings: " + expected.getMessage());
        }

        // Test 6: Recursive calculation of booked seats
        testCount++;
        int recCount = mgr.countTotalBookedSeatsRecursive(mgr.getBookings(), 0);
        if (recCount > 0) {
            passCount++;
            System.out.println("[PASS] Test 6: Recursive booked seats calculation returned valid count: " + recCount);
        } else {
            System.err.println("[FAIL] Test 6: Recursive counter failed.");
        }

        System.out.println(String.format(">>> VERIFICATION SUMMARY: %d/%d TESTS PASSED <<<\n", passCount, testCount));
    }

    public static void main(String[] args) {
        runAutomatedCoreVerification();

        if (args.length > 0 && ("--test-only".equalsIgnoreCase(args[0]) || "-t".equalsIgnoreCase(args[0]))) {
            System.out.println("Headless verification flag supplied. Exiting cleanly without launching GUI.");
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {}

            // Ensure no residual pre-authenticated session
            BookingManager.getInstance().setCurrentUser(null);

            // Required Login Popup on application launch
            LoginDialog loginDialog = new LoginDialog(null);
            User authenticatedUser = loginDialog.showLoginDialog();

            if (authenticatedUser == null) {
                System.out.println("No active user session authenticated. Exiting CineBook.");
                System.exit(0);
            }

            MainFrame frame = new MainFrame();
            frame.onUserLoggedIn(authenticatedUser);
            frame.setVisible(true);
        });
    }
}
