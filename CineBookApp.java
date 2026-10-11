import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Arrays;

// Module 5: Application Entry Point & Verification Suite
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

        testCount++;
        if (mgr.getMovies().size() >= 6) {
            passCount++;
            System.out.println("[PASS] Test 1: Sample movie catalogue loaded (" + mgr.getMovies().size() + " movies).");
        } else {
            System.err.println("[FAIL] Test 1: Movies not loaded properly.");
        }

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

        testCount++;
        try {
            Customer cust = mgr.getCustomers().get(0);
            Booking bkg = bookingSvc.createPendingReservation(cust, s2, Arrays.asList("B1", "B2"));
            PaymentMethod card = new MockCardPayment("1111222233334444", "Test User");
            PaymentTransaction txn = payProc.processSimulatedPayment(bkg, card, "SUCCESS");

            if (bkg.getBookingStatus() == BookingStatus.CONFIRMED &&
                    txn.getPaymentStatus() == PaymentStatus.SUCCESS &&
                    s2.findSeat("B1").getStatus() == SeatStatus.BOOKED) {
                passCount++;
                System.out.println("[PASS] Test 3: Temporary reservation -> Payment -> Confirmed booking completed.");
            } else {
                System.err.println("[FAIL] Test 3: Booking confirmation state incorrect.");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3 encountered exception: " + e.getMessage());
        }

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

        testCount++;
        try {
            movieSvc.removeMovie("MOV-01");
            System.err.println("[FAIL] Test 5: Deletion guard did not trigger!");
        } catch (ValidationException expected) {
            passCount++;
            System.out.println("[PASS] Test 5: Safe deletion guard prevented deleting movie with bookings: " + expected.getMessage());
        }

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

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
