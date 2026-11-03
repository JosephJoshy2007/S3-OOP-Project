// Module 6: Customer user model
public class Customer extends User {
    private int loyaltyPoints;

    public Customer(String id, String username, String password, String name, String email, int loyaltyPoints) {
        super(id, username, password, name, email, "Customer");
        this.loyaltyPoints = loyaltyPoints;
    }

    public Customer(String id, String name, String email) {
        super(id, name, email, "Customer");
        this.loyaltyPoints = 0;
    }

    public Customer(String id, String name, String email, int loyaltyPoints) {
        super(id, name, email, "Customer");
        this.loyaltyPoints = loyaltyPoints;
    }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }

    @Override
    public String getDashboardGreeting() {
        return "Welcome back, " + getName() + "! Enjoy your cinema experience.";
    }
}
